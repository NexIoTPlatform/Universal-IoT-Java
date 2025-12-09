<template>
  <div class="cloud-record-player" :style="containerStyle">
    <!-- 视频播放区域 -->
    <div class="cloud-record-play-box" :style="playBoxStyle">
      <jessibuca
        v-if="playerType === 'Jessibuca'"
        ref="videoPlayer"
        :videoUrl="videoUrl"
        :hasAudio="hasAudio"
        @play="onPlayerPlay"
        @pause="onPlayerPause"
        @stop="onPlayerStop"
      />
    </div>

    <!-- 进度条和时间显示 -->
    <div class="cloud-record-player-option-box">
      <div class="cloud-record-show-time">
        {{ currentTimeDisplay }}
      </div>
      <div 
        class="cloud-record-time-process" 
        ref="timeProcess" 
        @click="onProgressClick"
        @mouseenter="onProgressMouseEnter"
        @mousemove="onProgressMouseMove"
        @mouseleave="onProgressMouseLeave"
      >
        <div v-if="streamInfo">
          <div class="cloud-record-time-process-value" :style="progressStyle"></div>
          <transition name="el-fade-in-linear">
            <div v-show="showTimeHover" class="cloud-record-time-process-title" :style="hoverTimeStyle">
              {{ hoverTimeDisplay }}
            </div>
          </transition>
        </div>
      </div>
      <div class="cloud-record-show-time">
        {{ totalTimeDisplay }}
      </div>
    </div>

    <!-- 控制按钮 -->
    <div class="cloud-record-control-bar">
      <!-- 左侧控制 -->
      <div class="control-section left">
        <div class="cloud-record-record-play-control">
          <a class="control-item icon-camera" title="截图" @click="onScreenshot">
            <a-icon type="camera" />
          </a>
        </div>
      </div>

      <!-- 中间控制 -->
      <div class="control-section center">
        <div class="cloud-record-record-play-control">
          <a class="control-item" title="快退5秒" @click="seekBackward">
            <a-icon type="backward" />
          </a>
          <a class="control-item" title="停止" @click="onStop">
            <a-icon type="stop" />
          </a>
          <a v-if="isPlaying" class="control-item" title="暂停" @click="onPause">
            <a-icon type="pause-circle" />
          </a>
          <a v-else class="control-item" title="播放" @click="onPlay">
            <a-icon type="play-circle" />
          </a>
          <a class="control-item" title="快进5秒" @click="seekForward">
            <a-icon type="forward" />
          </a>
          
          <!-- 倍速选择 -->
          <a-dropdown :trigger="['click']">
            <a class="control-item control-speed" title="倍速播放">
              {{ playSpeed }}X
            </a>
            <a-menu slot="overlay" @click="onSpeedChange">
              <a-menu-item v-for="speed in speedOptions" :key="speed">
                <span>{{ speed }}X</span>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </div>
      </div>

      <!-- 右侧控制 -->
      <div class="control-section right">
        <div class="cloud-record-record-play-control">
          <a v-if="!isFullScreen" class="control-item" title="全屏" @click="toggleFullScreen">
            <a-icon type="fullscreen" />
          </a>
          <a v-else class="control-item" title="退出全屏" @click="toggleFullScreen">
            <a-icon type="fullscreen-exit" />
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Jessibuca from '@/components/Jessibuca'
import moment from 'moment'
import screenfull from 'screenfull'

