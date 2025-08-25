package com.example.example.service;

import com.example.example.entity.PdfTable;
import com.example.example.repository.PdfTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * PDF表格业务服务类
 */
@Service
public class PdfTableService {
    
    private final PdfTableRepository pdfTableRepository;
    
    @Autowired
    public PdfTableService(PdfTableRepository pdfTableRepository) {
        this.pdfTableRepository = pdfTableRepository;
    }
    
    /**
     * 根据PDF文档ID查找所有关联的表格的table_id、body、caption和footnote
     * 
     * @param pdfId PDF文档ID
     * @return 包含table_id、body、caption和footnote的表格信息列表
     */
    public List<Object> findTableIdBodyCaptionAndFootnoteByPdfId(String pdfId) {
        return pdfTableRepository.findTableIdBodyCaptionAndFootnoteByPdfId(pdfId);
    }
    
    /**
     * 根据表格ID更新表格的caption和footnote字段
     * 
     * @param tableId 表格ID
     * @param caption 新的caption内容
     * @param footnote 新的footnote内容
     * @return 更新后的PDF表格对象
     */
    public PdfTable updateCaptionAndFootnoteByTableId(Long tableId, String caption, String footnote) {
        PdfTable pdfTable = pdfTableRepository.findByTableId(tableId);
        if (pdfTable != null) {
            // 只有当caption非空时才更新caption字段
            if (caption != null && !caption.isEmpty()) {
                pdfTable.setCaption(caption);
            }
            // 只有当footnote非空时才更新footnote字段
            if (footnote != null) {
                pdfTable.setFootnote(footnote);
            }
            return pdfTableRepository.save(pdfTable);
        }
        return null;
    }
    
    /**
     * 根据PDF ID删除表格记录
     * 
     * @param pdfId PDF文档ID
     */
    public void deleteByPdfId(String pdfId) {
        List<PdfTable> tables = pdfTableRepository.findByPdfId(pdfId);
        pdfTableRepository.deleteAll(tables);
    }
}