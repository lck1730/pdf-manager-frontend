import apiClient from './api'

export const tagService = {
  // 创建标签 - 需要传入包含tag、pdfId和fileName的对象
  createTag(tagData) {
    return apiClient.post('/tags/create', tagData)
  },

  // 根据标签搜索PDF - 需要传入标签数组
  searchByTags(tags) {
    return apiClient.post('/tags/search', tags)
  },

  // 删除标签 - 需要传入pdfId和tag作为查询参数
  deleteTag(pdfId, tag) {
    return apiClient.delete('/tags/delete', {
      params: { pdfId, tag }
    })
  },

  // 根据PDF ID获取标签
  getTagsByPdfId(pdfId) {
    return apiClient.get(`/tags/by-pdf/${pdfId}`)
  },

  // 添加标签到PDF - 使用现有的add接口
  addTagToPdf(pdfId, tag) {
    return apiClient.post('/tags/add', { pdfId, tag })
  },

  // 从PDF删除标签 - 使用现有的delete接口
  removeTagFromPdf(pdfId, tag) {
    return apiClient.delete('/pdf-delete/tag', {
      params: { pdfId, tag }
    })
  }
}
