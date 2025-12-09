<template>
  <a-modal
    v-model="visible"
    title="设备录像下载"
    :width="480"
    :footer="null"
    :destroyOnClose="true"
    :closable="true"
    :maskClosable="false"
    :bodyStyle="{ padding: '16px' }"
    @cancel="handleCancel"
    wrapClassName="device-record-download-modal"
  >
    <div class="download-container">
      <!-- 下载状态 -->
      <div v-if="downloadState === 'idle'" class="download-idle">
        <div class="download-info">
          <a-icon type="info-circle" class="info-icon" />
          <div class="info-text">
            <p>文件名：{{ record.name || '设备录像' }}</p>
            <p>时间：{{ formatTimeRange(record.startTime, record.endTime) }}</p>
            <p>大小：{{ formatFileSize(record.fileSize) || '未知' }}</p>
            <!-- 文件大小警告提示 -->
            <a-alert
              v-if="isLargeFile"
              type="warning"
              show-icon
              style="margin-top: 12px;"
            >
              <template slot="message">
                <div style="font-size: 13px;">
                  <strong>提示：</strong>文件大小超过 100MB，下载可能较慢或失败，请慎重选择。
                </div>
              </template>
            </a-alert>
          </div>
        </div>
        <div class="download-actions">
          <a-button @click="handleCancel">取消</a-button>
          <a-button type="primary" :loading="starting" @click="startDownload">
            开始下载
          </a-button>
        </div>
      </div>
      
      <!-- 下载进度（参考 wvp 官方实现） -->
      <div v-else-if="downloadState === 'downloading' || downloadState === 'completed'" class="download-progress">
        <div class="progress-info">
          <div class="progress-text">
            <span>{{ downloadState === 'completed' ? '下载完成' : '正在下载...' }}</span>
            <span>{{ progress }}%</span>
          </div>
          <a-progress :percent="progress" :status="downloadState === 'completed' ? 'success' : progressStatus" />
        </div>
        <div class="progress-actions">
          <!-- 下载完成后显示下载按钮（参考 wvp 官方） -->
          <a-button 
            v-if="downloadUrl" 
            type="primary" 
            icon="download" 
            @click="startBrowserDownload"
          >
            下载
          </a-button>
          <a-button 
            v-else-if="downloadState === 'downloading'" 
            type="danger" 
            @click="stopDownload"
          >
            <a-icon type="close" /> 取消
          </a-button>
          <a-button @click="handleCancel">
            {{ downloadState === 'completed' ? '关闭' : '取消' }}
          </a-button>
        </div>
      </div>
      
      <!-- 下载失败 -->
      <div v-else-if="downloadState === 'failed'" class="download-failed">
        <div class="result-info">
          <a-icon type="close-circle" class="error-icon" />
          <div class="result-text">
            <h3>下载失败</h3>
            <p>{{ errorMessage }}</p>
          </div>
        </div>
        <div class="result-actions">
          <a-button @click="handleCancel">关闭</a-button>
          <a-button type="primary" @click="retryDownload">
            <a-icon type="reload" /> 重试
          </a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { 
  startGBRecordDownload, 
  stopGBRecordDownload, 
  getGBRecordDownloadProgress 
} from '@/api/video/channel'

