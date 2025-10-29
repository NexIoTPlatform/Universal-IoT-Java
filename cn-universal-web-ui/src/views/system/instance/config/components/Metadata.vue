<template>
  <a-drawer
    title="批量配置"
    :width="720"
    :visible="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="close"
  >

    <a-form-model :model="form" ref="form">

      <a-form-model-item
        label="功能指令"
        prop="function"
        :rules="[{ required: true, message: '请选择功能指令', blur: 'trigger' }]"
      >
        <a-select placeholder="请选择下发的指令" size="large" v-model="form.function" allow-clear
                  @change="optionChange">
          <a-select-option v-for="(item, index) in functions" :key="index" :value="item.id">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-model-item>

      <a-form-model-item
        v-for="item in inputs"
        :key="item.id"
        :prop="item.id"
        :rules="(() => validate(item))()"
      >
        <span slot="label">
          {{ item.name }}
          <a-tooltip class="tip" v-if="!!item.description">
            <template slot="title">
              {{ item.description }}
            </template>
            <a-icon type="question"/>
          </a-tooltip>
        </span>

        <template v-if="item.valueType.type === 'string'">
          <a-input
            placeholder="请输入"
            v-model="form[item.id]"
            :suffix="item.valueType.unit"
          >
            <span slot="suffix" v-if="!!item.valueType.unit">{{ item.valueType.unit }}</span>
          </a-input>
        </template>

        <template v-else-if="item.valueType.type === 'int'">
          <a-input
            v-model.number="form[item.id]"
            :suffix="item.valueType.unit"
            placeholder="请输入"
          />
        </template>

        <template v-else-if="item.valueType.type === 'enum'">
          <a-select v-model="form[item.id]" placeholder="请选择" allow-clear>
            <a-select-option
              v-for="t in item.valueType.elements"
              :key="t.value"
              :value="t.value"
            >
              {{ t.text }}
            </a-select-option>
          </a-select>
        </template>

      </a-form-model-item>

      <a-form-model-item v-if="detail.thirdPlatform === 'ctwing'" label="服务类型(serviceType)"
                         prop="serviceType">
        <a-input placeholder="服务类型(电信设备附带，非必填项)" v-model="form.serviceType"/>
      </a-form-model-item>

      <a-button type="primary" @click="functionDown">
        立即发送
      </a-button>

    </a-form-model>

  </a-drawer>
</template>

<script>
import {getProByKey} from '@/api/system/dev/product'
import {batchConfigTask} from '@/api/system/dev/batch'

export default {
  props: {
    queryParam: {
      type: Object,
      default: () => ({})
    },
    selectedRowKeys: {
      type: Array,
      default: () => ([])
    },
    unSelectRowKeys: {
      type: Array,
      default: () => ([])
    },
    productList: {
      type: Array,
      default: () => ([])
    },
    chooseAll: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: false,
      detail: {
        thirdPlatform: ''
      },
      metadata: {},
      functions: [],
      funcName: '',
      inputs: [],
      extParam: [],
      form: {
        function: undefined,
        serviceType: undefined
      }
    }
  },
  methods: {
    validate(item) {
      if (item.valueType.type === 'string') {
        const rule = [{required: true, message: `请输入${item.name}`, trigger: 'blur'}]
        if (item.valueType.expands) {
          const max = Number(item.valueType.expands.maxLength)
          rule.push({min: 0, max, message: `最大长度为${max}`})
        }
        return rule
      } else if (item.valueType.type === 'int') {
        return [
          {required: true, message: `请输入${item.name}`, trigger: 'blur'},
          {
            type: 'integer',
            min: item.valueType.min,
            max: item.valueType.max,
            message: `请输入${item.valueType.min}~${item.valueType.max}之间整数`,
            trigger: 'blur'
          }
        ]
      } else if (item.valueType.type === 'enum') {
        return [{required: true, message: `请选择${item.name}`, trigger: 'change'}]
      }
    },
    async open() {
      this.init()
      await this.getFunction()
      if (!this.functions.length) {
        this.$notification.error({
          message: '错误',
          description: '当前产品无可下发功能，请选择其它产品！',
          duration: 3
        })
        return
      }
      this.visible = true
    },
    close() {
      this.visible = false
    },
    init() {
      this.metadata = {}
      this.functions = []
      this.extParam = []
      this.detail = {
        thirdPlatform: ''
      }
      this.form = {
        function: undefined,
        serviceType: undefined
      }
    },
    getFunction() {
      return getProByKey(this.queryParam.productKey).then(res => {
        try {
          this.detail = res.data
          this.metadata = JSON.parse(res.data.metadata)
          this.functions = this.metadata.functions
        } catch (error) {
          console.log(error)
        }
      })
    },
    optionChange(value) {
      const {functions} = this
      const funcItem = functions.find(item => item.id === value)
      if (!funcItem) {
        return
      }

      // 指令名称
      this.funcName = funcItem.name

      this.inputs = []
      const extParam = ['childDeviceId', 'deviceId', 'deviceCode', 'meterNo', 'imei']
      this.extParam = []
      funcItem.inputs.forEach(item => {
        if (extParam.indexOf(item.id) !== -1) {
          this.extParam.push(item.id)
        } else {
          this.inputs.push(item)
        }
      })
    },
    functionDown() {
      const productData = this.productList.find(t => t.productKey === this.queryParam.productKey)
      let taskName
      if (productData) {
        taskName = productData.name + this.funcName
      }
      const data = {
        taskName,
        command: this.form.function,
        commandData: JSON.stringify({
          function: this.form.function,
          messageType: 'FUNCTIONS',
          serviceType: this.form.serviceType,
          data: this.form
        }),
        extParam: this.extParam,
        applicationId: this.queryParam.application,
        productKey: this.queryParam.productKey,
        chooseAll: this.chooseAll,
        ids: this.selectedRowKeys,
        removeIds: this.unSelectRowKeys
      }
      this.$refs.form.validate(valid => {
        if (valid) {
          batchConfigTask(data).then(() => {
            this.$message.success(
              '下发批量任务成功!',
              3
            )
            this.close()
          })
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
.tip {
  background-color: #efcbc4;
  padding: 2px;
  border-radius: 50%;
  font-size: 14px;
}
</style>
