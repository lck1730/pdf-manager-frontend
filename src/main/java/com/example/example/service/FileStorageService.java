package com.example.example.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.original}")
    private String originalBucket;

    @Value("${minio.bucket.images}")
    private String imagesBucket;

    @Value("${minio.bucket.markdown}")
    private String markdownBucket;

    @Value("${minio.bucket.json}")
    private String jsonBucket;

    public FileStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 根据指定的PDF ID上传原始PDF文件
     * @param file PDF文件
     * @param pdfId PDF文件ID
     * @throws Exception 上传过程中可能发生的异常
     */
    public void uploadOriginalPdf(MultipartFile file, String pdfId) throws Exception {
        String fileName = pdfId + ".pdf";
        
        // 上传文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(originalBucket)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    /**
     * 删除原始PDF文件
     * @param pdfId PDF文件ID
     * @throws Exception 删除过程中可能发生的异常
     */
    public void deleteOriginalPdf(String pdfId) throws Exception {
        String fileName = pdfId + ".pdf";
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(originalBucket)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 上传PDF中提取的图片，保持原始文件名不变
     * @param imageFile 图片文件
     * @throws Exception 上传过程中可能发生的异常
     */
    public void uploadImage(MultipartFile imageFile) throws Exception {
        // 使用原始文件名上传图片
        String fileName = imageFile.getOriginalFilename();
        
        // 上传文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(imagesBucket)
                        .object(fileName)
                        .stream(imageFile.getInputStream(), imageFile.getSize(), -1)
                        .contentType(imageFile.getContentType())
                        .build()
        );
    }

    /**
     * 删除图片文件
     * @param imageFileName 图片文件名
     * @throws Exception 删除过程中可能发生的异常
     */
    public void deleteImage(String imageFileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(imagesBucket)
                        .object(imageFileName)
                        .build()
        );
    }

    /**
     * 上传PDF转换的Markdown文件
     *
     * @param content Markdown内容
     * @param pdfId   关联的PDF文件ID
     * @throws Exception 上传过程中可能发生的异常
     */
    public void uploadMarkdown(String content, String pdfId) throws Exception {
        String fileName = pdfId + ".md";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);
        
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(markdownBucket)
                        .object(fileName)
                        .stream(inputStream, contentBytes.length, -1)
                        .contentType("text/markdown")
                        .build()
        );

    }

    /**
     * 删除Markdown文件
     * @param pdfId PDF文件ID
     * @throws Exception 删除过程中可能发生的异常
     */
    public void deleteMarkdown(String pdfId) throws Exception {
        String fileName = pdfId + ".md";
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(markdownBucket)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 上传PDF的JSON数据
     *
     * @param content JSON内容
     * @param pdfId   关联的PDF文件ID
     * @throws Exception 上传过程中可能发生的异常
     */
    public void uploadJson(String content, String pdfId) throws Exception {
        String fileName = pdfId + ".json";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);
        
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(jsonBucket)
                        .object(fileName)
                        .stream(inputStream, contentBytes.length, -1)
                        .contentType("application/json")
                        .build()
        );

    }

    /**
     * 删除JSON文件
     * @param pdfId PDF文件ID
     * @throws Exception 删除过程中可能发生的异常
     */
    public void deleteJson(String pdfId) throws Exception {
        String fileName = pdfId + ".json";
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(jsonBucket)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 批量删除图片文件
     * @param imageFileNames 图片文件名列表
     * @throws Exception 删除过程中可能发生的异常
     */
    public void deleteImages(List<String> imageFileNames) throws Exception {
        for (String imageFileName : imageFileNames) {
            deleteImage(imageFileName);
        }
    }

    /**
     * 下载文件
     * @param bucketName 存储桶名称
     * @param fileName 文件名
     * @return 文件流
     * @throws Exception 下载过程中可能发生的异常
     */
    public GetObjectResponse downloadFile(String bucketName, String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 获取文件的预签名URL，用于直接访问
     * @param bucketName 存储桶名称
     * @param fileName 文件名
     * @param expiry 过期时间（秒）
     * @return 预签名URL
     * @throws Exception 获取URL过程中可能发生的异常
     */
    public String getPresignedUrl(String bucketName, String fileName, int expiry) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(fileName)
                        .expiry(expiry)
                        .build()
        );
    }
}