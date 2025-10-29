/**
 * 设备配置信息解析工具
 * 统一管理设备配置信息的解析和显示
 */

/**
 * 字段自动填充策略
 * 定义哪些字段需要自动填充，以及填充的优先级和来源
 */
const AUTO_FILL_FIELD_CONFIG = {
  // 设备标识类字段 - 优先使用 deviceNo
  deviceId: {
    priority: 1,
    source: 'deviceNo',
    fallback: null
  },
  childDeviceId: {
    priority: 1,
    source: 'deviceNo',
    fallback: null
  },
  deviceCode: {
    priority: 1,
    source: 'deviceNo',
    fallback: null
  },
  
  // 配置信息类字段 - 优先从配置中读取，否则使用 deviceNo
  meterNo: {
    priority: 2,
    source: 'config',
    fallback: 'deviceNo'
  },
  imei: {
    priority: 2,
    source: 'config',
    fallback: 'deviceNo'
  },
  imsi: {
    priority: 2,
    source: 'config',
    fallback: 'deviceNo'
  },
  iccid: {
    priority: 2,
    source: 'config',
    fallback: 'deviceNo'
  },
  slaveAddress: {
    priority: 2,
    source: 'config',
    fallback: null
  },
  serialNo: {
    priority: 2,
    source: 'config',
    fallback: null
  }
}

/**
 * 配置字段映射表
 * 定义哪些配置字段需要显示,以及显示的中文名称
 */
const CONFIG_FIELD_MAPPING = {
  // Modbus 相关配置
  slaveAddress: '从站地址',
  protocolType: '协议类型',

  // 通信相关配置
  iccid: 'ICCID',
  imei: 'IMEI',
  imsi: 'IMSI',

  // 设备标识相关
  meterNo: '表号',
  serialNo: '序列号',
  deviceNo: '设备编号',

  // 版本信息
  version: '版本',
  firmwareVersion: '固件版本',
  hardwareVersion: '硬件版本',

  // 网络配置
  ip: 'IP地址',
  port: '端口',
  mac: 'MAC地址',

  // 其他配置
  location: '位置',
  description: '描述',
  manufacturer: '制造商',
  model: '型号',

  // 网关相关
  gwDeviceId: '网关设备ID',
  gwProductKey: '网关产品Key',

  // 子设备相关
  parentId: '父设备ID',
  subDeviceType: '子设备类型',

  // 传感器相关
  sensorType: '传感器类型',
  measurementRange: '测量范围',
  accuracy: '精度',

  // 电源相关
  batteryLevel: '电池电量',
  powerMode: '电源模式',

  // 时间相关
  timezone: '时区',
  syncTime: '同步时间'
}

/**
 * 获取配置信息的中文显示名称
 * @param {string} fieldName 配置字段名
 * @returns {string} 中文显示名称
 */
export function getConfigFieldDisplayName(fieldName) {
  return CONFIG_FIELD_MAPPING[fieldName] || fieldName
}

/**
 * 解析设备配置信息
 * @param {string} configStr 配置字符串（JSON格式）
 * @param {Array} includeFields 需要包含的字段列表，为空则包含所有映射的字段
 * @param {Array} excludeFields 需要排除的字段列表
 * @returns {Object|null} 解析后的配置信息对象，解析失败返回null
 */
export function parseDeviceConfig(configStr, includeFields = [], excludeFields = []) {
  if (!configStr) {
    return null
  }

  try {
    const config = JSON.parse(configStr)

    // 如果没有指定包含字段，则使用所有映射的字段
    const fieldsToCheck = includeFields.length > 0
      ? includeFields
      : Object.keys(CONFIG_FIELD_MAPPING)

    // 构建配置信息对象
    const keyConfigs = {}

    fieldsToCheck.forEach(fieldName => {
      // 跳过排除的字段
      if (excludeFields.includes(fieldName)) {
        return
      }

      // 检查配置中是否存在该字段且有值
      if (config[fieldName] !== undefined && config[fieldName] !== null && config[fieldName] !== '') {
        const displayName = getConfigFieldDisplayName(fieldName)
        keyConfigs[displayName] = config[fieldName]
      }
    })

    // 如果没有任何配置信息，返回null
    return Object.keys(keyConfigs).length > 0 ? keyConfigs : null

  } catch (e) {
    console.warn('解析设备配置失败:', e)
    return null
  }
}

/**
 * 获取设备配置信息的简化版本（只显示最重要的字段）
 * @param {string} configStr 配置字符串
 * @returns {Object|null} 简化的配置信息
 */
export function getSimplifiedDeviceConfig(configStr) {
  const importantFields = [
    'slaveAddress',    // Modbus从站地址
    'iccid',          // ICCID
    'imei',           // IMEI
    'meterNo',        // 表号
    'version',        // 版本
    'ip',             // IP地址
    'mac'             // MAC地址
  ]

  return parseDeviceConfig(configStr, importantFields)
}

/**
 * 获取Modbus设备配置信息
 * @param {string} configStr 配置字符串
 * @returns {Object|null} Modbus相关配置信息
 */
export function getModbusDeviceConfig(configStr) {
  const modbusFields = [
    'slaveAddress',    // 从站地址
    'protocolType',    // 协议类型
    'version',         // 版本
    'meterNo'          // 表号
  ]

  return parseDeviceConfig(configStr, modbusFields)
}

/**
 * 获取网关设备配置信息
 * @param {string} configStr 配置字符串
 * @returns {Object|null} 网关相关配置信息
 */
export function getGatewayDeviceConfig(configStr) {
  const gatewayFields = [
    'ip',              // IP地址
    'port',            // 端口
    'mac',             // MAC地址
    'version',         // 版本
    'subDeviceType'    // 子设备类型
  ]

  return parseDeviceConfig(configStr, gatewayFields)
}

