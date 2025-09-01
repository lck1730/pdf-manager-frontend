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

const { fetchPdfList, setFilteredPdfList, getDebugInfo } = usePdfManager()
const allTags = ref([])
const selectedTags = ref([])

// 获取所有标签
const fetchAllTags = async () => {
  try {
    console.log('正在获取所有标签...')
    const response = await tagService.getAllTags()
    console.log('获取所有标签成功:', response)
    allTags.value = response.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

// 选择全部标签
const selectAll = () => {
  selectedTags.value = []
  console.log('选择全部标签')
  setFilteredPdfList(null) // 传入null表示显示全部
}

// 切换标签选择状态
const toggleTag = (tag) => {
  console.log('切换标签:', tag)
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    // 如果标签已选中，则取消选中
    selectedTags.value.splice(index, 1)
    console.log('取消选中标签:', tag)
  } else {
    // 如果标签未选中，则选中该标签
    selectedTags.value.push(tag)
    console.log('选中标签:', tag)
  }

  // 根据选中的标签筛选PDF
  filterPdfsByTags()
}

// 根据选中的标签筛选PDF
const filterPdfsByTags = async () => {
  try {
    console.log('开始筛选PDF...')
    if (selectedTags.value.length === 0) {
      // 如果没有选中任何标签，显示所有PDF
      console.log('没有选中任何标签，显示所有PDF')
      setFilteredPdfList(null)
    } else {
      // 根据选中的标签筛选PDF
      console.log('根据标签筛选PDF:', selectedTags.value)
      const response = await tagService.searchByTags(selectedTags.value)

      // 检查API响应
      console.log('API response:', response)

      // 根据后端实际返回的数据结构调整
      if (response.data && Array.isArray(response.data)) {
        // 提取PDF ID列表
        const pdfIds = response.data.map(item => item.pdfId || item.id)
        console.log('提取到的PDF ID列表:', pdfIds)

        // 调试信息
        console.log('筛选前的调试信息:', getDebugInfo())

        setFilteredPdfList(pdfIds)

        // 调试信息
        console.log('筛选后的调试信息:', getDebugInfo())
      } else {
        // 如果没有找到匹配的PDF，清空列表
        console.log('没有找到匹配的PDF，清空列表')
        setFilteredPdfList([])
      }
    }
  } catch (error) {
    console.error('筛选PDF失败:', error)
    // 如果筛选失败，显示所有PDF
    setFilteredPdfList(null)
  }
}

// 组件挂载时获取所有标签
onMounted(() => {
  console.log('组件挂载，开始获取所有标签...')
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
