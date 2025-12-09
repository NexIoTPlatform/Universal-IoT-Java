<template>
  <page-header-wrapper>
    <a-card :bordered="false" class="detail-container">
      <div class="page-header">
        <div class="header-left">
          <a-button type="text" icon="left" @click="$router.back()" class="back-btn" />
          <div class="page-title">
            <h1>{{ deviceInfo.deviceName || 'WVP设备详情' }}（{{ deviceInfo.deviceId }}）</h1>
          </div>
        </div>
      </div>

      <a-tabs v-model="activeTab" type="card">
        <a-tab-pane key="info" tab="设备信息">
          <a-descriptions size="small" :column="2">
            <a-descriptions-item label="设备ID">{{ deviceInfo.deviceId }}</a-descriptions-item>
            <a-descriptions-item label="设备名称">{{ deviceInfo.deviceName }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-badge :status="deviceInfo.deviceStatus === 'ON' ? 'success' : 'default'"
                       :text="deviceInfo.deviceStatus === 'ON' ? '在线' : '离线'"/>
            </a-descriptions-item>
            <a-descriptions-item label="设备型号">{{ deviceInfo.deviceModel || '-' }}</a-descriptions-item>
            <a-descriptions-item label="厂商">{{ deviceInfo.manufacturer || '-' }}</a-descriptions-item>
            <a-descriptions-item label="通道数">{{ (deviceInfo.channelList || []).length }}</a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="channels" tab="通道管理">
          <a-table :loading="channelLoading"
                   rowKey="channelId"
                   :columns="channelColumns"
                   :data-source="channels"
                   :pagination="false"
                   size="small">
            <span slot="status" slot-scope="text">
              <a-badge :status="text === 'ON' ? 'success' : 'default'" :text="text === 'ON' ? '在线' : '离线'"/>
            </span>
            <span slot="action" slot-scope="text, record">
              <a @click="previewChannel(record)">预览</a>
              <a-divider type="vertical"/>
              <a @click="viewRecords(record)">录像</a>
            </span>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="preview" tab="实时预览" :disabled="!currentChannel">
          <div class="video-container">
            <video-player ref="videoPlayer" :url="streamUrl" :has-audio="true"/>
          </div>
        </a-tab-pane>

        <a-tab-pane key="records" tab="录像查询" :disabled="!currentChannel">
          <a-form layout="inline" style="margin-bottom: 16px;">
            <a-form-item label="时间范围">
              <a-range-picker v-model="recordQuery.timeRange" show-time format="YYYY-MM-DD HH:mm:ss"
                              :placeholder="['开始时间', '结束时间']"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="queryRecords"><a-icon type="search"/>查询录像</a-button>
            </a-form-item>
          </a-form>
          <a-table :loading="recordLoading"
                   rowKey="id"
                   :columns="recordColumns"
                   :data-source="records"
                   :pagination="recordPagination"
                   size="small">
            <span slot="startTime" slot-scope="text">{{ text | formatTime }}</span>
            <span slot="endTime" slot-scope="text">{{ text | formatTime }}</span>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { axios } from '@/utils/request'
import moment from 'moment'
import VideoPlayer from '@/components/VideoPlayer'

export default {
  name: 'WvpDeviceDetailsPro',
  components: { VideoPlayer },
  data () {
    return {
      instanceKey: '',
      deviceId: '',
      activeTab: 'info',
      deviceInfo: {},

      // 通道
      channelLoading: false,
      channels: [],
      channelColumns: [
        { title: '通道ID', dataIndex: 'channelId', width: 180 },
        { title: '通道名称', dataIndex: 'name', width: 150, ellipsis: true },
        { title: '状态', dataIndex: 'status', width: 80, scopedSlots: { customRender: 'status' } },
        { title: '编码格式', dataIndex: 'streamType', width: 100 },
        { title: '操作', key: 'action', width: 120, scopedSlots: { customRender: 'action' } }
      ],
      currentChannel: null,
      streamUrl: '',

      // 录像
      recordQuery: { timeRange: [] },
      records: [],
      recordLoading: false,
      recordPagination: { current: 1, pageSize: 10, total: 0 },
      recordColumns: [
        { title: '开始时间', dataIndex: 'startTime', scopedSlots: { customRender: 'startTime' } },
        { title: '结束时间', dataIndex: 'endTime', scopedSlots: { customRender: 'endTime' } },
        { title: '文件大小', dataIndex: 'fileSize' }
      ]
    }
  },
  filters: {
    formatTime (timestamp) {
      return timestamp ? moment(timestamp).format('YYYY-MM-DD HH:mm:ss') : '-'
    }
  },
  created () {
    this.instanceKey = this.$route.params.instanceKey
    this.deviceId = this.$route.params.deviceId
    this.loadDevice()
  },
  methods: {
    async loadDevice () {
      try {
        const res = await axios.get(`/api/video/platforms/${this.instanceKey}/devices/${this.deviceId}`)
        this.deviceInfo = res.data || {}
        this.channels = this.deviceInfo.channelList || []
      } catch (e) {
        this.$message.error('获取设备信息失败：' + (e.response?.data?.msg || e.message))
      }
    },
    async previewChannel (channel) {
      this.currentChannel = channel
      this.activeTab = 'preview'
      try {
        const res = await axios.get(`/api/video/platforms/${this.instanceKey}/devices/${this.deviceInfo.deviceId}/channels/${channel.channelId}/preview`)
        const urls = res.data || {}
        this.streamUrl = urls.flv || urls.hls || urls.rtmp || ''
      } catch (e) {
        this.$message.error('获取流地址失败：' + (e.response?.data?.msg || e.message))
      }
    },
    viewRecords (channel) {
      this.currentChannel = channel
      this.activeTab = 'records'
      const now = moment()
      this.recordQuery.timeRange = [moment().startOf('day'), now]
      this.queryRecords()
    },
    async queryRecords () {
      if (!this.recordQuery.timeRange || this.recordQuery.timeRange.length !== 2) return
      this.recordLoading = true
      try {
        const params = {
          startTime: this.recordQuery.timeRange[0].valueOf(),
          endTime: this.recordQuery.timeRange[1].valueOf()
        }
        const res = await axios.get(`/api/video/platforms/${this.instanceKey}/devices/${this.deviceInfo.deviceId}/channels/${this.currentChannel.channelId}/records`, { params })
        this.records = res.data || []
        this.recordPagination.total = this.records.length
      } catch (e) {
        this.$message.error('查询录像失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.recordLoading = false
      }
    }
  }
}
</script>

<style lang="less" scoped>
.detail-container {
  min-height: calc(100vh - 150px);
}
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.header-left {
  display: flex;
  align-items: center;
}
.page-title h1 {
  margin: 0 0 0 8px;
  font-size: 16px;
}
.video-container {
  width: 100%;
  height: 500px;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
}
</style>