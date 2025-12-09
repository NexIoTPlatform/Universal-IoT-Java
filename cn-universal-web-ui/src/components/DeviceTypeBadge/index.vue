<template>
  <span
    class="device-type-badge"
    :class="badgeClass"
    :style="badgeStyle"
  >
    {{ displayText }}
  </span>
</template>

<script>
export default {
  name: 'DeviceTypeBadge',
  props: {
    // 设备类型（可选，用于默认颜色映射）
    type: {
      type: String,
      required: false,
      validator: value => {
        if (!value) return true;
        const allowedTypes = [
          // 设备类型
          'DEVICE', 'GATEWAY', 'GATEWAY_SUB_DEVICE', 'VIDEO_DEVICE',
          // 驱动类型
          'jar', 'jscript', 'magic',
          // 数据源范围
          'ALL_PRODUCTS', 'SPECIFIC_PRODUCTS', 'APPLICATION',
          // 桥接类型
          'JDBC', 'KAFKA', 'MQTT', 'HTTP', 'IOTDB', 'INFLUXDB',
          // 状态
          'ENABLED', 'DISABLED', '1', '0',
          // 数据方向
          'INPUT', 'OUTPUT', 'BIDIRECTIONAL', 'IN', 'OUT'
        ];
        return allowedTypes.includes(value);
      }
    },
    // 显示文本（必填，完全自定义）
    text: {
      type: String,
      required: true
    },
    // 颜色主题（可选，默认使用内置颜色）
    color: {
      type: String,
      default: '',
      validator: value => ['', 'blue', 'green', 'orange', 'red', 'purple', 'cyan', 'magenta'].includes(value)
    },
    // 自定义背景色
    backgroundColor: {
      type: String,
      default: ''
    },
    // 自定义文字颜色
    textColor: {
      type: String,
      default: ''
    },
    // 尺寸
    size: {
      type: String,
      default: 'default',
      validator: value => ['small', 'default', 'large'].includes(value)
    },
    // 是否显示边框
    bordered: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    // 显示文本（优先使用传入的text，否则使用默认映射）
    displayText() {
      return this.text || this.getDefaultText();
    },

    // 徽章样式类
    badgeClass() {
      const classes = ['device-type-badge'];

      // 尺寸类
      classes.push(`device-type-badge-${this.size}`);

      // 边框类
      if (this.bordered) {
        classes.push('device-type-badge-bordered');
      }

      // 类型类（如果指定了type）
      if (this.type) {
        classes.push(`device-type-badge-${this.type.toLowerCase()}`);
      }

      // 颜色类（优先使用指定颜色，否则使用默认颜色）
      const colorToUse = this.color || this.getDefaultColor();
      if (colorToUse) {
        classes.push(`device-type-badge-${colorToUse}`);
      }

      return classes;
    },

    // 徽章样式
    badgeStyle() {
      const style = {};

      // 自定义背景色
      if (this.backgroundColor) {
        style.backgroundColor = this.backgroundColor;
      }

      // 自定义文字颜色
      if (this.textColor) {
        style.color = this.textColor;
      }

      return style;
    }
  },
  methods: {
    // 获取默认显示文本（保留方法，用于向后兼容）
    getDefaultText() {
      const textMap = {
        // 设备类型
        'DEVICE': '直',
        'GATEWAY': '网',
        'GATEWAY_SUB_DEVICE': '子',
        'VIDEO_DEVICE': '视',
        // 数据源范围
        'ALL_PRODUCTS': '全量',
        'SPECIFIC_PRODUCTS': '定向',
        'APPLICATION': '应用',
        // 桥接类型
        'JDBC': 'JDBC',
        'KAFKA': 'Kafka',
        'MQTT': 'MQTT',
        'HTTP': 'HTTP',
        'IOTDB': 'IoTDB',
        'INFLUXDB': 'InfluxDB',
        // 状态
        'ENABLED': '启用',
        'DISABLED': '禁用',
        '1': '启用',
        '0': '禁用',
        // 数据方向
        'INPUT': '输入',
        'OUTPUT': '输出',
        'BIDIRECTIONAL': '双向',
        'IN': '输入',
        'OUT': '输出'
      };
      return textMap[this.type] || this.type;
    },

    // 获取默认颜色
    getDefaultColor() {
      const colorMap = {
        // 设备类型
        'DEVICE': 'blue',
        'GATEWAY': 'green',
        'GATEWAY_SUB_DEVICE': 'orange',
        'VIDEO_DEVICE': 'purple',
        // 驱动类型
        'jar': 'blue',
        'jscript': 'orange',
        'magic': 'purple',
        // 数据源范围
        'ALL_PRODUCTS': 'blue',
        'SPECIFIC_PRODUCTS': 'green',
        'APPLICATION': 'orange',
        // 桥接类型
        'JDBC': 'blue',
        'KAFKA': 'purple',
        'MQTT': 'red',
        'HTTP': 'cyan',
        'IOTDB': 'green',
        'INFLUXDB': 'magenta',
        // 状态
        'ENABLED': 'green',
        'DISABLED': 'red',
        '1': 'green',
        '0': 'red',
        // 数据方向
        'INPUT': 'blue',
        'OUTPUT': 'green',
        'BIDIRECTIONAL': 'orange',
        'IN': 'blue',
        'OUT': 'green'
      };
      return colorMap[this.type] || 'default';
    }
  }
}
</script>

