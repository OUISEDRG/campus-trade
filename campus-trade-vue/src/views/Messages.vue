<template>
  <div class="message-center-container">
    <div class="session-list glass-card">
      <div class="list-header">
        <h3>我的私信</h3>
        <span class="session-count">{{ chatStore.sessionList.length }} 个会话</span>
      </div>
      
      <div v-if="chatStore.sessionList.length === 0" class="empty-state">
        <el-icon class="empty-icon"><ChatDotRound /></el-icon>
        <p>暂无消息</p>
        <p class="empty-hint">去商品详情页与卖家沟通吧</p>
      </div>
      
      <div
        v-for="session in chatStore.sessionList"
        :key="session.targetUserId"
        :class="['session-item', chatStore.activeSessionId === session.targetUserId ? 'active' : '']"
        @click="chatStore.selectSession(session)"
      >
        <div class="avatar-area">
          <div class="avatar">{{ session.targetUserName?.charAt(0).toUpperCase() || 'U' }}</div>
        </div>
        <div class="session-info">
          <div class="name-time">
            <span class="name">{{ session.targetUserName || '用户' }}</span>
            <span class="time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="preview">{{ session.lastMessage }}</div>
        </div>
        <el-badge v-if="session.unreadCount > 0" :value="session.unreadCount" class="unread-badge" />
      </div>
    </div>

    <div class="chat-detail" v-if="chatStore.activeSessionId">
      <div class="chat-header glass-card">
        <div class="session-title">
          <span class="title">{{ getCurrentSession?.targetUserName || '对话' }}</span>
          <span class="online-dot online"></span>
        </div>
        <button class="back-btn" @click="backToHome">
          <el-icon><ArrowLeft /></el-icon>
        </button>
      </div>

      <div class="context-goods-card" v-if="chatStore.currentContextGoods">
        <el-image
          :src="chatStore.currentContextGoods.imageUrl"
          class="goods-img"
          fit="cover"
        >
          <template #error>
            <div class="image-error-slot">
              <el-icon :size="24"><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </template>
          <template #placeholder>
            <div class="image-loading-slot" v-loading="true"></div>
          </template>
        </el-image>
        <div class="goods-meta">
          <div class="goods-title">{{ chatStore.currentContextGoods.title }}</div>
          <div class="goods-price">¥{{ chatStore.currentContextGoods.price }}</div>
          <el-button size="small" type="primary" @click="goToGoods(chatStore.currentContextGoods.id)">
            查看商品
          </el-button>
        </div>
      </div>

      <el-scrollbar class="message-flow" ref="scrollbarRef">
        <div
          v-for="(msg, index) in chatStore.currentMessages"
          :key="index"
          :class="['msg-bubble-wrapper', msg.isMine || msg.senderId === currentUserId ? 'mine' : 'theirs']"
        >
          <div class="msg-bubble">{{ msg.content }}</div>
          <div class="msg-status">
            {{ formatTime(msg.createTime) }}
            <span v-if="msg.isMine || msg.senderId === currentUserId">
              {{ msg.status === 1 ? '已读' : '已发送' }}
            </span>
          </div>
        </div>
      </el-scrollbar>

      <div class="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="发送私信..."
          @keyup.enter="sendMsg"
          resize="none"
        />
        <el-button type="primary" @click="sendMsg">发送</el-button>
      </div>
    </div>

    <div class="empty-chat" v-else>
      <el-icon class="empty-chat-icon"><ChatDotRound /></el-icon>
      <p>选择一个会话开始聊天</p>
      <button class="glass-btn primary" @click="backToHome">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()
const inputText = ref('')
const scrollbarRef = ref(null)

const currentUser = userStore.currentUser || {}
const currentUserId = currentUser.id

const getCurrentSession = computed(() => {
  return chatStore.sessionList.find(s => s.targetUserId === chatStore.activeSessionId)
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const sendMsg = () => {
  if (!inputText.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  chatStore.sendMessage(inputText.value.trim())
  inputText.value = ''
  
  nextTick(() => {
    if (scrollbarRef.value) {
      scrollbarRef.value.setScrollTop(9999)
    }
  })
}

const goToGoods = (goodsId) => {
  router.push(`/detail/${goodsId}`)
}

const backToHome = () => {
  router.push('/home')
}

const loadSessions = async () => {
  try {
    const request = (await import('../utils/request')).default
    const res = await request.get(`/chat/sessions/${currentUserId}`)
    
    if (res.data.code === 200) {
      chatStore.sessionList = res.data.data
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

onMounted(() => {
  if (currentUserId) {
    chatStore.initGlobalWebSocket(currentUserId)
    loadSessions()
  }
})

onUnmounted(() => {
  // 不关闭socket，保持全局连接
})
</script>

<style scoped>
.message-center-container {
  display: flex;
  height: calc(100vh - 100px);
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  gap: 20px;
}

.session-list {
  width: 320px;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.list-header {
  padding: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.session-count {
  font-size: 13px;
  color: #666;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.empty-icon {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 15px;
}

.empty-state p {
  margin: 5px 0;
  color: #666;
}

.empty-hint {
  font-size: 13px;
  color: #999 !important;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.session-item:hover {
  background: rgba(64, 158, 255, 0.08);
}

.session-item.active {
  background: rgba(255, 255, 255, 0.5);
  border-left: 4px solid #409EFF;
}

.avatar-area {
  margin-right: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.name-time {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.name {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.preview {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  margin-left: 8px;
}

.chat-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.5);
}

.session-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-weight: bold;
  font-size: 16px;
}

.online-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.online-dot.online {
  background-color: #67C23A;
  box-shadow: 0 0 5px #67C23A;
}

.back-btn {
  padding: 6px 12px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.back-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

.context-goods-card {
  display: flex;
  padding: 15px;
  background: rgba(64, 158, 255, 0.08);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.goods-img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  margin-right: 15px;
}

.image-error-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  border-radius: 12px;
  font-size: 12px;
}

.image-error-slot .el-icon {
  margin-bottom: 4px;
}

.image-loading-slot {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  border-radius: 12px;
}

.goods-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.goods-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 5px;
}

.goods-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 10px;
}

.message-flow {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.msg-bubble-wrapper {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
  max-width: 70%;
}

.msg-bubble-wrapper.mine {
  align-items: flex-end;
  margin-left: auto;
}

.msg-bubble-wrapper.theirs {
  align-items: flex-start;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.mine .msg-bubble {
  background: #409EFF;
  color: white;
  border-bottom-right-radius: 4px;
}

.theirs .msg-bubble {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-bottom-left-radius: 4px;
}

.msg-status {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.input-area {
  padding: 20px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.2);
}

.input-area .el-input {
  flex: 1;
}

.input-area .el-textarea__inner {
  border-radius: 12px;
  resize: none;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.empty-chat-icon {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 20px;
}

.empty-chat p {
  margin: 0 0 20px;
  color: #666;
}

.glass-btn {
  padding: 12px 30px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.glass-btn.primary {
  background: #409eff;
  color: white;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.glass-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

@media (max-width: 768px) {
  .message-center-container {
    flex-direction: column;
    height: auto;
  }
  
  .session-list {
    width: 100%;
    height: 300px;
  }
  
  .chat-detail {
    height: 400px;
  }
}
</style>
