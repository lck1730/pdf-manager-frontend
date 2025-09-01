package com.example.example.service;

import com.example.example.controller.TagManagementController;
import com.example.example.entity.Tag;
import com.example.example.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * 标签业务服务类
 */
@Service
public class TagService {
    
    private final TagRepository tagRepository;
    private static final Logger log = LoggerFactory.getLogger(TagService.class);
    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    
    /**
     * 保存标签
     * 
     * @param tag 标签对象
     * @return 保存后的标签对象
     */
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }
    
    /**
     * 查找所有不重复的标签
     * 
     * @return 标签列表
     */
    public List<String> findAllTags() {
        return tagRepository.findAllTags();
    }
    
    /**
     * 根据标签查找所有关联的PDF ID和文件名
     * 
     * @param tag 标签
     * @return 包含PDF ID和文件名的列表
     */
    public List<Object> findPdfIdAndFileNameByTag(String tag) {
        return tagRepository.findPdfIdAndFileNameByTag(tag);
    }
    
    /**
     * 根据PDF ID和标签删除记录
     * 
     * @param pdfId PDF ID
     * @param tag 标签
     */
    @Transactional
    public void deleteByPdfIdAndTag(String pdfId, String tag) {
        try {
            // 添加参数校验
            if (pdfId == null || pdfId.trim().isEmpty() ||
                    tag == null || tag.trim().isEmpty()) {
                throw new IllegalArgumentException("PDF ID和标签不能为空");
            }

            tagRepository.deleteByPdfIdAndTag(pdfId, tag);
        } catch (Exception e) {
            log.error("删除标签失败，pdfId: {}, tag: {}", pdfId, tag, e);
            throw new RuntimeException("删除标签失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 根据标签查找标签记录
     * 
     * @param tag 标签
     * @return 标签记录列表
     */
    public List<Tag> findByTag(String tag) {
        return tagRepository.findByTag(tag);
    }
    
    /**
     * 根据PDF ID查找标签记录
     * 
     * @param pdfId PDF ID
     * @return 标签记录列表
     */
    public List<Tag> findByPdfId(String pdfId) {
        return tagRepository.findByPdfId(pdfId);
    }
}