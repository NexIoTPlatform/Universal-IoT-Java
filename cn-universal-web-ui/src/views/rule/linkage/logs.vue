<template>
  <div>
    <page-header-wrapper>
      <a-card :bordered="false">
        <!-- 条件搜索 -->
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('linkage.sceneName')">
                  <a-input v-model="queryParam.cName" placeholder="请输入场景名称" allow-clear/>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="执行状态">
                  <a-select v-model="queryParam.cStatus" placeholder="请选择执行状态" allow-clear
                            style="width: 100%">
                    <a-select-option :value="1">成功</a-select-option>
                    <a-select-option :value="2">失败</a-select-option>
                    <a-select-option :value="0">部分成功</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('linkage.triggerMethod')">
                  <a-select v-model="queryParam.triggerType" placeholder="请选择触发方式"
                            allow-clear
                            style="width: 100%">
                    <a-select-option value="device">设备触发</a-select-option>
                    <a-select-option value="time">定时触发</a-select-option>
                    <a-select-option value="manual">手动触发</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <template v-if="advanced">
                <a-col :md="8" :sm="24">
                  <a-form-item label="业务ID">
                    <a-input v-model="queryParam.cId" placeholder="请输入业务ID" allow-clear/>
                  </a-form-item>
                </a-col>
                <a-col :md="8" :sm="24">
                  <a-form-item label="业务类型">
                    <a-select v-model="queryParam.cType" placeholder="请选择业务类型" allow-clear
                              style="width: 100%">
                      <a-select-option :value="1">场景联动</a-select-option>
                      <a-select-option :value="2">数据流转</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="8" :sm="24">
                  <a-form-item :label="$t('time.create')">
                    <a-range-picker v-model="dateRange" style="width: 100%"/>
                  </a-form-item>
                </a-col>
              </template>
              <a-col :md="!advanced && 8 || 24" :sm="24">
                <span class="table-page-search-submitButtons"
                      :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                  <a-button type="primary" @click="handleQuery"><iot-icon
                    type="icon-search"/>{{ $t('button.query') }}</a-button>
                  <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                    type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
                  <a @click="toggleAdvanced" style="margin-left: 8px">
                    {{ advanced ? $t('button.collapse') : $t('button.expand') }}
                    <a-icon :type="advanced ? 'up' : 'down'"/>
                  </a>
                </span>
              </a-col>
            </a-row>
          </a-form>
        </div>
        <div class="table-operations">
          <a-button
            type="dashed"
            shape="circle"
            :loading="loading"
            :style="{float: 'right'}"
            icon="reload"
            @click="getList"/>
        </div>
        <!-- 数据展示 -->
        <a-table
          :loading="loading"
          :size="tableSize"
          rowKey="id"
          :columns="columns"
          :dataSource="list"
          :pagination="false">
          <span slot="triggerType" slot-scope="text, record">
            <a-tag :color="getTriggerTypeColor(record.triggerType)">
              {{ getTriggerTypeText(record.triggerType) }}
            </a-tag>
          </span>
          <span slot="cStatus" slot-scope="text, record">
            <a-tag :color="getStatusColor(record.cstatus)">
              {{ getStatusText(record.cstatus) }}
            </a-tag>
          </span>
          <span slot="cType" slot-scope="text, record">
            <a-tag :color="record.ctype === 1 ? 'blue' : 'green'">
              {{ record.ctype === 1 ? '场景联动' : '数据流转' }}
            </a-tag>
          </span>
          <span slot="createTime" slot-scope="text, record">
            {{ formatDate(record.createTime) }}
          </span>
          <span slot="operation" slot-scope="text, record">
            <a @click="showLogDetail(record)">
              <a-icon type="eye"/>
              {{ $t('button.confirm') }}
            </a>
          </span>
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
    </page-header-wrapper>

    <!-- 日志详情弹窗 -->
    <a-modal
      v-model="logDetailVisible"
      title="执行日志详情"
      width="900px"
      :footer="null"
      @cancel="closeLogDetail"
    >
      <div v-if="logDetail">
        <!-- 设备触发信息 -->
        <div v-if="logDetail.cdeviceMeta" class="device-trigger-section">
          <a-card title="🔍 设备触发信息" size="small" class="device-trigger-card">
            <div v-if="parseDeviceMeta(logDetail.cdeviceMeta)">
              <!-- 基本信息 -->
              <div class="device-basic-info">
                <a-row :gutter="24">
                  <a-col :span="24">
                    <div class="info-item">
                      <a-icon type="home" class="info-icon info-icon-primary"/>
                      <b>场景名称：</b>
                      <span class="info-value info-value-primary">{{
                          parseDeviceMeta(logDetail.cdeviceMeta).sceneName
                        }}</span>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="info-item">
                      <a-icon type="key" class="info-icon info-icon-warning"/>
                      <b>所属产品：</b>
                      <span class="info-value info-value-warning">{{
                          parseDeviceMeta(logDetail.cdeviceMeta).productKey
                        }}</span>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="info-item">
                      <a-icon type="mobile" class="info-icon info-icon-success"/>
                      <b>设备ID：</b>
                      <span class="info-value info-value-success">{{
                          parseDeviceMeta(logDetail.cdeviceMeta).deviceId
                        }}</span>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="info-item">
                      <a-icon type="message" class="info-icon info-icon-purple"/>
                      <b>消息类型：</b>
                      <a-tag color="purple" class="info-tag">{{
                          (parseDeviceMeta(logDetail.cdeviceMeta).messageType) === 'PROPERTIES'
                            ? '属性' : '事件'
                        }}
                      </a-tag>
                    </div>
                  </a-col>
                </a-row>
              </div>

              <!-- 触发条件 -->
              <div v-if="parseDeviceMeta(logDetail.cdeviceMeta).triggerConditions"
                   class="trigger-conditions">
                <div class="trigger-conditions-header">
                  <a-icon type="filter" class="trigger-icon"/>
                  <b>触发条件</b>
                </div>
                <div
                  v-for="(condition, idx) in parseDeviceMeta(logDetail.cdeviceMeta).triggerConditions"
                  :key="idx"
                  class="condition-item">
                  <a-row class="condition-basic-info">
                    <a-col :span="10">
                      <div class="condition-info-item">
                        <a-icon type="desktop" class="condition-icon condition-icon-success"/>
                        <b>设备名称：</b>
                        <span class="condition-value condition-value-success">{{
                            condition.deviceName || condition.deviceId
                          }}</span>
                      </div>
                    </a-col>
                    <a-col :span="10">
                      <div class="condition-info-item">
                        <a-icon type="desktop" class="condition-icon condition-icon-success"/>
                        <b>设备ID：</b>
                        <span class="condition-value condition-value-success">{{
                            condition.deviceId
                          }}</span>
                      </div>
                    </a-col>
                  </a-row>
                  <div v-if="condition.filters && condition.filters.length > 0">
                    <div class="filters-header">
                      <a-icon type="setting" class="filters-icon"/>
                      <b>过滤条件：</b>
                    </div>
                    <div v-for="(filter, fIdx) in condition.filters" :key="fIdx"
                         class="filter-item">
                      <div class="filter-content">
                        <div class="filter-condition">
                          <a-icon type="code" class="filter-icon"/>
                          <span class="filter-key">{{ filter.key }}</span>
                          <span class="filter-operator">{{
                              getOperatorText(filter.operator)
                            }}</span>
                          <span class="filter-value">{{ filter.value }}</span>
                        </div>
                        <div v-if="getCurrentValue(filter.key, logDetail.cdeviceMeta)"
                             class="filter-result">
                          <a-tooltip placement="top"
                                     :title="getFullDeviceData(logDetail.cdeviceMeta)">
                            <span class="current-value">
                              <a-icon type="eye" class="current-value-icon"/>
                              触发值: {{ getCurrentValue(filter.key, logDetail.cdeviceMeta) }}
                            </span>
                          </a-tooltip>
                          <span v-if="isConditionMet(filter, logDetail.cdeviceMeta)"
                                class="condition-status condition-met">
                            <a-icon type="check-circle" class="status-icon"/>
                            满足条件
                          </span>
                          <span v-else class="condition-status condition-not-met">
                            <a-icon type="close-circle" class="status-icon"/>
                            不满足条件
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </div>

        <!-- 动作执行结果 -->
        <div>
          <a-card title="⚡ 动作执行结果" size="small" class="action-result-card">
            <div v-for="(item, idx) in parseLogContext(logDetail.content)" :key="idx"
                 class="action-item">
              <a-row type="flex" align="middle" class="action-header">
                <a-col :span="6">
                  <div class="action-info-item">
                    <a-icon type="thunderbolt" class="action-icon action-icon-primary"/>
                    <b>动作类型：</b>
                    <a-tag color="blue" class="action-tag">{{ triggerLabel(item.trigger) }}</a-tag>
                  </div>
                </a-col>
                <a-col :span="10">
                  <div class="action-info-item">
                    <a-icon type="target" class="action-icon action-icon-success"/>
                    <b>目标：</b>
                    <span class="action-value action-value-success">{{
                        item.targetName || item.target
                      }}</span>
                  </div>
                </a-col>
                <a-col :span="8">
                  <div class="action-info-item">
                    <a-icon type="check-circle" class="action-icon action-icon-success"/>
                    <b>执行结果：</b>
                    <a-tag :color="item.success ? 'green' : 'red'" class="action-tag">
                      <a-icon :type="item.success ? 'check' : 'close'" class="result-icon"/>
                      {{ item.success ? '成功' : '失败' }}
                    </a-tag>
                  </div>
                </a-col>
              </a-row>
              <div class="action-result-content">
                <div class="result-header">
                  <a-icon type="file-text" class="result-icon"/>
                  <b>结果内容：</b>
                </div>
                <a-tooltip placement="top" :title="formatResult(item.result)">
                  <pre :style="item.success ? resultPreStyle : errorPreStyle">{{
                      formatResult(item.result)
                    }}</pre>
                </a-tooltip>
                <a-icon
                  type="copy"
                  class="copy-icon"
                  @click="copyText(formatResult(item.result))"
                  title="复制结果"
                />
              </div>
              <a-divider v-if="idx !== parseLogContext(logDetail.content).length - 1"
                         class="action-divider"/>
            </div>
          </a-card>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import {getRuleLogPage} from '@/api/linkage/linkage'

