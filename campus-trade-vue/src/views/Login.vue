<template>
  <div class="login-wrapper">
    <!-- 3D 背景容器 -->
    <div ref="threeContainer" class="three-bg"></div>

    <!-- 登录卡片 -->
    <div class="glass-card login-card">
      <h2 class="title">CampusTrade</h2>
      <p class="subtitle">基于SpringBoot+Vue3的校园二手交易平台设计与实现</p>
      
      <div class="form-container">
        <div class="input-group">
          <input type="text" v-model="form.username" placeholder="学号/用户名" />
        </div>
        <div class="input-group">
          <input type="password" v-model="form.password" placeholder="密码" />
        </div>
        <button class="glass-btn primary" @click="handleLogin">进入探索</button>
        <p class="switch-text">没有账号？<span @click="isRegister = true">立即加入</span></p>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="isRegister" title="加入我们" width="400px" class="glass-dialog">
      <el-form :model="regForm" :rules="regRules" ref="regFormRef" label-position="top">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="regForm.username" placeholder="请设置登录账号" class="custom-input" />
        </el-form-item>
        
        <el-form-item label="登录密码" prop="password">
          <el-input type="password" v-model="regForm.password" placeholder="请设置6位以上密码" show-password class="custom-input" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="regForm.name" placeholder="请输入中英文姓名" class="custom-input" />
        </el-form-item>

        <el-form-item label="学号" prop="studentId">
          <el-input v-model="regForm.studentId" placeholder="请输入真实学号" class="custom-input" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="regForm.phone" placeholder="请输入11位手机号" maxlength="11" class="custom-input" />
        </el-form-item>

        <el-form-item label="账号角色" prop="role">
          <el-radio-group v-model="regForm.role">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-collapse-transition>
          <el-form-item
            v-if="regForm.role === 1"
            label="管理邀请码"
            prop="inviteCode"
          >
            <el-input
              v-model="regForm.inviteCode"
              type="password"
              placeholder="请输入系统授权邀请码"
              show-password
              class="custom-input"
            />
          </el-form-item>
        </el-collapse-transition>
      </el-form>
      
      <template #footer>
        <button class="glass-btn primary" @click="handleRegister" style="width:100%; margin-top: 10px;">提交注册</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useAdminStore } from '../stores/admin'
import * as THREE from 'three'

const router = useRouter()
const userStore = useUserStore()
const adminStore = useAdminStore()
const isRegister = ref(false)
const form = reactive({ username: '', password: '' })

const regFormRef = ref(null)
const regForm = reactive({
  username: '',
  password: '',
  name: '',
  studentId: '',
  phone: '',
  role: 0,
  inviteCode: ''
})

