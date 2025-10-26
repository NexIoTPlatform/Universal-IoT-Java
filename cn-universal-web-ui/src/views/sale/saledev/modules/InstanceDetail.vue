<template>
  <!-- 增加修改 -->
  <a-modal
    width="800px"
    :title="formTitle"
    :visible="open"
    @cancel="cancel"
    :footer="null"
    :maskClosable="false"
  >
    <a-row style="margin-bottom: 8px">
      <span>
        <span
          style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">基本信息</span>
      </span>
    </a-row>
    <a-descriptions title="" style="margin-top:18px">
      <a-descriptions-item label="序列号" :span="2">{{ form.deviceId }}</a-descriptions-item>
      <a-descriptions-item label="所属产品" :span="2">
        <span v-for="(d, index) in productList" :key="index">
          <span v-if="d.productKey === form.productKey">{{ d.companyNo + d.classifiedName }}</span>
        </span>
      </a-descriptions-item>
      <a-descriptions-item label="在线离线" :span="2">
        <span v-if="form.online === 0"><a-tag color="red">离线</a-tag></span>
        <span v-else-if="form.online === 1"><a-tag color="blue">在线</a-tag></span>
        <span v-else><a-tag color="red">未知</a-tag></span>
      </a-descriptions-item>
      <a-descriptions-item label="设备销售" :span="2">
        <span v-if="form.status === 0"><a-tag color="red">未售</a-tag></span>
        <span v-else-if="form.status === 1"><a-tag color="blue">已售</a-tag></span>
      </a-descriptions-item>
      <a-descriptions-item label="入库时间" :span="2">{{ form.createTime }}</a-descriptions-item>
      <a-descriptions-item label="出库时间" :span="2">{{ form.updateTime }}</a-descriptions-item>
      <a-descriptions-item label="激活上线时间" :span="2">{{
          parseTime(form.registryTime)
        }}
      </a-descriptions-item>
      <a-descriptions-item label="创建者" :span="2">{{ form.creatorId }}</a-descriptions-item>
    </a-descriptions>
    <a-row v-if="form.status === 1">
      <a-divider style="margin:0px"/>
      <a-row style="margin-bottom: 8px;margin-top: 8px">
        <span>
          <span
            style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">销售者</span>
        </span>
      </a-row>
      <a-descriptions title="" style="margin-top:0px">
        <a-descriptions-item label="姓名" :span="1">{{ saleDetail.sale.name }}</a-descriptions-item>
        <a-descriptions-item label="手机号" :span="1">{{
            saleDetail.sale.phone
          }}
        </a-descriptions-item>
        <a-descriptions-item label="地址" :span="1">{{ saleDetail.sale.addr }}</a-descriptions-item>
      </a-descriptions>
      <a-divider style="margin:0px"/>
      <a-row style="margin-bottom: 8px;margin-top: 8px">
        <span>
          <span
            style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">购买者</span>
        </span>
      </a-row>
      <a-descriptions title="" style="margin-top:8px">
        <a-descriptions-item label="姓名" :span="1">{{ saleDetail.buy.name }}</a-descriptions-item>
        <a-descriptions-item label="手机号" :span="1">{{
            saleDetail.buy.phone
          }}
        </a-descriptions-item>
        <a-descriptions-item label="地址" :span="1">{{ saleDetail.buy.addr }}</a-descriptions-item>
      </a-descriptions>
    </a-row>
    <a-divider style="margin:0px"/>
    <a-row style="margin-bottom: 8px;margin-top: 8px">
      <span>
        <span
          style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">设备记录信息</span>
      </span>
    </a-row>
    <a-timeline style="margin-top: 20px">
      <span v-for="(d, index) in form.recode" :key="index">
        <a-timeline-item color="red" v-if="d.index === 'sale'">
          <a-tag color="cyan">消息: {{ d.msg.sale.name }} 出售设备给 {{ d.msg.buy.name }}</a-tag>
          <a-tag color="blue">操作者: {{ d.createId }}</a-tag>
          <a-tag color="purple">时间: {{ d.time }}</a-tag>
          <a-button type="link"
                    @click="$refs.saleForm.editSaleUpdate(saleDetail, form.id)">修改</a-button>
        </a-timeline-item>
        <a-timeline-item color="green" v-else>
          <a-tag color="cyan">消息: {{ d.msg }}</a-tag>
          <a-tag color="blue">操作者: {{ d.createId }}</a-tag>
          <a-tag color="purple">时间: {{ d.time }}</a-tag>
        </a-timeline-item>
      </span>
    </a-timeline>
    <!-- 出售 -->
    <sale-form
      ref="saleForm"
      @ok="getDetails"
    />
    <!--设备详情-->
    <!--    &lt;!&ndash; 设备详情 &ndash;&gt;
    <a-row>
      <a-row style="margin-bottom: 8px">
        <span>
          <span style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">设备信息</span>
        </span>
      </a-row>
      <a-descriptions title="" bordered>
        <a-descriptions-item label="序列号">
          {{ form.deviceId }}
        </a-descriptions-item>
        <a-descriptions-item label="所属产品">
          <span v-for="(d, index) in productList" :key="index">
            <span v-if="d.productKey === form.productKey">{{ d.companyNo + d.classifiedName }}</span>
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <span v-if="form.status === 0"><a-tag color="red">未售</a-tag></span>
          <span v-else-if="form.status === 1"><a-tag color="blue">已售</a-tag></span>
        </a-descriptions-item>
        <a-descriptions-item label="创建者">
          {{ form.creatorId }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ form.createTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-row>
    <br/>
    <br/>
    &lt;!&ndash; 销售者 &ndash;&gt;
    <a-row v-if="form.status === 1">
      <a-row style="margin-bottom: 8px">
        <span>
          <span style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">销售者</span>
        </span>
      </a-row>
      <a-descriptions title="" bordered>
        <a-descriptions-item label="姓名">
          {{ saleDetail.sale.name }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ saleDetail.sale.phone }}
        </a-descriptions-item>
        <a-descriptions-item label="地址">
          {{ saleDetail.sale.addr }}
        </a-descriptions-item>
      </a-descriptions>
    </a-row>
    <br v-if="form.status === 1"/>
    <br v-if="form.status === 1"/>
    &lt;!&ndash; 购买者 &ndash;&gt;
    <a-row v-if="form.status === 1">
      <a-row style="margin-bottom: 8px">
        <span>
          <span style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">购买者</span>
        </span>
      </a-row>
      <a-descriptions title="" bordered>
        <a-descriptions-item label="姓名">
          {{ saleDetail.buy.name }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ saleDetail.buy.phone }}
        </a-descriptions-item>
        <a-descriptions-item label="地址">
          {{ saleDetail.buy.addr }}
        </a-descriptions-item>
      </a-descriptions>
    </a-row>
    <br v-if="form.status === 1"/>
    <br v-if="form.status === 1"/>
    &lt;!&ndash; 设备记录信息 &ndash;&gt;
    <a-row>
      <a-row style="margin-bottom: 8px">
        <span>
          <span
            style="color: rgba(0, 0, 0, 0.85);font-weight: bold;font-size: 16px;line-height: 1.5;">设备周期记录</span>
        </span>
      </a-row>
      <a-timeline style="margin-top: 10px">
        <span v-for="(d, index) in form.recode" :key="index">
          <a-timeline-item color="red" v-if="d.index === 'sale'">
            <a-tag color="cyan">消息: {{ d.msg.sale.name }} 出售设备给 {{ d.msg.buy.name }}</a-tag>
            <a-tag color="blue">操作者: {{ d.createId }}</a-tag>
            <a-tag color="purple">时间: {{ d.time }}</a-tag>
            <a-button type="link">修改</a-button>
          </a-timeline-item>
          <a-timeline-item color="green" v-else>
            <a-tag color="cyan">消息: {{ d.msg }}</a-tag>
            <a-tag color="blue">操作者: {{ d.createId }}</a-tag>
            <a-tag color="purple">时间: {{ d.time }}</a-tag>
          </a-timeline-item>
        </span>
      </a-timeline>
    </a-row>-->
  </a-modal>
