package com.example.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class TempDirConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TempDirConfig.class);
    
    @Value("${spring.servlet.multipart.location:${user.dir}/temp/uploads}")
    private String tempLocation;
    
    @PostConstruct
    public void createTempDir() {
        try {
            // 替换${user.dir}占位符
            String resolvedPath = tempLocation.replace("${user.dir}", System.getProperty("user.dir"));
            File tempDir = Paths.get(resolvedPath).toAbsolutePath().toFile();
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
        } catch (Exception e) {
            logger.error("创建临时目录失败: {}", e.getMessage(), e);
        }
    }
}