<template>
  <div class="notice-template-linkage">
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
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
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
        <a-button type="primary" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['rule:linkage:add']">
          <a-icon type="plus"/>
          {{ $t('button.add') }}
        </a-button>
        <a-button type="primary" :disabled="single" @click="handleEditTable"
                  v-hasPermi="['rule:linkage:edit']">
          <a-icon type="edit"/>
          {{ $t('button.edit') }}
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['rule:linkage:remove']">
          <a-icon type="delete"/>
          {{ $t('button.delete') }}
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
        :data-source="list"
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
        <span slot="operation" slot-scope="text, record">
          <a @click="handleEditTable(record)" v-hasPermi="['rule:linkage:edit']">
            <a-icon type="edit"/>
            {{ $t('button.edit') }} </a>
          <a-divider type="vertical" v-hasPermi="['rule:linkage:edit']"/>
          <a @click="confirmHandleStatus(record)" v-hasPermi="['rule:linkage:edit']">
            <a-icon :type="record.status === 1 ? 'play-circle' : 'pause-circle'"/>
            {{ record.status === 1 ? '启用' : '停用' }}
          </a>
          <a-divider type="vertical" v-hasPermi="['rule:linkage:remove']"/>
          <a @click="handleDelete(record)" v-hasPermi="['rule:linkage:remove']">
            <a-icon type="delete"/>
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
        :template="template"
        @ok="getList"
      />
    </a-card>
  </div>
</template>

<script>
import {delLinkage, listLinkage, updateLinkage} from '@/api/linkage/linkage'
import CreateForm from '@/views/rule/linkage/modules/CreateForm'

export default {
  name: 'NoticeTemplateLinkage',
  components: {
    CreateForm
  },
  props: {
    template: {
      type: Object,
      required: true
    }
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
      ids: [],
      loading: false,
      total: 0,
      tableSize: 'middle',
      // 查询参数
      queryParam: {
        sceneName: undefined,
        triggerCondition: undefined,
        pageNum: 1,
        pageSize: 10
      },
      // 表头
      columns: [
        {
          title: this.$t('linkage.sceneName'),
          dataIndex: 'sceneName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('linkage.triggerMethod'),
          dataIndex: 'triggerCondition',
          scopedSlots: {customRender: 'triggerCondition'},
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
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
    /** 查询场景联动列表 */
    getList() {
      this.loading = true
      listLinkage(this.queryParam).then(response => {
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
        sceneName: undefined,
        triggerCondition: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
    },

    /** 多选框选中数据 */
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.$refs.createForm.handleAdd()
    },

    /** 修改按钮操作 */
    handleEditTable(record) {
      this.$refs.createForm.handleUpdate(record)
    },

    /** 提交按钮 */
    handleOk() {
      this.$refs.createForm.submitForm()
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const templateIds = row?.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: `当前选中编号为${templateIds}的数据`,
        onOk: () => {
          return delLinkage(templateIds).then(() => {
            this.onSelectChange([], [])
            this.getList()
            this.$message.success('删除成功')
          })
        }
      })
    },

    /** 启用/停用按钮操作 */
    confirmHandleStatus(row) {
      const text = row.status === 1 ? '启用' : '停用'
      this.$confirm({
        title: `确认要${text}名称为"${row.sceneName}"的场景联动吗?`,
        onOk: () => {
          return updateLinkage(row.id, row.status === 1 ? 0 : 1).then(() => {
            this.$message.success(`${text}成功`)
            this.getList()
          })
        }
      })
    },

    /** 分页大小改变 */
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    /** 分页改变 */
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    /** 获取触发方式 */
    getTrigger(triggerCondition) {
      const triggerMap = {
        'device': '设备触发',
        'time': '定时触发',
        'manual': '手动触发'
      }
      return triggerMap[triggerCondition] || triggerCondition
    }
  }
}
</script>

<style lang="less" scoped>
.notice-template-linkage {
  .table-page-search-wrapper {
    margin-bottom: 16px;
  }

  .table-operations {
    margin-bottom: 16px;
  }

  .ant-table-pagination {
    margin-top: 16px;
  }
}
</style> 