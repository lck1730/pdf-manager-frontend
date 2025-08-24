package com.example.example;

import com.example.example.util.TempFileCleanupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class ExampleApplication {
    
    @Autowired
    private TempFileCleanupUtil tempFileCleanupUtil;

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
    
    @PostConstruct
    public void init() {
        // 启动定时清理任务
        tempFileCleanupUtil.startCleanupTask();
    }
    
    @PreDestroy
    public void destroy() {
        // 关闭定时清理任务
        tempFileCleanupUtil.shutdown();
    }
}