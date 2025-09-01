<template>
  <div class="tag-section section">
    <h3>标签筛选</h3>
    <div class="tag-list">
      <span
          class="tag-item"
          :class="{ selected: selectedTags.length === 0 }"
          @click="selectAll"
      >
        全部
      </span>
      <span
          v-for="tag in allTags"
          :key="tag"
          class="tag-item"
          :class="{ selected: selectedTags.includes(tag) }"
          @click="toggleTag(tag)"
      >
        {{ tag }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { tagService } from '@/services/tagService'
import { usePdfManager } from '@/composables/usePdfManager'

const { fetchPdfList, setFilteredPdfList } = usePdfManager()
const allTags = ref([])
const selectedTags = ref([])

// 获取所有标签
const fetchAllTags = async () => {
  try {
    const response = await tagService.getAllTags()
    allTags.value = response.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

// 选择全部标签
const selectAll = () => {
  selectedTags.value = []
  // 获取所有PDF列表（不筛选）
  fetchPdfList('yaya')
}

// 切换标签选择状态
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    // 如果标签已选中，则取消选中
    selectedTags.value.splice(index, 1)
  } else {
    // 如果标签未选中，则选中该标签
    selectedTags.value.push(tag)
  }

  // 根据选中的标签筛选PDF
  filterPdfsByTags()
}

// 根据选中的标签筛选PDF
const filterPdfsByTags = async () => {
  try {
    if (selectedTags.value.length === 0) {
      // 如果没有选中任何标签，获取所有PDF
      fetchPdfList('yaya')
    } else {
      // 根据选中的标签筛选PDF
      const response = await tagService.searchByTags(selectedTags.value)

      // 假设返回的是符合标签条件的PDF ID列表
      // 需要根据实际返回的数据结构调整
      const pdfIds = response.data || []

      // 使用这些ID获取对应的PDF信息
      // 这里假设有一个方法可以根据PDF ID列表获取PDF列表
      // 如果没有这个方法，可能需要在后端直接返回完整的PDF信息
      if (pdfIds.length > 0) {
        // 调用父组件的方法来设置过滤后的PDF列表
        setFilteredPdfList(pdfIds)
      } else {
        // 如果没有找到匹配的PDF，清空列表
        setFilteredPdfList([])
      }
    }
  } catch (error) {
    console.error('筛选PDF失败:', error)
    // 如果筛选失败，显示所有PDF
    fetchPdfList('yaya')
  }
}

// 组件挂载时获取所有标签
onMounted(() => {
  fetchAllTags()
})

// 暴露方法给父组件，用于在新建标签后刷新标签列表
defineExpose({
  refreshTags: fetchAllTags
})
</script>

<style scoped>
.tag-section {
  padding: 15px 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
  width: 100%;
  margin: 0 auto;
}

.tag-section h3 {
  color: white;
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 14px;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  color: white;
  white-space: nowrap;
}

.tag-item:hover {
  background-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
}

.tag-item.selected {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  color: #000;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(79, 172, 254, 0.4);
}
</style>
