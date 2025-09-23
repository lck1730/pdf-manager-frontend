<template>
  <div>
    <!-- 悬浮球 -->
    <div
        v-if="!isChatOpen"
        class="floating-ball"
        @click="openChat"
    >
      <span class="chat-icon">💬</span>
    </div>

    <!-- 聊天框 -->
    <div v-else class="chat-container">
      <!-- 聊天框头部 -->
      <div class="chat-header">
        <h3>AI 助手</h3>
        <button class="close-btn" @click="closeChat">×</button>
      </div>

      <!-- 聊天内容区域 -->
      <div class="chat-content">
        <!-- 功能区域 - 会折叠显示 -->
        <div v-if="showFunctionArea" class="function-area">
          <div class="function-tabs">
            <button
                :class="{ active: activeFunctionTab === 'session' }"
                @click="activeFunctionTab = 'session'"
            >
              历史会话
            </button>
            <button
                :class="{ active: activeFunctionTab === 'vector' }"
                @click="activeFunctionTab = 'vector'"
            >
              向量管理
            </button>
          </div>

          <!-- 历史会话管理 -->
          <div v-if="activeFunctionTab === 'session'" class="session-management">
            <div class="session-actions">
              <button @click="createNewSession" class="btn-primary">新建会话</button>
            </div>
            <div class="session-list">
              <div
                  v-for="session in sessions"
                  :key="session.sessionId"
                  class="session-item"
                  :class="{ active: currentSessionId === session.sessionId }"
                  @click="selectSession(session.sessionId)"
              >
                <span class="session-name">{{ session.sessionName }}</span>
                <div class="session-actions">
                  <button @click.stop="renameSession(session)" class="btn-icon">✏️</button>
                  <button @click.stop="deleteSession(session.sessionId)" class="btn-icon">🗑️</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 向量数据库管理 -->
          <div v-if="activeFunctionTab === 'vector'" class="vector-management">
            <div class="vector-list">
              <div
                  v-for="pdf in embeddedPdfs"
                  :key="pdf.pdfId"
                  class="pdf-item"
              >
                <label class="pdf-checkbox">
                  <input
                      type="checkbox"
                      :value="pdf.pdfId"
                      v-model="selectedPdfIds"
                  >
                  <span class="pdf-name">{{ pdf.fileName }}</span>
                </label>
                <button @click="removePdfFromVector(pdf.pdfId)" class="btn-icon">🗑️</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天区域 -->
        <div class="messages-area" ref="messagesArea">
          <div
              v-for="message in messages"
              :key="message.id"
              :class="['message', message.role.toLowerCase()]"
          >
            <div class="message-content">
              <div class="message-sender">
                {{ message.role === 'USER' ? '你' : 'AI助手' }}
              </div>
              <div class="message-text">{{ message.content }}</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-tools">
            <button @click="toggleFunctionArea" class="tool-btn">
              {{ showFunctionArea ? '▲' : '▼' }}
            </button>
            <button @click="togglePdfSelector" class="tool-btn">+</button>
          </div>

          <!-- PDF选择器 -->
          <div v-if="showPdfSelector" class="pdf-selector">
            <div class="pdf-selector-header">
              <h4>选择PDF进行分析</h4>
              <button @click="selectAllPdfs" class="btn-small">
                {{ selectedPdfIds.length === embeddedPdfs.length ? '取消全选' : '全选' }}
              </button>
            </div>
            <div class="pdf-list">
              <div
                  v-for="pdf in embeddedPdfs"
                  :key="pdf.pdfId"
                  class="pdf-item-selector"
              >
                <label>
                  <input
                      type="checkbox"
                      :value="pdf.pdfId"
                      v-model="selectedPdfIds"
                  >
                  <span>{{ pdf.fileName }}</span>
                </label>
              </div>
            </div>
          </div>

          <div class="input-container">
            <textarea
                v-model="userInput"
                placeholder="输入你的问题..."
                @keydown.enter.exact.prevent="sendMessage"
                class="message-input"
            ></textarea>
            <button
                @click="sendMessage"
                :disabled="!userInput.trim()"
                class="send-btn"
            >
              发送
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ragService } from '@/services/ragService'
import { pdfService } from '@/services/pdfService'

// 状态管理
const isChatOpen = ref(false)
const showFunctionArea = ref(true)
const activeFunctionTab = ref('session')
const showPdfSelector = ref(false)
const userInput = ref('')
const messages = ref([])
const sessions = ref([])
const currentSessionId = ref('')
const embeddedPdfs = ref([])
const selectedPdfIds = ref([])

// 打开聊天框
const openChat = () => {
  isChatOpen.value = true
  nextTick(() => {
    scrollToBottom()
  })
}

// 关闭聊天框
const closeChat = () => {
  isChatOpen.value = false
}

// 切换功能区域显示
const toggleFunctionArea = () => {
  showFunctionArea.value = !showFunctionArea.value
}

// 切换PDF选择器
const togglePdfSelector = () => {
  showPdfSelector.value = !showPdfSelector.value
}

