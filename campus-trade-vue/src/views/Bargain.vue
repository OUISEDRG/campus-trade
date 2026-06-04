<template>
  <div class="page-container">
    <header class="glass-header pc-only">
      <div class="nav-brand" @click="router.push('/home')">CampusTrade</div>
      <nav class="nav-links">
        <span @click="router.push('/home')">首页</span>
        <span @click="router.push('/exchange')">以旧换新</span>
        <span @click="router.push('/bargain')" class="active">砍价专区</span>
        <span @click="router.push('/notifications')">通知</span>
        <span @click="router.push('/me')">我的</span>
      </nav>
    </header>

    <main class="main-body">
      <div class="section-title">
        <h2>🔨 砍价专区</h2>
        <el-button type="primary" round @click="showCreate = true">发起砍价</el-button>
      </div>

      <!-- 砍价活动列表 -->
      <div class="bargain-grid" v-if="bargains.length > 0">
        <div v-for="item in bargains" :key="item.id" class="bargain-card glass-card">
          <div class="card-header">
            <span class="goods-name">{{ item.goodsTitle || '商品#' + item.goodsId }}</span>
            <el-tag :type="item.status === 0 ? 'warning' : item.status === 1 ? 'success' : 'info'" size="small">
              {{ item.status === 0 ? '进行中' : item.status === 1 ? '已成功' : '已取消' }}
            </el-tag>
          </div>
          <div class="price-section">
            <div class="price-item">
              <span class="label">原价</span>
              <span class="price original">￥{{ item.originalPrice }}</span>
            </div>
            <div class="price-item">
              <span class="label">当前价</span>
              <span class="price current">￥{{ item.currentPrice }}</span>
            </div>
            <div class="price-item">
              <span class="label">目标价</span>
              <span class="price target">￥{{ item.targetPrice }}</span>
            </div>
          </div>
          <div class="progress-section">
            <el-progress 
              :percentage="getProgress(item)" 
              :color="getProgressColor(item)"
              :stroke-width="12"
            />
            <span class="participant-count">{{ item.currentParticipants || 0 }}/{{ item.maxParticipants }} 人参与</span>
          </div>
          <div class="card-actions" v-if="item.status === 0">
            <button class="glass-btn primary small" @click="joinBargain(item)">帮砍一刀</button>
            <button class="glass-btn secondary small" @click="viewRecords(item)">砍价记录</button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无砍价活动，快来发起第一个吧！" />

      <!-- 砍价记录弹窗 -->
      <el-dialog v-model="showRecords" title="砍价记录" width="90%" max-width="450px" class="glass-dialog">
        <div class="record-list" v-if="records.length > 0">
          <div v-for="r in records" :key="r.id" class="record-item">
            <span>👤 用户#{{ r.userId }}</span>
            <span class="cut-amount">砍掉 ￥{{ r.cutAmount }}</span>
            <span class="after-price">剩余 ￥{{ r.afterPrice }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无砍价记录" />
      </el-dialog>

      <!-- 发起砍价弹窗 -->
      <el-dialog v-model="showCreate" title="发起砍价" width="90%" max-width="450px" class="glass-dialog">
        <el-form label-position="top">
          <el-form-item label="商品ID">
            <el-input-number v-model="createForm.goodsId" :min="1" class="w-full" />
          </el-form-item>
          <el-form-item label="目标价格">
            <el-input-number v-model="createForm.targetPrice" :min="0" :precision="2" class="w-full" />
          </el-form-item>
          <el-form-item label="最大参与人数">
            <el-input-number v-model="createForm.maxParticipants" :min="2" :max="100" class="w-full" />
          </el-form-item>
        </el-form>
        <template #footer>
          <button class="glass-btn secondary" @click="showCreate = false">取消</button>
          <button class="glass-btn primary" @click="submitCreate">发起砍价</button>
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
import { createBargain, participateBargain, getBargainsByGoods, getBargainRecords } from '../api/bargain'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.currentUser || {})

const bargains = ref([])
const showCreate = ref(false)
const showRecords = ref(false)
const records = ref([])
const createForm = ref({ goodsId: 1, targetPrice: 10, maxParticipants: 5 })

onMounted(async () => {
  if (!userStore.isLoggedIn) { router.push('/'); return }
  await loadBargains()
})

