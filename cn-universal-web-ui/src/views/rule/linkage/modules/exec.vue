<template>
  <div>
    <a-row :gutter="20">
      <a-col :span="6">
        <a-select v-model="value.trigger" placeholder="请选择执行动作" @change="onChangeTrigger">
          <a-select-option
            v-for="item in taggerTypes"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="6" v-if="isDevice" @click="$refs.device.openModal">
        <a-select v-model="deviceName" placeholder="请选择设备" @change="onChangeDevice">
        </a-select>
      </a-col>
      <a-col :span="6" v-if="isNotice" @click="$refs.noticeTemplate.openModal">
        <a-select v-model="noticeTemplateId" placeholder="请选择通知模板"
                  @change="onChangeNoticeTemplate">
          <a-select-option
            v-for="item in noticeTemplateOptions"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="10" v-if="isNotice && noticeTemplateId">
        <a-input v-model="receivers" placeholder="请输入接收人(手机号或邮箱，用逗号分隔)"
                 @change="onChangeReceivers">
        </a-input>
      </a-col>
      <a-col :span="6" v-if="chooseDevice">
        <a-select v-model="value.type" placeholder="请选择类型" @change="onChangeType">
          <a-select-option
            v-for="item in messageType"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="6" v-if="chooseDevice && isFunction">
        <a-select v-model="modelId" placeholder="物模型功能" @change="onChangeFunction">
          <a-select-option
            v-for="item in models"
            :key="item.id"
            :value="item.id">
            {{ item.name + (item.id) }}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
    <a-row>
      <div class="tagger" v-if="chooseDevice && isFunction&&isData" v-for="(item,index) in execData"
           :key="index">
        <strong>参数{{ index + 1 }}</strong>
        <functions
          ref="functions"
          v-model="execData[index]"
          :index="index"
        />
      </div>
    </a-row>
    <device
      @getDeviceInfo="getDeviceInfo"
      ref="device"/>
    <notice-template
      @getNoticeTemplateInfo="getNoticeTemplateInfo"
      ref="noticeTemplate"/>
  </div>
</template>

<script>
import Device from '@/views/rule/linkage/modules/device'
import {getMeta} from '@/api/system/dev/product'
import Functions from '@/views/rule/linkage/modules/functions'
import {listInstance} from '@/api/system/dev/instance'
import NoticeTemplate from '@/views/rule/linkage/modules/NoticeTemplate'

