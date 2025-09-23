<template>
  <a-modal
    width="85%"
    :visible="open"
    title="绑定设备"
    @ok="onClose"
    @cancel="onClose"
    :footer="null"
    :destroyOnClose="true"
    :maskClosable="false"
    :closable="true"
    :centered="true"
    :bodyStyle="{ maxHeight: '75vh', overflow: 'hidden' }"
  >
    <div class="modal-content">
      <!-- 顶部提示信息 -->
      <div class="warning-info">
        <a-icon
          type="warning"
          style="color: white;
        border: 0px solid red;
        border-radius: 50%;
        font-size: 14px;
        background: #5687e2;
        margin-right: 8px"/>
        <span style="color: #000000; font-size: 12px;">一个设备最多可以被添加到10个设备组中</span>
      </div>

      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="8" style="margin-bottom: 8px;">
            <a-col :span="5" :md="5" :sm="24">
              <a-select
                :default-value="productValue"
                style="width: 100%"
                @change="handleChange"
                :maxTagTextLength="1"
                :options="productList"
                v-model="productValue"
                size="small"
                placeholder="选择产品">
              </a-select>
            </a-col>
            <a-col :span="5" :md="5" :sm="24">
              <a-select
                :default-value="searchValue"
                style="width: 100%"
                @change="handleChange"
                :options="searchList"
                v-model="searchValue"
                size="small"
                placeholder="搜索类型">
              </a-select>
            </a-col>
            <a-col :span="6" :md="6" :sm="24">
              <a-input
                v-model="content"
                placeholder="请输入搜索内容"
                allow-clear
                size="small"
                @keyup.enter="searchInstance">
                <a-tooltip slot="suffix" title="点击搜索">
                  <a-icon type="search" @click="searchInstance"
                          style="cursor: pointer; color: #1890ff;"/>
                </a-tooltip>
              </a-input>
            </a-col>
            <a-col :span="2" :md="2" :sm="24" align="right">
              <a-button @click="retrySearch" size="small" type="text">
                <a-icon type="redo"/>
              </a-button>
            </a-col>
            <a-col :span="6" :md="6" :sm="24" align="right">
              <a-button
                type="primary"
                size="small"
                @click="binds"
                :disabled="selectedRowKeys.length==0"
                style="height: 24px; padding: 0 8px; font-size: 12px;">
                批量绑定
              </a-button>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 数据展示 -->
      <div class="table-container">
        <a-table
          :loading="loading"
          :size="tableSize"
          rowKey="id"
          :columns="columns"
          :data-source="list"
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
          :pagination="false"
          :scroll="{ y: 350 }">
          <span slot="productName" slot-scope="text, record">
            <div class="product-name">
              {{ record.productName || '未知产品' }}
              <span v-if="record.deviceNode === 'DEVICE'" class="sub-device-badge">直</span>
            <span v-if="record.deviceNode === 'GATEWAY'" class="sub-device-badge">网</span>
            <span v-if="record.deviceNode === 'GATEWAY_SUB_DEVICE'"
                  class="sub-device-badge">子</span>
            </div>
          </span>
          <span
            slot="state"
            slot-scope="text, record"
            :class="{'status online': record.state, 'status offline': !record.state}">
            <span>{{ stateFormat(record) }}</span>
          </span>
          <span slot="onlineTime" slot-scope="text, record">
            {{ parseTime(record.onlineTime) }}
          </span>
          <span slot="operation" slot-scope="text, record">
            <a-divider type="vertical" v-hasPermi="['system:instance:remove']"/>
            <a v-hasPermi="['system:instance:remove']" @click="binds(text , record)">
              <a-icon type="check-circle"/>绑定
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
      </div>
    </div>
  </a-modal>
</template>

<script>
import {listNotBindInstance} from '@/api/system/dev/instance'
import {bindsDev} from '@/api/system/dev/devGroup'
import {listAllProduct} from '@/api/system/dev/product'

