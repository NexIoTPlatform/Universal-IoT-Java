<template>
  <a-card bordered class="cert-bind-card">
    <div class="bind-cert-header">
      <span>当前绑定证书：</span>
      <template v-if="bindCert && bindCert.sslKey !== 'default-tcp'">
        <a-row gutter="24" style="margin: 16px 0 0 0;">
          <a-col :span="12">
            <div class="info-label">证书主题</div>
            <div class="info-value">{{ certInfoObj.subject || '-' }}</div>
          </a-col>
          <a-col :span="12">
            <div class="info-label">签发机构</div>
            <div class="info-value">{{ certInfoObj.issuer || '-' }}</div>
          </a-col>
          <a-col :span="12">
            <div class="info-label">有效期起始</div>
            <div class="info-value">{{ formatCertDate(certInfoObj.notBefore) }}</div>
          </a-col>
          <a-col :span="12">
            <div class="info-label">有效期截止</div>
            <div class="info-value">{{ formatCertDate(certInfoObj.notAfter) }}</div>
          </a-col>
          <a-col :span="12">
            <div class="info-label">序列号</div>
            <div class="info-value">{{ certInfoObj.serialNumber || '-' }}</div>
          </a-col>
          <a-col :span="12">
            <div class="info-label">签名算法</div>
            <div class="info-value">{{ certInfoObj.signatureAlgorithm || '-' }}</div>
          </a-col>
        </a-row>
        <div style="text-align: right; margin-top: 16px;">
          <a-button type="danger" size="small"
                    style="background:#ff4d4f;color:#fff;border:none;margin-right:8px;"
                    @click="handleUnbind" :disabled="bindCert.sslKey === 'default-tcp'">解绑
          </a-button>
          <a-button type="primary" size="small" @click="showCertModal = true">绑定证书</a-button>
        </div>
      </template>
      <template v-else>
        <span style="margin: 8px 0;">未绑定</span>
        <a-button type="primary" size="small" style="margin-left:16px;"
                  @click="showCertModal = true">绑定证书
        </a-button>
      </template>
    </div>
    <!-- 证书选择弹窗 -->
    <a-modal :visible.sync="showCertModal" title="选择证书" width="700px"
             @cancel="showCertModal=false" :footer="null" :bodyStyle="{ padding: '24px 32px' }">
      <a-table
        :columns="certColumns"
        :data-source="certList"
        :pagination="certPagination"
        rowKey="id"
        :loading="certLoading"
        :row-selection="{ type: 'radio', selectedRowKeys: selectedCertId ? [selectedCertId] : [], onChange: onCertSelect }"
        :rowClassName="rowClassName"
        :customRow="customRow"
        bordered
        size="middle"
        style="margin-bottom: 16px;"
      >
        <template slot="name" slot-scope="text, record">
          {{ record.name }}<span v-if="record.sslKey === 'default-tcp'"
                                 style="color:#faad14;">（默认）</span>
        </template>
        <span slot="expireTime" slot-scope="text, record">{{ parseTime(record.expireTime) }}</span>
      </a-table>
      <div style="text-align:right;margin-top:16px;">
        <a-button @click="showCertModal=false">取消</a-button>
        <a-button type="primary" style="margin-left:8px;min-width:90px;font-weight:bold;"
                  :disabled="!selectedCertId" @click="handleBind">确定绑定
        </a-button>
      </div>
    </a-modal>
  </a-card>
</template>
<script>
import {bindCertificate, getCertificateList, unbindCertificate} from '@/api/system/certificate'

