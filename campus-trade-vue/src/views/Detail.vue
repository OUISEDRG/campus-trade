<template>
  <div class="detail-container">
    <!-- 顶部的返回按钮 -->
    <div class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon> 返回交易大厅
    </div>

    <!-- 整个详情大卡片（带毛玻璃效果） -->
    <div class="glass-card detail-card">
      <div class="detail-layout">
        
        <!-- 左边：展示商品大图 -->
        <div class="image-section">
          <!-- 这里会优先显示数据库里的真实图片，如果没有真实图片，就用一张网图代替 -->
          <img :src="(goods.imageUrl || '').split(',')[0] || `https://picsum.photos/seed/${route.params.id}/800/800`" class="main-img" />
        </div>

        <!-- 右边：展示商品文字信息 -->
        <div class="info-section">
          <h1 class="title">{{ goods.title }}</h1>
          <div class="price">￥{{ goods.price }}</div>
          
          <div class="meta-info">
            <div class="meta-item"><label>成色：</label><span :class="['condition-badge', `condition-${goods.goodsCondition}`]">{{ getConditionText(goods.goodsCondition) }}</span></div>
            <div class="meta-item"><label>浏览量：</label><span>👁️ {{ goods.viewCount || 0 }}</span></div>
            <div class="meta-item">
              <label>状态：</label>
              <span v-if="goods.status === 0" class="status-active">在售</span>
              <span v-else-if="goods.status === 1" class="status-off">已下架</span>
              <span v-else class="status-sold">已售</span>
            </div>
          </div>

          <div class="description">
            <h3>详情介绍</h3>
            <p>{{ goods.description }}</p>
          </div>

          <!-- 卖家信息小卡片 -->
          <div class="seller-card glass-card">
            <div class="seller-info">
              <!-- 显示卖家ID的第一个字母当做头像 -->
              <div class="avatar">{{ goods.userId?.toString().substring(0, 1).toUpperCase() || 'U' }}</div>
              <div class="name">
                <p>卖家ID: {{ goods.userId }}</p>
                <span>实名认证已通过</span>
              </div>
              <div class="seller-rating" v-if="sellerRating > 0">
                <el-rate v-model="sellerRating" disabled show-score text-color="#ff9900" />
                <span class="review-count">({{ sellerReviewCount }}条评价)</span>
              </div>
            </div>
            <div v-if="goods.userId === currentUserId" class="seller-actions">
              <button 
                v-if="goods.status === 0" 
                class="glass-btn danger full" 
                @click="handleOffShelves" 
              > 
                下架商品 
              </button>
              <button 
                v-else 
                class="glass-btn disabled full" 
                disabled 
              > 
                {{ goods.status === 1 ? '已下架' : '已售出' }}
              </button>
            </div>
            <div v-else>
              <button 
                v-if="goods.status === 0" 
                class="glass-btn primary full" 
                @click="handleBuy" 
              > 
                立即购买 
              </button>
              <button 
                v-if="goods.status === 0 && goods.userId !== currentUserId" 
                :class="['glass-btn', isFavorited ? 'secondary' : 'primary']" 
                @click="toggleFavorite" 
                style="margin-top: 10px;"
              > 
                {{ isFavorited ? '取消收藏' : '收藏宝贝' }}
              </button>
              <button 
                v-else 
                class="glass-btn disabled full" 
                disabled 
              > 
                {{ goods.status === 1 ? '已下架' : '已售罄' }}
              </button>
            </div>
          </div>

          <!-- 砍价与交换操作区 -->
          <div v-if="goods.status === 0 && goods.userId !== currentUserId" class="action-extras">
            <div class="extra-buttons">
              <button class="glass-btn bargain-btn" @click="showBargainDialog = true">
                🔨 发起砍价
              </button>
              <button class="glass-btn exchange-btn" @click="showExchangeDialog = true">
                🔄 以旧换新
              </button>
            </div>
          </div>
        </div>
        
      </div>
    </div>

    <!-- 砍价弹窗 -->
    <el-dialog v-model="showBargainDialog" title="发起砍价" width="90%" max-width="420px" class="glass-dialog">
      <el-form label-position="top">
        <el-form-item label="目标价格（你希望砍到的价格）">
          <div class="price-input-wrap">
            <span class="price-prefix">￥</span>
            <el-input-number v-model="bargainForm.targetPrice" :min="0.01" :max="goods.price" :precision="2" class="flex-1" />
          </div>
          <span class="form-hint">原价 ￥{{ goods.price }}，砍价后需卖家确认同意</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="glass-btn secondary" @click="showBargainDialog = false">取消</button>
        <button class="glass-btn primary" @click="submitBargain">发起砍价</button>
      </template>
    </el-dialog>

    <!-- 以旧换新弹窗 -->
    <el-dialog v-model="showExchangeDialog" title="以旧换新" width="90%" max-width="420px" class="glass-dialog">
      <el-form label-position="top">
        <el-form-item label="选择你的商品（用哪个商品来交换）">
          <el-select v-model="exchangeForm.myGoodsId" placeholder="请选择你的商品" class="w-full" filterable>
            <el-option v-for="g in myGoodsList" :key="g.id" :label="`${g.title} (￥${g.price})`" :value="g.id" />
          </el-select>
          <span class="form-hint" v-if="myGoodsList.length === 0">你还没有在售商品，请先发布商品</span>
        </el-form-item>
        <el-form-item label="留言（可选）">
          <el-input v-model="exchangeForm.message" type="textarea" :rows="2" placeholder="告诉对方你想交换的理由..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="glass-btn secondary" @click="showExchangeDialog = false">取消</button>
        <button class="glass-btn primary" @click="submitExchange">发送交换请求</button>
      </template>
    </el-dialog>

    <!-- 留言区 -->
    <div class="glass-card comment-section">
      <h3>留言互动 ({{ commentList.length }})</h3>
      
      <div class="comment-input-box">
        <el-input 
          v-model="newComment" 
          type="textarea" 
          :rows="3" 
          placeholder="对宝贝有疑问？快给卖家留言吧..." 
        />
        <button class="glass-btn primary" @click="submitComment" style="margin-top: 15px; width: 120px;">
          发送留言
        </button>
      </div>

      <div class="comment-list">
        <div v-for="item in commentList" :key="item.id" class="comment-item">
          <div class="comment-header">
            <span class="nickname">{{ item.nickname }}</span>
            <span class="time">{{ item.createTime?.replace('T', ' ') }}</span>
          </div>
          <p class="content">{{ item.content }}</p>
        </div>
        <el-empty v-if="commentList.length === 0" description="暂无留言，快来抢沙发~" :image-size="60" />
      </div>
    </div>

    <!-- 聊天窗口组件 -->
    <ChatWindow 
      v-if="goods.userId && goods.userId !== currentUserId" 
      :sellerId="goods.userId" 
      :sellerName="`卖家${goods.userId}`" 
      :goodsId="goods.id" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from '../utils/request'
