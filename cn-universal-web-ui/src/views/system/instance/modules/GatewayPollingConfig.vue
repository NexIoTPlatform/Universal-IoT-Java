<template>
  <div class="gateway-polling-config">
    <a-card title="云端轮询配置" :bordered="false">
      
      <!-- 基础配置 -->
      <a-form-model
        ref="configForm"
        :model="pollingConfig"
        :label-col="{span: 6}"
        :wrapper-col="{span: 14}">
        
        <a-form-model-item label="启用轮询">
          <a-switch v-model="pollingConfig.enabled" @change="onEnabledChange" />
          <span class="tip">启用后将按设定间隔自动轮询网关设备</span>
        </a-form-model-item>
        
        <a-form-model-item label="轮询间隔" v-if="pollingConfig.enabled">
          <a-select v-model="pollingConfig.intervalSeconds" style="width: 200px">
            <a-select-option :value="30">30秒 (高频)</a-select-option>
            <a-select-option :value="60">60秒</a-select-option>
            <a-select-option :value="120">120秒 (推荐)</a-select-option>
            <a-select-option :value="300">300秒</a-select-option>
            <a-select-option :value="600">600秒 (低频)</a-select-option>
          </a-select>
          <span class="tip">⚠️ 请勿设置过于频繁，避免网关负载过高</span>
        </a-form-model-item>
        
        <a-form-model-item label="超时时间" v-if="pollingConfig.enabled">
          <a-input-number v-model="pollingConfig.timeoutSeconds" :min="5" :max="60" />
          <span> 秒</span>
        </a-form-model-item>
        
        <a-form-model-item label="重试次数" v-if="pollingConfig.enabled">
          <a-input-number v-model="pollingConfig.retryTimes" :min="0" :max="5" />
        </a-form-model-item>
        
        <a-form-model-item label="指令间隔" v-if="pollingConfig.enabled">
          <a-select v-model="pollingConfig.commandIntervalMs" style="width: 200px">
            <a-select-option :value="500">500毫秒 (高速)</a-select-option>
            <a-select-option :value="800">800毫秒</a-select-option>
            <a-select-option :value="1200">1200毫秒 (推荐)</a-select-option>
            <a-select-option :value="1800">1800毫秒</a-select-option>
            <a-select-option :value="2300">2300毫秒 (低速)</a-select-option>
          </a-select>
          <span class="tip">⚠️ 多条指令间的等待时间，防止Modbus设备缓冲区溢出</span>
        </a-form-model-item>
      
      </a-form-model>
      
      <!-- 轮询指令配置 -->
      <a-divider v-if="pollingConfig.enabled">轮询指令配置</a-divider>
      
      <div v-if="pollingConfig.enabled">
        <a-button type="primary" icon="plus" @click="addCommand" style="margin-bottom: 16px">
          添加轮询指令
        </a-button>
        
        <a-table 
          :columns="commandColumns" 
          :data-source="commandList" 
          :pagination="false"
          row-key="id"
          size="small">
          
          <template slot="order" slot-scope="text, record, index">
            <a-icon type="arrow-up" @click="moveUp(index)" v-if="index > 0" style="cursor: pointer; margin-right: 8px" />
            <a-icon type="arrow-down" @click="moveDown(index)" v-if="index < commandList.length - 1" style="cursor: pointer; margin-right: 8px" />
            <span>{{ index + 1 }}</span>
          </template>
          
          <template slot="commandHex" slot-scope="text">
            <a-tag color="blue" style="font-family: monospace">{{ text }}</a-tag>
          </template>
          
          <template slot="enabled" slot-scope="text">
            <a-badge :status="text ? 'success' : 'default'" :text="text ? '启用' : '禁用'" />
          </template>
          
          <template slot="action" slot-scope="text, record, index">
            <a @click="editCommand(index)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm title="确定删除?" @confirm="deleteCommand(index)">
              <a style="color: red">删除</a>
            </a-popconfirm>
          </template>
          
        </a-table>
      </div>
      
      <!-- 保存和测试按钮 -->
      <div style="text-align: center; margin-top: 24px">
        <a-button type="primary" @click="saveConfig" :loading="saving" style="margin-right: 12px">
          保存配置
        </a-button>
        <a-button 
          type="default" 
          @click="testPolling" 
          :loading="testing"
          :disabled="!pollingConfig.enabled || commandList.length === 0">
          <a-icon type="thunderbolt" />
          测试轮询
        </a-button>
        <div style="color: #999; font-size: 12px; margin-top: 8px;">
          💡 测试轮询将立即执行一次完整的轮询流程
        </div>
      </div>
      
    </a-card>
    
    <!-- 指令编辑弹窗 -->
    <a-modal 
      v-model="commandModalVisible" 
      title="配置轮询指令"
      width="800px"
      @ok="saveCommand"
      @cancel="cancelCommand">
      
      <a-form-model
        ref="commandForm"
        :model="currentCommand"
        :label-col="{span: 6}"
        :wrapper-col="{span: 16}">
        
        <a-form-model-item label="指令名称" required>
          <a-input v-model="currentCommand.commandName" placeholder="例如: 读取温湿度" />
        </a-form-model-item>
        
        <a-divider>Modbus指令配置</a-divider>
        
        <a-form-model-item label="功能码">
          <a-select v-model="modbusParams.functionCode" @change="generateCommandHex">
            <a-select-option :value="3">03 - 读保持寄存器</a-select-option>
            <a-select-option :value="4">04 - 读输入寄存器</a-select-option>
            <a-select-option :value="6">06 - 写单个寄存器</a-select-option>
          </a-select>
        </a-form-model-item>
        
        <a-form-model-item label="从站地址">
          <a-input-number v-model="modbusParams.slaveAddress" :min="1" :max="247" @change="generateCommandHex" />
          <span class="tip"> (1-247)</span>
        </a-form-model-item>
        
        <a-form-model-item label="寄存器地址">
          <a-input-number v-model="modbusParams.registerAddress" :min="0" :max="65535" @change="generateCommandHex" />
          <span class="tip"> (十进制)</span>
        </a-form-model-item>
        
        <a-form-model-item label="寄存器数量">
          <a-input-number v-model="modbusParams.registerCount" :min="1" :max="125" @change="generateCommandHex" />
        </a-form-model-item>
        
        <a-divider>生成的指令</a-divider>
        
        <a-form-model-item label="HEX指令">
          <a-input
            v-model="currentCommand.commandHex"
            readonly
            style="font-family: monospace; background: #f5f5f5">
            <a-icon slot="suffix" type="copy" @click="copyCommandHex" />
          </a-input>
          <div class="command-preview">
            <span v-for="(byte, index) in commandHexBytes" :key="index" class="hex-byte">
              {{ byte }}
            </span>
          </div>
        </a-form-model-item>
        
      </a-form-model>
      
    </a-modal>
    
  </div>
