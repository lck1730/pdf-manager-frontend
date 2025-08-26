import axios from 'axios'

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

export default apiClient