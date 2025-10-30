<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="databridge-overview">
        <div class="overview-header">
          <h2>数据桥接管理</h2>
          <p>管理数据连接和桥接规则，实现数据在不同系统间的流转</p>
        </div>

        <a-row :gutter="24" class="overview-cards">
          <a-col :span="12">
            <a-card
              hoverable
              class="feature-card"
              @click="handleActionClick('/DataBridge/DataBridgeConnection', $event)"
            >
              <div class="card-content">
                <div class="card-icon">
                  <a-icon type="link" style="font-size: 48px; color: #1890ff;"/>
                </div>
                <div class="card-info">
                  <h3>连接管理</h3>
                  <p>管理各种外部系统的连接配置，包括数据库、消息队列等</p>
                  <div class="card-features">
                    <a-tag color="blue">数据库连接</a-tag>
                    <a-tag color="green">消息队列</a-tag>
                    <a-tag color="orange">HTTP接口</a-tag>
                  </div>
                </div>
              </div>
            </a-card>
          </a-col>

          <a-col :span="12">
            <a-card
              hoverable
              class="feature-card"
              @click="handleActionClick('/DataBridge/DataBridgeBridge', $event)"
            >
              <div class="card-content">
                <div class="card-icon">
                  <a-icon type="swap" style="font-size: 48px; color: #52c41a;"/>
                </div>
                <div class="card-info">
                  <h3>桥接规则</h3>
                  <p>定义数据流转规则，实现数据在不同连接间的自动传输</p>
                  <div class="card-features">
                    <a-tag color="purple">数据转换</a-tag>
                    <a-tag color="cyan">实时同步</a-tag>
                    <a-tag color="magenta">批量处理</a-tag>
                  </div>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <div class="quick-actions">
          <h3>快速操作</h3>
          <a-space size="large">
            <a-button type="primary" size="large"
                      @click="handleActionClick('/DataBridge/DataBridgeConnection', $event)">
              <a-icon type="plus"/>
              新增连接
            </a-button>
            <a-button type="primary" size="large"
                      @click="handleActionClick('/DataBridge/DataBridgeBridge', $event)">
              <a-icon type="plus"/>
              新增规则
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import {PageHeaderWrapper} from '@/components/ProLayout'

export default {
  name: 'DataBridgeIndex',
  components: {
    PageHeaderWrapper
  },
  methods: {
    navigateTo(path) {
      console.log('Navigating to:', path)
      this.$router.push(path).catch(err => {
        console.log('Navigation handled:', err.name)
      })
    },
    handleActionClick(path, event) {
      console.log('Action clicked:', path, event)
      this.navigateTo(path)
    }
  }
}
</script>

<style scoped>
.databridge-overview {
  padding: 24px;
}

.overview-header {
  text-align: center;
  margin-bottom: 48px;
}

.overview-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.overview-header p {
  font-size: 16px;
  color: #8c8c8c;
  margin: 0;
}

.overview-cards {
  margin-bottom: 48px;
}

.feature-card {
  height: 200px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.feature-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 16px;
}

.card-icon {
  margin-right: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: #f6f8fa;
  border-radius: 12px;
}

.card-info {
  flex: 1;
}

.card-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.card-info p {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 16px;
  line-height: 1.5;
}

.card-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-actions {
  text-align: center;
  padding: 32px;
  background: #fafafa;
  border-radius: 8px;
}

.quick-actions h3 {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 24px;
}
</style>