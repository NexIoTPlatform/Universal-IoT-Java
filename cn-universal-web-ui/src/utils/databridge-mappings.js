/**
 * 数据桥接相关映射关系统一管理
 * 包含资源类型、插件类型、数据方向等的显示名称、图标、颜色等映射
 */

// 资源类型映射
export const RESOURCE_TYPE_MAPPINGS = {
  // 数据库类型
  MYSQL: {
    name: 'MySQL',
    icon: 'database',
    color: 'blue',
    category: 'database'
  },
  POSTGRESQL: {
    name: 'PostgreSQL',
    icon: 'database',
    color: 'blue',
    category: 'database'
  },
  ORACLE: {
    name: 'Oracle',
    icon: 'database',
    color: 'red',
    category: 'database'
  },
  SQLSERVER: {
    name: 'SQL Server',
    icon: 'database',
    color: 'blue',
    category: 'database'
  },

  // 消息队列类型
  KAFKA: {
    name: 'Kafka',
    icon: 'message',
    color: 'orange',
    category: 'message'
  },
  RABBITMQ: {
    name: 'RabbitMQ',
    icon: 'message',
    color: 'orange',
    category: 'message'
  },
  ROCKETMQ: {
    name: 'RocketMQ',
    icon: 'message',
    color: 'orange',
    category: 'message'
  },

  // 消息代理类型
  MQTT: {
    name: 'MQTT',
    icon: 'wifi',
    color: 'green',
    category: 'protocol'
  },
  COAP: {
    name: 'CoAP',
    icon: 'wifi',
    color: 'green',
    category: 'protocol'
  },

  // 接口类型
  HTTP: {
    name: 'HTTP',
    icon: 'api',
    color: 'magenta',
    category: 'api'
  },
  HTTPS: {
    name: 'HTTPS',
    icon: 'api',
    color: 'magenta',
    category: 'api'
  },
  WEBSOCKET: {
    name: 'WebSocket',
    icon: 'api',
    color: 'magenta',
    category: 'api'
  },

  // 时序数据库类型
  IOTDB: {
    name: 'IoTDB',
    icon: 'line-chart',
    color: 'purple',
    category: 'timeseries'
  },
  INFLUXDB: {
    name: 'InfluxDB',
    icon: 'line-chart',
    color: 'cyan',
    category: 'timeseries'
  },
  TDENGINE: {
    name: 'TDengine',
    icon: 'line-chart',
    color: 'purple',
    category: 'timeseries'
  },

  // 搜索引擎类型
  ELASTICSEARCH: {
    name: 'Elasticsearch',
    icon: 'search',
    color: 'geekblue',
    category: 'search'
  },

  // 缓存类型
  REDIS: {
    name: 'Redis',
    icon: 'database',
    color: 'red',
    category: 'cache'
  },
  MEMCACHED: {
    name: 'Memcached',
    icon: 'database',
    color: 'red',
    category: 'cache'
  },

  // 云平台类型
  ALIYUN_IOT: {
    name: '阿里云IoT',
    icon: 'cloud',
    color: 'blue',
    category: 'cloud'
  },
  TENCENT_IOT: {
    name: '腾讯云IoT',
    icon: 'cloud',
    color: 'green',
    category: 'cloud'
  },
  HUAWEI_IOT: {
    name: '华为云IoT',
    icon: 'cloud',
    color: 'red',
    category: 'cloud'
  },
  AWS_IOT: {
    name: 'AWS IoT',
    icon: 'cloud',
    color: 'orange',
    category: 'cloud'
  }
}

// 插件类型映射
export const PLUGIN_TYPE_MAPPINGS = {
  // 数据库插件
  JDBC: {
    name: 'JDBC',
    icon: 'database',
    color: 'blue',
    category: 'database'
  },
  MYSQL: {
    name: 'MySQL',
    icon: 'database',
    color: 'blue',
    category: 'database'
  },
  POSTGRESQL: {
    name: 'PostgreSQL',
    icon: 'database',
    color: 'blue',
    category: 'database'
  },

  // 消息队列插件
  KAFKA: {
    name: 'Kafka',
    icon: 'message',
    color: 'orange',
    category: 'message'
  },
  RABBITMQ: {
    name: 'RabbitMQ',
    icon: 'message',
    color: 'orange',
    category: 'message'
  },

  // 消息代理插件
  MQTT: {
    name: 'MQTT',
    icon: 'wifi',
    color: 'green',
    category: 'protocol'
  },

  // 接口插件
  HTTP: {
    name: 'HTTP',
    icon: 'api',
    color: 'magenta',
    category: 'api'
  },
  REST: {
    name: 'REST',
    icon: 'api',
    color: 'magenta',
    category: 'api'
  },

  // 时序数据库插件
  IOTDB: {
    name: 'IoTDB',
    icon: 'line-chart',
    color: 'purple',
    category: 'timeseries'
  },
  INFLUXDB: {
    name: 'InfluxDB',
    icon: 'line-chart',
    color: 'cyan',
    category: 'timeseries'
  },

  // 搜索引擎插件
  ELASTICSEARCH: {
    name: 'Elasticsearch',
    icon: 'search',
    color: 'geekblue',
    category: 'search'
  },

  // 缓存插件
  REDIS: {
    name: 'Redis',
    icon: 'database',
    color: 'red',
    category: 'cache'
  }
}

