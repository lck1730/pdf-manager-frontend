<template>
  <div class="images-viewer-container">
    <div class="images-viewer section">
      <div v-if="images && images.length > 0" class="images-content">
        <div class="image-navigation" v-if="images.length > 1">
          <button :disabled="currentIndex <= 0" @click="prevImage">‹</button>
          <span>{{ currentIndex + 1 }} / {{ images.length }}</span>
          <button :disabled="currentIndex >= images.length - 1" @click="nextImage">›</button>
        </div>

        <div class="image-container">
          <div class="image-content">
            <!-- 图片标题区域 -->
            <div class="image-caption-section">
              <div class="image-caption">
                <h3>{{ currentImage.caption || '猪猪˃̶͈🐽˂̶͈ 这个图片标题不知道哦~' }}</h3>
              </div>
            </div>

            <!-- 图片主体区域 -->
            <div class="image-wrapper">
              <div class="image-scroll-container" ref="scrollContainer">
                <img
                    v-if="imageUrl"
                    :src="imageUrl"
                    alt="PDF图片"
                    class="pdf-image"
                    @load="onImageLoad"
                />
                <div v-else class="image-loading">图片加载中...</div>
              </div>
            </div>

            <!-- 图片脚注区域 -->
            <div class="image-footnote-section">
              <div class="footnote-content">
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

                <!-- 删除按钮 -->
                <button
                    class="delete-image-btn"
                    @click="deleteImage"
                    title="删除图片"
                >
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="no-images">
        <p>该PDF中未找到图片</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onUnmounted } from 'vue'
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
const scrollContainer = ref(null)

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
      const firstImage = images.value[0]

      // 检查图片名称字段，支持多种命名方式
      const imageName = firstImage?.imageName || firstImage?.name || firstImage?.image_name

      if (imageName && typeof imageName === 'string' && imageName.trim() !== '') {
        loadImage(imageName)
      } else {
        console.warn('第一张图片的名称无效:', firstImage)
        imageUrl.value = ''
      }
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
    // 验证图片名称是否有效
    if (!imageName || imageName === 'undefined' || !imageName.trim()) {
      console.error('图片名称无效:', imageName)
      imageUrl.value = ''
      return
    }

    const response = await pdfService.getOriginalImageFile(imageName)
    const blob = new Blob([response.data], { type: 'image/jpeg' })
    imageUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('加载图片失败:', error)
    imageUrl.value = ''
  }
}

// 图片加载完成后的处理
const onImageLoad = () => {
  // 图片加载完成后，确保滚动容器正确显示
  if (scrollContainer.value) {
    // 重置滚动位置到左上角
    scrollContainer.value.scrollLeft = 0
    scrollContainer.value.scrollTop = 0
  }
}

// 上一张图片
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    const image = images.value[currentIndex.value]

    // 检查图片名称字段
    const imageName = image?.imageName || image?.name || image?.image_name

    if (imageName && typeof imageName === 'string' && imageName.trim() !== '') {
      loadImage(imageName)
    } else {
      console.warn('图片名称无效:', image)
      imageUrl.value = ''
    }
  }
}

// 下一张图片
const nextImage = () => {
  if (currentIndex.value < images.value.length - 1) {
    currentIndex.value++
    const image = images.value[currentIndex.value]

    // 检查图片名称字段
    const imageName = image?.imageName || image?.name || image?.image_name

    if (imageName && typeof imageName === 'string' && imageName.trim() !== '') {
      loadImage(imageName)
    } else {
      console.warn('图片名称无效:', image)
      imageUrl.value = ''
    }
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
    // 使用正确的字段名（imageName）
    await pdfService.updateImageFootnote(currentImage.value.imageName, editingFootnote.value)
    currentImage.value.footnote = editingFootnote.value
    isEditingFootnote.value = false
  } catch (error) {
    console.error('保存脚注失败:', error)
    alert('保存脚注失败: ' + (error.message || '未知错误'))
  }
}

// 删除图片
const deleteImage = async () => {
  if (!currentImage.value) return

  // 确认删除
  const confirmDelete = confirm(`确定要删除图片 "${currentImage.value.imageName}" 吗？`)
  if (!confirmDelete) return

  try {
    // 调用删除接口
    await pdfService.deleteImageAndFile(currentImage.value.imageName)

    // 从图片列表中移除
    images.value.splice(currentIndex.value, 1)

    // 如果删除的是最后一张图片，调整索引
    if (currentIndex.value >= images.value.length && images.value.length > 0) {
      currentIndex.value = images.value.length - 1
    }

    // 如果还有图片，加载新当前图片
    if (images.value.length > 0) {
      const image = images.value[currentIndex.value]
      const imageName = image?.imageName || image?.name || image?.image_name

      if (imageName && typeof imageName === 'string' && imageName.trim() !== '') {
        loadImage(imageName)
      } else {
        console.warn('图片名称无效:', image)
        imageUrl.value = ''
      }
    } else {
      // 如果没有图片了，清空显示
      imageUrl.value = ''
    }

    alert('图片删除成功')
  } catch (error) {
    console.error('删除图片失败:', error)
    alert('删除图片失败: ' + (error.message || '未知错误'))
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

<style scoped>
.images-viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.images-viewer {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 15px;
  box-sizing: border-box;
}

.images-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.image-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  flex-shrink: 0;
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
  flex: 1;
  display: flex;
  flex-direction: column;
}

.image-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  overflow: hidden;
}

.image-caption-section {
  background-color: #e9ecef;
  border-bottom: 1px solid #dee2e6;
}

.image-caption {
  padding: 15px;
  text-align: center;
}

.image-caption h3 {
  font-size: 16px;
  margin: 0;
  color: #495057;
}

.image-wrapper {
  flex: 1;
  overflow: hidden;
  padding: 0; /* 移除padding */
  background-color: #ffffff;
}

.image-scroll-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  padding: 18px; /* 将padding移到这里 */
  box-sizing: border-box;
}

.pdf-image {
  max-width: none;
  max-height: none;
  object-fit: contain;
  display: block;
  flex-shrink: 0;
}

.image-loading {
  color: #6c757d;
  font-style: italic;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
}

.image-footnote-section {
  padding: 15px;
  background-color: #e9ecef;
  border-top: 1px solid #dee2e6;
}

.footnote-content {
  font-size: 14px;
  color: #495057;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.footnote-content strong {
  margin-right: 5px;
}

.footnote-input {
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  flex: 1;
  min-width: 150px;
}

.edit-footnote-btn,
.delete-image-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.edit-footnote-btn {
  background-color: #007bff;
  color: white;
}

.delete-image-btn {
  background-color: #dc3545;
  color: white;
}

.delete-image-btn:hover {
  background-color: #c82333;
}

.no-images {
  text-align: center;
  padding: 30px;
  color: #6c757d;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
