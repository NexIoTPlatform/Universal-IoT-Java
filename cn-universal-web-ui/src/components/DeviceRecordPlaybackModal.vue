<template>
  <a-modal
    v-model="visible"
    v-modal-drag
    :title="recordInfo ? `${formatRecordName(recordInfo)} - 设备录像回放` : '设备录像回放'"
    :width="showRecordList ? '1200px' : '960px'"
    :footer="null"
    :destroyOnClose="true"
    @cancel="handleCancel"
    wrapClassName="cloud-record-play-modal"
  >
    <div class="device-record-modal-content" :class="{ 'with-sidebar': showRecordList }">
      <!-- 录像片段列表侧边栏（参考 wvp 官方实现） -->
      <div v-if="showRecordList" class="record-list-sidebar">
        <div class="sidebar-header">
          <span>录像片段</span>
          <a-icon type="close" @click="showRecordList = false" />
        </div>
        <div class="sidebar-content-wrapper">
          <!-- 日期选择器（参考 wvp 官方实现） -->
          <div class="date-picker-wrapper">
            <a-date-picker
              v-model="chooseDate"
              size="small"
              format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
              @change="dateChange"
            />
          </div>
          <!-- 录像列表 -->
          <div class="sidebar-content">
            <ul v-if="dayRecords.length > 0" class="record-list">
              <li
                v-for="(item, index) in dayRecords"
                :key="index"
                :class="['record-item', { 'active': currentRecordIndex === index }]"
                @click="selectRecord(index)"
              >
                <div class="record-item-content">
                  <a-icon type="video-camera" />
                  <div class="record-time-wrapper">
                    <span class="record-time">{{ getFileShowName(item) }}</span>
                    <span class="record-duration">{{ getRecordDuration(item) }}</span>
                  </div>
                </div>
                <a-icon type="download" class="record-download" @click.stop="downloadRecord(item)" />
              </li>
            </ul>
            <div v-else class="record-list-no-val">暂无数据</div>
          </div>
        </div>
      </div>
      
      <!-- 播放器区域 -->
      <div class="player-container" :style="{ width: showRecordList ? 'calc(100% - 220px)' : '100%', height: '580px' }">
        <device-record-player
          v-if="playerReady && !playLoading"
          ref="recordPlayer"
          :hasAudio="true"
          :height="'580px'"
          :instanceKey="instanceKey"
          :deviceId="deviceId"
          :channelId="channelId"
          :records="dayRecords"
          :currentRecord="recordInfo"
          @play="onPlayerPlay"
          @pause="onPlayerPause"
          @stop="onPlayerStop"
          @userStop="onUserStop"
          @seek="onPlayerSeek"
          @speedChange="onPlayerSpeedChange"
          @segmentClick="onSegmentClick"
          @toggleRecordList="showRecordList = !showRecordList"
          @streamReady="onStreamReady"
          @error="onPlayerError"
        />
        <!-- 添加加载中状态 -->
        <div 
          v-else-if="playLoading && recordInfo"
          style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; background: #000;"
        >
          <a-spin size="large" />
          <span style="margin-left: 12px; font-size: 16px;">正在加载视频流...</span>
        </div>
        <a-spin 
           v-else
          :spinning="true" 
          tip="加载视频流..." 
          style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;"
        />
      </div>
    </div>
    
    <!-- 下载弹窗 -->
    <device-record-download
      v-if="downloadModalVisible"
      v-model="downloadModalVisible"
      :device="downloadDevice"
      :channel="downloadChannel"
      :instanceKey="instanceKey"
      :record="currentDownloadRecord"
    />
  </a-modal>
</template>

<script>
import DeviceRecordPlayer from './DeviceRecordPlayer'
import DeviceRecordDownload from './DeviceRecordDownload'
import moment from 'moment'
import modalDrag from '@/directive/modal-drag'
import { getRecords } from '@/api/video/channel'

