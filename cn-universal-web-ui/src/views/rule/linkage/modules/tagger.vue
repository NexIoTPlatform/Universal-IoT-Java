<template>
  <div>
    <a-row :gutter="20">
      <a-col :span="6">
        <a-select v-model="value.trigger" placeholder="请选择触发器类型" @change="onChangeTrigger">
          <a-select-option
            v-for="item in taggerTypes"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="8" v-if="isTimer">
        <a-input v-model="cron" placeholder="cron表达式"/>
      </a-col>
      <a-col :span="6" v-if="isDevice" @click="$refs.device.openModal">
        <a-select v-model="value.deviceName" placeholder="请选择设备" @change="onChangeDevice">
        </a-select>
      </a-col>
      <a-col :span="6" v-if="chooseDevice">
        <a-select v-model="value.type" placeholder="请选择类型，如：属性/事件" @change="onChangeType">
          <a-select-option
            v-for="item in messageType"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="6" v-if="chooseDevice && isEvent">
        <a-select v-model="modelId" placeholder="物模型事件">
          <a-select-option
            v-for="item in models"
            :key="item.id"
            :value="item.id">
            {{ item.name + (item.id) }}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
    <div v-if="chooseDevice && isProperties">
      <a-row :gutter="20" v-for="(item,index) in filters" :key="index">
        <a-col :span="6">
          <a-select v-model="filters[index].key" placeholder="物模型属性">
            <a-select-option
              v-for="item in models"
              :key="item.id"
              :value="item.id">
              {{ item.name + (item.id) }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-select v-model="filters[index].operator" placeholder="操作符">
            <a-select-option
              v-for="item in operators"
              :key="item.value"
              :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="8">
          <a-input v-model="filters[index].value" placeholder="过滤条件值"/>
        </a-col>
        <a-button type="text" @click="delFilter(index)">删除</a-button>
      </a-row>
      <a-row :gutter="20">
        <a-button type="text" @click="addFilter()">+ 添加</a-button>
      </a-row>
    </div>
    <device
      @getDeviceInfo="getDeviceInfo"
      ref="device"/>
  </div>
</template>

<script>
import Device from '@/views/rule/linkage/modules/device'
import {getMeta} from '@/api/system/dev/product'

export default {
  name: 'Tagger',
  components: {Device},
  props: {
    value: {
      type: Object,
      // eslint-disable-next-line vue/require-valid-default-prop
      default: {}
    },
    index: {
      type: Number
    },
    touch: {
      type: String
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
      taggerTypes: [{value: 'device', label: '设备触发'},
        {value: 'timer', label: '定时触发'},
        {value: 'manual', label: '手动触发'}
      ],
      messageType: [{value: 'properties', label: '属性'},
        {value: 'event', label: '事件'}
      ],
      operators: [{value: 'eq', label: '='},
        {value: 'not', label: '!='},
        {value: 'gt', label: '>'},
        {value: 'lt', label: '<'},
        {value: 'gte', label: '>='},
        {value: 'lte', label: '<='},
        {value: 'like', label: 'like'}
      ],
      models: [],
      filters: [],
      isTimer: false,
      isDevice: false,
      modelId: undefined,
      meta: {},
      chooseDevice: false,
      isProperties: false,
      isEvent: false
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
      this.chooseDevice = false
      this.isProperties = false
      this.isEvent = false
    },
    touch(val) {
      if (val && val !== '') {
        if (val === 'all') {
          this.taggerTypes = [{value: 'device', label: '设备触发'}]
        } else {
          this.taggerTypes = [{value: 'device', label: '设备触发'},
            {value: 'timer', label: '定时触发'},
            {value: 'manual', label: '手动触发'}]
        }
      }
    },
    cron(val) {
      this.value.cron = val
    },
    modelId(val) {
      this.value.modelId = val
    }
  },
  mounted: function () {
    if (this.value.trigger) {
      this.isTimer = (this.value.trigger === 'timer')
      this.isDevice = (this.value.trigger === 'device')
    }
    if (this.value.deviceName) {
      this.chooseDevice = true
    }
    if (this.value.type) {
      this.getMeta().then(res => {
        this.models = (this.value.type === 'event') ? res.events : res.properties
        this.isProperties = (this.value.type === 'properties')
        this.isEvent = (this.value.type === 'event')
      })
    }
    if (this.value.filters) {
      this.filters = this.value.filters
    }
    if (this.value.cron) {
      this.cron = this.value.cron
    }
    if (this.value.modelId) {
      this.modelId = this.value.modelId
    }
    if (this.touch === 'all') {
      this.taggerTypes = [{value: 'device', label: '设备触发'}]
    }
  },

  methods: {
    onChangeTrigger(data) {
      this.isTimer = (data === 'timer')
      this.isDevice = (data === 'device')
      this.value.type = undefined
      this.cron = null
      this.filters = []
      this.modelId = undefined
      this.isProperties = false
      this.isEvent = false
      this.value.deviceId = undefined
      this.value.deviceName = undefined
      this.meta = {}
    },
    onChangeType(data) {
      this.getMeta().then(res => {
        this.models = (data === 'event') ? res.events : res.properties
        this.isProperties = (data === 'properties')

        this.isEvent = (data === 'event')
        this.filters = this.isProperties ? [{}] : []
        this.modelId = undefined
      })
    },
    onChangeDevice(data) {
      this.value.type = undefined
      this.modelId = undefined
      this.isProperties = false
      this.isEvent = false
      this.filters = []
    },
    addFilter() {
      this.filters.push({})
    },
    delFilter(index) {
      this.filters.splice(index, 1)
    },
    getDeviceInfo(val) {
      this.value.deviceId = val.deviceId
      this.value.deviceName = val.deviceName
      this.chooseDevice = true
      this.onChangeDevice()
      this.getMeta()
    },
    getMeta() {
      return new Promise(resolve => {
        getMeta(this.value.deviceId).then((res) => {
          this.meta = JSON.parse(res.msg)
          resolve(this.meta)
        })
      })
    }
  }
}
</script>

<style scoped>

.a-row {
  margin-bottom: 10px;
}
</style>
