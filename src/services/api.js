import axios from 'axios'
import authService from './authService'

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // 可以根据需要修改为远程地址
  timeout: 10000
})

// 请求拦截器 - 添加认证头
apiClient.interceptors.request.use(
    (config) => {
      const token = authService.getAccessToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
)

// 响应拦截器 - 处理认证错误
apiClient.interceptors.response.use(
    (response) => {
      return response
    },
    async (error) => {
      const originalRequest = error.config

      // 如果是401错误且不是刷新令牌请求
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        // 尝试刷新令牌
        const refreshToken = authService.getRefreshToken()
        if (refreshToken) {
          try {
            const response = await axios.post('/api/auth/refresh', { refreshToken })
            const { accessToken } = response.data

            // 存储新的访问令牌
            authService.setAccessToken(accessToken)

            // 重新发送原始请求
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
            return apiClient(originalRequest)
          } catch (refreshError) {
            // 刷新令牌失败，清除令牌并重定向到登录页
            authService.clearTokens()
            window.location.href = '/login'
            return Promise.reject(refreshError)
          }
        } else {
          // 没有刷新令牌，清除令牌并重定向到登录页
          authService.clearTokens()
          window.location.href = '/login'
        }
      }

      return Promise.reject(error)
    }
)

export default apiClient
