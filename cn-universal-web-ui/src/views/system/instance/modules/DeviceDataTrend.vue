<template>
  <a-modal
    :maskClosable="false"
    :title="meta"
    v-model="open"
    width="900px"
    :footer="null"
    :destroyOnClose="true"
    @cancel="closePanel"
  >
    <a-row class="content">
      <div class="trend-select" v-show="showBtn">
        <div class="btn-group">
          <span class="btn" :class="{'active': trendNum === 1}" @click="toggle(1)">1H</span>
          <span class="btn" :class="{'active': trendNum === 2}" @click="toggle(2)">24H</span>
          <span class="btn" :class="{'active': trendNum === 3}" @click="toggle(3)">7D</span>
          <span class="btn" :class="{'active': trendNum === 4}" @click="toggle(4)">15D</span>
          <a-popover v-model="visible" trigger="click" placement="bottomRight">
            <div slot="content" style="width: 280px;">
              <div class="custom-date-picker">
                <label>选择时间范围：</label>
                <a-range-picker v-model="createTimeRange" show-time
                                style="width: 100%; margin-top: 8px;"/>
                <div class="picker-actions">
                  <a-button type="primary" size="small" @click="submit">{{ $t('button.confirm') }}</a-button>
                  <a-button size="small" @click="hide">{{ $t('button.cancel') }}</a-button>
                </div>
              </div>
            </div>
            <span class="btn custom-btn" :class="{'active': trendNum === 5}">自定义</span>
          </a-popover>
        </div>
      </div>
      <a-tabs default-active-key="1" @change="callback">
        <a-tab-pane key="1" tab="数据列表">
          <!-- 数据统计信息 -->
          <div class="data-stats" v-if="!loading && logList.length > 0">
            <div class="stat-item">
              <span class="stat-label">总记录数：</span>
              <span class="stat-value">{{ total }}</span>
            </div>
            <div class="stat-item" v-if="type === 'PROPERTIES'">
              <span class="stat-label">最新值：</span>
              <span class="stat-value">{{ latestValue }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">最后更新：</span>
              <span class="stat-value">{{ latestTime }}</span>
            </div>
          </div>

          <a-table
            bordered
            :rowKey="(record,index)=>{return index}"
            :loading="loading"
            :data-source="logList"
            :pagination="false"
            size="default"
            class="optimized-table"
          >
            <a-table-column title="时间" key="time" align="center" data-index="createTime"
                            width="180">
              <template slot-scope="text">
                <div class="time-cell">
                  <a-icon type="clock-circle" style="margin-right: 4px; color: #1890ff;"/>
                  {{ text }}
                </div>
              </template>
            </a-table-column>

            <a-table-column :title="type === 'EVENT'?'事件内容':'属性值'" key="attributeType"
                            align="center" data-index="attribute">
              <template slot-scope="text, record">
                <div class="value-cell">
                  <span class="value-content">{{ record.metaLog.content }}</span>
                  <span class="value-unit" v-if="record.metaLog.ext3">{{
                      record.metaLog.ext3
                    }}</span>
                </div>
              </template>
            </a-table-column>

            <a-table-column title="属性名称" key="propertyName" align="center" width="120"
                            v-if="type === 'PROPERTIES'">
              <template slot-scope="text, record">
                <a-tag color="blue">{{ record.metaLog.ext1 || meta }}</a-tag>
              </template>
            </a-table-column>

            <a-table-column title="设备信息" key="deviceInfo" align="center" width="150">
              <template slot-scope="text, record">
                <a-tag color="blue">{{ record.metaLog.deviceName }}</a-tag>
                <!-- <div class="device-info">
                  <div class="device-name">{{ record.metaLog.deviceName }}</div>
                  <div class="device-id">ID: {{ record.metaLog.deviceId }}</div>
                </div> -->
              </template>
            </a-table-column>

            <a-table-column title="操作" align="center" key="operate" width="120">
              <template slot-scope="text, record">
                <div class="action-btn" @click="showDetail(record)">
                  <a-icon type="eye"/>
                  <span>详情</span>
                </div>
              </template>
            </a-table-column>
          </a-table>

          <!-- 分页 -->
          <a-pagination
            class="ant-table-pagination"
            show-size-changer
            show-quick-jumper
            :current="queryParams.pageNum"
            :total="total"
            :page-size="queryParams.pageSize"
            :showTotal="(total) => '共 ' + total + ' 条数据'"
            @showSizeChange="onShowSizeChange"
            @change="changeSize"
          />
        </a-tab-pane>
        <a-tab-pane key="2" v-if="showTrend" tab="趋势图表" force-render>
          <div class="chart-container">
            <div class="chart-header">
              <h4>{{ meta }} 数据趋势</h4>
              <div class="chart-info">
                <span>数据点数：{{ trendList.length }}</span>
                <span v-if="trendList.length > 0">范围：{{ minValue }} ~ {{ maxValue }}</span>
              </div>
            </div>
            <div ref="dom" class="dom"></div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-row>

    <a-modal :maskClosable="false" title="数据详情" v-model="detailOpen" width="650px"
             :footer="null">
      <div class="detail-content">
        <div class="detail-header">
          <h4>{{ (message && message.ext1) || meta }} 详细信息</h4>
          <a-tag color="green" v-if="message">{{
              formatDate(new Date(message.createTime))
            }}
          </a-tag>
        </div>
        <json-viewer :value="message" :expand-depth="10" copyable boxed>
          <template v-slot:copy>复制</template>
        </json-viewer>
      </div>
    </a-modal>
  </a-modal>
</template>

<script>
import {listDeviceMetaV2} from '@/api/system/dev/deviceLog'
import JsonViewer from 'vue-json-viewer'
import * as echarts from 'echarts'

export default {
  name: 'DeviceDataTrend',
  props: {
    devId: {
      type: String,
      require: true
    },
    productKey: {
      type: String,
      require: true
    },
    show: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: ''
    },
    meta: {
      type: String,
      default: ''
    },
    metaId: {
      type: String,
      default: ''
    },
    symbol: {
      type: String,
      default: ''
    },
    lookData: {
      type: Object,
      default: () => ({})
    }
  },
  components: {
    JsonViewer
  },
  watch: {
    show(val) {
      this.open = val
      if (val) {
        this.resetQueryParams()
        this.getList()
        this.trendNumChanged(this.trendNum)
      } else {
        this.showBtn = false
        this.showTrend = false
        this.trendNum = 1
      }
    },
    showTrend(val) {
      if (val) {
        this.$nextTick(() => {
          this.drawChart()
        })
      }
    }
  },
  data() {
    return {
      visible: false,
      createTimeRange: [],
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 日志数据
      logList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 是否显示详情弹出层
      detailOpen: false,
      // 查询参数
      queryParams: {
        iotId: this.devId,
        pageNum: 1,
        pageSize: 2000,
        productKey: this.productKey,
        property: this.metaId,
        messageType: 'PROPERTIES',
        beginCreateTime: null,
        endCreateTime: null,
        params: {}
      },
      // 详情信息
      message: null,
      chart: null,
      showBtn: false,
      showTrend: false,
      trendNum: 1,
      trendList: []
    }
  },
  computed: {
    latestValue() {
      if (this.logList.length > 0) {
        const latest = this.logList[0].metaLog
        return latest.content + (latest.ext3 || '')
      }
      return '--'
    },
    latestTime() {
      if (this.logList.length > 0) {
        return this.logList[0].createTime
      }
      return '--'
    },
    minValue() {
      if (this.trendList.length > 0) {
        return Math.min(...this.trendList.map(t => parseFloat(t.value))).toFixed(2)
      }
      return '--'
    },
    maxValue() {
      if (this.trendList.length > 0) {
        return Math.max(...this.trendList.map(t => parseFloat(t.value))).toFixed(2)
      }
      return '--'
    }
  },
  methods: {
    callback(key) {
      key === '2' ? this.showBtn = true : this.showBtn = false
    },
    submit() {
      if (this.createTimeRange.length) {
        this.trendNumChanged(5)
        this.$nextTick(() => {
          this.drawChart()
        })
        this.visible = false
      } else {
        this.$message.warning('自定义日期不能为空!')
      }
    },
    hide() {
      this.createTimeRange = []
      this.visible = false
    },
    drawChart() {
      this.chart = echarts.init(this.$refs.dom)
      const values = this.trendList.map(t => parseFloat(t.value))
      const avgValue = values.reduce((a, b) => a + b, 0) / values.length

      this.chart.setOption({
        title: {
          text: '平均值: ' + avgValue.toFixed(2) + (this.symbol || ''),
          textStyle: {
            fontSize: 14,
            color: '#666'
          },
          right: 10,
          top: 10
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(50,50,50,0.9)',
          borderColor: '#1890ff',
          borderWidth: 1,
          textStyle: {
            color: '#fff'
          },
          formatter: (params) => {
            const param = params[0]
            return '<div style="padding: 8px;">' +
              '<div style="margin-bottom: 6px; font-weight: bold; color: #ffd666;">' +
              '<i class="anticon anticon-clock-circle" style="margin-right: 4px;"></i>' +
              param.name + // 显示完整时间
              '</div>' +
              '<div style="display: flex; align-items: center;">' +
              '<span style="display: inline-block; width: 10px; height: 10px; background: '
              + param.color + '; border-radius: 50%; margin-right: 8px;"></span>' +
              '<span style="font-size: 14px;">' + this.meta + ': <strong style="color: #40a9ff;">' +
              param.value + (this.symbol || '') + '</strong></span>' +
              '</div>' +
              '</div>'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.trendList.map(t => t.createTime),
          axisLine: {
            lineStyle: {
              color: '#e6e6e6'
            }
          },
          axisLabel: {
            color: '#666',
            rotate: 45,
            // 根据数据量自动调整显示间隔
            interval: (index, value) => {
              const dataLength = this.trendList.length
              // 根据数据量动态调整显示间隔
              if (dataLength <= 20) {
                return true // 显示所有标签
              } else if (dataLength <= 50) {
                return index % 2 === 0 // 每2个显示1个
              } else if (dataLength <= 100) {
                return index % 5 === 0 // 每5个显示1个
              } else if (dataLength <= 500) {
                return index % 10 === 0 // 每10个显示1个
              } else if (dataLength <= 1000) {
                return index % 20 === 0 // 每20个显示1个
              } else if (dataLength <= 5000) {
                return index % 50 === 0 // 每50个显示1个
              } else {
                return index % 100 === 0 // 每100个显示1个
              }
            },
            formatter: (value) => {
              // 简化时间显示，只显示时分秒
              const parts = value.split(' ')
              if (parts.length === 2) {
                return parts[1] // 只返回时间部分
              }
              return value
            }
          }
        },
        yAxis: {
          type: 'value',
          name: this.symbol || '',
          nameTextStyle: {
            color: '#666'
          },
          axisLine: {
            lineStyle: {
              color: '#e6e6e6'
            }
          },
          axisLabel: {
            color: '#666',
            formatter: (value) => {
              return value.toFixed(2)
            }
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0',
              type: 'dashed'
            }
          }
        },
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            start: 0,
            end: 100,
            height: 30,
            bottom: 20,
            textStyle: {
              color: '#666'
            },
            borderColor: '#ddd',
            fillerColor: 'rgba(24,144,255,0.2)',
            handleStyle: {
              color: '#1890ff'
            }
          }
        ],
        series: [
          {
            name: this.meta,
            data: this.trendList.map(t => parseFloat(t.value)),
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 3,
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 1,
                y2: 0,
                colorStops: [
                  {offset: 0, color: '#1890ff'},
                  {offset: 1, color: '#52c41a'}
                ]
              }
            },
            itemStyle: {
              color: '#1890ff',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  {offset: 0, color: 'rgba(24,144,255,0.3)'},
                  {offset: 1, color: 'rgba(24,144,255,0.05)'}
                ]
              }
            },
            markLine: {
              silent: true,
              data: [
                {
                  type: 'average',
                  name: '平均值',
                  lineStyle: {
                    color: '#ff7875',
                    type: 'dashed'
                  },
                  label: {
                    formatter: '平均: {c}' + (this.symbol || ''),
                    color: '#ff7875'
                  }
                }
              ]
            }
          }
        ]
      }, true)
    },
    toggle(val) {
      this.trendNum = val
      this.trendNumChanged(val)
    },
    trendNumChanged(val) {
      if (this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.trendList = []
        const params = {}
        let pageSize = 2000 // 默认1H的pageSize
        if (this.type === 'EVENT') {
          this.queryParams.event = this.metaId
          params.event = this.metaId
        } else {
          this.queryParams.properties = this.metaId
          params.properties = this.metaId
        }
        if (val === 1) {
          // 1小时
          pageSize = 500
          this.queryParams.beginCreateTime = parseInt(
            new Date().setHours(new Date().getHours() - 1) / 1000)
          this.queryParams.endCreateTime = parseInt(new Date().getTime() / 1000)
          params.beginCreateTime = parseInt(new Date().setHours(new Date().getHours() - 1) / 1000)
          params.endCreateTime = parseInt(new Date().getTime() / 1000)
        } else if (val === 2) {
          // 24小时
          pageSize = 2000
          this.queryParams.beginCreateTime = parseInt(
            new Date().setDate((new Date().getDate() - 1)) / 1000)
          this.queryParams.endCreateTime = parseInt(new Date().getTime() / 1000)
          params.beginCreateTime = parseInt(new Date().setDate((new Date().getDate() - 1)) / 1000)
          params.endCreateTime = parseInt(new Date().getTime() / 1000)
        } else if (val === 3) {
          // 7天
          pageSize = 5000
          this.queryParams.beginCreateTime = parseInt(
            new Date().setDate((new Date().getDate() - 7)) / 1000)
          this.queryParams.endCreateTime = parseInt(new Date().getTime() / 1000)
          params.beginCreateTime = parseInt(new Date().setDate((new Date().getDate() - 7)) / 1000)
          params.endCreateTime = parseInt(new Date().getTime() / 1000)
        } else if (val === 4) {
          // 15天
          pageSize = 10000
          this.queryParams.beginCreateTime = parseInt(
            new Date().setDate((new Date().getDate() - 15)) / 1000)
          this.queryParams.endCreateTime = parseInt(new Date().getTime() / 1000)
          params.beginCreateTime = parseInt(new Date().setDate((new Date().getDate() - 15)) / 1000)
          params.endCreateTime = parseInt(new Date().getTime() / 1000)
        } else {
          // 自定义时间范围，使用较大的pageSize
          pageSize = 20000
          // 将日期转换为 Unix 时间戳（秒）
          this.queryParams.beginCreateTime = parseInt(this.createTimeRange[0].valueOf() / 1000)
          this.queryParams.endCreateTime = parseInt(this.createTimeRange[1].valueOf() / 1000)
          params.beginCreateTime = parseInt(this.createTimeRange[0].valueOf() / 1000)
          params.endCreateTime = parseInt(this.createTimeRange[1].valueOf() / 1000)
        }
        const query = Object.assign({}, this.queryParams, {pageSize: pageSize, pageNum: 1, params})
        listDeviceMetaV2(query).then(response => {
          const logsArray = response.list
          if (this.type === 'PROPERTIES') {
            // 扩展数字类型判断，支持更多数字类型
            const numericTypes = ['ist', 'double', 'float', 'number', 'long', 'short', 'byte',
              'decimal', 'numeric']
            if (numericTypes.includes(this.lookData.type) ||
              (typeof this.lookData.type === 'string' && this.lookData.type.toLowerCase().includes(
                'number'))) {
              this.showTrend = true
              this.$nextTick(() => {
                this.drawChart()
              })
            }
            logsArray.forEach((item, index) => {
              // 扩展数字类型判断，支持更多数字类型
              const numericTypes = ['int', 'double', 'float', 'number', 'long', 'short', 'byte',
                'decimal', 'numeric']
              if (numericTypes.includes(this.lookData.type) ||
                (typeof this.lookData.type === 'string'
                  && this.lookData.type.toLowerCase().includes('number'))) {
                this.trendList.unshift({
                  createTime: this.formatDate(new Date(item.createTime)),
                  value: item.content
                })
              }
            })
          }
        })
      }, 500)
    },
    /** 设备日志列表 */
    getList() {
      this.loading = true
      this.logList = []
      // 如果未传pageSize，默认2000
      if (!this.queryParams.pageSize) {
        this.queryParams.pageSize = 2000
      }
      return listDeviceMetaV2(this.queryParams).then(response => {
        const logsArray = response.list
        this.total = parseInt(response.totalCount)
        if (this.type === 'PROPERTIES') {
          logsArray.forEach((item) => {
            this.logList.push(
              {
                createTime: this.formatDate(new Date(item.createTime)),
                attribute: item.content + (item.ext3 || ''),
                metaLog: item
              })
          })
        } else if (this.type === 'EVENT') {
          logsArray.forEach(item => {
            this.logList.push(
              {
                createTime: this.formatDate(new Date(item.createTime)),
                attribute: item.content + (item.ext3 || ''),
                metaLog: item
              })
          })
        }
      }).finally(() => {
        this.loading = false
      })
    },
    // 配置参数
    resetQueryParams() {
      const params = {}
      if (this.type === 'EVENT') {
        params.event = this.metaId
      } else {
        params.properties = this.metaId
      }
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        productKey: this.productKey,
        messageType: this.type,
        iotId: this.devId,
        property: this.metaId,
        beginCreateTime: null,
        endCreateTime: null,
        params: params
      }
    },
    // 数据格式化
    formatMessage(row) {
      if (this.type === 'EVENT') {
        return this.meta
      } else {
        const message = JSON.parse(row.message)
        const value = message.properties[this.metaId]
        return this.meta + ' ： ' + value + this.symbol
      }
    },
    // 显示详情
    showDetail(row) {
      this.detailOpen = true
      this.message = row.metaLog
    },
    // 关闭弹出层
    closePanel() {
      this.$emit('close')
    },
    // 分页
    onShowSizeChange(current, pageSize) {
      this.queryParams.pageSize = pageSize
      this.queryParams.beginCreateTime = null
      this.queryParams.endCreateTime = null
      this.getList()
    },
    // 分页
    changeSize(current, pageSize) {
      this.queryParams.pageNum = current
      this.queryParams.pageSize = pageSize
      this.queryParams.beginCreateTime = null
      this.queryParams.endCreateTime = null
      this.getList()
    },
    // 时间转换
    formatDate(time) {
      // 获取年
      const year = time.getFullYear()
      // 获取月
      const month = time.getMonth() + 1
      // 获取日
      const date = time.getDate()
      // 获取星期
      // eslint-disable-next-line no-unused-vars
      const day = time.getDay()
      // 获取小时
      const hours = time.getHours()
      // 获取分钟
      const minutes = time.getMinutes()
      // 获取秒
      const seconds = time.getSeconds()
      // 获取毫秒
      // eslint-disable-next-line no-unused-vars
      const ms = time.getMilliseconds()
      let curDateTime = year
      if (month > 9) {
        curDateTime = curDateTime + '-' + month
      } else {
        curDateTime = curDateTime + '-0' + month
      }
      if (date > 9) {
        curDateTime = curDateTime + '-' + date
      } else {
        curDateTime = curDateTime + '-0' + date
      }
      if (hours > 9) {
        curDateTime = curDateTime + ' ' + hours
      } else {
        curDateTime = curDateTime + ' 0' + hours
      }
      if (minutes > 9) {
        curDateTime = curDateTime + ':' + minutes
      } else {
        curDateTime = curDateTime + ':0' + minutes
      }
      if (seconds > 9) {
        curDateTime = curDateTime + ':' + seconds
      } else {
        curDateTime = curDateTime + ':0' + seconds
      }
      return curDateTime
    }
  }
}
</script>
<style scoped lang="less">
.dom {
  width: 100%;
  height: 450px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content {
  position: relative;
}

.trend-select {
  position: absolute;
  right: 0;
  top: 4px;
  z-index: 999;
  display: flex;
  align-items: center;

  .btn-group {
    margin-right: 10px;
    display: flex;
    border-radius: 6px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .btn {
      cursor: pointer;
      width: 60px;
      height: 32px;
      line-height: 30px;
      border: 1px solid #d9d9d9;
      text-align: center;
      background: #fff;
      transition: all 0.3s;
      font-size: 12px;

      &:hover {
        background: #f0f8ff;
        border-color: #1890ff;
      }

      &:not(:last-child) {
        border-right: none;
      }
    }

    .custom-btn {
      width: 70px;
    }

    .active {
      background: #1890ff;
      border-color: #1890ff;
      color: #fff;

      &:hover {
        background: #40a9ff;
      }
    }
  }
}

.custom-date-picker {
  padding: 8px;

  label {
    font-weight: 500;
    color: #333;
    font-size: 13px;
  }

  .picker-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 12px;
  }
}

