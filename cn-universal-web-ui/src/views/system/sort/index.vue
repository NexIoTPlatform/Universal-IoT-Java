<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="标识" prop="identification">
                <a-input v-model="queryParam.identification" placeholder="请输入标识"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="分类名称" prop="classifiedName">
                <a-input v-model="queryParam.classifiedName" placeholder="请输入分类名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
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
        <a-button type="primary" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['system:sort:add']">
          <iot-icon type="icon-u-add"/>{{ $t('button.add') }}
        </a-button>
        <a-button type="primary" :disabled="single"
                  @click="$refs.createForm.handleUpdate(undefined, ids)"
                  v-hasPermi="['system:sort:edit']">
          <iot-icon type="icon-u-edit"/>{{ $t('button.edit') }}
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['system:sort:remove']">
          <iot-icon type="icon-u-del"/>{{ $t('button.delete') }}
        </a-button>
        <a-button type="primary" @click="handleExport" v-hasPermi="['system:sort:export']">
          <iot-icon type="icon-u-export"/>{{ $t('button.export') }}
        </a-button>
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
        :sortOptions="sortOptions"
        @select-tree="getTreeselect"
        @ok="getList"
      />
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="id"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false">
        <span slot="operation" slot-scope="text, record">
          <a @click="$refs.createForm.handleUpdate(record, undefined)"
             v-hasPermi="['system:sort:edit']">
            <iot-icon type="icon-u-edit"/>
            {{ $t('button.edit') }} </a>
          <a-divider type="vertical" v-hasPermi="['system:sort:add']"
                     v-show="record.id.length!==9"/>
          <a @click="$refs.createForm.handleAdd(record)" v-hasPermi="['system:sort:add']"
             v-show="record.id.length!==9">
            <iot-icon type="icon-u-add"/>
            {{ $t('button.add') }} </a>
          <a-divider type="vertical" v-hasPermi="['system:sort:remove']"/>
          <a style="color:#F53F3F" @click="handleDelete(record)"
             v-hasPermi="['system:sort:remove']">
            <iot-icon type="icon-u-del"/>
            {{ $t('button.delete') }} </a>
        </span>
      </a-table>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import {delSort, listSort} from '@/api/system/dev/sort'
import CreateForm from './modules/CreateForm'

export default {
  name: 'Sort',
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
      sortOptions: [],
      // 查询参数
      queryParam: {
        parentId: null,
        hasChild: null,
        identification: null,
        classifiedName: null,
        description: null,
        createTime: null,
        createBy: null,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '分类ID',
          dataIndex: 'id',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '标识',
          dataIndex: 'identification',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '分类名称',
          dataIndex: 'classifiedName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '说明',
          dataIndex: 'description',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '22%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  filters: {},
  created() {
    this.getList()
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询产品分类列表 */
    getList() {
      this.loading = true
      listSort(this.queryParam).then(response => {
        this.list = this.handleTree(response.data, 'id', 'parentId')
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
        parentId: undefined,
        hasChild: undefined,
        identification: undefined,
        classifiedName: undefined,
        description: undefined,
        createTime: undefined,
        createBy: undefined,
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
    /** 查询菜单下拉树结构 */
    getTreeselect() {
      listSort().then(response => {
        this.sortOptions = []
        const data = {id: 0, classifiedName: '主目录', children: []}
        data.children = this.handleTree(response.data, 'id', 'parentId')
        this.sortOptions.push(data)
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return delSort(ids)
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
          that.download('system/sort/export', {
            ...that.queryParam
          }, `sort_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    }
  }
}
</script>
