package com.example.example.controller;

import com.example.example.entity.PdfDocument;
import com.example.example.service.PdfSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdf-info")
@CrossOrigin(origins = "*")
public class PdfInfoSummaryController {

    private final PdfSummaryService pdfSummaryService;

    public PdfInfoSummaryController(PdfSummaryService pdfSummaryService) {
        this.pdfSummaryService = pdfSummaryService;
    }

    /**
     * 获取所有未总结的PDF文档
     *
     * @return 未总结的PDF文档列表
     */
    @GetMapping("/unsummarized")
    public ResponseEntity<List<PdfDocument>> getUnsummarizedPdfDocuments() {
        List<PdfDocument> unsummarizedDocuments = pdfSummaryService.findUnsummarizedPdfDocuments();
        return ResponseEntity.ok(unsummarizedDocuments);
    }

    /**
     * 为指定PDF文档生成摘要
     *
     * @param pdfId PDF文档ID
     * @return 操作结果
     */
    @PostMapping("/summarize/{pdfId}")
    public ResponseEntity<String> summarizePdf(@PathVariable String pdfId) {
        try {
            pdfSummaryService.summarizePdf(pdfId);
            return ResponseEntity.ok("PDF摘要生成成功: " + pdfId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("PDF摘要生成失败: " + pdfId + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 为所有未处理PDF文档生成摘要
     *
     * @return 操作结果
     */
    @PostMapping("/summarize-all")
    public ResponseEntity<String> summarizeAllPdf() {
        try {
            List<PdfDocument> unsummarizedDocuments = pdfSummaryService.findUnsummarizedPdfDocuments();
            int successCount = 0;
            int failureCount = 0;

            for (PdfDocument document : unsummarizedDocuments) {
                try {
                    pdfSummaryService.summarizePdf(document.getPdfId());
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