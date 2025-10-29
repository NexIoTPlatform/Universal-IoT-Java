<template>
  <a-modal
    :title="formData.id ? '编辑数据源管理' : '新增数据源管理'"
    :visible="visible"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    width="60vw"
    :style="{ maxWidth: '700px', minWidth: '450px' }"
    :body-style="{ padding: '0', maxHeight: '70vh', overflow: 'auto' }"
    :footer="null"
    class="resource-form-modal"
  >
    <!-- 步骤指示器 -->
    <div class="step-indicator">
      <div class="step-item" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
        <div class="step-number">1</div>
        <div class="step-title">基本信息</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 1 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
        <div class="step-number">2</div>
        <div class="step-title">连接配置</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 2 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 3 }">
        <div class="step-number">3</div>
        <div class="step-title">高级设置</div>
      </div>
    </div>

    <div class="form-container">
      <a-form-model
        ref="form"
        :model="form"
        :rules="rules"
        class="resource-form"
      >
        <!-- 步骤1: 基本信息 -->
        <div v-show="currentStep === 1" class="step-content">
          <div class="step-header">
            <h3>基本信息配置</h3>
            <p>请填写资源的基本信息和数据方向（暂只支持JDBC）</p>
          </div>

          <div class="form-section">
            <a-form-model-item label="数据源名称" prop="name" class="form-item-large">
              <a-input
                v-model="form.name"
                placeholder="请输入数据源名称，如：生产环境MQTT代理"
                size="large"
              />
              <div class="form-help-text">建议使用有意义的名称，便于后续管理</div>
            </a-form-model-item>

            <a-form-model-item label="数据方向" prop="dataDirection" required class="form-item-large">
              <div class="direction-cards">
                <div
                  class="direction-card"
                  :class="{ active: form.dataDirection === 'OUTPUT' }"
                  @click="selectDirection('OUTPUT')"
                >
                  <div class="card-icon">
                    <a-icon type="upload"/>
                  </div>
                  <div class="card-content">
                    <h4>数据输出</h4>
                    <p>IoT平台 → 外部系统</p>
                    <div class="card-desc">将IoT平台的设备数据推送到外部系统</div>
                  </div>
                  <div class="card-check">
                    <a-icon type="check" v-if="form.dataDirection === 'OUTPUT'"/>
                  </div>
                </div>
                <div
                  class="direction-card"
                  :class="{ active: form.dataDirection === 'INPUT' }"
                  @click="selectDirection('INPUT')"
                >
                  <div class="card-icon">
                    <a-icon type="download"/>
                  </div>
                  <div class="card-content">
                    <h4>数据输入</h4>
                    <p>外部系统 → IoT平台</p>
                    <div class="card-desc">将外部系统的数据导入到IoT平台中</div>
                  </div>
                  <div class="card-check">
                    <a-icon type="check" v-if="form.dataDirection === 'INPUT'"/>
                  </div>
                </div>
                <div
                  class="direction-card"
                  :class="{ active: form.dataDirection === 'BIDIRECTIONAL' }"
                  @click="selectDirection('BIDIRECTIONAL')"
                >
                  <div class="card-icon">
                    <a-icon type="swap"/>
                  </div>
                  <div class="card-content">
                    <h4>双向流转</h4>
                    <p>支持输入和输出</p>
                    <div class="card-desc">支持数据的双向同步，既可输出也可输入</div>
                  </div>
                  <div class="card-check">
                    <a-icon type="check" v-if="form.dataDirection === 'BIDIRECTIONAL'"/>
                  </div>
                </div>
              </div>
            </a-form-model-item>

            <a-form-model-item label="数据源类型" prop="type" class="form-item-large">
              <a-select
                v-model="form.type"
                placeholder="请选择数据源类型"
                :disabled="!form.dataDirection"
                @change="handleTypeChange"
                size="large"
                class="resource-type-select"
              >
                <!-- 数据库类型 -->
                <a-select-opt-group v-if="Object.keys(resourceTypes.databases).length > 0" label="数据库">
                  <a-select-option
                    v-for="(name, key) in resourceTypes.databases"
                    :key="key"
                    :value="key"
                  >
                    <a-icon type="database" style="margin-right: 8px;"/>
                    <a-tag :color="getTypeColor(key)">{{ name }}</a-tag>
                  </a-select-option>
                </a-select-opt-group>

                <!-- 消息队列类型 -->
                <a-select-opt-group v-if="Object.keys(resourceTypes.messageQueues).length > 0" label="消息队列">
                  <a-select-option
                    v-for="(name, key) in resourceTypes.messageQueues"
                    :key="key"
                    :value="key"
                  >
                    <a-icon type="message" style="margin-right: 8px;"/>
                    <a-tag :color="getTypeColor(key)">{{ name }}</a-tag>
                  </a-select-option>
                </a-select-opt-group>

                <!-- 时序数据库类型 -->
                <a-select-opt-group v-if="Object.keys(resourceTypes.timeSeries).length > 0" label="时序数据库">
                  <a-select-option
                    v-for="(name, key) in resourceTypes.timeSeries"
                    :key="key"
                    :value="key"
                  >
                    <a-icon type="line-chart" style="margin-right: 8px;"/>
                    <a-tag :color="getTypeColor(key)">{{ name }}</a-tag>
                  </a-select-option>
                </a-select-opt-group>

                <!-- 搜索引擎类型 -->
                <a-select-opt-group v-if="Object.keys(resourceTypes.searchEngines).length > 0" label="搜索引擎">
                  <a-select-option
                    v-for="(name, key) in resourceTypes.searchEngines"
                    :key="key"
                    :value="key"
                  >
                    <a-icon type="search" style="margin-right: 8px;"/>
                    <a-tag :color="getTypeColor(key)">{{ name }}</a-tag>
                  </a-select-option>
                </a-select-opt-group>

                <!-- 云平台类型 -->
                <a-select-opt-group v-if="Object.keys(resourceTypes.cloudPlatforms).length > 0" label="云平台">
                  <a-select-option
                    v-for="(name, key) in resourceTypes.cloudPlatforms"
                    :key="key"
                    :value="key"
                  >
                    <a-icon type="cloud" style="margin-right: 8px;"/>
                    <a-tag :color="getTypeColor(key)">{{ name }}</a-tag>
                  </a-select-option>
                </a-select-opt-group>

                <!-- 其他类型 -->
                <a-select-opt-group v-if="Object.keys(resourceTypes.others).length > 0" label="其他">
                  <a-select-option
                    v-for="(name, key) in resourceTypes.others"
                    :key="key"
                    :value="key"
                  >
                    <a-icon type="api" style="margin-right: 8px;"/>
                    <a-tag :color="getTypeColor(key)">{{ name }}</a-tag>
                  </a-select-option>
                </a-select-opt-group>
              </a-select>
              <div class="form-help-text">选择与您的数据源匹配的数据源类型</div>
            </a-form-model-item>

            <a-form-model-item label="插件类型" prop="pluginType" class="form-item-large">
              <a-select
                v-model="form.pluginType"
                placeholder="请选择插件类型"
                :disabled="!form.type"
                @change="handlePluginTypeChange"
                size="large"
                class="plugin-type-select"
              >
                <a-select-option
                  v-for="plugin in availablePlugins"
                  :key="plugin.value"
                  :value="plugin.value"
                >
                  <div class="plugin-option">
                    <a-icon :type="plugin.icon || 'api'" style="margin-right: 8px;"/>
                    <span class="plugin-name">{{ plugin.label }}</span>
                    <a-tag :color="getPluginTypeColor(plugin.value)" size="small">
                      {{ plugin.dataDirection }}
                    </a-tag>
                  </div>
                </a-select-option>
              </a-select>
              <div class="form-help-text">选择处理该数据源的插件类型</div>
            </a-form-model-item>

            <a-form-model-item :label="$t('common.status')" prop="status" class="form-item-large">
              <a-radio-group v-model="form.status" class="status-radio-group">
                <a-radio :value="1" class="status-radio">
                  <a-icon type="check-circle" style="color: #52c41a; margin-right: 8px;"/>
                  {{ $t('status.enable') }}
                </a-radio>
                <a-radio :value="0" class="status-radio">
                  <a-icon type="close-circle" style="color: #ff4d4f; margin-right: 8px;"/>
                  {{ $t('status.disable') }}
                </a-radio>
              </a-radio-group>
            </a-form-model-item>

            <a-form-model-item label="描述" prop="description" class="form-item-large">
              <a-textarea
                v-model="form.description"
                placeholder="请输入资源描述，如：用于接收生产环境设备数据的MQTT代理"
                :rows="3"
                class="description-textarea"
              />
            </a-form-model-item>
          </div>
        </div>

        <!-- 步骤2: 连接配置 -->
        <div v-show="currentStep === 2" class="step-content">
          <div class="step-header">
            <h3>连接配置</h3>
            <p>配置与外部系统的连接参数</p>
          </div>

          <div class="form-section" v-if="dynamicFields.length > 0">
            <!-- 核心连接字段 -->
            <div class="config-grid">
              <div
                v-for="field in coreConnectionFields"
                :key="field.field"
                class="config-item"
              >
                <a-form-model-item
                  :label="field.label"
                  :prop="'dynamicConfig.' + field.field"
                  :rules="field.config && field.config.required ? [{ required: true, message: '请输入' + field.label }] : []"
                >
                  <!-- 输入框 -->
                  <a-input
                    v-if="field.config && field.config.type === 'input'"
                    v-model="form.dynamicConfig[field.field]"
                    :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                    size="large"
                  />

                  <!-- 密码框 -->
                  <a-input-password
                    v-else-if="field.config && field.config.type === 'password'"
                    v-model="form.dynamicConfig[field.field]"
                    :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                    size="large"
                  />

                  <!-- 数字输入框 -->
                  <a-input-number
                    v-else-if="field.config && field.config.type === 'number'"
                    v-model="form.dynamicConfig[field.field]"
                    :min="field.config && field.config.min"
                    :max="field.config && field.config.max"
                    :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                    style="width: 100%"
                    size="large"
                  />

                  <!-- 下拉选择框 -->
                  <a-select
                    v-else-if="field.config && field.config.type === 'select'"
                    v-model="form.dynamicConfig[field.field]"
                    :placeholder="(field.config && field.config.placeholder) || '请选择' + field.label"
                    size="large"
                  >
                    <a-select-option
                      v-for="option in (field.config && field.config.options) || []"
                      :key="option"
                      :value="option"
                    >
                      {{ option }}
                    </a-select-option>
                  </a-select>

                  <!-- 开关 -->
                  <a-switch
                    v-else-if="field.config && field.config.type === 'switch'"
                    v-model="form.dynamicConfig[field.field]"
                    size="large"
                  />

                  <!-- 多行文本框 -->
                  <a-textarea
                    v-else-if="field.config && field.config.type === 'textarea'"
                    v-model="form.dynamicConfig[field.field]"
                    :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                    :rows="3"
                  />
                </a-form-model-item>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <a-icon type="info-circle"/>
            <p>请先完成基本信息配置</p>
          </div>
        </div>

        <!-- 步骤3: 高级设置 -->
        <div v-show="currentStep === 3" class="step-content">
          <div class="step-header">
            <h3>高级设置</h3>
            <p>配置高级参数和扩展选项</p>
          </div>

          <div class="form-section">
            <!-- 高级配置字段 -->
            <div v-if="advancedFields.length > 0" class="advanced-config">
              <div class="config-grid">
                <div
                  v-for="field in advancedFields"
                  :key="field.field"
                  class="config-item"
                >
                  <a-form-model-item
                    :label="field.label"
                    :prop="'dynamicConfig.' + field.field"
                    :rules="field.config && field.config.required ? [{ required: true, message: '请输入' + field.label }] : []"
                  >
                    <!-- 输入框 -->
                    <a-input
                      v-if="field.config && field.config.type === 'input'"
                      v-model="form.dynamicConfig[field.field]"
                      :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                      size="large"
                    />

                    <!-- 密码框 -->
                    <a-input-password
                      v-else-if="field.config && field.config.type === 'password'"
                      v-model="form.dynamicConfig[field.field]"
                      :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                      size="large"
                    />

                    <!-- 数字输入框 -->
                    <a-input-number
                      v-else-if="field.config && field.config.type === 'number'"
                      v-model="form.dynamicConfig[field.field]"
                      :min="field.config && field.config.min"
                      :max="field.config && field.config.max"
                      :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                      style="width: 100%"
                      size="large"
                    />

                    <!-- 下拉选择框 -->
                    <a-select
                      v-else-if="field.config && field.config.type === 'select'"
                      v-model="form.dynamicConfig[field.field]"
                      :placeholder="(field.config && field.config.placeholder) || '请选择' + field.label"
                      size="large"
                    >
                      <a-select-option
                        v-for="option in (field.config && field.config.options) || []"
                        :key="option"
                        :value="option"
                      >
                        {{ option }}
                      </a-select-option>
                    </a-select>

                    <!-- 开关 -->
                    <a-switch
                      v-else-if="field.config && field.config.type === 'switch'"
                      v-model="form.dynamicConfig[field.field]"
                      size="large"
                    />

                    <!-- 多行文本框 -->
                    <a-textarea
                      v-else-if="field.config && field.config.type === 'textarea'"
                      v-model="form.dynamicConfig[field.field]"
                      :placeholder="(field.config && field.config.placeholder) || '请输入' + field.label"
                      :rows="3"
                    />
                  </a-form-model-item>
                </div>
              </div>
            </div>

            <!-- 扩展配置 -->
            <div class="config-section">
              <div class="section-header">
                <a-icon type="code" class="section-icon"/>
                <span class="section-title">扩展配置</span>
                <a-tooltip title="用于配置特定于数据源类型的额外参数">
                  <a-icon type="question-circle" style="margin-left: 8px; color: #999;"/>
                </a-tooltip>
              </div>
              <div class="section-content">
                <a-form-model-item label="扩展配置" prop="extraConfig" class="form-item-large">
                  <a-textarea
                    v-model="form.extraConfig"
                    placeholder="请输入JSON格式的扩展配置（可选）"
                    :rows="6"
                    class="extra-config-textarea"
                  />
                  <div class="form-help-text">
                    <p>扩展配置示例：</p>
                    <div class="help-examples">
                      <div v-if="form.type === 'KAFKA'" class="help-example">
                        <strong>Kafka配置：</strong>
                        <pre class="code-block">{
  "security.protocol": "SASL_SSL",
  "sasl.mechanism": "PLAIN",
  "acks": "all",
  "retries": 3
}</pre>
                      </div>
                      <div v-if="form.type === 'HTTP'" class="help-example">
                        <strong>HTTP配置：</strong>
                        <pre class="code-block">{
  "headers": {
    "Authorization": "Bearer token",
    "Content-Type": "application/json"
  },
  "timeout": 30000
}</pre>
                      </div>
                      <div v-if="form.type === 'MQTT'" class="help-example">
                        <strong>MQTT配置：</strong>
                        <pre class="code-block">{
  "cleanSession": true,
  "keepAliveInterval": 60,
  "qos": 1,
  "retained": false
}</pre>
                      </div>
                    </div>
                  </div>
                </a-form-model-item>
              </div>
            </div>
          </div>
        </div>
      </a-form-model>
    </div>

    <!-- 底部按钮 -->
    <div class="modal-footer">
      <div class="footer-left">
        <a-button @click="handleCancel" size="large">{{ $t('button.cancel') }}
        </a-button>
      </div>
      <div class="footer-right">
        <a-button
          v-if="currentStep > 1"
          @click="prevStep"
          size="large"
          style="margin-right: 12px;"
        >
          上一步
        </a-button>
        <a-button
          v-if="currentStep < 3"
          type="primary"
          @click="nextStep"
          size="large"
        >
          下一步
        </a-button>
        <a-button
          v-if="currentStep === 3"
          type="primary"
          @click="handleOk"
          :loading="confirmLoading"
          size="large"
        >
          完成
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script>
import {getAllResourceTypes} from '@/api/databridge/resource'
import {getPluginTypesByResourceType} from '@/api/databridge/plugin'
import DataBridgeMappings from '@/utils/databridge-mappings'

