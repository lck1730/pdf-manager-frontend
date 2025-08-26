<template>
  <div class="upload-section section">
    <div v-if="!isUploading">
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

const { isUploading, uploadProgress, handleZipUpload } = usePdfManager()
const fileInputRef = ref(null)

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


.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  width: 95%;
  font-weight: 600;
  transition: background 0.3s ease;
  border: 2px dashed rgba(255, 255, 255, 0.5);
  margin: 0 auto;
}

.upload-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.upload-icon {
  margin-right: 8px;
  font-size: 24px;
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
</style>