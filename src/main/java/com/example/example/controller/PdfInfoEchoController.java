package com.example.example.controller;

import com.example.example.service.*;
import io.minio.GetObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdf-echo")
@CrossOrigin(origins = "*")
public class PdfInfoEchoController {

    @Value("${minio.bucket.original}")
    private String originalBucket;

    @Value("${minio.bucket.images}")
    private String imagesBucket;

    private final PdfDocumentService pdfDocumentService;
    private final PdfTableService pdfTableService;
    private final PdfImageService pdfImageService;
    private final FileStorageService fileStorageService;

    @Autowired
    public PdfInfoEchoController(PdfDocumentService pdfDocumentService,
                                 PdfTableService pdfTableService,
                                 PdfImageService pdfImageService,
                                 FileStorageService fileStorageService) {
        this.pdfDocumentService = pdfDocumentService;
        this.pdfTableService = pdfTableService;
        this.pdfImageService = pdfImageService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 根据当前用户返回所有的pdfID和filename
     *
     * @param userName 用户名
     * @return PDF文档的PDF ID和文件名列表
     */
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<Object>> getPdfIdAndFileNameByUserName(@PathVariable String userName) {
        List<Object> result = pdfDocumentService.findPdfIdAndFileNameByUserName(userName);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据pdfID返回pdf表的info 和 note
     *
     * @param pdfId PDF文档ID
     * @return PDF文档的info和note字段
     */
    @GetMapping("/{pdfId}/info-note")
    public ResponseEntity<Object> getInfoAndNoteByPdfId(@PathVariable String pdfId) {
        Object result = pdfDocumentService.findInfoAndNoteByPdfId(pdfId);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据pdfID回显tables表的table_id, body, caption, footnote以及image_path
     *
     * @param pdfId PDF文档ID
     * @return 包含表格ID和相关信息的表格信息列表
     */
    @GetMapping("/{pdfId}/tables")
    public ResponseEntity<List<Object>> getTablesByPdfId(@PathVariable String pdfId) {
        List<Object> result = pdfTableService.findTableIdBodyCaptionAndFootnoteByPdfId(pdfId);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据pdfID回显images表的image_name，caption，footnote
     *
     * @param pdfId PDF文档ID
     * @return 图片信息列表
     */
    @GetMapping("/{pdfId}/images")
    public ResponseEntity<List<Object>> getImagesByPdfId(@PathVariable String pdfId) {
        List<Object> result = pdfImageService.findImageNameCaptionAndFootnoteByPdfId(pdfId);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据pdfID去MinIO里找原始pdf文件并返回
     *
     * @param pdfId PDF文档ID
     * @return 原始PDF文件
     */
    @GetMapping("/{pdfId}/pdf-file")
    public ResponseEntity<byte[]> getOriginalPdfFile(@PathVariable String pdfId) {
        try {
            String fileName = pdfId + ".pdf";
            GetObjectResponse response = fileStorageService.downloadFile(originalBucket, fileName);
            
            // 读取文件内容
            InputStream inputStream = response;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileData = buffer.toByteArray();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据image_name去MinIO找原始图片文件并返回
     *
     * @param imageName 图片名称
     * @return 原始图片文件
     */
    @GetMapping("/image-file/{imageName}")
    public ResponseEntity<byte[]> getOriginalImageFile(@PathVariable String imageName) {
        try {
            GetObjectResponse response = fileStorageService.downloadFile(imagesBucket, imageName);
            
            // 读取文件内容
            InputStream inputStream = response;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileData = buffer.toByteArray();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + imageName);
            // 根据文件扩展名设置内容类型
            String contentType = "image/jpeg";
            if (imageName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imageName.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}