export default {
  name: 'ResourceForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    formData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      confirmLoading: false,
      currentStep: 1, // 当前步骤
      resourceTypes: {
        databases: {},
        messageQueues: {},
        timeSeries: {},
        searchEngines: {},
        cloudPlatforms: {},
        others: {},
        inputTypes: [],
        outputTypes: [],
        bidirectionalTypes: []
      },
      dynamicFields: [], // 动态配置字段
      availablePlugins: [], // 可用的插件类型
      form: {
        name: '',
        type: '',
        pluginType: '', // 插件类型
        direction: 'OUT', // 默认为输出
        dataDirection: '', // 数据方向
        host: '',
        port: null,
        username: '',
        password: '',
        databaseName: '',
        status: 1,
        description: '',
        extraConfig: '',
        dynamicConfig: {} // 动态配置字段数据
      },
      rules: {
        name: [
          {required: true, message: '请输入数据源名称', trigger: 'blur'}
        ],
        type: [
          {required: true, message: '请选择数据源类型', trigger: 'change'}
        ],
        host: [
          {required: true, message: '请输入主机地址', trigger: 'blur'}
        ],
        port: [
          {required: true, message: '请输入端口', trigger: 'blur'}
        ],
        direction: [
          {required: true, message: '请选择数据方向', trigger: 'change'}
        ]
      }
    }
  },
  computed: {
    // 核心连接字段（主机、端口、用户名、密码等）
    coreConnectionFields() {
      if (!this.dynamicFields || this.dynamicFields.length === 0) return []

      const coreFields = ['host', 'port', 'username', 'password', 'database', 'databaseName', 'region', 'regionId', 'accessKeyId', 'accessKeySecret', 'secretId', 'secretKey', 'instanceId', 'projectId', 'appId', 'appSecret']

      return this.dynamicFields.filter(field =>
        coreFields.includes(field.field) && field.config && field.config.required
      )
    },

    // 高级配置字段（非核心字段）
    advancedFields() {
      if (!this.dynamicFields || this.dynamicFields.length === 0) return []

      const coreFields = ['host', 'port', 'username', 'password', 'database', 'databaseName', 'region', 'regionId', 'accessKeyId', 'accessKeySecret', 'secretId', 'secretKey', 'instanceId', 'projectId', 'appId', 'appSecret']

      return this.dynamicFields.filter(field =>
        !coreFields.includes(field.field) || (field.config && !field.config.required)
      )
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initForm()
      }
    },
    formData: {
      handler() {
        this.initForm()
      },
      deep: true
    }
  },
  methods: {
    initForm() {
      if (this.formData && Object.keys(this.formData).length > 0) {
        this.form = {...this.formData}

        // 从extraConfig中解析dynamicConfig
        if (this.form.extraConfig) {
          try {
            const extraConfigObj = JSON.parse(this.form.extraConfig)

            // 检查是否是旧的嵌套格式
            if (extraConfigObj.dynamicConfig) {
              // 旧格式：{dynamicConfig: {...}, otherConfig: ...}
              this.form.dynamicConfig = extraConfigObj.dynamicConfig
              // 移除dynamicConfig，保留其他扩展配置
              delete extraConfigObj.dynamicConfig
              this.form.extraConfig = Object.keys(extraConfigObj).length > 0 ? JSON.stringify(extraConfigObj) : ''
            } else {
              // 新格式：直接是动态配置 {host: ..., port: ..., ...}
              // 将extraConfig作为dynamicConfig
              this.form.dynamicConfig = extraConfigObj
              this.form.extraConfig = ''
            }
          } catch (error) {
            console.warn('解析extraConfig失败:', error)
            this.form.dynamicConfig = {}
          }
        }

        // 编辑模式时，从第1步开始
        this.currentStep = 1
        // 编辑模式下，如果已有类型，直接加载动态字段
        if (this.form.type) {
          this.loadDynamicFields()
        }
      } else {
        this.form = {
          name: '',
          type: '',
          pluginType: '',
          direction: 'OUT',
          dataDirection: '',
          host: '',
          port: null,
          username: '',
          password: '',
          databaseName: '',
          status: 1,
          description: '',
          extraConfig: '',
          dynamicConfig: {}
        }
        this.currentStep = 1
      }
    },

    // 步骤控制方法
    nextStep() {
      if (this.currentStep === 1) {
        // 验证第一步
        if (!this.form.name) {
          this.$message.error('请输入数据源名称')
          return
        }
        if (!this.form.dataDirection) {
          this.$message.error('请选择数据方向')
          return
        }
        if (!this.form.type) {
          this.$message.error('请选择数据源类型')
          return
        }
        this.currentStep = 2
        // 加载动态字段
        this.loadDynamicFields()
      } else if (this.currentStep === 2) {
        // 验证第二步（如果有动态字段的话）
        if (this.dynamicFields.length > 0) {
          const requiredFields = this.coreConnectionFields.filter(field =>
            field.config && field.config.required
          )
          for (const field of requiredFields) {
            if (!this.form.dynamicConfig[field.field]) {
              this.$message.error(`请输入${field.label}`)
              return
            }
          }
        }
        this.currentStep = 3
      }
    },

    prevStep() {
      if (this.currentStep > 1) {
        this.currentStep--
      }
    },

    // 选择数据方向
    selectDirection(direction) {
      this.form.dataDirection = direction
      this.handleDirectionChange()
    },


    getDirectionMessage() {
      if (!this.form.dataDirection) return ''

      const messages = {
        'INPUT': '数据输入模式：此资源的数据将导入到IoT平台',
        'OUTPUT': '数据输出模式：IoT平台的设备数据将推送到此资源',
        'BIDIRECTIONAL': '双向流转模式：支持数据的双向同步，既可输出也可输入'
      }

      return messages[this.form.dataDirection] || ''
    },

    getDirectionType() {
      if (!this.form.dataDirection) return 'info'

      const types = {
        'INPUT': 'processing',
        'OUTPUT': 'success',
        'BIDIRECTIONAL': 'warning'
      }

      return types[this.form.dataDirection] || 'info'
    },

    handleOk() {
      // 如果是分步模式，确保在最后一步
      if (this.currentStep < 3) {
        this.nextStep()
        return
      }

      this.$refs.form.validate(valid => {
        if (valid) {
          this.confirmLoading = true

          const formData = {...this.form}

          // 将动态配置直接作为extraConfig
          if (this.form.dynamicConfig && Object.keys(this.form.dynamicConfig).length > 0) {
            try {
              // 合并动态配置和其他扩展配置
              let extraConfigObj = {}

              // 解析现有的extraConfig（除了dynamicConfig之外的其他配置）
              if (this.form.extraConfig) {
                try {
                  const parsed = JSON.parse(this.form.extraConfig)
                  // 如果解析成功，说明是其他扩展配置
                  extraConfigObj = parsed
                } catch (e) {
                  // 如果解析失败，说明可能是旧的嵌套格式，忽略
                  extraConfigObj = {}
                }
              }

              // 将动态配置直接合并到extraConfig中
              const finalConfig = {
                ...extraConfigObj,
                ...this.form.dynamicConfig
              }

              formData.extraConfig = JSON.stringify(finalConfig)
            } catch (error) {
              this.$message.error('配置合并失败: ' + error.message)
              this.confirmLoading = false
              return
            }
          }

          // 验证扩展配置JSON格式
          if (formData.extraConfig) {
            try {
              JSON.parse(formData.extraConfig)
            } catch (error) {
              this.$message.error('扩展配置JSON格式错误')
              this.confirmLoading = false
              return
            }
          }

          // 移除dynamicConfig字段，因为已经合并到extraConfig中
          delete formData.dynamicConfig

          this.$emit('ok', formData)
          this.confirmLoading = false
        }
      })
    },

    handleCancel() {
      this.$emit('cancel')
    },

    // 加载数据源类型
    async loadResourceTypes(direction = null) {
      try {
        const params = direction ? {direction} : {}
        const response = await getAllResourceTypes(params)
        if (response.code === 0) {
          this.resourceTypes = response.data
        } else {
          console.warn('API返回错误，使用默认数据源类型')
          this.loadDefaultResourceTypes()
        }
      } catch (error) {
        console.error('加载数据源类型失败:', error)
        console.warn('使用默认数据源类型作为降级方案')
        this.loadDefaultResourceTypes()
      }
    },

    // 加载默认数据源类型（降级方案）
    loadDefaultResourceTypes() {
      this.resourceTypes = {
        databases: {
          'MYSQL': 'MySQL数据库',
          'POSTGRESQL': 'PostgreSQL数据库',
          'ORACLE': 'Oracle数据库'
        },
        messageQueues: {
          'KAFKA': 'Kafka消息队列',
          'RABBITMQ': 'RabbitMQ消息队列'
        },
        timeSeries: {
          'IOTDB': 'IoTDB时序数据库',
          'INFLUXDB': 'InfluxDB时序数据库'
        },
        searchEngines: {
          'ELASTICSEARCH': 'Elasticsearch搜索引擎'
        },
        cloudPlatforms: {
          'ALIYUN_IOT': '阿里云IoT平台',
          'TENCENT_IOT': '腾讯云IoT平台'
        },
        others: {
          'HTTP': 'HTTP接口',
          'MQTT': 'MQTT协议',
          'REDIS': 'Redis缓存'
        },
        inputTypes: ['ALIYUN_IOT', 'TENCENT_IOT', 'MYSQL', 'KAFKA', 'MQTT'],
        outputTypes: ['IOTDB', 'INFLUXDB', 'ELASTICSEARCH', 'MYSQL', 'KAFKA', 'MQTT', 'HTTP', 'REDIS'],
        bidirectionalTypes: ['MYSQL', 'KAFKA', 'MQTT', 'HTTP', 'REDIS']
      }
    },

    // 加载可用的插件类型
    async loadAvailablePlugins() {
      if (!this.form.type) {
        this.availablePlugins = []
        return
      }

      try {
        const response = await getPluginTypesByResourceType(this.form.type)
        if (response.code === 0) {
          this.availablePlugins = response.data || []

          // 如果只有一个插件类型，自动选择
          if (this.availablePlugins.length === 1) {
            this.form.pluginType = this.availablePlugins[0].value
          }
        } else {
          console.warn('API返回错误，使用默认插件类型')
          this.loadDefaultPlugins()
        }
      } catch (error) {
        console.error('加载插件类型失败:', error)
        console.warn('使用默认插件类型作为降级方案')
        this.loadDefaultPlugins()
      }
    },

    // 加载默认插件类型（降级方案）
    loadDefaultPlugins() {
      // 根据资源类型提供默认的插件类型
      const defaultPlugins = {
        'MYSQL': [{value: 'JDBC', label: 'JDBC连接器'}],
        'POSTGRESQL': [{value: 'JDBC', label: 'JDBC连接器'}],
        'KAFKA': [{value: 'KAFKA', label: 'Kafka连接器'}],
        'IOTDB': [{value: 'IOTDB', label: 'IoTDB连接器'}],
        'INFLUXDB': [{value: 'INFLUXDB', label: 'InfluxDB连接器'}],
        'ELASTICSEARCH': [{value: 'ELASTICSEARCH', label: 'Elasticsearch连接器'}],
        'HTTP': [{value: 'HTTP', label: 'HTTP连接器'}],
        'MQTT': [{value: 'MQTT', label: 'MQTT连接器'}],
        'REDIS': [{value: 'REDIS', label: 'Redis连接器'}]
      }

      this.availablePlugins = defaultPlugins[this.form.type] || [{
        value: this.form.type,
        label: `${this.form.type}连接器`
      }]

      // 如果只有一个插件类型，自动选择
      if (this.availablePlugins.length === 1) {
        this.form.pluginType = this.availablePlugins[0].value
      }
    },

    // 处理数据方向变化
    async handleDirectionChange() {
      // 清空已选择的数据源类型
      this.form.type = ''

      // 根据数据方向自动设置数据源管理的方向
      switch (this.form.dataDirection) {
        case 'INPUT':
          this.form.direction = 'IN'
          break
        case 'OUTPUT':
          this.form.direction = 'OUT'
          break
        case 'BIDIRECTIONAL':
          this.form.direction = 'BOTH'
          break
        default:
          this.form.direction = 'OUT'
      }

      // 重新加载对应流向的数据源类型
      await this.loadResourceTypes(this.form.dataDirection)
    },

    // 处理数据源类型变化
    async handleTypeChange() {
      // 清空插件类型
      this.form.pluginType = ''
      this.availablePlugins = []

      // 设置默认端口
      this.setDefaultPort()

      // 根据数据方向自动设置方向
      switch (this.form.dataDirection) {
        case 'INPUT':
          this.form.direction = 'IN'
          break
        case 'OUTPUT':
          this.form.direction = 'OUT'
          break
        case 'BIDIRECTIONAL':
          this.form.direction = 'BOTH'
          break
        default:
          this.form.direction = 'OUT'
      }

      // 加载可用的插件类型
      await this.loadAvailablePlugins()

      // 加载动态配置字段
      await this.loadDynamicFields()
    },

    // 处理插件类型变化
    handlePluginTypeChange() {
      // 插件类型变化时，可以重新加载动态字段
      this.loadDynamicFields()
    },

    // 设置默认端口
    setDefaultPort() {
      const defaultPorts = {
        'MYSQL': 3306,
        'REDIS': 6379,
        'KAFKA': 9092,
        'MQTT': 1883,
        'HTTP': 80,
        'IOTDB': 6667,
        'INFLUXDB': 8086,
        'ELASTICSEARCH': 9200,
        'ALIYUN_IOT': 443,
        'TENCENT_IOT': 443,
        'HUAWEI_IOT': 443
      }

      if (this.form.type && defaultPorts[this.form.type]) {
        this.form.port = defaultPorts[this.form.type]
      }
    },

    getTypeColor(type) {
      const colorMap = {
        'MYSQL': 'blue',
        'REDIS': 'red',
        'KAFKA': 'orange',
        'MQTT': 'green',
        'IOTDB': 'purple',
        'INFLUXDB': 'cyan',
        'ELASTICSEARCH': 'geekblue',
        'HTTP': 'magenta',
        'ALIYUN_IOT': 'gold',
        'TENCENT_IOT': 'lime'
      }
      return colorMap[type] || 'default'
    },

    getPluginTypeColor(pluginType) {
      return DataBridgeMappings.getPluginType(pluginType).color
    },

    // 加载动态配置字段
    async loadDynamicFields() {
      if (!this.form.type) {
        this.dynamicFields = []
        return
      }

      try {
        // 根据数据源类型获取对应的字典类型
        const dictType = `databridge_${this.form.type.toLowerCase()}_fields`
        console.log('加载动态字段:', dictType)

        // 使用字典查询功能
        const dictArray = [dictType]
        const response = await this.getDictMap(dictArray)

        console.log('字典查询响应:', response)

        if (response && response.code === 0 && response.data && response.data[dictType]) {
          console.log('字典数据:', response.data[dictType])
          // 转换字典数据为动态字段配置
          const fields = response.data[dictType].map(item => {
            console.log('处理字典项:', item)

            let config = {}
            try {
              if (item.remark) {
                config = JSON.parse(item.remark)
                console.log('解析成功:', config)
              } else {
                console.warn('remark字段为空:', item)
                config = {}
              }
            } catch (e) {
              console.warn('解析动态字段配置失败:', item.remark, e)
              config = {}
            }

            const fieldConfig = {
              field: item.dictValue,
              label: item.dictLabel,
              config: config
            }

            console.log('字段配置:', fieldConfig)
            return fieldConfig
          })

          // 使用 Object.freeze 来避免Vue响应式系统的影响
          this.dynamicFields = Object.freeze(fields)

          console.log('最终动态字段:', this.dynamicFields)

          // 初始化动态字段的默认值
          this.initDynamicFieldDefaults()
        } else {
          this.dynamicFields = []
        }
      } catch (error) {
        console.error('加载动态字段失败:', error)
        this.dynamicFields = []
      }
    },

    // 初始化动态字段的默认值
    initDynamicFieldDefaults() {
      console.log('开始初始化动态字段默认值:', this.dynamicFields)

      // 确保 dynamicConfig 对象存在
      if (!this.form.dynamicConfig) {
        this.$set(this.form, 'dynamicConfig', {})
      }

      // 直接使用冻结的字段，避免Vue响应式系统的影响
      this.dynamicFields.forEach((field, index) => {
        console.log(`处理字段 ${index}:`, field)

        // 安全检查：确保所有必要的属性都存在
        if (field &&
          field.field &&
          field.config &&
          field.config.default !== undefined &&
          this.form.dynamicConfig &&
          !this.form.dynamicConfig[field.field]) {
          console.log(`设置默认值: ${field.field} = ${field.config.default}`)
          this.$set(this.form.dynamicConfig, field.field, field.config.default)
        } else {
          console.log(`跳过字段 ${field ? field.field : 'undefined'}:`, {
            fieldExists: !!field,
            fieldFieldExists: !!(field && field.field),
            configExists: !!(field && field.config),
            hasDefault: !!(field && field.config && field.config.default !== undefined),
            dynamicConfigExists: !!this.form.dynamicConfig,
            alreadySet: !!(field && field.field && this.form.dynamicConfig && this.form.dynamicConfig[field.field])
          })
        }
      })

      console.log('动态配置初始化完成:', this.form.dynamicConfig)
    }
  },

  async created() {
    await this.loadResourceTypes()
  }
}
</script>