// 全选PDF
const selectAllPdfs = () => {
  if (selectedPdfIds.value.length === embeddedPdfs.value.length) {
    selectedPdfIds.value = []
  } else {
    selectedPdfIds.value = embeddedPdfs.value.map(pdf => pdf.pdfId)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  const messagesArea = document.querySelector('.messages-area')
  if (messagesArea) {
    messagesArea.scrollTop = messagesArea.scrollHeight
  }
}

// 创建新会话
const createNewSession = async () => {
  try {
    const response = await ragService.createSession({ sessionName: '新会话' })
    if (response.data.success) {
      sessions.value.push({
        sessionId: response.data.sessionId,
        sessionName: '新会话'
      })
      currentSessionId.value = response.data.sessionId
      messages.value = []
    }
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

// 选择会话
const selectSession = async (sessionId) => {
  currentSessionId.value = sessionId
  try {
    const response = await ragService.getChatHistory(sessionId)
    if (response.data.success) {
      messages.value = response.data.history || []
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('获取聊天历史失败:', error)
    messages.value = []
  }
}

// 重命名会话
const renameSession = async (session) => {
  const newName = prompt('请输入新的会话名称:', session.sessionName)
  if (newName && newName.trim() !== session.sessionName) {
    try {
      await ragService.renameSession(session.sessionId, { newName: newName.trim() })
      session.sessionName = newName.trim()
    } catch (error) {
      console.error('重命名会话失败:', error)
    }
  }
}

// 删除会话
const deleteSession = async (sessionId) => {
  if (confirm('确定要删除这个会话吗？')) {
    try {
      await ragService.deleteSession(sessionId)
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
      if (currentSessionId.value === sessionId) {
        currentSessionId.value = ''
        messages.value = []
      }
    } catch (error) {
      console.error('删除会话失败:', error)
    }
  }
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || !currentSessionId.value) {
    if (!currentSessionId.value) {
      alert('请先创建或选择一个会话')
    }
    return
  }

  const message = {
    id: Date.now(),
    role: 'USER',
    content: userInput.value.trim()
  }

  messages.value.push(message)
  const userQuestion = userInput.value.trim()
  userInput.value = ''

  nextTick(() => {
    scrollToBottom()
  })

  try {
    const response = await ragService.ragChat({
      query: userQuestion,
      pdfIds: selectedPdfIds.value,
      sessionId: currentSessionId.value
    })

    if (response.data.success) {
      const aiMessage = {
        id: Date.now() + 1,
        role: 'ASSISTANT',
        content: response.data.response
      }
      messages.value.push(aiMessage)
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    const errorMessage = {
      id: Date.now() + 1,
      role: 'ASSISTANT',
      content: '抱歉，我无法回答你的问题，请稍后重试。'
    }
    messages.value.push(errorMessage)
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 移除PDF向量化
const removePdfFromVector = async (pdfId) => {
  if (confirm('确定要从向量数据库中移除这个PDF吗？')) {
    try {
      await ragService.deletePdfDocuments({ pdfId })
      embeddedPdfs.value = embeddedPdfs.value.filter(pdf => pdf.pdfId !== pdfId)
      selectedPdfIds.value = selectedPdfIds.value.filter(id => id !== pdfId)
    } catch (error) {
      console.error('移除PDF向量失败:', error)
    }
  }
}

// 获取会话列表
const fetchSessions = async () => {
  try {
    const response = await ragService.getUserSessions()
    if (response.data.success) {
      sessions.value = response.data.sessions.map(sessionId => ({
        sessionId,
        sessionName: `会话 ${sessionId.substring(0, 8)}`
      }))
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

// 获取已向量化的PDF列表
const fetchEmbeddedPdfs = async () => {
  try {
    const response = await ragService.getEmbeddedPdfs()
    if (response.data.success) {
      embeddedPdfs.value = response.data.pdfs || []
    }
  } catch (error) {
    console.error('获取已向量化的PDF列表失败:', error)
  }
}

// 初始化数据
onMounted(async () => {
  await Promise.all([
    fetchSessions(),
    fetchEmbeddedPdfs()
  ])
})
</script>

<style scoped>
.floating-ball {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: transform 0.2s;
}

.floating-ball:hover {
  transform: scale(1.1);
}

.chat-icon {
  font-size: 24px;
  color: white;
}

.chat-container {
  position: fixed;
  top: 0;
  right: 0;
  width: 75%;
  height: 100%;
  background: white;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 1001;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.function-area {
  border-bottom: 1px solid #eee;
  max-height: 300px;
  overflow-y: auto;
}

.function-tabs {
  display: flex;
  background: #f5f5f5;
}

.function-tabs button {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-weight: 500;
}

.function-tabs button.active {
  background: white;
  border-bottom: 2px solid #667eea;
}

.session-management,
.vector-management {
  padding: 15px;
}

.session-actions {
  margin-bottom: 10px;
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.session-list {
  max-height: 200px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 5px;
  cursor: pointer;
}

.session-item:hover {
  background: #f9f9f9;
}

.session-item.active {
  background: #e3f2fd;
  border-color: #667eea;
}

.session-name {
  flex: 1;
}

.session-actions .btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  margin-left: 5px;
  font-size: 14px;
}

.vector-list {
  max-height: 200px;
  overflow-y: auto;
}

.pdf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 5px;
}

.pdf-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex: 1;
}

.pdf-name {
  margin-left: 8px;
}

.messages-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fafafa;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 18px;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: white;
  border: 1px solid #eee;
  border-bottom-left-radius: 4px;
}

.message-sender {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.7;
}

.input-area {
  border-top: 1px solid #eee;
  padding: 15px;
  background: white;
}

.input-tools {
  display: flex;
  margin-bottom: 10px;
}

.tool-btn {
  background: #f0f0f0;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 4px;
  margin-right: 5px;
  cursor: pointer;
}

.pdf-selector {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  max-height: 200px;
  overflow-y: auto;
}

.pdf-selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
}

.pdf-list {
  max-height: 120px;
  overflow-y: auto;
}

.pdf-item-selector {
  padding: 5px 0;
}

.pdf-item-selector label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.input-container {
  display: flex;
}

.message-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: none;
  height: 60px;
  font-family: inherit;
}

.send-btn {
  margin-left: 10px;
  padding: 0 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
