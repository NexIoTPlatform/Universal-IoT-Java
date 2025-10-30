<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="用户名">
                <a-input v-model="queryParam.username" placeholder="请输入"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="手机号">
                <a-input v-model="queryParam.mobile" placeholder="请输入" @keyup.enter="handleQuery"
                         allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery"><iot-icon
                  type="icon-search"/>{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" @click="$refs.selectUser.handleAuthUser()"
                  v-hasPermi="['system:role:add']">
          <iot-icon type="icon-u-add"/>
          添加用户
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="cancelAuthUserAll"
                  v-hasPermi="['system:role:remove']">
          <iot-icon type="icon-u-del"/>
          {{ $t('button.cancel') }}批量授权
        </a-button>
        <a-button type="primary" @click="back">
          <iot-icon type="icon-u-edit"/>
          返回
        </a-button>
        <a-button
          type="dashed"
          shape="circle"
          :loading="loading"
          :style="{float: 'right'}"
          icon="reload"
          @click="getList"/>
      </div>
      <select-user
        ref="selectUser"
        :roleId="queryParam.roleId"
        :statusOptions="statusOptions"
        @ok="getList"
      />
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="userId"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false">
        <span slot="status" slot-scope="text, record">
          {{ statusFormat(record) }}
        </span>
        <span slot="createDate" slot-scope="text, record">
          {{ parseTime(record.createDate) }}
        </span>
        <span slot="operation" slot-scope="text, record">
          <a @click="cancelAuthUser(record)" v-hasPermi="['system:role:remove']">
            <iot-icon type="icon-u-edit"/>
            取消授权
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
</template>

<script>

import {allocatedUserList, authUserCancel, authUserCancelAll} from '@/api/system/role'
import CreateForm from './modules/CreateForm'
import CreateDataScopeForm from './modules/CreateDataScopeForm'
import SelectUser from './modules/SelectUser'

export default {
  name: 'AuthUser',
  components: {
    CreateForm,
    CreateDataScopeForm,
    SelectUser
  },
  data() {
    return {
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      unionIds: [],
      loading: false,
      total: 0,
      // 状态数据字典
      statusOptions: [],
      // 日期范围
      dateRange: [],
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        username: undefined,
        mobile: undefined,
        roleId: ''
      },
      columns: [
        {
          title: '用户名',
          dataIndex: 'username',
          align: 'center'
        },
        {
          title: '用户别名',
          dataIndex: 'alias',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '邮箱',
          dataIndex: 'email',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '手机号',
          dataIndex: 'mobile',
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createDate',
          scopedSlots: {customRender: 'createDate'},
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '20%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  filters: {},
  created() {
  },
  mounted() {
    const roleId = this.$route.query && this.$route.query.roleId
    if (roleId) {
      this.queryParam.roleId = roleId
      this.getList()
      this.getDicts('sys_normal_disable').then(response => {
        this.statusOptions = response.data
      })
    }
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询授权用户列表 */
    getList() {
      this.loading = true
      allocatedUserList(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    statusFormat(row) {
      return this.selectDictLabel(this.statusOptions, row.status)
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
        roleId: this.queryParam.roleId,
        username: undefined,
        mobile: undefined
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
      this.unionIds = this.selectedRows.map(item => item.unionId)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    /** 取消授权按钮操作 */
    cancelAuthUser(row) {
      var that = this
      const username = row.username
      const roleId = this.queryParam.roleId
      this.$confirm({
        title: '确认要取消该用户的角色吗?',
        content: '当前选中用户' + username,
        onOk() {
          const param = {
            unionId: row.unionId,
            roleId: roleId
          }
          return authUserCancel(param)
          .then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success(
              '取消授权成功',
              3
            )
          })
        },
        onCancel() {
        }
      })
    },
    /** 批量取消授权按钮操作 */
    cancelAuthUserAll() {
      var that = this
      const roleId = this.queryParam.roleId
      this.$confirm({
        title: '是否取消选中用户授权数据项?',
        onOk() {
          const param = {
            roleId: roleId,
            unionIds: that.unionIds
          }
          return authUserCancelAll(param)
          .then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success(
              '取消授权成功',
              3
            )
          })
        },
        onCancel() {
        }
      })
    },
    back() {
      this.$router.push({path: '/system/role'})
    }
  }
}
</script>
