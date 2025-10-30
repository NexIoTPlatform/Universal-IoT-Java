<template>
  <div class="dashboard-container">
    <!-- 核心统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="card-header">
          <div class="card-icon">
            <a-icon type="deployment-unit"/>
          </div>
          <div class="card-trend up">
            <a-icon type="arrow-up"/>
            <span>{{ deviceGrowth }}%</span>
          </div>
        </div>
        <div class="card-content">
          <div class="card-number">
            <CountTo :endVal="dashboardData.totalDevices" class="count-animation"/>
          </div>
          <div class="card-label">{{ $t('dashboard.total.devices') }}</div>
          <div class="card-sublabel">{{ dashboardData.totalDevices }} {{
              $t('dashboard.devices.unit')
            }}
          </div>
        </div>
        <div class="card-glow"></div>
      </div>

      <div class="stat-card success">
        <div class="card-header">
          <div class="card-icon">
            <a-icon type="check-circle"/>
          </div>
          <div class="card-trend up">
            <a-icon type="arrow-up"/>
            <span>{{ onlineGrowth }}%</span>
          </div>
        </div>
        <div class="card-content">
          <div class="card-number">
            <CountTo :endVal="dashboardData.onlineDevices" class="count-animation"/>
          </div>
          <div class="card-label">{{ $t('dashboard.online.devices') }}</div>
          <div class="card-sublabel">{{ dashboardData.onlineDevices }} {{
              $t('dashboard.active.unit')
            }}
          </div>
        </div>
        <div class="card-glow"></div>
      </div>

      <div class="stat-card warning">
        <div class="card-header">
          <div class="card-icon">
            <a-icon type="disconnect"/>
          </div>
          <div class="card-trend down">
            <a-icon type="arrow-down"/>
            <span>{{ offlineGrowth }}%</span>
          </div>
        </div>
        <div class="card-content">
          <div class="card-number">
            <CountTo :endVal="dashboardData.offlineDevices" class="count-animation"/>
          </div>
          <div class="card-label">{{ $t('dashboard.offline.devices') }}</div>
          <div class="card-sublabel">{{ dashboardData.offlineDevices }} {{
              $t('dashboard.inactive.unit')
            }}
          </div>
        </div>
        <div class="card-glow"></div>
      </div>

      <div class="stat-card info">
        <div class="card-header">
          <div class="card-icon">
            <a-icon type="percentage"/>
          </div>
          <div class="card-trend up">
            <a-icon type="arrow-up"/>
            <span>{{ onlineRateGrowth }}%</span>
          </div>
        </div>
        <div class="card-content">
          <div class="card-number">
            <CountTo :endVal="dashboardData.onlineRate" :decimals="1" class="count-animation"/>
          </div>
          <div class="card-label">{{ $t('dashboard.online.rate') }}</div>
          <div class="card-sublabel">{{ dashboardData.onlineRate }}{{
              $t('dashboard.percent.unit')
            }} {{ $t('dashboard.online.devices') }}
          </div>
        </div>
        <div class="card-glow"></div>
      </div>
    </div>

    <!-- 图表和告警区域 -->
    <!-- <a-row :gutter="24" class="charts-section">
      <a-col :span="16">
        <TitleBox title="设备状态趋势" class="chart-card">
          <template #extra>
            <a-radio-group v-model="trendPeriod" size="small">
              <a-radio-button value="24h">24小时</a-radio-button>
              <a-radio-button value="7d">7天</a-radio-button>
              <a-radio-button value="30d">30天</a-radio-button>
            </a-radio-group>
          </template>
          <div class="chart-container">
            <div id="deviceTrendChart" class="chart"></div>
          </div>
        </TitleBox>
      </a-col>
      <a-col :span="8">
        <TitleBox title="实时告警" class="alert-card">
          <template #extra>
            <a-badge :count="alerts.length" :offset="[10, 0]">
              <a-button type="link" size="small" @click="viewAllAlerts">查看全部</a-button>
            </a-badge>
          </template>
          <div class="alerts-container">
            <div v-if="alerts.length === 0" class="no-alerts">
              <a-icon type="check-circle" />
              <p>暂无告警信息</p>
            </div>
            <div v-else class="alerts-list">
              <div
                v-for="alert in alerts.slice(0, 5)"
                :key="alert.id"
                :class="['alert-item', alert.level]"
              >
                <div class="alert-icon">
                  <a-icon :type="getAlertIcon(alert.type)" />
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.message }}</div>
                  <div class="alert-meta">
                    <span class="alert-device">{{ alert.deviceName }}</span>
                    <span class="alert-time">{{ formatTime(alert.time) }}</span>
                  </div>
                </div>
                <div class="alert-level">
                  <a-tag :color="getAlertColor(alert.level)">{{ getAlertText(alert.level) }}</a-tag>
                </div>
              </div>
            </div>
          </div>
        </TitleBox>
      </a-col>
    </a-row> -->

    <!-- 平台概况 -->
    <TitleBox :title="$t('dashboard.platform.overview')" class="platform-overview">
      <div class="overview-content">
        <div class="overview-features">
          <div class="feature-item">
            <div class="feature-icon">
              <a-icon type="api"/>
            </div>
            <div class="feature-content">
              <h4>{{ $t('platform.feature.multi.protocol') }}</h4>
              <p>{{ $t('platform.feature.multi.protocol.desc') }}</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <a-icon type="cluster"/>
            </div>
            <div class="feature-content">
              <h4>{{ $t('platform.feature.massive.devices') }}</h4>
              <p>{{ $t('platform.feature.massive.devices.desc') }}</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <a-icon type="thunderbolt"/>
            </div>
            <div class="feature-content">
              <h4>{{ $t('platform.feature.real.time') }}</h4>
              <p>{{ $t('platform.feature.real.time.desc') }}</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <a-icon type="safety-certificate"/>
            </div>
            <div class="feature-content">
              <h4>{{ $t('platform.feature.security') }}</h4>
              <p>{{ $t('platform.feature.security.desc') }}</p>
            </div>
          </div>
        </div>
        <div class="overview-cta">
          <a-button type="primary" size="large"
                    @click="handleActionClick('/product/product', $event)">
            <a-icon type="rocket"/>
            {{ $t('platform.start.using') }}
          </a-button>
        </div>
      </div>
    </TitleBox>

    <!-- 快捷操作区域 -->
    <TitleBox :title="$t('dashboard.quick.actions')" class="quick-actions-card">
      <div class="quick-actions-grid">
        <div class="quick-action-item" @click="handleActionClick('/app/applicationList', $event)"
             style="cursor: pointer; pointer-events: auto;">
          <div class="action-icon primary">
            <a-icon type="plus-circle"/>
          </div>
          <div class="action-content">
            <div class="action-title">{{ $t('quick.action.create.app') }}</div>
            <div class="action-desc">{{ $t('quick.action.create.app.desc') }}</div>
          </div>
          <div class="action-arrow">
            <a-icon type="right"/>
          </div>
        </div>

        <div class="quick-action-item" @click="handleActionClick('/product/product', $event)"
             style="cursor: pointer; pointer-events: auto;">
          <div class="action-icon secondary">
            <a-icon type="plus-square"/>
          </div>
          <div class="action-content">
            <div class="action-title">{{ $t('quick.action.create.product') }}</div>
            <div class="action-desc">{{ $t('quick.action.create.product.desc') }}</div>
          </div>
          <div class="action-arrow">
            <a-icon type="right"/>
          </div>
        </div>

        <div class="quick-action-item" @click="handleActionClick('/rule/flow', $event)"
             style="cursor: pointer; pointer-events: auto;">
          <div class="action-icon success">
            <a-icon type="branches"/>
          </div>
          <div class="action-content">
            <div class="action-title">{{ $t('quick.action.rule.engine') }}</div>
            <div class="action-desc">{{ $t('quick.action.rule.engine.desc') }}</div>
          </div>
          <div class="action-arrow">
            <a-icon type="right"/>
          </div>
        </div>

        <div class="quick-action-item" @click="handleActionClick('/platform/device', $event)"
             style="cursor: pointer; pointer-events: auto;">
          <div class="action-icon warning">
            <a-icon type="monitor"/>
          </div>
          <div class="action-content">
            <div class="action-title">{{ $t('quick.action.device.monitor') }}</div>
            <div class="action-desc">{{ $t('quick.action.device.monitor.desc') }}</div>
          </div>
          <div class="action-arrow">
            <a-icon type="right"/>
          </div>
        </div>
      </div>
    </TitleBox>

    <!-- 常见问题 -->
    <TitleBox title="常见问题" class="faq-card">
      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="如何理解产品和设备的关系？">
          <p>在物联网平台中，<strong>产品</strong>是设备的抽象模型，定义了设备的功能和属性。</p>
          <p><strong>设备</strong>是产品的具体实例，每个设备都属于某个产品类型。</p>
          <p>例如：智能温度计是一个产品，而具体的每个温度计设备就是该产品的实例。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="平台支持哪些物联网模型？">
          <p>平台支持多种主流的物联网数据模型：</p>
          <ul>
            <li><strong>属性（Properties）</strong>：设备的状态数据，如温度、湿度等</li>
            <li><strong>事件（Events）</strong>：设备主动上报的信息，如告警、故障等</li>
            <li><strong>服务（Services）</strong>：平台可以调用的设备功能，如开关控制等</li>
            <li><strong>标签（Tags）</strong>：设备的静态信息，用于分类和检索</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="设备如何接入？">
          <p>设备接入方式需要以下步骤：</p>
          <ul>
            <li><strong>创建产品</strong>：定义设备类型和功能模型</li>
            <li><strong>添加设备</strong>：在产品下创建具体的设备实例</li>
            <li><strong>获取认证信息</strong>：获取设备连接所需的密钥或证书</li>
            <li><strong>设备连接</strong>：使用MQTT、CoAP等协议连接到平台</li>
            <li><strong>数据通信</strong>：按照定义的数据格式进行数据交互</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="平台支持哪些通信协议？">
          <p>平台支持多种物联网通信协议：</p>
          <ul>
            <li><strong>MQTT</strong>：轻量级消息传输协议，适用于大多数IoT场景</li>
            <li><strong>CoAP</strong>：受限应用协议，适用于资源受限的设备</li>
            <li><strong>HTTP/HTTPS</strong>：标准Web协议，适用于简单的数据上报</li>
            <li><strong>WebSocket</strong>：双向通信协议，适用于实时数据交互</li>
            <li><strong>TCP/UDP</strong>：支持自定义协议的透传模式</li>
          </ul>
        </a-collapse-panel>
      </a-collapse>
    </TitleBox>
  </div>
