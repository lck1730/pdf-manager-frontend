package com.example.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 标签信息实体类
 * 对应数据库表: tags
 */
@Entity
@Table(name = "tags")
public class Tag {
    
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的PDF文档ID（外键）
     */
    @Column(name = "pdf_id", nullable = false, length = 36)
    private String pdfId;
    
    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    /**
     * 标签
     */
    @Column(name = "tag", nullable = false, length = 100)
    private String tag;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 构造函数
    public Tag() {
    }
    
    public Tag(String pdfId, String fileName, String tag) {
        this.pdfId = pdfId;
        this.fileName = fileName;
        this.tag = tag;
        this.createdAt = LocalDateTime.now();
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
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", pdfId='" + pdfId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", tag='" + tag + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}