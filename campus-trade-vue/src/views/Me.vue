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
          <img :src="item.imageUrl || `https://picsum.photos/seed/${item.id}/100/100`" class="item-thumb" />
          <div class="item-info">
            <h4>{{ item.title }}</h4>
            <p class="price">￥{{ item.price }}</p>
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
      <el-form label-position="top">
        <el-form-item label="商品图片">
          <div class="upload-section">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
            >
              <div v-if="editForm.imageUrl" class="image-preview">
                <img :src="editForm.imageUrl" class="uploaded-avatar" />
                <div class="hover-mask">
                  <el-icon><Camera /></el-icon>
                  <span>更换图片</span>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
                <span class="upload-text">点击选择图片</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="物品名称">
          <el-input v-model="editForm.title" class="custom-input" />
        </el-form-item>
        <el-form-item label="预期价格">
          <div class="price-input-wrapper">
            <span class="price-prefix">￥</span>
            <el-input-number v-model="editForm.price" :precision="2" :min="0" class="price-input" style="width: 100%" />
          </div>
        </el-form-item>
        <el-form-item label="详细描述">
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
      </div>
      <el-empty v-else description="暂无买家信息~"></el-empty>
      <template #footer>
        <button class="glass-btn primary full" @click="showBuyerDialog = false">关闭窗口</button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showEditProfileDialog"
      title="完善个人资料"
      width="90%"
      max-width="400px"
      class="glass-dialog"
    >
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-position="top">
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="profileForm.name" placeholder="请输入您的姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货地址 / 宿舍" prop="college">
          <el-input v-model="profileForm.college" placeholder="例如：东区13舍 / 计算机学院" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="glass-btn secondary" @click="showEditProfileDialog = false">取消</button>
          <button class="glass-btn primary" @click="submitProfileUpdate">保存修改</button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, SwitchButton, EditPen, Shop, CircleClose, Delete, Plus, Camera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import request from '../utils/request'
import { offShelvesGoods } from '../api/goods'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const user = ref({})
const uploadUrl = request.defaults.baseURL + '/file/upload'

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
      detailList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取列表失败啦')
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
  } else {
    goToDetail(item.id)
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

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    editForm.imageUrl = res.data
    ElMessage.success('图片上传成功！')
  } else {
    ElMessage.error(res.message || '图片上传失败，请重试')
  }
}

const beforeUpload = (file) => {
  const isAllowedType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isAllowedType) {
    ElMessage.error('上传图片只能是 JPG, PNG 或 WEBP 格式!')
  }
  if (!isLt10M) {
    ElMessage.error('上传图片大小不能超过 10MB!')
  }
  return isAllowedType && isLt10M
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

.glass-btn { border: none; border-radius: 12px; padding: 12px 30px; cursor: pointer; font-weight: bold; transition: 0.3s; }
.glass-btn.primary { background: var(--primary-color, #409eff); color: white; }
.glass-btn.full { width: 100%; }
.glass-btn.secondary {
  background: rgba(255, 255, 255, 0.3);
  color: var(--text-color, #333);
  border: 1px solid rgba(255, 255, 255, 0.4);
}
.glass-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.5);
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 8px;
}

.upload-section {
  text-align: center;
  width: 100%;
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s;
  position: relative;
  cursor: pointer;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--primary-color, #409eff);
  background: rgba(64, 158, 255, 0.1);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-uploader-icon {
  font-size: 32px;
  color: var(--primary-color, #409eff);
}

.upload-text {
  font-size: 14px;
  color: #999;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.uploaded-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 14px;
}

.hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 14px;
}

.image-preview:hover .hover-mask {
  opacity: 1;
}

.hover-mask .el-icon {
  font-size: 24px;
}

.hover-mask span {
  font-size: 13px;
  font-weight: bold;
}

.price-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-prefix {
  font-weight: bold;
  color: #f56c6c;
  font-size: 18px;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.3s;
  padding: 8px 15px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.8);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.custom-textarea :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.3s;
  padding: 12px 15px;
}

.custom-textarea :deep(.el-textarea__inner:hover) {
  border-color: rgba(255, 255, 255, 0.8);
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
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
