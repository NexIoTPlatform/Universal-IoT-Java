<template>
  <a-card :bordered="false">
    <!-- 条件搜索 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="协议名" prop="name">
              <a-input v-model="queryParam.name" placeholder="请输入协议名称" allow-clear/>
            </a-form-item>
          </a-col>
          <template>
            <a-col :md="8" :sm="24">
              <a-form-item label="状态" prop="state">
                <a-select placeholder="请选择发布状态" style="width: 100%"
                          v-model="queryParam.state" allow-clear>
                  <a-select-option v-for="(d, index) in protocolStateOptions" :key="index"
                                   :value="d.dictValue">
                    {{ d.dictLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span
              class="table-page-search-submitButtons"
              :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="handleQuery"><a-icon type="search"/>查询</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><a-icon
                type="reload"/>重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 操作 -->
    <div class="table-operations">
      <a-space>
        <a-button type="primary" @click="$refs.createForm.productAdd()"
                  v-hasPermi="['protocol:protocol:add']" v-if="userName===$store.state.user.name"
                  :disabled="list.length !== 0">
          <a-icon type="plus"/>
          新增
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['protocol:protocol:remove']" ghost>
          <a-icon type="delete"/>
          删除
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
      :protocolTypeOptions="protocolTypeOptions"
      :protocol-state-options="protocolStateOptions"
      :product-option="productOptions"
      :product-key="productKey"
      :product-names="productName"
      :connectionOptions="connectionOptions"
      @ok="getList"
    />
    <codec-test ref="codecTest"/>
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
      <!--         <span slot="state" slot-scope="text, record">-->
      <!--          {{ record.state === 1 ? '已发布' : '未发布' }}-->
      <!--        </span>-->
      <span slot="state" slot-scope="text, record">
        <a-popconfirm
          :disabled="userName!==$store.state.user.name"
          ok-text="是"
          cancel-text="否"
          @confirm="confirmHandleStatus(record)"
          @cancel="cancelHandleStatus(record)"
        >
          <span slot="title">确认<b>{{ record.state === 0 ? '发布' : '停用' }}</b> {{ record.name }} 协议吗?</span>
          <a-switch default-checked :checked="record.state == 1"
                    :disabled="userName!==$store.state.user.name"/>
          <!-- <a-switch checked-children="已发布" un-checked-children="未发布" :checked="record.state == 1" :disabled="userName!==$store.state.user.name"/> -->
        </a-popconfirm>
      </span>
      <span slot="operation" slot-scope="text, record">
        <a-space>
          <a-button type="link" icon="edit"
                    @click="$refs.createForm.handleUpdate(record, undefined)"
                    v-hasPermi="['protocol:protocol:edit']"
                    v-if="userName===$store.state.user.name">
            修改
          </a-button>
          <a-button type="link" icon="bug" @click="goToMagicWeb"
                    v-hasPermi="['protocol:protocol:codec']">
            开发调试
          </a-button>
          <a-button
            type="link"
            icon="delete"
            danger
            @click="handleDelete(record)"
            v-hasPermi="['protocol:protocol:remove']"
            v-if="userName===$store.state.user.name">
            删除
          </a-button>
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
</template>

<script>
import {delProtocol, listProtocol, updateProtocol} from '@/api/system/protocol'
import {listProduct} from '@/api/system/dev/product'
import CreateForm from '@/views/system/protocol/modules/CreateForm'
import CodecTest from '@/views/system/protocol/modules/codecTest'

export default {
  name: 'ProductProtocol',
  components: {
    CreateForm,
    CodecTest
  },
  props: {
    productKey: {
      type: String,
      require: true,
      default: ''
    },
    userName: {
      type: String,
      default: ''
    },
    productName: {
      type: String,
      require: true,
      default: ''
    }
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
      protocolStateOptions: [],
      protocolTypeOptions: [],
      productOptions: [],
      connectionOptions: [],
      // 查询参数
      queryParam: {
        id: null,
        name: null,
        description: null,
        state: undefined,
        type: null,
        configuration: null,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '协议名',
          dataIndex: 'name',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '类型',
          dataIndex: 'type',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '描述',
          dataIndex: 'description',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '状态',
          dataIndex: 'state',
          scopedSlots: {customRender: 'state'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '操作',
          dataIndex: 'operation',
          width: '18%',
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
    /** 查询设备协议列表 */
    getList() {
      this.loading = true
      this.queryParam.id = this.productKey
      listProtocol(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
      this.getDicts('protocol_state').then(response => {
        this.protocolStateOptions = response.data
      })
      this.getDicts('connection_protocol').then(response => {
        this.connectionOptions = response.data
      })
      this.getDicts('protocol_type').then(response => {
        this.protocolTypeOptions = response.data
      })
      listProduct(null).then(response => {
        this.productOptions = response.rows.map(i => {
          return {label: i.name, value: i.productKey}
        })
        // console.log(this.productOptions)
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
        id: undefined,
        name: undefined,
        description: undefined,
        state: undefined,
        type: undefined,
        configuration: undefined,
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
    /* 任务状态修改 */
    confirmHandleStatus(row) {
      const text = row.state === 1 ? '停用' : '发布'
      row.state = row.state === 0 ? 1 : 0
      updateProtocol(row)
      .then(() => {
        this.$message.success(
          text + '成功',
          3
        )
        this.getList()
      }).catch(function () {
        this.$message.error(
          text + '发生异常',
          3
        )
      })
    },
    cancelHandleStatus(row) {
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return delProtocol(ids)
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
          that.download('protocol/protocol/export', {
            ...that.queryParam
          }, `protocol_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    goToMagicWeb() {
      window.open('/magic/debug/index.html', '_blank');
    }
  }
}
</script>

<style lang="less" scoped>
.table-operations {
  display: flex;
  justify-content: space-between;
  padding-bottom: 16px;
}

/deep/ .ant-table-pagination {
  padding-top: 16px;
}
</style>
