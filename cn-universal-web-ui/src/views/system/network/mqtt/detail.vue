<template>
  <div class="app-container">
    <a-card :bordered="false">
      <div class="page-header">
        <div class="header-left">
          <a-button type="text" icon="left" @click="$router.back()" class="back-btn" />
          <div class="page-title">
            <h1>{{ networkInfo.name || 'MQTT网络组件' }}</h1>
          </div>
          <a-tag :color="networkInfo.running ? 'green' : 'red'" style="margin-left: 12px;">
            {{ networkInfo.running ? '运行中' : '已停止' }}
          </a-tag>
        </div>
        <div class="header-right">
          <a-button 
            v-if="!editing" 
            type="primary" 
            icon="edit" 
            @click="startEdit" 
            v-hasPermi="['network:mqtt:edit']">
            编辑配置
          </a-button>
          <template v-else>
            <a-button @click="cancelEdit" style="margin-right: 8px;">取消</a-button>
            <a-button type="primary" @click="handleSaveAll" :loading="saving" v-hasPermi="['network:mqtt:edit']">
              保存配置
            </a-button>
          </template>
        </div>
      </div>
      
      <a-spin :spinning="loading" tip="Loading...">
        <!-- 自定义标签页导航 -->
        <div class="custom-tabs-container">
          <div class="custom-tabs-nav">
            <div class="custom-tab-item" :class="{ active: activeTab === '1' }" @click="switchTab('1')">
              <a-icon type="info-circle" style="margin-right: 6px;" />
              基础配置
            </div>
            <div class="custom-tab-item" :class="{ active: activeTab === '2' }" @click="switchTab('2')">
              <a-icon type="api" style="margin-right: 6px;" />
              主题订阅
            </div>
            <div class="custom-tab-item" :class="{ active: activeTab === '3' }" @click="switchTab('3')">
              <a-icon type="setting" style="margin-right: 6px;" />
              高级设置
            </div>
          </div>

          <!-- 标签页内容 -->
          <div class="custom-tab-content">
            <!-- 基础配置（合并基础信息和连接配置） -->
            <div v-show="activeTab === '1'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>基础配置</h3>
                  <a-button v-if="!editing" type="link" size="small" @click="startEdit" v-hasPermi="['network:mqtt:edit']">
                    <a-icon type="edit" /> 编辑
                  </a-button>
                </div>
                
                <!-- 基础信息部分 -->
                <div class="info-section">
                  <div class="section-subtitle">基础信息</div>
                  <div class="basic-info-grid compact-grid">
                    <div class="info-item">
                      <span class="info-label">组件类型</span>
                      <a-tag color="blue">
                        <template v-if="networkInfo.type === 'MQTT_CLIENT'">MQTT客户端</template>
                        <template v-else-if="networkInfo.type === 'MQTT_SERVER'">MQTT服务端</template>
                        <template v-else>{{ networkInfo.type }}</template>
                      </a-tag>
                    </div>
                    <div class="info-item">
                      <span class="info-label">创建时间</span>
                      <span class="info-value">{{ parseTime(networkInfo.createDate) }}</span>
                    </div>
                    <div class="info-item info-item-full">
                      <span class="info-label">唯一标识</span>
                      <div class="info-value-group">
                        <span class="info-value code">{{ networkInfo.unionId }}</span>
                        <a-button type="text" size="small" class="copy-action-btn" @click.stop="copyToClipboard(networkInfo.unionId)" title="复制">
                          <a-icon type="copy"/>
                        </a-button>
                      </div>
                    </div>
                    <div class="info-item info-item-full" v-if="networkInfo.description">
                      <span class="info-label">描述信息</span>
                      <span class="info-value">{{ networkInfo.description }}</span>
                    </div>
                  </div>
                </div>

                <!-- 连接配置部分 -->
                <div class="info-section" style="margin-top: 24px;">
                  <div class="section-subtitle">连接配置</div>
                  <template v-if="!editing">
                    <div class="basic-info-grid compact-grid">
                      <div class="info-item" v-for="field in connectionFields" :key="field.key" v-if="!field.hide">
                        <span class="info-label">{{ field.label }}</span>
                        <span class="info-value">{{ renderReadValue(field) }}</span>
                      </div>
                    </div>
                  </template>
                  
                  <template v-else>
                    <a-form :model="formData" layout="vertical">
                      <a-row :gutter="[16, 0]">
                        <a-col :span="12" v-for="field in connectionFields" :key="field.key" v-if="!field.hide">
                          <a-form-item :label="field.label" :required="field.required">
                            <a-input
                              v-if="field.type === 'string' && field.key !== 'password'"
                              v-model="formData[field.key]"
                              :placeholder="field.remark"
                            />
                            <a-input-password
                              v-else-if="field.key === 'password'"
                              v-model="formData[field.key]"
                              :placeholder="field.remark"
                            />
                            <a-input-number
                              v-else-if="field.type === 'int'"
                              v-model="formData[field.key]"
                              :placeholder="field.remark"
                              style="width:100%"
                              :step="1"
                              :precision="0"
                              :min="field.min"
                              :max="field.max"
                            />
                            <a-select
                              v-else-if="field.type === 'select'"
                              v-model="formData[field.key]"
                              :placeholder="field.remark"
                            >
                              <a-select-option v-for="opt in field.options" :key="opt.value" :value="opt.value">
                                {{ opt.label }}
                              </a-select-option>
                            </a-select>
                            <a-switch
                              v-else-if="field.type === 'boolean'"
                              v-model="formData[field.key]"
                            />
                          </a-form-item>
                        </a-col>
                      </a-row>
                    </a-form>
                  </template>
                </div>
              </div>
            </div>

            <!-- 主题订阅 -->
            <div v-show="activeTab === '2'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>主题订阅配置</h3>
                  <a-button v-if="!editing" type="link" size="small" @click="startEdit" v-hasPermi="['network:mqtt:edit']">
                    <a-icon type="edit" /> 编辑
                  </a-button>
                </div>

                <!-- 已绑定产品 -->
                <div class="bound-products-section">
                  <div class="section-title">
                    <a-icon type="appstore" style="color: #1890ff; margin-right: 8px;" />
                    <span>已绑定产品</span>
                    <a-tag color="blue" style="margin-left: 8px;">
                      {{ (networkInfo.bindMqttServerProducts && networkInfo.bindMqttServerProducts.length) || 0 }}个
                    </a-tag>
                  </div>
                  <div v-if="networkInfo.bindMqttServerProducts && networkInfo.bindMqttServerProducts.length > 0" class="product-tags">
                    <a-tag 
                      v-for="product in networkInfo.bindMqttServerProducts" 
                      :key="product.productKey" 
                      color="blue" 
                      style="margin: 4px; padding: 4px 12px; font-size: 13px;">
                      {{ product.name }} ({{ product.productKey }})
                    </a-tag>
                  </div>
                  <div v-else class="empty-tip-small">
                    <span style="color: #8c8c8c;">暂无绑定产品</span>
                  </div>
                </div>

                <!-- 主题模式测试工具（仅编辑模式） -->
                <!-- <div v-if="editing" class="topic-tester-section"> -->
                <div class="topic-tester-section">

                  <div class="section-title">
                    <a-icon type="experiment" style="color: #52c41a; margin-right: 8px;" />
                    <span>主题模式测试工具</span>
                    <a-tooltip title="输入实际topic测试是否匹配配置的模式">
                      <a-icon type="question-circle" style="color: #999; margin-left: 8px; cursor: pointer;" />
                    </a-tooltip>
                  </div>
                  <div class="tester-content">
                    <a-input 
                      v-model="testTopic" 
                      placeholder="输入实际topic进行测试，例如: $third/up/device1/data"
                      @pressEnter="testTopicMatch"
                      style="margin-bottom: 12px;">
                      <a-button slot="addonAfter" type="primary" @click="testTopicMatch">测试匹配</a-button>
                    </a-input>
                    <div v-if="testResult" class="test-result" :class="{ 'test-success': testResult.matched, 'test-fail': !testResult.matched }">
                      <a-icon :type="testResult.matched ? 'check-circle' : 'close-circle'" />
                      <span>{{ testResult.message }}</span>
                      <span v-if="testResult.matched && testResult.productKey" class="matched-product">
                        匹配产品: {{ getProductName(testResult.productKey) }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 主题映射配置 -->
                <div class="topic-mapping-section">
                  <div class="section-title">
                    <a-icon type="link" style="color: #fa8c16; margin-right: 8px;" />
                    <span>主题映射配置</span>
                    <a-tooltip title="配置订阅主题与产品的映射关系，用于消息路由。留空产品时将根据实际消息topic自动匹配productKey">
                      <a-icon type="question-circle" style="color: #999; margin-left: 8px; cursor: pointer;" />
                    </a-tooltip>
                    <a-button 
                      v-if="editing" 
                      type="primary" 
                      icon="plus" 
                      size="small" 
                      @click="handleAddTopicMapping" 
                      style="margin-left: auto;"
                      v-hasPermi="['network:mqtt:edit']">
                      添加主题
                    </a-button>
                  </div>

                  <template v-if="!editing">
                    <a-table 
                      :columns="topicMappingColumns" 
                      :dataSource="topicMappings" 
                      :pagination="false"
                      size="middle"
                      :rowKey="(record, index) => index"
                      style="margin-top: 16px;">
                      <template slot="qos" slot-scope="text">
                        <a-tag :color="getQosColor(text)">
                          {{ text !== null && text !== undefined ? String(text) : '-' }}
                        </a-tag>
                      </template>
                      <template slot="topicCategory" slot-scope="text">
                        <a-tag :color="text === 'THING_MODEL' ? 'blue' : text === 'PASSTHROUGH' ? 'green' : 'default'">
                          {{ text === 'THING_MODEL' ? '物模型' : text === 'PASSTHROUGH' ? '透传' : '未配置' }}
                        </a-tag>
                      </template>
                      <template slot="productKey" slot-scope="text, record">
                        <span v-if="text" class="product-name">{{ getProductName(text) }}</span>
                        <span v-else class="text-muted">自动匹配</span>
                      </template>
                      <template slot="enabled" slot-scope="text">
                        <a-tag :color="text ? 'green' : 'red'">{{ text ? '启用' : '禁用' }}</a-tag>
                      </template>
                    </a-table>
                    <div v-if="topicMappings.length === 0" class="empty-tip">
                      <a-empty description="暂无主题映射配置" :image="false" />
                    </div>
                  </template>

                  <template v-else>
                    <a-table 
                      :columns="topicMappingEditColumns" 
                      :dataSource="topicMappings" 
                      :pagination="false"
                      size="middle"
                      :rowKey="(record, index) => index"
                      style="margin-top: 16px;">
                      <template slot="topicPattern" slot-scope="text, record, index">
                        <a-input 
                          v-model="record.topicPattern" 
                          placeholder="例如: $third/up/# 或 device/+/data" 
                          @blur="validateTopicPattern(record, index)">
                          <template slot="suffix">
                            <a-tooltip title="支持MQTT通配符：+ 匹配单级， # 匹配多级">
                              <a-icon type="question-circle" style="color: #999;" />
                            </a-tooltip>
                          </template>
                        </a-input>
                        <div v-if="record.topicError" class="field-error">{{ record.topicError }}</div>
                      </template>
                      <template slot="qos" slot-scope="text, record, index">
                        <a-select v-model="record.qos" style="width: 100%">
                          <a-select-option :value="0">0 - 最多一次</a-select-option>
                          <a-select-option :value="1">1 - 至少一次（推荐）</a-select-option>
                          <a-select-option :value="2">2 - 仅一次</a-select-option>
                        </a-select>
                      </template>
                      <template slot="topicCategory" slot-scope="text, record, index">
                        <div class="select-with-tooltip">
                          <a-select 
                            v-model="record.topicCategory" 
                            placeholder="请选择主题类型（必选）"
                            style="flex: 1;"
                            :required="true">
                            <a-select-option value="THING_MODEL">物模型</a-select-option>
                            <a-select-option value="PASSTHROUGH">透传</a-select-option>
                          </a-select>
                          <a-tooltip title="物模型：标准JSON格式；透传：需要编解码处理">
                            <a-icon type="info-circle" class="tooltip-icon" />
                          </a-tooltip>
                        </div>
                      </template>
                      <template slot="productKey" slot-scope="text, record, index">
                        <div class="select-with-tooltip">
                          <a-select 
                            v-model="record.productKey" 
                            placeholder="选择产品（留空则自动匹配）"
                            allowClear
                            showSearch
                            :filterOption="filterProductOption"
                            style="flex: 1;"
                            :notFoundContent="(!networkInfo.bindMqttServerProducts || networkInfo.bindMqttServerProducts.length === 0) ? '暂无绑定产品' : undefined">
                            <template v-if="networkInfo.bindMqttServerProducts && networkInfo.bindMqttServerProducts.length > 0">
                              <a-select-option 
                                v-for="product in networkInfo.bindMqttServerProducts" 
                                :key="product.productKey" 
                                :value="product.productKey">
                                {{ product.name }} ({{ product.productKey }})
                              </a-select-option>
                            </template>
                            <template v-else>
                              <a-select-option disabled value="">
                                暂无绑定产品，留空将自动匹配
                              </a-select-option>
                            </template>
                          </a-select>
                          <a-tooltip v-if="!record.productKey" title="留空时将根据实际消息topic自动匹配productKey">
                            <a-icon type="info-circle" class="tooltip-icon" />
                          </a-tooltip>
                        </div>
                      </template>
                      <template slot="enabled" slot-scope="text, record, index">
                        <a-switch v-model="record.enabled" />
                      </template>
                      <template slot="action" slot-scope="text, record, index">
                        <a-button type="link" size="small" danger @click="handleDeleteTopicMapping(index)">
                          <a-icon type="delete" /> 删除
                        </a-button>
                      </template>
                    </a-table>
                    <div v-if="topicMappings.length === 0" class="empty-tip">
                      <a-button type="dashed" icon="plus" @click="handleAddTopicMapping" block>
                        添加第一个主题映射
                      </a-button>
                    </div>
                  </template>
                </div>
              </div>
            </div>

            <!-- 高级设置 -->
            <div v-show="activeTab === '3'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>高级设置</h3>
                  <a-button v-if="!editing" type="link" size="small" @click="startEdit" v-hasPermi="['network:mqtt:edit']">
                    <a-icon type="edit" /> 编辑
                  </a-button>
                </div>
                
                <template v-if="!editing">
                  <div class="basic-info-grid">
                    <div class="info-item" v-for="field in advancedFields" :key="field.key" v-if="!field.hide">
                      <span class="info-label">{{ field.label }}</span>
                      <span class="info-value">{{ renderReadValue(field) }}</span>
                    </div>
                  </div>
                </template>
                
                <template v-else>
                  <a-form :model="formData" layout="vertical">
                    <a-row :gutter="16">
                      <a-col :span="8" v-for="field in advancedFields" :key="field.key" v-if="!field.hide">
                        <a-form-item :label="field.label">
                          <a-input
                            v-if="field.type === 'string'"
                            v-model="formData[field.key]"
                            :placeholder="field.remark"
                          />
                          <a-input-number
                            v-else-if="field.type === 'int'"
                            v-model="formData[field.key]"
                            :placeholder="field.remark"
                            style="width:100%"
                            :step="1"
                            :precision="0"
                            :min="field.min"
                            :max="field.max"
                          />
                          <a-select
                            v-else-if="field.type === 'select'"
                            v-model="formData[field.key]"
                            :placeholder="field.remark"
                          >
                            <a-select-option v-for="opt in field.options" :key="opt.value" :value="opt.value">
                              {{ opt.label }}
                            </a-select-option>
                          </a-select>
                          <a-switch
                            v-else-if="field.type === 'boolean'"
                            v-model="formData[field.key]"
                          />
                        </a-form-item>
                      </a-col>
                    </a-row>
                  </a-form>
                </template>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<script>
import {getNetwork, updateNetwork} from '@/api/system/network'
import {getDictMap, getDicts} from '@/api/system/dict/data'

export default {
  name: 'MqttNetworkDetail',
  data() {
    return {
      loading: false,
      saving: false,
      networkInfo: {
        id: undefined,
        type: undefined,
        unionId: undefined,
        productKey: undefined,
        name: undefined,
        description: undefined,
        configuration: '{}',
        state: false,
        running: false,
        createDate: undefined,
        bindMqttServerProducts: []
      },
      configFields: [],
      formData: {},
      formDataBackup: {},
      editing: false,
      activeTab: '1',
      editingTopicMapping: false,
      topicMappings: [],
      topicMappingsBackup: [],
      testTopic: '',
      testResult: null,
      topicMappingColumns: [
        { title: '主题模式', dataIndex: 'topicPattern', key: 'topicPattern', width: '30%' },
        { title: '数据类型', dataIndex: 'topicCategory', key: 'topicCategory', width: '15%', scopedSlots: { customRender: 'topicCategory' } },
        { title: 'QoS', dataIndex: 'qos', key: 'qos', width: '10%', scopedSlots: { customRender: 'qos' } },
        { title: '关联产品', dataIndex: 'productKey', key: 'productKey', width: '25%', scopedSlots: { customRender: 'productKey' } },
        { title: '状态', dataIndex: 'enabled', key: 'enabled', width: '10%', scopedSlots: { customRender: 'enabled' } }
      ],
      topicMappingEditColumns: [
        { title: '订阅主题', dataIndex: 'topicPattern', key: 'topicPattern', width: '28%', scopedSlots: { customRender: 'topicPattern' } },
        { title: '数据类型', dataIndex: 'topicCategory', key: 'topicCategory', width: '15%', scopedSlots: { customRender: 'topicCategory' } },
        { title: 'QoS', dataIndex: 'qos', key: 'qos', width: '10%', scopedSlots: { customRender: 'qos' } },
        { title: '关联产品', dataIndex: 'productKey', key: 'productKey', width: '27%', scopedSlots: { customRender: 'productKey' } },
        { title: '启用', dataIndex: 'enabled', key: 'enabled', width: '10%', scopedSlots: { customRender: 'enabled' } },
        { title: '操作', key: 'action', width: '10%', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  computed: {
    connectionFields() {
      return this.configFields.filter(f => 
        !f.hide && ['host', 'port', 'username', 'password', 'clientIdPrefix', 'connectTimeout'].includes(f.key)
      )
    },
    advancedFields() {
      return this.configFields.filter(f => 
        !f.hide && !['host', 'port', 'username', 'password', 'clientIdPrefix', 'connectTimeout', 'subscribeTopics'].includes(f.key)
      )
    }
  },
  created() {
    this.getNetworkDetail()
  },
  methods: {
    getNetworkDetail() {
      const id = this.$route.params.id
      if (!id) {
        this.$message.error('MQTT网络组件ID不能为空')
        this.goBack()
        return
      }
      this.loading = true
      getNetwork(id).then(async response => {
        this.networkInfo = response.data
        if (!this.networkInfo.bindMqttServerProducts) {
          this.networkInfo.bindMqttServerProducts = []
        }
        this.loading = false
        await this.loadConfigFields()
        this.loadTopicMappings()
      }).catch(() => {
        this.loading = false
        this.goBack()
      })
    },
    startEdit() {
      this.editing = true
      this.formDataBackup = JSON.parse(JSON.stringify(this.formData))
      this.topicMappingsBackup = JSON.parse(JSON.stringify(this.topicMappings))
    },
    cancelEdit() {
      this.editing = false
      this.formData = JSON.parse(JSON.stringify(this.formDataBackup))
      this.topicMappings = JSON.parse(JSON.stringify(this.topicMappingsBackup))
      this.testTopic = ''
      this.testResult = null
    },
    switchTab(tab) {
      this.activeTab = tab
    },
    handleSaveAll() {
      // 验证主题配置
      for (let i = 0; i < this.topicMappings.length; i++) {
        const mapping = this.topicMappings[i]
        if (!mapping.topicPattern || mapping.topicPattern.trim() === '') {
          this.$message.error(`第${i + 1}行的主题模式不能为空`)
          this.activeTab = '2'
          return
        }
        // 验证主题类型必选
        if (!mapping.topicCategory) {
          this.$message.error(`第${i + 1}行的主题类型不能为空，请选择物模型或透传`)
          this.activeTab = '2'
          return
        }
      }

      this.saving = true
      try {
        const config = {...this.formData}
        const subscribeTopics = this.topicMappings.map(mapping => {
          const item = {
            topicPattern: mapping.topicPattern.trim(),
            qos: mapping.qos,
            enabled: mapping.enabled
          }
          if (mapping.productKey) {
            item.productKey = mapping.productKey
          }
          if (mapping.topicCategory) {
            item.topicCategory = mapping.topicCategory
          }
          return item
        })
        config.subscribeTopics = subscribeTopics

        updateNetwork({
          id: this.networkInfo.id,
          type: this.networkInfo.type,
          unionId: this.networkInfo.unionId,
          productKey: this.networkInfo.productKey,
          name: this.networkInfo.name,
          description: this.networkInfo.description,
          configuration: JSON.stringify(config)
        }).then(() => {
          this.$message.success('配置保存成功')
          this.editing = false
          this.getNetworkDetail()
        }).catch(() => {
          this.$message.error('保存失败')
        }).finally(() => {
          this.saving = false
        })
      } catch (error) {
        console.error('保存配置失败:', error)
        this.$message.error('保存失败: ' + error.message)
        this.saving = false
      }
    },
    testTopicMatch() {
      if (!this.testTopic || !this.testTopic.trim()) {
        this.$message.warning('请输入要测试的topic')
        return
      }
      const topic = this.testTopic.trim()
      let matched = false
      let matchedProductKey = null
      let matchedPattern = null

      for (const mapping of this.topicMappings) {
        if (!mapping.enabled || !mapping.topicPattern) continue
        if (this.matchesTopicPattern(topic, mapping.topicPattern)) {
          matched = true
          matchedPattern = mapping.topicPattern
          if (mapping.productKey) {
            matchedProductKey = mapping.productKey
          }
          break
        }
      }

      if (matched) {
        this.testResult = {
          matched: true,
          message: `匹配成功！匹配模式: ${matchedPattern}`,
          productKey: matchedProductKey
        }
      } else {
        this.testResult = {
          matched: false,
          message: '未匹配任何主题模式'
        }
      }
    },
    matchesTopicPattern(topic, pattern) {
      const regex = pattern
        .replace(/\//g, '\\/')
        .replace(/\+/g, '[^/]+')
        .replace(/#/g, '.*')
        .replace(/\$/g, '\\$')
      try {
        return new RegExp(`^${regex}$`).test(topic)
      } catch (e) {
        return false
      }
    },
    async loadConfigFields() {
      const res = await getDictMap(['mqtt_parser_type'])
      const dicts = res.data['mqtt_parser_type'] || []
      this.configFields = []
      for (const d of dicts) {
        let meta = {}
        if (d.remark && typeof d.remark === 'string') {
          try {
            meta = JSON.parse(d.remark)
          } catch (e) {
            meta = {}
          }
        }
        const field = {
          key: d.dictValue,
          label: d.dictLabel,
          remark: meta.remark || '',
          type: meta.type || 'string',
          hide: meta.hide === 'true',
          options: [],
          default: meta.default,
          required: meta.required === 'true',
          min: meta.min,
          max: meta.max,
          fullWidth: meta.fullWidth === 'true',
          help: meta.help
        }
        if (field.type === 'select' && meta.url) {
          const dictRes = await getDicts(meta.url)
          field.options = (dictRes.data || []).map(
            opt => ({label: opt.dictLabel, value: opt.dictValue}))
        }
        if (field.type === 'boolean') {
          field.options = [
            {label: '是', value: true},
            {label: '否', value: false}
          ]
        }
        this.configFields.push(field)
      }
      let config = {}
      try {
        config = JSON.parse(this.networkInfo.configuration)
      } catch {
      }
      this.configFields.forEach(f => {
        const val = config[f.key]
        this.$set(this.formData, f.key,
          val !== undefined ? val : f.default !== undefined ? f.default : '')
      })
      this.formDataBackup = JSON.parse(JSON.stringify(this.formData))
    },
    renderReadValue(field) {
      const val = this.formData[field.key]
      // 密码字段显示为掩码
      if (field.key === 'password' || field.key.toLowerCase().includes('password')) {
        if (val && val.trim()) {
          return '••••••••'
        }
        return '未配置'
      }
      if (field.type === 'boolean') {
        return val === true || val === 'true' ? '是' : '否'
      }
      if (field.type === 'select' && field.options && field.options.length) {
        const opt = field.options.find(o => o.value === val)
        return opt ? opt.label : val
      }
      return val || '未配置'
    },
    loadTopicMappings() {
      try {
        const config = JSON.parse(this.networkInfo.configuration || '{}')
        const subscribeTopics = config.subscribeTopics
        
        if (!subscribeTopics) {
          this.topicMappings = []
          return
        }
        
        if (typeof subscribeTopics === 'string') {
          this.topicMappings = subscribeTopics.split(/[,;]/).map(topic => ({
            topicPattern: topic.trim(),
            qos: config.qos || 0,
            enabled: true,
            productKey: null,
            topicCategory: null,
            topicError: null
          })).filter(item => item.topicPattern)
        } else if (Array.isArray(subscribeTopics)) {
          this.topicMappings = subscribeTopics.map(item => {
            if (typeof item === 'string') {
              return {
                topicPattern: item,
                qos: config.qos || 0,
                enabled: true,
                productKey: null,
                topicCategory: null,
                topicError: null
              }
            } else {
              return {
                topicPattern: item.topicPattern || item.topic || '',
                qos: item.qos !== undefined ? item.qos : (config.qos || 0),
                enabled: item.enabled !== undefined ? item.enabled : true,
                productKey: item.productKey || null,
                topicCategory: item.topicCategory || null,
                topicError: null
              }
            }
          }).filter(item => item.topicPattern)
        } else {
          this.topicMappings = []
        }
        
        this.topicMappingsBackup = JSON.parse(JSON.stringify(this.topicMappings))
      } catch (error) {
        console.error('加载主题映射配置失败:', error)
        this.topicMappings = []
        this.topicMappingsBackup = []
      }
    },
    handleAddTopicMapping() {
      this.topicMappings.push({
        topicPattern: '',
        qos: 1,
        enabled: true,
        productKey: null,
        topicCategory: null,
        topicError: null
      })
    },
    handleDeleteTopicMapping(index) {
      this.topicMappings.splice(index, 1)
    },
    validateTopicPattern(record, index) {
      if (!record.topicPattern || record.topicPattern.trim() === '') {
        this.$set(record, 'topicError', '主题模式不能为空')
        return false
      }
      const pattern = record.topicPattern.trim()
      if (pattern.includes('##') || pattern.includes('++')) {
        this.$set(record, 'topicError', '主题模式格式错误')
        return false
      }
      this.$set(record, 'topicError', null)
      return true
    },
    getProductName(productKey) {
      if (!this.networkInfo.bindMqttServerProducts) {
        return productKey
      }
      const product = this.networkInfo.bindMqttServerProducts.find(p => p.productKey === productKey)
      return product ? `${product.name} (${productKey})` : productKey
    },
    getQosColor(qos) {
      if (qos === 0) {
        return 'orange'
      } else if (qos === 1) {
        return 'blue'
      } else if (qos === 2) {
        return 'green'
      }
      return 'default'
    },
    filterProductOption(input, option) {
      const text = option.componentOptions.children[0].text
      return text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },
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
    goBack() {
      this.$router.go(-1)
    },
    parseTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
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

.header-right {
  display: flex;
  align-items: center;
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

.basic-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.compact-grid {
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}

.info-section {
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #595959;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
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
  
  &.highlight {
    color: #1890ff;
    font-weight: 500;
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

/* 表单操作按钮 */
.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

/* 已绑定产品区域 */
.bound-products-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.empty-tip-small {
  margin-top: 12px;
  padding: 12px;
  text-align: center;
}

/* 主题测试工具区域 */
.topic-tester-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f6ffed;
  border-radius: 6px;
  border: 1px solid #b7eb8f;
}

.tester-content {
  margin-top: 12px;
}

.test-result {
  padding: 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-top: 12px;
  
  &.test-success {
    background: #f6ffed;
    border: 1px solid #b7eb8f;
    color: #52c41a;
  }
  
  &.test-fail {
    background: #fff2f0;
    border: 1px solid #ffccc7;
    color: #ff4d4f;
  }
  
  .matched-product {
    margin-left: auto;
    font-size: 12px;
    color: #1890ff;
    font-weight: 500;
  }
}

/* 主题映射配置区域 */
.topic-mapping-section {
  margin-top: 24px;
}

.product-name {
  color: #1890ff;
  font-weight: 500;
}

.text-muted {
  color: #8c8c8c;
  font-style: italic;
}

.product-tip {
  margin-top: 4px;
}

.field-error {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
}

.select-with-tooltip {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.tooltip-icon {
  color: #999;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: 2px;
}
</style>