<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('rule.ruleName')">
                <a-input v-model="queryParam.ruleName" placeholder="请输入"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {}">
                <a-button type="primary" @click="handleQuery">
                  <a-icon type="search"/>{{ $t('button.query') }} </a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">
                  <a-icon type="reload"/>{{ $t('button.reset') }} </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="table-operations">
        <a-button type="primary" v-hasPermi="['rule:flow:add']" @click="addNew">
          <a-icon type="plus"/>
          {{ $t('button.add') }}
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="getList">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
      <a-table
        :columns="columns"
        :data-source="data"
        rowKey="id"
        :pagination="false"
        :size="tableSize"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      >
        <span slot="ruleName" slot-scope="text, record">
          <a @click="toDetail(record)" style="color: #1890ff; cursor: pointer;">
            {{ text }}
          </a>
        </span>
        <span slot="status" slot-scope="value">
          <a-tag :color="value === 'stop' ? 'red' : 'green'">
            {{ value === 'stop' ? '停止' : '运行中' }}
          </a-tag>
        </span>
        <span slot="operation" slot-scope="text, record">
          <div class="operation-buttons">
            <a-button type="link" :icon="record.status === 'run' ? 'pause' : 'caret-right'"
                      style="color: #FF7D00" @click="changeStatus(record)"
                      v-hasPermi="['rule:flow:status']">
              {{ record.status === 'run' ? '停止' : '运行' }}
            </a-button>
            <a-button type="link" icon="delete" danger @click="deleteRecord(record)"
                      v-hasPermi="['rule:flow:del']">{{ $t('button.delete') }}</a-button>
          </div>
        </span>
      </a-table>
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
  </page-header-wrapper>
</template>

<script>
import {changeRuleStatus, deleteRule, listRule} from '@/api/rule/rule'

export default {
  name: 'RuleFlow',
  data() {
    return {
      loading: false,
      total: 3,
      selectedRowKeys: [],
      selectedRows: [],
      // 账号父id
      queryParam: {
        ruleName: '',
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: this.$t('rule.ruleName'),
          dataIndex: 'ruleName',
          key: 'ruleName',
          scopedSlots: {customRender: 'ruleName'},
          width: '20%',
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          key: 'status',
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          key: 'createTime',
          width: '25%',
          align: 'center'
        },
        {
          title: this.$t('rule.description'),
          dataIndex: 'description',
          key: 'description',
          width: '18%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          key: 'operation',
          width: '25%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      data: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 查询菜单列表 */
    getList() {
      this.loading = true
      listRule(this.queryParam).then((res) => {
        this.data = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        ruleName: '',
        creatorId: this.creatorId
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
      this.ids = this.selectedRows.map((item) => item.configId)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    addNew() {
      this.$router.push('/rule/flow/detail/create')
    },
    toDetail(row) {
      this.$router.push('/rule/flow/detail/' + row.id)
    },
    changeStatus(row) {
      const that = this
      this.$confirm({
        title: `确认${row.status === 'run' ? '停止' : '启用'}所选中数据?`,
        content: '当前选中编号为' + row.id + '的数据',
        onOk() {
          row.status = row.status === 'run' ? 'stop' : 'run'
          return changeRuleStatus(row).then(() => {
            that.getList()
            that.$message.success(`${row.status === 'run' ? '停止' : '启用'}成功`, 3)
          })
        },
        onCancel() {
        }
      })
    },
    deleteRecord(row) {
      const that = this
      this.$confirm({
        title: `确认删除所选中数据?`,
        content: '当前选中删除为' + row.id + '的数据',
        onOk() {
          return deleteRule(row.id).then(() => {
            that.getList()
            that.$message.success(`删除成功`, 3)
          })
        },
        onCancel() {
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 120px;
}

.operation-buttons .ant-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}

.operation-buttons .ant-btn .anticon {
  font-size: 12px;
}
</style>