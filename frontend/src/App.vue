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
      <div v-if="selectedPdfComputed" class="five-cards-layout">
        <div class="grid-container">
          <!-- 左上角卡片：宽40%，高10% -->
          <div class="card card-a">
            <h3>标签区域</h3>
          </div>

          <!-- 左中卡片：宽40%，高45% -->
          <div class="card card-b">
            <h3>信息区域</h3>
          </div>

          <!-- 左下卡片：宽40%，高45% -->
          <div class="card card-c">
            <h3>备注区域</h3>
          </div>

          <!-- 右上卡片：宽60%，高55% -->
          <div class="card card-d">
            <h3>表格区域</h3>
          </div>

          <!-- 右下卡片：宽60%，高45% -->
          <div class="card card-e">
            <h3>图片区域</h3>
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


<style>html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #ffffff;
}
</style>

<style scoped>* {
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
  background-color: #ffffff;
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

/* 五卡片布局样式 */
.five-cards-layout {
  height: 100%;
  position: relative;
  padding: 20px;
}

.grid-container {
  display: grid;
  grid-template-columns: 40% 60%;
  grid-template-rows: 10% 45% 45%;
  gap: 15px;
  height:  calc(100% - 30px);
  width: 100%;
}

.card {
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

/* 左上角卡片 - 标签区域 */
.card-a {
  grid-column: 1;
  grid-row: 1;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
}

/* 左中卡片 - 信息区域 */
.card-b {
  grid-column: 1;
  grid-row: 2;
  background: linear-gradient(135deg, #f1f8e9, #c8e6c9);
}

/* 左下卡片 - 备注区域 */
.card-c {
  grid-column: 1;
  grid-row: 3;
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
}

/* 右上卡片 - 表格区域 */
.card-d {
  grid-column: 2;
  grid-row: 1 / span 2;
  background: linear-gradient(135deg, #fce4ec, #f8bbd0);
}

/* 右下卡片 - 图片区域 */
.card-e {
  grid-column: 2;
  grid-row: 3;
  background: linear-gradient(135deg, #f3e5f5, #e1bee7);
}

.card h3 {
  color: #333;
  font-weight: 600;
  text-shadow: 1px 1px 1px rgba(0,0,0,0.1);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .grid-container {
    display: flex;
    flex-direction: column;
    height: auto;
  }

  .card {
    width: 100% !important;
    height: 150px !important;
    margin-bottom: 20px;
  }
}
</style>