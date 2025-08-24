<script setup>
import { ref, onMounted } from 'vue'

// 上传相关
const isUploading = ref(false)
const uploadProgress = ref(0)

// Tag相关
const tags = ref([
  { id: 1, name: '全部', selected: true },
  { id: 2, name: '未标记', selected: false },
  { id: 3, name: '重要', selected: false },
  { id: 4, name: '待处理', selected: false }
])

// PDF列表相关
const pdfList = ref([
  { id: 1, name: '示例文档1.pdf', tags: ['重要'] },
  { id: 2, name: '示例文档2.pdf', tags: ['待处理'] },
  { id: 3, name: '示例文档3.pdf', tags: [] },
  { id: 4, name: '示例文档4.pdf', tags: ['重要', '待处理'] }
])

const selectedPdf = ref(null)
const fileInputRef = ref(null)
const zipFileInputRef = ref(null)

// 选择PDF
const selectPdf = (pdf) => {
  selectedPdf.value = pdf
}

// 切换Tag选择
const toggleTag = (tag) => {
  tags.value.forEach(t => {
    t.selected = t.id === tag.id
  })
}


// 处理压缩包上传
const handleZipUpload = async (event) => {
  const files = event.target.files
  if (!files.length) return

  // 准备上传数据
  const formData = new FormData()
  
  // 将所有ZIP文件添加到formData
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    // 检查文件是否为ZIP格式
    if (!file.name.toLowerCase().endsWith('.zip')) {
      console.error('请选择ZIP格式的文件')
      continue
    }
    formData.append('files', file)
  }

  // 执行上传
  try {
    isUploading.value = true
    uploadProgress.value = 0
    
    // 使用压缩包上传接口
    const response = await fetch('http://localhost:8080/api/files/upload-zip', {
      method: 'POST',
      body: formData
    })
    
    const result = await response.json()
    
    if (result.success) {
      console.log('上传成功:', result)
      // 这里可以更新PDF列表
    } else {
      console.error('上传失败:', result.message)
    }
  } catch (error) {
    console.error('上传出错:', error)
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
    // 重置input元素
    event.target.value = ''
  }
}
</script>

<template>
  <div class="pdf-manager">
    <!-- 左侧管理面板 (1/4宽度) -->
    <div class="sidebar">
      <!-- 上传按钮 -->
      <div class="upload-section section">
        <h3>文件上传</h3>
        <div class="upload-description">
          <p>选择包含PDF文件的ZIP压缩包进行上传</p>
        </div>
        
        <div v-if="!isUploading">
          <!-- ZIP压缩包上传 -->
          <label class="upload-btn">
            <input 
              ref="fileInputRef"
              type="file" 
              multiple 
              accept=".zip"
              @change="handleZipUpload" 
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

      <!-- Tag区域 -->
      <div class="tag-section section">
        <h3>标签筛选</h3>
        <div class="tag-list">
          <span 
            v-for="tag in tags" 
            :key="tag.id" 
            class="tag-item"
            :class="{ selected: tag.selected }"
            @click="toggleTag(tag)"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>

      <!-- PDF列表区域 -->
      <div class="pdf-list-section section">
        <h3>PDF文档列表</h3>
        <div class="pdf-list">
          <div 
            v-for="pdf in pdfList" 
            :key="pdf.id" 
            class="pdf-item"
            :class="{ selected: selectedPdf && selectedPdf.id === pdf.id }"
            @click="selectPdf(pdf)"
          >
            <div class="pdf-icon">📄</div>
            <div class="pdf-info">
              <div class="pdf-name">{{ pdf.name }}</div>
              <div class="pdf-tags">
                <span 
                  v-for="tag in pdf.tags" 
                  :key="tag" 
                  class="pdf-tag"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧内容展示区 (3/4宽度) -->
    <div class="content-area">
      <div v-if="selectedPdf" class="pdf-viewer">
        <div class="pdf-header">
          <h2>{{ selectedPdf.name }}</h2>
          <div class="pdf-actions">
            <button class="action-btn">下载</button>
            <button class="action-btn">分享</button>
          </div>
        </div>
        <div class="pdf-content">
          <div class="pdf-placeholder">
            <div class="pdf-preview">
              <div class="preview-page">
                <div class="preview-content">
                  PDF内容预览区域
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="placeholder">
        <div class="welcome-message">
          <div class="welcome-icon">📚</div>
          <h2>欢迎使用PDF管理系统</h2>
          <p>请选择左侧的PDF文档进行查看或上传新的ZIP压缩包</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #ffffff;
}

</style>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.pdf-manager {
  display: flex;
  height: 100vh;
  width: 100vw;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f8f9fa;
  color: #333;
  overflow: hidden;
}

.sidebar {
  width: 25%;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 3px 0 10px rgba(0, 0, 0, 0.1);
}

.content-area {
  width: 75%;
  padding: 20px;
  overflow-y: auto;
}

.section {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 600;
  font-size: 18px;
}

.upload-description {
  margin-bottom: 15px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  width: 100%;
  font-weight: 500;
  transition: background 0.3s ease;
  border: 1px dashed rgba(255, 255, 255, 0.5);
}

.upload-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.upload-icon {
  margin-right: 8px;
  font-size: 20px;
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

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 12px;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.tag-item:hover {
  background-color: rgba(255, 255, 255, 0.25);
}

.tag-item.selected {
  background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%);
  color: #000;
  font-weight: 500;
}

.pdf-list {
  max-height: calc(100vh - 350px);
  overflow-y: auto;
}

.pdf-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.pdf-item:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.pdf-item.selected {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

.pdf-icon {
  font-size: 20px;
  margin-right: 12px;
}

.pdf-info {
  flex: 1;
}

.pdf-name {
  font-weight: 500;
  margin-bottom: 5px;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pdf-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.pdf-tag {
  font-size: 11px;
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
}

.pdf-viewer {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #ffffff; /* 确保背景为白色 */
}

.pdf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.pdf-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.pdf-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: transform 0.2s;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.pdf-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff; /* 确保背景为白色 */
}

.pdf-placeholder {
  width: 100%;
  text-align: center;
  background-color: #ffffff; /* 确保背景为白色 */
}

.pdf-preview {
  display: flex;
  justify-content: center;
  background-color: #ffffff; /* 确保背景为白色 */
}

.preview-page {
  width: 70%;
  height: 600px;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff; /* 确保背景为白色 */
}

.preview-content {
  color: #666;
  font-size: 18px;
  font-weight: 500;
}

.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #ffffff; /* 确保背景为白色 */
}

.welcome-message {
  text-align: center;
  max-width: 500px;
  background-color: #ffffff; /* 确保背景为白色 */
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.welcome-message h2 {
  color: #333;
  margin-bottom: 15px;
  font-size: 28px;
}

.welcome-message p {
  color: #666;
  font-size: 18px;
  line-height: 1.6;
}

/* 滚动条样式 */
.pdf-list::-webkit-scrollbar {
  width: 6px;
}

.pdf-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.pdf-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 10px;
}

.pdf-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 隐藏滚动条但保持功能 */
.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.content-area::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content-area::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>