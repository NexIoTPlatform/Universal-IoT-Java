<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="应用名称">
                <a-input v-model="queryParam.appName" placeholder="请输入" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="应用状态">
                <a-select v-model="queryParam.appStatus" placeholder="请选择" allow-clear
                          style="width: 100%">
                  <a-select-option value="0">启用</a-select-option>
                  <a-select-option value="1">禁用</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item label="创建时间">
                  <a-range-picker style="width: 100%" v-model="dateRange" valueFormat="YYYY-MM-DD"
                                  format="YYYY-MM-DD" allow-clear/>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="handleQuery" class="btn-primary-small">
                  <iot-icon type="icon-search"/>
                  查询
                </a-button>
                <a-button @click="resetQuery" class="btn-default-small" style="margin-left: 8px">
                  <iot-icon type="icon-refresh"/>
                  重置
                </a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? '收起' : '展开' }}
                  <a-icon :type="advanced ? 'up' : 'down'"/>
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="table-operations btn-group-small">
        <a-button type="primary" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['application:application:add']" class="btn-primary-small">
          <iot-icon type="icon-u-add"/>
          新增
        </a-button>
        <a-button type="primary" :disabled="single" @click="handleEditTable"
                  v-hasPermi="['application:application:edit']" class="btn-primary-small">
          <iot-icon type="icon-u-edit"/>
          修改
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['application:application:remove']" class="btn-danger-small">
          <iot-icon type="icon-u-del"/>
          删除
        </a-button>
        <a-button
          type="dashed"
          shape="circle"
          :loading="loading"
          :style="{float: 'right'}"
          icon="reload"
          @click="getList"
          class="btn-circle-small"/>
      </div>
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="appUniqueId"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false">
        <span slot="appName" slot-scope="text, record">
          <a @click="handleViewDetails(record)" class="app-name-link">{{ record.appName }}</a>
        </span>
        <span slot="appStatus" slot-scope="text, record">
          <a-tag :color="record.appStatus === 0 ? 'green' : 'red'">
            {{ record.appStatus === 0 ? '启用' : '禁用' }}
          </a-tag>
        </span>
        <span slot="createDate" slot-scope="text, record">
          {{ parseTime(record.createDate) }}
        </span>
        <span slot="operation" slot-scope="text, record">
          <a @click="handleEditTable(record)" v-hasPermi="['application:application:edit']">
            <iot-icon type="icon-u-edit"/>
            编辑
          </a>
          <a-divider type="vertical" v-hasPermi="['application:application:edit']"/>
          <a @click="confirmHandleStatus(record)" v-hasPermi="['application:application:edit']">
            <iot-icon :type="record.appStatus === 1 ? 'icon-u-play' : 'icon-u-pause'"/>
            {{ record.appStatus === 1 ? '启用' : '停用' }}
          </a>
          <a-divider type="vertical" v-hasPermi="['application:application:remove']"/>
          <a style="color:#F53F3F" @click="handleDelete(record)"
             v-hasPermi="['application:application:remove']">
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
      <!-- 新增/编辑表单 -->
      <create-form ref="createForm" :statusOptions="statusOptions" :deleteOptions="deleteOptions"
                   @ok="getList"/>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import {
  delApplication,
  listApplication,
  resetSecret,
  updateApplication
} from '@/api/application/application'
import CreateForm from './modules/CreateForm'

export default {
  name: 'ApplicationList',
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
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        appName: undefined,
        appStatus: undefined
      },
      // 状态选项
      statusOptions: [
        {dictLabel: '启用', dictValue: '0'},
        {dictLabel: '禁用', dictValue: '1'}
      ],
      // 删除选项
      deleteOptions: [
        {dictLabel: '正常', dictValue: '0'},
        {dictLabel: '删除', dictValue: '1'}
      ],
      // 表头
      columns: [
        {
          title: '应用名称',
          dataIndex: 'appName',
          ellipsis: true,
          scopedSlots: {customRender: 'appName'},
          align: 'center'
        },
        {
          title: '应用ID',
          dataIndex: 'appId',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '应用描述',
          dataIndex: 'remark',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '状态',
          dataIndex: 'appStatus',
          scopedSlots: {customRender: 'appStatus'},
          align: 'center'
        },
        {
          title: '创建时间',
          dataIndex: 'createDate',
          ellipsis: true,
          scopedSlots: {customRender: 'createDate'},
          align: 'center'
        },
        {
          title: '操作',
          dataIndex: 'action',
          width: '20%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询应用列表 */
    getList() {
      this.loading = true
      listApplication(this.addDateRange(this.queryParam, this.dateRange)).then(response => {
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
      this.dateRange = []
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        appName: undefined,
        appStatus: undefined
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
      this.ids = this.selectedRows.map(item => item.appUniqueId)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    /** 设备绑定 */
    deviceBind(row) {
      this.$router.push({
        name: 'DeviceBind',
        params: {
          appUniqueId: row.appUniqueId
        }
      })
    },
    /** 确认状态操作 */
    confirmHandleStatus(row) {
      const text = row.appStatus === 1 ? '启用' : '停用'
      const status = row.appStatus === 0 ? 1 : 0

      this.$confirm({
        title: `确认${text}应用?`,
        content: `是否${text}应用 "${row.appName}"？`,
        onOk: () => {
          return updateApplication({appUniqueId: row.appUniqueId, appStatus: status})
          .then(() => {
            this.$message.success(text + '成功', 3)
            row.appStatus = status
          })
          .catch(() => {
            this.$message.error(text + '异常', 3)
          })
        },
        onCancel: () => {
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const appUniqueIds = row.appUniqueId || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + appUniqueIds + '的数据',
        onOk() {
          return delApplication(appUniqueIds)
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
      this.$refs.createForm.handleUpdate(row)
    },
    /** 重置密钥操作 */
    handleReset(row) {
      var that = this
      this.$confirm({
        title: '确认重置密钥?',
        content: '重置后需要重新配置应用密钥',
        onOk() {
          return resetSecret({appUniqueId: row.appUniqueId})
          .then(() => {
            that.getList()
            that.$message.success(
              '重置成功',
              3
            )
          })
        },
        onCancel() {
        }
      })
    },
    /** 查看详情操作 */
    handleViewDetails(row) {
      this.$router.push({
        name: 'applicationDetails',
        params: {
          appUniqueId: row.appUniqueId
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
.app-name-link {
  color: #1890ff;
  text-decoration: none;

  &:hover {
    color: #40a9ff;
    text-decoration: underline;
  }
}
</style>