</template>

<script>
import TitleBox from './components/TitleBox.vue'
import CountTo from 'vue-count-to'
import {getDashboardOverview} from '@/api/dashboard'
import * as echarts from 'echarts'

export default {
  name: 'DashboardIndex',
  components: {
    TitleBox,
    CountTo
  },
  data() {
    return {
      trendPeriod: '24h',
      dashboardData: {
        totalDevices: 0,
        onlineDevices: 0,
        offlineDevices: 0,
        onlineRate: 0
      },
      deviceGrowth: 0,
      onlineGrowth: 0,
      offlineGrowth: 0,
      onlineRateGrowth: 0,
      alerts: [
        {
          id: 1,
          type: 'device_offline',
          deviceName: '温度传感器TH-001',
          message: '设备离线超过30分钟',
          level: 'warning',
          time: new Date(Date.now() - 1800000).toISOString()
        },
        {
          id: 2,
          type: 'high_temperature',
          deviceName: '环境监测器ENV-002',
          message: '温度异常，当前值45°C',
          level: 'error',
          time: new Date(Date.now() - 900000).toISOString()
        },
        {
          id: 3,
          type: 'low_battery',
          deviceName: '智能水表WM-003',
          message: '电池电量低于10%',
          level: 'warning',
          time: new Date(Date.now() - 600000).toISOString()
        }
      ],
      chartInstance: null,
      resizeHandler: null
    }
  },
  computed: {},
  created() {
    this.loadData()
  },
  mounted() {
    // 延迟初始化图表，确保DOM完全渲染
    this.$nextTick(() => {
      setTimeout(() => {
        this.initChart()
      }, 100)
    })

    // 监听页面可见性变化
    document.addEventListener('visibilitychange', this.handleVisibilityChange)
  },
  activated() {
    // 如果组件被keep-alive缓存，激活时重新调整图表尺寸
    if (this.chartInstance) {
      this.$nextTick(() => {
        setTimeout(() => {
          this.chartInstance.resize()
        }, 100)
      })
    }
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
    if (this.resizeHandler) {
      window.removeEventListener('resize', this.resizeHandler)
    }
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)
  },
  methods: {
    async loadData() {
      try {
        const res = await getDashboardOverview()
        console.log('Full API response:', res)
        console.log('Dashboard overview response:', res.data)

        let data
        if (res && res.data) {
          data = res.data
        } else if (res) {
          // 如果数据直接在res中
          data = res
        } else {
          console.error('No data received from API')
          return
        }

        console.log('Processing data:', data)

        // 更新仪表盘数据 - 使用$set确保响应式
        this.$set(this.dashboardData, 'totalDevices', parseInt(data.deviceStats?.totalDevices) || 0)
        this.$set(this.dashboardData, 'onlineDevices',
          parseInt(data.deviceStats?.onlineDevices) || 0)
        this.$set(this.dashboardData, 'offlineDevices',
          parseInt(data.deviceStats?.offlineDevices) || 0)
        this.$set(this.dashboardData, 'onlineRate', parseFloat(data.deviceStats?.onlineRate) || 0)

        console.log('Parsed dashboard data:', this.dashboardData)

        // 计算增长率（这里可以根据历史数据计算，暂时使用固定值）
        this.deviceGrowth = 12.5
        this.onlineGrowth = 8.3
        this.offlineGrowth = -5.2
        this.onlineRateGrowth = 2.1

        // 强制更新视图
        this.$forceUpdate()

        console.log('Updated dashboard data:', this.dashboardData)
      } catch (error) {
        console.error('加载仪表盘数据失败:', error)
        // 使用静态数据作为fallback
        this.$set(this.dashboardData, 'totalDevices', 21)
        this.$set(this.dashboardData, 'onlineDevices', 14)
        this.$set(this.dashboardData, 'offlineDevices', 7)
        this.$set(this.dashboardData, 'onlineRate', 66.67)
        this.deviceGrowth = 12.5
        this.onlineGrowth = 8.3
        this.offlineGrowth = -5.2
        this.onlineRateGrowth = 2.1
      }
    },
    initChart() {
      const chartDom = document.getElementById('deviceTrendChart')
      if (!chartDom) {
        console.warn('Chart container not found')
        return
      }

      // 确保容器有明确的尺寸
      const containerRect = chartDom.getBoundingClientRect()
      if (containerRect.width === 0 || containerRect.height === 0) {
        console.warn('Chart container has no size, retrying...')
        setTimeout(() => this.initChart(), 200)
        return
      }

      // 如果已存在实例，先销毁
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }

      this.chartInstance = echarts.init(chartDom)

      // 生成模拟数据
      const hours = []
      const onlineData = []
      const offlineData = []

      for (let i = 23; i >= 0; i--) {
        const hour = new Date(Date.now() - i * 60 * 60 * 1000).getHours()
        hours.push(hour + ':00')
        onlineData.push(Math.floor(Math.random() * 200) + 800)
        offlineData.push(Math.floor(Math.random() * 100) + 150)
      }

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          }
        },
        legend: {
          data: ['在线设备', '离线设备'],
          top: 10,
          right: 20
        },
        grid: {
          left: '8%',
          right: '8%',
          bottom: '10%',
          top: '15%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: hours,
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            axisLabel: {
              color: '#666',
              fontSize: 11
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            axisLabel: {
              color: '#666',
              fontSize: 11
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0'
              }
            }
          }
        ],
        series: [
          {
            name: '在线设备',
            type: 'line',
            smooth: true,
            lineStyle: {
              width: 3,
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: '#4facfe'
                }, {
                  offset: 1, color: '#00f2fe'
                }]
              }
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(79, 172, 254, 0.3)'
                }, {
                  offset: 1, color: 'rgba(0, 242, 254, 0.1)'
                }]
              }
            },
            emphasis: {
              focus: 'series'
            },
            data: onlineData
          },
          {
            name: '离线设备',
            type: 'line',
            smooth: true,
            lineStyle: {
              width: 3,
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: '#fa709a'
                }, {
                  offset: 1, color: '#fee140'
                }]
              }
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(250, 112, 154, 0.3)'
                }, {
                  offset: 1, color: 'rgba(254, 225, 64, 0.1)'
                }]
              }
            },
            emphasis: {
              focus: 'series'
            },
            data: offlineData
          }
        ]
      }

      this.chartInstance.setOption(option, true)

      // 立即调整一次尺寸
      setTimeout(() => {
        if (this.chartInstance) {
          this.chartInstance.resize()
        }
      }, 50)

      // 响应式调整
      if (!this.resizeHandler) {
        this.resizeHandler = () => {
          if (this.chartInstance) {
            this.chartInstance.resize()
          }
        }
        window.addEventListener('resize', this.resizeHandler)
      }
    },
    navigateTo(path) {
      console.log('Navigating to:', path)
      this.$router.push(path).catch(err => {
        console.log('Navigation handled:', err.name)
      })
    },
    viewAllAlerts() {
      this.$router.push('/alerts')
    },
    getAlertIcon(type) {
      const icons = {
        device_offline: 'disconnect',
        high_temperature: 'fire',
        low_battery: 'battery',
        network_error: 'wifi',
        data_anomaly: 'warning'
      }
      return icons[type] || 'exclamation-circle'
    },
    getAlertColor(level) {
      const colors = {
        info: 'blue',
        warning: 'orange',
        error: 'red'
      }
      return colors[level] || 'default'
    },
    getAlertText(level) {
      const texts = {
        info: '信息',
        warning: '警告',
        error: '严重'
      }
      return texts[level] || '未知'
    },
    formatTime(time) {
      const now = new Date()
      const alertTime = new Date(time)
      const diff = now - alertTime
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(minutes / 60)

      if (minutes < 1) {
        return '刚刚'
      }
      if (minutes < 60) {
        return `${minutes}分钟前`
      }
      if (hours < 24) {
        return `${hours}小时前`
      }
      return alertTime.toLocaleDateString()
    },
    queryDoc(queryName) {
      this.$bus.$emit('resizeabler', queryName)
    },
    handleCardClick(path, event) {
      console.log('Card clicked:', path, event)
      this.navigateTo(path)
    },
    handleActionClick(path, event) {
      console.log('Action clicked:', path, event)
      this.navigateTo(path)
    },
    updateChartData() {
      if (!this.chartInstance) {
        return
      }

      // 根据时间段生成不同的数据
      let dataPoints = 24
      let timeUnit = 'hour'

      switch (this.trendPeriod) {
        case '24h':
          dataPoints = 24
          timeUnit = 'hour'
          break
        case '7d':
          dataPoints = 7
          timeUnit = 'day'
          break
        case '30d':
          dataPoints = 30
          timeUnit = 'day'
          break
      }

      const labels = []
      const onlineData = []
      const offlineData = []

      for (let i = dataPoints - 1; i >= 0; i--) {
        let label
        if (timeUnit === 'hour') {
          const hour = new Date(Date.now() - i * 60 * 60 * 1000).getHours()
          label = hour + ':00'
        } else {
          const date = new Date(Date.now() - i * 24 * 60 * 60 * 1000)
          label = (date.getMonth() + 1) + '/' + date.getDate()
        }

        labels.push(label)
        onlineData.push(Math.floor(Math.random() * 200) + 800)
        offlineData.push(Math.floor(Math.random() * 100) + 150)
      }

      // 更新图表数据
      this.chartInstance.setOption({
        xAxis: {
          data: labels
        },
        series: [
          {
            data: onlineData
          },
          {
            data: offlineData
          }
        ]
      })

      // 确保图表正确渲染
      setTimeout(() => {
        if (this.chartInstance) {
          this.chartInstance.resize()
        }
      }, 100)
    },
    handleVisibilityChange() {
      if (document.visibilityState === 'visible' && this.chartInstance) {
        console.log('Page became visible, resizing chart')
        setTimeout(() => {
          if (this.chartInstance) {
            this.chartInstance.resize()
          }
        }, 100)
      }
    }
  },
  watch: {
    trendPeriod: {
      handler(newVal) {
        console.log('Trend period changed to:', newVal)
        // 重新初始化图表数据
        if (this.chartInstance) {
          this.updateChartData()
        }
      }
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-container {
  min-height: calc(100vh - 64px);
  padding: 20px;
  margin-left: -24px;
  margin-right: -24px;

  // 覆盖全局的左右内边距
  /deep/ .layout-content,
  /deep/ .ant-layout-content,
  /deep/ .ant-pro-basicLayout-content {
    padding-left: 0 !important;
    padding-right: 0 !important;
  }

  * {
    pointer-events: auto;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      position: relative;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid rgba(0, 0, 0, 0.06);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      pointer-events: auto;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

        .card-glow {
          opacity: 1;
        }
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .card-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;

          .anticon {
            font-size: 18px;
            color: white;
          }
        }

        .card-trend {
          display: flex;
          align-items: center;
          font-size: 11px;
          font-weight: 600;
          padding: 3px 6px;
          border-radius: 8px;

          .anticon {
            margin-right: 3px;
            font-size: 9px;
          }

          &.up {
            background: rgba(82, 196, 26, 0.1);
            color: #52c41a;
          }
        }
      }

      .card-content {
        .card-number {
          font-size: 24px;
          font-weight: 700;
          line-height: 1;
          margin-bottom: 6px;
          color: #1a1a1a;
        }

        .card-label {
          font-size: 14px;
          font-weight: 600;
          color: #666;
          margin-bottom: 2px;
        }

        .card-sublabel {
          font-size: 11px;
          color: #999;
        }
      }

      .card-glow {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        opacity: 0;
        transition: opacity 0.3s ease;
        pointer-events: none;
        z-index: 0;
      }

      .card-header,
      .card-content {
        position: relative;
        z-index: 1;
        pointer-events: auto;
      }

      &.primary {
        .card-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .card-glow {
          background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
        }
      }

      &.secondary {
        .card-icon {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        .card-glow {
          background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(245, 87, 108, 0.1) 100%);
        }
      }

      &.success {
        .card-icon {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        .card-glow {
          background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.1) 100%);
        }
      }

      &.warning {
        .card-icon {
          background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        }

        .card-glow {
          background: linear-gradient(135deg, rgba(250, 112, 154, 0.1) 0%, rgba(254, 225, 64, 0.1) 100%);
        }
      }

      &.info {
        .card-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .card-glow {
          background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
        }
      }

      &.down {
        background: rgba(255, 77, 79, 0.1);
        color: #ff4d4f;
      }
    }
  }

  .charts-section {
    margin-bottom: 24px;

    .chart-card, .alert-card {
      height: 400px;

      /deep/ .content {
        height: calc(100% - 60px);
        display: flex;
        flex-direction: column;
        padding: 16px;
      }
    }

    .chart-container {
      flex: 1;
      position: relative;
      min-height: 300px;
      width: 100%;

      .chart {
        width: 100%;
        height: 100%;
        min-height: 300px;
      }
    }

    .alerts-container {
      flex: 1;
      overflow: hidden;

      .no-alerts {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: #999;

        .anticon {
          font-size: 40px;
          color: #52c41a;
          margin-bottom: 12px;
        }

        p {
          font-size: 14px;
          margin: 0;
        }
      }

      .alerts-list {
        height: 100%;
        overflow-y: auto;

        .alert-item {
          display: flex;
          align-items: center;
          padding: 10px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .alert-icon {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 10px;

            .anticon {
              color: white;
              font-size: 12px;
            }
          }

          .alert-content {
            flex: 1;

            .alert-title {
              font-size: 13px;
              font-weight: 500;
              color: #333;
              margin-bottom: 2px;
            }

            .alert-meta {
              display: flex;
              gap: 8px;
              font-size: 11px;
              color: #999;
            }
          }

          .alert-level {
            margin-left: 8px;

            .ant-tag {
              font-size: 10px;
              padding: 1px 6px;
              margin: 0;
            }
          }

          &.warning .alert-icon {
            background: #fa8c16;
          }

          &.error .alert-icon {
            background: #ff4d4f;
          }

          &.info .alert-icon {
            background: #1890ff;
          }
        }
      }
    }
  }

  .quick-actions-card {
    margin-bottom: 24px;

    .quick-actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;

      .quick-action-item {
        display: flex;
        align-items: center;
        padding: 16px;
        background: #fafafa;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.3s ease;
        border: 1px solid transparent;
        position: relative;
        z-index: 1;

        &:hover {
          background: white;
          border-color: #d9d9d9;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .action-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;

          .anticon {
            font-size: 16px;
            color: white;
          }

          &.primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.secondary {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.success {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.warning {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
          }
        }

        .action-content {
          flex: 1;

          .action-title {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 2px;
          }

          .action-desc {
            font-size: 11px;
            color: #999;
            line-height: 1.4;
          }
        }

        .action-arrow {
          color: #999;
          transition: all 0.3s ease;
          font-size: 12px;
        }

        &:hover .action-arrow {
          color: #1890ff;
          transform: translateX(2px);
        }
      }
    }
  }

  .platform-overview {
    margin-bottom: 24px;

    .overview-content {
      .overview-features {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 20px;
        margin-bottom: 24px;

        .feature-item {
          display: flex;
          align-items: flex-start;

          .feature-icon {
            width: 40px;
            height: 40px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            flex-shrink: 0;

            .anticon {
              color: white;
              font-size: 16px;
            }
          }

          .feature-content {
            h4 {
              font-size: 14px;
              font-weight: 600;
              color: #333;
              margin: 0 0 6px 0;
            }

            p {
              font-size: 12px;
              color: #666;
              line-height: 1.5;
              margin: 0;
            }
          }
        }
      }

      .overview-cta {
        text-align: center;
        position: relative;
        z-index: 1;

        .ant-btn {
          height: 40px;
          padding: 0 24px;
          font-size: 14px;
          border-radius: 20px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;
          position: relative;
          z-index: 1;

          &:hover {
            background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
          }

          .anticon {
            margin-right: 6px;
          }
        }
      }
    }
  }

  .faq-card {
    /deep/ .ant-collapse {
      background: transparent;

      .ant-collapse-item {
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .ant-collapse-header {
          padding: 12px 16px;
          font-weight: 600;
          color: #333;
          font-size: 14px;

          .ant-collapse-arrow {
            right: 16px;
          }
        }

        .ant-collapse-content {
          border-top: 1px solid #f0f0f0;

          .ant-collapse-content-box {
            padding: 12px 16px;

            p {
              margin-bottom: 8px;
              line-height: 1.5;
              font-size: 13px;

              &:last-child {
                margin-bottom: 0;
              }
            }

            ul {
              margin: 8px 0;
              padding-left: 16px;

              li {
                margin-bottom: 4px;
                line-height: 1.5;
                font-size: 13px;

                strong {
                  color: #1890ff;
                }
              }
            }
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(0, 255, 136, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(0, 255, 136, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 255, 136, 0);
  }
}


@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;

    .stats-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .charts-section {
      .ant-col {
        margin-bottom: 12px;
      }

      .chart-card, .alert-card {
        height: 300px;
      }
    }

    .quick-actions-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .overview-features {
      grid-template-columns: 1fr;
      gap: 16px;
    }
  }
}
</style>
