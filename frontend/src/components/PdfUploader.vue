<template>
  <div class="upload-section section">
    <div v-if="!isUploading" class="button-container">
      <!-- 上传ZIP文件按钮 -->
      <label class="upload-btn">
        <input
            ref="fileInputRef"
            type="file"
            multiple
            accept=".zip"
            @change="handleFileSelect"
            style="display: none;"
        />
        <i class="upload-icon">📦</i>
        选择ZIP文件
      </label>

      <!-- ExtractAndSummary按钮 -->
      <button
          class="extract-summary-btn"
          :disabled="isProcessing"
          @click="handleExtractAndSummary"
      >
        <i class="process-icon" :class="{ 'spinning': isProcessing }">⚡</i>
        {{ isProcessing ? '处理中...' : 'ExtractAndSummary' }}
      </button>
    </div>

    <div v-else class="upload-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <p>上传中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { usePdfManager } from '@/composables/usePdfManager'
import { pdfService } from '@/services/pdfService'

const { isUploading, uploadProgress, handleZipUpload } = usePdfManager()
const fileInputRef = ref(null)
const isProcessing = ref(false)

const handleFileSelect = async (event) => {
  const files = event.target.files
  if (!files.length) return

  try {
    await handleZipUpload(files)
    // 上传成功后重置input
    event.target.value = ''
  } catch (error) {
    console.error('上传出错:', error)
    // 重置input
    event.target.value = ''
  }
}

// 处理ExtractAndSummary按钮点击事件
const handleExtractAndSummary = async () => {
  if (isProcessing.value) return

  isProcessing.value = true
  try {
    console.log('开始调用summarizeAllPdf接口...')
    const summaryResponse = await pdfService.summarizeAllPdf()
    console.log('summarizeAllPdf响应:', summaryResponse)

    console.log('开始调用extractAllPdfInfo接口...')
    const extractResponse = await pdfService.extractAllPdfInfo()
    console.log('extractAllPdfInfo响应:', extractResponse)

    // 显示成功消息（可以通过全局消息提示或弹窗）
    alert('PDF信息提取和摘要生成完成！')
  } catch (error) {
    console.error('处理失败:', error)
    alert('处理失败: ' + (error.message || '未知错误'))
  } finally {
    isProcessing.value = false
  }
}

// 公开方法，供父组件调用
defineExpose({
  fileInputRef
})
</script>

<style scoped>
.upload-section {
  padding: 10px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.button-container {
  display: flex;
  gap: 10px;
}

.upload-btn, .extract-summary-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  font-weight: 600;
  transition: background 0.3s ease;
  border: 2px dashed rgba(255, 255, 255, 0.5);
  background: transparent;
  font-size: 14px;
}

.upload-btn:hover:not(:disabled), .extract-summary-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
}

.upload-btn:disabled, .extract-summary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-icon, .process-icon {
  margin-right: 8px;
  font-size: 20px;
}

.process-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-progress {
  text-align: center;
  padding: 20px;
}

.progress-bar {
  width: 100%;
  height: 10px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 15px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  transition: width 0.3s;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .button-container {
    flex-direction: column;
  }
}
</style>
