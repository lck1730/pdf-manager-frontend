// src/services/ragService.js
import apiClient from './api'

export const ragService = {
    // 创建新的对话会话
    createSession(sessionData) {
        return apiClient.post('/api/rag/session', sessionData)
    },

    // 获取用户的会话列表
    getUserSessions() {
        return apiClient.get('/api/rag/sessions')
    },

    // 重命名会话
    renameSession(sessionId, sessionData) {
        return apiClient.put(`/api/rag/session/${sessionId}`, sessionData)
    },

    // 删除会话
    deleteSession(sessionId) {
        return apiClient.delete(`/api/rag/session/${sessionId}`)
    },

    // 获取指定会话的聊天历史
    getChatHistory(sessionId) {
        return apiClient.get(`/api/rag/history/${sessionId}`)
    },

    // 基于RAG的问答
    ragChat(chatData) {
        return apiClient.post('/api/rag/chat', chatData)
    },

    // 将PDF存储到向量数据库
    storePdfDocument(pdfData) {
        return apiClient.post('/api/rag/document', pdfData)
    },

    // 从向量数据库删除PDF
    deletePdfDocuments(pdfData) {
        return apiClient.delete('/api/rag/document', { data: pdfData })
    },

    // 获取未向量化存储的PDF文档列表
    getUnembeddedPdfs() {
        return apiClient.get('/api/rag/pdfs/unembedded')
    },

    // 获取已向量化存储的PDF文档列表
    getEmbeddedPdfs() {
        return apiClient.get('/api/rag/pdfs/embedded')
    },

    // 将PDF文档向量化存储
    embedPdfDocument(pdfData) {
        return apiClient.post('/api/rag/pdfs/embed', pdfData)
    },

    // 移除PDF文档的向量化存储
    unembedPdfDocument(pdfData) {
        return apiClient.delete('/api/rag/pdfs/embed', { data: pdfData })
    }
}
