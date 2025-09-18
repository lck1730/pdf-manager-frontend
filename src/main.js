import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

createApp(App).use(router).mount('#app');

// 添加全局样式确保应用占据整个屏幕
const style = document.createElement('style')
style.innerHTML = `
  html, body, #app {
    margin: 0;
    padding: 0;
    height: 100%;
    width: 100%;
    overflow: hidden;
  }
`
document.head.appendChild(style)
