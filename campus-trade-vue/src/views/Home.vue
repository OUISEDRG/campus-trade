<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <header class="glass-header pc-only">
      <div class="nav-brand" @click="router.push('/home')">CampusTrade</div>
      <div class="glass-search">
        <el-icon><Search /></el-icon>
        <input type="text" v-model="searchQuery" placeholder="搜搜心仪好物..." @keyup.enter="handleSearch" />
      </div>
      <nav class="nav-links">
        <span @click="router.push('/home')" class="active">首页</span>
        <span @click="router.push('/category')">分类</span>
        <span @click="router.push('/me')">我的</span>
        <div class="message-badge" @click="router.push('/messages')">
          <el-badge :value="Math.max(0, chatStore?.unreadTotal || 0)" :hidden="!chatStore?.unreadTotal || chatStore.unreadTotal <= 0" class="msg-badge-wrapper">
            <span class="nav-text">消息</span>
          </el-badge>
        </div>
        <div class="user-badge" @click="router.push('/me')">{{ user.username?.[0]?.toUpperCase() }}</div>
      </nav>
    </header>

    <main class="main-body">
      <!-- 轮播图 -->
      <div class="banner-section">
        <el-carousel :interval="4000" type="card" height="280px">
          <el-carousel-item v-for="item in carouselGoods" :key="item.id">
            <div class="banner-card" @click="toDetail(item.id)">
              <img :src="(item.imageUrl || '').split(',')[0] || defaultCarouselImage" @error="handleImageError($event, defaultCarouselImage)" />
              <div class="banner-glass-info">
                <h3 class="text-ellipsis">{{ item.title }}</h3>
                <p style="color: #ffdeeb; font-weight: bold; font-size: 18px;">￥{{ item.price }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 标题和发布按钮 -->
      <div class="section-title">
        <h2>最新闲置</h2>
        <el-button type="primary" round @click="openPublish">发布闲置</el-button>
      </div>

      <!-- 商品展示 -->
      <div class="goods-grid">
        <div v-for="item in goods" :key="item.id" class="item-card glass-card" @click="toDetail(item.id)">
          <div class="item-img">
            <img :src="(item.imageUrl || '').split(',')[0] || `https://picsum.photos/seed/${item.id}/400/400`" @error="handleImageError($event, defaultImage)" />
            <!-- 🌟 这里会自动显示商品的真实分类标签 -->
            <div class="category-tag">{{ item.categoryName || '其他' }}</div>
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <div class="item-price">￥{{ item.price }}</div>
            <div class="item-footer">
              <span>{{ item.description?.substring(0, 10) }}...</span>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 🌟 发布弹窗：包含了分类下拉框 -->
    <el-dialog v-model="showPublish" title="分享闲置" width="90%" max-width="500px" class="glass-dialog">
      <el-form label-position="top" class="publish-form">
        <el-form-item label="商品照片" class="form-item">
          <div class="upload-section">
            <MultiImageUpload v-model="publishForm.imageUrl" :limit="5" :max-size="3" />
          </div>
        </el-form-item>

        <el-form-item label="物品名称" class="form-item">
          <el-input v-model="publishForm.title" placeholder="给你的宝贝起个好名字" class="custom-input" />
        </el-form-item>

        <el-form-item label="所属分类" class="form-item">
          <el-select v-model="publishForm.categoryName" placeholder="请选择物品分类" class="custom-select">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="预期价格" class="form-item">
          <div class="price-input-wrapper">
            <span class="price-prefix">￥</span>
            <el-input-number v-model="publishForm.price" :precision="2" :min="0" :controls-position="right" class="price-input" />
          </div>
        </el-form-item>

        <el-form-item label="详细描述" class="form-item">
          <el-input 
            type="textarea" 
            v-model="publishForm.description" 
            :rows="4"
            placeholder="请详细描述你的闲置物品：新旧程度、入手渠道、是否有瑕疵等..." 
            class="custom-textarea" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="glass-btn secondary" @click="showPublish = false">取消</button>
          <button class="glass-btn primary" @click="submitPublish">确认发布</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight, ChatDotRound, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import MultiImageUpload from '../components/MultiImageUpload.vue'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.currentUser || {})
const goods = ref([])
const carouselGoods = ref([])
const showPublish = ref(false)
const searchQuery = ref('')
const chatStore = useChatStore()

const defaultImage = 'https://picsum.photos/seed/default/400/400'
const defaultCarouselImage = 'https://picsum.photos/seed/carousel/800/400'

