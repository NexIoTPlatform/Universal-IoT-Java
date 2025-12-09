<template>
  <div class="app-container">
    <a-card :bordered="false">
      <div class="page-header">
        <div class="header-left">
          <a-button type="text" icon="left" @click="back()" class="back-btn" />
          <div class="page-title">
            <h1>{{ application.appName || '应用详情' }}</h1>
          </div>
          <a-tag :color="application.appStatus === 0 ? 'green' : 'red'" style="margin-left: 12px;">
            {{ application.appStatus === 0 ? '启用' : '禁用' }}
          </a-tag>
        </div>
      </div>
      
      <a-spin :spinning="loading" tip="Loading...">
        <!-- 自定义标签页导航 -->
        <div class="custom-tabs-container">
          <div class="custom-tabs-nav">
            <div class="custom-tab-item" :class="{ active: activeKey === '1' }" @click="switchTab('1')">
              <a-icon type="info-circle" style="margin-right: 6px;" />
              应用信息
            </div>
            <div class="custom-tab-item" :class="{ active: activeKey === '2' }" @click="switchTab('2')">
              <a-icon type="api" style="margin-right: 6px;" />
              推送配置
            </div>
            <div class="custom-tab-item" :class="{ active: activeKey === '3' }" @click="switchTab('3')">
              <a-icon type="hdd" style="margin-right: 6px;" />
              设备管理
            </div>
          </div>

          <!-- 标签页内容 -->
          <div class="custom-tab-content">
            <!-- 应用信息 -->
            <div v-show="activeKey === '1'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>基础信息</h3>
                  <a-button type="link" size="small" @click="$refs.createForm.handleUpdate(undefined, application.appUniqueId)" v-hasPermi="['application:application:edit']">
                    <a-icon type="edit" /> 编辑
                  </a-button>
                </div>
                
                <div class="basic-info-grid three-columns">
                  <div class="info-item">
                    <span class="info-label">应用名称</span>
                    <span class="info-value">{{ application.appName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">应用状态</span>
                    <a-tag :color="application.appStatus === 0 ? 'green' : 'red'">
                      {{ application.appStatus === 0 ? '启用' : '禁用' }}
                    </a-tag>
                  </div>
                  <div class="info-item">
                    <span class="info-label">创建账号</span>
                    <span class="info-value">{{ application.unionId }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">创建时间</span>
                    <span class="info-value">{{ formatDateTime(application.createDate) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">应用标识</span>
                    <div class="info-value-group">
                      <span class="info-value code">{{ application.appUniqueId }}</span>
                      <a-button type="text" size="small" class="copy-action-btn" @click.stop="copyToClipboard(application.appUniqueId)" title="复制">
                        <a-icon type="copy"/>
                      </a-button>
                    </div>
                  </div>
                </div>
                <div v-if="application.remark" class="info-description">
                  <span class="info-label">应用描述</span>
                  <span class="info-value">{{ application.remark }}</span>
                </div>
              </div>

              <!-- 开发信息 -->
              <div class="device-basic-info" style="margin-top: 16px;">
                <div class="basic-info-header">
                  <div class="header-left">
                    <h3>开发信息</h3>
                    <a-button type="link" size="small" @click="openSdkModal" class="sdk-inline-btn">
                      <a-icon type="code" /> 查看SDK配置
                    </a-button>
                  </div>
                  <div class="header-actions">
                    <a-button type="link" size="small" href="https://docs.nexiot.cc/iot/http/java-sdk.html" target="_blank">
                      <a-icon type="book" /> SDK文档
                    </a-button>
                    <a-button type="link" size="small" @click="showModal" v-hasPermi="['application:application:resetSecret']">
                      <a-icon type="key" /> 重置密钥
                    </a-button>
                  </div>
                </div>
                
                <div class="dev-info-container">
                  <div class="dev-info-item">
                    <div class="dev-info-label">Client ID</div>
                    <div class="dev-info-value">
                      <span class="code-text">{{ application.appId }}</span>
                      <a-button type="text" size="small" class="copy-action-btn" @click.stop="copyToClipboard(application.appId)" title="复制">
                        <a-icon type="copy"/>
                      </a-button>
                    </div>
                  </div>
                  <div class="dev-info-item">
                    <div class="dev-info-label">Client Secret</div>
                    <div class="dev-info-value">
                      <span class="code-text">{{ showAppSecret ? application.appSecret : '********************************' }}</span>
                      <a-button type="text" size="small" class="copy-action-btn" @click.stop="appSecretShow" title="显示/隐藏">
                        <a-icon :type="showAppSecret ? 'eye-invisible' : 'eye'"/>
                      </a-button>
                      <a-button v-if="showAppSecret" type="text" size="small" class="copy-action-btn" @click.stop="copyToClipboard(application.appSecret)" title="复制">
                        <a-icon type="copy"/>
                      </a-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>


            <!-- SDK配置示例已移至弹窗 -->

            <!-- 推送配置 -->
            <div v-show="activeKey === '2'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>推送方式配置</h3>
                  <a-button type="link" size="small" @click="$refs.createForm.handleDevelopUpdate(undefined, application.appUniqueId, application.appId, unionId)" v-hasPermi="['application:application:edit']">
                    <a-icon type="edit" /> 编辑配置
                  </a-button>
                </div>

                <!-- HTTP推送 -->
                <div v-if="getConfig('http').support !== undefined" class="push-config-section">
                  <div class="config-header">
                    <div class="config-title">
                      <a-icon type="global" style="color: #1890ff; margin-right: 8px;" />
                      <span>HTTP推送</span>
                      <a-tag v-if="!hasHttpConfig()" color="orange" style="margin-left: 8px; font-size: 12px;">未配置</a-tag>
                      <a-tag v-else color="green" style="margin-left: 8px; font-size: 12px;">已配置</a-tag>
                    </div>
                    <a-switch
                      :checked="getConfig('http').enable"
                      :disabled="!getConfig('http').support"
                      @change="(checked) => onHttpEnableChange(checked)"
                      size="small"
                    />
                  </div>
                  <HttpPushConfig
                    v-if="getConfig('http').enable"
                    :config="getConfig('http')"
                    :http-config="getHttpConfig()"
                    @change="onHttpEnableChange"
                  />
                  <div v-if="!hasHttpConfig() && !getConfig('http').enable" class="config-empty-tip">
                    <a-icon type="info-circle" style="color: #faad14; margin-right: 6px;" />
                    <span>请先配置 HTTP 推送地址和密钥，然后再启用</span>
                  </div>
                </div>

                <!-- MQTT推送 -->
                <div v-if="getConfig('mqtt').support !== undefined" class="push-config-section">
                  <div class="config-header">
                    <div class="config-title">
                      <a-icon type="cloud" style="color: #52c41a; margin-right: 8px;" />
                      <span>MQTT推送</span>
                      <!-- <a-tag color="blue" style="margin-left: 8px; font-size: 12px;">系统内置</a-tag> -->
                    </div>
                    <a-switch
                      :checked="getConfig('mqtt').enable"
                      :disabled="!getConfig('mqtt').support"
                      @change="(checked) => onMqttEnableChange(checked)"
                      size="small"
                    />
                  </div>
                  <MQTTPushConfig
                    v-if="getConfig('mqtt').enable"
                    :config="getConfig('mqtt')"
                    :mqtt-config="getMQTTConfig()"
                    @change="onMqttEnableChange"
                  />
                </div>

                <!-- RocketMQ推送 -->
                <div v-if="getConfig('rocketmq').support !== undefined" class="push-config-section">
                  <div class="config-header">
                    <div class="config-title">
                      <a-icon type="cluster" style="color: #fa8c16; margin-right: 8px;" />
                      <span>RocketMQ推送</span>
                    </div>
                    <a-switch
                      :checked="getConfig('rocketmq').enable"
                      :disabled="!getConfig('rocketmq').support"
                      @change="onRocketMQEnableChange"
                      size="small"
                    />
                  </div>
                  <RocketMQPushConfig
                    v-if="getConfig('rocketmq').enable"
                    :config="getConfig('rocketmq')"
                    :rocket-m-q-config="getRocketMQConfig()"
                    @change="onRocketMQEnableChange"
                  />
                </div>

                <!-- JDBC推送 -->
                <div v-if="getConfig('jdbc').support !== undefined" class="push-config-section">
                  <div class="config-header">
                    <div class="config-title">
                      <a-icon type="database" style="color: #722ed1; margin-right: 8px;" />
                      <span>JDBC推送</span>
                    </div>
                    <a-switch
                      :checked="getConfig('jdbc').enable"
                      :disabled="!getConfig('jdbc').support"
                      @change="onJDBCEnableChange"
                      size="small"
                    />
                  </div>
                  <JDBCPushConfig
                    v-if="getConfig('jdbc').enable"
                    :config="getConfig('jdbc')"
                    :jdbc-config="getJDBCConfig()"
                    @change="onJDBCEnableChange"
                  />
                </div>

                <!-- 未配置状态 -->
                <div v-if="!hasAnyConfig()" class="empty-tip">
                  <a-empty description="暂无推送配置">
                    <a-button type="primary" @click="$refs.createForm.handleDevelopUpdate(undefined, application.appUniqueId, application.appId, unionId)">
                      <a-icon type="plus" />
                      配置推送方式
                    </a-button>
                  </a-empty>
                </div>
              </div>
            </div>

            <!-- 设备管理 -->
            <div v-show="activeKey === '3'" class="tab-pane">
              <device-bind ref="deviceBind" :appUniqueId="appUniqueId"/>
            </div>
          </div>
        </div>
      </a-spin>

      <!-- 重置密钥确认弹窗 -->
      <a-modal
        v-model="modalVisible"
        title="⚠️ 重置密钥确认"
        @ok="handleReset(application)"
        @cancel="modalCancel"
        width="600px"
        :ok-button-props="{ danger: true }"
        :ok-text="'确认重置'"
        :cancel-text="'取消'"
      >
        <div class="reset-confirm">
          <div class="warning-header">
            <a-icon type="exclamation-circle" class="warning-icon"/>
            <h4 class="warning-title">确认重置应用密钥？</h4>
          </div>

          <div class="risk-warning">
            <div class="risk-title">
              <a-icon type="warning" class="risk-icon"/>
              <span>操作风险提示</span>
            </div>
            <ul class="risk-list">
              <li>当前密钥将立即失效，所有使用旧密钥的连接将被断开</li>
              <li>已启用的MQTT推送配置将受到影响，订阅消息可能无法正常接收</li>
              <li>需要重新配置所有使用该密钥的客户端和应用程序</li>
              <li>此操作不可逆，请确保已备份重要配置信息</li>
            </ul>
          </div>

          <div class="confirm-section">
            <p class="confirm-text">为了确认您了解操作风险，请输入应用ID：</p>
            <div class="confirm-input">
              <span class="confirm-code">{{ application.appId }}</span>
              <a-input
                v-model="checkInfo"
                placeholder="请输入上方应用ID进行确认"
                @paste.native.capture.prevent="handlePaste"
                style="margin-top: 12px"
                size="large"
              />
            </div>
          </div>
        </div>
      </a-modal>

      <!-- SDK配置示例弹窗 -->
      <a-modal
        :visible="sdkModalVisible"
        title="SDK配置示例"
        :width="800"
        @cancel="closeSdkModal"
        :footer="null"
        :destroyOnClose="true"
      >
        <div class="sdk-config-content">
          <div class="config-block">
            <div class="config-title">Maven依赖</div>
            <pre class="config-code"><code>&lt;dependency&gt;
    &lt;groupId&gt;cc.nexiot&lt;/groupId&gt;
    &lt;artifactId&gt;nexiot-openapi-sdk&lt;/artifactId&gt;
    &lt;version&gt;1.0.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
          </div>
          <div class="config-block">
            <div class="config-title">Spring Boot配置</div>
            <div class="config-hint">配置文件位置：classpath:resources/config/nexiot.properties</div>
            <pre class="config-code"><code># 协议配置（默认HTTP，生产环境建议启用HTTPS）
iot.sdk.enable.https=false

# 服务器地址（不包含协议前缀http/https）
universal.iot.host={{ getServerHost() }}

# OAuth2客户端凭证
universal.iot.clientId={{ application.appId }}
universal.iot.clientSecret={{ showAppSecret ? application.appSecret : '请先在应用信息中显示密钥' }}

# 授权模式配置
# password: 密码模式，需要提供用户名密码
# client_credentials: 客户端模式，仅使用clientId和clientSecret（推荐）
universal.iot.grantType=client_credentials

# 密码模式专用配置（仅当grantType=password时需要）
# universal.iot.username=your-username
# universal.iot.password=your-password</code></pre>
          </div>
          <div class="sdk-tips">
            <a-icon type="info-circle" style="color: #1890ff; margin-right: 6px;" />
            <span>推荐使用 <strong>client_credentials</strong> 模式，更安全便捷。详细用法请查看 
              <a href="https://docs.nexiot.cc/iot/http/java-sdk.html" target="_blank" style="color: #1890ff;">官方文档</a>
            </span>
            <a-button type="link" size="small" @click="copySdkConfig" style="margin-left: 12px;">
              <a-icon type="copy" /> 复制配置
            </a-button>
          </div>
        </div>
      </a-modal>

      <!-- 表单组件 -->
      <create-form
        ref="createForm"
        :statusOptions="statusOptions"
        :deleteOptions="[]"
        @ok="getList"
      />
    </a-card>
  </div>
</template>

<script>
import DeviceBind from '@/views/application/modules/DeviceBind'
import {enable, enableOrDisablePush, getApplication, resetSecret} from '@/api/application/application'
import CreateForm from '@/views/application/modules/CreateForm'
import HttpPushConfig from '@/views/application/modules/HttpPushConfig'
import RocketMQPushConfig from '@/views/application/modules/RocketMQPushConfig'
import MQTTPushConfig from '@/views/application/modules/MQTTPushConfig'
import JDBCPushConfig from '@/views/application/modules/JDBCPushConfig'

export default {
  name: 'ApplicationDetail',
  components: {
    DeviceBind,
    CreateForm,
    HttpPushConfig,
    RocketMQPushConfig,
    MQTTPushConfig,
    JDBCPushConfig
  },
  data() {
    return {
      unionId: null,
      appUniqueId: null,
      application: {},
      loading: false,
      statusOptions: [],
      activeKey: '1',
      showAppSecret: false,
      sdkModalVisible: false,
      checkInfo: undefined,
      modalVisible: false,
      httpEnable: false,
      openEnable: false,
      httpConfig: null
    }
  },
  created() {
    this.appUniqueId = this.$route.params.appUniqueId
    this.getList()
  },
  methods: {
    /** 获取应用详情 */
    getList() {
      this.loading = true
      getApplication(this.appUniqueId).then(response => {
        this.application = response.data
        this.unionId = response.data.unionId
        this.httpEnable = response.data.httpEnable

        // 解析配置JSON
        try {
          this.httpConfig = response.data.cfg ? JSON.parse(response.data.cfg) : null
        } catch (error) {
          console.error('解析配置JSON失败:', error)
          this.httpConfig = null
        }

        this.loading = false
      }).catch(() => {
        this.$message.error('获取应用信息失败')
        this.back()
      })

      this.getDicts('sys_normal_disable').then(response => {
        this.statusOptions = response.data
      })
    },

    /** 切换标签页 */
    switchTab(key) {
      this.activeKey = key
    },

    /** 格式化日期时间 */
    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },

    /** 显示/隐藏密钥 */
    appSecretShow() {
      this.showAppSecret = !this.showAppSecret
    },

    /** 获取HTTP配置 */
    getHttpConfig() {
      if (!this.httpConfig) return null
      return this.httpConfig.http || null
    },

    /** 获取RocketMQ配置 */
    getRocketMQConfig() {
      if (!this.httpConfig || !this.httpConfig.rocketMQ) return {}
      return this.httpConfig.rocketMQ
    },

    /** 获取MQTT配置 */
    getMQTTConfig() {
      if (!this.httpConfig || !this.httpConfig.mqtt) return {}
      return this.httpConfig.mqtt
    },

    /** 获取JDBC配置 */
    getJDBCConfig() {
      if (!this.httpConfig || !this.httpConfig.jdbc) return {}
      return this.httpConfig.jdbc
    },

    /** 检查是否有任何配置 */
    hasAnyConfig() {
      return (this.httpConfig && this.httpConfig.http && (this.httpConfig.http.url || this.httpConfig.http.secret)) ||
        (this.httpConfig && this.httpConfig.rocketMQ && Object.keys(this.httpConfig.rocketMQ).length > 0) ||
        (this.httpConfig && this.httpConfig.mqtt && Object.keys(this.httpConfig.mqtt).length > 0) ||
        (this.httpConfig && this.httpConfig.jdbc && Object.keys(this.httpConfig.jdbc).length > 0)
    },

    /** 获取配置 */
    getConfig(type) {
      if (!this.httpConfig || !this.httpConfig[type]) {
        const defaultSupport = {
          'http': true,
          'rocketmq': false,
          'kafka': false,
          'mqtt': true,
          'jdbc': false
        }
        return {
          support: defaultSupport[type] || false,
          enable: false
        }
      }

      const config = this.httpConfig[type]
      if (config.support === undefined) {
        const defaultSupport = {
          'http': true,
          'rocketmq': false,
          'kafka': false,
          'mqtt': true,
          'jdbc': false
        }
        config.support = defaultSupport[type] || false
      }

      if (config.enable === undefined) {
        if (type === 'http') {
          config.enable = !!(config.url || config.secret)
        } else if (type === 'rocketmq') {
          config.enable = !!(config.namesrvAddr || config.topic)
        } else if (type === 'kafka') {
          config.enable = !!(config.bootstrapServers || config.topic)
        } else if (type === 'mqtt') {
          config.enable = !!(config.url || config.topic)
        } else if (type === 'jdbc') {
          config.enable = !!(config.url || config.username)
        } else {
          config.enable = false
        }
      }

      return config
    },

    onHttpEnableChange(checked) {
      // 启用前检查是否有配置
      if (checked && !this.hasHttpConfig()) {
        this.$message.warning('请先配置 HTTP 推送地址和密钥')
        // 引导用户去配置
        this.$refs.createForm.handleDevelopUpdate(undefined, this.application.appUniqueId, this.application.appId, this.unionId)
        return
      }
      this.enableOrDisablePushConfig('http', checked)
    },
    onMqttEnableChange(checked) {
      // MQTT是系统内置，点击启用会自动配置，不需要校验
      this.enableOrDisablePushConfig('mqtt', checked)
    },
    onRocketMQEnableChange(checked) {
      this.enableOrDisablePushConfig('rocketmq', checked)
    },
    onJDBCEnableChange(checked) {
      this.enableOrDisablePushConfig('jdbc', checked)
    },

    /** 启用/停用推送配置 */
    enableOrDisablePushConfig(pushType, enable) {
      if (this.httpConfig && this.httpConfig[pushType]) {
        this.httpConfig[pushType].enable = enable
      }
      enableOrDisablePush(this.appUniqueId, pushType, enable)
        .then(response => {
          if (response.code === 0) {
            this.$message.success(enable ? `${this.getPushTypeName(pushType)}推送已启用` : `${this.getPushTypeName(pushType)}推送已停用`)
            this.refreshPushConfig()
          } else {
            if (this.httpConfig && this.httpConfig[pushType]) {
              this.httpConfig[pushType].enable = !enable
            }
            this.$message.error(response.msg || '操作失败')
          }
        })
        .catch(error => {
          if (this.httpConfig && this.httpConfig[pushType]) {
            this.httpConfig[pushType].enable = !enable
          }
          this.$message.error('网络错误，操作失败')
          console.error('启用/停用推送配置失败:', error)
        })
    },

    /** 获取推送方式的中文名称 */
    getPushTypeName(pushType) {
      const typeNames = {
        'http': 'HTTP',
        'rocketmq': 'RocketMQ',
        'mqtt': 'MQTT',
        'jdbc': 'JDBC'
      }
      return typeNames[pushType] || pushType
    },

    /** 刷新推送配置信息 */
    refreshPushConfig() {
      this.loading = true
      getApplication(this.appUniqueId).then(response => {
        this.application = response.data
        try {
          const newHttpConfig = response.data.cfg ? JSON.parse(response.data.cfg) : null
          if (newHttpConfig) {
            this.httpConfig = {...this.httpConfig, ...newHttpConfig}
            Object.keys(newHttpConfig).forEach(key => {
              if (this.httpConfig[key] && typeof newHttpConfig[key] === 'object') {
                this.httpConfig[key] = {...this.httpConfig[key], ...newHttpConfig[key]}
              }
            })
          }
        } catch (error) {
          console.error('解析配置JSON失败:', error)
        }
        this.loading = false
      }).catch(error => {
        console.error('刷新推送配置失败:', error)
        this.$message.warning('配置刷新失败，但本地状态已更新')
        this.loading = false
      })
    },

    /** 返回上一级 */
    back() {
      this.$router.push({name: 'Application'})
    },

    /** 显示重置密钥弹窗 */
    showModal() {
      this.modalVisible = true
      this.checkInfo = undefined
    },

    /** 取消重置密钥 */
    modalCancel() {
      this.modalVisible = false
      this.checkInfo = undefined
    },

    /** 禁止粘贴 */
    handlePaste() {
      this.$message.warning('为确保操作安全，请手动输入应用ID')
    },

    /** 重置密钥 */
    handleReset(row) {
      if (this.checkInfo !== this.application.appId) {
        this.$message.error('输入的应用ID不正确')
        return
      }

      resetSecret({appUniqueId: row.appUniqueId}).then(() => {
        this.getList()
        this.$message.success('密钥重置成功')
        this.modalCancel()
      }).catch(() => {
        this.$message.error('密钥重置失败')
      })
    },

    /** 复制到剪贴板 */
    copyToClipboard(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopyTextToClipboard(text)
        })
      } else {
        this.fallbackCopyTextToClipboard(text)
      }
    },
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea')
      textArea.value = text
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        document.execCommand('copy')
        this.$message.success('已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败')
      }
      document.body.removeChild(textArea)
    },

    /** 打开SDK弹窗 */
    openSdkModal() {
      this.sdkModalVisible = true
    },
    /** 关闭SDK弹窗 */
    closeSdkModal() {
      this.sdkModalVisible = false
    },

    /** 复制SDK配置 */
    copySdkConfig() {
      const config = `# Maven依赖
<dependency>
    <groupId>cc.nexiot</groupId>
    <artifactId>nexiot-openapi-sdk</artifactId>
    <version>1.0.0</version>
</dependency>

# Spring Boot配置
# 协议配置（默认HTTP，生产环境建议启用HTTPS）
iot.sdk.enable.https=false

# 服务器地址（不包含协议前缀http/https）
universal.iot.host=${this.getServerHost()}

# OAuth2客户端凭证
universal.iot.clientId=${this.application.appId}
universal.iot.clientSecret=${this.showAppSecret ? this.application.appSecret : '请先显示密钥'}

# 授权模式配置
# password: 密码模式，需要提供用户名密码
# client_credentials: 客户端模式，仅使用clientId和clientSecret（推荐）
universal.iot.grantType=client_credentials

# 密码模式专用配置（仅当grantType=password时需要）
# universal.iot.username=your-username
# universal.iot.password=your-password`
      this.copyToClipboard(config)
    },

    /** 获取服务器地址 */
    getServerHost() {
      return window.location.host
    },

    /** 检查是否有HTTP配置 */
    hasHttpConfig() {
      const httpConfig = this.getHttpConfig()
      return httpConfig && httpConfig.url && httpConfig.appSecret
    },

    /** 检查是否有MQTT配置 */
    hasMQTTConfig() {
      const mqttConfig = this.getMQTTConfig()
      return mqttConfig && mqttConfig.server && mqttConfig.port
    }
  }
}
</script>

