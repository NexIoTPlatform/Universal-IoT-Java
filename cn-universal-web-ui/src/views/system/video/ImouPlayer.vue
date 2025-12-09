<!-- eslint-disable -->
<template>
  <a-modal
    v-model="visible"
    v-modal-drag
    :title="null"
    :closable="false"
    width="900px"
    :footer="null"
    :destroyOnClose="true"
    :bodyStyle="{ padding: 0 }"
    wrapClassName="imou-player-modal"
  >
  <div style="width: 100%; background: #000; display: flex; flex-direction: column;">
    <!-- 顶部信息栏 -->
    <div class="player-header">
      <div class="header-left">
        <a-icon type="video-camera" style="font-size: 18px; margin-right: 8px;" />
        <span class="header-title">乐橙摄像机 - {{ deviceId || '实时预览' }}</span>
      </div>
      <div class="header-right">
        <a-icon type="close" class="close-btn" @click="handleClose" />
      </div>
    </div>

    <!-- 视频播放区 -->
    <div class="video-main">
      <div ref="container" :id="containerId" style="width: 100%; height: 100%; background: #000; display: flex; align-items: center; justify-content: center;">
        <video v-if="!isSdkMode" ref="video" style="width: 100%; height: 100%; background: #000;" controls autoplay playsinline></video>
      </div>
    </div>

    <!-- 视频控制栏 -->
    <div class="video-controls">
      <div class="controls-left">
        <!-- 码流类型选择 -->
        <span class="control-label">流:</span>
        <a-radio-group v-model="currentStreamId" @change="onStreamTypeChange" size="small" class="stream-radio-group" button-style="solid">
          <a-radio-button :value="0" class="stream-option">
            <a-icon type="desktop"/>
            主
          </a-radio-button>
          <a-radio-button :value="1" class="stream-option">
            <a-icon type="mobile"/>
            辅
          </a-radio-button>
        </a-radio-group>

        <span class="divider-text">|</span>

        <!-- 传输协议选择 -->
        <span class="control-label">协:</span>
        <a-select v-model="currentProtocol" @change="onProtocolChange" size="small" class="protocol-select">
          <a-select-option value="hls">HLS</a-select-option>
          <a-select-option value="flv">FLV</a-select-option>
          <a-select-option value="rtmp">RTMP</a-select-option>
        </a-select>

        <span class="divider-text">|</span>

        <!-- 静音 -->
        <a-tooltip :title="isMuted ? '取消静音' : '静音'" placement="top">
          <a-button
            size="small"
            :type="isMuted ? 'primary' : 'default'"
            :class="['control-btn', { active: isMuted }]"
            @click="toggleMuted"
          >
            <a-icon type="sound"/>
            静音
          </a-button>
        </a-tooltip>

        <span class="divider-text">|</span>

        <!-- 截图 -->
        <a-tooltip title="截图" placement="top">
          <a-button size="small" @click="handleCapture" class="control-btn">
            <a-icon type="camera"/>
            截图
          </a-button>
        </a-tooltip>

        <span class="divider-text">|</span>

        <!-- 录制 -->
        <a-tooltip :title="isRecording ? '录制中' : '录制'" placement="top">
          <a-button
            size="small"
            :type="isRecording ? 'primary' : 'default'"
            :class="['control-btn', { active: isRecording }]"
            @click="toggleRecord"
          >
            <a-icon type="video-camera"/>
            {{ isRecording ? '录中' : '录制' }}
          </a-button>
        </a-tooltip>

        <span class="divider-text">|</span>

        <!-- 对讲 -->
        <a-tooltip :title="isTalking ? '对讲中' : '对讲'" placement="top">
          <a-button
            size="small"
            :type="isTalking ? 'primary' : 'default'"
            :class="['control-btn', { active: isTalking }]"
            @click="toggleTalk"
          >
            <a-icon type="phone"/>
            {{ isTalking ? '说' : '对讲' }}
          </a-button>
        </a-tooltip>

        <span class="divider-text">|</span>

        <!-- 缩放控制 -->
        <a-tooltip title="放大" placement="top">
          <a-button size="small" @click="zoomIn" class="control-btn">
            <a-icon type="zoom-in"/>
            大
          </a-button>
        </a-tooltip>
        <a-tooltip title="缩小" placement="top">
          <a-button size="small" @click="zoomOut" class="control-btn">
            <a-icon type="zoom-out"/>
            小
          </a-button>
        </a-tooltip>

        <span class="divider-text">|</span>

        <!-- 全屏按钮 -->
        <a-tooltip title="全屏" placement="top">
          <a-button size="small" @click="handleFullScreen" class="control-btn">
            <a-icon type="fullscreen"/>
            全屏
          </a-button>
        </a-tooltip>
      </div>
    </div>
  </div>
  </a-modal>
