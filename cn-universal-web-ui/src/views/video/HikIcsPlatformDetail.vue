<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 筛选条件区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="内容类型">
                <a-select v-model="contentType" placeholder="请选择内容类型" style="width: 100%">
                  <a-select-option value="devices">设备列表</a-select-option>
                  <a-select-option value="channels">通道列表</a-select-option>
                  <a-select-option value="preview">实时预览</a-select-option>
                  <a-select-option value="records">录像查询</a-select-option>
                  <a-select-option value="playback">录像回放</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24" v-if="contentType === 'devices'">
              <a-form-item label="设备名称/ID">
                <a-input v-model="deviceQuery.keyword" placeholder="请输入设备名称或ID" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24" v-if="contentType === 'devices'">
              <a-form-item label="在线状态">
                <a-select v-model="deviceQuery.status" placeholder="请选择状态" allow-clear style="width: 100%">
                  <a-select-option value="ON">在线</a-select-option>
                  <a-select-option value="OFF">离线</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24" v-if="contentType === 'records'">
              <a-form-item label="时间范围">
                <a-range-picker
                  v-model="recordQuery.timeRange"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="['开始时间', '结束时间']"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <span class="table-page-search-submitButtons" style="float: right">
                <a-button type="primary" @click="handleQuery" v-if="contentType === 'devices' || contentType === 'records'">
                  <a-icon type="search"/>查询
                </a-button>
                <a-button style="margin-left: 8px" @click="handleReset" v-if="contentType === 'devices'">
                  <a-icon type="redo"/>重置
                </a-button>
                <a-button type="primary" style="margin-left: 8px" @click="syncDevices" v-if="contentType === 'devices'">
                  <a-icon type="sync"/>同步设备
                </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 设备列表操作按钮 -->
      <div class="table-operations" v-if="contentType === 'devices'">
        <a-button type="primary" :disabled="multiple" @click="showImportModal">
          <a-icon type="plus"/>勾选使用
        </a-button>
      </div>

      <!-- 左侧组织树 + 右侧内容 -->
      <div class="content-wrapper">
        <!-- 左侧组织树 -->
        <div class="left-tree">
          <div class="tree-header">
            <span class="tree-title">组织结构</span>
          </div>
          <a-input-search 
            v-model="orgKeyword" 
            placeholder="搜索组织" 
            @search="loadOrganizations" 
            size="small"
            style="margin-bottom: 12px"
          />
          <div class="tree-container">
            <a-tree
              :tree-data="orgTree"
              :default-expand-all="false"
              :expandedKeys="expandedKeys"
              :selectedKeys="selectedOrgKeys"
              @select="onOrgSelect"
              @expand="onExpand"
            />
          </div>
        </div>

        <!-- 右侧内容区 -->
        <div class="right-content">
          <!-- 设备列表 -->
          <div v-if="contentType === 'devices'">
            <a-table
              :loading="loading"
              rowKey="deviceId"
              :columns="deviceColumns"
              :data-source="devices"
              :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
              :pagination="pagination"
              @change="handleTableChange"
              size="small"
            >
              <span slot="status" slot-scope="text">
                <a-badge :status="text === 'ON' ? 'success' : 'default'" :text="text === 'ON' ? '在线' : '离线'" />
              </span>
              <span slot="action" slot-scope="text, record">
                <a @click="viewDevice(record)">查看</a>
                <a-divider type="vertical" />
                <a @click="viewChannels(record)">通道</a>
              </span>
            </a-table>
          </div>

          <!-- 通道列表 -->
          <div v-if="contentType === 'channels'">
            <a-descriptions size="small" :column="4" style="margin-bottom: 16px;" v-if="currentDevice">
              <a-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</a-descriptions-item>
              <a-descriptions-item label="摄像机编码">{{ currentDevice.cameraIndexCode || '-' }}</a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-badge :status="currentDevice.deviceStatus === 'ON' ? 'success' : 'default'" 
                         :text="currentDevice.deviceStatus === 'ON' ? '在线' : '离线'" />
              </a-descriptions-item>
              <a-descriptions-item label="通道数">{{ (currentDevice.channelList || []).length }}</a-descriptions-item>
            </a-descriptions>

            <a-table
              :loading="channelLoading"
              rowKey="channelId"
              :columns="channelColumns"
              :data-source="channels"
              :pagination="false"
              size="small"
            >
              <span slot="status" slot-scope="text">
                <a-badge :status="text === 'ON' ? 'success' : 'default'" :text="text === 'ON' ? '在线' : '离线'" />
              </span>
              <span slot="action" slot-scope="text, record">
                <a @click="previewChannel(record)">预览</a>
                <a-divider type="vertical" />
                <a @click="viewRecords(record)">录像</a>
              </span>
            </a-table>
          </div>

          <!-- 实时预览 -->
          <div v-if="contentType === 'preview'">
            <a-row :gutter="16">
              <a-col :span="18">
                <div class="video-container">
                  <video-player 
                    ref="videoPlayer"
                    :url="streamUrl" 
                    :hasAudio="true"
                    @play="handleVideoPlay"
                    @stop="handleVideoStop"
                  />
                </div>
              </a-col>
              <a-col :span="6">
                <a-card title="云台控制" size="small" :bordered="false">
                  <ptz-control
                    :showPresets="true"
                    :presets="presets"
                    @command="handlePtzCommand"
                    @queryPresets="queryPresets"
                    @gotoPreset="gotoPreset"
                    @setPreset="setPreset"
                  />
                </a-card>
                
                <a-card title="通道信息" size="small" :bordered="false" style="margin-top: 16px;" v-if="currentChannel">
                  <a-descriptions size="small" :column="1">
                    <a-descriptions-item label="通道名称">{{ currentChannel.name }}</a-descriptions-item>
                    <a-descriptions-item label="通道ID">{{ currentChannel.channelId }}</a-descriptions-item>
                    <a-descriptions-item label="摄像机编码">{{ currentChannel.cameraIndexCode || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="状态">
                      <a-badge :status="currentChannel.status === 'ON' ? 'success' : 'default'" 
                               :text="currentChannel.status === 'ON' ? '在线' : '离线'" />
                    </a-descriptions-item>
                  </a-descriptions>
                </a-card>
              </a-col>
            </a-row>
          </div>

          <!-- 录像查询 -->
          <div v-if="contentType === 'records'">
            <a-table
              :loading="recordLoading"
              rowKey="id"
              :columns="recordColumns"
              :data-source="records"
              :pagination="recordPagination"
              @change="handleRecordTableChange"
              size="small"
            >
              <span slot="startTime" slot-scope="text">
                {{ text | formatTime }}
              </span>
              <span slot="endTime" slot-scope="text">
                {{ text | formatTime }}
              </span>
              <span slot="duration" slot-scope="text, record">
                {{ calcDuration(record.startTime, record.endTime) }}
              </span>
              <span slot="action" slot-scope="text, record">
                <a @click="playbackRecord(record)">回放</a>
                <a-divider type="vertical" />
                <a @click="downloadRecord(record)">下载</a>
              </span>
            </a-table>
          </div>

          <!-- 录像回放 -->
          <div v-if="contentType === 'playback'">
            <a-row :gutter="16">
              <a-col :span="18">
                <div class="video-container">
                  <video-player 
                    ref="playbackPlayer"
                    :url="playbackUrl" 
                    :hasAudio="true"
                    @play="handlePlaybackPlay"
                    @stop="handlePlaybackStop"
                  />
                </div>
                <div class="playback-controls" v-if="currentRecord">
                  <a-button-group>
                    <a-button @click="playbackSpeed(0.5)">0.5x</a-button>
                    <a-button @click="playbackSpeed(1)">1x</a-button>
                    <a-button @click="playbackSpeed(2)">2x</a-button>
                    <a-button @click="playbackSpeed(4)">4x</a-button>
                  </a-button-group>
                  <a-slider 
                    v-model="playbackProgress" 
                    :min="0" 
                    :max="100" 
                    style="flex: 1; margin: 0 16px;"
                    @change="seekPlayback"
                  />
                  <span>{{ playbackTime }}</span>
                </div>
              </a-col>
              <a-col :span="6">
                <a-card title="录像信息" size="small" :bordered="false" v-if="currentRecord">
                  <a-descriptions size="small" :column="1">
                    <a-descriptions-item label="开始时间">{{ currentRecord.startTime | formatTime }}</a-descriptions-item>
                    <a-descriptions-item label="结束时间">{{ currentRecord.endTime | formatTime }}</a-descriptions-item>
                    <a-descriptions-item label="时长">{{ calcDuration(currentRecord.startTime, currentRecord.endTime) }}</a-descriptions-item>
                    <a-descriptions-item label="文件大小">{{ currentRecord.fileSize || '-' }}</a-descriptions-item>
                  </a-descriptions>
                </a-card>
              </a-col>
            </a-row>
          </div>
        </div>
      </div>
    </a-card>

    <!-- 设备详情弹窗 -->
    <a-modal v-model="deviceModalVisible" title="设备详情" width="800px" :footer="null">
      <a-descriptions size="small" :column="2" v-if="selectedDevice">
        <a-descriptions-item label="设备ID">{{ selectedDevice.deviceId }}</a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ selectedDevice.deviceName }}</a-descriptions-item>
        <a-descriptions-item label="摄像机编码">{{ selectedDevice.cameraIndexCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge :status="selectedDevice.deviceStatus === 'ON' ? 'success' : 'default'" 
                   :text="selectedDevice.deviceStatus === 'ON' ? '在线' : '离线'" />
        </a-descriptions-item>
        <a-descriptions-item label="设备型号">{{ selectedDevice.deviceModel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="所属组织">{{ selectedDevice.orgName || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 勾选使用弹窗 -->
    <a-modal v-model="importModalVisible" title="勾选使用设备" @ok="handleImport" @cancel="handleImportCancel" :confirmLoading="importLoading">
      <a-form :label-col="{span:6}" :wrapper-col="{span:16}">
        <a-form-item label="选择产品">
          <a-select v-model="importForm.productKey" placeholder="自动创建平台产品" allow-clear>
            <a-select-option value="">自动创建平台产品</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="已选设备">
          <a-tag v-for="dev in selectedRows" :key="dev.deviceId" style="margin-bottom:8px">{{ dev.deviceName }}</a-tag>
          <div v-if="selectedRows.length===0" style="color:#999">未选择任何设备</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import { 
  getPlatformOrgs,
  getPlatformDevices, 
  syncPlatform, 
  importDevices
} from '@/api/video/platform'
import { 
  getPlatformChannels,
  getPreviewUrl,
  getPlaybackUrl,
  getRecords,
  controlPTZ
} from '@/api/video/channel'
import VideoPlayer from '@/components/VideoPlayer'
import PtzControl from '@/components/PtzControl'
import moment from 'moment'

export default {
  name: 'HikIcsPlatformDetail',
  components: { VideoPlayer, PtzControl },
  data() {
    return {
      instanceKey: '',
      contentType: 'devices',
      loading: false,
      channelLoading: false,
      recordLoading: false,
      
      // 组织树
      orgTree: [],
      orgKeyword: undefined,
      expandedKeys: [],
      selectedOrgKeys: [],
      
      // 设备列表
      devices: [],
      deviceQuery: {
        keyword: undefined,
        status: undefined,
        orgId: undefined,
        page: 1,
        pageSize: 10
      },
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条`
      },
      selectedRowKeys: [],
      selectedRows: [],
      multiple: true,
      deviceColumns: [
        { title: '设备ID', dataIndex: 'deviceId', width: 180, ellipsis: true },
        { title: '设备名称', dataIndex: 'deviceName', width: 150, ellipsis: true },
        { title: '摄像机编码', dataIndex: 'cameraIndexCode', width: 150, ellipsis: true },
        { title: '状态', dataIndex: 'deviceStatus', width: 80, scopedSlots: { customRender: 'status' } },
        { title: '所属组织', dataIndex: 'orgName', width: 120, ellipsis: true },
        { title: '操作', key: 'action', width: 120, fixed: 'right', scopedSlots: { customRender: 'action' } }
      ],
      
      // 当前选中
      currentDevice: null,
      currentChannel: null,
      currentRecord: null,
      selectedDevice: null,
      deviceModalVisible: false,
      
      // 通道列表
      channels: [],
      channelColumns: [
        { title: '通道ID', dataIndex: 'channelId', width: 180 },
        { title: '通道名称', dataIndex: 'name', width: 150, ellipsis: true },
        { title: '摄像机编码', dataIndex: 'cameraIndexCode', width: 150, ellipsis: true },
        { title: '状态', dataIndex: 'status', width: 80, scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', width: 120, scopedSlots: { customRender: 'action' } }
      ],
      
      // 视频预览
      streamUrl: '',
      presets: [],
      
      // 录像查询
      recordQuery: {
        timeRange: []
      },
      records: [],
      recordPagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      recordColumns: [
        { title: '开始时间', dataIndex: 'startTime', scopedSlots: { customRender: 'startTime' } },
        { title: '结束时间', dataIndex: 'endTime', scopedSlots: { customRender: 'endTime' } },
        { title: '时长', key: 'duration', scopedSlots: { customRender: 'duration' } },
        { title: '文件大小', dataIndex: 'fileSize' },
        { title: '操作', key: 'action', width: 120, scopedSlots: { customRender: 'action' } }
      ],
      
      // 录像回放
      playbackUrl: '',
      playbackProgress: 0,
      playbackTime: '00:00:00',
      
      // 导入弹窗
      importModalVisible: false,
      importLoading: false,
      importForm: { productKey: '' }
    }
  },
  filters: {
    formatTime(timestamp) {
      return timestamp ? moment(timestamp).format('YYYY-MM-DD HH:mm:ss') : '-'
    }
  },
  created() {
    this.instanceKey = this.$route.params.instanceKey
    this.loadOrganizations()
    this.queryDevices()
  },
  methods: {
    async loadOrganizations() {
      try {
        const params = {}
        if (this.orgKeyword) params.keyword = this.orgKeyword
        const res = await getPlatformOrgs(this.instanceKey, params)
        const data = res.data || []
        this.orgTree = this.buildTree(data)
        if (this.orgTree.length > 0) {
          this.expandedKeys = [this.orgTree[0].key]
        }
      } catch (e) {
        this.$message.error('加载组织树失败：' + (e.response?.data?.msg || e.message))
      }
    },
    buildTree(list) {
      const map = {}
      list.forEach(n => { 
        map[n.orgId] = { 
          key: n.orgId, 
          title: n.orgName, 
          parentId: n.parentOrgId, 
          children: [] 
        } 
      })
      const roots = []
      list.forEach(n => {
        const node = map[n.orgId]
        if (n.parentOrgId && map[n.parentOrgId]) {
          map[n.parentOrgId].children.push(node)
        } else {
          roots.push(node)
        }
      })
      return roots
    },
    onOrgSelect(keys) {
      this.selectedOrgKeys = keys
      this.deviceQuery.orgId = keys && keys.length ? keys[0] : undefined
      this.queryDevices()
    },
    onExpand(keys) {
      this.expandedKeys = keys
    },
    async queryDevices() {
      this.loading = true
      try {
        const params = {
          ...this.deviceQuery,
          page: this.pagination.current,
          pageSize: this.pagination.pageSize
        }
        const res = await getPlatformDevices(this.instanceKey, params)
        this.devices = res.data || []
        this.pagination.total = this.devices.length
      } catch (e) {
        this.$message.error('查询设备失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      if (this.contentType === 'devices') {
        this.pagination.current = 1
        this.queryDevices()
      } else if (this.contentType === 'records') {
        this.queryRecords()
      }
    },
    handleReset() {
      this.deviceQuery = {
        keyword: undefined,
        status: undefined,
        orgId: this.deviceQuery.orgId,
        page: 1,
        pageSize: 10
      }
      this.pagination.current = 1
      this.queryDevices()
    },
    async syncDevices() {
      this.loading = true
      try {
        await syncPlatform(this.instanceKey)
        this.$message.success('同步成功')
        this.queryDevices()
      } catch (e) {
        this.$message.error('同步失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.loading = false
      }
    },
    handleTableChange(pagination) {
      this.pagination.current = pagination.current
      this.pagination.pageSize = pagination.pageSize
      this.queryDevices()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.multiple = !selectedRowKeys.length
    },
    viewDevice(device) {
      this.selectedDevice = device
      this.deviceModalVisible = true
    },
    viewChannels(device) {
      this.currentDevice = device
      this.channels = device.channelList || []
      this.contentType = 'channels'
    },
    async previewChannel(channel) {
      this.currentChannel = channel
      this.contentType = 'preview'
      
      try {
        const res = await getPreviewUrl(
          this.instanceKey,
          this.currentDevice.deviceId,
          channel.channelId,
          'main'
        )
        const urls = res.data || {}
        this.streamUrl = urls.flv || urls.hls || urls.rtmp || ''
        
        if (!this.streamUrl) {
          this.$message.warning('未获取到流地址')
        }
      } catch (e) {
        this.$message.error('获取流地址失败：' + (e.response?.data?.msg || e.message))
      }
    },
    handlePtzCommand({ command, speed }) {
      if (!this.currentChannel) return
      
      controlPTZ(
        this.instanceKey,
        this.currentDevice.deviceId,
        this.currentChannel.channelId,
        command,
        speed
      ).catch(e => {
        this.$message.error('PTZ控制失败：' + (e.response?.data?.msg || e.message))
      })
    },
    queryPresets() {
      this.$message.info('查询预置位功能待实现')
    },
    gotoPreset(preset) {
      this.$message.info('跳转预置位：' + preset)
    },
    setPreset(preset) {
      this.$message.info('设置预置位：' + preset)
    },
    handleVideoPlay() {
      console.log('视频开始播放')
    },
    handleVideoStop() {
      this.streamUrl = ''
    },
    viewRecords(channel) {
      this.currentChannel = channel
      this.contentType = 'records'
      
      const now = moment()
      this.recordQuery.timeRange = [
        moment().startOf('day'),
        now
      ]
      this.queryRecords()
    },
    async queryRecords() {
      if (!this.recordQuery.timeRange || this.recordQuery.timeRange.length !== 2) {
        this.$message.warning('请选择时间范围')
        return
      }
      
      this.recordLoading = true
      try {
        const res = await getRecords(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          this.recordQuery.timeRange[0].valueOf(),
          this.recordQuery.timeRange[1].valueOf()
        )
        this.records = res.data || []
        this.recordPagination.total = this.records.length
      } catch (e) {
        this.$message.error('查询录像失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.recordLoading = false
      }
    },
    handleRecordTableChange(pagination) {
      this.recordPagination.current = pagination.current
      this.recordPagination.pageSize = pagination.pageSize
    },
    calcDuration(startTime, endTime) {
      if (!startTime || !endTime) return '-'
      const duration = moment.duration(endTime - startTime)
      const hours = Math.floor(duration.asHours())
      const minutes = duration.minutes()
      const seconds = duration.seconds()
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    },
    async playbackRecord(record) {
      this.currentRecord = record
      this.contentType = 'playback'
      
      try {
        const res = await getPlaybackUrl(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            startTime: record.startTime,
            endTime: record.endTime
          }
        )
        const urls = res.data || {}
        this.playbackUrl = urls.flv || urls.hls || urls.rtmp || ''
        
        if (!this.playbackUrl) {
          this.$message.warning('未获取到回放地址')
        }
      } catch (e) {
        this.$message.error('获取回放地址失败：' + (e.response?.data?.msg || e.message))
      }
    },
    downloadRecord(record) {
      this.$message.info('下载录像功能待实现')
    },
    handlePlaybackPlay() {
      console.log('回放开始播放')
    },
    handlePlaybackStop() {
      this.playbackUrl = ''
    },
    playbackSpeed(speed) {
      this.$message.info(`设置倍速：${speed}x`)
    },
    seekPlayback(progress) {
      this.$message.info(`跳转进度：${progress}%`)
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
        const res = await importDevices(this.instanceKey, payload)
        const { success, failed, exists } = res.data || {}
        this.$message.success(`导入完成：成功${success}，失败${failed}，已存在${exists}`)
        this.importModalVisible = false
        this.onSelectChange([], [])
      } catch (e) {
        this.$message.error('导入失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.importLoading = false
      }
    }
  }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}

.table-operations {
  margin-bottom: 16px;
}

.content-wrapper {
  display: flex;
  gap: 16px;
}

.left-tree {
  width: 220px;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  
  .tree-header {
    margin-bottom: 8px;
    padding-bottom: 6px;
    border-bottom: 1px solid #e8e8e8;
    
    .tree-title {
      font-size: 13px;
      font-weight: 600;
      color: #262626;
    }
  }
  
  .tree-container {
    max-height: 600px;
    overflow-y: auto;
  }
}

.right-content {
  flex: 1;
  min-width: 0;
}

.video-container {
  width: 100%;
  height: 500px;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
}

.playback-controls {
  display: flex;
  align-items: center;
  margin-top: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}
</style>
