<!-- eslint-disable -->
<template>
  <div style="width: 100vw; height: 100vh; background: #000; display: flex; flex-direction: column;">
    <!-- 顶部控制栏 -->
    <div style="padding: 12px 16px; background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); color: #fff; display: flex; flex-wrap: wrap; gap: 12px; align-items: center; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
      <div style="display: flex; align-items: center; gap: 8px;">
        <a-icon type="video-camera" style="color: #1890ff; font-size: 16px;" />
        <span style="font-weight: 500; color: #fff;">视频播放器</span>
      </div>
      
      <div style="display: flex; align-items: center; gap: 8px; margin-left: auto;">
        <a-input size="small" v-model="form.deviceId" placeholder="设备ID" style="width: 160px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);" />
        <a-input-number size="small" v-model="form.channelId" placeholder="通道" :min="0" style="width: 100px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);" />
        <a-input size="small" v-model="form.token" placeholder="Token" style="width: 200px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);" />
      </div>
      
      <div style="display: flex; align-items: center; gap: 8px;">
        <a-select size="small" v-model="form.type" style="width: 100px;">
          <a-select-option :value="1">
            <a-icon type="play-circle" style="margin-right: 4px;" />
            直播
          </a-select-option>
          <a-select-option :value="2">
            <a-icon type="history" style="margin-right: 4px;" />
            回放
          </a-select-option>
        </a-select>
        
        <a-select size="small" v-model="form.streamId" style="width: 100px;" :disabled="form.type !== 1">
          <a-select-option :value="0">
            <a-icon type="mobile" style="margin-right: 4px;" />
            标清
          </a-select-option>
          <a-select-option :value="1">
            <a-icon type="video-camera" style="margin-right: 4px;" />
            高清
          </a-select-option>
        </a-select>
        
        <a-select size="small" v-model="form.protocol" style="width: 120px;">
          <a-select-option value="hls">HLS</a-select-option>
          <a-select-option value="flv">FLV</a-select-option>
          <a-select-option value="rtmp">RTMP</a-select-option>
        </a-select>
        
        <a-select size="small" v-model="form.recordType" style="width: 120px;" :disabled="form.type !== 2">
          <a-select-option value="cloud">云存储</a-select-option>
          <a-select-option value="localRecord">本地存储</a-select-option>
        </a-select>
        
        <a-input size="small" v-model="form.code" placeholder="验证码" style="width: 120px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);" />
        
        <a-button size="small" type="primary" @click="handleInit" style="background: #1890ff; border-color: #1890ff;">
          <a-icon type="play-circle" style="margin-right: 4px;" />
          初始化
        </a-button>
      </div>
    </div>

    <div ref="container" :id="containerId" style="flex: 1; background: #000; display: flex; align-items: center; justify-content: center;">
      <video v-if="!isSdkMode" ref="video" style="width: 100%; height: 100%; background: #000;" controls autoplay playsinline></video>
    </div>

    <!-- 底部控制栏 -->
    <div style="padding: 12px 16px; background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); color: #fff; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; box-shadow: 0 -2px 8px rgba(0,0,0,0.3);">
      <!-- 基础播放控制 -->
      <div style="display: flex; align-items: center; gap: 6px; margin-right: 16px;">
        <a-button size="small" @click="handlePlay" style="background: #52c41a; border-color: #52c41a; color: #fff;">
          <a-icon type="play-circle" />
        </a-button>
        <a-button size="small" @click="handlePause" style="background: #faad14; border-color: #faad14; color: #fff;">
          <a-icon type="pause-circle" />
        </a-button>
        <a-button size="small" @click="handleStop" style="background: #ff4d4f; border-color: #ff4d4f; color: #fff;">
          <a-icon type="stop" />
        </a-button>
        <a-button size="small" @click="handleStart" style="background: #1890ff; border-color: #1890ff; color: #fff;">
          <a-icon type="caret-right" />
        </a-button>
      </div>
      
      <!-- 音频控制 -->
      <div style="display: flex; align-items: center; gap: 6px; margin-right: 16px;">
        <a-button size="small" @click="() => handleVolume(1)" style="background: #722ed1; border-color: #722ed1; color: #fff;">
          <a-icon type="sound" />
        </a-button>
        <a-button size="small" @click="() => handleVolume(0)" style="background: #eb2f96; border-color: #eb2f96; color: #fff;">
          <a-icon type="mute" />
        </a-button>
        <a-button size="small" @click="toggleMuted" style="background: #13c2c2; border-color: #13c2c2; color: #fff;">
          <a-icon type="audio" />
        </a-button>
      </div>
      
      <!-- 对讲控制 -->
      <div style="display: flex; align-items: center; gap: 6px; margin-right: 16px;">
        <a-button size="small" @click="handleStartTalk" style="background: #fa8c16; border-color: #fa8c16; color: #fff;">
          <a-icon type="phone" />
        </a-button>
        <a-button size="small" @click="handleStartTalkVideo" style="background: #a0d911; border-color: #a0d911; color: #fff;">
          <a-icon type="video-camera" />
        </a-button>
        <a-button size="small" @click="handleStopTalk" style="background: #f5222d; border-color: #f5222d; color: #fff;">
          <a-icon type="phone" />
        </a-button>
      </div>
      
      <!-- 录制控制 -->
      <div style="display: flex; align-items: center; gap: 6px; margin-right: 16px;">
        <a-button size="small" @click="handleStartRecord" style="background: #eb2f96; border-color: #eb2f96; color: #fff;">
          <a-icon type="video-camera-add" />
        </a-button>
        <a-button size="small" @click="handleStopRecord" style="background: #722ed1; border-color: #722ed1; color: #fff;">
          <a-icon type="stop" />
        </a-button>
        <a-button size="small" @click="handleCapture" style="background: #13c2c2; border-color: #13c2c2; color: #fff;">
          <a-icon type="camera" />
        </a-button>
      </div>
      
      <!-- 显示控制 -->
      <div style="display: flex; align-items: center; gap: 6px; margin-right: 16px;">
        <a-button size="small" @click="handleFullScreen" style="background: #52c41a; border-color: #52c41a; color: #fff;">
          <a-icon type="fullscreen" />
        </a-button>
        <a-button size="small" @click="handleExitFullScreen" style="background: #faad14; border-color: #faad14; color: #fff;">
          <a-icon type="fullscreen-exit" />
        </a-button>
        <a-button size="small" @click="zoomIn" style="background: #1890ff; border-color: #1890ff; color: #fff;">
          <a-icon type="zoom-in" />
        </a-button>
        <a-button size="small" @click="zoomOut" style="background: #fa8c16; border-color: #fa8c16; color: #fff;">
          <a-icon type="zoom-out" />
        </a-button>
        <a-button size="small" @click="resetZoom" style="background: #722ed1; border-color: #722ed1; color: #fff;">
          <a-icon type="reload" />
        </a-button>
      </div>
      
      <!-- 播放速度 -->
      <div style="display: flex; align-items: center; gap: 6px;">
        <span style="color: #ccc; font-size: 12px;">速度:</span>
        <a-select size="small" v-model="speed" style="width: 80px;" @change="handleSpeed">
          <a-select-option :value="0.5">0.5x</a-select-option>
          <a-select-option :value="1">1x</a-select-option>
          <a-select-option :value="2">2x</a-select-option>
          <a-select-option :value="4">4x</a-select-option>
          <a-select-option :value="8">8x</a-select-option>
          <a-select-option :value="16">16x</a-select-option>
          <a-select-option :value="32">32x</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 状态信息栏 -->
    <div style="padding: 8px 16px; background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%); color: #ccc; font-size: 12px; border-top: 1px solid #333;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; gap: 20px;">
          <div>
            <span style="color: #1890ff;">状态:</span> 
            <span :style="{ color: statusText === 'playing' ? '#52c41a' : statusText === 'error' ? '#ff4d4f' : '#faad14' }">{{ statusText || '未初始化' }}</span>
          </div>
          <div v-if="form.protocol">
            <span style="color: #1890ff;">协议:</span> 
            <span style="color: #722ed1;">{{ form.protocol.toUpperCase() }}</span>
          </div>
          <div v-if="form.streamId !== undefined">
            <span style="color: #1890ff;">码流:</span> 
            <span style="color: #13c2c2;">{{ form.streamId === 0 ? '高清' : '标清' }}</span>
          </div>
        </div>
        <div style="display: flex; gap: 20px;">
          <div v-if="lastError" style="color: #ff4d4f;">
            <a-icon type="exclamation-circle" style="margin-right: 4px;" />
            错误: {{ lastError }}
          </div>
          <div v-if="lastEvent" style="color: #52c41a;">
            <a-icon type="info-circle" style="margin-right: 4px;" />
            事件: {{ lastEvent }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { loadExternalScript } from '@/utils/loadScript'

export default {
  name: 'ImouPlayer',
  data() {
    return {
      player: null,
      containerId: 'imou-root',
      isSdkMode: false,
      speed: 1,
      lastError: '',
      lastEvent: '',
      statusText: '',
      form: {
        deviceId: '',
        channelId: 0,
        token: '',
        type: 1,
        streamId: 0,
        recordType: 'cloud',
        protocol: 'hls',
        code: ''
      }
    }
  },
  async mounted() {
    const query = this.$route.query || {}
    const directUrl = query.url || ''

    // Prefill from query
    this.form.deviceId = query.deviceId || this.form.deviceId
    this.form.channelId = query.channelId !== undefined ? Number(query.channelId) : this.form.channelId
    this.form.token = query.token || this.form.token
    this.form.type = query.type !== undefined ? Number(query.type) : this.form.type
    this.form.streamId = query.streamId !== undefined ? Number(query.streamId) : this.form.streamId
    this.form.recordType = query.recordType || this.form.recordType
    this.form.protocol = query.protocol || this.form.protocol
    this.form.code = query.code || this.form.code

    this.tryLoadCss('/imou/imou-player.css')

    let sdkReady = false
    try {
      if (!window.imouPlayer) {
        await loadExternalScript('/imou/imou-player.js')
      }
      sdkReady = !!window.imouPlayer
    } catch (e) {
      sdkReady = !!window.imouPlayer
    }

    if (sdkReady && this.form.deviceId && this.form.token) {
      await this.initSdkPlayer()
      return
    }

    if (directUrl) {
      const video = this.$refs.video
      this.isSdkMode = false
      video.src = directUrl
      video.play().catch(() => {})
      return
    }
  },
  beforeDestroy() {
    try { if (this.player && this.player.destroy) this.player.destroy() } catch (e) {}
    this.player = null
  },
  methods: {
    async handleInit() {
      await this.initSdkPlayer()
    },
    async initSdkPlayer() {
      try {
        if (!window.imouPlayer) {
          await loadExternalScript('/imou/imou-player.js')
        }
        if (this.player && this.player.destroy) {
          this.player.destroy()
          this.player = null
        }
        const opts = {
          id: this.containerId,
          width: this.$refs.container.clientWidth || 1200,
          height: this.$refs.container.clientHeight || 700,
          deviceId: String(this.form.deviceId),
          channelId: Number(this.form.channelId) || 0,
          token: String(this.form.token),
          type: Number(this.form.type) || 1,
          streamId: Number(this.form.streamId) || 0,
          recordType: String(this.form.recordType) || 'cloud',
          WasmLibPath: '/WasmLib',
          code: this.form.code || undefined,
          muted: false,
          handleError: (err) => {
            this.lastError = typeof err === 'string' ? err : JSON.stringify(err)
            console.error('handleError', err)
          },
          handleCallBack: (ev) => {
            this.lastEvent = typeof ev === 'string' ? ev : JSON.stringify(ev)
            // 可根据事件更新状态
          },
          controlsConfig: {
            play: true,
            fullScreen: true,
            capture: true,
            voice: true
          }
        }
        // eslint-disable-next-line no-new
        this.player = new window.imouPlayer(opts)
        this.isSdkMode = true
        this.statusText = 'initialized'
      } catch (e) {
        this.isSdkMode = false
        this.statusText = 'fallback video'
      }
    },
    handlePlay() { try { this.player?.play?.(); this.statusText = 'playing' } catch (e) {} },
    handlePause() { try { this.player?.pause?.(); this.statusText = 'paused' } catch (e) {} },
    handleStop() { try { this.player?.stop?.(); this.statusText = 'stopped' } catch (e) {} },
    handleStart() { try { this.player?.start?.(); this.statusText = 'playing' } catch (e) {} },
    handleCapture() { try { this.player?.capture?.() } catch (e) {} },
    handleStartTalk() { try { this.player?.startTalk?.() } catch (e) {} },
    handleStartTalkVideo() { try { this.player?.startTalk?.('video') } catch (e) {} },
    handleStopTalk() { try { this.player?.stopTalk?.() } catch (e) {} },
    handleVolume(v) { try { this.player?.volume?.(v) } catch (e) {} },
    toggleMuted() { try { this.player?.volume?.(0); setTimeout(()=>this.player?.volume?.(1), 300) } catch (e) {} },
    handleFullScreen() { try { this.player?.fullScreen?.() } catch (e) {} },
    handleExitFullScreen() { try { this.player?.exitFullScreen?.() } catch (e) {} },
    handleStartRecord() { try { this.player?.startRecord?.() } catch (e) {} },
    handleStopRecord() { try { this.player?.stopRecord?.() } catch (e) {} },
    handleSpeed(val) { try { this.player?.setSpeed?.(Number(val)); this.speed = Number(val) } catch (e) {} },
    zoomIn() { try { this.player?.zoomIn?.() } catch (e) {} },
    zoomOut() { try { this.player?.zoomOut?.() } catch (e) {} },
    resetZoom() { try { this.player?.resetZoom?.() } catch (e) {} },
    answerVideoTalk() { try { this.player?.answerVideoTalk?.() } catch (e) {} },
    tryLoadCss(href) {
      try {
        const existed = Array.from(document.getElementsByTagName('link')).some(l => l.href.includes(href))
        if (existed) return
        const link = document.createElement('link')
        link.rel = 'stylesheet'
        link.href = href
        document.head.appendChild(link)
      } catch (e) {}
    }
  }
}
</script>
