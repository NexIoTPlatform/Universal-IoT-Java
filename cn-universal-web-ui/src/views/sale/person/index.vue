<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="人员类型" prop="dictType">
                <a-select style="width: 100%" v-model="queryParam.dictType" :allow-clear="false"
                          @change="typeChange">
                  <a-select-option v-for="(d, index) in statusOptions" :key="index"
                                   :value="d.dictValue">
                    {{ d.dictLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="人员名称" prop="dictName">
                <a-input v-model="queryParam.dictLabel" placeholder="请输人员名称" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="创建时间">
                <a-range-picker
                  :show-time="{ format: 'HH:mm:ss' }"
                  style="width: 100%"
                  v-model="dateRange"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm:ss"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="handleQuery"><iot-icon
                  type="icon-search"/>查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['sale:person:add']">
          <iot-icon type="icon-u-add"/>
          新增
        </a-button>
        <a-button
          type="primary"
          :disabled="single"
          @click="$refs.createForm.handleUpdate(undefined, ids)"
          v-hasPermi="['sale:person:edit']"
        >
          <iot-icon type="icon-u-edit"/>
          修改
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['sale:person:del']">
          <iot-icon type="icon-u-del"/>
          删除
        </a-button>
        <a-button
          type="dashed"
          shape="circle"
          :loading="loading"
          :style="{ float: 'right' }"
          icon="reload"
          @click="getList"
        />
      </div>
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="id"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false"
      >
        <span slot="operation" slot-scope="text, record" v-if="record.id !== 1">
          <a @click="$refs.createForm.handleUpdate(record, undefined)"
             v-hasPermi="['sale:person:edit']">
            <iot-icon type="icon-u-edit"/>
            修改
          </a>
          <a-divider type="vertical" v-hasPermi="['sale:person:del']"/>
          <a style="color: #f53f3f" @click="handleDelete(record)" v-hasPermi="['sale:person:del']">
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
        :showTotal="(total) => `共 ${total} 条`"
        @showSizeChange="onShowSizeChange"
        @change="changeSize"
      />
    </a-card>
    <!-- 增加修改 -->
    <!-- 创建/编辑用户,单独封装了组件 -->
    <create-form ref="createForm" :statusOptions="statusOptions" @ok="getList"/>
  </page-header-wrapper>
</template>

<script>
import {delData, listData} from '@/api/system/dict/data'
import CreateForm from './modules/CreateForm'

export default {
  name: 'SalePerson',
  components: {
    CreateForm
  },
  data() {
    return {
      // 人员类型字典
      statusOptions: [],
      // 日期范围
      dateRange: [],
      // 搜索条件
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        dictLabel: undefined,
        dictType: 'sales_staff',
        status: undefined,
        startTime: undefined,
        endTime: undefined
      },
      // table列表
      list: [],
      // 加载
      loading: false,
      // table列表数据项
      columns: [
        {
          title: '人员名称',
          dataIndex: 'username',
          align: 'center'
        },
        {
          title: '联系方式',
          dataIndex: 'phone',
          align: 'center'
        },
        {
          title: '地址',
          dataIndex: 'addr',
          align: 'center'
        },
        {
          title: '创建时间',
          dataIndex: 'createDate',
          align: 'center'
        },
        {
          title: '操作',
          dataIndex: 'operation',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      // 批量
      selectedRowKeys: [],
      selectedRows: [],
      ids: [],
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 数据总数
      total: 0
    }
  },
  filters: {},
  created() {
    this.getDicts('sale_person_type').then(response => {
      this.statusOptions = response.data
      console.log('this.statusOptions = ', this.statusOptions)
    })
    this.getList()
  },
  computed: {},
  watch: {},
  methods: {
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      if (this.dateRange.length > 0) {
        console.log('this.dateRange = ', this.dateRange)
        this.queryParam.startTime = this.dateRange[0]
        this.queryParam.endTime = this.dateRange[1]
      } else {
        this.queryParam.startTime = undefined
        this.queryParam.endTime = undefined
      }
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        dictName: undefined,
        dictType: 'sales_staff',
        status: undefined,
        startTime: undefined,
        endTime: undefined
      }
      this.handleQuery()
    },
    /** 查询用户列表 */
    getList() {
      console.log('form = ', this.queryParam)
      this.loading = true
      this.list = []
      listData(this.queryParam).then(response => {
          response.rows.forEach((item) => {
            this.list.push({
              id: item.dictCode,
              username: item.dictLabel,
              phone: JSON.parse(item.remark).phone,
              addr: JSON.parse(item.remark).addr,
              createDate: item.createTime
            })
          })
          this.total = response.total
          this.loading = false
        }
      ).finally(() => {
        this.loading = false
      })
    },
    // 改变分页大小
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    // 选择分页
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    // 批量选择
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map((item) => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const userIds = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + userIds + '的数据',
        onOk() {
          return delData(userIds).then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success('删除成功', 3)
          })
        },
        onCancel() {
        }
      })
    },
    // 人员类型发生改变
    typeChange(value, option) {
      this.queryParam.pageNum = 1
      if (this.dateRange.length > 0) {
        console.log('this.dateRange = ', this.dateRange)
        this.queryParam.startTime = this.dateRange[0]
        this.queryParam.endTime = this.dateRange[1]
      } else {
        this.queryParam.startTime = undefined
        this.queryParam.endTime = undefined
      }
      this.getList()
    }
  }
}
</script>
