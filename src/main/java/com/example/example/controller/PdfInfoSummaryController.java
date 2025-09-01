package com.example.example.controller;

import com.example.example.entity.PdfDocument;
import com.example.example.service.PdfSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdf-info")
@CrossOrigin(origins = "*") // 添加这个注解
public class PdfInfoSummaryController {

    private static final Logger logger = LoggerFactory.getLogger(PdfInfoSummaryController.class);

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
        logger.info("收到获取未总结PDF文档列表的请求");
        List<PdfDocument> unsummarizedDocuments = pdfSummaryService.findUnsummarizedPdfDocuments();
        logger.info("成功获取未总结PDF文档列表，共 {} 条记录", unsummarizedDocuments.size());
        return ResponseEntity.ok(unsummarizedDocuments);
    }

    /**
     * 为指定PDF生成摘要
     *
     * @param pdfId PDF文档ID
     * @return 操作结果
     */
    @PostMapping("/summarize/{pdfId}")
    public ResponseEntity<String> summarizePdf(@PathVariable String pdfId) {
        logger.info("收到为PDF文档生成摘要的请求，PDF ID: {}", pdfId);
        try {
            pdfSummaryService.summarizePdf(pdfId);
            logger.info("成功为PDF文档 {} 生成摘要", pdfId);
            return ResponseEntity.ok("PDF摘要生成成功");
        } catch (Exception e) {
            logger.error("为PDF文档 {} 生成摘要时发生错误: {}", pdfId, e.getMessage(), e);
            return ResponseEntity.status(500).body("PDF摘要生成失败: " + e.getMessage());
        }
    }

    /**
     * 为所有未总结的PDF生成摘要
     *
     * @return 操作结果
     */
    @PostMapping("/summarize-all")
    public ResponseEntity<String> summarizeAllPdf() {
        logger.info("收到为所有未总结PDF文档生成摘要的请求");
        try {
            List<PdfDocument> unsummarizedDocuments = pdfSummaryService.findUnsummarizedPdfDocuments();
            logger.info("找到 {} 个未总结的PDF文档", unsummarizedDocuments.size());

            for (PdfDocument document : unsummarizedDocuments) {
                try {
                    pdfSummaryService.summarizePdf(document.getPdfId());
                } catch (Exception e) {
                    logger.error("为PDF文档 {} 生成摘要时发生错误: {}", document.getPdfId(), e.getMessage(), e);
                    // 继续处理下一个文档，不中断整个流程
                }
            }

            logger.info("完成所有PDF文档摘要生成任务");
            return ResponseEntity.ok("所有PDF摘要生成任务已完成");
        } catch (Exception e) {
            logger.error("执行所有PDF摘要生成任务时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("PDF摘要生成任务失败: " + e.getMessage());
        }
    }
}