const handleImageError = (event, fallbackUrl) => {
  event.target.src = fallbackUrl
  event.target.onerror = null
}

// 🌟 这里必须和分类页的选项一模一样
const categories = ['教材书籍', '数码电子', '生活用品', '美妆服饰', '运动健身', '其他闲置']

const publishForm = reactive({ 
  title: '', 
  price: 0, 
  description: '', 
  imageUrl: '',
  categoryName: '' // 🌟 选中的分类会存在这里
})

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  loadGoods()
  loadCarousel()
  
  // 初始化全局 WebSocket
  if (userStore.activeUserId) {
    chatStore.initGlobalWebSocket(userStore.activeUserId)
  }
})

const loadGoods = async () => {
  const res = await request.get('/goods/list', {
    params: { keyword: searchQuery.value }
  })
  if (res.data.code === 200) goods.value = res.data.data
}

// 🌟 新增：去仓库拿随机展品的方法
const loadCarousel = async () => {
  const res = await request.get('/goods/carousel')
  if (res.data.code === 200) {
    carouselGoods.value = res.data.data
  }
}

const handleSearch = () => loadGoods()
const openPublish = () => showPublish.value = true

const submitPublish = async () => {
  if(!publishForm.title || !publishForm.categoryName) {
    return ElMessage.warning('名称和分类都要填哦')
  }
  
  const res = await request.post('/goods/publish', { ...publishForm, userId: user.value.id })
  if (res.data.code === 200) {
    ElMessage.success('发布成功！')
    showPublish.value = false
    // 发布完记得清空盒子
    publishForm.title = ''
    publishForm.price = 0
    publishForm.description = ''
    publishForm.imageUrl = ''
    publishForm.categoryName = ''
    loadGoods()
  }
}

const toDetail = (id) => router.push(`/detail/${id}`)
</script>