export default {
  name: 'LinkageLogs',
  data() {
    return {
      list: [],
      loading: false,
      total: 0,
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {
        cName: undefined,
        cStatus: undefined,
        cId: undefined,
        cType: undefined,
        triggerType: undefined,
        pageNum: 1,
        pageSize: 10
      },
      dateRange: [],
      // 表头
      columns: [
        {
          title: this.$t('linkage.sceneName'),
          dataIndex: 'cname',
          ellipsis: true,
          width: '45%',
          align: 'center'
        },
        {
          title: this.$t('linkage.executionStatus'),
          dataIndex: 'cstatus',
          scopedSlots: {customRender: 'cStatus'},
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('linkage.businessType'),
          dataIndex: 'ctype',
          scopedSlots: {customRender: 'cType'},
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          scopedSlots: {customRender: 'createTime'},
          width: '15%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'action',
          width: '17%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      logDetailVisible: false,
      logDetail: null,
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询场景联动日志列表 */
    getList() {
      this.loading = true
      const params = {
        ...this.queryParam,
        createTimeStart: this.dateRange && this.dateRange.length > 0 ? this.dateRange[0]
          : undefined,
        createTimeEnd: this.dateRange && this.dateRange.length > 0 ? this.dateRange[1] : undefined
      }
      getRuleLogPage(params).then(response => {
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
        cName: undefined,
        cStatus: undefined,
        cId: undefined,
        cType: undefined,
        triggerType: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.dateRange = []
      this.handleQuery()
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
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    getStatusColor(status) {
      const colorMap = {
        1: 'green',
        2: 'red',
        0: 'orange'
      }
      return colorMap[status] || 'default'
    },
    getStatusText(status) {
      const textMap = {
        1: '成功',
        2: '失败',
        0: '部分成功'
      }
      return textMap[status] || '未知'
    },
    getTriggerTypeColor(triggerType) {
      const colorMap = {
        'device': 'blue',
        'time': 'green',
        'manual': 'orange',
        'unknown': 'default'
      }
      return colorMap[triggerType] || 'default'
    },
    getTriggerTypeText(triggerType) {
      const textMap = {
        'device': '设备触发',
        'time': '定时触发',
        'manual': '手动触发',
        'unknown': '未知'
      }
      return textMap[triggerType] || '未知'
    },
    formatDate(val) {
      if (!val) {
        return ''
      }
      const d = new Date(val)
      if (isNaN(d.getTime())) {
        return val
      }
      const pad = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
        d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    showLogDetail(log) {
      this.logDetail = log
      this.logDetailVisible = true
    },
    closeLogDetail() {
      this.logDetail = null
      this.logDetailVisible = false
    },
    parseLogContext(content) {
      try {
        return JSON.parse(content)
      } catch (e) {
        return []
      }
    },
    formatResult(result) {
      if (!result) {
        return ''
      }
      if (typeof result === 'string') {
        try {
          return JSON.stringify(JSON.parse(result), null, 2)
        } catch (e) {
          return result
        }
      }
      try {
        return JSON.stringify(result, null, 2)
      } catch (e) {
        return ''
      }
    },
    triggerLabel(type) {
      if (type === 'notice') {
        return '通知'
      }
      if (type === 'device') {
        return '设备联动'
      }
      return type
    },
    resultPreStyle() {
      return {
        display: 'inline',
        whiteSpace: 'pre-wrap',
        wordBreak: 'break-all',
        maxWidth: '100%',
        background: '#f8f8f8',
        borderRadius: '4px',
        padding: '4px 8px',
        margin: 0
      }
    },
    errorPreStyle() {
      return {
        display: 'inline',
        whiteSpace: 'pre-wrap',
        wordBreak: 'break-all',
        maxWidth: '100%',
        background: '#fff0f0',
        borderRadius: '4px',
        padding: '4px 8px',
        margin: 0,
        color: '#f5222d',
        border: '1px solid #ffccc7'
      }
    },
    copyText(text) {
      if (!text) {
        return
      }
      const textarea = document.createElement('textarea')
      textarea.value = text
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('已复制到剪贴板')
    },
    parseDeviceMeta(deviceMetaStr) {
      if (!deviceMetaStr) {
        return null
      }
      try {
        return JSON.parse(deviceMetaStr)
      } catch (e) {
        console.error('解析设备元数据失败:', e)
        return null
      }
    },
    getOperatorText(operator) {
      const operatorMap = {
        'gte': '>=',
        'gt': '>',
        'lte': '<=',
        'lt': '<',
        'eq': '=',
        'ne': '!=',
        'in': '包含',
        'nin': '不包含',
        'like': '模糊匹配',
        'regex': '正则匹配'
      }
      return operatorMap[operator] || operator
    },
    getCurrentValue(key, cdeviceMeta) {
      if (!cdeviceMeta) {
        return null
      }
      try {
        const metaData = JSON.parse(cdeviceMeta)
        if (metaData.actualDeviceData && metaData.actualDeviceData.properties) {
          return metaData.actualDeviceData.properties[key]
        }
        return null
      } catch (e) {
        return null
      }
    },
    isConditionMet(filter, cdeviceMeta) {
      if (!cdeviceMeta) {
        return false
      }
      try {
        const metaData = JSON.parse(cdeviceMeta)
        if (!metaData.actualDeviceData || !metaData.actualDeviceData.properties) {
          return false
        }

        const currentValue = metaData.actualDeviceData.properties[filter.key]
        const expectedValue = parseFloat(filter.value)
        const currentValueNum = parseFloat(currentValue)

        if (isNaN(currentValueNum) || isNaN(expectedValue)) {
          return false
        }

        switch (filter.operator) {
          case 'gte':
            return currentValueNum >= expectedValue
          case 'gt':
            return currentValueNum > expectedValue
          case 'lte':
            return currentValueNum <= expectedValue
          case 'lt':
            return currentValueNum < expectedValue
          case 'eq':
            return currentValueNum === expectedValue
          case 'ne':
            return currentValueNum !== expectedValue
          default:
            return false
        }
      } catch (e) {
        return false
      }
    },
    getFullDeviceData(cdeviceMeta) {
      if (!cdeviceMeta) {
        return '无设备数据'
      }
      try {
        const metaData = JSON.parse(cdeviceMeta)
        if (metaData.actualDeviceData && metaData.actualDeviceData.properties) {
          return JSON.stringify(metaData.actualDeviceData.properties, null, 2)
        }
        return '无设备属性数据'
      } catch (e) {
        return '数据解析失败'
      }
    },
  }
}
</script>

<style scoped lang="less">
/* 设备触发信息样式 */
.device-trigger-section {
  margin-bottom: 24px;
}

.device-trigger-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #dee2e6;
  border-radius: 8px;
}

.device-basic-info {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-icon {
  margin-right: 8px;
  font-size: 14px;
}

.info-icon-primary {
  color: #1890ff;
}

.info-icon-success {
  color: #52c41a;
}

.info-icon-warning {
  color: #fa8c16;
}

.info-icon-purple {
  color: #722ed1;
}

.info-value {
  font-weight: bold;
  margin-left: 4px;
}

.info-value-primary {
  color: #1890ff;
}

.info-value-success {
  color: #52c41a;
}

.info-value-warning {
  color: #fa8c16;
}

.info-tag {
  margin-left: 4px;
}

/* 触发条件样式 */
.trigger-conditions {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.trigger-conditions-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.trigger-icon {
  color: #1890ff;
  margin-right: 8px;
  font-size: 16px;
}

.trigger-conditions-header b {
  font-size: 16px;
  color: #1890ff;
}

.condition-item {
  margin: 12px 0;
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
}

.condition-basic-info {
  margin-bottom: 12px;
}

.condition-info-item {
  display: flex;
  align-items: center;
}

.condition-icon {
  margin-right: 6px;
  font-size: 14px;
}

.condition-icon-success {
  color: #52c41a;
}

.condition-icon-warning {
  color: #fa8c16;
}

.condition-icon-purple {
  color: #722ed1;
}

.condition-value {
  font-weight: bold;
  margin-left: 4px;
}

.condition-value-success {
  color: #52c41a;
}

.condition-tag {
  margin-left: 4px;
}

.filters-header {
  margin-bottom: 8px;
}

.filters-icon {
  color: #1890ff;
  margin-right: 6px;
}

.filter-item {
  margin: 8px 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.filter-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-condition {
  display: flex;
  align-items: center;
}

.filter-icon {
  color: #1890ff;
  margin-right: 6px;
}

.filter-key {
  font-weight: bold;
  color: #1890ff;
}

.filter-operator {
  margin: 0 8px;
  color: #666;
}

.filter-value {
  font-weight: bold;
  color: #fa8c16;
}

.filter-result {
  display: flex;
  align-items: center;
}

.current-value {
  color: #52c41a;
  font-weight: bold;
  cursor: help;
  border-bottom: 1px dashed #52c41a;
  padding: 2px 4px;
  background: #f6ffed;
  border-radius: 4px;
}

.current-value-icon {
  margin-right: 4px;
}

.condition-status {
  margin-left: 12px;
  font-weight: bold;
}

.condition-met {
  color: #52c41a;
}

.condition-not-met {
  color: #f5222d;
}

.status-icon {
  margin-right: 4px;
}

/* 动作执行结果样式 */
.action-result-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 8px;
}

.action-item {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-header {
  margin-bottom: 12px;
}

.action-info-item {
  display: flex;
  align-items: center;
}

.action-icon {
  margin-right: 6px;
  font-size: 14px;
}

.action-icon-primary {
  color: #1890ff;
}

.action-icon-success {
  color: #52c41a;
}

.action-tag {
  margin-left: 4px;
}

.action-value {
  font-weight: bold;
  margin-left: 4px;
}

.action-value-success {
  color: #52c41a;
}

.result-icon {
  margin-right: 4px;
}

.action-result-content {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.result-header .result-icon {
  color: #1890ff;
  margin-right: 6px;
}

.copy-icon {
  margin-left: 8px;
  cursor: pointer;
  color: #1890ff;
}

.action-divider {
  margin: 16px 0;
}
</style>
