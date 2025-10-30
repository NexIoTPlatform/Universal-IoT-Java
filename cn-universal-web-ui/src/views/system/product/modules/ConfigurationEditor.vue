<template>
  <div class="configuration-editor">
    <!-- 公共配置 -->
    <div class="config-section" v-if="publicConfigurationArray.length > 0">
      <div class="section-header">
        <div class="section-title">
          <a-icon type="setting"/>
          <span>公共配置</span>
        </div>
        <a-button
          v-if="!publicConfigEditing"
          type="link"
          icon="edit"
          @click="startEditConfig('publicConfiguration')"
          v-hasPermi="['system:product:edit']"
          class="edit-btn"
        >
          编辑
        </a-button>
        <div v-else style="display: inline-block;">
          <a-button type="link" size="small" @click="saveConfig('publicConfiguration')">保存
          </a-button>
          <a-button type="link" size="small" style="margin-left:8px;"
                    @click="cancelEditConfig('publicConfiguration')">取消
          </a-button>
        </div>
      </div>
      <div class="section-content">
        <template v-if="!publicConfigEditing">
          <div class="config-list">
            <div
              v-for="item in enhancedPublicConfigurationArray"
              :key="item.key"
              class="config-item"
            >
              <div class="config-label">{{ item.displayName }}</div>
              <div class="config-value">
                <a-tooltip :title="item.remark">
                  <template v-if="publicConfigEditing">
                    {{ getDisplayValue(publicConfigForm[item.key], item.valueType) }}
                  </template>
                  <template v-else>
                    {{ getDisplayValue(item.value, item.valueType) }}
                  </template>
                </a-tooltip>
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <a-form :model="publicConfigForm" layout="horizontal">
            <a-row :gutter="0">
              <a-col :span="24" v-for="item in enhancedPublicConfigurationArray" :key="item.key">
                <a-form-item :label="item.displayName">
                  <template v-if="item.valueType === 'int'">
                    <a-input-number v-model="publicConfigForm[item.key]" :placeholder="item.remark"
                                    style="width:100%" :step="1" :precision="0" @blur="handleBlur"/>
                  </template>
                  <template v-else-if="item.valueType === 'json'">
                    <a-textarea v-model="publicConfigForm[item.key]" :placeholder="item.remark"
                                auto-size @blur="handleBlur"/>
                  </template>
                  <template v-else-if="item.valueType === 'file'">
                    <a-upload v-model="publicConfigForm[item.key]" :show-upload-list="false"
                              @change="handleBlur"/>
                  </template>
                  <template v-else-if="item.valueType === 'select'">
                    <a-select
                      v-model="safePublicConfigForm[item.key]"
                      :placeholder="item.remark"
                      show-search
                      option-filter-prop="children"
                      @blur="handleBlur"
                      @change="(value) => handleSelectChange(value, item.key)"
                    >
                      <a-select-option v-for="opt in item.valueData" :key="opt.dictValue"
                                       :value="opt.dictValue">{{ opt.dictLabel }}
                      </a-select-option>
                    </a-select>
                  </template>
                  <template v-else-if="item.valueType === 'boolean'">
                    <a-select
                      v-model="safePublicConfigForm[item.key]"
                      :placeholder="item.remark"
                      @blur="handleBlur"
                      @change="(value) => handleSelectChange(value, item.key)"
                    >
                      <a-select-option value="true">是</a-select-option>
                      <a-select-option value="false">否</a-select-option>
                    </a-select>
                  </template>
                  <template v-else>
                    <a-input v-model="publicConfigForm[item.key]" :placeholder="item.remark"
                             @blur="handleBlur"/>
                  </template>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </template>
      </div>
    </div>

    <!-- 端云配置 -->
    <div class="config-section" v-if="configurationArray.length > 0"
         v-hasPermi="['system:product:config']">
      <div class="section-header">
        <div class="section-title">
          <a-icon type="cloud"/>
          <span>端云配置</span>
        </div>
        <a-button
          v-if="!privateConfigEditing"
          type="link"
          icon="edit"
          @click="startEditConfig('privateConfiguration')"
          v-hasPermi="['system:product:config']"
          class="edit-btn"
        >
          编辑
        </a-button>
        <div v-else style="display: inline-block;">
          <a-button type="link" size="small" @click="saveConfig('privateConfiguration')">保存
          </a-button>
          <a-button type="link" size="small" style="margin-left:8px;"
                    @click="cancelEditConfig('privateConfiguration')">取消
          </a-button>
        </div>
      </div>
      <div class="section-content">
        <template v-if="!privateConfigEditing">
          <div class="config-list">
            <div
              v-for="item in enhancedConfigurationArray"
              :key="item.key"
              class="config-item"
            >
              <div class="config-label">{{ item.displayName }}</div>
              <div class="config-value">
                <a-tooltip :title="item.remark">
                  <template v-if="privateConfigEditing">
                    {{ getDisplayValue(privateConfigForm[item.key], item.valueType) }}
                  </template>
                  <template v-else>
                    {{ getDisplayValue(item.value, item.valueType) }}
                  </template>
                </a-tooltip>
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <a-form :model="privateConfigForm" layout="horizontal">
            <a-row :gutter="0">
              <a-col :span="24" v-for="item in enhancedConfigurationArray" :key="item.key">
                <a-form-item :label="item.displayName">
                  <template v-if="item.valueType === 'int'">
                    <a-input-number v-model="privateConfigForm[item.key]" :placeholder="item.remark"
                                    style="width:100%" :step="1" :precision="0" @blur="handleBlur"/>
                  </template>
                  <template v-else-if="item.valueType === 'json'">
                    <a-textarea v-model="privateConfigForm[item.key]" :placeholder="item.remark"
                                auto-size @blur="handleBlur"/>
                  </template>
                  <template v-else-if="item.valueType === 'file'">
                    <a-upload v-model="privateConfigForm[item.key]" :show-upload-list="false"
                              @change="handleBlur"/>
                  </template>
                  <template v-else-if="item.valueType === 'select'">
                    <a-select
                      v-model="safePrivateConfigForm[item.key]"
                      :placeholder="item.remark"
                      show-search
                      option-filter-prop="children"
                      @blur="handleBlur"
                      @change="(value) => handleSelectChange(value, item.key)"
                    >
                      <a-select-option v-for="opt in item.valueData" :key="opt.dictValue"
                                       :value="opt.dictValue">{{ opt.dictLabel }}
                      </a-select-option>
                    </a-select>
                  </template>
                  <template v-else-if="item.valueType === 'boolean'">
                    <a-select
                      v-model="safePrivateConfigForm[item.key]"
                      :placeholder="item.remark"
                      @blur="handleBlur"
                      @change="(value) => handleSelectChange(value, item.key)"
                    >
                      <a-select-option value="true">是</a-select-option>
                      <a-select-option value="false">否</a-select-option>
                    </a-select>
                  </template>
                  <template v-else>
                    <a-input v-model="privateConfigForm[item.key]" :placeholder="item.remark"
                             @blur="handleBlur"/>
                  </template>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </template>
      </div>
    </div>

    <!-- JSON预览模态框 -->
    <a-modal
      title="配置预览"
      :visible="previewVisible"
      @ok="confirmSaveConfig"
      @cancel="previewVisible = false"
      width="600px"
    >
      <div>
        <p>请确认以下配置信息：</p>
        <a-input
          type="textarea"
          v-model="previewJson"
          :rows="15"
          readonly
          style="font-family: 'Courier New', monospace;"
        />
      </div>
    </a-modal>
  </div>
