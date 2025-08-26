<template>
  <div class="pdf-note section">
    <h3>PDF备注</h3>
    <div class="note-content">
      <div v-if="!isEditing">
        <div class="note-display">
          <pre v-if="note">{{ note }}</pre>
          <p v-else class="no-note">暂无备注</p>
        </div>
        <button class="edit-btn" @click="startEdit">编辑</button>
      </div>
      <div v-else>
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
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pdf-note h3 {
  margin-top: 0;
  color: #333;
}

.note-display {
  min-height: 100px;
  margin-bottom: 15px;
}

.note-display pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #eee;
  max-height: 300px;
  overflow-y: auto;
}

.note-editor {
  width: 100%;
  min-height: 100px;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
}

.note-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
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
  margin: 20px 0;
}
</style>