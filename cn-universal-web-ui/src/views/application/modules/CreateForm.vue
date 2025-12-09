<template>
  <a-drawer width="50%" :visible="open" @close="onClose" class="application-form-drawer">
    <div class="drawer-header">
      <h3 class="drawer-title">{{ formTitle }}</h3>
    </div>
    <div class="drawer-content">
      <a-form-model ref="form" :model="form" :rules="rules" layout="vertical">
        <!-- 应用名称 -->
        <a-form-model-item :label="$t('app.appName')" prop="appName" v-if="formType!=3">
          <a-input
            v-model="form.appName"
            placeholder="请输入应用名称"
            size="large"
            class="form-input"
          >
            <a-icon slot="prefix" type="appstore"/>
          </a-input>
        </a-form-model-item>

        <!-- 备注 -->
        <a-form-model-item :label="$t('common.remark')" prop="remark" v-if="formType!=3">
          <a-textarea
            v-model="form.remark"
            placeholder="请输入备注信息"
            :rows="4"
            size="large"
            class="form-input"
            :maxlength="200"
            show-count
          >
            <a-icon slot="prefix" type="edit"/>
          </a-textarea>
        </a-form-model-item>

        <!-- 推送方式选择 -->
        <a-form-model-item label="推送方式" prop="pushMethod" v-if="formType===3">
          <a-select
            v-model="pushMethod"
            size="large"
            class="form-input"
            @change="onPushMethodChange"
          >
            <a-select-option value="HTTP" :disabled="!getPushMethodSupport('HTTP')">
              <a-icon type="global"/>
              HTTP推送
              <span v-if="!getPushMethodSupport('HTTP')" class="dev-tag">(开发中)</span>
            </a-select-option>
            <a-select-option value="RocketMQ" :disabled="!getPushMethodSupport('RocketMQ')">
              <a-icon type="message"/>
              RocketMQ
              <span v-if="!getPushMethodSupport('RocketMQ')" class="dev-tag">(开发中)</span>
            </a-select-option>
            <a-select-option value="Kafka" :disabled="!getPushMethodSupport('Kafka')">
              <a-icon type="cluster"/>
              Kafka
              <span v-if="!getPushMethodSupport('Kafka')" class="dev-tag">(开发中)</span>
            </a-select-option>
            <a-select-option value="JDBC" :disabled="!getPushMethodSupport('JDBC')">
              <a-icon type="database"/>
              JDBC插入
              <span v-if="!getPushMethodSupport('JDBC')" class="dev-tag">(开发中)</span>
            </a-select-option>
          </a-select>
        </a-form-model-item>

        <!-- HTTP推送配置 -->
        <template v-if="formType===3 && pushMethod==='HTTP'">
          <template v-for="field in getPushFields('HTTP')">
            <a-form-model-item
              :key="field.dictValue"
              :label="field.dictLabel"
              :prop="getFieldProp(field.dictValue, 'HTTP')"
              :extra="getFieldExtra(field.dictValue)"
            >
              <!-- 特殊字段处理 -->
              <template v-if="field.dictValue === 'url'">
                <div class="input-with-button">
                  <a-input
                    v-model="form.notifyUrl"
                    placeholder="请输入回调地址,支持http(s)"
                    size="large"
                    class="form-input"
                  >
                    <a-icon slot="prefix" type="global"/>
                  </a-input>
                  <a-button type="primary" size="small" @click="checkUrl" class="test-btn">
                    测试地址
                  </a-button>
                </div>
              </template>

              <template v-else-if="field.dictValue === 'secret'">
                <div class="input-with-button">
                  <a-input
                    v-model="form.httpSecret"
                    placeholder="请输入32位推送密钥"
                    allow-clear
                    size="large"
                    class="form-input"
                    maxlength="32"
                  >
                    <a-icon slot="prefix" type="key"/>
                  </a-input>
                  <a-button type="dashed" size="small" @click="generateSecret" class="generate-btn">
                    <a-icon type="reload"/>
                  </a-button>
                </div>
              </template>

              <template v-else-if="field.dictValue === 'header'">
                <a-input
                  v-model="form.httpHeader"
                  placeholder="AuthHeader"
                  allow-clear
                  size="large"
                  class="form-input"
                >
                  <a-icon slot="prefix" type="tag"/>
                </a-input>
              </template>

              <!-- 默认字段处理 -->
              <template v-else>
                <a-input
                  v-model="form[getFieldPath(field.dictValue, 'HTTP')]"
                  :placeholder="`请输入${field.dictLabel}`"
                  size="large"
                  class="form-input"
                >
                  <a-icon slot="prefix" :type="getFieldIcon(field.dictValue)"/>
                </a-input>
              </template>
            </a-form-model-item>
          </template>
        </template>

        <!-- 其他推送方式配置 -->
        <template v-if="formType===3 && pushMethod!='HTTP'">
          <!-- 不支持该推送方式时显示开发中提示 -->
          <template v-if="!getPushMethodSupport(pushMethod)">
            <a-alert
              message="功能开发中"
              description="该推送方式正在开发中，敬请期待！"
              type="info"
              show-icon
              class="dev-alert"
            />
          </template>

          <!-- 支持该推送方式时显示配置表单 -->
          <template v-else>
            <!-- RocketMQ配置 -->
            <template v-if="pushMethod==='RocketMQ'">
              <template v-for="field in getPushFields('RocketMQ')">
                <a-form-model-item
                  :key="field.dictValue"
                  :label="field.dictLabel"
                  :prop="getFieldProp(field.dictValue, 'RocketMQ')"
                >
                  <a-input
                    v-model="form[getFieldPath(field.dictValue, 'RocketMQ')]"
                    :placeholder="`请输入${field.dictLabel}`"
                    size="large"
                    class="form-input"
                  >
                    <a-icon slot="prefix" :type="getFieldIcon(field.dictValue)"/>
                  </a-input>
                </a-form-model-item>
              </template>
            </template>

            <!-- Kafka配置 -->
            <template v-if="pushMethod==='Kafka'">
              <template v-for="field in getPushFields('Kafka')">
                <a-form-model-item
                  :key="field.dictValue"
                  :label="field.dictLabel"
                  :prop="getFieldProp(field.dictValue, 'Kafka')"
                >
                  <a-input
                    v-model="form[getFieldPath(field.dictValue, 'Kafka')]"
                    :placeholder="`请输入${field.dictLabel}`"
                    size="large"
                    class="form-input"
                  >
                    <a-icon slot="prefix" :type="getFieldIcon(field.dictValue)"/>
                  </a-input>
                </a-form-model-item>
              </template>
            </template>

            <!-- JDBC配置 -->
            <template v-if="pushMethod==='JDBC'">
              <template v-for="field in getPushFields('JDBC')">
                <a-form-model-item
                  :key="field.dictValue"
                  :label="field.dictLabel"
                  :prop="getFieldProp(field.dictValue, 'JDBC')"
                >
                  <!-- 特殊字段处理 -->
                  <template v-if="field.dictValue === 'type'">
                    <a-select
                      v-model="form.jdbc.type"
                      placeholder="请选择数据库类型"
                      size="large"
                      class="form-input"
                    >
                      <a-select-option value="mysql">MySQL</a-select-option>
                      <a-select-option value="oracle">Oracle</a-select-option>
                      <a-select-option value="postgresql">PostgreSQL</a-select-option>
                      <a-select-option value="sqlite">SQLite</a-select-option>
                    </a-select>
                  </template>

                  <template v-else-if="field.dictValue === 'password'">
                    <a-input-password
                      v-model="form[getFieldPath(field.dictValue, 'JDBC')]"
                      :placeholder="`请输入${field.dictLabel}`"
                      size="large"
                      class="form-input"
                    >
                      <a-icon slot="prefix" :type="getFieldIcon(field.dictValue)"/>
                    </a-input-password>
                  </template>

                  <!-- 默认字段处理 -->
                  <template v-else>
                    <a-input
                      v-model="form[getFieldPath(field.dictValue, 'JDBC')]"
                      :placeholder="`请输入${field.dictLabel}`"
                      size="large"
                      class="form-input"
                    >
                      <a-icon slot="prefix" :type="getFieldIcon(field.dictValue)"/>
                    </a-input>
                  </template>
                </a-form-model-item>
              </template>
            </template>
          </template>
        </template>

        <!-- 底部按钮 -->
        <div class="bottom-control">
          <a-space size="large">
            <a-button type="primary" @click="submitForm" class="btn-primary-small">
              <a-icon type="save"/>
              保存
            </a-button>
            <a-button @click="cancel" class="btn-default-small">
              <a-icon type="close"/>
              取消
            </a-button>
          </a-space>
        </div>
      </a-form-model>
    </div>
  </a-drawer>
