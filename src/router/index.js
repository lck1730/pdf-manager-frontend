// 文件路径: src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainApp from '@/components/MainApp.vue'
import Login from '@/components/Login.vue'
import Register from '@/components/Register.vue'
import authService from '@/services/authService'

const routes = [
  {
    path: '/',
    redirect: '/app'
  },
  {
    path: '/app',
    component: MainApp,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    component: Register,
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory('/pdf-manager-frontend/'),
  routes
})

// 优化后的路由守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated()

  // 如果目标页面需要认证但用户未认证
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')  // 重定向到登录页
  }
  // 如果目标页面不需要认证但用户已认证
  else if (!to.meta.requiresAuth && isAuthenticated) {
    next('/app')    // 重定向到主应用
  }
  // 其他情况正常跳转
  else {
    next()
  }
})

export default router
