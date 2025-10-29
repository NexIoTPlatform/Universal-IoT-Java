<template>
  <a-modal
    title="产品节点生成器"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    width="800px"
    :footer="null"
  >
    <div class="product-node-generator">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form layout="vertical">
            <a-form-item label="选择产品">
              <a-select
                v-model="selectedProductKey"
                placeholder="请选择产品"
                @change="onProductChange"
                show-search
                :filter-option="filterProduct"
              >
                <a-select-option
                  v-for="product in products"
                  :key="product.productKey"
                  :value="product.productKey"
                >
                  {{ product.productName }} ({{ product.productKey }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-col>
        <a-col :span="12">
          <a-button type="primary" @click="generateNodes" :loading="generating">
            <a-icon type="plus"/>
            生成节点
          </a-button>
          <a-button style="margin-left: 8px" @click="previewNodes">
            <a-icon type="eye"/>
            预览节点
          </a-button>
        </a-col>
      </a-row>

      <!-- 物模型预览 -->
      <div v-if="productMetadata" class="metadata-preview">
        <a-divider>物模型预览</a-divider>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic title="属性数量" :value="productMetadata.properties?.length || 0"/>
          </a-col>
          <a-col :span="8">
            <a-statistic title="事件数量" :value="productMetadata.events?.length || 0"/>
          </a-col>
          <a-col :span="8">
            <a-statistic title="功能数量" :value="productMetadata.functions?.length || 0"/>
          </a-col>
        </a-row>
      </div>

      <!-- 生成的节点预览 -->
      <div v-if="generatedNodes.length > 0" class="generated-nodes">
        <a-divider>生成的节点</a-divider>
        <a-tabs>
          <a-tab-pane key="events" tab="事件节点">
            <a-list
              :data-source="generatedNodes.filter(n => n.category === 'product_events')"
              :grid="{ gutter: 16, column: 3 }"
            >
              <a-list-item slot="renderItem" slot-scope="node">
                <a-card size="small" :title="node.name">
                  <template slot="extra">
                    <a-icon :type="node.icon"/>
                  </template>
                  <p>{{ node.type }}</p>
                </a-card>
              </a-list-item>
            </a-list>
          </a-tab-pane>
          <a-tab-pane key="properties" tab="属性节点">
            <a-list
              :data-source="generatedNodes.filter(n => n.category === 'product_properties')"
              :grid="{ gutter: 16, column: 3 }"
            >
              <a-list-item slot="renderItem" slot-scope="node">
                <a-card size="small" :title="node.name">
                  <template slot="extra">
                    <a-icon :type="node.icon"/>
                  </template>
                  <p>{{ node.type }}</p>
                </a-card>
              </a-list-item>
            </a-list>
          </a-tab-pane>
          <a-tab-pane key="functions" tab="功能节点">
            <a-list
              :data-source="generatedNodes.filter(n => n.category === 'product_functions')"
              :grid="{ gutter: 16, column: 3 }"
            >
              <a-list-item slot="renderItem" slot-scope="node">
                <a-card size="small" :title="node.name">
                  <template slot="extra">
                    <a-icon :type="node.icon"/>
                  </template>
                  <p>{{ node.type }}</p>
                </a-card>
              </a-list-item>
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>

    <template slot="footer">
      <a-button @click="handleCancel">{{ $t('button.cancel') }}</a-button>
      <a-button type="primary" @click="handleOk" :disabled="generatedNodes.length === 0">
        确认生成
      </a-button>
    </template>
  </a-modal>
</template>

<script>
import {getModel, listAllProduct} from '@/api/system/dev/product'

export default {
  name: 'ProductNodeGenerator',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      products: [],
      selectedProductKey: null,
      productMetadata: null,
      generatedNodes: [],
      generating: false
    }
  },
  created() {
    this.loadProducts()
  },
  methods: {
    // 加载产品列表
    async loadProducts() {
      try {
        const response = await listAllProduct()
        this.products = response.data || []
      } catch (error) {
        this.$message.error('加载产品列表失败')
        console.error('加载产品列表失败:', error)
      }
    },

    // 产品选择变化
    async onProductChange(productKey) {
      if (!productKey) {
        this.productMetadata = null
        this.generatedNodes = []
        return
      }

      try {
        const response = await getModel(productKey)
        this.productMetadata = response.data
        this.generateNodes()
      } catch (error) {
        this.$message.error('加载产品物模型失败')
        console.error('加载产品物模型失败:', error)
      }
    },

    // 产品搜索过滤
    filterProduct(input, option) {
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase())
        >= 0
    },

    // 生成节点
    generateNodes() {
      if (!this.productMetadata) {
        this.$message.warning('请先选择产品')
        return
      }

      this.generating = true
      try {
        const nodes = []

        // 生成事件节点
        if (this.productMetadata.events) {
          const eventNodes = this.generateEventNodes(this.productMetadata.events)
          nodes.push(...eventNodes)
        }

        // 生成属性节点
        if (this.productMetadata.properties) {
          const propertyNodes = this.generatePropertyNodes(this.productMetadata.properties)
          nodes.push(...propertyNodes)
        }

        // 生成功能节点
        if (this.productMetadata.functions) {
          const functionNodes = this.generateFunctionNodes(this.productMetadata.functions)
          nodes.push(...functionNodes)
        }

        this.generatedNodes = nodes
        this.$emit('nodes-generated', nodes)
        this.$message.success(`成功生成 ${nodes.length} 个节点`)
      } catch (error) {
        this.$message.error('生成节点失败')
        console.error('生成节点失败:', error)
      } finally {
        this.generating = false
      }
    },

    // 生成设备事件节点
    generateEventNodes(events) {
      return events.map(event => ({
        type: `product_event_${event.id}`,
        name: event.name || event.id,
        icon: this.getEventIcon(event),
        category: 'product_events',
        description: event.description || '',
        config: {
          productKey: this.selectedProductKey,
          eventId: event.id,
          eventName: event.name,
          eventType: 'product_event',
          valueType: event.valueType,
          expands: event.expands
        },
        // 节点配置面板
        configPanel: {
          template: `
            <a-form layout="vertical">
              <a-form-item label="事件名称">
                <a-input v-model="node.config.eventName" disabled />
              </a-form-item>
              <a-form-item label="事件描述">
                <a-textarea v-model="node.config.description" disabled />
              </a-form-item>
              <a-form-item label="触发条件">
                <a-input v-model="node.config.condition" placeholder="输入触发条件表达式" />
              </a-form-item>
            </a-form>
          `
        }
      }))
    },

    // 生成设备属性节点
    generatePropertyNodes(properties) {
      return properties.map(property => ({
        type: `product_property_${property.id}`,
        name: property.name || property.id,
        icon: this.getPropertyIcon(property),
        category: 'product_properties',
        description: property.description || '',
        config: {
          productKey: this.selectedProductKey,
          propertyId: property.id,
          propertyName: property.name,
          propertyType: 'product_property',
          valueType: property.valueType,
          mode: property.mode,
          expands: property.expands
        },
        // 节点配置面板
        configPanel: {
          template: `
            <a-form layout="vertical">
              <a-form-item label="属性名称">
                <a-input v-model="node.config.propertyName" disabled />
              </a-form-item>
              <a-form-item label="数据类型">
                <a-input v-model="node.config.valueType.type" disabled />
              </a-form-item>
              <a-form-item label="读写权限">
                <a-input v-model="node.config.mode" disabled />
              </a-form-item>
              <a-form-item label="条件判断">
                <a-input v-model="node.config.condition" placeholder="输入条件表达式" />
              </a-form-item>
            </a-form>
          `
        }
      }))
    },

    // 生成设备功能节点
    generateFunctionNodes(functions) {
      return functions.map(func => ({
        type: `product_function_${func.id}`,
        name: func.name || func.id,
        icon: this.getFunctionIcon(func),
        category: 'product_functions',
        description: func.description || '',
        config: {
          productKey: this.selectedProductKey,
          functionId: func.id,
          functionName: func.name,
          functionType: 'product_function',
          inputs: func.inputs || [],
          output: func.output || {},
          expands: func.expands
        },
        // 节点配置面板
        configPanel: {
          template: `
            <a-form layout="vertical">
              <a-form-item label="功能名称">
                <a-input v-model="node.config.functionName" disabled />
              </a-form-item>
              <a-form-item label="输入参数" v-if="node.config.inputs.length > 0">
                <div v-for="input in node.config.inputs" :key="input.id">
                  <a-input 
                    v-model="node.config.parameters[input.id]" 
                    :placeholder="input.name"
                    :type="getInputType(input.valueType)" />
                </div>
              </a-form-item>
              <a-form-item label="设备选择">
                <a-select v-model="node.config.deviceId" placeholder="选择设备">
                  <a-select-option v-for="device in devices" :key="device.deviceId" :value="device.deviceId">
                    {{ device.deviceName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-form>
          `
        }
      }))
    },

    // 获取事件图标
    getEventIcon(event) {
      const iconMap = {
        alarm: 'alert',
        warning: 'warning',
        error: 'close-circle',
        info: 'info-circle',
        success: 'check-circle'
      }

      // 从事件名称或ID中提取关键词
      const eventName = (event.name || event.id).toLowerCase()
      for (const [key, icon] of Object.entries(iconMap)) {
        if (eventName.includes(key)) {
          return icon
        }
      }

      return 'bell'
    },

    // 获取属性图标
    getPropertyIcon(property) {
      const iconMap = {
        temperature: 'thermometer',
        humidity: 'cloud',
        pressure: 'dashboard',
        voltage: 'thunderbolt',
        current: 'flash',
        power: 'poweroff',
        status: 'check-circle',
        switch: 'swap'
      }

      const propertyName = (property.name || property.id).toLowerCase()
      for (const [key, icon] of Object.entries(iconMap)) {
        if (propertyName.includes(key)) {
          return icon
        }
      }

      return 'setting'
    },

    // 获取功能图标
    getFunctionIcon(func) {
      const iconMap = {
        control: 'control',
        set: 'setting',
        get: 'eye',
        reset: 'reload',
        restart: 'poweroff',
        update: 'upload'
      }

      const funcName = (func.name || func.id).toLowerCase()
      for (const [key, icon] of Object.entries(iconMap)) {
        if (funcName.includes(key)) {
          return icon
        }
      }

      return 'control'
    },

    // 获取输入类型
    getInputType(valueType) {
      const typeMap = {
        int: 'number',
        float: 'number',
        double: 'number',
        string: 'text',
        boolean: 'checkbox',
        enum: 'select'
      }

      return typeMap[valueType?.type] || 'text'
    },

    // 预览节点
    previewNodes() {
      if (this.generatedNodes.length === 0) {
        this.$message.warning('请先生成节点')
        return
      }

      this.$modal.info({
        title: '生成的节点预览',
        content: h => {
          return h('div', [
            h('p', `共生成 ${this.generatedNodes.length} 个节点：`),
            h('ul', this.generatedNodes.map(node =>
              h('li', `${node.name} (${node.type})`)
            ))
          ])
        }
      })
    },

    // 确认生成
    handleOk() {
      if (this.generatedNodes.length === 0) {
        this.$message.warning('请先生成节点')
        return
      }

      this.$emit('nodes-generated', this.generatedNodes)
      this.$emit('update:visible', false)
      this.$message.success(`成功生成 ${this.generatedNodes.length} 个节点`)
    },

    // 取消
    handleCancel() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style lang="less" scoped>
.product-node-generator {
  .metadata-preview {
    margin-top: 16px;
  }

  .generated-nodes {
    margin-top: 16px;

    .ant-list-item {
      .ant-card {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
}
</style> 