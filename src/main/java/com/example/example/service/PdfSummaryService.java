package com.example.example.service;

import com.example.example.entity.PdfDocument;
import com.example.example.repository.PdfDocumentRepository;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

// 添加阿里云DashScope SDK相关导入
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

@Service
public class PdfSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(PdfSummaryService.class);

    private final PdfDocumentRepository pdfDocumentRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket.markdown}")
    private String markdownBucket;

    @Value("${app.ai.summary.max-content-length:8000}")
    private int maxContentLength;

    // 修改构造函数，移除ChatModel依赖
    public PdfSummaryService(PdfDocumentRepository pdfDocumentRepository,
                             MinioClient minioClient) {
        this.pdfDocumentRepository = pdfDocumentRepository;
        this.minioClient = minioClient;
    }

    /**
     * 查找所有未总结的PDF文档
     *
     * @return 未总结的PDF文档列表
     */
    public List<PdfDocument> findUnsummarizedPdfDocuments() {
        logger.info("查找所有未总结的PDF文档");
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
        logger.info("开始为PDF文档生成摘要: {}", pdfId);
        
        // 从MinIO获取Markdown文件
        try (GetObjectResponse response = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(markdownBucket)
                        .object(pdfId + ".md")
                        .build())) {

            // 读取Markdown内容
            String markdownContent = readMarkdownContent(response);
            logger.debug("成功读取PDF {} 的Markdown内容，长度: {}", pdfId, markdownContent.length());
            
            // 如果内容太长，截取前部分以避免超出模型限制
            if (markdownContent.length() > maxContentLength) {
                markdownContent = markdownContent.substring(0, maxContentLength) + "...(内容已截取)";
                logger.warn("PDF {} 的内容超过最大长度限制 {}，已截取", pdfId, maxContentLength);
            }
            
            // 生成摘要提示
            String promptText = createSummaryPrompt(markdownContent);
            logger.debug("为PDF {} 构造提示词完成", pdfId);
            
            // 调用AI模型生成摘要
            logger.info("准备调用AI模型生成摘要，PDF ID: {}", pdfId);
            String summary;
            try {
                // 调用阿里云DashScope生成摘要
                summary = callDashScope(promptText);
                logger.info("成功调用AI模型，获得响应");
            } catch (Exception e) {
                logger.error("调用AI模型时发生错误:");
                logger.error("  错误类型: {}", e.getClass().getName());
                logger.error("  错误消息: {}", e.getMessage());
                logger.error("  错误堆栈: ", e);
                throw new RuntimeException("AI模型调用失败: " + e.getMessage(), e);
            }
            logger.debug("成功获取PDF {} 的AI摘要，长度: {}", pdfId, summary.length());
            
            // 更新PDF文档的摘要信息
            PdfDocument pdfDocument = pdfDocumentRepository.findById(pdfId)
                    .orElseThrow(() -> new RuntimeException("PDF文档不存在: " + pdfId));
            pdfDocument.setInfo(summary);
            pdfDocument.setIsSummarized(true);
            pdfDocumentRepository.save(pdfDocument);
            
            logger.info("成功为PDF文档 {} 生成摘要", pdfId);
        } catch (Exception e) {
            logger.error("为PDF文档 {} 生成摘要时发生错误: {}", pdfId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**

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
        return "请为以下Markdown格式的中医文献内容生成摘要和总结:\n\n" +
                markdownContent + "\n\n" +
                "请提供以下信息:\n" +
                "1. 文档的主要内容概述\n" +
                "2. 关键观点或结论\n" +
                "3. 重要数据或发现（如果适用）\n" +
                "请用中文回复，保持简洁明了。";
    }
    
    /**
     * 调用阿里云DashScope生成摘要
     * 
     * @param promptText 提示词文本
     * @return 生成的摘要内容
     * @throws NoApiKeyException 当没有提供API Key时抛出
     * @throws ApiException 当API调用出现错误时抛出
     * @throws InputRequiredException 当输入内容不符合要求时抛出
     */
    private String callDashScope(String promptText) throws NoApiKeyException, ApiException, InputRequiredException {
        Generation gen = new Generation();
        
        // 构建系统消息
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的中文文献摘要生成助手，能够准确地从文档中提取关键信息并生成结构化的摘要。")
                .build();
        
        // 构建用户消息
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(promptText)
                .build();
        
        // 构建参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model(System.getenv("MODEL_USE"))
                .messages(List.of(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        
        // 调用模型
        GenerationResult result = gen.call(param);
        
        // 返回结果
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }
}