package com.example.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PDF表格信息实体类
 * 对应数据库表: pdf_tables
 */
@Entity
@Table(name = "pdf_tables")
public class PdfTable {
    
    /**
     * 表格ID（自增主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 关联的PDF文档ID（外键）
     */
    @Column(name = "pdf_id", nullable = false, length = 36)
    private String pdfId;
    
    /**
     * 表格内容
     */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;
    
    /**
     * 图片路径
     */
    @Column(name = "image_path", length = 500)
    private String imagePath;
    
    /**
     * 表格标题
     */
    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;
    
    /**
     * 表格脚注
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
    public PdfTable() {
    }
    
    public PdfTable(String pdfId, String body, String imagePath, String caption, String footnote) {
        this.pdfId = pdfId;
        this.body = body;
        this.imagePath = imagePath;
        this.caption = caption;
        this.footnote = footnote;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPdfId() {
        return pdfId;
    }
    
    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
        return "PdfTable{" +
                "id=" + id +
                ", pdfId='" + pdfId + '\'' +
                ", body='" + body + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", caption='" + caption + '\'' +
                ", footnote='" + footnote + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}