export default {
  name: 'CertificateBind',
  props: {
    productKey: String,
    bindCertKey: String
  },
  data() {
    return {
      showCertModal: false,
      certList: [],
      certLoading: false,
      certPagination: {current: 1, pageSize: 10, total: 0},
      certColumns: [
        {
          title: '证书名称',
          dataIndex: 'name',
          align: 'center',
          scopedSlots: {customRender: 'name'}
        },
        {title: '证书标识', dataIndex: 'sslKey', align: 'center'},
        {
          title: '到期时间',
          dataIndex: 'expireTime',
          scopedSlots: {customRender: 'expireTime'},
          align: 'center'
        }
      ],
      selectedCertId: null,
      bindCert: null
    }
  },
  computed: {
    certInfoObj() {
      if (!this.bindCert || !this.bindCert.certInfo) {
        return {}
      }
      if (typeof this.bindCert.certInfo === 'string') {
        try {
          return JSON.parse(this.bindCert.certInfo)
        } catch {
          return {}
        }
      }
      return this.bindCert.certInfo
    }
  },
  watch: {
    bindCertKey: {
      immediate: true,
      handler() {
        this.loadBindCert()
      }
    }
  },
  methods: {
    loadBindCert() {
      if (this.bindCertKey) {
        getCertificateList({sslKey: this.bindCertKey}).then(res => {
          if (res.rows && res.rows.length) {
            this.bindCert = res.rows[0]
          } else {
            this.bindCert = null
          }
        })
      } else {
        this.bindCert = null
      }
    },
    loadCertList(page = 1, size = 10) {
      this.certLoading = true
      getCertificateList({pageNum: page, pageSize: size}).then(res => {
        // 过滤掉 sslKey 为 'default-tcp' 的证书
        this.certList = (res.rows || []).filter(item => item.sslKey !== 'default-tcp')
        this.certPagination.total = this.certList.length
        this.certPagination.current = page
        this.certPagination.pageSize = size
      }).finally(() => {
        this.certLoading = false
      })
    },
    onCertSelect(selectedRowKeys) {
      this.selectedCertId = selectedRowKeys[0]
    },
    handleBind() {
      const cert = this.certList.find(c => c.id === this.selectedCertId)
      if (!cert) {
        return
      }
      bindCertificate(this.productKey, cert.sslKey).then(res => {
        this.$message.success('绑定成功')
        this.showCertModal = false
        this.loadBindCert()
        this.$emit('refresh') // 新增，通知父组件刷新
      })
    },
    handleUnbind() {
      unbindCertificate(this.productKey).then(res => {
        this.$message.success('解绑成功')
        this.loadBindCert()
        this.$emit('refresh') // 新增，通知父组件刷新
      })
    },
    parseTime(val) {
      if (!val) {
        return ''
      }
      const d = new Date(val)
      if (isNaN(d.getTime())) {
        return val
      }
      const pad = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
        d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    formatCertDate(val) {
      if (!val) {
        return ''
      }
      let d
      if (typeof val === 'number') {
        d = new Date(val)
      } else {
        d = new Date(val)
      }
      if (isNaN(d.getTime())) {
        return val
      }
      const pad = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
        d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    formatCertVersion(val) {
      if (!val) {
        return ''
      }
      return 'V' + val
    },
    formatKeyUsage(val) {
      if (!val) {
        return ''
      }
      // 例："[true, false, true...]" 或数组
      let arr = []
      if (typeof val === 'string') {
        try {
          arr = JSON.parse(val)
        } catch {
          return val
        }
      } else if (Array.isArray(val)) {
        arr = val
      }
      const usageMap = ['数字签名', '非否认', '密钥加密', '数据加密', '密钥协商', '证书签名',
        'CRL签名', '仅限解密', '仅限加密']
      return arr.map((v, i) => v ? usageMap[i] : null).filter(Boolean).join('、')
    },
    formatExtKeyUsage(val) {
      if (!val) {
        return ''
      }
      // OID常见映射
      const map = {
        '1.3.6.1.5.5.7.3.1': '服务器认证',
        '1.3.6.1.5.5.7.3.2': '客户端认证',
        '1.3.6.1.5.5.7.3.3': '代码签名',
        '1.3.6.1.5.5.7.3.4': '安全邮件',
        '1.3.6.1.5.5.7.3.8': '时间戳'
      }
      return (Array.isArray(val) ? val : []).map(v => map[v] || v).join('、')
    },
    rowClassName(record) {
      return record.id === this.selectedCertId ? 'cert-row-selected' : ''
    },
    customRow(record) {
      return {
        on: {
          click: () => {
            this.selectedCertId = record.id
          }
        }
      }
    }
  },
  mounted() {
    this.loadCertList()
  }
}
</script>
<style scoped>
.cert-bind-card {
  margin-top: 0;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.04);
}

.bind-cert-header {
  display: flex;
  align-items: center;
  font-size: 15px;
  margin-bottom: 12px;
}

.cert-row-selected {
  background: #e6f7ff !important;
}

.ant-table-thead > tr > th {
  font-weight: bold;
  background: #fafafa;
}

.ant-table-tbody > tr > td {
  padding: 10px 12px;
}

.cert-info-section {
  display: none;
}

.info-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 2px;
}

.info-value {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  margin-bottom: 8px;
}
</style> 