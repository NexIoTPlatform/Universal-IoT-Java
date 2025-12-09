<template>
  <a-card :bordered="false">
    <!-- 条件搜索 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="订阅级别" prop="application">
              <a-select placeholder="请选择订阅级别" size="large" v-model="queryParam.subType"
                        style="width: 100%" allow-clear>
                <a-select-option v-for="(d, index) in subscribeLevel" :key="index"
                                 :value="d.dictValue">{{
                    d.dictLabel
                  }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="消息类型" prop="application">
              <a-select placeholder="请选择消息类型" size="large" v-model="queryParam.msgType"
                        style="width: 100%" allow-clear>
                <a-select-option v-for="(d, index) in subscribeMessageType" :key="index"
                                 :value="d.dictValue">{{
                    d.dictLabel
                  }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span
              class="table-page-search-submitButtons"
              :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="handleQuery"><iot-icon
                type="icon-search"/>{{ $t('button.search') }}</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
              <!--              <a @click="toggleAdvanced" style="margin-left: 8px">-->
              <!--                  {{ advanced ? $t('button.collapse') : $t('button.expand') }}-->
              <!--                  <a-icon :type="advanced ? 'up' : 'down'"/>-->
              <!--               </a>-->
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 操作 -->
    <div class="table-operations">
      <a-button type="primary" @click="addSubscribe">
        <a-icon type="plus"/>
       {{ $t('button.add') }}</a-button>
      <a-button type="danger" :disabled="multiple" @click="handleDelete">
        <a-icon type="delete"/>
       {{ $t('button.delete') }}</a-button>
      <a-button
        type="primary"
        size="small"
        :loading="loading"
        :style="{float: 'right'}"
        @click="getList"
      >
        <a-icon type="sync" :spin="loading"/>
        {{ $t('button.refresh') }}
      </a-button>
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
      <span slot="subscribeLevel" slot-scope="text, record">
        <a-tag color="#108ee9">{{ getSubscribeLevel(record.subType) }}</a-tag>
      </span>
      <span slot="subscribeMessageType" slot-scope="text, record">
        <a-tag color="rgb(22 195 212)">{{ getSubscribeMessageType(record.msgType) }}</a-tag>
      </span>
      <div slot="subscribeAdd" slot-scope="text">
        <!--color="#108ee9"-->
        <div v-if="text === undefined || text === null || text === ''">
          无
        </div>
        <div v-else-if="Array.isArray(text.split(','))">
          <div
            v-for="(item, index) in text.split(',')"
            style="margin-bottom: 4px"
            :key="index">
            <a-tooltip :key="index">
              <template slot="title">
                {{ item }}
              </template>
              <a-tag
                :key="index"
                style="margin-top: 0px;"
              >{{ item }}
              </a-tag>
            </a-tooltip>
          </div>
        </div>
      </div>
      <span slot="operation" slot-scope="text, record">
        <a @click="deleteDevSubscribe(record)">
          <a-icon type="delete"/>{{ $t('button.delete') }} 
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
    <subscribe-create-form
      @ok="getList"
      ref="subscribeCreateForm"
      :dev-id="devId"
      :product-key="productKey"
      :subscribe-level="subscribeLevel"
      :subscribe-message-type="subscribeMessageType">
    </subscribe-create-form>
  </a-card>
</template>

<script>
import {deleteSubscribe, listSubscribe} from '@/api/system/dev/subscribe'
import SubscribeCreateForm from './SubscribeCreateForm'

export default {
  name: 'LogManage',
  props: {
    devId: {
      type: String,
      default: '',
      require: true
    },
    productKey: {
      type: String,
      default: '',
      require: true
    }
  },
  components: {
    SubscribeCreateForm
  },
  data() {
    return {
      // 订阅消息类型
      subscribeMessageType: [],
      // 订阅级别
      subscribeLevel: [],
      // 列表数据
      list: [],
      // 多选框值
      selectedRowKeys: [],
      selectedRows: [],
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 多选框选中行id值
      ids: [],
      // 是否显示加载效果
      loading: false,
      total: 0,
      // 查询参数
      queryParam: {
        msgType: undefined,
        subType: undefined,
        pageNum: 1,
        pageSize: 10,
        params: {}
      },
      columns: [
        {
          title: '订阅级别',
          dataIndex: 'subType',
          scopedSlots: {customRender: 'subscribeLevel'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '消息类型',
          dataIndex: 'msgType',
          scopedSlots: {customRender: 'subscribeMessageType'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '订阅地址',
          dataIndex: 'url',
          scopedSlots: {customRender: 'subscribeAdd'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '创建者人',
          dataIndex: 'creater',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createDate',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
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
    this.getDicts('subscribe_type').then(response => {
      this.subscribeMessageType = response.data
    })
    this.getDicts('subscribe_level').then(response => {
      this.subscribeLevel = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询log列表 */
    getList() {
      this.loading = true
      this.queryParam.iotId = this.devId
      this.queryParam.productKey = this.productKey
      listSubscribe(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      }).finally(() => {
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
        msgType: undefined,
        subType: undefined,
        pageNum: 1,
        pageSize: 10,
        params: {}
      }
      // this.resetForm('queryForm')
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
          return deleteSubscribe(that.devId, that.productKey, ids)
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
          that.download('system/log/export', {
            ...that.queryParam
          }, `log_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    deleteDevSubscribe(row) {
      const that = this
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + row.id + '的数据',
        onOk() {
          return deleteSubscribe(that.devId, that.productKey, [row.id]).then((res) => {
            that.getList()
            that.$message.success('删除成功', 3)
          })
        },
        onCancel() {
        }
      })
    },
    getSubscribeMessageType(e) {
      const actions = []
      Object.keys(this.subscribeMessageType).some((key) => {
        if (this.subscribeMessageType[key].dictValue === ('' + e)) {
          actions.push(this.subscribeMessageType[key].dictLabel)
          return true
        }
      })
      return actions.join('')
    },
    getSubscribeLevel(e) {
      console.log('this.subscribeLevel = ', this.subscribeLevel)
      console.log('this.e = ', e)
      const actions = []
      Object.keys(this.subscribeLevel).some((key) => {
        if (this.subscribeLevel[key].dictValue === ('' + e)) {
          actions.push(this.subscribeLevel[key].dictLabel)
          return true
        }
      })
      return actions.join('')
    },
    // 新增订阅
    addSubscribe() {
      this.$refs.subscribeCreateForm.handleAdd()
    }
  }
}
</script>