</template>

<script>
import {getSale} from '@/api/sale/sale'
import SaleForm from './SaleForm'

export default {
  name: 'InstanceDetail',
  props: {
    productList: {
      type: Array,
      required: true
    }
  },
  components: {
    SaleForm
  },
  data() {
    return {
      saleDetail: {
        buy: {
          name: undefined,
          addr: undefined,
          phone: undefined
        },
        sale: {
          name: undefined,
          addr: undefined,
          phone: undefined
        }
      },
      loading: false,
      formTitle: '设备详情',
      // 表单参数
      form: {
        id: null,

        deviceId: null,

        productKey: undefined,

        status: 0,

        recode: null,

        creatorId: null,

        createTime: null,

        updateTime: null

      },
      open: false
    }
  },
  filters: {},
  created() {
  },
  computed: {},
  watch: {},
  mounted() {
  },
  methods: {
    // 取消按钮
    cancel() {
      this.open = false
    },
    /** 查看详情按钮操作 */
    checkDetails(row) {
      this.saleDetail = {
        buy: {
          name: undefined,
          addr: undefined,
          phone: undefined
        },
        sale: {
          name: undefined,
          addr: undefined,
          phone: undefined
        }
      }
      getSale(row).then(response => {
        this.form = response.data
        this.form.recode = JSON.parse(this.form.recode)
        for (let i = 0; i < this.form.recode.length; i++) {
          const item = this.form.recode[i]
          if (item.index === 'sale') {
            this.saleDetail = item.msg
            break
          }
        }
        console.log('this.form = ', this.form)
        console.log('this.saleDetail = ', this.saleDetail)
        this.open = true
      })
    },
    getDetails() {
      getSale(this.form.id).then(response => {
        this.form = response.data
        this.form.recode = JSON.parse(this.form.recode)
        for (let i = 0; i < this.form.recode.length; i++) {
          const item = this.form.recode[i]
          if (item.index === 'sale') {
            this.saleDetail = item.msg
            break
          }
        }
        console.log('this.form = ', this.form)
        console.log('this.saleDetail = ', this.saleDetail)
      })
    }
  }
}
</script>
