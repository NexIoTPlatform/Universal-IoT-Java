<template>
  <a-form-model
    ref="form"
    :model="formData"
    :rules="formRules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <template v-for="field in visibleFields" :key="field.key">
      <!-- 隐藏字段 -->
      <template v-if="field.type === 'hidden'">
        <!-- 隐藏字段不渲染 -->
      </template>

      <!-- 输入框 -->
      <a-form-model-item
        v-else-if="field.type === 'input'"
        :label="field.label"
        :prop="field.key"
      >
        <a-input
          v-model="formData[field.key]"
          :placeholder="field.placeholder"
          :maxLength="field.maxLength"
          :disabled="field.disabled"
        />
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>

      <!-- 密码框 -->
      <a-form-model-item
        v-else-if="field.type === 'password'"
        :label="field.label"
        :prop="field.key"
      >
        <a-input-password
          v-model="formData[field.key]"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
        />
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>

      <!-- 数字输入框 -->
      <a-form-model-item
        v-else-if="field.type === 'number'"
        :label="field.label"
        :prop="field.key"
      >
        <a-input-number
          v-model="formData[field.key]"
          :placeholder="field.placeholder"
          :min="field.min"
          :max="field.max"
          :disabled="field.disabled"
          style="width: 100%"
        />
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>

      <!-- 选择框 -->
      <a-form-model-item
        v-else-if="field.type === 'select'"
        :label="field.label"
        :prop="field.key"
      >
        <a-select
          v-model="formData[field.key]"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          @change="handleFieldChange(field.key, $event)"
        >
          <a-select-option
            v-for="option in field.options"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>

      <!-- 开关 -->
      <a-form-model-item
        v-else-if="field.type === 'switch'"
        :label="field.label"
        :prop="field.key"
      >
        <a-switch
          v-model="switchValues[field.key]"
          :checked-children="field.trueLabel || '开'"
          :un-checked-children="field.falseLabel || '关'"
          :disabled="field.disabled"
          @change="handleSwitchChange(field.key, $event)"
        />
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>

      <!-- 文本域 -->
      <a-form-model-item
        v-else-if="field.type === 'textarea'"
        :label="field.label"
        :prop="field.key"
      >
        <a-textarea
          v-model="formData[field.key]"
          :placeholder="field.placeholder"
          :rows="field.rows || 3"
          :maxLength="field.maxLength"
          :disabled="field.disabled"
        />
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>

      <!-- JSON编辑器 -->
      <a-form-model-item
        v-else-if="field.type === 'json'"
        :label="field.label"
        :prop="field.key"
      >
        <a-textarea
          v-model="formData[field.key]"
          :placeholder="field.placeholder"
          :rows="field.rows || 4"
          :disabled="field.disabled"
          @blur="validateJson(field.key)"
        />
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
        <div v-if="jsonErrors[field.key]" class="field-error">
          <a-icon type="exclamation-circle"/>
          {{ jsonErrors[field.key] }}
        </div>
      </a-form-model-item>

      <!-- 代码编辑器 -->
      <a-form-model-item
        v-else-if="field.type === 'code'"
        :label="field.label"
        :prop="field.key"
      >
        <div class="code-editor-wrapper">
          <a-textarea
            v-model="formData[field.key]"
            :placeholder="field.placeholder"
            :rows="field.rows || 6"
            :disabled="field.disabled"
            class="code-editor"
          />
          <div class="code-language-tag">{{ field.language || 'text' }}</div>
        </div>
        <div v-if="field.tooltip" class="field-tooltip">
          <a-icon type="question-circle"/>
          {{ field.tooltip }}
        </div>
      </a-form-model-item>
    </template>
  </a-form-model>
</template>

