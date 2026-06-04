<template>
  <div class="page-container">
    <header class="glass-header pc-only">
      <div class="nav-brand" @click="router.push('/home')">CampusTrade</div>
      <nav class="nav-links">
        <span @click="router.push('/home')">首页</span>
        <span @click="router.push('/exchange')">以旧换新</span>
        <span @click="router.push('/bargain')">砍价专区</span>
        <span @click="router.push('/notifications')" class="active">通知</span>
        <span @click="router.push('/me')">我的</span>
      </nav>
    </header>

    <main class="main-body">
      <div class="section-title">
        <h2>📬 系统通知</h2>
        <el-button type="primary" round size="small" @click="markAll">全部已读</el-button>
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
            <el-tag size="small" type="info">{{ msg.pushType || '系统通知' }}</el-tag>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无系统通知" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getUserMessages, markAsRead, markAllAsRead } from '../api/message'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.currentUser || {})

const messages = ref([])

onMounted(() => {
  if (!userStore.isLoggedIn) { router.push('/'); return }
  loadMessages()
})

const loadMessages = async () => {
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
    const res = await markAllAsRead(user.value.id)
    ElMessage.success(res.data?.message || '已全部标记为已读')
    loadMessages()
  } catch (e) { /* handled */ }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.page-container { padding-top: 100px; padding-bottom: 80px; }
.glass-header { position: fixed; top: 20px; left: 50%; transform: translateX(-50%); width: 90%; max-width: 1100px; height: 65px; background: rgba(255,255,255,0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.3); border-radius: 20px; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; z-index: 100; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
.nav-brand { font-size: 20px; font-weight: 900; color: #409eff; cursor: pointer; }
.nav-links { display: flex; align-items: center; gap: 20px; }
.nav-links span { cursor: pointer; font-weight: 500; opacity: 0.7; transition: 0.3s; font-size: 14px; }
.nav-links span:hover { opacity: 1; color: #409eff; }
.nav-links span.active { color: #409eff; opacity: 1; }
.section-title { display: flex; justify-content: space-between; align-items: center; max-width: 900px; margin: 0 auto; padding: 0 20px; }
.message-list { max-width: 900px; margin: 0 auto; padding: 20px; display: flex; flex-direction: column; gap: 12px; }
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
.glass-card { background: rgba(255,255,255,0.45); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.3); }
</style>
