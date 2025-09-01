<script setup>
import { onMounted, computed, ref } from 'vue'
import { usePdfManager } from '@/composables/usePdfManager'
import PdfUploader from '@/components/PdfUploader.vue'
import TagFilter from '@/components/TagFilter.vue'
import PdfList from '@/components/PdfList.vue'
import PdfViewer from '@/components/PdfViewer.vue'
import PdfInfo from '@/components/PdfInfo.vue'
import PdfNote from '@/components/PdfNote.vue'
import PdfTags from '@/components/PdfTags.vue'
import TablesViewer from '@/components/TablesViewer.vue'
import ImagesViewer from '@/components/ImagesViewer.vue'

const {
  pdfList,
  fetchPdfList,
  selectPdf,
  selectedPdf
} = usePdfManager()

const selectedPdfComputed = computed(() => selectedPdf.value)

// 添加视图模式切换状态，默认为 'tables'
const viewMode = ref('tables') // 'pdf'、'images' 或 'tables'

// 切换视图模式
const toggleViewMode = (mode) => {
  viewMode.value = mode
}

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

      <!-- 视图切换按钮，调整顺序为 tables、images、pdf -->
      <div class="view-toggle">
        <button
            :class="{ active: viewMode === 'tables' }"
            @click="toggleViewMode('tables')"
        >
          Tables
        </button>
        <button
            :class="{ active: viewMode === 'images' }"
            @click="toggleViewMode('images')"
        >
          Images
        </button>
        <button
            :class="{ active: viewMode === 'pdf' }"
            @click="toggleViewMode('pdf')"
        >
          PDF
        </button>
      </div>

      <!-- PDF列表区域 -->
      <PdfList
          :pdf-list="pdfList"
          :selected-pdf="selectedPdf"
          @select-pdf="selectPdf"
      />
    </div>

    <!-- 右侧内容展示区 (3/4宽度) -->
    <div class="content-area">
      <div v-if="selectedPdfComputed" class="three-cards-layout">
        <!-- 左上角卡片：宽40%，高10% -->
        <div class="card card-a">
          <PdfTags :pdf="selectedPdfComputed" />
        </div>

        <!-- 左中卡片：宽40%，高45% -->
        <div class="card card-b">
          <PdfInfo :pdf="selectedPdfComputed" />
        </div>

        <!-- 左下卡片：宽40%，高45% -->
        <div class="card card-c">
          <PdfNote :pdf="selectedPdfComputed" />
        </div>

        <!-- 右侧区域根据视图模式切换，调整顺序 -->
        <template v-if="viewMode === 'tables'">
          <div class="card card-view">
            <TablesViewer :pdf="selectedPdfComputed" />
          </div>
        </template>

        <template v-else-if="viewMode === 'images'">
          <div class="card card-view">
            <ImagesViewer :pdf="selectedPdfComputed" />
          </div>
        </template>

        <template v-else-if="viewMode === 'pdf'">
          <div class="card card-view">
            <PdfViewer :pdf="selectedPdfComputed" />
          </div>
        </template>
      </div>
      <div v-else class="placeholder">
        <div class="welcome-message">
          <div class="welcome-icon">📚</div>
          <h2>欢迎鸭鸭🐷使用PDF管理系统</h2>
          <p>请猪猪🐷选择左侧的PDF文档进行查看或上传新的压缩包哦~</p>
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
  background-color: #ffffff;
  position: relative;
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

/* 视图切换按钮样式 */
.view-toggle {
  display: flex;
  margin: 10px 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.view-toggle button {
  flex: 1;
  padding: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-toggle button:not(:last-child) {
  border-right: 1px solid rgba(255, 255, 255, 0.3);
}

.view-toggle button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.view-toggle button.active {
  background: white;
  color: #2575fc;
}

/* 三卡片布局样式 */
.three-cards-layout {
  display: grid;
  grid-template-columns: 40% 60%;
  grid-template-rows: 1fr 4.5fr 4.5fr; /* 使用fr单位更好地分配空间 */
  gap: 15px;
  height: 100%;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 确保内容不会溢出卡片边界 */
}

/* 左上角卡片 - 标签区域 */
.card-a {
  grid-column: 1;
  grid-row: 1;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  padding: 0; /* 减少padding以适应内容 */
}
.card-a :deep(.pdf-tags-container) {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}
/* 左中卡片 - 信息区域 */
.card-b {
  grid-column: 1;
  grid-row: 2;
  background: linear-gradient(135deg, #f1f8e9, #c8e6c9);
  padding: 0;
}

/* 左下卡片 - 备注区域 */
.card-c {
  grid-column: 1;
  grid-row: 3;
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  padding: 0;
}

/* 右侧视图区域 */
.card-view {
  grid-column: 2;
  grid-row: 1 / span 3;
  padding: 0;
}

/* 确保组件正确适配容器 */
.card-b :deep(.pdf-info-container),
.card-c :deep(.pdf-note-container) {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.card-view :deep(.pdf-viewer-container),
.card-view :deep(.tables-viewer-container),
.card-view :deep(.images-viewer-container) {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .three-cards-layout {
    display: flex;
    flex-direction: column;
    height: auto;
    padding: 10px;
    gap: 10px;
  }

  .card {
    width: 100% !important;
    min-height: 150px;
    margin-bottom: 10px;
    padding: 15px;
  }
}
</style>