const regRules = reactive({
  username: [
    { required: true, message: '账号不能为空', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度需在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' },
    { min: 2, max: 15, message: '姓名长度在 2 到 15 个字符', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/, message: '姓名只能包含中文或英文', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '学号不能为空', trigger: 'blur' },
    { pattern: /^\d{8,12}$/, message: '请输入有效的纯数字学号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号格式', trigger: 'blur' }
  ],
  inviteCode: [
    {
      validator: (rule, value, callback) => {
        if (regForm.role === 1 && !value) {
          callback(new Error('注册管理员必须提供有效的邀请码'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// ==================== 3D 魔法区域 ====================
const threeContainer = ref(null)
let scene, camera, renderer, mesh, animationId

const initThreeJS = () => {
  if (!threeContainer.value) return;

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.z = 30
  
  renderer = new THREE.WebGLRenderer({ 
    alpha: true,      // 允许透明，让后面的背景色透过来
    antialias: true   // 开启抗锯齿，让线条更平滑
  })
  
  renderer.setSize(window.innerWidth, window.innerHeight)
  threeContainer.value.appendChild(renderer.domElement)

  // 捏一个几何体
  const geometry = new THREE.IcosahedronGeometry(15, 1)
  
  // 🌟 美化核心：科技蓝，带一点半透明全息质感
  const material = new THREE.MeshBasicMaterial({ 
    color: 0x409eff,   // 柔和的科技蓝
    wireframe: true,   // 线框模式
    transparent: true, 
    opacity: 0.25      // 25%的不透明度，若隐若现最高级
  })
  
  mesh = new THREE.Mesh(geometry, material)
  scene.add(mesh)

  // 让它动起来
  const animate = () => {
    animationId = requestAnimationFrame(animate)
    // 匀速缓慢自转
    mesh.rotation.x += 0.002
    mesh.rotation.y += 0.003
    renderer.render(scene, camera)
  }
  animate()

  // 监听窗口大小改变，防止拉伸变形
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  if (camera && renderer) {
    camera.aspect = window.innerWidth / window.innerHeight
    camera.updateProjectionMatrix()
    renderer.setSize(window.innerWidth, window.innerHeight)
  }
}

onMounted(() => {
  // 稍微延迟确保 DOM 加载完毕
  setTimeout(() => {
    initThreeJS()
  }, 100)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', handleResize)
  if (renderer) renderer.dispose()
})
// ==================== 3D 魔法区域结束 ====================

const handleLogin = async () => {
  if (!form.username || !form.password) return ElMessage.warning('填全信息再出发~')
  try {
    const res = await request.post('/user/login', form)
    if (res.data.code === 200) {
      const userData = res.data.data
      
      if (userData.role === 1) {
        adminStore.setAdminLoginInfo(userData)
        ElMessage.success('欢迎回来，管理员！')
        router.push('/admin')
      } else {
        userStore.addUserSession(userData.id, userData.token || '', userData)
        ElMessage.success('欢迎回来！')
        router.push('/home')
      }
    } else { ElMessage.error(res.data.message) }
  } catch (e) { ElMessage.error('网络开了个小差') }
}

const handleRegister = async () => {
  if (!regFormRef.value) return
  
  await regFormRef.value.validate(async (valid) => {
    if (valid) {
      const res = await request.post('/user/register', regForm)
      if (res.data.code === 200) {
        ElMessage.success('太棒了，欢迎加入平台！注册成功！')
        isRegister.value = false
        regFormRef.value.resetFields()
      } else {
        ElMessage.error(res.data.message)
      }
    } else {
      ElMessage.warning('请检查红色的错误提示哦~')
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  /* 专属淡雅渐变背景，烘托科技蓝星球 */
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
}

.three-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1; /* 压在渐变背景上 */
  pointer-events: none; /* 防止挡住鼠标点击事件 */
}

.login-card {
  position: relative;
  z-index: 10; /* 悬浮在 3D 星球之上 */
  background: var(--glass-bg, rgba(255, 255, 255, 0.45));
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.3));
  box-shadow: 0 15px 50px rgba(0,0,0,0.1);
  padding: 50px 40px;
  border-radius: 30px;
  width: 100%;
  max-width: 360px;
  text-align: center;
}

.title { color: var(--primary-color, #409eff); font-size: 32px; margin-bottom: 10px; font-weight: 900;}
.subtitle { font-size: 13px; opacity: 0.6; margin-bottom: 40px; letter-spacing: 1px; }

.input-group input {
  width: 100%; padding: 12px 0; margin-bottom: 25px;
  background: transparent; border: none;
  border-bottom: 1px solid var(--glass-border, rgba(0, 0, 0, 0.1));
  color: var(--text-color, #333); outline: none;
  transition: 0.3s; font-size: 15px;
}
.input-group input:focus { border-bottom-color: var(--primary-color, #409eff); }

.glass-btn {
  background: var(--primary-color, #409eff); color: white;
  border: none; padding: 14px 30px; border-radius: 15px;
  cursor: pointer; width: 100%; font-weight: bold; font-size: 16px;
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
  transition: 0.3s;
}
.glass-btn:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(64, 158, 255, 0.5); }

.switch-text { font-size: 13px; margin-top: 25px; opacity: 0.7; }
.switch-text span { color: var(--primary-color, #409eff); cursor: pointer; font-weight: bold; }

/* 注册弹窗玻璃拟物化统一样式 */
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
  padding: 20px 24px;
}

.glass-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-color, #333);
  padding-bottom: 4px;
}

/* 统一定制输入框样式 */
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
  border-color: var(--primary-color, #409eff);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
</style>