<template>
  <a-modal
    v-model="visible"
    v-modal-drag
    :title="null"
    :closable="false"
    width="1000px"
    :footer="null"
    :destroyOnClose="true"
    :bodyStyle="{ padding: 0 }"
    wrapClassName="live-player-modal-immersive"
  >
    <!-- 顶部信息栏 -->
    <div class="player-header">
      <div class="header-left">
        <a-icon type="video-camera" style="font-size: 18px; margin-right: 8px;" />
        <span class="header-title">{{ title }}</span>
      </div>
      <div class="header-right">
        <a-icon type="close" class="close-btn" @click="handleClose" />
      </div>
    </div>
    
    <!-- 视频播放区（全屏） -->
    <div class="video-main">
      <!-- Jessibuca播放器 -->
      <jessibuca
        v-if="activePlayer === 'jessibuca' && videoUrl"
        ref="jessibucaPlayer"
        :videoUrl="videoUrl"
        :hasAudio="hasAudio"
        style="width: 100%; height: 100%;"
      />
      
      <!-- H265web播放器 -->
      <h265-player
        v-else-if="activePlayer === 'h265web' && videoUrl"
        ref="h265Player"
        :videoUrl="videoUrl"
        :hasAudio="hasAudio"
        style="width: 100%; height: 100%;"
      />
      
      <!-- 加载中状态 -->
      <div 
        v-else-if="!videoUrl && streamInfo"
        style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; background: #000;"
      >
        <a-spin size="large" />
        <span style="margin-left: 12px; font-size: 16px;">正在加载视频流...</span>
      </div>
      
      <!-- 未获取到视频流 -->
      <a-empty 
        v-else
        description="正在加载视频流" 
        style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; background: #000;"
      />
      
      <!-- PTZ控制浮层（可拖拽） -->
      <transition name="slide-fade">
        <div v-if="showPtz && ptzPanelVisible" v-draggable class="ptz-overlay">
          <div class="ptz-overlay-header drag-handle">
            <div class="ptz-title">
              <span><a-icon type="control" /> 云台控制</span>
              <span v-if="ptzHint" class="ptz-hint">{{ ptzHint }}</span>
            </div>
            <a-icon type="close" @click="closePtzPanel" />
          </div>
          
          <!-- 方向控制 -->
          <div class="ptz-direction">
            <div class="direction-row">
              <a-button class="direction-btn" @mousedown="handlePtz('up')" @mouseup="handlePtz('stop')">
                <a-icon type="arrow-up" />
              </a-button>
            </div>
            <div class="direction-row">
              <a-button class="direction-btn" @mousedown="handlePtz('left')" @mouseup="handlePtz('stop')">
                <a-icon type="arrow-left" />
              </a-button>
              <a-button class="direction-btn center" disabled>
                <a-icon type="border" />
              </a-button>
              <a-button class="direction-btn" @mousedown="handlePtz('right')" @mouseup="handlePtz('stop')">
                <a-icon type="arrow-right" />
              </a-button>
            </div>
            <div class="direction-row">
              <a-button class="direction-btn" @mousedown="handlePtz('down')" @mouseup="handlePtz('stop')">
                <a-icon type="arrow-down" />
              </a-button>
            </div>
          </div>
          
          <!-- 速度控制 -->
          <div class="ptz-speed">
            <div class="speed-label">速度：{{ ptzSpeed }}</div>
            <a-slider v-model="ptzSpeed" :min="1" :max="100" />
          </div>
          
          <!-- 变焦/聚焦/光圈 -->
          <div class="ptz-controls">
            <div class="control-row">
              <span class="control-label">变焦</span>
              <div class="control-buttons">
                <a-button size="small" @mousedown="handlePtz('zoomIn')" @mouseup="handlePtz('stop')">+</a-button>
                <a-button size="small" @mousedown="handlePtz('zoomOut')" @mouseup="handlePtz('stop')">-</a-button>
              </div>
            </div>
            
            <div class="control-row">
              <span class="control-label">聚焦</span>
              <div class="control-buttons">
                <a-button size="small" @mousedown="handlePtz('focusIn')" @mouseup="handlePtz('stop')">+</a-button>
                <a-button size="small" @mousedown="handlePtz('focusOut')" @mouseup="handlePtz('stop')">-</a-button>
              </div>
            </div>
            
            <div class="control-row">
              <span class="control-label">光圈</span>
              <div class="control-buttons">
                <a-button size="small" @mousedown="handlePtz('irisIn')" @mouseup="handlePtz('stop')">+</a-button>
                <a-button size="small" @mousedown="handlePtz('irisOut')" @mouseup="handlePtz('stop')">-</a-button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
    
    <!-- 底部工具栏 -->
    <div class="player-footer">
      <div class="footer-left">
        <!-- 播放器切换 -->
        <a-radio-group v-model="activePlayer" size="small" @change="handlePlayerChange">
          <a-radio-button value="jessibuca">
            <a-icon type="play-circle" /> Jessibuca
          </a-radio-button>
          <a-radio-button value="h265web" disabled>
            <a-icon type="video-camera" /> H265web
          </a-radio-button>
        </a-radio-group>
        
        <!-- 更多地址（仅复制） -->
        <a-dropdown v-if="streamInfo" :trigger="['click']" placement="bottomLeft">
          <a-button size="small">
            <a-icon type="link" /> 更多地址
          </a-button>
          <a-menu slot="overlay" style="max-height: 400px; overflow-y: auto; min-width: 200px;">
            <a-menu-item v-if="streamInfo.flv" @click="copyUrl(streamInfo.flv)">
              <a-tag color="orange">FLV</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.https_flv" @click="copyUrl(streamInfo.https_flv)">
              <a-tag color="orange">FLV(HTTPS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.ws_flv" @click="copyUrl(streamInfo.ws_flv)">
              <a-tag color="blue">FLV(WS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.wss_flv" @click="copyUrl(streamInfo.wss_flv)">
              <a-tag color="green">FLV(WSS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.fmp4" @click="copyUrl(streamInfo.fmp4)">
              <a-tag color="purple">FMP4</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.https_fmp4" @click="copyUrl(streamInfo.https_fmp4)">
              <a-tag color="purple">FMP4(HTTPS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.ws_fmp4" @click="copyUrl(streamInfo.ws_fmp4)">
              <a-tag color="purple">FMP4(WS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.wss_fmp4" @click="copyUrl(streamInfo.wss_fmp4)">
              <a-tag color="purple">FMP4(WSS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.hls" @click="copyUrl(streamInfo.hls)">
              <a-tag color="cyan">HLS</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.https_hls" @click="copyUrl(streamInfo.https_hls)">
              <a-tag color="cyan">HLS(HTTPS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.ws_hls" @click="copyUrl(streamInfo.ws_hls)">
              <a-tag color="cyan">HLS(WS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.wss_hls" @click="copyUrl(streamInfo.wss_hls)">
              <a-tag color="cyan">HLS(WSS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.ts" @click="copyUrl(streamInfo.ts)">
              <a-tag color="geekblue">TS</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.https_ts" @click="copyUrl(streamInfo.https_ts)">
              <a-tag color="geekblue">TS(HTTPS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.ws_ts" @click="copyUrl(streamInfo.ws_ts)">
              <a-tag color="geekblue">TS(WS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.wss_ts" @click="copyUrl(streamInfo.wss_ts)">
              <a-tag color="geekblue">TS(WSS)</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.rtc" @click="copyUrl(streamInfo.rtc)">
              <a-tag color="volcano">RTC</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.rtcs" @click="copyUrl(streamInfo.rtcs)">
              <a-tag color="volcano">RTCS</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.rtmp" @click="copyUrl(streamInfo.rtmp)">
              <a-tag color="red">RTMP</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.rtmps" @click="copyUrl(streamInfo.rtmps)">
              <a-tag color="red">RTMPS</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.rtsp" @click="copyUrl(streamInfo.rtsp)">
              <a-tag color="magenta">RTSP</a-tag>
            </a-menu-item>
            <a-menu-item v-if="streamInfo.rtsps" @click="copyUrl(streamInfo.rtsps)">
              <a-tag color="magenta">RTSPS</a-tag>
            </a-menu-item>
          </a-menu>
        </a-dropdown>
      </div>
      
      <div class="footer-center">
        <!-- PTZ控制开关 -->
        <a-button v-if="showPtz" size="small" :type="ptzPanelVisible ? 'primary' : 'default'" @click="togglePtzPanel">
          <a-icon type="control" /> {{ ptzPanelVisible ? '隐藏PTZ' : '显示PTZ' }}
        </a-button>
        
        <!-- 语音对讲（暂时隐藏） -->
        <!-- <a-button v-if="showBroadcast" size="small" :type="getBroadcastBtnType()" :disabled="broadcastStatus === -2" :loading="broadcastStatus === 0" @click="toggleBroadcast">
          <a-icon type="sound" /> {{ getBroadcastBtnText() }}
        </a-button> -->
        
        <!-- 复制地址 -->
        <a-button size="small" icon="copy" @click="copyCurrentUrl">复制地址</a-button>
      </div>
      
      <div class="footer-right">
        <span v-if="streamInfo && streamInfo.app" class="footer-info">应用: <strong>{{ streamInfo.app }}</strong></span>
        <span v-if="streamInfo && streamInfo.stream" class="footer-info">流ID: <strong>{{ formatStreamId(streamInfo.stream) }}</strong></span>
      </div>
    </div>
  </a-modal>
</template>

<script>
import Jessibuca from '@/components/Jessibuca'
import H265Player from '@/components/H265Player'
import modalDrag from '@/directive/modal-drag'
import draggable from '@/directive/draggable'

export default {
  name: 'LivePlayerModal',
  components: { Jessibuca, H265Player },
  directives: { modalDrag, draggable },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '视频预览'
    },
    streamInfo: {
      type: Object,
      default: null
    },
    hasAudio: {
      type: Boolean,
      default: true
    },
    showPtz: {
      type: Boolean,
      default: false
    },
    showBroadcast: {
      type: Boolean,
      default: false
    },
    deviceId: {
      type: String,
      default: ''
    },
    channelId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      activePlayer: 'jessibuca',
      ptzSpeed: 50,
      videoUrl: '',
      currentStreamType: 'ws_flv',
      ptzPanelVisible: false,
      ptzHint: '',
      ptzHintTimer: null,
      
      // 语音对讲状态
      broadcastMode: true,
      broadcastStatus: -1,
      broadcastRtc: null
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
    streamInfo: {
      handler(val) {
        if (val) {
          this.updateVideoUrl()
        }
      },
      immediate: true
    },
    visible: {
      handler(val) {
        if (val && this.videoUrl) {
          // 弹窗打开时，如果已有视频地址，延迟播放（等待DOM渲染完成）
          this.$nextTick(() => {
            setTimeout(() => {
              this.playCurrentUrl()
            }, 1200)
          })
        }
      },
      immediate: false
    },
    videoUrl: {
      handler(val) {
        if (val && this.visible) {
          // 视频地址更新且弹窗可见时，自动播放
          this.$nextTick(() => {
            setTimeout(() => {
              this.playCurrentUrl()
            }, 300)
          })
        }
      },
      immediate: false
    }
  },
  methods: {
    updateVideoUrl() {
      if (!this.streamInfo) return
      
      if (location.protocol === 'https:') {
        this.videoUrl = this.streamInfo.wss_flv || this.streamInfo.ws_flv || this.streamInfo.flv
        this.currentStreamType = this.streamInfo.wss_flv ? 'wss_flv' : (this.streamInfo.ws_flv ? 'ws_flv' : 'flv')
      } else {
        this.videoUrl = this.streamInfo.ws_flv || this.streamInfo.flv
        this.currentStreamType = this.streamInfo.ws_flv ? 'ws_flv' : 'flv'
      }
    },

    togglePtzPanel() {
      this.ptzPanelVisible = !this.ptzPanelVisible
      if (this.ptzPanelVisible) {
        this.$message.success('已开启云台控制')
      } else {
        this.$message.info('已关闭云台控制')
      }
    },

    closePtzPanel() {
      if (this.ptzPanelVisible) {
        this.ptzPanelVisible = false
        this.$message.info('已关闭云台控制')
      }
    },

    handlePlayerChange() {
      this.$nextTick(() => {
        this.playCurrentUrl()
      })
    },
    
    handleStreamSwitch({ key }) {
      this.videoUrl = this.streamInfo[key]
      this.currentStreamType = key
      this.$message.success('已切换流格式: ' + key.toUpperCase())
      
      this.$nextTick(() => {
        this.playCurrentUrl()
      })
    },
    
    getStreamTypeLabel() {
      const labels = {
        ws_flv: 'WS_FLV',
        wss_flv: 'WSS_FLV',
        flv: 'FLV',
        https_flv: 'HTTPS_FLV',
        hls: 'HLS',
        rtmp: 'RTMP',
        rtsp: 'RTSP'
      }
      return labels[this.currentStreamType] || this.currentStreamType.toUpperCase()
    },
    
    playCurrentUrl() {
      if (!this.videoUrl) return
      
      if (this.activePlayer === 'jessibuca' && this.$refs.jessibucaPlayer) {
        this.$refs.jessibucaPlayer.play(this.videoUrl)
      } else if (this.activePlayer === 'h265web' && this.$refs.h265Player) {
        this.$refs.h265Player.play(this.videoUrl)
      }
    },
    
    handlePtz(command) {
      const speed = Math.floor(this.ptzSpeed * 255 / 100)
      this.$emit('ptz', { command, speed })
      const base = this.getPtzHintText(command)
      if (!base) return
      if (command === 'stop') {
        this.setPtzHint(base, 800)
      } else {
        this.setPtzHint(`${base} · 速度 ${this.ptzSpeed}`)
      }
    },

    setPtzHint(text, autoClearMs) {
      if (this.ptzHintTimer) {
        clearTimeout(this.ptzHintTimer)
        this.ptzHintTimer = null
      }
      this.ptzHint = text
      if (autoClearMs && autoClearMs > 0) {
        this.ptzHintTimer = setTimeout(() => {
          this.ptzHint = ''
          this.ptzHintTimer = null
        }, autoClearMs)
      }
    },

    getPtzHintText(command) {
      const map = {
        up: '↑ 上移',
        down: '↓ 下移',
        left: '← 左移',
        right: '→ 右移',
        zoomIn: '变焦 +',
        zoomOut: '变焦 -',
        focusIn: '聚焦 +',
        focusOut: '聚焦 -',
        irisIn: '光圈 +',
        irisOut: '光圈 -',
        stop: '已停止'
      }
      return map[command] || ''
    },
    
    copyUrl(url) {
      if (!url) {
        this.$message.warning('该流格式地址不存在')
        return
      }
      
      this.$copyText(url).then(() => {
        this.$message.success('已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },
    
    copyCurrentUrl() {
      if (!this.videoUrl) {
        this.$message.warning('暂无流地址')
        return
      }
      this.copyUrl(this.videoUrl)
    },
    
    formatStreamId(stream) {
      if (!stream) return '-'
      return stream.length > 20 ? stream.substring(0, 20) + '...' : stream
    },
    
    // 语音对讲相关
    getBroadcastBtnType() {
      if (this.broadcastStatus === 1) return 'danger'
      if (this.broadcastStatus === 0) return 'primary'
      return 'default'
    },
    
    getBroadcastBtnText() {
      if (this.broadcastStatus === -2) return '释放中'
      if (this.broadcastStatus === 0) return '连接中'
      if (this.broadcastStatus === 1) return '停止对讲'
      return '开始对讲'
    },
    
    toggleBroadcast() {
      if (this.broadcastStatus === -1) {
        this.startBroadcast()
      } else if (this.broadcastStatus === 1) {
        this.stopBroadcast()
      }
    },
    
    async startBroadcast() {
      this.broadcastStatus = 0
      this.$emit('broadcast-start', { 
        deviceId: this.deviceId, 
        channelId: this.channelId, 
        mode: this.broadcastMode 
      })
      
      this.$message.info('语音对讲功能待后端完善')
      this.broadcastStatus = -1
    },
    
    stopBroadcast() {
      this.broadcastStatus = -2
      if (this.broadcastRtc) {
        this.broadcastRtc.close()
        this.broadcastRtc = null
      }
      this.$emit('broadcast-stop', { deviceId: this.deviceId, channelId: this.channelId })
      this.broadcastStatus = -1
    },
    
    handleClose() {
      if (this.$refs.jessibucaPlayer) {
        this.$refs.jessibucaPlayer.destroy()
      }
      if (this.$refs.h265Player) {
        this.$refs.h265Player.destroy()
      }
      
      if (this.broadcastStatus === 1) {
        this.stopBroadcast()
      }
      
      this.visible = false
      this.$emit('close')
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
  height: 500px;
  background: #000;
  position: relative;
}

/* PTZ浮层 - 位于视频区右上角 */
.ptz-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 200px;
  background: rgba(20, 20, 20, 0.92);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  padding: 12px;
  backdrop-filter: blur(10px);
  z-index: 100;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.ptz-overlay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  font-weight: 600;
  color: #fff;
  font-size: 13px;
}

.ptz-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ptz-hint {
  font-weight: 500;
  font-size: 12px;
  color: #91d5ff;
  opacity: 0.9;
}

/* 拖拽提示 */
.drag-handle {
  cursor: move !important;
  user-select: none;
}

.drag-handle:active {
  cursor: grabbing !important;
}

.ptz-overlay-header i {
  cursor: pointer;
  font-size: 14px;
  color: #999;
  transition: color 0.2s;
}

.ptz-overlay-header i:hover {
  color: #1890ff;
}

/* 方向控制 */
.ptz-direction {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.direction-row {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.direction-btn {
  width: 42px;
  height: 42px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.2s;
}

.direction-btn:hover:not(:disabled) {
  background: rgba(24, 144, 255, 0.3);
  border-color: #1890ff;
}

.direction-btn.center {
  cursor: not-allowed;
  background: rgba(255, 255, 255, 0.05);
  opacity: 0.5;
}

/* 速度控制 */
.ptz-speed {
  margin-bottom: 12px;
}

.speed-label {
  font-size: 11px;
  color: #d9d9d9;
  margin-bottom: 6px;
  text-align: center;
  font-weight: 500;
}

/* 变焦/聚焦/光圈 */
.ptz-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.control-label {
  font-size: 12px;
  color: #d9d9d9;
  width: 45px;
  font-weight: 500;
}

.control-buttons {
  display: flex;
  gap: 4px;
}

.control-buttons .ant-btn {
  width: 36px;
  padding: 0;
  font-weight: 600;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.2s;
}

.control-buttons .ant-btn:hover {
  background: rgba(24, 144, 255, 0.3);
  border-color: #1890ff;
  color: #fff;
}

/* 底部工具栏 */
.player-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #1f1f1f;
  border-top: 1px solid #333;
}

.footer-left,
.footer-center,
.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-info {
  color: #d9d9d9;
  font-size: 12px;
}

.footer-info strong {
  color: #fff;
  font-family: 'Courier New', monospace;
}

/* 动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}
</style>
