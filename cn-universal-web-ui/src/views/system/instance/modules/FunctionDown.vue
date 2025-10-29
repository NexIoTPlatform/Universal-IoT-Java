<template>
  <div v-show="open" class="function-down-container">
    <div class="function-section">
      <div class="section-header">
        <h3>操作区域</h3>
        <p>选择功能指令并填写参数，点击发送执行操作</p>
      </div>

      <div class="form-section">
        <a-form-model :model="functionFrom" ref="functionForm" class="function-form">
          <a-form-model-item
            label="功能标识"
            prop="function"
            :rules="{
              required: true, message: '请选择功能标识', trigger: 'change'
            }">
            <a-select placeholder="请选择下发的指令" size="large" v-model="functionFrom.function"
                      allow-clear @change="optionChange">
              <a-select-option v-for="(d, index) in metaData.functions" :key="index" :value="d.id">
                {{ d.name }}
              </a-select-option>
            </a-select>
          </a-form-model-item>

          <a-form-model-item
            v-if="sourceShow"
            label="服务类型(serviceType)"
            prop="source">
            <a-input placeholder="服务类型(电信设备附带，非必填项)" v-model="serviceType"/>
          </a-form-model-item>

          <template v-for="(item, index) in functionParams">
            <template
              v-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'int'">
              <a-form-model-item
                :key="index"
                :prop="item.id"
                :rules="[
                  {required: true, message: item.name + '不能为空', trigger: 'blur'},
                  {min: Number.parseInt(item.valueType.min) , max: Number.parseInt(item.valueType.max), type: 'number', message: '参数必须为整数，且大于'+item.valueType.min+',小于'+item.valueType.max}
                ]">
                <span slot="label">
                  {{ item.name }}
                  <a-tooltip
                    style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                    <template slot="title">
                      {{ item.description }}
                    </template>
                    <a-icon type="question"/>
                  </a-tooltip>
                </span>
                <a-input
                  :placeholder="`请输入${item.description === undefined ? '内容' : item.description}`"
                  :suffix="item.valueType.unit" v-model.number="functionFrom[item.id]"/>
              </a-form-model-item>
            </template>
            <template
              v-else-if="item.valueType !== undefined && item.valueType !== null && (item.valueType.type === 'float'||item.valueType.type === 'double')">
              <a-form-model-item
                :key="index"
                :prop="item.id"
                :rules="[
                  {required: true, validator: checkFloat, trigger: 'blur'}
                ]">
                <span slot="label">
                  {{ item.name }}
                  <a-tooltip
                    style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                    <template slot="title">
                      {{ item.description }}
                    </template>
                    <a-icon type="question"/>
                  </a-tooltip>
                </span>
                <a-input
                  :placeholder="`请输入${item.description === undefined ? '内容' : item.description}`"
                  v-model="functionFrom[item.id]" :suffix="item.valueType.unit"/>
              </a-form-model-item>
            </template>
            <template
              v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'string'">
              <a-form-model-item
                :key="index"
                :prop="item.id"
                :rules="[
                  {required: true, message: item.name + '不能为空', trigger: 'blur'},
                  {min: 0 , max: item.valueType.expands !== undefined ? Number.parseInt(item.valueType.expands.maxLength) : undefined , message: item.valueType.expands !== undefined ? '最大输入长度' + Number.parseInt(item.valueType.expands.maxLength) : undefined, trigger: 'blur'}
                ]">
                <span slot="label">
                  {{ item.name }}
                  <a-tooltip
                    style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                    <template slot="title">
                      {{ item.description }}
                    </template>
                    <a-icon type="question"/>
                  </a-tooltip>
                </span>
                <a-input
                  :placeholder="`请输入${item.description === undefined ? '内容' : item.description}`"
                  v-model="functionFrom[item.id]" :suffix="item.valueType.unit"/>
              </a-form-model-item>
            </template>
            <template
              v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'object'">
              <a-form-model-item
                :key="index"
                :prop="item.id"
                :rules="[
                  {required: true, validator: checkJson, trigger: 'blur'}
                ]">
                <span slot="label">
                  {{ item.name }}
                  <a-tooltip
                    style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                    <template slot="title">
                      {{ item.description }}
                    </template>
                    <a-icon type="question"/>
                  </a-tooltip>
                </span>
                <a-input
                  :placeholder="`请输入${item.description === undefined ? '内容' : item.description}`"
                  v-model="functionFrom[item.id]"/>
              </a-form-model-item>
            </template>
            <template
              v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'enum'">
              <a-form-model-item
                :key="index"
                :prop="item.id"
                :rules="[
                  {required: true, message: item.name + '不能为空', trigger: 'change'}
                ]">
                <span slot="label">
                  {{ item.name }}
                  <a-tooltip
                    style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                    <template slot="title">
                      {{ item.description }}
                    </template>
                    <a-icon type="question"/>
                  </a-tooltip>
                </span>
                <a-select
                  :placeholder="`请选择${item.description === undefined ? '选项' : item.description}`"
                  size="large" v-model="functionFrom[item.id]" allow-clear>
                  <a-select-option v-for="(d, indexT) in item.valueType.elements" :key="indexT"
                                   :value="d.value">{{ d.text }}
                  </a-select-option>
                </a-select>
              </a-form-model-item>
            </template>
          </template>

          <div class="action-section">
            <a-button type="primary" @click="functonDown" :loading="submitting">
              立即发送
            </a-button>
          </div>
        </a-form-model>
      </div>
    </div>

    <div class="log-section">
      <div class="log-header">
        <span>实时日志</span>
        <a-button type="link" class="log-clear-btn" @click="clearMessage">
          <iot-icon type="icon-u-del"/>
          <span>清空</span>
        </a-button>
      </div>
      <div class="log-content">
        <div
          v-for="(line, idx) in parsedLogLines"
          :key="idx"
          class="log-line"
          :class="{
            'log-success': line.status === 'success',
            'log-fail': line.status === 'fail'
          }"
        >
          <div>
            <span class="log-time">{{ line.time }}</span>
            <span class="log-action">{{ line.action }}</span>
            <span class="log-result">{{ line.result }}</span>
          </div>
          <div v-if="line.params" class="log-params-block">
            <span class="log-params-label">参数: </span>
            <span class="log-params-content">{{ line.params }}</span>
          </div>
          <div v-if="line.resultDetail" class="log-result-block">
            <span class="log-result-label">结果: </span>
            <span class="log-result-content">{{ line.resultDetail }}</span>
          </div>
        </div>
        <div v-if="!parsedLogLines.length" class="log-empty">暂无日志</div>
      </div>
    </div>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer'
