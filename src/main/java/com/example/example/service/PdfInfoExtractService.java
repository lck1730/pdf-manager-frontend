package com.example.example.service;

import com.example.example.entity.PdfDocument;
import com.example.example.entity.PdfImage;
import com.example.example.entity.PdfTable;
import com.example.example.repository.PdfDocumentRepository;
import com.example.example.repository.PdfImageRepository;
import com.example.example.repository.PdfTableRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class PdfInfoExtractService {

    private final PdfDocumentRepository pdfDocumentRepository;
    private final PdfImageRepository pdfImageRepository;
    private final PdfTableRepository pdfTableRepository;
    private final MinioClient minioClient;
    private final ObjectMapper objectMapper;

    @Value("${minio.bucket.json}")
    private String jsonBucket;

    public PdfInfoExtractService(PdfDocumentRepository pdfDocumentRepository,
                                 PdfImageRepository pdfImageRepository,
                                 PdfTableRepository pdfTableRepository,
                                 MinioClient minioClient,
                                 ObjectMapper objectMapper) {
        this.pdfDocumentRepository = pdfDocumentRepository;
        this.pdfImageRepository = pdfImageRepository;
        this.pdfTableRepository = pdfTableRepository;
        this.minioClient = minioClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 查找所有未提取信息的PDF文档
     *
     * @return 未提取信息的PDF文档列表
     */
    public List<PdfDocument> findUnextractedPdfDocuments() {
        return pdfDocumentRepository.findByIsExtractedFalse();
    }

    /**
     * 提取指定PDF文档的信息
     *
     * @param pdfId PDF文档ID
     * @throws Exception 处理过程中可能发生的异常
     */
    @Transactional
    public void extractPdfInfo(String pdfId) throws Exception {
        // 从MinIO获取JSON文件
        try (GetObjectResponse response = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(jsonBucket)
                        .object(pdfId + ".json")
                        .build())) {

            // 读取JSON内容
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // 处理JSON内容
            processJsonContent(pdfId, jsonNode);
            
            // 更新PDF文档的提取状态
            PdfDocument pdfDocument = pdfDocumentRepository.findById(pdfId)
                    .orElseThrow(() -> new RuntimeException("PDF文档不存在: " + pdfId));
            pdfDocument.setIsExtracted(true);
            pdfDocumentRepository.save(pdfDocument);
        }
    }

    /**
     * 处理JSON内容，提取图像和表格信息
     *
     * @param pdfId    PDF文档ID
     * @param jsonNode JSON内容节点
     * @throws Exception 处理过程中可能发生的异常
     */
    private void processJsonContent(String pdfId, JsonNode jsonNode) throws Exception {
        // 确保JSON是一个数组
        if (!jsonNode.isArray()) {
            throw new RuntimeException("JSON内容格式不正确，应为数组格式");
        }

        // 遍历JSON数组中的每个元素
        Iterator<JsonNode> elements = jsonNode.elements();
        List<PdfTable> tablesToSave = new ArrayList<>();

        while (elements.hasNext()) {
            JsonNode element = elements.next();
            String type = element.get("type").asText();

            if ("image".equals(type)) {
                // 处理图像信息
                processImageElement(pdfId, element);
            } else if ("table".equals(type)) {
                // 处理表格信息
                PdfTable table = processTableElement(pdfId, element);
                tablesToSave.add(table);
            }
        }

        // 批量保存表格记录
        pdfTableRepository.saveAll(tablesToSave);
    }

    /**
     * 处理图像元素
     *
     * @param pdfId    PDF文档ID
     * @param element  图像元素节点
     * @throws Exception 处理过程中可能发生的异常
     */
    private void processImageElement(String pdfId, JsonNode element) throws Exception {
        // 获取图像路径
        JsonNode imgPathNode = element.get("img_path");
        if (imgPathNode == null || imgPathNode.isNull()) {
            throw new RuntimeException("图像元素缺少img_path字段");
        }

        String imgPath = imgPathNode.asText();
        // 去掉"images/"前缀获取图像名称
        String imageName = imgPath.replace("images/", "");

        // 查找图像记录
        PdfImage pdfImage = pdfImageRepository.findById(imageName)
                .orElseThrow(() -> new RuntimeException("图像记录不存在: " + imageName));

        // 更新图像的标题和脚注
        JsonNode captionNode = element.get("image_caption");
        if (captionNode != null && !captionNode.isNull()) {
            StringBuilder captionBuilder = new StringBuilder();
            Iterator<JsonNode> captionElements = captionNode.elements();
            while (captionElements.hasNext()) {
                captionBuilder.append(captionElements.next().asText());
                if (captionElements.hasNext()) {
                    captionBuilder.append(" ");
                }
            }
            pdfImage.setCaption(captionBuilder.toString());
        }

        JsonNode footnoteNode = element.get("image_footnote");
        if (footnoteNode != null && !footnoteNode.isNull()) {
            StringBuilder footnoteBuilder = new StringBuilder();
            Iterator<JsonNode> footnoteElements = footnoteNode.elements();
            while (footnoteElements.hasNext()) {
                footnoteBuilder.append(footnoteElements.next().asText());
                if (footnoteElements.hasNext()) {
                    footnoteBuilder.append(" ");
                }
            }
            pdfImage.setFootnote(footnoteBuilder.toString());
        }

        // 保存更新后的图像记录
        pdfImageRepository.save(pdfImage);
    }

    /**
     * 处理表格元素
     *
     * @param pdfId   PDF文档ID
     * @param element 表格元素节点
     * @return PdfTable对象
     * @throws Exception 处理过程中可能发生的异常
     */
    private PdfTable processTableElement(String pdfId, JsonNode element) throws Exception {
        // 获取表格图像路径
        JsonNode imgPathNode = element.get("img_path");
        if (imgPathNode == null || imgPathNode.isNull()) {
            throw new RuntimeException("表格元素缺少img_path字段");
        }

        String imgPath = imgPathNode.asText();
        // 去掉"images/"前缀获取图像路径
        String imagePath = imgPath.replace("images/", "");

        // 获取表格标题
        String caption = "";
        JsonNode captionNode = element.get("table_caption");
        if (captionNode != null && !captionNode.isNull()) {
            StringBuilder captionBuilder = new StringBuilder();
            Iterator<JsonNode> captionElements = captionNode.elements();
            while (captionElements.hasNext()) {
                captionBuilder.append(captionElements.next().asText());
                if (captionElements.hasNext()) {
                    captionBuilder.append(" ");
                }
            }
            caption = captionBuilder.toString();
        }

        // 获取表格脚注
        String footnote = "";
        JsonNode footnoteNode = element.get("table_footnote");
        if (footnoteNode != null && !footnoteNode.isNull()) {
            StringBuilder footnoteBuilder = new StringBuilder();
            Iterator<JsonNode> footnoteElements = footnoteNode.elements();
            while (footnoteElements.hasNext()) {
                footnoteBuilder.append(footnoteElements.next().asText());
                if (footnoteElements.hasNext()) {
                    footnoteBuilder.append(" ");
                }
            }
            footnote = footnoteBuilder.toString();
        }

        // 获取表格内容
        String tableBody = "";
        JsonNode tableBodyNode = element.get("table_body");
        if (tableBodyNode != null && !tableBodyNode.isNull()) {
            tableBody = tableBodyNode.asText();
        }

        // 创建表格记录
        return new PdfTable(pdfId, tableBody, imagePath, caption, footnote);
    }
}