<template>
  <div class="protocol-config">
      <template v-if="!config.support">
        <div class="protocol-dev">该推送方式正在开发中</div>
      </template>
      <template v-else-if="config.enable">
        <div class="config-item">
          <div class="config-label">
            <a-icon type="cloud"/>
            服务器地址
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="url-text">{{ mqttConfig.url }}</span>
              <div class="config-actions">
                <a-tooltip title="复制地址">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(mqttConfig.url)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div class="config-item">
          <div class="config-label">
            <a-icon type="user"/>
            用户名
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="username-text">{{ mqttConfig.username }}</span>
              <div class="config-actions">
                <a-tooltip title="复制用户名">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(mqttConfig.username)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div class="config-item">
          <div class="config-label">
            <a-icon type="key"/>
            密码
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="secret-text">
                <span v-if="showPassword">{{ mqttConfig.password }}</span>
                <span v-else>********************************</span>
              </span>
              <div class="config-actions">
                <a-tooltip title="显示/隐藏密码">
                  <a-button
                    type="link"
                    size="small"
                    @click="togglePassword"
                    class="action-btn"
                  >
                    <a-icon :type="showPassword ? 'eye-invisible' : 'eye'"/>
                  </a-button>
                </a-tooltip>
                <a-tooltip title="复制密码" v-if="showPassword">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(mqttConfig.password)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div class="config-item">
          <div class="config-label">
            <a-icon type="idcard"/>
            客户端ID
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="clientid-text">{{ mqttConfig.clientId }}</span>
              <div class="config-actions">
                <a-tooltip title="复制客户端ID">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(mqttConfig.clientId)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div class="config-tips">
          <a-icon type="info-circle"/>
          <span>MQTT物联网消息推送</span>
        </div>
      </template>
      <template v-else>
        <div class="protocol-empty">未启用</div>
      </template>
    </div>
</template>

<script>
export default {
  name: 'MQTTPushConfig',
  props: {
    config: {
      type: Object,
      required: true
    },
    mqttConfig: {
      type: Object,
      default: () => ({})
    }
  },
  watch: {
    // 监听配置变化，确保UI及时更新
    config: {
      handler(newConfig) {
        console.log('MQTT配置已更新:', newConfig)
      },
      deep: true,
      immediate: true
    },
    mqttConfig: {
      handler(newMqttConfig) {
        console.log('MQTT详细配置已更新:', newMqttConfig)
      },
      deep: true,
      immediate: true
    }
  },
  data() {
    return {
      showPassword: false
    }
  },
  methods: {
    onEnableChange(checked) {
      this.$emit('change', checked)
    },
    togglePassword() {
      this.showPassword = !this.showPassword
    },
    copyToClipboard(text) {
      if (!text) return
      
      const textarea = document.createElement('textarea')
      textarea.value = text
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      
      this.$message.success('复制成功')
    }
  }
}
</script>

<style scoped lang="less">
.protocol-header-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #222;
  
  .anticon {
    margin-right: 8px;
    color: #1890ff;
  }
}

.protocol-switch {
  margin-left: 12px;
  flex-shrink: 0;
}

.protocol-config {
  .config-item {
    margin-bottom: 10px;
  }

  .config-label {
    font-size: 13px;
    color: #4e5969;
    margin-bottom: 4px;
    font-weight: 500;
    display: flex;
    align-items: center;
    
    .anticon {
      margin-right: 6px;
      color: #1890ff;
    }
  }

  .config-value .config-input {
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
  }

  .config-actions {
    gap: 4px;
    margin-left: 8px;
  }
}

.config-tips {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f0f8ff;
  border-radius: 6px;
  border-left: 3px solid #1890ff;
  font-size: 12px;
  color: #1890ff;

  .anticon {
    margin-right: 6px;
  }
}

.protocol-empty {
  padding: 32px 16px;
  text-align: center;
  color: #b0b3b9;
  font-size: 15px;
}

.protocol-dev {
  padding: 32px 16px;
  text-align: center;
  color: #b0b3b9;
  font-size: 15px;
}
</style>
