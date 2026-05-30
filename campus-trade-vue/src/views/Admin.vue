<template>
  <div class="admin-container">
    <div class="glass-card sidebar">
      <h2 class="brand">CampusTrade<br><span>管理中枢</span></h2>
      <ul class="nav-menu">
        <li :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">
          <el-icon><User /></el-icon> 用户管理
        </li>
        <li :class="{ active: activeTab === 'stats' }" @click="activeTab = 'stats'">
          <el-icon><DataLine /></el-icon> 数据统计
        </li>
        <li class="logout text-danger" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出系统
        </li>
      </ul>
    </div>

    <div class="main-content">
      <div v-if="activeTab === 'users'" class="glass-card panel fade-in">
        <div class="search-box">
          <el-input 
            v-model="searchKeyword" 
            placeholder="输入用户名 / 用户ID / 邮箱进行多维搜索" 
            class="custom-input"
            style="width: 400px; margin-right: 15px;"
            clearable
            @keyup.enter="loadUsers"
          >
            <template #append>
              <el-button @click="loadUsers">搜索</el-button>
            </template>
          </el-input>
          <button class="glass-btn primary" @click="exportData">导出 Excel</button>
        </div>

        <el-table :data="userData" style="width: 100%" class="glass-table" v-loading="loading">
          <el-table-column prop="id" label="用户ID" width="100" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="name" label="真实姓名" width="120" />
          <el-table-column prop="studentId" label="学号" width="130" />
          <el-table-column prop="phone" label="联系电话" width="150" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column label="账号状态" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" class="glass-tag">
                {{ scope.row.status === 0 ? '正常' : '已封禁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" />
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewDetails(scope.row)">
                全景详情
              </el-button>
              <el-button 
                size="small" 
                :type="scope.row.status === 0 ? 'danger' : 'warning'" 
                @click="toggleBan(scope.row)"
              >
                {{ scope.row.status === 0 ? '封禁账号' : '解除封禁' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
          class="glass-pagination"
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalUsers"
          layout="prev, pager, next"
          @current-change="loadUsers"
        />
      </div>

      <div v-if="activeTab === 'stats'" class="glass-card panel fade-in">
        <h3>近7日全站交易走势</h3>
        <div id="tradeChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>

    <el-dialog v-model="showDetailDialog" title="用户全景详情" width="90%" max-width="500px" class="glass-dialog">
      <div v-if="selectedUser" class="detail-card">
        <div class="detail-row">
          <span class="label">用户ID：</span>
          <span class="value">{{ selectedUser.id }}</span>
        </div>
        <div class="detail-row">
          <span class="label">用户名：</span>
          <span class="value font-bold">{{ selectedUser.username }}</span>
        </div>
        <div class="detail-row">
          <span class="label">真实姓名：</span>
          <span class="value">{{ selectedUser.name }}</span>
        </div>
        <div class="detail-row">
          <span class="label">学号：</span>
          <span class="value">{{ selectedUser.studentId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">联系电话：</span>
          <span class="value highlight">{{ selectedUser.phone }}</span>
        </div>
        <div class="detail-row">
          <span class="label">邮箱：</span>
          <span class="value">{{ selectedUser.email }}</span>
        </div>
        <div class="detail-row">
          <span class="label">角色：</span>
          <span :class="['value', 'role-badge', selectedUser.role === 1 ? 'admin' : 'user']">
            {{ selectedUser.role === 1 ? '管理员' : '普通用户' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="label">账号状态：</span>
          <span :class="['value', 'status-badge', selectedUser.status === 0 ? 'active' : 'disabled']">
            {{ selectedUser.status === 0 ? '正常' : '已封禁' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="label">注册时间：</span>
          <span class="value text-muted">{{ selectedUser.createTime }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无用户信息~"></el-empty>
      <template #footer>
        <button class="glass-btn primary full" @click="showDetailDialog = false">关闭窗口</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User, DataLine, SwitchButton, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import * as echarts from 'echarts'
import { useAdminStore } from '../stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const activeTab = ref('users')

// 账号管理状态
const searchKeyword = ref('')
const userData = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const totalUsers = ref(0)
const loading = ref(false)

// 详情弹窗
const showDetailDialog = ref(false)
const selectedUser = ref(null)

onMounted(() => {
  if (!adminStore.isAdminLoggedIn) {
    ElMessage.error('请先登录管理员账号')
    router.replace('/')
    return
  }
  loadUsers()
})

watch(activeTab, async (newVal) => {
  if (newVal === 'stats') {
    await nextTick()
    initChart()
  }
})

// 加载分页用户
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
      params: { current: currentPage.value, size: pageSize.value, keyword: searchKeyword.value }
    })
    if (res.data.code === 200) {
      userData.value = res.data.data.records
      totalUsers.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetails = (row) => {
  selectedUser.value = row
  showDetailDialog.value = true
}

// 封禁/解封
const toggleBan = async (row) => {
  const action = row.status === 0 ? '封禁' : '解封'
  const newStatus = row.status === 0 ? 1 : 0
  
  await ElMessageBox.confirm(`确定要${action}用户 ${row.username} 吗？`, '高危操作提示', {
    type: 'warning'
  })
  
  const res = await request.post('/admin/toggleUser', {
    userId: row.id,
    status: newStatus
  })
  
  if (res.data.code === 200) {
    ElMessage.success(`账号已成功${action}`)
    loadUsers()
  } else {
    ElMessage.error(res.data.message || `${action}失败`)
  }
}

// 二次确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm('此操作将永久删除该账号及其所有数据, 是否继续?', '高危操作警告', {
    confirmButtonText: '强制删除',
    cancelButtonText: '取消',
    type: 'error',
  }).then(async () => {
    const res = await request.delete(`/admin/user/${id}`)
    if(res.data.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  }).catch(() => {})
}

// 纯前端导出 CSV
const exportData = () => {
  let csvContent = "data:text/csv;charset=utf-8,\uFEFF"
  csvContent += "ID,账号,姓名,学号,电话,邮箱,状态,注册时间\n"
  userData.value.forEach(row => {
    const statusStr = row.status === 0 ? '正常' : '封禁'
    const rowStr = `${row.id},${row.username},${row.name},${row.studentId || ''},${row.phone || ''},${row.email || ''},${statusStr},${row.createTime}`
    csvContent += rowStr + "\n"
  })
  const encodedUri = encodeURI(csvContent)
  const link = document.createElement("a")
  link.setAttribute("href", encodedUri)
  link.setAttribute("download", "campus_users.csv")
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 初始化图表
const initChart = async () => {
  const chartDom = document.getElementById('tradeChart')
  if (!chartDom) return
  const myChart = echarts.init(chartDom)
  
  const res = await request.get('/admin/stats/daily')
  const rawData = res.data.data.reverse() // 将日期从小到大排
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rawData.map(d => d.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '成交单量',
        data: rawData.map(d => d.count),
        type: 'line',
        smooth: true,
        areaStyle: { color: 'rgba(64,158,255,0.2)' },
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff' }
      }
    ]
  }
  myChart.setOption(option)
}

const handleLogout = () => {
  adminStore.clear()
  router.push('/')
}
</script>

<style scoped>
/* 继承并扩展项目原有的 Glassmorphism 风格 */
.admin-container {
  display: flex;
  height: 100vh;
  padding: 20px;
  gap: 20px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
}

.sidebar {
  width: 250px;
  display: flex;
  flex-direction: column;
  padding: 30px 20px;
}

.brand {
  color: var(--primary-color);
  font-size: 24px;
  font-weight: 900;
  text-align: center;
  margin-bottom: 40px;
  line-height: 1.4;
}
.brand span { font-size: 14px; opacity: 0.6; }

.nav-menu { list-style: none; padding: 0; margin: 0; }
.nav-menu li {
  padding: 15px 20px;
  margin-bottom: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  opacity: 0.7;
}
.nav-menu li:hover { background: rgba(255,255,255,0.5); opacity: 1; }
.nav-menu li.active { background: var(--primary-color); color: white; opacity: 1; box-shadow: 0 4px 15px rgba(64,158,255,0.3); }
.nav-menu li.logout { margin-top: auto; color: #f56c6c; }

.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.panel { flex: 1; padding: 30px; overflow-y: auto; }
.search-box { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  margin-bottom: 20px; 
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 15px 20px;
  border-radius: 12px;
}

/* 表格拟物化透视样式 */
.glass-table {
  background: transparent !important;
}
:deep(.el-table tr), :deep(.el-table th.el-table__cell) {
  background: rgba(255,255,255,0.2) !important;
}
:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(255,255,255,0.3);
}

.small-btn { border: none; padding: 6px 12px; border-radius: 8px; font-size: 12px; cursor: pointer; font-weight: bold; transition: 0.3s; margin-right: 8px;}
.edit-btn { background: rgba(64, 158, 255, 0.1); color: #409eff; }
.edit-btn:hover { background: #409eff; color: white; }
.delete-btn { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
.delete-btn:hover { background: #f56c6c; color: white; }

.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* 详情弹窗玻璃拟物化统一样式 */
.glass-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.detail-card {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
  font-size: 15px;
  line-height: 1.5;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-row .label {
  color: #666;
  width: 90px;
  flex-shrink: 0;
  font-weight: 500;
}

.detail-row .value {
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

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.admin {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.role-badge.user {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.disabled {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}
</style>