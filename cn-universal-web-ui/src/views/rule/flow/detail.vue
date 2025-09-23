<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <a-steps :current="current" @change="onChange">
        <a-step>
          <template slot="title">设置转发数据</template>
          <!-- <template slot="description">针对部分类型数据提供的快速配置，将引导您完成简单的业务设置。您也可以直接编辑过滤语句，实现更复杂的查询要求。</template> -->
        </a-step>
        <a-step>
          <template slot="title">设置转发目标</template>
        </a-step>
        <a-step>
          <template slot="title">启动规则</template>
        </a-step>
      </a-steps>
    </a-card>
    <a-card :bordered="false" style="margin-top: 10px">

      <div v-show="current === 0">
        <FlowBase
          :data="data"
          ref="base"
          :current="current"
          :product-list="productList"
          :device-group="deviceGroup"
          @loadDeviceList="loadDeviceList"
        />
      </div>

      <div v-show="current === 1">
        <FlowTarget
          :data="data"
          :current="current"
          @updateTargets="updateTargets"
        />
      </div>

      <div v-show="current === 2">
        <FlowView
          :data="data"
          :current="current"
          :product-list="productList"
          :device-group="deviceGroup"
          :device-list="deviceList"
        />
      </div>

    </a-card>
    <div class="btn-wrap">
      <a-button v-show="current === 0" class="btn" type="primary" @click="create">{{
          this.data.id ? '修改' : '创建'
        }}
      </a-button>
      <a-button v-show="current > 0" class="btn" @click="prev">上一步</a-button>
      <a-button v-show="current === 1" class="btn" type="primary" @click="next">下一步</a-button>
    </div>
  </page-header-wrapper>
</template>

<script>
import FlowBase from './components/FlowBase'
import FlowTarget from './components/FlowTarget'
import FlowView from './components/FlowView'
import {getRule} from '@/api/rule/rule'
import {listProduct} from '@/api/system/dev/product'
import {listGroup} from '@/api/system/dev/devGroup'

export default {
  name: 'RuleFlowDetail',
  components: {
    FlowBase,
    FlowTarget,
    FlowView
  },
  data() {
    return {
      current: 0,
      data: {
        id: undefined,
        ruleName: undefined,
        description: undefined,
        dataLevel: undefined,
        productKey: undefined,
        status: 'stop',
        payload: '{\n' +
          '  "time": 1644303132765,\n' +
          '  "iotId": "ae0e2153ef644d5ea2f61668c7ac38d6",\n' +
          '  "deviceId": "1234567890",\n' +
          '  "deviceName": "设备名称",\n' +
          '  "deviceNode": "DEVICE",\n' +
          '  "productKey": "产品编号",\n' +
          '  "properties": {\n' +
          '    "battery": "70",\n' +
          '    "temperature": 100\n' +
          '  },\n' +
          '  "messageType": "PROPERTIES"\n' +
          '}',
        groupId: undefined,
        relationIds: [],
        config: {
          appId: '*',
          fields: '*',
          condition: undefined,
          targets: []
        }
      },
      productList: [],
      deviceGroup: [],
      deviceList: []
    }
  },
  created() {
    listProduct({
      state: 0,
      pageNum: 1,
      pageSize: 99999999
    }).then(res => {
      this.productList = res.rows
    })
    listGroup().then(res => {
      this.deviceGroup = res[0].children || []
    })
    const id = this.$route.params.id
    if (id && id !== 'create') {
      getRule(id).then(res => {
        this.data = {...this.data, ...res.data}
      })
    }
  },
  methods: {
    onChange(val) {
      if (!this.data.id) {
        return
      }
      this.current = val
    },
    create() {
      if (this.data.id) {
        this.$refs.base.updateRule().then(() => {
          this.current++
        })
      } else {
        this.$refs.base.submitRule().then(id => {
          if (id) {
            this.data.id = id
            this.current++
          }
        })
      }
    },
    updateTargets(targets) {
      this.data.config.targets = targets
    },
    prev() {
      this.current--
    },
    next() {
      this.current++
    },
    loadDeviceList(list) {
      this.deviceList = list
    }
  }
}
</script>

<style scoped lang='less'>
.btn-wrap {
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;

  .btn {
    width: 200px;
    margin-right: 20px;
  }
}
</style>