export default {
  name: 'Exec',
  components: {Device, Functions, NoticeTemplate},
  props: {
    value: {
      type: Object,
      // eslint-disable-next-line vue/require-valid-default-prop
      default: {}
    },
    index: {
      type: Number
    },
    reset: {
      type: Number,
      default: 0
    }
  },

  data() {
    return {
      type: null,
      cron: null,
      taggerTypes: [
        {value: 'device', label: '设备输出'},
        {value: 'notice', label: '通知发送'}
      ],
      messageType: [{value: 'functions', label: '功能调用'}
      ],
      models: [],
      filters: [],
      modelId: undefined,
      meta: {},
      deviceId: undefined,
      deviceName: undefined,
      noticeTemplateId: undefined,
      noticeTemplateName: undefined,
      receivers: undefined,
      config: {},
      isDevice: false,
      isNotice: false,
      chooseDevice: false,
      isFunction: false,
      isData: false,
      // 当前动作方法的参数
      execData: [],
      // 当前选中方法的参数物模型
      metaFunction: [],
      // 特殊编号 表号 电信等
      devNo: undefined,
      noticeTemplateOptions: []
    }
  },
  watch: {
    filters(val) {
      this.$emit('filter-change', this.index, val)
    },
    reset(val) {
      this.models = []
      this.filters = []
      this.modelId = undefined
      this.isTimer = false
      this.isDevice = false
      this.isFunction = false
    },
    modelId(val) {
      this.value.modelId = val
    },
    execData(val) {
      this.value.execData = val
    },
    deviceId(val) {
      this.value.deviceId = val
    },
    deviceName(val) {
      this.value.deviceName = val
    },
    receivers(val) {
      this.value.receivers = val
    }
  },
  mounted: function () {
    if (this.value.trigger) {
      this.isDevice = (this.value.trigger === 'device')
      this.isNotice = (this.value.trigger === 'notice')
    }
    if (this.value.deviceName) {
      this.chooseDevice = true
      this.deviceName = this.value.deviceName
      this.deviceId = this.value.deviceId
      this.selectDevInfo()
    }
    if (this.value.modelId) {
      this.modelId = this.value.modelId
    }
    if (this.value.execData) {
      this.execData = this.value.execData
      this.isData = true
    }
    if (this.value.type) {
      this.getMeta().then((res) => {
        if (res && res.functions) {
          this.models = res.functions
          this.isFunction = (this.value.type === 'functions')
          if (this.execData && this.execData.length > 0) {
            this.execData.forEach(this.initValueType)
          }
        }
      }).catch(error => {
        console.warn('Failed to load meta data for editing:', error)
        this.isFunction = (this.value.type === 'functions')
        
        // 如果是因为缺少 productKey 导致的错误，提供更友好的提示
        if (!this.value.productKey && this.value.deviceId) {
          console.warn(`设备 ${this.value.deviceId} 缺少 productKey，无法加载物模型数据`)
        }
      })
    }
    // 通知模板编辑初始化
    if (this.isNotice && this.value.noticeTemplateId && this.value.noticeTemplateName) {
      this.noticeTemplateId = this.value.noticeTemplateId
      this.noticeTemplateName = this.value.noticeTemplateName
      this.noticeTemplateOptions = [{value: this.noticeTemplateId, label: this.noticeTemplateName}]
    }
    // 接收人编辑初始化
    if (this.isNotice && this.value.receivers) {
      this.receivers = this.value.receivers
    }
  },
  methods: {
    onChangeTrigger(data) {
      this.isDevice = (data === 'device')
      this.isNotice = (data === 'notice')
      this.value.type = undefined
      this.filters = []
      this.modelId = undefined
      this.isFunction = false
      this.deviceId = undefined
      this.deviceName = undefined
      this.noticeTemplateId = undefined
      this.noticeTemplateName = undefined
      this.noticeTemplateOptions = []
      this.receivers = undefined
      this.meta = {}
    },
    onChangeType(data) {
      this.models = (data === 'functions') ? this.meta.functions : []
      this.isFunction = (data === 'functions')
      this.modelId = undefined
    },
    onChangeDevice(data) {
      this.value.type = undefined
      this.modelId = undefined
      this.isFunction = false
      this.isData = false
    },

    onChangeNoticeTemplate(id) {
      const option = this.noticeTemplateOptions.find(item => item.value === id)
      this.noticeTemplateId = id
      this.noticeTemplateName = option ? option.label : ''
      this.value.noticeTemplateId = id
      this.value.noticeTemplateName = this.noticeTemplateName
    },
    onChangeReceivers(value) {
      this.value.receivers = value
    },
    onChangeFunction(data) {
      this.isData = false
      this.execData = []
      this.metaFunction = this.meta.functions.filter((item) => item.id === data)[0].inputs
      this.execData = this.metaFunction
      // 功能有参
      if (this.metaFunction.length !== 0) {
        // 参数是否有表号等设备固定的
        this.execData.map((value) => {
          // eslint-disable-next-line no-unused-expressions
          if (value.id === 'childDeviceId' || value.id === 'deviceId' || value.id === 'meterNo'
            || value.id === 'imei') {
            value.params = this.config[value.id] || this.deviceId
          }
        })
        this.isData = true
      }
    },
    addFilter() {
      this.filters.push({})
    },
    delFilter(index) {
      this.filters.splice(index, 1)
    },
    // 打开已存在的场景联动时 初始化查询下拉框内容
    initValueType(item, index) {
      if (this.models && this.models.length > 0 && this.modelId) {
        const model = this.models.find(o => o.id === this.modelId)
        if (model && model.inputs) {
          const input = model.inputs.find(o => o.id === item.id)
          if (input && input.valueType) {
            item.valueType = input.valueType
          }
        }
      }
    },
    // 保存选择的设备信息
    getDeviceInfo(val) {
      this.value.modelId = undefined
      this.deviceId = val.deviceId
      this.deviceName = val.deviceName
      this.value.productKey = val.productKey

      // 选中设备返回的信息
      if (val.config !== undefined && val.config !== 'undefined') {
        this.config = JSON.parse(val.config)
      }
      this.chooseDevice = true
      this.onChangeDevice()
      this.getMeta()
    },
    // 根据设备id获取物模型
    getMeta() {
      return new Promise(resolve => {
        if (!this.value.productKey) {
          console.warn('productKey is empty, cannot get meta data')
          resolve({})
          return
        }
        getMeta(this.value.productKey).then((res) => {
          if (res && res.msg) {
            this.meta = JSON.parse(res.msg)
            resolve(this.meta)
          }
        }).catch(error => {
          console.error('Failed to get meta data:', error)
          resolve({})
        })
      })
    },
    // 根据设备id查询设备详细信息
    selectDevInfo() {
      return new Promise(resolve => {
        listInstance({deviceId: this.deviceId}).then((res) => {
          if (res && res.code === 0 && res.rows.length > 0 && res.rows[0].configuration) {
            this.config = JSON.parse(res.rows[0].configuration)
            resolve(res)
          }
        })
      })
    },

    // 获取通知模板信息
    getNoticeTemplateInfo(data) {
      this.noticeTemplateId = data.id
      this.noticeTemplateName = data.name
      this.value.noticeTemplateId = data.id
      this.value.noticeTemplateName = data.name
      if (!this.noticeTemplateOptions.find(item => item.value === data.id)) {
        this.noticeTemplateOptions.push({value: data.id, label: data.name})
      }
    }
  }
}
</script>

<style scoped>

.a-row {
  margin-bottom: 10px;
}
</style>
