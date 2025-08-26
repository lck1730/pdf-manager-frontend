import { ref, reactive } from 'vue'
import { pdfService } from '@/services/pdfService'
import { tagService } from '@/services/tagService'

export function usePdfManager() {
  // 状态管理
  const pdfList = ref([])
  const selectedPdf = ref(null)
  const tags = ref([])
  const isUploading = ref(false)
  const uploadProgress = ref(0)
  const loading = ref(false)

  // 获取PDF列表的方法
  const fetchPdfList = async (username) => {
    loading.value = true
    try {
      const response = await pdfService.getPdfListByUsername(username)
      console.log('API response:', response)
      // 确保数据正确赋值
      pdfList.value = response.data || []
      console.log('PDF list updated:', pdfList.value)
    } catch (error) {
      console.error('获取PDF列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 选择PDF
  const selectPdf = (pdf) => {
    console.log('Selecting PDF in usePdfManager:', pdf)
    selectedPdf.value = pdf
  }

  // 切换标签选择
  const toggleTag = (tag) => {
    tags.value.forEach(t => {
      t.selected = t.id === tag.id
    })
  }

  // 获取PDF的标签
  const fetchTagsByPdfId = async (pdfId) => {
    try {
      const response = await tagService.getTagsByPdfId(pdfId)
      return response.data
    } catch (error) {
      console.error('获取PDF标签失败:', error)
      return []
    }
  }

  // 处理ZIP文件上传
  const handleZipUpload = async (files, username) => {
    isUploading.value = true
    uploadProgress.value = 0
    
    try {
      // 创建FormData对象
      const formData = new FormData()
      
      // 添加文件和用户名到FormData
      for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i])
      }
      formData.append('username', username)
      
      // 上传文件
      const response = await pdfService.uploadZipFiles(formData, (progressEvent) => {
        // 计算上传进度
        const { loaded, total } = progressEvent
        uploadProgress.value = Math.round((loaded / total) * 100)
      })
      
      // 上传完成后重新获取PDF列表
      await fetchPdfList(username)
      
      return response.data
    } catch (error) {
      console.error('ZIP文件上传失败:', error)
      throw error
    } finally {
      isUploading.value = false
      uploadProgress.value = 0
    }
  }

  // 删除PDF
  const deletePdf = async (pdfId, username) => {
    try {
      await pdfService.deletePdf(pdfId)
      // 删除完成后重新获取PDF列表
      await fetchPdfList(username)
    } catch (error) {
      console.error('删除PDF失败:', error)
      throw error
    }
  }

  return {
    // 响应式数据
    pdfList,
    selectedPdf,
    tags,
    isUploading,
    uploadProgress,
    loading,
    
    // 方法
    fetchPdfList,
    selectPdf,
    toggleTag,
    fetchTagsByPdfId,
    handleZipUpload,
    deletePdf
  }
}