</template>

<script>
import { savePollingConfig, getPollingConfig, testPolling } from '@/api/system/gatewayPolling'

export default {
  name: 'GatewayPollingConfig',
  props: {
    deviceInfo: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      pollingConfig: {
        deviceId: '',
        productKey: '',
        enabled: false,
        intervalSeconds: 120,
        timeoutSeconds: 10,
        retryTimes: 3,
        commandIntervalMs: 300
      },
      commandList: [],
      commandModalVisible: false,
      currentCommand: {
        commandName: '',
        commandHex: '',
        commandType: 'MODBUS',
        enabled: true
      },
      currentCommandIndex: -1,
      modbusParams: {
        functionCode: 3,
        slaveAddress: 1,
        registerAddress: 0,
        registerCount: 1
      },
      saving: false,
      testing: false,
      commandColumns: [
        { title: '顺序', dataIndex: 'executionOrder', width: 100, scopedSlots: { customRender: 'order' } },
        { title: '指令名称', dataIndex: 'commandName' },
        { title: 'HEX指令', dataIndex: 'commandHex', scopedSlots: { customRender: 'commandHex' } },
        { title: '状态', dataIndex: 'enabled', width: 80, scopedSlots: { customRender: 'enabled' } },
        { title: '操作', width: 150, scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  computed: {
    commandHexBytes() {
      if (!this.currentCommand.commandHex) return []
      return this.currentCommand.commandHex.match(/.{1,2}/g) || []
    }
  },
  mounted() {
    if (this.deviceInfo && this.deviceInfo.deviceId) {
      this.pollingConfig.deviceId = this.deviceInfo.deviceId
      this.pollingConfig.productKey = this.deviceInfo.productKey
      this.loadConfig()
    }
  },
  methods: {
    // 加载配置
    async loadConfig() {
      try {
        const { data } = await getPollingConfig(this.deviceInfo.productKey, this.deviceInfo.deviceId)
        if (data) {
          // 处理后端返回的数据，支持下划线和驼峰两种格式
          this.pollingConfig = {
            deviceId: this.deviceInfo.deviceId,
            productKey: this.deviceInfo.productKey,
            enabled: data.enabled || false,
            intervalSeconds: data.intervalSeconds || data.interval_seconds || 120,
            timeoutSeconds: data.timeoutSeconds || data.timeout_seconds || 10,
            retryTimes: data.retryTimes || data.retry_times || 3,
            commandIntervalMs: data.commandIntervalMs || data.command_interval_ms || 300
          }
          this.commandList = data.commands || []
        }
      } catch (error) {
        console.error('加载配置失败', error)
      }
    },
    
    // 保存配置
    async saveConfig() {
      this.saving = true
      try {
        const data = {
          ...this.pollingConfig,
          commands: this.commandList
        }
        await savePollingConfig(data)
        this.$message.success('保存成功')
      } catch (error) {
        this.$message.error('保存失败: ' + error.message)
      } finally {
        this.saving = false
      }
    },
    
    // 添加指令
    addCommand() {
      this.currentCommand = {
        commandName: '',
        commandHex: '',
        commandType: 'MODBUS',
        enabled: true
      }
      this.modbusParams = {
        functionCode: 3,
        slaveAddress: 1,
        registerAddress: 0,
        registerCount: 1
      }
      this.currentCommandIndex = -1
      this.generateCommandHex()
      this.commandModalVisible = true
    },
    
    // 编辑指令
    editCommand(index) {
      const cmd = this.commandList[index]
      this.currentCommand = { ...cmd }
      this.currentCommandIndex = index
      
      // 如果有protocolParams，解析出来
      if (cmd.protocolParams) {
        try {
          this.modbusParams = JSON.parse(cmd.protocolParams)
        } catch (e) {
          console.error('解析协议参数失败', e)
        }
      }
      
      this.commandModalVisible = true
    },
    
    // 保存指令
    saveCommand() {
      if (!this.currentCommand.commandName) {
        this.$message.warning('请输入指令名称')
        return
      }
      
      if (!this.currentCommand.commandHex) {
        this.$message.warning('请生成指令')
        return
      }
      
      // 保存协议参数
      this.currentCommand.protocolParams = JSON.stringify(this.modbusParams)
      
      if (this.currentCommandIndex >= 0) {
        // 更新
        this.$set(this.commandList, this.currentCommandIndex, { ...this.currentCommand })
      } else {
        // 新增
        this.currentCommand.executionOrder = this.commandList.length
        this.commandList.push({ ...this.currentCommand })
      }
      
      this.commandModalVisible = false
    },
    
    // 取消编辑
    cancelCommand() {
      this.commandModalVisible = false
    },
    
    // 删除指令
    deleteCommand(index) {
      this.commandList.splice(index, 1)
      // 重新设置顺序
      this.commandList.forEach((cmd, idx) => {
        cmd.executionOrder = idx
      })
    },
    
    // 上移
    moveUp(index) {
      if (index > 0) {
        const temp = this.commandList[index]
        this.$set(this.commandList, index, this.commandList[index - 1])
        this.$set(this.commandList, index - 1, temp)
        // 更新顺序
        this.commandList.forEach((cmd, idx) => {
          cmd.executionOrder = idx
        })
      }
    },
    
    // 下移
    moveDown(index) {
      if (index < this.commandList.length - 1) {
        const temp = this.commandList[index]
        this.$set(this.commandList, index, this.commandList[index + 1])
        this.$set(this.commandList, index + 1, temp)
        // 更新顺序
        this.commandList.forEach((cmd, idx) => {
          cmd.executionOrder = idx
        })
      }
    },
    
    // 生成Modbus HEX指令
    generateCommandHex() {
      const { functionCode, slaveAddress, registerAddress, registerCount } = this.modbusParams
      
      // 构建Modbus RTU指令
      const bytes = [
        slaveAddress,
        functionCode,
        (registerAddress >> 8) & 0xFF,
        registerAddress & 0xFF,
        (registerCount >> 8) & 0xFF,
        registerCount & 0xFF
      ]
      
      // 计算CRC16
      const crc = this.calculateModbusCRC(bytes)
      bytes.push(crc & 0xFF, (crc >> 8) & 0xFF)
      
      // 转换为HEX字符串
      this.currentCommand.commandHex = bytes
        .map(b => b.toString(16).toUpperCase().padStart(2, '0'))
        .join('')
    },
    
    // 计算Modbus CRC16
    calculateModbusCRC(data) {
      let crc = 0xFFFF
      for (let i = 0; i < data.length; i++) {
        crc ^= data[i]
        for (let j = 0; j < 8; j++) {
          if (crc & 0x0001) {
            crc = (crc >> 1) ^ 0xA001
          } else {
            crc >>= 1
          }
        }
      }
      return crc
    },
    
    // 复制指令
    copyCommandHex() {
      const input = document.createElement('input')
      input.value = this.currentCommand.commandHex
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$message.success('已复制到剪贴板')
    },
    
    // 启用状态改变
    onEnabledChange(value) {
      if (!value) {
        this.commandList = []
      }
    },
    
    // 测试轮询
    async testPolling() {
      // 检查是否有启用的指令
      const enabledCommands = this.commandList.filter(cmd => cmd.enabled !== false)
      if (enabledCommands.length === 0) {
        this.$message.warning('请至少添加一条启用的轮询指令')
        return
      }
      
      this.testing = true
      try {
        const { code, msg } = await testPolling(this.deviceInfo.productKey, this.deviceInfo.deviceId)
        if (code === 0) {
          this.$message.success(msg || '测试轮询执行成功，请查看设备日志')
        } else {
          this.$message.error(msg || '测试轮询失败')
        }
      } catch (error) {
        console.error('测试轮询失败', error)
        this.$message.error('测试轮询失败: ' + (error.message || '未知错误'))
      } finally {
        this.testing = false
      }
    }
  }
}
</script>

<style scoped>
.command-preview {
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-family: monospace;
}
.hex-byte {
  display: inline-block;
  margin: 2px;
  padding: 4px 8px;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 2px;
  font-size: 12px;
}
.tip {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}
</style>
