<template>
  <div class="alert-manager">
    <TitleBox title="告警管理" class="alert-card">
      <template #extra>
        <div class="alert-actions">
          <a-badge :count="criticalAlerts.length" :offset="[10, 0]">
            <a-button type="link" size="small" @click="viewAllAlerts">查看全部</a-button>
          </a-badge>
          <a-button type="primary" size="small" @click="acknowledgeAll">全部确认</a-button>
        </div>
      </template>

      <div class="alert-content">
        <!-- 告警统计 -->
        <div class="alert-stats">
          <div class="stat-item critical">
            <div class="stat-icon">
              <a-icon type="exclamation-circle"/>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ criticalAlerts.length }}</div>
              <div class="stat-label">严重告警</div>
            </div>
          </div>
          <div class="stat-item warning">
            <div class="stat-icon">
              <a-icon type="warning"/>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ warningAlerts.length }}</div>
              <div class="stat-label">警告</div>
            </div>
          </div>
          <div class="stat-item info">
            <div class="stat-icon">
              <a-icon type="info-circle"/>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ infoAlerts.length }}</div>
              <div class="stat-label">信息</div>
            </div>
          </div>
        </div>

        <!-- 告警列表 -->
        <div class="alerts-container">
          <div v-if="filteredAlerts.length === 0" class="no-alerts">
            <a-icon type="check-circle"/>
            <p>暂无告警信息</p>
          </div>
          <div v-else class="alerts-list">
            <div
              v-for="alert in filteredAlerts"
              :key="alert.id"
              :class="['alert-item', alert.level, { unread: !alert.acknowledged }]"
              @click="handleAlertClick(alert)"
            >
              <div class="alert-icon">
                <a-icon :type="getAlertIcon(alert.type)"/>
              </div>
              <div class="alert-content">
                <div class="alert-title">{{ alert.message }}</div>
                <div class="alert-meta">
                  <span class="alert-device">{{ alert.deviceName }}</span>
                  <span class="alert-time">{{ formatTime(alert.time) }}</span>
                </div>
                <div class="alert-description">{{ alert.description }}</div>
              </div>
              <div class="alert-actions">
                <a-tag :color="getAlertColor(alert.level)">{{ getAlertText(alert.level) }}</a-tag>
                <a-button
                  v-if="!alert.acknowledged"
                  type="link"
                  size="small"
                  @click.stop="acknowledgeAlert(alert.id)"
                >
                  确认
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 告警过滤器 -->
        <div class="alert-filters">
          <a-radio-group v-model="alertFilter" size="small">
            <a-radio-button value="all">全部</a-radio-button>
            <a-radio-button value="critical">严重</a-radio-button>
            <a-radio-button value="warning">警告</a-radio-button>
            <a-radio-button value="info">信息</a-radio-button>
            <a-radio-button value="unread">未读</a-radio-button>
          </a-radio-group>
        </div>
      </div>
    </TitleBox>

    <!-- 告警详情弹窗 -->
    <a-modal
      v-model="showAlertDetail"
      title="告警详情"
      :footer="null"
      width="600px"
    >
      <div v-if="selectedAlert" class="alert-detail">
        <div class="detail-header">
          <div class="detail-level">
            <a-tag :color="getAlertColor(selectedAlert.level)">
              {{ getAlertText(selectedAlert.level) }}
            </a-tag>
          </div>
          <div class="detail-time">{{ formatTime(selectedAlert.time) }}</div>
        </div>
        <div class="detail-content">
          <h4>{{ selectedAlert.message }}</h4>
          <p class="detail-description">{{ selectedAlert.description }}</p>
          <div class="detail-meta">
            <div class="meta-item">
              <span class="label">设备名称：</span>
              <span class="value">{{ selectedAlert.deviceName }}</span>
            </div>
            <div class="meta-item">
              <span class="label">告警类型：</span>
              <span class="value">{{ getAlertTypeText(selectedAlert.type) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">告警值：</span>
              <span class="value">{{ selectedAlert.value }}</span>
            </div>
            <div class="meta-item">
              <span class="label">阈值：</span>
              <span class="value">{{ selectedAlert.threshold }}</span>
            </div>
          </div>
        </div>
        <div class="detail-actions">
          <a-button type="primary" @click="acknowledgeAlert(selectedAlert.id)">
            确认告警
          </a-button>
          <a-button @click="showAlertDetail = false">关闭</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import TitleBox from './TitleBox.vue'
import {getDashboardAlerts} from '@/api/dashboard'

export default {
  name: 'AlertManager',
  components: {
    TitleBox
  },
  data() {
    return {
      alerts: [
        {
          id: 1,
          type: 'device_offline',
          deviceName: '温度传感器TH-001',
          message: '设备离线超过30分钟',
          description: '设备TH-001在2024-01-15 14:30:00开始离线，已超过30分钟未收到心跳信号。',
          level: 'critical',
          time: new Date(Date.now() - 1800000).toISOString(),
          acknowledged: false,
          value: '离线',
          threshold: '30分钟'
        },
        {
          id: 2,
          type: 'high_temperature',
          deviceName: '环境监测器ENV-002',
          message: '温度异常，当前值45°C',
          description: '设备ENV-002检测到温度异常，当前温度45°C，超过设定阈值40°C。',
          level: 'warning',
          time: new Date(Date.now() - 900000).toISOString(),
          acknowledged: false,
          value: '45°C',
          threshold: '40°C'
        },
        {
          id: 3,
          type: 'low_battery',
          deviceName: '智能水表WM-003',
          message: '电池电量低于10%',
          description: '设备WM-003电池电量不足，当前电量8%，建议及时更换电池。',
          level: 'warning',
          time: new Date(Date.now() - 600000).toISOString(),
          acknowledged: false,
          value: '8%',
          threshold: '10%'
        },
        {
          id: 4,
          type: 'network_error',
          deviceName: '网关设备GW-001',
          message: '网络连接不稳定',
          description: '网关设备GW-001网络连接出现波动，可能影响数据传输。',
          level: 'info',
          time: new Date(Date.now() - 300000).toISOString(),
          acknowledged: true,
          value: '连接不稳定',
          threshold: '正常连接'
        }
      ],
      alertFilter: 'all',
      showAlertDetail: false,
      selectedAlert: null
    }
  },
  computed: {
    criticalAlerts() {
      return this.alerts.filter(alert => alert.level === 'critical')
    },
    warningAlerts() {
      return this.alerts.filter(alert => alert.level === 'warning')
    },
    infoAlerts() {
      return this.alerts.filter(alert => alert.level === 'info')
    },
    filteredAlerts() {
      if (this.alertFilter === 'all') {
        return this.alerts
      } else if (this.alertFilter === 'unread') {
        return this.alerts.filter(alert => !alert.acknowledged)
      } else {
        return this.alerts.filter(alert => alert.level === this.alertFilter)
      }
    }
  },
  mounted() {
    this.loadAlerts()
  },
  methods: {
    async loadAlerts() {
      try {
        const res = await getDashboardAlerts()
        this.alerts = res.data || this.alerts
      } catch (error) {
        console.error('加载告警数据失败:', error)
      }
    },

    getAlertIcon(type) {
      const icons = {
        device_offline: 'disconnect',
        high_temperature: 'fire',
        low_battery: 'battery',
        network_error: 'wifi',
        data_anomaly: 'warning',
        threshold_exceeded: 'exclamation-circle'
      }
      return icons[type] || 'exclamation-circle'
    },

    getAlertColor(level) {
      const colors = {
        critical: 'red',
        warning: 'orange',
        info: 'blue'
      }
      return colors[level] || 'default'
    },

    getAlertText(level) {
      const texts = {
        critical: '严重',
        warning: '警告',
        info: '信息'
      }
      return texts[level] || '未知'
    },

    getAlertTypeText(type) {
      const types = {
        device_offline: '设备离线',
        high_temperature: '温度异常',
        low_battery: '电量不足',
        network_error: '网络错误',
        data_anomaly: '数据异常',
        threshold_exceeded: '阈值超限'
      }
      return types[type] || '未知类型'
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

    handleAlertClick(alert) {
      this.selectedAlert = alert
      this.showAlertDetail = true
    },

    acknowledgeAlert(alertId) {
      const alert = this.alerts.find(a => a.id === alertId)
      if (alert) {
        alert.acknowledged = true
        this.$message.success('告警已确认')
      }
    },

    acknowledgeAll() {
      this.alerts.forEach(alert => {
        alert.acknowledged = true
      })
      this.$message.success('所有告警已确认')
    },

    viewAllAlerts() {
      this.$router.push('/system/notice')
    }
  }
}
</script>

<style lang="less" scoped>
.alert-manager {
  .alert-card {
    /deep/ .content {
      padding: 20px;
    }
  }

  .alert-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .alert-content {
    .alert-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      margin-bottom: 20px;

      .stat-item {
        display: flex;
        align-items: center;
        padding: 12px;
        background: #fafafa;
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
          background: white;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .stat-icon {
          width: 32px;
          height: 32px;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;

          .anticon {
            color: white;
            font-size: 14px;
          }
        }

        .stat-info {
          .stat-number {
            font-size: 18px;
            font-weight: 700;
            color: #1a1a1a;
            line-height: 1;
          }

          .stat-label {
            font-size: 12px;
            color: #666;
            margin-top: 2px;
          }
        }

        &.critical .stat-icon {
          background: #ff4d4f;
        }

        &.warning .stat-icon {
          background: #fa8c16;
        }

        &.info .stat-icon {
          background: #1890ff;
        }
      }
    }

    .alerts-container {
      max-height: 400px;
      overflow-y: auto;

      .no-alerts {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 200px;
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
        .alert-item {
          display: flex;
          align-items: flex-start;
          padding: 12px;
          border-radius: 8px;
          margin-bottom: 8px;
          cursor: pointer;
          transition: all 0.3s ease;
          border: 1px solid transparent;

          &:hover {
            background: #f8f9fa;
            border-color: #e8e8e8;
          }

          &.unread {
            background: #fff7e6;
            border-color: #ffd591;
          }

          .alert-icon {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            flex-shrink: 0;

            .anticon {
              color: white;
              font-size: 12px;
            }
          }

          .alert-content {
            flex: 1;
            min-width: 0;

            .alert-title {
              font-size: 14px;
              font-weight: 600;
              color: #333;
              margin-bottom: 4px;
              line-height: 1.4;
            }

            .alert-meta {
              display: flex;
              gap: 12px;
              font-size: 12px;
              color: #999;
              margin-bottom: 4px;
            }

            .alert-description {
              font-size: 12px;
              color: #666;
              line-height: 1.4;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
          }

          .alert-actions {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 8px;
            margin-left: 12px;
            flex-shrink: 0;
          }

          &.critical .alert-icon {
            background: #ff4d4f;
          }

          &.warning .alert-icon {
            background: #fa8c16;
          }

          &.info .alert-icon {
            background: #1890ff;
          }
        }
      }
    }

    .alert-filters {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
      text-align: center;
    }
  }

  .alert-detail {
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;

      .detail-time {
        font-size: 12px;
        color: #999;
      }
    }

    .detail-content {
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
      }

      .detail-description {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
        margin-bottom: 16px;
      }

      .detail-meta {
        .meta-item {
          display: flex;
          margin-bottom: 8px;

          .label {
            font-size: 14px;
            color: #666;
            width: 80px;
            flex-shrink: 0;
          }

          .value {
            font-size: 14px;
            color: #333;
            font-weight: 500;
          }
        }
      }
    }

    .detail-actions {
      margin-top: 20px;
      text-align: right;

      .ant-btn {
        margin-left: 8px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .alert-manager {
    .alert-content {
      .alert-stats {
        grid-template-columns: 1fr;
        gap: 12px;
      }

      .alerts-container {
        max-height: 300px;
      }
    }
  }
}
</style> 