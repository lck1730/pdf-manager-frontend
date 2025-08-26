<template>
  <div class="pdf-info section">
    <h3>PDF信息</h3>
    <div class="info-content">
      <pre v-if="info">{{ info }}</pre>
      <p v-else class="no-info">暂无信息</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { pdfService } from '@/services/pdfService'

const props = defineProps({
  pdf: {
    type: Object,
    default: null
  }
})

const info = ref('')

// 获取PDF信息
const fetchPdfInfo = async (pdfId) => {
  try {
    const response = await pdfService.getPdfInfoAndNote(pdfId)
    info.value = response.data.info || ''
  } catch (error) {
    console.error('获取PDF信息失败:', error)
    info.value = ''
  }
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchPdfInfo(newPdf.id)
  } else {
    info.value = ''
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-info {
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pdf-info h3 {
  margin-top: 0;
  color: #333;
}

.info-content {
  min-height: 100px;
}

.info-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #eee;
  max-height: 300px;
  overflow-y: auto;
}

.no-info {
  color: #999;
  font-style: italic;
  text-align: center;
  margin: 20px 0;
}
</style>