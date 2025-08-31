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
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pdf-info h3 {
  margin-top: 0;
  color: #333;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.info-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #eee;
  overflow-y: auto;
  flex: 1;
}

.info-content pre::-webkit-scrollbar {
  width: 6px;
}

.info-content pre::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.info-content pre::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.info-content pre::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.no-info {
  color: #999;
  font-style: italic;
  text-align: center;
  margin: 20px 0;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>