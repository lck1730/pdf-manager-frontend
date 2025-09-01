<template>
  <div class="pdf-list-section section">
    <div class="search-container">
      <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索PDF文件..."
          class="search-input"
          @input="handleSearch"
      />
    </div>
    <div class="pdf-list">
      <div
          v-for="(pdf, index) in filteredPdfList"
          :key="pdf.id || index"
          class="pdf-item"
          :class="{ selected: selectedPdf && selectedPdf.id === pdf.id }"
          @click="selectPdf(pdf)"
      >
        <div class="pdf-icon">📄</div>
        <div class="pdf-info">
          <div class="pdf-name">{{ pdf.filename || '未知文件' }}</div>
          <div class="pdf-tags">
            <span
                v-for="tag in pdf.tags"
                :key="tag"
                class="pdf-tag"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    pdfList: {
      type: Array,
      default: () => []
    },
    selectedPdf: {
      type: Object,
      default: () => null
    }
  },
  data() {
    return {
      searchQuery: ''
    }
  },
  computed: {
    processedPdfList() {
      console.log('Processing pdfList:', this.pdfList);
      if (!Array.isArray(this.pdfList) || this.pdfList.length === 0) {
        return []
      }

      // 直接返回PDF对象数组，不再处理二维数组（因为usePdfManager已经处理过了）
      return this.pdfList.map(item => {
        // 确保返回的对象有正确的字段
        if (item && typeof item === 'object') {
          return {
            id: item.id,
            filename: item.filename || '未知文件',
            tags: item.tags || [],
            ...item // 保留其他属性
          }
        }
        // 其他情况返回默认对象
        return {
          id: null,
          filename: '未知文件',
          tags: []
        }
      })
    },
    filteredPdfList() {
      if (!this.searchQuery) {
        return this.processedPdfList
      }

      const query = this.searchQuery.toLowerCase()
      return this.processedPdfList.filter(pdf =>
          (pdf.filename || '').toLowerCase().includes(query)
      )
    }
  },
  methods: {
    selectPdf(pdf) {
      console.log('Selecting PDF:', pdf);
      this.$emit('select-pdf', pdf)
    },
    handleSearch() {
      // 搜索处理已经在computed属性中完成
      // 这里可以添加防抖等高级功能
    }
  }
}
</script>

<style scoped>
.search-container {
  padding: 0 10px 15px 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.search-input {
  width: 100%;
  padding: 10px 15px;
  border-radius: 20px;
  border: none;
  background-color: rgba(255, 255, 255, 0.15);
  color: white;
  font-size: 14px;
  box-sizing: border-box;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.search-input:focus {
  outline: none;
  background-color: rgba(255, 255, 255, 0.25);
}

.pdf-list {
  padding: 0 10px 15px 10px;
  max-height: calc(100vh - 350px);
  overflow-y: auto;
}

.pdf-item {
  display: flex;
  align-items: center;
  padding: 6px;
  border-radius: 20px;
  cursor: pointer;
  margin-bottom: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.pdf-item:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.pdf-item.selected {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

.pdf-icon {
  font-size: 20px;
  margin-right: 12px;
}

.pdf-info {
  flex: 1;
}

.pdf-name {
  font-weight: 500;
  margin-bottom: 5px;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pdf-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.pdf-tag {
  font-size: 11px;
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 滚动条样式 */
.pdf-list::-webkit-scrollbar {
  width: 6px;
}

.pdf-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.pdf-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 10px;
}

.pdf-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
