<template>
  <div class="device-record-player" :style="containerStyle">
    <!-- 视频播放区域 -->
    <div class="playBox" :style="playBoxStyle">
      <jessibuca
        ref="videoPlayer"
        :videoUrl="videoUrl"
        :hasAudio="hasAudio"
        @play="onPlayerPlay"
        @pause="onPlayerPause"
        @stop="onPlayerStop"
        @playTimeChange="showPlayTimeChange"
      />
    </div>

    <!-- 时间线 -->
    <div class="player-option-box">
      <record-timeline
        ref="timeline"
        :records="records"
        :date="currentDate"
        :initialZoomIndex="4"
        :containerWidth="960"
        @segmentClick="onTimelineSegmentClick"
        @timeChange="onTimelineTimeChange"
        @mousedown="timelineMouseDown"
        @mouseup="mouseupTimeline"
      />
      <div v-if="showTime" class="time-line-show">{{ showTimeValue }}</div>
    </div>

    <!-- 控制按钮 -->
    <div class="control-bar">
      <div style="text-align: left;">
        <div class="record-play-control" style="background-color: transparent; box-shadow: 0 0 10px transparent">
          <a class="control-item" title="录像列表" @click="$emit('toggleRecordList')">
            <a-icon type="unordered-list" />
          </a>
          <a class="control-item" title="截图" @click="snap">
            <a-icon type="camera" />
          </a>
          <a class="control-item" title="放大时间轴" @click="timelineZoomIn">
            <a-icon type="zoom-in" />
          </a>
          <a class="control-item" title="缩小时间轴" @click="timelineZoomOut">
            <a-icon type="zoom-out" />
          </a>
          <a class="control-item" title="重置时间轴" @click="timelineResetZoom">
            <a-icon type="fullscreen-exit" />
          </a>
        </div>
      </div>
      <div style="text-align: center;">
        <div class="record-play-control">
          <a
            v-if="currentRecordIndex > 0"
            class="control-item"
            title="上一个"
            @click="playLast"
          />
          <a
            v-else
            class="control-item"
            style="color: #acacac; cursor: not-allowed"
            title="上一个"
          />
          <a class="control-item" title="快退五秒" @click="seekBackward">
            <a-icon type="backward" />
          </a>
          <a class="control-item" title="停止" @click="stopPlay">
            <a-icon type="stop" />
          </a>
          <a
            v-if="playing"
            class="control-item"
            title="暂停"
            @click="pausePlay"
          >
            <a-icon type="pause-circle" />
          </a>
          <a v-if="!playing" class="control-item" title="播放" @click="play">
            <a-icon type="play-circle" />
          </a>
          <a class="control-item" title="快进五秒" @click="seekForward">
            <a-icon type="forward" />
          </a>
          <a
            v-if="currentRecordIndex >= 0 && currentRecordIndex < records.length - 1"
            class="control-item"
            title="下一个"
            @click="playNext"
          />
          <a
            v-else
            class="control-item"
            style="color: #acacac; cursor: not-allowed"
            title="下一个"
          />
          <a-dropdown :trigger="['click']">
            <a class="control-item control-speed" title="倍速播放">
              {{ playSpeed }}X
            </a>
            <a-menu slot="overlay" @click="changePlaySpeed">
              <a-menu-item v-for="speed in playSpeedRange" :key="speed">
                <span>{{ speed }}X</span>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </div>
      </div>
      <div style="text-align: right;">
        <div class="record-play-control" style="background-color: transparent; box-shadow: 0 0 10px transparent">
          <a v-if="!isFullScreen" class="control-item" title="全屏" @click="fullScreen">
            <a-icon type="fullscreen" />
          </a>
          <a v-else class="control-item" title="退出全屏" @click="fullScreen">
            <a-icon type="fullscreen-exit" />
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Jessibuca from '@/components/Jessibuca'
import RecordTimeline from '@/components/RecordTimeline'
import moment from 'moment'
import screenfull from 'screenfull'
import { getPlaybackUrl } from '@/api/video/channel'

