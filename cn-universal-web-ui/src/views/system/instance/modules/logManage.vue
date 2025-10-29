<template>
  <a-card :bordered="false">
    <!-- 条件搜索 -->
    <div class="search-card">
      <a-row type="flex" align="middle" justify="start" :gutter="8">
        <a-col :xs="24" :sm="6" :md="5" :lg="5">
          <a-form-item label="消息ID">
            <a-input v-model="queryParam.id" placeholder="输入消息ID" allow-clear
                     style="width: 180px;"/>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="6" :md="5" :lg="5">
          <a-form-item label="消息类型">
            <a-select v-model="queryParam.messageType" placeholder="全部类型" allow-clear
                      style="width: 120px;">
              <a-select-option v-for="(d, index) in messageType" :key="index" :value="d.dictValue">
                <span class="message-type-option">
                  <span class="type-icon">{{ getTypeIcon(d.dictValue) }}</span>
                  {{ d.dictLabel }}
                </span>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="8" :md="8" :lg="8">
          <a-form-item label="时间范围">
            <a-range-picker
              v-model="createTimeRange"
              style="width: 260px;"
              :placeholder="['开始时间', '结束时间']"
              format="YYYY-MM-DD HH:mm:ss"
              show-time
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="4" :md="6" :lg="6" style="text-align: right;">
          <a-space size="small">
            <a-button type="primary" size="small" style="min-width:48px;height:24px;font-size:12px;"
                      @click="handleQuery" class="modern-btn">
              <a-icon type="search"/>
              {{ $t('button.query') }} </a-button>
            <a-button size="small" style="min-width:48px;height:24px;font-size:12px;"
                      @click="resetQuery" class="modern-btn">
              <a-icon type="redo"/>
              {{ $t('button.reset') }} </a-button>
          </a-space>
        </a-col>
      </a-row>
    </div>
    <div>
      <a-modal :visible="modelVisible" title="详细信息" @cancel="closeModel" :footer="null">
        <json-viewer :value="messages" :expand-depth="10" copyable>
          <template v-slot:copy>复制</template>
        </json-viewer>
      </a-modal>
    </div>
    <!-- 操作栏 -->
    <div class="table-operations">
      <div class="operations-left">
        <a-statistic
          title="总记录数"
          :value="total"
          class="record-count"
        />
      </div>
      <div class="operations-right">
        <a-button
          type="primary"
          size="small"
          :loading="loading"
          @click="getList"
          class="refresh-btn"
        >
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
    </div>
    <!-- 数据展示 -->
    <a-table
      :loading="loading"
      :size="tableSize"
      :rowKey="record => record.id || record.key"
      :columns="columns"
      :data-source="list"
      :pagination="false">
      <div slot="messageId" slot-scope="text, record" class="message-id-cell">
        <a-tooltip title="点击复制消息ID">
          <a-button
            size="small"
            shape="circle"
            icon="copy"
            @click="copyMessageId(record.id)"
            class="copy-id-btn"
          />
        </a-tooltip>
      </div>

      <div slot="deviceId" slot-scope="text, record" class="device-id-cell">
        <div
          class="device-id-copy"
          @click="copyDeviceId(record.deviceId)"
          :title="'点击复制设备ID: ' + record.deviceId"
        >
          <span class="device-id-text">{{ record.deviceId }}</span>
          <a-icon type="copy" class="copy-icon"/>
        </div>
      </div>

      <span slot="messageType" slot-scope="text, record" class="message-type-cell">
        <a-tag :color="getTypeColor(record.messageType)" class="type-tag">
          <span class="type-icon">{{ getTypeIcon(record.messageType) }}</span>
          {{ getDataType(record) }}
        </a-tag>
      </span>

      <div slot="messageData" slot-scope="text, record" class="message-data-cell">
        <!-- 事件消息 -->
        <div v-if="record.messageType === 'EVENT'" class="event-data">
          <div class="event-info">
            <a-tag size="small" class="event-tag">
              {{ record.event || '未知事件' }}
            </a-tag>
          </div>
          <div v-if="getEventData(record).properties" class="event-properties">
            <!-- <div class="properties-title">关联属性:</div> -->
            <div class="properties-list">
              <div v-for="(value, key) in getEventData(record).properties" :key="key"
                   class="property-item">
                <span class="property-key">{{ key }}:</span>
                <span class="property-value">{{ value }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 属性消息 -->
        <div v-else-if="record.messageType === 'PROPERTIES'" class="properties-data">
          <div v-if="getPropertiesData(record)" class="properties-list">
            <!-- <a-tag>点击悬停 🫱 </a-tag> -->
            <span class="message-info">{{ getPropertiesData(record) }}</span>

          </div>
          <a-tag v-else>无属性数据</a-tag>
          <!-- <span v-else class="no-data">无属性数据</span> -->
        </div>

        <!-- 其他消息类型 -->
        <div v-else class="other-data">
          <span class="message-info">{{ getOtherMessageInfo(record) }}</span>
        </div>
      </div>

      <div slot="createTime" slot-scope="text, record" class="time-cell">
        <div class="time-main">{{ formatTime(record.createTime) }}</div>
        <div class="time-sub">{{ formatTimeAgo(record.createTime) }}</div>
      </div>
      <div slot="content" slot-scope="text, record" class="content-cell">
        <a-popover
          placement="leftTop"
          trigger="hover"
          overlayClassName="json-popover"
          :title="getContentTitle(record)"
        >
          <template slot="content">
            <div class="json-content">
              <pre>{{ formatJsonContent(record.content) || '' }}</pre>
            </div>
          </template>
          <a-button size="small" type="dashed" class="content-preview-btn">
            <a-icon type="code"/>
            悬停查看
          </a-button>
        </a-popover>

      </div>
    </a-table>
    <!-- 分页 -->
    <a-pagination
      class="ant-table-pagination"
      show-size-changer
      show-quick-jumper
      :current="queryParam.pageNum"
      :total="total"
      :page-size="queryParam.pageSize"
      :showTotal="total => `共 ${total} 条`"
      @showSizeChange="onShowSizeChange"
      @change="changeSize"
    />
  </a-card>
</template>

<script>
import {listDeviceLog} from '@/api/system/dev/deviceLog'
import JsonViewer from 'vue-json-viewer'

export default {
  name: 'LogManage',
  props: {
    devId: {
      type: String,
      require: true
    },
    productKey: {
      type: String,
      require: true
    },
    deviceId: {
      type: String,
      require: true
    }
  },
  components: {
    JsonViewer
  },
  data() {
    return {
      typeOptions: [],
      messages: undefined,
      createTimeRange: [],
      messageType: [
        {dictValue: 'EVENT', dictLabel: '事件上报'},
        {dictValue: 'PROPERTIES', dictLabel: '属性上报'},
        {dictValue: 'REPLY', dictLabel: '功能回复'},
        {dictValue: 'FUNCTIONS', dictLabel: '功能调用'}
      ],
      // 选择应用弹窗可视
      modelVisible: false,
      list: [],
      loading: false,
      total: 0,
      // 查询参数
      queryParam: {
        messageType: undefined,
        createTime: null,
        pageNum: 1,
        pageSize: 10,
        params: {}
      },
      columns: [
        {
          title: '消息ID',
          dataIndex: 'id',
          scopedSlots: {customRender: 'messageId'},
          width: '10%',
          align: 'center'
        },
        {
          title: '消息类型',
          dataIndex: 'messageType',
          scopedSlots: {customRender: 'messageType'},
          width: '12%',
          align: 'center'
        },
        {
          title: '消息数据',
          dataIndex: 'messageData',
          scopedSlots: {customRender: 'messageData'},
          width: '48%',
          align: 'center'
        },
        {
          title: '记录时间',
          dataIndex: 'createTime',
          scopedSlots: {customRender: 'createTime'},
          width: '22%',
          align: 'center'
        },
        {
          title: '详细内容',
          dataIndex: 'content',
          scopedSlots: {customRender: 'content'},
          width: '20%',
          align: 'center'
        }
      ],
      overflowMap: {}, // 记录每条数据是否溢出
    }
  },
  filters: {},
  created() {
    this.getList()
    this.getDicts('data_type').then(response => {
      this.typeOptions = response.data
    })
  },
  mounted() {
    this.$nextTick(this.checkAllMarqueeOverflow)
  },
  updated() {
    this.$nextTick(this.checkAllMarqueeOverflow)
  },
  computed: {},
  watch: {},
  methods: {
    // 日期改变时
    changeTime(value, mode) {
      // console.log('value data = ', value)
      // console.log('mode data = ', mode)
    },
    /** 查询log列表 */
    getList() {
      this.loading = true
      this.queryParam.iotId = this.devId
      this.queryParam.productKey = this.productKey
      this.queryParam.deviceId = this.deviceId
      if (this.createTimeRange.length > 0) {
        // 将日期转换为 ISO 格式字符串，后端 LocalDateTime 可以自动解析
        this.queryParam.params['beginCreateTime'] = this.createTimeRange[0].format('YYYY-MM-DD HH:mm:ss')
        this.queryParam.params['endCreateTime'] = this.createTimeRange[1].format('YYYY-MM-DD HH:mm:ss')
        console.log(this.queryParam.params['beginCreateTime'])
      } else {
        this.queryParam.params = {}
      }
      listDeviceLog(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        messageType: undefined,
        deviceName: undefined,
        deviceId: undefined,
        createTime: undefined,
        pageNum: 1,
        pageSize: 10,
        params: {}
      }
      this.createTimeRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },

    // 复制消息ID
    copyMessageId(messageId) {
      this.$copyText(messageId).then(() => {
        this.$message.success(`消息ID已复制: ${messageId}`, 2)
      }).catch(() => {
        this.$message.error('复制失败，请手动复制')
      })
    },

    // 复制设备ID
    copyDeviceId(deviceId) {
      this.$copyText(deviceId).then(() => {
        this.$message.success(`设备ID已复制: ${deviceId}`, 2)
      }).catch(() => {
        this.$message.error('复制失败，请手动复制')
      })
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    showDetail(row) {
      this.modelVisible = true
      row.content = row.content.replace(/:s*([0-9]{15,})s*(,?)/g, ': "$1" $2')
      this.messages = JSON.parse(row.content)
    },
    getEventName(e) {
      var ct = JSON.parse(e.content)
      if (ct && ct.eventName) {
        return e.event + '(' + ct.eventName + ')'
      }
      return e.event
    },
    closeModel() {
      this.modelVisible = false
    },
    getDataType(e) {
      const actions = []
      Object.keys(this.typeOptions).some((key) => {
        if (this.typeOptions[key].dictValue === ('' + e.messageType)) {
          actions.push(this.typeOptions[key].dictLabel)
          return true
        }
      })
      return actions.join('')
    },
    formatJsonContent(content) {
      try {
        return JSON.stringify(JSON.parse(content), "", 2)
      } catch (e) {
        return content
      }
    },
    truncateContent(content) {
      if (content.length > 50) {
        return content.slice(0, 50) + '...'
      }
      return content
    },

    // 获取消息类型图标
    getTypeIcon(messageType) {
      const icons = {
        'EVENT': '🔔',
        'PROPERTIES': '📊',
        'REPLY': '↩️',
        'FUNCTIONS': '⚡'
      }
      return icons[messageType] || '📄'
    },

    // 获取消息类型颜色
    getTypeColor(messageType) {
      const colors = {
        'EVENT': 'orange',
        'PROPERTIES': 'blue',
        'REPLY': 'green',
        'FUNCTIONS': 'purple'
      }
      return colors[messageType] || 'default'
    },

    // 获取属性数据
    getPropertiesData(record) {
      try {
        const content = JSON.parse(record.content)
        const properties = content
        // 判断 properties 是否存在且为空对象
        if (properties && typeof properties === 'object' && !Array.isArray(properties)) {
          const isEmpty = Object.keys(properties).length === 0
          if (isEmpty) {
            return null
          } // 空对象返回 null
        }
        return content || null
      } catch (e) {
        return null
      }
    },

    // 获取事件数据
    getEventData(record) {
      try {
        const content = JSON.parse(record.content)
        return {
          eventName: content.eventName || content.event,
          event: content.event,
          properties: content.data || content.properties
        }
      } catch (e) {
        return {eventName: null, event: null, properties: null}
      }
    },

    // 获取其他消息类型信息
    getOtherMessageInfo(record) {
      try {
        const content = JSON.parse(record.content)
        if (record.messageType === 'FUNCTIONS') {
          return `${content.function || '未知功能'} -> ${content.payload || ''}`
        } else if (record.messageType === 'REPLY') {
          const formattedData = JSON.stringify(content.data, null, 2)
          return `${content.function || '未知回复'} -> ${formattedData}`
        }
        return '查看详情获取更多信息'
      } catch (e) {
        return '数据解析失败'
      }
    },

    // 获取内容标题
    getContentTitle(record) {
      const typeMap = {
        'EVENT': '事件详情',
        'PROPERTIES': '属性详情',
        'FUNCTIONS': '功能调用详情',
        'REPLY': '功能回复详情'
      }
      return typeMap[record.messageType] || '消息详情'
    },

    // 格式化时间
    formatTime(createTime) {
      // createTime 现在是 LocalDateTime 格式的字符串，如 "2025-01-14T15:30:27"
      const date = new Date(createTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },

    // 格式化相对时间
    formatTimeAgo(createTime) {
      const now = new Date()
      const time = new Date(createTime)
      const diff = now - time

      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)

      if (days > 0) {
        return `${days}天前`
      }
      if (hours > 0) {
        return `${hours}小时前`
      }
      if (minutes > 0) {
        return `${minutes}分钟前`
      }
      return '刚刚'
    },
    checkAllMarqueeOverflow() {
      if (!this.list) {
        return;
      }
      this.list.forEach(record => {
        this.checkMarqueeOverflow(record);
      });
    },
    checkMarqueeOverflow(record) {
      this.$nextTick(() => {
        const container = this.$refs['marqueeContainer' + record.id];
        const content = this.$refs['marqueeContent' + record.id];
        if (container && content && content[0] && container[0]) {
          this.$set(this.overflowMap, record.id, content[0].scrollWidth > container[0].clientWidth);
        } else if (container && content) {
          // 兼容非数组情况
          this.$set(this.overflowMap, record.id, content.scrollWidth > container.clientWidth);
        }
      });
    }
  }
}
</script>

<style scoped>
.search-card {
  background: #fafbfc;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 14px 18px 4px 18px;
  margin-bottom: 18px;
  border: none;
}

.search-form-modern .ant-form-item {
  margin-bottom: 0;
  margin-right: 18px;
}

.modern-btn {
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  min-width: 48px;
  height: 24px;
  padding: 0 8px;
}

.message-id-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.copy-id-btn {
  color: #1890ff;
  border: none;
  background: #f4f8ff;
  box-shadow: none;
  transition: background 0.2s;
}

.copy-id-btn:hover {
  background: #e6f7ff;
  color: #40a9ff;
}

@media (max-width: 768px) {
  .search-card {
    padding: 10px 8px 4px 8px;
  }

  .search-form-modern .ant-form-item {
    width: 100%;
    margin-right: 0;
  }
}

/* 操作栏样式 */
.table-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.operations-left {
  display: flex;
  align-items: center;
}

.operations-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-count {
  margin-right: 20px;
}

.record-count .ant-statistic-title {
  font-size: 12px;
  color: #718096;
}

.record-count .ant-statistic-content {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.refresh-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  font-weight: 500;
}

/* 表格单元格样式 */

/* 消息ID单元格样式 */
.message-id-cell {
  padding: 8px 0;
  display: flex;
  justify-content: center;
}

.message-id-copy {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  max-width: 100%;
}

.message-id-copy:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.message-id-text {
  color: #1a202c;
  font-weight: 600;
  margin-right: 6px;
  word-break: break-all;
  flex: 1;
}

.copy-icon {
  color: #1890ff;
  font-size: 12px;
  opacity: 0.7;
  transition: opacity 0.3s ease;
  flex-shrink: 0;
}

.message-id-copy:hover .copy-icon {
  opacity: 1;
}

/* 设备ID单元格样式 */
.device-id-cell {
  padding: 8px 0;
  display: flex;
  justify-content: center;
}

.device-id-copy {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  max-width: 100%;
}

.device-id-copy:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.device-id-text {
  color: #1a202c;
  font-weight: 600;
  margin-right: 6px;
  word-break: break-all;
  flex: 1;
}

.device-id-copy:hover .copy-icon {
  opacity: 1;
}

.message-type-cell {
  text-align: center;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  font-weight: 500;
  border-radius: 12px;
  padding: 4px 8px;
}

.type-tag .type-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 消息数据单元格样式 */
.message-data-cell {
  padding: 8px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.event-data {
  /* border-left: 1px solid #fa8c16; */
  padding-left: 8px;
  /* text-align: left; */
  /* width: 100%; */
}

.event-info {
  margin-bottom: 8px;
  text-align: center;
}

.event-tag {
  display: inline-flex;
  align-items: center;
  font-weight: 500;
  border-radius: 12px;
}

.event-icon {
  margin-right: 4px;
  font-size: 12px;
}

.event-properties {
  margin-top: 6px;
}

.properties-title {
  font-size: 11px;
  color: #718096;
  margin-bottom: 4px;
  font-weight: 500;
  text-align: center;
}

.properties-data {
  /* border-left: 1px solid #1890ff; */
  padding-left: 8px;
  text-align: left;
  /* width: 100%; */
}

.properties-list {
  max-height: 80px;
  overflow-y: auto;
}

.property-item {
  display: flex;
  margin-bottom: 3px;
  font-size: 12px;
  line-height: 1.4;
}

.property-key {
  color: #718096;
  min-width: 60px;
  font-weight: 500;
  flex-shrink: 0;
}

.property-value {
  color: #1a202c;
  font-weight: 600;
  word-break: break-word;
  flex: 1;
  margin-left: 4px;
}

.other-data {
  /* border-left: 1px solid #52c41a; */
  padding-left: 8px;
  text-align: center;
  /* width: 100%; */
}

.message-info {
  font-size: 12px;
  color: #52c41a;
  font-weight: 500;
}

.no-data {
  color: #a0aec0;
  font-style: italic;
  font-size: 12px;
}

.time-cell {
  text-align: center;
  padding: 8px 0;
}

.time-main {
  font-size: 13px;
  color: #1a202c;
  font-weight: 500;
  margin-bottom: 2px;
}

.time-sub {
  font-size: 11px;
  color: #718096;
}

/* 详细内容区域样式 */
.content-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.content-preview-btn {
  font-size: 11px;
  height: 24px;
  padding: 0 8px;
  border-style: dashed;
}

.content-detail-btn {
  font-size: 11px;
  height: 20px;
  padding: 0 6px;
}

.json-content {
  max-width: 500px;
  max-height: 400px;
  overflow: auto;
}

.json-content pre {
  margin: 0;
  font-size: 11px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
}


@media (max-width: 768px) {
  .search-card {
    padding: 16px;
  }

  .device-name {
    font-size: 13px;
  }

  .device-id {
    font-size: 11px;
  }

  .property-item {
    font-size: 11px;
  }

  .time-main {
    font-size: 12px;
  }

  .time-sub {
    font-size: 10px;
  }
}
</style>

<style>
.json-tooltip .ant-tooltip-inner {
  max-width: 600px;
  white-space: pre-wrap;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  text-align: left;
  background: #2d3748;
  color: #e2e8f0;
  border-radius: 6px;
  padding: 12px;
}

.json-tooltip .ant-tooltip-arrow::before {
  background: #2d3748;
}

.json-popover .ant-popover-inner-content {
  padding: 8px;
  background: #f8fafc;
  border-radius: 6px;
}

.json-popover .ant-popover-title {
  font-weight: 600;
  color: #1a202c;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 8px;
  margin-bottom: 8px;
}
</style>
