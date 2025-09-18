<template>
  <div class="register-container">
    <div class="register-form">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名:</label>
          <input 
            type="text" 
            id="username" 
            v-model="registerForm.username" 
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码:</label>
          <input 
            type="password" 
            id="password" 
            v-model="registerForm.password" 
            required
          />
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">确认密码:</label>
          <input 
            type="password" 
            id="confirmPassword" 
            v-model="registerForm.confirmPassword" 
            required
          />
        </div>
        
        <div class="form-group">
          <button type="submit" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <div v-if="success" class="success-message">
          {{ success }}
        </div>
      </form>
      
      <div class="login-link">
        已有账户？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '../services/api';
import { useRouter } from 'vue-router'
export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    return { router }
  },
  data() {
    return {
      registerForm: {
        username: '',
        password: '',
        confirmPassword: ''
      },
      loading: false,
      error: '',
      success: ''
    };
  },
  methods: {
    async handleRegister() {
      // 检查密码是否匹配
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.error = '两次输入的密码不一致';
        return;
      }
      
      this.loading = true;
      this.error = '';
      this.success = '';
      
      try {
        // 发送注册请求
        const response = await apiClient.post('/api/auth/register', {
          username: this.registerForm.username,
          password: this.registerForm.password,
          role: 'USER' // 默认角色
        });
        
        this.success = '注册成功！即将跳转到登录页面...';
        
        // 3秒后跳转到登录页面
        setTimeout(() => {
          this.$router.push('/login');
        }, 3000);
      } catch (error) {
        this.error = error.response?.data?.message || '注册失败';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.register-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  margin-top: 1rem;
  text-align: center;
}

.success-message {
  color: #28a745;
  margin-top: 1rem;
  text-align: center;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
}
</style>