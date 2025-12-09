<template>
  <div class="map-track-container">
    <div class="map-header">
      <div class="header-left">
        <h4>{{ title }}</h4>
      </div>
      <div class="map-info">
        <span class="info-item">
          <i class="info-dot"></i>
          数据源 <strong>{{ dataList.length }}</strong> 条
        </span>
        <span class="info-item">
          <i class="info-dot"></i>
          轨迹点 <strong>{{ trackPoints.length }}</strong> 个
        </span>
        <span class="info-item" v-if="trackPoints.length > 0">
          <i class="info-dot"></i>
          {{ timeRange }}
        </span>
      </div>
    </div>
    <div ref="mapContainer" class="map-container" v-show="!loading && !errorMsg"></div>
    <div v-if="loading" class="map-loading">
      <a-spin tip="地图加载中..." />
    </div>
    <div v-if="errorMsg" class="map-error">
      <a-icon type="exclamation-circle" style="font-size: 48px; color: #ff4d4f; margin-bottom: 16px;" />
      <p>{{ errorMsg }}</p>
      <a-button type="primary" @click="retryInit">重试</a-button>
    </div>
  </div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader'
import mapConfig from '@/config/map.config'

export default {
  name: 'MapTrack',
  props: {
    // 轨迹数据列表
    dataList: {
      type: Array,
      default: () => []
    },
    // 地图标题
    title: {
      type: String,
      default: '设备轨迹'
    },
    // 地图高度
    height: {
      type: String,
      default: '500px'
    }
  },
  data() {
    return {
      map: null,
      loading: false,
      errorMsg: '',
      trackPoints: [],
      polyline: null,
      markers: [],
      mapLoaded: false
    }
  },
  computed: {
    timeRange() {
      if (this.trackPoints.length === 0) return ''
      const first = this.trackPoints[0]
      const last = this.trackPoints[this.trackPoints.length - 1]
      return `${this.formatTime(first.createTime)} - ${this.formatTime(last.createTime)}`
    }
  },
  watch: {
    dataList: {
      handler(newVal) {
        this.parseTrackData(newVal)
        if (this.mapLoaded && this.map) {
          this.drawTrack()
        }
      },
      deep: true
    }
  },
  mounted() {
    // 等待父组件Modal完全显示后再初始化
    this.$nextTick(() => {
      setTimeout(() => {
        this.initMap()
      }, 500)
    })
  },
  beforeDestroy() {
    this.destroyMap()
  },
  methods: {
    /**
     * 初始化高德地图
     */
    async initMap() {
      if (!this.$refs.mapContainer) {
        this.errorMsg = '地图容器不存在'
        return
      }

      this.loading = true
      this.errorMsg = ''

      try {
        const AMap = await AMapLoader.load({
          key: mapConfig.amap.key || '',
          version: '2.0',
          plugins: ['AMap.Polyline', 'AMap.Marker', 'AMap.InfoWindow']
        })

        this.map = new AMap.Map(this.$refs.mapContainer, {
          zoom: 13,
          center: [120.184074, 30.188022],
          viewMode: '3D',
          pitch: 0,
          resizeEnable: true
        })

        this.mapLoaded = true
        this.loading = false

        // 解析并绘制轨迹
        this.parseTrackData(this.dataList)
        if (this.trackPoints.length > 0) {
          setTimeout(() => {
            this.drawTrack()
          }, 200)
        }
      } catch (e) {
        this.loading = false
        this.errorMsg = '地图加载失败: ' + (e.message || '未知错误')
      }
    },

    /**
     * 重试初始化
     */
    retryInit() {
      this.errorMsg = ''
      this.mapLoaded = false
      if (this.map) {
        this.map.destroy()
        this.map = null
      }
      this.$nextTick(() => {
        setTimeout(() => {
          this.initMap()
        }, 300)
      })
    },

    /**
     * 解析轨迹数据
     */
    parseTrackData(dataList) {
      if (!dataList || dataList.length === 0) {
        this.trackPoints = []
        return
      }

      this.trackPoints = dataList
        .filter(item => item && item.ext2) // 确保有坐标数据
        .map(item => {
          try {
            const coords = item.ext2.split(',')
            
            if (coords.length >= 2) {
              // 去除空格并转换
              const lng = parseFloat(coords[0].trim())
              const lat = parseFloat(coords[1].trim())
              
              // 验证坐标是否为有效数字且在合理范围内
              if (!isNaN(lng) && !isNaN(lat) && 
                  lng !== 0 && lat !== 0 &&
                  lng >= -180 && lng <= 180 && 
                  lat >= -90 && lat <= 90) {
                return {
                  lng: lng,
                  lat: lat,
                  createTime: item.createTime,
                  deviceName: item.deviceName || '未知设备',
                  content: item.content || ''
                }
              }
            }
          } catch (e) {
            // 生产环境保留错误日志
            console.error('坐标解析失败:', item.ext2, e)
          }
          return null
        })
        .filter(point => point !== null)
        .sort((a, b) => new Date(a.createTime) - new Date(b.createTime)) // 按时间正序排列
    },

    /**
     * 绘制轨迹
     */
    drawTrack() {
      if (!this.map || !this.mapLoaded) {
        return
      }
      
      if (this.trackPoints.length === 0) {
        this.$message.info('暂无轨迹数据')
        return
      }

      // 清除之前的轨迹和标记
      this.clearTrack()

      const AMap = window.AMap
      
      // 验证数据有效性
      const validPoints = this.trackPoints.filter(point => {
        return !isNaN(point.lng) && !isNaN(point.lat) && 
          point.lng !== 0 && point.lat !== 0 &&
          Math.abs(point.lng) <= 180 && Math.abs(point.lat) <= 90
      })
      
      if (validPoints.length === 0) {
        this.errorMsg = '所有轨迹点坐标无效'
        return
      }
      
      try {
        const path = validPoints.map(point => [point.lng, point.lat])

        // 绘制轨迹线
        this.polyline = new AMap.Polyline({
          path: path,
          strokeColor: '#1890ff', // 轨迹颜色
          strokeWeight: 6,
          strokeOpacity: 0.8,
          showDir: true, // 显示方向箭头
          lineJoin: 'round'
        })
        this.map.add(this.polyline)

        // 添加起点标记（绿色）
        const startPoint = validPoints[0]
        const startMarker = new AMap.Marker({
          position: [startPoint.lng, startPoint.lat],
          icon: new AMap.Icon({
            size: new AMap.Size(32, 32),
            image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
            imageSize: new AMap.Size(32, 32)
          }),
          offset: new AMap.Pixel(-16, -32),
          title: '起点'
        })

        // 添加起点信息窗体
        const startInfo = new AMap.InfoWindow({
          content: `<div style="padding: 10px;">
            <h4 style="margin: 0 0 5px 0; color: #52c41a;">起点</h4>
            <p style="margin: 3px 0;">设备：${startPoint.deviceName}</p>
            <p style="margin: 3px 0;">时间：${this.formatTime(startPoint.createTime)}</p>
            <p style="margin: 3px 0;">坐标：${startPoint.lng}, ${startPoint.lat}</p>
          </div>`
        })
        startMarker.on('click', () => {
          startInfo.open(this.map, [startPoint.lng, startPoint.lat])
        })
        this.markers.push(startMarker)
        this.map.add(startMarker)

        // 添加终点标记（红色）
        const endPoint = validPoints[validPoints.length - 1]
        const endMarker = new AMap.Marker({
          position: [endPoint.lng, endPoint.lat],
          icon: new AMap.Icon({
            size: new AMap.Size(32, 32),
            image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
            imageSize: new AMap.Size(32, 32)
          }),
          offset: new AMap.Pixel(-16, -32),
          title: '终点'
        })

        // 添加终点信息窗体
        const endInfo = new AMap.InfoWindow({
          content: `<div style="padding: 10px;">
            <h4 style="margin: 0 0 5px 0; color: #f5222d;">终点</h4>
            <p style="margin: 3px 0;">设备：${endPoint.deviceName}</p>
            <p style="margin: 3px 0;">时间：${this.formatTime(endPoint.createTime)}</p>
            <p style="margin: 3px 0;">坐标：${endPoint.lng}, ${endPoint.lat}</p>
          </div>`
        })
        endMarker.on('click', () => {
          endInfo.open(this.map, [endPoint.lng, endPoint.lat])
        })
        this.markers.push(endMarker)
        this.map.add(endMarker)

        // 自动调整视野以显示完整轨迹
        this.map.setFitView()
      } catch (e) {
        this.errorMsg = '绘制轨迹失败: ' + (e.message || '未知错误')
      }
    },

    /**
     * 清除轨迹和标记
     */
    clearTrack() {
      if (this.polyline) {
        this.map.remove(this.polyline)
        this.polyline = null
      }
      if (this.markers.length > 0) {
        this.map.remove(this.markers)
        this.markers = []
      }
    },

    /**
     * 销毁地图
     */
    destroyMap() {
      if (this.map) {
        this.clearTrack()
        this.map.destroy()
        this.map = null
      }
    },

    /**
     * 格式化时间
     */
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      const second = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
  }
}
</script>

