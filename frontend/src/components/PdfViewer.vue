<template>
  <div class="pdf-viewer">
    <!-- 主体 -->
    <main class="pdf-content">
      <iframe v-if="pdfUrl" :src="pdfUrl" class="pdf-iframe" />
      <p v-else-if="loading" class="loading">加载中…</p>
      <p v-else-if="loadError" style="color:red">{{ errorMessage }}</p>
      <div v-else class="pdf-placeholder">请选择 PDF</div>
    </main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { pdfService } from '@/services/pdfService'

// 定义props
const props = defineProps({
  pdf: {
    type: Object,
    default: null
  }
})

const pdfUrl = ref('')
const loading = ref(false)
const loadError = ref(false)
const errorMessage = ref('')

// 加载PDF内容 - 提前定义函数以避免初始化错误
const loadPdfContent = async (pdfId) => {
  // 清理之前的URL
  if (pdfUrl.value) {
    URL.revokeObjectURL(pdfUrl.value)
    pdfUrl.value = ''
  }

  loading.value = true
  loadError.value = false
  errorMessage.value = ''

  try {
    console.log('开始加载PDF:', pdfId)
    const response = await pdfService.getOriginalPdfFile(pdfId)
    console.log('PDF响应状态:', response.status)
    console.log('PDF响应头:', response.headers)
    console.log('PDF响应数据类型:', typeof response.data)

    // 检查响应数据
    if (!response.data) {
      throw new Error('未收到PDF数据')
    }

    // 创建Blob URL
    const blob = new Blob([response.data], { type: 'application/pdf' })
    console.log('创建的Blob大小:', blob.size)

    if (blob.size === 0) {
      throw new Error('PDF数据为空')
    }

    pdfUrl.value = URL.createObjectURL(blob)
    console.log('PDF URL创建成功:', pdfUrl.value)
  } catch (error) {
    console.error('加载PDF失败:', error)
    loadError.value = true
    errorMessage.value = error.message || '未知错误'
    pdfUrl.value = ''
  } finally {
    loading.value = false
  }
}

// 监听PDF属性的变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    // 确保使用正确的PDF ID调用后端接口
    console.log('正在调用后端接口获取PDF文件:', newPdf.id)
    loadPdfContent(newPdf.id)
  } else {
    // 清理资源
    if (pdfUrl.value) {
      URL.revokeObjectURL(pdfUrl.value)
    }
    pdfUrl.value = ''
    loadError.value = false
    errorMessage.value = ''
  }
}, { immediate: true })

// 下载PDF
const downloadPdf = async () => {
  if (!props.pdf) return

  try {
    const response = await pdfService.getOriginalPdfFile(props.pdf.id)
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', props.pdf.name)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载PDF失败:', error)
    alert('下载PDF失败: ' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
/* 1. 最外层占满父级 */
.pdf-viewer {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: #ce4040;
}


.pdf-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

/* 2.pdf主体 */
.pdf-content {
  flex: 1 1 auto;
  overflow: auto;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

/* 3. 内嵌 PDF */
.pdf-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 4. 占位 / 提示 */
.pdf-placeholder,
.loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #666;
}
</style>
