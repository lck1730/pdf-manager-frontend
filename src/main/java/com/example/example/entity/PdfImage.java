package com.example.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PDF图片信息实体类
 * 对应数据库表: pdf_images
 */
@Entity
@Table(name = "pdf_images")
public class PdfImage {
    
    /**
     * 图片名称（唯一主键）
     */
    @Id
    @Column(name = "image_name", length = 255)
    private String imageName;
    
    /**
     * 关联的PDF文档ID（外键）
     */
    @Column(name = "pdf_id", nullable = false, length = 36)
    private String pdfId;
    
    /**
     * 图片标题，默认为空
     */
    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;
    
    /**
     * 图片脚注，默认为空
     */
    @Column(name = "footnote", columnDefinition = "TEXT")
    private String footnote;
    
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
    
    // 构造函数
    public PdfImage() {
    }
    
    public PdfImage(String imageName, String pdfId) {
        this.imageName = imageName;
        this.pdfId = pdfId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public String getPdfId() {
        return pdfId;
    }
    
    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getFootnote() {
        return footnote;
    }
    
    public void setFootnote(String footnote) {
        this.footnote = footnote;
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
        return "PdfImage{" +
                "imageName='" + imageName + '\'' +
                ", pdfId='" + pdfId + '\'' +
                ", caption='" + caption + '\'' +
                ", footnote='" + footnote + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}