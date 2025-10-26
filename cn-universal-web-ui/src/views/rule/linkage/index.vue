<template>
  <div>
    <page-header-wrapper>
      <a-card :bordered="false">
        <!-- 条件搜索 -->
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item label="场景名称">
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
                  <a-form-item label="触发方式">
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
                    type="icon-search"/>查询</a-button>
                  <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                    type="icon-refresh"/>重置</a-button>
                  <a @click="toggleAdvanced" style="margin-left: 8px">
                    {{ advanced ? '收起' : '展开' }}
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
            新增
          </a-button>
          <a-button type="primary" :disabled="single" @click="handleEditTable"
                    v-hasPermi="['rule:linkage:edit']">
            <iot-icon type="icon-u-edit"/>
            修改
          </a-button>
          <a-button type="danger" :disabled="multiple" @click="handleDelete"
                    v-hasPermi="['rule:linkage:remove']">
            <iot-icon type="icon-u-del"/>
            删除
          </a-button>
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
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
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
            <a @click="$refs.createForm.manualExec(record)" v-hasPermi="['rule:linkage:exec']">
              <a-icon type="play-circle"/>
              触发
            </a>
            <a-divider type="vertical" v-hasPermi="['rule:linkage:exec']"/>
            <a @click="showLogModal(record)">
              <a-icon type="profile"/>
              日志
            </a>
            <a-divider type="vertical"/>
            <a @click="confirmHandleStatus(record)" v-hasPermi="['rule:linkage:edit']">
              <iot-icon :type="record.status === 1 ? 'icon-u-play' : 'icon-u-pause'"/>
              {{ record.status === 1 ? '启用' : '停用' }}
            </a>
            <a-divider type="vertical" v-hasPermi="['rule:linkage:remove']"/>
            <a @click="handleDelete(record)" v-hasPermi="['rule:linkage:remove']">
              <iot-icon type="icon-u-del"/>
              删除
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
            <a-button type="link" @click="showLogDetail(record)">查看</a-button>
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
        v-model="logDetail"
        title="动作详细日志"
        width="700px"
        :footer="null"
        @cancel="closeLogDetail"
      >
        <div v-if="logDetail">
          <div v-for="(item, idx) in parseLogContext(logDetail.content)" :key="idx">
            <a-row type="flex" align="middle" style="margin-bottom: 4px;">
              <a-col :span="6"><b>动作类型：</b>{{ triggerLabel(item.trigger) }}</a-col>
              <a-col :span="10"><b>目标：</b>{{ item.targetName || item.target }}</a-col>
              <a-col :span="8">
                <b>执行结果：</b>
                <a-tag :color="item.success ? 'green' : 'red'">{{
                    item.success ? '成功' : '失败'
                  }}
                </a-tag>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="24">
                <b>结果内容：</b>
                <a-tooltip placement="top" :title="formatResult(item.result)">
                  <pre :style="item.success ? resultPreStyle : errorPreStyle">{{
                      formatResult(item.result)
                    }}</pre>
                </a-tooltip>
                <a-icon
                  type="copy"
                  style="margin-left:8px;cursor:pointer;"
                  @click="copyText(formatResult(item.result))"
                  title="复制"
                />
              </a-col>
            </a-row>
            <a-divider v-if="idx !== parseLogContext(logDetail.content).length - 1"
                       style="margin: 12px 0;"/>
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
          title: '场景名称',
          dataIndex: 'sceneName',
          ellipsis: true,
          width: '25%',
          align: 'center',
          scopedSlots: {customRender: 'sceneName'}
        },
        {
          title: '触发方式',
          dataIndex: 'triggerCondition',
          scopedSlots: {customRender: 'triggerCondition'},
          width: '20%',
          align: 'center'
        },
        {
          title: '状态',
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          width: '15%',
          align: 'center'
        },
        {
          title: '操作',
          dataIndex: 'action',
          width: '40%',
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
      const id = row.id || this.ids[0]
      this.$refs.createForm.handleUpdate(row)
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
    },
    closeLogDetail() {
      this.logDetail = null
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
</style>
