<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="DeviceId" prop="deviceId">
                <a-input v-model="queryParam.deviceId" placeholder="请输入DeviceId"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('product.name')" prop="productName">
                <a-select v-model="queryParam.productName" placeholder="请选择或搜索产品名称"
                          show-search :filter-option="false"
                          :loading="productLoading" @search="handleProductSearch"
                          @focus="handleProductFocus" allow-clear
                          style="width: 100%">
                  <a-select-option v-for="product in productList" :key="product.id"
                                   :value="product.name"
                                   :title="product.name">
                    <div class="product-option">
                      <div class="product-name">{{ product.name }}</div>
                      <div class="product-key">{{ product.productKey }}</div>
                    </div>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('device.name')" prop="deviceName">
                <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item label="$t('device.status')">
                  <a-select placeholder="请选择" v-model="queryParam.state" style="width: 100%"
                            allow-clear>
                    <a-select-option v-for="(d, index) in stateOptions" :key="index"
                                     :value="d.dictValue">{{
                        d.dictLabel
                      }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="设备iotId" prop="iotId">
                  <a-input v-model="queryParam.iotId" placeholder="请输入iotId"
                           @keyup.enter="handleQuery" allow-clear/>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons"
                    :style="advanced && { float: 'right', overflow: 'hidden' } || {}">
                <a-button type="primary" @click="handleQuery"><iot-icon
                  type="icon-search"/>{{ $t('button.query') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? $t('button.collapse') : $t('button.expand') }}
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
                  v-hasPermi="['system:instance:add']">
          <iot-icon type="icon-u-add"/>{{ $t('button.add') }}
        </a-button>
        <a-button type="primary" :disabled="single"
                  @click="$refs.createForm.handleUpdate(undefined, ids)"
                  v-hasPermi="['system:instance:edit']">
          <iot-icon type="icon-u-edit"/>{{ $t('button.edit') }}
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['system:instance:remove']">
          <iot-icon type="icon-u-del"/>{{ $t('button.delete') }}
        </a-button>
        <a-button type="primary" @click="handleExport" v-hasPermi="['system:instance:export']">
          <iot-icon type="icon-u-export"/>{{ $t('button.export') }}
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="getList">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
      <!-- 增加修改 -->
      <create-form ref="createForm" :stateOptions="stateOptions" @ok="getList"/>
      <!-- 数据展示 -->
      <a-table :loading="loading" :size="tableSize" rowKey="id" :columns="columns"
               :data-source="list"
               :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
               :pagination="false">
        <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
          <a-popover placement="rightTop" trigger="hover" :title="'设备配置信息'"
                     v-if="getConfigInfo(record.configuration, record.deviceNode)">
            <template slot="content">
              <div class="config-popover">
                <div v-for="(value, key) in getConfigInfo(record.configuration, record.deviceNode)" :key="key"
                     class="config-popover-item">
                  <span class="config-popover-key">{{ key }}:</span>
                  <span class="config-popover-value">{{ value }}</span>
                </div>
              </div>
            </template>
            <div class="device-info-content">
              <div class="device-name">
                <a-button type="link"
                          @click="$router.push(`/system/instance/instance-details/${record.id}`)"
                          class="device-name-link">
                  {{ record.deviceName || '未命名设备' }}
                </a-button>
              </div>
              <div class="device-id">{{ record.deviceId }}</div>
              <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
            </div>
          </a-popover>
          <div v-else class="device-info-content">
            <div class="device-name">
              <a-button type="link"
                        @click="$router.push(`/system/instance/instance-details/${record.id}`)"
                        class="device-name-link">
                {{ record.deviceName || '未命名设备' }}
              </a-button>
            </div>
            <div class="device-id">{{ record.deviceId }}</div>
            <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
          </div>
        </div>

        <div slot="productInfo" slot-scope="text, record" class="product-info-cell">
          <div class="product-name">
            {{ record.productName || '未知产品' }}
            <device-type-badge :type="record.deviceNode" :text="getDeviceTypeText(record.deviceNode)"/>
          </div>
          <div class="product-key">{{ record.productKey }}</div>
        </div>

        <div slot="state" slot-scope="text, record" class="state-cell">
          <div
            :class="{ 'status-badge online': record.state, 'status-badge offline': !record.state }">
            <span class="status-dot"></span>
            <span class="status-text">{{ stateFormat(record) }}</span>
          </div>
        </div>

        <div slot="onlineTime" slot-scope="text, record" class="online-time-cell">
          <div class="time-main">{{ formatOnlineTime(record.onlineTime) }}</div>
          <div class="time-ago">{{ getTimeAgo(record.onlineTime) }}</div>
        </div>

        <span slot="operation" slot-scope="text, record" class="operation-buttons">
          <a @click="$refs.createForm.handleUpdate(record, undefined)"
             v-hasPermi="['system:instance:edit']"
             class="operation-btn">
            <iot-icon type="icon-u-edit"/>
            {{ $t('button.edit') }} </a>
          <a-divider type="vertical" v-hasPermi="['system:instance:remove']"/>
          <a style="color:#F53F3F" @click="handleDelete(record)"
             v-hasPermi="['system:instance:remove']"
             class="operation-btn">
            <iot-icon type="icon-u-del"/>
            {{ $t('button.delete') }} </a>
        </span>
      </a-table>
      <!-- 分页 -->
      <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper
                    :current="queryParam.pageNum"
                    :total="total" :page-size="queryParam.pageSize"
                    :showTotal="total => `共 ${total} 条`"
                    @showSizeChange="onShowSizeChange" @change="changeSize"/>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import {delInstance, listInstance} from '@/api/system/dev/instance'
import {queryProductList} from '@/api/system/dev/product'
import CreateForm from './modules/CreateForm'
import {getDeviceConfigByType} from '@/utils/deviceConfig'

export default {
  name: 'IoTDevice',
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
      // 0-离线，1-在线字典
      stateOptions: [],
      // 产品列表数据
      productList: [],
      productSearchValue: '',
      productLoading: false,
      searchTimer: null,
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
        state: undefined,
        detail: null,
        registryTime: null,
        onlineTime: null,
        areasId: null,
        pageNum: 1,
        pageSize: 10
        // parentUnionId: this.$store.state.user.parentUnionId
      },
      columns: [
        {
          title: this.$t('compound.deviceNameDeviceId'),
          dataIndex: 'deviceInfo',
          scopedSlots: {customRender: 'deviceInfo'},
          width: '28%',
          align: 'left'
        },
        {
          title: this.$t('compound.productProductKey'),
          dataIndex: 'productInfo',
          scopedSlots: {customRender: 'productInfo'},
          width: '22%',
          align: 'left'
        },
        {
          title: this.$t('device.status'),
          dataIndex: 'state',
          scopedSlots: {customRender: 'state'},
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('device.communicationStatus'),
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
          width: '18%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '20%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  created() {
    const state = this.$route.query.state
    if (state) {
      this.queryParam.state = state
    }
    this.getList()
    this.loadProductList()
    this.getDicts('dev_instance_state').then(response => {
      this.stateOptions = response.data
    })
  },
  activated() {
    const state = this.$route.query.state
    if (state) {
      this.queryParam.state = state
      this.getList()
    }
  },
  beforeDestroy() {
    // 清理定时器
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  },
  methods: {
    /** 查询设备列表 */
    getList() {
      this.loading = true
      if (this.queryParam.iotId !== undefined && this.queryParam.iotId !== null) {
        this.queryParam.iotId = this.queryParam.iotId.replace(/^\s*|\s*$/g, '')
      }
      if (this.queryParam.deviceId !== undefined && this.queryParam.deviceId !== null) {
        this.queryParam.deviceId = this.queryParam.deviceId.replace(/^\s*|\s*$/g, '')
      }
      if (this.queryParam.deviceName !== undefined && this.queryParam.deviceName !== null) {
        this.queryParam.deviceName = this.queryParam.deviceName.replace(/^\s*|\s*$/g, '')
      }
      if (this.queryParam.productName !== undefined && this.queryParam.productName !== null) {
        this.queryParam.productName = this.queryParam.productName.replace(/^\s*|\s*$/g, '')
      }
      if (this.queryParam.productKey !== undefined && this.queryParam.productKey !== null) {
        this.queryParam.productKey = this.queryParam.productKey.replace(/^\s*|\s*$/g, '')
      }
      listInstance(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 0-离线，1-在线 字典翻译
    stateFormat(row, column) {
      return this.selectDictLabel(this.stateOptions, row.state)
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
          return delInstance(ids)
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
          that.download('/admin/v1/device/export', {
            ...that.queryParam
          }, `instance_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    /** 查看设备详情 */
    checkDetails(row) {
      const deviceId = row.id
      this.$router.push(`/system/instance/instance-details/${deviceId}`)
    },

    /** 加载产品列表 */
    async loadProductList(searchValue = '') {
      try {
        this.productLoading = true
        const params = {
          pageNum: 1,
          pageSize: 50, // 限制返回数量
          name: searchValue // 根据名称搜索
        }
        const response = await queryProductList(params)
        this.productList = response.rows || []
      } catch (error) {
        console.error('加载产品列表失败:', error)
        this.$message.error('加载产品列表失败')
      } finally {
        this.productLoading = false
      }
    },

    /** 处理产品搜索 */
    handleProductSearch(value) {
      this.productSearchValue = value
      // 防抖处理
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.loadProductList(value)
      }, 300)
    },

    /** 处理产品下拉框获得焦点 */
    handleProductFocus() {
      // 如果产品列表为空，则加载初始数据
      if (this.productList.length === 0) {
        this.loadProductList()
      }
    },

    /** 获取设备类型显示文本 */
    getDeviceTypeText(deviceNode) {
      const textMap = {
        'DEVICE': '直',
        'GATEWAY': '网',
        'GATEWAY_SUB_DEVICE': '子'
      };
      return textMap[deviceNode] || deviceNode;
    },

    /** 格式化在线时间 */
    formatOnlineTime(timestamp) {
      if (!timestamp || timestamp === '0') {
        return '从未通信'
      }
      // 如果是字符串格式的时间戳，转换为数字
      const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      // 如果时间戳是10位，转换为13位（毫秒）
      const timeMs = time.toString().length === 10 ? time * 1000 : time
      return this.parseTime(timeMs, '{y}-{m}-{d} {h}:{i}:{s}')
    },

    /** 获取时间距离现在的描述 */
    getTimeAgo(timestamp) {
      if (!timestamp || timestamp === '0') {
        return '从未通信'
      }
      const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      const timeMs = time.toString().length === 10 ? time * 1000 : time
      const now = Date.now()
      const diff = now - timeMs

      if (diff < 60000) { // 1分钟内
        return '刚刚在线'
      } else if (diff < 3600000) { // 1小时内
        const minutes = Math.floor(diff / 60000)
        return `${minutes}分钟前`
      } else if (diff < 86400000) { // 1天内
        const hours = Math.floor(diff / 3600000)
        return `${hours}小时前`
      } else if (diff < 2592000000) { // 30天内
        const days = Math.floor(diff / 86400000)
        return `${days}天前`
      } else {
        return '长时间未通信'
      }
    },

    /** 解析配置信息 */
    getConfigInfo(configStr, deviceNode) {
      return getDeviceConfigByType(configStr, deviceNode)
    }
  }
}
</script>

<style scoped lang="less">
.status {
  display: flex;
  align-items: center;
  justify-content: center;
}

.online {
  &::before {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #52c41a;
  }
}

.offline {
  &::before {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #f5222d;
  }
}

.product-option {
  display: flex;
  flex-direction: column;
  padding: 2px 0;
}

.product-name {
  font-size: 13px;
  color: #1a202c;
  font-weight: 500;
  line-height: 1.4;
}

.product-key {
  font-size: 11px;
  color: #718096;
  font-family: 'Courier New', monospace;
  line-height: 1.2;
  margin-top: 2px;
}

/* 新的表格单元格样式 */
.device-info-cell {
  padding: 12px 0;
}

.device-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-name {
  line-height: 1.4;
}

.device-name-link {
  font-size: 15px;
  font-weight: 600;
  padding: 0 !important;
  margin: 0;
  height: auto;
  color: #1890ff;
  text-align: left;
  justify-content: flex-start;
  line-height: 1.4;
  border: none;
  box-shadow: none;
  background: none;
}

.device-name-link:focus,
.device-name-link:active {
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.device-name-link:hover {
  color: #40a9ff;
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.device-id {
  font-size: 13px;
  color: #595959;
  font-family: 'Courier New', monospace;
  font-weight: 600;
  line-height: 1.4;
}

.iot-id {
  font-size: 11px;
  color: #8c8c8c;
  font-family: 'Courier New', monospace;
  line-height: 1.4;
}

.product-info-cell {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-name {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
}

.product-key {
  font-size: 12px;
  color: #595959;
  font-family: 'Courier New', monospace;
  line-height: 1.4;
}

/* 配置信息悬停弹窗样式 */
.config-popover {
  max-width: 300px;
}

.config-popover-item {
  display: flex;
  margin-bottom: 6px;
  font-size: 12px;
  line-height: 1.4;
}

.config-popover-key {
  color: #595959;
  font-weight: 500;
  min-width: 60px;
  margin-right: 8px;
}

.config-popover-value {
  color: #262626;
  font-family: 'Courier New', monospace;
  word-break: break-all;
  flex: 1;
}

.state-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-badge {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}

.status-badge.offline {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  color: #cf1322;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-badge.online .status-dot {
  background-color: #52c41a;
}

.status-badge.offline .status-dot {
  background-color: #f5222d;
}

.online-time-cell {
  text-align: center;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.time-main {
  font-size: 13px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
}

.time-ago {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.3;
  font-weight: 400;
}

/* 操作按钮样式优化 */
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 120px;
}

.operation-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}

.operation-btn .anticon,
.operation-btn .iot-icon {
  font-size: 12px;
}

/* 设备标识样式 */
.device-badge {
  display: inline-block;
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 8px;
  margin-left: 6px;
  font-weight: 500;
  line-height: 1.2;
  color: white;
}

/* 直连设备样式 - 蓝色 */
.device-badge-direct {
  background-color: #1890ff;
  border: 1px solid #40a9ff;
}

/* 网关设备样式 - 绿色 */
.device-badge-gateway {
  background-color: #52c41a;
  border: 1px solid #73d13d;
}

/* 子设备样式 - 橙色 */
.device-badge-sub {
  background-color: #fa8c16;
  border: 1px solid #ffc53d;
}
</style>