<style lang="less" scoped>
/* 页面容器样式 */
.app-container {
  background: #ffffff;
}

.ant-card {
  background: #ffffff;
  border: none;
  box-shadow: none;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e8eaed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: #64748b;

  &:hover {
    background: #e2e8f0;
    border-color: #1966ff;
    color: #1966ff;
    transform: scale(1.05);
  }
}

.page-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  line-height: 1.2;
}

/* 自定义标签页样式 */
.custom-tabs-container {
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

.custom-tabs-nav {
  display: flex;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.custom-tabs-nav::-webkit-scrollbar {
  display: none;
}

.custom-tab-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  min-width: fit-content;
  border-right: 1px solid #e8e8e8;
  background: #fafafa;
}

.custom-tab-item:last-child {
  border-right: none;
}

.custom-tab-item:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.custom-tab-item.active {
  background: #ffffff;
  color: #1890ff;
  border-bottom: 2px solid #1890ff;
  margin-bottom: -1px;
}

.custom-tab-content {
  min-height: 500px;
  background: #fff;
}

.tab-pane {
  padding: 10px;
}

/* 设备信息页面样式 */
.device-basic-info {
  background: #ffffff;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;
}

.basic-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.basic-info-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.basic-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.compact-grid {
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}

.three-columns {
  grid-template-columns: repeat(3, 1fr);
  gap: 16px 24px;
}

.info-description {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  margin-top: 8px;
  border-top: 1px solid #f0f0f0;
  
  .info-label {
    min-width: 90px;
    width: 90px;
  }
  
  .info-value {
    flex: 1;
    line-height: 1.6;
  }
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.info-item-full {
  grid-column: 1 / -1;
}

.info-label {
  min-width: 90px;
  width: 90px;
  font-size: 13px;
  color: #8c8c8c;
  margin-right: 12px;
  flex-shrink: 0;
  text-align: left;
}

.info-value-group {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.info-value {
  font-size: 13px;
  color: #262626;
  line-height: 1.4;
  
  &.code {
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    color: #1890ff;
  }
}

.copy-action-btn {
  width: 18px;
  height: 18px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  background: transparent;
  border: none;
  transition: all 0.2s ease;
  font-size: 10px;
  margin-left: 2px;
  flex-shrink: 0;
}

.copy-action-btn:hover {
  color: #1890ff;
  background: #f0f8ff;
}

/* 开发信息特殊布局 */
.dev-info-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dev-info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dev-info-label {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 500;
}

.dev-info-value {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f5f6fa;
  border-radius: 6px;
  border: 1px solid #e8eaed;
  
  .code-text {
    flex: 1;
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    font-size: 13px;
    color: #1890ff;
    word-break: break-all;
  }
}

/* SDK配置区域 */
.sdk-config-section {
  margin-top: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.sdk-config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sdk-config-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.sdk-config-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-block {
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  
  .config-title {
    padding: 8px 12px;
    background: #f5f6fa;
    border-bottom: 1px solid #e8e8e8;
    font-size: 13px;
    font-weight: 600;
    color: #595959;
  }
}

.config-hint {
  margin: 6px 12px 8px;
  font-size: 12px;
  color: #8c8c8c;
}

.config-code {
  margin: 0;
  padding: 12px;
  background: #fff;
  
  code {
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    font-size: 12px;
    line-height: 1.6;
    color: #262626;
    display: block;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.sdk-tips {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: #e6f7ff;
  border-radius: 6px;
  border: 1px solid #91d5ff;
  font-size: 13px;
  line-height: 1.6;
  color: #595959;
  
  strong {
    color: #1890ff;
    font-weight: 600;
  }
  
  a {
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

/* 推送配置区域 */
.push-config-section {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.config-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.config-empty-tip {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-top: 12px;
  background: #fffbe6;
  border-radius: 6px;
  border: 1px solid #ffe58f;
  font-size: 13px;
  color: #8c8c8c;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
}

/* 重置密钥确认弹窗样式 */
.reset-confirm {
  .warning-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .warning-icon {
      color: #faad14;
      font-size: 24px;
      margin-right: 12px;
    }

    .warning-title {
      font-size: 18px;
      font-weight: 600;
      color: #d48806;
      margin: 0;
    }
  }

  .risk-warning {
    background: #fff7e6;
    border: 1px solid #ffd591;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 20px;

    .risk-title {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      font-weight: 600;
      color: #d48806;

      .risk-icon {
        color: #fa8c16;
        margin-right: 8px;
      }
    }

    .risk-list {
      margin: 0;
      padding-left: 20px;

      li {
        color: #8c8c8c;
        line-height: 1.6;
        margin-bottom: 8px;
        font-size: 14px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .confirm-section {
    .confirm-text {
      font-size: 14px;
      color: #595959;
      margin-bottom: 16px;
      font-weight: 500;
    }

    .confirm-input {
      .confirm-code {
        display: block;
        background: #f5f5f5;
        padding: 8px 12px;
        border-radius: 6px;
        font-size: 14px;
        font-family: 'Monaco', 'Menlo', monospace;
        color: #1890ff;
        text-align: center;
        margin-bottom: 8px;
        border: 1px solid #d9d9d9;
      }
    }
  }
}
</style>
