<template>
  <a-modal
    :visible="open"
    :title="title"
    width="600px"
    :footer="null"
    @cancel="onClose"
    :maskClosable="false"
    centered
  >
    <a-divider orientation="left">
      <b>{{ title }}</b>
    </a-divider>
    <a-form-model ref="form" :model="form">
      <a-form-model-item
        label="属性标识"
        prop="id"
        :rules="[
          { required: true , message: '请输入属性标识', trigger: 'change' },
          { pattern: /^[a-zA-Z].*/, message: '字母开头', trigger: 'blur' }]">
        <a-input size="large" v-model="form.id" placeholder="请输入属性标识"/>
      </a-form-model-item>
      <a-form-model-item
        label="属性名称"
        prop="name"
        :rules="{
          required: true , message: '请输入属性名称', trigger: 'change'
        }">
        <a-input size="large" v-model="form.name" placeholder="请输入属性名称"/>
      </a-form-model-item>
      <a-form-model-item
        label="数据类型"
        prop="valueType"
        :rules="{
          required: true , message: '请选择数据类型', trigger: 'change'
        }">
        <a-select placeholder="请选择数据类型" size="large" v-model="form.valueType" allow-clear
                  @change="valueChange">
          <a-select-opt-group v-for="(d) in modelValueType" :key="d.id">
            <span slot="label">{{ d.name }}</span>
            <a-select-option v-for="(dd) in d.array" :key="dd.dictLabel+dd.dictValue"
                             :value="dd.dictLabel">
              {{ dd.dictValue }}
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </a-form-model-item>
      <!--控制显示：v-show="showNumberRange"-->
      <!--属性中去除数据类型限制的输入框：v-if="false"-->
      <a-form-model-item
        v-if="false"
        label="最小值"
        prop="minRange">
        <a-input-number :precision="precision" size="large" v-model="form.minRange"
                        style="width: 100%"/>
      </a-form-model-item>
      <!--控制显示：v-show="showNumberRange"-->
      <!--属性中去除数据类型限制的输入框：v-if="false"-->
      <a-form-model-item
        v-if="false"
        label="最大值"
        prop="maxRange">
        <a-input-number :precision="precision" size="large" v-model="form.maxRange"
                        style="width: 100%"/>
      </a-form-model-item>
      <!--控制显示：v-show="showPrecision"-->
      <!--属性中去除数据类型限制的输入框：v-if="false"-->
      <a-form-model-item
        v-if="false"
        label="小数精度"
        prop="precision">
        <a-input-number
          :precision="precision"
          size="large"
          v-model="form.precision"
          :min="1"
          :max="10"
          style="width: 100%"/>
      </a-form-model-item>
      <!--控制显示：v-show="showLength"-->
      <!--属性中去除数据类型限制的输入框：v-if="false"-->
      <a-form-model-item
        v-if="false"
        label="最大长度"
        prop="precision">
        <a-input-number :precision="precision" size="large" v-model="form.length" :min="1"
                        style="width: 100%"/>
      </a-form-model-item>
      <a-form-model-item
        v-show="showEnum"
        label="枚举项"
        prop="precision">
        <template v-for="(item, index) in form.enum">
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
        </template>
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
        label="读写权限"
        prop="onlyRead"
        :rules="{
          required: true , message: '请选择读写权限', trigger: 'change'
        }">
        <a-radio-group size="large" v-model="form.onlyRead">
          <a-radio value="r">
            只读
          </a-radio>
          <a-radio value="rw">
            读写
          </a-radio>
        </a-radio-group>
      </a-form-model-item>
      <a-form-model-item
        label="属性来源"
        prop="source">
        <a-select
          v-model="form.source"
          placeholder="请选择属性来源">
          <a-select-option value="device">设备</a-select-option>
          <a-select-option value="extended">扩展</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="描述"
        prop="describe">
        <a-textarea
          v-model="form.describe"
          placeholder="属性描述信息"
          :auto-size="{ minRows: 3, maxRows: 5 }"
        />
      </a-form-model-item>
    </a-form-model>
    <div class="bottom-control" style="text-align: right; margin-top: 24px;">
      <a-space>
        <a-button type="primary" @click="submit">
          保存
        </a-button>
        <a-button type="dashed" @click="onClose">{{ $t('button.cancel') }}
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script>
import {metadataAdd, metadataUpdate} from '@/api/system/dev/product'

