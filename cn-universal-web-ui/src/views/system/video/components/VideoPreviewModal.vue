<!-- eslint-disable -->
<template>
  <a-modal
    :visible="visible"
    :title="null"
    width="1200px"
    :footer="null"
    :destroyOnClose="true"
    :maskClosable="false"
    :closable="true"
    @cancel="handleClose"
    class="video-preview-modal resizable-modal"
    :wrapClassName="'resizable-modal-wrap'"
  >
    <div class="video-preview-container">
      <!-- 顶部标题栏（合并设备信息） -->
      <div class="preview-header" ref="dragHandle">
        <div class="header-left">
          <a-icon type="video-camera" class="header-icon" />
          <span class="header-title">视频预览</span>
          <span class="device-name">{{ deviceInfo?.deviceName }}</span>
          
          <!-- 设备信息 -->
          <div class="device-info-inline">
            <div class="info-item">
              <span class="info-label">设备ID</span>
              <span class="info-value">{{ deviceInfo?.deviceId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">产品Key</span>
              <span class="info-value">{{ deviceInfo?.productKey }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">设备状态</span>
              <a-tag :color="deviceInfo?.state ? 'green' : 'red'" size="small">
                {{ deviceInfo?.state ? '在线' : '离线' }}
              </a-tag>
            </div>
          </div>
        </div>
        <div class="header-right">
          <!-- 视频状态显示 -->
          <div class="video-status-header">
            <span class="status-label">状态:</span>
            <span class="status-value" :class="getStatusClass()">{{ videoStatus }}</span>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="preview-content">
        <!-- 视频播放区域 -->
        <div class="video-panel">
          <div class="video-container" ref="videoContainer">
            <!-- ImouPlayer 播放器 -->
            <div v-if="playerVisible" ref="playerContainer" :id="playerContainerId" class="imou-player-container">
              <!-- ImouPlayer 将在这里初始化 -->
            </div>
            
            <!-- 占位符 -->
            <div v-else class="video-placeholder">
              <div class="placeholder-content">
                <a-icon type="video-camera" class="placeholder-icon" />
                <div class="placeholder-text">点击开始预览</div>
                <div class="placeholder-desc">选择码流类型和传输协议后开始播放</div>
              </div>
            </div>
          </div>

          <!-- 视频控制栏 -->
          <div class="video-controls">
            <!-- 左侧：码流和协议设置 -->
            <div class="controls-left">
              <!-- 码流类型选择 -->
              <div class="stream-selector">
                <label class="control-label">码流:</label>
                <a-radio-group v-model="previewOptions.streamType" @change="onStreamTypeChange" size="small" class="stream-radio-group">
                  <a-radio-button value="main" class="stream-option">
                    <a-icon type="video-camera" />
                    主码流
                  </a-radio-button>
                  <a-radio-button value="sub" class="stream-option">
                    <a-icon type="mobile" />
                    辅码流
                  </a-radio-button>
                </a-radio-group>
              </div>
              
              <!-- 分隔线 -->
              <a-divider type="vertical" />
              
              <!-- 传输协议选择 -->
              <div class="protocol-selector">
                <label class="control-label">协议:</label>
                <a-select v-model="previewOptions.protocol" @change="onProtocolChange" size="small" class="protocol-select">
                  <a-select-option value="hls">HLS</a-select-option>
                  <a-select-option value="flv">FLV</a-select-option>
                  <a-select-option value="rtmp">RTMP</a-select-option>
                </a-select>
              </div>
              
              <!-- 分隔线 -->
              <a-divider type="vertical" />
              
              <!-- 基础播放控制 -->
              <a-button size="small" @click="togglePlay" :type="isPlaying ? 'default' : 'primary'">
                <a-icon :type="isPlaying ? 'pause' : 'play-circle'" />
                {{ isPlaying ? '暂停' : '播放' }}
              </a-button>
              <a-button size="small" @click="toggleMute">
                <a-icon :type="isMuted ? 'mute' : 'sound'" />
                {{ isMuted ? '取消静音' : '静音' }}
              </a-button>
              <a-button size="small" @click="handleStop">
                <a-icon type="stop" />
                停止
              </a-button>
              
              <!-- 分隔线 -->
              <a-divider type="vertical" />
              
              <!-- 截图和录制 -->
              <a-button size="small" @click="handleCapture" :loading="captureLoading">
                <a-icon type="camera" />
                截图
              </a-button>
              <a-button size="small" @click="toggleRecord" :type="isRecording ? 'danger' : 'default'">
                <a-icon :type="isRecording ? 'pause-circle' : 'video-camera'" />
                {{ isRecording ? '停止录制' : '开始录制' }}
              </a-button>
              
              <!-- 对讲功能 -->
              <a-button size="small" @click="toggleTalk" :type="isTalking ? 'danger' : 'default'">
                <a-icon :type="isTalking ? 'stop' : 'phone'" />
                {{ isTalking ? '停止对讲' : '开始对讲' }}
              </a-button>
              
              <!-- 分隔线 -->
              <a-divider type="vertical" />
              
              <!-- 缩放控制 -->
              <a-button size="small" @click="zoomIn">
                <a-icon type="zoom-in" />
                放大
              </a-button>
              <a-button size="small" @click="zoomOut">
                <a-icon type="zoom-out" />
                缩小
              </a-button>
              
              <!-- 全屏按钮 -->
              <a-button size="small" @click="toggleFullscreen" class="fullscreen-btn">
                <a-icon type="fullscreen" />
                全屏
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { functionDown } from '@/api/system/dev/instance'
import { loadExternalScript } from '@/utils/loadScript'

export default {
  name: 'VideoPreviewModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    deviceInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      previewOptions: {
        streamType: 'main', // main: 高清主码流, sub: 标清辅码流
        protocol: 'hls' // flv, rtmp, hls
      },
      previewLoading: false,
      playerVisible: false,
      player: null,
      playerContainerId: 'imou-preview-player',
      isPlaying: false,
      isMuted: false,
      isRecording: false,
      isTalking: false,
      captureLoading: false,
      videoStatus: '未连接'
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.resetVideoState()
        // 延迟自动开始预览
        this.$nextTick(() => {
          setTimeout(() => {
            this.startPreview()
          }, 500)
          // 初始化拖拽和缩放功能
          this.initDragAndResize()
        })
      } else {
        this.destroyPlayer()
        this.removeDragAndResize()
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    // 获取状态样式类
    getStatusClass() {
      const statusMap = {
        '未连接': 'status-disconnected',
        '正在连接...': 'status-connecting',
        '正在初始化...': 'status-initializing',
        '正在切换...': 'status-connecting',
        '切换失败': 'status-error',
        '初始化完成': 'status-ready',
        '已停止': 'status-stopped',
        '播放错误': 'status-error',
        '连接失败': 'status-error',
        '连接错误': 'status-error',
        '获取地址失败': 'status-error',
        'SDK加载失败': 'status-error',
        '初始化失败': 'status-error'
      }
      return statusMap[this.videoStatus] || 'status-default'
    },
    // 码流类型切换
    onStreamTypeChange() {
      console.log('码流类型切换:', this.previewOptions.streamType)
      if (this.playerVisible) {
        this.restartPreview()
      }
    },
    // 传输协议切换
    onProtocolChange() {
      console.log('传输协议切换:', this.previewOptions.protocol)
      if (this.playerVisible) {
        this.restartPreview()
      }
    },
    // 重新开始预览
    async restartPreview() {
      try {
        this.videoStatus = '正在切换...'
        this.destroyPlayer()
        this.playerVisible = false
        
        // 延迟一下再重新开始预览
        setTimeout(() => {
          this.startPreview()
        }, 500)
      } catch (e) {
        console.error('重启预览失败:', e)
        this.videoStatus = '切换失败'
      }
    },
    resetVideoState() {
      this.playerVisible = false
      this.isPlaying = false
      this.isMuted = false
      this.isRecording = false
      this.isTalking = false
      this.captureLoading = false
      this.videoStatus = '未连接'
      this.previewLoading = false
      this.destroyPlayer()
    },
    destroyPlayer() {
      try {
        if (this.player && this.player.destroy) {
          this.player.destroy()
        }
        this.player = null
      } catch (e) {
        console.error('Destroy player error:', e)
      }
    },
    async startPreview() {
      try {
        this.previewLoading = true
        this.videoStatus = '正在连接...'
        
        // 构建请求参数
        const requestData = {
          appUnionId: 'iot',
          productKey: this.deviceInfo.productKey,
          deviceId: this.deviceInfo.deviceId,
          cmd: 'DEV_FUNCTION',
          function: {
            messageType: 'FUNCTIONS',
            function: 'cameraLiveStream',
            data: {
              streamType: this.previewOptions.streamType,
              protocol: this.previewOptions.protocol
            }
          }
        }
        
        // 通过functionDown接口获取播放地址
        const response = await functionDown(this.deviceInfo.productKey, requestData)
        
        if (response.code === 0 && response.data) {
          const { url, token, type } = response.data
          
          if (!url) {
            this.$message.warning('设备暂无预览地址')
            this.videoStatus = '获取地址失败'
            return
          }
          
          // 保存token到deviceInfo中，供ImouPlayer使用
          this.deviceInfo.token = token
          
          // 使用ImouPlayer SDK播放
          await this.initVideoPlayer(url)
          
          this.$message.success('视频连接成功')
        } else {
          this.$message.error(response.msg || '获取播放地址失败')
          this.videoStatus = '连接失败'
        }
      } catch (e) {
        this.$message.error(e.message || '获取播放地址失败')
        this.videoStatus = '连接错误'
      } finally {
        this.previewLoading = false
      }
    },
    async initVideoPlayer(url) {
      try {
        // 显示播放器容器
        this.playerVisible = true
        this.videoStatus = '正在初始化...'
        
        // 确保ImouPlayer SDK已加载
        if (typeof imouPlayer === 'undefined') {
          try {
            await loadExternalScript('/imou/imou-player.js')
            // 检查SDK是否成功加载
            if (typeof imouPlayer === 'undefined') {
              throw new Error('ImouPlayer SDK加载失败')
            }
          } catch (e) {
            this.videoStatus = 'SDK加载失败'
            this.$message.error('ImouPlayer SDK加载失败: ' + e.message)
            console.error('SDK load error:', e)
            return
          }
        }
        
        // 等待DOM更新
        await this.$nextTick()
        
        // 初始化ImouPlayer
        const container = this.$refs.playerContainer
        if (!container) {
          throw new Error('Player container not found')
        }
        
        // 清理之前的播放器
        if (this.player && this.player.destroy) {
          this.player.destroy()
          this.player = null
        }
        
        const opts = {
          id: this.playerContainerId,
          width: container.clientWidth || 800,
          height: container.clientHeight || 450,
          deviceId: String(this.deviceInfo.deviceId),
          channelId: 0,
          token: String(this.deviceInfo.token || ''),
          type: 1, // 直播
          streamId: this.previewOptions.streamType === 'main' ? 0 : 1,
          recordType: "cloud",
          WasmLibPath: '/imou/', // 使用绝对路径
          muted: false,
          handleError: (err) => {
            this.videoStatus = '播放错误'
            // this.$message.error('视频播放失败: ' + (typeof err === 'string' ? err : JSON.stringify(err)))
            console.error('ImouPlayer error:', err)
          }
        }
        console.log(opts);
        // 创建播放器实例 - 按照官方demo方式
        this.player = new imouPlayer(opts)
        this.videoStatus = '初始化完成'
        
      } catch (e) {
        this.videoStatus = '初始化失败'
        this.$message.error('播放器初始化失败: ' + e.message)
        console.error('Init ImouPlayer error:', e)
      }
    },
    togglePlay() {
      try {
        if (this.player) {
          if (this.isPlaying) {
            this.player.pause()
          } else {
            this.player.play()
          }
        }
      } catch (e) {
        console.error('Toggle play error:', e)
      }
    },
    toggleMute() {
      try {
        if (this.player) {
          this.player.volume(this.isMuted ? 1 : 0)
          this.isMuted = !this.isMuted
        }
      } catch (e) {
        console.error('Toggle mute error:', e)
      }
    },
    toggleFullscreen() {
      try {
        if (this.player) {
          this.player.fullScreen()
        }
      } catch (e) {
        console.error('Toggle fullscreen error:', e)
      }
    },
    // 停止播放
    handleStop() {
      try {
        if (this.player) {
          this.player.stop()
          this.isPlaying = false
          this.videoStatus = '已停止'
        }
      } catch (e) {
        console.error('Stop error:', e)
      }
    },
    // 截图功能
    async handleCapture() {
      try {
        this.captureLoading = true
        if (this.player && this.player.capture) {
          await this.player.capture()
          this.$message.success('截图成功')
        } else {
          this.$message.warning('当前播放器不支持截图功能')
        }
      } catch (e) {
        this.$message.error('截图失败: ' + e.message)
        console.error('Capture error:', e)
      } finally {
        this.captureLoading = false
      }
    },
    // 录制功能
    toggleRecord() {
      try {
        if (this.player) {
          if (this.isRecording) {
            this.player.stopRecord()
            this.isRecording = false
            this.$message.success('录制已停止')
          } else {
            this.player.startRecord()
            this.isRecording = true
            this.$message.success('开始录制')
          }
        }
      } catch (e) {
        this.$message.error('录制操作失败: ' + e.message)
        console.error('Record toggle error:', e)
      }
    },
    // 对讲功能
    toggleTalk() {
      try {
        if (this.player) {
          if (this.isTalking) {
            this.player.stopTalk()
            this.isTalking = false
            this.$message.success('对讲已停止')
          } else {
            // // 检查浏览器是否支持音频录制
            // if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            //   this.$message.warning('当前浏览器不支持音频录制功能')
            //   return
            // }
            this.player.startTalk()
            // 检查麦克风权限
            // navigator.mediaDevices.getUserMedia({ audio: true })
            //   .then(() => {
            //     this.player.startTalk()
            //     this.isTalking = true
            //     this.$message.success('开始对讲')
            //   })
            //   .catch((error) => {
            //     console.error('麦克风权限获取失败:', error)
            //     if (error.name === 'NotAllowedError') {
            //       this.$message.error('请允许访问麦克风权限')
            //     } else if (error.name === 'NotFoundError') {
            //       this.$message.error('未找到麦克风设备')
            //     } else {
            //       this.$message.error('麦克风访问失败: ' + error.message)
            //     }
            //   })
          }
        } else {
          this.$message.warning('播放器未初始化')
        }
      } catch (e) {
        console.error('Talk toggle error:', e)
        if (e.name === 'AbortError') {
          this.$message.error('对讲功能被中断，请重试')
        } else {
          this.$message.error('对讲操作失败: ' + (e.message || '未知错误'))
        }
      }
    },
    // 缩放功能
    zoomIn() {
      try {
        if (this.player && this.player.zoomIn) {
          this.player.zoomIn()
          this.$message.success('放大')
        }
      } catch (e) {
        console.error('Zoom in error:', e)
      }
    },
    zoomOut() {
      try {
        if (this.player && this.player.zoomOut) {
          this.player.zoomOut()
          this.$message.success('缩小')
        }
      } catch (e) {
        console.error('Zoom out error:', e)
      }
    },
    resetZoom() {
      try {
        if (this.player && this.player.resetZoom) {
          this.player.resetZoom()
          this.$message.success('缩放已重置')
        }
      } catch (e) {
        console.error('Reset zoom error:', e)
      }
    },
    // 初始化拖拽和缩放功能
    initDragAndResize() {
      this.$nextTick(() => {
        const modal = document.querySelector('.resizable-modal-wrap .ant-modal')
        const dragHandle = this.$refs.dragHandle
        
        if (modal && dragHandle) {
          // 添加拖拽功能
          this.addDragFunctionality(modal, dragHandle)
          // 添加缩放功能
          this.addResizeFunctionality(modal)
        }
      })
    },
    // 添加拖拽功能
    addDragFunctionality(modal, dragHandle) {
      let isDragging = false
      let startX = 0
      let startY = 0
      let initialLeft = 0
      let initialTop = 0

      dragHandle.style.cursor = 'move'
      
      dragHandle.addEventListener('mousedown', (e) => {
        isDragging = true
        startX = e.clientX
        startY = e.clientY
        
        const rect = modal.getBoundingClientRect()
        initialLeft = rect.left
        initialTop = rect.top
        
        document.addEventListener('mousemove', handleDrag)
        document.addEventListener('mouseup', handleDragEnd)
        e.preventDefault()
      })

      const handleDrag = (e) => {
        if (!isDragging) return
        
        const deltaX = e.clientX - startX
        const deltaY = e.clientY - startY
        
        const newLeft = initialLeft + deltaX
        const newTop = initialTop + deltaY
        
        // 限制在视窗内
        const maxLeft = window.innerWidth - modal.offsetWidth
        const maxTop = window.innerHeight - modal.offsetHeight
        
        modal.style.left = Math.max(0, Math.min(newLeft, maxLeft)) + 'px'
        modal.style.top = Math.max(0, Math.min(newTop, maxTop)) + 'px'
        modal.style.margin = '0'
        modal.style.transform = 'none'
      }

      const handleDragEnd = () => {
        isDragging = false
        document.removeEventListener('mousemove', handleDrag)
        document.removeEventListener('mouseup', handleDragEnd)
      }
    },
    // 添加缩放功能
    addResizeFunctionality(modal) {
      const resizeHandle = document.createElement('div')
      resizeHandle.className = 'resize-handle'
      resizeHandle.innerHTML = '↗'
      modal.appendChild(resizeHandle)

      let isResizing = false
      let startX = 0
      let startY = 0
      let startWidth = 0
      let startHeight = 0

      resizeHandle.addEventListener('mousedown', (e) => {
        isResizing = true
        startX = e.clientX
        startY = e.clientY
        startWidth = modal.offsetWidth
        startHeight = modal.offsetHeight

        document.addEventListener('mousemove', handleResize)
        document.addEventListener('mouseup', handleResizeEnd)
        e.preventDefault()
      })

      const handleResize = (e) => {
        if (!isResizing) return

        const deltaX = e.clientX - startX
        const deltaY = e.clientY - startY

        const newWidth = Math.max(800, startWidth + deltaX)
        const newHeight = Math.max(600, startHeight + deltaY)

        modal.style.width = newWidth + 'px'
        modal.style.height = newHeight + 'px'
      }

      const handleResizeEnd = () => {
        isResizing = false
        document.removeEventListener('mousemove', handleResize)
        document.removeEventListener('mouseup', handleResizeEnd)
      }
    },
    // 移除拖拽和缩放功能
    removeDragAndResize() {
      const modal = document.querySelector('.resizable-modal-wrap .ant-modal')
      if (modal) {
        const resizeHandle = modal.querySelector('.resize-handle')
        if (resizeHandle) {
          resizeHandle.remove()
        }
        modal.style.left = ''
        modal.style.top = ''
        modal.style.margin = ''
        modal.style.transform = ''
        modal.style.width = ''
        modal.style.height = ''
      }
    }
  },
  beforeDestroy() {
    this.destroyPlayer()
  }
}
</script>

<style scoped>
/* 视频预览弹窗样式 */
.video-preview-modal .ant-modal-content {
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border: none;
}

.video-preview-modal .ant-modal-body {
  padding: 0;
}

.video-preview-container {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #333;
  height: 800px;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
}

/* 顶部标题栏 */
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  user-select: none;
  cursor: move;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

/* 设备信息内联样式 */
.device-info-inline {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left: 20px;
  flex: 1;
}

.device-info-inline .info-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.device-info-inline .info-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.device-info-inline .info-value {
  font-size: 12px;
  color: #fff;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

/* 视频状态头部样式 */
.video-status-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.status-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.status-value {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.status-value.status-ready {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.2);
  border-color: rgba(82, 196, 26, 0.3);
}

.status-value.status-connecting,
.status-value.status-initializing {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.2);
  border-color: rgba(24, 144, 255, 0.3);
}

.status-value.status-stopped {
  color: #faad14;
  background: rgba(250, 173, 20, 0.2);
  border-color: rgba(250, 173, 20, 0.3);
}

.status-value.status-error {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.2);
  border-color: rgba(255, 77, 79, 0.3);
}

