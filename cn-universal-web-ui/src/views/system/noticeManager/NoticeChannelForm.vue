<template>
  <a-modal
    :title="title"
    :visible="visible"
    :width="600"
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

        <a-form-model-item label="配置名称" prop="name">
          <a-input
            v-model="form.name"
            placeholder="请输入配置名称"
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
                <span class="channel-icon" v-html="type.icon"></span>
                <span>{{ type.label }}</span>
              </div>
            </a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item :label="$t('common.status')" prop="status">
          <a-radio-group v-model="form.status">
            <a-radio value="0">启用</a-radio>
            <a-radio value="1">停用</a-radio>
          </a-radio-group>
        </a-form-model-item>
      </div>

      <!-- 渠道配置 -->
      <div v-if="form.channelType" class="form-section">
        <div class="section-title">
          {{ getChannelTypeLabel(form.channelType) }} 配置
        </div>

        <div class="config-tip">
          <a-icon type="info-circle"/>
          {{ getChannelTypeConfig(form.channelType).tip }}
        </div>

        <template v-for="field in getChannelTypeConfig(form.channelType).fields">
          <a-form-model-item
            :key="field.key"
            :label="field.label"
            :prop="`config.${field.key}`"
            :required="field.required"
          >
            <a-input
              v-if="field.type === 'input'"
              v-model="form.config[field.key]"
              :placeholder="field.placeholder"
              :type="field.inputType || 'text'"
            />
            <a-textarea
              v-else-if="field.type === 'textarea'"
              v-model="form.config[field.key]"
              :placeholder="field.placeholder"
              :rows="field.rows || 3"
            />
            <a-select
              v-else-if="field.type === 'select'"
              v-model="form.config[field.key]"
              :placeholder="field.placeholder"
            >
              <a-select-option
                v-for="option in field.options"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </a-select-option>
            </a-select>

            <div v-if="field.hint" class="field-hint">
              <a-icon type="info-circle"/>
              {{ field.hint }}
            </div>
          </a-form-model-item>
        </template>
      </div>
    </a-form-model>
  </a-modal>
</template>

<script>
import {channelTypes, getChannelTypeConfig} from './channelConfig'

export default {
  name: 'NoticeChannelForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '新增渠道配置'
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
        status: '0',
        config: {}
      },
      channelTypes,
      rules: {
        name: [
          {required: true, message: '请输入配置名称', trigger: 'blur'},
          {min: 2, max: 50, message: '配置名称长度为2-50个字符', trigger: 'blur'}
        ],
        channelType: [
          {required: true, message: '请选择通知渠道', trigger: 'change'}
        ]
      }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initForm()
      }
    }
  },
  methods: {
    initForm() {
      if (this.data.id) {
        this.form = {...this.data}
      } else {
        this.form = {
          name: '',
          channelType: '',
          status: '0',
          config: {}
        }
      }
    },

    handleChannelTypeChange(value) {
      this.form.config = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },

    getChannelTypeLabel(type) {
      const channelType = this.channelTypes.find(t => t.value === type)
      return channelType ? channelType.label : ''
    },

    getChannelTypeConfig(type) {
      return getChannelTypeConfig(type)
    },

    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true
          this.$emit('submit', this.form)
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

.config-tip {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  margin-bottom: 16px;
  color: #52c41a;
  font-size: 14px;

  .anticon {
    margin-right: 8px;
    font-size: 16px;
  }
}

.channel-option {
  display: flex;
  align-items: center;

  .channel-icon {
    width: 16px;
    height: 16px;
    margin-right: 8px;
    color: #1890ff;
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
</style> 