<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="产品类型" prop="productKey">
                <a-select
                  style="width: 100%"
                  v-model="queryParam.productKey"
                  :allow-clear="true"
                  show-search
                  placeholder="请选择产品"
                  option-filter-prop="children"
                  :filter-option="filterOption">
                  <a-select-option v-for="(d, index) in productList" :key="index"
                                   :value="d.productKey">
                    {{ d.companyNo + d.classifiedName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="设备序列号" prop="deviceId">
                <a-input v-model="queryParam.deviceId" placeholder="请输入设备序列号" allow-clear/>
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
            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item label="设备状态" prop="dictType">
                  <a-select style="width: 100%" v-model="queryParam.status" :allow-clear="false"
                            placeholder="请选择设备状态">
                    <a-select-option v-for="(d, index) in statusOptions" :key="index"
                                     :value="d.dictValue">
                      {{ d.dictLabel }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="handleQuery"><a-icon type="search"/>查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><a-icon
                  type="redo"/>重置</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? '收起' : '展开' }}
                  <a-icon :type="advanced ? 'up' : 'down'"/>
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['sale:saledev:add']">
          <a-icon type="plus"/>
          新增
        </a-button>
        <a-button type="primary" @click="$refs.importForm.handleImport()"
                  v-hasPermi="['sale:saledev:import']">
          <a-icon type="import"/>
          导入
        </a-button>
        <a-button type="primary" :disabled="multiple"
                  @click="$refs.saleForm.saleUpdate(undefined, ids)"
                  v-hasPermi="['sale:saledev:sale']">
          <a-icon type="transaction"/>
          出售
        </a-button>
        <a-button type="primary" :disabled="single"
                  @click="$refs.createForm.handleUpdate(undefined, ids)"
                  v-hasPermi="['sale:saledev:edit']">
          <a-icon type="edit"/>
          修改
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['sale:saledev:del']">
          <a-icon type="delete"/>
          删除
        </a-button>
        <a-button type="primary" @click="handleExport" v-hasPermi="['sale:saledev:export']">
          <a-icon type="download"/>
          导出
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
        :product-list="productList"
        @ok="getList"
      />
      <!-- 导入 -->
      <import-form
        ref="importForm"
        :product-list="productList"
        @ok="getList"
      />
      <!-- 详情 -->
      <instance-detail
        ref="instanceDetail"
        :product-list="productList"
      />
      <!-- 出售 -->
      <sale-form
        ref="saleForm"
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
        <span slot="deviceId" slot-scope="text, record">
          <span v-if="!record.devId">{{ record.deviceId }}</span>
          <a-button v-else type="link"
                    @click="$router.push(`/system/instance/instance-details/${record.devId}`)">{{
              record.deviceId
            }}</a-button>
        </span>
        <span slot="status" slot-scope="text, record">
          <span v-if="record.status === 0"><a-tag color="red">未售</a-tag></span>
          <span v-else-if="record.status === 1"><a-tag color="blue">已售</a-tag></span>
        </span>
        <span slot="productKey" slot-scope="text, record">
          <span v-for="(d, index) in productList" :key="index">
            <span v-if="d.productKey === record.productKey">{{
                d.companyNo + d.classifiedName
              }}</span>
          </span>
        </span>
        <span slot="operation" slot-scope="text, record">
          <a-divider type="vertical" v-hasPermi="['sale:saledev:detail']"/>
          <a @click="$refs.instanceDetail.checkDetails(record.id)"
             v-hasPermi="['sale:saledev:detail']">
            <a-icon type="file-text"/>详情
          </a>
          <a-divider type="vertical" v-if="record.status === 0" v-hasPermi="['sale:saledev:sale']"/>
          <a style="color: #f53f3f" @click="$refs.saleForm.saleUpdate(record, undefined)"
             v-if="record.status === 0" v-hasPermi="['sale:saledev:sale']">
            <a-icon type="transaction"/>出售
          </a>
          <a-divider type="vertical" v-hasPermi="['sale:saledev:edit', 'sale:saledev:del']"/>
          <a-dropdown v-hasPermi="['sale:saledev:edit', 'sale:saledev:del']">
            <a class="ant-dropdown-link" @click="(e) => e.preventDefault()">
              更多
              <iot-icon type="icon-d-more"/>
            </a>
            <a-menu slot="overlay">
              <a-menu-item v-hasPermi="['sale:saledev:edit']">
                <a @click="$refs.createForm.handleUpdate(record, undefined)"
                   v-hasPermi="['sale:saledev:edit']">
                  <a-icon type="edit"/>修改设备
                </a>
              </a-menu-item>
              <a-menu-item v-hasPermi="['sale:saledev:del']">
                <a @click="handleDelete(record)" v-hasPermi="['sale:saledev:del']">
                  <a-icon type="delete"/>删除设备
                </a>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
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
import {delSale, listSale} from '@/api/sale/sale'
import {listProduct} from '@/api/system/dev/product'
import CreateForm from './modules/CreateForm'
import ImportForm from './modules/ImportForm'
import SaleForm from './modules/SaleForm'
import InstanceDetail from './modules/InstanceDetail'

export default {
  name: 'Sale',
  components: {
    CreateForm,
    ImportForm,
    SaleForm,
    InstanceDetail
  },
  data() {
    return {
      // 产品列表
      productList: [],
      // 时间范围
      dateRange: [],
      // 设备状态字典
      statusOptions: [
        {
          dictValue: '0',
          dictLabel: '未售'
        },
        {
          dictValue: '1',
          dictLabel: '已售'
        }
      ],

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
        deviceId: undefined,
        productKey: undefined,
        status: undefined,
        recode: undefined,
        creatorId: undefined,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '设备序列号',
          dataIndex: 'deviceId',
          ellipsis: true,
          scopedSlots: {customRender: 'deviceId'},
          align: 'center'
        },
        {
          title: '产品类型',
          dataIndex: 'productKey',
          ellipsis: true,
          scopedSlots: {customRender: 'productKey'},
          align: 'center'
        },
        {
          title: '设备状态',
          dataIndex: 'status',
          ellipsis: true,
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: '创建者',
          dataIndex: 'creatorId',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '操作',
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
    listProduct().then(response => {
      this.productList = response.rows
      console.log(this.productList)
    })
    this.getList()
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询sale列表 */
    getList() {
      this.loading = true
      listSale(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
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
        deviceId: undefined,
        productKey: undefined,
        status: undefined,
        recode: undefined,
        creatorId: undefined,
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
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return delSale(ids)
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
          that.download('admin/sale/export', {
            ...that.queryParam
          }, `sale_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    filterOption(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
