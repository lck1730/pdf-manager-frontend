package com.example.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 处理日志实体类
 * 对应数据库表: processing_logs
 */
@Entity
@Table(name = "processing_logs")
public class ProcessingLog {
    
    /**
     * 日志唯一标识符(UUID)
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    /**
     * 关联的PDF文档ID
     */
    @Column(name = "pdf_id", nullable = false, length = 36)
    private String pdfId;
    
    /**
     * 日志级别(INFO, WARN, ERROR)
     */
    @Column(name = "log_level", nullable = false, length = 20)
    private String logLevel;
    
    /**
     * 日志消息
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // 构造函数
    public ProcessingLog() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ProcessingLog(String id, String pdfId, String logLevel, String message) {
        this();
        this.id = id;
        this.pdfId = pdfId;
        this.logLevel = logLevel;
        this.message = message;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPdfId() {
        return pdfId;
    }
    
    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "ProcessingLog{" +
                "id='" + id + '\'' +
                ", pdfId='" + pdfId + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}