.data-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f6f9fc 0%, #e9f4ff 100%);
  border-radius: 8px;
  border: 1px solid #e6f7ff;

  .stat-item {
    display: flex;
    align-items: center;

    .stat-label {
      font-weight: 500;
      color: #666;
      font-size: 13px;
    }

    .stat-value {
      margin-left: 8px;
      font-weight: 600;
      color: #1890ff;
      font-size: 14px;
    }
  }
}

.optimized-table {
  border-radius: 8px;
  overflow: hidden;

  :deep(.ant-table-thead > tr > th) {
    background: #fafafa;
    font-weight: 600;
    color: #333;
    border-bottom: 2px solid #e8e8e8;
  }

  :deep(.ant-table-tbody > tr) {
    // 添加行间距
    margin-bottom: 4px;

    &:hover > td {
      background: #f0f8ff;
    }

    // 为每行添加底部间距
    &:not(:last-child) {
      border-bottom: 8px solid transparent;
    }
  }

  // 增加表格行高
  :deep(.ant-table-tbody > tr > td) {
    padding: 16px 12px;
    line-height: 1.6;
  }

  .time-cell {
    display: flex;
    align-items: center;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
  }

  .value-cell {
    display: flex;
    align-items: center;
    justify-content: center;

    .value-content {
      font-weight: 600;
      color: #1890ff;
      font-size: 14px;
    }

    .value-unit {
      color: #999;
      font-size: 12px;
      margin-left: 4px;
    }
  }

  .device-info {
    text-align: left;

    .device-name {
      font-weight: 500;
      color: #333;
      margin-bottom: 2px;
      font-size: 13px;
    }

    .device-id {
      color: #999;
      font-size: 11px;
      font-family: 'Monaco', 'Menlo', monospace;
    }
  }

  .action-btn {
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: 16px;
    background: linear-gradient(135deg, #1890ff, #40a9ff);
    color: #fff;
    font-size: 12px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
    }

    .anticon {
      margin-right: 4px;
      font-size: 12px;
    }
  }
}

.chart-container {
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 8px;
    border-left: 4px solid #1890ff;

    h4 {
      margin: 0;
      color: #333;
      font-size: 16px;
      font-weight: 600;
    }

    .chart-info {
      display: flex;
      gap: 16px;

      span {
        font-size: 12px;
        color: #666;
        padding: 2px 8px;
        background: #fff;
        border-radius: 4px;
        border: 1px solid #e8e8e8;
      }
    }
  }
}

.detail-content {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e8e8e8;

    h4 {
      margin: 0;
      color: #333;
      font-size: 16px;
      font-weight: 600;
    }
  }
}


@media (max-width: 768px) {
  .trend-select {
    position: static;
    margin-bottom: 16px;
    justify-content: center;

    .btn-group .btn {
      width: 50px;
      font-size: 11px;
    }
  }

  .data-stats {
    flex-direction: column;
    gap: 8px;
  }

  .chart-container .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