<script>
export default {
  name: 'DynamicForm',
  props: {
    fields: {
      type: Array,
      default: () => []
    },
    modelValue: {
      type: Object,
      default: () => ({})
    },
    labelCol: {
      type: Object,
      default: () => ({span: 6})
    },
    wrapperCol: {
      type: Object,
      default: () => ({span: 16})
    }
  },
  emits: ['update:modelValue', 'field-change'],
  data() {
    return {
      formData: {},
      formRules: {},
      switchValues: {},
      jsonErrors: {}
    }
  },
  computed: {
    visibleFields() {
      return this.fields.filter(field => {
        if (typeof field.visible === 'function') {
          return field.visible(this.formData)
        }
        return field.visible !== false
      })
    }
  },
  watch: {
    modelValue: {
      handler(newVal) {
        this.initFormData(newVal)
      },
      immediate: true,
      deep: true
    },
    fields: {
      handler() {
        this.initFormRules()
        this.initFormData(this.modelValue)
      },
      immediate: true
    },
    formData: {
      handler(newVal) {
        this.$emit('update:modelValue', {...newVal})
      },
      deep: true
    }
  },
  methods: {
    initFormData(data = {}) {
      const newFormData = {...data}
      const newSwitchValues = {}

      // 设置默认值
      this.fields.forEach(field => {
        if (newFormData[field.key] === undefined && field.defaultValue !== undefined) {
          if (field.type === 'json' && typeof field.defaultValue === 'object') {
            newFormData[field.key] = JSON.stringify(field.defaultValue, null, 2)
          } else {
            newFormData[field.key] = field.defaultValue
          }
        }

        // 处理开关字段
        if (field.type === 'switch') {
          newSwitchValues[field.key] = newFormData[field.key] === 1 || newFormData[field.key] === true
        }
      })

      this.formData = newFormData
      this.switchValues = newSwitchValues
    },

    initFormRules() {
      const rules = {}

      this.fields.forEach(field => {
        if (field.required) {
          rules[field.key] = [
            {required: true, message: `${field.label}为必填项`, trigger: 'blur'}
          ]
        }

        if (field.maxLength) {
          if (!rules[field.key]) rules[field.key] = []
          rules[field.key].push({
            max: field.maxLength,
            message: `${field.label}长度不能超过${field.maxLength}个字符`,
            trigger: 'blur'
          })
        }

        if (field.type === 'number') {
          if (!rules[field.key]) rules[field.key] = []
          if (field.min !== undefined) {
            rules[field.key].push({
              validator: (rule, value, callback) => {
                if (value !== undefined && value < field.min) {
                  callback(new Error(`${field.label}不能小于${field.min}`))
                } else {
                  callback()
                }
              },
              trigger: 'blur'
            })
          }
          if (field.max !== undefined) {
            rules[field.key].push({
              validator: (rule, value, callback) => {
                if (value !== undefined && value > field.max) {
                  callback(new Error(`${field.label}不能大于${field.max}`))
                } else {
                  callback()
                }
              },
              trigger: 'blur'
            })
          }
        }
      })

      this.formRules = rules
    },

    handleFieldChange(fieldKey, value) {
      this.formData[fieldKey] = value
      this.$emit('field-change', fieldKey, value, this.formData)
    },

    handleSwitchChange(fieldKey, checked) {
      this.switchValues[fieldKey] = checked
      this.formData[fieldKey] = checked ? 1 : 0
      this.$emit('field-change', fieldKey, this.formData[fieldKey], this.formData)
    },

    validateJson(fieldKey) {
      const value = this.formData[fieldKey]
      if (value) {
        try {
          JSON.parse(value)
          this.$delete(this.jsonErrors, fieldKey)
        } catch (e) {
          this.$set(this.jsonErrors, fieldKey, 'JSON格式不正确')
        }
      } else {
        this.$delete(this.jsonErrors, fieldKey)
      }
    },

    validate() {
      return new Promise((resolve) => {
        this.$refs.form.validate((valid) => {
          // 检查JSON错误
          const hasJsonErrors = Object.keys(this.jsonErrors).length > 0
          resolve(valid && !hasJsonErrors)
        })
      })
    },

    resetFields() {
      this.$refs.form.resetFields()
      this.jsonErrors = {}
    },

    clearValidate() {
      this.$refs.form.clearValidate()
      this.jsonErrors = {}
    }
  }
}
</script>

<style scoped>
.field-tooltip {
  margin-top: 4px;
  font-size: 12px;
  color: #666;
}

.field-error {
  margin-top: 4px;
  font-size: 12px;
  color: #f5222d;
}

.code-editor-wrapper {
  position: relative;
}

.code-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.code-language-tag {
  position: absolute;
  top: 4px;
  right: 8px;
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 2px;
  font-size: 10px;
  color: #666;
  pointer-events: none;
}
</style>

