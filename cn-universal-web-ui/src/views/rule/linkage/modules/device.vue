<template>
  <div>
    <a-modal :visible="modelVisible" title="设备选择" width="80%" @ok="handleOk"
             @cancel="closeModel">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="设备序列号">
                <a-input v-model="queryParam.deviceId" placeholder="请输入设备序列号" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item ::label="$t('device.name')">
                <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称" allow-clear/>
              </a-form-item>
            </a-col>
            <span
              class="table-page-search-submitButtons"
              :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="getList"><iot-icon
                type="icon-search"/>{{ $t('button.search') }}</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>{{
                  $t('button.reset')
                }}</a-button>
            </span>
          </a-row>
        </a-form>
      </div>
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="id"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange,type:'radio'}"
        :pagination="false"
      >
        <span slot="productName" slot-scope="text, record">
          <div class="product-name">
            {{ record.productName || '未知产品' }}
            <span v-if="record.deviceNode === 'DEVICE'" class="sub-device-badge">直</span>
            <span v-if="record.deviceNode === 'GATEWAY'" class="sub-device-badge">网</span>
            <span v-if="record.deviceNode === 'GATEWAY_SUB_DEVICE'"
                  class="sub-device-badge">子</span>
          </div>
        </span>
      </a-table>
      <!-- 分页 -->
      <a-pagination
        style="float:right;margin-top:16px"
        show-size-changer
        show-quick-jumper
        :current="queryParam.pageNum"
        :total="total"
        :page-size="queryParam.pageSize"
        :showTotal="total => `共 ${total} 条`"
        @showSizeChange="onShowSizeChange"
        @change="changeSize"
      />
      <div style="clear:both"></div>
    </a-modal>
  </div>
</template>

<script>
import {listInstance} from '@/api/system/dev/instance'

export default {
  name: 'Device',
  props: {},

  data() {
    return {
      list: [],
      selectedRowKeys: [],
      // 弹出的绑定页面
      selectedRows: [],
      // 弹出的绑定页面
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      id: undefined,
      ids: [],
      loading: false,
      total: 0,
      open: false,
      devGroup: [],
      // 0-离线，1-在线字典
      stateOptions: [],
      // 设备选择弹窗可视
      modelVisible: false,
      // 分组可视
      groupVisible: false,
      // 查询参数
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
      columns: [
        {
          title: '编号',
          dataIndex: 'id',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('device.deviceSerial'),
          dataIndex: 'deviceId',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('device.deviceName'),
          dataIndex: 'deviceName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('device.productName'),
          dataIndex: 'productName',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'productName'}
        },
        {
          title: '产品Key',
          dataIndex: 'productKey',
          ellipsis: true,
          align: 'center'
        }
      ]
    }
  },
  watch: {},
  created() {
    this.getList()
  },
  methods: {
    /** 查询设备列表 */
    getList() {
      this.loading = true
      listInstance(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    openModal() {
      this.modelVisible = true
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        iotId: undefined,
        deviceId: undefined,
        extDeviceId: undefined,
        deriveMetadata: undefined,
        configuration: undefined,
        productName: undefined,
        nickName: undefined,
        features: undefined,
        parentId: undefined,
        orgId: undefined,
        productKey: undefined,
        deviceName: undefined,
        creatorId: undefined,
        instance: undefined,
        application: undefined,
        creatorName: undefined,
        state: undefined,
        detail: undefined,
        registryTime: undefined,
        onlineTime: undefined,
        areasId: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
    },
    handleOk() {
      if (this.ids.length === 0) {
        this.$message.error('请先选择设备')
        return
      }
      const str = this.ids[0].split('^&')
      const selectedDevice = this.selectedRows[0]
      this.$emit('getDeviceInfo', {
        'deviceId': str[0],
        'deviceName': str[1],
        'config': str[2],
        'productKey': selectedDevice.productKey
      })
      this.modelVisible = false
    },
    closeModel() {
      this.modelVisible = false
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
      this.ids = this.selectedRows.map(
        item => item.deviceId + '^&' + item.deviceName + '^&' + item.configuration)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    }
  }
}
</script>