.status-value.status-disconnected {
  color: #8c8c8c;
  background: rgba(140, 140, 140, 0.2);
  border-color: rgba(140, 140, 140, 0.3);
}

.header-icon {
  font-size: 20px;
  color: #fff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.device-name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 16px;
}


/* 拖拽和缩放相关样式 */
.resizable-modal-wrap .ant-modal {
  position: fixed !important;
  margin: 0 !important;
  transform: none !important;
}

.resize-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  cursor: nw-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  border-radius: 0 0 12px 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.resize-handle:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  transform: scale(1.1);
}

/* 主要内容区域 */
.preview-content {
  display: flex;
  flex: 1;
  height: calc(100% - 80px);
}


/* 右侧视频播放区域 */
.video-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #000;
}

.video-container {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.imou-player-container {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 视频占位符 */
.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
}

.placeholder-content {
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}

.placeholder-icon {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 16px;
}

.placeholder-text {
  font-size: 18px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
}

.placeholder-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

/* 视频控制栏 */
.video-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.95) 0%, rgba(20, 20, 20, 0.9) 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.3);
  overflow-x: auto;
  min-height: 50px;
}

.video-controls .ant-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px;
  font-weight: 500;
  backdrop-filter: blur(5px);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  white-space: nowrap;
}

.video-controls .ant-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
}

