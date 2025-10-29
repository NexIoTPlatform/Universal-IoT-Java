<template>
  <a-modal
    :maskClosable="false"
    title="功能详情"
    v-model="open"
    width="800px"
    :footer="null"
    @cancel="closePanel"
  >
    <span
      style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;margin-bottom: 10px;margin-top: -10px;display: block;">参数列表</span>
    <!-- 数据展示 -->
    <a-table
      :loading="loading"
      :size="tableSize"
      rowKey="id"
      :columns="functionParamsFormat"
      :data-source="functionParams"
      :pagination="false"
      :expandedRowKeys="expandedRowKeys"
      :expandIcon="() => null"
      :expandIconAsCell="false"
    >
      <template slot="action" slot-scope="text, record">
        <a-button
          class="expand-btn"
          icon="plus"
          type="default"
          size="small"
          @click="handleExpand(record.id)"
        />
      </template>
      <template slot="unit" slot-scope="text, record">
        <span v-if="record.unit === '%'">{{ record.unit }}(百分比)</span>
        <span v-else-if="record.unit === 'count'">{{ record.unit }}(次)</span>
        <span v-else-if="record.unit === 'r/min'">{{ record.unit }}(转每分钟)</span>
        <span v-else-if="record.unit === 'db'">{{ record.unit }}(分贝)</span>
        <span v-else-if="record.unit === 'dBm'">{{ record.unit }}(信号强度)</span>
        <span v-else-if="record.unit === 'kj'">{{ record.unit }}(千焦)</span>
        <span v-else-if="record.unit === 'ppm'">{{ record.unit }}(百万分率)</span>

        <span v-else-if="record.unit === 'nm'">{{ record.unit }}(纳米)</span>
        <span v-else-if="record.unit === 'μm'">{{ record.unit }}(微米)</span>
        <span v-else-if="record.unit === 'mm'">{{ record.unit }}(毫米)</span>
        <span v-else-if="record.unit === 'cm'">{{ record.unit }}(厘米)</span>
        <span v-else-if="record.unit === 'm'">{{ record.unit }}(米)</span>
        <span v-else-if="record.unit === 'km'">{{ record.unit }}(千米)</span>

        <span v-else-if="record.unit === 'mm²'">{{ record.unit }}(平方毫米)</span>
        <span v-else-if="record.unit === 'cm²'">{{ record.unit }}(平方厘米)</span>
        <span v-else-if="record.unit === 'm²'">{{ record.unit }}(平方米)</span>
        <span v-else-if="record.unit === 'km²'">{{ record.unit }}(平方千米)</span>
        <span v-else-if="record.unit === 'hm²'">{{ record.unit }}(公顷)</span>

        <span v-else-if="record.unit === 'd'">{{ record.unit }}(天)</span>
        <span v-else-if="record.unit === 'h'">{{ record.unit }}(小时)</span>
        <span v-else-if="record.unit === 'min'">{{ record.unit }}(分钟)</span>
        <span v-else-if="record.unit === 's'">{{ record.unit }}(秒)</span>
        <span v-else-if="record.unit === 'ms'">{{ record.unit }}(毫秒)</span>
        <span v-else-if="record.unit === 'μs'">{{ record.unit }}(微秒)</span>
        <span v-else-if="record.unit === 'ns'">{{ record.unit }}(纳秒)</span>

        <span v-else-if="record.unit === 'mm³'">{{ record.unit }}(立方毫米)</span>
        <span v-else-if="record.unit === 'cm³'">{{ record.unit }}(立方厘米)</span>
        <span v-else-if="record.unit === 'm³'">{{ record.unit }}(立方米)</span>
        <span v-else-if="record.unit === 'km³'">{{ record.unit }}(立方千米)</span>

        <span v-else-if="record.unit === 'm³/s'">{{ record.unit }}(立方米每秒)</span>
        <span v-else-if="record.unit === 'km³/s'">{{ record.unit }}(立方千米每秒)</span>
        <span v-else-if="record.unit === 'cm³/s'">{{ record.unit }}(立方厘米每秒)</span>
        <span v-else-if="record.unit === 'l/s'">{{ record.unit }}(升每秒)</span>
        <span v-else-if="record.unit === 'm³/h'">{{ record.unit }}(立方米每小时)</span>
        <span v-else-if="record.unit === 'km³/h'">{{ record.unit }}(立方千米每小时)</span>
        <span v-else-if="record.unit === 'cm³/h'">{{ record.unit }}(立方厘米每小时)</span>
        <span v-else-if="record.unit === 'l/h'">{{ record.unit }}(升每小时)</span>

        <span v-else-if="record.unit === 'mL'">{{ record.unit }}(毫升)</span>
        <span v-else-if="record.unit === 'L'">{{ record.unit }}(升)</span>

        <span v-else-if="record.unit === 'mg'">{{ record.unit }}(毫克)</span>
        <span v-else-if="record.unit === 'g'">{{ record.unit }}(克)</span>
        <span v-else-if="record.unit === 'kg'">{{ record.unit }}(千克)</span>
        <span v-else-if="record.unit === 't'">{{ record.unit }}(吨)</span>

        <span v-else-if="record.unit === 'Pa'">{{ record.unit }}(帕斯卡)</span>
        <span v-else-if="record.unit === 'kPa'">{{ record.unit }}(千帕斯卡)</span>
        <span v-else-if="record.unit === 'N'">{{ record.unit }}(牛顿)</span>
        <span v-else-if="record.unit === 'N.m'">{{ record.unit }}(牛·米)</span>

        <span v-else-if="record.unit === 'K'">{{ record.unit }}(开尔文)</span>
        <span v-else-if="record.unit === '℃'">{{ record.unit }}(摄氏度)</span>
        <span v-else-if="record.unit === '℉'">{{ record.unit }}(华氏度)</span>

        <span v-else-if="record.unit === 'J'">{{ record.unit }}(焦耳)</span>
        <span v-else-if="record.unit === 'cal'">{{ record.unit }}(卡)</span>

        <span v-else-if="record.unit === 'W'">{{ record.unit }}(瓦特)</span>
        <span v-else-if="record.unit === 'kW'">{{ record.unit }}(千瓦特)</span>

        <span v-else-if="record.unit === 'rad'">{{ record.unit }}(弧度)</span>
        <span v-else-if="record.unit === '°'">{{ record.unit }}(度)</span>
        <span v-else-if="record.unit === '′'">{{ record.unit }}([角]分)</span>
        <span v-else-if="record.unit === '″'">{{ record.unit }}([角]秒)</span>

        <span v-else-if="record.unit === 'Hz'">{{ record.unit }}(赫兹)</span>
        <span v-else-if="record.unit === 'MHz'">{{ record.unit }}(兆赫兹)</span>
        <span v-else-if="record.unit === 'GHz'">{{ record.unit }}(G赫兹)</span>

        <span v-else-if="record.unit === 'm/s'">{{ record.unit }}(米每秒)</span>
        <span v-else-if="record.unit === 'km/h'">{{ record.unit }}(千米每小时)</span>
        <span v-else-if="record.unit === 'kn'">{{ record.unit }}(节)</span>

        <span v-else-if="record.unit === 'V'">{{ record.unit }}(伏特)</span>
        <span v-else-if="record.unit === 'kV'">{{ record.unit }}(千伏)</span>
        <span v-else-if="record.unit === 'mV'">{{ record.unit }}(毫伏)</span>
        <span v-else-if="record.unit === 'μV'">{{ record.unit }}(微伏)</span>
        <span v-else-if="record.unit === 'A'">{{ record.unit }}(安培)</span>
        <span v-else-if="record.unit === 'mA'">{{ record.unit }}(毫安)</span>
        <span v-else-if="record.unit === 'μA'">{{ record.unit }}(微安)</span>
        <span v-else-if="record.unit === 'nA'">{{ record.unit }}(纳安)</span>
        <span v-else-if="record.unit === 'Ω'">{{ record.unit }}(欧姆)</span>
        <span v-else-if="record.unit === 'KΩ'">{{ record.unit }}(千欧)</span>
        <span v-else-if="record.unit === 'MΩ'">{{ record.unit }}(兆欧)</span>
        <span v-else-if="record.unit === 'eV'">{{ record.unit }}(电子伏)</span>
        <span v-else-if="record.unit === 'kW·h'">{{ record.unit }}(千瓦·时)</span>
        <span v-else>{{ record.unit }}</span>
      </template>
      <template slot="expandedRowRender" slot-scope="record">
        <Analysis type="functions" :record="record"/>
      </template>

    </a-table>
    <!-- <span style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;margin-top: 10px;display: block;">请求示例</span>
    <a-spin :spinning="loading">
      <json-viewer :value="functionParamsData" :expand-depth="10" copyable >
        <template v-slot:copy>复制</template>
      </json-viewer>
    </a-spin>
    <a-alert message="注意：只为显示传参结构，实际调用请检查【参数类型】，避免下发失败!" banner /> -->
  </a-modal>
