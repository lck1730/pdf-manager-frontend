import apiClient from './api'

export const pdfService = {
  // 获取用户的所有PDF ID和文件名
  getPdfListByUsername(username) {
    return apiClient.get(`/pdf-echo/user/${username}`)
  },

  // 根据PDF ID获取PDF的info和note
  getPdfInfoAndNote(pdfId) {
    return apiClient.get(`/pdf-echo/${pdfId}/info-note`)
  },

  // 根据PDF ID获取表格信息
  getTablesByPdfId(pdfId) {
    return apiClient.get(`/pdf-echo/${pdfId}/tables`)
  },

  // 根据PDF ID获取图片信息
  getImagesByPdfId(pdfId) {
    return apiClient.get(`/pdf-echo/${pdfId}/images`)
  },

  // 根据PDF ID获取原始PDF文件
  getOriginalPdfFile(pdfId) {
    return apiClient.get(`/pdf-echo/${pdfId}/pdf-file`, {
      responseType: 'blob'
    })
  },

  // 根据图片名称获取原始图片文件
  getOriginalImageFile(imageName) {
    return apiClient.get(`/pdf-echo/image-file/${imageName}`, {
      responseType: 'blob'
    })
  },

  // 更新PDF的note字段
  updatePdfNote(pdfId, note) {
    return apiClient.put(`/pdf-edit/${pdfId}/note`, { note })
  },

  // 更新图片的footnote字段
  updateImageFootnote(imageName, footnote) {
    return apiClient.put(`/pdf-edit/image/${imageName}/footnote`, { footnote })
  },

  // 更新表格的footnote字段
  updateTableFootnote(tableId, footnote) {
    return apiClient.put(`/pdf-edit/table/${tableId}/footnote`, { footnote })
  },

  // 上传ZIP文件
  uploadZipFiles(files) {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return apiClient.post('/files/upload-zip', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除PDF及其相关数据
  deletePdfAndRelatedData(pdfId) {
    return apiClient.delete(`/pdf-delete/pdf/${pdfId}`)
  },

  // 根据图片名称删除图片及其文件
  deleteImageAndFile(imageName) {
    return apiClient.delete(`/pdf-delete/image/${imageName}`)
  },

  // 以下是新增的PDF信息提取和摘要接口
  // 获取所有未提取信息的PDF文档
  getUnextractedPdfDocuments() {
    return apiClient.get('/pdf-info/unextracted')
  },

  // 提取指定PDF文档的信息
  extractPdfInfo(pdfId) {
    return apiClient.post(`/pdf-info/extract/${pdfId}`)
  },

  // 提取所有未处理PDF文档的信息
  extractAllPdfInfo() {
    return apiClient.post('/pdf-info/extract-all')
  },

  // 获取所有未总结的PDF文档
  getUnsummarizedPdfDocuments() {
    return apiClient.get('/pdf-info/unsummarized')
  },

  // 为指定PDF生成摘要
  summarizePdf(pdfId) {
    return apiClient.post(`/pdf-info/summarize/${pdfId}`)
  },

  // 为所有未总结的PDF生成摘要
  summarizeAllPdf() {
    return apiClient.post('/pdf-info/summarize-all')
  }
}
