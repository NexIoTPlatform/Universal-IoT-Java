<template>
  <div class="notice-template-container">
    <!-- 统计信息 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-content">
              <div class="stat-icon"
                   style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <a-icon type="file-text"/>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ total }}</div>
                <div class="stat-label">{{ $t('template.total') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-content">
              <div class="stat-icon"
                   style="background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);">
                <a-icon type="check-circle"/>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ enabledCount }}</div>
                <div class="stat-label">{{ $t('template.enabled') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-content">
              <div class="stat-icon"
                   style="background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);">
                <a-icon type="stop"/>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ disabledCount }}</div>
                <div class="stat-label">{{ $t('template.disabled') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-content">
              <div class="stat-icon"
                   style="background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);">
                <a-icon type="message"/>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ channelTypeCount }}</div>
                <div class="stat-label">{{ $t('notice.channel') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和操作区域 -->
    <a-card class="search-action-card" :bordered="false">
      <div class="search-action-content">
        <div class="search-section">
          <a-form layout="inline" :model="queryParam">
            <a-form-item :label="$t('template.name')">
              <a-input v-model="queryParam.name" placeholder="请输入模板名称" allow-clear
                       style="width: 200px;"/>
            </a-form-item>
            <a-form-item :label="$t('notice.channel')">
              <a-select v-model="queryParam.channelType" placeholder="请选择通知渠道" allow-clear
                        style="width: 180px;">
                <a-select-option v-for="option in channelTypeOptions" :key="option.dictValue"
                                 :value="option.dictValue">
                  <img v-if="getChannelLogo(option.dictValue)"
                       :src="getChannelLogo(option.dictValue)"
                       :alt="option.dictLabel"
                       style="width: 16px; height: 16px; margin-right: 8px; vertical-align: middle;"/>
                  <a-icon v-else :type="getChannelTypeIcon(option.dictValue)"
                          style="margin-right: 8px;"/>
                  {{ option.dictLabel }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleQuery">
                  <a-icon type="search"/>
                 {{ $t('button.query') }} </a-button>
                <a-button @click="resetQuery">
                  <a-icon type="reload"/>
                 {{ $t('button.reset') }} </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
        <div class="action-section">
          <a-space size="small">
            <a-button type="primary" size="small" @click="handleAdd"
                      v-hasPermi="['notice:template:add']">
              <a-icon type="plus"/>
              {{ $t('button.add') }}
            </a-button>
            <a-button size="small" @click="handleUpdate" :disabled="single"
                      v-hasPermi="['notice:template:edit']">
              <a-icon type="edit"/>
              {{ $t('button.edit') }} </a-button>
            <a-button size="small" @click="handleBatchDelete" :disabled="multiple" danger
                      v-hasPermi="['notice:template:del']">
              <a-icon type="delete"/>
              {{ $t('button.delete') }} </a-button>
            <a-button size="small" @click="handleBatchEnable" :disabled="multiple"
                      v-hasPermi="['notice:template:edit']">
              <a-icon type="check-circle"/>
              {{ $t('status.enable') }}
            </a-button>
            <a-button size="small" @click="handleBatchDisable" :disabled="multiple"
                      v-hasPermi="['notice:template:edit']">
              <a-icon type="stop"/>
              {{ $t('status.disable') }}
            </a-button>
            <a-divider type="vertical"/>
            <a-button size="small" @click="handleExport" :disabled="!list.length">
              <a-icon type="export"/>
              {{ $t('button.export') }}
            </a-button>
            <a-button size="small" @click="getList" :loading="loading">
              <a-icon type="reload"/>
              {{ $t('button.refresh') }}
            </a-button>
            <a-button size="small" @click="handleColumnSettings">
              <a-icon type="setting"/>
              {{ $t('system.settings') }}
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- 模板卡片列表 -->
    <a-card class="content-card" :bordered="false" :loading="loading">
      <div v-if="list.length === 0" class="empty-state">
        <a-empty description="暂无通知模板数据"/>
      </div>

      <div v-else class="template-table">
        <a-table :columns="columns" :data-source="list" :row-selection="rowSelection"
                 :pagination="false"
                 :row-key="record => record.id" :scroll="{ x: 1200 }" size="middle" bordered
                 v-hasPermi="['notice:template:list']">
          <template slot="channel" slot-scope="text, record">
            <div class="channel-option">
              <img :src="getChannelLogo(record.channelType)"
                   :alt="getChannelTypeLabel(record.channelType)"
                   class="channel-icon"/>
              <span class="value" style="margin-left: 8px;">{{
                  getChannelName(record.channelId)
                }}</span>
            </div>
          </template>

          <template slot="content" slot-scope="text, record">
            <div class="content-cell">
              <a-tooltip :title="record.content" placement="top">
                <div class="content-preview">{{ formatContent(record.content) }}</div>
              </a-tooltip>
              <span v-if="getVariables(record.content).length > 0" class="param-count-badge">
                +{{ getVariables(record.content).length }}
              </span>
            </div>
          </template>


          <template slot="status" slot-scope="text">
            <a-badge :status="text === '1' ? 'success' : 'error'"
                     :text="text === '1' ? '启用' : '停用'"/>
          </template>

          <template slot="createTime" slot-scope="text">
            {{ parseTime(text) }}
          </template>

          <template slot="operation" slot-scope="text, record">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleDetail(record)"
                        v-hasPermi="['notice:template:detail']">
                <a-icon type="eye"/>
              </a-button>
              <a-button type="link" size="small" @click="handleTest(record)">
                <a-icon type="play-circle"/>
              </a-button>
              <a-button type="link" size="small" @click="handleUpdate(record)"
                        v-hasPermi="['notice:template:edit']">
                <a-icon type="edit"/>
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)"
                        v-hasPermi="['notice:template:del']">
                <a-icon type="delete"/>
              </a-button>
            </a-space>
          </template>
        </a-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <a-pagination show-size-changer show-quick-jumper :current="queryParam.pageNum"
                      :total="total"
                      :page-size="queryParam.pageSize" :showTotal="total => `共 ${total} 条`"
                      @showSizeChange="onShowSizeChange"
                      @change="changeSize"/>
      </div>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <NoticeTemplateForm :visible="formVisible" :title="formTitle" :data="formData"
                        @submit="handleFormSubmit"
                        @cancel="handleFormCancel"/>

    <!-- 详情弹窗 -->
    <a-modal v-if="detailVisible" :visible="detailVisible" title="通知模板详情"
             @cancel="detailVisible = false" :footer="null"
             width="900px" class="detail-modal">
      <div class="detail-content">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">
            <a-icon type="info-circle"/>
            基本信息
          </div>
          <a-row :gutter="24">
            <a-col :span="8">
              <div class="info-item">
                <span class="label">模板名称：</span>
                <span class="value">{{ currentItem.name }}</span>
              </div>
            </a-col>
            <a-col :span="16">
              <div class="info-item">
                <span class="label">渠道：</span>
                <a-tag :color="getChannelTypeColor(currentItem.channelType)">
                  <a-icon :type="getChannelTypeIcon(currentItem.channelType)"/>
                  {{ getChannelTypeLabel(currentItem.channelType) }}
                </a-tag>
                <span class="value" style="margin-left: 8px;">{{
                    getChannelName(currentItem.channelId)
                  }}</span>
              </div>
            </a-col>
          </a-row>
          <a-row :gutter="24" style="margin-top: 16px;">
            <a-col :span="8">
              <div class="info-item">
                <span class="label">状态：</span>
                <a-badge :status="currentItem.status === '1' ? 'success' : 'error'"
                         :text="currentItem.status === '1' ? '启用' : '停用'"/>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="info-item">
                <span class="label">{{ $t('app.creator') }}：</span>
                <span class="value">{{ currentItem.creator || '系统' }}</span>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="info-item">
                <span class="label">{{ $t('app.createTime') }}：</span>
                <span class="value">{{ parseTime(currentItem.createTime) }}</span>
              </div>
            </a-col>
          </a-row>
        </div>

        <!-- 模板内容 -->
        <div class="detail-section">
          <div class="section-title">
            <a-icon type="file-text"/>
            模板内容
          </div>
          <div class="content-detail">
            <div class="content-text">{{ currentItem.content }}</div>
            <div class="content-variables" v-if="getVariables(currentItem.content).length > 0">
              <div class="variables-title">变量列表：</div>
              <a-tag v-for="variable in getVariables(currentItem.content)" :key="variable"
                     color="blue"
                     style="margin: 4px 8px 4px 0;">
                {{ variable }}
              </a-tag>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div class="detail-section" v-if="currentItem.remark">
          <div class="section-title">
            <a-icon type="edit"/>
            备注信息
          </div>
          <div class="remark-content">
            {{ currentItem.remark }}
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 场景联动弹窗 -->
    <a-modal v-if="linkageVisible" :visible="linkageVisible" title="通知模板场景联动"
             @cancel="linkageVisible = false"
             :footer="null" width="80%" class="linkage-modal">
      <NoticeTemplateLinkage :template="currentItem" @close="linkageVisible = false"/>
    </a-modal>

    <!-- 测试发送弹窗 -->
    <a-modal v-if="testVisible" :visible="testVisible" title="测试发送" @ok="handleTestSend"
             @cancel="testVisible = false"
             :confirmLoading="testLoading" width="700px" class="test-modal">
      <div class="test-content">
        <!-- 模板信息 -->
        <div class="template-info">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="info-item">
                <span class="label">模板名称：</span>
                <span class="value">{{ currentItem.name }}</span>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="info-item">
                <span class="label">{{ $t('notice.channel') }}：</span>
                <a-tag :color="getChannelTypeColor(currentItem.channelType)">
                  <a-icon :type="getChannelTypeIcon(currentItem.channelType)"/>
                  {{ getChannelTypeLabel(currentItem.channelType) }}
                </a-tag>
              </div>
            </a-col>
          </a-row>
        </div>

        <!-- 原始模板内容 -->
        <div class="template-section">
          <div class="section-title">
            <a-icon type="file-text"/>
            原始模板内容
          </div>
          <div class="original-content">{{ currentItem.content }}</div>
        </div>

        <!-- 变量参数配置 -->
        <div class="variables-section" v-if="getVariables(currentItem.content).length > 0">
          <div class="section-title">
            <a-icon type="setting"/>
            变量参数配置
            <a-tag color="blue" style="margin-left: 8px;">
              {{ getVariables(currentItem.content).length }} 个变量
            </a-tag>
          </div>
          <a-row :gutter="16">
            <a-col :span="12" v-for="variable in getVariables(currentItem.content)" :key="variable">
              <a-form-item :label="variable" style="margin-bottom: 12px;">
                <a-input v-model="testForm.params[variable]" :placeholder="`请输入${variable}的值`"
                         @input="updatePreview"/>
              </a-form-item>
            </a-col>
          </a-row>
        </div>

        <!-- 预览效果 -->
        <div class="preview-section" v-if="getVariables(currentItem.content).length > 0">
          <div class="section-title">
            <a-icon type="eye"/>
            预览效果
          </div>
          <div class="preview-content">{{ previewContent }}</div>
        </div>

        <!-- 测试收件人 -->
        <div class="receivers-section">
          <div class="section-title">
            <a-icon type="user"/>
            测试收件人
          </div>
          <a-form-item>
            <a-input v-model="testForm.receivers" placeholder="请输入测试收件人，多个用逗号分隔"
                     size="large"/>
          </a-form-item>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import NoticeTemplateForm from './NoticeTemplateForm.vue'
import NoticeTemplateLinkage from './NoticeTemplateLinkage.vue'
import {getDictMap} from '@/api/system/dict/data'
import {parseTime} from '@/utils/ruoyi'
import {
  deleteNoticeTemplate,
  disableNoticeTemplate,
  enableNoticeTemplate,
  getNoticeTemplate,
  listNoticeChannel,
  listNoticeTemplate,
  saveNoticeTemplate,
  testNoticeTemplate
} from '@/api/system/noticeManager'
import {getChannelLogo} from '@/assets/icons/channel-logos'

export default {
  name: 'NoticeTemplate',
  components: {
    NoticeTemplateForm,
    NoticeTemplateLinkage
  },
  data() {
    return {
      // 列表数据
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      loading: false,
      total: 0,
      // 查询参数
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      },
      // 字典数据
      channelTypeOptions: [],
      channelList: [],
      // 表格配置
      tableSize: 'middle',
      columns: [
        {
          title: this.$t('notice.name'),
          dataIndex: 'name',
          width: '18%',
          ellipsis: true,
          align: 'left',
          fixed: 'left'
        },
        {
          title: this.$t('notice.channel'),
          dataIndex: 'channel',
          width: '15%',
          scopedSlots: {customRender: 'channel'},
          align: 'left'
        },
        {
          title: this.$t('notice.content'),
          dataIndex: 'content',
          width: '32%',
          scopedSlots: {customRender: 'content'},
          align: 'left'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          width: '8%',
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          width: '17%',
          scopedSlots: {customRender: 'createTime'},
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '10%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      // 弹窗控制
      formVisible: false,
      formTitle: '新增通知模板',
      formData: {},
      detailVisible: false,
      testVisible: false,
      linkageVisible: false,
      currentItem: {},

      // 测试表单
      testForm: {
        receivers: '',
        params: {}
      },
      testLoading: false,
      previewContent: '',

      // 选择控制
      selectedRowKeys: [],
      selectedRows: [],
      single: true,
      multiple: true,
      ids: []
    }
  },
  computed: {
    rowSelection() {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange,
        getCheckboxProps: record => ({
          props: {
            disabled: record.name === 'Disabled User', // Column configuration not to be checked
            name: record.name,
          },
        }),
      }
    },

    // 统计信息计算
    enabledCount() {
      return this.list.filter(item => item.status === '1').length
    },

    disabledCount() {
      return this.list.filter(item => item.status === '0').length
    },

    channelTypeCount() {
      return new Set(this.list.map(item => item.channelType)).size
    }
  },
  created() {
    this.getList()
    this.getDictData()
    this.getChannelList()
  },
  methods: {
    parseTime,

    /** 获取字典数据 */
    getDictData() {
      getDictMap('notice_channel_type').then(response => {
        // 确保返回的是数组
        if (Array.isArray(response.data)) {
          this.channelTypeOptions = response.data
        } else if (response.data && Array.isArray(response.data.notice_channel_type)) {
          this.channelTypeOptions = response.data.notice_channel_type
        } else {
          // 如果数据格式不正确，使用默认数据
          this.channelTypeOptions = [
            {dictValue: 'dingTalk', dictLabel: '钉钉机器人'},
            {dictValue: 'email', dictLabel: '邮件'},
            {dictValue: 'feishu', dictLabel: '飞书机器人'},
            {dictValue: 'sms_ali', dictLabel: '阿里云短信'},
            {dictValue: 'sms_tencent', dictLabel: '腾讯云短信'},
            {dictValue: 'audio_ali', dictLabel: '阿里云语音'},
            {dictValue: 'audio_tencent', dictLabel: '腾讯云语音'},
            {dictValue: 'webhook', dictLabel: 'webhook'}
          ]
        }
      }).catch(() => {
        // 如果字典获取失败，使用默认数据
        this.channelTypeOptions = [
          {dictValue: 'dingTalk', dictLabel: '钉钉机器人'},
          {dictValue: 'email', dictLabel: '邮件'},
          {dictValue: 'feishu', dictLabel: '飞书机器人'},
          {dictValue: 'sms_ali', dictLabel: '阿里云短信'},
          {dictValue: 'sms_tencent', dictLabel: '腾讯云短信'},
          {dictValue: 'audio_ali', dictLabel: '阿里云语音'},
          {dictValue: 'audio_tencent', dictLabel: '腾讯云语音'},
          {dictValue: 'webhook', dictLabel: 'webhook'}
        ]
      })
    },

    /** 获取渠道列表 */
    getChannelList() {
      listNoticeChannel({pageSize: 1000}).then(response => {
        // 修复数据结构访问
        this.channelList = response.rows || response.data?.rows || []
      })
    },

    /** 获取列表数据 */
    getList() {
      this.loading = true
      console.log('开始获取通知模板列表，查询参数:', this.queryParam)
      listNoticeTemplate(this.queryParam).then(response => {
        console.log('通知模板列表响应:', response)
        // 修复数据结构访问
        this.list = response.rows || response.data?.rows || []
        this.total = response.total || response.data?.total || 0
        console.log('设置模板列表数据:', this.list)
        console.log('设置模板总数:', this.total)
        this.loading = false
      }).catch((error) => {
        console.error('获取通知模板列表失败:', error)
        this.loading = false
      })
    },

    /** 查询按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        channelType: undefined
      }
      this.handleQuery()
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.formData = null
      this.formTitle = '新增通知模板'
      this.formVisible = true
    },

    /** 表单提交处理 */
    async handleFormSubmit(formData) {
      try {
        await saveNoticeTemplate(formData)
        this.$message.success(formData.id ? '修改成功' : '新增成功')
        this.formVisible = false
        this.getList()
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败')
      }
    },

    /** 表单取消处理 */
    handleFormCancel() {
      this.formVisible = false
    },

    /** 批量删除 */
    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的模板')
        return
      }

      this.$confirm({
        title: '确认批量删除',
        content: `确定要删除选中的 ${this.selectedRows.length} 个模板吗？`,
        onOk: () => {
          const ids = this.selectedRows.map(item => item.id)
          return deleteNoticeTemplate(ids.join(','))
            .then(() => {
              this.$message.success('批量删除成功')
              this.onSelectChange([], [])
              this.getList()
            })
        }
      })
    },

    /** 批量启用 */
    handleBatchEnable() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要启用的模板')
        return
      }
      this.$confirm({
        title: '确认批量启用',
        content: `确定要启用选中的 ${this.selectedRows.length} 个模板吗？`,
        onOk: () => {
          const ids = this.selectedRows.map(item => item.id)
          return enableNoticeTemplate(ids)
            .then(() => {
              this.$message.success('批量启用成功')
              this.getList()
            })
        }
      })
    },

    /** 批量停用 */
    handleBatchDisable() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要停用的模板')
        return
      }
      this.$confirm({
        title: '确认批量停用',
        content: `确定要停用选中的 ${this.selectedRows.length} 个模板吗？`,
        onOk: () => {
          const ids = this.selectedRows.map(item => item.id)
          return disableNoticeTemplate(ids)
            .then(() => {
              this.$message.success('批量停用成功')
              this.getList()
            })
        }
      })
    },

    /** 导出数据 */
    handleExport() {
      this.$message.info('导出功能开发中...')
    },

    /** 列设置 */
    handleColumnSettings() {
      this.$message.info('列设置功能开发中...')
    },

    /** 修改按钮操作 */
    async handleUpdate(row) {
      console.log('handleUpdate被调用，参数:', row)
      console.log('参数类型:', typeof row)
      console.log('参数是否为事件对象:', row instanceof Event)

      let templateId
      if (row && !(row instanceof Event) && row.id) {
        // 从行操作按钮点击，确保是行数据而不是事件对象
        templateId = row.id
        console.log('使用行数据ID:', templateId)
      } else if (this.selectedRows.length === 1) {
        // 从顶部按钮点击，使用选中的行
        templateId = this.selectedRows[0].id
        console.log('使用选中行数据ID:', templateId)
      } else {
        this.$message.warning('请先选择一个模板进行修改')
        return
      }

      try {
        // 从后端获取最新的模板数据
        console.log('正在获取模板最新数据，ID:', templateId)
        const response = await getNoticeTemplate(templateId)
        
        if (response && response.code === 0) {
          this.formData = {...response.data}
          console.log('获取到最新模板数据:', this.formData)
        } else {
          this.$message.error('获取模板数据失败')
          return
        }
      } catch (error) {
        console.error('获取模板数据失败:', error)
        this.$message.error('获取模板数据失败: ' + (error.message || '未知错误'))
        return
      }

      this.formTitle = '编辑通知模板'
      this.formVisible = true

      // 确保数据传递到子组件
      this.$nextTick(() => {
        console.log('表单数据已设置:', this.formData)
      })
    },

    /** 查看详情 */
    handleDetail(row) {
      this.currentItem = row
      this.detailVisible = true
    },

    /** 测试发送 */
    handleTest(row) {
      this.currentItem = row
      this.testForm = {
        receivers: '',
        params: {}
      }
      // 初始化变量参数
      const variables = this.getVariables(row.content)
      variables.forEach(variable => {
        this.testForm.params[variable] = ''
      })
      this.previewContent = row.content
      this.testVisible = true
    },

    /** 场景联动 */
    handleLinkage(row) {
      this.currentItem = row
      this.linkageVisible = true
    },

    /** 更新预览内容 */
    updatePreview() {
      let content = this.currentItem.content
      Object.keys(this.testForm.params).forEach(variable => {
        const value = this.testForm.params[variable] || `#{${variable}}`
        // 替换 #{variable} 和 ${variable} 两种格式
        content = content.replace(new RegExp(`#\\{${variable}\\}`, 'g'), value)
        content = content.replace(new RegExp(`\\$\\{${variable}\\}`, 'g'), value)
      })
      this.previewContent = content
    },

    /** 执行测试发送 */
    async handleTestSend() {
      if (!this.testForm.receivers) {
        this.$message.error('请输入测试收件人')
        return
      }

      this.testLoading = true
      try {
        await testNoticeTemplate({
          templateId: this.currentItem.id,
          receivers: this.testForm.receivers,
          params: this.testForm.params
        })

        this.$message.success('测试发送成功')
        this.testVisible = false
      } catch (error) {
        this.$message.error('测试发送失败')
      } finally {
        this.testLoading = false
      }
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const templateIds = row?.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: `当前选中编号为${templateIds}的数据`,
        onOk: () => {
          return deleteNoticeTemplate(templateIds).then(() => {
            this.onSelectChange([], [])
            this.getList()
            this.$message.success('删除成功')
          })
        }
      })
    },

    /** 选择项变化 */
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },

    /** 分页大小改变 */
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    /** 分页改变 */
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    /** 获取通知渠道标签 */
    getChannelTypeLabel(type) {
      const option = this.channelTypeOptions.find(item => item.dictValue === type)
      return option ? option.dictLabel : type
    },

    /** 获取通知渠道颜色 */
    getChannelTypeColor(type) {
      const colorMap = {
        'dingTalk': 'blue',
        'email': 'green',
        'feishu': 'purple',
        'sms_ali': 'orange',
        'sms_tencent': 'cyan',
        'audio_ali': 'red',
        'audio_tencent': 'magenta',
        'webhook': 'blue'
      }
      return colorMap[type] || 'default'
    },

    /** 获取通知渠道图标 */
    getChannelTypeIcon(type) {
      const iconMap = {
        'dingTalk': 'dingtalk',
        'email': 'mail',
        'feishu': 'message',
        'sms_ali': 'cloud',
        'sms_tencent': 'cloud',
        'audio_ali': 'sound',
        'audio_tencent': 'sound',
        'webhook': 'link'
      }
      return iconMap[type] || 'notification'
    },

    /** 获取通知渠道样式类 */
    getChannelTypeClass(type) {
      const classMap = {
        'dingTalk': 'dingTalk',
        'email': 'email',
        'feishu': 'feishu',
        'sms_ali': 'aliyun',
        'sms_tencent': 'tencent',
        'audio_ali': 'aliyun-audio',
        'audio_tencent': 'tencent-audio',
        'webhook': 'webhook'
      }
      return classMap[type] || 'default'
    },

    /** 获取渠道Logo */
    getChannelLogo(type) {
      const logoMap = {
        'dingTalk': require('@/assets/notice/钉钉.svg'),
        'email': require('@/assets/notice/邮箱.svg'),
        'feishu': require('@/assets/notice/飞书.svg'),
        'sms_ali': require('@/assets/notice/阿里云.svg'),
        'sms_tencent': require('@/assets/notice/腾讯云.svg'),
        'audio_ali': require('@/assets/notice/阿里云.svg'),
        'audio_tencent': require('@/assets/notice/腾讯云.svg'),
        'webhook': require('@/assets/notice/webhook.svg')
      }
      return logoMap[type] || require('@/assets/notice/邮箱.svg')
    },

    /** 获取渠道名称 */
    getChannelName(channelId) {
      const channel = this.channelList.find(item => item.id === channelId)
      return channel ? channel.name : '未知'
    },

    /** 格式化内容 */
    formatContent(content) {
      if (!content) {
        return '无'
      }
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    },

    /** 格式化收件人 */
    formatReceivers(receivers) {
      if (!receivers) {
        return '无'
      }
      const list = this.formatReceiversList(receivers)
      return list.length > 3 ? list.slice(0, 3).join(', ') + '...' : list.join(', ')
    },

    /** 格式化收件人列表 */
    formatReceiversList(receivers) {
      if (!receivers) {
        return []
      }
      if (Array.isArray(receivers)) {
        return receivers
      }
      if (typeof receivers === 'string') {
        try {
          return JSON.parse(receivers)
        } catch (e) {
          return receivers.split(',').map(item => item.trim())
        }
      }
      return []
    },

    /** 获取内容中的变量 */
    getVariables(content) {
      if (!content) {
        return []
      }
      // 修正：添加空格支持（根据业务需求决定是否保留 \s），修复正则笔误
      const regex = /#\{([a-zA-Z0-9_.\s]+)\}|\$\{([a-zA-Z0-9_.\s]+)\}/g;
      const variablesSet = new Set(); // 用 Set 自动去重，提升效率
      let match;
      while ((match = regex.exec(content)) !== null) {
        const variable = match[1] || match[2];
        if (variable) {
          // 可选：去除变量名前后的空格（如果业务不允许首尾空格）
          const trimmedVar = variable.trim();
          if (trimmedVar) { // 过滤空字符串（如 #{} 这种无效格式）
            variablesSet.add(trimmedVar);
          }
        }
      }
      return Array.from(variablesSet);
    },

    /** 处理卡片点击 */
    handleCardClick(item) {
      this.selectedRowKeys = [item.id]
      this.selectedRows = [item]
      this.single = true
      this.multiple = false
      this.currentItem = item
      this.detailVisible = true
    },

  }
}
</script>

