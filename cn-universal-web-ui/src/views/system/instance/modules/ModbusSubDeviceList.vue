<template>
  <div>
    <!-- Tab导航 -->
    <a-tabs v-model="activeTab" type="card" class="modbus-tabs" @change="onTabChange">
      <a-tab-pane key="visualization" tab="拓扑关系">
        <div class="topology-container">
          <div class="topology-header">
            <h4>设备拓扑关系</h4>
            <a-button
              type="primary"
              size="small"
              icon="reload"
              @click="refreshTopology"
              :loading="topologyLoading"
            >
              {{ $t('button.refresh') }}
            </a-button>
          </div>

          <div class="topology-content">
            <!-- 网关设备 -->
            <div class="gateway-card">
              <div class="gateway-badge">网关</div>
              <div class="gateway-image">
                <div class="pic" v-if="getGatewayImageUrl()">
                  <img :src="getGatewayImageUrl()" alt="网关设备图片"/>
                </div>
                <div class="pic-placeholder" v-else>
                  <div class="default-icon">🌐</div>
                </div>
              </div>
              <div class="gateway-info">
                <div class="gateway-name">{{ gatewayInfo.deviceName || gwDeviceId }}</div>
                <div class="gateway-type">{{ gatewayInfo.productName || '网关设备' }}</div>
                <div class="gateway-protocol" v-if="gatewayProductInfo.transportProtocol">
                  协议: {{ gatewayProductInfo.transportProtocol }}
                </div>
                <div class="gateway-status">
                  <span class="status-dot" :class="gatewayInfo.state ? 'online' : 'offline'"></span>
                  {{ gatewayInfo.state ? '在线' : '离线' }}
                  <span v-if="gatewayInfo.onlineTime" class="online-time">
                        ({{ formatOnlineTime(gatewayInfo.onlineTime) }})
                      </span>
                </div>
              </div>
            </div>

            <!-- 连接线 -->
            <div class="connection-area">
              <div class="connection-line"></div>
            </div>

            <!-- 子设备列表 -->
            <div class="subdevices-grid">
              <div v-for="(subDevice, slaveAddress) in subDeviceMapping" :key="slaveAddress"
                   class="subdevice-card" :class="{ 'offline': !subDevice.state }">

                <!-- 设备图片 -->
                <div class="subdevice-image">
                  <div class="pic" v-if="getSubDeviceImageUrl(subDevice)">
                    <img :src="getSubDeviceImageUrl(subDevice)" alt="子设备图片"/>
                  </div>
                  <div class="pic-placeholder" v-else>
                    <div class="default-icon">📱</div>
                  </div>
                </div>

                <!-- 设备信息 -->
                <div class="subdevice-info">
                  <div class="subdevice-name">{{ subDevice.productName }}</div>
                  <div class="subdevice-id">{{ subDevice.deviceId }}</div>
                  <div class="subdevice-address">地址: {{ slaveAddress || '未知' }}</div>
                  <!-- 在线状态 -->
                  <div class="subdevice-status">
                    <span class="status-dot" :class="subDevice.state ? 'online' : 'offline'"></span>
                    {{ subDevice.state ? '在线' : '离线' }}
                    <span v-if="subDevice.onlineTime" class="online-time">
                      ({{ formatOnlineTime(subDevice.onlineTime) }})
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-tab-pane>
      <a-tab-pane key="list" tab="子设备列表">
        <!-- 搜索条件 -->
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item label="从站地址">
                  <a-input-number
                    v-model="queryParam.slaveAddress"
                    :min="1"
                    :max="247"
                    placeholder="请输入从站地址"
                    allow-clear
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('device.name')">
                  <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称"
                           allow-clear/>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
            <span class="table-page-search-submitButtons">
              <a-button type="primary" @click="handleQuery">
                <a-icon type="search"/>查询
              </a-button>
              <a-button style="margin-left: 8px" @click="resetQuery">
                <a-icon type="refresh"/>重置
              </a-button>
            </span>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 操作按钮 -->
        <div class="table-operations">
          <a-button type="primary" @click="$refs.createForm.handleAdd()">
            <a-icon type="plus"/>
            新增{{ gatewayProductInfo.transportProtocol }}子设备
          </a-button>
          <a-button type="danger" :disabled="multiple" @click="handleDelete">
            <a-icon type="delete"/>
            {{ $t('button.delete') }}
          </a-button>
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

        <!-- 数据表格 -->
        <a-table
          :loading="loading"
          :columns="columns"
          :data-source="deviceList"
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
          :pagination="false"
          row-key="id"
        >
          <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
            <a-popover placement="rightTop" trigger="hover" :title="'设备配置信息'"
                       v-if="getConfigInfo(record.configuration)">
              <template slot="content">
                <div class="config-popover">
                  <div v-for="(value, key) in getConfigInfo(record.configuration)" :key="key"
                       class="config-popover-item">
                    <span class="config-popover-key">{{ key }}:</span>
                    <span class="config-popover-value">{{ value }}</span>
                  </div>
                </div>
              </template>
              <div class="device-info-content">
                <div class="device-name">
                  <a-button type="link"
                            @click="viewDeviceDetails(record)"
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
                          @click="viewDeviceDetails(record)"
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
              <device-type-badge :type="record.deviceNode"
                                 :text="getDeviceTypeText(record.deviceNode)"/>
            </div>
            <div class="product-key">{{ record.productKey }}</div>
          </div>

          <div slot="slaveAddress" slot-scope="text, record" class="slave-address-cell">
            <a-tag color="blue" class="slave-address-tag">{{ getSlaveAddress(record) }}</a-tag>
            <!-- <device-type-badge :type="GATEWAY" :text="getSlaveAddress(record)" /> -->

          </div>

          <div slot="state" slot-scope="text, record" class="state-cell">
            <div
              :class="{ 'status-badge online': record.state, 'status-badge offline': !record.state }">
              <span class="status-dot"></span>
              <span class="status-text">{{ stateFormat(record) }}</span>
            </div>
          </div>

          <div slot="onlineTime" slot-scope="text, record" class="online-time-cell">
            <div class="time-main">{{ formatTimeFromUtils(record.onlineTime) }}</div>
            <div class="time-ago">{{ getTimeAgoFromUtils(record.onlineTime) }}</div>
          </div>

          <span slot="operation" slot-scope="text, record" class="operation-buttons">
        <a @click="$refs.createForm.handleUpdate(record)"
           class="operation-btn">
          <a-icon type="edit"/>{{ $t('button.edit') }}
        </a>
        <a-divider type="vertical"/>
        <a style="color:#F53F3F" @click="handleDelete(record)"
           class="operation-btn">
          <a-icon type="delete"/>{{ $t('button.delete') }}
        </a>
      </span>
        </a-table>

        <!-- 分页 -->
        <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper
                      :current="pagination.current"
                      :total="pagination.total" :page-size="pagination.pageSize"
                      :showTotal="total => `共 ${total} 条`"
                      @showSizeChange="onShowSizeChange" @change="changeSize"/>

        <!-- 新增/修改表单 -->
        <ChildDeviceCreateForm
          ref="createForm"
          :stateOptions="stateOptions"
          :gwProductKey="gwProductKey"
          :gwDeviceId="gwDeviceId"
          @ok="getList"
        />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import ChildDeviceCreateForm from './ChildDeviceCreateForm.vue'
