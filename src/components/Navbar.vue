<template>
  <nav class="navbar">
    <div class="nav-container">
      <div class="nav-brand">
        <router-link to="/">PDF管理系统</router-link>
      </div>
      
      <div class="nav-menu">
        <template v-if="isAuthenticated">
          <span class="user-info">欢迎, {{ username }}</span>
          <button @click="handleLogout" class="logout-btn">登出</button>
        </template>
        
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script>
import authService from '../services/authService';
import apiClient from '../services/apiClient';

export default {
  name: 'Navbar',
  data() {
    return {
      isAuthenticated: authService.isAuthenticated(),
      username: ''
    };
  },
  mounted() {
    // 页面加载时检查认证状态
    this.checkAuthStatus();
  },
  methods: {
    checkAuthStatus() {
      this.isAuthenticated = authService.isAuthenticated();
      if (this.isAuthenticated) {
        // 从JWT令牌中提取用户名（简化处理）
        const token = authService.getAccessToken();
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.username = payload.sub;
          } catch (e) {
            console.error('解析JWT令牌失败', e);
          }
        }
      }
    },
    
    async handleLogout() {
      try {
        const accessToken = authService.getAccessToken();
        const refreshToken = authService.getRefreshToken();
        
        // 发送登出请求
        await apiClient.post('/api/auth/logout', {
          accessToken,
          refreshToken
        });
      } catch (error) {
        console.error('登出请求失败:', error);
      } finally {
        // 清除本地令牌
        authService.clearTokens();
        
        // 更新认证状态
        this.isAuthenticated = false;
        this.username = '';
        
        // 重定向到登录页面
        this.$router.push('/login');
      }
    }
  },
  
  // 监听路由变化以更新认证状态
  watch: {
    '$route'() {
      this.checkAuthStatus();
    }
  }
};
</script>

<style scoped>
.navbar {
  background-color: #343a40;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,.1);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 1rem;
}

.nav-brand a {
  color: white;
  text-decoration: none;
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-menu a {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-menu a:hover {
  background-color: #495057;
}

.user-info {
  color: white;
  margin-right: 1rem;
}

.logout-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #c82333;
}
</style>