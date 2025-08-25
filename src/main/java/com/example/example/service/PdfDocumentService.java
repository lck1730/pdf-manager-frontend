package com.example.example.service;

import com.example.example.entity.PdfDocument;
import com.example.example.repository.PdfDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * PDF文档业务服务类
 */
@Service
public class PdfDocumentService {
    
    private final PdfDocumentRepository pdfDocumentRepository;
    
    @Autowired
    public PdfDocumentService(PdfDocumentRepository pdfDocumentRepository) {
        this.pdfDocumentRepository = pdfDocumentRepository;
    }
    
    /**
     * 创建PDF文档记录
     * 
     * @param pdfDocument PDF文档对象
     * @return 保存后的PDF文档对象
     */
    public PdfDocument createPdfDocument(PdfDocument pdfDocument) {
        return pdfDocumentRepository.save(pdfDocument);
    }
    
    /**
     * 根据用户名查找PDF文档的PDF ID和文件名
     * 
     * @param userName 用户名
     * @return PDF文档的PDF ID和文件名列表
     */
    public List<Object> findPdfIdAndFileNameByUserName(String userName) {
        return pdfDocumentRepository.findPdfIdAndFileNameByUserName(userName);
    }
    
    /**
     * 根据PDF ID查找info字段和note字段
     * 
     * @param pdfId PDF文档ID
     * @return PDF文档的info和note字段
     */
    public Object findInfoAndNoteByPdfId(String pdfId) {
        return pdfDocumentRepository.findInfoAndNoteByPdfId(pdfId);
    }
    
    /**
     * 根据PDF ID修改note字段
     * 
     * @param pdfId PDF文档ID
     * @param note 新的note内容
     * @return 更新后的PDF文档对象
     */
    public PdfDocument updateNoteByPdfId(String pdfId, String note) {
        PdfDocument pdfDocument = pdfDocumentRepository.findByPdfId(pdfId);
        if (pdfDocument != null) {
            pdfDocument.setNote(note);
            return pdfDocumentRepository.save(pdfDocument);
        }
        return null;
    }
    
    /**
     * 根据PDF ID删除PDF文档记录
     * 
     * @param pdfId PDF文档ID
     */
    public void deleteByPdfId(String pdfId) {
        pdfDocumentRepository.deleteById(pdfId);
    }
}