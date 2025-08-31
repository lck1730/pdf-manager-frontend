<template>
  <div class="pdf-note section">
    <h3>PDF备注</h3>
    <div class="note-content">
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

// 获取PDF备注
const fetchPdfNote = async (pdfId) => {
  try {
    const response = await pdfService.getPdfInfoAndNote(pdfId)
    note.value = response.data.note || ''
  } catch (error) {
    console.error('获取PDF备注失败:', error)
    note.value = ''
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
  }
}, { immediate: true })
</script>

<style scoped>
.pdf-note {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pdf-note h3 {
  margin-top: 0;
  color: #333;
  flex-shrink: 0;
}

.note-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.note-display-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.note-display {
  flex: 1;
  overflow: hidden;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
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
  text-align: center;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
}
</style>