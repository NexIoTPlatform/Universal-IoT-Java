<template>
  <page-header-wrapper>
    <a-card :bordered="false" class="search-card">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="参数名称">
                <a-input v-model="queryParam.configName" placeholder="请输入"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="参数键名">
                <a-input v-model="queryParam.configKey" placeholder="请输入"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item label="参数类型">
                  <a-select placeholder="请选择" v-model="queryParam.configType" style="width: 100%"
                            allow-clear>
                    <a-select-option v-for="(d, index) in typeOptions" :key="index"
                                     :value="d.dictValue">{{ d.dictLabel }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('time.create')">
                  <a-range-picker style="width: 100%" v-model="dateRange" valueFormat="YYYY-MM-DD"
                                  format="YYYY-MM-DD" allow-clear/>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="handleQuery"><a-icon type="search"/>{{
                    $t('button.search')
                  }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><a-icon
                  type="reload"/>{{ $t('button.reset') }}</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? $t('button.collapse') : $t('button.expand') }}
                  <a-icon :type="advanced ? 'up' : 'down'"/>
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </a-card>
    <a-card :bordered="false" class="table-card">
      <!-- 操作 -->
      <div class="table-operations">
        <a-space>
          <a-button type="primary" @click="$refs.createForm.handleAdd()"
                    v-hasPermi="['system:config:add']">
            <a-icon type="plus"/>
            {{ $t('button.add') }}
          </a-button>
          <a-button type="primary" :disabled="single"
                    @click="$refs.createForm.handleUpdate(undefined, ids)"
                    v-hasPermi="['system:config:edit']" ghost>
            <a-icon type="edit"/>
            {{ $t('button.edit') }}
          </a-button>
          <a-button type="danger" :disabled="multiple" @click="handleDelete"
                    v-hasPermi="['system:config:remove']" ghost>
            <a-icon type="delete"/>
            {{ $t('button.delete') }}
          </a-button>
          <a-button @click="handleExport" v-hasPermi="['system:config:export']">
            <a-icon type="export"/>
            {{ $t('button.export') }}
          </a-button>
          <a-button @click="handleRefreshCache" v-hasPermi="['system:config:remove']">
            <a-icon type="redo"/>
            刷新缓存
          </a-button>
        </a-space>
        <a-button
          type="dashed"
          shape="circle"
          :loading="loading"
          :style="{float: 'right'}"
          icon="reload"
          @click="getList"/>
      </div>
      <!-- 增加修改 -->
      <create-form
        ref="createForm"
        :typeOptions="typeOptions"
        @ok="getList"
      />
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="configId"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false"
        :scroll="{ x: true }">
        <span slot="configType" slot-scope="text, record">
          {{ typeFormat(record) }}
        </span>
        <span slot="createTime" slot-scope="text, record">
          {{ parseTime(record.createTime) }}
        </span>
        <span slot="operation" slot-scope="text, record">
          <a-space>
            <a-button type="link" icon="edit"
                      @click="$refs.createForm.handleUpdate(record, undefined)"
                      v-hasPermi="['system:config:edit']">
              {{ $t('button.edit') }} </a-button>
            <a-button type="link" icon="delete" danger @click="handleDelete(record)"
                      v-hasPermi="['system:config:remove']">
              {{ $t('button.delete') }} </a-button>
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
        :showTotal="total => `共 ${total} 条`"
        @showSizeChange="onShowSizeChange"
        @change="changeSize"
      />
    </a-card>
  </page-header-wrapper>
</template>

<script>

import {delConfig, listConfig, refreshCache} from '@/api/system/config'
import CreateForm from './modules/CreateForm'

export default {
  name: 'ConfigPage',
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
      // 状态数据字典
      typeOptions: [],
      // 日期范围
      dateRange: [],
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        configName: undefined,
        configKey: undefined,
        configType: undefined
      },
      columns: [
        {
          title: '参数主键',
          dataIndex: 'configId',
          align: 'center'
        },
        {
          title: '参数名称',
          dataIndex: 'configName',
          ellipsis: true,
          align: 'center',
          customRender: (text) => {
            return text && text.length > 12
              ? <a-tooltip placement="topLeft" title={text}><span
                style="max-width:120px;display:inline-block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{text}</span>
              </a-tooltip>
              : text
          }
        },
        {
          title: '参数键名',
          dataIndex: 'configKey',
          ellipsis: true,
          align: 'center',
          customRender: (text) => {
            return text && text.length > 12
              ? <a-tooltip placement="topLeft" title={text}><span
                style="max-width:120px;display:inline-block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{text}</span>
              </a-tooltip>
              : text
          }
        },
        {
          title: '参数键值',
          dataIndex: 'configValue',
          ellipsis: true,
          align: 'center',
          customRender: (text) => {
            return text && text.length > 12
              ? <a-tooltip placement="topLeft" title={text}><span
                style="max-width:120px;display:inline-block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{text}</span>
              </a-tooltip>
              : text
          }
        },
        {
          title: '系统内置',
          dataIndex: 'configType',
          scopedSlots: {customRender: 'configType'},
          align: 'center'
        },
        {
          title: this.$t('common.remark'),
          dataIndex: 'remark',
          ellipsis: true,
          align: 'center',
          customRender: (text) => {
            return text && text.length > 12
              ? <a-tooltip placement="topLeft" title={text}><span
                style="max-width:160px;display:inline-block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{text}</span>
              </a-tooltip>
              : text
          }
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          scopedSlots: {customRender: 'createTime'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '15%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  filters: {},
  created() {
    this.getList()
    this.getDicts('sys_yes_no').then(response => {
      this.typeOptions = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询参数列表 */
    getList() {
      this.loading = true
      listConfig(this.addDateRange(this.queryParam, this.dateRange)).then(response => {
          this.list = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    // 参数系统内置字典翻译
    typeFormat(row) {
      return this.selectDictLabel(this.typeOptions, row.configType)
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
        configName: undefined,
        configKey: undefined,
        configType: undefined
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
      this.ids = this.selectedRows.map(item => item.configId)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const configIds = row.configId || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + configIds + '的数据',
        onOk() {
          return delConfig(configIds)
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
    /** 导出按钮操作 */
    handleExport() {
      var that = this
      this.$confirm({
        title: '是否确认导出?',
        content: '此操作将导出当前条件下所有数据而非选中数据',
        onOk() {
          that.download('system/config/export', {
            ...that.queryParam
          }, `config_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    /** 刷新缓存按钮操作 */
    handleRefreshCache() {
      refreshCache().then(response => {
        this.$message.success(
          '刷新成功',
          3
        )
      })
    }
  }
}
</script>

<style lang="less" scoped>
.search-card {
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.table-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.table-operations {
  display: flex;
  justify-content: space-between;
  padding-bottom: 16px;
}

/deep/ .ant-table-pagination {
  padding-top: 16px;
}
</style>
