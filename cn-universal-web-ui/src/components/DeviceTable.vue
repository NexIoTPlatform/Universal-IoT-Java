<template>
  <div class="device-table-wrapper">
    <!-- 筛选条件 -->
    <div class="table-page-search-wrapper" v-if="showSearch">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="设备名称/ID">
              <a-input v-model="queryParams.keyword" placeholder="请输入设备名称或ID" allow-clear @pressEnter="handleSearch"/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="在线状态">
              <a-select v-model="queryParams.status" placeholder="请选择状态" allow-clear style="width: 100%">
                <a-select-option value="ON">在线</a-select-option>
                <a-select-option value="OFF">离线</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <span class="table-page-search-submitButtons">
              <a-button type="primary" @click="handleSearch">
                <a-icon type="search"/>查询
              </a-button>
              <a-button style="margin-left: 8px" @click="handleReset">
                <a-icon type="redo"/>重置
              </a-button>
              <slot name="extraButtons"></slot>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- 设备表格 -->
    <a-table
      :loading="loading"
      :rowKey="rowKey"
      :columns="computedColumns"
      :data-source="dataSource"
      :pagination="paginationConfig"
      :row-selection="rowSelection ? rowSelectionConfig : null"
      @change="handleTableChange"
      size="small"
      :scroll="scroll"
    >
      <!-- 状态列 -->
      <span slot="status" slot-scope="text">
        <a-badge :status="text === 'ON' ? 'success' : 'default'" :text="text === 'ON' ? '在线' : '离线'" />
      </span>

      <!-- 通道数列 -->
      <span slot="channelCount" slot-scope="text, record">
        {{ getChannelCount(record) }}
      </span>

      <!-- 操作列 -->
      <span slot="action" slot-scope="text, record">
        <slot name="action" :record="record">
          <a @click="handleView(record)">查看</a>
          <a-divider type="vertical" v-if="showChannelAction"/>
          <a @click="handleViewChannels(record)" v-if="showChannelAction">通道</a>
        </slot>
      </span>

      <!-- 自定义列插槽 -->
      <template v-for="col in customSlots" :slot="col" slot-scope="text, record">
        <slot :name="col" :text="text" :record="record">{{ text }}</slot>
      </template>
    </a-table>
  </div>
</template>

<script>
export default {
  name: 'DeviceTable',
  props: {
    // 数据源
    dataSource: {
      type: Array,
      default: () => []
    },
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 行主键
    rowKey: {
      type: String,
      default: 'deviceId'
    },
    // 列配置
    columns: {
      type: Array,
      default: () => []
    },
    // 平台类型(wvp/hik/dahua),用于自动适配列
    platformType: {
      type: String,
      default: ''
    },
    // 分页配置
    pagination: {
      type: [Object, Boolean],
      default: () => ({
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条`
      })
    },
    // 是否显示搜索栏
    showSearch: {
      type: Boolean,
      default: true
    },
    // 是否显示通道操作
    showChannelAction: {
      type: Boolean,
      default: true
    },
    // 是否支持行选择
    rowSelection: {
      type: Boolean,
      default: false
    },
    // 已选中的行key
    selectedRowKeys: {
      type: Array,
      default: () => []
    },
    // 表格滚动配置
    scroll: {
      type: Object,
      default: () => ({ x: 1200 })
    },
    // 自定义插槽列名
    customSlots: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      queryParams: {
        keyword: undefined,
        status: undefined
      }
    }
  },
  computed: {
    // 计算列配置
    computedColumns() {
      // 如果传入了columns,直接使用
      if (this.columns && this.columns.length > 0) {
        return this.columns
      }

      // 根据平台类型自动生成列配置
      const baseColumns = [
        { title: '设备ID', dataIndex: 'deviceId', width: 180, ellipsis: true },
        { title: '设备名称', dataIndex: 'deviceName', width: 150, ellipsis: true }
      ]

      // 平台特有字段
      const platformColumns = {
        wvp: [
          { title: '厂商', dataIndex: 'manufacturer', width: 100 },
          { title: '型号', dataIndex: 'model', width: 120 },
          { title: '通道数', key: 'channelCount', width: 80, scopedSlots: { customRender: 'channelCount' } }
        ],
        hik: [
          { title: '摄像机编码', dataIndex: 'cameraIndexCode', width: 150, ellipsis: true },
          { title: '所属组织', dataIndex: 'orgName', width: 120, ellipsis: true }
        ],
        dahua: [
          { title: '设备序列号', dataIndex: 'deviceSn', width: 150, ellipsis: true },
          { title: '所属组织', dataIndex: 'orgName', width: 120, ellipsis: true }
        ]
      }

      const commonColumns = [
        { title: '状态', dataIndex: 'deviceStatus', width: 80, scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', width: 120, fixed: 'right', scopedSlots: { customRender: 'action' } }
      ]

      return [
        ...baseColumns,
        ...(platformColumns[this.platformType] || []),
        ...commonColumns
      ]
    },

    // 分页配置
    paginationConfig() {
      if (this.pagination === false) {
        return false
      }
      return this.pagination
    },

    // 行选择配置
    rowSelectionConfig() {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange,
        onSelect: this.onSelect,
        onSelectAll: this.onSelectAll
      }
    }
  },
  methods: {
    // 获取通道数
    getChannelCount(record) {
      if (record.channelList && Array.isArray(record.channelList)) {
        return record.channelList.length
      }
      if (record.channelCount !== undefined) {
        return record.channelCount
      }
      return 0
    },

    // 搜索
    handleSearch() {
      this.$emit('search', this.queryParams)
    },

    // 重置
    handleReset() {
      this.queryParams = {
        keyword: undefined,
        status: undefined
      }
      this.$emit('reset')
    },

    // 表格变化
    handleTableChange(pagination, filters, sorter) {
      this.$emit('change', pagination, filters, sorter)
    },

    // 查看详情
    handleView(record) {
      this.$emit('view', record)
    },

    // 查看通道
    handleViewChannels(record) {
      this.$emit('viewChannels', record)
    },

    // 行选择变化
    onSelectChange(selectedRowKeys, selectedRows) {
      this.$emit('update:selectedRowKeys', selectedRowKeys)
      this.$emit('selectionChange', selectedRowKeys, selectedRows)
    },

    // 单行选择
    onSelect(record, selected, selectedRows) {
      this.$emit('select', record, selected, selectedRows)
    },

    // 全选
    onSelectAll(selected, selectedRows, changeRows) {
      this.$emit('selectAll', selected, selectedRows, changeRows)
    }
  }
}
</script>

<style lang="less" scoped>
.device-table-wrapper {
  .table-page-search-wrapper {
    margin-bottom: 16px;
  }

  .table-page-search-submitButtons {
    display: inline-block;
    white-space: nowrap;
  }
}
</style>
