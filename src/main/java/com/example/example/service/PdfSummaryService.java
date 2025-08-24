package com.example.example.service;

import com.example.example.entity.PdfDocument;
import com.example.example.repository.PdfDocumentRepository;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfSummaryService {

    private final PdfDocumentRepository pdfDocumentRepository;
    private final MinioClient minioClient;
    private final ChatModel chatModel;

    @Value("${minio.bucket.markdown}")
    private String markdownBucket;

    public PdfSummaryService(PdfDocumentRepository pdfDocumentRepository,
                             MinioClient minioClient,
                             ChatModel chatModel) {
        this.pdfDocumentRepository = pdfDocumentRepository;
        this.minioClient = minioClient;
        this.chatModel = chatModel;
    }

    /**
     * 查找所有未总结的PDF文档
     *
     * @return 未总结的PDF文档列表
     */
    public List<PdfDocument> findUnsummarizedPdfDocuments() {
        return pdfDocumentRepository.findByIsSummarizedFalse();
    }

    /**
     * 为指定PDF生成摘要
     *
     * @param pdfId PDF文档ID
     * @throws Exception 处理过程中可能发生的异常
     */
    @Transactional
    public void summarizePdf(String pdfId) throws Exception {
        // 从MinIO获取Markdown文件
        try (GetObjectResponse response = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(markdownBucket)
                        .object(pdfId + ".md")
                        .build())) {

            // 读取Markdown内容
            String markdownContent = readMarkdownContent(response);
            
            // 如果内容太长，截取前部分以避免超出模型限制
            if (markdownContent.length() > 4000) {
                markdownContent = markdownContent.substring(0, 4000) + "...(内容已截取)";
            }
            
            // 生成摘要提示
            String promptText = createSummaryPrompt(markdownContent);
            
            // 调用AI模型生成摘要
            UserMessage userMessage = new UserMessage(promptText);
            Prompt prompt = new Prompt(userMessage);
            ChatResponse chatResponse = chatModel.call(prompt);
            
            // 获取AI生成的摘要
            String summary = chatResponse.getResult().getOutput().getContent();
            
            // 更新PDF文档的摘要信息
            PdfDocument pdfDocument = pdfDocumentRepository.findById(pdfId)
                    .orElseThrow(() -> new RuntimeException("PDF文档不存在: " + pdfId));
            pdfDocument.setInfo(summary);
            pdfDocument.setIsSummarized(true);
            pdfDocumentRepository.save(pdfDocument);
        }
    }

    /**
     * 读取Markdown内容
     *
     * @param response MinIO响应
     * @return Markdown内容字符串
     * @throws Exception 读取过程中可能发生的异常
     */
    private String readMarkdownContent(GetObjectResponse response) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * 创建摘要生成提示
     *
     * @param markdownContent Markdown内容
     * @return 提示文本
     */
    private String createSummaryPrompt(String markdownContent) {
        return "请为以下Markdown文档内容生成摘要和总结:\n\n" +
                markdownContent + "\n\n" +
                "请提供以下信息:\n" +
                "1. 文档的主要内容概述\n" +
                "2. 关键观点或结论\n" +
                "3. 重要数据或发现（如果适用）\n" +
                "4. 文档的用途或目标受众\n\n" +
                "请用中文回复，保持简洁明了。";
    }
}