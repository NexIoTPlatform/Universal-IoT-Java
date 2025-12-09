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
        <div v-for="(log, idx) in logList" :key="idx" class="log-item">
          <div class="log-header" @click="toggleLog(idx)">
            <a-icon :type="log.collapsed ? 'down' : 'up'" class="log-collapse-icon" />
            <span class="log-time">{{ log.time }}</span>
            <span class="log-action">{{ log.action }}</span>
          </div>
          <div v-show="!log.collapsed">
            <div v-if="log.params" class="log-block log-block-params">
              <div class="log-label log-label-params">请求参数</div>
              <div class="log-json-text log-json-params">{{ formatJsonString(log.params) }}</div>
            </div>
            <div v-if="log.response" class="log-block log-block-response">
              <div class="log-label log-label-response">响应结果</div>
              <!-- 如果响应包含图片URL -->
              <div v-if="log.response.imgUrl && log.response.url" class="log-image-preview">
                <div class="image-wrapper">
                  <img 
                    v-if="!log.imageError"
                    :src="log.imageSrc || log.response.url" 
                    alt="响应图片" 
                    @error="handleImageError($event, idx)" 
                    @load="handleImageLoad($event, idx)" 
                  />
                  <div v-if="log.imageLoading" class="image-loading">
                    <a-spin size="small" /> 图片处理中，请稍候...
                  </div>
                  <div v-if="!log.imageError && !log.imageLoading" class="image-actions">
                    <a-button type="link" size="small" @click.stop="downloadFile(log.response.url)">
                      <a-icon type="download" /> 下载图片
                    </a-button>
                  </div>
                  <div v-if="log.imageError" class="image-error">
                    <a-icon type="exclamation-circle" /> 图片加载失败，后台处理中，请稍后刷新
                  </div>
                </div>
              </div>
              <!-- 如果响应包含文件URL -->
              <div v-else-if="log.response.fileUrl" class="log-file-download">
                <a-button type="primary" size="small" @click.stop="downloadFile(log.response.url || log.response.fileUrl)">
                  <a-icon type="download" /> 下载文件
                </a-button>
                <span class="file-url">{{ log.response.url || log.response.fileUrl }}</span>
              </div>
              <!-- 普通文本显示 -->
              <div v-else>
                <div class="log-json-text log-json-response">{{ formatJsonString(log.response) }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="!logList.length" class="log-empty">暂无日志</div>
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
    // 日志列表（使用新的数据结构，不再解析字符串）
    logList() {
      return this.logs || []
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
      // 日志列表（新的数据结构）
      logs: [],
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
      this.logs = []
    },
    
    // 过滤掉 null 值的辅助函数
    removeNullValues(obj) {
      if (obj === null || obj === undefined) {
        return obj
      }
      if (Array.isArray(obj)) {
        return obj.map(item => this.removeNullValues(item))
      }
      if (typeof obj === 'object') {
        const result = {}
        for (const key in obj) {
          if (obj.hasOwnProperty(key) && obj[key] !== null) {
            result[key] = this.removeNullValues(obj[key])
          }
        }
        return result
      }
      return obj
    },
    
    // 简化参数显示，只保留 function 对象
    simplifyParams(params) {
      if (!params || typeof params !== 'object') {
        return params
      }
      // 如果参数有 function 字段，只返回 function 对象
      if (params.function && typeof params.function === 'object') {
        return this.removeNullValues(params.function)
      }
      return this.removeNullValues(params)
    },
    
    // 处理响应，检查是否需要显示图片或下载文件
    processResponse(response) {
      if (!response || typeof response !== 'object') {
        return response
      }
      
      const processed = this.removeNullValues(response)
      
      // 不自动打开或下载，只在界面上显示预览和下载按钮
      return processed
    },
    
    // 下载文件
    downloadFile(url) {
      if (!url) return
      
      const link = document.createElement('a')
      link.href = url
      link.download = ''
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
    
    // 在新标签页打开图片
    openImageInNewTab(url) {
      if (!url) return
      window.open(url, '_blank')
    },
    
    // 图片加载错误处理（带重试机制）
    handleImageError(event, index) {
      const log = this.logs[index]
      if (!log || !log.response || !log.response.url) return
      
      // 如果重试次数未达到上限，延迟重试
      const retryCount = log.imageRetryCount || 0
      const maxRetries = 5 // 最多重试5次
      const retryDelay = (retryCount + 1) * 2000 // 递增延迟：2s, 4s, 6s, 8s, 10s
      
      if (retryCount < maxRetries) {
        // 设置重试次数和加载状态
        this.$set(log, 'imageRetryCount', retryCount + 1)
        this.$set(log, 'imageLoading', true)
        this.$set(log, 'imageError', false)
        
        // 延迟重试，添加时间戳避免缓存
        setTimeout(() => {
          if (this.logs[index] && this.logs[index].response && this.logs[index].response.url) {
            const url = this.logs[index].response.url
            const separator = url.includes('?') ? '&' : '?'
            this.$set(this.logs[index], 'imageSrc', `${url}${separator}_t=${Date.now()}`)
          }
        }, retryDelay)
      } else {
        // 超过重试次数，显示错误
        this.$set(log, 'imageError', true)
        this.$set(log, 'imageLoading', false)
        if (event.target) {
          event.target.style.display = 'none'
        }
      }
    },
    
    // 图片加载成功
    handleImageLoad(event, index) {
      const log = this.logs[index]
      if (log) {
        this.$set(log, 'imageError', false)
        this.$set(log, 'imageLoading', false)
      }
    },
    
    // 添加日志条目（新消息放在前面）
    addLog(action, params, response) {
      // 简化参数显示，只保留 function 对象
      const simplifiedParams = this.simplifyParams(params)
      
      // 处理响应，检查图片和文件
      const processedResponse = this.processResponse(response)
      
      // 将其他日志折叠，新日志展开
      this.logs.forEach(log => {
        log.collapsed = true
      })
      
      // 使用 unshift 将新消息添加到数组前面，默认展开
      const logEntry = {
        time: this.formatDate(new Date()),
        action: action,
        params: simplifiedParams,
        response: processedResponse,
        collapsed: false,  // 最新消息默认展开
        imageError: false,  // 图片加载错误标志
        imageLoading: false,  // 图片加载中标志
        imageRetryCount: 0,  // 图片重试次数
        imageSrc: null  // 图片源（带时间戳避免缓存）
      }
      
      // 如果响应包含图片URL，延迟加载图片（给后台处理时间）
      if (processedResponse && processedResponse.imgUrl && processedResponse.url) {
        logEntry.imageLoading = true
        // 延迟1秒后开始加载图片，给后台处理时间
        setTimeout(() => {
          const logIndex = this.logs.findIndex(l => l === logEntry)
          if (logIndex !== -1 && this.logs[logIndex]) {
            const url = this.logs[logIndex].response.url
            const separator = url.includes('?') ? '&' : '?'
            this.$set(this.logs[logIndex], 'imageSrc', `${url}${separator}_t=${Date.now()}`)
          }
        }, 1000)
      }
      
      this.logs.unshift(logEntry)
      
      // 自动滚动到顶部（因为新消息在前面）
      this.$nextTick(() => {
        const logContent = this.$el?.querySelector('.log-content')
        if (logContent) {
          logContent.scrollTop = 0
        }
      })
    },
    
    // 切换日志折叠状态
    toggleLog(index) {
      if (this.logs[index]) {
        this.logs[index].collapsed = !this.logs[index].collapsed
      }
    },
    
    // 格式化JSON为压缩字符串
    formatJsonString(obj) {
      if (!obj) return ''
      try {
        return JSON.stringify(obj)
      } catch (e) {
        return String(obj)
      }
    },
    
    // 构建请求数据
    buildRequestData() {
      const keyArray = Object.keys(this.functionFrom)
      const keyValue = Object.values(this.functionFrom)
      const data = {}
      
      // 提取参数数据（排除 function 字段）
      for (let i = 0; i < keyArray.length; i++) {
        if (keyArray[i] === 'function') {
          continue
        }
        data[keyArray[i]] = keyValue[i]
      }
      
      // 构建请求数据
      const formData = {
        appUnionId: this.$store.state.user.name,
        productKey: this.productKey,
        deviceId: this.deviceNo,
        cmd: 'DEV_FUNCTION',
        function: {
          messageType: 'FUNCTIONS',
          serviceType: this.serviceType,
          function: this.functionFrom.function,
          data: data
        }
      }
      
      // 获取功能名称和 source
      let functionName = ''
      for (let i = 0; i < this.metaData.functions.length; i++) {
        if (this.metaData.functions[i].id === this.functionFrom.function) {
          formData.function.source = this.metaData.functions[i].source
          functionName = this.metaData.functions[i].name
          break
        }
      }
      
      return {
        formData,
        functionName,
        params: {
          deviceId: this.deviceNo,
          cmd: 'DEV_FUNCTION',
          function: formData.function
        }
      }
    },
    // 功能下发
    functonDown() {
      this.$refs.functionForm.validate(valid => {
        if (!valid) {
          this.$message.error('请完善必填项')
          return false
        }
        
        this.submitting = true
        
        try {
          // 构建请求数据
          const { formData, functionName, params } = this.buildRequestData()
          
          // 发送请求
          functionDown(this.productKey, formData)
            .then(res => {
              // 添加日志
              this.addLog(
                `${functionName}`,
                params,
                res.data || res
              )
              this.$message.success('下发成功', 3)
              this.submitting = false
            })
            .catch(reason => {
              // 处理错误响应
              let errorResponse = null
              
              if (typeof reason === 'string') {
                errorResponse = { error: reason }
              } else if (reason?.response?.data) {
                errorResponse = reason.response.data
              } else if (reason?.data) {
                errorResponse = reason.data
              } else {
                errorResponse = { error: reason?.message || reason?.toString() || '下发失败' }
              }
              
              const { functionName, params } = this.buildRequestData()
              
              // 添加日志
              this.addLog(
                `${functionName}`,
                params,
                errorResponse
              )
              
              this.$message.error('下发失败', 3)
              this.submitting = false
            })
        } catch (error) {
          console.error('构建请求数据失败:', error)
          this.$message.error('构建请求数据失败')
          this.submitting = false
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
      
      // 重置表单，保留功能选择
      this.functionFrom = {
        function: value
      }
      
      if (value !== undefined && value !== null) {
        this.metaData.functions.forEach(item => {
          if (item.id === value) {
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
  padding: 12px 16px;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.4;
  background: #fff;
}

.log-item {
  margin-bottom: 0;
  padding: 4px 0;
  border-bottom: 1px solid #e8e8e8;
}

.log-item:last-child {
  border-bottom: none;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
  cursor: pointer;
  user-select: none;
  line-height: 1.2;
}

.log-collapse-icon {
  font-size: 10px;
  color: #8c8c8c;
  flex-shrink: 0;
  margin-right: 0;
}

.log-time {
  color: #8c8c8c;
  font-size: 10px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  flex-shrink: 0;
  min-width: 150px;
  white-space: nowrap;
  font-weight: normal;
}

.log-action {
  color: #262626;
  font-size: 11px;
  flex: 1;
  margin-left: 0;
  font-weight: normal;
}

.log-block {
  margin-top: 4px;
}

.log-block:first-of-type {
  margin-top: 0;
}

.log-label {
  font-size: 12px;
  color: #595959;
  margin-bottom: 2px;
  font-weight: normal;
}

.log-json-text {
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  color: #333;
  padding: 6px 10px;
  border-radius: 2px;
  word-break: break-all;
  line-height: 1.4;
  white-space: pre-wrap;
}

/* 参数样式 - 蓝色系 */
.log-label-params {
  color: #1890ff;
}

.log-json-params {
  background: #e6f7ff;
}

/* 结果样式 - 绿色系 */
.log-label-response {
  color: #52c41a;
}

.log-json-response {
  background: #f6ffed;
}

.log-block ::v-deep .jv-container {
  margin: 0;
}

.log-block ::v-deep .jv-container .jv-code {
  padding: 4px 8px;
  font-size: 12px;
  line-height: 1.4;
}

.log-empty {
  color: #bbb;
  text-align: center;
  padding: 40px 0;
  font-size: 14px;
}

.log-image-preview {
  margin-top: 4px;
}

.image-wrapper {
  position: relative;
  display: inline-block;
  max-width: 100%;
}

.image-wrapper img {
  max-width: 500px;
  max-height: 400px;
  display: block;
}

.image-actions {
  margin-top: 4px;
  display: flex;
  gap: 16px;
}

.image-actions .ant-btn-link {
  padding: 0;
  height: auto;
  font-size: 12px;
}

.image-loading {
  padding: 20px;
  text-align: center;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.image-error {
  padding: 20px;
  text-align: center;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.log-file-download {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-url {
  flex: 1;
  font-size: 12px;
  color: #595959;
  word-break: break-all;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}


@media (max-width: 1200px) {
  .function-down-container {
    gap: 16px;
  }
}
</style>