</template>

<script>
import JsonViewer from 'vue-json-viewer'
import Analysis from './Analysis'

export default {
  name: 'FunctionParamsDetails',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    // 物模型功能参数
    functionParams: {
      type: Array,
      default: () => {
      }
    },
    functionId: {
      type: String,
      default: () => {
      }
    }
  },
  components: {
    JsonViewer,
    Analysis
  },
  watch: {
    show(val) {
      this.open = val
      if (val === true) {
        this.functionParamsData = {}
        this.functionParamsData.messageType = 'FUNCTIONS'
        this.functionParamsData.function = this.functionId
        this.functionParamsData.data = {}
        for (let i = this.functionParams.length - 1; i >= 0; i--) {
          if (this.functionParams[i].valueType !== undefined && this.functionParams[i].valueType
            !== null) {
            this.functionParams[i].type = this.functionParams[i].valueType.type
            this.functionParams[i].unit = this.functionParams[i].valueType.unit
            this.functionParamsData.data[`${this.functionParams[i].id}`] = null
          } else {
            this.functionParams.splice(i, 1)
          }
        }
      }
    }
  },
  data() {
    return {
      // 传参示例
      functionParamsData: {},
      // 是否显示弹出层
      open: false,
      // 遮罩层
      loading: false,
      // 功能参数列表
      functionParamsFormat: [
        {
          title: '',
          dataIndex: 'action',
          width: '50px',
          scopedSlots: {customRender: 'action'},
          align: 'center'
        },
        {
          title: '参数标识',
          dataIndex: 'id',
          scopedSlots: {customRender: 'id'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '参数名称',
          dataIndex: 'name',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '参数类型',
          dataIndex: 'type',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '单位',
          dataIndex: 'unit',
          scopedSlots: {customRender: 'unit'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('rule.description'),
          dataIndex: 'description',
          scopedSlots: {customRender: 'description'},
          ellipsis: true,
          align: 'center'
        }
      ],
      expandedRowKeys: []
    }
  },
  methods: {
    // 关闭弹出层
    closePanel() {
      this.$emit('close')
    },
    handleExpand(rowkey) {
      // 只允许当行展开逻辑
      if (this.expandedRowKeys.length > 0) {
        const index = this.expandedRowKeys.indexOf(rowkey)
        if (index > -1) {
          this.expandedRowKeys.splice(index, 1)
        } else {
          this.expandedRowKeys.splice(0, this.expandedRowKeys.length)
          this.expandedRowKeys.push(rowkey)
        }
      } else {
        this.expandedRowKeys.push(rowkey)
      }
    }
  }

}
</script>
<style scoped>
.expand-btn {
  width: 18px;
  height: 18px;
  padding: 0;
  font-size: 10px;
  border-radius: 3px;
  min-width: 18px;
  min-height: 18px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #64748b;
}
</style>
