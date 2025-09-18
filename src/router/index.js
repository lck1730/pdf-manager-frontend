import { createRouter, createWebHistory } from 'vue-router';
import authService from '../services/authService';

// 动态导入组件
const Login = () => import('../components/Login.vue');
const Register = () => import('../components/Register.vue');
const MainApp = () => import('../components/MainApp.vue');

// 创建路由
const routes = [
  {
    path: '/',
    name: 'Home',
    redirect: '/app' // 默认重定向到/app
  },
  {
    path: '/app',
    name: 'App',
    component: MainApp // 指向MainApp.vue组件
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查目标路由是否需要认证
  const requiresAuth = !['/login', '/register'].includes(to.path);
  const isAuthenticated = authService.isAuthenticated();
  
  if (requiresAuth && !isAuthenticated) {
    // 需要认证但未认证，重定向到登录页
    next('/login');
  } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    // 已认证用户访问登录或注册页面，重定向到/app
    next('/app');
  } else {
    // 其他情况允许访问
    next();
  }
});

export default router;