<template>
  <div class="tag-section section">
    <div class="tag-list">
      <span 
        v-for="tag in tags" 
        :key="tag.id" 
        class="tag-item"
        :class="{ selected: tag.selected }"
        @click="toggleTag(tag)"
      >
        {{ tag.name }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { usePdfManager } from '@/composables/usePdfManager'
const { tags, toggleTag } = usePdfManager()

// 初始化标签数据
tags.value = [
  { id: 1, name: '全部', selected: true },
  { id: 2, name: '未标记', selected: false },
  { id: 3, name: '重要', selected: false },
  { id: 4, name: '待处理', selected: false }
]
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
  width: 90%;

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