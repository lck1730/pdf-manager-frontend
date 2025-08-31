<template>
  <div class="images-viewer section">
    <div v-if="images && images.length > 0" class="images-content">
      <div class="image-navigation" v-if="images.length > 1">
        <button :disabled="currentIndex <= 0" @click="prevImage">‹</button>
        <span>{{ currentIndex + 1 }} / {{ images.length }}</span>
        <button :disabled="currentIndex >= images.length - 1" @click="nextImage">›</button>
      </div>
      
      <div class="image-container">
        <div class="image-info">
          <div class="image-name">图片名称: {{ currentImage.name }}</div>
          <div class="image-footnote">
            <strong>脚注:</strong> 
            <span v-if="!isEditingFootnote">{{ currentImage.footnote || '无' }}</span>
            <input 
              v-else 
              v-model="editingFootnote" 
              class="footnote-input"
              @keyup.enter="saveFootnote"
              @blur="saveFootnote"
            />
            <button 
              v-if="!isEditingFootnote" 
              class="edit-footnote-btn" 
              @click="editFootnote"
            >
              编辑
            </button>
          </div>
        </div>
        
        <div class="image-wrapper">
          <img 
            v-if="imageUrl" 
            :src="imageUrl" 
            alt="PDF图片" 
            class="pdf-image"
          />
          <div v-else class="image-loading">图片加载中...</div>
        </div>
      </div>
    </div>
    <div v-else class="no-images">
      <p>该PDF中未找到图片</p>
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

const images = ref([])
const currentIndex = ref(0)
const imageUrl = ref('')
const isEditingFootnote = ref(false)
const editingFootnote = ref('')

// 当前图片
const currentImage = computed(() => {
  return images.value.length > 0 ? images.value[currentIndex.value] : null
})

// 获取图片数据
const fetchImages = async (pdfId) => {
  try {
    const response = await pdfService.getImagesByPdfId(pdfId)
    images.value = response.data || []
    currentIndex.value = 0
    
    // 加载第一张图片
    if (images.value.length > 0) {
      loadImage(images.value[0].name)
    } else {
      imageUrl.value = ''
    }
  } catch (error) {
    console.error('获取图片信息失败:', error)
    images.value = []
    currentIndex.value = 0
    imageUrl.value = ''
  }
}

// 加载图片
const loadImage = async (imageName) => {
  try {
    const response = await pdfService.getOriginalImageFile(imageName)
    const blob = new Blob([response.data], { type: 'image/jpeg' })
    imageUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('加载图片失败:', error)
    imageUrl.value = ''
  }
}

// 上一张图片
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    loadImage(images.value[currentIndex.value].name)
  }
}

// 下一张图片
const nextImage = () => {
  if (currentIndex.value < images.value.length - 1) {
    currentIndex.value++
    loadImage(images.value[currentIndex.value].name)
  }
}

// 编辑脚注
const editFootnote = () => {
  editingFootnote.value = currentImage.value.footnote || ''
  isEditingFootnote.value = true
}

// 保存脚注
const saveFootnote = async () => {
  if (!currentImage.value) return
  
  try {
    await pdfService.updateImageFootnote(currentImage.value.name, editingFootnote.value)
    currentImage.value.footnote = editingFootnote.value
    isEditingFootnote.value = false
  } catch (error) {
    console.error('保存脚注失败:', error)
    alert('保存脚注失败: ' + (error.message || '未知错误'))
  }
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchImages(newPdf.id)
  } else {
    images.value = []
    currentIndex.value = 0
    imageUrl.value = ''
  }
}, { immediate: true })

// 清理图片URL
onUnmounted(() => {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
  }
})
</script>

<script>
import { computed, onUnmounted } from 'vue'
export default {
  computed: {
    currentImage() {
      return this.images.length > 0 ? this.images[this.currentIndex] : null
    }
  },
  beforeUnmount() {
    if (this.imageUrl) {
      URL.revokeObjectURL(this.imageUrl)
    }
  }
}
</script>

<style scoped>
.images-viewer {
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.images-content {
  margin-top: 15px;
}

.image-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.image-navigation button {
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-navigation button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.image-container {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.image-info {
  padding: 10px;
  background-color: #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.image-name {
  font-weight: 500;
}

.image-footnote {
  display: flex;
  align-items: center;
  gap: 10px;
}

.footnote-input {
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.edit-footnote-btn {
  padding: 4px 8px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.image-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background-color: #f8f9fa;
}

.pdf-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.image-loading {
  color: #6c757d;
  font-style: italic;
}

.no-images {
  text-align: center;
  padding: 30px;
  color: #6c757d;
}
</style>