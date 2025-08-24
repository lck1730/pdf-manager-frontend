package com.example.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PDF文档信息实体类
 * 对应数据库表: pdf_documents
 */
@Entity
@Table(name = "pdf_documents")
public class PdfDocument {
    
    /**
     * PDF文档唯一标识符(UUID)
     */
    @Id
    @Column(name = "pdf_id", length = 36)
    private String pdfId;
    
    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    /**
     * 所属用户
     */
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;
    
    /**
     * 信息字段，默认为空
     */
    @Column(name = "info", columnDefinition = "TEXT")
    private String info;
    
    /**
     * 笔记字段，默认为空
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 是否已提取信息，默认为false
     */
    @Column(name = "is_extracted", nullable = false)
    private Boolean isExtracted = false;
    
    /**
     * 是否已总结，默认为false
     */
    @Column(name = "is_summarized", nullable = false)
    private Boolean isSummarized = false;
    
    // 构造函数
    public PdfDocument() {
    }
    
    public PdfDocument(String pdfId, String fileName, String userName) {
        this.pdfId = pdfId;
        this.fileName = fileName;
        this.userName = userName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isExtracted = false;
        this.isSummarized = false;
    }
    
    // Getter和Setter方法
    public String getPdfId() {
        return pdfId;
    }
    
    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getInfo() {
        return info;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getIsExtracted() {
        return isExtracted;
    }
    
    public void setIsExtracted(Boolean isExtracted) {
        this.isExtracted = isExtracted;
    }
    
    public Boolean getIsSummarized() {
        return isSummarized;
    }
    
    public void setIsSummarized(Boolean isSummarized) {
        this.isSummarized = isSummarized;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "PdfDocument{" +
                "pdfId='" + pdfId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", userName='" + userName + '\'' +
                ", info='" + info + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isExtracted=" + isExtracted +
                ", isSummarized=" + isSummarized +
                '}';
    }
}