<template>
  <div>
    <!-- 通道列表弹窗 -->
    <a-modal
      :visible="visible"
      :title="`${deviceInfo.deviceName || '设备'} - 通道列表`"
      width="900px"
      :footer="null"
      @cancel="handleCancel"
      :destroyOnClose="true"
    >
      <a-table
        :columns="columns"
        :data-source="channels"
        rowKey="channelId"
        size="small"
        :pagination="false"
        :scroll="{ y: 500 }"
        :loading="channelLoading"
        :locale="{ emptyText: '暂无通道数据' }"
      >
        <!-- <span slot="channelStatus" slot-scope="text">
          <a-badge :status="text === 'ON' ? 'success' : 'default'" :text="text === 'ON' ? '在线' : '离线'" />
        </span> -->
        <span slot="ptzType" slot-scope="text">
          {{ formatPtzType(text) }}
        </span>
        <span slot="channelAction" slot-scope="text, record">
          <a @click="openPreviewModal(record)" style="font-size: 12px; margin-right: 12px;">
            <a-icon type="play-circle" /> 预览
          </a>
          <a @click="handleSelectChannel(record)" style="font-size: 12px;">
            <a-icon type="eye" /> 详情
          </a>
        </span>
      </a-table>
    </a-modal>
    
    <!-- 视频预览弹窗 - 使用LivePlayerModal -->
    <live-player-modal
      v-model="previewModalVisible"
      :title="currentPreviewChannel ? `${currentPreviewChannel.name || currentPreviewChannel.channelId} - 实时预览` : '实时预览'"
      :streamInfo="streamInfo"
      :hasAudio="true"
      :showPtz="isPtzSupported"
      :deviceId="deviceInfo.deviceId"
      :channelId="currentPreviewChannel ? currentPreviewChannel.channelId : ''"
      @ptz="handlePtzCommand"
      @close="closePreviewModal"
    />
  </div>
</template>

<script>
import { getChannelList } from '@/api/video/channel'
import { getPreviewUrl, controlPTZ } from '@/api/video/channel'
import LivePlayerModal from '@/components/LivePlayerModal'

export default {
  name: 'VideoChannelSelectModal',
  components: { LivePlayerModal },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    deviceInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      channels: [],
      channelLoading: false,
      // 预览相关
      previewModalVisible: false,
      currentPreviewChannel: null,
      streamInfo: null,
      previewLoading: false,
      columns: [
        { title: '通道ID', dataIndex: 'channelId', key: 'channelId', width: 160, ellipsis: true },
        { title: '通道名称', dataIndex: 'name', key: 'name', width: 150, ellipsis: true },
        // { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'channelStatus' } },
        { title: 'PTZ类型', dataIndex: 'ptzType', key: 'ptzType', width: 100, scopedSlots: { customRender: 'ptzType' } },
        // { title: '编码格式', dataIndex: 'streamType', key: 'streamType', width: 100 },
        { title: '操作', key: 'channelAction', width: 150, scopedSlots: { customRender: 'channelAction' } }
      ]
    }
  },
  computed: {
    // 判断当前通道是否支持PTZ
    isPtzSupported() {
      if (!this.currentPreviewChannel) return false
      const ptzType = this.currentPreviewChannel.ptzType
      return ptzType !== undefined && ptzType !== 0 && this.formatPtzType(ptzType) !== '不支持'
    }
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          this.loadChannels()
        } else {
          // 关闭时清空数据
          this.channels = []
        }
      }
    }
  },
  methods: {
    handleCancel() {
      this.$emit('close')
    },
    handleSelectChannel(channel) {
      this.$emit('select', channel)
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
    async loadChannels() {
      if (!this.deviceInfo || !this.deviceInfo.thirdPlatform || !this.deviceInfo.deviceId) {
        console.warn('设备信息不完整，无法加载通道列表', this.deviceInfo)
        return
      }
      
      this.channelLoading = true
      try {
        // 根据平台类型构造instanceKey
        let instanceKey = ''
        if (this.deviceInfo.thirdPlatform === 'wvp') {
          // WVP平台使用gwProductKey作为instanceKey
          instanceKey = this.deviceInfo.gwProductKey || this.deviceInfo.extDeviceId || this.deviceInfo.deviceId
        } else if (this.deviceInfo.thirdPlatform === 'ics') {
          instanceKey = this.deviceInfo.gwProductKey || this.deviceInfo.extDeviceId || this.deviceInfo.deviceId
        } else if (this.deviceInfo.thirdPlatform === 'icc') {
          instanceKey = this.deviceInfo.gwProductKey || this.deviceInfo.extDeviceId || this.deviceInfo.deviceId
        } else {
          instanceKey = this.deviceInfo.gwProductKey || this.deviceInfo.extDeviceId || this.deviceInfo.deviceId
        }
        
        console.log('加载通道列表:', { instanceKey, deviceId: this.deviceInfo.deviceId, platform: this.deviceInfo.thirdPlatform })
        
        const res = await getChannelList(instanceKey, this.deviceInfo.deviceId, {})
        console.log('通道列表响应:', res)
        this.channels = res.data || []
        
        if (this.channels.length === 0) {
          console.warn('未获取到通道数据')
        }
      } catch (error) {
        console.error('加载通道列表失败:', error)
        this.$message.error('加载通道列表失败: ' + (error.response?.data?.msg || error.message))
        this.channels = []
      } finally {
        this.channelLoading = false
      }
    },
    
    // 预览相关方法
    async openPreviewModal(channel) {
      this.currentPreviewChannel = channel
      this.previewModalVisible = true
      this.previewLoading = true
      this.streamInfo = null
      
      try {
        const instanceKey = this.deviceInfo.gwProductKey || this.deviceInfo.extDeviceId || this.deviceInfo.deviceId
        
        const res = await getPreviewUrl(
          instanceKey,
          this.deviceInfo.deviceId,
          channel.channelId,
          'main'
        )
        
        // LivePlayerModal需要streamInfo对象格式
        this.streamInfo = res.data || {}
        
        if (!this.streamInfo || Object.keys(this.streamInfo).length === 0) {
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
      this.streamInfo = null
      this.currentPreviewChannel = null
    },
    
    handlePtzCommand({ command, speed }) {
      if (!this.currentPreviewChannel) {
        this.$message.warning('请先选择通道')
        return
      }
      
      const instanceKey = this.deviceInfo.gwProductKey || this.deviceInfo.extDeviceId || this.deviceInfo.deviceId
      
      controlPTZ(
        instanceKey,
        this.deviceInfo.deviceId,
        this.currentPreviewChannel.channelId,
        command,
        speed
      ).catch(e => {
        console.error('PTZ控制失败:', e)
        // 不显示错误提示，避免干扰
      })
    }
  }
}
</script>

<style scoped>
/* 样式保持简洁 */
</style>