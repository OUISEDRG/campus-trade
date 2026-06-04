<template>
  <div class="page-container">
    <header class="glass-header pc-only">
      <div class="nav-brand" @click="router.push('/home')">CampusTrade</div>
      <nav class="nav-links">
        <span @click="router.push('/home')">首页</span>
        <span @click="router.push('/notifications')" class="active">通知</span>
        <span @click="router.push('/me')">我的</span>
      </nav>
    </header>

    <main class="main-body">
      <!-- Tab 切换 -->
      <div class="tab-bar">
        <div :class="['tab-item', activeTab === 'system' ? 'active' : '']" @click="activeTab = 'system'">
          系统通知
          <el-badge v-if="unreadSystemCount > 0" :value="unreadSystemCount" class="tab-badge" />
        </div>
        <div :class="['tab-item', activeTab === 'chat' ? 'active' : '']" @click="activeTab = 'chat'">
          私信
          <el-badge v-if="chatStore.unreadTotal > 0" :value="chatStore.unreadTotal" class="tab-badge" />
        </div>
      </div>

      <!-- ==================== 系统通知 Tab ==================== -->
      <div v-if="activeTab === 'system'" class="tab-content">
        <div class="section-title">
          <h2>系统通知</h2>
          <el-button v-if="messages.length > 0" type="primary" round size="small" @click="markAll">全部已读</el-button>
        </div>

        <div class="message-list" v-if="messages.length > 0">
          <div 
            v-for="msg in messages" :key="msg.id" 
            :class="['message-card', 'glass-card', { unread: msg.status === 0 }]"
            @click="readMessage(msg)"
          >
            <div class="msg-header">
              <div class="msg-title-row">
                <span class="unread-dot" v-if="msg.status === 0"></span>
                <span class="msg-title">{{ msg.title }}</span>
              </div>
              <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <p class="msg-content">{{ msg.content }}</p>
            <div class="msg-meta">
              <el-tag size="small" :type="msg.pushType === 'exchange' ? 'success' : msg.pushType === 'bargain' ? 'warning' : 'info'">
                {{ typeLabel(msg.pushType) }}
              </el-tag>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无系统通知" />
      </div>

      <!-- ==================== 私信 Tab ==================== -->
      <div v-if="activeTab === 'chat'" class="tab-content chat-layout">
        <!-- 会话列表 -->
        <div class="session-list glass-card">
          <div class="list-header">
            <h3>会话列表</h3>
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
            @click="openChatSession(session)"
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

        <!-- 聊天详情 -->
        <div class="chat-detail" v-if="chatStore.activeSessionId">
          <div class="chat-header glass-card">
            <div class="session-title">
              <span class="title">{{ getCurrentSession?.targetUserName || '对话' }}</span>
              <span class="online-dot online"></span>
            </div>
            <button class="back-btn" @click="chatStore.activeSessionId = null">
              <el-icon><ArrowLeft /></el-icon>
            </button>
          </div>

          <div class="context-goods-card" v-if="chatStore.currentContextGoods">
            <el-image
              :src="(chatStore.currentContextGoods.imageUrl || '').split(',')[0]"
              class="goods-img"
              fit="cover"
            >
              <template #error>
                <div class="image-error-slot">
                  <el-icon :size="24"><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </template>
            </el-image>
            <div class="goods-meta">
              <div class="goods-title">{{ chatStore.currentContextGoods.title }}</div>
              <div class="goods-price">¥{{ chatStore.currentContextGoods.price }}</div>
              <el-button size="small" type="primary" @click="goToGoods(chatStore.currentContextGoods.id)">查看商品</el-button>
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
              v-model="chatInput" type="textarea" :rows="2"
              placeholder="回复消息..." @keyup.enter="sendChatMsg"
              resize="none"
            />
            <el-button type="primary" @click="sendChatMsg">发送</el-button>
          </div>
        </div>

        <div class="empty-chat" v-else>
          <el-icon class="empty-chat-icon"><ChatDotRound /></el-icon>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { getUserMessages, markAsRead, markAllAsRead } from '../api/message'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const user = computed(() => userStore.currentUser || {})
const currentUserId = computed(() => user.value.id)

const activeTab = ref('system')
const chatInput = ref('')
const scrollbarRef = ref(null)

// 系统通知
const messages = ref([])
const unreadSystemCount = computed(() => messages.value.filter(m => m.status === 0).length)

// 私信
const getCurrentSession = computed(() => {
  return chatStore.sessionList.find(s => s.targetUserId === chatStore.activeSessionId)
})

