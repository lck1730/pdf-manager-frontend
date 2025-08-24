package com.example.example.controller;

import com.example.example.entity.PdfDocument;
import com.example.example.service.PdfInfoExtractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdf-info")
@CrossOrigin(origins = "*")
public class PdfInfoExtractController {

    private final PdfInfoExtractService pdfInfoExtractService;

    public PdfInfoExtractController(PdfInfoExtractService pdfInfoExtractService) {
        this.pdfInfoExtractService = pdfInfoExtractService;
    }

    /**
     * 获取所有未提取信息的PDF文档
     *
     * @return 未提取信息的PDF文档列表
     */
    @GetMapping("/unextracted")
    public ResponseEntity<List<PdfDocument>> getUnextractedPdfDocuments() {
        List<PdfDocument> unextractedDocuments = pdfInfoExtractService.findUnextractedPdfDocuments();
        return ResponseEntity.ok(unextractedDocuments);
    }

    /**
     * 提取指定PDF文档的信息
     *
     * @param pdfId PDF文档ID
     * @return 操作结果
     */
    @PostMapping("/extract/{pdfId}")
    public ResponseEntity<String> extractPdfInfo(@PathVariable String pdfId) {
        try {
            pdfInfoExtractService.extractPdfInfo(pdfId);
            return ResponseEntity.ok("PDF信息提取成功: " + pdfId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("PDF信息提取失败: " + pdfId + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 提取所有未处理PDF文档的信息
     *
     * @return 操作结果
     */
    @PostMapping("/extract-all")
    public ResponseEntity<String> extractAllPdfInfo() {
        try {
            List<PdfDocument> unextractedDocuments = pdfInfoExtractService.findUnextractedPdfDocuments();
            int successCount = 0;
            int failureCount = 0;

            for (PdfDocument document : unextractedDocuments) {
                try {
                    pdfInfoExtractService.extractPdfInfo(document.getPdfId());
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    // 记录错误但继续处理其他文档
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(String.format("处理完成。成功: %d, 失败: %d", successCount, failureCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("批量处理失败: " + e.getMessage());
        }
    }
}