export default {
  name: 'DeviceRecordPlaybackModal',
  components: { DeviceRecordPlayer, DeviceRecordDownload },
  directives: { modalDrag },
  props: {
    value: {
      type: Boolean,
      default: false
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
    record: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      playLoading: false,
      playerReady: false,
      recordInfo: null,
      showLoadingDelay: null,
      dayRecords: [], // 当天的所有录像记录
      isUserStopped: false, // 标记是否是用户主动停止
      showRecordList: true, // 是否显示录像列表侧边栏（默认显示，参考 wvp 官方）
      currentRecordIndex: -1, // 当前选中的录像索引
      chooseDate: null, // 选择的日期（参考 wvp 官方实现）
      errorMessageShown: false, // 防止重复显示错误消息
      errorMessageTimer: null, // 错误消息定时器
      // 下载相关
      downloadModalVisible: false,
      currentDownloadRecord: null,
      downloadDevice: { deviceId: '' },
      downloadChannel: { channelId: '' }
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
    visible(newVal) {
      if (newVal) {
        this.onModalOpen()
      } else {
        this.onModalClose()
      }
    },
    record: {
      handler(newVal) {
        if (newVal && this.visible) {
          this.playRecord(newVal)
        }
      },
      immediate: true
    }
  },
  beforeDestroy() {
    if (this.showLoadingDelay) {
      clearTimeout(this.showLoadingDelay)
    }
    if (this.errorMessageTimer) {
      clearTimeout(this.errorMessageTimer)
    }
  },
  methods: {
    async onModalOpen() {
      this.playerReady = false
      this.playLoading = false
      this.recordInfo = null
      this.isUserStopped = false
      this.currentRecordIndex = -1
      this.errorMessageShown = false
      if (this.errorMessageTimer) {
        clearTimeout(this.errorMessageTimer)
        this.errorMessageTimer = null
      }
      
      // 设置日期（参考 wvp 官方实现）
      if (this.record) {
        let startTime = this.record.startTime
        if (typeof startTime === 'string') {
          startTime = parseInt(startTime)
        }
        if (startTime && startTime.toString().length === 10) {
          startTime = startTime * 1000
        }
        this.chooseDate = moment(startTime)
      } else {
        this.chooseDate = moment()
      }
      
      // 查询当天的所有录像记录，用于显示时间线
      await this.loadDayRecords()
      
      if (this.record) {
        // 查找当前录像在列表中的索引
        this.findCurrentRecordIndex()
        this.playRecord(this.record)
      }
    },
    
    findCurrentRecordIndex() {
      // 使用当前播放的recordInfo而不是初始的record
      const currentRecord = this.recordInfo || this.record
      if (!currentRecord || !this.dayRecords.length) {
        this.currentRecordIndex = -1
        return
      }
      
      const recordStartTime = this.normalizeTime(currentRecord.startTime)
      const foundIndex = this.dayRecords.findIndex(r => {
        const rStartTime = this.normalizeTime(r.startTime)
        return rStartTime === recordStartTime
      })
      
      this.currentRecordIndex = foundIndex
      
      console.log('findCurrentRecordIndex:', {
        currentRecord,
        recordStartTime,
        currentRecordIndex: this.currentRecordIndex,
        dayRecordsCount: this.dayRecords.length
      })
    },
    
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
     * 日期变化（参考 wvp 官方 dateChange 实现）
     */
    async dateChange() {
      this.dayRecords = []
      this.currentRecordIndex = -1
      await this.loadDayRecords()
      
      // 如果有录像记录，自动选择第一个
      if (this.dayRecords.length > 0) {
        this.selectRecord(0)
      }
    },
    
    /**
     * 加载当天录像记录（参考 wvp 官方实现）
     */
    async loadDayRecords() {
      if (!this.chooseDate) {
        this.chooseDate = moment()
      }
      
      try {
        // 查询当天的所有录像：从 00:00:00 到 23:59:59（参考 wvp 官方实现）
        const dayStart = this.chooseDate.clone().startOf('day').valueOf()
        const dayEnd = this.chooseDate.clone().endOf('day').valueOf()
        
        // 查询当天的所有录像
        const res = await getRecords(
          this.instanceKey,
          this.deviceId,
          this.channelId,
          dayStart,
          dayEnd
        )
        
        this.dayRecords = res.data || []
        console.log('加载当天录像记录:', {
          date: this.chooseDate.format('YYYY-MM-DD'),
          dayStart: moment(dayStart).format('YYYY-MM-DD HH:mm:ss'),
          dayEnd: moment(dayEnd).format('YYYY-MM-DD HH:mm:ss'),
          count: this.dayRecords.length
        })
      } catch (error) {
        console.error('加载当天录像记录失败:', error)
        this.dayRecords = []
        // 加载录像记录失败时，使用友好的错误提示
        this.showFriendlyError(error)
      }
    },
    
    onModalClose() {
      if (this.$refs.recordPlayer) {
        this.$refs.recordPlayer.stopPlay()
      }
      if (this.showLoadingDelay) {
        clearTimeout(this.showLoadingDelay)
        this.showLoadingDelay = null
      }
      this.playerReady = false
      this.playLoading = false
      this.recordInfo = null
    },
    
    async playRecord(record, shouldAdjustWindow = true) {
      if (!record) return
      
      // 检查是否是相同的记录，避免重复调用
      if (this.recordInfo) {
        const currentStartTime = this.normalizeTime(this.recordInfo.startTime)
        const newStartTime = this.normalizeTime(record.startTime)
        if (currentStartTime === newStartTime && this.playerReady) {
          console.log('⚠️ 相同的记录，忽略 playRecord 调用')
          return
        }
      }
      
      console.log('playRecord called with:', record, 'shouldAdjustWindow:', shouldAdjustWindow)
      
      // 清除之前的延迟定时器
      if (this.showLoadingDelay) {
        clearTimeout(this.showLoadingDelay)
        this.showLoadingDelay = null
      }
      
      // 先停止当前播放
      if (this.$refs.recordPlayer) {
        this.$refs.recordPlayer.stopPlay()
      }
      
      // 先更新recordInfo，这样findCurrentRecordIndex才能正确工作
      this.recordInfo = record
      this.playerReady = false
      
      // 更新当前录像索引（使用新的recordInfo）
      this.findCurrentRecordIndex()
      
      // 延迟100ms显示加载状态，避免快速加载时的闪烁
      this.showLoadingDelay = setTimeout(() => {
        this.playLoading = true
      }, 100)
      
      // 等待播放器准备就绪
      await this.$nextTick()
      this.playerReady = true
      
      // 设置录像信息到播放器
      this.$nextTick(async () => {
        if (this.$refs.recordPlayer) {
          // 传递 shouldAdjustWindow 参数，控制是否调整时间窗口
          await this.$refs.recordPlayer.setRecordInfo(record, shouldAdjustWindow)
          // 设置完成后再次更新索引，确保同步
          this.findCurrentRecordIndex()
        }
        // 清除加载状态
        if (this.showLoadingDelay) {
          clearTimeout(this.showLoadingDelay)
          this.showLoadingDelay = null
        }
        this.playLoading = false
      })
    },
    
    onPlayerPlay() {
      console.log('设备录像开始播放')
    },
    
    onPlayerPause() {
      console.log('设备录像暂停')
    },
    
    onPlayerStop(event) {
      console.log('设备录像停止', event)
      // 如果事件包含 userStopped 标志，说明是用户主动停止
      if (event && event.userStopped) {
        this.isUserStopped = true
        this.handleCancel()
      }
    },
    
    onUserStop() {
      // 用户主动停止
      this.isUserStopped = true
    },
    
    onStreamReady(streamInfo) {
      console.log('流信息准备就绪:', streamInfo)
    },
    
    onPlayerSeek(seekInfo) {
      console.log('跳转播放位置:', seekInfo)
    },
    
    onPlayerSpeedChange(speedInfo) {
      console.log('改变播放速度:', speedInfo)
    },
    
    onSegmentClick(segment, shouldAdjustWindow = true) {
      // 时间线片段点击，切换到该录像片段
      // shouldAdjustWindow: 是否调整时间窗口（点击片段时为true，拖动时为false）
      // 参考 wvp 的逻辑，切换片段后从片段开始播放
      this.playRecord(segment, shouldAdjustWindow)
      // 更新当前录像索引
      this.findCurrentRecordIndex()
      // 等待播放器准备好后，自动播放
      this.$nextTick(() => {
        setTimeout(() => {
          if (this.$refs.recordPlayer) {
            this.$refs.recordPlayer.onPlay()
          }
        }, 500)
      })
    },
    
    selectRecord(index) {
      if (index >= 0 && index < this.dayRecords.length) {
        this.currentRecordIndex = index
        const record = this.dayRecords[index]
        this.playRecord(record)
        
        // 设置时间线播放位置，定位光标
        this.$nextTick(() => {
          if (this.$refs.recordPlayer && this.$refs.recordPlayer.$refs.timeline) {
            const timeline = this.$refs.recordPlayer.$refs.timeline
            // 使用录像的开始时间定位光标
            let startTime = record.startTime
            if (typeof startTime === 'string') {
              startTime = parseInt(startTime)
            }
            if (startTime && startTime.toString().length === 10) {
              startTime = startTime * 1000
            }
            timeline.setPlayPosition(startTime, true) // adjustWindow = true，调整时间窗口
          }
        })
      }
    },
    
    /**
     * 下载录像（参考 wvp 官方 downloadFile 实现）
     */
    async downloadRecord(record) {
      if (!record) {
        // 如果没有传入 record，使用当前播放的时间范围
        if (!this.recordInfo) {
          this.$message.warning('请先选择要下载的录像片段')
          return
        }
        const startTimeStr = moment(this.recordInfo.startTime).format('YYYY-MM-DD HH:mm:ss')
        const endTimeStr = moment(this.recordInfo.endTime).format('YYYY-MM-DD HH:mm:ss')
        record = {
          startTime: this.recordInfo.startTime,
          endTime: this.recordInfo.endTime,
          startTimeStr,
          endTimeStr
        }
      }
      
      // 如果正在播放，先停止播放，然后递归调用（参考 wvp 官方逻辑）
      const streamInfo = this.getStreamInfo()
      if (streamInfo !== null) {
        if (this.$refs.recordPlayer) {
          await this.$refs.recordPlayer.stopPlayRecord(() => {
            this.downloadRecord(record)
          })
        }
        return
      }
      
      // 构建下载记录信息
      const downloadRecord = {
        id: record.id || `${record.startTime}_${record.endTime}`,
        name: record.name || `设备录像_${this.formatTime(record.startTime)}`,
        startTime: record.startTime,
        endTime: record.endTime,
        fileSize: record.fileSize || 0
      }
      
      // 构建设备和通道信息
      this.downloadDevice = { deviceId: this.deviceId }
      this.downloadChannel = { channelId: this.channelId }
      this.currentDownloadRecord = downloadRecord
      
      // 打开下载弹窗（DeviceRecordDownload 组件内部会处理下载逻辑）
      this.downloadModalVisible = true
    },
    
    getFileShowName(item) {
      const start = this.formatTime(item.startTime)
      const end = this.formatTime(item.endTime)
      return `${start}-${end}`
    },
    
    /**
     * 获取录像时长（格式化显示）
     */
    getRecordDuration(item) {
      if (!item || !item.startTime || !item.endTime) return ''
      
      let startTime = this.normalizeTime(item.startTime)
      let endTime = this.normalizeTime(item.endTime)
      
      const duration = endTime - startTime // 毫秒
      const seconds = Math.floor(duration / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)
      
      if (hours > 0) {
        const remainingMinutes = minutes % 60
        return remainingMinutes > 0 ? `${hours}小时${remainingMinutes}分钟` : `${hours}小时`
      } else if (minutes > 0) {
        const remainingSeconds = seconds % 60
        return remainingSeconds > 0 ? `${minutes}分${remainingSeconds}秒` : `${minutes}分钟`
      } else {
        return `${seconds}秒`
      }
    },
    
    /**
     * 显示友好的错误消息（避免重复弹窗）
     */
    showFriendlyError(error) {
      // 清除之前的定时器
      if (this.errorMessageTimer) {
        clearTimeout(this.errorMessageTimer)
        this.errorMessageTimer = null
      }
      
      // 防止重复显示错误消息
      if (this.errorMessageShown) {
        return
      }
      
      this.errorMessageShown = true
      
      // 解析错误信息，转换为用户友好的提示
      let errorMsg = '未知错误'
      if (error) {
        if (error.response?.data?.msg) {
          errorMsg = error.response.data.msg
        } else if (error.message) {
          errorMsg = error.message
        }
      }
      
      // 将技术性错误信息转换为用户友好的提示
      let friendlyMsg = this.getFriendlyErrorMessage(errorMsg)
      
      // 使用 notification 而不是 message，避免与系统弹窗冲突
      this.$notification.error({
        message: '播放提示',
        description: friendlyMsg,
        duration: 4,
        placement: 'topRight'
      })
      
      // 5秒后重置标志，允许再次显示错误
      this.errorMessageTimer = setTimeout(() => {
        this.errorMessageShown = false
        this.errorMessageTimer = null
      }, 5000)
    },
    
    /**
     * 将技术性错误信息转换为用户友好的提示
     */
    getFriendlyErrorMessage(errorMsg) {
      if (!errorMsg) return '播放失败，请稍后重试'
      
      const msg = errorMsg.toLowerCase()
      
      // 超时相关错误
      if (msg.includes('timeout') || msg.includes('timed out') || msg.includes('超时')) {
        return '视频流连接超时，请检查网络连接或稍后重试'
      }
      
      // 网络相关错误
      if (msg.includes('network') || msg.includes('网络') || msg.includes('connection')) {
        return '网络连接异常，请检查网络后重试'
      }
      
      // 400 错误
      if (msg.includes('400') || msg.includes('bad request')) {
        return '请求参数错误，请重新选择录像片段'
      }
      
      // 404 错误
      if (msg.includes('404') || msg.includes('not found')) {
        return '未找到该录像片段，请选择其他时间段'
      }
      
      // 500 错误
      if (msg.includes('500') || msg.includes('server error')) {
        return '服务器异常，请稍后重试'
      }
      
      // WVP 相关错误
      if (msg.includes('wvp') || msg.includes('回放失败')) {
        if (msg.includes('端口') || msg.includes('ssrc')) {
          return '视频流资源暂时不可用，请稍后重试'
        }
        return '视频流获取失败，请稍后重试'
      }
      
      // 其他错误，显示简化后的提示
      if (msg.length > 50) {
        return '播放失败，请稍后重试'
      }
      
      return '播放失败：' + errorMsg
    },
    
    onPlayerError(error) {
      console.error('播放器错误:', error)
      this.showFriendlyError(error)
      
      if (this.showLoadingDelay) {
        clearTimeout(this.showLoadingDelay)
        this.showLoadingDelay = null
      }
      this.playLoading = false
    },
    
    handleCancel() {
      this.visible = false
      this.$emit('close')
    },
    
    formatRecordName(record) {
      if (!record) return '设备录像'
      const start = this.formatTime(record.startTime)
      const end = this.formatTime(record.endTime)
      return `${start} - ${end}`
    },
    
    formatTime(timestamp) {
      if (!timestamp) return '--:--:--'
      let time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      if (time && time.toString().length === 10) {
        time = time * 1000
      }
      return moment(time).format('HH:mm:ss')
    },
    
    getStreamInfo() {
      if (this.$refs.recordPlayer) {
        return this.$refs.recordPlayer.getStreamInfo()
      }
      return null
    }
  }
}
</script>

<style scoped>
::v-deep .cloud-record-play-modal {
  .ant-modal-header {
    padding: 12px 16px;
    border: none;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  .ant-modal-title {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
  }
  
  .ant-modal-body {
    padding: 0;
  }
  
  .ant-modal-close {
    top: 8px;
    right: 8px;
  }
  
  .ant-modal-close-x {
    color: #fff;
    font-size: 16px;
    line-height: 40px;
    width: 40px;
    height: 40px;
  }
  
  .ant-modal-close-x:hover {
    color: rgba(255, 255, 255, 0.8);
  }
}

.device-record-modal-content {
  display: flex;
  width: 100%;
  height: 580px;
  background: #000;
}

.device-record-modal-content.with-sidebar {
  display: flex;
}

.record-list-sidebar {
  width: 220px;
  background: #1f1f1f;
  border-right: 1px solid #383838;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  height: 40px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #383838;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.sidebar-header .anticon {
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.sidebar-header .anticon:hover {
  color: #fff;
}

.sidebar-content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.date-picker-wrapper {
  padding: 12px;
  border-bottom: 1px solid #383838;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.record-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.record-item {
  padding: 0;
  margin: 0;
  margin: 0.5rem 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.3s;
  color: #bfbfbf;
  min-height: 32px;
  height: auto;
  line-height: 20px;
  box-sizing: border-box;
  padding: 6px 12px;
}

.record-item:hover {
  background-color: #2a2a2a;
}

.record-item.active {
  background-color: #1890ff;
  color: #fff;
}

.record-item-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  line-height: 20px;
}

.record-item-content .anticon {
  flex-shrink: 0;
  font-size: 14px;
  line-height: 20px;
  vertical-align: middle;
}

.record-time-wrapper {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.record-time {
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 16px;
  vertical-align: middle;
}

.record-duration {
  font-size: 11px;
  color: #8c8c8c;
  line-height: 14px;
  white-space: nowrap;
}

.record-item.active .record-duration {
  color: rgba(255, 255, 255, 0.8);
}

.record-download {
  flex-shrink: 0;
  color: #1890ff;
  cursor: pointer;
  padding: 0 4px;
  transition: color 0.3s;
  font-size: 14px;
  line-height: 20px;
  vertical-align: middle;
  display: inline-flex;
  align-items: center;
}

.record-item.active .record-download {
  color: #fff;
}

.record-download:hover {
  color: #40a9ff;
}

.record-list-no-val {
  width: fit-content;
  position: relative;
  color: #9f9f9f;
  top: 50%;
  left: calc(50% - 2rem);
  transform: translateY(-50%);
  margin: 0 auto;
  padding: 20px;
}

.player-container {
  flex: 1;
  background: #000;
  position: relative;
}
</style>

