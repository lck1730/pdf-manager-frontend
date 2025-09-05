<template>
  <div class="pdf-note-container">
    <div class="pdf-note section">
      <h3>雅雅🐷的笔记~</h3>
      <div class="note-content">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">加载失败: {{ error }}</div>
        <div v-else>
          <div v-if="!isEditing" class="note-display-wrapper">
            <div class="note-display">
              <pre v-if="note">{{ note }}</pre>
              <p v-else class="no-note">暂无备注</p>
            </div>
            <button class="edit-btn" @click="startEdit">编辑</button>
          </div>
          <div v-else class="note-editor-wrapper">
            <textarea
                v-model="editingNote"
                class="note-editor"
                placeholder="请输入备注内容"
            ></textarea>
            <div class="note-actions">
              <button class="save-btn" @click="saveNote">保存</button>
              <button class="cancel-btn" @click="cancelEdit">取消</button>
            </div>
          </div>
        </div>
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

const note = ref('')
const isEditing = ref(false)
const editingNote = ref('')
const loading = ref(false)
const error = ref('')

// 获取PDF备注
const fetchPdfNote = async (pdfId) => {
  loading.value = true
  error.value = ''

  try {
    const response = await pdfService.getPdfInfoAndNote(pdfId)
    // 根据你的说明，响应数据是 ["info内容", "note内容"] 的数组
    // 我们只需要第二个元素作为note
    if (Array.isArray(response.data) && response.data.length >= 2) {
      note.value = response.data[1] || ''
    } else {
      note.value = ''
    }
  } catch (err) {
    console.error('获取PDF备注失败:', err)
    error.value = err.message || '未知错误'
    note.value = ''
  } finally {
    loading.value = false
  }
}

// 开始编辑
const startEdit = () => {
  editingNote.value = note.value
  isEditing.value = true
}

// 保存备注
const saveNote = async () => {
  if (!props.pdf) return

  try {
    await pdfService.updatePdfNote(props.pdf.id, editingNote.value)
    note.value = editingNote.value
    isEditing.value = false
  } catch (error) {
    console.error('保存备注失败:', error)
    alert('保存失败: ' + (error.message || '未知错误'))
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  editingNote.value = ''
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchPdfNote(newPdf.id)
  } else {
    note.value = ''
    loading.value = false
    error.value = ''
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-note-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.pdf-note {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
}

.pdf-note h3 {
  margin-top: 0;
  color: #333;
  flex-shrink: 0;
  text-align: left;
}

.note-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  text-align: left;
}

.loading, .error {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 100%;
  font-size: 16px;
  text-align: left;
}

.error {
  color: #f56c6c;
}

.note-display-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  text-align: left;
}

.note-display {
  flex: 1;
  overflow: hidden;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  text-align: left;
}

.note-display pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #eee;
  overflow-y: auto;
  flex: 1;
  text-align: left;
}

.note-display pre::-webkit-scrollbar {
  width: 6px;
}

.note-display pre::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.note-display pre::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.note-display pre::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.note-editor-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  text-align: left;
}

.note-editor {
  width: 100%;
  flex: 1;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  font-family: inherit;
  resize: none;
  box-sizing: border-box;
  margin-bottom: 10px;
  text-align: left;
}

.note-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.edit-btn, .save-btn, .cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.edit-btn {
  background-color: #007bff;
  color: white;
}

.save-btn {
  background-color: #28a745;
  color: white;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.no-note {
  color: #999;
  font-style: italic;
  text-align: left; /* 修改为左对齐 */
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start; /* 修改为左对齐 */
  margin: 0;
  padding: 20px;
}
</style>
