<template>
  <div class="network-component-bind-debug">
    <a-card title="网络组件绑定调试工具" :bordered="false">
      <a-alert
        message="调试信息"
        description="用于检查产品数据中的字段名和值"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="产品数据检查" size="small">
            <div style="margin-bottom: 16px;">
              <h4>产品对象：</h4>
              <pre
                style="background: #f5f5f5; padding: 8px; border-radius: 4px; font-size: 12px; max-height: 200px; overflow-y: auto;">
{{ JSON.stringify(product, null, 2) }}
              </pre>
            </div>

            <div style="margin-bottom: 16px;">
              <h4>字段检查：</h4>
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="product.networkUnionId">
                  <a-tag :color="product.networkUnionId ? 'green' : 'red'">
                    {{ product.networkUnionId || 'undefined/null' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="product.network_union_id">
                  <a-tag :color="product.network_union_id ? 'green' : 'red'">
                    {{ product.network_union_id || 'undefined/null' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="product.thirdPlatform">
                  <a-tag :color="product.thirdPlatform ? 'green' : 'red'">
                    {{ product.thirdPlatform || 'undefined/null' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="product.id">
                  <a-tag :color="product.id ? 'green' : 'red'">
                    {{ product.id || 'undefined/null' }}
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </div>

            <div>
              <h4>状态判断：</h4>
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="!product.networkUnionId">
                  <a-tag :color="!product.networkUnionId ? 'green' : 'red'">
                    {{ !product.networkUnionId ? 'true (未绑定)' : 'false (已绑定)' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="!product.network_union_id">
                  <a-tag :color="!product.network_union_id ? 'green' : 'red'">
                    {{ !product.network_union_id ? 'true (未绑定)' : 'false (已绑定)' }}
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="测试数据" size="small">
            <a-form layout="vertical">
              <a-form-item label="networkUnionId">
                <a-input v-model="testData.networkUnionId" placeholder="输入networkUnionId值"/>
              </a-form-item>
              <a-form-item label="network_union_id">
                <a-input v-model="testData.network_union_id" placeholder="输入network_union_id值"/>
              </a-form-item>
              <a-form-item label="thirdPlatform">
                <a-select v-model="testData.thirdPlatform" placeholder="选择平台">
                  <a-select-option value="tcp">TCP</a-select-option>
                  <a-select-option value="mqtt">MQTT</a-select-option>
                </a-select>
              </a-form-item>
            </a-form>

            <a-divider/>

            <a-space>
              <a-button type="primary" @click="updateTestData">
                更新测试数据
              </a-button>
              <a-button type="default" @click="resetTestData">
                重置数据
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-divider/>

      <a-card title="网络组件绑定组件" :bordered="false">
        <network-component-bind
          :product="product"
          @refresh="handleRefresh"
        />
      </a-card>
    </a-card>
  </div>
</template>

<script>
import NetworkComponentBind from './NetworkComponentBind'

export default {
  name: 'NetworkComponentBindDebug',
  components: {
    NetworkComponentBind
  },
  data() {
    return {
      product: {
        id: 1,
        name: '调试产品',
        thirdPlatform: 'tcp',
        networkUnionId: 'univ_mqtt_server'
      },
      testData: {
        networkUnionId: 'univ_mqtt_server',
        network_union_id: '',
        thirdPlatform: 'tcp'
      }
    }
  },
  methods: {
    updateTestData() {
      this.product = {
        ...this.product,
        networkUnionId: this.testData.networkUnionId,
        network_union_id: this.testData.network_union_id,
        thirdPlatform: this.testData.thirdPlatform
      }
    },

    resetTestData() {
      this.testData = {
        networkUnionId: 'univ_mqtt_server',
        network_union_id: '',
        thirdPlatform: 'tcp'
      }
      this.product = {
        id: 1,
        name: '调试产品',
        thirdPlatform: 'tcp',
        networkUnionId: 'univ_mqtt_server'
      }
    },

    handleRefresh() {
      console.log('收到刷新事件')
    }
  }
}
</script>

<style lang="less" scoped>
.network-component-bind-debug {
  padding: 20px;
}
</style> 