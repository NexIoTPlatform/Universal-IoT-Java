<template>
  <a-modal
    :visible="open"
    :title="title"
    width="600px"
    :footer="null"
    @cancel="onClose"
    centered
    class="function-modal"
  >
    <a-form-model ref="form" :model="form">
      <!--      <a-form-model-item label="是否属性设置" prop="isConfig" :required="true" :colon="true">-->
      <!--        <a-switch v-model="form.isConfig" :disabled="functionParams.length > 0"-->
      <!--                  checked-children="是" un-checked-children="否" @change="configChange"/>-->
      <!--      </a-form-model-item>-->
      <!--      <a-alert v-if="form.isConfig"-->
      <!--               message="只能使用物模型中'读写'的属性为参数。用于设备配置值不支持读取，设备影子来保存配置值。"-->
      <!--               banner/>-->
      <!--      <a-alert v-if="form.isConfig" message="功能标识必须以英文`set`开头，使用小驼峰命名规则"-->
      <!--               banner/>-->
      <a-form-model-item
        label="功能标识"
        prop="id"
        :rules="{
          required: true , message: '必须英文，小驼峰', trigger: 'change'
        }">
        <a-input size="large" v-model="form.id" placeholder="必须英文，小驼峰"/>
      </a-form-model-item>
      <a-form-model-item
        label="功能名称"
        prop="name"
        :rules="{
          required: true , message: '中文功能名称', trigger: 'change'
        }">
        <a-input size="large" v-model="form.name" placeholder="中文功能名称"/>
      </a-form-model-item>
      <a-form-model-item
        v-if="false"
        label="是否异步"
        prop="asyn"
        :rules="{
          required: true , message: '请选择是否异步', trigger: 'change'
        }">
        <a-radio-group size="large" v-model="form.asyn">
          <a-radio :value="true">
            是
          </a-radio>
          <a-radio :value="false">
            否
          </a-radio>
        </a-radio-group>
      </a-form-model-item>
      <a-form-model-item
        label="输入参数(*参数标志相同的参数会被替换掉)"
        prop="input">
        <a-row
          v-if="functionParams.length > 0">
          <a-list
            item-layout="horizontal"
            :data-source="functionParams"
            :bordered="true"
          >
            <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
              <div>参数标志：{{ item.id }} 参数名称：{{ item.name }}</div>
              <a slot="actions" @click="editParam(item)" v-show="!form.isConfig">{{
                  $t('button.edit')
                }}</a>
              <a slot="actions" @click="delParam(index)">{{ $t('button.delete') }}</a>
            </a-list-item>
          </a-list>
        </a-row>
        <a-icon type="plus" style="width: 6%;color: #1966ff;font-size: 16px"
                @click="addFunctionParams"/>
        <a-button type="link" @click="addFunctionParams">添加参数</a-button>
      </a-form-model-item>
      <a-form-model-item
        v-if="false"
        label="输出参数"
        prop="valueType">
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
          placeholder="功能描述信息"
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
    <FunctionParamsForm
      ref="functionParamsForm"
      :isConfig="form.isConfig"
      :product-id="productId"
      :show="functionParamsShow"
      @addParams="addParams"
      @ok="closeFunctionParamsForm"
    />
    <PropertiesTable
      :show="propertiesTableShow"
      :product-id="productId"
      @ok="propertiesTableShow = false"
      @addProperties="addProperties"
    />
  </a-modal>
</template>

<script>
import {metadataAdd, metadataUpdate} from '@/api/system/dev/product'
import FunctionParamsForm from './FunctionParamsForm'
import PropertiesTable from './PropertiesTable'

