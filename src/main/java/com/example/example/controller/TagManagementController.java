package com.example.example.controller;

import com.example.example.entity.Tag;
import com.example.example.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagManagementController {

    private final TagService tagService;

    @Autowired
    public TagManagementController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 根据tag、pdfID以及filename创建tag表记录
     *
     * @param tagRequest 包含tag、pdfId和fileName的请求体
     * @return 创建的标签对象
     */
    @PostMapping("/create")
    public ResponseEntity<Tag> createTag(@RequestBody Map<String, String> tagRequest) {
        String tag = tagRequest.get("tag");
        String pdfId = tagRequest.get("pdfId");
        String fileName = tagRequest.get("fileName");

        Tag newTag = new Tag(pdfId, fileName, tag);
        Tag savedTag = tagService.saveTag(newTag);
        return ResponseEntity.ok(savedTag);
    }

    /**
     * 根据tag内容获得符合条件的pdfID和filename，支持多个tag同时匹配
     *
     * @param tags 标签列表
     * @return 符合条件的PDF ID和文件名列表
     */
    @PostMapping("/search")
    public ResponseEntity<List<Object>> searchByTags(@RequestBody List<String> tags) {
        List<Object> result = tags.stream()
                .map(tagService::findPdfIdAndFileNameByTag)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据tag和pdfID删除一条tag记录
     *
     * @param pdfId PDF文档ID
     * @param tag 标签
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTag(@RequestParam String pdfId, @RequestParam String tag) {
        tagService.deleteByPdfIdAndTag(pdfId, tag);
        return ResponseEntity.ok("标签删除成功");
    }
    
    /**
     * 根据pdfID在tag表找到其所有的tag
     *
     * @param pdfId PDF文档ID
     * @return 标签列表
     */
    @GetMapping("/by-pdf/{pdfId}")
    public ResponseEntity<List<Tag>> getTagsByPdfId(@PathVariable String pdfId) {
        List<Tag> tags = tagService.findByPdfId(pdfId);
        return ResponseEntity.ok(tags);
    }
}