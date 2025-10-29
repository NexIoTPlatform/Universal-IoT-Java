<template>
  <div class="network-component-bind-test">
    <a-card title="网络组件绑定功能测试" :bordered="false">
      <a-alert
        message="功能测试"
        description="这是一个测试页面，用于验证网络组件绑定功能是否正常工作。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="模拟产品数据" size="small">
            <a-form layout="vertical">
              <a-form-item label="产品ID">
                <a-input v-model="testProduct.id" placeholder="产品ID"/>
              </a-form-item>
              <a-form-item :label="$t('product.name')">
                <a-input v-model="testProduct.name" placeholder="产品名称"/>
              </a-form-item>
              <a-form-item label="第三方平台">
                <a-select v-model="testProduct.thirdPlatform" placeholder="选择平台">
                  <a-select-option value="tcp">TCP</a-select-option>
                  <a-select-option value="mqtt">MQTT</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="网络组件ID">
                <a-input v-model="testProduct.networkUnionId" placeholder="网络组件ID（可选）"/>
              </a-form-item>
            </a-form>

            <a-divider/>

            <a-space>
              <a-button type="primary" @click="testUnbindState">
                测试未绑定状态
              </a-button>
              <a-button type="primary" @click="testBindState">
                测试已绑定状态
              </a-button>
              <a-button type="default" @click="resetTest">
                重置测试数据
              </a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="测试结果" size="small">
            <div style="margin-bottom: 16px;">
              <h4>当前状态：</h4>
              <a-tag :color="testProduct.networkUnionId ? 'green' : 'orange'">
                {{ testProduct.networkUnionId ? '已绑定' : '未绑定' }}
              </a-tag>
            </div>

            <div style="margin-bottom: 16px;">
              <h4>产品信息：</h4>
              <pre style="background: #f5f5f5; padding: 8px; border-radius: 4px; font-size: 12px;">
{{ JSON.stringify(testProduct, null, 2) }}
              </pre>
            </div>

            <div>
              <h4>测试日志：</h4>
              <a-textarea
                v-model="testLog"
                :rows="6"
                readonly
                placeholder="测试日志将显示在这里..."
              />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-divider/>

      <a-card title="网络组件绑定组件" :bordered="false">
        <network-component-bind
          :product="testProduct"
          @refresh="handleRefresh"
        />
      </a-card>
    </a-card>
  </div>
</template>

<script>
import NetworkComponentBind from './NetworkComponentBind'

export default {
  name: 'NetworkComponentBindTest',
  components: {
    NetworkComponentBind
  },
  data() {
    return {
      testProduct: {
        id: 1,
        name: '测试产品',
        thirdPlatform: 'tcp',
        networkUnionId: ''
      },
      testLog: '点击上方按钮开始测试...'
    }
  },
  methods: {
    testUnbindState() {
      this.testProduct = {
        id: 1,
        name: '测试产品',
        thirdPlatform: 'tcp',
        networkUnionId: ''
      }
      this.testLog = '✅ 测试未绑定状态\n'
      this.testLog += '- 应该显示组件列表\n'
      this.testLog += '- 可以选择组件进行绑定\n'
      this.testLog += '- 不应该显示已绑定组件信息'
    },

    testBindState() {
      this.testProduct = {
        id: 1,
        name: '测试产品',
        thirdPlatform: 'tcp',
        networkUnionId: 'test_union_id_123'
      }
      this.testLog = '✅ 测试已绑定状态\n'
      this.testLog += '- 应该显示已绑定组件信息\n'
      this.testLog += '- 不应该显示组件列表\n'
      this.testLog += '- 可以查看详情和解绑'
    },

    resetTest() {
      this.testProduct = {
        id: 1,
        name: '测试产品',
        thirdPlatform: 'tcp',
        networkUnionId: ''
      }
      this.testLog = '🔄 测试数据已重置...'
    },

    handleRefresh() {
      this.testLog += '\n🔄 收到刷新事件，产品数据已更新'
    }
  }
}
</script>

<style lang="less" scoped>
.network-component-bind-test {
  padding: 20px;
}
</style> 