export default {
  name: 'PropertiesForm',
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
      // source
      source: undefined,
      // 弹出层标题
      title: '添加属性',
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
        minRange: undefined,
        maxRange: undefined,
        onlyRead: undefined,
        describe: undefined,
        source: 'device',
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
      // 提交类型 1：增加；2：修改
      submitType: 1
    }
  },
  created() {
    // 封装数据类型
    this.modelValueType = []
    this.modelUnit = []

    // 获取物模型数据
    var dictArray = []
    dictArray.push('model_basic_data')
    dictArray.push('model_other_data')
    dictArray.push('model_unit')
    this.getDictMap(dictArray).then(res => {
      this.modelBasicData = res.data['model_basic_data']
      this.modelValueType.push({
        id: 'model_basic_data',
        name: '基本类型',
        array: this.modelBasicData
      })
      this.modelOtherData = res.data['model_other_data']
      this.modelValueType.push({
        id: 'model_other_data',
        name: '其他类型',
        array: this.modelOtherData
      })
      this.modelUnit = res.data['model_unit']
      console.log('dictArray = ', res.data)
    })
    console.log('this.modelValueType = ', this.modelValueType)
    console.log('this.modelUnit = ', this.modelUnit)
  },
  methods: {
    // 关闭抽屉
    onClose(e) {
      this.$emit('ok')
      this.open = false
    },
    // 重置参数
    resetQueryParams() {
      this.source = undefined
      this.title = '添加属性'
      this.submitType = 1
      this.$nextTick(() => {
        this.$refs.form.resetFields()
        this.form = {
          id: undefined,
          name: undefined,
          valueType: undefined,
          unit: undefined,
          precision: undefined,
          length: undefined,
          minRange: undefined,
          maxRange: undefined,
          onlyRead: undefined,
          describe: undefined,
          source: 'device',
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
          console.log('enum = ', this.form.enum)
          if (this.form.enum !== undefined) {
            for (let i = this.form.enum.length - 1; i >= 0; i--) {
              if (this.form.enum[i].value === undefined || this.form.enum[i].text === undefined
                || this.form.enum[i].value === '' || this.form.enum[i].text === '') {
                this.form.enum.splice(i, 1)
              }
            }
          }
          console.log('enum = ', this.form.enum)
          const data = {
            // key
            productKey: this.productKey,
            // 属性、事件、功能类型，三选一
            type: 'properties',
            // 用来判断删除或者修改那个属性
            oldId: this.oldId,
            ioTProductMetadataBO: {
              id: this.form.id,
              name: this.form.name,
              source: this.form.source,
              valueType: {
                // 数据类型，共有
                type: this.form.valueType,
                // 功能、属性的单位
                unit: this.form.unit,
                // 枚举
                elements: this.form.enum.length === 0 ? undefined : this.form.enum
              },
              mode: this.form.onlyRead,
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
    // 修改属性
    editProperties(data) {
      console.log('data = ', data)
      this.submitType = 2
      this.title = '修改属性'
      const value = data.type
      this.oldId = data.id
      this.source = data.source
      this.form = {
        id: data.id,
        name: data.name,
        valueType: data.type,
        unit: data.unit,
        precision: undefined,
        length: undefined,
        minRange: undefined,
        maxRange: undefined,
        onlyRead: data.mode,
        describe: data.description,
        source: data.source || 'device',
        enum: data.type === 'enum' ? data.elements !== undefined ? JSON.parse(data.elements)
          : [{value: undefined, text: undefined}] : [{value: undefined, text: undefined}]
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

</style>
