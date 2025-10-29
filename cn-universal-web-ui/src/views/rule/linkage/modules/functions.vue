<template>
  <div>
    <a-form-model ref="form" :model="value">
      <a-row :gutter="20">
        <a-col :span="6">
          <a-input v-model="value.id" placeholder="id" :disabled="true">
          </a-input>
        </a-col>
        <a-col :span="6">
          <a-input v-model="value.name" placeholder="名称" :disabled="true">
          </a-input>
        </a-col>
        <a-col :span="12">
          <template
            v-if="value.valueType !== undefined && value.valueType !== null && value.valueType.type === 'int'">
            <!--:label="item.name"-->
            <a-form-model-item
              prop="params"
              style="width: 100%"
              :rules="[
                {required: true , message: value.name + '不能为空', trigger: 'blur'},
                {min: value.valueType.min , max: value.valueType.max, type: 'number', message: '参数必须为整数，且大于'+value.valueType.min+',小于'+value.valueType.max}
              ]">
              <a-input
                :placeholder="`${value.description}`"
                :suffix="value.valueType.unit"
                v-model.number="value.params"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="value.valueType !== undefined && value.valueType !== null && value.valueType.type === 'string'">
            <!--:label="item.name"-->
            <a-form-model-item
              prop="params"
              style="width: 100%"
              :rules="[
                {required: true , message: value.name + '不能为空', trigger: 'blur'},
                {min: 0 , max: value.valueType.expands !== undefined ? Number.parseInt(value.valueType.expands.maxLength) : undefined , message: value.valueType.expands !== undefined ? '最大输入长度' + Number.parseInt(value.valueType.expands.maxLength) : undefined, trigger: 'blur'}
              ]">
              <a-input
                v-if="value.id === 'childDeviceId' || value.id === 'deviceId' || value.id === 'meterNo' || value.id === 'imei'"
                :disabled="value.params!=undefined"
                :placeholder="`${value.description}`"
                v-model="value.params"
                :suffix="value.valueType.unit"/>
              <a-input
                v-else
                :placeholder="`${value.description}`"
                v-model="value.params"
                :suffix="value.valueType.unit"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="value.valueType !== undefined && value.valueType !== null && value.valueType.type === 'float'">
            <!--:label="item.name"-->
            <a-form-model-item
              prop="params"
              style="width: 100%"
              :rules="[
                {required: true , message: value.name + '不能为空', trigger: 'blur'},
                {type: 'number', message: '参数必须为数字', trigger: 'blur'}
              ]">
              <a-input
                :placeholder="`${value.description}`"
                :suffix="value.valueType.unit"
                v-model.number="value.params"
                type="number"
                step="any"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="value.valueType !== undefined && value.valueType !== null && value.valueType.type === 'double'">
            <!--:label="item.name"-->
            <a-form-model-item
              prop="params"
              style="width: 100%"
              :rules="[
                {required: true , message: value.name + '不能为空', trigger: 'blur'},
                {type: 'number', message: '参数必须为数字', trigger: 'blur'}
              ]">
              <a-input
                :placeholder="`${value.description}`"
                :suffix="value.valueType.unit"
                v-model.number="value.params"
                type="number"
                step="any"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="value.valueType !== undefined && value.valueType !== null && value.valueType.type === 'boolean'">
            <!--:label="item.name"-->
            <a-form-model-item
              prop="params"
              style="width: 100%"
              :rules="[
                {required: true , message: value.name + '不能为空', trigger: 'blur'}
              ]">
              <a-select :placeholder="`${value.description}`" size="large" v-model="value.params"
                        allow-clear>
                <a-select-option value="true">是</a-select-option>
                <a-select-option value="false">否</a-select-option>
              </a-select>
            </a-form-model-item>
          </template>
          <template
            v-else-if="value.valueType !== undefined && value.valueType !== null && value.valueType.type === 'enum'">
            <!--:label="item.name"-->
            <a-form-model-item
              prop="params"
              style="width: 100%"
              :rules="[
                {required: true , message: value.name + '不能为空', trigger: 'blur'}
              ]">
              <a-select :placeholder="`${value.description}`" size="large" v-model="value.params"
                        allow-clear>
                <a-select-option v-for="(d, index) in value.valueType.elements" :key="index"
                                 :value="d.value">{{
                    d.text
                  }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </template>
          <template v-else-if="value.valueType === undefined">
            <a-input
              v-model="value.params"
              :disabled="value.id === 'childDeviceId' || value.id === 'deviceId' || value.id === 'meterNo' || value.id === 'imei'"
              placeholder="请输入参数">
            </a-input>
          </template>
        </a-col>
      </a-row>
    </a-form-model>
  </div>
</template>

<script>

export default {
  name: 'Functions',
  props: {
    value: {
      type: Object,
      // eslint-disable-next-line vue/require-valid-default-prop
      default: () => {
      }
    },
    index: {
      type: Number
    }
  },

  data() {
    return {}
  },
  watch: {},
  created() {
    // if (this.devNo && (this.value.id === 'childDeviceId' || this.value.id === 'deviceId' || this.value.id === 'meterNo' || this.value.id === 'imei')) {
    //   // this.form.params = this.devNo
    //   this.value.params = this.devNo
    // }
  },
  mounted: function () {
    // if (this.value.params) {
    //   this.form.params = this.value.params
    // }
    // if (this.value.id) {
    //   this.form.id = this.value.id
    // }
    // if (this.value.name) {
    //   this.form.name = this.value.name
    // }
  },
  methods: {
    validate() {
      this.$refs.form.validate(valid => {
        if (valid) {
          return true
        } else {
          return false
        }
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
