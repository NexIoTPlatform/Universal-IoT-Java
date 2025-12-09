<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="设备名称/ID">
                <a-input v-model="deviceQuery.keyword" placeholder="请输入设备名称或ID" @keyup.enter="queryDevices" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="在线状态">
                <a-select v-model="deviceQuery.status" placeholder="请选择状态" allow-clear style="width: 100%">
                  <a-select-option value="online">在线</a-select-option>
                  <a-select-option value="offline">离线</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons" :style="{ float: 'right', overflow: 'hidden' }">
                <a-button type="primary" @click="queryDevices">
                  <a-icon type="search"/>查询
                </a-button>
                <a-button style="margin-left: 8px" @click="resetDeviceQuery">
                  <a-icon type="redo"/>重置
                </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 操作按钮 -->
      <div class="table-operations">
        <a-button type="primary" :disabled="multiple" @click="showImportModal">
          <a-icon type="plus"/>勾选使用
        </a-button>
      </div>

      <a-table
        :loading="loading"
        rowKey="deviceId"
        :columns="deviceColumns"
        :data-source="devices"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false"
        size="default"
      >
        <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
          <div class="device-info-content">
            <div class="device-name">
              <a-button type="link" @click="showChannelModal(record)"  class="device-name-link">{{ record.deviceName || '未命名设备' }}</a-button>
            </div>
            <div class="device-id">{{ record.deviceId }}</div>
          </div>
        </div>

        <div slot="productInfo" slot-scope="text, record" class="product-info-cell">
          <div class="product-name">{{ record.deviceModel || '未知型号' }}</div>
          <div class="product-key">{{ record.manufacturer || '-' }}</div>
        </div>

        <div slot="networkInfo" slot-scope="text, record" class="network-info-cell">
          <div class="network-ip">{{ record.deviceIp || '-' }}/{{ record.devicePort || '-' }}</div>
          <div class="network-port"></div>
        </div>

        <div slot="state" slot-scope="text, record" class="state-cell">
          <div :class="{ 'status-badge online': record.deviceStatus === 'ON' || record.deviceStatus === 'online', 'status-badge offline': record.deviceStatus !== 'ON' && record.deviceStatus !== 'online' }">
            <span class="status-dot"></span>
            <span class="status-text">{{ (record.deviceStatus === 'ON' || record.deviceStatus === 'online') ? '在线' : '离线' }}</span>
          </div>
        </div>

        <span slot="channelCount" slot-scope="text, record">
          <a @click="showChannelModal(record)" style="color: #1890ff; cursor: pointer;">
            {{ (record.channelList || []).length }}
          </a>
        </span>
        <span slot="action" slot-scope="text, record" class="operation-buttons">
          <a @click="showChannelModal(record)" class="operation-btn">
            <a-icon type="unordered-list" />通道列表
          </a>
        </span>
      </a-table>

      <a-pagination
        class="ant-table-pagination"
        show-size-changer
        show-quick-jumper
        :current="deviceQuery.page"
        :page-size="deviceQuery.pageSize"
        :total="deviceTotal"
        :showTotal="t => `共 ${t} 条`"
        @change="changeDevicePage"
        @showSizeChange="onDeviceSizeChange"
      />
    </a-card>

    <!-- 勾选使用弹窗 -->
    <a-modal 
      v-model="importModalVisible" 
      title="勾选使用设备" 
      @ok="handleImport" 
      @cancel="handleImportCancel" 
      :confirmLoading="importLoading"
      wrapClassName="import-modal"
    >
      <a-form :label-col="{span:6}" :wrapper-col="{span:16}">
        <a-form-item label="选择产品">
          <a-select v-model="importForm.productKey" placeholder="自动创建平台产品" allow-clear>
            <a-select-option value="">自动创建平台产品</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="已选设备">
          <div class="selected-device-tags">
            <a-tag 
              v-for="dev in selectedRows" 
              :key="dev.deviceId || dev.extDeviceId"
            >
              {{ (dev.deviceName || dev.name) }}（{{ dev.deviceId || dev.extDeviceId }}）
            </a-tag>
          </div>
          <div v-if="selectedRows.length===0" class="selected-empty">未选择任何设备</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 通道列表弹窗 -->
    <a-modal 
      v-model="channelModalVisible" 
      :title="currentDeviceForChannels ? `${currentDeviceForChannels.deviceName} - 通道列表` : '通道列表'"
      width="900px"
      :footer="null"
      :bodyStyle="{ padding: '20px' }"
    >
      <a-table
        :columns="channelListColumns"
        :data-source="(currentDeviceForChannels && currentDeviceForChannels.channelList) || []"
        rowKey="channelId"
        size="small"
        :pagination="false"
        :scroll="{ y: 500 }"
      >
        <span slot="channelStatus" slot-scope="text">
          <a-badge :status="text === 'ON' ? 'success' : 'default'" :text="text === 'ON' ? '在线' : '离线'" />
        </span>
        <span slot="ptzType" slot-scope="text">
          {{ formatPtzType(text) }}
        </span>
        <span slot="channelAction" slot-scope="text, record">
          <a @click="openPreviewModal(record)" style="font-size: 12px; margin-right: 12px;">
            <a-icon type="play-circle" /> 预览
          </a>
          <a @click="viewChannelDetail(record)" style="font-size: 12px">
            <a-icon type="eye" /> 详情
          </a>
        </span>
      </a-table>
    </a-modal>
    
    <!-- 实时预览弹窗 -->
    <live-player-modal
      v-model="previewModalVisible"
      :title="currentPreviewChannel ? `${currentPreviewChannel.name} - 实时预览` : '实时预览'"
      :streamInfo="previewStreamInfo"
      :hasAudio="true"
      :showPtz="currentPreviewChannel && formatPtzType(currentPreviewChannel.ptzType) !== '不支持'"
      :showBroadcast="true"
      :deviceId="currentDeviceForChannels ? currentDeviceForChannels.deviceId : ''"
      :channelId="currentPreviewChannel ? currentPreviewChannel.channelId : ''"
      @ptz="handlePtzCommand"
      @broadcast-start="handleBroadcastStart"
      @broadcast-stop="handleBroadcastStop"
      @close="closePreviewModal"
    />
  </page-header-wrapper>
