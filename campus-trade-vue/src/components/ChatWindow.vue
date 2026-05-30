<template>
  <div>
    <div class="chat-trigger" @click="toggleChat" v-if="!isChatOpen">
      <el-badge :is-dot="hasUnread" class="item">
        <el-button type="primary" circle size="large" :icon="ChatDotRound" />
      </el-badge>
    </div>

    <transition name="el-zoom-in-bottom">
      <div class="chat-glass-panel" v-show="isChatOpen">
        <div class="chat-header">
          <div class="seller-info">
            <span class="name">与卖家 {{ sellerName }} 沟通中</span>
            <span :class="['status-dot', isSellerOnline ? 'online' : 'offline']"></span>
            <span class="status-text">{{ isSellerOnline ? '在线' : '离线' }}</span>
          </div>
          <el-button link @click="toggleChat" :icon="Close" />
        </div>

        <el-scrollbar ref="scrollbarRef" class="chat-messages">
          <div
            v-for="(msg, index) in messageList"
            :key="index"
            :class="['message-item', msg.senderId === currentUserId ? 'my-msg' : 'their-msg']"
          >
            <div class="msg-bubble">{{ msg.content }}</div>
            <div class="msg-meta" v-if="msg.senderId === currentUserId">
              {{ msg.status === 1 ? '已读' : '已发送' }}
            </div>
          </div>
        </el-scrollbar>

        <div class="chat-input-area">
          <el-input
            v-model="inputMsg"
            type="textarea"
            :rows="2"
            placeholder="给卖家发消息..."
            @keyup.enter="sendMessage"
            resize="none"
          />
          <el-button type="primary" class="send-btn" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ChatDotRound, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const props = defineProps({
  sellerId: { type: Number, required: true },
  sellerName: { type: String, required: true },
  goodsId: { type: Number, required: true }
})

const userStore = useUserStore()
const currentUser = userStore.currentUser || {}
const currentUserId = currentUser.id

const isChatOpen = ref(false)
const hasUnread = ref(false)
const isSellerOnline = ref(false)
const inputMsg = ref('')
const messageList = ref([])
const scrollbarRef = ref(null)

let ws = null

const initWebSocket = () => {
  if (typeof WebSocket === "undefined") {
    console.error("您的浏览器不支持 WebSocket")
    return
  }
  
  try {
    const wsUrl = `ws://localhost:8080/ws/chat/${currentUserId}`
    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log("聊天服务已连接")
      ElMessage.success('聊天服务已连接')
    }
    
    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        messageList.value.push(msg)
        if (!isChatOpen.value) hasUnread.value = true
        scrollToBottom()
      } catch (e) {
        console.error('解析消息失败:', e)
      }
    }

    ws.onclose = () => {
      console.log("聊天服务已断开")
    }

    ws.onerror = (error) => {
      console.error("WebSocket 错误:", error)
    }
  } catch (e) {
    console.error("WebSocket 连接失败:", e)
  }
}

const loadHistory = async () => {
  try {
    const request = (await import('../utils/request')).default
    const res = await request.get(`/chat/history/${currentUserId}/${props.sellerId}`)
    if (res.data.code === 200) {
      messageList.value = res.data.data
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  }
}

const toggleChat = () => {
  isChatOpen.value = !isChatOpen.value
  if (isChatOpen.value) {
    hasUnread.value = false
    if (messageList.value.length === 0) loadHistory()
    nextTick(() => scrollToBottom())
  }
}

const sendMessage = () => {
  if (!inputMsg.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天服务未连接，请稍后重试')
    return
  }
  
  const msgObj = {
    senderId: currentUserId,
    receiverId: props.sellerId,
    goodsId: props.goodsId,
    content: inputMsg.value.trim(),
    createTime: new Date().toISOString(),
    status: 0
  }
  
  ws.send(JSON.stringify(msgObj))
  messageList.value.push(msgObj)
  inputMsg.value = ''
  scrollToBottom()
}

const scrollToBottom = () => {
  if (scrollbarRef.value) {
    scrollbarRef.value.setScrollTop(9999)
  }
}

onMounted(() => {
  initWebSocket()
})

onUnmounted(() => {
  if (ws) ws.close()
})
</script>

<style scoped>
.chat-trigger {
  position: fixed;
  right: 40px;
  bottom: 100px;
  z-index: 999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border-radius: 50%;
}

.chat-glass-panel {
  position: fixed;
  right: 40px;
  bottom: 100px;
  width: 350px;
  height: 500px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
}

.chat-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.3);
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #333;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background-color: #67C23A;
  box-shadow: 0 0 5px #67C23A;
}

.status-dot.offline {
  background-color: #909399;
}

.status-text {
  font-size: 12px;
  color: #666;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  background: rgba(255, 255, 255, 0.2);
}

.message-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
  max-width: 80%;
}

.my-msg {
  align-self: flex-end;
  align-items: flex-end;
  margin-left: auto;
}

.their-msg {
  align-self: flex-start;
  align-items: flex-start;
}

.msg-bubble {
  padding: 10px 15px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.4;
}

.my-msg .msg-bubble {
  background: #409EFF;
  color: white;
  border-bottom-right-radius: 2px;
}

.their-msg .msg-bubble {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-bottom-left-radius: 2px;
}

.msg-meta {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.chat-input-area {
  padding: 12px;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  gap: 10px;
  align-items: flex-end;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.send-btn {
  height: 52px;
}
</style>
