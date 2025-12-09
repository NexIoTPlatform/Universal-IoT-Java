<template>
  <div class="channel-table-container">
    <!-- 筛选栏 -->
    <a-form v-if="showFilter" layout="inline" class="filter-form">
      <a-form-item label="关键词">
        <a-input
          v-model:value="filters.keyword"
          placeholder="通道名称/ID"
          allow-clear
          style="width: 200px"
          @pressEnter="handleSearch"
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          v-model:value="filters.status"
          placeholder="全部"
          allow-clear
          style="width: 120px"
          @change="handleSearch"
        >
          <a-select-option value="online">在线</a-select-option>
          <a-select-option value="offline">离线</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="PTZ类型">
        <a-select
          v-model:value="filters.ptzType"
          placeholder="全部"
          allow-clear
          style="width: 140px"
          @change="handleSearch"
        >
          <a-select-option :value="0">不支持</a-select-option>
          <a-select-option :value="1">球机</a-select-option>
          <a-select-option :value="2">半球</a-select-option>
          <a-select-option :value="3">固定枪机</a-select-option>
          <a-select-option :value="4">遥控枪机</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">
          <a-icon type="search" />搜索
        </a-button>
        <a-button style="margin-left: 8px" @click="handleReset">
          <a-icon type="reload" />重置
        </a-button>
      </a-form-item>
    </a-form>

    <!-- 通道列表表格 -->
    <a-table
      :loading="loading"
      :columns="columns"
      :data-source="channelList"
      :pagination="false"
      :scroll="{ x: 'max-content', y: tableHeight }"
      size="small"
      bordered
    >
      <template #channelStatus="{ text }">
        <a-tag :color="text === 'online' ? 'green' : 'default'" size="small">
          {{ text === 'online' ? '在线' : '离线' }}
        </a-tag>
      </template>
      
      <template #ptzType="{ text }">
        {{ getPtzTypeText(text) }}
      </template>

      <!-- 平台特有字段插槽 -->
      <slot name="platform-columns" />

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            @click="handlePreview(record)"
          >
            <a-icon type="video-camera" />预览
          </a-button>
          <a-button
            v-if="record.ptzType > 0"
            type="link"
            size="small"
            @click="handlePTZ(record)"
          >
            <a-icon type="control" />云台
          </a-button>
          <slot name="actions" :row="record" />
        </a-space>
      </template>
    </a-table>

    <!-- 空数据提示 -->
    <a-empty v-if="!loading && channelList.length === 0" description="暂无通道数据" />
  </div>
</template>

<script>
import { getChannelList } from '@/api/video/channel'

export default {
  name: 'ChannelTable',
  props: {
    // 平台实例Key
    instanceKey: {
      type: String,
      required: true
    },
    // 设备ID(可选,为空则查询所有通道)
    deviceId: {
      type: String,
      default: ''
    },
    // 是否显示筛选栏
    showFilter: {
      type: Boolean,
      default: true
    },
    // 表格高度
    tableHeight: {
      type: [String, Number],
      default: '500px'
    }
  },
  data() {
    return {
      loading: false,
      channelList: [],
      filters: {
        keyword: '',
        status: undefined,
        ptzType: undefined
      },
      columns: [
        { title: '通道ID', dataIndex: 'channelId', key: 'channelId', width: 180, ellipsis: true },
        { title: '通道名称', dataIndex: 'channelName', key: 'channelName', width: 200, ellipsis: true },
        { 
          title: '状态', 
          dataIndex: 'channelStatus', 
          key: 'channelStatus', 
          width: 100, 
          align: 'center',
          slots: { customRender: 'channelStatus' }
        },
        { 
          title: 'PTZ类型', 
          dataIndex: 'ptzType', 
          key: 'ptzType', 
          width: 120, 
          align: 'center',
          slots: { customRender: 'ptzType' }
        },
        { title: '厂商', dataIndex: 'manufacturer', key: 'manufacturer', width: 120, ellipsis: true },
        { title: '型号', dataIndex: 'model', key: 'model', width: 120, ellipsis: true },
        { title: '安装位置', dataIndex: 'address', key: 'address', ellipsis: true },
        { 
          title: '操作', 
          key: 'action', 
          width: 180, 
          fixed: 'right', 
          align: 'center',
          slots: { customRender: 'action' }
        }
      ]
    }
  },
  mounted() {
    this.loadChannels()
  },
  methods: {
    /**
     * 加载通道列表
     */
    async loadChannels() {
      this.loading = true
      try {
        const res = await getChannelList(this.instanceKey, this.deviceId, this.filters)
        this.channelList = res.data || []
      } catch (error) {
        console.error('加载通道列表失败:', error)
        this.$message.error('加载通道列表失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 搜索
     */
    handleSearch() {
      this.loadChannels()
    },

    /**
     * 重置筛选条件
     */
    handleReset() {
      this.filters = {
        keyword: '',
        status: undefined,
        ptzType: undefined
      }
      this.loadChannels()
    },

    /**
     * 刷新列表(供父组件调用)
     */
    refresh() {
      this.loadChannels()
    },

    /**
     * PTZ类型文本
     */
    getPtzTypeText(ptzType) {
      const map = {
        0: '不支持',
        1: '球机',
        2: '半球',
        3: '固定枪机',
        4: '遥控枪机'
      }
      return map[ptzType] || '未知'
    },

    /**
     * 预览
     */
    handlePreview(row) {
      this.$emit('preview', row)
    },

    /**
     * 云台控制
     */
    handlePTZ(row) {
      this.$emit('ptz', row)
    }
  }
}
</script>

<style lang="less" scoped>
.channel-table-container {
  .filter-form {
    margin-bottom: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
  }
}
</style>
