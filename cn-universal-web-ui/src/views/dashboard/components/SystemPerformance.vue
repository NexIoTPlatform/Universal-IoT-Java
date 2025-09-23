<template>
  <div class="system-performance">
    <div class="performance-box">
      <div class="performance-header">
        <h3>系统性能监控</h3>
        <a-radio-group v-model="refreshInterval" size="small">
          <a-radio-button value="5">5秒</a-radio-button>
          <a-radio-button value="10">10秒</a-radio-button>
          <a-radio-button value="30">30秒</a-radio-button>
        </a-radio-group>
      </div>

      <div class="performance-content">
        <LoadingOverlay :loading="loading" tip="加载性能数据..." transparent/>

        <div class="performance-grid">
          <!-- CPU使用率 -->
          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-icon cpu">
                <a-icon type="dashboard"/>
              </div>
              <div class="metric-title">CPU使用率</div>
            </div>
            <div class="metric-value">
              <CountTo :endVal="performanceData.cpuUsage" :decimals="1"/>
              <span class="unit">%</span>
            </div>
            <div class="metric-chart">
              <div class="progress-ring">
                <svg width="60" height="60">
                  <circle
                    cx="30"
                    cy="30"
                    r="25"
                    stroke="#f0f0f0"
                    stroke-width="4"
                    fill="none"
                  />
                  <circle
                    cx="30"
                    cy="30"
                    r="25"
                    stroke="#1890ff"
                    stroke-width="4"
                    fill="none"
                    stroke-dasharray="157"
                    :stroke-dashoffset="157 - (157 * performanceData.cpuUsage) / 100"
                    transform="rotate(-90 30 30)"
                  />
                </svg>
                <div class="progress-text">{{ performanceData.cpuUsage }}%</div>
              </div>
            </div>
          </div>

          <!-- 内存使用率 -->
          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-icon memory">
                <a-icon type="database"/>
              </div>
              <div class="metric-title">内存使用率</div>
            </div>
            <div class="metric-value">
              <CountTo :endVal="performanceData.memoryUsage" :decimals="1"/>
              <span class="unit">%</span>
            </div>
            <div class="metric-chart">
              <div class="progress-ring">
                <svg width="60" height="60">
                  <circle
                    cx="30"
                    cy="30"
                    r="25"
                    stroke="#f0f0f0"
                    stroke-width="4"
                    fill="none"
                  />
                  <circle
                    cx="30"
                    cy="30"
                    r="25"
                    stroke="#52c41a"
                    stroke-width="4"
                    fill="none"
                    stroke-dasharray="157"
                    :stroke-dashoffset="157 - (157 * performanceData.memoryUsage) / 100"
                    transform="rotate(-90 30 30)"
                  />
                </svg>
                <div class="progress-text">{{ performanceData.memoryUsage }}%</div>
              </div>
            </div>
          </div>

          <!-- 磁盘使用率 -->
          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-icon disk">
                <a-icon type="hdd"/>
              </div>
              <div class="metric-title">磁盘使用率</div>
            </div>
            <div class="metric-value">
              <CountTo :endVal="performanceData.diskUsage" :decimals="1"/>
              <span class="unit">%</span>
            </div>
            <div class="metric-chart">
              <div class="progress-ring">
                <svg width="60" height="60">
                  <circle
                    cx="30"
                    cy="30"
                    r="25"
                    stroke="#f0f0f0"
                    stroke-width="4"
                    fill="none"
                  />
                  <circle
                    cx="30"
                    cy="30"
                    r="25"
                    stroke="#fa8c16"
                    stroke-width="4"
                    fill="none"
                    stroke-dasharray="157"
                    :stroke-dashoffset="157 - (157 * performanceData.diskUsage) / 100"
                    transform="rotate(-90 30 30)"
                  />
                </svg>
                <div class="progress-text">{{ performanceData.diskUsage }}%</div>
              </div>
            </div>
          </div>

          <!-- 网络流量 -->
          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-icon network">
                <a-icon type="global"/>
              </div>
              <div class="metric-title">网络流量</div>
            </div>
            <div class="metric-value">
              <CountTo :endVal="performanceData.networkTraffic" :decimals="1"/>
              <span class="unit">MB/s</span>
            </div>
            <div class="metric-trend">
              <a-icon :type="performanceData.networkTrend >= 0 ? 'arrow-up' : 'arrow-down'"/>
              <span>{{ Math.abs(performanceData.networkTrend) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import LoadingOverlay from './LoadingOverlay.vue'
import {getDashboardSystemMonitor} from '@/api/dashboard'

export default {
  name: 'SystemPerformance',
  components: {
    CountTo,
    LoadingOverlay
  },
  data() {
    return {
      loading: false,
      refreshInterval: 10,
      performanceData: {
        cpuUsage: 0,
        memoryUsage: 0,
        diskUsage: 0,
        networkTraffic: 0,
        networkTrend: 0
      },
      refreshTimer: null
    }
  },
  mounted() {
    this.loadData()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getDashboardSystemMonitor(this.refreshInterval)
        console.log('SystemPerformance API response:', res.data)

        if (res.data && res.data.systemMetrics) {
          const metrics = res.data.systemMetrics
          this.performanceData = {
            cpuUsage: parseFloat(metrics.cpuUsage) || 0,
            memoryUsage: parseFloat(metrics.memoryUsage) || 0,
            diskUsage: parseFloat(metrics.diskUsage) || 0,
            networkTraffic: metrics.networkTraffic
              ? (parseFloat(metrics.networkTraffic.rxMBps || 0) + parseFloat(
                metrics.networkTraffic.txMBps || 0))
              : 0,
            networkTrend: 0
          }
          console.log('Updated performanceData:', this.performanceData)
        }
      } catch (error) {
        console.error('加载系统性能数据失败:', error)
      } finally {
        this.loading = false
      }
    },

    startAutoRefresh() {
      this.stopAutoRefresh()
      this.refreshTimer = setInterval(() => {
        this.loadData()
      }, this.refreshInterval * 1000)
    },

    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    }
  },
  watch: {
    refreshInterval() {
      this.startAutoRefresh()
    }
  }
}
</script>

