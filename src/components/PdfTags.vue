<template>
  <div class="pdf-tags-container">
    <div class="pdf-tags section">
      <div class="tags-layout">
        <!-- 左侧：新建标签按钮 -->
        <div class="add-tag-section">
          <button class="add-tag-button" @click="showAddTagInput">
            +
          </button>
        </div>

        <!-- 右侧：标签展示区 -->
        <div class="tags-display-section">
          <div class="tags-scroll-container">
            <div v-if="tags.length === 0" class="no-tags">
              暂无标签
            </div>
            <div v-else class="tag-list">
              <div
                  v-for="tag in tags"
                  :key="tag"
                  class="tag-item"
              >
                <span class="tag-text">{{ tag }}</span>
                <span class="tag-remove" @click="removeTag(tag)">×</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入标签的弹窗 -->
      <div v-if="isAddingTag" class="add-tag-overlay" @click="hideAddTagInput">
        <div class="add-tag-modal" @click.stop>
          <h3>添加标签</h3>
          <div class="add-tag-form">
            <input
                v-model="newTag"
                type="text"
                class="tag-input"
                placeholder="输入标签名称..."
                @keyup.enter="addTag"
                ref="tagInput"
            />
            <div class="form-buttons">
              <button class="cancel-btn" @click="hideAddTagInput">取消</button>
              <button class="confirm-btn" @click="addTag" :disabled="!newTag.trim()">添加</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { tagService } from '@/services/tagService'

const props = defineProps({
  pdf: {
    type: Object,
    default: null
  }
})

const tags = ref([])
const newTag = ref('')
const isAddingTag = ref(false)
const tagInput = ref(null)

// 获取PDF标签
const fetchPdfTags = async (pdfId) => {
  try {
    const response = await tagService.getTagsByPdfId(pdfId)
    // 根据后端返回的数据结构调整，Tag对象应该有tag属性
    tags.value = response.data.map(tagObj => tagObj.tag) || []
  } catch (error) {
    console.error('获取PDF标签失败:', error)
    tags.value = []
  }
}

// 显示添加标签输入框
const showAddTagInput = () => {
  isAddingTag.value = true
  nextTick(() => {
    tagInput.value?.focus()
  })
}

// 隐藏添加标签输入框
const hideAddTagInput = () => {
  isAddingTag.value = false
  newTag.value = ''
}

// 添加标签
const addTag = async () => {
  if (!newTag.value.trim() || !props.pdf) return

  try {
    // 获取PDF ID和文件名，支持多种可能的字段名
    const pdfId = props.pdf.id || props.pdf.pdfId
    const fileName = props.pdf.filename || props.pdf.fileName || props.pdf.name || props.pdf.pdfName

    // 构造符合后端要求的数据结构
    const tagData = {
      tag: newTag.value.trim(),
      pdfId: pdfId,
      fileName: fileName
    }

    // 验证参数完整性
    if (!tagData.tag) {
      throw new Error('标签名称不能为空')
    }

    if (!tagData.pdfId) {
      throw new Error('PDF ID不能为空')
    }

    if (!tagData.fileName) {
      throw new Error('文件名不能为空')
    }

    await tagService.createTag(tagData)
    tags.value.push(newTag.value.trim())
    hideAddTagInput()
  } catch (error) {
    console.error('添加标签失败:', error)
    alert('添加标签失败: ' + (error.message || '未知错误'))
  }
}

// 删除标签
const removeTag = async (tagToRemove) => {
  if (!props.pdf) {
    console.error('PDF对象为空')
    alert('无法删除标签：未选择PDF文档')
    return
  }

  // 获取PDF ID，支持多种可能的字段名
  const pdfId = props.pdf.id || props.pdf.pdfId

  if (!pdfId) {
    console.error('PDF ID为空，当前PDF对象:', props.pdf)
    alert('无法删除标签：PDF ID为空')
    return
  }

  if (!tagToRemove) {
    console.error('标签内容为空')
    alert('无法删除标签：标签内容为空')
    return
  }

  try {
    console.log('删除标签参数:', { pdfId, tag: tagToRemove })
    await tagService.deleteTag(pdfId, tagToRemove)
    tags.value = tags.value.filter(tag => tag !== tagToRemove)
  } catch (error) {
    console.error('删除标签失败:', error)

    // 更详细的错误提示
    if (error.response && error.response.status === 500) {
      alert('服务器内部错误，请检查后端日志')
    } else if (error.response && error.response.status === 400) {
      alert('请求参数错误：' + (error.response.data || '未知错误'))
    } else {
      alert('删除标签失败: ' + (error.message || '未知错误'))
    }
  }
}


// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    console.log('当前选中的PDF:', newPdf)
    fetchPdfTags(newPdf.id)
  } else {
    tags.value = []
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-tags-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.pdf-tags {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 15px;
  box-sizing: border-box;
  position: relative;
}

.tags-layout {
  display: flex;
  height: 100%;
  gap: 15px;
}

.add-tag-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.add-tag-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4f46e5;
  color: white;
  border: none;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.add-tag-button:hover {
  background-color: #4338ca;
  transform: scale(1.05);
}

.tags-display-section {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
}

.tags-scroll-container {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  padding: 5px 0;
}

.tags-scroll-container::-webkit-scrollbar {
  height: 6px;
}

.tags-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.tags-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.tags-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.no-tags {
  color: #6b7280;
  font-style: italic;
  padding: 5px 0;
}

.tag-list {
  display: inline-block;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background-color: #e0e7ff;
  color: #4f46e5;
  border-radius: 20px;
  font-size: 14px;
  margin-right: 8px;
  position: relative;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background-color: #c7d2fe;
}

.tag-text {
  margin-right: 8px;
}

.tag-remove {
  cursor: pointer;
  font-weight: bold;
  color: #ef4444;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.tag-remove:hover {
  background-color: #fee2e2;
}

/* 添加标签弹窗样式 */
.add-tag-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.add-tag-modal {
  background: white;
  border-radius: 12px;
  padding: 20px;
  width: 300px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.add-tag-modal h3 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: center;
}

.add-tag-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tag-input {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.tag-input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-buttons {
  display: flex;
  gap: 10px;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.cancel-btn {
  background-color: #f3f4f6;
  color: #374151;
}

.cancel-btn:hover {
  background-color: #e5e7eb;
}

.confirm-btn {
  background-color: #4f46e5;
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background-color: #4338ca;
}

.confirm-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}
</style>
