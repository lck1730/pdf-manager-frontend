package com.example.example.repository;

import com.example.example.entity.PdfImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PDF图片数据访问接口
 */
@Repository
public interface PdfImageRepository extends JpaRepository<PdfImage, String> {
    
    /**
cha     * 根据PDF文档ID查找所有关联的图片的image_name、caption和footnote
     * 
     * @param pdfId PDF文档ID
     * @return 包含image_name、caption和footnote的图片信息列表
     */
    @Query("SELECT new map(i.imageName as imageName, i.caption as caption, i.footnote as footnote) FROM PdfImage i WHERE i.pdfId = :pdfId")
    List<Object> findImageNameCaptionAndFootnoteByPdfId(@Param("pdfId") String pdfId);
    
    /**
     * 根据PDF文档ID查找所有关联的图片记录
     * 
     * @param pdfId PDF文档ID
     * @return 图片记录列表
     */
    List<PdfImage> findByPdfId(String pdfId);
    
    /**
     * 根据图片名称查找图片
     * 
     * @param imageName 图片名称
     * @return 图片对象
     */
    Optional<PdfImage> findByImageName(String imageName);
}