<style scoped>
/* 步骤指示器样式 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #d9d9d9;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.step-item.active .step-number {
  background: #1890ff;
}

.step-item.completed .step-number {
  background: #52c41a;
}

.step-title {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.step-item.active .step-title {
  color: #1890ff;
}

.step-line {
  width: 120px;
  height: 2px;
  background: #d9d9d9;
  margin: 0 16px;
  margin-top: -20px;
  transition: all 0.3s ease;
}

.step-line.active {
  background: #1890ff;
}

/* 表单容器样式 */
.form-container {
  padding: 16px;
  min-height: 300px;
}

.step-content {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-header {
  margin-bottom: 16px;
  text-align: center;
}

.step-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
}

.step-header p {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

/* 表单区域样式 */
.form-section {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 20px;
}

.form-item-large {
  margin-bottom: 16px;
}

.form-help-text {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
  line-height: 1.4;
}

/* 数据方向卡片样式 */
.direction-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
  margin-top: 6px;
}

.direction-card {
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  background: #fff;
}

.direction-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.direction-card.active {
  border-color: #1890ff;
  background: #f6ffed;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.card-icon {
  font-size: 20px;
  color: #1890ff;
  margin-bottom: 8px;
}

.card-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 3px 0;
}

.card-content p {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
  margin: 0 0 6px 0;
}

