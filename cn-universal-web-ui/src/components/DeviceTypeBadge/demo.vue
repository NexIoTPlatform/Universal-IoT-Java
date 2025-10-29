<template>
  <div class="device-type-badge-demo">
    <a-card title="DeviceTypeBadge 组件演示" style="margin: 20px;">

      <!-- 基础用法 -->
      <a-divider orientation="left">基础用法</a-divider>
      <div class="demo-section">
        <p>默认样式：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE"/>
          <device-type-badge type="GATEWAY"/>
          <device-type-badge type="GATEWAY_SUB_DEVICE"/>
        </div>
      </div>

      <!-- 自定义文本 -->
      <a-divider orientation="left">自定义文本</a-divider>
      <div class="demo-section">
        <p>自定义显示文本：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE" text="直连"/>
          <device-type-badge type="GATEWAY" text="网关"/>
          <device-type-badge type="GATEWAY_SUB_DEVICE" text="子设备"/>
        </div>
      </div>

      <!-- 不同尺寸 -->
      <a-divider orientation="left">不同尺寸</a-divider>
      <div class="demo-section">
        <p>小尺寸：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE" size="small"/>
          <device-type-badge type="GATEWAY" size="small"/>
          <device-type-badge type="GATEWAY_SUB_DEVICE" size="small"/>
        </div>

        <p>默认尺寸：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE" size="default"/>
          <device-type-badge type="GATEWAY" size="default"/>
          <device-type-badge type="GATEWAY_SUB_DEVICE" size="default"/>
        </div>

        <p>大尺寸：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE" size="large"/>
          <device-type-badge type="GATEWAY" size="large"/>
          <device-type-badge type="GATEWAY_SUB_DEVICE" size="large"/>
        </div>
      </div>

      <!-- 颜色主题 -->
      <a-divider orientation="left">颜色主题</a-divider>
      <div class="demo-section">
        <p>内置颜色主题：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE" color="blue"/>
          <device-type-badge type="DEVICE" color="green"/>
          <device-type-badge type="DEVICE" color="orange"/>
          <device-type-badge type="DEVICE" color="red"/>
          <device-type-badge type="DEVICE" color="purple"/>
          <device-type-badge type="DEVICE" color="cyan"/>
          <device-type-badge type="DEVICE" color="magenta"/>
        </div>
      </div>

      <!-- 自定义颜色 -->
      <a-divider orientation="left">自定义颜色</a-divider>
      <div class="demo-section">
        <p>自定义背景色和文字颜色：</p>
        <div class="demo-row">
          <device-type-badge
            type="DEVICE"
            :backgroundColor="'#ff6b6b'"
            :textColor="'white'"
          />
          <device-type-badge
            type="GATEWAY"
            :backgroundColor="'#4ecdc4'"
            :textColor="'white'"
          />
          <device-type-badge
            type="GATEWAY_SUB_DEVICE"
            :backgroundColor="'#45b7d1'"
            :textColor="'white'"
          />
        </div>
      </div>

      <!-- 边框样式 -->
      <a-divider orientation="left">边框样式</a-divider>
      <div class="demo-section">
        <p>带边框的标识：</p>
        <div class="demo-row">
          <device-type-badge type="DEVICE" :bordered="true"/>
          <device-type-badge type="GATEWAY" :bordered="true" color="blue"/>
          <device-type-badge type="GATEWAY_SUB_DEVICE" :bordered="true" color="orange"/>
        </div>
      </div>

      <!-- 实际应用场景 -->
      <a-divider orientation="left">实际应用场景</a-divider>
      <div class="demo-section">
        <p>在表格中使用：</p>
        <a-table :columns="columns" :dataSource="demoData" :pagination="false" size="small">
          <template slot="deviceType" slot-scope="text, record">
            <device-type-badge :type="record.deviceNode"/>
          </template>
        </a-table>
      </div>

      <div class="demo-section" style="margin-top: 20px;">
        <p>在列表中中使用：</p>
        <a-list :dataSource="demoData" size="small">
          <template slot="renderItem" slot-scope="item">
            <a-list-item>
              <a-list-item-meta>
                <template slot="title">
                  {{ item.deviceName }}
                  <device-type-badge :type="item.deviceNode" size="small"/>
                </template>
                <template slot="description">
                  {{ item.productKey }}
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>

    </a-card>
  </div>
</template>

<script>
export default {
  name: 'DeviceTypeBadgeDemo',
  data() {
    return {
      columns: [
        {
          title: this.$t('device.deviceName'),
          dataIndex: 'deviceName',
          key: 'deviceName',
        },
        {
          title: '设备类型',
          dataIndex: 'deviceNode',
          key: 'deviceType',
          scopedSlots: {customRender: 'deviceType'},
        },
        {
          title: '产品Key',
          dataIndex: 'productKey',
          key: 'productKey',
        },
      ],
      demoData: [
        {
          key: '1',
          deviceName: '温度传感器001',
          deviceNode: 'DEVICE',
          productKey: 'temp_sensor_001',
        },
        {
          key: '2',
          deviceName: '智能网关001',
          deviceNode: 'GATEWAY',
          productKey: 'smart_gateway_001',
        },
        {
          key: '3',
          deviceName: '网关子设备001',
          deviceNode: 'GATEWAY_SUB_DEVICE',
          productKey: 'gateway_sub_001',
        },
        {
          key: '4',
          deviceName: '湿度传感器002',
          deviceNode: 'DEVICE',
          productKey: 'humidity_sensor_002',
        },
        {
          key: '5',
          deviceName: '智能网关002',
          deviceNode: 'GATEWAY',
          productKey: 'smart_gateway_002',
        },
      ]
    }
  }
}
</script>

<style scoped>
.device-type-badge-demo {
  padding: 20px;
}

.demo-section {
  margin-bottom: 20px;
}

.demo-section p {
  margin-bottom: 10px;
  font-weight: 500;
  color: #666;
}

.demo-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.demo-row > * {
  margin-right: 8px;
}
</style>
