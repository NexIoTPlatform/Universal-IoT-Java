<template>
  <a-modal
    :visible="open"
    :title="title"
    width="520px"
    :footer="null"
    @cancel="onClose"
    centered
    class="event-modal"
  >
    <a-form-model ref="form" :model="form">
      <a-form-model-item
        label="事件标识"
        prop="id"
        :rules="{
          required: true , message: '请输入事件标识', trigger: 'change'
        }">
        <a-input size="large" v-model="form.id" placeholder="请输入事件标识"/>
      </a-form-model-item>
      <a-form-model-item
        label="事件名称"
        prop="name"
        :rules="{
          required: true , message: '请输入事件名称', trigger: 'change'
        }">
        <a-input size="large" v-model="form.name" placeholder="请输入事件名称"/>
      </a-form-model-item>
      <a-form-model-item
        label="告警等级"
        prop="level"
        :rules="{
          required: false , message: '请选择告警等级', trigger: 'change'
        }">
        <a-radio-group size="large" v-model="form.level">
          <template v-for="(item, index) in alarmLevel">
            <a-radio :value="item.dictValue" :key="index">
              {{ item.dictLabel }}
            </a-radio>
          </template>
        </a-radio-group>
      </a-form-model-item>
      <a-form-model-item
        v-if="false"
        label="数据类型"
        prop="valueType">
        <a-select placeholder="请选择数据类型" size="large" v-model="form.valueType" allow-clear
                  @change="valueChange">
          <a-select-opt-group v-for="(d) in modelValueType" :key="d.id">
            <span slot="label">{{ d.name }}</span>
            <a-select-option v-for="(dd) in d.array" :value="dd.dictLabel"
                             :key="dd.dictLabel+dd.dictValue">
              {{ dd.dictValue }}
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        v-show="showNumberRange"
        label="最小值"
        prop="minRange">
        <a-input-number :precision="precision" size="large" v-model="form.minRange"
                        style="width: 100%"/>
      </a-form-model-item>
      <a-form-model-item
        v-show="showNumberRange"
        label="最大值"
        prop="maxRange">
        <a-input-number :precision="precision" size="large" v-model="form.maxRange"
                        style="width: 100%"/>
      </a-form-model-item>
      <a-form-model-item
        v-show="showPrecision"
        label="小数精度"
        prop="precision">
        <a-input-number
          :precision="precision"
          size="large"
          v-model="form.precision"
          :min="1"
          :max="10"
          style="width: 100%;"/>
      </a-form-model-item>
      <a-form-model-item
        v-show="showLength"
        label="最大长度"
        prop="precision">
        <a-input-number :precision="precision" size="large" v-model="form.length" :min="1"
                        style="width: 100%"/>
      </a-form-model-item>
      <a-form-model-item
        v-show="showEnum"
        label="枚举项"
        prop="precision">
        <div v-for="(item, index) in form.enum" :key="item.id">
          <a-row v-if="form.enum.length === 1" :key="index">
            <a-input placeholder="标识" style="width: 40%;" v-model="item.value"/>
            <a-icon type="forward" style="width: 6%"/>
            <a-input placeholder="对枚举的描述" style="width: 40%" v-model="item.text"/>
            <a-icon type="plus-circle" style="width: 14%;font-size: 18px"
                    @click="addEnum(item, index)"/>
          </a-row>
          <a-row v-else-if="form.enum.length > 1 && form.enum.length-1 !== index" :key="index">
            <a-input placeholder="标识" style="width: 40%;" v-model="item.value"/>
            <a-icon type="forward" style="width: 6%"/>
            <a-input placeholder="对枚举的描述" style="width: 40%" v-model="item.text"/>
            <a-icon type="minus-circle" style="width: 14%;font-size: 18px"
                    @click="reduceEnum(item, index)"/>
          </a-row>
          <a-row v-else-if="form.enum.length > 1 && form.enum.length-1 === index" :key="index">
            <a-input placeholder="标识" style="width: 40%;" v-model="item.value"/>
            <a-icon type="forward" style="width: 6%"/>
            <a-input placeholder="对枚举的描述" style="width: 40%" v-model="item.text"/>
            <a-icon type="minus-circle" style="width: 7%;font-size: 18px"
                    @click="reduceEnum(item, index)"/>
            <a-icon type="plus-circle" style="width: 7%;font-size: 18px"
                    @click="addEnum(item, index)"/>
          </a-row>
        </div>
      </a-form-model-item>
      <a-form-model-item
        v-show="showUnit"
        label="单位"
        prop="unit">
        <a-select placeholder="请选择单位" size="large" v-model="form.unit" allow-clear>
          <a-select-opt-group v-for="(d) in modelUnit" :key="d.id">
            <span slot="label">{{ d.name }}</span>
            <a-select-option v-for="(dd) in d.array" :key="dd.dictLabel+dd.dictValue"
                             :value="dd.dictLabel">
              {{ dd.dictValue }}
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="描述"
        prop="describe">
        <a-textarea
          v-model="form.describe"
          placeholder="事件描述信息"
          :auto-size="{ minRows: 3, maxRows: 5 }"
        />
      </a-form-model-item>
      <div class="bottom-control">
        <a-space>
          <a-button type="primary" @click="submit">
            保存
          </a-button>
          <a-button type="dashed" @click="onClose">
            取消
          </a-button>
        </a-space>
      </div>
    </a-form-model>
  </a-modal>
