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
                  <a-input v-model="queryParam.sceneName" placeholder="请输入" allow-clear/>
                </a-form-item>
              </a-col>
              <!-- <a-col :md="8" :sm="24">
                <a-form-item label="设备ID">
                  <a-input v-model="queryParam.devId" placeholder="请输入" allow-clear/>
                </a-form-item>
              </a-col> -->
              <template v-if="advanced">
                <a-col :md="8" :sm="24">
                  <a-form-item :label="$t('linkage.triggerMethod')">
                    <a-select v-model="queryParam.triggerCondition" placeholder="请选择" allow-clear
                              style="width: 100%">
                      <a-select-option value="device">设备触发</a-select-option>
                      <a-select-option value="time">定时触发</a-select-option>
                      <a-select-option value="manual">手动触发</a-select-option>
                    </a-select>
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
          <a-button type="primary" @click="$refs.createForm.handleAdd()"
                    v-hasPermi="['rule:linkage:add']">
            <iot-icon type="icon-u-add"/>
            {{ $t('button.add') }}</a-button>
          <a-button type="primary" :disabled="single" @click="handleEditTable"
                    v-hasPermi="['rule:linkage:edit']">
            <iot-icon type="icon-u-edit"/>
            {{ $t('button.edit') }}</a-button>
          <a-button type="danger" :disabled="multiple" @click="handleDelete"
                    v-hasPermi="['rule:linkage:remove']">
            <iot-icon type="icon-u-del"/>
            {{ $t('button.delete') }}</a-button>
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
          :row-selection="{ 
            selectedRowKeys: selectedRowKeys, 
            onChange: onSelectChange,
            type: 'radio'
          }"
          :pagination="false">
          <span slot="triggerCondition" slot-scope="text, record">
            {{ getTrigger(record.triggerCondition) }}
          </span>
          <span slot="status" slot-scope="text, record">
            <a-tag :color="record.status === 0 ? 'green' : 'red'">
              {{ record.status === 1 ? '未发布' : '已发布' }}
            </a-tag>
          </span>
          <span slot="sceneName" slot-scope="text, record">
            <a @click="handleEditTable(record)" style="color: #1890ff; cursor: pointer;">
              {{ record.sceneName }}
            </a>
          </span>
          <span slot="operation" slot-scope="text, record">
            <a @click="handleTriggerConfirm(record)" v-hasPermi="['rule:linkage:exec']">
              <a-icon type="play-circle"/>
              {{ $t('linkage.trigger') }}
            </a>
            <a-divider type="vertical" v-hasPermi="['rule:linkage:exec']"/>
            <a @click="showLogModal(record)">
              <a-icon type="profile"/>
              {{ $t('linkage.logs') }}
            </a>
            <a-divider type="vertical"/>
            <a @click="confirmHandleStatus(record)" v-hasPermi="['rule:linkage:edit']">
              <iot-icon :type="record.status === 1 ? 'icon-u-play' : 'icon-u-pause'"/>
              {{ record.status === 1 ? '启用' : '停用' }}
            </a>
            <a-divider type="vertical" v-hasPermi="['rule:linkage:remove']"/>
            <a @click="handleDelete(record)" v-hasPermi="['rule:linkage:remove']">
              <iot-icon type="icon-u-del"/>
              {{ $t('button.delete') }} </a>
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
        <!-- 增加修改 -->
        <create-form
          ref="createForm"
          @ok="getList"
        />
      </a-card>
    </page-header-wrapper>
    <a-modal
      v-model="logModalVisible"
      title="执行日志"
      width="900px"
      :footer="null"
      @cancel="logModalVisible = false"
    >
      <a-table
        :dataSource="logList"
        :loading="logModalLoading"
        row-key="id"
        :pagination="false"
        bordered
        size="small"
      >
        <a-table-column title="ID" dataIndex="id" width="60"/>
        <a-table-column title="状态" dataIndex="cstatus" width="40">
          <template slot-scope="text, record">
            <a-tag
              :color="record.cstatus === 1 ? 'green' : (record.cstatus === 2 ? 'red' : 'orange')">
              {{ record.cstatus === 1 ? '成功' : (record.cstatus === 2 ? '失败' : '部分成功') }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="时间" dataIndex="createTime" width="120">
          <template slot-scope="text, record">
            {{ formatDate(record.createTime) }}
          </template>
        </a-table-column>
        <a-table-column title="触发方式" dataIndex="conditions" width="80">
          <template slot-scope="text, record">
            {{ formatDate(record.conditions) }}
          </template>
        </a-table-column>
        <a-table-column title="动作日志" width="80">
          <template slot-scope="text, record">
            <a-button type="link" @click="showLogDetail(record)">{{ $t('app.view') }}</a-button>
          </template>
        </a-table-column>
      </a-table>
      <a-pagination
        style="margin-top: 12px; text-align: right;"
        :current="logPageNum"
        :page-size="logPageSize"
        :total="logTotal"
        show-size-changer
        show-quick-jumper
        @change="handleLogPageChange"
        @showSizeChange="handleLogPageChange"
      />
      <a-modal
        v-model="logDetailVisible"
        title="动作详细日志"
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
                        <span class="info-value info-value-primary">{{ parseDeviceMeta(logDetail.cdeviceMeta).sceneName }}</span>
                      </div>
                    </a-col>
                    <a-col :span="8">
                      <div class="info-item">
                        <a-icon type="key" class="info-icon info-icon-warning"/>
                        <b>所属产品：</b>
                        <span class="info-value info-value-warning">{{ parseDeviceMeta(logDetail.cdeviceMeta).productKey }}</span>
                      </div>
                    </a-col>
                    <a-col :span="8">
                      <div class="info-item">
                        <a-icon type="mobile" class="info-icon info-icon-success"/>
                        <b>设备ID：</b>
                        <span class="info-value info-value-success">{{ parseDeviceMeta(logDetail.cdeviceMeta).deviceId }}</span>
                      </div>
                    </a-col>
                    <a-col :span="8">
                      <div class="info-item">
                        <a-icon type="message" class="info-icon info-icon-purple"/>
                        <b>消息类型：</b>
                        <a-tag color="purple" class="info-tag">{{ (parseDeviceMeta(logDetail.cdeviceMeta).messageType) === 'PROPERTIES' ? '属性' : '事件' }}</a-tag>
                      </div>
                    </a-col>
                   
                  </a-row>
                </div>
                <!-- 触发条件 -->
                <div v-if="parseDeviceMeta(logDetail.cdeviceMeta).triggerConditions" class="trigger-conditions">
                  <div class="trigger-conditions-header">
                    <a-icon type="filter" class="trigger-icon"/>
                    <b>触发条件</b>
                  </div>
                  <div v-for="(condition, idx) in parseDeviceMeta(logDetail.cdeviceMeta).triggerConditions" :key="idx" 
                       class="condition-item">
                    <a-row class="condition-basic-info">
                      <a-col :span="10">
                        <div class="condition-info-item">
                          <a-icon type="desktop" class="condition-icon condition-icon-success"/>
                          <b>设备名称：</b>
                          <span class="condition-value condition-value-success">{{ condition.deviceName || condition.deviceId }}</span>
                        </div>
                      </a-col>
                      <a-col :span="10">
                        <div class="condition-info-item">
                          <a-icon type="desktop" class="condition-icon condition-icon-success"/>
                          <b>设备ID：</b>
                          <span class="condition-value condition-value-success">{{ condition.deviceId }}</span>
                        </div>
                      </a-col>
                      <!-- <a-col :span="6">
                        <div class="condition-info-item">
                          <a-icon type="thunderbolt" class="condition-icon condition-icon-purple"/>
                          <b>触发方式：</b>
                          <a-tag color="purple" class="condition-tag">{{ condition.trigger === 'device' ? '设备触发' : condition.trigger }}</a-tag>
                        </div>
                      </a-col>
                      <a-col :span="6">
                        <div class="condition-info-item">
                          <a-icon type="message" class="condition-icon condition-icon-warning"/>
                          <b>消息类型：</b>
                          <a-tag :color="condition.type === 'properties' ? 'blue' : 'green'" class="condition-tag">
                            {{ condition.type === 'properties' ? '属性' : '事件' }}
                          </a-tag>
                        </div>
                      </a-col> -->
                      
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
                            <span class="filter-operator">{{ getOperatorText(filter.operator) }}</span>
                            <span class="filter-value">{{ filter.value }}</span>
                          </div>
                          <div v-if="getCurrentValue(filter.key, logDetail.cdeviceMeta)" class="filter-result">
                            <a-tooltip placement="top" :title="getFullDeviceData(logDetail.cdeviceMeta)">
                              <span class="current-value">
                                <a-icon type="eye" class="current-value-icon"/>
                                触发值: {{ getCurrentValue(filter.key, logDetail.cdeviceMeta) }}
                              </span>
                            </a-tooltip>
                            <span v-if="isConditionMet(filter, logDetail.cdeviceMeta)" class="condition-status condition-met">
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
                      <span class="action-value action-value-success">{{ item.targetName || item.target }}</span>
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
    </a-modal>
  </div>
</template>

<script>
import {delLinkage, getLinkageLogs, listLinkage, updateLinkage} from '@/api/linkage/linkage'
import CreateForm from './modules/CreateForm'

export default {
  name: 'Linkage',
  components: {
    CreateForm
  },
  data() {
    return {
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      ids: [],
      loading: false,
      total: 0,
      // 查询参数
      queryParam: {
        sceneName: undefined,
        touch: undefined,
        triggerCondition: undefined,
        execAction: undefined,
        sleepCycle: undefined,
        status: undefined,
        devId: undefined,
        pageNum: 1,
        pageSize: 10
      },
      // 表头
      columns: [
        {
          title: this.$t('linkage.sceneName'),
          dataIndex: 'sceneName',
          ellipsis: true,
          width: '45%',
          align: 'center',
          scopedSlots: {customRender: 'sceneName'}
        },
        {
          title: this.$t('linkage.triggerMethod'),
          dataIndex: 'triggerCondition',
          scopedSlots: {customRender: 'triggerCondition'},
          width: '10%',
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          width: '10%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'action',
          width: '25%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      logModalVisible: false,
      logModalLoading: false,
      logModalScene: {},
      logList: [],
      logTotal: 0,
      logPageNum: 1,
      logPageSize: 10,
      logDetail: null,
      logDetailVisible: false,
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询场景联动列表 */
    getList() {
      this.loading = true
      listLinkage(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 获取触发方式
    getTrigger(val) {
      let result = ''
      if (!val) {
        return ''
      }
      if (val.indexOf('device') > -1) {
        result += '设备触发 '
      }
      if (val.indexOf('time') > -1) {
        result += '定时触发 '
      }
      if (val.indexOf('manual') > -1) {
        result += '手动触发 '
      }
      return result
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        sceneName: undefined,
        touch: undefined,
        triggerCondition: undefined,
        execAction: undefined,
        sleepCycle: undefined,
        status: undefined,
        devId: undefined,
        pageNum: 1,
        pageSize: 10
      }
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
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    confirmHandleStatus(row) {
      const text = row.status === 1 ? '启用' : '停用'
      const status = row.status === 0 ? 1 : 0
      updateLinkage({id: row.id, status: status})
        .then(() => {
          this.$message.success(text + '成功', 3)
          row.status = status
        })
        .catch(() => {
          this.$message.error(text + '异常', 3)
        })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中 ' + row.sceneName + ' 场景',
        onOk() {
          return delLinkage(ids)
            .then(() => {
              that.onSelectChange([], [])
              that.getList()
              that.$message.success(
                '删除成功',
                3
              )
            })
        },
        onCancel() {
        }
      })
    },
    /** 修改按钮操作 */
    handleEditTable(row) {
      if (row && row.id) {
        // 如果传入了行数据，直接使用
        this.$refs.createForm.handleUpdate(row)
      } else if (this.selectedRows.length === 1) {
        // 如果没有传入行数据，使用选中的行
        this.$refs.createForm.handleUpdate(this.selectedRows[0])
      } else {
        this.$message.warning('请选择一条记录进行修改')
      }
    },
    /** 触发确认弹窗 */
    handleTriggerConfirm(record) {
      this.$confirm({
        title: '确认触发',
        content: `确定要触发场景联动"${record.sceneName}"吗？`,
        okText: '确定',
        cancelText: '取消',
        onOk: () => {
          this.$refs.createForm.manualExec(record)
        }
      })
    },

    showLogModal(scene) {
      this.logModalScene = scene
      this.logModalVisible = true
      this.logPageNum = 1
      this.loadLogList()
    },
    loadLogList() {
      this.logModalLoading = true
      getLinkageLogs(this.logModalScene.id,
        {pageNum: this.logPageNum, pageSize: this.logPageSize}).then(res => {
        console.log('接口返回', res)
        if (res && Array.isArray(res.rows)) {
          this.logList = res.rows
          this.logTotal = res.total || res.rows.length
        } else if (Array.isArray(res)) {
          this.logList = res
          this.logTotal = res.length
        } else {
          this.logList = []
          this.logTotal = 0
        }
        console.log('最终赋值', this.logList)
        this.logModalLoading = false
      }).catch(() => {
        this.logModalLoading = false
      })
    },
    handleLogPageChange(page, pageSize) {
      this.logPageNum = page
      this.logPageSize = pageSize
      this.loadLogList()
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
          // 尝试格式化 JSON 字符串
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
    formatDate(val) {
      if (!val) {
        return ''
      }
      // 兼容 ISO8601 带时区格式
      const d = new Date(val)
      if (isNaN(d.getTime())) {
        return val
      }
      const pad = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
        d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
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
      if (!cdeviceMeta) return null
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
      if (!cdeviceMeta) return false
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
      if (!cdeviceMeta) return '无设备数据'
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
.icon-eye {
  color: #1966ff;
}

.icon-stop {
  color: #f5222d;
}

.icon-start {
  color: #52c41a;
}

.icon-wrapper {
  background: #fff;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  box-shadow: 0 0 6px 3px #efefef;
  display: flex;
  align-items: center;
  justify-content: center;

  .database {
    color: #1966ff;
    font-size: 20px;
  }
}

.card {
  padding: 16px;
  background: #ffffff;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #e5e8ef;
}

.device-name {
  height: 22px;
  line-height: 22px;
  font-size: 14px;
  font-weight: bold;
  color: #1d2129;
}

.device-id {
  height: 20px;
  font-size: 12px;
  color: #86909c;
  line-height: 20px;
}

.device-info {
  margin: 12px 0 34px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.device-label {
  margin-right: 12px;
  height: 20px;
  font-size: 12px;
  color: #86909c;
  line-height: 20px;
}

.device-num {
  font-size: 12px;
  color: #1966ff;
}

.publish {
  font-size: 12px;
  color: #4e5969;
  display: flex;
  align-items: center;

  &::before {
    content: '';
    display: block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: #52c41a;
    margin-right: 4px;
  }
}

.unpublish {
  font-size: 12px;
  color: #4e5969;
  display: flex;
  align-items: center;

  &::before {
    content: '';
    display: block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: #f5222d;
    margin-right: 4px;
  }
}

.device-opt {
  display: flex;
  justify-content: flex-end;
}

.loading-wrap {
  width: 100%;
  height: 400px;
  text-align: center;
  line-height: 400px;
}

.ant-table {
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  background: #fff;
}

.ant-table-thead > tr > th {
  background: #fafbfc;
  font-weight: 600;
  color: #333;
  font-size: 14px;
  border-bottom: none !important;
  padding: 12px 8px;
}

.ant-table-tbody > tr > td {
  font-size: 13px;
  color: #444;
  border-bottom: none !important;
  background: #fff;
  padding: 12px 8px;
  transition: background 0.2s;
}

.ant-table-tbody > tr:nth-child(odd) > td {
  background: #fafbfc;
}

.ant-table-tbody > tr:hover > td {
  background: #e6f7ff !important;
}

.ant-table-wrapper .ant-table {
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.ant-table-pagination {
  border-top: none !important;
  margin-top: 16px;
}

// 统一操作按钮样式
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 120px;
}

.operation-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}

.operation-btn .anticon {
  font-size: 12px;
}

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
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
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
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
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
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
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
