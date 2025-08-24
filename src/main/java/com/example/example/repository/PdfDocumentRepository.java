package com.example.example.repository;

import com.example.example.entity.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PDF文档数据访问接口
 */
@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, String> {
    /**
     * 根据用户名查找PDF文档
     * 
     * @param userName 用户名
     * @return PDF文档列表
     */
    List<PdfDocument> findByUserName(String userName);
    
    /**
     * 根据用户名查找PDF文档的PDF ID和文件名
     * 
     * @param userName 用户名
     * @return PDF文档的PDF ID和文件名列表
     */
    @Query("SELECT p.pdfId, p.fileName FROM PdfDocument p WHERE p.userName = ?1")
    List<Object> findPdfIdAndFileNameByUserName(String userName);
    
    /**
     * 根据PDF ID查找info字段和note字段
     * 
     * @param pdfId PDF文档ID
     * @return PDF文档的info和note字段
     */
    @Query("SELECT p.info, p.note FROM PdfDocument p WHERE p.pdfId = ?1")
    Object findInfoAndNoteByPdfId(String pdfId);
    
    /**
     * 根据PDF ID查找PDF文档
     * 
     * @param pdfId PDF文档ID
     * @return PDF文档
     */
    @Query("SELECT p FROM PdfDocument p WHERE p.pdfId = ?1")
    PdfDocument findByPdfId(String pdfId);
    
    /**
     * 查找所有未提取信息的PDF文档
     * 
     * @return 未提取信息的PDF文档列表
     */
    @Query("SELECT p FROM PdfDocument p WHERE p.isExtracted = false OR p.isExtracted IS NULL")
    List<PdfDocument> findByIsExtractedFalse();
    
    /**
     * 查找所有未总结的PDF文档
     * 
     * @return 未总结的PDF文档列表
     */
    @Query("SELECT p FROM PdfDocument p WHERE p.isSummarized = false OR p.isSummarized IS NULL")
    List<PdfDocument> findByIsSummarizedFalse();
}