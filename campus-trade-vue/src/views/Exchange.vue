<template>
  <div class="page-container">
    <header class="glass-header pc-only">
      <div class="nav-brand" @click="router.push('/home')">CampusTrade</div>
      <nav class="nav-links">
        <span @click="router.push('/home')">首页</span>
        <span @click="router.push('/exchange')" class="active">以旧换新</span>
        <span @click="router.push('/bargain')">砍价专区</span>
        <span @click="router.push('/notifications')">通知</span>
        <span @click="router.push('/me')">我的</span>
      </nav>
    </header>

    <main class="main-body">
      <div class="section-title">
        <h2>🔄 以旧换新 / 物品交换</h2>
        <el-button type="primary" round @click="showCreate = true">发起交换</el-button>
      </div>

      <!-- Tab切换 -->
      <div class="tab-bar">
        <span :class="{ active: activeTab === 'my' }" @click="activeTab = 'my'; loadMyExchanges()">我的交换</span>
        <span :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'; loadPendingExchanges()">
          待处理
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-inline" />
        </span>
      </div>

      <!-- 我的交换记录 -->
      <div v-if="activeTab === 'my'" class="exchange-list">
        <div v-for="item in myExchanges" :key="item.id" class="exchange-card glass-card">
          <div class="exchange-header">
            <span>交换请求 #{{ item.id }}</span>
            <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
          </div>
          <div class="exchange-info">
            <div class="user-info">
              <span class="label">发起方商品ID：</span><span>{{ item.initiatorGoodsId }}</span>
            </div>
            <div class="user-info">
              <span class="label">目标商品ID：</span><span>{{ item.targetGoodsId }}</span>
            </div>
            <div v-if="item.message" class="exchange-msg">
              <span class="label">留言：</span><span>{{ item.message }}</span>
            </div>
          </div>
          <div class="exchange-time">{{ item.createTime }}</div>
        </div>
        <el-empty v-if="myExchanges.length === 0" description="暂无交换记录" />
      </div>

      <!-- 待处理交换 -->
      <div v-if="activeTab === 'pending'" class="exchange-list">
        <div v-for="item in pendingExchanges" :key="item.id" class="exchange-card glass-card highlight-border">
          <div class="exchange-header">
            <span>交换请求 #{{ item.id }}</span>
            <el-tag type="warning" size="small">待响应</el-tag>
          </div>
          <div class="exchange-info">
            <div class="user-info">
              <span class="label">发起方ID：</span><span>{{ item.initiatorId }}</span>
            </div>
            <div class="user-info">
              <span class="label">发起方商品：</span><span>#{{ item.initiatorGoodsId }}</span>
            </div>
            <div class="user-info">
              <span class="label">目标商品：</span><span>#{{ item.targetGoodsId }}</span>
            </div>
            <div v-if="item.message" class="exchange-msg">
              <span class="label">留言：</span><span>{{ item.message }}</span>
            </div>
          </div>
          <div class="card-actions">
            <button class="glass-btn success small" @click="respond(item.id, 1)">接受交换</button>
            <button class="glass-btn danger small" @click="respond(item.id, 2)">拒绝</button>
          </div>
        </div>
        <el-empty v-if="pendingExchanges.length === 0" description="暂无待处理的交换请求" />
      </div>

      <!-- 发起交换弹窗 -->
      <el-dialog v-model="showCreate" title="发起物品交换" width="90%" max-width="450px" class="glass-dialog">
        <el-form label-position="top">
          <el-form-item label="目标用户ID（对方的ID）">
            <el-input-number v-model="createForm.targetUserId" :min="1" class="w-full" />
          </el-form-item>
          <el-form-item label="我的商品ID">
            <el-input-number v-model="createForm.initiatorGoodsId" :min="1" class="w-full" />
          </el-form-item>
          <el-form-item label="目标商品ID（对方的商品）">
            <el-input-number v-model="createForm.targetGoodsId" :min="1" class="w-full" />
          </el-form-item>
          <el-form-item label="留言（可选）">
            <el-input v-model="createForm.message" type="textarea" :rows="2" placeholder="告诉对方你想交换的理由..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <button class="glass-btn secondary" @click="showCreate = false">取消</button>
          <button class="glass-btn primary" @click="submitCreate">发送请求</button>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { createExchange, getUserExchanges, getPendingExchanges, respondExchange } from '../api/exchange'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.currentUser || {})

