# 设备配置信息解析工具使用说明

## 概述

`deviceConfig.js` 是一个统一的设备配置信息解析工具，用于解析和显示设备配置信息。它提供了多种方法来处理不同类型的设备配置。

## 主要功能

### 1. 基础配置解析

```javascript
import { parseDeviceConfig } from '@/utils/deviceConfig'

// 解析所有配置信息
const config = parseDeviceConfig(configStr)

// 只解析指定字段
const config = parseDeviceConfig(configStr, ['slaveAddress', 'version'])

// 排除某些字段
const config = parseDeviceConfig(configStr, [], ['imei'])
```

### 2. 按设备类型解析

```javascript
import { getDeviceConfigByType } from '@/utils/deviceConfig'

// 根据设备类型自动选择解析方式
const config = getDeviceConfigByType(configStr, 'GATEWAY_SUB_DEVICE')
```

### 3. 专用解析方法

```javascript
import { 
  getModbusDeviceConfig,
  getGatewayDeviceConfig,
  getSubDeviceConfig,
  getSimplifiedDeviceConfig
} from '@/utils/deviceConfig'

// Modbus设备配置
const modbusConfig = getModbusDeviceConfig(configStr)

// 网关设备配置
const gatewayConfig = getGatewayDeviceConfig(configStr)

// 子设备配置
const subConfig = getSubDeviceConfig(configStr)

// 简化配置（只显示重要字段）
const simpleConfig = getSimplifiedDeviceConfig(configStr)
```

## 在Vue组件中使用

### 1. 在设备列表中使用

```vue
<template>
  <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
    <a-popover placement="rightTop" trigger="hover" :title="'设备配置信息'"
               v-if="getConfigInfo(record.configuration, record.deviceNode)">
      <template slot="content">
        <div class="config-popover">
          <div v-for="(value, key) in getConfigInfo(record.configuration, record.deviceNode)" :key="key"
               class="config-popover-item">
            <span class="config-popover-key">{{ key }}:</span>
            <span class="config-popover-value">{{ value }}</span>
          </div>
        </div>
      </template>
      <!-- 设备信息内容 -->
    </a-popover>
  </div>
</template>

<script>
import { getDeviceConfigByType } from '@/utils/deviceConfig'

export default {
  methods: {
    getConfigInfo(configStr, deviceNode) {
      return getDeviceConfigByType(configStr, deviceNode)
    }
  }
}
</script>
```

### 2. 在Modbus子设备列表中使用

```vue
<script>
import { getModbusDeviceConfig } from '@/utils/deviceConfig'

export default {
  methods: {
    getConfigInfo(configStr) {
      return getModbusDeviceConfig(configStr)
    }
  }
}
</script>
```

## 配置字段映射

工具内置了常用的配置字段映射，包括：

- **Modbus相关**: `slaveAddress` (从站地址), `protocolType` (协议类型)
- **通信相关**: `iccid` (ICCID), `imei` (IMEI), `imsi` (IMSI)
- **设备标识**: `meterNo` (表号), `serialNo` (序列号), `deviceNo` (设备编号)
- **版本信息**: `version` (版本), `firmwareVersion` (固件版本)
- **网络配置**: `ip` (IP地址), `port` (端口), `mac` (MAC地址)
- **其他配置**: `location` (位置), `description` (描述), `manufacturer` (制造商)

## 自定义配置字段

```javascript
import { addCustomConfigMapping } from '@/utils/deviceConfig'

// 添加自定义字段映射
addCustomConfigMapping({
  customField1: '自定义字段1',
  customField2: '自定义字段2'
})
```

## 工具方法

```javascript
import { 
  getConfigFieldDisplayName,
  isValidConfig,
  getConfigFieldMapping
} from '@/utils/deviceConfig'

// 获取字段显示名称
const displayName = getConfigFieldDisplayName('slaveAddress') // '从站地址'

// 检查配置是否有效
const isValid = isValidConfig('{"slaveAddress": "1"}') // true

// 获取完整映射表
const mapping = getConfigFieldMapping()
```

## 注意事项

1. 所有方法都会自动处理JSON解析错误，返回null而不是抛出异常
2. 空值、null、undefined的字段会被自动过滤
3. 可以根据需要选择不同的解析方法
4. 支持自定义字段映射，便于扩展

## 更新记录

- v1.0.0: 初始版本，支持基础配置解析
- v1.1.0: 添加按设备类型解析功能
- v1.2.0: 添加自定义字段映射功能
