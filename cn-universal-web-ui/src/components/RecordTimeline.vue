<template>
  <div class="record-timeline-wrapper">
      <!-- 时间轴容器 -->
      <div class="timeline-container" ref="timelineContainer" @wheel.prevent="onWheel">
        <!-- 时间刻度 -->
        <div class="timeline-ruler" :style="{ width: totalWidth + 'px' }">
          <div 
            v-for="(tick, index) in timeTicks" 
            :key="index" 
            class="ruler-item"
            :style="{ left: tick.left + 'px', width: tick.width + 'px' }"
          >
            <span class="ruler-label">{{ tick.label }}</span>
          </div>
        </div>

        <!-- 录像片段 -->
        <div 
          class="timeline-track" 
          :style="{ width: totalWidth + 'px' }"
          @mousedown="onMouseDown" 
          @mouseup="onMouseUp"
          @mousemove="onMouseMove"
          @mouseleave="onMouseLeave"
        >
          <div 
            v-for="(segment, index) in processedSegments" 
            :key="index"
            class="record-segment"
            :class="{ 'selected': selectedSegment === segment }"
            :style="{ 
              left: segment.left + 'px', 
              width: segment.width + 'px' 
            }"
            @click.stop="handleSegmentClick(segment)"
            @mouseenter="handleSegmentHover(segment, $event)"
            @mouseleave="handleSegmentLeave"
          >
          </div>

          <!-- 选择范围 -->
          <div 
            v-if="selectionRange" 
            class="selection-range"
            :style="{ 
              left: selectionRange.left + 'px', 
              width: selectionRange.width + 'px' 
            }"
          ></div>

          <!-- 当前播放位置 -->
          <div 
            v-if="playPosition !== null" 
            class="play-position"
            :style="{ left: playPosition + 'px' }"
          >
            <div class="play-marker"></div>
          </div>
          
          <!-- 中心线（光标位置）- 不接收点击事件 -->
          <div 
            class="center-line"
            :style="{ left: containerWidth / 2 + 'px' }"
            @click.stop
            @mousedown.stop
          ></div>
        </div>

      <!-- 间隙标记 -->
      <div class="timeline-gaps" :style="{ width: totalWidth + 'px' }">
        <div 
          v-for="(gap, index) in recordGaps" 
          :key="index"
          class="gap-mark"
          :style="{ left: gap.left + 'px', width: gap.width + 'px' }"
        ></div>
      </div>
    </div>

    <!-- Tooltip -->
    <div 
      v-show="tooltipVisible" 
      class="timeline-tooltip"
      :style="{ left: tooltipLeft + 'px', top: tooltipTop + 'px' }"
    >
      <div v-if="hoveredSegment">
        <div><strong>开始:</strong> {{ formatTime(hoveredSegment.startTime) }}</div>
        <div><strong>结束:</strong> {{ formatTime(hoveredSegment.endTime) }}</div>
        <div><strong>时长:</strong> {{ formatDuration(hoveredSegment.duration) }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import moment from 'moment'

// 时间分辨率数组（小时）：只支持当天的时间范围，0.5小时、1小时、2小时、6小时、12小时、24小时
const ZOOM = [0.5, 1, 2, 6, 12, 24]
const ONE_HOUR_STAMP = 60 * 60 * 1000

export default {
  name: 'RecordTimeline',
  props: {
    // 录像数据列表
    records: {
      type: Array,
      default: () => []
    },
    // 当前日期
    date: {
      type: String,
      default: () => moment().format('YYYY-MM-DD')
    },
    // 初始缩放级别索引
    initialZoomIndex: {
      type: Number,
      default: 4 // 默认24小时
    },
    // 容器宽度
    containerWidth: {
      type: Number,
      default: 960
    }
  },
  data() {
    return {
      currentZoomIndex: this.initialZoomIndex,
      selectedSegment: null,
      selectionRange: null,
      playPosition: null,
      tooltipVisible: false,
      tooltipLeft: 0,
      tooltipTop: 0,
      hoveredSegment: null,
      isDragging: false,
      dragStartX: 0,
      startTimestamp: 0, // 时间窗口的起始时间戳
      currentTime: 0, // 中心点的时间戳
      hoverTime: null
    }
  },
  computed: {
    // 时间窗口的总毫秒数
    totalMS() {
      return ZOOM[this.currentZoomIndex] * ONE_HOUR_STAMP
    },

    // 总宽度（像素）
    totalWidth() {
      return this.containerWidth
    },

    // 每毫秒对应的像素
    pxPerMS() {
      return this.totalWidth / this.totalMS
    },

    // 时间刻度 - 确保显示完整的一天（0-24点）
    timeTicks() {
      const ticks = []
      const dayStart = moment(this.date).startOf('day').valueOf()
      const dayEnd = moment(this.date).endOf('day').valueOf() + 1 // +1 确保包含24点（下一天的0点）
      const startTime = this.startTimestamp
      const endTime = startTime + this.totalMS
      
      // 根据缩放级别决定刻度间隔
      const interval = this.getTickInterval()
      
      // 计算需要显示的时间范围：从当天0点到24点，但只显示在时间窗口内的部分
      const displayStart = Math.max(dayStart, startTime)
      const displayEnd = Math.min(dayEnd, endTime)
      
      // 从当天0点开始计算刻度，但只显示在显示范围内的
      // 如果时间窗口从0点开始，从0点开始计算；否则从第一个可见的刻度开始
      let startTick = Math.ceil(displayStart / interval) * interval
      // 确保从整点开始（如果可能）
      if (startTick > dayStart && startTick - interval >= dayStart) {
        startTick = Math.max(dayStart, startTick - interval)
      }
      
      for (let time = startTick; time <= displayEnd; time += interval) {
        // 确保不超过当天24点
        if (time > dayEnd) break
        
        const left = (time - startTime) * this.pxPerMS
        // 只显示在时间窗口内的刻度（允许一些边距，确保0点和24点可见）
        if (left >= -10 && left <= this.totalWidth + 10) {
          const label = this.formatTickLabel(time)
          ticks.push({ left, width: 50, label, time })
        }
      }
      
      return ticks
    },

    // 处理录像片段数据
    recordSegments() {
      if (!this.records || this.records.length === 0) {
        return []
      }

      return this.records.map(record => {
        // 处理时间戳格式
        let startTime = record.startTime
        let endTime = record.endTime
        
        // 如果是字符串，尝试转换为数字
        if (typeof startTime === 'string') {
          startTime = parseInt(startTime)
        }
        if (typeof endTime === 'string') {
          endTime = parseInt(endTime)
        }
        
        // 如果是10位秒级时间戳，转换为毫秒
        if (startTime && startTime.toString().length === 10) {
          startTime = startTime * 1000
        }
        if (endTime && endTime.toString().length === 10) {
          endTime = endTime * 1000
        }
        
        // 使用 moment 处理时间戳
        const startMoment = moment(startTime)
        const endMoment = moment(endTime)
        const duration = endMoment.diff(startMoment, 'seconds')
        
        return {
          ...record,
          startTime: startTime,
          endTime: endTime,
          duration: duration
        }
      })
    },

    // 转换为时间轴坐标的片段
    processedSegments() {
      return this.recordSegments.map(segment => {
        // 计算位置和宽度
        const left = (segment.startTime - this.startTimestamp) * this.pxPerMS
        const width = (segment.endTime - segment.startTime) * this.pxPerMS
        
        return {
          ...segment,
          left: left,
          width: Math.max(width, 2) // 最小宽度2px
        }
      })
    },

    // 录像间隙
    recordGaps() {
      if (this.recordSegments.length === 0) {
        return []
      }

      const gaps = []
      const sortedSegments = [...this.recordSegments].sort((a, b) => a.startTime - b.startTime)

      for (let i = 0; i < sortedSegments.length - 1; i++) {
        const currentEnd = sortedSegments[i].endTime
        const nextStart = sortedSegments[i + 1].startTime
        
        if (nextStart - currentEnd > 60000) { // 间隙大于1分钟
          const left = (currentEnd - this.startTimestamp) * this.pxPerMS
          const width = (nextStart - currentEnd) * this.pxPerMS
          
          gaps.push({ left, width })
        }
      }

      return gaps
    }
  },
  mounted() {
    // 初始化时间窗口：默认显示完整的一天（0-24点）
    const dayStart = moment(this.date).startOf('day').valueOf()
    const dayEnd = moment(this.date).endOf('day').valueOf()
    
    // 如果缩放级别是24小时，直接显示完整的一天
    if (ZOOM[this.currentZoomIndex] >= 24) {
      this.startTimestamp = dayStart
      this.currentTime = dayStart + 12 * ONE_HOUR_STAMP // 当天12点
    } else {
      // 其他缩放级别，以当天12点为中心
      this.currentTime = dayStart + 12 * ONE_HOUR_STAMP
      this.startTimestamp = this.currentTime - this.totalMS / 2
    }
    
    // 限制在当天范围内
    this.clampTimeWindow()
    
    // 如果有录像，以第一个录像的开始时间为中心（但不超过当天范围）
    if (this.recordSegments.length > 0) {
      const firstRecord = this.recordSegments[0]
      const recordStartTime = firstRecord.startTime
      // 确保录像时间在当天范围内
      if (recordStartTime >= dayStart && recordStartTime <= dayEnd) {
        this.currentTime = recordStartTime
        this.startTimestamp = this.currentTime - this.totalMS / 2
        this.clampTimeWindow()
      }
    }
    
    this.$emit('ready')
  },
  watch: {
    date() {
      this.selectedSegment = null
      this.playPosition = null
      // 重新初始化时间窗口：默认显示完整的一天（0-24点）
      const dayStart = moment(this.date).startOf('day').valueOf()
      const dayEnd = moment(this.date).endOf('day').valueOf()
      
      // 如果缩放级别是24小时，直接显示完整的一天
      if (ZOOM[this.currentZoomIndex] >= 24) {
        this.startTimestamp = dayStart
        this.currentTime = dayStart + 12 * ONE_HOUR_STAMP
      } else {
        // 其他缩放级别，以当天12点为中心
        this.currentTime = dayStart + 12 * ONE_HOUR_STAMP
        this.startTimestamp = this.currentTime - this.totalMS / 2
      }
      
      // 限制在当天范围内
      this.clampTimeWindow()
    },
    currentTime: {
      handler(newVal) {
        // 注意：这里不再直接触发 timeChange，因为已经在 onMouseUp 和 onMouseMove 中处理
        // 避免重复触发
      },
      immediate: false
    }
  },
  methods: {
    // 获取刻度间隔（毫秒）- 只支持当天范围
    getTickInterval() {
      const zoom = ZOOM[this.currentZoomIndex]
      if (zoom <= 0.5) return 1 * 60 * 1000 // 1分钟
      if (zoom <= 1) return 5 * 60 * 1000 // 5分钟
      if (zoom <= 2) return 10 * 60 * 1000 // 10分钟
      if (zoom <= 6) return 30 * 60 * 1000 // 30分钟
      if (zoom <= 12) return 60 * 60 * 1000 // 1小时
      if (zoom <= 24) return 2 * 60 * 60 * 1000 // 2小时
      return 2 * 60 * 60 * 1000 // 默认2小时
    },

    // 格式化刻度标签 - 只显示时间，不显示日期（因为只支持当天）
    formatTickLabel(time) {
      const m = moment(time)
      const zoom = ZOOM[this.currentZoomIndex]
      
      // 所有缩放级别都只显示时间，不显示日期
      if (zoom <= 0.5) return m.format('HH:mm')
      if (zoom <= 1) return m.format('HH:mm')
      if (zoom <= 2) return m.format('HH:mm')
      if (zoom <= 6) return m.format('HH:mm')
      if (zoom <= 12) return m.format('HH:mm')
      if (zoom <= 24) return m.format('HH:mm')
      return m.format('HH:mm')
    },

    // 限制时间窗口在当天范围内
    clampTimeWindow() {
      const dayStart = moment(this.date).startOf('day').valueOf()
      const dayEnd = moment(this.date).endOf('day').valueOf()
      const minStart = dayStart
      const maxStart = dayEnd - this.totalMS
      
      if (this.startTimestamp < minStart) {
        this.startTimestamp = minStart
        this.currentTime = this.startTimestamp + this.totalMS / 2
      }
      if (this.startTimestamp > maxStart) {
        this.startTimestamp = maxStart
        this.currentTime = this.startTimestamp + this.totalMS / 2
      }
    },

    // 放大
    zoomIn() {
      if (this.currentZoomIndex > 0) {
        const oldCurrentTime = this.currentTime
        this.currentZoomIndex--
        // 保持当前时间在中心
        this.startTimestamp = oldCurrentTime - this.totalMS / 2
        // 限制在当天范围内
        this.clampTimeWindow()
        this.$emit('zoomChange', this.currentZoomIndex)
      }
    },

    // 缩小
    zoomOut() {
      if (this.currentZoomIndex < ZOOM.length - 1) {
        const oldCurrentTime = this.currentTime
        this.currentZoomIndex++
        // 保持当前时间在中心
        this.startTimestamp = oldCurrentTime - this.totalMS / 2
        // 限制在当天范围内
        this.clampTimeWindow()
        this.$emit('zoomChange', this.currentZoomIndex)
      }
    },

    // 重置缩放
    resetZoom() {
      const oldCurrentTime = this.currentTime
      this.currentZoomIndex = this.initialZoomIndex
      this.startTimestamp = oldCurrentTime - this.totalMS / 2
      // 限制在当天范围内
      this.clampTimeWindow()
    },

    onWheel(e) {
      e.preventDefault()
      const delta = Math.max(-1, Math.min(1, e.wheelDelta || -e.detail))
      if (delta < 0) {
        this.zoomOut()
      } else {
        this.zoomIn()
      }
    },

    // 点击片段
    handleSegmentClick(segment) {
      this.selectedSegment = segment
      // 点击片段时，设置时间窗口，使片段在中心显示（调整窗口）
      const segmentCenter = segment.startTime + (segment.endTime - segment.startTime) / 2
      this.setTimeWindow(segmentCenter, true) // preserveZoom = true，但会调整窗口位置
      this.$emit('segmentClick', segment)
    },

    onMouseDown(event) {
      this.isDragging = true
      const rect = this.$refs.timelineContainer.getBoundingClientRect()
      this.dragStartX = event.clientX - rect.left
      // 保存拖动开始时的startTimestamp
      this.dragStartTimestamp = this.startTimestamp
      this.$emit('mousedown')
    },

    onMouseUp(event) {
      if (this.isDragging) {
        const rect = this.$refs.timelineContainer.getBoundingClientRect()
        const endX = event.clientX - rect.left
        
        // 检查是否点击在中心线上（中心线宽度2px，允许一些容差）
        const centerLineX = this.containerWidth / 2
        const centerLineTolerance = 5 // 中心线左右5px范围内不触发点击
        
        // 如果拖动距离很小，认为是点击
        if (Math.abs(endX - this.dragStartX) < 5) {
          // 如果点击在中心线附近，不触发跳转（避免刷新时间窗口）
          // 检查点击位置和拖动起始位置是否都在中心线附近
          const clickNearCenter = Math.abs(endX - centerLineX) <= centerLineTolerance
          const dragStartNearCenter = Math.abs(this.dragStartX - centerLineX) <= centerLineTolerance
          
          if (clickNearCenter && dragStartNearCenter) {
            // 点击在中心线上，不触发任何操作
            this.isDragging = false
            this.dragStartX = 0
            this.dragStartTimestamp = 0
            this.$emit('mouseup')
            return
          }
          
          // 点击事件：计算点击位置的时间
          const clickTime = this.startTimestamp + (endX * this.pxPerMS)
          this.currentTime = clickTime
          // 点击时触发跳转播放
          this.$emit('timeChange', { time: this.currentTime, shouldSeek: true })
        } else {
          // 拖动结束，更新currentTime为窗口中心时间
          // 参考 wvp 的 mouseupTimeline：拖动结束后才触发播放
          this.currentTime = this.startTimestamp + this.totalMS / 2
          // 拖动结束时，触发时间变化事件，并标记为需要跳转播放
          this.$emit('timeChange', { time: this.currentTime, shouldSeek: true })
        }
      }
      this.isDragging = false
      this.dragStartX = 0
      this.dragStartTimestamp = 0
      this.$emit('mouseup')
    },
    
    onMouseMove(event) {
      if (!this.$refs.timelineContainer) return
      
      const rect = this.$refs.timelineContainer.getBoundingClientRect()
      const x = event.clientX - rect.left
      
      if (this.isDragging) {
        // 拖动中：拖动时间窗口（光标不动，时间窗口移动）
        // 参考 wvp 的 drag 方法：只更新 startTimestamp，不触发播放
        const diffX = x - this.dragStartX
        const diffMS = diffX / this.pxPerMS
        let newStartTimestamp = this.dragStartTimestamp - diffMS
        
        // 限制时间窗口范围（不能超出当天）
        const dayStart = moment(this.date).startOf('day').valueOf()
        const dayEnd = moment(this.date).endOf('day').valueOf()
        const minStart = dayStart
        const maxStart = dayEnd - this.totalMS
        
        if (newStartTimestamp < minStart) {
          newStartTimestamp = minStart
        }
        if (newStartTimestamp > maxStart) {
          newStartTimestamp = maxStart
        }
        
        this.startTimestamp = newStartTimestamp
        // 更新中心时间（拖动过程中只更新显示，不触发跳转）
        this.currentTime = this.startTimestamp + this.totalMS / 2
        // 拖动过程中只更新显示时间，不触发播放
        this.$emit('timeChange', { time: this.currentTime, shouldSeek: false })
      } else {
        // 悬停时显示时间
        const hoverTime = this.startTimestamp + (x * this.pxPerMS)
        this.hoverTime = hoverTime
        this.$emit('timeHover', hoverTime)
      }
    },
    
    onMouseLeave() {
      this.isDragging = false
      this.dragStartX = 0
      this.dragStartTimestamp = 0
      this.hoverTime = null
    },

    // 鼠标悬停片段
    handleSegmentHover(segment, event) {
      this.hoveredSegment = segment
      this.tooltipVisible = true
      this.updateTooltipPosition(event)
    },

    // 鼠标离开片段
    handleSegmentLeave() {
      this.tooltipVisible = false
      this.hoveredSegment = null
    },

    // 更新tooltip位置
    updateTooltipPosition(event) {
      const container = this.$refs.timelineContainer
      if (!container) return

      const rect = container.getBoundingClientRect()
      this.tooltipLeft = event.clientX - rect.left + 10
      this.tooltipTop = event.clientY - rect.top - 60
    },

    // 设置播放位置
    setPlayPosition(time, adjustWindow = false) {
      // 处理时间戳格式
      let playTime = time
      if (typeof playTime === 'string') {
        playTime = parseInt(playTime)
      }
      if (playTime && playTime.toString().length === 10) {
        playTime = playTime * 1000
      }
      
      // 计算播放位置相对于时间窗口的偏移
      const left = (playTime - this.startTimestamp) * this.pxPerMS
      
      if (left >= 0 && left <= this.totalWidth) {
        // 播放位置在窗口内，直接更新位置
        this.playPosition = left
        // 滚动到播放位置（确保播放位置可见）
        this.scrollToPosition(left)
      } else if (adjustWindow) {
        // 只有在明确要求时才调整时间窗口（比如切换片段时），保持缩放级别
        this.setTimeWindow(playTime, true) // preserveZoom = true
        // 设置完成后，滚动到中心位置
        this.$nextTick(() => {
          this.scrollToCenter()
        })
      } else {
        // 播放位置不在窗口内，但不调整窗口（比如拖动时）
        this.playPosition = null
      }
    },
    
    // 滚动到指定位置
    scrollToPosition(left) {
      if (!this.$refs.timelineContainer) return
      const container = this.$refs.timelineContainer
      const containerWidth = container.clientWidth
      const scrollLeft = container.scrollLeft
      
      // 如果位置在可视区域外，滚动到该位置
      if (left < scrollLeft) {
        // 位置在左侧，滚动到位置
        container.scrollLeft = Math.max(0, left - 20) // 留20px边距
      } else if (left > scrollLeft + containerWidth) {
        // 位置在右侧，滚动到位置
        container.scrollLeft = left - containerWidth + 20 // 留20px边距
      }
    },
    
    // 滚动到中心位置
    scrollToCenter() {
      if (!this.$refs.timelineContainer) return
      const container = this.$refs.timelineContainer
      const containerWidth = container.clientWidth
      const centerX = this.totalWidth / 2
      container.scrollLeft = centerX - containerWidth / 2
    },
    
    // 设置时间窗口，使指定时间在中心（保持当前缩放级别）
    setTimeWindow(time, preserveZoom = true) {
      // 如果 preserveZoom 为 true，保持当前的缩放级别，只调整时间窗口位置
      if (preserveZoom) {
        this.currentTime = time
        this.startTimestamp = time - this.totalMS / 2
      } else {
        // 如果 preserveZoom 为 false，重置缩放级别（用于 resetZoom）
        this.currentTime = time
        this.startTimestamp = time - this.totalMS / 2
      }
      
      // 统一限制时间窗口范围（不能超出当天）
      this.clampTimeWindow()
      
      // 更新播放位置
      const left = (time - this.startTimestamp) * this.pxPerMS
      if (left >= 0 && left <= this.totalWidth) {
        this.playPosition = left
      }
    },
    
    // 设置选中的片段（供外部调用）
    setSelectedSegment(segment) {
      if (segment) {
        this.selectedSegment = segment
        // 找到对应的片段对象（从 recordSegments 中）
        const foundSegment = this.recordSegments.find(s => {
          return this.normalizeTime(s.startTime) === this.normalizeTime(segment.startTime)
        })
        if (foundSegment) {
          this.selectedSegment = foundSegment
        }
      } else {
        this.selectedSegment = null
      }
    },
    
    // 标准化时间戳
    normalizeTime(time) {
      if (typeof time === 'string') {
        time = parseInt(time)
      }
      if (time && time.toString().length === 10) {
        time = time * 1000
      }
      return time
    },

    // 格式化时间
    formatTime(timestamp) {
      return moment(timestamp).format('HH:mm:ss')
    },

    // 格式化时长
    formatDuration(seconds) {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      
      if (hours > 0) {
        return `${hours}时${minutes}分${secs}秒`
      } else if (minutes > 0) {
        return `${minutes}分${secs}秒`
      } else {
        return `${secs}秒`
      }
    }
  }
}
</script>

<style lang="less" scoped>
.record-timeline-wrapper {
  padding: 8px 12px;
  background: #303030;
  border-top: 1px solid #505050;

  .timeline-container {
    position: relative;
    overflow-x: auto;
    overflow-y: hidden;
    background: #303030;
    border: 1px solid #505050;
    border-radius: 4px;
    scroll-behavior: smooth; /* 平滑滚动 */
    
    /* 自定义滚动条样式 */
    &::-webkit-scrollbar {
      height: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: #2a2a2a;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #555;
      border-radius: 4px;
      
      &:hover {
        background: #666;
      }
    }

    .timeline-ruler {
      position: relative;
      height: 32px;
      border-bottom: 1px solid #505050;
      color: #bfbfbf;

      .ruler-item {
        position: absolute;
        border-left: 1px solid #505050;
        height: 100%;
        padding-left: 4px;
        font-size: 12px;

        .ruler-label {
          position: absolute;
          top: 2px;
          left: 6px;
        }
      }
    }

    .timeline-track {
      position: relative;
      height: 36px;
      background: #2a2a2a;
      cursor: grab;

      &:active {
        cursor: grabbing;
      }

      .record-segment {
        position: absolute;
        top: 6px;
        height: 22px;
        background: #1890ff;
        border-radius: 2px;
        transition: all 0.2s;

        &:hover {
          background: #40a9ff;
          transform: scaleY(1.05);
          z-index: 10;
        }

        &.selected {
          background: #096dd9;
          box-shadow: 0 0 0 1px #0050b3 inset;
        }
      }

      .selection-range {
        position: absolute;
        top: 0;
        height: 100%;
        background: rgba(24, 144, 255, 0.1);
        border: 1px dashed #1890ff;
        pointer-events: none;
      }

      .play-position {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 2px;
        background: #ff4d4f;
        pointer-events: none;
        z-index: 20;

        .play-marker {
          position: absolute;
          top: -4px;
          left: -4px;
          width: 10px;
          height: 10px;
          background: #ff4d4f;
          border-radius: 50%;
        }
      }

      .center-line {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 2px;
        background: #fff;
        pointer-events: none;
        z-index: 15;
      }
    }

    .timeline-gaps {
      position: absolute;
      top: 32px;
      left: 0;
      height: 32px;
      pointer-events: none;

      .gap-mark {
        position: absolute;
        top: 6px;
        height: 20px;
        background: repeating-linear-gradient(
          45deg,
          #ffa39e,
          #ffa39e 4px,
          #ffccc7 4px,
          #ffccc7 8px
        );
        opacity: 0.3;
      }
    }
  }

  .timeline-tooltip {
    position: absolute;
    padding: 6px 8px;
    background: rgba(0, 0, 0, 0.85);
    color: #fff;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.6;
    pointer-events: none;
    z-index: 1000;
    white-space: nowrap;

    div {
      margin: 2px 0;
    }
  }
}
</style>