const activeTab = ref('my')
const myExchanges = ref([])
const pendingExchanges = ref([])
const pendingCount = ref(0)
const showCreate = ref(false)
const createForm = ref({ targetUserId: 1, initiatorGoodsId: 1, targetGoodsId: 1, message: '' })

onMounted(() => {
  if (!userStore.isLoggedIn) { router.push('/'); return }
  loadMyExchanges()
  loadPendingExchanges()
})

const loadMyExchanges = async () => {
  try {
    const res = await getUserExchanges(user.value.id)
    if (res.data.code === 200) myExchanges.value = res.data.data || []
  } catch (e) { /* handled */ }
}

const loadPendingExchanges = async () => {
  try {
    const res = await getPendingExchanges(user.value.id)
    if (res.data.code === 200) {
      pendingExchanges.value = res.data.data || []
      pendingCount.value = pendingExchanges.value.length
    }
  } catch (e) { /* handled */ }
}

const submitCreate = async () => {
  if (!createForm.value.targetUserId || !createForm.value.initiatorGoodsId || !createForm.value.targetGoodsId) {
    return ElMessage.warning('请填写完整信息')
  }
  try {
    const res = await createExchange({ ...createForm.value, initiatorId: user.value.id })
    if (res.data.code === 200) {
      ElMessage.success('交换请求已发送！')
      showCreate.value = false
      createForm.value = { targetUserId: 1, initiatorGoodsId: 1, targetGoodsId: 1, message: '' }
      loadMyExchanges()
    }
  } catch (e) { /* handled */ }
}

const respond = async (id, status) => {
  try {
    const res = await respondExchange(id, { status })
    if (res.data.code === 200) {
      ElMessage.success(status === 1 ? '已接受交换！' : '已拒绝交换')
      loadMyExchanges()
      loadPendingExchanges()
    }
  } catch (e) { /* handled */ }
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}
const getStatusText = (status) => {
  const map = { 0: '待确认', 1: '已接受', 2: '已拒绝', 3: '已取消' }
  return map[status] || '未知'
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
.tab-bar { display: flex; gap: 0; max-width: 900px; margin: 15px auto 0; padding: 0 20px; }
.tab-bar span { padding: 10px 24px; cursor: pointer; border-radius: 12px 12px 0 0; font-weight: 600; font-size: 14px; opacity: 0.6; transition: 0.3s; background: rgba(255,255,255,0.2); }
.tab-bar span.active { opacity: 1; background: rgba(255,255,255,0.5); color: #409eff; }
.badge-inline { margin-left: 6px; }
.exchange-list { max-width: 900px; margin: 0 auto; padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.exchange-card { padding: 20px; border-radius: 16px; }
.exchange-card.highlight-border { border: 2px solid #e6a23c; }
.exchange-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.exchange-header span:first-child { font-weight: 700; font-size: 15px; }
.exchange-info { display: flex; flex-direction: column; gap: 8px; margin-bottom: 10px; }
.user-info .label { color: #999; font-size: 13px; }
.exchange-msg { padding: 8px 12px; background: rgba(64,158,255,0.08); border-radius: 8px; font-size: 13px; }
.exchange-msg .label { color: #409eff; font-weight: 600; }
.exchange-time { font-size: 12px; color: #bbb; text-align: right; }
.card-actions { display: flex; gap: 10px; margin-top: 12px; }
.glass-btn { border: none; border-radius: 12px; padding: 12px 30px; cursor: pointer; font-weight: 600; font-size: 15px; transition: 0.3s; }
.glass-btn.primary { background: linear-gradient(135deg, #409eff, #66b1ff); color: white; }
.glass-btn.primary:hover { transform: translateY(-2px); }
.glass-btn.secondary { background: rgba(255,255,255,0.4); color: #333; border: 1px solid rgba(255,255,255,0.5); }
.glass-btn.success { background: #67c23a; color: white; flex: 1; }
.glass-btn.danger { background: #f56c6c; color: white; flex: 1; }
.glass-btn.small { padding: 8px 16px; font-size: 13px; }
.glass-card { background: rgba(255,255,255,0.45); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.3); }
.w-full { width: 100%; }
.glass-dialog :deep(.el-dialog) { background: rgba(255,255,255,0.7); backdrop-filter: blur(20px); border-radius: 24px; }
</style>