<style lang="less" scoped>
.map-track-container {
  position: relative;
  width: 100%;
  
  .map-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 20px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;
    border-radius: 2px 2px 0 0;

    .header-left {
      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    .map-info {
      display: flex;
      gap: 16px;
      align-items: center;

      .info-item {
        display: flex;
        align-items: center;
        font-size: 12px;
        color: #595959;
        white-space: nowrap;
        padding: 4px 10px;
        background: #fff;
        border: 1px solid #e8e8e8;
        border-radius: 4px;
        transition: all 0.3s;

        &:hover {
          border-color: #d9d9d9;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
        }

        .info-dot {
          display: inline-block;
          width: 5px;
          height: 5px;
          background: #1890ff;
          border-radius: 50%;
          margin-right: 6px;
        }

        strong {
          font-weight: 600;
          color: #262626;
          margin: 0 3px;
          font-size: 13px;
        }
      }
    }
  }

  .map-container {
    width: 100%;
    height: v-bind(height);
    position: relative;
  }

  .map-loading {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(255, 255, 255, 0.9);
    padding: 30px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .map-error {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    background: rgba(255, 255, 255, 0.95);
    padding: 40px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);

    p {
      color: #666;
      font-size: 14px;
      margin-bottom: 20px;
      max-width: 300px;
    }
  }
}
</style>
