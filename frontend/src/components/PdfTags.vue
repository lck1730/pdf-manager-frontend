<template>
  <div class="pdf-tags section">
    <h3>标签管理</h3>
    <div class="tags-content">
      <div class="existing-tags">
        <div v-if="tags.length > 0" class="tags-list">
          <span 
            v-for="tag in tags" 
            :key="tag" 
            class="tag-item"
          >
            {{ tag }}
            <button class="remove-tag-btn" @click="removeTag(tag)">×</button>
          </span>
        </div>
        <div v-else class="no-tags">暂无标签</div>
      </div>
      
      <div class="add-tag-section">
        <input 
          v-model="newTag" 
          type="text" 
          class="tag-input" 
          placeholder="输入新标签" 
          @keyup.enter="addTag"
        />
        <button class="add-tag-btn" @click="addTag">添加标签</button>
      </div>
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

const tags = ref([])
const newTag = ref('')

// 获取PDF标签
const fetchTags = async (pdfId) => {
  try {
    const response = await pdfService.getTagsByPdfId(pdfId)
    tags.value = response.data || []
  } catch (error) {
    console.error('获取标签失败:', error)
    tags.value = []
  }
}

// 添加标签
const addTag = async () => {
  if (!props.pdf || !newTag.value.trim()) return
  
  try {
    // 发送到后端添加标签
    await pdfService.addTag(props.pdf.id, newTag.value.trim())
    // 更新本地标签列表
    if (!tags.value.includes(newTag.value.trim())) {
      tags.value.push(newTag.value.trim())
    }
    newTag.value = ''
  } catch (error) {
    console.error('添加标签失败:', error)
    alert('添加标签失败: ' + (error.message || '未知错误'))
  }
}

// 删除标签
const removeTag = async (tag) => {
  if (!props.pdf) return
  
  try {
    // 发送到后端删除标签
    await pdfService.deleteTag(props.pdf.id, tag)
    // 更新本地标签列表
    tags.value = tags.value.filter(t => t !== tag)
  } catch (error) {
    console.error('删除标签失败:', error)
    alert('删除标签失败: ' + (error.message || '未知错误'))
  }
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchTags(newPdf.id)
  } else {
    tags.value = []
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-tags {
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pdf-tags h3 {
  margin-top: 0;
  color: #333;
}

.tags-content {
  margin-top: 15px;
}

.existing-tags {
  margin-bottom: 20px;
  min-height: 30px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: flex;
  align-items: center;
  background-color: #007bff;
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 14px;
}

.remove-tag-btn {
  background: none;
  border: none;
  color: white;
  font-size: 16px;
  cursor: pointer;
  margin-left: 6px;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-tag-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.no-tags {
  color: #999;
  font-style: italic;
}

.add-tag-section {
  display: flex;
  gap: 10px;
}

.tag-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
}

.add-tag-btn {
  padding: 8px 16px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.add-tag-btn:hover {
  background-color: #218838;
}
</style>