<style lang="less" scoped>
.search-card {
  margin-bottom: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.table-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.table-operations {
  display: flex;
  justify-content: space-between;
  padding-bottom: 8px;
}

/deep/ .ant-table-pagination {
  padding-top: 16px;
}

.content-preview {
  .content-text {
    background: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
    margin-bottom: 8px;
    white-space: pre-wrap;
    word-wrap: break-word;
  }

  .content-variables {
    .variables-title {
      font-weight: 500;
      margin-bottom: 4px;
    }
  }
}

.receivers-list {
  .ant-tag {
    margin-bottom: 4px;
  }
}

.test-content {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.notice-template-container {
  .stats-cards {
    margin-bottom: 8px;

    .stat-card {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .stat-content {
        display: flex;
        align-items: center;
        padding: 8px 0;
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;

        .anticon {
          font-size: 24px;
          color: white;
        }
      }

      .stat-info {
        flex: 1;
      }

      .stat-number {
        font-size: 24px;
        font-weight: 600;
        color: #262626;
        line-height: 1;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }

  .search-action-card {
    margin-bottom: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 8px;

    .search-action-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;

      .search-section {
        flex: 1;
        min-width: 400px;
      }

      .action-section {
        display: flex;
        align-items: center;
        flex-wrap: nowrap;
        gap: 4px;

        .ant-btn {
          height: 28px;
          padding: 4px 8px;
          font-size: 12px;
          white-space: nowrap;
          min-width: auto;

          .anticon {
            font-size: 12px;
          }
        }
      }
    }
  }

  .content-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 8px;

    .template-table {
      padding: 20px;

      .channel-type-cell {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;

        .channel-logo {
          width: 20px;
          height: 20px;
          object-fit: contain;
          vertical-align: middle;
        }
      }

      .content-cell {
        position: relative;

        .content-preview {
          background: #f8f9fa;
          padding: 6px 8px;
          border-radius: 4px;
          font-size: 12px;
          color: #495057;
          border: 1px solid #e9ecef;
          cursor: pointer;
          word-break: break-all;
          word-wrap: break-word;
          max-height: 60px;
          overflow: hidden;
          line-height: 1.4;
        }

        .variables-preview {
          .more-variables {
            margin-left: 4px;
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }


    }

    .template-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      padding: 20px;
    }

    .template-card {
      position: relative;
      cursor: pointer;
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid #f0f0f0;
      overflow: hidden;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
        border-color: #1890ff;
      }

      &.template-card-selected {
        border-color: #1890ff;
        box-shadow: 0 8px 30px rgba(24, 144, 255, 0.15);
        background: linear-gradient(135deg, #f0f8ff 0%, #e6f7ff 100%);
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
      }

      .card-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        position: relative;
        overflow: hidden;

        &.dingtalk {
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        }

        &.email {
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        }

        &.feishu {
          background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
        }

        &.aliyun {
          background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
        }

        &.tencent {
          background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
        }

        &.aliyun-audio {
          background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);
        }

        &.tencent-audio {
          background: linear-gradient(135deg, #eb2f96 0%, #c41d7f 100%);
        }

        .channel-logo {
          width: 32px;
          height: 32px;
          object-fit: contain;
          filter: brightness(0) invert(1);
        }
      }

      .card-status {
        display: flex;
        align-items: center;
      }

      .card-content {
        flex: 1;
      }

      .card-title {
        font-size: 18px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 8px;
        line-height: 1.4;
      }

      .card-type {
        margin-bottom: 12px;
      }

      .card-text {
        margin-bottom: 16px;
        font-size: 14px;
        color: #595959;
        line-height: 1.5;

        .content-preview {
          background: #f8f9fa;
          padding: 12px;
          border-radius: 8px;
          margin-bottom: 12px;
          white-space: pre-wrap;
          word-wrap: break-word;
          border: 1px solid #e9ecef;
          font-size: 13px;
          color: #495057;
        }

        .variables-preview {
          .variables-title {
            font-weight: 500;
            margin-bottom: 6px;
            color: #1890ff;
            font-size: 13px;
          }

          .more-variables {
            margin-left: 6px;
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }

      .card-meta {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        color: #8c8c8c;
        margin-bottom: 12px;
      }

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .receivers-preview {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #595959;
        margin-bottom: 16px;
        padding: 8px 12px;
        background: #f8f9fa;
        border-radius: 6px;
        border: 1px solid #e9ecef;

        .anticon {
          margin-right: 6px;
          font-size: 16px;
          color: #1890ff;
        }
      }

      .card-actions {
        display: flex;
        justify-content: flex-end;
        padding-top: 12px;
        border-top: 1px solid #f0f0f0;
      }

      .card-glow {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: linear-gradient(90deg, #1890ff, #52c41a, #722ed1, #fa8c16);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover .card-glow {
        opacity: 1;
      }
    }

    .empty-state {
      padding: 60px 0;
      text-align: center;
    }

    .pagination-wrapper {
      padding: 20px;
      text-align: right;
      border-top: 1px solid #f0f0f0;
    }
  }
}

// 详情弹窗样式
.detail-modal {
  .detail-content {
    .detail-section {
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid #f0f0f0;

        .anticon {
          margin-right: 8px;
          color: #1890ff;
        }
      }

      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;

        .label {
          font-weight: 500;
          color: #595959;
          min-width: 80px;
          margin-right: 8px;
        }

        .value {
          color: #262626;
          flex: 1;
        }
      }

      .content-detail {
        .content-text {
          background: #f8f9fa;
          padding: 16px;
          border-radius: 8px;
          // border: 1px solid #e9ecef;
          font-size: 14px;
          color: #495057;
          line-height: 1.6;
          white-space: pre-wrap;
          word-wrap: break-word;
          margin-bottom: 16px;
        }

        .content-variables {
          .variables-title {
            font-weight: 500;
            margin-bottom: 8px;
            color: #1890ff;
            font-size: 14px;
          }
        }
      }

      .remark-content {
        background: #f8f9fa;
        padding: 16px;
        border-radius: 8px;
        border: 1px solid #e9ecef;
        font-size: 14px;
        color: #495057;
        line-height: 1.6;
      }
    }
  }
}

// 测试发送弹窗样式
.test-modal {
  .test-content {
    .template-info {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 8px;
      border: 1px solid #e9ecef;

      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          font-weight: 500;
          color: #595959;
          min-width: 80px;
          margin-right: 8px;
        }

        .value {
          color: #262626;
          flex: 1;
        }
      }
    }

    .template-section,
    .variables-section,
    .preview-section,
    .receivers-section {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        display: flex;
        align-items: center;
        font-size: 14px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 12px;
        padding-bottom: 6px;
        border-bottom: 1px solid #f0f0f0;

        .anticon {
          margin-right: 6px;
          color: #1890ff;
        }
      }

      .original-content {
        background: #f8f9fa;
        padding: 12px;
        border-radius: 6px;
        border: 1px solid #e9ecef;
        font-size: 13px;
        color: #495057;
        line-height: 1.5;
        white-space: pre-wrap;
        word-wrap: break-word;
      }

      .preview-content {
        background: #e6f7ff;
        padding: 12px;
        border-radius: 6px;
        border: 1px solid #91d5ff;
        font-size: 13px;
        color: #1890ff;
        line-height: 1.5;
        white-space: pre-wrap;
        word-wrap: break-word;
        font-weight: 500;
      }
    }
  }
}

/deep/ .ant-table {
  .ant-table-thead > tr > th {
    background: #fafafa;
    font-weight: 600;
    color: #262626;
    border-right: none;
  }

  .ant-table-tbody > tr:hover > td {
    background: #f0f8ff;
  }

  .ant-table-tbody > tr > td {
    padding: 12px 8px;
    vertical-align: top;
    border-right: none;
  }

  .ant-table-thead > tr > th:last-child,
  .ant-table-tbody > tr > td:last-child {
    border-right: none;
  }
}

.channel-option {
  display: flex;
  align-items: center;
}

.channel-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 6px;
  background: #f5f5f5;
  border: 1px solid #eee;
}

.param-count-badge {
  position: absolute;
  top: 2px;
  right: 10px;
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  border-radius: 8px;
  padding: 0 6px;
  line-height: 18px;
  height: 18px;
  font-weight: 500;
  pointer-events: none;
}
</style>