<style lang="less" scoped>
.system-performance {
  .performance-box {
    width: 100%;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .performance-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-left: 22px;
    padding-right: 22px;
    height: 50px;
    line-height: 50px;
    border-bottom: 1px solid #F2F3F5;

    h3 {
      height: 100%;
      font-size: 16px;
      font-weight: 600;
      padding-left: 8px;
      position: relative;
      margin: 0;

      &::before {
        content: '';
        display: inline-block;
        width: 3px;
        height: 18px;
        background-color: #1966ff;
        position: absolute;
        left: 0;
        top: calc(50% - 9px);
      }
    }
  }

  .performance-content {
    padding: 16px 22px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .performance-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    flex: 1;
    align-content: center;

    .metric-card {
      background: #fafafa;
      border-radius: 8px;
      padding: 12px;
      text-align: center;
      transition: all 0.3s ease;

      &:hover {
        background: white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .metric-header {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 6px;

        .metric-icon {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 8px;

          .anticon {
            color: white;
            font-size: 16px;
          }

          &.cpu {
            background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
          }

          &.memory {
            background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
          }

          &.disk {
            background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
          }

          &.network {
            background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
          }
        }

        .metric-title {
          font-size: 14px;
          font-weight: 600;
          color: #333;
        }
      }

      .metric-value {
        font-size: 20px;
        font-weight: 700;
        color: #1a1a1a;
        margin-bottom: 4px;

        .unit {
          font-size: 14px;
          color: #666;
          margin-left: 4px;
        }
      }

      .metric-chart {
        display: flex;
        justify-content: center;

        .progress-ring {
          position: relative;
          display: inline-block;

          .progress-text {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: 12px;
            font-weight: 600;
            color: #333;
          }
        }
      }

      .metric-trend {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: #52c41a;

        .anticon {
          margin-right: 4px;
        }
      }
    }
  }
}
</style> 