import ChatWindow from '../components/ChatWindow.vue'
import { offShelvesGoods } from '../api/goods'
import { createBargain, getBargainsByGoods } from '../api/bargain'
import { createExchange } from '../api/exchange'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentUser = userStore.currentUser || {}
const currentUserId = currentUser.id

// 准备一个小盒子，用来装从后端拿回来的商品数据
const goods = ref({})

// 商品新旧程度
const conditionOptions = ['全新', '几乎全新', '轻微使用', '明显痕迹']
const getConditionText = (condition) => {
  return conditionOptions[condition] || '几乎全新'
}
const newComment = ref('')
const commentList = ref([])
const sellerRating = ref(0)
const sellerReviewCount = ref(0)
const isFavorited = ref(false)

// 砍价相关
const showBargainDialog = ref(false)
const bargainForm = reactive({ targetPrice: null })
const goodsBargains = ref([])

// 换新相关
const showExchangeDialog = ref(false)
const exchangeForm = reactive({ myGoodsId: null, message: '' })
const myGoodsList = ref([])

onMounted(async () => {
  // 🌟 核心魔法：页面一打开，就带着当前页面的 ID 去找后端要数据
  try {
    const res = await request.get(`/goods/${route.params.id}`)
    if (res.data.code === 200) {
      goods.value = res.data.data // 把拿到的真实商品信息存进盒子里
      loadSellerRating()
    }
    loadComments()
    checkFavorite()
  } catch (error) {
    console.error('糟糕，获取商品详情失败啦:', error)
  }
})