export default {
  name: 'DeviceRecordDownload',
  props: {
    // 模态框显示状态
    value: {
      type: Boolean,
      default: false
    },
    // 设备信息
    device: {
      type: Object,
      required: true
    },
    // 通道信息
    channel: {
      type: Object,
      required: true
    },
    // 实例Key
    instanceKey: {
      type: String,
      required: true
    },
    // 录像记录
    record: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      // 下载状态: idle, downloading, paused, completed, failed
      downloadState: 'idle',
      // 是否正在启动
      starting: false,
      // 下载进度
      progress: 0,
      // 下载速度
      downloadSpeed: '0 KB/s',
      // 剩余时间
      remainingTime: '--:--:--',
      // 是否暂停
      paused: false,
      // 进度状态
      progressStatus: 'active',
      // 错误信息
      errorMessage: '',
      // 下载任务ID
      downloadTaskId: null,
      // 流信息
      streamInfo: null,
      // 下载链接
      downloadUrl: null,
      // 进度查询定时器
      progressTimer: null
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    },
    // 判断是否为大文件（超过100MB）
    isLargeFile() {
      const fileSize = this.record.fileSize
      if (!fileSize) return false
      
      // 如果是字符串格式（如 "4.56 MB"），提取数字
      if (typeof fileSize === 'string') {
        if (fileSize.includes('MB')) {
          const mb = parseFloat(fileSize)
          return !isNaN(mb) && mb > 100
        } else if (fileSize.includes('GB')) {
          return true // GB 肯定超过 100MB
        } else if (fileSize.includes('KB')) {
          const kb = parseFloat(fileSize)
          return !isNaN(kb) && kb > 100 * 1024
        } else if (fileSize.includes('B')) {
          const bytes = parseFloat(fileSize)
          return !isNaN(bytes) && bytes > 100 * 1024 * 1024
        }
        // 尝试解析为数字
        const num = parseFloat(fileSize)
        if (!isNaN(num)) {
          // 假设是字节数
          return num > 100 * 1024 * 1024
        }
      } else if (typeof fileSize === 'number') {
        // 假设是字节数
        return fileSize > 100 * 1024 * 1024
      }
      
      return false
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        // 检查记录数据是否有效
        if (!this.record || !this.record.startTime || !this.record.endTime) {
          this.$message.error('录像数据无效')
          this.visible = false
          return
        }
        this.onModalOpen()
      } else {
        this.onModalClose()
      }
    }
  },
  beforeDestroy() {
    this.cleanup()
  },
  methods: {
    // 模态框打开
    onModalOpen() {
      this.resetState()
      // 检查是否支持下载
      if (!this.checkDownloadSupport()) {
        this.$message.warning('当前浏览器不支持下载功能')
        return
      }
    },
    
    // 模态框关闭
    onModalClose() {
      this.cleanup()
    },
    
    // 检查下载支持
    checkDownloadSupport() {
      // 检查是否有下载权限
      const hasPermission = this.$hasPermi(['video:record:download'])
      if (!hasPermission) {
        this.$message.error('没有下载权限')
        return false
      }
      return true
    },
    
    // 重置状态
    resetState() {
      this.downloadState = 'idle'
      this.starting = false
      this.progress = 0
      this.downloadSpeed = '0 KB/s'
      this.remainingTime = '--:--:--'
      this.paused = false
      this.progressStatus = 'active'
      this.errorMessage = ''
      this.downloadTaskId = null
      this.streamInfo = null
      this.downloadUrl = null
    },
    
    // 清理资源（参考 wvp 官方实现）
    cleanup() {
      // 停止进度查询
      if (this.progressTimer) {
        clearTimeout(this.progressTimer)
        this.progressTimer = null
      }
      
      // 如果正在下载且未完成，通知后端停止（参考 wvp 官方 close 方法）
      if (this.downloadState === 'downloading' && this.streamInfo && this.progress < 1) {
        this.stopDownloadTask()
      }
      this.streamInfo = null
    },
    
    // 开始下载
    async startDownload() {
      if (this.starting) return
      
      this.starting = true
      this.downloadState = 'downloading'
      this.progress = 0
      
      try {
        // 格式化时间
        const startTime = this.formatApiTime(this.record.startTime)
        const endTime = this.formatApiTime(this.record.endTime)
        
        // 调用后端接口开始下载（参考record.vue，使用最大倍速）
        const res = await startGBRecordDownload(
          this.instanceKey,
          this.device.deviceId,
          this.channel.channelId,
          {
            startTime: startTime,
            endTime: endTime,
            downloadSpeed: 16 // 使用最大倍速下载（注意：后端期望的参数名是 downloadSpeed，不是 speed）
          }
        )
        
        // 后端返回的 code 是 0 表示成功，不是 200
        if (res.code === 0 && res.data) {
          // res.data 应该是 streamInfo 对象，包含 app, stream, mediaServerId
          this.streamInfo = res.data
          this.downloadTaskId = res.data.stream || res.data.streamId || res.data
          this.$message.success('开始下载任务')
          // 开始轮询进度
          this.startProgressPolling()
        } else {
          // 更好的错误消息处理
          let errorMsg = '开始下载失败'
          if (res.msg && res.msg !== 'success') {
            errorMsg = res.msg
          } else if (!res.data) {
            errorMsg = '下载任务创建失败，未返回任务信息'
          }
          throw new Error(errorMsg)
        }
      } catch (error) {
        this.downloadState = 'failed'
        this.errorMessage = error.message || '开始下载失败'
        this.$message.error(this.errorMessage)
      } finally {
        this.starting = false
      }
    },
    
    // 开始进度轮询（参考 wvp 官方实现）
    startProgressPolling() {
      // 延迟5秒后开始查询（参考recordDownload.vue）
      this.getProgressTimer()
    },
    
    // 进度查询定时器（参考 wvp 官方实现）
    getProgressTimer() {
      if (this.downloadState !== 'downloading') {
        return
      }
      if (this.downloadUrl) {
        // 如果已经有下载地址，停止轮询
        return
      }
      setTimeout(() => {
        if (this.visible && this.downloadState === 'downloading') {
          this.queryProgress()
        }
      }, 5000)
    },
    
    // 查询下载进度
    async queryProgress() {
      if (!this.streamInfo || !this.streamInfo.stream) {
        console.warn('缺少流信息，无法查询进度')
        return
      }
      
      // 如果已经完成或失败，停止轮询
      if (this.downloadState === 'completed' || this.downloadState === 'failed') {
        return
      }
      
      try {
        // 使用stream查询进度（参考record.vue）
        const res = await getGBRecordDownloadProgress(
          this.instanceKey,
          this.device.deviceId,
          this.channel.channelId,
          this.streamInfo.stream
        )
        
        // 后端返回的 code 是 0 表示成功，不是 200
        if (res.code === 0 && res.data) {
          const progressData = res.data
          
          // 更新进度信息（参考recordDownload.vue）
          if (parseFloat(progressData.progress) === 1) {
            this.progress = 100
          } else {
            this.progress = parseFloat((parseFloat(progressData.progress) * 100).toFixed(1))
          }
          
          // 如果有下载文件路径，说明下载完成
          if (progressData.downLoadFilePath) {
            let downloadPath = ''
            if (location.protocol === 'https:') {
              downloadPath = progressData.downLoadFilePath.httpsPath || progressData.downLoadFilePath.httpPath
            } else {
              downloadPath = progressData.downLoadFilePath.httpPath || progressData.downLoadFilePath.httpsPath
            }
            
            if (downloadPath) {
              this.downloadUrl = downloadPath
              this.downloadCompleted(progressData)
              return
            }
          }
          
          // 继续轮询（每5秒查询一次，参考 wvp 官方实现）
          if (this.progress < 100 && this.downloadState === 'downloading' && !this.downloadUrl) {
            this.getProgressTimer()
          }
        } else {
          // API返回错误，继续轮询
          if (this.downloadState === 'downloading' && !this.downloadUrl) {
            this.getProgressTimer()
          }
        }
      } catch (error) {
        console.error('查询下载进度失败:', error)
        // 继续轮询，不立即失败（参考 wvp 官方实现）
        if (this.downloadState === 'downloading' && !this.downloadUrl) {
          this.getProgressTimer()
        }
      }
    },
    
    // 下载完成处理（参考 wvp 官方实现，不自动下载，显示下载按钮）
    downloadCompleted(progress) {
      this.downloadState = 'completed'
      this.progress = 100
      this.progressStatus = 'success'
      
      // 停止进度查询
      if (this.progressTimer) {
        clearTimeout(this.progressTimer)
        this.progressTimer = null
      }
      
      this.$message.success('下载完成，请点击下载按钮保存文件')
    },
    
    // 下载失败处理
    downloadFailed(errorMessage) {
      this.downloadState = 'failed'
      this.errorMessage = errorMessage
      this.progressStatus = 'exception'
      
      this.$message.error('下载失败: ' + errorMessage)
    },
    
    // 暂停下载
    async pauseDownload() {
      this.paused = true
      this.progressStatus = 'normal'
      // 这里可以实现真正的暂停逻辑
      this.$message.info('下载已暂停')
    },
    
    // 继续下载
    async resumeDownload() {
      this.paused = false
      this.progressStatus = 'active'
      // 这里可以实现真正的继续逻辑
      this.$message.info('继续下载')
    },
    
    // 停止下载
    async stopDownload() {
      if (this.streamInfo) {
        await this.stopDownloadTask()
      }
      
      this.downloadState = 'failed'
      this.errorMessage = '下载已取消'
      this.$message.info('下载已取消')
    },
    
    // 停止下载任务
    async stopDownloadTask() {
      try {
        if (this.streamInfo && this.streamInfo.stream) {
          await stopGBRecordDownload(
            this.instanceKey,
            this.device.deviceId,
            this.channel.channelId,
            this.streamInfo.stream
          )
        }
      } catch (error) {
        console.error('停止下载任务失败:', error)
      } finally {
        // 停止进度查询
        if (this.progressTimer) {
          clearTimeout(this.progressTimer)
          this.progressTimer = null
        }
      }
    },
    
    // 重试下载
    retryDownload() {
      this.resetState()
      this.startDownload()
    },
    
    // 开始浏览器下载（参考 wvp 官方 recordDownload.vue）
    startBrowserDownload() {
      if (!this.downloadUrl) {
        this.$message.warning('下载地址不存在，请稍后重试')
        return
      }
      
      // 使用XMLHttpRequest下载（参考recordDownload.vue）
      const x = new XMLHttpRequest()
      x.open('GET', this.downloadUrl, true)
      x.responseType = 'blob'
      x.onload = (e) => {
        if (x.status === 200) {
          const url = window.URL.createObjectURL(x.response)
          const a = document.createElement('a')
          a.href = url
          // 构建文件名（参考 wvp 官方：deviceId-channelId.mp4）
          const fileName = `${this.device.deviceId}-${this.channel.channelId}.mp4`
          a.download = fileName
          a.click()
          window.URL.revokeObjectURL(url)
        } else {
          this.$message.error('下载文件失败：HTTP ' + x.status)
        }
      }
      x.onerror = () => {
        this.$message.error('下载文件失败：网络错误')
      }
      x.send()
    },
    
    // 打开下载文件夹
    openDownloadFolder() {
      // 尝试打开下载文件夹
      if (window.showOpenFilePicker) {
        // 现代浏览器支持
        window.showOpenFilePicker().catch(() => {
          this.$message.info('请在浏览器下载管理中查看文件')
        })
      } else {
        // 旧版浏览器提示
        this.$message.info('请在浏览器下载管理中查看文件')
      }
    },
    
    // 关闭模态框
    handleCancel() {
      this.visible = false
    },
    
    // 工具方法
    formatApiTime(timestamp) {
      if (typeof timestamp === 'string') {
        timestamp = parseInt(timestamp)
      }
      if (timestamp.toString().length === 10) {
        timestamp = timestamp * 1000
      }
      const date = new Date(timestamp)
      return date.getFullYear() + '-' + 
             String(date.getMonth() + 1).padStart(2, '0') + '-' + 
             String(date.getDate()).padStart(2, '0') + ' ' +
             String(date.getHours()).padStart(2, '0') + ':' +
             String(date.getMinutes()).padStart(2, '0') + ':' +
             String(date.getSeconds()).padStart(2, '0')
    },
    
    formatTimeRange(startTime, endTime) {
      const start = this.formatTime(startTime)
      const end = this.formatTime(endTime)
      return `${start} - ${end}`
    },
    
    formatTime(timestamp) {
      if (!timestamp) return '-'
      let time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      if (!time || Number.isNaN(time)) return '-'
      if (time.toString().length === 10) {
        time = time * 1000
      }
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    
    formatFileSize(bytes) {
      // 如果已经是格式化后的字符串（如 "4.56 MB"），直接返回
      if (typeof bytes === 'string' && bytes.includes(' ')) {
        return bytes
      }
      // 如果是数字，进行格式化
      if (typeof bytes === 'number' && bytes > 0) {
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
        const i = Math.floor(Math.log(bytes) / Math.log(1024))
        return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
      }
      // 如果是字符串数字，尝试转换
      if (typeof bytes === 'string') {
        const numBytes = parseFloat(bytes)
        if (!isNaN(numBytes) && numBytes > 0) {
          const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
          const i = Math.floor(Math.log(numBytes) / Math.log(1024))
          return Math.round(numBytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
        }
      }
      return null
    },
    
    formatSpeed(speed) {
      if (speed < 1024) {
        return speed + ' B/s'
      } else if (speed < 1024 * 1024) {
        return (speed / 1024).toFixed(1) + ' KB/s'
      } else {
        return (speed / (1024 * 1024)).toFixed(1) + ' MB/s'
      }
    },
    
    formatRemainingTime(seconds) {
      if (!seconds || seconds <= 0) return '--:--:--'
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = Math.floor(seconds % 60)
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }
  }
}
</script>

<style lang="less" scoped>
.download-container {
  padding: 0;
  margin: 0;
  min-height: auto;
  max-height: none;
  overflow: visible;
}

// 扁平化超窄边框样式
/deep/ .device-record-download-modal {
  .ant-modal {
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #e8e8e8;
    padding-bottom: 0 !important;
  }
  
  .ant-modal-content {
    border-radius: 4px;
    overflow: hidden;
    box-shadow: none;
    display: flex;
    flex-direction: column;
  }
  
  .ant-modal-header {
    padding: 12px 16px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;
    border-radius: 4px 4px 0 0;
    flex-shrink: 0;
    
    .ant-modal-title {
      font-size: 14px;
      font-weight: 500;
      color: #262626;
    }
  }
  
  .ant-modal-body {
    padding: 16px;
    background: #fff;
    min-height: auto !important;
    max-height: none !important;
    flex: 1;
    overflow: visible;
  }
  
  .ant-modal-footer {
    display: none !important;
    padding: 0 !important;
    margin: 0 !important;
    height: 0 !important;
    min-height: 0 !important;
  }
  
  .ant-modal-close {
    top: 12px;
    right: 16px;
  }
  
  .ant-modal-close-x {
    width: 28px;
    height: 28px;
    line-height: 28px;
    font-size: 16px;
    color: #8c8c8c;
    
    &:hover {
      color: #262626;
    }
  }
}

.download-idle {
  .download-info {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .info-icon {
      font-size: 24px;
      color: #1890ff;
      margin-right: 12px;
      margin-top: 2px;
    }
    
    .info-text {
      flex: 1;
      
      p {
        margin: 0 0 8px 0;
        color: #595959;
        font-size: 14px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
  
  .download-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.download-progress {
  .progress-info {
    margin-bottom: 20px;
    
    .progress-text {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      color: #262626;
    }
    
    .progress-details {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
      font-size: 12px;
      color: #8c8c8c;
    }
  }
  
  .progress-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.download-completed,
.download-failed {
  .result-info {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .success-icon {
      font-size: 32px;
      color: #52c41a;
      margin-right: 12px;
    }
    
    .error-icon {
      font-size: 32px;
      color: #ff4d4f;
      margin-right: 12px;
    }
    
    .result-text {
      flex: 1;
      
      h3 {
        margin: 0 0 4px 0;
        font-size: 16px;
        font-weight: 500;
        color: #262626;
      }
      
      p {
        margin: 0;
        font-size: 14px;
        color: #595959;
      }
    }
  }
  
  .result-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>