</template>

<script>
import {loadExternalScript} from '@/utils/loadScript'
import modalDrag from '@/directive/modal-drag'

export default {
  name: 'ImouPlayer',
  directives: { modalDrag },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    deviceId: {
      type: String,
      default: ''
    },
    channelId: {
      type: Number,
      default: 0
    },
    token: {
      type: String,
      default: ''
    },
    deviceModel: {
      type: String,
      default: ''
    },
    playType: {
      type: Number,
      default: 1
    },
    streamId: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      player: null,
      containerId: 'imou-modal-player',
      isSdkMode: false,
      currentStreamId: 0,
      currentProtocol: 'hls',
      isRecording: false,
      isTalking: false,
      isMuted: false,
      prevVolume: 1
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
    }
  },
  watch: {
    value(val) {
      if (val && this.deviceId && this.token) {
        this.$nextTick(() => {
          this.initSdkPlayer()
        })
      } else if (!val) {
        this.destroyPlayer()
      }
    },
    deviceId(val) {
      if (this.value && val && this.token) {
        this.$nextTick(() => {
          this.initSdkPlayer()
        })
      }
    },
    token(val) {
      if (this.value && this.deviceId && val) {
        this.$nextTick(() => {
          this.initSdkPlayer()
        })
      }
    }
  },
  mounted() {
    this.tryLoadCss('/imou/imou-player.css')
    // 初始化currentStreamId
    this.currentStreamId = this.streamId || 0
  },
  beforeDestroy() {
    this.destroyPlayer()
  },
  methods: {
    async initSdkPlayer() {
      try {
        if (!window.imouPlayer) {
          await loadExternalScript('/imou/imou-player.js')
        }
        
        await this.$nextTick()
        
        if (this.player && this.player.destroy) {
          this.player.destroy()
          this.player = null
        }
        
        if (!this.$refs.container) {
          console.error('Player container not found')
          return
        }
        
        const opts = {
          id: this.containerId,
          width: this.$refs.container.clientWidth || 900,
          height: this.$refs.container.clientHeight || 480,
          deviceId: String(this.deviceId),
          channelId: Number(this.channelId) || 0,
          token: String(this.token),
          type: Number(this.playType) || 1,
          streamId: Number(this.currentStreamId) || 0,
          recordType: 'cloud',
          WasmLibPath: '/imou/',
          muted: false,
          handleError: (err) => {
            const errMsg = typeof err === 'string' ? err : JSON.stringify(err)
            console.error('Imou player error:', errMsg)
          },
          handleCallBack: (ev) => {
            console.log('Imou player event:', ev)
          }
        }
        
        console.log('Initializing imouPlayer with opts:', opts)
        this.player = new window.imouPlayer(opts)
        this.isSdkMode = true
        // this.$message.success('播放器初始化成功')
      } catch (e) {
        console.error('Init imou player failed:', e)
        this.$message.error('播放器初始化失败：' + e.message)
        this.isSdkMode = false
      }
    },
    destroyPlayer() {
      try {
        if (this.player && this.player.destroy) {
          this.player.destroy()
        }
      } catch (e) {
        console.error('Destroy player error:', e)
      }
      this.player = null
    },
    handleClose() {
      this.visible = false
    },
    onStreamTypeChange(e) {
      const newStreamId = e.target.value
      console.log('码流切换:', newStreamId)
      this.currentStreamId = newStreamId
      // 重新初始化播放器
      this.$nextTick(() => {
        this.initSdkPlayer()
      })
    },
    onProtocolChange(value) {
      console.log('协议切换:', value)
      this.currentProtocol = value
      // 重新初始化播放器
      this.$nextTick(() => {
        this.initSdkPlayer()
      })
    },
    handlePlay() {
      try {
        this.player?.play?.()
      } catch (e) {
        console.error('Play error:', e)
      }
    },
    handlePause() {
      try {
        this.player?.pause?.()
      } catch (e) {
        console.error('Pause error:', e)
      }
    },
    handleStop() {
      try {
        this.player?.stop?.()
      } catch (e) {
        console.error('Stop error:', e)
      }
    },
    handleStart() {
      try {
        this.player?.start?.()
      } catch (e) {
        console.error('Start error:', e)
      }
    },
    handleCapture() {
      try {
        // 优先使用 SDK 的截图能力
        if (this.player && typeof this.player.capture === 'function') {
          const res = this.player.capture()
          // 部分 SDK 同步触发保存或返回数据；统一给出轻量反馈
          this.$message.success('已触发截图')
          // 如果返回了可保存的数据，尽可能帮用户下载
          if (res) {
            if (res instanceof Blob) {
              this.saveBlob(res, `snapshot_${new Date().toISOString().replace(/[:.]/g, '-')}.png`)
            } else if (typeof res === 'string' && res.startsWith('data:image')) {
              this.saveDataUrl(res, `snapshot_${new Date().toISOString().replace(/[:.]/g, '-')}.png`)
            } else if (typeof res?.then === 'function') {
              // 兼容返回 Promise 的情况
              res
                .then((data) => {
                  if (data instanceof Blob) {
                    this.saveBlob(data, `snapshot_${new Date().toISOString().replace(/[:.]/g, '-')}.png`)
                  } else if (typeof data === 'string' && data.startsWith('data:image')) {
                    this.saveDataUrl(data, `snapshot_${new Date().toISOString().replace(/[:.]/g, '-')}.png`)
                  }
                })
                .catch((err) => {
                  console.error('Capture promise error:', err)
                  this.$message.error('截图失败：' + (err?.message || err))
                })
            }
          }
          return
        }

        // 非 SDK 模式，从 <video> 元素回退截图
        const video = this.$refs.video
        if (video) {
          const w = video.videoWidth || video.clientWidth
          const h = video.videoHeight || video.clientHeight
          if (!w || !h) {
            this.$message.warning('视频未就绪，稍后再试')
            return
          }
          const canvas = document.createElement('canvas')
          canvas.width = w
          canvas.height = h
          const ctx = canvas.getContext('2d')
          ctx.drawImage(video, 0, 0, w, h)
          const url = canvas.toDataURL('image/png')
          this.saveDataUrl(url, `snapshot_${new Date().toISOString().replace(/[:.]/g, '-')}.png`)
          this.$message.success('截图已保存')
          return
        }

        this.$message.warning('当前模式不支持截图')
      } catch (e) {
        console.error('Capture error:', e)
        this.$message.error('截图失败：' + (e?.message || e))
      }
    },
    handleStartTalk() {
      try {
        this.player?.startTalk?.()
      } catch (e) {
        console.error('Start talk error:', e)
      }
    },
    handleStartTalkVideo() {
      try {
        this.player?.startTalk?.('video')
      } catch (e) {
        console.error('Start talk video error:', e)
      }
    },
    handleStopTalk() {
      try {
        this.player?.stopTalk?.()
      } catch (e) {
        console.error('Stop talk error:', e)
      }
    },
    handleVolume(v) {
      try {
        this.player?.volume?.(v)
      } catch (e) {
        console.error('Volume error:', e)
      }
    },
    toggleMuted() {
      try {
        // 非 SDK 模式，使用原生 video.muted 回退
        const video = this.$refs.video
        if (!this.isSdkMode && video) {
          video.muted = !this.isMuted
        }

        // SDK 模式使用音量控制模拟静音
        if (this.player && typeof this.player.volume === 'function') {
          if (this.isMuted) {
            // 取消静音，恢复到之前音量（默认1）
            this.player.volume(this.prevVolume || 1)
          } else {
            // 记住当前目标音量（暂无getter，按1作为恢复值）后静音
            this.prevVolume = 1
            this.player.volume(0)
          }
        }

        this.isMuted = !this.isMuted
        this.$message[this.isMuted ? 'success' : 'info'](this.isMuted ? '已静音' : '已取消静音')
      } catch (e) {
        console.error('Toggle mute error:', e)
        this.$message.error('静音切换失败：' + (e?.message || e))
      }
    },
    handleFullScreen() {
      try {
        this.player?.fullScreen?.()
      } catch (e) {
        console.error('Fullscreen error:', e)
      }
    },
    handleExitFullScreen() {
      try {
        this.player?.exitFullScreen?.()
      } catch (e) {
        console.error('Exit fullscreen error:', e)
      }
    },
    handleStartRecord() {
      try {
        this.player?.startRecord?.()
      } catch (e) {
        console.error('Start record error:', e)
      }
    },
    handleStopRecord() {
      try {
        this.player?.stopRecord?.()
      } catch (e) {
        console.error('Stop record error:', e)
      }
    },
    handleSpeed(val) {
      try {
        this.player?.setSpeed?.(Number(val))
        this.speed = Number(val)
      } catch (e) {
        console.error('Set speed error:', e)
      }
    },
    zoomIn() {
      try {
        this.player?.zoomIn?.()
      } catch (e) {
        console.error('Zoom in error:', e)
      }
    },
    zoomOut() {
      try {
        this.player?.zoomOut?.()
      } catch (e) {
        console.error('Zoom out error:', e)
      }
    },
    resetZoom() {
      try {
        this.player?.resetZoom?.()
      } catch (e) {
        console.error('Reset zoom error:', e)
      }
    },
    answerVideoTalk() {
      try {
        this.player?.answerVideoTalk?.()
      } catch (e) {
        console.error('Answer video talk error:', e)
      }
    },
    saveDataUrl(dataUrl, filename) {
      try {
        const a = document.createElement('a')
        a.href = dataUrl
        a.download = filename
        a.click()
      } catch (e) {
        console.error('Save data url error:', e)
      }
    },
    saveBlob(blob, filename) {
      try {
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        a.click()
        URL.revokeObjectURL(url)
      } catch (e) {
        console.error('Save blob error:', e)
      }
    },
    toggleRecord() {
      try {
        if (this.isRecording) {
          this.handleStopRecord()
          this.isRecording = false
          this.$message.info('已停止录制')
        } else {
          this.handleStartRecord()
          this.isRecording = true
          this.$message.success('开始录制')
        }
      } catch (e) {
        console.error('Toggle record error:', e)
      }
    },
    toggleTalk() {
      try {
        if (this.isTalking) {
          this.handleStopTalk()
          this.isTalking = false
          this.$message.info('已停止对讲')
        } else {
          this.handleStartTalk()
          this.isTalking = true
          this.$message.success('开始对讲')
        }
      } catch (e) {
        console.error('Toggle talk error:', e)
      }
    },
    tryLoadCss(href) {
      try {
        const existed = Array.from(document.getElementsByTagName('link')).some(l => l.href.includes(href))
        if (existed) return
        const link = document.createElement('link')
        link.rel = 'stylesheet'
        link.href = href
        document.head.appendChild(link)
      } catch (e) {
        console.error('Load CSS error:', e)
      }
    }
  }
}
</script>

