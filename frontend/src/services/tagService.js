import apiClient from './api'

export const tagService = {
  // 创建标签
  createTag(tagData) {
    return apiClient.post('/tags/create', tagData)
  },

  // 根据标签搜索PDF
  searchByTags(tags) {
    return apiClient.post('/tags/search', tags)
  },

  // 删除标签
  deleteTag(pdfId, tag) {
    return apiClient.delete('/tags/delete', {
      params: { pdfId, tag }
    })
  },

  // 根据PDF ID获取标签
  getTagsByPdfId(pdfId) {
    return apiClient.get(`/tags/by-pdf/${pdfId}`)
  }
}