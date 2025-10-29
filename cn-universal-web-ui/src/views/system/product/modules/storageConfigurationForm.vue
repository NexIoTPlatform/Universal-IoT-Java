<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-spin :spinning="loading" tip="Loading...">
      <a-divider orientation="left">
        <b>{{ formTitle }}</b>
      </a-divider>
      <a-form-model ref="form" :model="form" :rules="rules">
        <a-alert
          message="灵活配置物模型的属性和事件。依据已配置策略，额外单独存储历史消息，提供API接口查询"
          type="info"/>
        <div>
          <a-button type="primary" @click="proStorageConfig" style="margin-bottom: 20px">
            {{ proStorageConfigTitle }}
          </a-button>
        </div>

        <!-- 遍历属性 -->
        <template v-for="(item, index) in storageConfigurationProArray">

          <a-form-model-item
            v-show="proStorageConfigShow"
            :key="index+`Pro`"
            :prop="item.id"
            style="width: 100%"
            :rules="[
              {min: 1 , max: 1000, type: 'number', message: '参数必须为整数，1-1000之间'}
            ]">
            <span slot="label">
              {{ item.name + `(${item.idValue})` }}
            </span>
            <a-input placeholder="请输入存储数量" suffix="条" v-model.number="form[item.id]"/>
          </a-form-model-item>

        </template>

        <div>
          <a-button type="primary" @click="eventStorageConfig" style="margin-bottom: 20px">
            {{ eventStorageConfigTitle }}
          </a-button>
        </div>

        <!-- 遍历事件 -->
        <template v-for="(item, index) in storageConfigurationEventArray">

          <a-form-model-item
            v-show="eventStorageConfigShow"
            :key="index+`Event`"
            :prop="item.id"
            style="width: 100%"
            :rules="[
              {min: 1 , max: 1000, type: 'number', message: '参数必须为整数，1-1000之间'}
            ]">
            <span slot="label">
              {{ item.name + `(${item.idValue})` }}
            </span>
            <a-input placeholder="请输入存储数量" suffix="条" v-model.number="form[item.id]"/>
          </a-form-model-item>

        </template>

        <div>
          <a-space>
            <a-button type="primary" @click="submitForm">
              保存
            </a-button>
            <a-button type="dashed" @click="cancel">
              取消
            </a-button>
          </a-space>
        </div>
      </a-form-model>
    </a-spin>
  </a-drawer>
</template>

<script>
import {getProduct, updateStoreConfig} from '@/api/system/dev/product'

