<template>
  <div class="desc-wrap" v-if="detail.iccid">
    <a-descriptions title="物联网卡信息" border class="mt20" :column="4">
      <a-descriptions-item label="ICCID">{{ detail.iccid }}</a-descriptions-item>
      <a-descriptions-item label="IMEI">{{ detail.imei }}</a-descriptions-item>
      <a-descriptions-item label="卡号">{{ detail.cardNo }}</a-descriptions-item>
      <a-descriptions-item label="运营商">
        <a-tag size="small">{{ detail.ispCname }}</a-tag>
      </a-descriptions-item>

      <a-descriptions-item label="SIM卡状态">
        <a-tag :type="detail.status | tag">{{ detail.statusCname }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="归属代理">{{ detail.deptName }}</a-descriptions-item>

      <a-descriptions-item label="激活时间">{{ detail.activeTime }}</a-descriptions-item>
    </a-descriptions>

    <a-descriptions title="套餐信息" border class="mt20" v-if="!detail.poolName">
      <a-descriptions-item v-if="detail.goodsName" label="套餐名称">{{
          detail.goodsName
        }}
      </a-descriptions-item>
      <a-descriptions-item label="总流量" v-if="detail.flowUnit">{{
          detail.flowQty
        }}{{ detail.flowUnit }}
      </a-descriptions-item>
      <a-descriptions-item label="总流量" v-else>{{
          flowFormat(Number(detail.flowQty || 0))
        }}
      </a-descriptions-item>
      <a-descriptions-item label="已用流量">{{
          flowFormat(Number(detail.flowUsed || 0))
        }}
      </a-descriptions-item>
      <a-descriptions-item label="剩余流量">{{
          flowFormat(Number(detail.flowSurplus || 0))
        }}
      </a-descriptions-item>
      <a-descriptions-item label="到期时间">{{ detail.expireTime }}</a-descriptions-item>
    </a-descriptions>

    <a-descriptions title="流量池信息" border class="mt20" v-if="detail.poolName">
      <a-descriptions-item label="流量池名称">{{ detail.poolName }}</a-descriptions-item>
      <a-descriptions-item label="入池时间">{{ detail.joinPoolTime }}</a-descriptions-item>
      <a-descriptions-item label="流量池总流量">{{ detail.poolTotal }}GB</a-descriptions-item>
      <a-descriptions-item label="已用流量">{{
          flowFormat(Number(detail.poolUsed || 0))
        }}
      </a-descriptions-item>
      <a-descriptions-item label="流量池剩余流量">{{
          flowFormat(Number(detail.poolSurplus || 0))
        }}
      </a-descriptions-item>
      <a-descriptions-item label="流量池停用时间">{{ detail.outageTime }}</a-descriptions-item>
    </a-descriptions>

  </div>

  <a-empty v-else description="未查到SIM卡信息"></a-empty>
</template>

<script>
import axios from 'axios'

export default {
  filters: {
    tag(val) {
      switch (val) {
        case 'wait':
          return ''
        case 'normal':
          return 'success'
        case 'deactivate':
          return 'info'
        case 'unknown':
          return 'warning'
        case 'error':
          return 'danger'
        default :
          return ''
      }
    }
  },
  props: {
    iccid: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      detail: {}
    }
  },
  created() {
    const apiUrl = process.env.NODE_ENV === 'production'
      ? `https://xx.cn/iccid?iccid=${this.iccid}`
      : `https://xx.cn/iccid?iccid=${this.iccid}`
    axios.get(apiUrl).then(res => {
      this.detail = res.data.data || {}
    })
  },
  methods: {
    flowFormat(value) {
      let unitIndex = 0
      const unitArr = ['KB', 'MB', 'GB', 'TB']
      while (value > 1024 && unitIndex < unitArr.length) {
        unitIndex++
        value = value / 1024
      }
      return value.toFixed(2) + unitArr[unitIndex]
    }
  }
}
</script>
