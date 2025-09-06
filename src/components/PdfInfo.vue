<template>
  <div class="pdf-info-container">
    <div class="pdf-info section">
      <div class="info-content">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">加载失败: {{ error }}</div>
        <div v-else class="info-details">
          <div class="info-item">
            <div v-if="info" class="markdown-content" v-html="renderedMarkdown"></div>
            <div v-else class="no-info">无信息</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { marked } from 'marked'
import { pdfService } from '@/services/pdfService'

const props = defineProps({
  pdf: {
    type: Object,
    default: null
  }
})

const info = ref('')
const loading = ref(false)
const error = ref('')

// 渲染Markdown内容
const renderedMarkdown = computed(() => {
  if (!info.value) return ''
  return marked.parse(info.value)
})

// 获取PDF信息
const fetchPdfInfo = async (pdfId) => {
  loading.value = true
  error.value = ''

  try {
    const response = await pdfService.getPdfInfoAndNote(pdfId)
    // 根据你的说明，响应数据是 ["info内容", "note内容"] 的数组
    // 我们只需要第一个元素作为info
    if (Array.isArray(response.data) && response.data.length >= 1) {
      info.value = response.data[0] || ''
    } else {
      info.value = ''
    }
  } catch (err) {
    console.error('获取PDF信息失败:', err)
    error.value = err.message || '未知错误'
    info.value = ''
  } finally {
    loading.value = false
  }
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchPdfInfo(newPdf.id)
  } else {
    info.value = ''
    loading.value = false
    error.value = ''
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-info-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.pdf-info {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 15px;
  box-sizing: border-box;
}

.info-content {
  flex: 1;
  overflow-y: auto;
  text-align: left; /* 确保文本左对齐 */
}

.loading, .error {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 100%;
  font-size: 16px;
  text-align: left;
}

.error {
  color: #f56c6c;
}

.info-details {
  display: block;
  text-align: left;
}

.info-item {
  display: block;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  text-align: left;
}

.no-info {
  color: #6c757d;
  font-style: italic;
  text-align: left;
  padding: 20px;
}

.markdown-content {
  line-height: 1.6;
  text-align: left;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  text-align: left;
}

.markdown-content :deep(p) {
  text-align: left;
  margin: 1em 0;
}

.markdown-content :deep(*) {
  text-align: left;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 1em 0;
  padding-left: 40px;
}

.markdown-content :deep(li) {
  margin: 0.5em 0;
}

.markdown-content :deep(a) {
  color: #007bff;
  text-decoration: underline;
}

.markdown-content :deep(a:hover) {
  color: #0056b3;
}

.markdown-content :deep(code) {
  background-color: #e9ecef;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.markdown-content :deep(pre) {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 1em 0;
  text-align: left;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #007bff;
  padding-left: 16px;
  margin: 1em 0;
  color: #6c757d;
  text-align: left;
}

.info-content::-webkit-scrollbar {
  width: 6px;
}

.info-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.info-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.info-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