import {functionDown} from '@/api/system/dev/instance'
import {autoFillFromParams} from '@/utils/deviceConfig'

export default {
  name: 'FunctionDown',
  props: {
    metaData: {
      type: Object,
      require: true,
      default: () => {
      }
    },
    show: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: ''
    },
    configration: {
      type: String,
      default: ''
    },
    productKey: {
      type: String,
      default: ''
    },
    deviceNo: {
      type: String,
      default: ''
    },
    source: {
      type: String,
      default: ''
    }
  },
  components: {
    JsonViewer
  },
  computed: {
    parsedLogLines() {
      if (!this.message) {
        return [];
      }
      const lines = this.message.split('\n').filter(Boolean);
      const result = [];
      for (let i = 0; i < lines.length; i += 2) {
        const opLine = lines[i];
        const resLine = lines[i + 1] || '';
        const timeMatch = opLine.match(/^\[(.*?)\]/);
        const time = timeMatch ? timeMatch[1] : '';
        let status = '';
        if (opLine.includes('成功')) {
          status = 'success';
        }
        if (opLine.includes('失败')) {
          status = 'fail';
        }
        const actionMatch = opLine.match(/功能下发\[(.*?)\]/) || opLine.match(/功能调用\[(.*?)\]/);
        const action = actionMatch ? actionMatch[1] : '';
        const resultMatch = opLine.match(/成功|失败/);
        const resultText = resultMatch ? resultMatch[0] : '';
        const paramsMatch = opLine.match(/参数: (\{.*\})/) || opLine.match(/下发参数: (\{.*\})/);
        const params = paramsMatch ? paramsMatch[1] : '';
        const resultDetailMatch = resLine.match(/结果: (.*)$/) || resLine.match(/下发结果: (.*)$/)
          || resLine.match(/调用结果: (.*)$/);
        const resultDetail = resultDetailMatch ? resultDetailMatch[1] : resLine.replace(
          /^\[.*?\]\s*/, '');
        result.push({time, status, action, result: resultText, params, resultDetail});
      }
      return result;
    }
  },
  watch: {
    show(val) {
      this.open = val
      if (val === true) {
        this.initData()
      } else {
        this.open = false
      }
    }
  },
  data() {
    return {
      // message
      messageCopy: '',
      // message
      message: '',
      // 是否显示弹出层
      open: false,
      // 提交状态
      submitting: false,
      // 下发表单
      functionFrom: {
        function: undefined
      },
      // 下发参数
      functionParams: [],
      // 控制电信服务类型属性的显示
      sourceShow: false,
      // 服务类型
      serviceType: undefined
    }
  },
  methods: {
    checkJson(rule, value, callback) {
      function isValidJsonString(str) {
        try {
          JSON.parse(str)
        } catch (e) {
          return false
        }
        return true
      }

      if (!value) {
        callback(new Error('输入框不能为空'))
      } else {
        const jsonRegex = /^[\],:{}\s]*$|^[\s]*\[.+?\][\s]*$|^[\s]*\{.+\}[\s]*$/
        if (!jsonRegex.test(value) || !isValidJsonString(value)) {
          callback(new Error('输入框必须为json格式'))
        } else {
          callback()
        }
      }
    },
    checkFloat(rule, value, callback) {
      if (!value) {
        callback(new Error('输入框不能为空'))
      } else {
        const jsonRegex = /^\d+(\.\d+)?$/
        if (!jsonRegex.test(value)) {
          callback(new Error('请输入正确的浮点数'))
        } else {
          callback()
        }
      }
    },
    // 清除日志
    clearMessage() {
      this.message = ''
      this.messageCopy = ''
    },
    // 功能下发
    functonDown() {
      console.log('this.functionFrom = ', this.functionFrom)
      this.$refs.functionForm.validate(valid => {
        if (valid) {
          this.submitting = true
          console.log('this.functionFrom success = ', this.functionFrom)
          const formData = {}
          const formDataPy = {}
          formData.appUnionId = this.$store.state.user.name
          formData.productKey = this.productKey
          formData.deviceId = this.deviceNo
          formData.cmd = 'DEV_FUNCTION'
          formDataPy.deviceId = this.deviceNo
          formDataPy.cmd = 'DEV_FUNCTION'
          const keyArray = Object.keys(this.functionFrom)
          const keyValue = Object.values(this.functionFrom)
          const data = {}
          for (let i = 0; i < keyArray.length; i++) {
            if (keyArray[i] === 'function') {
              continue
            }
            data[keyArray[i]] = keyValue[i]
          }
          formData.function = {
            messageType: 'FUNCTIONS',
            serviceType: this.serviceType,
            function: this.functionFrom.function,
            data: data
          }
          formDataPy.function = formData.function
          console.log('formData success = ', formData)
          let fun = ''
          for (let i = 0; i < this.metaData.functions.length; i++) {
            if (this.metaData.functions[i].id === this.functionFrom.function) {
              formData.function.source = this.metaData.functions[i].source
              fun = this.metaData.functions[i].name
              break
            }
          }
          functionDown(this.productKey, formData).then(res => {
            console.log('res = ', res)
            if (this.messageCopy === '') {
              this.messageCopy = this.messageCopy + '[' + this.formatDate(new Date()) + '] 功能下发['
                + fun + ']: ' + '下发成功' + '[下发参数: ' + JSON.stringify(formDataPy) + ']'
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 下发结果: ' + JSON.stringify(res.data)
            } else {
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 功能下发[' + fun + ']: ' + '下发成功' + '[下发参数: ' + JSON.stringify(
                  formDataPy) + ']'
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 下发结果: ' + JSON.stringify(res.data)
            }
            this.message = '\n' + this.messageCopy + '\n'
            this.$message.success(
              '下发成功',
              3
            )
            this.submitting = false
          }).catch(reason => {
            // 兼容处理：reason 可能是字符串（被拦截器处理过）或者是原始错误对象
            let errorMsg = ''
            if (typeof reason === 'string') {
              // 如果是字符串，说明被 request.js 拦截器处理过
              errorMsg = reason
            } else if (reason && reason.response && reason.response.data) {
              // 如果是原始错误对象
              errorMsg = reason.response.data.msg || reason.message || '下发失败'
            } else {
              // 其他情况
              errorMsg = reason.message || reason.toString() || '下发失败'
            }

            if (this.messageCopy === '') {
              this.messageCopy = this.messageCopy + '[' + this.formatDate(new Date()) + '] 功能下发['
                + fun + ']: ' + '下发失败' + '[下发参数: ' + JSON.stringify(formDataPy) + ']'
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 下发结果: ' + errorMsg
            } else {
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 功能下发[' + fun + ']: ' + '下发失败' + '[下发参数: ' + JSON.stringify(
                  formDataPy) + ']'
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 下发结果: ' + errorMsg
            }
            this.message = '\n' + this.messageCopy + '\n'
            this.submitting = false
          })
        } else {
          this.$message.error('请完善必填项')
          return false
        }
      })
    },
    // 下发功能发生改变
    optionChange(value, option) {
      if (this.source === 'ctaiot') {
        this.sourceShow = true
      } else {
        this.sourceShow = false
      }
      this.serviceType = undefined
      console.log('configration = ', this.configration)
      console.log('value event = ', value)
      console.log('option event = ', option)
      
      // 重置表单，保留功能选择
      this.functionFrom = {
        function: value
      }
      
      if (value !== undefined && value !== null) {
        this.metaData.functions.forEach(item => {
          if (item.id === value) {
            console.log('item.id = ', item.id)
            // 初始化参数项
            this.functionParams = item.inputs
            
            // 使用 deviceConfig.js 的自动填充功能
            const autoFilledValues = autoFillFromParams(
              this.functionParams,
              this.deviceNo,
              this.configration
            )
            
            // 合并自动填充的值到表单
            this.functionFrom = {
              ...this.functionFrom,
              ...autoFilledValues
            }
            
            console.log('this.functionFrom = ', this.functionFrom, this.functionParams)
          }
        })
      } else {
        this.functionParams = []
        this.functionFrom = {
          function: undefined
        }
      }
      this.$refs.functionForm.validateField('function')
      this.$refs.functionForm.resetFields()
    },
    // 初始化数据
    initData() {
      console.log('metaData event = ', this.metaData)
      console.log('metaData productKey = ', this.productKey)
      console.log('metaData deviceNo = ', this.deviceNo)
      this.functionParams = []
      this.clearMessage()
      this.functionFrom = {
        function: undefined
      }
      this.open = true
      this.$nextTick(() => {
        this.$refs.functionForm.resetFields()
      })
    },
    // 关闭弹出层
    closePanel() {
      this.$emit('close')
    },
    // 时间转换
    formatDate(time) {
      // 获取年
      const year = time.getFullYear()
      // 获取月
      const month = time.getMonth() + 1
      // 获取日
      const date = time.getDate()
      // 获取星期
      // eslint-disable-next-line no-unused-vars
      const day = time.getDay()
      // 获取小时
      const hours = time.getHours()
      // 获取分钟
      const minutes = time.getMinutes()
      // 获取秒
      const seconds = time.getSeconds()
      // 获取毫秒
      // eslint-disable-next-line no-unused-vars
      const ms = time.getMilliseconds()
      let curDateTime = year
      if (month > 9) {
        curDateTime = curDateTime + '-' + month
      } else {
        curDateTime = curDateTime + '-0' + month
      }
      if (date > 9) {
        curDateTime = curDateTime + '-' + date
      } else {
        curDateTime = curDateTime + '-0' + date
      }
      if (hours > 9) {
        curDateTime = curDateTime + ' ' + hours
      } else {
        curDateTime = curDateTime + ' 0' + hours
      }
      if (minutes > 9) {
        curDateTime = curDateTime + ':' + minutes
      } else {
        curDateTime = curDateTime + ':0' + minutes
      }
      if (seconds > 9) {
        curDateTime = curDateTime + ':' + seconds
      } else {
        curDateTime = curDateTime + ':0' + seconds
      }
      return curDateTime
    }
  }

}
</script>
<style scoped>
.function-down-container {
  display: flex;
  gap: 24px;
  margin-top: 15px;
  height: calc(100vh - 200px);
}