export default {
  name: 'CreateForm',
  props: {},
  components: {},
  data() {
    return {
      // 分组id
      groupId: null,
      stateOptions: [],
      // 输入框内容
      content: '',
      // 抽屉组件显示控制
      open: false,
      // 产品选项框的值
      productValue: '0',
      // 产品选项列表
      productList: [],
      // 搜索选项框的值
      searchValue: '0',
      // 搜做选项列表
      searchList: [
        {
          value: '0',
          key: '0',
          label: '设备名'
        },
        {
          value: '1',
          key: '1',
          label: 'iotId'
        },
        {
          value: '2',
          key: '2',
          label: '序列号'
        }
      ],
      // 设备列表
      list: [],
      // 设备总数
      total: null,
      // 选择框
      selectedRowKeys: [],
      // 选择框选择设备iotId集合
      selectedIotIds: [],
      // columns 表格渲染规则
      columns: [
        {
          title: '设备序列号',
          dataIndex: 'deviceId',
          ellipsis: true,
          scopedSlots: {customRender: 'deviceId'},
          align: 'center'
        },
        {
          title: '设备名称',
          dataIndex: 'deviceName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '产品名称',
          dataIndex: 'productName',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'productName'}
        },
        {
          title: '最后通信时间',
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
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
      ],
      // 设备查询参数
      queryParam: {
        iotId: null,
        deviceId: null,
        extDeviceId: null,
        deriveMetadata: null,
        configuration: null,
        productName: null,
        nickName: null,
        features: null,
        parentId: null,
        orgId: null,
        productKey: null,
        deviceName: null,
        creatorId: null,
        instance: null,
        application: null,
        creatorName: null,
        state: null,
        detail: null,
        registryTime: null,
        onlineTime: null,
        areasId: null,
        pageNum: 1,
        pageSize: 10
      },
      // 产品查询参数
      productQueryParam: {
        pageNum: 1,
        pageSize: 9999
      },
      // 加载显示控制
      loading: false
    }
  },
  filters: {},
  created() {
    this.getList()
    this.getProductList()
    this.getDicts('dev_instance_state').then(response => {
      this.stateOptions = response.data
      // console.log(response.data)
    })
  },
  computed: {},
  watch: {},
  mounted() {
  },
  methods: {
    onClose() {
      this.open = false
      this.$emit('reset')
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.groupId = null
      this.queryParam.deviceName = undefined
      this.queryParam.iotId = undefined
      this.queryParam.deviceId = undefined
      this.queryParam.productKey = undefined
      this.queryParam.pageNum = 1
      this.queryParam.pageSize = 10
      this.selectedRowKeys = []
      this.searchValue = '0'
      this.productValue = '0'
      this.content = ''
    },
    /** 查询设备列表 */
    getList() {
      this.loading = true
      listNotBindInstance(this.queryParam, this.groupId).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.iotId)
      this.selectedIotIds = this.ids
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    // 产品选项框
    handleChange(value, option) {
    },
    // 搜索产品
    searchInstance() {
      this.queryParam.deviceName = undefined
      this.queryParam.iotId = undefined
      this.queryParam.deviceId = undefined
      this.queryParam.productKey = undefined
      this.queryParam.pageNum = 1
      this.queryParam.pageSize = 10
      this.selectedRowKeys = []

      // 去除前后空格和换行符
      const trimmedContent = this.content ? this.content.trim().replace(/[\r\n]/g, '') : ''

      if (trimmedContent !== null && trimmedContent !== undefined && trimmedContent !== '') {
        if (this.searchValue === '0') {
          this.queryParam.deviceName = trimmedContent
        } else if (this.searchValue === '1') {
          this.queryParam.iotId = trimmedContent
        } else if (this.searchValue === '2') {
          this.queryParam.deviceId = trimmedContent
        }
      }
      if (this.productValue !== '0') {
        this.queryParam.productKey = this.productValue
      }
      this.getList()
    },
    // 0-离线，1-在线字典翻译
    stateFormat(row, column) {
      return this.selectDictLabel(this.stateOptions, row.state)
    },
    // 重置搜索
    retrySearch() {
      this.queryParam.deviceName = undefined
      this.queryParam.iotId = undefined
      this.queryParam.deviceId = undefined
      this.queryParam.productKey = undefined
      this.queryParam.pageNum = 1
      this.queryParam.pageSize = 10
      this.selectedRowKeys = []
      this.searchValue = '0'
      this.productValue = '0'
      this.content = ''
      this.getList()
    },
    /** 查询设备产品列表 */
    getProductList() {
      // this.loading = true
      listAllProduct().then(response => {
        this.productList.push({
          value: '0',
          key: '0',
          label: '所有产品'
        })
        response.rows.forEach(item => {
          item.value = item.productKey
          item.key = item.productKey
          item.label = item.name
          this.productList.push(item)
        })
      })
    },
    binds(a, b) {
      var values
      if (b !== null && b !== undefined) {
        values = {
          id: this.groupId,
          devIds: [b.iotId]
        }
      } else {
        values = {
          id: this.groupId,
          devIds: this.selectedIotIds
        }
      }
      // console.log("values = ",values)
      bindsDev(values).then(response => {
        this.$message.success(
          response.msg,
          3
        )
      }).finally(() => {
        this.getList()
      })
    },
    show(id) {
      this.open = true
      this.groupId = id

      this.queryParam.deviceName = undefined
      this.queryParam.iotId = undefined
      this.queryParam.deviceId = undefined
      this.queryParam.productKey = undefined
      this.queryParam.pageNum = 1
      this.queryParam.pageSize = 10
      this.selectedRowKeys = []
      this.searchValue = '0'
      this.productValue = '0'
      this.content = ''
      this.getList()
    }
  }
}
</script>

<style scoped lang="less">
.modal-content {
  max-height: 75vh;
  overflow-y: auto;

  .warning-info {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    padding: 8px 12px;
    background: #f6ffed;
    border-radius: 4px;
    border-left: 3px solid #52c41a;
  }

  .table-page-search-wrapper {
    margin-bottom: 12px;

    .ant-form-item {
      margin-bottom: 8px;
    }

    .ant-select {
      .ant-select-selection {
        border-radius: 4px;
      }
    }

    .ant-input {
      border-radius: 4px;

      &:focus {
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      }
    }
  }

  .table-container {
    .ant-table {
      /deep/ .ant-table-thead > tr > th {
        padding: 8px 4px;
        font-size: 12px;
      }

      /deep/ .ant-table-tbody > tr > td {
        padding: 8px 4px;
        font-size: 12px;
      }
    }

    .ant-table-pagination {
      margin-top: 8px;
      text-align: right;

      /deep/ .ant-pagination-item {
        min-width: 24px;
        height: 24px;
        line-height: 22px;
      }

      /deep/ .ant-pagination-prev,
      /deep/ .ant-pagination-next {
        min-width: 24px;
        height: 24px;
        line-height: 22px;
      }
    }
  }

  /* 子设备标识样式 */

  .sub-device-badge {
    display: inline-block;
    background-color: #ff7a45;
    color: white;
    font-size: 11px;
    padding: 1px 4px;
    border-radius: 8px;
    margin-left: 6px;
    font-weight: 500;
    line-height: 1.2;
  }
}
</style>
