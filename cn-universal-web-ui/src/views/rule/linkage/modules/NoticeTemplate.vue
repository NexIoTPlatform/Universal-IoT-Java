<template>
  <div>
    <a-modal
      title="选择通知模板"
      :visible="visible"
      @ok="handleOk"
      @cancel="handleCancel"
      width="800px"
    >
      <a-card :bordered="false">
        <!-- 搜索区域 -->
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('template.name')">
                  <a-input v-model="queryParam.name" placeholder="请输入模板名称" allow-clear/>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('notice.channel')">
                  <a-select v-model="queryParam.channelType" placeholder="请选择通知渠道"
                            allow-clear style="width: 100%;">
                    <a-select-option value="dingTalk">钉钉</a-select-option>
                    <a-select-option value="email">邮箱</a-select-option>
                    <a-select-option value="feishu">飞书</a-select-option>
                    <a-select-option value="sms_ali">阿里云短信</a-select-option>
                    <a-select-option value="sms_tencent">腾讯云短信</a-select-option>
                    <a-select-option value="webhook">webhook</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item>
                  <a-button type="primary" @click="handleQuery">
                    <a-icon type="search"/>
                    {{ $t('button.search') }} </a-button>
                  <a-button style="margin-left: 8px" @click="resetQuery">
                    <a-icon type="reload"/>
                    重置
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 模板列表 -->
        <a-table
          :loading="loading"
          :columns="columns"
          :data-source="list"
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
          :pagination="false"
          rowKey="id"
          size="middle"
        >
          <span slot="channelType" slot-scope="text, record">
            <a-tag :color="getChannelTypeColor(record.channelType)">
              {{ getChannelTypeLabel(record.channelType) }}
            </a-tag>
          </span>
          <span slot="status" slot-scope="text, record">
            <a-tag :color="record.status === '1' ? 'green' : 'red'">
              {{ record.status === '1' ? '启用' : '停用' }}
            </a-tag>
          </span>
          <span slot="content" slot-scope="text, record">
            <a-tooltip :title="record.content" placement="top">
              <div class="content-preview">{{ formatContent(record.content) }}</div>
            </a-tooltip>
          </span>
        </a-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <a-pagination
            size="small"
            show-size-changer
            show-quick-jumper
            :current="queryParam.pageNum"
            :total="total"
            :page-size="queryParam.pageSize"
            :showTotal="total => `共 ${total} 条`"
            @showSizeChange="onShowSizeChange"
            @change="changeSize"
          />
        </div>
      </a-card>
    </a-modal>
  </div>
</template>

<script>
import {listNoticeTemplate} from '@/api/system/noticeManager'

export default {
  name: 'NoticeTemplate',
  data() {
    return {
      visible: false,
      loading: false,
      list: [],
      total: 0,
      selectedRowKeys: [],
      selectedRows: [],
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      },
      columns: [
        {
          title: this.$t('notice.name'),
          dataIndex: 'name',
          width: 150,
          ellipsis: true
        },
        {
          title: this.$t('notice.channel'),
          dataIndex: 'channelType',
          width: 100,
          scopedSlots: {customRender: 'channelType'}
        },
        {
          title: this.$t('notice.content'),
          dataIndex: 'content',
          scopedSlots: {customRender: 'content'}
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          width: 80,
          scopedSlots: {customRender: 'status'}
        }
      ]
    }
  },
  methods: {
    openModal() {
      this.visible = true
      this.getList()
    },

    handleOk() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择一个通知模板')
        return
      }

      const selectedTemplate = this.selectedRows[0]
      this.$emit('getNoticeTemplateInfo', selectedTemplate)
      this.visible = false
      this.reset()
    },

    handleCancel() {
      this.visible = false
      this.reset()
    },

    reset() {
      this.selectedRowKeys = []
      this.selectedRows = []
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      }
    },

    getList() {
      this.loading = true
      listNoticeTemplate(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },

    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },

    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      }
      this.handleQuery()
    },

    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
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

    getChannelTypeLabel(type) {
      const typeMap = {
        'dingTalk': '钉钉',
        'email': '邮箱',
        'feishu': '飞书',
        'sms_ali': '阿里云短信',
        'sms_tencent': '腾讯云短信',
        'webhook': 'webhook'
      }
      return typeMap[type] || type
    },

    getChannelTypeColor(type) {
      const colorMap = {
        'dingTalk': 'blue',
        'email': 'green',
        'feishu': 'purple',
        'sms_ali': 'orange',
        'sms_tencent': 'cyan',
        'webhook': 'blue'
      }
      return colorMap[type] || 'default'
    },

    formatContent(content) {
      if (!content) {
        return '无'
      }
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    }
  }
}
</script>

<style lang="less" scoped>
.content-preview {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-wrapper {
  margin-top: 16px;
  text-align: center;
}
</style> 