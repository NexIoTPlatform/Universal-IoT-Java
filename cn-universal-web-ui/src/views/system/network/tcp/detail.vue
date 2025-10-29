<template>
  <div class="tcp-detail-page minimal">
    <div class="tcp-detail-header">
      <div class="tcp-detail-title">
        TCP网络组件详情
        <a-tag :color="networkInfo.state ? 'green' : 'red'"
               style="margin-left:16px;vertical-align:middle;">
          {{ networkInfo.state ? '运行中' : '已停止' }}
        </a-tag>
      </div>
      <div style="margin: 16px 0 0 0;">
        <a-button type="default" icon="arrow-left" @click="$router.back()">返回上一页</a-button>
      </div>
    </div>
    <a-row :gutter="32" style="margin-top:24px;">
      <a-col :span="11">
        <a-card :bordered="false" class="minimal-card" :bodyStyle="{ padding: '20px 24px' }">
          <a-descriptions :column="1" size="default">
            <a-descriptions-item label="组件名称">{{ networkInfo.name }}</a-descriptions-item>
            <a-descriptions-item label="TCP类型">
              <template v-if="networkInfo.type === 'TCP_CLIENT'">TCP客户端</template>
              <template v-else-if="networkInfo.type === 'TCP_SERVER'">TCP服务端</template>
              <template v-else>{{ networkInfo.type }}</template>
            </a-descriptions-item>
            <a-descriptions-item label="唯一标识">{{ networkInfo.unionId }}</a-descriptions-item>
            <a-descriptions-item label="产品Key">{{ networkInfo.productKey }}</a-descriptions-item>
            <a-descriptions-item :label="$t('time.create')">{{
                parseTime(networkInfo.createDate)
              }}
            </a-descriptions-item>
            <a-descriptions-item label="描述">{{
                networkInfo.description || '暂无描述'
              }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card :bordered="false" class="minimal-card"
                :bodyStyle="{ padding: '20px 24px', marginTop: '16px' }">
          <div class="tcp-detail-section-title">
            配置信息
            <a-button v-if="!editing" type="link" size="small" style="float:right;"
                      v-hasPermi="['network:tcp:edit']"
                      @click="editing = true">{{ $t('button.edit') }} </a-button>
          </div>
          <template v-if="!editing">
            <a-descriptions :column="1" size="default">
              <a-descriptions-item v-for="field in configFields" :key="field.key" v-if="!field.hide"
                                   :label="field.label">
                {{ renderReadValue(field) }}
              </a-descriptions-item>
            </a-descriptions>
          </template>
          <template v-else>
            <a-form :model="formData" layout="horizontal" label-col="{ style: { width: '120px' } }"
                    wrapper-col="{ style: { width: '320px' } }">
              <a-row :gutter="0">
                <a-col :span="24" v-for="field in configFields" :key="field.key" v-if="!field.hide">
                  <a-form-item :label="field.label">
                    <template v-if="field.type === 'int'">
                      <a-input-number v-model="formData[field.key]" :placeholder="field.remark"
                                      style="width:100%" :step="1" :precision="0"/>
                    </template>
                    <template v-else-if="field.type === 'json'">
                      <a-textarea v-model="formData[field.key]" :placeholder="field.remark"
                                  auto-size/>
                    </template>
                    <template v-else-if="field.type === 'file'">
                      <a-upload v-model="formData[field.key]" :show-upload-list="false"/>
                    </template>
                    <template v-else-if="field.type === 'select'">
                      <a-select v-model="formData[field.key]" :placeholder="field.remark">
                        <a-select-option v-for="opt in field.options" :key="opt.value"
                                         :value="opt.value">{{ opt.label }}
                        </a-select-option>
                      </a-select>
                    </template>
                    <template v-else-if="field.type === 'boolean'">
                      <a-select v-model="formData[field.key]" :placeholder="field.remark">
                        <a-select-option v-for="opt in field.options" :key="opt.value"
                                         :value="opt.value">{{ opt.label }}
                        </a-select-option>
                      </a-select>
                    </template>
                    <template v-else>
                      <a-input v-model="formData[field.key]" :placeholder="field.remark"/>
                    </template>
                  </a-form-item>
                </a-col>
              </a-row>
              <div style="margin-top:16px;text-align:right;">
                <a-button type="link" size="small" v-hasPermi="['network:tcp:edit']" @click="handleSaveConfig">保存
                </a-button>
                <a-button type="link" size="small" style="margin-left:8px;" @click="cancelEdit">
                  取消
                </a-button>
              </div>
            </a-form>
          </template>
        </a-card>
      </a-col>
      <a-col :span="13">
        <!-- 主要监测区域 -->
        <a-card :bordered="false" class="minimal-card monitor-card strong"
                :bodyStyle="{ padding: '20px 24px' }">
          <div class="monitor-title">主要监测</div>
          <div class="monitor-stats-row">
            <div class="stat-block">
              <a-icon type="cluster" class="stat-icon cluster"/>
              <span class="stat-label">集群节点</span>
              <span class="stat-value">{{ monitorData.cluster.online }}/{{
                  monitorData.cluster.total
                }}</span>
            </div>
            <div class="stat-block">
              <a-icon type="team" class="stat-icon device"/>
              <span class="stat-label">活跃连接</span>
              <span class="stat-value">{{ monitorData.device.active }}</span>
            </div>
            <div class="stat-block">
              <a-icon type="message" class="stat-icon message"/>
              <span class="stat-label">消息QPS</span>
              <span class="stat-value">{{ monitorData.message.qps }}</span>
            </div>
            <div class="stat-block">
              <a-icon type="warning" class="stat-icon alert"/>
              <span class="stat-label">异常节点</span>
              <span class="stat-value">{{ monitorData.cluster.abnormal }}</span>
            </div>
          </div>
        </a-card>

        <!-- TCP订阅主题信息 -->
        <a-card :bordered="false" class="minimal-card"
                :bodyStyle="{ padding: '20px 24px', marginTop: '16px' }">
          <div class="tcp-detail-section-title">
            <a-icon type="message" style="margin-right: 8px; color: #1890ff;"/>
            TCP通信
            <a-tooltip title="${productKey} 和 ${deviceId} 分别替换为产品Key和设备ID">
              <a-icon type="question-circle"
                      style="margin-left: 8px; color: #999; cursor: pointer;"/>
            </a-tooltip>
          </div>

          <!-- TCP客户端主题 -->
          <div class="topic-section" v-if="networkInfo.type === 'TCP_CLIENT'">
            <div class="topic-category">
              <a-icon type="user" style="color: #1890ff; margin-right: 6px;"/>
              <span class="topic-category-title">TCP客户端通信</span>
            </div>
            <div class="topic-list">
              <div class="topic-item">
                <div class="topic-label">连接地址</div>
                <div class="topic-value">{{
                    getConfigValue(networkInfo, 'host')
                  }}:{{ getConfigValue(networkInfo, 'port') }}
                </div>
              </div>
              <div class="topic-item">
                <div class="topic-label">设备标识</div>
                <div class="topic-value">${productKey}/${deviceId}</div>
              </div>
              <div class="topic-item">
                <div class="topic-label">心跳间隔</div>
                <div class="topic-value">{{
                    getConfigValue(networkInfo, 'heartbeat', '30秒')
                  }}
                </div>
              </div>
            </div>
          </div>

          <!-- TCP服务端主题 -->
          <div class="topic-section" v-if="networkInfo.type === 'TCP_SERVER'">
            <div class="topic-category">
              <a-icon type="cloud-server" style="color: #52c41a; margin-right: 6px;"/>
              <span class="topic-category-title">TCP服务端监听</span>
            </div>
            <div class="topic-list">
              <div class="topic-item">
                <div class="topic-label">监听地址</div>
                <div class="topic-value">0.0.0.0:{{ getConfigValue(networkInfo, 'port') }}</div>
              </div>
            </div>
          </div>

          <!-- 数据格式说明 -->
          <!-- <div class="topic-section">
            <div class="topic-category">
              <a-icon type="code" style="color: #fa8c16; margin-right: 6px;" />
              <span class="topic-category-title">数据格式</span>
            </div>
            <div class="topic-list">
              <div class="topic-item">
                <div class="topic-label">设备上报</div>
                <div class="topic-value">{"productKey":"${productKey}","deviceId":"${deviceId}","data":{...}}</div>
              </div>
              <div class="topic-item">
                <div class="topic-label">指令下发</div>
                <div class="topic-value">{"productKey":"${productKey}","deviceId":"${deviceId}","command":{...}}</div>
              </div>
            </div>
          </div> -->
        </a-card>
      </a-col>
    </a-row>
    <a-modal v-model="previewVisible" title="请确认即将提交的配置JSON" :footer="null" width="600px">
      <pre
        style="background:#f6f6f6;padding:16px;border-radius:4px;max-height:400px;overflow:auto;">{{
          previewJson
        }}</pre>
      <div style="text-align:right;margin-top:16px;">
        <a-button @click="previewVisible=false">{{ $t('button.cancel') }}</a-button>
        <a-button type="primary" style="margin-left:8px;" v-hasPermi="['network:tcp:edit']" @click="confirmSaveConfig">{{ $t('button.confirm') }}提交
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script>
import {getNetwork, restartNetwork, startNetwork, stopNetwork, updateNetwork} from '@/api/system/network'
import {getDictMap, getDicts} from '@/api/system/dict/data'
import * as echarts from 'echarts'

export default {
  name: 'TcpNetworkDetail',
  data() {
    return {
      loading: false,
      networkInfo: {
        id: undefined,
        type: undefined,
        unionId: undefined,
        productKey: undefined,
        name: undefined,
        description: undefined,
        configuration: '{}',
        state: false,
        createDate: undefined
      },
      configFields: [],
      formData: {},
      editing: false,
      previewVisible: false,
      previewJson: '',
      formDataBackup: {},
      healthData: {
        connected: false,
        responseTime: null,
        errorRate: 0,
        packetLoss: 0,
        lastCheckTime: null,
        uptime: null
      },
      monitorData: {
        cluster: {
          total: 5,
          online: 4,
          abnormal: 1,
          nodes: [
            {name: '节点1', status: 'online'},
            {name: '节点2', status: 'online'},
            {name: '节点3', status: 'abnormal'},
            {name: '节点4', status: 'online'},
            {name: '节点5', status: 'online'}
          ]
        },
        device: {active: 120},
        region: {
          barData: [
            {name: '浙江', value: 30},
            {name: '江苏', value: 10},
            {name: '北京', value: 8},
            {name: '上海', value: 7},
            {name: '广东', value: 5}
          ]
        },
        message: {qps: 320}
      }
    }
  },
  computed: {
    formattedConfig() {
      try {
        const config = JSON.parse(this.networkInfo.configuration)
        return JSON.stringify(config, null, 2)
      } catch (error) {
        return this.networkInfo.configuration
      }
    },
    configCardExtra() {
      if (!this.editing) {
        return this.$createElement('a-button', {
          props: {type: 'link', size: 'small'},
          on: {
            click: () => {
              this.editing = true
            }
          },
          attrs: {icon: 'edit'},
          style: {float: 'right', padding: '0 8px'}
        }, '编辑')
      } else {
        return this.$createElement('span', [
          this.$createElement('a-button', {
            props: {type: 'link', size: 'small'},
            on: {click: this.handleSaveConfig},
            style: {padding: '0 8px'}
          }, '保存'),
          this.$createElement('a-button', {
            style: {marginLeft: '8px', padding: '0 8px'},
            props: {size: 'small', type: 'link'},
            on: {click: this.cancelEdit}
          }, '取消')
        ])
      }
    }
  },
  created() {
    this.getNetworkDetail()
  },
  mounted() {
    this.initRegionBarChart()
  },
  methods: {
    /** 获取TCP网络组件详情 */
    getNetworkDetail() {
      const id = this.$route.params.id
      if (!id) {
        this.$message.error('TCP网络组件ID不能为空')
        this.goBack()
        return
      }
      this.loading = true
      getNetwork(id).then(async response => {
        this.networkInfo = response.data
        this.loading = false
        await this.loadConfigFields()
      }).catch(() => {
        this.loading = false
        this.goBack()
      })
    },
    /** 编辑 */
    handleEdit() {
      this.$router.push(`/system/network/tcp/edit/${this.networkInfo.id}`)
    },
    /** 启动/停止 */
    handleToggleState() {
      const action = this.networkInfo.state ? '停止' : '启动'
      this.$confirm({
        title: '确认操作',
        content: `确定要${action}TCP网络组件"${this.networkInfo.name}"吗？`,
        onOk: () => {
          const api = this.networkInfo.state ? stopNetwork : startNetwork
          api(this.networkInfo.id).then(response => {
            this.$message.success(`${action}成功`)
            this.getNetworkDetail()
          })
        }
      })
    },
    /** 重启 */
    handleRestart() {
      this.$confirm({
        title: '确认操作',
        content: `确定要重启TCP网络组件"${this.networkInfo.name}"吗？`,
        onOk: () => {
          restartNetwork(this.networkInfo.id).then(response => {
            this.$message.success('重启成功')
            this.getNetworkDetail()
          })
        }
      })
    },
    /** 复制配置 */
    copyConfig() {
      navigator.clipboard.writeText(this.formattedConfig).then(() => {
        this.$message.success('配置已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },
    /** 下载配置 */
    downloadConfig() {
      const blob = new Blob([this.formattedConfig], {type: 'application/json'})
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${this.networkInfo.name}_config.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      this.$message.success('配置已下载')
    },
    /** 返回 */
    goBack() {
      this.$router.go(-1)
    },
    async loadConfigFields() {
      const res = await getDictMap(['network_tcp_config'])
      const dicts = res.data['network_tcp_config'] || []
      this.configFields = []
      for (const d of dicts) {
        let meta = {}
        if (d.remark && typeof d.remark === 'string') {
          try {
            meta = JSON.parse(d.remark)
          } catch (e) {
            meta = {}
          }
        }
        const field = {
          key: d.dictValue,
          label: d.dictLabel,
          remark: meta.remark || '',
          type: meta.type || 'string',
          hide: meta.hide === 'true',
          options: [],
          default: meta.default
        }
        // select类型需要请求url对应的字典
        if (field.type === 'select' && meta.url) {
          const dictRes = await getDicts(meta.url)
          field.options = (dictRes.data || []).map(
            opt => ({label: opt.dictLabel, value: opt.dictValue}))
        }
        // boolean类型固定是/否
        if (field.type === 'boolean') {
          field.options = [
            {label: '是', value: true},
            {label: '否', value: false}
          ]
        }
        this.configFields.push(field)
      }
      let config = {}
      try {
        config = JSON.parse(this.networkInfo.configuration)
      } catch {
      }
      this.configFields.forEach(f => {
        const val = config[f.key]
        this.$set(this.formData, f.key,
          val !== undefined ? val : f.default !== undefined ? f.default : '')
      })
      console.log('configFields:', this.configFields)
      // 备份初始值用于取消恢复
      this.formDataBackup = JSON.parse(JSON.stringify(this.formData))
    },
    renderReadValue(field) {
      const val = this.formData[field.key]
      if (field.type === 'boolean') {
        return val === true || val === 'true' ? '是' : '否'
      }
      if (field.type === 'select' && field.options && field.options.length) {
        const opt = field.options.find(o => o.value === val)
        return opt ? opt.label : val
      }
      return val
    },
    handleSaveConfig() {
      this.previewJson = JSON.stringify(this.formData, null, 2)
      this.previewVisible = true
    },
    confirmSaveConfig() {
      this.previewVisible = false
      // 修正select/boolean类型字段的值类型
      this.configFields.forEach(field => {
        if ((field.type === 'select' || field.type === 'boolean') && field.options
          && field.options.length) {
          const val = this.formData[field.key]
          // 统一转为option.value的类型
          const match = field.options.find(opt => opt.value === val)
          if (match) {
            this.formData[field.key] = match.value
          }
        }
      })
      updateNetwork({
        id: this.networkInfo.id,
        type: this.networkInfo.type,
        unionId: this.networkInfo.unionId,
        productKey: this.networkInfo.productKey,
        name: this.networkInfo.name,
        description: this.networkInfo.description,
        configuration: JSON.stringify(this.formData)
      }).then(() => {
        this.$message.success('配置保存成功')
        this.editing = false
        this.getNetworkDetail()
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    cancelEdit() {
      // 恢复编辑前的值
      this.formData = JSON.parse(JSON.stringify(this.formDataBackup))
      this.editing = false
    },
    getTcpPort(item) {
      try {
        const config = JSON.parse(item.configuration)
        return config.port || '未配置'
      } catch (error) {
        return '配置错误'
      }
    },
    getConfigValue(item, key, defaultValue = '未配置') {
      try {
        const config = JSON.parse(item.configuration)
        return config[key] || defaultValue
      } catch (error) {
        return '配置错误'
      }
    },
    getDecodeTyoe(item) {
      return this.getConfigValue(item, 'decodeType')
    },
    getTcpHost(item) {
      return this.getConfigValue(item, 'host')
    },
    initRegionBarChart() {
      if (!this.$refs.regionBarChart) {
        return
      }
      const chart = echarts.init(this.$refs.regionBarChart)
      const data = this.monitorData.region.barData
      chart.setOption({
        grid: {left: 40, right: 20, top: 20, bottom: 20},
        xAxis: {
          type: 'category',
          data: data.map(i => i.name),
          axisLine: {lineStyle: {color: '#e0e0e0'}},
          axisLabel: {color: '#666', fontSize: 13}
        },
        yAxis: {
          type: 'value',
          axisLine: {show: false},
          axisLabel: {color: '#666', fontSize: 13},
          splitLine: {lineStyle: {color: '#f0f0f0'}}
        },
        series: [{
          data: data.map(i => i.value),
          type: 'bar',
          barWidth: 32,
          itemStyle: {color: '#1890ff', borderRadius: [4, 4, 0, 0]}
        }]
      })
    }
  }
}
</script>

<style lang="less" scoped>
.tcp-detail-page.minimal {
  background: #fff;
  padding: 0 32px 32px 32px;
}

.tcp-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 24px;
}

.tcp-detail-title {
  font-size: 20px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.tcp-detail-actions {
  display: flex;
  align-items: center;
}

.minimal-card {
  box-shadow: none;
  border-radius: 0;
  background: #fafbfc;
  margin-bottom: 0;
}

.tcp-detail-section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
}

.health-metrics {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.health-metric-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #222;
}

.monitor-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.monitor-block {
  margin-bottom: 0;
}

.monitor-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #222;
}

.monitor-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #444;
  margin-bottom: 2px;
}

.monitor-dashboard {
  width: 100%;
}

.monitor-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(24, 144, 255, 0.06);
  margin-bottom: 0;
  min-height: 180px;
}

.monitor-card.strong {
  border-left: 4px solid #1890ff;
}

.monitor-chart-row {
  margin-bottom: 8px;
}

.monitor-stats-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 32px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.stat-block {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-right: 16px;
}

.stat-icon {
  font-size: 20px;
  vertical-align: middle;
}

.stat-icon.cluster {
  color: #1890ff;
}

.stat-icon.online {
  color: #52c41a;
}

.stat-icon.alert {
  color: #faad14;
}

.stat-icon.device {
  color: #13c2c2;
}

.stat-icon.message {
  color: #722ed1;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin-left: 2px;
}

.monitor-pie-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.mini-list {
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 13px;
}

.dashboard-monitor {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
}

.monitor-chart {
  width: 100%;
  height: 180px;
  margin-bottom: 16px;
}

.monitor-list {
  font-size: 13px;
  color: #888;
  margin-top: 8px;
  display: flex;
  align-items: center;
}

.tcp-monitor-card {
  max-width: 900px;
  margin: 0 auto;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(24, 144, 255, 0.08);
  padding: 32px 40px;
}

.status-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.status-item {
  display: flex;
  align-items: center;
  font-size: 18px;
  color: #222;
  gap: 8px;
}

.status-item.running {
  color: #52c41a;
}

.trend-section {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}

.trend-chart {
  flex: 1;
  height: 120px;
}

.distribution-section {
  display: flex;
  gap: 32px;
  margin-top: 16px;
}

.map-chart, .type-chart {
  width: 320px;
  height: 180px;
}

.region-bar-chart {
  width: 320px;
  height: 180px;
}

.mini-monitor-card {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.06);
  padding: 20px 28px;
  min-height: 120px;
  margin-bottom: 0;
}

.mini-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.mini-status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.mini-label {
  font-size: 15px;
  color: #52c41a;
  font-weight: 500;
}

.mini-metrics-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: center;
}

.mini-metric {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #444;
}

.mini-icon {
  font-size: 18px;
  vertical-align: middle;
}

.mini-icon.running {
  color: #52c41a;
}

.mini-icon.alert {
  color: #faad14;
}

.mini-value {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-left: 2px;
}

/* TCP订阅主题样式 */
.topic-section {
  margin-bottom: 20px;
}

.topic-section:last-child {
  margin-bottom: 0;
}

.topic-category {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.topic-category-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.topic-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 3px solid #1890ff;
  transition: all 0.2s;
}

.topic-item:hover {
  background: #f0f8ff;
  border-left-color: #40a9ff;
}

.topic-label {
  font-size: 13px;
  color: #666;
  min-width: 80px;
  margin-right: 12px;
}

.topic-value {
  font-size: 13px;
  color: #1890ff;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  flex: 1;
  word-break: break-all;
  position: relative;
  cursor: pointer;
  transition: all 0.2s;
}

.topic-value:hover {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.topic-value::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(24, 144, 255, 0.05) 50%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.topic-value:hover::before {
  opacity: 1;
}
</style>

<svg style="display:none;">
<symbol id="icon-cluster" viewBox="0 0 1024 1024">
  <circle cx="512" cy="512" r="400" fill="#e6f7ff"/>
  <circle cx="512" cy="512" r="320" fill="#91d5ff"/>
  <circle cx="512" cy="512" r="160" fill="#1890ff"/>
</symbol>
<symbol id="icon-online" viewBox="0 0 1024 1024">
  <circle cx="512" cy="512" r="400" fill="#f6ffed"/>
  <circle cx="512" cy="512" r="320" fill="#b7eb8f"/>
  <circle cx="512" cy="512" r="160" fill="#52c41a"/>
</symbol>
<symbol id="icon-alert" viewBox="0 0 1024 1024">
  <circle cx="512" cy="512" r="400" fill="#fffbe6"/>
  <circle cx="512" cy="512" r="320" fill="#ffe58f"/>
  <circle cx="512" cy="512" r="160" fill="#faad14"/>
</symbol>
<symbol id="icon-device" viewBox="0 0 1024 1024">
  <rect x="192" y="192" width="640" height="640" rx="80" fill="#e6f7ff"/>
  <rect x="272" y="272" width="480" height="480" rx="60" fill="#91d5ff"/>
  <rect x="352" y="352" width="320" height="320" rx="40" fill="#1890ff"/>
</symbol>
<symbol id="icon-active" viewBox="0 0 1024 1024">
  <rect x="192" y="192" width="640" height="640" rx="80" fill="#f6ffed"/>
  <rect x="272" y="272" width="480" height="480" rx="60" fill="#b7eb8f"/>
  <rect x="352" y="352" width="320" height="320" rx="40" fill="#52c41a"/>
</symbol>
<symbol id="icon-offline" viewBox="0 0 1024 1024">
  <rect x="192" y="192" width="640" height="640" rx="80" fill="#fff1f0"/>
  <rect x="272" y="272" width="480" height="480" rx="60" fill="#ffccc7"/>
  <rect x="352" y="352" width="320" height="320" rx="40" fill="#ff4d4f"/>
</symbol>
<symbol id="icon-message" viewBox="0 0 1024 1024">
  <ellipse cx="512" cy="512" rx="400" ry="320" fill="#f0f5ff"/>
  <ellipse cx="512" cy="512" rx="320" ry="240" fill="#adc6ff"/>
  <ellipse cx="512" cy="512" rx="160" ry="120" fill="#2f54eb"/>
</symbol>
<symbol id="icon-qps" viewBox="0 0 1024 1024">
  <ellipse cx="512" cy="512" rx="400" ry="320" fill="#e6fffb"/>
  <ellipse cx="512" cy="512" rx="320" ry="240" fill="#87e8de"/>
  <ellipse cx="512" cy="512" rx="160" ry="120" fill="#13c2c2"/>
</symbol>
<symbol id="icon-backlog" viewBox="0 0 1024 1024">
  <ellipse cx="512" cy="512" rx="400" ry="320" fill="#fff0f6"/>
  <ellipse cx="512" cy="512" rx="320" ry="240" fill="#ffadd2"/>
  <ellipse cx="512" cy="512" rx="160" ry="120" fill="#eb2f96"/>
</symbol>
</svg>