.function-section {
  flex: 1;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

.section-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.section-header h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.section-header p {
  margin: 0;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.4;
}

.form-section {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.function-form {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.action-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.log-section {
  flex: 1;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

.log-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-header span {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.log-clear-btn {
  font-size: 12px !important;
  padding: 4px 8px !important;
  height: 24px !important;
  line-height: 16px !important;
  background: #f5f5f5 !important;
  border-radius: 4px;
}

.log-content {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.6;
}

.log-line {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  word-break: break-all;
}

.log-line:last-child {
  border-bottom: none;
}

.log-time {
  color: #999;
  min-width: 90px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.log-action {
  color: #333;
  font-weight: bold;
}

.log-result {
  margin-left: 4px;
}

.log-success .log-result {
  color: #52c41a;
  font-weight: bold;
}

.log-fail .log-result {
  color: #f5222d;
  font-weight: bold;
}

.log-params-block, .log-result-block {
  margin-left: 12px;
  margin-top: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  padding: 4px 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.log-params-label, .log-result-label {
  color: #666;
  font-weight: 500;
}

.log-params-content, .log-result-content {
  color: #333;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.log-empty {
  color: #bbb;
  text-align: center;
  padding: 40px 0;
  font-size: 14px;
}


@media (max-width: 1200px) {
  .function-down-container {
    gap: 16px;
  }
}
</style>
