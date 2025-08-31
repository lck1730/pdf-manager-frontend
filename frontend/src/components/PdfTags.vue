<template>
  <div class="pdf-tags section">
    <h3>PDF标签</h3>
    <div class="tags-content">
      <div class="tag-list">
        <span 
          v-for="tag in tags" 
          :key="tag" 
          class="tag-item"
          @click="removeTag(tag)"
        >
          {{ tag }} ×
        </span>
        <div class="add-tag-wrapper">
          <input 
            v-model="newTag" 
            type="text" 
            class="tag-input" 
            placeholder="添加标签..." 
            @keyup.enter="addTag"
          />
          <button class="add-tag-btn" @click="addTag">+</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { tagService } from '@/services/tagService'

const props = defineProps({
  pdf: {
    type: Object,
    default: null
  }
})

const tags = ref([])
const newTag = ref('')

// 获取PDF标签
const fetchPdfTags = async (pdfId) => {
  try {
    const response = await tagService.getTagsByPdfId(pdfId)
    tags.value = response.data || []
  } catch (error) {
    console.error('获取PDF标签失败:', error)
    tags.value = []
  }
}

// 添加标签
const addTag = async () => {
  if (!newTag.value.trim() || !props.pdf) return
  
  try {
    await tagService.addTagToPdf(props.pdf.id, newTag.value.trim())
    tags.value.push(newTag.value.trim())
    newTag.value = ''
  } catch (error) {
    console.error('添加标签失败:', error)
    alert('添加标签失败: ' + (error.message || '未知错误'))
  }
}

// 删除标签
const removeTag = async (tagToRemove) => {
  if (!props.pdf) return
  
  try {
    await tagService.removeTagFromPdf(props.pdf.id, tagToRemove)
    tags.value = tags.value.filter(tag => tag !== tagToRemove)
  } catch (error) {
    console.error('删除标签失败:', error)
    alert('删除标签失败: ' + (error.message || '未知错误'))
  }
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchPdfTags(newPdf.id)
  } else {
    tags.value = []
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-tags {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pdf-tags h3 {
  margin-top: 0;
  color: #333;
  flex-shrink: 0;
}

.tags-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  overflow-y: auto;
  flex: 1;
}

.tag-list::-webkit-scrollbar {
  width: 6px;
}

.tag-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.tag-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.tag-list::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.tag-item {
  padding: 6px 12px;
  background-color: #e0e7ff;
  color: #4f46e5;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tag-item:hover {
  background-color: #c7d2fe;
  transform: translateY(-2px);
}

.add-tag-wrapper {
  display: flex;
  margin-top: 10px;
  gap: 5px;
  flex-shrink: 0;
}

.tag-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

.tag-input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.add-tag-btn {
  padding: 8px 15px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.add-tag-btn:hover {
  background-color: #4338ca;
}
</style>