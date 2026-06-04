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
        <div class="login-divider">
          <span>或</span>
        </div>
        <button class="glass-btn wechat" @click="showWeChatQR = true">
          <span class="wechat-icon">💬</span> 微信扫码登录
        </button>
        <p class="switch-text">没有账号？<span @click="isRegister = true">立即加入</span></p>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="isRegister" title="加入我们" width="420px" class="glass-dialog">
      <el-form :model="regForm" :rules="regRules" ref="regFormRef" label-position="top" class="publish-form">
        <el-form-item label="登录账号" prop="username" class="form-item">
          <el-input v-model="regForm.username" placeholder="请设置登录账号" class="custom-input" />
        </el-form-item>
        
        <el-form-item label="登录密码" prop="password" class="form-item">
          <el-input type="password" v-model="regForm.password" placeholder="请设置6位以上密码" show-password class="custom-input" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="name" class="form-item">
          <el-input v-model="regForm.name" placeholder="请输入中英文姓名" class="custom-input" />
        </el-form-item>

        <el-form-item label="学号" prop="studentId" class="form-item">
          <el-input v-model="regForm.studentId" placeholder="请输入真实学号" class="custom-input" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone" class="form-item">
          <el-input v-model="regForm.phone" placeholder="请输入11位手机号" maxlength="11" class="custom-input" />
        </el-form-item>

        <el-form-item label="账号角色" prop="role" class="form-item">
          <el-radio-group v-model="regForm.role" class="custom-radio">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-collapse-transition>
          <el-form-item
            v-if="regForm.role === 1"
            label="管理邀请码"
            prop="inviteCode"
            class="form-item"
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
        <div class="dialog-footer">
          <button class="glass-btn secondary" @click="isRegister = false">取消</button>
          <button class="glass-btn primary" @click="handleRegister">提交注册</button>
        </div>
      </template>
    </el-dialog>

    <!-- 微信扫码登录弹窗 -->
    <el-dialog v-model="showWeChatQR" title="微信扫码登录" width="90%" max-width="360px" class="glass-dialog">
      <div class="wechat-qr-box">
        <div v-if="qrLoading" class="qr-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>正在生成二维码...</p>
        </div>
        <div v-else class="qr-container">
          <p class="qr-tip">请使用微信扫描下方二维码</p>
          <div class="qr-placeholder">
            <span class="qr-icon">📱</span>
            <p>{{ qrInfo }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="glass-btn secondary" @click="showWeChatQR = false">关闭</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useAdminStore } from '../stores/admin'
import { useThreeBg } from '../hooks/useThreeBg'
import { getQRCodeUrl } from '../api/wechat'

const router = useRouter()
const userStore = useUserStore()
const adminStore = useAdminStore()
const isRegister = ref(false)
const form = reactive({ username: '', password: '' })

// 微信登录
const showWeChatQR = ref(false)
const qrLoading = ref(false)
const qrInfo = ref('')

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
useThreeBg(threeContainer)
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
        setTimeout(() => {
          router.push('/admin')
        }, 100)
      } else {
        userStore.addUserSession(userData.id, userData.token || '', userData)
        ElMessage.success('欢迎回来！')
        router.push('/home')
      }
    } else { ElMessage.error(res.data.message) }
  } catch (e) { 
    console.error('[Login] 登录异常:', e)
    ElMessage.error('网络开了个小差') 
  }
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

// 微信扫码登录
const loadWeChatQR = async () => {
  qrLoading.value = true
  try {
    const res = await getQRCodeUrl(window.location.origin + '/wechat/callback')
    if (res.data.code === 200) {
      const url = res.data.data?.url || ''
      qrInfo.value = url
        ? '二维码已生成 (实际部署后会显示微信扫码图片)'
        : '请在 application.yml 中配置微信AppID和AppSecret'
    }
  } catch (e) {
    qrInfo.value = '二维码生成失败，请检查微信配置'
  } finally {
    qrLoading.value = false
  }
}

// 监听微信弹窗打开
watch(showWeChatQR, (val) => {
  if (val) loadWeChatQR()
})
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
  background: transparent;
  z-index: 5;
}

.login-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(253, 251, 251, 0.9) 0%, rgba(235, 237, 238, 0.9) 100%);
  z-index: 1;
}

.three-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  pointer-events: none;
}

.login-card {
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.04);
  padding: 50px 40px;
  border-radius: 30px;
  width: 100%;
  max-width: 360px;
  text-align: center;
  overflow: visible;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 30px;
  pointer-events: none;
}