</template>

<script>
import { getPlatform, subscribePlatform, unsubscribePlatform, testPlatform, getPlatformDevices, importDevices } from '@/api/video/platform'
import { getPreviewUrl, controlPTZ } from '@/api/video/channel'
import LivePlayerModal from '@/components/LivePlayerModal'

export default {
  name: 'VideoPlatformDetail',
  components: { LivePlayerModal },
  data() {
    return {
      instance: {},
      metrics: { deviceTotal: '-', onlineRate: '-', latencyMs: '-' },
      loading: false,
      subscription: { enabled: false, topics: [], callback: '' },
      // 组织树
      // 设备列表
      devices: [],
      deviceTotal: 0,
      deviceQuery: {
        page: 1,
        pageSize: 10,
        keyword: undefined,
        status: undefined,
        orgId: undefined
      },
      selectedRowKeys: [],
      selectedRows: [],
      multiple: true,
      // 传感器列表
      sensors: [],
      sensorTotal: 0,
      sensorLoading: false,
      sensorQuery: {
        page: 1,
        pageSize: 10,
        name: undefined,
        type: undefined,
        status: undefined
      },
      sensorColumns: [
        { title: '传感器ID', dataIndex: 'id', key: 'id', width: 130, ellipsis: true },
        { title: '传感器名称', dataIndex: 'name', key: 'name', width: 140, ellipsis: true },
        { title: '类型', dataIndex: 'typeName', key: 'typeName', width: 100 },
        { title: '所属区域', dataIndex: 'area', key: 'area', width: 120, ellipsis: true },
        { title: '当前值', dataIndex: 'value', key: 'value', width: 100, scopedSlots: { customRender: 'value' } },
        { title: '阈值范围', dataIndex: 'threshold', key: 'threshold', width: 100 },
        { title: '告警', dataIndex: 'alert', key: 'alert', width: 70, scopedSlots: { customRender: 'alert' } },
        { title: '状态', dataIndex: 'status', key: 'status', width: 70, scopedSlots: { customRender: 'status' } },
        { title: '最后上报', dataIndex: 'lastReport', key: 'lastReport', width: 150 }
      ],
      // 通道列表
      channels: [],
      channelTotal: 0,
      channelLoading: false,
      channelQuery: {
        page: 1,
        pageSize: 10,
        name: undefined,
        status: undefined
      },
      channelColumns: [
        { title: '通道编号', dataIndex: 'channelNo', key: 'channelNo', width: 80 },
        { title: '通道名称', dataIndex: 'name', key: 'name', width: 140, ellipsis: true },
        { title: '所属设备', dataIndex: 'deviceName', key: 'deviceName', width: 140, ellipsis: true },
        { title: '设备ID', dataIndex: 'deviceId', key: 'deviceId', width: 160, ellipsis: true },
        { title: '码流类型', dataIndex: 'streamType', key: 'streamType', width: 90 },
        { title: '分辨率', dataIndex: 'resolution', key: 'resolution', width: 100 },
        { title: '帧率', dataIndex: 'frameRate', key: 'frameRate', width: 70 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 70, scopedSlots: { customRender: 'status' } },
        { title: '最后更新', dataIndex: 'lastUpdate', key: 'lastUpdate', width: 150 },
        { title: '操作', key: 'action', width: 120, fixed: 'right', scopedSlots: { customRender: 'action' } }
      ],
      // 导入弹窗
      importModalVisible: false,
      importLoading: false,
      importForm: { productKey: '' },
      deviceColumns: [
        { 
          title: '设备名称/ID', 
          dataIndex: 'deviceInfo', 
          scopedSlots: { customRender: 'deviceInfo' },
          width: '22%',
          align: 'left'
        },
        { 
          title: '型号/制造商', 
          dataIndex: 'productInfo', 
          scopedSlots: { customRender: 'productInfo' },
          width: '18%',
          align: 'left'
        },
        { 
          title: 'IP/端口', 
          dataIndex: 'networkInfo', 
          scopedSlots: { customRender: 'networkInfo' },
          width: '16%',
          align: 'left'
        },
        { 
          title: '状态', 
          dataIndex: 'state', 
          width: '10%', 
          scopedSlots: { customRender: 'state' },
          align: 'center'
        },
        { 
          title: '通道数', 
          key: 'channelCount', 
          width: '10%', 
          scopedSlots: { customRender: 'channelCount' },
          align: 'center'
        },
        { 
          title: '操作', 
          key: 'action', 
          width: '120px', 
          fixed: 'right', 
          scopedSlots: { customRender: 'action' },
          align: 'center'
        }
      ],
      // 通道列表弹窗
      channelModalVisible: false,
      currentDeviceForChannels: null,
      channelListColumns: [
        { title: '通道ID', dataIndex: 'channelId', key: 'channelId', width: 150, ellipsis: true },
        { title: '通道名称', dataIndex: 'name', key: 'name', width: 140, ellipsis: true },
        { title: '状态', dataIndex: 'status', key: 'status', width: 80, scopedSlots: { customRender: 'channelStatus' } },
        { title: 'PTZ类型', dataIndex: 'ptzType', key: 'ptzType', width: 100, scopedSlots: { customRender: 'ptzType' } },
        { title: '编码格式', dataIndex: 'streamType', key: 'streamType', width: 100 },
        { title: '操作', key: 'channelAction', width: 100, scopedSlots: { customRender: 'channelAction' } }
      ],
      // 实时预览弹窗
      previewModalVisible: false,
      currentPreviewChannel: null,
      previewStreamInfo: null,
      previewLoading: false
    }
  },

  created() {
    this.loadInstance()
  },
  methods: {
    // 格式
    platformTypeFormat(val) {
      const map = { wvp: 'WVP', ics: '海康ISC', icc: '大华ICC' }
      return map[val] || val
    },
    getPlatformLogo(type) {
      const logoMap = {
        wvp: require('@/assets/images/logo.png'),
        ics: require('@/assets/images/logo.png'),
        icc: require('@/assets/images/logo.png')
      }
      return logoMap[type] || require('@/assets/images/logo.png')
    },

    async loadInstance() {
      const instanceKey = this.$route.params.instanceKey
      const res = await getPlatform(instanceKey)
      this.instance = res.data || {}
      this.loadMetrics()
      this.loadSubscription()
      this.queryDevices()
    },
    loadSubscription() {
      try {
        const options = this.instance.options ? JSON.parse(this.instance.options) : {}
        this.subscription = options.subscription || { enabled: false, topics: [], callback: '' }
      } catch (e) {
        this.subscription = { enabled: false, topics: [], callback: '' }
      }
    },
    async applySubscription() {
      await subscribePlatform(this.instance.instanceKey)
      this.$message.success('订阅已应用')
    },
    async cancelSubscription() {
      await unsubscribePlatform(this.instance.instanceKey)
      this.$message.success('订阅已取消')
    },
    async loadMetrics() {
      try {
        const t = await testPlatform({ instanceKey: this.instance.instanceKey })
        const { latencyMs } = t.data || {}
        const list = await getPlatformDevices(this.instance.instanceKey, { page: 1, pageSize: 100 })
        const arr = list.data || []
        const online = arr.filter(d => d.status === 'online').length
        this.metrics = {
          deviceTotal: arr.length,
          onlineRate: arr.length ? `${Math.round((online / arr.length) * 100)}%` : '-',
          latencyMs: latencyMs || '-'
        }
      } catch (e) {
        // 安全降级
      }
    },

    async queryDevices() {
      this.loading = true
      try {
        const params = {
          page: this.deviceQuery.page,
          pageSize: this.deviceQuery.pageSize,
          keyword: this.deviceQuery.keyword,
          status: this.deviceQuery.status,
          orgId: this.deviceQuery.orgId
        }
        const res = await getPlatformDevices(this.instance.instanceKey, params)
        this.devices = res.data || []
        this.deviceTotal = res.total || this.devices.length
      } catch (e) {
        this.$message.error('查询设备失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.loading = false
      }
    },
    resetDeviceQuery() {
      this.deviceQuery.keyword = undefined
      this.deviceQuery.status = undefined
      this.deviceQuery.page = 1
      this.queryDevices()
    },
    changeDevicePage(page, pageSize) {
      this.deviceQuery.page = page
      this.deviceQuery.pageSize = pageSize
      this.queryDevices()
    },
    onDeviceSizeChange(current, size) {
      this.deviceQuery.page = 1
      this.deviceQuery.pageSize = size
      this.queryDevices()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.multiple = !selectedRowKeys.length
    },
    showImportModal() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择设备')
        return
      }
      this.importForm.productKey = ''
      this.importModalVisible = true
    },
    handleImportCancel() {
      this.importModalVisible = false
    },
    async handleImport() {
      this.importLoading = true
      try {
        const payload = { devices: this.selectedRows, productKey: this.importForm.productKey || null }
        const res = await importDevices(this.instance.instanceKey, payload)
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
    async handleTest() {
      try {
        const res = await testPlatform({ instanceKey: this.instance.instanceKey })
        const { reachable, latencyMs } = res.data || {}
        if (reachable) {
          this.$message.success(`连接测试成功，延时${latencyMs}ms`)
        } else {
          this.$message.error('连接测试失败')
        }
      } catch (e) {
        this.$message.error('测试失败：' + (e.response?.data?.msg || e.message))
      }
    },
    handleRefresh() {
      this.queryDevices()
      this.loadMetrics()
    },
    async querySensors() {
      this.sensorLoading = true
      try {
        // 模拟数据
        const mockSensors = [
          { id: 'SEN001', name: '机房温度传感器', type: 'temperature', typeName: '温度传感器', area: '一楼机房', value: '24.5', unit: '℃', threshold: '18-28℃', alert: false, status: 'online', lastReport: '2025-11-04 20:32:10' },
          { id: 'SEN002', name: '机房湿度传感器', type: 'humidity', typeName: '湿度传感器', area: '一楼机房', value: '65', unit: '%', threshold: '40-70%', alert: false, status: 'online', lastReport: '2025-11-04 20:32:08' },
          { id: 'SEN003', name: '一楼烟雾报警器', type: 'smoke', typeName: '烟雾传感器', area: '一楼大厅', value: '正常', unit: '', threshold: '-', alert: false, status: 'online', lastReport: '2025-11-04 20:31:55' },
          { id: 'SEN004', name: '大门门磁', type: 'door', typeName: '门磁传感器', area: '一楼前门', value: '关闭', unit: '', threshold: '-', alert: false, status: 'online', lastReport: '2025-11-04 20:30:42' },
          { id: 'SEN005', name: '仓库温度监控', type: 'temperature', typeName: '温度传感器', area: '仓库A区', value: '18.2', unit: '℃', threshold: '15-25℃', alert: false, status: 'offline', lastReport: '2025-11-03 15:20:10' },
          { id: 'SEN006', name: '配电室温度', type: 'temperature', typeName: '温度传感器', area: '地下配电室', value: '32.8', unit: '℃', threshold: '18-30℃', alert: true, status: 'online', lastReport: '2025-11-04 20:31:45' },
          { id: 'SEN007', name: '仓库湿度监控', type: 'humidity', typeName: '湿度传感器', area: '仓库A区', value: '72', unit: '%', threshold: '40-70%', alert: true, status: 'online', lastReport: '2025-11-04 20:30:28' },
          { id: 'SEN008', name: '二楼烟雾报警', type: 'smoke', typeName: '烟雾传感器', area: '二楼走廊', value: '正常', unit: '', threshold: '-', alert: false, status: 'online', lastReport: '2025-11-04 20:32:00' },
          { id: 'SEN009', name: '仓库门磁', type: 'door', typeName: '门磁传感器', area: '仓库B区', value: '开启', unit: '', threshold: '-', alert: false, status: 'online', lastReport: '2025-11-04 20:29:15' },
          { id: 'SEN010', name: '会议室温度', type: 'temperature', typeName: '温度传感器', area: '三楼会议室', value: '22.3', unit: '℃', threshold: '20-26℃', alert: false, status: 'online', lastReport: '2025-11-04 20:31:50' }
        ]
        
        let filtered = mockSensors
        if (this.sensorQuery.name) {
          filtered = filtered.filter(s => s.name.includes(this.sensorQuery.name))
        }
        if (this.sensorQuery.type) {
          filtered = filtered.filter(s => s.type === this.sensorQuery.type)
        }
        if (this.sensorQuery.status) {
          filtered = filtered.filter(s => s.status === this.sensorQuery.status)
        }
        
        this.sensors = filtered
        this.sensorTotal = filtered.length
      } finally {
        this.sensorLoading = false
      }
    },
    resetSensorQuery() {
      this.sensorQuery.name = undefined
      this.sensorQuery.type = undefined
      this.sensorQuery.status = undefined
      this.sensorQuery.page = 1
      this.querySensors()
    },
    changeSensorPage(page, pageSize) {
      this.sensorQuery.page = page
      this.sensorQuery.pageSize = pageSize
      this.querySensors()
    },
    onSensorSizeChange(current, size) {
      this.sensorQuery.page = 1
      this.sensorQuery.pageSize = size
      this.querySensors()
    },
    async queryChannels() {
      this.channelLoading = true
      try {
        // 模拟数据
        const mockChannels = [
          { id: 1, channelNo: '1', name: '主码流', deviceName: '前门口摄像机', deviceId: '34020000001320000001', streamType: 'H.264', resolution: '1920x1080', frameRate: '25fps', status: 'online', lastUpdate: '2025-11-04 20:30:15' },
          { id: 2, channelNo: '2', name: '子码流', deviceName: '前门口摄像机', deviceId: '34020000001320000001', streamType: 'H.264', resolution: '704x576', frameRate: '15fps', status: 'online', lastUpdate: '2025-11-04 20:30:15' },
          { id: 3, channelNo: '1', name: '主码流', deviceName: '后门监控', deviceId: '34020000001320000002', streamType: 'H.265', resolution: '2560x1440', frameRate: '25fps', status: 'online', lastUpdate: '2025-11-04 20:28:42' },
          { id: 4, channelNo: '2', name: '子码流', deviceName: '后门监控', deviceId: '34020000001320000002', streamType: 'H.265', resolution: '640x480', frameRate: '15fps', status: 'online', lastUpdate: '2025-11-04 20:28:42' },
          { id: 5, channelNo: '1', name: '主码流', deviceName: '车库入口', deviceId: '34020000001320000003', streamType: 'H.264', resolution: '1920x1080', frameRate: '25fps', status: 'offline', lastUpdate: '2025-11-03 18:22:10' },
          { id: 6, channelNo: '1', name: '主码流', deviceName: '一楼大厅', deviceId: '34020000001320000004', streamType: 'H.265', resolution: '3840x2160', frameRate: '30fps', status: 'online', lastUpdate: '2025-11-04 20:31:05' },
          { id: 7, channelNo: '2', name: '子码流', deviceName: '一楼大厅', deviceId: '34020000001320000004', streamType: 'H.265', resolution: '1280x720', frameRate: '15fps', status: 'online', lastUpdate: '2025-11-04 20:31:05' },
          { id: 8, channelNo: '1', name: '主码流', deviceName: '二楼走廊', deviceId: '34020000001320000005', streamType: 'H.264', resolution: '1920x1080', frameRate: '25fps', status: 'online', lastUpdate: '2025-11-04 20:29:33' },
          { id: 9, channelNo: '1', name: '主码流', deviceName: '会议室监控', deviceId: '34020000001320000006', streamType: 'H.265', resolution: '2560x1440', frameRate: '25fps', status: 'online', lastUpdate: '2025-11-04 20:31:20' },
          { id: 10, channelNo: '2', name: '子码流', deviceName: '会议室监控', deviceId: '34020000001320000006', streamType: 'H.265', resolution: '704x576', frameRate: '15fps', status: 'online', lastUpdate: '2025-11-04 20:31:20' }
        ]
        
        let filtered = mockChannels
        if (this.channelQuery.name) {
          filtered = filtered.filter(c => c.name.includes(this.channelQuery.name) || c.deviceName.includes(this.channelQuery.name))
        }
        if (this.channelQuery.status) {
          filtered = filtered.filter(c => c.status === this.channelQuery.status)
        }
        
        this.channels = filtered
        this.channelTotal = filtered.length
      } finally {
        this.channelLoading = false
      }
    },
    resetChannelQuery() {
      this.channelQuery.name = undefined
      this.channelQuery.status = undefined
      this.channelQuery.page = 1
      this.queryChannels()
    },
    changeChannelPage(page, pageSize) {
      this.channelQuery.page = page
      this.channelQuery.pageSize = pageSize
      this.queryChannels()
    },
    onChannelSizeChange(current, size) {
      this.channelQuery.page = 1
      this.channelQuery.pageSize = size
      this.queryChannels()
    },
    viewDevice(record) {
      // 根据平台类型跳转到对应的设备详情页
      let routePath = ''
      
      if (this.instance.platformType === 'wvp') {
        routePath = `/videoCenter/wvp/${this.instance.instanceKey}/${record.deviceId}`
      } else if (this.instance.platformType === 'ics') {
        routePath = `/videoCenter/hik/${this.instance.instanceKey}/${record.deviceId}`
      } else if (this.instance.platformType === 'icc') {
        routePath = `/videoCenter/dahua/${this.instance.instanceKey}/${record.deviceId}`
      } else {
        this.$message.warning('暂不支持该平台类型的设备详情页')
        return
      }
      
      this.$router.push(routePath).catch(err => {
        console.error('路由跳转失败:', err)
        this.$message.error('页面跳转失败，请稍后重试')
      })
    },
    showChannelModal(device) {
      this.currentDeviceForChannels = device
      this.channelModalVisible = true
    },
    async openPreviewModal(channel, playbackUrl = null) {
      this.currentPreviewChannel = channel
      this.previewModalVisible = true
      this.previewLoading = true
      this.previewStreamInfo = null
      
      // 如果传入了回放地址，直接使用
      if (playbackUrl) {
        // 为回放地址构造streamInfo
        this.previewStreamInfo = {
          flv: playbackUrl,
          app: 'playback',
          stream: channel.channelId,
          mediaServerId: 'unknown'
        }
        this.previewLoading = false
        return
      }
      
      try {
        const res = await getPreviewUrl(
          this.instance.instanceKey,
          this.currentDeviceForChannels.deviceId,
          channel.channelId,
          'main'
        )
        
        // 直接使用后端返回的streamInfo结构
        this.previewStreamInfo = res.data || {}
        
        // 检查是否有有效流地址
        const hasValidStream = this.previewStreamInfo.ws_flv || 
                              this.previewStreamInfo.wss_flv || 
                              this.previewStreamInfo.flv || 
                              this.previewStreamInfo.hls || 
                              this.previewStreamInfo.rtmp
        
        if (!hasValidStream) {
          this.$message.warning('未获取到流地址')
        }
      } catch (e) {
        this.$message.error('获取预览地址失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.previewLoading = false
      }
    },
    closePreviewModal() {
      this.previewModalVisible = false
      this.previewStreamInfo = null
      this.currentPreviewChannel = null
    },
    handlePtzCommand({ command, speed }) {
      if (!this.currentPreviewChannel) {
        this.$message.warning('请先选择通道')
        return
      }
      
      controlPTZ(
        this.instance.instanceKey,
        this.currentDeviceForChannels.deviceId,
        this.currentPreviewChannel.channelId,
        command,
        speed
      ).catch(e => {
        this.$message.error('PTZ控制失败：' + (e.response?.data?.msg || e.message))
      })
    },
    handleBroadcastStart({ deviceId, channelId, mode }) {
      console.log('Start broadcast:', { deviceId, channelId, mode })
      // TODO: 调用后端接口启动语音对讲
      // 这里需要后端完善相关接口
      this.$message.info('语音对讲功能待后端接口完善')
    },
    handleBroadcastStop({ deviceId, channelId }) {
      console.log('Stop broadcast:', { deviceId, channelId })
      // TODO: 调用后端接口停止语音对讲
    },
    viewChannelDetail(channel) {
      // 跳转到通道详情页（WvpPlatformDetail.vue）
      const deviceId = this.currentDeviceForChannels.deviceId
      const channelId = channel.channelId
      
      let routePath = ''
      if (this.instance.platformType === 'wvp') {
        routePath = `/videoCenter/wvp/${this.instance.instanceKey}/${deviceId}/${channelId}`
      } else if (this.instance.platformType === 'ics') {
        routePath = `/videoCenter/hik/${this.instance.instanceKey}/${deviceId}/${channelId}`
      } else if (this.instance.platformType === 'icc') {
        routePath = `/videoCenter/dahua/${this.instance.instanceKey}/${deviceId}/${channelId}`
      }
      
      this.$router.push(routePath).catch(err => {
        console.error('跳转失败:', err)
      })
      
      this.channelModalVisible = false
    },
    formatPtzType(type) {
      const map = {
        0: '不支持',
        1: '球机',
        2: '半球',
        3: '固定枪机',
        4: '遥控枪机'
      }
      return map[type] || '-'
    },
    previewChannel(record) {
      this.$message.info('预览通道：' + record.name)
    },
    playbackChannel(record) {
      this.$message.info('回放通道：' + record.name)
    }
  }
}
</script>

<style scoped>
/* 导入弹窗样式优化 */
::v-deep .import-modal .ant-modal-header {
  padding: 12px 16px;
}
::v-deep .import-modal .ant-modal-title {
  font-size: 14px;
  font-weight: 600;
}
::v-deep .import-modal .ant-modal-body {
  padding: 16px;
}

.selected-device-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 160px;
  overflow-y: auto;
}
.selected-empty {
  color: #999;
}
.device-info-cell {
  padding: 12px 0;
}

.device-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
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

.device-name-link:hover {
  color: #40a9ff;
}

.device-id {
  font-size: 13px;
  color: #595959;
  font-family: 'Courier New', monospace;
  font-weight: 600;
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

.network-info-cell {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.network-ip {
  font-size: 13px;
  color: #262626;
  font-family: 'Courier New', monospace;
  font-weight: 500;
  line-height: 1.4;
}

.network-port {
  font-size: 12px;
  color: #595959;
  font-family: 'Courier New', monospace;
  line-height: 1.4;
}
</style>
