<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>{{ title }}</b>
    </a-divider>
    <a-form-model-item
      label="参数标识"
      prop="id"
      :rules="{
        required: true , message: '请输入参数标识', trigger: 'change'
      }">
      <a-input size="large" v-model="form.id" placeholder="请输入参数标识，英文"/>
    </a-form-model-item>
    <a-form-model ref="form" :model="form">
      <a-form-model-item
        label="参数名称"
        prop="name"
        :rules="{
          required: true , message: '请输入参数名称', trigger: 'change'
        }">
        <a-input size="large" v-model="form.name" placeholder="请输入参数名称，中文"/>
      </a-form-model-item>

      <a-form-model-item
        label="参数类型"
        prop="valueType"
        :rules="{
          required: true , message: '请选择参数类型', trigger: 'change'
        }">
        <a-select placeholder="请选择参数类型" size="large" v-model="form.valueType" allow-clear
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
          style="width: 100%"/>
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
            <a-select-option v-for="(dd, index) in d.array" :key="index" :value="dd.dictLabel">
              {{ dd.dictValue }}
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="描述"
        prop="description">
        <a-textarea
          v-model="form.description"
          placeholder="参数描述信息"
          :auto-size="{ minRows: 3, maxRows: 5 }"
        />
      </a-form-model-item>
      <div class="bottom-control">
        <a-space>
          <!--@click="submitForm"-->
          <a-button type="primary" @click="submit">
            保存
          </a-button>
          <!--@click="cancel"-->
          <a-button type="dashed" @click="onClose">
            取消
          </a-button>
        </a-space>
      </div>
    </a-form-model>
  </a-drawer>
</template>

<script>
// import { listDeviceLog } from '@/api/system/dev/deviceLog'

export default {
  name: 'FunctionParamsForm',
  props: {
    productId: {
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
      console.log('val = ', val)
      this.open = val
      if (val === true) {
        this.resetQueryParams()
      }
    }
  },
  data() {
    return {
      // 弹出层标题
      title: '添加参数',
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
        description: undefined,
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
      this.submitType = 1
      this.title = '添加参数'
      this.form = {
        id: undefined,
        name: undefined,
        valueType: undefined,
        unit: undefined,
        precision: undefined,
        length: undefined,
        minRange: undefined,
        maxRange: undefined,
        description: undefined,
        enum: [{
          value: undefined,
          text: undefined
        }]
      }
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
          if (this.submitType === 1) {
            this.$emit('addParams', this.form)
            this.$emit('ok')
            this.open = false
          } else if (this.submitType === 2) {
            this.$emit('ok')
            this.submitType = 1
            this.open = false
          }
        } else {
          return false
        }
      })
    },
    // 修改参数
    editParams(data) {
      console.log('data = ', data)
      this.submitType = 2
      this.title = '修改参数'
      const value = data.valueType
      this.form = data
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
