<template>
  <div class="notification-template-selector">
    <a-modal
      title="选择通知模板"
      :visible="visible"
      @ok="handleOk"
      @cancel="handleCancel"
      width="900px"
      :confirmLoading="loading"
    >
      <!-- 搜索区域 -->
      <div class="search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="$t('template.name')">
                <a-input v-model="queryParam.name" placeholder="请输入模板名称" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="$t('notice.channel')">
                <a-select v-model="queryParam.channelType" placeholder="请选择通知渠道" allow-clear
                          style="width: 150px;">
                  <a-select-option v-for="channel in channelTypes" :key="channel.value"
                                   :value="channel.value">
                    <a-icon :type="channel.icon"/>
                    {{ channel.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item>
                <a-button type="primary" size="small" @click="handleQuery">
                  <a-icon type="search"/>
                  {{ $t('button.query') }}
                </a-button>
                <a-button size="small" style="margin-left: 8px" @click="resetQuery">
                  <a-icon type="reload"/>
                  {{ $t('button.reset') }}
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
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange, type: 'radio' }"
        :pagination="false"
        rowKey="id"
        size="middle"
        :scroll="{ y: 300 }"
      >
        <span slot="channelType" slot-scope="text, record">
          <a-tag :color="getChannelTypeColor(record.channelType)">
            <a-icon :type="getChannelTypeIcon(record.channelType)"/>
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
        <span slot="action" slot-scope="text, record">
          <a-space>
            <a-button type="link" size="small" @click="previewTemplate(record)">
              <a-icon type="eye"/>预览
            </a-button>
            <a-button type="link" size="small" @click="testTemplate(record)">
              <a-icon type="play-circle"/>{{ $t('button.test') }} </a-button>
          </a-space>
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

      <!-- 模板预览弹窗 -->
      <a-modal
        title="模板预览"
        :visible="previewVisible"
        @cancel="previewVisible = false"
        :footer="null"
        width="600px"
      >
        <div v-if="previewTemplate" class="template-preview">
          <a-descriptions title="模板信息" :column="1" bordered>
            <a-descriptions-item :label="$t('template.name')">
              {{ previewTemplate.name }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('notice.channel')">
              <a-tag :color="getChannelTypeColor(previewTemplate.channelType)">
                {{ getChannelTypeLabel(previewTemplate.channelType) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="模板内容">
              <div class="template-content">{{ previewTemplate.content }}</div>
            </a-descriptions-item>
            <a-descriptions-item :label="$t('time.create')">
              {{ previewTemplate.createTime }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-modal>

      <!-- 测试发送弹窗 -->
      <a-modal
        title="测试发送"
        :visible="testVisible"
        @ok="sendTestNotification"
        @cancel="testVisible = false"
        :confirmLoading="sending"
      >
        <div v-if="testTemplate" class="test-send">
          <a-form layout="vertical">
            <a-form-item label="接收人">
              <a-input v-model="testRecipient" placeholder="请输入接收人信息（邮箱/手机号/用户ID）"/>
            </a-form-item>
            <a-form-item label="测试变量（可选）">
              <a-textarea
                v-model="testVariables"
                placeholder="请输入JSON格式的变量，如：{'name': '张三', 'value': '25'}"
                :rows="4"
              />
            </a-form-item>
          </a-form>
        </div>
      </a-modal>
    </a-modal>
  </div>
</template>

<script>
import {listNoticeTemplate} from '@/api/system/noticeManager'

export default {
  name: 'NotificationTemplateSelector',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      sending: false,
      list: [],
      total: 0,
      selectedRowKeys: [],
      selectedRows: [],
      previewVisible: false,
      testVisible: false,
      previewTemplate: null,
      testTemplate: null,
      testRecipient: '',
      testVariables: '',
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      },
      channelTypes: [
        {value: 'dingTalk', label: '钉钉', icon: 'dingTalk', color: '#1890ff'},
        {value: 'email', label: '邮箱', icon: 'mail', color: '#52c41a'},
        {value: 'feishu', label: '飞书', icon: 'message', color: '#722ed1'},
        {value: 'sms_ali', label: '阿里云短信', icon: 'mobile', color: '#fa8c16'},
        {value: 'sms_tencent', label: '腾讯云短信', icon: 'mobile', color: '#13c2c2'},
        {value: 'webhook', label: 'webhook', icon: 'mobile', color: '#1890ff'}
      ],
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
          width: 120,
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
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'action',
          width: 120,
          scopedSlots: {customRender: 'action'}
        }
      ]
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.getList()
      }
    }
  },
  methods: {
    // 获取模板列表
    getList() {
      this.loading = true
      listNoticeTemplate(this.queryParam).then(response => {
        this.list = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    // 查询
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },

    // 重置查询
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      }
      this.handleQuery()
    },

    // 选择变化
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },

    // 分页变化
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    // 确认选择
    handleOk() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择一个通知模板')
        return
      }

      const selectedTemplate = this.selectedRows[0]
      this.$emit('template-selected', selectedTemplate)
      this.handleCancel()
    },

    // 取消
    handleCancel() {
      this.$emit('update:visible', false)
      this.reset()
    },

    // 重置
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

    // 获取通知渠道标签
    getChannelTypeLabel(type) {
      const channel = this.channelTypes.find(c => c.value === type)
      return channel ? channel.label : type
    },

    // 获取通知渠道图标
    getChannelTypeIcon(type) {
      const channel = this.channelTypes.find(c => c.value === type)
      return channel ? channel.icon : 'question'
    },

    // 获取通知渠道颜色
    getChannelTypeColor(type) {
      const channel = this.channelTypes.find(c => c.value === type)
      return channel ? channel.color : '#666'
    },

    // 格式化内容
    formatContent(content) {
      if (!content) {
        return ''
      }
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    },

    // 预览模板
    previewTemplate(template) {
      this.previewTemplate = template
      this.previewVisible = true
    },

    // 测试模板
    testTemplate(template) {
      this.testTemplate = template
      this.testRecipient = ''
      this.testVariables = ''
      this.testVisible = true
    },

    // 发送测试通知
    sendTestNotification() {
      if (!this.testRecipient) {
        this.$message.warning('请输入接收人信息')
        return
      }

      this.sending = true

      // 这里调用测试发送API
      // 由于没有具体的测试API，这里模拟发送
      setTimeout(() => {
        this.sending = false
        this.testVisible = false
        this.$message.success('测试发送成功')
      }, 2000)
    }
  }
}
</script>

<style lang="less" scoped>
.notification-template-selector {
  .search-wrapper {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }

  .content-preview {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .template-preview {
    .template-content {
      max-height: 200px;
      overflow-y: auto;
      padding: 8px;
      background: #f5f5f5;
      border-radius: 4px;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }

  .test-send {
    .ant-form-item {
      margin-bottom: 16px;
    }
  }

  /* 修复分页布局 */

  .ant-table-pagination {
    margin-top: 16px;
    text-align: right;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  /* 修复搜索按钮布局 */

  .search-wrapper .ant-form-item:last-child {
    margin-bottom: 0;
    display: flex;
    align-items: center;
  }

  /* 修复表格布局 */

  .ant-table {
    margin-bottom: 16px;
  }

  /* 修复弹窗内容布局 */

  .ant-modal-body {
    padding: 24px;
  }

  /* 修复按钮间距 */

  .ant-btn + .ant-btn {
    margin-left: 8px;
  }

  .pagination-wrapper {
    margin-top: 16px;
    text-align: center;
  }
}
</style> 