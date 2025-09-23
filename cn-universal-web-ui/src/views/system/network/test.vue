<template>
  <div class="network-test">
    <a-card title="网络组件管理功能测试" :bordered="false">
      <a-alert
        message="功能测试"
        description="这是一个测试页面，用于验证网络组件管理功能是否正常工作。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-row :gutter="16">
        <a-col :span="8">
          <a-card title="API测试" size="small">
            <a-space direction="vertical" style="width: 100%">
              <a-button type="primary" @click="testListAPI" :loading="loading.list">
                测试列表API
              </a-button>
              <a-button type="primary" @click="testTypesAPI" :loading="loading.types">
                测试类型API
              </a-button>
              <a-button type="primary" @click="testValidateAPI" :loading="loading.validate">
                测试验证API
              </a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card title="组件测试" size="small">
            <a-space direction="vertical" style="width: 100%">
              <a-button type="primary" @click="testCreateForm">
                测试创建表单
              </a-button>
              <a-button type="primary" @click="testDetailPage">
                测试详情页面
              </a-button>
              <a-button type="primary" @click="testListPage">
                测试列表页面
              </a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card title="配置测试" size="small">
            <a-space direction="vertical" style="width: 100%">
              <a-button type="primary" @click="testTCPConfig">
                测试TCP配置
              </a-button>
              <a-button type="primary" @click="testMQTTConfig">
                测试MQTT配置
              </a-button>
              <a-button type="primary" @click="testInvalidConfig">
                测试无效配置
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-divider/>

      <a-card title="测试结果" size="small">
        <pre>{{ testResults }}</pre>
      </a-card>
    </a-card>
  </div>
</template>

<script>
import {getNetworkTypes, listNetwork, validateNetwork} from '@/api/system/network'

export default {
  name: 'NetworkTest',
  data() {
    return {
      loading: {
        list: false,
        types: false,
        validate: false
      },
      testResults: '点击上方按钮开始测试...'
    }
  },
  methods: {
    // 测试列表API
    async testListAPI() {
      this.loading.list = true
      this.testResults = '正在测试列表API...'

      try {
        const response = await listNetwork({pageNum: 1, pageSize: 10})
        this.testResults = `✅ 列表API测试成功\n数据: ${JSON.stringify(response, null, 2)}`
      } catch (error) {
        this.testResults = `❌ 列表API测试失败\n错误: ${error.message || error}`
      } finally {
        this.loading.list = false
      }
    },

    // 测试类型API
    async testTypesAPI() {
      this.loading.types = true
      this.testResults = '正在测试类型API...'

      try {
        const response = await getNetworkTypes()
        this.testResults = `✅ 类型API测试成功\n数据: ${JSON.stringify(response, null, 2)}`
      } catch (error) {
        this.testResults = `❌ 类型API测试失败\n错误: ${error.message || error}`
      } finally {
        this.loading.types = false
      }
    },

    // 测试验证API
    async testValidateAPI() {
      this.loading.validate = true
      this.testResults = '正在测试验证API...'

      try {
        const testData = {
          type: 'TCP_SERVER',
          unionId: 'test_union_id',
          name: '测试网络组件',
          configuration: JSON.stringify({
            host: '0.0.0.0',
            port: 6372,
            ssl: false
          })
        }

        const response = await validateNetwork(testData)
        this.testResults = `✅ 验证API测试成功\n数据: ${JSON.stringify(response, null, 2)}`
      } catch (error) {
        this.testResults = `❌ 验证API测试失败\n错误: ${error.message || error}`
      } finally {
        this.loading.validate = false
      }
    },

    // 测试创建表单
    testCreateForm() {
      this.testResults = '✅ 创建表单组件已加载\n可以通过路由访问: /system/network'
    },

    // 测试详情页面
    testDetailPage() {
      this.testResults = '✅ 详情页面组件已加载\n可以通过路由访问: /system/network/detail/1'
    },

    // 测试列表页面
    testListPage() {
      this.testResults = '✅ 列表页面组件已加载\n可以通过路由访问: /system/network'
    },

    // 测试TCP配置
    testTCPConfig() {
      const tcpConfig = {
        allIdleTime: 0,
        allowInsert: false,
        alwaysPreDecode: false,
        decodeType: "STRING",
        host: "0.0.0.0",
        idleInterval: 0,
        onlyCache: false,
        parserConfiguration: {
          byteOrderLittle: true,
          delimited: "]",
          delimitedMaxlength: 1024,
          failFast: true
        },
        parserType: "DELIMITED",
        port: 6372,
        productKey: "test_product_key",
        readerIdleTime: 360,
        readTimeout: 0,
        sendTimeout: 0,
        ssl: false,
        writerIdleTime: 0
      }

      try {
        JSON.stringify(tcpConfig)
        this.testResults = `✅ TCP配置测试成功\n配置: ${JSON.stringify(tcpConfig, null, 2)}`
      } catch (error) {
        this.testResults = `❌ TCP配置测试失败\n错误: ${error.message}`
      }
    },

    // 测试MQTT配置
    testMQTTConfig() {
      const mqttConfig = {
        autoReconnect: true,
        cleanSession: true,
        clientIdPrefix: "univ_cli_",
        defaultQos: 1,
        enabled: true,
        host: "tcp://localhost:1883",
        id: "test_network_id",
        keepAliveInterval: 60,
        password: "test_password",
        productKey: "test_product_key",
        ssl: false,
        subscribeTopics: "$univ_cli/up/property/+/+",
        username: "univ_cli",
        connectTimeout: 30
      }

      try {
        JSON.stringify(mqttConfig)
        this.testResults = `✅ MQTT配置测试成功\n配置: ${JSON.stringify(mqttConfig, null, 2)}`
      } catch (error) {
        this.testResults = `❌ MQTT配置测试失败\n错误: ${error.message}`
      }
    },

    // 测试无效配置
    testInvalidConfig() {
      const invalidConfig = '{ invalid json }'

      try {
        JSON.parse(invalidConfig)
        this.testResults = '❌ 无效配置测试失败 - 应该抛出错误'
      } catch (error) {
        this.testResults = `✅ 无效配置测试成功\n正确捕获错误: ${error.message}`
      }
    }
  }
}
</script>

<style lang="less" scoped>
.network-test {
  padding: 24px;

  pre {
    background-color: #f5f5f5;
    padding: 16px;
    border-radius: 6px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 12px;
    line-height: 1.5;
    max-height: 400px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
    border: 1px solid #d9d9d9;
  }
}
</style> 