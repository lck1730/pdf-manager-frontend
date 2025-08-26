<script setup>
import { onMounted, computed, ref } from 'vue'
import { usePdfManager } from '@/composables/usePdfManager'
import PdfUploader from '@/components/PdfUploader.vue'
import TagFilter from '@/components/TagFilter.vue'
import PdfList from '@/components/PdfList.vue'
import PdfViewer from '@/components/PdfViewer.vue'

const { 
  pdfList,
  fetchPdfList,
  selectPdf,
  selectedPdf
} = usePdfManager()

const selectedPdfComputed = computed(() => selectedPdf.value)

// 页面加载时触发
onMounted(() => {
  const currentUser = 'yaya'
  fetchPdfList(currentUser) // 调用获取PDF列表的方法
})
</script>

<template>
  <div class="pdf-manager">
    <!-- 左侧管理面板 (1/4宽度) -->
    <div class="sidebar">
      <!-- 上传按钮 -->
      <PdfUploader />
      
      <!-- Tag区域 -->
      <TagFilter />
      
      <!-- PDF列表区域 -->
      <PdfList 
        :pdf-list="pdfList"
        :selected-pdf="selectedPdf"
        @select-pdf="selectPdf"
      />
    </div>

    <!-- 右侧内容展示区 (3/4宽度) -->
    <div class="content-area">
      <PdfViewer v-if="selectedPdfComputed" :pdf="selectedPdfComputed" />
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
  height: 100%;
  width: 100%;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f8f9fa;
  color: #333;
  overflow: hidden;
}

.sidebar {
  width: 25%;
  background: linear-gradient(135deg, #8945d4 0%, #2575fc 100%);
  color: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 3px 0 10px rgba(0, 0, 0, 0.1);
}

.content-area {
  width: 75%;
  overflow: hidden;
  background-color: #5d8630;
}

.section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 600;
  font-size: 18px;
}

.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #ffffff;
}

.welcome-message {
  text-align: center;
  max-width: 500px;
  background-color: #ffffff;
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