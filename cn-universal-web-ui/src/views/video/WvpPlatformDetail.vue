<template>
  <div class="app-container">
    <a-card :bordered="false" class="main-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <a-button
            type="text"
            icon="left"
            @click="back()"
            class="back-btn"
            v-hasPermi="['video:platform:view']"
          />
          <div class="page-title">
            <h1>
              <template v-if="channelId && currentChannelDetail">
                {{ currentChannelDetail.name || '通道详情' }}（{{ channelId }}）
              </template>
              <template v-else-if="currentDevice">
                {{ currentDevice.deviceName }}（{{ currentDevice.deviceId }}）
              </template>
              <template v-else>
                加载中...
              </template>
            </h1>
          </div>
        </div>
      </div>
      <!-- 自定义标签页导航 -->
      <a-spin :spinning="loading" tip="Loading...">
        <div class="custom-tabs-container">
          <div class="custom-tabs-header">
            <div class="custom-tabs-nav">
              <div
                class="custom-tab-item"
                :class="{ active: activeTab === 'info' }"
                @click="switchTab('info')"
                v-hasPermi="['video:platform:view']"
              >
                基本信息
              </div>
              <div
                class="custom-tab-item"
                :class="{ active: activeTab === 'cloud-records' }"
                @click="switchTab('cloud-records')"
                v-hasPermi="['video:cloud-record:query']"
              >
                云端录像
              </div>
              <div
                class="custom-tab-item"
                :class="{ active: activeTab === 'records' }"
                @click="switchTab('records')"
                v-hasPermi="['video:record:query']"
              >
                设备录像
              </div>
            </div>
            <!-- <div class="tabs-actions">
              <a-button
                type="primary"
                icon="sync"
                size="small"
                @click="syncDevices"
                :loading="syncLoading"
                v-hasPermi="['video:platform:sync']"
              >
                同步设备
              </a-button>
            </div> -->
          </div>

          <!-- 标签页内容 -->
          <div class="custom-tab-content">
            <!-- 设备信息 -->
            <div v-show="activeTab === 'info'" class="tab-pane">
              <!-- 设备基础信息 -->
              <div class="info-card">
                <div class="card-header">
                  <h3><a-icon type="database" /> 设备基础信息</h3>
                </div>
                <div class="info-grid" v-if="currentDevice">
                  <div class="info-row">
                    <span class="label">设备ID</span>
                    <span class="value">{{ currentDevice.deviceId }}
                      <a-icon 
                        type="copy" 
                        class="copy-icon" 
                        @click="copyToClipboard(currentDevice.deviceId)"
                        v-hasPermi="['video:platform:view']" 
                      />
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="label">设备名称</span>
                    <span class="value">{{ currentDevice.deviceName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">在线状态</span>
                    <span class="value">
                      <a-badge :status="currentDevice.deviceStatus === 'online' ? 'success' : 'default'" 
                               :text="currentDevice.deviceStatus === 'online' ? '在线' : '离线'" />
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="label">设备型号</span>
                    <span class="value">{{ currentDevice.deviceModel || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">设备IP</span>
                    <span class="value">{{ currentDevice.deviceIp || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">设备端口</span>
                    <span class="value">{{ currentDevice.devicePort || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">制造商</span>
                    <span class="value">{{ currentDevice.manufacturer || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">所属组织</span>
                    <span class="value">{{ currentDevice.orgName || '-' }}</span>
                  </div>
                  <div class="info-row info-row-full">
                    <span class="label">GPS坐标</span>
                    <span class="value">
                      <template v-if="currentDevice.gpsX || currentDevice.gpsY">
                        经度: {{ currentDevice.gpsX || '-' }}, 纬度: {{ currentDevice.gpsY || '-' }}
                        <template v-if="currentDevice.gpsZ">, 高度: {{ currentDevice.gpsZ }}</template>
                      </template>
                      <template v-else>-</template>
                    </span>
                  </div>
                  <div class="info-row info-row-full" v-if="currentDevice.remark">
                    <span class="label">备注</span>
                    <span class="value">{{ currentDevice.remark }}</span>
                  </div>
                </div>
                <a-empty v-else description="暂无设备信息" />
              </div>

              <!-- 设备扩展信息 -->
              <div class="info-card" v-if="deviceExt">
                <div class="card-header">
                  <h3><a-icon type="setting" /> 设备扩展信息</h3>
                </div>
                <div class="info-grid">
                  <!-- WVP特有字段 -->
                  <template v-if="deviceExt.charset || deviceExt.transport">
                    <div class="info-row">
                      <span class="label">字符集</span>
                      <span class="value">{{ deviceExt.charset || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">传输协议</span>
                      <span class="value">{{ deviceExt.transport || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">流模式</span>
                      <span class="value">{{ deviceExt.streamMode || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">注册时间</span>
                      <span class="value">{{ deviceExt.registerTime || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">心跳时间</span>
                      <span class="value">{{ deviceExt.keepaliveTime || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">流媒体服务器</span>
                      <span class="value">{{ deviceExt.mediaServerId || '-' }}</span>
                    </div>
                  </template>
                  
                  <!-- 海康特有字段 -->
                  <template v-if="deviceExt.encodeDevIndexCode">
                    <div class="info-row">
                      <span class="label">编码设备索引码</span>
                      <span class="value">{{ deviceExt.encodeDevIndexCode }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">设备能力集</span>
                      <span class="value">{{ deviceExt.deviceCapabilitySet || '-' }}</span>
                    </div>
                  </template>
                  
                  <!-- 大华特有字段 -->
                  <template v-if="deviceExt.deviceSn">
                    <div class="info-row">
                      <span class="label">设备SN</span>
                      <span class="value">{{ deviceExt.deviceSn }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">设备类别</span>
                      <span class="value">{{ deviceExt.deviceCategory || '-' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">休眠状态</span>
                      <span class="value">{{ deviceExt.sleepStat === 1 ? '休眠' : '非休眠' }}</span>
                    </div>
                  </template>
                </div>
              </div>

              <!-- 通道详细信息 -->
              <div class="info-card" v-if="channelId && currentChannelDetail">
                <div class="card-header">
                  <h3><a-icon type="video-camera" /> 通道详细信息</h3>
                </div>
                <div class="info-grid">
                  <div class="info-row">
                    <span class="label">通道ID</span>
                    <span class="value">{{ currentChannelDetail.channelId }}
                      <a-icon 
                        type="copy" 
                        class="copy-icon" 
                        @click="copyToClipboard(currentChannelDetail.channelId)"
                        v-hasPermi="['video:platform:view']" 
                      />
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="label">通道名称</span>
                    <span class="value">{{ currentChannelDetail.name }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">通道状态</span>
                    <span class="value">
                      <a-badge :status="currentChannelDetail.status === 'ON' ? 'success' : 'default'" 
                               :text="currentChannelDetail.status === 'ON' ? '在线' : '离线'" />
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="label">通道类型</span>
                    <span class="value">{{ currentChannelDetail.channelType || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">PTZ类型</span>
                    <span class="value">{{ formatPtzType(currentChannelDetail.ptzType) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">编码格式</span>
                    <span class="value">{{ currentChannelDetail.streamType || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">制造商</span>
                    <span class="value">{{ currentChannelDetail.manufacturer || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">型号</span>
                    <span class="value">{{ currentChannelDetail.model || '-' }}</span>
                  </div>
                  <div class="info-row" v-if="currentChannelDetail.ipAddress">
                    <span class="label">IP地址</span>
                    <span class="value">{{ currentChannelDetail.ipAddress }}:{{ currentChannelDetail.port || '-' }}</span>
                  </div>
                  <div class="info-row" v-if="currentChannelDetail.longitude || currentChannelDetail.latitude">
                    <span class="label">坐标</span>
                    <span class="value">经度: {{ currentChannelDetail.longitude }}, 纬度: {{ currentChannelDetail.latitude }}</span>
                  </div>
                  <div class="info-row info-row-full" v-if="currentChannelDetail.address">
                    <span class="label">安装地址</span>
                    <span class="value">{{ currentChannelDetail.address }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 设备录像 -->
            <div v-show="activeTab === 'records'" class="tab-pane">
              <div class="simple-header">
                <h3>设备录像</h3>
                <p v-if="currentChannel">{{ currentChannel.name }} ({{ currentChannel.channelId }})</p>
              </div>
          <a-form layout="inline" style="margin-bottom: 16px;">
            <a-form-item label="时间范围">
              <a-range-picker
                v-model="recordQuery.timeRange"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                :placeholder="['开始时间', '结束时间']"
                style="width: 360px"
              />
            </a-form-item>
            <a-form-item label="快捷选择">
              <a-button-group>
                <a-button size="small" @click="selectTimeRange('today')" v-hasPermi="['video:record:query']">今天</a-button>
                <a-button size="small" @click="selectTimeRange('yesterday')" v-hasPermi="['video:record:query']">昨天</a-button>
                <a-button size="small" @click="selectTimeRange('last6hours')" v-hasPermi="['video:record:query']">近6小时</a-button>
                <a-button size="small" @click="selectTimeRange('last24hours')" v-hasPermi="['video:record:query']">近24小时</a-button>
                <a-button size="small" @click="selectTimeRange('last3days')" v-hasPermi="['video:record:query']">近3天</a-button>
              </a-button-group>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="queryRecords" v-hasPermi="['video:record:query']">
                <a-icon type="search"/>查询录像
              </a-button>
            </a-form-item>
          </a-form>

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
              <a @click="playbackRecord(record)" v-hasPermi="['video:record:playback']">回放</a>
              <a-divider type="vertical" v-hasPermi="['video:record:playback', 'video:record:download']" />
              <!-- 暂时禁用下载功能，保留代码逻辑 -->
              <a 
                class="download-disabled" 
                @click.prevent="handleDownloadDisabled"
                v-hasPermi="['video:record:download']"
                title="禁止下载"
              >
                下载
              </a>
              <!-- 原始下载代码（已禁用，保留逻辑） -->
              <!-- <a @click="downloadRecord(record)" v-hasPermi="['video:record:download']">下载</a> -->
            </span>
          </a-table>
            </div>

            <!-- 云端录像 -->
            <div v-show="activeTab === 'cloud-records'" class="tab-pane">
              <div class="simple-header">
                <h3>云端录像</h3>
                <p v-if="currentChannel">{{ currentChannel.name }} ({{ currentChannel.channelId }})</p>
              </div>
          <a-row :gutter="16">
            <a-col :span="5">
              <a-card title="日期选择" size="small" :bordered="false" :bodyStyle="{ padding: '8px' }" class="date-select-card">
                <div slot="extra">
                  <a-button size="small" icon="reload" @click="refreshCloudRecordDates" v-hasPermi="['video:cloud-record:query']">刷新</a-button>
                </div>
                
                <!-- 日期列表 -->
                <div v-if="cloudRecordDates.length > 0" class="date-list">
                  <div 
                    v-for="date in sortedCloudRecordDates" 
                    :key="date"
                    :class="['date-item', { 'active': isDateSelected(date) }]"
                    @click="selectCloudDate(date)"
                  >
                    <div class="date-label">
                      <a-icon type="calendar" />
                      <span>{{ formatDateLabel(date) }}</span>
                    </div>
                    <a-icon type="right" class="date-arrow" />
                  </div>
                </div>
                <a-empty v-else-if="cloudRecordDate" description="暂无录像日期" />
                <div v-else style="padding: 20px; text-align: center; color: #999; font-size: 12px;">
                  <a-icon type="info-circle" style="font-size: 20px; margin-bottom: 8px;" />
                  <div>请选择通道查看云端录像</div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="19">
              <a-card title="录像列表" size="small" :bordered="false" class="record-list-card">
                <div slot="extra">
                  <a-button size="small" icon="reload" @click="queryCloudRecords" style="margin-right: 8px;" v-hasPermi="['video:cloud-record:query']">刷新</a-button>
                </div>
                <a-table
                  :loading="cloudRecordLoading"
                  rowKey="id"
                  :columns="cloudRecordColumns"
                  :data-source="cloudRecords"
                  :pagination="cloudRecordPagination"
                  @change="handleCloudRecordTableChange"
                  size="small"
                  :scroll="{ x: 1000 }"
                >
                  <span slot="startTime" slot-scope="text">
                    {{ formatCloudRecordTime(text) }}
                  </span>
                  <span slot="endTime" slot-scope="text">
                    {{ formatCloudRecordTime(text) }}
                  </span>
                  <span slot="timeLen" slot-scope="text">
                    {{ formatCloudRecordDuration(text) }}
                  </span>
                  <span slot="fileSize" slot-scope="text">
                    {{ formatFileSize(text) }}
                  </span>
                  <span slot="action" slot-scope="text, record">
                    <a @click="playCloudRecord(record)" v-hasPermi="['video:cloud-record:play']">播放</a>
                    <a-divider type="vertical" v-hasPermi="['video:cloud-record:play', 'video:cloud-record:download']" />
                    <a @click="downloadCloudRecord(record)" v-hasPermi="['video:cloud-record:download']">下载</a>
                  </span>
                </a-table>
              </a-card>
            </a-col>
          </a-row>
            </div>
          </div>
        </div>
      </a-spin>
    </a-card>
    
    <!-- 云端录像播放弹窗 -->
    <a-modal
      v-model="playModalVisible"
      v-modal-drag
      :title="currentRecord ? `${currentRecord.fileName} - 录像播放` : '录像播放'"
      width="960px"
      :footer="null"
      :destroyOnClose="true"
      @cancel="closePlayModal"
      wrapClassName="cloud-record-play-modal"
    >
      <div style="width: 100%; height: 580px; background: #000;">
        <cloud-record-player
          v-if="cloudPlayerReady && !playLoading"
          ref="cloudPlayer"
          :hasAudio="true"
          height="580px"
          @play="onCloudPlayerPlay"
          @pause="onCloudPlayerPause"
          @stop="onCloudPlayerStop"
          @seek="onCloudPlayerSeek"
          @speedChange="onCloudPlayerSpeedChange"
        />
        <!-- 添加加载中状态 -->
        <div 
          v-else-if="playLoading && currentRecord"
          style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; background: #000;"
        >
          <a-spin size="large" />
          <span style="margin-left: 12px; font-size: 16px;">正在加载视频流...</span>
        </div>
        <a-spin 
           v-else
          :spinning="true" 
          tip="加载视频流..." 
          style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;"
        />
      </div>
    </a-modal>

    <!-- 设备录像播放弹窗 -->
    <device-record-playback-modal
      v-model="devicePlayModalVisible"
      :instanceKey="instanceKey"
      :deviceId="currentDevice ? currentDevice.deviceId : ''"
      :channelId="currentChannel ? currentChannel.channelId : ''"
      :record="currentRecord"
      @close="closeDevicePlayModal"
      @download="handleRecordDownloadFromModal"
    />

    <!-- 设备录像下载弹窗 -->
    <device-record-download
      v-if="downloadModalVisible"
      v-model="downloadModalVisible"
      :device="currentDevice"
      :channel="currentChannel"
      :instanceKey="instanceKey"
      :record="currentDownloadRecord"
      @close="closeDownloadModal"
    />
  </div>
</template>

<script>
import { 
  getPlatformDevices, 
  syncPlatform, 
  importDevices
} from '@/api/video/platform'
import {
  getPlatformChannels,
  getPreviewUrl,
  getPlaybackUrl,
  getRecords,
  controlPTZ,
  startGBRecordDownload,
  stopGBRecordDownload,
  getGBRecordDownloadProgress,
  queryCloudRecordDates,
  queryCloudRecords,
  loadCloudRecord,
  seekCloudRecord,
  setCloudRecordSpeed,
  getCloudRecordPlayPath
} from '@/api/video/channel'
import CloudRecordPlayer from '@/components/CloudRecordPlayer'
import DeviceRecordPlaybackModal from '@/components/DeviceRecordPlaybackModal'
import DeviceRecordDownload from '@/components/DeviceRecordDownload'
import moment from 'moment'
import { listAllProduct } from '@/api/system/dev/product'
import modalDrag from '@/directive/modal-drag'

export default {
  name: 'WvpPlatformDetail',
  components: { CloudRecordPlayer, DeviceRecordPlaybackModal, DeviceRecordDownload },
  directives: { modalDrag },
  data() {
    return {
      instanceKey: '',
      deviceId: '',
      channelId: '',  // 新增：通道ID参数
      activeTab: 'info',
      loading: false,
      channelLoading: false,
      recordLoading: false,
      syncLoading: false,
      
      // 当前设备和通道
      currentDevice: null,
      deviceExt: null,  // 新增：设备扩展信息
      currentChannel: null,
      currentChannelDetail: null,  // 新增：通道详细信息
      currentRecord: null,
      
      // 通道列表
      channels: [],
      channelQuery: {
        keyword: undefined,
        status: undefined
      },
      channelPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条`
      },
      channelColumns: [
        { title: '通道ID', dataIndex: 'channelId', width: 180, ellipsis: true },
        { title: '通道名称', dataIndex: 'name', width: 150, ellipsis: true },
        { title: '状态', dataIndex: 'status', width: 80, scopedSlots: { customRender: 'status' } },
        { title: '编码格式', dataIndex: 'streamType', width: 100 },
        { title: '操作', key: 'action', width: 180, scopedSlots: { customRender: 'action' } }
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
      playbackMediaServerId: '',  // 流媒体服务器ID
      playbackApp: '',  // 应用名
      playbackStream: '',  // 流ID
      
      // 下载相关
      downloadModalVisible: false,
      currentDownloadRecord: null,
      
      // 云端录像
      cloudRecordDate: null,  // 初始化为null，在viewCloudRecords时设置
      cloudRecordDates: [], // 有录像的日期列表
      selectedCloudDate: null, // 当前选中的日期
      cloudRecords: [],
      cloudRecordLoading: false,
      cloudRecordPagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      cloudRecordColumns: [
        { title: '文件名', dataIndex: 'fileName', width: 180, ellipsis: true },
        { title: '开始时间', dataIndex: 'startTime', width: 140, scopedSlots: { customRender: 'startTime' } },
        { title: '结束时间', dataIndex: 'endTime', width: 140, scopedSlots: { customRender: 'endTime' } },
        { title: '时长', dataIndex: 'timeLen', width: 80, scopedSlots: { customRender: 'timeLen' } },
        { title: '文件大小', dataIndex: 'fileSize', width: 90, scopedSlots: { customRender: 'fileSize' } },
        { title: '操作', key: 'action', width: 120, fixed: 'right', scopedSlots: { customRender: 'action' } }
      ],
      
      // 播放弹窗
      playModalVisible: false,
      playLoading: false,
      cloudPlayerReady: false, // 云端播放器是否准备好
      cloudStreamInfo: null, // 云端播放器流信息
      cloudRecordInfo: null, // 云端播放器录像信息
      // 设备录像播放弹窗
      devicePlayModalVisible: false,
      devicePlayerReady: false,
      deviceStreamInfo: null,
      deviceRecordInfo: null,
      
      // 优化加载体验
      showLoadingDelay: null // 用于延迟显示加载状态的定时器
    }
  },
  filters: {
    formatTime(timestamp) {
      if (!timestamp) return '-'
      let time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      if (!time || Number.isNaN(time)) return '-'
      if (time.toString().length === 10) {
        time = time * 1000
      }
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  computed: {
    sortedCloudRecordDates() {
      return [...this.cloudRecordDates].sort((a, b) => {
        return moment(b).valueOf() - moment(a).valueOf()
      })
    },
    filteredChannels() {
      let result = this.channels
      
      if (this.channelQuery.keyword) {
        const keyword = this.channelQuery.keyword.toLowerCase()
        result = result.filter(ch => 
          (ch.name && ch.name.toLowerCase().includes(keyword)) ||
          (ch.channelId && ch.channelId.toLowerCase().includes(keyword))
        )
      }
      
      if (this.channelQuery.status) {
        result = result.filter(ch => ch.status === this.channelQuery.status)
      }
      
      this.channelPagination.total = result.length
      const start = (this.channelPagination.current - 1) * this.channelPagination.pageSize
      const end = start + this.channelPagination.pageSize
      return result.slice(start, end)
    },
    deviceTimelineRecords() {
      return (this.records || []).map(record => ({
        id: record.id || `${record.startTime}_${record.endTime}`,
        name: record.name || `录像_${this.formatTime(record.startTime)}`,
        startTime: record.startTime,
        endTime: record.endTime,
        duration: this.calcDuration(record.startTime, record.endTime),
        fileSize: record.fileSize || 0
      }))
    },
    deviceTimelineDate() {
      let ts = this.currentRecord && this.currentRecord.startTime
      if (!ts) return moment().format('YYYY-MM-DD')
      let time = typeof ts === 'string' ? parseInt(ts) : ts
      if (time && time.toString().length === 10) time = time * 1000
      return moment(time).format('YYYY-MM-DD')
    }
  },
  watch: {
    // 监听 tab 切换，当切换到设备录像时自动查询
    activeTab(newTab, oldTab) {
      // 只在从其他 tab 切换到 records 时触发，避免初始化时重复调用
      if (newTab === 'records' && oldTab && oldTab !== 'records') {
        // 延迟执行，确保 channels 已经加载完成
        this.$nextTick(() => {
          if (this.currentChannel) {
            // 如果已经有通道，自动查询
            this.viewRecords(this.currentChannel)
          } else if (this.channels.length > 0) {
            // 如果没有选中通道，选择第一个通道
            this.viewRecords(this.channels[0])
          }
        })
      }
    }
  },
  created() {
    this.instanceKey = this.$route.params.instanceKey
    this.deviceId = this.$route.params.deviceId
    this.channelId = this.$route.params.channelId  // 获取channelId参数
    
    if (this.deviceId) {
      this.loadDeviceDetail()
    }
  },
  beforeDestroy() {
    // 清理下载进度定时器
    this.stopDownloadProgressPolling()
  },
  methods: {
    back() {
      this.$router.back()
    },
    switchTab(tab) {
      this.activeTab = tab
      
      // 当切换到云端录像或设备录像时，如果没有选中通道，自动选择第一个通道
      if ((tab === 'cloud-records' || tab === 'records') && !this.currentChannel && this.channels.length > 0) {
        const firstChannel = this.channels[0]
        if (tab === 'cloud-records') {
          this.viewCloudRecords(firstChannel)
        } else if (tab === 'records') {
          this.viewRecords(firstChannel)
        }
      }
    },
    copyToClipboard(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopyTextToClipboard(text)
        })
      } else {
        this.fallbackCopyTextToClipboard(text)
      }
    },
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea')
      textArea.value = text
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        document.execCommand('copy')
        this.$message.success('已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败')
      }
      document.body.removeChild(textArea)
    },
    async loadDeviceDetail() {
      this.loading = true
      try {
        console.log('Loading device detail:', { instanceKey: this.instanceKey, deviceId: this.deviceId })
        const res = await getPlatformDevices(this.instanceKey, {})
        console.log('API response:', res)
        const devices = res.data || []
        console.log('Devices list:', devices)
        
        // 尝试多种字段名匹配
        this.currentDevice = devices.find(d => 
          d.deviceId === this.deviceId || 
          d.extDeviceId === this.deviceId ||
          d.deviceNo === this.deviceId
        )
        
        console.log('Found device:', this.currentDevice)
        
        if (this.currentDevice) {
          // 加载设备扩展信息
          this.loadDeviceExtInfo()
          
          // 加载通道列表
          this.channels = this.currentDevice.channelList || []
          this.channelPagination.total = this.channels.length
          console.log('Channels:', this.channels)
          
          // 如果有channelId，自动选中并加载该通道，但保持基础信息页面
          if (this.channelId && this.channels.length > 0) {
            const channel = this.channels.find(ch => ch.channelId === this.channelId)
            if (channel) {
              this.currentChannel = channel
              this.currentChannelDetail = channel
              // 不自动跳转，保持在基础信息页面
              this.activeTab = 'cloud-records'
              this.viewCloudRecords(channel)
            } else {
              this.$message.warning('未找到指定的通道')
            }
          }
        } else {
          console.error('Device not found. DeviceId:', this.deviceId, 'Available devices:', devices.map(d => ({ id: d.deviceId, extId: d.extDeviceId })))
          this.$message.error('未找到设备信息，请检查设备ID是否正确')
        }
      } catch (e) {
        console.error('Load device detail error:', e)
        this.$message.error('加载设备详情失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.loading = false
      }
    },
    loadDeviceExtInfo() {
      // 模拟加载设备扩展信息
      // TODO: 实际项目中需要调用后端API获取video_platform_device_ext表数据
      if (this.currentDevice) {
        this.deviceExt = {
          charset: this.currentDevice.charset || 'GB2312',
          transport: this.currentDevice.transport || 'UDP',
          streamMode: this.currentDevice.streamMode,
          registerTime: this.currentDevice.registerTime,
          keepaliveTime: this.currentDevice.keepaliveTime,
          mediaServerId: this.currentDevice.mediaServerId
        }
      }
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
    queryChannels() {
      // 过滤逻辑在computed中处理
      this.channelPagination.current = 1
    },
    resetChannelQuery() {
      this.channelQuery = {
        keyword: undefined,
        status: undefined
      }
      this.channelPagination.current = 1
    },
    async syncDevices() {
      this.syncLoading = true
      try {
        await syncPlatform(this.instanceKey)
        this.$message.success('同步成功')
        // 重新加载设备信息
        await this.loadDeviceDetail()
      } catch (e) {
      } finally {
        this.syncLoading = false
      }
    },
    handleChannelTableChange(pagination) {
      this.channelPagination.current = pagination.current
      this.channelPagination.pageSize = pagination.pageSize
    },
    async previewChannel(channel) {
      this.currentChannel = channel
      this.activeTab = 'preview'
      
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
        } else {
          this.$message.success('正在加载视频流...')
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
    async viewRecords(channel) {
      this.currentChannel = channel
      this.activeTab = 'records'
      
      // 确保 currentDevice 已设置
      if (!this.currentDevice && this.deviceId) {
        await this.loadDeviceDetail()
      }
      
      // 确保 currentDevice 和 currentChannel 都存在
      if (!this.currentDevice || !this.currentChannel) {
        this.$message.warning('设备或通道信息不完整，无法查询录像')
        return
      }
      
      // 默认查询今天的录像（0点到23:59:59）
      this.selectTimeRange('today')
      
      // 自动查询当天的录像
      await this.queryRecords()
    },
    selectTimeRange(type) {
      const now = moment()
      let startTime, endTime
      
      switch (type) {
        case 'today':
          startTime = moment().startOf('day')
          endTime = moment().endOf('day') // 查询到23:59:59
          break
        case 'yesterday':
          startTime = moment().subtract(1, 'day').startOf('day')
          endTime = moment().subtract(1, 'day').endOf('day')
          break
        case 'last6hours':
          startTime = moment().subtract(6, 'hours')
          endTime = now
          break
        case 'last24hours':
          startTime = moment().subtract(24, 'hours')
          endTime = now
          break
        case 'last3days':
          startTime = moment().subtract(3, 'days').startOf('day')
          endTime = now
          break
        default:
          startTime = moment().startOf('day')
          endTime = now
      }
      
      this.recordQuery.timeRange = [startTime, endTime]
    },
    async viewCloudRecords(channel) {
      this.currentChannel = channel
      this.activeTab = 'cloud-records'
      
      // 初始化云端录像日期和列表
      this.cloudRecordDate = moment()
      await this.queryCloudRecordDates()
      
      // 如果有日期，默认选中最新的日期
      if (this.cloudRecordDates.length > 0) {
        const latestDate = this.sortedCloudRecordDates[0]
        await this.selectCloudDate(latestDate)
      } else {
        // 即使没有日期，也尝试查询当天的录像
        await this.queryCloudRecords()
      }
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
      let s = typeof startTime === 'string' ? parseInt(startTime) : startTime
      let e = typeof endTime === 'string' ? parseInt(endTime) : endTime
      if (Number.isNaN(s) || Number.isNaN(e)) return '-'
      if (s.toString().length === 10) s = s * 1000
      if (e.toString().length === 10) e = e * 1000
      const duration = moment.duration(e - s)
      const hours = Math.floor(duration.asHours())
      const minutes = duration.minutes()
      const seconds = duration.seconds()
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    },
    async playbackRecord(record) {
      console.log('触发设备录像回放', record)
      this.currentRecord = record
      this.devicePlayModalVisible = true
      console.log('devicePlayModalVisible =', this.devicePlayModalVisible)
    },
    closeDevicePlayModal() {
      this.devicePlayModalVisible = false
      this.currentRecord = null
    },
    // 处理禁用下载（暂时禁用，保留代码逻辑）
    handleDownloadDisabled() {
      this.$message.warning('禁止下载')
    },
    
    // 下载录像（代码逻辑保留，暂时禁用）
    async downloadRecord(record) {
      // 确保记录数据有效
      if (!record || !record.startTime || !record.endTime) {
        this.$message.error('录像数据无效')
        return
      }
      
      // 构建完整的记录信息
      const downloadRecord = {
        id: record.id || `${record.startTime}_${record.endTime}`,
        name: record.name || `设备录像_${this.formatTime(record.startTime)}`,
        startTime: record.startTime,
        endTime: record.endTime,
        fileSize: record.fileSize || 0
      }
      
      // 打开下载弹窗
      this.currentDownloadRecord = downloadRecord
      this.downloadModalVisible = true
    },
    closeDownloadModal() {
      this.downloadModalVisible = false
      this.currentDownloadRecord = null
    },
    handleRecordDownloadFromModal(record) {
      // 从播放弹窗触发的下载
      this.downloadRecord(record)
    },
    handlePlaybackPlay() {
      console.log('回放开始播放')
    },
    handlePlaybackStop() {
      this.playbackUrl = ''
    },
    async playbackSpeed(speed) {
      if (!this.playbackMediaServerId || !this.playbackApp || !this.playbackStream) {
        this.$message.warning('播放信息不完整，无法设置倍速')
        console.error('播放信息:', {
          mediaServerId: this.playbackMediaServerId,
          app: this.playbackApp,
          stream: this.playbackStream
        })
        return
      }
      
      try {
        await setCloudRecordSpeed(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            mediaServerId: this.playbackMediaServerId,
            app: this.playbackApp,
            stream: this.playbackStream,
            speed: speed,
            schema: 'ts'
          }
        )
        this.$message.success(`已设置${speed}x倍速`)
      } catch (e) {
        this.$message.error('设置倍速失败：' + (e.response?.data?.msg || e.message))
      }
    },
    async seekPlayback(progress) {
      if (!this.currentChannel || !this.currentRecord) return
      
      try {
        // 计算对应的时间戳（毫秒）
        const duration = this.currentRecord.endTime - this.currentRecord.startTime
        const seekTime = this.currentRecord.startTime + (duration * progress / 100)
        
        await seekCloudRecord(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            app: 'rtp',
            stream: `${this.currentDevice.deviceId}_${this.currentChannel.channelId}`,
            seek: seekTime
          }
        )
        this.$message.success(`已跳转到${progress}%`)
      } catch (e) {
        this.$message.error('跳转失败：' + (e.response?.data?.msg || e.message))
      }
    },
    
    // ==================== 云端录像相关方法 ====================
    async queryCloudRecordDates() {
      if (!this.currentChannel) return
      
      // 确保 cloudRecordDate 有效
      if (!this.cloudRecordDate) {
        this.cloudRecordDate = moment()
      }
      
      try {
        const res = await queryCloudRecordDates(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            app: 'rtp',
            stream: `${this.currentDevice.deviceId}_${this.currentChannel.channelId}`,
            year: this.cloudRecordDate.year(),
            month: this.cloudRecordDate.month() + 1
          }
        )
        
        console.log('云端录像日期查询返回:', res)
        
        // 确保赋值为数组
        this.cloudRecordDates = Array.isArray(res.data) ? res.data : []
        
        console.log('赋值后的cloudRecordDates:', this.cloudRecordDates)
      } catch (e) {
        console.error('查询云端录像日期失败：', e)
      }
    },
    async selectCloudDate(dateStr) {
      // 选中日期并查询当日录像
      this.selectedCloudDate = dateStr
      this.cloudRecordDate = moment(dateStr)
      await this.queryCloudRecords()
    },
    isDateSelected(dateStr) {
      // 判断日期是否被选中
      return this.selectedCloudDate === dateStr
    },
    formatDateLabel(dateStr) {
      // 格式化日期显示
      const date = moment(dateStr)
      const today = moment().startOf('day')
      const yesterday = moment().subtract(1, 'day').startOf('day')
      
      if (date.isSame(today, 'day')) {
        return '今天 ' + date.format('MM-DD')
      } else if (date.isSame(yesterday, 'day')) {
        return '昨天 ' + date.format('MM-DD')
      } else {
        // 显示星期
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        return date.format('MM-DD') + ' ' + weekdays[date.day()]
      }
    },

    async queryCloudRecords() {
      if (!this.currentChannel) return
      
      // 确保 cloudRecordDate 有效
      if (!this.cloudRecordDate) {
        this.cloudRecordDate = moment()
      }
      
      this.cloudRecordLoading = true
      try {
        // 查询当前选中日期的录像
        const startTime = this.cloudRecordDate.clone().startOf('day').format('YYYY-MM-DD HH:mm:ss')
        const endTime = this.cloudRecordDate.clone().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        
        console.log('查询云端录像:', { startTime, endTime })
        
        const res = await queryCloudRecords(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            app: 'rtp',
            stream: `${this.currentDevice.deviceId}_${this.currentChannel.channelId}`,
            page: this.cloudRecordPagination.current,
            count: this.cloudRecordPagination.pageSize,
            startTime,
            endTime
          }
        )
        
        console.log('云端录像返回:', res.data)
        
        if (res.data) {
          this.cloudRecords = res.data.list || []
          this.cloudRecordPagination.total = res.data.total || 0
          
          // 调试：输出第一条录像的时间戳信息
          if (this.cloudRecords.length > 0) {
            const first = this.cloudRecords[0]
            console.log('第一条录像时间信息:', {
              startTime: first.startTime,
              endTime: first.endTime,
              startTimeType: typeof first.startTime,
              endTimeType: typeof first.endTime,
              formatted: this.formatCloudRecordTime(first.startTime)
            })
          }
        }
      } catch (e) {
        this.$message.error('查询云端录像失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.cloudRecordLoading = false
      }
    },
    handleCloudRecordTableChange(pagination) {
      this.cloudRecordPagination.current = pagination.current
      this.cloudRecordPagination.pageSize = pagination.pageSize
      this.queryCloudRecords()
    },
    async playCloudRecord(record) {
      try {
        // 清除之前的延迟定时器
        if (this.showLoadingDelay) {
          clearTimeout(this.showLoadingDelay)
          this.showLoadingDelay = null
        }
        
        // 立即显示弹窗
        this.playModalVisible = true
        this.currentRecord = record
        this.cloudPlayerReady = false
        
        // 延迟100ms显示加载状态，避免快速加载时的闪烁
        this.showLoadingDelay = setTimeout(() => {
          this.playLoading = true
        }, 100)
        
        const res = await loadCloudRecord(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            app: 'rtp',
            stream: `${this.currentDevice.deviceId}_${this.currentChannel.channelId}`,
            cloudRecordId: record.id
          }
        )
        
        console.log('加载云端录像返回:', res.data)
        
        if (res.data) {
          // 构造流信息对象（参考WVP官方格式）
          const streamInfo = {
            flv: res.data.flv,
            hls: res.data.hls,
            rtmp: res.data.rtmp,
            rtsp: res.data.rtsp,
            ws_flv: res.data.ws_flv || res.data.flv,
            wss_flv: res.data.wss_flv || res.data.flv,
            mediaServerId: res.data.mediaServerId || record.mediaServerId,
            app: res.data.app || 'mp4_record',
            stream: res.data.stream || '',
            key: res.data.key || '',
            duration: record.timeLen || 0 // 录像时长（毫秒）
          }
          
          // 构造录像信息对象
          const recordInfo = {
            id: record.id,
            fileName: record.fileName,
            startTime: record.startTime,
            endTime: record.endTime,
            timeLen: record.timeLen,
            fileSize: record.fileSize
          }
          
          // 保存播放流信息
          this.playbackMediaServerId = streamInfo.mediaServerId
          this.playbackApp = streamInfo.app
          this.playbackStream = streamInfo.stream
          this.cloudStreamInfo = streamInfo
          this.cloudRecordInfo = recordInfo
          
          console.log('播放信息:', { streamInfo, recordInfo })
          
          // 准备播放器
          this.cloudPlayerReady = true
          
          // 清除加载状态
          if (this.showLoadingDelay) {
            clearTimeout(this.showLoadingDelay)
            this.showLoadingDelay = null
          }
          this.playLoading = false
          
          // 等待播放器渲染完成后设置流信息
          this.$nextTick(() => {
            if (this.$refs.cloudPlayer) {
              this.$refs.cloudPlayer.setStreamInfo(streamInfo, recordInfo)
            }
          })
        } else {
          this.$message.warning('未获取到播放地址')
          // 清除加载状态
          if (this.showLoadingDelay) {
            clearTimeout(this.showLoadingDelay)
            this.showLoadingDelay = null
          }
          this.playLoading = false
          this.playModalVisible = false
        }
      } catch (e) {
        this.$message.error('加载录像失败：' + (e.response?.data?.msg || e.message))
        // 清除加载状态
        if (this.showLoadingDelay) {
          clearTimeout(this.showLoadingDelay)
          this.showLoadingDelay = null
        }
        this.playLoading = false
        this.playModalVisible = false
      }
    },
    closePlayModal() {
      // 停止播放器
      if (this.$refs.cloudPlayer) {
        this.$refs.cloudPlayer.stopPlay()
      }
      
      // 清除延迟定时器
      if (this.showLoadingDelay) {
        clearTimeout(this.showLoadingDelay)
        this.showLoadingDelay = null
      }
      
      this.playModalVisible = false
      this.cloudPlayerReady = false
      this.playLoading = false
      this.playbackUrl = ''
      this.currentRecord = null
      this.cloudStreamInfo = null
      this.cloudRecordInfo = null
    },
    
    // ==================== 云端播放器事件处理 ====================
    onCloudPlayerPlay() {
      console.log('云端录像开始播放')
    },
    onCloudPlayerPause() {
      console.log('云端录像暂停')
    },
    onCloudPlayerStop() {
      console.log('云端录像停止')
      this.closePlayModal()
    },
    async onCloudPlayerSeek(params) {
      // 播放器发起seek请求
      console.log('云端录像seek:', params)
      try {
        await seekCloudRecord(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            mediaServerId: params.mediaServerId,
            app: params.app,
            stream: params.stream,
            seek: params.seek,
            schema: 'fmp4'
          }
        )
      } catch (e) {
        console.error('Seek失败:', e)
        this.$message.error('跳转失败：' + (e.response?.data?.msg || e.message))
      }
    },
    async onCloudPlayerSpeedChange(params) {
      // 播放器发起倍速请求
      console.log('云端录像倍速:', params)
      try {
        await setCloudRecordSpeed(
          this.instanceKey,
          this.currentDevice.deviceId,
          this.currentChannel.channelId,
          {
            mediaServerId: params.mediaServerId,
            app: params.app,
            stream: params.stream,
            speed: params.speed,
            schema: 'ts'
          }
        )
        this.$message.success(`已设置${params.speed}x倍速`)
      } catch (e) {
        console.error('设置倍速失败:', e)
        this.$message.error('设置倍速失败：' + (e.response?.data?.msg || e.message))
      }
    },
    async refreshCloudRecordDates() {
      // 确保 cloudRecordDate 有效
      if (!this.cloudRecordDate) {
        this.cloudRecordDate = moment()
      }
      
      this.$message.loading('正在刷新日期...', 0)
      try {
        await this.queryCloudRecordDates()
        this.$message.destroy()
        this.$message.success('刷新成功')
      } catch (e) {
        this.$message.destroy()
        this.$message.error('刷新失败')
      }
    },
    async downloadCloudRecord(record) {
      try {
        // 调用WVP接口获取下载地址
        const res = await getCloudRecordPlayPath(
          this.instanceKey,
          record.id
        )
        
        console.log('云端录像下载地址:', res.data)
        
        // 后端已解析data字段，直接使用
        if (res.data && (res.data.httpPath || res.data.httpsPath)) {
          // 根据当前协议选择对应的下载地址
          let downloadUrl = ''
          if (location.protocol === 'https:') {
            // HTTPS环境优先使用httpsPath
            downloadUrl = res.data.httpsPath || res.data.httpPath
          } else {
            // HTTP环境优先使用httpPath
            downloadUrl = res.data.httpPath || res.data.httpsPath
          }
          
          if (!downloadUrl) {
            this.$message.error('未获取到下载地址')
            return
          }
          
          // 添加save_name参数强制下载（参考WVP官方实现）
          const fileName = record.fileName || 'video.mp4'
          const downloadUrlWithParam = downloadUrl + '&save_name=' + encodeURIComponent(fileName)
          
          // 使用a标签触发下载
          const link = document.createElement('a')
          link.href = downloadUrlWithParam
          link.target = '_blank'
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          
          this.$message.success('开始下载')
        } else {
          this.$message.error('未获取到下载地址')
        }
      } catch (e) {
        this.$message.error('获取下载地址失败：' + (e.response?.data?.msg || e.message))
      }
    },
    formatCloudRecordTime(timestamp) {
      // 处理时间戳，支持毫秒和秒，支持字符串和数字类型
      if (!timestamp) return '-'
      
      // 先转换为数字类型
      let time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      
      // 如果是10位秒时间戳，转为毫秒
      if (time.toString().length === 10) {
        time = time * 1000
      }
      
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },
    formatApiTime(timestamp) {
      if (!timestamp) return ''
      let time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      if (!time || Number.isNaN(time)) return ''
      if (time.toString().length === 10) {
        time = time * 1000
      }
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },
    formatCloudRecordDuration(timeLen) {
      // 使用WVP的时长格式化方法
      if (!timeLen) return '-'
      const h = parseInt(timeLen / 3600 / 1000)
      const minute = parseInt((timeLen - h * 3600 * 1000) / 60 / 1000)
      let second = Math.ceil((timeLen - h * 3600 * 1000 - minute * 60 * 1000) / 1000)
      if (second < 0) {
        second = 0
      }
      return (h > 0 ? h + `小时` : '') + (minute > 0 ? minute + '分' : '') + (second > 0 ? second + '秒' : '')
    },
    formatDuration(seconds) {
      if (!seconds) return '-'
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    formatFileSize(bytes) {
      if (!bytes) return '-'
      const units = ['B', 'KB', 'MB', 'GB']
      let size = bytes
      let unitIndex = 0
      while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024
        unitIndex++
      }
      return `${size.toFixed(2)} ${units[unitIndex]}`
    }
  }
}
</script>

<style lang="less" scoped>
/* 页面容器样式 */


/* 主卡片 - 整体容器 */
.main-card {
  background: #ffffff;
  border: none;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
  border-radius: 6px;
  overflow: hidden;
}

/* 卡片内容区域 - 移除默认padding */
.main-card ::v-deep .ant-card-body {
  padding: 0;
}

/* 云端录像区域样式优化 */
.date-select-card {
  height: 100%;
  
  ::v-deep .ant-card-head {
    min-height: 40px;
    padding: 0 12px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;
    
    .ant-card-head-title {
      padding: 8px 0;
      font-size: 13px;
      font-weight: 600;
    }
    
    .ant-card-extra {
      padding: 8px 0;
    }
  }
  
  ::v-deep .ant-card-body {
    padding: 8px !important;
  }
}

.record-list-card {
  ::v-deep .ant-card-head {
    min-height: 40px;
    padding: 0 12px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;
    
    .ant-card-head-title {
      padding: 8px 0;
      font-size: 13px;
      font-weight: 600;
    }
    
    .ant-card-extra {
      padding: 8px 0;
    }
  }
}

/* 页面头部样式 - 在卡片内部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: #64748b;

  &:hover {
    background: #e2e8f0;
    border-color: #1966ff;
    color: #1966ff;
    transform: scale(1.05);
  }
}

.page-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  line-height: 1.2;
}

/* 自定义标签页样式 */
.custom-tabs-container {
  background: #fff;
  overflow: hidden;
}

.custom-tabs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  border-bottom: 1px solid #e8e8e8;
}

.custom-tabs-nav {
  display: flex;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  flex: 1;
}

.tabs-actions {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-left: 1px solid #e8e8e8;
}

.custom-tabs-nav::-webkit-scrollbar {
  display: none;
}

.custom-tab-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  min-width: fit-content;
  border-right: 1px solid #e8e8e8;
  background: #fafafa;
}

.custom-tab-item:last-child {
  border-right: none;
}

.custom-tab-item:hover:not([disabled]) {
  background: #e6f7ff;
  color: #1890ff;
}

.custom-tab-item.active {
  background: #ffffff;
  color: #1890ff;
  border-bottom: 2px solid #1890ff;
  margin-bottom: -1px;
}

/* 禁用样式 */
.custom-tab-item[disabled] {
  color: #bfbfbf;
  cursor: not-allowed;
  opacity: 0.6;
  
  &:hover {
    background: #fafafa;
    color: #bfbfbf;
  }
}

.custom-tab-content {
  min-height: 500px;
  background: #fff;
}

.tab-pane {
  border: 1px solid #e8e8e8;
  padding: 16px;
}

/* 信息卡片样式 */
.info-card {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  margin-bottom: 16px;
  overflow: hidden;
}

.card-header {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  
  h3 {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #262626;
    
    .anticon {
      margin-right: 8px;
      color: #1890ff;
    }
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  padding: 16px;
}

.info-row {
  display: flex;
  padding: 8px 12px;
  border-bottom: 1px dashed #f0f0f0;
  
  &:hover {
    background: #fafafa;
  }
  
  .label {
    min-width: 100px;
    font-size: 13px;
    color: #8c8c8c;
    flex-shrink: 0;
  }
  
  .value {
    flex: 1;
    font-size: 13px;
    color: #262626;
    word-break: break-all;
    
    .copy-icon {
      margin-left: 6px;
      color: #8c8c8c;
      cursor: pointer;
      transition: color 0.2s;
      
      &:hover {
        color: #1890ff;
      }
    }
  }
  
  &.info-row-full {
    grid-column: 1 / -1;
  }
}

/* 老样式（保留以防兼容问题） */
.device-basic-info {
  background: #ffffff;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;
}

.basic-info-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.basic-info-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.basic-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  min-width: 90px;
  width: 90px;
  font-size: 13px;
  color: #8c8c8c;
  margin-right: 12px;
  flex-shrink: 0;
  text-align: left;
}

.info-value-group {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.info-value {
  font-size: 13px;
  color: #262626;
  line-height: 1.4;
  max-width: 420px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copy-action-btn {
  width: 18px;
  height: 18px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  background: transparent;
  border: none;
  transition: all 0.2s ease;
  font-size: 10px;
  margin-left: 2px;
  flex-shrink: 0;
}

.copy-action-btn:hover {
  color: #1890ff;
  background: #f0f8ff;
}

.info-item-full {
  grid-column: 1 / -1;
}

/* 简化其他页面样式 */
.simple-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.simple-header h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.simple-header p {
  margin: 0;
  font-size: 13px;
  color: #8c8c8c;
}

.table-page-search-wrapper {
  margin-bottom: 16px;
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

// 云端录像日期列表样式
.date-list {
  max-height: 650px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 2px;
    
    &:hover {
      background: #bfbfbf;
    }
  }
  
  .date-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    margin-bottom: 4px;
    background: #ffffff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    min-height: 40px;
    
    &:hover {
      background: #e6f7ff;
      border-color: #91d5ff;
      transform: translateX(2px);
    }
    
    &.active {
      background: linear-gradient(90deg, #e6f7ff 0%, #f0f9ff 100%);
      border-color: #1890ff;
      box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1);
      
      .date-label {
        color: #1890ff;
        font-weight: 600;
      }
      
      .date-arrow {
        color: #1890ff;
      }
    }
    
    .date-label {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #262626;
      flex: 1;
      min-width: 0;
      
      .anticon {
        font-size: 14px;
        flex-shrink: 0;
      }
      
      span {
        font-size: 13px;
        line-height: 1.4;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
    
    .date-arrow {
      color: #bfbfbf;
      font-size: 12px;
      flex-shrink: 0;
      margin-left: 8px;
      transition: transform 0.2s ease;
    }
    
    &:hover .date-arrow {
      transform: translateX(2px);
    }
  }
}

// 云端录像播放弹窗样式
::v-deep .cloud-record-play-modal {
  .ant-modal-header {
    padding: 12px 16px;
    border: none;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  .ant-modal-title {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
  }
  
  .ant-modal-body {
    padding: 0;
  }
  
  .ant-modal-close {
    top: 8px;
    right: 8px;
  }
  
  .ant-modal-close-x {
    color: #fff;
    font-size: 16px;
    line-height: 40px;
    width: 40px;
    height: 40px;
  }
  
  .ant-modal-close-x:hover {
    color: rgba(255, 255, 255, 0.8);
  }
}

// 禁用下载按钮样式
.download-disabled {
  color: #d9d9d9 !important;
  cursor: not-allowed !important;
  pointer-events: none;
  
  &:hover {
    color: #d9d9d9 !important;
  }
}
</style>
