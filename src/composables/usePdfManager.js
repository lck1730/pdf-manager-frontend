// 文件路径: src/composables/usePdfManager.js
import { ref, computed } from 'vue'
import { pdfService } from '@/services/pdfService'

// 创建单例状态
const state = {
  allPdfList: ref([]),     // 存储所有PDF（统一格式）
  filteredPdfIds: ref([]), // 存储筛选后的PDF ID
  selectedPdf: ref(null),
  isFiltered: ref(false),
  isUploading: ref(false),      // 添加上传状态
  uploadProgress: ref(0),       // 添加上传进度
  currentUser: ref('yaya')      // 添加当前用户状态
}

// 创建一次性的函数定义  单例模式？
const functions = {
  // 获取用户的所有PDF列表
  fetchPdfList: async (username) => {
    try {
      console.log('正在获取PDF列表...')
      // 更新当前用户
      if (username) {
        state.currentUser.value = username
      }
      
      const response = await pdfService.getPdfListByUsername(username)
      console.log('获取PDF列表成功:', response)

      // 统一处理数据格式，确保所有PDF对象都有统一的字段
      if (response.data && Array.isArray(response.data)) {
        state.allPdfList.value = response.data.map(item => {
          // 处理二维数组格式 [id, filename]
          if (Array.isArray(item) && item.length >= 2) {
            return {
              id: item[0],           // ID在索引0位置
              filename: item[1],     // 文件名在索引1位置
              tags: []
            }
          }
          // 处理对象格式
          else if (typeof item === 'object' && item !== null) {
            return {
              id: item.id || item.pdfId,                    // 统一ID字段
              filename: item.filename || item.fileName || item.name,
              tags: item.tags || [],
              ...item // 保留其他属性
            }
          }
          // 其他情况返回默认对象
          return {
            id: null,
            filename: '未知文件',
            tags: []
          }
        })
      } else {
        state.allPdfList.value = []
      }

      state.filteredPdfIds.value = [] // 重置筛选
      state.isFiltered.value = false  // 重置筛选状态
      state.selectedPdf.value = null
      console.log('设置allPdfList:', state.allPdfList.value)
    } catch (error) {
      console.error('获取PDF列表失败:', error)
      state.allPdfList.value = []
      state.filteredPdfIds.value = []
      state.isFiltered.value = false
    }
  },

  // 设置筛选后的PDF ID列表
  setFilteredPdfList: (pdfIds) => {
    console.log('设置筛选后的PDF列表:', pdfIds)
    console.log('当前allPdfList:', state.allPdfList.value)
    console.log('当前allPdfList长度:', state.allPdfList.value.length)

    // 如果传入null或undefined，表示显示所有PDF
    if (pdfIds === null || pdfIds === undefined) {
      console.log('显示所有PDF')
      state.filteredPdfIds.value = []
      state.isFiltered.value = false
      return
    }

    // 如果传入空数组，表示无匹配结果
    if (Array.isArray(pdfIds) && pdfIds.length === 0) {
      console.log('无匹配结果')
      state.filteredPdfIds.value = []
      state.isFiltered.value = true
      return
    }

    // 如果传入非空数组，表示筛选结果
    if (Array.isArray(pdfIds) && pdfIds.length > 0) {
      console.log('设置筛选结果')
      state.filteredPdfIds.value = pdfIds
      state.isFiltered.value = true
      return
    }

    // 其他情况，默认显示所有PDF
    console.log('默认显示所有PDF')
    state.filteredPdfIds.value = []
    state.isFiltered.value = false
  },

  // 选择PDF
  selectPdf: (pdf) => {
    state.selectedPdf.value = pdf
  },

  // 新增：处理ZIP文件上传
  handleZipUpload: async (files) => {
    if (!files || !files.length) {
      console.warn('没有选择文件')
      return
    }

    // 设置上传状态
    state.isUploading.value = true
    state.uploadProgress.value = 0

    try {
      console.log('开始上传ZIP文件:', files)
      
      // 调用后端API上传ZIP文件，带进度回调
      const response = await pdfService.uploadZipFiles(files, (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        state.uploadProgress.value = percentCompleted
        console.log(`上传进度: ${percentCompleted}%`)
      })
      
      console.log('上传成功:', response)
      
      // 上传成功后刷新PDF列表，使用当前用户
      await functions.fetchPdfList(state.currentUser.value)
      
      // 重置上传状态
      state.isUploading.value = false
      state.uploadProgress.value = 100
      
      return response
    } catch (error) {
      console.error('上传失败:', error)
      // 重置上传状态
      state.isUploading.value = false
      state.uploadProgress.value = 0
      throw error
    }
  },

  // 暴露一个方法来获取当前状态，用于调试
  getDebugInfo: () => {
    return {
      allPdfList: state.allPdfList.value,
      filteredPdfIds: state.filteredPdfIds.value,
      isFiltered: state.isFiltered.value,
      allPdfListLength: state.allPdfList.value.length
    }
  }
}

// 计算属性：返回当前应该显示的PDF列表
const pdfList = computed(() => {
  console.log('计算pdfList:', {
    isFiltered: state.isFiltered.value,
    filteredPdfIds: state.filteredPdfIds.value,
    allPdfList: state.allPdfList.value,
    allPdfListLength: state.allPdfList.value.length
  })

  // 如果没有进行筛选，显示所有PDF
  if (!state.isFiltered.value) {
    console.log('返回所有PDF:', state.allPdfList.value)
    return state.allPdfList.value
  }

  // 如果进行了筛选且有筛选ID，返回匹配的PDF
  if (state.isFiltered.value && state.filteredPdfIds.value.length > 0) {
    const result = state.allPdfList.value.filter(pdf => {
      // 使用统一的ID字段进行匹配
      const includes = pdf.id && state.filteredPdfIds.value.includes(pdf.id)
      console.log(`检查PDF ${pdf.id}: ${includes}`)
      return includes
    })
    console.log('返回筛选结果:', result)
    return result
  }

  // 如果进行了筛选但没有筛选ID，返回空数组
  console.log('返回空数组')
  return []
})

// 导出单例
export function usePdfManager() {
  return {
    pdfList,
    fetchPdfList: functions.fetchPdfList,
    selectPdf: functions.selectPdf,
    selectedPdf: state.selectedPdf,
    setFilteredPdfList: functions.setFilteredPdfList,
    isFiltered: state.isFiltered,
    getDebugInfo: functions.getDebugInfo,
    handleZipUpload: functions.handleZipUpload,
    isUploading: state.isUploading,     // 导出上传状态
    uploadProgress: state.uploadProgress, // 导出上传进度
    currentUser: state.currentUser      // 导出当前用户
  }
}