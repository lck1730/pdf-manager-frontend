package com.example.example.controller;

import com.example.example.entity.PdfDocument;
import com.example.example.entity.PdfImage;
import com.example.example.entity.PdfTable;
import com.example.example.service.PdfDocumentService;
import com.example.example.service.PdfImageService;
import com.example.example.service.PdfTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdf-edit")
@CrossOrigin(origins = "*")
public class PdfEditController {

    private final PdfDocumentService pdfDocumentService;
    private final PdfImageService pdfImageService;
    private final PdfTableService pdfTableService;

    @Autowired
    public PdfEditController(PdfDocumentService pdfDocumentService,
                             PdfImageService pdfImageService,
                             PdfTableService pdfTableService) {
        this.pdfDocumentService = pdfDocumentService;
        this.pdfImageService = pdfImageService;
        this.pdfTableService = pdfTableService;
    }

    /**
     * 根据pdfID编辑pdf表中的note字段
     *
     * @param pdfId PDF文档ID
     * @param noteRequest 包含新note内容的请求体
     * @return 更新后的PDF文档对象
     */
    @PutMapping("/{pdfId}/note")
    public ResponseEntity<PdfDocument> updatePdfNote(@PathVariable String pdfId,
                                                     @RequestBody Map<String, String> noteRequest) {
        String note = noteRequest.get("note");
        PdfDocument updatedDocument = pdfDocumentService.updateNoteByPdfId(pdfId, note);
        if (updatedDocument != null) {
            return ResponseEntity.ok(updatedDocument);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据imageName编辑images表中的footnote字段
     *
     * @param imageName 图片名称
     * @param footnoteRequest 包含新footnote内容的请求体
     * @return 更新后的PDF图片对象
     */
    @PutMapping("/image/{imageName}/footnote")
    public ResponseEntity<PdfImage> updateImageFootnote(@PathVariable String imageName,
                                                        @RequestBody Map<String, String> footnoteRequest) {
        String footnote = footnoteRequest.get("footnote");
        // 为了更新footnote，我们传入空字符串作为caption，表示不更新caption
        Optional<PdfImage> updatedImage = pdfImageService.updateCaptionAndFootnoteByImageName(
                imageName, "", footnote);
        return updatedImage.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据table id编辑table表中的footnote字段
     *
     * @param tableId 表格ID
     * @param footnoteRequest 包含新footnote内容的请求体
     * @return 更新后的PDF表格对象
     */
    @PutMapping("/table/{tableId}/footnote")
    public ResponseEntity<PdfTable> updateTableFootnote(@PathVariable Long tableId,
                                                        @RequestBody Map<String, String> footnoteRequest) {
        String footnote = footnoteRequest.get("footnote");
        // 为了更新footnote，我们传入空字符串作为caption，表示不更新caption
        PdfTable updatedTable = pdfTableService.updateCaptionAndFootnoteByTableId(
                tableId, "", footnote);
        if (updatedTable != null) {
            return ResponseEntity.ok(updatedTable);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}