</template>

<script>
import {updateConfig} from '@/api/system/dev/product'

export default {
  name: 'ConfigurationEditor',
  props: {
    productId: {
      type: [String, Number],
      required: true
    },
    publicConfigurationArray: {
      type: Array,
      default: () => []
    },
    configurationArray: {
      type: Array,
      default: () => []
    },
    // 添加字典数据支持
    publicConfigurationDicts: {
      type: Array,
      default: () => []
    },
    configurationDicts: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      publicConfigEditing: false,
      privateConfigEditing: false,
      publicConfigForm: {},
      privateConfigForm: {},
      publicConfigBackup: {},
      privateConfigBackup: {},
      previewVisible: false,
      previewJson: '',
      currentEditType: '',
      // 存储字典数据
      dictDataCache: {}
    }
  },
  async mounted() {
    // 预加载所有需要的字典数据
    await this.preloadDictData()
  },
  computed: {
    // 获取公共配置的完整数据
    enhancedPublicConfigurationArray() {
      // console.log('enhancedPublicConfigurationArray - publicConfigurationArray:',
      //   this.publicConfigurationArray)
      // console.log('enhancedPublicConfigurationArray - publicConfigurationDicts:',
      //   this.publicConfigurationDicts)
      // console.log('enhancedPublicConfigurationArray - dictDataCache:', this.dictDataCache)

      return this.publicConfigurationArray.map(item => {
        const dictItem = this.publicConfigurationDicts.find(dict => dict.dictLabel === item.key)
        // console.log('enhancedPublicConfigurationArray - item:', item, 'dictItem:', dictItem)

        if (dictItem && dictItem.remark) {
          try {
            const config = JSON.parse(dictItem.remark)
            // console.log('enhancedPublicConfigurationArray - config:', config)

            const result = {
              ...item,
              key: dictItem.dictValue, // Use English key for form binding
              displayName: item.key, // Use Chinese label for display
              valueType: config.type,
              valueData: config.url ? (this.dictDataCache[config.url] || []) : []
            }
            // console.log('enhancedPublicConfigurationArray - result:', result)
            return result
          } catch (e) {
            // console.error('enhancedPublicConfigurationArray - parse error:', e)
            return {...item, key: dictItem ? dictItem.dictValue : item.key, displayName: item.key}
          }
        }
        return {...item, displayName: item.key}
      })
    },
    // 获取端云配置的完整数据
    enhancedConfigurationArray() {
      // console.log('enhancedConfigurationArray - configurationArray:', this.configurationArray)
      // console.log('enhancedConfigurationArray - configurationDicts:', this.configurationDicts)
      // console.log('enhancedConfigurationArray - dictDataCache:', this.dictDataCache)

      return this.configurationArray.map(item => {
        const dictItem = this.configurationDicts.find(dict => dict.dictLabel === item.key)
        // console.log('enhancedConfigurationArray - item:', item, 'dictItem:', dictItem)

        if (dictItem && dictItem.remark) {
          try {
            const config = JSON.parse(dictItem.remark)
            // console.log('enhancedConfigurationArray - config:', config)

            const result = {
              ...item,
              key: dictItem.dictValue, // Use English key for form binding
              displayName: item.key, // Use Chinese label for display
              valueType: config.type,
              valueData: config.url ? (this.dictDataCache[config.url] || []) : []
            }
            // console.log('enhancedConfigurationArray - result:', result)
            return result
          } catch (e) {
            // console.error('enhancedConfigurationArray - parse error:', e)
            return {...item, key: dictItem ? dictItem.dictValue : item.key, displayName: item.key}
          }
        }
        return {...item, displayName: item.key}
      })
    },
    // 确保select值类型正确
    safePublicConfigForm: {
      get() {
        const safeForm = {}
        Object.keys(this.publicConfigForm).forEach(key => {
          const value = this.publicConfigForm[key]
          if (value === undefined || value === null) {
            safeForm[key] = ''
          } else {
            safeForm[key] = String(value)
          }
        })
        return safeForm
      },
      set(value) {
        // 更新原始表单数据
        Object.keys(value).forEach(key => {
          this.$set(this.publicConfigForm, key, value[key])
        })
      }
    },
    // 确保select值类型正确
    safePrivateConfigForm: {
      get() {
        const safeForm = {}
        Object.keys(this.privateConfigForm).forEach(key => {
          const value = this.privateConfigForm[key]
          if (value === undefined || value === null) {
            safeForm[key] = ''
          } else {
            safeForm[key] = String(value)
          }
        })
        return safeForm
      },
      set(value) {
        // 更新原始表单数据
        Object.keys(value).forEach(key => {
          this.$set(this.privateConfigForm, key, value[key])
        })
      }
    }
  },
  methods: {
    // 开始编辑配置
    startEditConfig(type) {
      this.currentEditType = type
      if (type === 'publicConfiguration') {
        this.publicConfigEditing = true
        // 初始化表单数据
        this.publicConfigForm = {}
        this.enhancedPublicConfigurationArray.forEach(item => {
          // 确保select类型的值为字符串类型
          if (item.valueType === 'select' || item.valueType === 'boolean') {
            this.publicConfigForm[item.key] = item.value !== undefined && item.value !== null
              ? String(item.value) : ''
          } else {
            this.publicConfigForm[item.key] = item.value
          }
        })
        // 备份原始数据
        this.publicConfigBackup = JSON.parse(JSON.stringify(this.publicConfigForm))
      } else if (type === 'privateConfiguration') {
        this.privateConfigEditing = true
        // 初始化表单数据
        this.privateConfigForm = {}
        this.enhancedConfigurationArray.forEach(item => {
          // 确保select类型的值为字符串类型
          if (item.valueType === 'select' || item.valueType === 'boolean') {
            this.privateConfigForm[item.key] = item.value !== undefined && item.value !== null
              ? String(item.value) : ''
          } else {
            this.privateConfigForm[item.key] = item.value
          }
        })
        // 备份原始数据
        this.privateConfigBackup = JSON.parse(JSON.stringify(this.privateConfigForm))
      }
    },
    // 保存配置
    saveConfig(type) {
      // 强制更新表单数据，确保获取最新值
      this.$forceUpdate()
      this.$nextTick(() => {
        const rawForm = type === 'publicConfiguration' ? this.publicConfigForm
          : this.privateConfigForm
        const metaArray = type === 'publicConfiguration' ? this.enhancedPublicConfigurationArray
          : this.enhancedConfigurationArray
        const formData = this.normalizeFormData(metaArray, rawForm)
        console.log('saveConfig - type:', type)
        console.log('saveConfig - raw form before preview:', rawForm)
        console.log('saveConfig - normalized formData before preview:', formData)
        this.previewJson = JSON.stringify(formData, null, 2)
        this.previewVisible = true
      })
    },
    // 确认保存配置
    confirmSaveConfig() {
      this.previewVisible = false
      const rawForm = this.currentEditType === 'publicConfiguration' ? this.publicConfigForm
        : this.privateConfigForm
      const metaArray = this.currentEditType === 'publicConfiguration'
        ? this.enhancedPublicConfigurationArray
        : this.enhancedConfigurationArray
      const formData = this.normalizeFormData(metaArray, rawForm)
      updateConfig({
        id: this.productId,
        configuration: JSON.stringify(formData),
        type: this.currentEditType
      }).then(() => {
        this.$message.success('配置保存成功')
        if (this.currentEditType === 'publicConfiguration') {
          this.publicConfigEditing = false
        } else {
          this.privateConfigEditing = false
        }
        this.$emit('config-updated') // 通知父组件刷新
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    // 根据字段元数据将表单值转换为正确类型
    normalizeFormData(metaArray, formObj) {
      const metaMap = {}
      metaArray.forEach(item => {
        metaMap[item.key] = item.valueType
      })
      const result = {}
      Object.keys(formObj).forEach(key => {
        const type = metaMap[key]
        const value = formObj[key]
        if (value === '' || value === undefined || value === null) {
          result[key] = value
          return
        }
        if (type === 'boolean') {
          if (typeof value === 'string') {
            result[key] = value === 'true'
          } else {
            result[key] = !!value
          }
        } else if (type === 'int') {
          const num = parseInt(value, 10)
          result[key] = isNaN(num) ? value : num
        } else {
          result[key] = value
        }
      })
      return result
    },
    // 取消编辑配置
    cancelEditConfig(type) {
      if (type === 'publicConfiguration') {
        this.publicConfigEditing = false
        this.publicConfigForm = JSON.parse(JSON.stringify(this.publicConfigBackup))
      } else if (type === 'privateConfiguration') {
        this.privateConfigEditing = false
        this.privateConfigForm = JSON.parse(JSON.stringify(this.privateConfigBackup))
      }
    },
    // 处理离开焦点事件，强制更新显示
    handleBlur() {
      // 强制更新视图
      this.$forceUpdate()
      // 触发计算属性重新计算
      this.$nextTick(() => {
        this.$forceUpdate()
      })
    },
    // 处理select change事件
    handleSelectChange(value, key) {
      console.log('handleSelectChange - value:', value, 'key:', key)

      // 直接更新对应的表单数据
      if (this.publicConfigEditing && this.publicConfigForm.hasOwnProperty(key)) {
        this.$set(this.publicConfigForm, key, value)
        console.log('handleSelectChange - updated publicConfigForm:', this.publicConfigForm)
      } else if (this.privateConfigEditing && this.privateConfigForm.hasOwnProperty(key)) {
        this.$set(this.privateConfigForm, key, value)
        console.log('handleSelectChange - updated privateConfigForm:', this.privateConfigForm)
      }

      // 强制更新视图
      this.$forceUpdate()
      this.$nextTick(() => {
        this.$forceUpdate()
        // console.log('handleSelectChange - publicConfigForm after update:', this.publicConfigForm)
        // console.log('handleSelectChange - privateConfigForm after update:', this.privateConfigForm)
      })
    },
    // 预加载所有需要的字典数据
    async preloadDictData() {
      // console.log('preloadDictData - start')
      // console.log('preloadDictData - publicConfigurationDicts:', this.publicConfigurationDicts)
      // console.log('preloadDictData - configurationDicts:', this.configurationDicts)

      // 处理公共配置字典
      this.publicConfigurationDicts.forEach(dict => {
        if (dict.remark) {
          try {
            const config = JSON.parse(dict.remark)
            // console.log('preloadDictData - public config:', config)
            if (config.url && config.url !== undefined && config.url !== '') {
              // console.log('preloadDictData - calling getDicts for public url:', config.url)
              this.getDicts(config.url).then(response => {
                // console.log('preloadDictData - public response:', response)
                this.$set(this.dictDataCache, config.url, response.data)
                // console.log('preloadDictData - cached public data for url:', config.url,
                //   response.data)
                // console.log('preloadDictData - dictDataCache after public:', this.dictDataCache)
              })
            }
          } catch (e) {
            console.error(`Failed to parse remark for dict ${dict.dictLabel}:`, e)
          }
        }
      })

      // 处理私有配置字典
      this.configurationDicts.forEach(dict => {
        if (dict.remark) {
          try {
            const config = JSON.parse(dict.remark)
            // console.log('preloadDictData - private config:', config)
            if (config.url && config.url !== undefined && config.url !== '') {
              // console.log('preloadDictData - calling getDicts for private url:', config.url)
              this.getDicts(config.url).then(response => {
                // console.log('preloadDictData - private response:', response)
                this.$set(this.dictDataCache, config.url, response.data)
                // console.log('preloadDictData - cached private data for url:', config.url,
                //   response.data)
                // console.log('preloadDictData - dictDataCache after private:', this.dictDataCache)
              })
            }
          } catch (e) {
            console.error(`Failed to parse remark for dict ${dict.dictLabel}:`, e)
          }
        }
      })
    },
    // 获取显示值
    getDisplayValue(value, type) {
      if (type === 'boolean') {
        // 处理字符串形式的布尔值
        if (typeof value === 'string') {
          return value === 'true' ? '是' : '否'
        }
        return value ? '是' : '否'
      }
      return value !== undefined && value !== null && value !== '' ? value : '缺省'
    }
  }
}
</script>

<style scoped lang="less">
.configuration-editor {
  .config-section {
    margin-bottom: 24px;
    background: #ffffff;
    border: 1px solid #e8eaed;
    border-radius: 12px;
    padding: 24px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .section-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 500;
        color: #333;

        .anticon {
          margin-right: 8px;
          color: #1890ff;
        }
      }
    }

    .section-content {
      background: #fafbfc;
      border-radius: 8px;
      padding: 16px;

      .config-list {
        .config-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .config-label {
            font-weight: 500;
            color: #333;
            min-width: 120px;
          }

          .config-value {
            color: #666;
            flex: 1;
            text-align: right;
          }
        }
      }

      .ant-form {
        .ant-form-item {
          margin-bottom: 16px;

          .ant-form-item-label {
            label {
              font-weight: 500;
              color: #333;
            }
          }
        }
      }
    }
  }
}
</style> 