const loadComments = async () => {
  const res = await request.get('/comment/list', { params: { goodsId: route.params.id } })
  if (res.data.code === 200) commentList.value = res.data.data
}

const submitComment = async () => {
  if (!newComment.value.trim()) return ElMessage.warning('留言内容不能为空哦')
  
  const cu = userStore.currentUser || {}
  const res = await request.post('/comment/add', {
    goodsId: route.params.id,
    userId: cu.id,
    nickname: cu.nickname || cu.username,
    content: newComment.value
  })

  if (res.data.code === 200) {
    ElMessage.success('留言成功！')
    newComment.value = ''
    loadComments() // 刷新列表
  }
}

const handleOffShelves = () => {
  ElMessageBox.confirm('真的要狠心下架这个宝贝吗？下架后首页大厅将不再展示。', '下架确认', {
    confirmButtonText: '果断下架',
    cancelButtonText: '我再想想',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await offShelvesGoods(goods.value.id)
      if (res.data.code === 200) {
        ElMessage.success('商品已成功下架！')
        goods.value.status = 1
      } else {
        ElMessage.error(res.data.message || '操作失败，请重试')
      }
    } catch (error) {
      ElMessage.error('操作失败，请重试')
    }
  }).catch(() => {})
}

const loadGoodsDetail = async () => {
  try {
    const res = await request.get(`/goods/${route.params.id}`)
    if (res.data.code === 200) {
      goods.value = res.data.data
    }
  } catch (error) {
    console.error('获取商品详情失败', error)
  }
}

const loadSellerRating = async () => {
  if (!goods.value.userId) return
  try {
    const res = await request.get(`/review/user/${goods.value.userId}`)
    if (res.data.code === 200) {
      sellerRating.value = res.data.data.avgRating
      sellerReviewCount.value = res.data.data.totalReviews
    }
  } catch (e) { /* ignore */ }
}