export default {
  name: 'CloudRecordPlayer',
  components: { Jessibuca },
  props: {
    hasAudio: {
      type: Boolean,
      default: true
    },
    height: {
      type: String,
      default: '540px'
    }
  },
  data() {
    return {
      playerType: 'Jessibuca',
      videoUrl: '',
      streamInfo: null,
      recordInfo: null, // 录像信息（startTime, endTime, timeLen等）
      
      // 播放状态
      isPlaying: false,
      currentTime: 0, // 当前播放位置（毫秒）
      totalDuration: 0, // 总时长（毫秒）
      playStartTime: 0, // 播放开始的真实时间戳
      timeUpdateTimer: null, // 时间更新定时器
      
      // 倍速
      playSpeed: 1,
      speedOptions: [1, 2, 4, 6, 8, 16, 20],
      
      // 进度条
      showTimeHover: false,
      hoverPosition: 0,
      
      // 全屏
      isFullScreen: false
    }
  },
  computed: {
    containerStyle() {
      return {
        height: this.isFullScreen ? '100vh' : this.height
      }
    },
    playBoxStyle() {
      return {
        height: this.isFullScreen ? 'calc(100vh - 61px)' : 'calc(100% - 61px)'
      }
    },
    currentTimeDisplay() {
      if (!this.streamInfo) return '--:--:--'
      return this.formatDuration(this.currentTime)
    },
    totalTimeDisplay() {
      if (!this.streamInfo) return '--:--:--'
      return this.formatDuration(this.totalDuration)
    },
    progressStyle() {
      if (!this.totalDuration) return { width: '0%' }
      const percent = (this.currentTime / this.totalDuration) * 100
      return { width: `${Math.min(100, Math.max(0, percent))}%` }
    },
    hoverTimeStyle() {
      return { 
        left: `${this.hoverPosition - 40}px` 
      }
    },
    hoverTimeDisplay() {
      if (!this.showTimeHover || !this.$refs.timeProcess) return ''
      const percent = this.hoverPosition / this.$refs.timeProcess.clientWidth
      const hoverTime = percent * this.totalDuration
      const playTime = this.formatDuration(hoverTime)
      
      // 如果有录像的实际时间信息，也显示真实时间
      if (this.recordInfo && this.recordInfo.startTime) {
        // 字符串时间戳兼容处理
        let startTime = this.recordInfo.startTime
        if (typeof startTime === 'string') {
          startTime = parseInt(startTime)
        }
        // 处理10位秒级时间戳
        if (startTime && startTime.toString().length === 10) {
          startTime = startTime * 1000
        }
        
        const realTime = startTime + hoverTime
        const realTimeStr = moment(realTime).format('HH:mm:ss')
        return `${playTime} (${realTimeStr})`
      }
      return playTime
    }
  },
  mounted() {
    // 监听全屏变化
    if (screenfull.isEnabled) {
      screenfull.on('change', this.onFullScreenChange)
    }
  },
  beforeDestroy() {
    if (screenfull.isEnabled) {
      screenfull.off('change', this.onFullScreenChange)
    }
    this.stopTimeUpdateTimer()
    this.stopPlay()
  },
  methods: {
    /**
     * 设置流信息并开始播放
     * @param {Object} streamInfo - 流信息 { flv, hls, ws_flv, wss_flv, mediaServerId, app, stream, key 等 }
     * @param {Object} recordInfo - 录像信息 { startTime, endTime, timeLen, fileName 等 }
     */
    setStreamInfo(streamInfo, recordInfo) {
      console.log('CloudRecordPlayer setStreamInfo:', { streamInfo, recordInfo })
      this.streamInfo = streamInfo
      this.recordInfo = recordInfo
      
      // 计算总时长（毫秒）
      if (recordInfo && recordInfo.timeLen) {
        let timeLen = recordInfo.timeLen
        // 字符串兼容
        if (typeof timeLen === 'string') {
          timeLen = parseInt(timeLen)
        }
        this.totalDuration = timeLen
      } else if (recordInfo && recordInfo.startTime && recordInfo.endTime) {
        // 处理字符串时间戳
        let startTime = recordInfo.startTime
        let endTime = recordInfo.endTime
        
        if (typeof startTime === 'string') {
          startTime = parseInt(startTime)
        }
        if (typeof endTime === 'string') {
          endTime = parseInt(endTime)
        }
        
        // 处理10位秒级时间戳
        if (startTime && startTime.toString().length === 10) {
          startTime = startTime * 1000
        }
        if (endTime && endTime.toString().length === 10) {
          endTime = endTime * 1000
        }
        
        this.totalDuration = endTime - startTime
      } else {
        this.totalDuration = 0
      }
      
      // 选择合适的流地址
      let url = ''
      if (location.protocol === 'https:') {
        url = streamInfo.wss_flv || streamInfo.ws_flv || streamInfo.flv
      } else {
        url = streamInfo.ws_flv || streamInfo.flv
      }
      
      this.videoUrl = url
      this.currentTime = 0
      this.playSpeed = 1
      
      console.log('设置云端录像流信息:', { url, totalDuration: this.totalDuration })
    },
    
    /**
     * 播放
     */
    onPlay() {
      if (this.$refs.videoPlayer) {
        if (this.videoUrl && !this.$refs.videoPlayer.playing) {
          this.$refs.videoPlayer.playBtnClick()
        }
        this.isPlaying = true
        this.playStartTime = Date.now()
        this.startTimeUpdateTimer()
      }
    },
    
    /**
     * 暂停
     */
    onPause() {
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause()
        this.isPlaying = false
        this.stopTimeUpdateTimer()
      }
    },
    
    /**
     * 停止
     */
    onStop() {
      this.stopPlay()
      this.$emit('stop')
    },
    
    /**
     * 停止播放并清理
     */
    stopPlay() {
      this.stopTimeUpdateTimer()
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.destroy()
      }
      this.isPlaying = false
      this.currentTime = 0
      this.playStartTime = 0
      this.videoUrl = ''
      this.streamInfo = null
      this.recordInfo = null
    },
    
    /**
     * 启动时间更新定时器
     */
    startTimeUpdateTimer() {
      this.stopTimeUpdateTimer()
      this.timeUpdateTimer = setInterval(() => {
        if (this.isPlaying && this.playStartTime > 0) {
          // 计算已播放时长（考虑倍速）
          const elapsed = (Date.now() - this.playStartTime) * this.playSpeed
          this.currentTime = Math.min(this.currentTime + elapsed, this.totalDuration)
          this.playStartTime = Date.now()
          
          // 如果到达结尾，停止播放
          if (this.currentTime >= this.totalDuration) {
            this.onPause()
          }
        }
      }, 500) // 每500ms更新一次进度
    },
    
    /**
     * 停止时间更新定时器
     */
    stopTimeUpdateTimer() {
      if (this.timeUpdateTimer) {
        clearInterval(this.timeUpdateTimer)
        this.timeUpdateTimer = null
      }
    },
    
    /**
     * 快退5秒
     */
    seekBackward() {
      const newTime = Math.max(0, this.currentTime - 5000)
      this.seekTo(newTime)
    },
    
    /**
     * 快进5秒
     */
    seekForward() {
      const newTime = Math.min(this.totalDuration, this.currentTime + 5000)
      this.seekTo(newTime)
    },
    
    /**
     * 跳转到指定位置
     * @param {Number} timeMs - 时间（毫秒）
     */
    seekTo(timeMs) {
      if (!this.streamInfo) return
      
      this.$emit('seek', {
        mediaServerId: this.streamInfo.mediaServerId,
        app: this.streamInfo.app,
        stream: this.streamInfo.stream,
        seek: timeMs
      })
      
      this.currentTime = timeMs
      this.playStartTime = Date.now() // 重置播放开始时间
    },
    
    /**
     * 改变播放速度
     */
    onSpeedChange({ key }) {
      const speed = parseInt(key)
      this.playSpeed = speed
      
      // 调用后端倍速API
      if (this.streamInfo) {
        this.$emit('speedChange', {
          mediaServerId: this.streamInfo.mediaServerId,
          app: this.streamInfo.app,
          stream: this.streamInfo.stream,
          speed: speed
        })
      }
      
      // 设置播放器倍速
      if (this.$refs.videoPlayer && this.$refs.videoPlayer.setPlaybackRate) {
        this.$refs.videoPlayer.setPlaybackRate(speed)
      }
    },
    
    /**
     * 截图
     */
    onScreenshot() {
      if (this.$refs.videoPlayer && this.$refs.videoPlayer.screenshot) {
        this.$refs.videoPlayer.screenshot()
        this.$message.success('截图成功')
      } else {
        this.$message.warning('当前播放器不支持截图功能')
      }
    },
    
    /**
     * 全屏切换
     */
    toggleFullScreen() {
      if (!screenfull.isEnabled) {
        this.$message.warning('当前浏览器不支持全屏')
        return
      }
      
      const el = this.$el
      if (screenfull.isFullscreen) {
        screenfull.exit()
      } else {
        screenfull.request(el)
      }
    },
    
    /**
     * 全屏状态变化
     */
    onFullScreenChange() {
      this.isFullScreen = screenfull.isFullscreen
    },
    
    /**
     * 进度条点击
     */
    onProgressClick(event) {
      if (!this.$refs.timeProcess || !this.streamInfo) return
      
      const x = event.offsetX
      const width = this.$refs.timeProcess.clientWidth
      const percent = x / width
      const targetTime = percent * this.totalDuration
      
      this.seekTo(targetTime)
    },
    
    /**
     * 进度条鼠标进入
     */
    onProgressMouseEnter(event) {
      this.showTimeHover = true
      this.hoverPosition = event.offsetX
    },
    
    /**
     * 进度条鼠标移动
     */
    onProgressMouseMove(event) {
      this.hoverPosition = event.offsetX
    },
    
    /**
     * 进度条鼠标离开
     */
    onProgressMouseLeave() {
      this.showTimeHover = false
    },
    
    /**
     * 播放器播放事件
     */
    onPlayerPlay() {
      this.isPlaying = true
      this.playStartTime = Date.now()
      this.startTimeUpdateTimer()
      this.$emit('play')
    },
    
    /**
     * 播放器暂停事件
     */
    onPlayerPause() {
      this.isPlaying = false
      this.stopTimeUpdateTimer()
      this.$emit('pause')
    },
    
    /**
     * 播放器停止事件
     */
    onPlayerStop() {
      this.stopPlay()
      this.$emit('stop')
    },
    
    /**
     * 格式化时长
     */
    formatDuration(ms) {
      if (!ms || ms < 0) return '00:00:00'
      const duration = moment.duration(ms, 'milliseconds')
      const hours = Math.floor(duration.asHours())
      const minutes = duration.minutes()
      const seconds = duration.seconds()
      return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.cloud-record-player {
  width: 100%;
  background-color: #000;
  display: flex;
  flex-direction: column;
}

.cloud-record-play-box {
  width: 100%;
  background-color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cloud-record-player-option-box {
  height: 20px;
  width: 100%;
  display: grid;
  grid-template-columns: 70px auto 70px;
  background-color: #000;
}

.cloud-record-time-process {
  width: 100%;
  height: 8px;
  margin: 6px 0;
  border-radius: 4px;
  border: 1px solid #505050;
  background-color: #383838;
  cursor: pointer;
  position: relative;
}

.cloud-record-show-time {
  color: #fff;
  text-align: center;
  font-size: 14px;
  line-height: 20px;
  user-select: none;
}

.cloud-record-time-process-value {
  height: 6px;
  background-color: #1890ff;
  border-radius: 4px;
  transition: width 0.1s ease;
}

.cloud-record-time-process-title {
  position: absolute;
  top: -30px;
  padding: 4px 8px;
  background-color: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 10;
}

.cloud-record-control-bar {
  height: 40px;
  background-color: #383838;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
}

.control-section {
  display: flex;
  align-items: center;
}

.control-section.left {
  justify-content: flex-start;
}

.control-section.center {
  justify-content: center;
}

.control-section.right {
  justify-content: flex-end;
}

.cloud-record-record-play-control {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 10px;
  height: 32px;
  line-height: 32px;
  background-color: #262626;
  box-shadow: 0 0 10px #262626;
  margin: 4px 0;
  border-radius: 4px;
}

.control-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
  color: #fff;
  cursor: pointer;
  transition: color 0.3s;
  user-select: none;
}

.control-item:hover {
  color: #1890ff;
}

.control-item.control-speed {
  font-weight: bold;
  min-width: 50px;
  text-align: center;
}

.el-fade-in-linear-enter-active,
.el-fade-in-linear-leave-active {
  transition: opacity 0.2s linear;
}

.el-fade-in-linear-enter,
.el-fade-in-linear-leave-to {
  opacity: 0;
}
</style>
