package com.example.example.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfContentExtractor {

    /**
     * 从PDF文件输入流中提取文本内容
     *
     * @param inputStream PDF文件输入流
     * @return 提取的文本内容
     * @throws IOException 读取或解析过程中可能发生的异常
     */
    public String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            
            // 设置提取范围（可选，用于处理大文件）
            // pdfStripper.setStartPage(1);
            // pdfStripper.setEndPage(10); // 只提取前10页
            
            return pdfStripper.getText(document);
        }
    }

    /**
     * 从字节数组中提取PDF文本内容
     *
     * @param pdfBytes PDF文件字节数组
     * @return 提取的文本内容
     * @throws IOException 读取或解析过程中可能发生的异常
     */
    public String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}