package com.example.example.repository;

import com.example.example.entity.PdfTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PDF表格数据访问接口
 */
@Repository
public interface PdfTableRepository extends JpaRepository<PdfTable, Long> {
    
    /**
     * 根据PDF ID查找所有表格记录
     * 
     * @param pdfId PDF文档ID
     * @return 表格记录列表
     */
    List<PdfTable> findByPdfId(String pdfId);
    
    /**
     * 根据PDF ID查找所有关联的表格的id、body、caption和footnote
     * 
     * @param pdfId PDF文档ID
     * @return 包含id、body、caption和footnote的表格信息列表
     */
    @Query("SELECT t.id, t.body, t.caption, t.footnote FROM PdfTable t WHERE t.pdfId = ?1")
    List<Object> findTableIdBodyCaptionAndFootnoteByPdfId(String pdfId);
    
    /**
     * 根据表格ID查找表格记录
     * 
     * @param tableId 表格ID
     * @return 表格记录
     */
    @Query("SELECT t FROM PdfTable t WHERE t.id = ?1")
    PdfTable findByTableId(Long tableId);
}