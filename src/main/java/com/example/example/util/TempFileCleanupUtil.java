package com.example.example.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TempFileCleanupUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(TempFileCleanupUtil.class);
    
    @Value("${spring.servlet.multipart.location:${user.dir}/temp/uploads}")
    private String tempLocation;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * 启动定时清理任务，定期清理上传的临时文件
     */
    public void startCleanupTask() {
        // 每天清理一次超过24小时的临时文件
        scheduler.scheduleAtFixedRate(this::cleanupOldTempFiles, 1, 24, TimeUnit.HOURS);
    }
    
    /**
     * 清理超过24小时的临时文件
     */
    private void cleanupOldTempFiles() {
        try {
            // 替换${user.dir}占位符
            String resolvedPath = tempLocation.replace("${user.dir}", System.getProperty("user.dir"));
            Path tempDir = Paths.get(resolvedPath).toAbsolutePath();
            
            File tempFolder = tempDir.toFile();
            if (tempFolder.exists() && tempFolder.isDirectory()) {
                long currentTime = System.currentTimeMillis();
                long oneDayInMillis = 24 * 60 * 60 * 1000; // 24小时
                
                File[] files = tempFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        // 删除超过24小时的文件
                        if (currentTime - file.lastModified() > oneDayInMillis) {
                            deleteFileRecursively(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("清理临时文件时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 递归删除文件或文件夹
     * @param file 要删除的文件或文件夹
     */
    private void deleteFileRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFileRecursively(child);
                }
            }
        }
        file.delete();
    }
    
    /**
     * 立即执行一次清理任务
     */
    public void cleanupNow() {
        cleanupOldTempFiles();
    }
    
    /**
     * 关闭清理任务
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}