export default {
  name: 'StorageConfigurationForm',
  props: {},
  components: {},
  data() {
    return {
      // 产品id
      productId: undefined,
      // 加载过度显示开关
      loading: false,
      // 标题
      formTitle: '修改产品存储策略',
      // 表单参数
      form: {},
      // 规则
      rules: {},
      // 表单开关
      open: false,
      // 策略配置数组标题(属性)
      proStorageConfigTitle: '属性展开',
      // 策略配置数组标题(事件)
      eventStorageConfigTitle: '事件展开',
      // 是否展示策略配置数组(属性)
      proStorageConfigShow: false,
      // 是否展示策略配置数组(事件)
      eventStorageConfigShow: false,
      // 策略配置数组(属性)
      storageConfigurationProArray: [],
      // 策略配置数组(事件)
      storageConfigurationEventArray: [],
      // 物模型
      metadata: null
    }
  },
  filters: {},
  created() {
  },
  computed: {},
  watch: {},
  mounted() {
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
      this.proStorageConfigTitle = '属性展开'
      this.eventStorageConfigTitle = '事件展开'
      this.proStorageConfigShow = false
      this.eventStorageConfigShow = false
      this.storageConfigurationProArray = []
      this.storageConfigurationEventArray = []
      this.form = {}
    },
    /** 修改按钮操作 */
    handleUpdateById(productId) {
      this.reset()
      this.productId = productId
      console.log('productId = ', productId)
      this.loading = true
      this.open = true
      getProduct(productId).then(response => {
        console.log('response = ', response)
        try {
          // 解析物模型
          const metadata = JSON.parse(response.data.metadata)
          let storePolicyConfiguration
          if (response.data.storePolicyConfiguration) {
            storePolicyConfiguration = JSON.parse(response.data.storePolicyConfiguration)
          }
          this.metadata = metadata
          if (metadata) {
            // 解析属性
            if (metadata.properties) {
              for (let i = 0; i < metadata.properties.length; i++) {
                const pro = {}
                pro['idValue'] = metadata.properties[i].id
                pro['id'] = metadata.properties[i].id + 'Pro'
                pro['name'] = metadata.properties[i].name
                // pro[`${metadata.properties[i].id}Count`] = null
                this.storageConfigurationProArray.push(pro)
                console.log('storePolicyConfiguration = ', storePolicyConfiguration)
                if (storePolicyConfiguration) {
                  if (storePolicyConfiguration.properties) {
                    for (let j = 0; j < storePolicyConfiguration.properties.length; j++) {
                      if (storePolicyConfiguration.properties[j].id === metadata.properties[i].id) {
                        this.$set(this.form, metadata.properties[i].id + 'Pro',
                          storePolicyConfiguration.properties[j].maxStorage)
                      }
                    }
                  }
                }
              }
            }
            // 解析事件
            if (metadata.events) {
              for (let i = 0; i < metadata.events.length; i++) {
                const pro = {}
                pro['idValue'] = metadata.events[i].id
                pro['id'] = metadata.events[i].id + 'Event'
                pro['name'] = metadata.events[i].name
                // pro[`${metadata.events[i].id}Count`] = null
                this.storageConfigurationEventArray.push(pro)
                if (storePolicyConfiguration) {
                  if (storePolicyConfiguration.event) {
                    for (let j = 0; j < storePolicyConfiguration.event.length; j++) {
                      if (storePolicyConfiguration.event[j].id === metadata.events[i].id) {
                        this.$set(this.form, metadata.events[i].id + 'Event',
                          storePolicyConfiguration.event[j].maxStorage)
                      }
                    }
                  }
                }
              }
            }
          }
        } catch (e) {

        }
        console.log('response mete = ', JSON.parse(response.data.metadata))
        console.log('this.storageConfigurationEventArray = ', this.storageConfigurationEventArray)
        console.log('this.storageConfigurationProArray = ', this.storageConfigurationProArray)
      }).finally(() => {
        this.loading = false
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      console.log('form', this.form)
      console.log('formStr', JSON.stringify(this.form))
      console.log('id', this.productId)
      this.$refs.form.validate(valid => {
        if (valid) {
          const storeProForm = []
          const storeEventForm = []
          if (this.metadata != null) {
            // 解析属性
            if (this.metadata.properties) {
              for (let i = 0; i < this.metadata.properties.length; i++) {
                if (this.form[this.metadata.properties[i].id + 'Pro'] !== undefined &&
                  this.form[this.metadata.properties[i].id + 'Pro'] !== null &&
                  this.form[this.metadata.properties[i].id + 'Pro'] !== '') {
                  const pro = {}
                  pro['id'] = this.metadata.properties[i].id
                  pro['maxStorage'] = Number.parseInt(
                    this.form[this.metadata.properties[i].id + 'Pro'])
                  storeProForm.push(pro)
                }
              }
            }
            // 解析事件
            if (this.metadata.events) {
              for (let i = 0; i < this.metadata.events.length; i++) {
                if (this.form[this.metadata.events[i].id + 'Event'] !== undefined &&
                  this.form[this.metadata.events[i].id + 'Event'] !== null &&
                  this.form[this.metadata.events[i].id + 'Event'] !== '') {
                  const pro = {}
                  pro['id'] = this.metadata.events[i].id
                  pro['maxStorage'] = Number.parseInt(
                    this.form[this.metadata.events[i].id + 'Event'])
                  storeEventForm.push(pro)
                }
              }
            }

            const storeForm = {
              'properties': storeProForm,
              'event': storeEventForm
            }

            console.log('storeForm = ', storeForm)
            console.log('productId = ', this.productId)
            if (this.productId !== undefined && this.productId !== null) {
              updateStoreConfig({
                id: this.productId,
                storePolicyConfiguration: JSON.stringify(storeForm)
              }).then(response => {
                this.$message.success(
                  '修改成功',
                  3
                )
                this.open = false
                this.$emit('ok')
              })
            }
          }
        } else {
          return false
        }
      })
    },
    // 属性展开
    proStorageConfig() {
      if (this.proStorageConfigShow) {
        this.proStorageConfigTitle = '属性展开'
        this.proStorageConfigShow = false
      } else {
        this.proStorageConfigTitle = '属性隐藏'
        this.proStorageConfigShow = true
      }
    },
    // 事件展开
    eventStorageConfig() {
      if (this.eventStorageConfigShow) {
        this.eventStorageConfigTitle = '事件展开'
        this.eventStorageConfigShow = false
      } else {
        this.eventStorageConfigTitle = '事件隐藏'
        this.eventStorageConfigShow = true
      }
    }
  }
}
</script>