import {delInstance, getSubDeviceRelation, listInstance} from '@/api/system/dev/instance'
import {parseTime} from '@/utils/ruoyi'
import {getModbusDeviceConfig} from '@/utils/deviceConfig'
import {formatOnlineTime, getTimeAgo} from '@/utils/time'

export default {
  name: 'ModbusSubDeviceList',
  components: {
    ChildDeviceCreateForm
  },
  props: {
    gwDeviceId: {
      type: String,
      required: true
    },
    gwProductKey: {
      type: String,
      required: true
    },
    // 新增：从父组件传递的网关详细信息
    gatewayInfo: {
      type: Object,
      default: () => ({})
    },
    // 新增：从父组件传递的产品详细信息
    gatewayProductInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      topologyLoading: false, // 拓扑关系加载状态
      deviceList: [],
      selectedRowKeys: [],
      selectedRows: [],
      multiple: true,
      stateOptions: [],
      activeTab: 'visualization', // 当前激活的tab
      gatewayConfig: null, // 网关配置信息
      subDeviceMapping: {}, // 子设备映射关系
      queryParam: {
        gwProductKey: '',
        extDeviceId: '',
        slaveAddress: undefined,
        deviceName: undefined,
        pageNum: 1,
        pageSize: 40
      },
      pagination: {
        current: 1,
        pageSize: 40,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: total => `共 ${total} 条`
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
          width: '20%',
          align: 'left'
        },
        {
          title: '从站地址',
          dataIndex: 'slaveAddress',
          scopedSlots: {customRender: 'slaveAddress'},
          width: '12%',
          align: 'center'
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
          width: '10%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  created() {
    this.initQueryParam()
    this.getList()
    this.getGatewayConfig()
    this.getDicts('dev_instance_state').then(response => {
      this.stateOptions = response.data
    })
  },
  methods: {
    /** 初始化查询参数 */
    initQueryParam() {
      this.queryParam.gwProductKey = this.gwProductKey
      this.queryParam.extDeviceId = this.gwDeviceId
    },
    getList() {
      this.loading = true
      console.log('ModbusSubDeviceList getList queryParam:', this.queryParam)
      listInstance(this.queryParam).then(response => {
        console.log('ModbusSubDeviceList getList response:', response)
        // 直接使用返回的子设备数据，不需要过滤
        this.deviceList = response.rows || []
        this.pagination.total = this.deviceList.length
        this.loading = false
      }).catch(error => {
        console.error('获取设备列表失败:', error)
        this.loading = false
      })
    },
    getSlaveAddress(record) {
      try {
        const config = JSON.parse(record.configuration || '{}')
        return config.slaveAddress || '-'
      } catch (e) {
        return '-'
      }
    },
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParam = {
        gwProductKey: this.gwProductKey,
        extDeviceId: this.gwDeviceId,
        slaveAddress: undefined,
        deviceName: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.multiple = !selectedRowKeys.length
    },
    handleDelete(row) {
      // 判断是否为单个删除（row是对象且有id属性）还是批量删除（row是事件对象或undefined）
      const ids = (row && typeof row === 'object' && row.id) ? [row.id] : this.selectedRowKeys
      this.$confirm({
        title: '确认删除所选中数据?',
        content: `当前选中 ${ids.length} 条数据`,
        onOk: () => {
          return delInstance(ids).then(() => {
            this.onSelectChange([], [])
            this.getList()
            this.$message.success('删除成功')
          })
        }
      })
    },
    viewDeviceDetails(record) {
      this.$router.push(`/system/instance/instance-details/${record.id}`)
    },
    parseTime(time, pattern) {
      return parseTime(time, pattern)
    },
    // 0-离线，1-在线 字典翻译
    stateFormat(row, column) {
      return this.selectDictLabel(this.stateOptions, row.state)
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
    /** 解析配置信息 */
    getConfigInfo(configStr) {
      return getModbusDeviceConfig(configStr)
    },
    /** 格式化在线时间 - 使用time.js中的方法，支持Unix时间戳和字符串时间 */
    formatTimeFromUtils(timestamp) {
      return formatOnlineTime(timestamp)
    },
    /** 获取时间距离现在的描述 - 使用time.js中的方法，支持Unix时间戳和字符串时间 */
    getTimeAgoFromUtils(timestamp) {
      return getTimeAgo(timestamp)
    },
    /** 格式化在线时间 - 简洁版本，用于拓扑图显示 */
    formatOnlineTime(onlineTime) {
      return getTimeAgo(onlineTime)
    },

    onShowSizeChange(current, pageSize) {
      this.pagination.pageSize = pageSize
      this.pagination.current = 1
      this.getList()
    },

    changeSize(current, pageSize) {
      this.pagination.current = current
      this.pagination.pageSize = pageSize
      this.getList()
    },

    /** 获取网关配置信息 */
    async getGatewayConfig() {
      try {
        console.log('开始获取网关配置，参数:',
          {gwProductKey: this.gwProductKey, gwDeviceId: this.gwDeviceId})

        // 构建查询网关设备的参数
        const gatewayQueryParam = {
          deviceId: this.gwDeviceId,
          productKey: this.gwProductKey,
          pageNum: 1,
          pageSize: 1
        }

        const response = await listInstance(gatewayQueryParam)
        if (response.rows && response.rows.length > 0) {
          const gatewayDevice = response.rows[0]
          this.gatewayConfig = gatewayDevice
          console.log('网关配置获取成功:', gatewayDevice)

          // 使用新的API获取子设备关系
          await this.getSubDeviceRelation()
        } else {
          console.warn('未找到网关设备')
        }
      } catch (error) {
        console.error('获取网关配置失败:', error)
        this.gatewayConfig = null
        this.subDeviceMapping = {}
      }
    },

    // 获取子设备关系
    async getSubDeviceRelation() {
      try {
        const response = await getSubDeviceRelation(this.gwProductKey, this.gwDeviceId)
        if (response && response.code === 0) {
          const subDevices = response.data || []
          // 将子设备列表转换为映射关系
          this.subDeviceMapping = {}
          subDevices.forEach(device => {
            // 优先使用ext1字段作为从站地址，如果为空则使用deviceId的最后部分
            let slaveAddress = device.ext1
            if (!slaveAddress) {
              // 从deviceId中提取从站地址（如：860048070262660-21 -> 21）
              const parts = device.deviceId.split('-')
              slaveAddress = parts[parts.length - 1] || device.deviceId
            }

            this.subDeviceMapping[slaveAddress] = device
          })

        } else {
          console.warn('子设备关系API返回错误:', response)
        }
      } catch (error) {
        console.error('获取子设备关系失败:', error)
        this.subDeviceMapping = {}
      }
    },

    // 刷新拓扑关系数据
    async refreshTopology() {
      this.topologyLoading = true
      try {
        await this.getGatewayConfig()
        await this.getSubDeviceRelation()
        this.$message.success('已刷新')
      } catch (error) {
        // this.$message.error('刷新失败，请重试')
      } finally {
        this.topologyLoading = false
      }
    },

    // Tab切换事件
    onTabChange(activeKey) {
      console.log('Tab切换到:', activeKey)
      if (activeKey === 'visualization') {
        // 切换到拓扑关系时刷新数据
        this.refreshTopology()
      } else if (activeKey === 'list') {
        // 切换到子设备列表时刷新列表数据
        this.getList()
      }
    },

    // 获取网关设备图片URL
    getGatewayImageUrl() {
      if (!this.gatewayProductInfo || !this.gatewayProductInfo.photoUrl) {
        return null
      }

      try {
        // 如果photoUrl是字符串，需要解析JSON
        let photoUrlData = this.gatewayProductInfo.photoUrl
        if (typeof photoUrlData === 'string') {
          photoUrlData = JSON.parse(photoUrlData)
        }

        return photoUrlData && photoUrlData.img ? photoUrlData.img : null
      } catch (e) {
        console.warn('解析网关产品图片URL失败:', e)
        return null
      }
    },

    // 获取子设备图片URL
    getSubDeviceImageUrl(subDevice) {
      if (!subDevice || !subDevice.photoUrl) {
        return null
      }

      try {
        // 如果photoUrl是字符串，需要解析JSON
        let photoUrlData = subDevice.photoUrl
        if (typeof photoUrlData === 'string') {
          photoUrlData = JSON.parse(photoUrlData)
        }

        return photoUrlData && photoUrlData.img ? photoUrlData.img : null
      } catch (e) {
        console.warn('解析子设备图片URL失败:', e)
        return null
      }
    }
  }
}
</script>

<style scoped lang="less">
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

/* 从站地址样式 */
.slave-address-cell {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0;
}

.slave-address-tag {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
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
  padding: 12px 0;
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

.operation-btn .anticon {
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

/* 子设备样式 - 橙色 */
.device-badge-sub {
  background-color: #fa8c16;
  border: 1px solid #ffc53d;
}

/* Tab样式 */
.modbus-tabs {
  .ant-tabs-content {
    padding-top: 16px;
  }
}

/* 拓扑关系样式 - 简洁美观版本 */
.topology-container {
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
  min-height: 300px;
}

.topology-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.topology-header h4 {
  color: #262626;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.topology-content {
  max-width: 1000px;
  margin: 0 auto;
}

/* 网关设备卡片 - 与子设备卡片风格一致，更窄的宽度 */
.gateway-card {
  position: relative;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #fff9f0 0%, #ffffff 100%);
  border: 2px solid #ff9800;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.15);
  max-width: 340px;
  margin-left: auto;
  margin-right: auto;
  transition: all 0.2s ease;
}

.gateway-card:hover {
  border-color: #ff6f00;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.25);
  transform: translateY(-2px);
}

/* 网关标识 */
.gateway-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ff9800;
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  z-index: 1;
  box-shadow: 0 1px 3px rgba(255, 152, 0, 0.3);
}

/* 网关设备图片 */
.gateway-image {
  margin-right: 14px;
  flex-shrink: 0;
}

.gateway-image .pic {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
}

.gateway-image .pic img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gateway-image .pic-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #1890ff;
}

.gateway-image .default-icon {
  font-size: 24px;
  color: #1890ff;
}

.gateway-info {
  flex: 1;
  min-width: 0;
  padding-right: 48px; /* 为网关标识留出空间 */
}

.gateway-name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gateway-type {
  font-size: 13px;
  color: #595959;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gateway-protocol {
  font-size: 12px;
  color: #1890ff;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gateway-status {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
}

.gateway-status .status-dot.online + * {
  color: #52c41a;
}

.gateway-status .status-dot.offline + * {
  color: #ff4d4f;
}

.online-time {
  margin-left: 4px;
  color: #8c8c8c;
  font-size: 10px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
  flex-shrink: 0;
}

.status-dot.online {
  background: #52c41a;
}

.status-dot.offline {
  background: #ff4d4f;
}

/* 连接线 */
.connection-area {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.connection-line {
  width: 1px;
  height: 20px;
  background: #d9d9d9;
}

/* 子设备网格 - 居中布局 */
.subdevices-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  max-width: 100%;
}

/* 子设备卡片样式 */
.subdevice-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  width: 280px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.subdevice-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

/* 离线状态样式 */
.subdevice-card.offline {
  background: #fafafa;
  border-color: #d9d9d9;
  opacity: 0.8;
}

.subdevice-card.offline:hover {
  border-color: #d9d9d9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transform: none;
}

/* 子设备图片 */
.subdevice-image {
  margin-right: 14px;
  flex-shrink: 0;
}

.subdevice-image .pic {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
}

.subdevice-image .pic img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.subdevice-image .pic-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  background: linear-gradient(135deg, #f0f5ff 0%, #d6e4ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #adc6ff;
}

.subdevice-image .default-icon {
  font-size: 24px;
  color: #597ef7;
}

/* 离线状态的图片样式 */
.subdevice-card.offline .subdevice-image .pic-placeholder {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border-color: #d9d9d9;
}

.subdevice-card.offline .subdevice-image .default-icon {
  color: #bfbfbf;
}

.subdevice-card.offline .subdevice-image .pic {
  opacity: 0.6;
  filter: grayscale(50%);
}

.subdevice-info {
  flex: 1;
  min-width: 0;
}

.subdevice-name {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.subdevice-id {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Courier New', monospace;
  line-height: 1.3;
}

.subdevice-address {
  font-size: 11px;
  color: #1890ff;
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 8px;
  font-weight: 500;
}

/* 子设备在线状态 - 与网关保持一致 */
.subdevice-status {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  margin-top: 4px;
}

.subdevice-status .status-dot.online + * {
  color: #52c41a;
}

.subdevice-status .status-dot.offline + * {
  color: #ff4d4f;
}


@media (max-width: 768px) {
  .subdevices-grid {
    grid-template-columns: 1fr;
  }

  .topology-content {
    max-width: 100%;
  }

  .topology-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .topology-header h4 {
    margin-bottom: 0;
  }
}
</style>