</template>

<script>
import {addApplication, checkUrl, getApplication, updateApplication} from '@/api/application/application'

export default {
  name: 'CreateForm',
  props: {
    statusOptions: {
      type: Array,
      required: true
    },
    deleteOptions: {
      type: Array,
      required: true
    }
  },
  components: {},
  data() {
    return {
      unionId: null,
      clientID: null,
      appUnionId: null,
      loading: false,
      formTitle: '',
      // 表单参数
      form: {
        appName: null,
        appUniqueId: null,
        appId: null,
        upTopic: null,
        downTopic: null,
        notifyUrl: null,
        appSecret: null,
        validEndDate: null,
        appStatus: '0',
        instance: null,
        remark: null,
        createDate: null,
        deleted: null,
        httpSecret: '',
        httpHeader: 'AuthToken',
        cfg: null,
        // 其他推送方式配置字段
        rocketMQ: {
          namesrvAddr: '',
          topic: '',
          group: ''
        },
        kafka: {
          bootstrapServers: '',
          topic: ''
        },
        mqtt: {
          url: '',
          topic: ''
        },
        jdbc: {
          type: 'mysql',
          url: '',
          username: '',
          password: ''
        }
      },
      pushMethod: 'HTTP',
      // 1增加,2修改,3开发配置
      formType: 1,
      open: false,
      // 字典配置数据
      pushFields: {
        HTTP: [],
        RocketMQ: [],
        Kafka: [],
        MQTT: [],
        JDBC: []
      },
      rules: {
        appName: [
          {required: true, message: '应用名不能为空', trigger: 'blur'}
        ],
        notifyUrl: [
          {
            required: true,
            message: '推送地址不能为空',
            trigger: 'blur'
          },
          {
            match: true,
            // eslint-disable-next-line no-useless-escape
            pattern: '((http|ftp|https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?',
            message: '地址不合法'
          }
        ],
        httpSecret: [
          {
            required: true,
            message: 'HTTP推送密钥不能为空',
            trigger: 'blur'
          },
          {
            min: 6,
            max: 32,
            message: '推送密钥长度应在6-32位之间',
            trigger: 'blur'
          }
        ],
        httpHeader: [
          {
            required: true,
            message: 'HTTP消息头不能为空',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  filters: {},
  created() {
  },
  computed: {},
  watch: {},
  mounted() {
    // 组件挂载时加载字典配置
    this.loadPushFields().catch(error => {
      console.error('组件挂载时加载字典配置失败:', error)
    })
  },
  methods: {
    onClose() {
      this.open = false
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.formType = 1
      this.pushMethod = 'HTTP'
      this.form = {
        appName: null,
        appUniqueId: null,
        appId: null,
        upTopic: null,
        downTopic: null,
        notifyUrl: null,
        appSecret: null,
        validEndDate: null,
        appStatus: '0',
        instance: null,
        remark: null,
        createDate: null,
        deleted: null,
        httpSecret: '',
        httpHeader: 'AuthToken',
        cfg: null,
        // 其他推送方式配置字段
        rocketMQ: {
          namesrvAddr: '',
          topic: '',
          group: ''
        },
        kafka: {
          bootstrapServers: '',
          topic: ''
        },
        jdbc: {
          type: 'mysql',
          url: '',
          username: '',
          password: ''
        }
      }
    },
    // 推送方式变更
    onPushMethodChange(value) {
      this.pushMethod = value
      // 只重置 notifyUrl，保留 httpSecret 和 httpHeader
      if (value === 'HTTP') {
        this.form.notifyUrl = null
      }
    },
    // 生成随机密钥
    generateSecret() {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
      let result = ''
      for (let i = 0; i < 32; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length))
      }
      this.form.httpSecret = result
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.validateField('httpSecret')
        this.$message.success('32位密钥已自动生成')
      })
    },
    // 构建配置JSON
    buildConfigJson() {
      // 获取现有配置或创建新配置
      let config = {}
      if (this.form.cfg) {
        try {
          config = JSON.parse(this.form.cfg)
        } catch (error) {
          config = {}
        }
      }

      // 根据字典配置动态构建配置
      if (this.pushMethod === 'HTTP' && this.getPushMethodSupport('HTTP')) {
        // 保留原有配置，避免覆盖 enable 和 support 等字段
        if (!config.http) {
          config.http = {}
        }
        this.getPushFields('HTTP').forEach(field => {
          const value = this.form[this.getFieldPath(field.dictValue, 'HTTP')]
          if (value !== undefined && value !== null && value !== '') {
            config.http[field.dictValue] = value
          }
        })
      }

      if (this.pushMethod === 'RocketMQ' && this.getPushMethodSupport('RocketMQ')) {
        // 保留原有配置，避免覆盖 enable 和 support 等字段
        if (!config.rocketMQ) {
          config.rocketMQ = {}
        }
        this.getPushFields('RocketMQ').forEach(field => {
          const value = this.form[this.getFieldPath(field.dictValue, 'RocketMQ')]
          if (value !== undefined && value !== null && value !== '') {
            config.rocketMQ[field.dictValue] = value
          }
        })
      }

      if (this.pushMethod === 'Kafka' && this.getPushMethodSupport('Kafka')) {
        // 保留原有配置，避免覆盖 enable 和 support 等字段
        if (!config.kafka) {
          config.kafka = {}
        }
        this.getPushFields('Kafka').forEach(field => {
          const value = this.form[this.getFieldPath(field.dictValue, 'Kafka')]
          if (value !== undefined && value !== null && value !== '') {
            config.kafka[field.dictValue] = value
          }
        })
      }

      if (this.pushMethod === 'JDBC' && this.getPushMethodSupport('JDBC')) {
        // 保留原有配置，避免覆盖 enable 和 support 等字段
        if (!config.jdbc) {
          config.jdbc = {}
        }
        this.getPushFields('JDBC').forEach(field => {
          const value = this.form[this.getFieldPath(field.dictValue, 'JDBC')]
          if (value !== undefined && value !== null && value !== '') {
            config.jdbc[field.dictValue] = value
          }
        })
      }

      // 确保其他推送方式存在（即使为空对象）
      if (!config.kafka) {
        config.kafka = {}
      }
      if (!config.rocketMQ) {
        config.rocketMQ = {}
      }
      if (!config.mqtt) {
        config.mqtt = {}
      }
      if (!config.jdbc) {
        config.jdbc = {}
      }

      return JSON.stringify(config)
    },
    // 解析配置JSON
    parseConfigJson(cfgStr) {
      if (!cfgStr) {
        return
      }
      try {
        const config = JSON.parse(cfgStr)

        // 根据字典配置动态解析配置
        if (config.http && Object.keys(config.http).length > 0) {
          this.pushMethod = 'HTTP'
          this.getPushFields('HTTP').forEach(field => {
            const value = config.http[field.dictValue]
            if (value !== undefined) {
              this.form[this.getFieldPath(field.dictValue, 'HTTP')] = value
            }
          })
        } else if (config.rocketMQ && Object.keys(config.rocketMQ).length > 0) {
          this.pushMethod = 'RocketMQ'
          this.getPushFields('RocketMQ').forEach(field => {
            const value = config.rocketMQ[field.dictValue]
            if (value !== undefined) {
              this.form[this.getFieldPath(field.dictValue, 'RocketMQ')] = value
            }
          })
        } else if (config.kafka && Object.keys(config.kafka).length > 0) {
          this.pushMethod = 'Kafka'
          this.getPushFields('Kafka').forEach(field => {
            const value = config.kafka[field.dictValue]
            if (value !== undefined) {
              this.form[this.getFieldPath(field.dictValue, 'Kafka')] = value
            }
          })
        } else if (config.jdbc && Object.keys(config.jdbc).length > 0) {
          this.pushMethod = 'JDBC'
          this.getPushFields('JDBC').forEach(field => {
            const value = config.jdbc[field.dictValue]
            if (value !== undefined) {
              this.form[this.getFieldPath(field.dictValue, 'JDBC')] = value
            }
          })
        } else {
          // 默认设置为HTTP推送方式
          this.pushMethod = 'HTTP'
        }
      } catch (error) {
        console.error('解析配置JSON失败:', error)
        this.pushMethod = 'HTTP'
      }
    },
    /** 推送地址测试 */
    checkUrl() {
      this.$refs.form.validate(valid => {
        if (valid) {
          checkUrl({notifyUrl: this.form.notifyUrl}).then(response => {
            if (response.msg === '200') {
              this.$message.success('该地址可正常访问')
            } else {
              this.$message.error(response.msg)
            }
          })
        }
      })
    },
    /** mq上行赋值 */
    uplink() {
      if (this.pushMethod === 'HTTP') {
        this.form.upTopic = this.unionId + '/' + this.clientID + '/#'
      }
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset()
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
    },
    /** 修改应用信息操作 */
    handleUpdate(row, ids) {
      this.reset()
      this.formType = 2
      const uuid = row ? row.appUniqueId : ids
      getApplication(uuid).then(response => {
        this.form = response.data
        this.form.appStatus = this.form.appStatus.toString()
        this.form.deleted = this.form.deleted.toString()
        this.open = true
        this.formTitle = '修改应用信息'
      })
    },
    /** 修改开发信息操作 */
    handleDevelopUpdate(row, ids, clientID, unionId) {
      this.reset()
      this.clientID = clientID
      this.unionId = unionId
      this.appUnionId = ids
      this.formType = 3
      const uuid = row ? row.appUniqueId : ids

      // 确保字典配置已加载
      Promise.all([
        this.loadPushFields(),
        getApplication(uuid)
      ]).then(([_, response]) => {
        // 只合并后端数据，不覆盖本地form结构
        Object.assign(this.form, response.data)
        this.form.appStatus = this.form.appStatus.toString()
        this.form.deleted = this.form.deleted.toString()
        // 只在首次打开时解析配置
        this.parseConfigJson(this.form.cfg)
        this.open = true
        this.formTitle = '修改推送配置'
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      // 动态验证规则
      const validateRules = {}

      if (this.formType === 3) {
        if (this.getPushMethodSupport(this.pushMethod)) {
          // 根据字典配置动态生成验证规则
          this.getPushFields(this.pushMethod).forEach(field => {
            const prop = this.getFieldProp(field.dictValue, this.pushMethod)
            validateRules[prop] = [
              {required: true, message: `${field.dictLabel}不能为空`, trigger: 'blur'}
            ]

            // 特殊字段的验证规则
            if (this.pushMethod === 'HTTP' && field.dictValue === 'url') {
              validateRules[prop] = this.rules.notifyUrl
            } else if (this.pushMethod === 'HTTP' && field.dictValue === 'secret') {
              validateRules[prop] = this.rules.httpSecret
            } else if (this.pushMethod === 'HTTP' && field.dictValue === 'header') {
              validateRules[prop] = this.rules.httpHeader
            }
          })
        }
      } else if (this.formType !== 3) {
        validateRules.appName = this.rules.appName
      }

      this.$refs.form.validate(valid => {
        if (valid) {
          // 构建配置JSON
          this.form.cfg = this.buildConfigJson()

          if (this.form.appUniqueId !== undefined && this.form.appUniqueId !== null) {
            updateApplication(this.form).then(response => {
              this.$message.success('修改成功', 3)
              this.open = false
              this.$emit('ok')
            })
          } else {
            addApplication(this.form).then(response => {
              this.$message.success('新增成功', 3)
              this.open = false
              this.$emit('ok')
            })
          }
        } else {
          return false
        }
      })
    },
    // 加载推送字段配置
    loadPushFields() {
      const dictArray = [
        'app_push_http',
        // 'app_push_mqtt',
        'app_push_rocketmq',
        'app_push_kafka',
        'app_push_jdbc'
      ]

      return this.getDictMap(dictArray).then(res => {
        this.pushFields.HTTP = res.data['app_push_http'] || []
        // this.pushFields.MQTT = res.data['app_push_mqtt'] || []
        this.pushFields.RocketMQ = res.data['app_push_rocketmq'] || []
        this.pushFields.Kafka = res.data['app_push_kafka'] || []
        this.pushFields.JDBC = res.data['app_push_jdbc'] || []

        console.log('推送字段配置加载完成:', this.pushFields)
        return res
      }).catch(error => {
        console.error('加载推送字段配置失败:', error)
        throw error
      })
    },

    // 获取推送字段
    getPushFields(method) {
      return this.pushFields[method] || []
    },

    // 获取字段属性名
    getFieldProp(fieldValue, method) {
      if (method === 'HTTP') {
        const fieldMap = {
          'url': 'notifyUrl',
          'secret': 'httpSecret',
          'header': 'httpHeader'
        }
        return fieldMap[fieldValue] || fieldValue
      }
      return `${method.toLowerCase()}.${fieldValue}`
    },

    // 获取字段路径
    getFieldPath(fieldValue, method) {
      if (method === 'HTTP') {
        const fieldMap = {
          'url': 'notifyUrl',
          'secret': 'httpSecret',
          'header': 'httpHeader'
        }
        return fieldMap[fieldValue] || fieldValue
      }
      return `${method.toLowerCase()}.${fieldValue}`
    },

    // 获取字段图标
    getFieldIcon(fieldValue) {
      const iconMap = {
        'url': 'global',
        'secret': 'key',
        'header': 'tag',
        'namesrvAddr': 'cluster',
        'topic': 'message',
        'group': 'team',
        'bootstrapServers': 'cluster',
        'username': 'user',
        'password': 'lock',
        'type': 'database'
      }
      return iconMap[fieldValue] || 'setting'
    },

    // 获取字段说明
    getFieldExtra(fieldValue) {
      const extraMap = {
        'url': 'HTTP推送，请求方法要求POST，数据格式为application/json',
        'secret': '用于HTTP请求鉴权，将在请求头中携带',
        'header': 'HTTP请求头名称，用于携带推送密钥'
      }
      return extraMap[fieldValue] || ''
    },

    // 获取推送方式支持状态
    getPushMethodSupport(method) {
      // 这里可以根据后端API返回的support字段来判断
      // 目前先硬编码支持状态，后续可以改为从后端获取
      const supportConfig = {
        'HTTP': true, // HTTP推送已支持
        'RocketMQ': false, // RocketMQ开发中
        'Kafka': false, // Kafka开发中
        // 'MQTT': false, // MQTT开发中
        'JDBC': false // JDBC开发中
      }

      // 如果后端返回了support字段，优先使用后端数据
      if (this.form.cfg) {
        try {
          const config = JSON.parse(this.form.cfg)
          if (config[method.toLowerCase()] && config[method.toLowerCase()].support !== undefined) {
            return config[method.toLowerCase()].support
          }
        } catch (error) {
          console.error('解析support字段失败:', error)
        }
      }

      return supportConfig[method] || false
    },

    // 获取字典数据
    getDictMap(dictArray) {
      return new Promise((resolve, reject) => {
        // 这里应该调用实际的API，暂时返回模拟数据
        const mockData = {
          'app_push_http': [
            {dictLabel: '推送地址', dictValue: 'url'},
            {dictLabel: '推送密钥', dictValue: 'secret'},
            {dictLabel: '消息头', dictValue: 'header'}
          ],
          // 'app_push_mqtt': [
          // ],
          'app_push_rocketmq': [
            {dictLabel: 'NameServer地址', dictValue: 'namesrvAddr'},
            {dictLabel: '主题', dictValue: 'topic'},
            {dictLabel: '消费者组', dictValue: 'group'}
          ],
          'app_push_kafka': [
            {dictLabel: '服务器地址', dictValue: 'bootstrapServers'},
            {dictLabel: '主题', dictValue: 'topic'}
          ],
          'app_push_jdbc': [
            {dictLabel: '数据库类型', dictValue: 'type'},
            {dictLabel: '连接地址', dictValue: 'url'},
            {dictLabel: '用户名', dictValue: 'username'},
            {dictLabel: '密码', dictValue: 'password'}
          ]
        }

        setTimeout(() => {
          resolve({data: mockData})
        }, 100)
      })
    }
  }
}
</script>

<style lang="less" scoped>
.application-form-drawer {
  .drawer-header {
    padding: 24px 24px 0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 24px;

    .drawer-title {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #1a202c;
      display: flex;
      align-items: center;

      &::before {
        content: '';
        width: 4px;
        height: 18px;
        background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
        border-radius: 2px;
        margin-right: 12px;
      }
    }
  }

  .drawer-content {
    padding: 0 24px 24px;

    .ant-form-item {
      margin-bottom: 24px;

      .ant-form-item-label {
        label {
          font-weight: 500;
          color: #374151;
          font-size: 14px;
        }
      }

      .ant-form-item-explain {
        font-size: 12px;
        margin-top: 4px;
      }
    }

    .form-input {
      border-radius: 8px;
      border: 1px solid #d9d9d9;
      transition: all 0.3s ease;

      &:hover {
        border-color: #40a9ff;
      }

      &:focus {
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
      }

      .ant-input-prefix {
        color: #8c8c8c;
        margin-right: 8px;
      }
    }

    .input-with-button {
      display: flex;
      gap: 12px;
      align-items: flex-start;

      .form-input {
        flex: 1;
      }

      .test-btn {
        border-radius: 8px;
        height: 40px;
        padding: 0 16px;
        font-weight: 500;
        white-space: nowrap;
        flex-shrink: 0;
      }

      .generate-btn {
        border-radius: 6px;
        height: 40px;
        width: 40px;
        padding: 0;
        border-style: dashed;
        color: #1890ff;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        &:hover {
          border-color: #40a9ff;
          color: #40a9ff;
        }

        .anticon {
          font-size: 14px;
        }
      }
    }

    .dev-alert {
      margin-bottom: 24px;
      border-radius: 8px;
    }

    .dev-tag {
      color: #faad14;
      font-size: 12px;
      margin-left: 8px;
      font-style: italic;
    }

    .bottom-control {
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #f0f0f0;
      text-align: center;
    }
  }
}


@media (max-width: 768px) {
  .application-form-drawer {
    .drawer-content {
      .input-with-button {
        flex-direction: column;

        .test-btn {
          width: 100%;
        }

        .generate-btn {
          width: 40px;
          align-self: flex-end;
        }
      }

      .bottom-control {
        .ant-space {
          width: 100%;

          .ant-btn {
            flex: 1;
          }
        }
      }
    }
  }
}
</style>
