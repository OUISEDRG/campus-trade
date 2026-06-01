<template>
  <div class="multi-upload-container">
    <el-upload
      v-model:file-list="internalFileList"
      :action="uploadUrl"
      :headers="uploadHeaders"
      list-type="picture-card"
      :multiple="true"
      :limit="limit"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      :on-exceed="handleExceed"
      accept="image/jpeg,image/png,image/webp"
      class="glass-uploader"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <el-dialog v-model="dialogVisible" title="图片预览" append-to-body width="60%">
      <img :src="dialogImageUrl" alt="Preview" style="width: 100%; border-radius: 12px; object-fit: contain;" />
    </el-dialog>

    <div class="upload-tip">
      <el-icon style="vertical-align: -2px; margin-right: 4px;"><InfoFilled /></el-icon>
      支持 JPG/PNG/WEBP 格式，单张不超过 {{ maxSize }}MB，最多可上传 {{ limit }} 张
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useUserStore } from '../stores/user'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: ''
  },
  limit: {
    type: Number,
    default: 5
  },
  maxSize: {
    type: Number,
    default: 2
  }
})

const emit = defineEmits(['update:modelValue'])
const userStore = useUserStore()

const uploadUrl = request.defaults.baseURL + '/file/upload'
const uploadHeaders = computed(() => {
  return {
    'X-User-Id': userStore.activeUserId || ''
  }
})

const internalFileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

watch(() => props.modelValue, (newVal) => {
  if (!newVal) {
    internalFileList.value = []
    return
  }
  const urls = Array.isArray(newVal) ? newVal : newVal.split(',').filter(Boolean)
  
  const currentUrls = internalFileList.value.map(f => f.url)
  if (urls.join(',') !== currentUrls.join(',')) {
    internalFileList.value = urls.map((url, index) => ({
      name: `image-${index}`,
      url: url,
      status: 'success'
    }))
  }
}, { immediate: true })

const updateModelValue = () => {
  const urls = internalFileList.value
    .filter(file => file.status === 'success')
    .map(file => file.response?.data || file.url)
    .filter(url => url && !url.startsWith('blob:'))

  const emitValue = Array.isArray(props.modelValue) ? urls : urls.join(',')
  emit('update:modelValue', emitValue)
}

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  const isLtSize = file.size / 1024 / 1024 < props.maxSize

  if (!isImage) {
    ElMessage.error('仅支持 JPG/PNG/WEBP 格式的图片！')
    return false
  }
  if (!isLtSize) {
    ElMessage.error(`每张图片大小不能超过 ${props.maxSize}MB！`)
    return false
  }
  return true
}

const handleSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    updateModelValue()
  } else {
    ElMessage.error(response.message || '部分图片上传失败')
    internalFileList.value = internalFileList.value.filter(f => f.uid !== uploadFile.uid)
  }
}

const handleError = () => {
  ElMessage.error('网络或服务器异常，图片上传失败')
}

const handleRemove = () => {
  updateModelValue()
}

const handlePreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url || uploadFile.response?.data
  dialogVisible.value = true
}

const handleExceed = () => {
  ElMessage.warning(`数量超限，最多只能上传 ${props.limit} 张图片！`)
}
</script>

<style scoped>
.upload-tip {
  font-size: 12px;
  color: rgba(0,0,0,0.4);
  margin-top: 12px;
  line-height: 1.6;
  text-align: center;
}

.glass-uploader {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.glass-uploader :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  border: 2px dashed rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.glass-uploader :deep(.el-upload--picture-card:hover) {
  border-color: var(--primary-color, #409eff);
  background: rgba(64, 158, 255, 0.08);
  transform: scale(1.02);
}

.glass-uploader :deep(.el-upload--picture-card .el-icon) {
  font-size: 28px;
  color: var(--primary-color, #409eff);
}

.glass-uploader :deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  overflow: hidden;
}

.glass-uploader :deep(.el-upload-list__item .el-upload-list__item-status-label) {
  border-radius: 12px;
}

.glass-uploader :deep(.el-upload-list__item-actions) {
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
}

.glass-uploader :deep(.el-upload-list__item-name) {
  display: none;
}
</style>
