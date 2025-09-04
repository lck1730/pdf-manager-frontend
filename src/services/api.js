import axios from 'axios'

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'https://8.137.152.40/api',
  timeout: 10000
})

export default apiClient