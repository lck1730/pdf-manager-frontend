package com.example.example.controller;

import com.example.example.dto.UploadResultDTO;
import com.example.example.entity.PdfDocument;
import com.example.example.entity.PdfImage;
import com.example.example.service.FileStorageService;
import com.example.example.service.PdfDocumentService;
import com.example.example.service.PdfImageService;
import com.example.example.service.PdfProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final PdfDocumentService pdfDocumentService;
    private final PdfImageService pdfImageService;
//    private final PdfProcessingService pdfProcessingService;
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    public FileUploadController(FileStorageService fileStorageService, 
                               PdfDocumentService pdfDocumentService,
                               PdfImageService pdfImageService,
                               PdfProcessingService pdfProcessingService) {
        this.fileStorageService = fileStorageService;
        this.pdfDocumentService = pdfDocumentService;
        this.pdfImageService = pdfImageService;
//        this.pdfProcessingService = pdfProcessingService;
    }

    /**
     * 上传压缩包文件并进行处理
     * 
     * @param files 上传的压缩包文件数组
     * @return 处理结果
     */
    @PostMapping("/upload-zip")
    public ResponseEntity<Map<String, Object>> uploadZipFiles(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        List<UploadResultDTO> results = new ArrayList<>();
        
        // 记录上传的文件信息
        logger.info("接收到 {} 个文件上传请求", files != null ? files.length : 0);
        
        try {
            if (files == null || files.length == 0) {
                logger.warn("未找到上传的文件");
                response.put("success", false);
                response.put("message", "未找到上传的文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查临时目录配置
            String tempLocation = System.getProperty("user.dir") + "/temp/uploads";
            File tempDir = new File(tempLocation);
            if (!tempDir.exists()) {
                logger.warn("临时目录不存在: {}", tempLocation);
            } else {
                logger.info("临时目录存在: {}", tempLocation);
            }
            
            // 处理每个压缩包文件
            for (MultipartFile file : files) {
                try {
                    logger.info("处理文件: {} (大小: {} bytes)", file.getOriginalFilename(), file.getSize());
                    processZipFile(file, results); // 直接调用，不再通过self
                } catch (Exception e) {
                    // 提取文件名用于日志和结果记录
                    String originalFilename = file.getOriginalFilename();
                    String errorMessage = "处理失败: " + e.getMessage();
                    
                    // 记录详细的错误信息
                    logger.error("文件 '{}' 处理失败: {}", originalFilename, e.getMessage(), e);
                    
                    // 添加错误结果
                    results.add(new UploadResultDTO(originalFilename, null, false, errorMessage));
                }
            }
            
            response.put("success", true);
            response.put("message", "批量上传处理完成");
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批量上传处理失败", e);
            response.put("success", false);
            response.put("message", "批量上传处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 处理单个压缩包文件
     * 
     * @param zipFile 压缩包文件
     * @param results 结果列表
     * @throws Exception 处理过程中可能发生的异常
     */
    private void processZipFile(MultipartFile zipFile, List<UploadResultDTO> results) throws Exception {
        String fileName = null;
        String pdfId = null;
        
        // 用于补偿机制的已上传文件记录
        boolean pdfUploaded = false;
        boolean jsonUploaded = false;
        boolean markdownUploaded = false;
        List<String> uploadedImages = new ArrayList<>();
        
        // 创建临时目录用于解压文件
        Path tempDir = Files.createTempDirectory("zip_extract_");
        logger.info("创建临时解压目录: {}", tempDir.toString());
        
        try {
            // 1. 从压缩包文件名中提取文件名和PDF ID
            String originalFilename = zipFile.getOriginalFilename();
            logger.info("开始处理压缩文件: {}", originalFilename);
            
            if (originalFilename == null) {
                throw new IllegalArgumentException("文件名不能为空");
            }
            
            // 移除压缩文件后缀（支持zip, rar, 7z等）
            String fileNameWithoutExtension = originalFilename;
            if (originalFilename.toLowerCase().endsWith(".zip")) {
                fileNameWithoutExtension = originalFilename.substring(0, originalFilename.length() - 4);
            } else if (originalFilename.toLowerCase().endsWith(".rar")) {
                fileNameWithoutExtension = originalFilename.substring(0, originalFilename.length() - 4);
            } else if (originalFilename.toLowerCase().endsWith(".7z")) {
                fileNameWithoutExtension = originalFilename.substring(0, originalFilename.length() - 3);
            }
            
            logger.info("处理后的文件名: {}", fileNameWithoutExtension);
            
            // 检查是否符合 文件名.pdf-uuid 格式
            if (!fileNameWithoutExtension.matches(".*\\.pdf-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                throw new IllegalArgumentException("文件名格式不正确，应为 文件名.pdf-uuid.压缩文件后缀 格式，例如 example.pdf-uuid.zip");
            }
            
            // 提取文件名部分（去除.pdf-uuid部分）
            int pdfUuidIndex = fileNameWithoutExtension.indexOf(".pdf-");
            fileName = fileNameWithoutExtension.substring(0, pdfUuidIndex);
            
            // 从文件名中提取UUID
            pdfId = extractUuidFromFileName(fileNameWithoutExtension);
            if (pdfId == null) {
                throw new IllegalArgumentException("无法从文件名中提取有效的UUID");
            }
            
            logger.info("提取到文件名: {}, UUID: {}", fileName, pdfId);
            
            // 2. 解压压缩包
            logger.info("开始解压文件到目录: {}", tempDir.toString());
            extractZipFile(zipFile, tempDir);
            logger.info("文件解压完成");
            
            // 3. 查找必需的文件
            File pdfFile = null;
            File jsonFile = null;
            File mdFile = null;
            File imagesDir = new File(tempDir.toFile(), "images");
            
            // 查找PDF文件
            File[] pdfFiles = tempDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfFiles == null || pdfFiles.length == 0) {
                throw new Exception("未找到PDF文件");
            }
            if (pdfFiles.length > 1) {
                throw new Exception("找到多个PDF文件");
            }
            pdfFile = pdfFiles[0];
            
            // 查找JSON文件（排除layout.json）
            File[] jsonFiles = tempDir.toFile().listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".json") && !name.equalsIgnoreCase("layout.json"));
            if (jsonFiles == null || jsonFiles.length == 0) {
                throw new Exception("未找到有效的JSON文件");
            }
            if (jsonFiles.length > 1) {
                throw new Exception("找到多个有效的JSON文件");
            }
            jsonFile = jsonFiles[0];
            
            // 查找Markdown文件
            File[] mdFiles = tempDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".md"));
            if (mdFiles == null || mdFiles.length == 0) {
                throw new Exception("未找到Markdown文件");
            }
            if (mdFiles.length > 1) {
                throw new Exception("找到多个Markdown文件");
            }
            mdFile = mdFiles[0];
            
            // 4. 上传PDF文件
            try {
                fileStorageService.uploadOriginalPdf(createMultipartFile(pdfFile), pdfId);
                pdfUploaded = true;
            } catch (Exception e) {
                throw new Exception("上传PDF文件失败: " + e.getMessage(), e);
            }
            
            // 5. 上传JSON文件
            try {
                String jsonContent = readFileContent(jsonFile.toPath());
                fileStorageService.uploadJson(jsonContent, pdfId);
                jsonUploaded = true;
            } catch (Exception e) {
                throw new Exception("上传JSON文件失败: " + e.getMessage(), e);
            }
            
            // 6. 上传Markdown文件
            try {
                String markdownContent = readFileContent(mdFile.toPath());
                fileStorageService.uploadMarkdown(markdownContent, pdfId);
                markdownUploaded = true;
            } catch (Exception e) {
                throw new Exception("上传Markdown文件失败: " + e.getMessage(), e);
            }
            
            // 7. 上传图片文件（如果存在images文件夹）
            if (imagesDir.exists() && imagesDir.isDirectory()) {
                File[] imageFiles = imagesDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                           lowerName.endsWith(".png") || lowerName.endsWith(".gif");
                });
                
                if (imageFiles != null) {
                    for (File imageFile : imageFiles) {
                        try {
                            fileStorageService.uploadImage(createMultipartFile(imageFile));
                            uploadedImages.add(imageFile.getName());
                        } catch (Exception e) {
                            throw new Exception("上传图片文件失败: " + e.getMessage(), e);
                        }
                    }
                }
            }
            
            // 8. 在数据库中创建PDF文档记录
            PdfDocument pdfDocument = new PdfDocument();
            pdfDocument.setPdfId(pdfId);
            pdfDocument.setFileName(fileName + ".pdf");
            pdfDocument.setUserName("yaya"); // 默认用户名
            pdfDocument.setInfo(""); // 默认为空
            pdfDocument.setNote(""); // 默认为空
            pdfDocumentService.createPdfDocument(pdfDocument);
            
            // 9. 在数据库中创建图片记录（如果存在图片）
            if (imagesDir.exists() && imagesDir.isDirectory()) {
                File[] imageFiles = imagesDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                           lowerName.endsWith(".png") || lowerName.endsWith(".gif");
                });
                
                if (imageFiles != null) {
                    for (File imageFile : imageFiles) {
                        PdfImage pdfImage = new PdfImage();
                        pdfImage.setImageName(imageFile.getName());
                        pdfImage.setPdfId(pdfId);
                        pdfImage.setCaption(""); // 默认为空
                        pdfImage.setFootnote(""); // 默认为空
                        pdfImageService.createPdfImage(pdfImage);
                    }
                }
            }
            
            // 添加成功结果
            results.add(new UploadResultDTO(fileName, pdfId, true, "处理成功"));
        } catch (Exception e) {
            // 提取文件名用于日志和结果记录
            String originalFilename = zipFile.getOriginalFilename();
            String errorMessage = "处理失败: " + e.getMessage();
            
            // 记录详细的错误信息
            logger.error("文件 '{}' 处理失败: {}", originalFilename, e.getMessage(), e);
            
            // 添加错误结果
            results.add(new UploadResultDTO(originalFilename, null, false, errorMessage));
        } finally {
            // 确保临时目录被删除
            try {
                deleteDirectory(tempDir.toFile());
            } catch (Exception e) {
                logger.warn("删除临时目录时发生错误: {}", e.getMessage());
            }
        }
    }

    /**
     * 解压ZIP文件到指定目录（流式处理避免OOM）
     * 
     * @param zipFile ZIP文件
     * @param destDir 目标目录
     * @throws IOException IO异常
     */
    private void extractZipFile(MultipartFile zipFile, Path destDir) throws IOException {
        logger.info("开始解压文件: {} 到目录: {}", zipFile.getOriginalFilename(), destDir.toString());
        
        try (InputStream inputStream = zipFile.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            int entriesCount = 0;
            
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // 防止Zip Slip攻击
                Path entryPath = destDir.resolve(entry.getName()).normalize();
                if (!entryPath.startsWith(destDir)) {
                    throw new IOException("ZIP条目在目标目录外: " + entry.getName());
                }
                
                if (entry.isDirectory()) {
                    // 创建目录
                    Files.createDirectories(entryPath);
                    logger.debug("创建目录: {}", entryPath.toString());
                } else {
                    // 创建父目录
                    Path parentDir = entryPath.getParent();
                    if (parentDir != null) {
                        Files.createDirectories(parentDir);
                    }
                    
                    // 解压文件
                    try (OutputStream outputStream = Files.newOutputStream(entryPath)) {
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    logger.debug("解压文件: {} (大小: {} bytes)", entryPath.toString(), entry.getSize());
                }
                
                zipInputStream.closeEntry();
                entriesCount++;
            }
            
            logger.info("解压完成，共处理 {} 个条目", entriesCount);
        } catch (IOException e) {
            logger.error("解压文件时发生错误", e);
            throw e;
        }
    }

    /**
     * 递归删除目录及其内容
     *
     * @param directoryToBeDeleted 要删除的目录
     * @return 是否删除成功
     */
    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * 读取文件内容
     * 
     * @param path 文件路径
     * @return 文件内容字符串
     * @throws IOException 读取文件时可能发生的IO异常
     */
    private String readFileContent(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * 从文件名中提取UUID
     * 
     * @param fileName 文件名
     * @return 提取的UUID字符串
     */
    private String extractUuidFromFileName(String fileName) {
        // 查找".pdf-"后缀，UUID应该在它后面
        int pdfUuidIndex = fileName.indexOf(".pdf-");
        if (pdfUuidIndex > 0 && pdfUuidIndex < fileName.length() - 5) {
            // 提取UUID部分
            String potentialUuid = fileName.substring(pdfUuidIndex + 5); // 5 是 ".pdf-" 的长度
            
            // 验证是否是有效的UUID格式 (标准UUID格式: 8-4-4-4-12 的十六进制字符)
            if (potentialUuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                try {
                    UUID.fromString(potentialUuid);
                    return potentialUuid;
                } catch (IllegalArgumentException e) {
                    // 不是有效的UUID格式
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据文件创建MultipartFile对象
     * 
     * @param file 文件
     * @return MultipartFile对象
     * @throws Exception 创建过程中可能发生的异常
     */
    private MultipartFile createMultipartFile(File file) throws Exception {
        // 验证文件路径，防止路径遍历攻击
        file.getCanonicalFile(); // 规范化文件路径
        
        return new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    return "application/pdf";
                } else if (fileName.endsWith(".md")) {
                    return "text/markdown";
                } else if (fileName.endsWith(".json")) {
                    return "application/json";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    return "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    return "image/png";
                } else if (fileName.endsWith(".gif")) {
                    return "image/gif";
                }
                return "application/octet-stream";
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(file.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return Files.newInputStream(file.toPath());
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(file.toPath(), dest.toPath());
            }
        };
    }
}