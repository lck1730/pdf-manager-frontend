package com.example.example.repository;

import com.example.example.entity.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 处理日志数据访问接口
 */
@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, String> {
    
    /**
     * 根据PDF文档ID查找所有关联的日志
     * 
     * @param pdfId PDF文档ID
     * @return 日志列表
     */
    List<ProcessingLog> findByPdfId(String pdfId);
    
    /**
     * 根据日志级别查找日志
     * 
     * @param logLevel 日志级别
     * @return 日志列表
     */
    List<ProcessingLog> findByLogLevel(String logLevel);
    
    /**
     * 根据PDF文档ID和日志级别查找日志
     * 
     * @param pdfId PDF文档ID
     * @param logLevel 日志级别
     * @return 日志列表
     */
    List<ProcessingLog> findByPdfIdAndLogLevel(String pdfId, String logLevel);
    
    /**
     * 根据创建时间范围查找日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<ProcessingLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据PDF文档ID查找最新的N条日志
     * 
     * @param pdfId PDF文档ID
     * @param limit 限制数量
     * @return 日志列表
     */
    @Query("SELECT p FROM ProcessingLog p WHERE p.pdfId = :pdfId ORDER BY p.createdAt DESC LIMIT :limit")
    List<ProcessingLog> findLatestByPdfId(@Param("pdfId") String pdfId, @Param("limit") int limit);
}