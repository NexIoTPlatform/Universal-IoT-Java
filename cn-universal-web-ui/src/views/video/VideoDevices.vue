<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="平台实例">
                <a-select v-model="queryParam.instanceKey" placeholder="请选择" allow-clear style="width: 100%" @change="handleInstanceChange">
                  <a-select-option v-for="inst in platformInstances" :key="inst.instanceKey" :value="inst.instanceKey">
                    {{ inst.name }} ({{ inst.platformType }})
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="设备名称">
                <a-input v-model="queryParam.keyword" placeholder="请输入" @keyup.enter="handleQuery" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery"><iot-icon type="icon-search" />{{ $t('button.query') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh" />{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </a-card>

    <a-card :bordered="false">
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" :disabled="multiple" @click="showImportModal">
          <iot-icon type="icon-u-add" />勾选使用
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="handleRefresh">
          <a-icon type="sync" :spin="loading" />
        </a-button>
      </div>

      <!-- 数据展示（样式与系统设备列表保持一致） -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="deviceId"
        :columns="columns"
        :data-source="devices"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false"
        :scroll="{ x: true }"
        :expandedRowKeys="expandedRowKeys"
        @expand="handleExpand"
      >
        <!-- 设备信息单元格（含配置弹窗，完全对齐系统样式） -->
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
                          @click="handleDetail(record)"
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
                        @click="handleDetail(record)"
                        class="device-name-link">
                {{ record.deviceName || '未命名设备' }}
              </a-button>
            </div>
            <div class="device-id">{{ record.deviceId }}</div>
            <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
          </div>
        </div>

        <!-- 产品信息单元格（厂商/型号或产品信息，使用类型徽章） -->
        <div slot="productInfo" slot-scope="text, record" class="product-info-cell">
          <div class="product-name">
            {{ record.productName || record.manufacturer || '未知产品' }}
            <device-type-badge :type="record.deviceNode" :text="getDeviceTypeText(record.deviceNode)"/>
          </div>
          <div class="product-key">{{ record.productKey || record.deviceModel || '-' }}</div>
        </div>

        <!-- 在线状态单元格（与系统设备列表一致） -->
        <div slot="state" slot-scope="text, record" class="state-cell">
          <div :class="{ 'status-badge online': isOnline(record), 'status-badge offline': !isOnline(record) }">
            <span class="status-dot"></span>
            <span class="status-text">{{ stateFormat(record) }}</span>
          </div>
        </div>

        <!-- 通信状态单元格（与系统列表一致） -->
        <div slot="onlineTime" slot-scope="text, record" class="online-time-cell">
          <div class="time-main">{{ formatOnlineTime(record.onlineTime) }}</div>
          <div class="time-ago">{{ getTimeAgo(record.onlineTime) }}</div>
        </div>

        <!-- 操作列（详情） -->
        <span slot="operation" slot-scope="text, record" class="operation-buttons">
          <a @click="handleDetail(record)" class="operation-btn">
            <a-icon type="eye" /> 详情
          </a>
        </span>

        <!-- 展开行：显示通道列表（保留原有功能） -->
        <template slot="expandedRowRender" slot-scope="record">
          <ChannelTable
            :ref="`channelTable_${record.deviceId}`"
            :instance-key="queryParam.instanceKey"
            :device-id="record.deviceId"
            :show-filter="false"
            :table-height="300"
            @preview="handleChannelPreview"
            @ptz="handleChannelPTZ"
          />
        </template>
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

    <!-- 勾选使用弹窗 -->
    <a-modal
      v-model:visible="importModalVisible"
      title="勾选使用设备"
      @ok="handleImport"
      @cancel="handleImportCancel"
      width="600px"
      :confirmLoading="importLoading"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="选择产品">
          <a-select v-model:value="importForm.productKey" placeholder="自动创建平台产品" allow-clear>
            <a-select-option value="">自动创建平台产品</a-select-option>
            <a-select-option v-for="prod in availableProducts" :key="prod.productKey" :value="prod.productKey">
              {{ prod.name }} ({{ prod.productKey }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="已选设备">
          <a-tag v-for="dev in selectedDevices" :key="dev.extDeviceId" style="margin-bottom: 8px">
            {{ dev.name }}
          </a-tag>
          <div v-if="selectedDevices.length === 0" style="color: #999">未选择任何设备</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import ChannelTable from '@/components/ChannelTable.vue'
import { listAllProduct } from '@/api/system/dev/product'
import { listPlatform, getPlatformDevices, importDevices } from '@/api/video/platform'
import { getDeviceConfigByType } from '@/utils/deviceConfig'
import DeviceTypeBadge from '@/components/DeviceTypeBadge/index.vue'

export default {
  name: 'VideoDevices',
  components: {
    ChannelTable,
    DeviceTypeBadge
  },
  data() {
    return {
      loading: false,
      importLoading: false,
      devices: [],
      platformInstances: [],
      availableProducts: [],
      selectedRowKeys: [],
      selectedRows: [],
      selectedDevices: [],
      expandedRowKeys: [], // 展开的行
      single: true,
      multiple: true,
      ids: [],
      total: 0,
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        instanceKey: undefined,
        keyword: undefined
      },
      columns: [
        {
          title: this.$t('compound.deviceNameDeviceId'),
          dataIndex: 'deviceInfo',
          scopedSlots: { customRender: 'deviceInfo' },
          width: '28%',
          align: 'left'
        },
        {
          title: this.$t('compound.productProductKey'),
          dataIndex: 'productInfo',
          scopedSlots: { customRender: 'productInfo' },
          width: '22%',
          align: 'left'
        },
        {
          title: this.$t('device.status'),
          dataIndex: 'state',
          scopedSlots: { customRender: 'state' },
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('device.communicationStatus'),
          dataIndex: 'onlineTime',
          scopedSlots: { customRender: 'onlineTime' },
          width: '18%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '20%',
          scopedSlots: { customRender: 'operation' },
          align: 'center'
        }
      ],
      importModalVisible: false,
      importForm: {
        productKey: ''
      }
    }
  },
  created() {
    this.loadPlatformInstances()
  },
  methods: {
    async loadPlatformInstances() {
      try {
        const res = await listPlatform()
        this.platformInstances = res.data || []
        if (this.platformInstances.length > 0) {
          this.queryParam.instanceKey = this.platformInstances[0].instanceKey
          this.handleQuery()
        }
      } catch (e) {
        this.$message.error('加载平台实例失败：' + (e.response?.data?.msg || e.message))
      }
    },
    async handleQuery() {
      if (!this.queryParam.instanceKey) {
        this.$message.warning('请先选择平台实例')
        return
      }
      this.loading = true
      try {
        const filters = {
          keyword: this.queryParam.keyword,
          page: this.queryParam.pageNum,
          pageSize: this.queryParam.pageSize
        }
        const res = await getPlatformDevices(this.queryParam.instanceKey, filters)
        this.devices = res.data || []
        this.total = this.devices.length
      } catch (e) {
        this.$message.error('查询设备失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.loading = false
      }
    },
    handleInstanceChange() {
      this.handleQuery()
    },
    resetQuery() {
      this.queryParam.keyword = undefined
      this.queryParam.pageNum = 1
      this.handleQuery()
    },
    handleRefresh() {
      this.handleQuery()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.handleQuery()
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.handleQuery()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.selectedDevices = selectedRows
      this.ids = this.selectedRows.map((item) => item.extDeviceId)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    showImportModal() {
      if (this.selectedDevices.length === 0) {
        this.$message.warning('请先选择设备')
        return
      }
      this.importForm.productKey = ''
      this.importModalVisible = true
      this.loadAvailableProducts()
    },
    async loadAvailableProducts() {
      try {
        const res = await listAllProduct()
        this.availableProducts = res.data || []
      } catch (e) {
        this.$message.error('加载产品列表失败：' + (e.response?.data?.msg || e.message))
      }
    },
    handleImportCancel() {
      this.importModalVisible = false
    },
    async handleImport() {
      this.importLoading = true
      try {
        const payload = {
          devices: this.selectedDevices,
          productKey: this.importForm.productKey || null
        }
        const res = await importDevices(this.queryParam.instanceKey, payload)
        const { success, failed, exists } = res.data || {}
        this.$message.success(`导入完成：成功${success}，失败${failed}，已存在${exists}`)
        this.importModalVisible = false
        this.onSelectChange([], [])
      } catch (e) {
        this.$message.error('导入失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.importLoading = false
      }
    },
    
    /**
     * 切换展开行
     */
    toggleExpandRow(record) {
      const key = record.deviceId
      const index = this.expandedRowKeys.indexOf(key)
      if (index > -1) {
        this.expandedRowKeys.splice(index, 1)
      } else {
        this.expandedRowKeys.push(key)
      }
    },
    
    /**
     * 展开/收起事件
     */
    handleExpand(expanded, record) {
      if (expanded) {
        // 展开时刷新通道数据
        this.$nextTick(() => {
          const ref = this.$refs[`channelTable_${record.deviceId}`]
          if (ref && ref[0]) {
            ref[0].refresh()
          }
        })
      }
    },
    
    /**
     * 通道预览
     */
    handleChannelPreview(channel) {
      // TODO: 实现视频预览功能
      this.$message.info(`预览通道: ${channel.channelName}`)
      console.log('预览通道:', channel)
    },
    
    /**
     * 通道PTZ控制
     */
    handleChannelPTZ(channel) {
      // TODO: 实现PTZ控制面板
      this.$message.info(`PTZ控制: ${channel.channelName}`)
      console.log('PTZ控制:', channel)
    },

    /** 是否在线（兼容 online/ON 两种取值） */
    isOnline(record) {
      const s = record.deviceStatus || record.status
      return s === 'online' || s === 'ON'
    },

    /** 状态显示文案（统一字典文案） */
    stateFormat(record) {
      return this.isOnline(record) ? this.$t('device.online') || '在线' : this.$t('device.offline') || '离线'
    },

    /** 格式化在线时间（与系统设备列表一致） */
    formatOnlineTime(timestamp) {
      if (!timestamp || timestamp === '0') {
        return '从未通信'
      }
      const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      const timeMs = time.toString().length === 10 ? time * 1000 : time
      return this.parseTime ? this.parseTime(timeMs, '{y}-{m}-{d} {h}:{i}:{s}') : new Date(timeMs).toLocaleString()
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

      if (diff < 60000) {
        return '刚刚在线'
      } else if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000)
        return `${minutes}分钟前`
      } else if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000)
        return `${hours}小时前`
      } else if (diff < 2592000000) {
        const days = Math.floor(diff / 86400000)
        return `${days}天前`
      } else {
        return '长时间未通信'
      }
    },

    /** 获取设备类型显示文本（与系统一致） */
    getDeviceTypeText(deviceNode) {
      const textMap = {
        'DEVICE': '直',
        'GATEWAY': '网',
        'GATEWAY_SUB_DEVICE': '子'
      }
      return textMap[deviceNode] || deviceNode
    },

    /** 解析配置信息（与系统一致） */
    getConfigInfo(configStr, deviceNode) {
      return getDeviceConfigByType(configStr, deviceNode)
    },

    /** 跳转详情页（WVP专业详情） */
    handleDetail(record) {
      if (!this.queryParam.instanceKey) {
        this.$message.warning('请先选择平台实例')
        return
      }
      this.$router.push(`/video/wvp/details/${this.queryParam.instanceKey}/${record.deviceId}`)
    }
  }
}
</script>

<style lang="less" scoped>
.search-card {
  margin-bottom: 16px;
}

.table-card {}

.table-operations {
  display: flex;
  justify-content: space-between;
  padding-bottom: 16px;
}

/deep/ .ant-table-pagination {
  padding-top: 16px;
}

/* 统一样式：与系统设备列表一致 */
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

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #999;
  margin-right: 4px;
}

.online .status-dot {
  background-color: #52c41a;
}

.offline .status-dot {
  background-color: #f5222d;
}

.status-text {
  font-size: 12px;
}

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
</style>
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

.status-badge.online .status-dot {
  background-color: #52c41a;
}

.status-badge.offline .status-dot {
  background-color: #f5222d;
}