// 数据方向映射
export const DATA_DIRECTION_MAPPINGS = {
  INPUT: {
    name: '数据输入',
    icon: 'import',
    color: 'processing',
    description: '仅支持数据输入'
  },
  OUTPUT: {
    name: '数据输出',
    icon: 'export',
    color: 'success',
    description: '仅支持数据输出'
  },
  BIDIRECTIONAL: {
    name: '双向流转',
    icon: 'swap',
    color: 'warning',
    description: '支持双向数据流转'
  }
}

// 数据源范围映射
export const SOURCE_SCOPE_MAPPINGS = {
  ALL_PRODUCTS: {
    name: '所有产品',
    icon: 'global',
    color: 'blue',
    description: '处理所有产品的数据'
  },
  SPECIFIC_PRODUCTS: {
    name: '指定产品',
    icon: 'appstore',
    color: 'green',
    description: '仅处理指定产品的数据'
  },
  APPLICATION: {
    name: '应用级别',
    icon: 'appstore',
    color: 'orange',
    description: '处理特定应用的数据'
  }
}

// 状态映射
export const STATUS_MAPPINGS = {
  1: {
    name: '启用',
    color: 'green',
    icon: 'check-circle'
  },
  0: {
    name: '禁用',
    color: 'red',
    icon: 'close-circle'
  }
}

// 工具函数
export const DataBridgeMappings = {
  // 获取资源类型信息
  getResourceType(type) {
    return RESOURCE_TYPE_MAPPINGS[type] || {
      name: type,
      icon: 'api',
      color: 'default',
      category: 'unknown'
    }
  },

  // 获取插件类型信息
  getPluginType(pluginType) {
    return PLUGIN_TYPE_MAPPINGS[pluginType] || {
      name: pluginType,
      icon: 'api',
      color: 'default',
      category: 'unknown'
    }
  },

  // 获取数据方向信息
  getDataDirection(direction) {
    return DATA_DIRECTION_MAPPINGS[direction] || {
      name: direction,
      icon: 'question',
      color: 'default',
      description: '未知方向'
    }
  },

  // 获取数据源范围信息
  getSourceScope(scope) {
    return SOURCE_SCOPE_MAPPINGS[scope] || {
      name: scope,
      icon: 'question',
      color: 'default',
      description: '未知范围'
    }
  },

  // 获取状态信息
  getStatus(status) {
    return STATUS_MAPPINGS[status] || {
      name: '未知',
      color: 'default',
      icon: 'question'
    }
  },

  // 获取所有资源类型选项（用于筛选）
  getResourceTypeOptions() {
    return Object.entries(RESOURCE_TYPE_MAPPINGS).map(([value, config]) => ({
      value,
      label: config.name,
      icon: config.icon,
      color: config.color,
      category: config.category
    }))
  },

  // 获取所有插件类型选项（用于筛选）
  getPluginTypeOptions() {
    return Object.entries(PLUGIN_TYPE_MAPPINGS).map(([value, config]) => ({
      value,
      label: config.name,
      icon: config.icon,
      color: config.color,
      category: config.category
    }))
  },

  // 根据分类获取资源类型
  getResourceTypesByCategory(category) {
    return Object.entries(RESOURCE_TYPE_MAPPINGS)
      .filter(([, config]) => config.category === category)
      .map(([value, config]) => ({
        value,
        label: config.name,
        icon: config.icon,
        color: config.color
      }))
  },

  // 根据分类获取插件类型
  getPluginTypesByCategory(category) {
    return Object.entries(PLUGIN_TYPE_MAPPINGS)
      .filter(([, config]) => config.category === category)
      .map(([value, config]) => ({
        value,
        label: config.name,
        icon: config.icon,
        color: config.color
      }))
  }
}

export default DataBridgeMappings