</template>

<script>
import {metadataAdd, metadataUpdate} from '@/api/system/dev/product'

export default {
  name: 'EventForm',
  props: {
    productId: {
      type: String,
      require: true,
      default: ''
    },
    productKey: {
      type: String,
      require: true,
      default: ''
    },
    show: {
      type: Boolean,
      require: true
    }
  },
  components: {},
  watch: {
    show(val) {
      this.open = val
      if (val === true) {
        this.resetQueryParams()
      }
    }
  },
  data() {
    return {
      // oldId
      oldId: undefined,
      // 弹出层标题
      title: '添加事件',
      // 是否显示弹出层
      open: false,
      // 表单
      form: {
        id: undefined,
        name: undefined,
        valueType: undefined,
        unit: undefined,
        precision: undefined,
        length: undefined,
        level: undefined,
        minRange: undefined,
        maxRange: undefined,
        describe: undefined,
        enum: [{
          value: undefined,
          text: undefined
        }]
      },
      // 物模型数据类型
      modelValueType: [],
      // 物模型数据基本类型
      modelBasicData: [],
      // 物模型数据其他类型
      modelOtherData: [],
      // 物模型单位数据
      modelUnit: [],
      // 物模型单位数据是否显示
      showUnit: false,
      // 物模型小数精度是否显示
      showPrecision: false,
      // 物模型字符串最大长度
      showLength: false,
      // 枚举项是否显示
      showEnum: false,
      // 整数取值范围是否显示
      showNumberRange: false,
      // 小数经度
      precision: 0,
      // 枚举的数量(暂未使用)
      enumSize: 0,
      // 告警类型
      alarmLevel: [{
        dictValue: 'ordinary',
        dictLabel: '普通告警'
      }, {
        dictValue: 'warn',
        dictLabel: '提醒告警'
      }, {
        dictValue: 'urgent',
        dictLabel: '紧急告警'
      }],
      // 提交类型 1：增加；2：修改
      submitType: 1
    }
  },
  created() {
    // 封装数据类型
    this.modelValueType = []
    this.modelUnit = []
  },
  methods: {
    // 关闭抽屉
    onClose(e) {
      this.$emit('ok')
      this.open = false
    },
    // 重置参数
    resetQueryParams() {
      this.$nextTick(() => {
        this.submitType = 1
        this.title = '添加事件'
        this.$refs.form.resetFields()
        this.form = {
          id: undefined,
          name: undefined,
          valueType: undefined,
          unit: undefined,
          precision: undefined,
          length: undefined,
          level: undefined,
          minRange: undefined,
          maxRange: undefined,
          describe: undefined,
          enum: [{
            value: undefined,
            text: undefined
          }]
        }
      })
      this.showUnit = false
      this.showPrecision = false
      this.showLength = false
      this.showEnum = false
      this.showNumberRange = false
    },
    // 参数类型发生变化
    valueChange(value, option) {
      console.log('value event = ', value)
      console.log('option event = ', option)
      this.form.enum = [{
        value: undefined,
        text: undefined
      }]
      if (value !== undefined && value !== null) {
        if (value === 'int' ||
          value === 'long') {
          this.form.minRange = undefined
          this.form.maxRange = undefined
          this.form.precision = undefined
          this.form.length = undefined
          this.showUnit = true
          this.showLength = false
          this.showPrecision = false
          this.showEnum = false
          this.showNumberRange = true
        } else if (value === 'string' ||
          value === 'text') {
          this.form.minRange = undefined
          this.form.maxRange = undefined
          this.form.length = 50
          this.form.precision = undefined
          this.showUnit = true
          this.showLength = true
          this.showPrecision = false
          this.showEnum = false
          this.showNumberRange = false
        } else if (value === 'boolean') {
          this.form.minRange = undefined
          this.form.maxRange = undefined
          this.form.precision = undefined
          this.form.length = undefined
          this.form.unit = undefined
          this.showUnit = false
          this.showLength = false
          this.showPrecision = false
          this.showEnum = false
          this.showNumberRange = false
        } else if (value === 'float' ||
          value === 'double') {
          this.form.minRange = undefined
          this.form.maxRange = undefined
          this.form.precision = undefined
          this.form.length = undefined
          this.showUnit = true
          this.showPrecision = true
          this.showLength = false
          this.showEnum = false
          this.showNumberRange = false
        } else if (value === 'enum') {
          this.form.minRange = undefined
          this.form.maxRange = undefined
          this.form.unit = undefined
          this.form.precision = undefined
          this.form.length = undefined
          this.showUnit = false
          this.showLength = false
          this.showPrecision = false
          this.showEnum = true
          this.showNumberRange = false
        } else {
          this.form.minRange = undefined
          this.form.maxRange = undefined
          this.form.unit = undefined
          this.form.precision = undefined
          this.form.length = undefined
          this.showUnit = false
          this.showLength = false
          this.showPrecision = false
          this.showEnum = false
          this.showNumberRange = false
        }
      } else {
        this.form.enum = [{
          value: undefined,
          text: undefined
        }]
        this.form.minRange = undefined
        this.form.maxRange = undefined
        this.form.unit = undefined
        this.form.precision = undefined
        this.form.length = undefined
        this.showUnit = false
        this.showLength = false
        this.showPrecision = false
        this.showEnum = false
        this.showNumberRange = false
      }
    },
    // 枚举+
    addEnum(item, index) {
      console.log('枚举+ = ', item, index)
      console.log('枚举- = ', this.form)
      this.form.enum.push({
        value: undefined,
        text: undefined
      })
    },
    // 枚举-
    reduceEnum(item, index) {
      console.log('枚举- = ', item, index)
      console.log('枚举- = ', this.form)
      this.form.enum.splice(index, 1)
    },
    // 提交
    submit() {
      console.log('form = ', this.form)
      this.$refs.form.validate(valid => {
        if (valid) {
          const data = {
            // key
            productKey: this.productKey,
            // 属性、事件、功能类型，三选一
            type: 'events',
            // 用来判断删除或者修改那个属性
            oldId: this.oldId,
            ioTProductMetadataBO: {
              id: this.form.id,
              name: this.form.name,
              expands: {
                level: this.form.level
              },
              valueType: {
                // 数据类型，共有
                type: 'string',
                // 功能、属性的单位
                expands: {
                  maxLength: '50'
                }
              },
              description: this.form.describe
            }
          }
          console.log('data form = ', data)
          if (this.submitType === 1) {
            metadataAdd(data).then(res => {
              console.log('res = ', res)
            }).finally(() => {
              this.$emit('ok')
              this.oldId = undefined
              this.open = false
            })
          } else if (this.submitType === 2) {
            metadataUpdate(data).then(res => {
              console.log('res = ', res)
            }).finally(() => {
              this.$emit('ok')
              this.oldId = undefined
              this.open = false
            })
          }
        } else {
          return false
        }
      })
    },
    // 修改事件
    editEvents(data) {
      console.log('data = ', data)
      this.submitType = 2
      this.title = '修改事件'
      const value = data.type
      this.oldId = data.id
      this.form = {
        id: data.id,
        name: data.name,
        valueType: undefined,
        unit: undefined,
        precision: undefined,
        length: undefined,
        level: data.level,
        minRange: undefined,
        maxRange: undefined,
        describe: data.description,
        enum: [{value: undefined, text: undefined}]
      }
      if (value === 'int' ||
        value === 'long') {
        this.showUnit = true
        this.showLength = false
        this.showPrecision = false
        this.showEnum = false
        this.showNumberRange = true
      } else if (value === 'string' ||
        value === 'text') {
        this.showUnit = true
        this.showLength = true
        this.showPrecision = false
        this.showEnum = false
        this.showNumberRange = false
      } else if (value === 'boolean') {
        this.showUnit = false
        this.showLength = false
        this.showPrecision = false
        this.showEnum = false
        this.showNumberRange = false
      } else if (value === 'float' ||
        value === 'double') {
        this.showUnit = true
        this.showPrecision = true
        this.showLength = false
        this.showEnum = false
        this.showNumberRange = false
      } else if (value === 'enum') {
        this.showUnit = false
        this.showLength = false
        this.showPrecision = false
        this.showEnum = true
        this.showNumberRange = false
      } else {
        this.showUnit = false
        this.showLength = false
        this.showPrecision = false
        this.showEnum = false
        this.showNumberRange = false
      }
      this.open = true
    }
  }

}
</script>
<style scoped>
.event-modal >>> .ant-modal-content {
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  box-shadow: 0 4px 24px 0 rgba(25, 102, 255, 0.10);
  padding: 0;
}

.event-modal >>> .ant-modal-header {
  border-radius: 14px 14px 0 0;
  background: #f8fafc;
  font-size: 18px;
  font-weight: bold;
  padding: 24px 32px 12px 32px;
}

.event-modal >>> .ant-modal-title {
  font-size: 18px;
  font-weight: bold;
}

.event-modal >>> .ant-modal-body {
  flex: 1 1 auto;
  padding: 0 32px 0 32px;
  max-height: 60vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 0 0 14px 14px;
  margin-bottom: 0;
  scrollbar-width: thin;
  scrollbar-color: #d1d5db #f3f4f6;
}

.event-modal >>> .ant-modal-body::-webkit-scrollbar {
  width: 6px;
  background: #f3f4f6;
  border-radius: 6px;
}

.event-modal >>> .ant-modal-body::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 6px;
}

.bottom-control {
  flex-shrink: 0;
  background: #fff;
  padding: 18px 32px 24px 32px;
  border-top: 1px solid #f1f5f9;
  margin-top: 0;
  text-align: center;
  position: sticky;
  bottom: 0;
  z-index: 2;
}
</style>
