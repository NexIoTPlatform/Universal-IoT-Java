<template>
  <div class="protocol-config">
    <template v-if="!config.support">
      <div class="protocol-dev">该推送方式正在开发中</div>
    </template>
    <template v-else-if="config.enable">
      <div class="config-item">
        <div class="config-label">
          <a-icon type="link"/>
          推送地址
        </div>
        <div class="config-value">
          <div class="config-input">
            <span class="url-text">{{ httpConfig.url }}</span>
            <div class="config-actions">
              <a-tooltip title="复制地址">
                <a-button
                  type="link"
                  size="small"
                  @click="copyToClipboard(httpConfig.url)"
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
          HTTP推送密钥
        </div>
        <div class="config-value">
          <div class="config-input">
              <span class="secret-text">
                <span v-if="showSecret">{{ httpConfig.secret }}</span>
                <span v-else>********************************</span>
              </span>
            <div class="config-actions">
              <a-tooltip title="显示/隐藏密钥">
                <a-button
                  type="link"
                  size="small"
                  @click="toggleSecret"
                  class="action-btn"
                >
                  <a-icon :type="showSecret ? 'eye-invisible' : 'eye'"/>
                </a-button>
              </a-tooltip>
              <a-tooltip title="复制密钥" v-if="showSecret">
                <a-button
                  type="link"
                  size="small"
                  @click="copyToClipboard(httpConfig.secret)"
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
          <a-icon type="tag"/>
          HTTP消息头
        </div>
        <div class="config-value">
          <div class="config-input">
            <span class="header-text">{{ httpConfig.header }}</span>
            <div class="config-actions">
              <a-tooltip title="复制消息头">
                <a-button
                  type="link"
                  size="small"
                  @click="copyToClipboard(httpConfig.header)"
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
        <span>HTTP推送采用POST方法，数据格式为application/json，密钥将在请求头中携带</span>
      </div>
    </template>
    <template v-else>
      <div class="protocol-empty">未启用</div>
    </template>
  </div>
</template>

<script>
export default {
  name: 'HttpPushConfig',
  props: {
    config: {
      type: Object,
      required: true
    },
    httpConfig: {
      type: Object,
      default: () => ({})
    }
  },
  watch: {
    // 监听配置变化，确保UI及时更新
    config: {
      handler(newConfig) {
        console.log('HTTP配置已更新:', newConfig)
      },
      deep: true,
      immediate: true
    },
    httpConfig: {
      handler(newHttpConfig) {
        console.log('HTTP详细配置已更新:', newHttpConfig)
      },
      deep: true,
      immediate: true
    }
  },
  data() {
    return {
      showSecret: false
    }
  },
  methods: {
    onEnableChange(checked) {
      this.$emit('change', checked)
    },
    toggleSecret() {
      this.showSecret = !this.showSecret
    },
    copyToClipboard(text) {
      if (!text) {
        return
      }

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
