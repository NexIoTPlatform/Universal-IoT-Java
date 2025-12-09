<template>
  <a-modal
    :maskClosable="false"
    :title="title"
    v-model="visible"
    width="1200px"
    :footer="null"
    :destroyOnClose="true"
    @cancel="handleClose"
    :bodyStyle="{ padding: 0, height: '70vh' }"
  >
    <div class="map-track-container">
      <!-- Left Panel: Track Details -->
      <div class="left-panel">
        <!-- Time Range Selector -->
        <div class="time-selector">
          <div class="selector-title">时间范围</div>
          <div class="btn-group">
            <span class="time-btn" :class="{'active': timeRange === 1}" @click="changeTime(1)">6小时</span>
            <span class="time-btn" :class="{'active': timeRange === 2}" @click="changeTime(2)">24小时</span>
            <span class="time-btn" :class="{'active': timeRange === 3}" @click="changeTime(3)">7天</span>
            <span class="time-btn" :class="{'active': timeRange === 4}" @click="changeTime(4)">15天</span>
            <a-popover v-model="customVisible" trigger="click" placement="bottomRight">
              <div slot="content" style="width: 280px;">
                <div class="custom-date-picker">
                  <label>选择时间范围：</label>
                  <a-range-picker v-model="customRange" show-time style="width: 100%; margin-top: 8px;" />
                  <div class="picker-actions">
                    <a-button type="primary" size="small" @click="submitCustom">确定</a-button>
                    <a-button size="small" @click="hideCustom">取消</a-button>
                  </div>
                </div>
              </div>
              <span class="time-btn" :class="{'active': timeRange === 5}">自定义</span>
            </a-popover>
          </div>
        </div>

        <!-- Track Statistics -->
        <div class="track-stats" v-if="trackInfo">
          <div class="stats-row">
            <div class="stats-item">
              <a-icon type="environment" style="color: #1890ff; margin-right: 4px;" />
              <span class="stats-label">点数:</span>
              <span class="stats-value">{{ trackInfo.pointCount }}</span>
            </div>
            <div class="stats-item">
              <a-icon type="line-chart" style="color: #fa8c16; margin-right: 4px;" />
              <span class="stats-label">长度:</span>
              <span class="stats-value">{{ formatDistance(trackInfo.totalDistance) }}</span>
            </div>
          </div>
          <div class="stats-row">
            <div class="stats-item full-width">
              <a-icon type="clock-circle" style="color: #597ef7; margin-right: 4px;" />
              <span class="stats-label">时间:</span>
              <span class="stats-value">{{ trackInfo.timeRange }}</span>
            </div>
          </div>
        </div>

        <!-- Track Points Timeline -->
        <div class="timeline-section">
          <div class="section-title">
            轨迹详情
            <span class="point-count">共 {{ trackPoints.length }} 个点</span>
            <div class="group-controls" v-if="timeGroups.length > 1">
              <a-button size="small" type="link" @click="expandAllGroups">展开全部</a-button>
              <a-button size="small" type="link" @click="collapseAllGroups">收起全部</a-button>
            </div>
          </div>
          <div class="timeline-list" v-if="timeGroups.length > 0">
            <!-- 时间分组列表 -->
            <div class="time-group" v-for="group in timeGroups" :key="group.id">
              <div class="group-header" @click="toggleGroup(group)">
                <div class="group-info">
                  <a-icon :type="group.expanded ? 'down' : 'right'" />
                  <span class="group-time">{{ group.startTimeStr }}</span>
                  <span class="group-duration" v-if="group.duration > 0">
                    ({{ getDurationDesc(group.duration) }})
                  </span>
                </div>
                <div class="group-stats">
                  <span class="point-count">{{ group.points.length }} 个点</span>
                </div>
              </div>

              <!-- 分组内的轨迹点 -->
              <div class="group-points" v-show="group.expanded">
                <div
                  v-for="point in group.points"
                  :key="point.index"
                  class="timeline-item"
                  :class="{
                    active: currentPointIndex === point.index,
                    'is-start': point.index === 0,
                    'is-end': point.index === trackPoints.length - 1
                  }"
                  @click="focusPoint(point.index)"
                >
                  <div class="timeline-marker">
                    <div class="marker-dot" v-if="point.index !== 0 && point.index !== trackPoints.length - 1"></div>
                    <a-icon v-if="point.index === 0" type="play-circle" theme="filled" style="font-size: 16px; color: #52c41a;" />
                    <a-icon v-if="point.index === trackPoints.length - 1" type="check-circle" theme="filled" style="font-size: 16px; color: #f5222d;" />
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="point-label">
                        <template v-if="point.index === 0">🚩 起点</template>
                        <template v-else-if="point.index === trackPoints.length - 1">🏁 终点</template>
                        <template v-else>#{{ point.index + 1 }}</template>
                      </span>
                      <span class="point-time">{{ formatTime(point.createTime) }}</span>
                    </div>
                    <div class="timeline-coords">
                      <a-icon type="environment" style="margin-right: 4px; color: #999;" />
                      {{ point.lng.toFixed(6) }}, {{ point.lat.toFixed(6) }}
                    </div>
                    <!-- <div class="timeline-data" v-if="point.content">
                      <a-icon type="file-text" style="margin-right: 4px; color: #999;" />
                      <span class="data-preview">{{ point.content }}</span>
                    </div> -->
                  </div>
                </div>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无轨迹数据" />
        </div>
      </div>

      <!-- Right Panel: Map -->
      <div class="right-panel">
        <div ref="mapContainer" class="map-container"></div>
        <!-- Map Playback Controls -->
        <div class="map-playback-controls" v-if="trackPoints.length > 0">
          <div class="playback-buttons">
            <a-button
              type="primary"
              :icon="isPlaying ? 'pause' : 'caret-right'"
              @click="togglePlayback"
              size="small"
            >
              {{ isPlaying ? '暂停' : '播放' }}
            </a-button>
            <a-button
              icon="reload"
              @click="resetPlayback"
              size="small"
            >
              重播
            </a-button>
          </div>
          <div class="progress-container">
            <a-slider
              v-model="playbackProgress"
              :min="0"
              :max="100"
              @change="onProgressChange"
              :tipFormatter="(val) => `${Math.round(val)}%`"
              size="small"
            />
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { listDeviceMetaV2 } from '@/api/system/dev/deviceLog'
import AMapLoader from '@amap/amap-jsapi-loader'
import mapConfig from '@/config/map.config'