/**
 * 获取子设备配置信息
 * @param {string} configStr 配置字符串
 * @returns {Object|null} 子设备相关配置信息
 */
export function getSubDeviceConfig(configStr) {
  const subDeviceFields = [
    'slaveAddress',    // 从站地址
    'parentId',        // 父设备ID
    'gwDeviceId',      // 网关设备ID
    'subDeviceType',   // 子设备类型
    'version'          // 版本
  ]

  return parseDeviceConfig(configStr, subDeviceFields)
}

/**
 * 根据设备类型获取相应的配置信息
 * @param {string} configStr 配置字符串
 * @param {string} deviceNode 设备节点类型
 * @returns {Object|null} 设备配置信息
 */
export function getDeviceConfigByType(configStr, deviceNode) {
  switch (deviceNode) {
    case 'GATEWAY':
      return getGatewayDeviceConfig(configStr)
    case 'GATEWAY_SUB_DEVICE':
      return getSubDeviceConfig(configStr)
    case 'DEVICE':
    default:
      return getSimplifiedDeviceConfig(configStr)
  }
}

/**
 * 检查配置字符串是否有效
 * @param {string} configStr 配置字符串
 * @returns {boolean} 是否有效
 */
export function isValidConfig(configStr) {
  if (!configStr) {
    return false
  }

  try {
    const config = JSON.parse(configStr)
    return typeof config === 'object' && config !== null
  } catch (e) {
    return false
  }
}

/**
 * 获取配置字段的完整映射表
 * @returns {Object} 字段映射表
 */
export function getConfigFieldMapping() {
  return {...CONFIG_FIELD_MAPPING}
}

/**
 * 添加自定义配置字段映射
 * @param {Object} customMapping 自定义映射
 */
export function addCustomConfigMapping(customMapping) {
  Object.assign(CONFIG_FIELD_MAPPING, customMapping)
}

/**
 * 获取自动填充字段配置
 * @returns {Object} 自动填充字段配置
 */
export function getAutoFillFieldConfig() {
  return { ...AUTO_FILL_FIELD_CONFIG }
}

/**
 * 添加自定义自动填充字段配置
 * @param {Object} customConfig 自定义配置
 */
export function addCustomAutoFillConfig(customConfig) {
  Object.assign(AUTO_FILL_FIELD_CONFIG, customConfig)
}

/**
 * 检查字段是否需要自动填充
 * @param {string} fieldName 字段名
 * @returns {boolean} 是否需要自动填充
 */
export function isAutoFillField(fieldName) {
  return AUTO_FILL_FIELD_CONFIG.hasOwnProperty(fieldName)
}

/**
 * 获取字段的自动填充值
 * @param {string} fieldName 字段名
 * @param {string} deviceNo 设备编号
 * @param {string} configStr 配置字符串（JSON格式）
 * @returns {string|null} 自动填充的值
 */
export function getAutoFillValue(fieldName, deviceNo, configStr) {
  const fieldConfig = AUTO_FILL_FIELD_CONFIG[fieldName]
  if (!fieldConfig) {
    return null
  }

  // 如果数据源是 deviceNo
  if (fieldConfig.source === 'deviceNo') {
    return deviceNo || null
  }

  // 如果数据源是 config
  if (fieldConfig.source === 'config') {
    if (!configStr) {
      return fieldConfig.fallback === 'deviceNo' ? deviceNo : null
    }

    try {
      const config = JSON.parse(configStr)
      const value = config[fieldName]
      
      // 如果配置中有值，则返回配置值
      if (value !== undefined && value !== null && value !== '') {
        return value
      }
      
      // 否则使用回退策略
      return fieldConfig.fallback === 'deviceNo' ? deviceNo : null
    } catch (e) {
      console.warn(`解析配置失败，字段: ${fieldName}`, e)
      return fieldConfig.fallback === 'deviceNo' ? deviceNo : null
    }
  }

  return null
}

/**
 * 批量获取多个字段的自动填充值
 * @param {Array<string>} fieldNames 字段名数组
 * @param {string} deviceNo 设备编号
 * @param {string} configStr 配置字符串（JSON格式）
 * @returns {Object} 字段名与值的映射对象
 */
export function getAutoFillValues(fieldNames, deviceNo, configStr) {
  const result = {}
  
  fieldNames.forEach(fieldName => {
    const value = getAutoFillValue(fieldName, deviceNo, configStr)
    if (value !== null) {
      result[fieldName] = value
    }
  })
  
  return result
}

/**
 * 从参数列表中自动填充字段值
 * @param {Array} params 参数列表（每项包含 id 属性）
 * @param {string} deviceNo 设备编号
 * @param {string} configStr 配置字符串（JSON格式）
 * @returns {Object} 填充后的字段值对象
 */
export function autoFillFromParams(params, deviceNo, configStr) {
  if (!Array.isArray(params)) {
    return {}
  }

  const result = {}
  
  params.forEach(param => {
    if (param.id && isAutoFillField(param.id)) {
      const value = getAutoFillValue(param.id, deviceNo, configStr)
      if (value !== null) {
        result[param.id] = value
      }
    }
  })
  
  return result
}

export default {
  parseDeviceConfig,
  getSimplifiedDeviceConfig,
  getModbusDeviceConfig,
  getGatewayDeviceConfig,
  getSubDeviceConfig,
  getDeviceConfigByType,
  getConfigFieldDisplayName,
  isValidConfig,
  getConfigFieldMapping,
  addCustomConfigMapping,
  getAutoFillFieldConfig,
  addCustomAutoFillConfig,
  isAutoFillField,
  getAutoFillValue,
  getAutoFillValues,
  autoFillFromParams
}
