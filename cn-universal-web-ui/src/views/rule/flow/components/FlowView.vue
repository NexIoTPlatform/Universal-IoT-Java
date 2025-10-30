<template>
  <div class="flow-view">
    <div class="left">
      <a-icon v-show="dataForm.status === 'run'" type="code" class="icon running"/>
      <a-icon v-show="dataForm.status === 'stop'" type="minus-circle" class="icon stoped"/>
      <p class="tip">{{ dataForm.status === 'run' ? '规则运行中' : '规则未运行' }}</p>
      <a-button
        class="start-btn"
        :type="dataForm.status === 'run' ? 'danger' : 'primary'"
        @click="toggleRunning"
      >
        {{ dataForm.status === 'run' ? '停止规则' : '启动规则' }}
      </a-button>
    </div>

    <a-divider class="line" type="vertical" dashed/>

    <div class="right">
      <a-descriptions title="规则信息" :column="1" bordered size="small">
        <a-descriptions-item label="规则ID">
          {{ dataForm.id }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('rule.ruleName')">
          {{ dataForm.ruleName }}
        </a-descriptions-item>
        <a-descriptions-item label="规则描述">
          {{ dataForm.description }}
        </a-descriptions-item>
        <a-descriptions-item label="数据级别">
          {{ convertDataLevel() }}
        </a-descriptions-item>
        <a-descriptions-item label="$t('device.deviceGroup')" v-if="dataForm.dataLevel === 'group'">
          {{ convertGroup() }}
        </a-descriptions-item>
        <a-descriptions-item label="产品"
                             v-if="dataForm.dataLevel === 'device' || dataForm.dataLevel === 'product'">
          {{ convertProduct() }}
        </a-descriptions-item>
        <a-descriptions-item label="设备" v-if="dataForm.dataLevel === 'device' ">
          {{ convertDevices() }}
        </a-descriptions-item>
      </a-descriptions>
    </div>
  </div>
</template>

<script>
import {changeRuleStatus} from '@/api/rule/rule'

export default {
  // eslint-disable-next-line vue/require-prop-types
  props: ['current', 'data', 'productList', 'deviceGroup', 'deviceList'],
  data() {
    return {
      status: 1,
      dataForm: {},
      dataLevelList: [
        {key: 'product', value: '产品'},
        {key: 'group', value: '设备群组'},
        {key: 'device', value: '设备'}
      ]
    }
  },
  watch: {
    data: {
      handler: function (val, oldvalue) {
        this.dataForm = val
      },
      deep: true
    }
  },
  created() {
    this.dataForm = this.data
  },
  methods: {
    convertDataLevel() {
      let text = ''
      this.dataLevelList.forEach(t => {
        if (t.key === this.dataForm.dataLevel) {
          text = t.value
        }
      })
      return text
    },
    convertProduct() {
      let text = ''
      this.productList.forEach(t => {
        if (t.productKey === this.dataForm.productKey) {
          text = t.name + ' ' + t.productKey
        }
      })
      return text
    },
    convertDevices() {
      const text = []
      this.deviceList.forEach(t => {
        this.dataForm.relationIds.forEach(id => {
          if (t.iotId === id) {
            text.push(t.deviceName)
          }
        })
      })
      return text.join('，')
    },
    convertGroup() {
      let text = ''
      const groupId = this.dataForm.groupId

      function fundGroupName(node) {
        if (node.id === groupId) {
          text = node.groupName
          return
        }
        if (node.children && node.children.length > 0) {
          node.children.forEach(item => {
            fundGroupName(item)
          })
        }
      }

      this.deviceGroup.forEach(t => {
        fundGroupName(t)
      })
      return text
    },
    toggleRunning() {
      const that = this
      const dataForm = this.dataForm
      this.$confirm({
        title: `确认${dataForm.status === 'run' ? '停止' : '启用'}规则${dataForm.ruleName}?`,
        content: '',
        onOk() {
          dataForm.status = (dataForm.status === 'run' ? 'stop' : 'run')
          return changeRuleStatus(dataForm)
          .then(() => {
            that.$message.success(
              `${dataForm.status === 'run' ? '停止' : '启用'}成功`,
              3
            )
          })
        },
        onCancel() {
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
.flow-view {
  display: flex;
  flex: 1;
}

.left {
  flex-shrink: 0;
  width: 400px;
  height: inherit;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .icon {
    font-size: 72px;
    margin: 10px 0;
  }

  .running {
    color: #50d4ab;
  }

  .stoped {
    color: rgb(114, 0, 0);
  }

  .tip {
    font-size: 24px;
    font-weight: bold;
    margin: 10px 0;
  }

  .start-btn {
    margin: 10px 0;
  }
}

.right {
  height: inherit;
  padding-left: 20px;
  flex-grow: 1;
}

.line {
  height: inherit;
}

.flow-view /deep/ th {
  width: 200px;
}
</style>