export default {
  name: 'FunctionForm',
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
  components: {
    FunctionParamsForm,
    PropertiesTable
  },
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
      source: undefined,
      // 功能参数是否显示控制
      functionParamsShow: false,
      // 弹出层标题
      title: '添加功能',
      // 是否显示弹出层
      open: false,
      // 表单
      form: {
        isConfig: false,
        id: undefined,
        name: undefined,
        valueType: undefined,
        unit: undefined,
        precision: undefined,
        length: undefined,
        minRange: undefined,
        maxRange: undefined,
        asyn: undefined,
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
      // 功能参数
      functionParams: [],
      // 提交类型 1：增加；2：修改
      submitType: 1,
      propertiesTableShow: false
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
      this.submitType = 1
      this.title = '添加功能'
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
          asyn: undefined,
          describe: undefined,
          enum: [{
            value: undefined,
            text: undefined
          }]
        }
        this.functionParams = []
      })
      this.showUnit = false
      this.showPrecision = false
      this.showLength = false
      this.showEnum = false
      this.showNumberRange = false
    },
    configChange(value, option) {
      if (value) {
        this.form.id = 'set'
      } else {
        this.form.id = ''
      }
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
      console.log('this.functionParams = ', this.functionParams)
      if (this.form.isConfig === undefined || this.form.isConfig === null) {
        this.form.isConfig = false
      }
      this.$refs.form.validate(valid => {
        if (valid) {
          // if (!this.form.isConfig && this.form.id.startsWith('set')) {
          //   this.$message.error(
          //     '功能标识不能以set为前缀',
          //     3
          //   )
          //   return
          // } else if (this.form.isConfig && !this.form.id.startsWith('set')) {
          //   this.$message.error(
          //     '功能标识必须以set为前缀',
          //     3
          //   )
          //   return
          // }

          const inputValue = []

          if (!this.form.isConfig) {
            for (let i = 0; i < this.functionParams.length; i++) {
              // 遍历枚举
              if (this.functionParams[i].enum !== undefined) {
                for (let k = this.functionParams[i].enum.length - 1; k >= 0; k--) {
                  if (this.functionParams[i].enum[k].value === undefined
                    || this.functionParams[i].enum[k].text === undefined
                    || this.functionParams[i].enum[k].value === ''
                    || this.functionParams[i].enum[k].text === '') {
                    this.functionParams[i].enum.splice(k, 1)
                  }
                }
              }
              // 添加参数
              inputValue.push({
                id: this.functionParams[i].id,
                name: this.functionParams[i].name,
                valueType: {
                  precision: this.functionParams[i].precision,
                  type: this.functionParams[i].valueType,
                  min: this.functionParams[i].minRange,
                  max: this.functionParams[i].maxRange,
                  unit: this.functionParams[i].unit,
                  elements: this.functionParams[i].enum.length === 0 ? undefined
                    : this.functionParams[i].enum,
                  expands: this.functionParams[i].length !== undefined
                  && this.functionParams[i].length !== null
                    ? {maxLength: this.functionParams[i].length} : undefined
                },
                description: this.functionParams[i].description
              })
            }
          } else {
            for (let i = 0; i < this.functionParams.length; i++) {
              // 添加参数
              inputValue.push({
                id: this.functionParams[i].id,
                name: this.functionParams[i].name,
                valueType: {
                  precision: this.functionParams[i].precision,
                  type: this.functionParams[i].type,
                  min: undefined,
                  max: undefined,
                  unit: this.functionParams[i].unit,
                  elements: this.functionParams[i].elements === undefined ? undefined : JSON.parse(
                    this.functionParams[i].elements),
                  expands: undefined
                },
                description: this.functionParams[i].description
              })
            }
          }

          const data = {
            // key
            productKey: this.productKey,
            // 属性、事件、功能类型，三选一
            type: 'functions',
            // 用来判断删除或者修改那个属性
            oldId: this.oldId,
            ioTProductMetadataBO: {
              id: this.form.id,
              name: this.form.name,
              config: !((this.form.isConfig === undefined || this.form.isConfig === null
                || this.form.isConfig === false)),
              source: this.source,
              description: this.form.describe,
              inputs: inputValue, // this.form.isConfig ? this.functionParams : inputValue,
              output: {}
            }
          }
          console.log('data functionForm = ', data)
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
    // 修改功能
    editFunctions(data) {
      console.log('data = ', data)
      this.submitType = 2
      this.title = '修改功能'
      this.source = data.source
      const value = data.type
      this.functionParams = []
      let inputs = []
      if (data.inputs !== undefined && data.inputs === '[]') {
      } else if (data.inputs !== undefined && data.inputs !== '[]') {
        inputs = JSON.parse(data.inputs)
      }
      console.log('inputs = ', inputs)
      for (let i = 0; i < inputs.length; i++) {
        if (inputs[i].valueType === undefined || inputs[i].valueType === null) {
          continue
        }
        this.functionParams.push({
          id: inputs[i].id,
          name: inputs[i].name,
          valueType: inputs[i].valueType.type,
          unit: inputs[i].valueType.unit,
          precision: inputs[i].valueType.precision,
          length: inputs[i].valueType.type === 'string' || inputs[i].valueType.type === 'text'
            ? inputs[i].valueType.expands !== undefined ? inputs[i].valueType.expands.maxLength
              : undefined : undefined,
          minRange: inputs[i].valueType.min,
          maxRange: inputs[i].valueType.max,
          asyn: undefined,
          describe: inputs[i].description,
          enum: inputs[i].valueType.type === 'enum' ? inputs[i].valueType.elements !== undefined
              ? inputs[i].valueType.elements : [{value: undefined, text: undefined}]
            : [{value: undefined, text: undefined}]
        })
      }
      console.log('inputs = ', inputs)
      console.log('this.functionParams = ', this.functionParams)
      this.oldId = data.id
      this.form = {
        id: data.id,
        name: data.name,
        isConfig: data.config, //! ((data.isConfig === 'false' || data.isConfig === null)),
        valueType: undefined,
        unit: undefined,
        precision: undefined,
        length: undefined,
        minRange: undefined,
        maxRange: undefined,
        asyn: undefined,
        describe: data.description,
        enum: [{
          value: undefined,
          text: undefined
        }]
      }
      console.log('config form = ', this.form)
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
    },
    // 添加功能参数
    addFunctionParams() {
      if (this.form.isConfig) {
        this.propertiesTableShow = true
      } else {
        this.functionParamsShow = true
      }
      console.log(this.propertiesTableShow)
      console.log(this.functionParamsShow)
    },
    // 关闭功能参数表单
    closeFunctionParamsForm() {
      this.functionParamsShow = false
    },
    // 添加参数回调
    addParams(data) {
      for (let i = this.functionParams.length - 1; i >= 0; i--) {
        if (this.functionParams[i].id === data.id) {
          this.functionParams.splice(i, 1)
          break
        }
      }
      this.functionParams.push(data)
    },
    addProperties(list) {
      this.functionParams = list
    },
    // 修改参数
    editParam(data) {
      this.$refs.functionParamsForm.editParams(data)
    },
    // 删除参数
    delParam(index) {
      this.functionParams.splice(index, 1)
    }
  }

}
</script>
<style scoped>
.function-modal >>> .ant-modal-content {
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  box-shadow: 0 4px 24px 0 rgba(25, 102, 255, 0.10);
  padding: 0;
}

.function-modal >>> .ant-modal-header {
  border-radius: 14px 14px 0 0;
  background: #f8fafc;
  font-size: 18px;
  font-weight: bold;
  padding: 24px 32px 12px 32px;
}

.function-modal >>> .ant-modal-title {
  font-size: 18px;
  font-weight: bold;
}

.function-modal >>> .ant-modal-body {
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

.function-modal >>> .ant-modal-body::-webkit-scrollbar {
  width: 6px;
  background: #f3f4f6;
  border-radius: 6px;
}

.function-modal >>> .ant-modal-body::-webkit-scrollbar-thumb {
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
