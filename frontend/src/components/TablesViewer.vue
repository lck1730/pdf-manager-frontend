<template>
  <div class="tables-viewer section">
    <div v-if="tables && tables.length > 0" class="tables-content">
      <div class="table-navigation" v-if="tables.length > 1">
        <button :disabled="currentIndex <= 0" @click="prevTable">‹</button>
        <span>{{ currentIndex + 1 }} / {{ tables.length }}</span>
        <button :disabled="currentIndex >= tables.length - 1" @click="nextTable">›</button>
      </div>
      
      <div class="table-container">
        <div class="table-info">
          <div class="table-id">表格ID: {{ currentTable.id }}</div>
          <div class="table-footnote">
            <strong>脚注:</strong> 
            <span v-if="!isEditingFootnote">{{ currentTable.footnote || '无' }}</span>
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
        
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th v-for="(header, index) in currentTable.headers" :key="index">
                  {{ header }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, rowIndex) in currentTable.data" :key="rowIndex">
                <td v-for="(cell, cellIndex) in row" :key="cellIndex">
                  {{ cell }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div v-else class="no-tables">
      <p>该PDF中未找到表格</p>
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

const tables = ref([])
const currentIndex = ref(0)
const isEditingFootnote = ref(false)
const editingFootnote = ref('')

// 当前表格
const currentTable = computed(() => {
  return tables.value.length > 0 ? tables.value[currentIndex.value] : null
})

// 获取表格数据
const fetchTables = async (pdfId) => {
  try {
    const response = await pdfService.getTablesByPdfId(pdfId)
    tables.value = response.data || []
    currentIndex.value = 0
  } catch (error) {
    console.error('获取表格信息失败:', error)
    tables.value = []
    currentIndex.value = 0
  }
}

// 上一个表格
const prevTable = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

// 下一个表格
const nextTable = () => {
  if (currentIndex.value < tables.value.length - 1) {
    currentIndex.value++
  }
}

// 编辑脚注
const editFootnote = () => {
  editingFootnote.value = currentTable.value.footnote || ''
  isEditingFootnote.value = true
}

// 保存脚注
const saveFootnote = async () => {
  if (!currentTable.value) return
  
  try {
    await pdfService.updateTableFootnote(currentTable.value.id, editingFootnote.value)
    currentTable.value.footnote = editingFootnote.value
    isEditingFootnote.value = false
  } catch (error) {
    console.error('保存脚注失败:', error)
    alert('保存脚注失败: ' + (error.message || '未知错误'))
  }
}

// 监听PDF变化
watch(() => props.pdf, (newPdf) => {
  if (newPdf && newPdf.id) {
    fetchTables(newPdf.id)
  } else {
    tables.value = []
    currentIndex.value = 0
  }
}, { immediate: true })
</script>

<script>
import { computed } from 'vue'
export default {
  computed: {
    currentTable() {
      return this.tables.length > 0 ? this.tables[this.currentIndex] : null
    }
  }
}
</script>

<style scoped>
.tables-viewer {
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tables-content {
  margin-top: 15px;
}

.table-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.table-navigation button {
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

.table-navigation button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.table-container {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.table-info {
  padding: 10px;
  background-color: #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.table-id {
  font-weight: 500;
}

.table-footnote {
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

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 100%;
}

.data-table th,
.data-table td {
  padding: 8px 12px;
  text-align: left;
  border: 1px solid #ddd;
}

.data-table th {
  background-color: #f1f3f4;
  font-weight: 600;
}

.data-table tbody tr:nth-child(even) {
  background-color: #f8f9fa;
}

.no-tables {
  text-align: center;
  padding: 30px;
  color: #6c757d;
}
</style>