<style scoped>
.page-container { padding-top: 100px; padding-bottom: 80px; }
.glass-header { position: fixed; top: 20px; left: 50%; transform: translateX(-50%); width: 90%; max-width: 1100px; height: 65px; background: rgba(255,255,255,0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.3); border-radius: 20px; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; z-index: 100; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
.nav-brand { font-size: 20px; font-weight: 900; color: #409eff; cursor: pointer; }
.glass-search { flex: 1; max-width: 400px; margin: 0 40px; background: rgba(255,255,255,0.2); border-radius: 12px; display: flex; align-items: center; padding: 0 15px; border: 1px solid rgba(255,255,255,0.3); }
.glass-search input { background: transparent; border: none; outline: none; padding: 10px; flex: 1; color: #333; }
.nav-links { display: flex; align-items: center; gap: 20px; }
.nav-links span { cursor: pointer; font-weight: 500; opacity: 0.7; transition: 0.3s; font-size: 14px; }
.nav-links span:hover { opacity: 1; color: #409eff; }
.nav-links span.active { color: #409eff; opacity: 1; }
.user-badge { width: 35px; height: 35px; background: #409eff; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; cursor: pointer; }
.section-title { display: flex; justify-content: space-between; align-items: center; max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.goods-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 25px; padding: 20px; max-width: 1200px; margin: 0 auto; }
.item-card { border-radius: 24px; overflow: hidden; transition: 0.4s; cursor: pointer; background: rgba(255,255,255,0.45); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.3); }
.item-card:hover { transform: translateY(-10px); box-shadow: 0 20px 40px rgba(0,0,0,0.15); }
.item-img { position: relative; height: 180px; overflow: hidden; }
.item-img img { width: 100%; height: 100%; object-fit: cover; transition: 0.6s; }
.category-tag { position: absolute; top: 10px; left: 10px; padding: 4px 10px; background: rgba(0,0,0,0.5); color: white; font-size: 10px; border-radius: 8px; backdrop-filter: blur(5px); }
.item-content { padding: 15px; }
.item-price { color: #f56c6c; font-size: 20px; font-weight: 900; }
.item-footer { display: flex; justify-content: space-between; font-size: 12px; opacity: 0.6; align-items: center; }
.banner-section { max-width: 1200px; margin: 0 auto; padding: 20px; }
.banner-card { position: relative; border-radius: 20px; overflow: hidden; height: 100%; cursor: pointer; }
.banner-card img { width: 100%; height: 100%; object-fit: cover; }
.banner-glass-info { position: absolute; bottom: 0; left: 0; width: 100%; padding: 20px; background: linear-gradient(transparent, rgba(0,0,0,0.7)); color: white; }
.text-ellipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
/* 发布弹窗样式 */
.glass-dialog :deep(.el-dialog) {
  background: var(--glass-bg, rgba(255, 255, 255, 0.45));
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.3));
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.glass-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  border-bottom: 1px solid rgba(255,255,255,0.2);
  margin: 0;
}

.glass-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: bold;
  color: var(--text-color, #333);
}

.glass-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.publish-form {
  .form-item {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: var(--text-color, #333);
    margin-bottom: 10px;
    font-size: 14px;
    line-height: 1.5;
    display: block;
  }
}

/* 表单占位符样式 */
.publish-form :deep(.el-input__placeholder),
.publish-form :deep(.el-textarea__placeholder),
.publish-form :deep(.el-select__placeholder) {
  color: rgba(0,0,0,0.3);
  font-style: normal;
}

/* 上传区域 */
.upload-section {
  text-align: center;
  padding: 16px;
  background: rgba(255,255,255,0.3);
  backdrop-filter: blur(5px);
  border-radius: 16px;
  border: 1px dashed rgba(64,158,255,0.3);
  transition: all 0.3s;
}

.upload-section:hover {
  background: rgba(64,158,255,0.05);
  border-color: rgba(64,158,255,0.5);
}

/* 自定义输入框 */
.custom-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255,255,255,0.5);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color, #409eff);
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}

/* 自定义下拉框 */
.custom-select {
  width: 100%;
}

.custom-select :deep(.el-select__wrapper) {
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.3s;
}

.custom-select :deep(.el-select__wrapper:hover) {
  border-color: rgba(255,255,255,0.5);
}

.custom-select :deep(.el-select__wrapper.is-focused) {
  border-color: var(--primary-color, #409eff);
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}

/* 价格输入 */
.price-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 12px;
  transition: all 0.3s;
}

.price-input-wrapper:hover {
  border-color: rgba(255,255,255,0.5);
}

.price-input-wrapper:focus-within {
  border-color: var(--primary-color, #409eff);
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}

.price-prefix {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
  line-height: 1;
}

.price-input {
  flex: 1;
}

.price-input :deep(.el-input-number__wrapper) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

.price-input :deep(.el-input-number__input) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color, #333);
}

.price-input :deep(.el-input-number__decrease),
.price-input :deep(.el-input-number__increase) {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(0,0,0,0.05);
  border: none;
}

.price-input :deep(.el-input-number__decrease):hover,
.price-input :deep(.el-input-number__increase):hover {
  background: rgba(64,158,255,0.1);
}

/* 自定义文本域 */
.custom-textarea :deep(.el-textarea__inner) {
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 12px;
  box-shadow: none;
  resize: none;
  transition: all 0.3s;
}

.custom-textarea :deep(.el-textarea__inner:hover) {
  border-color: rgba(255,255,255,0.5);
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color, #409eff);
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}

/* 按钮样式 */
.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.2);
  margin-top: 8px;
}

.glass-btn {
  border: none;
  border-radius: 12px;
  padding: 12px 30px;
  cursor: pointer;
  font-weight: 600;
  font-size: 15px;
  font-family: 'PingFang SC', sans-serif;
  transition: all 0.3s ease;
  outline: none;
  position: relative;
  overflow: hidden;
}

.glass-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.glass-btn:hover::before {
  left: 100%;
}

.glass-btn.primary {
  background: linear-gradient(135deg, var(--primary-color, #409eff), #66b1ff);
  color: white;
  box-shadow: 0 4px 15px rgba(64,158,255,0.3), 0 2px 4px rgba(0,0,0,0.1);
}

.glass-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64,158,255,0.4), 0 4px 8px rgba(0,0,0,0.15);
}

.glass-btn.primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

.glass-btn.secondary {
  background: rgba(255,255,255,0.4);
  backdrop-filter: blur(5px);
  color: var(--text-color, #333);
  border: 1px solid rgba(255,255,255,0.5);
}

.glass-btn.secondary:hover {
  background: rgba(255,255,255,0.6);
  border-color: rgba(255,255,255,0.7);
}

.glass-btn.secondary:active {
  background: rgba(255,255,255,0.3);
}

.glass-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.glass-btn:disabled:hover {
  box-shadow: none;
}

/* 消息图标 */
.message-badge {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg-badge-wrapper {
  display: flex;
  align-items: center;
}

.nav-text {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-color, #333);
  opacity: 0.7;
  transition: 0.3s;
  cursor: pointer;
}

.message-badge:hover .nav-text {
  opacity: 1;
  color: #409eff;
}
</style>