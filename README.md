# Example Application

这是一个基于Spring Boot的简单Java项目，用于演示基础的Web应用开发结构和流程。

## 项目结构

```
.
├── src/main/java               # 后端Java代码
├── src/main/resources          # 后端资源配置
├── frontend/                   # 前端Vue3项目
├── target/                     # Maven构建输出
└── start-with-frontend.bat     # 前后端联合启动脚本
```

## 开发指南

### 后端开发

1. 确保已安装JDK 17和Maven 3.5+
2. 使用IntelliJ IDEA或其他IDE导入项目
3. 运行`ExampleApplication.java`启动应用

或者使用命令行：
```bash
mvn spring-boot:run
```

### 前端开发

前端使用Vue3 + Vite构建，独立于后端开发：

1. 进入frontend目录
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`

### 前后端联合开发

使用提供的脚本启动前后端服务：
```bash
start-with-frontend.bat
```

该脚本会同时启动：
- MinIO服务（文件存储）
- Spring Boot后端服务
- Vue3前端开发服务器

## API交互

前端通过代理方式访问后端API，避免跨域问题。所有API请求都代理到后端服务。

## 构建和部署

### 后端构建
```bash
mvn clean package
```

### 前端构建
```bash
cd frontend
npm run build
```