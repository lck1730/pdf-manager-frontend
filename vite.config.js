import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  // 设置正确的基础路径
  base: '/pdf-manager-frontend/',
  server: {
    port: 3000,
    open: true
  }
})