.title { color: var(--primary-color, #409eff); font-size: 32px; margin-bottom: 10px; font-weight: 900;}
.subtitle { font-size: 13px; opacity: 0.6; margin-bottom: 40px; letter-spacing: 1px; }

.input-group input {
  width: 100%; 
  padding: 16px 0; 
  margin-bottom: 25px;
  background: transparent !important; 
  border: none;
  border-bottom: 1px solid rgba(64, 158, 255, 0.3);
  color: var(--text-color, #333); 
  outline: none;
  transition: all 0.3s ease; 
  font-size: 15px;
  font-family: 'PingFang SC', sans-serif;
  backdrop-filter: none;
  box-shadow: none;
}

.input-group input::placeholder {
  color: rgba(0, 0, 0, 0.3);
  opacity: 1;
}

.input-group input:hover {
  border-bottom-color: rgba(64, 158, 255, 0.6);
}

.input-group input:focus { 
  border-bottom-color: var(--primary-color, #409eff); 
  box-shadow: 0 2px 0 0 var(--primary-color, #409eff);
}

.input-group input:not(:placeholder-shown) {
  border-bottom-color: rgba(64, 158, 255, 0.8);
}

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

/* 微信登录样式 */
.login-divider { display: flex; align-items: center; margin: 14px 0; }
.login-divider::before, .login-divider::after { content: ''; flex: 1; height: 1px; background: rgba(64,158,255,0.2); }
.login-divider span { padding: 0 12px; font-size: 12px; color: #bbb; }

.glass-btn.wechat { 
  background: #07c160; color: white; 
  display: flex; align-items: center; justify-content: center; gap: 6px;
  box-shadow: 0 5px 15px rgba(7,193,96,0.3);
}
.glass-btn.wechat:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(7,193,96,0.5); }
.wechat-icon { font-size: 18px; }

.wechat-qr-box { text-align: center; padding: 20px 0; }
.qr-loading { display: flex; flex-direction: column; align-items: center; gap: 10px; color: #999; }
.qr-tip { font-size: 14px; color: #666; margin-bottom: 16px; }
.qr-placeholder { 
  width: 200px; height: 200px; margin: 0 auto;
  background: rgba(7,193,96,0.05); border: 2px dashed rgba(7,193,96,0.2);
  border-radius: 16px; display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.qr-icon { font-size: 48px; margin-bottom: 10px; }
.qr-placeholder p { font-size: 12px; color: #999; padding: 0 10px; line-height: 1.5; }

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
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.5);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color, #409eff);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

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

/* 自定义单选框样式 */
.custom-radio {
  display: flex;
  gap: 24px;
}

.custom-radio :deep(.el-radio) {
  font-size: 14px;
  color: var(--text-color, #333);
}

.custom-radio :deep(.el-radio__inner) {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(64, 158, 255, 0.3);
}

.custom-radio :deep(.el-radio__inner:hover) {
  border-color: var(--primary-color, #409eff);
}

.custom-radio :deep(.el-radio.is-checked .el-radio__inner) {
  border-color: var(--primary-color, #409eff);
  background: var(--primary-color, #409eff);
}

.custom-radio :deep(.el-radio.is-checked .el-radio__inner::after) {
  background: white;
}

/* 弹窗底部 */
.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.2);
  margin-top: 8px;
}

/* 弹窗按钮样式 */
.dialog-footer .glass-btn {
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

.dialog-footer .glass-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.dialog-footer .glass-btn:hover::before {
  left: 100%;
}

.dialog-footer .glass-btn.primary {
  background: linear-gradient(135deg, var(--primary-color, #409eff), #66b1ff);
  color: white;
  box-shadow: 0 4px 15px rgba(64,158,255,0.3), 0 2px 4px rgba(0,0,0,0.1);
}

.dialog-footer .glass-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64,158,255,0.4), 0 4px 8px rgba(0,0,0,0.15);
}

.dialog-footer .glass-btn.primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

.dialog-footer .glass-btn.secondary {
  background: rgba(255,255,255,0.4);
  backdrop-filter: blur(5px);
  color: var(--text-color, #333);
  border: 1px solid rgba(255,255,255,0.5);
}

.dialog-footer .glass-btn.secondary:hover {
  background: rgba(255,255,255,0.6);
  border-color: rgba(255,255,255,0.7);
}

.dialog-footer .glass-btn.secondary:active {
  background: rgba(255,255,255,0.3);
}

.dialog-footer .glass-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.dialog-footer .glass-btn:disabled:hover {
  box-shadow: none;
}
</style>