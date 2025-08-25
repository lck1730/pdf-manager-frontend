package com.example.example.service;

import com.example.example.entity.PdfImage;
import com.example.example.repository.PdfImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * PDF图片业务服务类
 */
@Service
public class PdfImageService {

    private final PdfImageRepository pdfImageRepository;

    @Autowired
    public PdfImageService(PdfImageRepository pdfImageRepository) {
        this.pdfImageRepository = pdfImageRepository;
    }
    
    /**
     * 创建PDF图片记录
     * 
     * @param pdfImage PDF图片对象
     * @return 保存后的PDF图片对象
     */
    public PdfImage createPdfImage(PdfImage pdfImage) {
        return pdfImageRepository.save(pdfImage);
    }

    /**
     * 根据PDF文档ID查找所有关联的图片的image_name、caption和footnote
     *
     * @param pdfId PDF文档ID
     * @return 包含image_name、caption和footnote的图片信息列表
     */
    public List<Object> findImageNameCaptionAndFootnoteByPdfId(String pdfId) {
        return pdfImageRepository.findImageNameCaptionAndFootnoteByPdfId(pdfId);
    }

    /**
     * 根据图片名称更新图片的caption和footnote字段
     *
     * @param imageName 图片名称
     * @param caption   新的caption内容
     * @param footnote  新的footnote内容
     * @return 更新后的PDF图片对象
     */
    public Optional<PdfImage> updateCaptionAndFootnoteByImageName(String imageName, String caption, String footnote) {
        Optional<PdfImage> pdfImageOpt = pdfImageRepository.findByImageName(imageName);
        if (pdfImageOpt.isPresent()) {
            PdfImage pdfImage = pdfImageOpt.get();
            // 只有当caption非空时才更新caption字段
            if (caption != null && !caption.isEmpty()) {
                pdfImage.setCaption(caption);
            }
            // 只有当footnote非空时才更新footnote字段
            if (footnote != null) {
                pdfImage.setFootnote(footnote);
            }
            return Optional.of(pdfImageRepository.save(pdfImage));
        }
        return Optional.empty();
    }
    
    /**
     * 根据图片名称删除图片记录
     * 
     * @param imageName 图片名称
     */
    public void deleteByImageName(String imageName) {
        pdfImageRepository.deleteById(imageName);
    }
}