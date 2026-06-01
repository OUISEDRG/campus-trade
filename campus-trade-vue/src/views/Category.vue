<template>
  <div class="category-container">
    <!-- 返回交易大厅按钮 -->
    <div class="back-btn" @click="router.push('/home')">
      <el-icon><ArrowLeft /></el-icon> 返回交易大厅
    </div>
    
    <div class="glass-card layout-box">
      <div class="side-nav">
        <div 
          v-for="(item, index) in categories" 
          :key="index" 
          :class="['nav-item', activeIndex === index ? 'active' : '']" 
          @click="handleCategoryChange(index)" 
        >
          {{ item }}
        </div>
      </div>

      <div class="content-area">
        <h2 class="category-title">{{ categories[activeIndex] }}</h2>
        
        <div class="goods-grid" v-if="goodsList.length > 0">
          <div v-for="item in goodsList" :key="item.id" class="glass-card item-card" @click="toDetail(item.id)">
            <div class="img-wrapper">
              <img :src="(item.imageUrl || '').split(',')[0] || `https://picsum.photos/seed/${item.id}/300/300`" @error="handleImageError" />
            </div>
            <div class="info">
              <h4 class="text-ellipsis">{{ item.title }}</h4>
              <p class="price">￥{{ item.price }}</p>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="这个分类下暂时没有宝贝哦，快去发布吧~"></el-empty>
        
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const categories = ['全部好物', '教材书籍', '数码电子', '生活用品', '美妆服饰', '运动健身', '其他闲置']
const activeIndex = ref(0)

const defaultImage = 'https://picsum.photos/seed/default/300/300'

const handleImageError = (event) => {
  event.target.src = defaultImage
  event.target.onerror = null
}

// 🌟 准备一个空盒子装商品
const goodsList = ref([])

// 🌟 页面刚进来的时候，自动去加载"全部好物"
onMounted(() => {
  loadCategoryGoods()
})

// 🌟 当你点击左边不同分类时触发
const handleCategoryChange = (index) => {
  activeIndex.value = index
  loadCategoryGoods() // 重新去找后端要货
}

// 🌟 叫邮递员带着分类名字去找后端要货
const loadCategoryGoods = async () => {
  const currentCategory = categories[activeIndex.value]
  try {
    const res = await request.get('/goods/list', {
      params: { categoryName: currentCategory } // 告诉后端我要什么分类
    })
    if (res.data.code === 200) {
      goodsList.value = res.data.data
    }
  } catch (error) {
    console.error('加载分类商品失败', error)
  }
}

// 点击卡片去详情页
const toDetail = (id) => {
  router.push(`/detail/${id}`)
}
</script>

<style scoped>
.category-container { padding: 40px 20px 80px; max-width: 1200px; margin: 0 auto; min-height: 100vh; }

/* 返回按钮样式 */
.back-btn { 
  margin-bottom: 20px; display: inline-flex; align-items: center; 
  cursor: pointer; opacity: 0.7; transition: 0.3s; font-weight: bold;
}
.back-btn:hover { opacity: 1; transform: translateX(-5px); color: var(--primary-color); }
.layout-box { display: flex; min-height: 70vh; padding: 0; overflow: hidden; background: var(--glass-bg, rgba(255,255,255,0.45)); backdrop-filter: blur(20px); border: 1px solid var(--glass-border, rgba(255,255,255,0.3)); border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); }
.side-nav { width: 200px; background: rgba(255, 255, 255, 0.1); border-right: 1px solid var(--glass-border, rgba(255,255,255,0.3)); padding: 20px 0; }
.nav-item { padding: 15px 30px; cursor: pointer; transition: all 0.3s ease; font-weight: 500; opacity: 0.7; }
.nav-item:hover { background: rgba(255, 255, 255, 0.2); opacity: 1; }
.nav-item.active { background: var(--primary-color, #409eff); color: white; opacity: 1; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3); }
.content-area { flex: 1; padding: 30px; }
.category-title { margin-top: 0; margin-bottom: 30px; color: var(--primary-color, #409eff); border-bottom: 2px solid var(--glass-border, rgba(255,255,255,0.3)); padding-bottom: 10px; }
.goods-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 20px; }
.item-card { padding: 0; border-radius: 15px; cursor: pointer; background: var(--glass-bg, rgba(255,255,255,0.45)); border: 1px solid var(--glass-border, rgba(255,255,255,0.3)); transition: 0.3s; overflow: hidden; }
.item-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
.img-wrapper img { width: 100%; height: 160px; object-fit: cover; border-radius: 15px 15px 0 0; }
.info { padding: 10px 15px; }
.text-ellipsis { margin: 0 0 5px; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price { color: #f56c6c; margin: 0; font-weight: bold; }

@media (max-width: 768px) {
  .layout-box { flex-direction: column; }
  .side-nav { width: 100%; display: flex; overflow-x: auto; border-right: none; border-bottom: 1px solid var(--glass-border); padding: 10px; }
  .nav-item { white-space: nowrap; padding: 10px 20px; border-radius: 20px; margin-right: 10px; }
}
</style>
