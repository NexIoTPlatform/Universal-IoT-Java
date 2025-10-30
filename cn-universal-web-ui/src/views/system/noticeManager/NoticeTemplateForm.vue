<template>
  <a-modal
    :title="title"
    :visible="visible"
    :width="700"
    @cancel="handleCancel"
    @ok="handleSubmit"
    :confirmLoading="loading"
    :maskClosable="false"
    :destroyOnClose="true"
  >
    <a-form-model
      ref="form"
      :model="form"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
      layout="horizontal"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="section-title">基本信息</div>

        <a-form-model-item :label="$t('template.name')" prop="name">
          <a-input
            v-model="form.name"
            placeholder="请输入模板名称"
            maxlength="50"
            show-count
          />
        </a-form-model-item>

        <a-form-model-item :label="$t('notice.channel')" prop="channelType">
          <a-select
            v-model="form.channelType"
            placeholder="请选择通知渠道"
            @change="handleChannelTypeChange"
          >
            <a-select-option
              v-for="type in channelTypes"
              :key="type.value"
              :value="type.value"
            >
              <div class="channel-option">
                <img :src="getChannelLogo(type.value)" :alt="type.label" class="channel-icon"/>
                <span>{{ type.label }}</span>
              </div>
            </a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item label="渠道配置" prop="channelId">
          <a-select
            v-model="form.channelId"
            placeholder="请选择渠道配置"
            :disabled="!form.channelType"
            @change="handleChannelConfigChange"
            :loading="!channelList.length"
          >
            <a-select-option
              v-for="channel in filteredChannelList"
              :key="channel.id"
              :value="channel.id"
            >
              <div class="channel-config-option">
                <img :src="getChannelLogo(channel.channelType)"
                     :alt="getChannelTypeLabel(channel.channelType)" class="channel-icon"/>
                <span>{{ channel.name }}</span>
                <a-tag :color="getChannelTypeColor(channel.channelType)" size="small">
                  {{ getChannelTypeLabel(channel.channelType) }}
                </a-tag>
              </div>
            </a-select-option>
          </a-select>
          <div v-if="form.channelId && !selectedChannelName.includes('ID:')" class="field-hint">
            <a-icon type="check-circle"/>
            已选择: {{ selectedChannelName }}
          </div>
          <div v-if="form.channelId && selectedChannelName.includes('ID:')" class="field-hint"
               style="color: #ff4d4f;">
            <a-icon type="warning"/>
            警告: 找不到ID为 {{ form.channelId }} 的渠道配置
          </div>
        </a-form-model-item>

        <a-form-model-item :label="$t('common.status')" prop="status">
          <a-radio-group v-model="form.status">
            <a-radio value="1">启用</a-radio>
            <a-radio value="0">停用</a-radio>
          </a-radio-group>
        </a-form-model-item>
      </div>

      <!-- 模板内容 -->
      <div class="form-section">
        <div class="section-title">模板内容</div>
        <a-form-model-item label="模板内容" prop="content">
          <div class="content-editor">
            <a-textarea
              v-model="form.content"
              placeholder="请输入模板内容"
              :rows="6"
              maxlength="1000"
              show-count
            />
            <div class="variable-panel">
              <!-- <div class="variable-title">
                <a-icon type="code" />
                <span>IoT参数</span>
                <a-button type="link" size="small" @click="insertVariable">
                  <a-icon type="plus" />
                  插入参数
                </a-button>
              </div> -->
              <div class="variable-list">
                <a-tag
                  v-for="variable in iotVariables"
                  :key="variable.key"
                  class="variable-tag"
                  @click="insertVariableToContent(variable.key)"
                >
                  {{ variable.key }}
                  <span class="variable-desc">{{ variable.desc }}</span>
                </a-tag>
              </div>
            </div>
          </div>
          <div class="field-hint">
            <a-icon type="info-circle"/>
            支持变量替换，使用 #{变量名} 格式，点击上方参数可直接插入
          </div>
        </a-form-model-item>
        <!-- 默认收件人-->
        <a-form-model-item label="默认接收人" prop="receivers">
          <div class="receivers-input-wrapper">
            <a-select
              v-model="form.receivers"
              mode="tags"
              placeholder="请输入手机号或邮箱，按回车或逗号分隔"
              @change="handleReceiversChange"
              :token-separators="[',']"
              style="width: 100%"
              :allow-clear="true"
            >
            </a-select>
            <div class="field-hint">
              <a-icon type="info-circle"/>
              注意：如果填写，所有使用此模版的都会收到消息
            </div>
          </div>
        </a-form-model-item>

        <a-form-model-item :label="$t('common.remark')" prop="remark">
          <a-textarea
            v-model="form.remark"
            placeholder="请输入备注信息"
            :rows="3"
            maxlength="200"
            show-count
          />
        </a-form-model-item>
      </div>
    </a-form-model>
  </a-modal>