const typeLabel = (type) => {
  const map = { exchange: '物品交换', bargain: '砍价', order: '订单', system: '系统通知' }
  return map[type] || type || '系统通知'
}

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

// 系统通知
const loadMessages = async () => {
  if (!user.value.id) return
  try {
    const res = await getUserMessages(user.value.id)
    if (res.data.code === 200) {
      messages.value = (res.data.data || []).sort((a, b) => {
        if (a.status === 0 && b.status !== 0) return -1
        if (a.status !== 0 && b.status === 0) return 1
        return (b.id || 0) - (a.id || 0)
      })
    }
  } catch (e) { /* handled */ }
}

const readMessage = async (msg) => {
  if (msg.status === 1) return
  try {
    await markAsRead(msg.id)
    msg.status = 1
  } catch (e) { /* handled */ }
}

const markAll = async () => {
  try {
    await markAllAsRead(user.value.id)
    ElMessage.success('已全部标记为已读')
    loadMessages()
  } catch (e) { /* handled */ }
}

// 私信
const loadSessions = async () => {
  if (!user.value.id) return
  try {
    const request = (await import('../utils/request')).default
    const res = await request.get(`/chat/sessions/${user.value.id}`)
    if (res.data.code === 200) {
      chatStore.sessionList = res.data.data
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

const openChatSession = (session) => {
  chatStore.selectSession(session)
  nextTick(() => scrollToBottom())
}

const sendChatMsg = () => {
  if (!chatInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  chatStore.sendMessage(chatInput.value.trim())
  chatInput.value = ''
  nextTick(() => scrollToBottom())
}

const scrollToBottom = () => {
  if (scrollbarRef.value) {
    scrollbarRef.value.setScrollTop(9999)
  }
}

const goToGoods = (goodsId) => {
  router.push(`/detail/${goodsId}`)
}

onMounted(() => {
  if (!userStore.isLoggedIn) { router.push('/'); return }
  loadMessages()
  if (currentUserId.value) {
    chatStore.initGlobalWebSocket(currentUserId.value)
    loadSessions()
  }
})

onUnmounted(() => {
  // 不关闭socket，保持全局连接
})
</script>

<style scoped>
.page-container { padding-top: 100px; padding-bottom: 80px; }
.glass-header { position: fixed; top: 20px; left: 50%; transform: translateX(-50%); width: 90%; max-width: 1100px; height: 65px; background: rgba(255,255,255,0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.3); border-radius: 20px; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; z-index: 100; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
.nav-brand { font-size: 20px; font-weight: 900; color: #409eff; cursor: pointer; }
.nav-links { display: flex; align-items: center; gap: 20px; }
.nav-links span { cursor: pointer; font-weight: 500; opacity: 0.7; transition: 0.3s; font-size: 14px; }
.nav-links span:hover { opacity: 1; color: #409eff; }
.nav-links span.active { color: #409eff; opacity: 1; }

/* Tab 切换栏 */
.tab-bar { display: flex; max-width: 900px; margin: 0 auto 20px; background: rgba(255,255,255,0.45); backdrop-filter: blur(10px); border-radius: 16px; border: 1px solid rgba(255,255,255,0.3); overflow: hidden; }
.tab-item { flex: 1; padding: 14px 0; text-align: center; cursor: pointer; font-weight: 600; font-size: 15px; color: #666; transition: 0.3s; position: relative; display: flex; align-items: center; justify-content: center; gap: 8px; }
.tab-item.active { color: #409eff; background: rgba(64,158,255,0.08); }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40px; height: 3px; background: #409eff; border-radius: 2px; }
.tab-item:hover { color: #409eff; }
.tab-badge { pointer-events: none; }

.tab-content { max-width: 900px; margin: 0 auto; padding: 0 20px; }

/* 系统通知 */
.section-title { display: flex; justify-content: space-between; align-items: center; max-width: 900px; margin: 0 auto; }
.message-list { display: flex; flex-direction: column; gap: 12px; }
.message-card { padding: 18px 20px; border-radius: 16px; cursor: pointer; transition: 0.3s; }
.message-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.message-card.unread { border-left: 3px solid #409eff; background: rgba(64,158,255,0.05); }
.msg-header { display: flex; justify-content: space-between; align-items: center; }
.msg-title-row { display: flex; align-items: center; gap: 8px; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: #409eff; display: inline-block; }
.msg-title { font-weight: 700; font-size: 15px; }
.msg-time { font-size: 12px; color: #bbb; }
.msg-content { color: #666; margin: 8px 0; font-size: 14px; line-height: 1.6; }
.msg-meta { margin-top: 6px; }

/* 私信布局 */
.chat-layout { display: flex; gap: 16px; max-width: 1000px; }
.session-list { width: 300px; flex-shrink: 0; border-radius: 16px; background: rgba(255,255,255,0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.5); overflow: hidden; display: flex; flex-direction: column; max-height: 520px; }
.list-header { padding: 16px; border-bottom: 1px solid rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; }
.list-header h3 { margin: 0; font-size: 16px; font-weight: bold; }
.session-count { font-size: 12px; color: #666; }
.empty-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px; }
.empty-icon { font-size: 48px; color: #ccc; margin-bottom: 15px; }
.empty-state p { margin: 5px 0; color: #666; }
.empty-hint { font-size: 13px; color: #999 !important; }
.session-item { display: flex; align-items: center; padding: 12px; cursor: pointer; transition: all 0.3s; border-bottom: 1px solid rgba(0,0,0,0.03); }
.session-item:hover { background: rgba(64,158,255,0.08); }
.session-item.active { background: rgba(255,255,255,0.5); border-left: 4px solid #409EFF; }
.avatar-area { margin-right: 10px; }
.avatar { width: 40px; height: 40px; border-radius: 50%; background: #409eff; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 16px; }
.session-info { flex: 1; overflow: hidden; }
.name-time { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.name { font-weight: 600; font-size: 14px; color: #333; }
.time { font-size: 11px; color: #999; }
.preview { font-size: 12px; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.unread-badge { margin-left: 8px; }

.chat-detail { flex: 1; display: flex; flex-direction: column; border-radius: 16px; background: rgba(255,255,255,0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.5); overflow: hidden; max-height: 520px; }
.chat-header { padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.05); background: rgba(255,255,255,0.5); }
.session-title { display: flex; align-items: center; gap: 8px; }
.title { font-weight: bold; font-size: 15px; }
.online-dot { width: 8px; height: 8px; border-radius: 50%; background-color: #67C23A; box-shadow: 0 0 5px #67C23A; }
.back-btn { padding: 4px 10px; border: none; background: rgba(0,0,0,0.05); border-radius: 8px; cursor: pointer; display: flex; align-items: center; }
.back-btn:hover { background: rgba(0,0,0,0.1); }
.context-goods-card { display: flex; padding: 12px; background: rgba(64,158,255,0.08); border-bottom: 1px solid rgba(0,0,0,0.05); }
.goods-img { width: 60px; height: 60px; border-radius: 10px; object-fit: cover; margin-right: 12px; }
.image-error-slot { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; border-radius: 10px; font-size: 11px; }
.goods-meta { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.goods-title { font-weight: 600; font-size: 13px; margin-bottom: 4px; }
.goods-price { font-size: 16px; font-weight: bold; color: #f56c6c; margin-bottom: 6px; }
.message-flow { flex: 1; padding: 16px; overflow-y: auto; }
.msg-bubble-wrapper { display: flex; flex-direction: column; margin-bottom: 12px; max-width: 70%; }
.msg-bubble-wrapper.mine { align-items: flex-end; margin-left: auto; }
.msg-bubble-wrapper.theirs { align-items: flex-start; }
.msg-bubble { padding: 10px 14px; border-radius: 14px; font-size: 13px; line-height: 1.5; }
.mine .msg-bubble { background: #409EFF; color: white; border-bottom-right-radius: 4px; }
.theirs .msg-bubble { background: rgba(255,255,255,0.9); color: #333; border-bottom-left-radius: 4px; }
.msg-status { font-size: 10px; color: #999; margin-top: 3px; }
.input-area { padding: 14px; display: flex; gap: 10px; align-items: flex-end; border-top: 1px solid rgba(255,255,255,0.3); background: rgba(255,255,255,0.2); }
.empty-chat { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 16px; background: rgba(255,255,255,0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.5); }
.empty-chat-icon { font-size: 64px; color: #ccc; margin-bottom: 20px; }
.empty-chat p { margin: 0; color: #666; }

.glass-card { background: rgba(255,255,255,0.45); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.3); }

@media (max-width: 768px) {
  .chat-layout { flex-direction: column; }
  .session-list { width: 100%; max-height: 250px; }
  .chat-detail { max-height: 400px; }
  .page-container { padding-top: 80px; }
}
</style>