<style scoped>
.device-type-badge {
  display: inline-block;
  border-radius: 8px;
  font-weight: 500;
  font-size: 11px;
  line-height: 1.2;
  white-space: nowrap;
  user-select: none;
  transition: all 0.3s ease;
  margin-left: 6px;
}

/* 尺寸样式 */
.device-type-badge-small {
  padding: 2px 6px;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
}

.device-type-badge-default {
  padding: 1px 4px;
}

.device-type-badge-large {
  padding: 6px 10px;
  font-size: 14px;
  min-width: 24px;
  height: 24px;
}

/* 边框样式 */
.device-type-badge-bordered {
  border: 1px solid currentColor;
}

/* 设备类型默认颜色 */
.device-type-badge-device {
  background-color: #1890ff;
  color: white;
  border: 1px solid #40a9ff;
}

.device-type-badge-gateway {
  background-color: #52c41a;
  color: white;
  border: 1px solid #73d13d;
}

.device-type-badge-gateway_sub_device {
  background-color: #fa8c16;
  color: white;
  border: 1px solid #ffc53d;
}

.device-type-badge-video_device {
  background-color: #722ed1;
  color: white;
  border: 1px solid #9254de;
}

/* 颜色主题 */
.device-type-badge-blue {
  background-color: #1890ff !important;
  color: white !important;
  border: 1px solid #40a9ff !important;
}

.device-type-badge-green {
  background-color: #52c41a !important;
  color: white !important;
  border: 1px solid #73d13d !important;
}

.device-type-badge-orange {
  background-color: #fa8c16 !important;
  color: white !important;
  border: 1px solid #ffc53d !important;
}

.device-type-badge-red {
  background-color: #f5222d !important;
  color: white !important;
  border: 1px solid #ff4d4f !important;
}

.device-type-badge-purple {
  background-color: #722ed1 !important;
  color: white !important;
  border: 1px solid #9254de !important;
}

.device-type-badge-cyan {
  background-color: #13c2c2 !important;
  color: white !important;
  border: 1px solid #36cfc9 !important;
}

.device-type-badge-magenta {
  background-color: #eb2f96 !important;
  color: white !important;
  border: 1px solid #f759ab !important;
}

/* 悬停效果 */
.device-type-badge:hover {
  opacity: 0.8;
  transform: scale(1.05);
}


@media (max-width: 768px) {
  .device-type-badge-default {
    padding: 3px 6px;
    font-size: 11px;
    min-width: 18px;
    height: 18px;
  }

  .device-type-badge-large {
    padding: 4px 8px;
    font-size: 12px;
    min-width: 20px;
    height: 20px;
  }
}
</style>