const handleBuy = () => {
  const cu = userStore.currentUser || {}
  
  if (cu.id === goods.value.userId) {
    return ElMessage.warning('不能买自己发布的商品哦！')
  }

  ElMessageBox.confirm('确定要购买这件心仪的宝贝吗？', '提示', {
    confirmButtonText: '确定购买',
    cancelButtonText: '再想想',
    type: 'success',
  }).then(async () => {
    // 调起后端收银台
    const res = await request.post('/orders/buy', null, {
      params: {
        goodsId: goods.value.id,
        buyerId: currentUser.id
      }
    })

    if (res.data.code === 200) {
      ElMessage.success('恭喜你，抢购成功！')
      // 购买成功后刷新一下页面数据，按钮会自动变灰色
      const detailRes = await request.get(`/goods/${route.params.id}`)
      goods.value = detailRes.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  })
}

const checkFavorite = async () => {
  if (!currentUserId) return
  try {
    const res = await request.get('/favorite/check', {
      params: { userId: currentUserId, goodsId: route.params.id }
    })
    if (res.data.code === 200) isFavorited.value = res.data.data
  } catch (e) { /* ignore */ }
}

const toggleFavorite = async () => {
  if (isFavorited.value) {
    const res = await request.delete('/favorite/remove', {
      params: { userId: currentUserId, goodsId: route.params.id }
    })
    if (res.data.code === 200) {
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    }
  } else {
    const res = await request.post('/favorite/add', null, {
      params: { userId: currentUserId, goodsId: route.params.id }
    })
    if (res.data.code === 200) {
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  }
}

// --- 砍价功能 ---
const loadGoodsBargains = async () => {
  try {
    const res = await getBargainsByGoods(route.params.id)
    if (res.data.code === 200 && res.data.data) {
      goodsBargains.value = Array.isArray(res.data.data) ? res.data.data : [res.data.data]
    }
  } catch (e) { /* skip */ }
}

const submitBargain = async () => {
  if (!bargainForm.targetPrice || bargainForm.targetPrice <= 0) {
    return ElMessage.warning('请输入有效的目标价格')
  }
  if (bargainForm.targetPrice >= goods.value.price) {
    return ElMessage.warning('目标价格必须低于商品原价')
  }
  try {
    const res = await createBargain({
      goodsId: Number(route.params.id),
      userId: currentUserId,
      targetPrice: bargainForm.targetPrice
    })
    if (res.data.code === 200) {
      ElMessage.success('砍价请求已发送！请等待卖家确认')
      showBargainDialog.value = false
      bargainForm.targetPrice = null
      loadGoodsBargains()
    }
  } catch (e) { /* handled by interceptor */ }
}

// --- 以旧换新功能 ---
const loadMyGoods = async () => {
  try {
    const res = await request.get('/goods/list', {
      params: { userId: currentUserId, page: 1, pageSize: 100 }
    })
    if (res.data.code === 200) {
      const all = res.data.data.records || []
      myGoodsList.value = all.filter(g => g.status === 0 && g.id !== Number(route.params.id))
    }
  } catch (e) { /* skip */ }
}

const submitExchange = async () => {
  if (!exchangeForm.myGoodsId) {
    return ElMessage.warning('请选择你要交换的商品')
  }
  try {
    const res = await createExchange({
      initiatorId: currentUserId,
      targetUserId: goods.value.userId,
      initiatorGoodsId: exchangeForm.myGoodsId,
      targetGoodsId: Number(route.params.id),
      message: exchangeForm.message
    })
    if (res.data.code === 200) {
      ElMessage.success('交换请求已发送！请等待卖家回复')
      showExchangeDialog.value = false
      exchangeForm.myGoodsId = null
      exchangeForm.message = ''
    }
  } catch (e) { /* handled by interceptor */ }
}

// 监听弹窗打开，加载数据
watch(showBargainDialog, (val) => { if (val) loadGoodsBargains() })
watch(showExchangeDialog, (val) => { if (val) loadMyGoods() })
</script>

<style scoped>
.detail-container { padding: 40px 20px; max-width: 1000px; margin: 0 auto; }

/* 返回按钮样式 */
.back-btn { 
  margin-bottom: 20px; display: inline-flex; align-items: center; 
  cursor: pointer; opacity: 0.7; transition: 0.3s; font-weight: bold;
}
.back-btn:hover { opacity: 1; transform: translateX(-5px); color: var(--primary-color); }

/* 毛玻璃通用样式 */
.glass-card {
  background: var(--glass-bg, rgba(255, 255, 255, 0.45));
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.3));
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
}

.detail-card { border-radius: 30px; padding: 40px; overflow: hidden; margin-bottom: 30px; }

/* 左右两列排版 */
.detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }

/* 图片样式 */
.main-img { 
  width: 100%; height: 100%; max-height: 500px; 
  object-fit: cover; border-radius: 20px; 
  box-shadow: 0 10px 30px rgba(0,0,0,0.1); 
}

