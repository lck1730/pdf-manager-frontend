<script setup>
import { ref, onMounted, watch } from 'vue'
import authService from '@/services/authService'
import Login from '@/components/Login.vue'
import MainApp from '@/components/MainApp.vue'
import { useRoute } from 'vue-router'

// 检查用户是否已认证
const isAuthenticated = ref(false)
const route = useRoute()

// 更新认证状态的函数
const updateAuthStatus = () => {
  isAuthenticated.value = authService.isAuthenticated()
}

onMounted(() => {
  updateAuthStatus()
})

// 监听路由变化以更新认证状态
watch(
  () => route.path,
  () => {
    updateAuthStatus()
  }
)

// 监听localStorage变化以更新认证状态
window.addEventListener('storage', updateAuthStatus)
</script>

<template>
  <div id="app">
    <!-- 根据认证状态显示不同界面 -->
    <Login v-if="!isAuthenticated" />
    <MainApp v-else />
  </div>
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #ffffff;
}

#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  background-color: #f8f9fa;
  height: 100%;
  width: 100%;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
  min-height: calc(100vh - 120px);
}
</style>