.card-desc {
  font-size: 11px;
  color: #8c8c8c;
  line-height: 1.3;
}

.card-check {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 20px;
  height: 20px;
  background: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
}

/* 配置网格样式 */
.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-top: 16px;
}

.config-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e8e8e8;
}

/* 配置区域样式 */
.config-section {
  margin-top: 32px;
  background: #fafafa;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e8e8e8;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.section-icon {
  font-size: 18px;
  color: #1890ff;
  margin-right: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.section-content {
  margin-top: 16px;
}

/* 状态单选组样式 */
.status-radio-group {
  display: flex;
  gap: 24px;
}

.status-radio {
  display: flex;
  align-items: center;
  font-size: 14px;
}

/* 扩展配置样式 */
.extra-config-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.help-examples {
  margin-top: 12px;
}

.help-example {
  margin-bottom: 16px;
}

.help-example strong {
  display: block;
  margin-bottom: 8px;
  color: #262626;
  font-size: 13px;
}

.code-block {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #262626;
  overflow-x: auto;
  margin: 0;
  line-height: 1.4;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #8c8c8c;
}

.empty-state .anticon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #d9d9d9;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 底部按钮样式 */
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
}

.footer-left {
  flex: 1;
}

.footer-right {
  display: flex;
  align-items: center;
}


