package com.example.example.controller;

import com.example.example.service.*;
import com.example.example.repository.*;
import com.example.example.entity.PdfImage;
import com.example.example.entity.PdfTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdf-delete")
@CrossOrigin(origins = "*")
public class PdfDeleteController {

    @Value("${minio.bucket.original}")
    private String originalBucket;

    @Value("${minio.bucket.images}")
    private String imagesBucket;

    private final TagService tagService;
    private final PdfImageService pdfImageService;
    private final PdfDocumentService pdfDocumentService;
    private final PdfTableService pdfTableService;
    private final PdfImageRepository pdfImageRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PdfDeleteController(TagService tagService,
                               PdfImageService pdfImageService,
                               PdfDocumentService pdfDocumentService,
                               PdfTableService pdfTableService,
                               PdfImageRepository pdfImageRepository,
                               PdfTableRepository pdfTableRepository,
                               FileStorageService fileStorageService) {
        this.tagService = tagService;
        this.pdfImageService = pdfImageService;
        this.pdfDocumentService = pdfDocumentService;
        this.pdfTableService = pdfTableService;
        this.pdfImageRepository = pdfImageRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 根据pdfID和tag删除一条tag表记录
     *
     * @param pdfId PDF文档ID
     * @param tag 标签
     * @return 操作结果
     */
    @DeleteMapping("/tag")
    public ResponseEntity<String> deleteTagByPdfIdAndTag(@RequestParam String pdfId, @RequestParam String tag) {
        try {
            tagService.deleteByPdfIdAndTag(pdfId, tag);
            return ResponseEntity.ok("标签删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("标签删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据image name删除images表的一条记录，同时删MinIO中对应的文件，这两个动作需要保证一致性
     *
     * @param imageName 图片名称
     * @return 操作结果
     */
    @DeleteMapping("/image/{imageName}")
    @Transactional
    public ResponseEntity<String> deleteImageAndFile(@PathVariable String imageName) {
        try {
            // 先删除MinIO中的文件
            fileStorageService.deleteImage(imageName);
            
            // 再删除数据库中的记录
            pdfImageService.deleteByImageName(imageName);
            
            return ResponseEntity.ok("图片及其文件删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("图片及其文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据pdfID删除pdf的操作:
     * 1. 根据pdfID删除pdf表的记录
     * 2. 删除MinIO里的文件
     * 3. 删除与之关联的table表中的记录
     * 4. 找到images表中的与之关联的记录，找到后先删掉MinIO里image文件，再删除image中的表记录
     * 这些操作也必须保证一致性，即事务
     *
     * @param pdfId PDF文档ID
     * @return 操作结果
     */
    @DeleteMapping("/pdf/{pdfId}")
    @Transactional
    public ResponseEntity<String> deletePdfAndRelatedData(@PathVariable String pdfId) {
        try {
            // 1. 删除与PDF关联的表格记录
            pdfTableService.deleteByPdfId(pdfId);
            
            // 2. 找到与PDF关联的图片记录
            List<PdfImage> images = pdfImageRepository.findByPdfId(pdfId);
            
            // 3. 删除MinIO中的图片文件
            for (PdfImage image : images) {
                try {
                    fileStorageService.deleteImage(image.getImageName());
                } catch (Exception e) {
                    // 记录删除失败但继续执行
                    e.printStackTrace();
                }
            }
            
            // 4. 删除数据库中的图片记录
            pdfImageRepository.deleteAll(images);
            
            // 5. 删除MinIO中的PDF文件
            fileStorageService.deleteOriginalPdf(pdfId);
            
            // 6. 删除PDF文档记录
            pdfDocumentService.deleteByPdfId(pdfId);


            // TODO:定期检查数据一致性并维护
            return ResponseEntity.ok("PDF文档及其相关数据删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("PDF文档及其相关数据删除失败: " + e.getMessage());
        }
    }
}