const loadBargains = async () => {
  try {
    // 加载最新商品关联的砍价活动
    const goodsRes = await request.get('/goods/list', { params: { page: 1, pageSize: 6 } })
    if (goodsRes.data.code === 200) {
      const goodsList = goodsRes.data.data.records || []
      const allBargains = []
      for (const g of goodsList) {
        try {
          const res = await getBargainsByGoods(g.id)
          if (res.data.code === 200 && res.data.data) {
            const list = Array.isArray(res.data.data) ? res.data.data : [res.data.data]
            for (const b of list) {
              b.goodsTitle = g.title
              allBargains.push(b)
            }
          }
        } catch (e) { /* skip */ }
      }
      bargains.value = allBargains
    }
  } catch (e) {
    console.error('加载砍价失败:', e)
  }
}

const submitCreate = async () => {
  if (!createForm.value.goodsId || !createForm.value.targetPrice) {
    return ElMessage.warning('请填写完整信息')
  }
  try {
    const res = await createBargain({ ...createForm.value, userId: user.value.id })
    if (res.data.code === 200) {
      ElMessage.success('砍价活动已发起！')
      showCreate.value = false
      loadBargains()
    }
  } catch (e) { /* error handled by interceptor */ }
}

const joinBargain = async (item) => {
  try {
    const res = await participateBargain({ bargainId: item.id, userId: user.value.id })
    if (res.data.code === 200) {
      const record = res.data.data
      ElMessage.success(`砍价成功！帮砍掉了 ￥${record.cutAmount}`)
      loadBargains()
    }
  } catch (e) { /* handled */ }
}

const viewRecords = async (item) => {
  try {
    const res = await getBargainRecords(item.id)
    if (res.data.code === 200) {
      records.value = res.data.data || []
      showRecords.value = true
    }
  } catch (e) { /* handled */ }
}

const getProgress = (item) => {
  if (item.originalPrice <= 0 || item.targetPrice >= item.originalPrice) return 0
  const totalDrop = item.originalPrice - item.targetPrice
  const currentDrop = item.originalPrice - (item.currentPrice || item.originalPrice)
  return Math.min(Math.round((currentDrop / totalDrop) * 100), 100)
}

const getProgressColor = (item) => {
  const pct = getProgress(item)
  if (pct >= 80) return '#67c23a'
  if (pct >= 40) return '#409eff'
  return '#e6a23c'
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
.bargain-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; padding: 20px; max-width: 900px; margin: 0 auto; }
.bargain-card { padding: 24px; border-radius: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.goods-name { font-weight: 700; font-size: 16px; }
.price-section { display: flex; justify-content: space-between; margin-bottom: 16px; }
.price-item { text-align: center; }
.price-item .label { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.price { font-weight: 900; font-size: 18px; }
.price.original { color: #999; text-decoration: line-through; }
.price.current { color: #f56c6c; }
.price.target { color: #67c23a; }
.progress-section { margin-bottom: 16px; }
.participant-count { display: block; text-align: center; font-size: 12px; color: #999; margin-top: 6px; }
.card-actions { display: flex; gap: 10px; }
.glass-btn { border: none; border-radius: 12px; padding: 12px 30px; cursor: pointer; font-weight: 600; font-size: 15px; transition: 0.3s; }
.glass-btn.primary { background: linear-gradient(135deg, #409eff, #66b1ff); color: white; box-shadow: 0 4px 15px rgba(64,158,255,0.3); }
.glass-btn.primary:hover { transform: translateY(-2px); }
.glass-btn.secondary { background: rgba(255,255,255,0.4); backdrop-filter: blur(5px); color: #333; border: 1px solid rgba(255,255,255,0.5); }
.glass-btn.small { padding: 8px 16px; font-size: 13px; flex: 1; }
.glass-card { background: rgba(255,255,255,0.45); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.3); }
.w-full { width: 100%; }
.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: rgba(255,255,255,0.4); border-radius: 10px; }
.cut-amount { color: #f56c6c; font-weight: 700; }
.after-price { color: #409eff; }
.glass-dialog :deep(.el-dialog) { background: rgba(255,255,255,0.7); backdrop-filter: blur(20px); border-radius: 24px; }
</style>