</template>

<script>
import {channelTypes} from './channelConfig'
import {listNoticeChannel} from '@/api/system/noticeManager'

export default {
  name: 'NoticeTemplateForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '新增通知模板'
    },
    data: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      form: {
        name: '',
        channelType: '',
        channelId: '',
        title: '',
        content: '',
        status: '0',
        remark: '',
        receivers: []
      },
      channelTypes,
      channelList: [],
      iotVariables: [
        {key: '#{productKey}', desc: '产品标识'},
        {key: '#{deviceId}', desc: '设备ID'},
        {key: '#{deviceName}', desc: '设备名称'},
        {key: '#{messageType}', desc: '消息类型'},
        {key: '#{time}', desc: '毫秒时间戳'},
        {key: '#{properties.xxx}', desc: '某个属性'},
        {key: '#{event}', desc: '事件标识'},
        {key: '#{eventName}', desc: '事件名称'}
      ],
      rules: {
        name: [
          {required: true, message: '请输入模板名称', trigger: 'blur'},
          {min: 2, max: 50, message: '模板名称长度为2-50个字符', trigger: 'blur'}
        ],
        channelType: [
          {required: true, message: '请选择通知渠道', trigger: 'change'}
        ],
        channelId: [
          {required: true, message: '请选择渠道配置', trigger: 'change'}
        ],
        title: [
          {required: true, message: '请输入模板标题', trigger: 'blur'},
          {max: 100, message: '模板标题不能超过100个字符', trigger: 'blur'}
        ],
        content: [
          {required: true, message: '请输入模板内容', trigger: 'blur'},
          {max: 1000, message: '模板内容不能超过1000个字符', trigger: 'blur'}
        ],
        receivers: [
          {type: 'array', message: '请至少添加一个默认接收人', trigger: 'change'}
        ]
      }
    }
  },
  computed: {
    filteredChannelList() {
      if (!this.form.channelType) {
        return []
      }
      return this.channelList.filter(channel => channel.channelType === this.form.channelType)
    },

    // 获取当前选中的渠道配置名称
    selectedChannelName() {
      if (!this.form.channelId) {
        return ''
      }
      const channel = this.channelList.find(ch => ch.id === this.form.channelId)
      return channel ? channel.name : `ID: ${this.form.channelId}`
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initForm()
        this.getChannelList()
      }
    },
    data: {
      handler(newVal) {
        console.log('表单数据变化:', newVal)
        if (newVal && this.visible) {
          this.initForm()
        }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    // 获取渠道Logo
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

    // 获取通知渠道标签
    getChannelTypeLabel(type) {
      const channelType = this.channelTypes.find(t => t.value === type)
      return channelType ? channelType.label : type
    },

    // 获取通知渠道颜色
    getChannelTypeColor(type) {
      const colorMap = {
        dingTalk: 'blue',
        email: 'green',
        feishu: 'purple',
        sms_ali: 'orange',
        sms_tencent: 'cyan',
        audio_ali: 'red',
        audio_tencent: 'pink',
        webhook: 'blue'
      }
      return colorMap[type] || 'default'
    },

    // 获取渠道列表
    async getChannelList() {
      try {
        const response = await listNoticeChannel({pageSize: 1000})
        this.channelList = response.rows || []
        console.log('渠道列表加载成功:', this.channelList)
        console.log('当前表单渠道ID:', this.form.channelId)
        console.log('当前表单通知渠道:', this.form.channelType)

        // 检查当前选中的渠道配置是否存在
        if (this.form.channelId) {
          const selectedChannel = this.channelList.find(
            channel => channel.id === this.form.channelId)
          console.log('选中的渠道配置:', selectedChannel)
        }
      } catch (error) {
        console.error('获取渠道列表失败:', error)
      }
    },

    initForm() {
      console.log('初始化表单数据:', this.data)
      if (this.data && this.data.id && Object.keys(this.data).length > 0) {
        // 编辑模式，复制所有数据
        this.form = {...this.data}

        // 处理默认接收人数据：如果是字符串则转换为数组，空值处理
        console.log('原始receivers数据:', this.form.receivers, '类型:', typeof this.form.receivers)
        if (this.form.receivers && typeof this.form.receivers === 'string') {
          this.form.receivers = this.form.receivers.split(',').filter(r => r.trim())
          console.log('转换后的receivers数组:', this.form.receivers)
        } else if (!this.form.receivers || this.form.receivers === '') {
          this.form.receivers = []
          console.log('设置空数组')
        }

        console.log('编辑模式，表单数据:', this.form)

        // 确保通知渠道和渠道配置都正确设置
        if (this.form.channelId && this.channelList.length > 0) {
          const selectedChannel = this.channelList.find(
            channel => channel.id === this.form.channelId)
          if (selectedChannel) {
            console.log('找到匹配的渠道配置:', selectedChannel)
            // 如果通知渠道不匹配，更新通知渠道
            if (this.form.channelType !== selectedChannel.channelType) {
              this.form.channelType = selectedChannel.channelType
              console.log('更新渠道类型为:', this.form.channelType)
            }
          } else {
            console.warn('找不到匹配的渠道配置，ID:', this.form.channelId)
          }
        }
      } else {
        // 新增模式，重置表单
        this.form = {
          name: '',
          channelType: '',
          channelId: '',
          title: '',
          content: '',
          status: '1',
          remark: '',
          receivers: []
        }
        console.log('新增模式，表单数据:', this.form)
      }
    },

    handleChannelTypeChange(value) {
      // 清空渠道配置选择
      this.form.channelId = ''
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },

    handleChannelConfigChange(value) {
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },

    // 插入变量到内容
    insertVariableToContent(variable) {
      const textarea = this.$refs.form.$el.querySelector('textarea')
      if (textarea) {
        const start = textarea.selectionStart
        const end = textarea.selectionEnd
        const content = this.form.content
        this.form.content = content.substring(0, start) + variable + content.substring(end)

        // 设置光标位置
        this.$nextTick(() => {
          textarea.focus()
          textarea.setSelectionRange(start + variable.length, start + variable.length)
        })
      }
    },

    // 插入变量（通用方法）
    insertVariable() {
      // 可以在这里添加变量选择弹窗
      this.$message.info('点击上方参数标签可直接插入')
    },

    // 处理接收人变化（使用a-select的tags模式）
    handleReceiversChange(value) {
      // 过滤空值
      const filteredValue = value.filter(item => item && item.trim())
      console.log('接收人变化:', filteredValue)
    },

    // 验证接收人格式
    validateReceiver(receiver) {
      // 简单的手机号验证（11位数字）
      const phoneRegex = /^1[3-9]\d{9}$/
      // 简单的邮箱验证
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      //暂时不校验
      // return phoneRegex.test(receiver) || emailRegex.test(receiver)
      return true;
    },

    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true

          // 将接收人数组转换为逗号分隔的字符串
          const formData = {
            ...this.form,
            receivers: this.form.receivers.length > 0 ? this.form.receivers.join(',') : ''
          }

          this.$emit('submit', formData)
          this.loading = false
        }
      })
    },

    handleCancel() {
      this.$emit('cancel')
    }
  }
}
</script>

