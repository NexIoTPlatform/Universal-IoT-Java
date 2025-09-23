<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-spin :spinning="loading" tip="Loading...">
      <a-divider orientation="left">
        <b>修改产品存储策略</b>
      </a-divider>

      <a-form-model ref="form" class="form">

        <!-- 属性 -->
        <h3>属性:</h3>
        <div v-for="(item, index) in propertiesCache" :key="item.id" class="config-item">
          <a-select v-model="item.id" placeholder="请选择属性" @change="cacheChanged('properties')">
            <a-select-option
              v-for="t in storageConfigurationProArray"
              :key="t.id"
              :value="t.id"
              :disabled="t.disabled"
            >{{ t.name }}
            </a-select-option>
          </a-select>
          <a-icon type="forward" style="margin-left: 10px"/>
          <a-input
            v-model="item.maxStorage"
            placeholder="请输入值"
            style="margin-left: 10px"
            suffix="条"
            type="number"
            oninput="if(value>10000){value=10000;
}if(value<0){value=0;
}value=value.replace(/^[0]+[1-9]*/g,'')"
          ></a-input>
          <div class="btns">
            <a-icon
              @click="addOne('properties')"
              v-show="index === propertiesCache.length - 1"
              type="plus-circle"
              class="add-dec"
            />
            <a-icon
              @click="decs('properties', index)"
              type="minus-circle"
              class="add-dec"
            />
          </div>
        </div>

        <!-- 事件 -->
        <h3>事件:</h3>
        <div v-for="(item, index) in eventsCache" :key="item.id" class="config-item">
          <a-select v-model="item.id" placeholder="请选择事件" @change="cacheChanged('events')">
            <a-select-option
              v-for="t in storageConfigurationEventArray"
              :key="t.id"
              :value="t.id"
              :disabled="t.disabled"
            >{{ t.name }}
            </a-select-option>
          </a-select>
          <a-icon type="forward" style="margin-left: 10px"/>
          <a-input
            v-model="item.maxStorage"
            placeholder="请输入值"
            style="margin-left: 10px"
            suffix="条"
            type="number"
            oninput="if(value>10000){value=10000;
}if(value<0){value=0;
}value=value.replace(/^[0]+[1-9]*/g,'')"
          ></a-input>
          <a-icon
            @click="addOne('events')"
            v-show="index === eventsCache.length - 1"
            type="plus-circle"
            class="add-dec"
          />
          <div class="btns">
            <a-icon
              @click="decs('events', index)"
              type="minus-circle"
              class="add-dec"
            />
          </div>
        </div>
      </a-form-model>
      <br>
      <a-alert message="配置物模型的属性和事件,额外存储历史消息,提供API接口查询" type="warning"
               show-icon/>
      <div class="save-btns">
        <a-space>
          <a-button type="primary" @click="submitForm">
            保存
          </a-button>
          <a-button type="dashed" @click="cancel">
            取消
          </a-button>
        </a-space>
      </div>
    </a-spin>
  </a-drawer>
</template>

<script>
import {getProduct, updateStoreConfig} from '@/api/system/dev/product'

export default {
  name: 'StorageConfigurationForm',
  data() {
    return {
      // 产品id
      productId: undefined,
      // 加载过度显示开关
      loading: false,
      // 表单开关
      open: false,
      // 策略配置数组(属性)
      storageConfigurationProArray: [],
      // 策略配置数组(事件)
      storageConfigurationEventArray: [],
      // 物模型
      metadata: null,
      propertiesCache: [],
      eventsCache: []
    }
  },
  methods: {
    // 关闭
    onClose() {
      this.open = false
      this.form = {}
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.metadata = null
      this.productId = null
      this.storageConfigurationProArray = []
      this.storageConfigurationEventArray = []
      this.propertiesCache = [{id: undefined, maxStorage: undefined}]
      this.eventsCache = [{id: undefined, maxStorage: undefined}]
      // this.form = {}
    },
    addOne(type) {
      if (type === 'properties') {
        this.propertiesCache.push({
          id: undefined,
          maxStorage: undefined
        })
      } else {
        this.eventsCache.push({
          id: undefined,
          maxStorage: undefined
        })
      }
    },
    // 删除
    decs(type, index) {
      const cacheKey = type === 'properties' ? 'propertiesCache' : 'eventsCache'
      if (index === 0 && this[cacheKey].length === 1) {
        this[cacheKey].splice(0, 1, {
          id: undefined,
          maxStorage: undefined
        })
      } else {
        this[cacheKey].splice(index, 1)
      }
      this.handleDisable(type)
    },
    // select 选中项改变
    cacheChanged(type) {
      this.handleDisable(type)
    },
    // 禁用属性下拉框
    handleDisable(type) {
      const cacheKey = type === 'properties' ? 'propertiesCache' : 'eventsCache'
      const arrayKey = type === 'properties' ? 'storageConfigurationProArray'
        : 'storageConfigurationEventArray'
      const hasKey = this[cacheKey].map(t => t.id)
      this[arrayKey].forEach(item => {
        if (hasKey.includes(item.id)) {
          this.$set(item, 'disabled', true)
        } else {
          this.$set(item, 'disabled', false)
        }
      })
    },
    /** 修改按钮操作 */
    handleUpdateById(productId) {
      this.reset()
      this.productId = productId
      this.loading = true
      this.open = true
      getProduct(productId).then(response => {
        try {
          // 解析物模型
          const metadata = this.metadata = JSON.parse(response.data.metadata)
          let storePolicyConfiguration
          if (response.data.storePolicyConfiguration) {
            storePolicyConfiguration = JSON.parse(response.data.storePolicyConfiguration)
          }
          // 解析属性
          if (metadata?.properties) {
            for (let i = 0; i < metadata.properties.length; i++) {
              this.storageConfigurationProArray.push({
                id: metadata.properties[i].id,
                name: metadata.properties[i].name
              })
            }
            if (storePolicyConfiguration?.properties) {
              if (storePolicyConfiguration.properties.length) {
                this.propertiesCache = []
              }
              storePolicyConfiguration.properties.forEach(item => {
                this.propertiesCache.push({
                  id: item.id,
                  maxStorage: item.maxStorage
                })
              })
            }
            this.handleDisable('properties')
          }
          // 解析事件
          if (metadata?.events) {
            for (let i = 0; i < metadata.events.length; i++) {
              this.storageConfigurationEventArray.push({
                id: metadata.events[i].id,
                name: metadata.events[i].name
              })
            }
            if (storePolicyConfiguration?.event) {
              if (storePolicyConfiguration.event.length) {
                this.eventsCache = []
              }
              storePolicyConfiguration.event.forEach(item => {
                this.eventsCache.push({
                  id: item.id,
                  maxStorage: item.maxStorage
                })
              })
            }
            this.handleDisable('events')
          }
        } catch (e) {
          console.log(e)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      const properties = this.propertiesCache.filter(t => t.id && t.maxStorage)
      const event = this.eventsCache.filter(t => t.id && t.maxStorage)

      updateStoreConfig({
        id: this.productId,
        storePolicyConfiguration: JSON.stringify({
          properties,
          event
        })
      }).then(res => {
        this.$message.success('修改成功')
        this.open = false
        this.$emit('ok')
      })
    }
  }
}
</script>

<style lang="less" scoped>
.config-item {
  margin-bottom: 10px;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-dec {
  font-size: 18px;
  cursor: pointer;
}

.btns {
  margin-left: 10px;
  display: flex;
  justify-content: space-between;
  width: 128px;
}

.save-btns {
  margin-top: 40px;
  display: flex;
  justify-content: flex-start;
}
</style>