.video-controls .ant-btn:hover::before {
  left: 100%;
}

.video-controls .ant-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: #1890ff;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.video-controls .ant-btn-primary {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);
}

.video-controls .ant-btn-primary:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  border-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.5);
}

.video-controls .ant-btn-danger {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border-color: #ff4d4f;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
}

.video-controls .ant-btn-danger:hover {
  background: linear-gradient(135deg, #ff7875 0%, #ffa39e 100%);
  border-color: #ff7875;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.5);
}

.controls-left {
  display: flex;
  gap: 4px;
  align-items: center;
  flex: 1;
  min-width: 0;
  justify-content: flex-start;
}

.controls-left .ant-divider-vertical {
  height: 20px;
  margin: 0 2px;
  border-color: rgba(255, 255, 255, 0.2);
}

.fullscreen-btn {
  margin-left: 0;
  flex-shrink: 0;
}

/* 码流和协议选择器样式 */
.stream-selector,
.protocol-selector {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.stream-selector .control-label,
.protocol-selector .control-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.stream-selector .stream-radio-group {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

.stream-selector .stream-option {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.8) !important;
  border-radius: 4px !important;
  padding: 3px 6px !important;
  font-size: 11px !important;
  height: auto !important;
  line-height: normal !important;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}

.stream-selector .stream-option:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: #1890ff !important;
}

.stream-selector .stream-option.ant-radio-button-wrapper-checked {
  background: #1890ff !important;
  border-color: #1890ff !important;
  color: #fff !important;
}

.protocol-selector .protocol-select {
  min-width: 70px;
  flex-shrink: 0;
}

.protocol-selector .protocol-select .ant-select-selector {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 4px !important;
  height: 26px !important;
}

.protocol-selector .protocol-select .ant-select-selection-item {
  color: rgba(255, 255, 255, 0.8) !important;
  line-height: 24px !important;
  font-size: 11px !important;
}

.protocol-selector .protocol-select .ant-select-arrow {
  color: rgba(255, 255, 255, 0.6) !important;
}

.protocol-selector .protocol-select:hover .ant-select-selector {
  border-color: #1890ff !important;
}


/* 桌面端优化 */
@media (max-width: 1600px) {
  .video-preview-modal .ant-modal {
    width: 95vw !important;
    max-width: 95vw !important;
  }
  
  .video-preview-container {
    height: 700px;
  }
}

@media (max-width: 1200px) {
  .video-preview-modal .ant-modal {
    width: 95vw !important;
    max-width: 95vw !important;
  }
  
  .video-preview-container {
    height: 600px;
  }
}
</style>
