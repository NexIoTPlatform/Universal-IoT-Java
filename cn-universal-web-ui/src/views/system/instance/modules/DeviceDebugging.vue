<template>
  <!--  <a-modal-->
  <!--    :maskClosable="false"-->
  <!--    title="功能下发"-->
  <!--    v-model="open"-->
  <!--    width="600px"-->
  <!--    :footer="null"-->
  <!--    @cancel="closePanel" >-->
  <a-row v-show="open" style="margin-top: 15px">
    <a-col :span="12">
      <div data-v-17f65cca="" class="panel-title">
        <p data-v-17f65cca="">设备属性/事件模拟</p>
      </div>
      <a-form-model :model="functionFrom" ref="functionForm" class="operation-wrap">
        <a-form-model-item
          label="请选择服务类型"
          prop="function"
          style="width: 100%"
          :rules="{
            required: true , message: '请选择服务类型', trigger: 'blur'
          }">
          <a-select placeholder="请选择服务类型" size="large" v-model="functionFrom.function"
                    allow-clear @change="optionChange">
            <a-select-option v-for="(d, index) in deviceDebuggingSelect" :key="index" :value="d.id">
              {{ d.name }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-row style="margin-bottom: 15px" v-if="configAndHexShow">

          <a-space :size="8">
            <a-tooltip placement="left" title="仅模拟上报数据推送">
              <a-button :ghost="configTransparent" shape="round" type="primary" size="small"
                        @click="configClick">配置
              </a-button>
            </a-tooltip>
            <a-tooltip placement="right" title="模拟完整流程,协议编解码,在离线判断,上报数据推送">
              <a-button
                :disabled="!(device.hasProtocol)"
                :ghost="hexTransparent"
                shape="round"
                type="primary"
                size="small"
                @click="hexClick"
              >十六进制
              </a-button>
            </a-tooltip>
          </a-space>
        </a-row>
        <a-form-model-item
          v-if="hexShow"
          label="十六进制报文"
          prop="payload"
          style="width: 100%"
          :rules="[
            {required: true , message: '报文不能为空', trigger: 'blur'},
            {pattern: /^[a-fA-F0-9]+$/, message: '请输入有效十六进制格式的内容', trigger: 'change'}
          ]">
          <a-input placeholder="请输入十六进制报文" v-model="functionFrom.payload"/>
        </a-form-model-item>
        <a-form-model-item
          v-if="eventSelect.length > 0"
          label="请选择事件类型"
          prop="event"
          style="width: 100%"
          :rules="{
            required: true , message: '请选择事件类型', trigger: 'blur'
          }">
          <a-select placeholder="请选择事件类型" size="large" v-model="functionFrom.event"
                    allow-clear @change="eventChange">
            <a-select-option v-for="(d, index) in eventSelect" :key="index" :value="d.id">{{
                d.name
              }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <template v-for="(item, index) in proOrEventParams">
          <template
            v-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'int'">
            <!--:label="item.name"-->
            <a-form-model-item
              :key="index"
              :prop="item.id"
              style="width: 100%"
              :rules="[]">
              <span slot="label">
                {{ item.name }}
              </span>
              <a-input placeholder="" :suffix="item.valueType.unit"
                       v-model.number="functionFrom[item.id]"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'float'">
            <!--:label="item.name"-->
            <a-form-model-item
              :key="index"
              :prop="item.id"
              style="width: 100%"
              :rules="[]">
              <span slot="label">
                {{ item.name }}
              </span>
              <a-input
                v-if="item.id === 'deviceCode' ||item.id === 'deviceId' ||item.id === 'devEUI' ||item.id === 'meterNo' ||item.id === 'did'"
                :disabled="true" :placeholder="`${item.description}`"
                v-model="functionFrom[item.id]" :suffix="item.valueType.unit"/>
              <a-input v-else placeholder="" v-model="functionFrom[item.id]"
                       :suffix="item.valueType.unit"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'string'">
            <!--:label="item.name"-->
            <a-form-model-item
              :key="index"
              :prop="item.id"
              style="width: 100%"
              :rules="[]">
              <span slot="label">
                {{ item.name }}
              </span>
              <a-input
                v-if="item.id === 'deviceCode' ||item.id === 'deviceId' ||item.id === 'devEUI' ||item.id === 'meterNo' ||item.id === 'did'"
                :disabled="true" :placeholder="`${item.description}`"
                v-model="functionFrom[item.id]" :suffix="item.valueType.unit"/>
              <a-input v-else placeholder="" v-model="functionFrom[item.id]"
                       :suffix="item.valueType.unit"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'object'">
            <!--:label="item.name"-->
            <a-form-model-item
              :key="index"
              :prop="item.id"
              style="width: 100%"
              :rules="[
                {validator: checkJson, trigger: 'blur'}
              ]">
              <span slot="label">
                {{ item.name }}
              </span>
              <a-input placeholder="" v-model="functionFrom[item.id]"/>
            </a-form-model-item>
          </template>
          <template
            v-else-if="item.valueType !== undefined && item.valueType !== null && item.valueType.type === 'enum'">
            <!--:label="item.name"-->
            <a-form-model-item
              :key="index"
              :prop="item.id"
              style="width: 100%"
              :rules="[]">
              <span slot="label">
                {{ item.name }}
              </span>
              <a-select placeholder="" size="large" v-model="functionFrom[item.id]" allow-clear>
                <a-select-option v-for="(d, index) in item.valueType.elements" :key="index"
                                 :value="d.value">{{ d.text }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </template>
        </template>
      </a-form-model>
      <!-- 在页面底部添加模拟上行按钮 -->
      <div style="margin-top: 32px;">
        <a-button :disabled="functionCopy==='event'&&eventSelect.length===0&&hexShow===false"
                  size="small" type="primary" @click="functonDown">
          模拟上行
        </a-button>
      </div>
    </a-col>
    <a-col :span="12">
      <div class="log-panel-title">
        <span>设备模拟日志</span>
        <a-button type="link" class="log-clear-btn" @click="clearSimLog">
          <iot-icon type="icon-u-del"/>
          <span>清空日志</span>
        </a-button>
      </div>
      <div class="log-area">
        <div
          v-for="(line, idx) in parsedSimLogLines"
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
        <div v-if="!parsedSimLogLines.length" class="log-empty">暂无日志</div>
      </div>
    </a-col>
  </a-row>
</template>

<script>
import JsonViewer from 'vue-json-viewer'
import {upValue} from '@/api/system/dev/instance'

export default {
  name: 'DeviceDebugging',
  props: {
    metaData: {
      type: Object,
      require: true,
      default: () => {
      }
    },
    productDetails: {
      type: Object,
      require: true,
      default: () => {
      }
    },
    show: {
      type: Boolean,
      default: false
    },
    deviceNode: {
      type: String,
      default: ''
    },
    device: {
      type: Object,
      require: true,
      default: () => {
      }
    }
  },
  components: {
    JsonViewer,
  },
  watch: {
    show(val) {
      this.open = val
      if (val === true) {
        this.initData()
      }
    }
  },
  data() {
    return {
      // 十六进制报文显示
      hexShow: false,
      // 配置、十六进制按钮显示
      configAndHexShow: false,
      // 配置按钮是否透明
      configTransparent: false,
      // 十六机制按钮是否透明
      hexTransparent: true,
      // message
      messageCopy: '',
      // message
      message: '',
      // 是否显示弹出层
      open: false,
      // 事件和属性选择框
      deviceDebuggingSelect: [
        {
          id: 'properties',
          name: '数据上报'
        },
        {
          id: 'event',
          name: '事件上报'
        }
      ],
      // 上行属性
      proOrEventParams: [],
      // 事件选择框
      eventSelect: [],
      // 上行属性copy
      proOrEventParamsCopy: [],
      // 事件选择框copy
      eventSelectCopy: [],
      // 事件copy
      functionCopy: undefined,
      // 下发表单
      functionFrom: {
        payload: undefined,
        function: undefined,
        event: undefined,
        level: undefined,
        eventName: undefined
      },
      // 模拟日志
      simLog: '',
      // 模拟日志copy
      simLogCopy: ''
    }
  },
  computed: {
    parsedSimLogLines() {
      if (!this.simLog) {
        return [];
      }
      const lines = this.simLog.split('\n').filter(Boolean);
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
        const actionMatch = opLine.match(/模拟\[(.*?)\]/) || opLine.match(/操作\[(.*?)\]/);
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
    // 清除日志
    clearMessage() {
      this.message = ''
      this.messageCopy = ''
    },
    // 功能下发
    functonDown() {
      console.log('this.functionFrom = ', this.functionFrom, this.productDetails,
        (this.productDetails.thirdPlatform !== 'ctwing'))
      this.$refs.functionForm.validate(valid => {
        if (valid) {
          console.log('this.functionFrom success = ', this.functionFrom)
          const formData = {}
          if (this.functionFrom.function === 'properties') {
            formData.time = new Date().getTime()
            formData.payload = this.functionFrom.payload
            formData.type = this.functionFrom.payload === undefined ? 'notHex' : 'hex'
            formData.iotId = this.device.iotId
            formData.deviceId = this.device.deviceId
            formData.deviceName = this.device.deviceName
            formData.deviceNode = this.deviceNode
            formData.productKey = this.device.productKey
            formData.messageType = 'PROPERTIES'
            formData.userUnionId = this.$store.state.user.name
            formData.source = 'simulation'
            const keyArray = Object.keys(this.functionFrom)
            const keyValue = Object.values(this.functionFrom)
            const data = {}
            for (let i = 0; i < keyArray.length; i++) {
              if (keyArray[i] === 'function' ||
                keyArray[i] === 'event' ||
                keyArray[i] === 'level' ||
                keyArray[i] === 'eventName' ||
                keyArray[i] === 'payload') {
                continue
              }
              data[keyArray[i]] = keyValue[i]
            }
            formData.properties = data
            console.log('formData properties success = ', formData)
          } else if (this.functionFrom.function === 'event') {
            formData.time = new Date().getTime()
            formData.payload = this.functionFrom.payload
            formData.type = this.functionFrom.payload === undefined ? 'notHex' : 'hex'
            formData.event = this.functionFrom.event
            formData.iotId = this.device.iotId
            formData.deviceId = this.device.deviceId
            formData.eventName = this.functionFrom.eventName
            formData.deviceName = this.device.deviceName
            formData.deviceNode = this.deviceNode
            formData.productKey = this.device.productKey
            formData.messageType = 'EVENT'
            formData.userUnionId = this.$store.state.user.name
            formData.source = 'simulation'
            const keyArray = Object.keys(this.functionFrom)
            const keyValue = Object.values(this.functionFrom)
            const data = {}
            for (let i = 0; i < keyArray.length; i++) {
              if (keyArray[i] === 'function' ||
                keyArray[i] === 'event' ||
                keyArray[i] === 'eventName' ||
                keyArray[i] === 'payload') {
                continue
              }
              data[keyArray[i]] = keyValue[i]
            }
            formData.data = data
            console.log('formData event success = ', formData)
          }

          upValue(this.device.productKey, formData).then(res => {
            console.log('res = ', res)
            if (this.hexShow) {
              if (this.simLogCopy === '') {
                this.simLogCopy = this.simLogCopy + '[' + this.formatDate(new Date())
                  + '] 数据上报: payload = ' + this.formData
                this.simLogCopy = this.simLogCopy + '\n' + '[' + this.formatDate(new Date())
                  + '] 上报成功'
              } else {
                this.simLogCopy = this.simLogCopy + '\n' + '[' + this.formatDate(new Date())
                  + '] 数据上报: payload = ' + this.formData
                this.simLogCopy = this.simLogCopy + '\n' + '[' + this.formatDate(new Date())
                  + '] 上报成功'
              }
              this.simLog = '\n' + this.simLogCopy + '\n'
            } else {
              if (this.simLogCopy === '') {
                this.simLogCopy = this.simLogCopy + '[' + this.formatDate(new Date())
                  + '] 数据上报: 业务数据上报 = ' + this.formData
                this.simLogCopy = this.simLogCopy + '\n' + '[' + this.formatDate(new Date())
                  + '] 上报成功'
              } else {
                this.simLogCopy = this.simLogCopy + '\n' + '[' + this.formatDate(new Date())
                  + '] 数据上报: 业务数据上报 = ' + this.formData
                this.simLogCopy = this.simLogCopy + '\n' + '[' + this.formatDate(new Date())
                  + '] 上报成功'
              }
              this.simLog = '\n' + this.simLogCopy + '\n'
            }
            console.log('this.message = ', this.message)
            this.$message.success(
              '模拟成功',
              3
            )
          }).catch(reason => {
            if (this.messageCopy === '') {
              this.messageCopy = this.messageCopy + '[' + this.formatDate(new Date()) + '] 数据上报: '
                + '上报失败'
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 上报结果: ' + reason
            } else {
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 数据上报: ' + '上报失败'
              this.messageCopy = this.messageCopy + '\n' + '[' + this.formatDate(new Date())
                + '] 上报结果: ' + reason
            }
            this.message = '\n' + this.messageCopy + '\n'
          })
        } else {
          return false
        }
      })
    },
    // 下发功能发生改变
    optionChange(value, option) {
      console.log('value event = ', value)
      console.log('option event = ', option)
      this.functionFrom = {
        payload: undefined,
        function: value,
        event: undefined,
        level: undefined,
        eventName: undefined
      }
      if (value !== undefined && value !== null) {
        this.functionCopy = value
        this.configTransparent = false
        this.hexTransparent = true
        this.hexShow = false
        this.configAndHexShow = true
        if (value === 'properties') {
          this.eventSelect = []
          this.proOrEventParams = this.metaData.properties
          if (this.proOrEventParams !== undefined && this.proOrEventParams !== null) {
            this.proOrEventParams.forEach(item => {
              if (item.id === 'deviceCode' ||
                item.id === 'deviceId' ||
                item.id === 'devEUI' ||
                item.id === 'meterNo' ||
                item.id === 'did') {
                this.functionFrom[item.id] = this.device.deviceId
              }
            })
          }
          this.proOrEventParamsCopy = this.proOrEventParams
        } else if (value === 'event') {
          this.proOrEventParams = []
          this.eventSelect = this.metaData.events
          if (this.eventSelect === undefined || this.eventSelect === null) {
            this.eventSelect = []
          }
          this.eventSelectCopy = this.eventSelect
        }
        console.log('proOrEventParams = ', this.proOrEventParams)
      } else {
        this.functionCopy = undefined
        this.configTransparent = false
        this.hexTransparent = true
        this.hexShow = false
        this.configAndHexShow = false
        this.proOrEventParams = []
        this.eventSelect = []
        this.functionFrom = {
          payload: undefined,
          function: undefined,
          event: undefined,
          level: undefined,
          eventName: undefined
        }
      }
      this.$refs.functionForm.validateField('function')
      this.$refs.functionForm.resetFields()
    },
    // 事件选择发生改变
    eventChange(value, option) {
      console.log('value event = ', value)
      console.log('option event = ', option)
      if (value !== undefined && value !== null) {
        this.proOrEventParams = this.metaData.properties
        for (let i = 0; i < this.eventSelect.length; i++) {
          if (this.eventSelect[i].id === value) {
            this.functionFrom.level = this.eventSelect[i].expands.level
            this.functionFrom.eventName = this.eventSelect[i].name
          }
        }
        console.log('proOrEventParams = ', this.proOrEventParams)
        console.log('this.functionFrom = ', this.functionFrom)
        this.proOrEventParams.forEach(item => {
          if (item.id === 'deviceCode' ||
            item.id === 'deviceId' ||
            item.id === 'devEUI' ||
            item.id === 'meterNo' ||
            item.id === 'did') {
            this.functionFrom[item.id] = this.device.deviceId
          }
        })
      } else {
        this.proOrEventParams = []
        this.functionFrom.event = undefined
        this.functionFrom.level = undefined
      }
    },
    // 初始化数据
    initData() {
      // console.log('deviceDetails = ', this.device)
      // console.log('metaData event = ', this.metaData)
      // console.log('metaData productKey = ', this.productKey)
      // console.log('metaData deviceNo = ', this.deviceNo)
      this.proOrEventParams = []
      this.eventSelect = []
      this.functionFrom = {
        payload: undefined,
        function: undefined,
        event: undefined,
        level: undefined,
        eventName: undefined
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
    // 配置模拟上行
    configClick() {
      this.functionFrom = {
        payload: undefined,
        function: this.functionCopy,
        event: undefined,
        level: undefined,
        eventName: undefined
      }
      if (this.functionCopy === 'properties') {
        this.eventSelect = []
        this.proOrEventParams = this.proOrEventParamsCopy
        if (this.proOrEventParams !== undefined && this.proOrEventParams !== null) {
          this.proOrEventParams.forEach(item => {
            if (item.id === 'deviceCode' ||
              item.id === 'deviceId' ||
              item.id === 'devEUI' ||
              item.id === 'meterNo' ||
              item.id === 'did') {
              this.functionFrom[item.id] = this.device.deviceId
            }
          })
        }
      } else if (this.functionCopy === 'event') {
        this.proOrEventParams = []
        this.eventSelect = this.eventSelectCopy
      }
      this.$refs.functionForm.resetFields()
      this.configTransparent = false
      this.hexTransparent = true
      this.hexShow = false
    },
    // 十六进制模拟上行
    hexClick() {
      this.proOrEventParams = []
      this.eventSelect = []
      this.functionFrom = {
        payload: undefined,
        function: this.functionCopy,
        event: undefined,
        level: undefined,
        eventName: undefined
      }
      this.$refs.functionForm.resetFields()
      this.configTransparent = true
      this.hexTransparent = false
      this.hexShow = true
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
    },
    // 清除模拟日志
    clearSimLog() {
      this.simLog = '';
    }
  }

}
</script>
<style scoped>
.operation-wrap {
  margin-top: 5px;
  padding: 10px;
  border-right: 2px dashed #a8aaaf;
  height: calc(100vh - 435px);
  overflow: auto;
}

.operation-content {
  margin-top: 0px;
  padding: 15px 20px;
  border-left: 2px dashed #a8aaaf;
  height: calc(100vh - 435px);
  overflow: auto;
}

.panel-title {
  margin-bottom: 10px;
  height: 36px;
  line-height: 36px;
  background: #f5f5f5;
  padding: 0 0 0 20px;
}

.log-panel-title {
  margin-bottom: 10px;
  height: 36px;
  line-height: 36px;
  background: #f5f5f5;
  padding: 0 0 0 20px;
}

.log-clear-btn {
  float: right;
  background-color: #f5f5f5;
}

.log-area {
  padding: 15px 20px;
  border-left: 2px dashed #a8aaaf;
  height: calc(100vh - 435px);
  overflow: auto;
}

.log-line {
  margin-bottom: 10px;
  padding: 8px 10px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
}

.log-success {
  background-color: #e1f3d8;
  border: 1px solid #a5d6a7;
}

.log-fail {
  background-color: #ffebee;
  border: 1px solid #ef9a9a;
}

.log-time {
  font-weight: bold;
  color: #555;
}

.log-action {
  margin-left: 10px;
  color: #1890ff;
}

.log-result {
  margin-left: 10px;
  color: #666;
}

.log-params-block {
  margin-top: 5px;
  padding-left: 20px;
  font-size: 13px;
  color: #333;
}

.log-params-label {
  font-weight: bold;
  color: #1890ff;
}

.log-params-content {
  margin-left: 5px;
  color: #555;
}

.log-result-block {
  margin-top: 5px;
  padding-left: 20px;
  font-size: 13px;
  color: #333;
}

.log-result-label {
  font-weight: bold;
  color: #1890ff;
}

.log-result-content {
  margin-left: 5px;
  color: #555;
}

.log-empty {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>
