package com.example.example.repository;

import com.example.example.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问接口
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 查找所有不重复的标签
     * 
     * @return 标签列表
     */
    @Query("SELECT DISTINCT t.tag FROM Tag t")
    List<String> findAllTags();
    
    /**
     * 根据标签查找所有关联的PDF ID和文件名
     * 
     * @param tag 标签
     * @return 包含PDF ID和文件名的列表
     */
    @Query("SELECT new map(t.pdfId as pdfId, t.fileName as fileName) FROM Tag t WHERE t.tag = :tag")
    List<Object> findPdfIdAndFileNameByTag(@Param("tag") String tag);
    
    /**
     * 根据PDF ID和标签删除记录
     * 
     * @param pdfId PDF ID
     * @param tag 标签
     */
    void deleteByPdfIdAndTag(String pdfId, String tag);
    
    /**
     * 根据标签查找标签记录
     * 
     * @param tag 标签
     * @return 标签记录列表
     */
    List<Tag> findByTag(String tag);
    
    /**
     * 根据PDF ID查找标签记录
     * 
     * @param pdfId PDF ID
     * @return 标签记录列表
     */
    List<Tag> findByPdfId(String pdfId);
}