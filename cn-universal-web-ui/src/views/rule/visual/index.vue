<template>
  <page-header-wrapper>
    <a-card :bordered="false" class="search-card">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="流程名称">
                <a-input v-model="queryParam.flowName" placeholder="请输入流程名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('common.status')">
                <a-select v-model="queryParam.status" placeholder="请选择状态" allow-clear>
                  <a-select-option value="enabled">{{ $t('status.enable') }}</a-select-option>
                  <a-select-option value="disabled">{{ $t('status.disable') }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="{ float: 'right', overflow: 'hidden' } || {}">
                <a-button type="primary" @click="handleQuery"><a-icon type="search"/>{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><a-icon
                  type="reload"/>{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </a-card>
    <a-card :bordered="false" class="table-card">
      <div class="table-operations">
        <a-space>
          <a-button type="primary" v-hasPermi="['rule:visual:add']" @click="addNew">
            <a-icon type="plus"/>
            新增可视化流程
          </a-button>
          <a-button @click="goToLegacyRules">
            <a-icon type="code"/>
            传统规则管理
          </a-button>
        </a-space>
      </div>
      <a-table
        :columns="columns"
        :data-source="data"
        rowKey="id"
        :pagination="false"
        :size="tableSize"
        :loading="loading"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      >
        <span slot="status" slot-scope="value">
          <a-tag :color="value === 'disabled' ? 'red' : 'green'">
            {{ value === 'disabled' ? '禁用' : '启用' }}
          </a-tag>
        </span>
        <span slot="operation" slot-scope="text, record">
          <span class="op-btns">
            <a-button type="link" icon="eye" @click="toDetail(record)"
                      v-hasPermi="['rule:visual:detail']">详情</a-button>
            <a-button type="link" icon="edit" @click="toEdit(record)"
                      v-hasPermi="['rule:visual:edit']">{{ $t('button.edit') }}</a-button>
            <a-button type="link" icon="play-circle" @click="executeFlow(record)"
                      v-hasPermi="['rule:visual:execute']">执行</a-button>
            <a-button type="link" :icon="record.status === 'enabled' ? 'pause' : 'caret-right'"
                      style="color: #FF7D00" @click="changeStatus(record)"
                      v-hasPermi="['rule:visual:status']">
              {{ record.status === 'enabled' ? '禁用' : '启用' }}
            </a-button>
            <a-button type="link" icon="delete" danger @click="deleteRecord(record)"
                      v-hasPermi="['rule:visual:del']">{{ $t('button.delete') }}</a-button>
          </span>
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
import {changeRuleFlowStatus, deleteRuleFlow, executeRuleFlow, listRuleFlows} from '@/api/rule/visualRuleFlow'

export default {
  name: 'VisualRuleFlowList',
  data() {
    return {
      loading: false,
      total: 0,
      selectedRowKeys: [],
      tableSize: 'middle',
      // 查询参数
      queryParam: {
        flowName: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '流程名称',
          dataIndex: 'flowName',
          key: 'flowName',
          width: '20%',
          align: 'center'
        },
        {
          title: this.$t('rule.description'),
          dataIndex: 'description',
          key: 'description',
          width: '25%',
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          key: 'createTime',
          width: '15%',
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          key: 'status',
          dataIndex: 'status',
          width: '10%',
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          key: 'operation',
          width: '30%',
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
    /** 查询流程列表 */
    getList() {
      this.loading = true
      listRuleFlows(this.$store.getters.userId, this.queryParam.flowName,
        this.queryParam.status).then((res) => {
        this.data = res.data || []
        this.total = this.data.length
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.data = []
        this.total = 0
      })
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        flowName: '',
        status: ''
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
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    addNew() {
      this.$router.push('/rule/visualFlow/create')
    },
    goToLegacyRules() {
      this.$router.push('/rule/flow')
    },
    toDetail(row) {
      this.$router.push('/rule/visualFlow/detail/' + row.id)
    },
    toEdit(row) {
      this.$router.push('/rule/visualFlow/edit/' + row.id)
    },
    executeFlow(row) {
      this.$confirm({
        title: '确认执行可视化流程?',
        content: '当前选中编号为' + row.id + '的流程',
        onOk: () => {
          executeRuleFlow({flowId: row.id}).then(() => {
            this.$message.success('执行成功', 3)
          }).catch((error) => {
            this.$message.error('执行失败：' + (error.message || '未知错误'), 3)
          })
        }
      })
    },
    changeStatus(row) {
      const that = this
      const newStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
      const statusText = newStatus === 'enabled' ? '启用' : '禁用'

      this.$confirm({
        title: `确认${statusText}所选中数据?`,
        content: '当前选中编号为' + row.id + '的数据',
        onOk() {
          return changeRuleFlowStatus(row.id, newStatus, that.$store.getters.userId).then(() => {
            that.getList()
            that.$message.success(`${statusText}成功`, 3)
          })
        }
      })
    },
    deleteRecord(row) {
      const that = this
      this.$confirm({
        title: `确认删除所选中数据?`,
        content: '当前选中删除为' + row.id + '的数据',
        onOk() {
          return deleteRuleFlow(row.id, that.$store.getters.userId).then(() => {
            that.getList()
            that.$message.success(`删除成功`, 3)
          })
        }
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

.op-btns > .ant-btn {
  margin-right: 4px;
}

.op-btns > .ant-btn:last-child {
  margin-right: 0;
}
</style> 