export default {
  name: 'DeviceRecordPlayer',
  components: { Jessibuca, RecordTimeline },
  props: {
    hasAudio: {
      type: Boolean,
      default: true
    },
    height: {
      type: String,
      default: '540px'
    },
    instanceKey: {
      type: String,
      required: true
    },
    deviceId: {
      type: String,
      required: true
    },
    channelId: {
      type: String,
      required: true
    },
    records: {
      type: Array,
      default: () => []
    },
    currentRecord: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      videoUrl: null,
      streamInfo: null,
      recordInfo: null,
      
      // 播放状态
      playing: false,
      playerTime: 0, // 播放器时间（毫秒）
      playTime: null, // 当前播放的真实时间（时间戳）
      playSpeed: 1,
      playSpeedRange: [0.25, 0.5, 1, 2, 4],
      playSeekValue: 0,
      
      // 时间线
      showTime: false, // 隐藏播放时中间跳动的时间显示
      timelineControl: false,
      initTime: null,
      timeSegments: [],
      currentRecordIndex: null,
      
      // 全屏
      isFullScreen: false,
      
      // 防止重复请求
      isRequesting: false, // 是否正在请求中
      pendingRequest: null, // 待处理的请求
      lastRequestParams: null // 上次请求的参数，用于去重
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
        height: this.isFullScreen ? 'calc(100vh - 90px)' : 'calc(100% - 90px)',
        width: '100%',
        backgroundColor: '#000000'
      }
    },
    currentDate() {
      if (!this.recordInfo || !this.recordInfo.startTime) {
        return moment().format('YYYY-MM-DD')
      }
      let startTime = this.recordInfo.startTime
      if (typeof startTime === 'string') {
        startTime = parseInt(startTime)
      }
      if (startTime && startTime.toString().length === 10) {
        startTime = startTime * 1000
      }
      return moment(startTime).format('YYYY-MM-DD')
    },
    showTimeValue() {
      if (!this.playTime) return '--:--:--'
      return moment(this.playTime).format('YYYY-MM-DD HH:mm:ss')
    },
    endTime() {
      if (!this.recordInfo) {
        return moment().format('YYYY-MM-DD HH:mm:ss')
      }
      let endTime = this.recordInfo.endTime
      if (typeof endTime === 'string') {
        endTime = parseInt(endTime)
      }
      if (endTime && endTime.toString().length === 10) {
        endTime = endTime * 1000
      }
      return moment(endTime).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  watch: {
    records: {
      handler() {
        this.updateTimeSegments()
      },
      deep: true,
      immediate: true
    },
    currentRecord: {
      handler(val, oldVal) {
        // 只有当 currentRecord 真正变化时才调用 setRecordInfo
        // 避免初始化时和相同值时的重复调用
        if (val && (!oldVal || this.isRecordDifferent(val, oldVal))) {
          this.setRecordInfo(val)
        }
      },
      immediate: true
    }
  },
  mounted() {
    if (screenfull.isEnabled) {
      screenfull.on('change', this.onFullScreenChange)
    }
    window.addEventListener('beforeunload', this.stopPlayRecord)
  },
  beforeDestroy() {
    if (screenfull.isEnabled) {
      screenfull.off('change', this.onFullScreenChange)
    }
    window.removeEventListener('beforeunload', this.stopPlayRecord)
    this.stopPlayRecord()
  },
  methods: {
    /**
     * 判断两个录像记录是否不同
     */
    isRecordDifferent(record1, record2) {
      if (!record1 || !record2) return true
      const time1 = this.normalizeTime(record1.startTime)
      const time2 = this.normalizeTime(record2.startTime)
      return time1 !== time2
    },
    
    /**
     * 设置录像信息（参考 wvp 官方 chooseFile 实现）
     */
    setRecordInfo(recordInfo) {
      // 如果正在请求中，忽略新的设置请求
      if (this.isRequesting) {
        console.log('正在请求中，忽略 setRecordInfo')
        return
      }
      
      // 如果 recordInfo 相同，不重复设置
      if (this.recordInfo && 
          this.normalizeTime(this.recordInfo.startTime) === this.normalizeTime(recordInfo.startTime)) {
        console.log('录像信息相同，忽略 setRecordInfo')
        return
      }
      
      this.recordInfo = recordInfo
      
      // 计算总时长
      let startTime = recordInfo.startTime
      let endTime = recordInfo.endTime
      
      if (typeof startTime === 'string') {
        startTime = parseInt(startTime)
      }
      if (typeof endTime === 'string') {
        endTime = parseInt(endTime)
      }
      
      if (startTime && startTime.toString().length === 10) {
        startTime = startTime * 1000
      }
      if (endTime && endTime.toString().length === 10) {
        endTime = endTime * 1000
      }
      
      this.recordInfo.startTime = startTime
      this.recordInfo.endTime = endTime
      this.playTime = startTime
      this.initTime = startTime
      
      // 更新当前记录索引
      this.updateCurrentRecordIndex()
      
      // 播放录像
      this.playRecord(
        moment(startTime).format('YYYY-MM-DD HH:mm:ss'),
        this.endTime
      )
    },
    
    /**
     * 更新当前记录索引
     */
    updateCurrentRecordIndex() {
      if (!this.records || !this.records.length || !this.recordInfo) {
        this.currentRecordIndex = null
        return
      }
      
      const currentStartTime = this.normalizeTime(this.recordInfo.startTime)
      this.currentRecordIndex = this.records.findIndex(r => {
        return this.normalizeTime(r.startTime) === currentStartTime
      })
    },
    
    /**
     * 更新时间片段（参考 wvp 官方 dateChange 实现）
     */
    updateTimeSegments() {
      this.timeSegments = []
      if (!this.records || !this.records.length) return
      
      for (let i = 0; i < this.records.length; i++) {
        const record = this.records[i]
        let startTime = record.startTime
        let endTime = record.endTime
        
        if (typeof startTime === 'string') {
          startTime = parseInt(startTime)
        }
        if (typeof endTime === 'string') {
          endTime = parseInt(endTime)
        }
        
        if (startTime && startTime.toString().length === 10) {
          startTime = startTime * 1000
        }
        if (endTime && endTime.toString().length === 10) {
          endTime = endTime * 1000
        }
        
        this.timeSegments.push({
          beginTime: startTime,
          endTime: endTime,
          color: '#017690',
          startRatio: 0.7,
          endRatio: 0.85,
          index: i
        })
      }
      
      if (this.timeSegments.length > 0 && !this.initTime) {
        this.initTime = this.timeSegments[0].beginTime
      }
    },
    
    /**
     * 播放录像（完全参考 wvp 官方实现，添加防重复请求机制）
     */
    playRecord(startTime, endTime) {
      const requestKey = `${startTime}_${endTime}`
      
      console.log('playRecord 调用:', { startTime, endTime, requestKey, isRequesting: this.isRequesting, lastRequestParams: this.lastRequestParams, hasStreamInfo: this.streamInfo !== null })
      
      // 如果正在请求相同的参数，忽略
      if (this.isRequesting && this.lastRequestParams === requestKey) {
        console.log('❌ 正在请求相同参数，忽略:', { startTime, endTime })
        return
      }
      
      // 如果上次请求的参数相同且已有流信息，忽略
      if (this.lastRequestParams === requestKey && this.streamInfo !== null) {
        console.log('❌ 请求参数相同且已有流信息，忽略:', { startTime, endTime })
        return
      }
      
      // 如果正在请求中，保存待处理的请求（但参数不同）
      if (this.isRequesting) {
        console.log('⏳ 正在请求中，保存待处理请求:', { startTime, endTime })
        this.pendingRequest = { startTime, endTime }
        return
      }
      
      // 如果已有流信息，先停止
      if (this.streamInfo !== null) {
        console.log('🛑 已有流信息，先停止再请求:', { startTime, endTime })
        // 标记为正在停止，防止重复调用
        this.isRequesting = true
        this.stopPlayRecord(() => {
          // 停止完成后，确保 streamInfo 已清空，再执行新请求
          console.log('✅ 停止完成，streamInfo:', this.streamInfo)
          this.isRequesting = false
          this.pendingRequest = null
          // 确保 streamInfo 已清空后再执行
          if (this.streamInfo === null) {
            this.playRecord(startTime, endTime)
          } else {
            // 如果还没清空，再等一次
            setTimeout(() => {
              if (this.streamInfo === null) {
                this.playRecord(startTime, endTime)
              } else {
                console.warn('⚠️ streamInfo 仍未清空，强制清空后重试')
                this.streamInfo = null
                this.playRecord(startTime, endTime)
              }
            }, 100)
          }
        })
        return
      }
      
      // 标记为正在请求，保存请求参数
      this.isRequesting = true
      this.lastRequestParams = requestKey
      this.playerTime = 0
      
      console.log('🚀 开始请求播放地址:', { startTime, endTime, requestKey })
      
      getPlaybackUrl(
        this.instanceKey,
        this.deviceId,
        this.channelId,
        { startTime, endTime }
      )
        .then(res => {
          const data = res.data || {}
          this.streamInfo = data
          this.videoUrl = this.getUrlByStreamInfo()
          console.log('✅ 播放录像成功:', { startTime, endTime, videoUrl: this.videoUrl })
          
          // 请求完成，重置标志
          this.isRequesting = false
          
          // 如果有待处理的请求且参数不同，执行它
          if (this.pendingRequest) {
            const pending = this.pendingRequest
            // 检查待处理请求的参数是否与当前请求不同
            const pendingKey = `${pending.startTime}_${pending.endTime}`
            if (pendingKey !== requestKey) {
              this.pendingRequest = null
              // 延迟执行，确保当前请求完全完成
              setTimeout(() => {
                this.playRecord(pending.startTime, pending.endTime)
              }, 100)
            } else {
              // 参数相同，清除待处理请求
              console.log('⚠️ 待处理请求参数相同，清除')
              this.pendingRequest = null
            }
          }
        })
        .catch(error => {
          console.error('❌ 播放录像失败:', error)
          // 请求失败，重置标志
          this.isRequesting = false
          this.lastRequestParams = null
          this.pendingRequest = null
          // 不在这里显示错误消息，由父组件统一处理，避免重复弹窗
          this.$emit('error', error)
        })
    },
    
    /**
     * 根据流信息获取 URL（完全参考 wvp 官方实现）
     */
    getUrlByStreamInfo() {
      if (!this.streamInfo) return null
      if (location.protocol === 'https:') {
        return this.streamInfo.wss_flv || null
      } else {
        return this.streamInfo.ws_flv || null
      }
    },
    
    /**
     * 停止播放录像（完全参考 wvp 官方实现）
     */
    stopPlayRecord(callback) {
      console.log('停止录像回放')
      if (this.streamInfo !== null) {
        if (this.$refs.videoPlayer) {
          this.$refs.videoPlayer.pause()
        }
        this.videoUrl = null
        // 这里可以调用后端停止接口，如果有的话
        // await stopPlayback(this.instanceKey, this.deviceId, this.channelId, this.streamInfo.stream)
        this.streamInfo = null
      }
      // 确保 streamInfo 已清空后再执行 callback
      // 使用 setTimeout 确保状态更新完成
      setTimeout(() => {
        if (callback) callback()
      }, 50)
    },
    
    /**
     * 播放（完全参考 wvp 官方实现）
     */
    play() {
      if (this.$refs.videoPlayer && this.$refs.videoPlayer.loaded) {
        if (this.$refs.videoPlayer.unPause) {
          this.$refs.videoPlayer.unPause()
        } else {
          this.$refs.videoPlayer.playBtnClick()
        }
      } else {
        this.playRecord(this.showTimeValue, this.endTime)
      }
    },
    
    /**
     * 暂停（完全参考 wvp 官方实现）
     */
    pausePlay() {
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause()
      }
    },
    
    /**
     * 停止（完全参考 wvp 官方实现）
     */
    stopPlay() {
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.destroy()
      }
    },
    
    /**
     * 快退五秒（完全参考 wvp 官方实现）
     */
    seekBackward() {
      this.playSeekValue -= 5 * 1000
      this.play()
    },
    
    /**
     * 快进五秒（完全参考 wvp 官方实现）
     */
    seekForward() {
      this.playSeekValue += 5 * 1000
      this.play()
    },
    
    /**
     * 改变播放速度（参考 wvp 官方实现）
     */
    changePlaySpeed({ key }) {
      const speed = parseFloat(key)
      this.playSpeed = speed
      
      if (this.$refs.videoPlayer && this.$refs.videoPlayer.setPlaybackRate) {
        this.$refs.videoPlayer.setPlaybackRate(this.playSpeed)
      }
      
      // 这里可以调用后端倍速接口，如果有的话
      // if (this.streamInfo) {
      //   setPlaybackSpeed(this.instanceKey, this.deviceId, this.channelId, this.streamInfo.stream, speed)
      // }
    },
    
    /**
     * 截图（参考 wvp 官方实现）
     */
    snap() {
      if (this.$refs.videoPlayer && this.$refs.videoPlayer.screenshot) {
        this.$refs.videoPlayer.screenshot()
      }
    },
    
    /**
     * 全屏（完全参考 wvp 官方实现）
     */
    fullScreen() {
      if (this.isFullScreen) {
        screenfull.exit()
        this.isFullScreen = false
        return
      }
      
      const playerWidth = this.$refs.videoPlayer?.playerWidth || 0
      const playerHeight = this.$refs.videoPlayer?.playerHeight || 0
      const playerBox = this.$el.querySelector('.playBox') || this.$el
      
      screenfull.request(playerBox)
      screenfull.on('change', () => {
        if (this.$refs.videoPlayer && this.$refs.videoPlayer.resize) {
          this.$refs.videoPlayer.resize(playerWidth, playerHeight)
        }
        this.isFullScreen = screenfull.isFullscreen
      })
      this.isFullScreen = true
    },
    
    /**
     * 全屏状态变化
     */
    onFullScreenChange() {
      this.isFullScreen = screenfull.isFullscreen
    },
    
    /**
     * 播放上一个（参考 wvp 官方实现）
     */
    playLast() {
      if (this.currentRecordIndex === null || this.currentRecordIndex === 0) {
        return
      }
      const prevIndex = this.currentRecordIndex - 1
      if (this.records[prevIndex]) {
        this.$emit('segmentClick', this.records[prevIndex])
      }
    },
    
    /**
     * 播放下一个（参考 wvp 官方实现）
     */
    playNext() {
      if (this.currentRecordIndex === null || this.currentRecordIndex >= this.records.length - 1) {
        return
      }
      const nextIndex = this.currentRecordIndex + 1
      if (this.records[nextIndex]) {
        this.$emit('segmentClick', this.records[nextIndex])
      }
    },
    
    /**
     * 时间线片段点击
     */
    onTimelineSegmentClick(segment) {
      this.$emit('segmentClick', segment)
    },
    
    /**
     * 时间线时间变化（参考 wvp 官方 playTimeChange 实现）
     */
    onTimelineTimeChange(timeInfo) {
      let time
      if (typeof timeInfo === 'object' && timeInfo !== null) {
        time = timeInfo.time
      } else {
        time = timeInfo
      }
      
      if (time === this.playTime) {
        return
      }
      this.playTime = time
    },
    
    /**
     * 时间线鼠标按下（参考 wvp 官方实现）
     */
    timelineMouseDown() {
      this.timelineControl = true
    },
    
    /**
     * 时间线鼠标抬起（完全参考 wvp 官方 mouseupTimeline 实现）
     */
    mouseupTimeline() {
      if (!this.timelineControl) {
        this.timelineControl = false
        return
      }
      this.timelineControl = false
      this.playRecord(this.showTimeValue, this.endTime)
    },
    
    /**
     * 播放器播放事件（适配为 playStatusChange）
     */
    onPlayerPlay() {
      this.playing = true
    },
    
    /**
     * 播放器暂停事件（适配为 playStatusChange）
     */
    onPlayerPause() {
      this.playing = false
    },
    
    /**
     * 播放器停止事件
     */
    onPlayerStop() {
      this.playing = false
    },
    
    /**
     * 播放时间变化（参考 wvp 官方 showPlayTimeChange 实现）
     */
    showPlayTimeChange(val) {
      // val 是毫秒
      this.playTime += (val - this.playerTime)
      this.playerTime = val
    },
    
    /**
     * 标准化时间戳
     */
    normalizeTime(time) {
      if (typeof time === 'string') {
        time = parseInt(time)
      }
      if (time && time.toString().length === 10) {
        time = time * 1000
      }
      return time
    },
    
    /**
     * 时间轴放大
     */
    timelineZoomIn() {
      if (this.$refs.timeline) {
        this.$refs.timeline.zoomIn()
      }
    },
    
    /**
     * 时间轴缩小
     */
    timelineZoomOut() {
      if (this.$refs.timeline) {
        this.$refs.timeline.zoomOut()
      }
    },
    
    /**
     * 时间轴重置缩放
     */
    timelineResetZoom() {
      if (this.$refs.timeline) {
        this.$refs.timeline.resetZoom()
      }
    }
  }
}
</script>

