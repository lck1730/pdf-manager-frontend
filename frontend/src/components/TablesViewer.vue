<template>
  <div class="tables-viewer-container">
    <div class="tables-viewer section">
      <div v-if="tables && tables.length > 0" class="tables-content">
        <div class="table-navigation" v-if="tables.length > 1">
          <button :disabled="currentIndex <= 0" @click="prevTable">‹</button>
          <span>{{ currentIndex + 1 }} / {{ tables.length }}</span>
          <button :disabled="currentIndex >= tables.length - 1" @click="nextTable">›</button>
        </div>

        <div class="table-container">
          <div class="table-content">
            <!-- 表格完整区域 -->
            <div class="table-full-section">
              <!-- 标题区域 -->
              <div class="table-caption-section">
                <div class="table-caption">
                  <h3>{{ currentTable.caption || '猪猪˃̶͈🐽˂̶͈  这个表格标题不知道哦~' }}</h3>
                </div>
              </div>

              <!-- 表格主体区域 -->
              <div class="table-wrapper">
                <div ref="tableBody" class="table-body" v-html="sanitizedTableBody"></div>
              </div>

              <!-- 脚注区域 -->
              <div class="table-footnote-section">
                <div class="footnote-content">
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
            </div>
          </div>
        </div>
      </div>
      <div v-else class="no-tables">
        <p>猪猪🐷，这个文献没有表格哦~</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { pdfService } from '@/services/pdfService'
import DOMPurify from 'dompurify'
import katex from 'katex'
import 'katex/dist/katex.min.css'

const props = defineProps({
  pdf: {
    type: Object,
    default: null
  }
})

const tables = ref([])
const currentIndex = ref(0)
const tableBody = ref(null)
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

    // 处理返回的数据格式
    tables.value = response.data.map(item => ({
      id: item[0],
      body: item[1],
      caption: item[2],
      footnote: item[3]
    })) || []

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

// 清理HTML内容，防止XSS攻击
const sanitizedTableBody = computed(() => {
  if (!currentTable.value || !currentTable.value.body) return ''
  return DOMPurify.sanitize(currentTable.value.body)
})

// 渲染LaTeX公式
const renderMath = () => {
  if (tableBody.value) {
    // 查找所有包含数学表达式的元素
    const mathElements = tableBody.value.querySelectorAll('td, th')

    mathElements.forEach(element => {
      const text = element.textContent

      // 匹配行内数学表达式 $...$
      const inlineMathRegex = /\$(.*?)\$/g
      let hasInlineMath = inlineMathRegex.test(text)

      // 匹配块级数学表达式 $$...$$
      const blockMathRegex = /\$\$(.*?)\$\$/g
      let hasBlockMath = blockMathRegex.test(text)

      if (hasInlineMath || hasBlockMath) {
        // 处理行内数学表达式 $...$
        const newText = text
            .replace(/\$(.*?)\$/g, (match, expr) => {
              try {
                return katex.renderToString(expr, {
                  throwOnError: false,
                  displayMode: false
                })
              } catch (error) {
                console.warn('KaTeX渲染行内公式失败:', error)
                return match
              }
            })
            .replace(/\$\$(.*?)\$\$/g, (match, expr) => {
              try {
                return katex.renderToString(expr, {
                  throwOnError: false,
                  displayMode: true
                })
              } catch (error) {
                console.warn('KaTeX渲染块级公式失败:', error)
                return match
              }
            })

        element.innerHTML = newText
      }
    })
  }
}

// 监听当前表格变化，渲染LaTeX
watch(currentTable, () => {
  nextTick(() => {
    renderMath()
  })
})

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

<style scoped>
.tables-viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.tables-viewer {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 15px;
  box-sizing: border-box;
}

.tables-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  flex-shrink: 0;
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
  flex: 1;
  display: flex;
  flex-direction: column;
}

.table-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-full-section {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  overflow: hidden;
}

.table-caption-section {
  background-color: #e9ecef;
  border-bottom: 1px solid #dee2e6;
}

.table-caption {
  padding: 15px;
  text-align: center;
}

.table-caption h3 {
  font-size: 16px;
  margin: 0;
  color: #495057;
}

.table-wrapper {
  flex: 1;
  overflow: auto;
  padding: 15px;
  background-color: #ffffff;
}

.table-body {
  overflow: auto;
}

.table-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 0 auto;
  background-color: #ffffff;
}

.table-body th,
.table-body td {
  padding: 8px 12px;
  text-align: left;
  border: 1px solid #ddd;
  vertical-align: top;
}

.table-body th {
  background-color: #f1f3f4;
  font-weight: 600;
}

.table-body tbody tr:nth-child(even) {
  background-color: #f8f9fa;
}

/* KaTeX样式调整 */
.table-body :deep(.katex-display) {
  margin: 0.5em 0;
  text-align: center;
}

.table-body :deep(.katex) {
  font-size: 1.1em;
}

.table-footnote-section {
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

.edit-footnote-btn {
  padding: 4px 8px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.no-tables {
  text-align: center;
  padding: 30px;
  color: #6c757d;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
