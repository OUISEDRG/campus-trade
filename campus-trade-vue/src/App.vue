<template>
  <div :class="['app-container', isDark ? 'dark-theme' : 'light-theme']">
    <!-- 动态氛围背景 -->
    <div class="dynamic-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- 路由视图：带切换动画 -->
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>

    <!-- 移动端底部固定导航栏 -->
    <nav class="mobile-nav" v-if="showNav">
      <div class="nav-item" @click="router.push('/home')" :class="{ active: route.path === '/home' }">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item" @click="router.push('/category')" :class="{ active: route.path === '/category' }">
        <el-icon><Menu /></el-icon>
        <span>分类</span>
      </div>
      <div class="nav-item" @click="router.push('/me')" :class="{ active: route.path === '/me' }">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </nav>

    <!-- 悬浮深色模式切换按钮 -->
    <div class="theme-toggle" @click="isDark = !isDark">
      <el-icon v-if="isDark"><Sunny /></el-icon>
      <el-icon v-else><Moon /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, Menu, User, Sunny, Moon } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const isDark = ref(false)

const showNav = computed(() => route.path !== '/')
</script>

<style>
:root {
  --glass-bg: rgba(255, 255, 255, 0.45);
  --glass-border: rgba(255, 255, 255, 0.3);
  --text-color: #333;
  --primary-color: #409eff;
}

.dark-theme {
  --glass-bg: rgba(30, 30, 30, 0.6);
  --glass-border: rgba(255, 255, 255, 0.1);
  --text-color: #eee;
  --primary-color: #66b1ff;
}

body { margin: 0; font-family: 'PingFang SC', sans-serif; overflow-x: hidden; }

.app-container {
  min-height: 100vh;
  transition: background 0.5s ease;
  color: var(--text-color);
}

/* 动态背景 */
.dynamic-bg {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  z-index: -1;
  background: #f0f2f5;
  overflow: hidden;
}
.dark-theme .dynamic-bg { background: #1a1a1a; }

.blob {
  position: absolute;
  filter: blur(80px);
  opacity: 0.5;
  border-radius: 50%;
  animation: move 20s infinite alternate;
}
.blob-1 { width: 500px; height: 500px; background: #a2d2ff; top: -100px; left: -100px; }
.blob-2 { width: 400px; height: 400px; background: #ffafbd; bottom: -100px; right: -100px; animation-delay: -5s; }
.blob-3 { width: 300px; height: 300px; background: #ccffbd; top: 40%; left: 50%; animation-delay: -10s; }

@keyframes move {
  from { transform: translate(0, 0) rotate(0deg); }
  to { transform: translate(100px, 100px) rotate(360deg); }
}

/* 页面切换动画 */
.page-fade-enter-active, .page-fade-leave-active { transition: all 0.4s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(10px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-10px); }

/* 移动端导航 */
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0; width: 100%; height: 65px;
  background: var(--glass-bg);
  backdrop-filter: blur(15px);
  border-top: 1px solid var(--glass-border);
  z-index: 1000;
  justify-content: space-around;
  align-items: center;
}

.nav-item {
  display: flex; flex-direction: column; align-items: center;
  color: #888; cursor: pointer; transition: 0.3s;
}
.nav-item.active { color: var(--primary-color); transform: translateY(-5px); }

@media (max-width: 768px) { .mobile-nav { display: flex; } }

.theme-toggle {
  position: fixed; top: 85px; right: 30px;
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--glass-border);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; z-index: 2000; box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}
</style>