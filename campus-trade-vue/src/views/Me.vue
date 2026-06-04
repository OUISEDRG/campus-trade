<template>
  <div class="me-container">
    <div class="glass-card profile-card">
      <div class="avatar-section">
        <div class="avatar">{{ user.name?.substring(0, 1).toUpperCase() || 'U' }}</div>
        <div class="user-info">
          <h2 class="display-name">{{ user.name || '未设置姓名' }}</h2>
          <p class="account-info">账号: {{ user.username }}</p>
          <span class="tag">校园认证用户</span>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-item" @click="openList('published', '我发布的')">
          <strong>{{ stats.publishCount }}</strong>
          <span>我发布的</span>
        </div>
        <div class="stat-item" @click="openList('sold', '我卖出的')">
          <strong>{{ stats.soldCount }}</strong>
          <span>我卖出的</span>
        </div>
        <div class="stat-item" @click="openList('bought', '我买到的')">
          <strong>{{ stats.boughtCount }}</strong>
          <span>我买到的</span>
        </div>
      </div>
    </div>

    <div class="action-list">
      <div class="glass-card action-item" @click="openEditProfile">
        <div class="item-left">
          <el-icon><EditPen /></el-icon>
          <span>修改个人信息</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="glass-card action-item" @click="openFavorites">
        <div class="item-left">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="glass-card action-item" @click="router.push('/home')">
        <div class="item-left">
          <el-icon><Shop /></el-icon>
          <span>返回交易大厅</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="glass-card action-item text-danger" @click="handleLogout">
        <div class="item-left">
          <el-icon><SwitchButton /></el-icon>
          <span>退出当前账号</span>
        </div>
        <el-icon><CircleClose /></el-icon>
      </div>
      
      <div class="glass-card action-item danger-zone" @click="handleDeleteAccount">
        <div class="item-left">
          <el-icon><Delete /></el-icon>
          <span>注销账号 (清除所有数据)</span>
        </div>
        <el-icon><Delete /></el-icon>
      </div>
    </div>

    <el-dialog
      v-model="showListDialog"
      :title="listTitle"
      width="90%"
      max-width="500px"
      class="glass-dialog"
    >
      <div v-if="detailList.length > 0" class="list-container">
        <div v-for="(item, index) in detailList" :key="item.id" class="list-item glass-card" @click="handleItemClick(item)">
          <img :src="(item.imageUrl || '').split(',')[0] || `https://picsum.photos/seed/${item.id}/100/100`" class="item-thumb" />
          <div class="item-info">
            <h4>{{ item.title }}</h4>
            <p class="price">￥{{ item.price }}</p>
          </div>

          <div class="order-status" v-if="listTitle === '我卖出的' || listTitle === '我买到的'">
            <el-tag v-if="item.orderStatus === 0" type="warning" size="small">待发货</el-tag>
            <el-tag v-else-if="item.orderStatus === 1" type="primary" size="small">已发货</el-tag>
            <el-tag v-else-if="item.orderStatus === 2" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="item.orderStatus === 3" type="info" size="small">已取消</el-tag>
          </div>

          <div class="action-buttons" v-if="listTitle === '我发布的'">
            <span v-if="item.status === 1" class="status-badge">
              <el-tag type="info" size="small">已下架</el-tag>
            </span>
            <template v-else>
              <button class="small-btn edit-btn" @click.stop="openEdit(item)">修改</button>
              <button class="small-btn delete-btn" @click.stop="handleDelete(item, index)">下架商品</button>
            </template>
          </div>
          <div class="action-buttons" v-if="listTitle === '我的收藏'">
            <button class="small-btn delete-btn" @click.stop="handleUnfavorite(item, index)">取消收藏</button>
          </div>
          <el-icon v-else class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
      <el-empty v-else description="空空如也，快去大厅逛逛吧~"></el-empty>
    </el-dialog>

    <el-dialog
      v-model="showEditDialog"
      title="修改宝贝信息"
      width="90%"
      max-width="400px"
      class="glass-dialog"
    >
      <el-form label-position="top" class="publish-form">
        <el-form-item label="商品图片" class="form-item">
          <div class="upload-section">
            <MultiImageUpload v-model="editForm.imageUrl" :limit="5" :max-size="3" />
          </div>
        </el-form-item>

        <el-form-item label="物品名称" class="form-item">
          <el-input v-model="editForm.title" class="custom-input" />
        </el-form-item>
        <el-form-item label="预期价格" class="form-item">
          <div class="price-input-wrapper">
            <span class="price-prefix">￥</span>
            <el-input-number v-model="editForm.price" :precision="2" :min="0" class="price-input" style="width: 100%" />
          </div>
        </el-form-item>
        <el-form-item label="详细描述" class="form-item">
          <el-input type="textarea" v-model="editForm.description" :rows="3" class="custom-textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="glass-btn secondary" @click="showEditDialog = false">取消</button>
          <button class="glass-btn primary" @click="submitEdit">保存修改</button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="showBuyerDialog" title="📦 买家收货信息" width="90%" max-width="400px" class="glass-dialog">
      <div v-if="buyerInfo" class="buyer-info-card">
        <div class="info-row">
          <span class="label">收货人：</span>
          <span class="value font-bold">{{ buyerInfo.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">联系电话：</span>
          <span class="value highlight">{{ buyerInfo.phone }}</span>
        </div>
        <div class="info-row">
          <span class="label">详细地址：</span>
          <span class="value">{{ buyerInfo.address }}</span>
        </div>
        <div class="info-row">
          <span class="label">邮政编码：</span>
          <span class="value">{{ buyerInfo.postalCode }}</span>
        </div>
        <div class="info-row">
          <span class="label">交易时间：</span>
          <span class="value text-muted">{{ buyerInfo.orderTime?.replace('T', ' ') }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单状态：</span>
          <span class="value">
            <el-tag v-if="buyerInfo.orderStatus === 0" type="warning">待发货</el-tag>
            <el-tag v-else-if="buyerInfo.orderStatus === 1" type="primary">已发货</el-tag>
            <el-tag v-else-if="buyerInfo.orderStatus === 2" type="success">已完成</el-tag>
            <el-tag v-else-if="buyerInfo.orderStatus === 3" type="info">已取消</el-tag>
            <span v-else>未知</span>
          </span>
        </div>
      </div>
      <el-empty v-else description="暂无买家信息~"></el-empty>
      <template #footer>
        <div style="display: flex; gap: 12px; justify-content: flex-end;">
          <button v-if="buyerInfo?.orderStatus === 0" class="glass-btn primary" @click="handleShipOrder">确认发货</button>
          <button class="glass-btn secondary" @click="showBuyerDialog = false">关闭窗口</button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showEditProfileDialog"
      title="完善个人资料"
      width="90%"
      max-width="400px"
      class="glass-dialog"
    >
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-position="top" class="publish-form">
        <el-form-item label="真实姓名" prop="name" class="form-item">
          <el-input v-model="profileForm.name" placeholder="请输入您的姓名" class="custom-input" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone" class="form-item">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" class="custom-input" />
        </el-form-item>
        <el-form-item label="收货地址 / 宿舍" prop="college" class="form-item">
          <el-input v-model="profileForm.college" placeholder="例如：东区13舍 / 计算机学院" class="custom-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="glass-btn secondary" @click="showEditProfileDialog = false">取消</button>
          <button class="glass-btn primary" @click="submitProfileUpdate">保存修改</button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="showReviewDialog" title="评价交易" width="90%" max-width="400px" class="glass-dialog">
      <div class="review-form">
        <div class="review-target">评价对象：{{ reviewTargetName }}</div>
        <div class="rating-row">
          <span>评分：</span>
          <el-rate v-model="reviewForm.rating" />
        </div>
        <el-input 
          v-model="reviewForm.content" 
          type="textarea" 
          :rows="3" 
          placeholder="分享您的交易体验..." 
          style="margin-top: 15px"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="glass-btn secondary" @click="showReviewDialog = false">取消</button>
          <button class="glass-btn primary" @click="submitReview">提交评价</button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, SwitchButton, EditPen, Shop, CircleClose, Delete, Camera, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import request from '../utils/request'
import { offShelvesGoods } from '../api/goods'
import { useUserStore } from '../stores/user'
import MultiImageUpload from '../components/MultiImageUpload.vue'

const router = useRouter()
const userStore = useUserStore()
const user = ref({})

const stats = ref({ publishCount: 0, soldCount: 0, boughtCount: 0 })

const showListDialog = ref(false)
const listTitle = ref('')
const detailList = ref([])

const showEditDialog = ref(false)
const editForm = reactive({ id: null, title: '', price: 0, description: '', imageUrl: '' })

const showBuyerDialog = ref(false)
const buyerInfo = ref(null)

const showEditProfileDialog = ref(false)
const profileFormRef = ref(null)

const showReviewDialog = ref(false)
const reviewTargetName = ref('')
const reviewForm = reactive({ orderId: null, reviewerId: null, targetUserId: null, rating: 0, content: '' })

const profileForm = reactive({
  id: null,
  name: '',
  phone: '',
  college: ''
})

const profileRules = {
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

onMounted(() => {
  const currentUser = userStore.currentUser
  if (currentUser) {
    user.value = currentUser
    loadStats()
  } else {
    ElMessage.warning('请先登录哦')
    router.push('/')
  }
})

const loadStats = async () => {
  try {
    const res = await request.get('/me/stats', { params: { userId: user.value.id } })
    if (res.data.code === 200) stats.value = res.data.data
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

const openList = async (type, title) => {
  listTitle.value = title
  showListDialog.value = true
  detailList.value = []

  try {
    const res = await request.get('/me/goodsList', {
      params: { userId: user.value.id, type: type }
    })
    if (res.data.code === 200) {
      const items = res.data.data
      // 对于卖出和买到的，需要加载订单状态
      if (type === 'sold' || type === 'bought') {
        const enrichedItems = await Promise.all(items.map(async (item) => {
          try {
            const orderRes = await request.get(`/orders/buyerInfo/${item.id}`)
            if (orderRes.data.code === 200) {
              return { ...item, orderInfo: orderRes.data.data, orderStatus: orderRes.data.data.orderStatus }
            }
          } catch (e) { /* ignore */ }
          return item
        }))
        detailList.value = enrichedItems
      } else {
        detailList.value = items
      }
    }
  } catch (error) {
    ElMessage.error('获取列表失败啦')
  }
}

const handleShipOrder = async () => {
  try {
    const res = await request.put(`/orders/${buyerInfo.value.orderId}/ship`, null, {
      params: { sellerId: user.value.id }
    })
    if (res.data.code === 200) {
      ElMessage.success('发货成功！')
      buyerInfo.value.orderStatus = 1
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const goToDetail = (id) => {
  showListDialog.value = false
  router.push(`/detail/${id}`)
}

const handleItemClick = async (item) => {
  if (listTitle.value === '我卖出的') {
    try {
      const res = await request.get(`/orders/buyerInfo/${item.id}`)
      if (res.data.code === 200) {
        buyerInfo.value = res.data.data
        showBuyerDialog.value = true
      } else {
        ElMessage.error(res.data.message || '获取买家信息失败')
      }
    } catch (error) {
      ElMessage.error('网络开了个小差，请稍后再试')
    }
  } else if (listTitle.value === '我买到的') {
    try {
      const res = await request.get(`/orders/buyerInfo/${item.id}`)
      if (res.data.code === 200) {
        item.orderInfo = res.data.data
        openReview(item)
      }
    } catch (e) {
      goToDetail(item.id)
    }
  } else {
    goToDetail(item.id)
  }
}

const openReview = (item) => {
  reviewTargetName.value = '卖家 ' + (item.orderInfo?.sellerId || '未知')
  reviewForm.orderId = item.orderInfo?.orderId
  reviewForm.reviewerId = user.value.id
  reviewForm.targetUserId = item.orderInfo?.sellerId || item.userId
  reviewForm.rating = 0
  reviewForm.content = ''
  showReviewDialog.value = true
}

const submitReview = async () => {
  if (reviewForm.rating === 0) {
    ElMessage.warning('请给个评分吧')
    return
  }
  try {
    const res = await request.post('/review/add', reviewForm)
    if (res.data.code === 200) {
      ElMessage.success('评价成功！')
      showReviewDialog.value = false
    }
  } catch (e) {
    ElMessage.error('评价失败')
  }
}

const openEdit = (item) => {
  editForm.id = item.id
  editForm.title = item.title
  editForm.price = item.price
  editForm.description = item.description
  editForm.imageUrl = item.imageUrl || ''
  showEditDialog.value = true
}

const submitEdit = async () => {
  const res = await request.post('/goods/update', editForm)
  if (res.data.code === 200) {
    ElMessage.success('宝贝信息已更新！')
    showEditDialog.value = false
    openList('published', '我发布的')
  }
}

const handleDelete = (item, index) => {
  ElMessageBox.confirm('真的要狠心下架这个宝贝吗？下架后首页大厅将不再展示。', '下架确认', {
    confirmButtonText: '果断下架',
    cancelButtonText: '我再想想',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await offShelvesGoods(item.id)
      if (res.data.code === 200) {
        ElMessage.success('商品已成功下架，并从首页移除')
        // 实时更新本地状态
        detailList.value[index].status = 1
        // 关闭当前对话框
        showListDialog.value = false
        // 重新加载统计信息
        loadStats()
      } else {
        ElMessage.error(res.data.message || '操作失败，请重试')
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败，请重试')
      }
    }
  }).catch(() => {})
}

const openEditProfile = () => {
  profileForm.id = user.value.id
  profileForm.name = user.value.name || ''
  profileForm.phone = user.value.phone || ''
  profileForm.college = user.value.college || ''
  showEditProfileDialog.value = true
}

const submitProfileUpdate = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await request.post('/user/update', profileForm)
        if (res.data.code === 200) {
          ElMessage.success('资料更新成功！')
          user.value = res.data.data
          localStorage.setItem('user', JSON.stringify(res.data.data))
          showEditProfileDialog.value = false
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已安全退出')
  router.push('/')
}

const handleDeleteAccount = () => {
  ElMessageBox.confirm(
    '确定要注销并删除所有个人数据吗？此操作不可恢复',
    '危险操作警告',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '暂不注销',
      type: 'error',
      draggable: true,
    }
  ).then(async () => {
    const loading = ElLoading.service({
      lock: true,
      text: '正在彻底清除您的个人数据，请稍候...',
      background: 'rgba(0, 0, 0, 0.6)',
    })

    try {
      const res = await request.delete(`/user/deleteAccount/${user.value.id}`)
      
      if (res.data.code === 200) {
        localStorage.removeItem('user')
        sessionStorage.clear()
        
        ElMessage.success('注销成功，您的所有数据已删除')
        router.push('/')
      } else {
        ElMessage.error(res.data.message || '注销失败，请稍后重试')
      }
    } catch (error) {
      ElMessage.error('注销失败，请稍后重试')
    } finally {
      loading.close()
    }
  }).catch(() => {})
}

const openFavorites = async () => {
  listTitle.value = '我的收藏'
  showListDialog.value = true
  detailList.value = []
  try {
    const res = await request.get('/favorite/list', {
      params: { userId: user.value.id }
    })
    if (res.data.code === 200) {
      detailList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  }
}

const handleUnfavorite = async (item, index) => {
  try {
    const res = await request.delete('/favorite/remove', {
      params: { userId: user.value.id, goodsId: item.id }
    })
    if (res.data.code === 200) {
      ElMessage.success('已取消收藏')
      detailList.value.splice(index, 1)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.me-container { padding: 100px 20px 80px; max-width: 800px; margin: 0 auto; }
.profile-card { padding: 40px; border-radius: 25px; margin-bottom: 30px; background: var(--glass-bg, rgba(255, 255, 255, 0.45)); backdrop-filter: blur(20px); border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.3)); box-shadow: 0 10px 40px rgba(0,0,0,0.1); }
.avatar-section { display: flex; align-items: center; gap: 30px; margin-bottom: 40px; }
.avatar { width: 100px; height: 100px; border-radius: 50%; background: linear-gradient(135deg, #409eff, #a2d2ff); color: white; font-size: 40px; font-weight: bold; display: flex; align-items: center; justify-content: center; box-shadow: 0 10px 20px rgba(64, 158, 255, 0.3); }

.display-name {
  margin: 0 0 5px 0;
  font-size: 32px;
  font-weight: 800;
  color: var(--primary-color, #409eff);
  letter-spacing: -1px;
}

.account-info {
  margin: 0 0 10px 0;
  opacity: 0.5;
  font-size: 14px;
}

.tag { background: rgba(103, 194, 58, 0.2); color: #67c23a; padding: 4px 12px; border-radius: 20px; font-size: 12px; border: 1px solid rgba(103, 194, 58, 0.5); }
.stats-row { display: flex; justify-content: space-around; border-top: 1px solid rgba(255, 255, 255, 0.3); padding-top: 30px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 10px; cursor: pointer; transition: 0.3s; padding: 10px 20px; border-radius: 15px; }
.stat-item:hover { background: rgba(255, 255, 255, 0.3); transform: translateY(-5px); }
.stat-item strong { font-size: 24px; color: #409eff; }
.stat-item span { font-size: 14px; opacity: 0.7; }

.action-list { display: flex; flex-direction: column; gap: 15px; }

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-item { padding: 18px 25px; border-radius: 15px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: all 0.3s; font-weight: bold; background: rgba(255, 255, 255, 0.45); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.3); box-shadow: 0 10px 40px rgba(0,0,0,0.1); }
.action-item:hover { transform: translateX(10px); background: rgba(255, 255, 255, 0.5); }
.text-danger { color: #f56c6c; }

.danger-zone {
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.4);
  background: rgba(245, 108, 108, 0.05);
}

.danger-zone:hover {
  background: rgba(245, 108, 108, 0.15);
  box-shadow: 0 10px 40px rgba(245, 108, 108, 0.15);
}

.list-container { display: flex; flex-direction: column; gap: 15px; max-height: 50vh; overflow-y: auto; padding: 10px 5px; }
.list-item { display: flex; align-items: center; padding: 15px; border-radius: 15px; cursor: pointer; transition: 0.3s; gap: 15px; background: rgba(255, 255, 255, 0.5); border: 1px solid rgba(255, 255, 255, 0.4); }
.list-item:hover { background: rgba(255,255,255,0.8); transform: translateX(5px); box-shadow: 0 5px 15px rgba(0,0,0,0.05); }
.item-thumb { width: 60px; height: 60px; border-radius: 10px; object-fit: cover; }
.item-info { flex: 1; }
.item-info h4 { margin: 0 0 5px 0; font-size: 15px; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.item-info .price { color: #f56c6c; margin: 0; font-weight: bold; }
.arrow { color: #999; }

.action-buttons { display: flex; gap: 8px; }
.small-btn { border: none; padding: 6px 12px; border-radius: 8px; font-size: 12px; cursor: pointer; font-weight: bold; transition: 0.3s; }
.edit-btn { background: rgba(64, 158, 255, 0.1); color: #409eff; }
.edit-btn:hover { background: #409eff; color: white; }
.delete-btn { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
.delete-btn:hover { background: #f56c6c; color: white; }

/* 统一的表单样式 */
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

/* 统一的按钮样式 */
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

.glass-btn.full { 
  width: 100%; 
}



.buyer-info-card {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.info-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
  font-size: 15px;
  line-height: 1.5;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #666;
  width: 80px;
  flex-shrink: 0;
  font-weight: 500;
}

.info-row .value {
  color: var(--text-color, #333);
  flex: 1;
  word-break: break-all;
}

.font-bold {
  font-weight: bold;
  font-size: 16px;
}

.highlight {
  color: var(--primary-color, #409eff);
  font-weight: bold;
}

.text-muted {
  color: #999;
  font-size: 13px;
}
</style>