export default {
  name: 'MapTrackModal',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    deviceId: {
      type: String,
      required: true
    },
    productKey: {
      type: String,
      required: true
    },
    metaId: {
      type: String,
      required: true
    },
    metaName: {
      type: String,
      default: '设备轨迹'
    }
  },
  data() {
    return {
      visible: false,
      timeRange: 1,
      customVisible: false,
      customRange: [],
      trackData: [],
      loading: false,
      map: null,
      polyline: null,
      markers: [],
      movingMarker: null, // 移动的车辆标记
      trackPoints: [],
      trackInfo: null,
      // 播放控制
      isPlaying: false,
      playbackProgress: 0,
      currentPointIndex: 0,
      playbackTimer: null,
      playbackSpeed: 1,
      // UI 控制
      showPointsPanel: true,
      // 时间分组数据
      timeGroups: [],
      expandedGroups: []
    }
  },
  computed: {
    title() {
      return this.metaName + ' 轨迹地图'
    }
  },

  watch: {
    show: {
      immediate: true,
      handler(newVal) {
        console.log('MapTrackModal show prop changed to:', newVal)
        console.log('MapTrackModal current visible state:', this.visible)
        this.visible = newVal
        console.log('MapTrackModal visible set to:', this.visible)
        if (newVal) {
          console.log('MapTrackModal calling initMap...')
          this.$nextTick(() => {
            this.initMap()
          })
        }
      }
    }
  },

  methods: {
    // 按时间分组轨迹点
    groupTrackPointsByTime(points) {
      if (!points || points.length === 0) return []

      const groups = []
      let currentGroup = null

      points.forEach((point, index) => {
        const time = new Date(point.createTime)
        const timeStr = this.formatDateTime(time)
        const timeDiff = index > 0 ? time - new Date(points[index - 1].createTime) : 0

        // 如果时间间隔大于30分钟，创建新分组
        if (!currentGroup || timeDiff > 30 * 60 * 1000) {
          if (currentGroup) {
            groups.push(currentGroup)
          }
          currentGroup = {
            id: `group_${groups.length}`,
            startTime: time,
            startTimeStr: timeStr,
            endTime: time,
            endTimeStr: timeStr,
            duration: 0,
            points: [],
            expanded: false  // 默认收起分组
          }
        }

        // 更新当前分组的结束时间和持续时间
        currentGroup.endTime = time
        currentGroup.endTimeStr = timeStr
        currentGroup.duration = currentGroup.endTime - currentGroup.startTime
        currentGroup.points.push({
          ...point,
          index: index,
          timeInGroup: time - currentGroup.startTime
        })
      })

      // 添加最后一个分组
      if (currentGroup) {
        groups.push(currentGroup)
      }

      return groups
    },

    // 格式化日期时间
    formatDateTime(date) {
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
      const diffDays = Math.floor((today - targetDate) / (24 * 60 * 60 * 1000))

      const timeStr = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

      if (diffDays === 0) {
        return `今天 ${timeStr}`
      } else if (diffDays === 1) {
        return `昨天 ${timeStr}`
      } else if (diffDays === 2) {
        return `前天 ${timeStr}`
      } else {
        return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' + timeStr
      }
    },

    // 获取分组的持续时间描述
    getDurationDesc(duration) {
      if (duration < 60 * 1000) {
        return `${Math.floor(duration / 1000)}秒`
      } else if (duration < 60 * 60 * 1000) {
        return `${Math.floor(duration / (60 * 1000))}分钟`
      } else {
        const hours = Math.floor(duration / (60 * 60 * 1000))
        const minutes = Math.floor((duration % (60 * 60 * 1000)) / (60 * 1000))
        return `${hours}小时${minutes > 0 ? minutes + '分钟' : ''}`
      }
    },

    // 切换分组展开状态
    toggleGroup(group) {
      group.expanded = !group.expanded
    },

    // 展开/折叠所有分组
    expandAllGroups() {
      this.timeGroups.forEach(group => {
        group.expanded = true
      })
    },

    collapseAllGroups() {
      this.timeGroups.forEach(group => {
        group.expanded = false
      })
    },

    async initMapAndLoadData() {
      await this.initMap()
      if (this.map) {
        await this.loadTrackData()
      }
    },

    async initMap() {
      console.log('[MapTrack] initMap called')
      const container = this.$refs.mapContainer
      console.log('[MapTrack] mapContainer ref:', container)
      if (!container) {
        console.warn('[MapTrack] Map container not found')
        return
      }

      // 等待DOM完全渲染
      await this.$nextTick()

      const rect = container.getBoundingClientRect()
      console.log('[MapTrack] Map container rect:', rect)

      if (!rect.width || !rect.height) {
        console.warn('[MapTrack] Map container has no dimensions, retrying...')
        // 如果尺寸不正确，延迟重试
        setTimeout(() => {
          const retryRect = container.getBoundingClientRect()
          console.log('[MapTrack] Retry rect:', retryRect)
          if (retryRect.width && retryRect.height) {
            this.createMap(container)
          }
        }, 500)
        return
      }

      this.createMap(container)
    },

    async createMap(container) {
      try {
        const AMap = await AMapLoader.load({
          key: mapConfig.amap.key || '',
          version: '2.0',
          plugins: ['AMap.Polyline', 'AMap.Marker', 'AMap.InfoWindow']
        })

        // 确保容器有正确的尺寸
        const rect = container.getBoundingClientRect()
        console.log('[MapTrack] Creating map with container size:', rect)

        this.map = new AMap.Map(container, {
          zoom: 13,
          center: [120.184074, 30.188022],
          resizeEnable: true,
          zoomEnable: true,
          dragEnable: true,
          doubleClickZoom: true,
          scrollWheel: true,
          touchZoom: true,
          keyboardEnable: true,
          viewMode: '2D',
          mapStyle: 'amap://styles/normal'
        })

        console.log('[MapTrack] Map created successfully')

        // 地图创建成功后自动加载1小时数据
        setTimeout(() => {
          if (this.map && this.visible) {
            console.log('[MapTrack] Auto loading 1 hour data')
            this.loadTrackData()
          }
        }, 500)

        // 强制调整地图大小 - 多次调整确保显示正常
        setTimeout(() => {
          if (this.map) {
            this.map.resize()
            console.log('[MapTrack] Map resized - first time')
          }
        }, 200)

        setTimeout(() => {
          if (this.map) {
            this.map.resize()
            console.log('[MapTrack] Map resized - second time')
          }
        }, 500)

      } catch (e) {
        console.error('[MapTrack] Map initialization failed:', e)
        this.$message.error('地图初始化失败')
      }
    },

    changeTime(range) {
      this.timeRange = range
      this.stopPlayback()
      this.loadTrackData()
    },

    submitCustom() {
      if (this.customRange && this.customRange.length === 2) {
        this.timeRange = 5
        this.stopPlayback()
        this.customVisible = false
        this.loadTrackData()
      } else {
        this.$message.warning('自定义日期不能为空!')
      }
    },

    hideCustom() {
      this.customRange = []
      this.customVisible = false
    },

    async loadTrackData() {
      console.log('[MapTrack] loadTrackData called, this:', this)
      this.loading = true
      this.trackData = []

      console.log('Loading track data for time range:', this.timeRange)

      const params = { properties: this.metaId }
      let pageSize = 5000
      let beginTime, endTime
      const now = new Date().getTime()

      if (this.timeRange === 1) {
        pageSize = 5000
        beginTime = new Date().setHours(new Date().getHours() - 6)
        endTime = now
      } else if (this.timeRange === 2) {
        pageSize = 8000
        beginTime = new Date().setDate(new Date().getDate() - 1)
        endTime = now
      } else if (this.timeRange === 3) {
        pageSize = 15000
        beginTime = new Date().setDate(new Date().getDate() - 7)
        endTime = now
      } else if (this.timeRange === 4) {
        pageSize = 30000
        beginTime = new Date().setDate(new Date().getDate() - 15)
        endTime = now
      } else if (this.timeRange === 5 && this.customRange && this.customRange.length === 2) {
        pageSize = 30000
        beginTime = this.customRange[0].valueOf()
        endTime = this.customRange[1].valueOf()
      }
      
      params.beginCreateTime = parseInt(beginTime / 1000)
      params.endCreateTime = parseInt(endTime / 1000)
      
      console.log('Time range:', new Date(beginTime), 'to', new Date(endTime))
      
      const query = {
        pageNum: 1,
        pageSize: pageSize,
        productKey: this.productKey,
        messageType: 'PROPERTIES',
        iotId: this.deviceId,
        property: this.metaId,
        beginCreateTime: params.beginCreateTime,
        endCreateTime: params.endCreateTime,
        params: params
      }
      
      console.log('Query params:', query)
      
      try {
        const response = await listDeviceMetaV2(query)
        const dataList = response.list || []
        
        console.log('Received data list length:', dataList.length)
        
        // 解析坐标
        this.trackPoints = this.parseTrackData(dataList)
        
        console.log('Parsed track points:', this.trackPoints.length)
        
        // 按时间分组轨迹点
        console.log('About to call groupTrackPointsByTime, method exists:', typeof this.groupTrackPointsByTime)
        console.log('this context:', this)
        try {
          this.timeGroups = this.groupTrackPointsByTime(this.trackPoints)
          console.log('Time groups created:', this.timeGroups.length)
        } catch (error) {
          console.error('Error calling groupTrackPointsByTime:', error)
          this.timeGroups = []
        }
        
        if (this.trackPoints.length > 0 && this.map) {
          console.log('Drawing track on map')
          this.drawTrack(this.trackPoints)
          this.calculateTrackInfo()
        } else {
          console.warn('No track points to display')
          this.$message.info('该时间段内没有轨迹数据')
          // 即使没有数据也要更新统计信息
          this.trackInfo = {
            pointCount: 0,
            totalDistance: 0,
            timeRange: '0分钟'
          }
        }
      } catch (e) {
        console.error('加载轨迹数据失败:', e)
        this.$message.error('加载轨迹数据失败')
      } finally {
        this.loading = false
      }
    },
    
    parseTrackData(dataList) {
      if (!dataList || dataList.length === 0) {
        console.log('Empty data list')
        return []
      }
      
      console.log('Parsing data list with', dataList.length, 'items')
      
      const validPoints = dataList
        .filter(item => {
          if (!item || !item.ext2) {
            console.log('Filtering out item without ext2:', item)
            return false
          }
          return true
        })
        .map(item => {
          try {
            const coords = item.ext2.split(',')
            console.log('Parsing coordinates:', item.ext2, '->', coords)
            
            if (coords.length >= 2) {
              const lng = parseFloat(coords[0].trim())
              const lat = parseFloat(coords[1].trim())
              
              console.log('Parsed lng:', lng, 'lat:', lat)
              
              if (!isNaN(lng) && !isNaN(lat) && 
                  lng !== 0 && lat !== 0 &&
                  lng >= -180 && lng <= 180 && 
                  lat >= -90 && lat <= 90) {
                const point = {
                  lng: lng,
                  lat: lat,
                  createTime: item.createTime,
                  deviceName: item.deviceName || '未知设备',
                  content: item.content || ''
                }
                console.log('Valid point:', point)
                return point
              } else {
                console.log('Invalid coordinates:', lng, lat)
              }
            }
          } catch (e) {
            console.error('Error parsing coordinates:', e, item)
          }
          return null
        })
        .filter(point => point !== null)
        .sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
      
      console.log('Valid points after parsing:', validPoints.length)
      return validPoints
    },
    
    calculateTrackInfo() {
      if (this.trackPoints.length < 2) return
      
      // 计算总距离
      let totalDistance = 0
      for (let i = 1; i < this.trackPoints.length; i++) {
        totalDistance += this.calculateDistance(
          this.trackPoints[i-1].lat, this.trackPoints[i-1].lng,
          this.trackPoints[i].lat, this.trackPoints[i].lng
        )
      }
      
      // 计算时间范围
      const startTime = this.trackPoints[0].createTime
      const endTime = this.trackPoints[this.trackPoints.length - 1].createTime
      const timeRange = this.formatTimeRange(startTime, endTime)
      
      this.trackInfo = {
        pointCount: this.trackPoints.length,
        totalDistance: totalDistance,
        timeRange: timeRange
      }
    },
    
    calculateDistance(lat1, lng1, lat2, lng2) {
      // 验证输入参数
      if (isNaN(lat1) || isNaN(lng1) || isNaN(lat2) || isNaN(lng2)) {
        return 0
      }
      
      // 如果是同一个点，返回0
      if (lat1 === lat2 && lng1 === lng2) {
        return 0
      }
      
      const rad = Math.PI / 180
      const a = rad * lat1
      const b = rad * lat2
      const c = rad * (lng2 - lng1)
      const d = Math.sin(a) * Math.sin(b) + Math.cos(a) * Math.cos(b) * Math.cos(c)
      
      // 限制d在[-1, 1]范围内，避免Math.acos返回NaN
      const clampedD = Math.max(-1, Math.min(1, d))
      const distance = 6371 * Math.acos(clampedD)
      
      // 如果结果仍然是NaN，返回0
      return isNaN(distance) ? 0 : distance
    },
    
    formatTimeRange(startTime, endTime) {
      const start = new Date(startTime)
      const end = new Date(endTime)
      const diff = end - start
      
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      
      if (hours > 0) {
        return `${hours}小时${minutes}分钟`
      } else {
        return `${minutes}分钟`
      }
    },
    
    formatTime(timeStr) {
      if (!timeStr) return '--'
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    
    formatDistance(distance) {
      if (distance === null || distance === undefined || isNaN(distance)) {
        return '0.00km'
      }
      return `${distance.toFixed(2)}km`
    },
    
    drawTrack(trackPoints) {
      if (!this.map || !trackPoints || trackPoints.length === 0) {
        console.warn('No map or track points available')
        return
      }
      
      console.log('Drawing track with', trackPoints.length, 'points')
      
      // 清除旧轨迹
      this.clearTrack()
      
      // 强制调整地图大小确保显示正常
      setTimeout(() => {
        if (this.map) {
          console.log('Resizing map before drawing')
          this.map.resize()
        }
      }, 100)
      
      // 绘制轨迹线
      const path = trackPoints.map(p => [p.lng, p.lat])
      console.log('Track path:', path)
      
      this.polyline = new window.AMap.Polyline({
        path: path,
        strokeColor: '#2f54eb',
        strokeWeight: 4,
        strokeOpacity: 0.8,
        strokeDasharray: [10, 5], // 虚线样式
        lineJoin: 'round',
        showDir: true
      })
      this.map.add(this.polyline)
      console.log('Polyline added to map')
      
      // 绘制起点
      const startPoint = trackPoints[0]
      const startMarker = new window.AMap.Marker({
        position: [startPoint.lng, startPoint.lat],
        icon: new window.AMap.Icon({
          size: new window.AMap.Size(25, 34),
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png',
          imageSize: new window.AMap.Size(25, 34)
        }),
        offset: new window.AMap.Pixel(-12, -34),
        extData: { type: 'start', ...startPoint }
      })
      
      // 绘制终点
      const endPoint = trackPoints[trackPoints.length - 1]
      const endMarker = new window.AMap.Marker({
        position: [endPoint.lng, endPoint.lat],
        icon: new window.AMap.Icon({
          size: new window.AMap.Size(25, 34),
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png',
          imageSize: new window.AMap.Size(25, 34)
        }),
        offset: new window.AMap.Pixel(-12, -34),
        extData: { type: 'end', ...endPoint }
      })
      
      this.markers.push(startMarker, endMarker)
      
      // 绘制关键轨迹点（每10个点显示一个）
      const interval = Math.max(1, Math.floor(trackPoints.length / 15))
      for (let i = 0; i < trackPoints.length; i += interval) {
        const point = trackPoints[i]
        const marker = new window.AMap.Marker({
          position: [point.lng, point.lat],
          icon: new window.AMap.Icon({
            size: new window.AMap.Size(10, 10),
            image: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="10" height="10"%3E%3Ccircle cx="5" cy="5" r="4" fill="%231890ff" stroke="%23fff" stroke-width="1"/%3E%3C/svg%3E',
            imageSize: new window.AMap.Size(10, 10)
          }),
          offset: new window.AMap.Pixel(-5, -5),
          extData: { type: 'track', index: i, ...point }
        })
        this.markers.push(marker)
      }
      
      // 创建移动的车辆标记（初始隐藏）
      this.movingMarker = new window.AMap.Marker({
        position: [startPoint.lng, startPoint.lat],
        icon: new window.AMap.Icon({
          size: new window.AMap.Size(48, 48),
          image: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 48 48"%3E%3Cdefs%3E%3Cfilter id="shadow" x="-50%25" y="-50%25" width="200%25" height="200%25"%3E%3CfeGaussianBlur in="SourceAlpha" stdDeviation="2"/%3E%3CfeOffset dx="0" dy="2" result="offsetblur"/%3E%3CfeFlood flood-color="%23000000" flood-opacity="0.3"/%3E%3CfeComposite in2="offsetblur" operator="in"/%3E%3CfeMerge%3E%3CfeMergeNode/%3E%3CfeMergeNode in="SourceGraphic"/%3E%3C/feMerge%3E%3C/filter%3E%3C/defs%3E%3Cg filter="url(%23shadow)"%3E%3Crect x="14" y="18" width="20" height="14" rx="2" fill="%231890ff"/%3E%3Cpath d="M16 18 L18 12 L30 12 L32 18 Z" fill="%2340a9ff"/%3E%3Crect x="17" y="13" width="5" height="4" rx="1" fill="%23e6f7ff"/%3E%3Crect x="26" y="13" width="5" height="4" rx="1" fill="%23e6f7ff"/%3E%3Ccircle cx="18" cy="32" r="3" fill="%23333"/%3E%3Ccircle cx="18" cy="32" r="1.5" fill="%23666"/%3E%3Ccircle cx="30" cy="32" r="3" fill="%23333"/%3E%3Ccircle cx="30" cy="32" r="1.5" fill="%23666"/%3E%3Ccircle cx="13" cy="22" r="1.5" fill="%23fff176"/%3E%3Ccircle cx="13" cy="28" r="1.5" fill="%23ff5252"/%3E%3C/g%3E%3C/svg%3E',
          imageSize: new window.AMap.Size(48, 48)
        }),
        offset: new window.AMap.Pixel(-24, -24),
        zIndex: 1000
      })
      // 初始不添加到地图，播放时再添加
      // this.map.add(this.movingMarker)
      
      this.map.add(this.markers)
      
      // 添加点击事件
      this.markers.forEach(marker => {
        marker.on('click', (e) => {
          const data = e.target.getExtData()
          this.showPointInfo(data, data.index || 0)
        })
      })
      
      // 延迟调整视野，确保地图渲染完成
      setTimeout(() => {
        console.log('Setting map fit view')
        this.map.setFitView()
        
        // 再次确保地图显示正常
        setTimeout(() => {
          if (this.map) {
            console.log('Final map resize')
            this.map.resize()
          }
        }, 300)
      }, 200)
    },
    
    clearTrack() {
      console.log('Clearing track')
      if (this.polyline) {
        this.map.remove(this.polyline)
        this.polyline = null
      }
      if (this.markers.length > 0) {
        this.map.remove(this.markers)
        this.markers = []
      }
      if (this.movingMarker) {
        this.map.remove(this.movingMarker)
        this.movingMarker = null
      }
      // 重置地图状态
      this.map.clearMap()
    },
    
    showPointInfo(point, index) {
      const content = `
        <div style="padding: 12px; min-width: 200px;">
          <h4 style="margin: 0 0 8px 0; color: #333; font-size: 14px; font-weight: 600;">
            ${point.type === 'start' ? '🚩 起点' : point.type === 'end' ? '🏁 终点' : `📍 轨迹点 #${index + 1}`}
          </h4>
          <div style="font-size: 12px; color: #666; line-height: 1.8;">
            <div style="margin-bottom: 4px;">
              <span style="color: #999;">时间：</span>
              <strong>${this.formatTime(point.createTime)}</strong>
            </div>
            <div style="margin-bottom: 4px;">
              <span style="color: #999;">经度：</span>
              <strong>${point.lng.toFixed(6)}</strong>
            </div>
            <div style="margin-bottom: 4px;">
              <span style="color: #999;">纬度：</span>
              <strong>${point.lat.toFixed(6)}</strong>
            </div>
            ${point.content ? `
              <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #eee;">
                <span style="color: #999;">数据：</span>
                <div style="margin-top: 4px; background: #f5f5f5; padding: 6px; border-radius: 4px; word-break: break-all;">
                  ${point.content}
                </div>
              </div>
            ` : ''}
          </div>
        </div>
      `
      
      const infoWindow = new window.AMap.InfoWindow({
        content: content,
        offset: new window.AMap.Pixel(0, -30)
      })
      
      infoWindow.open(this.map, [point.lng, point.lat])
    },
    
    focusPoint(index) {
      if (index >= 0 && index < this.trackPoints.length) {
        const point = this.trackPoints[index]
        this.currentPointIndex = index
        this.map.setCenter([point.lng, point.lat])
        this.showPointInfo(point, index)
      }
    },
    
    // 播放控制
    togglePlayback() {
      if (this.isPlaying) {
        this.stopPlayback()
      } else {
        this.startPlayback()
      }
    },
    
    startPlayback() {
      if (!this.trackPoints || this.trackPoints.length < 2) {
        this.$message.warning('轨迹点数据不足')
        return
      }
      
      this.isPlaying = true
      this.playback()
    },
    
    stopPlayback() {
      this.isPlaying = false
      if (this.playbackTimer) {
        cancelAnimationFrame(this.playbackTimer)
        this.playbackTimer = null
      }
    },
    
    resetPlayback() {
      this.stopPlayback()
      this.playbackProgress = 0
      this.currentPointIndex = 0
      // 隐藏移动标记 - 从地图移除
      if (this.movingMarker && this.map) {
        this.map.remove(this.movingMarker)
      }
    },
    
    playback() {
      if (!this.isPlaying) return
      
      const totalPoints = this.trackPoints.length
      const progress = this.playbackProgress / 100
      const currentIndex = Math.floor(progress * (totalPoints - 1))
      
      if (currentIndex >= totalPoints - 1) {
        this.stopPlayback()
        this.playbackProgress = 100
        // 移动到终点
        if (this.movingMarker) {
          const endPoint = this.trackPoints[totalPoints - 1]
          this.movingMarker.setPosition([endPoint.lng, endPoint.lat])
        }
        this.$message.success('轨迹播放完成')
        return
      }
      
      // 显示并移动车辆标记到当前点
      const point = this.trackPoints[currentIndex]
      this.currentPointIndex = currentIndex
      
      if (this.movingMarker) {
        // 首次播放时添加到地图
        if (!this.map.getAllOverlays().includes(this.movingMarker)) {
          this.map.add(this.movingMarker)
        }
        
        this.movingMarker.setPosition([point.lng, point.lat])
        
        // 计算车辆角度（朝向下一个点）
        if (currentIndex < totalPoints - 1) {
          const nextPoint = this.trackPoints[currentIndex + 1]
          const angle = this.calculateAngle(
            point.lng, point.lat,
            nextPoint.lng, nextPoint.lat
          )
          this.movingMarker.setAngle(angle)
        }
      }
      
      // 更新进度 (速度可调整,数值越小越慢)
      this.playbackProgress += 0.05
      
      // 继续播放
      this.playbackTimer = requestAnimationFrame(() => {
        this.playback()
      })
    },
    
    // 计算两点之间的角度
    calculateAngle(lng1, lat1, lng2, lat2) {
      const dLng = lng2 - lng1
      const dLat = lat2 - lat1
      let angle = Math.atan2(dLng, dLat) * 180 / Math.PI
      // 调整角度使车头朝向正确方向
      return angle
    },
    
    onProgressChange(value) {
      this.stopPlayback()
      const totalPoints = this.trackPoints.length
      const currentIndex = Math.floor((value / 100) * (totalPoints - 1))
      this.focusPoint(currentIndex)
    },
    
    togglePointsPanel() {
      this.showPointsPanel = !this.showPointsPanel
    },
    
    handleClose() {
      this.stopPlayback()
      if (this.map) {
        this.map.destroy()
        this.map = null
      }
      this.$emit('close')
    }
  }
}
</script>

<style lang="less" scoped>
.map-track-container {
  display: flex;
  height: 100%;
  background: #f0f2f5;
  
  // Left Panel - Track Details
  .left-panel {
    width: 380px;
    background: #fff;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    // Time Selector
    .time-selector {
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;
      background: #1890ff;
      color: #fff;
      
      .selector-title {
        font-size: 13px;
        font-weight: 500;
        margin-bottom: 12px;
        opacity: 0.9;
      }
      
      .btn-group {
        display: flex;
        gap: 8px;
        
        .time-btn {
          flex: 1;
          height: 32px;
          line-height: 32px;
          text-align: center;
          background: rgba(255, 255, 255, 0.2);
          border: 1px solid rgba(255, 255, 255, 0.3);
          border-radius: 4px;
          cursor: pointer;
          transition: all 0.3s;
          font-size: 12px;
          font-weight: 500;
          
          &:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-1px);
          }
          
          &.active {
            background: #fff;
            color: #1890ff;
            border-color: #fff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          }
        }
      }
    }
    
    // Track Statistics
    .track-stats {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
      background: #fafafa;
      
      .stats-row {
        display: flex;
        gap: 16px;
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .stats-item {
          display: flex;
          align-items: center;
          font-size: 12px;
          
          &.full-width {
            flex: 1;
          }
          
          .stats-label {
            color: #666;
            margin-right: 4px;
          }
          
          .stats-value {
            font-weight: 600;
            color: #333;
          }
        }
      }
    }
    
    // Playback Section
    .playback-section {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
      
      .section-title {
        font-size: 14px;
        font-weight: 600;
        color: #333;
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        
        &:before {
          content: '';
          width: 3px;
          height: 14px;
          background: #1890ff;
          margin-right: 8px;
          border-radius: 2px;
        }
      }
      
      .playback-controls {
        .playback-buttons {
          display: flex;
          gap: 8px;
          margin-bottom: 8px;
          
          .ant-btn {
            flex: 1;
            font-size: 12px;
            height: 28px;
            padding: 0 8px;
            
            .anticon {
              font-size: 12px;
            }
          }
        }
        
        .progress-container {
          .ant-slider {
            margin: 0;
            
            .ant-slider-handle {
              width: 12px;
              height: 12px;
              margin-top: -5px;
            }
            
            .ant-slider-track {
              height: 4px;
            }
            
            .ant-slider-rail {
              height: 4px;
            }
          }
        }
      }
    }
    
    // Timeline Section
    .timeline-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      
      .section-title {
        font-size: 14px;
        font-weight: 600;
        color: #333;
        padding: 16px 16px 12px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid #f0f0f0;
        background: #fafafa;
        
        &:before {
          content: '';
          width: 3px;
          height: 14px;
          background: #1890ff;
          margin-right: 8px;
          border-radius: 2px;
        }
        
        .point-count {
          font-size: 12px;
          font-weight: 400;
          color: #999;
          margin-left: auto;
        }
        
        .group-controls {
          display: flex;
          gap: 8px;
          margin-left: 12px;
          
          .ant-btn {
            font-size: 12px;
            padding: 0 4px;
            height: 20px;
          }
        }
      }
      
      .timeline-list {
        flex: 1;
        overflow-y: auto;
        padding: 8px;
        
        &::-webkit-scrollbar {
          width: 6px;
        }
        
        &::-webkit-scrollbar-thumb {
          background: #d9d9d9;
          border-radius: 3px;
          
          &:hover {
            background: #bfbfbf;
          }
        }
        
        // 时间分组样式
        .time-group {
          margin-bottom: 12px;
          border: 1px solid #e8e8e8;
          border-radius: 6px;
          overflow: hidden;
          
          &:last-child {
            margin-bottom: 0;
          }
        }
        
        .group-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 10px 12px;
          background: #f5f5f5;
          cursor: pointer;
          transition: all 0.2s ease;
          
          &:hover {
            background: #e8e8e8;
          }
          
          .group-info {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .group-time {
              font-size: 13px;
              font-weight: 600;
              color: #333;
            }
            
            .group-duration {
              font-size: 11px;
              color: #999;
            }
          }
          
          .group-stats {
            .point-count {
              font-size: 11px;
              color: #666;
              background: #fff;
              padding: 2px 6px;
              border-radius: 10px;
              border: 1px solid #d9d9d9;
            }
          }
        }
        
        .group-points {
          background: #fafafa;
          border-top: 1px solid #e8e8e8;
          max-height: 300px;
          overflow-y: auto;
          
          &::-webkit-scrollbar {
            width: 4px;
          }
          
          &::-webkit-scrollbar-thumb {
            background: #ccc;
            border-radius: 2px;
          }
        }
        
        .timeline-item {
          display: flex;
          padding: 12px;
          margin-bottom: 8px;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s;
          background: #fafafa;
          border: 2px solid transparent;
          position: relative;
          
          &:hover {
            background: #f0f8ff;
            transform: translateX(4px);
          }
          
          &.active {
            background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
            border-color: #1890ff;
            box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
          }
          
          &.is-start {
            border-left: 3px solid #52c41a;
          }
          
          &.is-end {
            border-left: 3px solid #f5222d;
          }
          
          .timeline-marker {
            width: 24px;
            display: flex;
            align-items: flex-start;
            justify-content: center;
            padding-top: 2px;
            margin-right: 12px;
            flex-shrink: 0;
            
            .marker-dot {
              width: 8px;
              height: 8px;
              background: #1890ff;
              border-radius: 50%;
              border: 2px solid #fff;
              box-shadow: 0 0 0 2px #e6f7ff;
            }
          }
          
          .timeline-content {
            flex: 1;
            min-width: 0;
            
            .timeline-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 6px;
              
              .point-label {
                font-size: 13px;
                font-weight: 600;
                color: #333;
              }
              
              .point-time {
                font-size: 11px;
                color: #999;
              }
            }
            
            .timeline-coords {
              font-size: 12px;
              color: #666;
              margin-bottom: 4px;
              display: flex;
              align-items: center;
            }
            
            .timeline-data {
              font-size: 11px;
              color: #999;
              display: flex;
              align-items: flex-start;
              margin-top: 6px;
              padding-top: 6px;
              border-top: 1px dashed #e8e8e8;
              
              .data-preview {
                flex: 1;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }
            }
          }
        }
      }
    }
  }
  
  // Right Panel - Map
  .right-panel {
    flex: 1;
    position: relative;
    overflow: hidden;
    
    .map-container {
      width: 100%;
      height: 100%;
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
    }
    
    // Map Playback Controls
    .map-playback-controls {
      position: absolute;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(255, 255, 255, 0.95);
      border-radius: 20px;
      padding: 10px 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(10px);
      z-index: 100;
      min-width: 300px;
      
      .playback-buttons {
        display: flex;
        gap: 8px;
        margin-bottom: 8px;
        justify-content: center;
        
        .ant-btn {
          flex: 1;
          font-size: 12px;
          height: 28px;
          padding: 0 12px;
          
          .anticon {
            font-size: 12px;
          }
        }
      }
      
      .progress-container {
        .ant-slider {
          margin: 0;
          
          .ant-slider-handle {
            width: 12px;
            height: 12px;
            margin-top: -5px;
          }
          
          .ant-slider-track {
            height: 4px;
          }
          
          .ant-slider-rail {
            height: 4px;
          }
        }
      }
    }
  }
}

// Ant Design overrides
::v-deep .ant-modal-body {
  padding: 0 !important;
}

::v-deep .ant-empty {
  padding: 40px 20px;
}
</style>