<style lang="less" scoped>
.form-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.channel-option {
  display: flex;
  align-items: center;

  .channel-icon {
    width: 16px;
    height: 16px;
    margin-right: 8px;
    object-fit: contain;
  }
}

.channel-config-option {
  display: flex;
  align-items: center;
  gap: 8px;

  .channel-icon {
    width: 16px;
    height: 16px;
    object-fit: contain;
  }
}

.content-editor {
  .variable-panel {
    margin-top: 12px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 6px;
    border: 1px solid #e8e8e8;
  }

  .variable-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #262626;

    .anticon {
      color: #1890ff;
    }
  }

  .variable-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .variable-tag {
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 4px;

    &:hover {
      background: #1890ff;
      color: white;
      transform: translateY(-1px);
    }

    .variable-desc {
      font-size: 11px;
      opacity: 0.8;
    }
  }
}

.field-hint {
  display: flex;
  align-items: center;
  margin-top: 4px;
  color: #8c8c8c;
  font-size: 12px;

  .anticon {
    margin-right: 4px;
    font-size: 12px;
  }
}

// 强制左对齐
:deep(.ant-form-item-label) {
  text-align: left !important;
}

:deep(.ant-input),
:deep(.ant-select-selection),
:deep(.ant-textarea) {
  text-align: left !important;
}

:deep(.ant-select-selection__rendered) {
  text-align: left !important;
}

// 接收人输入框样式
.receivers-input-wrapper {
  // a-select tags模式会自动处理标签样式，无需额外样式
  // 空样式块，保留用于未来扩展
}
</style> 