<style scoped>
.device-record-player {
  width: 100%;
  background-color: #000;
  display: flex;
  flex-direction: column;
}

.playBox {
  width: 100%;
  background-color: #000000;
  position: relative;
}

.player-option-box {
  height: 50px;
  position: relative;
}

.time-line-show {
  position: absolute;
  color: rgba(250, 249, 249, 0.89);
  left: calc(50% - 85px);
  top: -72px;
  text-shadow: 1px 0 #5f6b7c, -1px 0 #5f6b7c, 0 1px #5f6b7c, 0 -1px #5f6b7c, 1.1px 1.1px #5f6b7c, 1.1px -1.1px #5f6b7c, -1.1px 1.1px #5f6b7c, -1.1px -1.1px #5f6b7c;
  z-index: 10;
  pointer-events: none;
}

.control-bar {
  height: 40px;
  background-color: #383838;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
}

.record-play-control {
  height: 32px;
  line-height: 32px;
  display: inline-block;
  width: fit-content;
  padding: 0 10px;
  box-shadow: 0 0 10px #262626;
  background-color: #262626;
  margin: 4px 0;
}

.control-item {
  display: inline-block;
  padding: 0 10px;
  color: #fff;
  margin-right: 2px;
  cursor: pointer;
  transition: color 0.3s;
}

.control-item:hover {
  color: #1f83e6;
}

.control-item.control-speed {
  font-weight: bold;
  color: #fff;
  user-select: none;
}
</style>
