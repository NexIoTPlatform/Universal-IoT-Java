<template>
  <page-header-wrapper>
    <div class="user-list-container">
      <!-- 条件搜索 -->
      <a-card :bordered="false" class="search-card">
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('user.username')">
                  <a-input v-model="queryParam.username" :placeholder="$t('user.placeholder.username')"
                           @keyup.enter="handleQuery" allow-clear/>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('user.alias')">
                  <a-input v-model="queryParam.alias" :placeholder="$t('user.placeholder.alias')"
                           @keyup.enter="handleQuery" allow-clear/>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('user.mobile')">
                  <a-input v-model="queryParam.mobile" :placeholder="$t('user.placeholder.mobile')"
                           @keyup.enter="handleQuery" allow-clear/>
                </a-form-item>
              </a-col>
              <template v-if="advanced">
                <a-col :md="8" :sm="24">
                  <a-form-item :label="$t('user.status')" prop="status">
                    <a-select :placeholder="$t('user.placeholder.status')" style="width: 100%"
                              v-model="queryParam.status" allow-clear>
                      <a-select-option v-for="(d, index) in statusOptions" :key="index"
                                       :value="d.dictValue">
                        {{ d.dictLabel }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="8" :sm="24">
                  <a-form-item :label="$t('user.createTime')">
                    <a-range-picker
                      style="width: 100%"
                      v-model="dateRange"
                      valueFormat="YYYY-MM-DD"
                      format="YYYY-MM-DD"
                      allow-clear
                    />
                  </a-form-item>
                </a-col>
              </template>
              <a-col :md="(!advanced && 8) || 24" :sm="24">
                <span
                  class="table-page-search-submitButtons"
                  :style="(advanced && { float: 'right', overflow: 'hidden' }) || {}"
                >
                  <a-space>
                    <a-button type="primary" @click="handleQuery"><a-icon
                      type="search"/>{{ $t('button.search') }}</a-button>
                    <a-button @click="resetQuery"><a-icon type="sync"/>{{ $t('button.reset') }}</a-button>
                    <a @click="toggleAdvanced">
                      {{ advanced ? $t('button.collapse') : $t('button.expand') }}
                      <a-icon :type="advanced ? 'up' : 'down'"/>
                    </a>
                  </a-space>
                </span>
              </a-col>
            </a-row>
          </a-form>
        </div>
      </a-card>

      <!-- 内容区域 -->
      <a-card :bordered="false" class="content-card">
        <!-- 操作 -->
        <div class="table-operations"
             v-if="($store.state.user.parentUnionId===undefined||$store.state.user.parentUnionId===null)">
          <a-space>
            <a-button type="primary" @click="$refs.createForm.handleAdd()"
                      v-hasPermi="['system:user:add']">
              <a-icon type="plus"/>
              {{ $t('button.add') }}
            </a-button>
            <a-button
              @click="$refs.createForm.handleUpdate(undefined, ids)"
              :disabled="single"
              v-hasPermi="['system:user:edit']"
            >
              <a-icon type="edit"/>
              {{ $t('button.edit') }}
            </a-button>
            <a-button type="danger" :disabled="multiple" @click="handleDelete"
                      v-hasPermi="['system:user:remove']">
              <a-icon type="delete"/>
              {{ $t('button.delete') }}
            </a-button>
            <a-button @click="handleExport" v-hasPermi="['system:user:export']">
              <a-icon type="export"/>
              {{ $t('button.export') }}
            </a-button>
          </a-space>
          <a-tooltip :title="$t('button.refresh')">
            <a-button shape="circle" :loading="loading" :style="{ float: 'right' }" icon="reload"
                      @click="getList"/>
          </a-tooltip>
        </div>
        <!-- 增加修改 -->
        <create-form ref="createForm" :statusOptions="statusOptions" :sexOptions="sexOptions"
                     @ok="getList"/>
        <!-- 修改密码抽屉 -->
        <reset-password ref="resetPassword"/>
        <!-- 分配角色模态框 -->
        <auth-role ref="authRole"/>
        <!-- 上传文件 -->
        <import-excel ref="importExcel" @ok="getList"/>
        <!-- 数据展示 -->
        <a-table
          :loading="loading"
          :size="tableSize"
          rowKey="id"
          :columns="columns"
          :data-source="list"
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
          :pagination="false"
          class="user-table"
        >
          <span slot="status" slot-scope="text, record">
            <a-popconfirm
              :ok-text="$t('button.yes')"
              :cancel-text="$t('button.no')"
              @confirm="confirmHandleStatus(record)"
              @cancel="cancelHandleStatus(record)"
            >
              <span
                slot="title"
              >{{
                  $t('user.confirm.status', {
                    action: record.status === '1' ? $t('common.enable') : $t('common.disable'),
                    name: record.alias
                  })
                }}</span
              >
              <a-switch :checked="record.status === '0'"/>
            </a-popconfirm>
          </span>
          <span slot="createDate" slot-scope="text, record">
            {{ parseTime(record.createDate) }}
          </span>
          <span slot="loginDate" slot-scope="text, record">
            {{ parseTime(record.loginDate) }}
          </span>
          <span slot="operation" slot-scope="text, record"
                v-if="record.id !== 1 && ($store.state.user.parentUnionId===undefined||$store.state.user.parentUnionId===null)">
            <a-space>
              <a-button type="link" size="small"
                        @click="$refs.createForm.handleUpdate(record, undefined)"
                        v-hasPermi="['system:user:edit']">
                        {{ $t('button.edit') }} 
              </a-button>
              <a-button type="link" danger size="small" @click="handleDelete(record)"
                        v-hasPermi="['system:user:remove']">
                {{ $t('button.delete') }} </a-button>
              <a-dropdown v-hasPermi="['system:user:resetPwd', 'system:user:edit']">
                <a-button type="link" size="small" @click="(e) => e.preventDefault()">
                  {{ $t('button.more') }}  <a-icon type="down"/>
                </a-button>
                <a-menu slot="overlay">
                  <a-menu-item v-hasPermi="['system:user:resetPwd']">
                    <a @click="$refs.resetPassword.handleResetPwd(record)">
                      <a-icon type="key"/> {{ $t('button.reset.password') }}
                    </a>
                  </a-menu-item>
                  <a-menu-item v-hasPermi="['system:user:edit']">
                    <a @click="$refs.authRole.handleAuthRole(record)">
                      <a-icon type="check-circle"/>{{ $t('button.assign.role') }} 
                    </a>
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </a-space>
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
          :showTotal="(total) => `共 ${total} 条`"
          @showSizeChange="onShowSizeChange"
          @change="changeSize"
        />
      </a-card>
    </div>
  </page-header-wrapper>
</template>

<script>
import {changeUserStatus, delUser, listUser} from '@/api/system/user'
import AuthRole from './modules/AuthRole'
import ResetPassword from './modules/ResetPassword'
import CreateForm from './modules/CreateForm'
import ImportExcel from './modules/ImportExcel'

export default {
  name: 'User',
  components: {
    AuthRole,
    ResetPassword,
    CreateForm,
    ImportExcel
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
      // 状态数据字典
      statusOptions: [],
      sexOptions: [],

      // 日期范围
      dateRange: [],
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        username: undefined,
        mobile: undefined,
        status: undefined,
        alias: undefined
      },
      columns: [
        {
          title: this.$t('user.username'),
          dataIndex: 'username',
          align: 'center'
        },
        {
          title: this.$t('user.alias'),
          dataIndex: 'alias',
          align: 'center'
        },
        {
          title: this.$t('user.mobile'),
          dataIndex: 'mobile',
          align: 'center'
        },
        {
          title: this.$t('user.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: this.$t('user.createTime'),
          dataIndex: 'createDate',
          scopedSlots: {customRender: 'createDate'},
          align: 'center'
        },
        {
          title: this.$t('user.lastLoginTime'),
          dataIndex: 'loginDate',
          scopedSlots: {customRender: 'loginDate'},
          align: 'center'
        },
        {
          title: this.$t('common.remark'),
          dataIndex: 'remark',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          scopedSlots: {customRender: 'operation'},
          align: 'center',
          width: '200px'
        }
      ]
    }
  },
  filters: {},
  created() {
    console.log(this.$store.state)
    this.getList()
    this.getDicts('sys_normal_disable').then((response) => {
      this.statusOptions = response.data
    })
    this.getDicts('sys_user_sex').then((response) => {
      this.sexOptions = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询用户列表 */
    getList() {
      this.loading = true
      listUser(this.addDateRange(this.queryParam, this.dateRange)).then((response) => {
        this.list = response.rows
        console.log(this.list)
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
        username: undefined,
        mobile: undefined,
        status: undefined,
        alias: undefined
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
      this.ids = this.selectedRows.map((item) => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    /* 用户状态修改 */
    confirmHandleStatus(row) {
      const text = row.status === '1' ? this.$t('common.enable') : this.$t('common.disable')
      row.status = row.status === '0' ? '1' : '0'
      const data = {
        id: row.id,
        unionId: row.unionId,
        status: row.status,
        mobile: row.mobile,
        username: row.username
      }
      changeUserStatus(data)
        .then(() => {
          this.$message.success(text + this.$t('common.success'), 3)
        })
        .catch(function () {
          this.$message.error(text + this.$t('common.error'), 3)
        })
    },
    cancelHandleStatus(row) {
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const userIds = row.id || this.ids
      this.$confirm({
        title: this.$t('user.confirm.delete.title'),
        content: this.$t('user.confirm.delete.content', {ids: userIds}),
        onOk() {
          return delUser(userIds).then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success(that.$t('user.success.delete'), 3)
          })
        },
        onCancel() {
        }
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      var that = this
      this.$confirm({
        title: '是否确认导出?',
        content: '此操作将导出当前条件下所有数据而非选中数据',
        onOk() {
          that.download(
            'admin/system/user/export',
            {
              ...that.queryParam
            },
            `user_${new Date().getTime()}.xlsx`
          )
        },
        onCancel() {
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.user-list-container {
  .search-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  }

  .content-card {
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  }

  .table-operations {
    padding-bottom: 20px;
    border-bottom: 1px solid #e8e8e8;
    margin-bottom: 20px;
  }

  .user-table {
    .ant-btn-link {
      padding: 0;
      height: auto;
    }
  }

  .ant-table-pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