<style scoped>
/* 顶部信息栏 */
.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.header-title {
  font-size: 16px;
}

.close-btn {
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.3s;
}

.close-btn:hover {
  transform: rotate(90deg);
}

/* 视频主区域 */
.video-main {
  width: 100%;
  height: 480px;
  background: #000;
  position: relative;
}

/* 视频控制栏 */
.video-controls {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  background: #000;
  border-top: 1px solid #333;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
}

.controls-left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: nowrap;
  width: 100%;
}

/* 分隔符 */
.divider-text {
  color: #666;
  font-size: 12px;
  margin: 0 1px;
  flex-shrink: 0;
}

/* 码流选择器 */
.stream-radio-group {
  display: flex;
  flex-shrink: 0;
}

.stream-option {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 0 6px !important;
  height: 22px !important;
  line-height: 20px !important;
  font-size: 12px !important;
}

/* 协议选择器 */
.protocol-select {
  min-width: 60px;
  flex-shrink: 0;
}

/* 控制按钮 */
.control-btn {
  height: 22px;
  padding: 0 4px;
  font-size: 12px;
  line-height: 20px;
  border-radius: 2px;
  background: #fff;
  border: 1px solid #d9d9d9;
  color: rgba(0, 0, 0, 0.65);
  flex-shrink: 0;
  white-space: nowrap;
}

.control-btn:hover {
  color: #40a9ff;
  border-color: #40a9ff;
}

.control-btn.active,
.control-btn.active:hover {
  color: #1890ff;
  border-color: #1890ff;
}

.control-btn .anticon {
  font-size: 12px;
}

/* 控制标签 */
.control-label {
  color: #ccc;
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0;
}
</style>
