<template>
  <div class="protocol-config">
    <template v-if="!config.support">
      <div class="protocol-dev">该推送方式正在开发中</div>
    </template>
    <template v-else-if="config.enable">
      <!-- MySQL配置 -->
      <div v-if="jdbcConfig.mysql" class="database-config">
        <div class="db-header">
          <a-tag color="blue">MySQL</a-tag>
        </div>
        <div class="config-item">
          <div class="config-label">
            <a-icon type="database"/>
            连接地址
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="url-text">{{ jdbcConfig.mysql.url }}</span>
              <div class="config-actions">
                <a-tooltip title="复制连接地址">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.mysql.url)"
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
              <span class="username-text">{{ jdbcConfig.mysql.username }}</span>
              <div class="config-actions">
                <a-tooltip title="复制用户名">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.mysql.username)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Oracle配置 -->
      <div v-if="jdbcConfig.oracle" class="database-config">
        <div class="db-header">
          <a-tag color="red">Oracle</a-tag>
        </div>
        <div class="config-item">
          <div class="config-label">
            <a-icon type="database"/>
            连接地址
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="url-text">{{ jdbcConfig.oracle.url }}</span>
              <div class="config-actions">
                <a-tooltip title="复制连接地址">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.oracle.url)"
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
              <span class="username-text">{{ jdbcConfig.oracle.username }}</span>
              <div class="config-actions">
                <a-tooltip title="复制用户名">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.oracle.username)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- PostgreSQL配置 -->
      <div v-if="jdbcConfig.postgres" class="database-config">
        <div class="db-header">
          <a-tag color="green">PostgreSQL</a-tag>
        </div>
        <div class="config-item">
          <div class="config-label">
            <a-icon type="database"/>
            连接地址
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="url-text">{{ jdbcConfig.postgres.url }}</span>
              <div class="config-actions">
                <a-tooltip title="复制连接地址">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.postgres.url)"
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
              <span class="username-text">{{ jdbcConfig.postgres.username }}</span>
              <div class="config-actions">
                <a-tooltip title="复制用户名">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.postgres.username)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- SQLite配置 -->
      <div v-if="jdbcConfig.sqlite" class="database-config">
        <div class="db-header">
          <a-tag color="orange">SQLite</a-tag>
        </div>
        <div class="config-item">
          <div class="config-label">
            <a-icon type="database"/>
            数据库路径
          </div>
          <div class="config-value">
            <div class="config-input">
              <span class="url-text">{{ jdbcConfig.sqlite.url }}</span>
              <div class="config-actions">
                <a-tooltip title="复制数据库路径">
                  <a-button
                    type="link"
                    size="small"
                    @click="copyToClipboard(jdbcConfig.sqlite.url)"
                    class="action-btn"
                  >
                    <a-icon type="copy"/>
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="config-tips">
        <a-icon type="info-circle"/>
        <span>JDBC数据库推送，支持多种数据库类型</span>
      </div>
    </template>
    <template v-else>
      <div class="protocol-empty">未启用</div>
    </template>
  </div>
</template>

<script>
export default {
  name: 'JDBCPushConfig',
  props: {
    config: {
      type: Object,
      required: true
    },
    jdbcConfig: {
      type: Object,
      default: () => ({})
    }
  },
  methods: {
    onEnableChange(checked) {
      this.$emit('change', checked)
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

.database-config {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fb;
  border-radius: 6px;
  border: 1px solid #f0f0f0;

  .db-header {
    margin-bottom: 8px;

    .ant-tag {
      font-size: 11px;
      padding: 1px 6px;
      border-radius: 8px;
    }
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