/* 文字信息样式 */
.title { font-size: 32px; margin: 0; }
.price { font-size: 36px; color: #f56c6c; font-weight: 900; margin: 20px 0; }

.meta-info { display: flex; gap: 20px; opacity: 0.6; margin-bottom: 30px; }
.condition-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; color: white; }
.condition-0 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.condition-1 { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.condition-2 { background: linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%); }
.condition-3 { background: linear-gradient(135deg, #434343 0%, #000000 100%); }

.description h3 { border-left: 4px solid var(--primary-color, #409eff); padding-left: 10px; }
.description p { line-height: 1.8; opacity: 0.8; white-space: pre-wrap; }

/* 卖家卡片样式 */
.seller-card { margin-top: 40px; padding: 20px; border-radius: 20px; }
.seller-info { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
.avatar { 
  width: 45px; height: 45px; background: var(--primary-color, #409eff); 
  color: white; border-radius: 50%; display: flex; 
  align-items: center; justify-content: center; font-weight: bold; font-size: 20px;
}
.name p { margin: 0; font-weight: bold; }
.name span { font-size: 12px; color: #67c23a; }

/* 按钮样式 */
.glass-btn { border: none; border-radius: 15px; padding: 12px 30px; cursor: pointer; transition: 0.3s; font-weight: bold; }
.glass-btn.primary { background: var(--primary-color, #409eff); color: white; }
.glass-btn.full { width: 100%; }
.glass-btn.disabled {
  background: rgba(150, 150, 150, 0.4);
  cursor: not-allowed;
  color: #eee;
}
.glass-btn.danger {
  background: rgba(245, 108, 108, 0.9);
  color: white;
}
.glass-btn.danger:hover {
  background: #f56c6c;
}
.glass-btn.secondary {
  background: rgba(255,255,255,0.4);
  backdrop-filter: blur(5px);
  color: #333;
  border: 1px solid rgba(255,255,255,0.5);
}
.glass-btn.secondary:hover { background: rgba(255,255,255,0.6); }

/* 砍价与交换操作区 */
.action-extras { margin-top: 16px; }
.extra-buttons { display: flex; gap: 10px; }
.glass-btn.bargain-btn {
  flex: 1; background: linear-gradient(135deg, #e6a23c, #f0ad4e); color: white;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}
.glass-btn.bargain-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(230, 162, 60, 0.4); }
.glass-btn.exchange-btn {
  flex: 1; background: linear-gradient(135deg, #67c23a, #85ce61); color: white;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}
.glass-btn.exchange-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(103, 194, 58, 0.4); }

/* 弹窗通用 */
.glass-dialog :deep(.el-dialog) {
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.4);
}
.w-full { width: 100%; }
.flex-1 { flex: 1; }
.price-input-wrap { display: flex; align-items: center; gap: 10px; padding: 10px 14px; background: rgba(255,255,255,0.5); border-radius: 12px; border: 1px solid rgba(255,255,255,0.3); }
.price-prefix { font-size: 20px; font-weight: 700; color: #f56c6c; }
.form-hint { display: block; font-size: 12px; color: #999; margin-top: 4px; }

.status-active { color: #67c23a; font-weight: bold; }
.status-off { color: #f56c6c; font-weight: bold; }
.status-sold { color: #909399; font-weight: bold; }

/* 3. 留言区精美样式 */
.comment-section { padding: 30px; border-radius: 20px; text-align: left; }
.comment-input-box { margin-bottom: 30px; border-bottom: 1px solid rgba(255,255,255,0.2); padding-bottom: 20px; }
.comment-item { padding: 15px 0; border-bottom: 1px solid rgba(255,255,255,0.1); }
.comment-header { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
.nickname { font-weight: bold; color: #409eff; }
.time { opacity: 0.5; }
.content { margin: 0; font-size: 15px; color: #333; line-height: 1.6; }

/* 手机屏幕适配：变成上下排版 */
@media (max-width: 768px) {
  .detail-layout { grid-template-columns: 1fr; }
}
</style>