@media (max-width: 1200px) {
  .direction-cards {
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    gap: 10px;
  }

  .form-container {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .direction-cards {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }

  .step-indicator {
    padding: 12px 16px;
  }

  .step-line {
    width: 40px;
  }

  .form-container {
    padding: 16px;
  }

  .modal-footer {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }

  .footer-right {
    width: 100%;
    justify-content: center;
  }

  .direction-card {
    padding: 12px;
  }

  .card-icon {
    font-size: 20px;
    margin-bottom: 8px;
  }

  .card-content h4 {
    font-size: 14px;
  }

  .card-content p {
    font-size: 12px;
  }

  .card-desc {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .step-indicator {
    padding: 8px 12px;
  }

  .step-line {
    width: 20px;
  }

  .form-container {
    padding: 12px;
  }

  .direction-card {
    padding: 10px;
  }
}

/* 数据源类型选择器样式 */
.resource-type-select {
  width: 100%;
}

.resource-type-select .ant-select-selection {
  border-radius: 6px;
}

/* 插件类型选择器样式 */
.plugin-type-select {
  width: 100%;
}

.plugin-type-select .ant-select-selection {
  border-radius: 6px;
}

/* 插件选项样式 */
.plugin-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.plugin-name {
  flex: 1;
}

/* 描述文本框样式 */
.description-textarea {
  resize: vertical;
  min-height: 80px;
}

/* 表单验证样式增强 */
.ant-form-item-has-error .direction-card {
  border-color: #ff4d4f;
}

.ant-form-item-has-error .direction-card.active {
  border-color: #ff4d4f;
  background: #fff2f0;
}
</style>
