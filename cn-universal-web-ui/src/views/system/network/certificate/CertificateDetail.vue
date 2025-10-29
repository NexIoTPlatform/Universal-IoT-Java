<template>
  <a-modal :visible.sync="visible" title="证书详情" @cancel="close" :footer="null" width="760px"
           class="cert-detail-modal">
    <div class="cert-header">
      <a-icon type="safety-certificate" style="font-size:28px;color:#1890ff;margin-right:12px;"/>
      <div class="cert-header-info">
        <div class="cert-title">{{ record.name || '未命名证书' }}</div>
        <div class="cert-meta">
          <a-descriptions-item label="证书标识">{{ record.sslKey }}</a-descriptions-item>
          <span class="cert-expire" :class="{ expired: isExpired(record.expireTime) }">
            <a-icon type="clock-circle"/>
            {{ record.expireTime }}
          </span>
        </div>
      </div>
    </div>
    <a-card class="cert-info-card" bordered>
      <a-row gutter="24">
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
        <!-- 原有信息 -->
        <a-col :span="12">
          <div class="info-label">备注</div>
          <div class="info-value">{{ record.remark || '-' }}</div>
        </a-col>
        <!-- <a-col :span="12">
          <div class="info-label">证书密码</div>
          <div class="info-value">{{ record.certPassword || '-' }}</div>
        </a-col> -->
        <a-col :span="12">
          <div class="info-label">{{ $t('app.createTime') }}</div>
          <div class="info-value">{{ record.createTime || '-' }}</div>
        </a-col>
        <!-- <a-col :span="12">
          <div class="info-label">私钥密码</div>
          <div class="info-value">{{ record.keyPassword || '-' }}</div>
        </a-col> -->
        <a-col :span="12">
          <div class="info-label">{{ $t('app.updateTime') }}</div>
          <div class="info-value">{{ record.updateTime || '-' }}</div>
        </a-col>
      </a-row>
    </a-card>
    <a-collapse class="cert-content-collapse" bordered>
      <a-collapse-panel key="1" header="证书内容 (PEM)">
        <div class="content-actions">
          <a-button size="small" icon="copy" @click="copy(record.certContent)">复制</a-button>
          <a-button size="small" icon="download" @click="download(record.certContent, 'cert.pem')"
                    style="margin-left:8px;">下载
          </a-button>
        </div>
        <pre class="content-pre">{{ record.certContent }}</pre>
      </a-collapse-panel>
      <a-collapse-panel key="2" header="私钥内容 (KEY)">
        <div class="content-actions">
          <a-button size="small" icon="copy" @click="copy(record.keyContent)">复制</a-button>
          <a-button size="small" icon="download" @click="download(record.keyContent, 'key.key')"
                    style="margin-left:8px;">下载
          </a-button>
        </div>
        <pre class="content-pre">{{ record.keyContent }}</pre>
      </a-collapse-panel>
    </a-collapse>
  </a-modal>
</template>
<script>
export default {
  name: 'CertificateDetail',
  props: {
    visible: Boolean,
    record: Object
  },
  computed: {
    certInfoObj() {
      if (!this.record || !this.record.certInfo) {
        return {}
      }
      if (typeof this.record.certInfo === 'string') {
        try {
          return JSON.parse(this.record.certInfo)
        } catch {
          return {}
        }
      }
      return this.record.certInfo
    }
  },
  methods: {
    close() {
      this.$emit('update:visible', false)
    },
    isExpired(dateStr) {
      if (!dateStr) {
        return false
      }
      const d = new Date(dateStr)
      return d < new Date()
    },
    copy(content) {
      if (!content) {
        return
      }
      const textarea = document.createElement('textarea')
      textarea.value = content
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('已复制')
    },
    download(content, filename) {
      if (!content) {
        return
      }
      const blob = new Blob([content], {type: 'text/plain'})
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = filename
      link.click()
      URL.revokeObjectURL(link.href)
    },
    formatCertDate(val) {
      if (!val) {
        return ''
      }
      // 支持时间戳（数字）和字符串
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
      const map = {
        '1.3.6.1.5.5.7.3.1': '服务器认证',
        '1.3.6.1.5.5.7.3.2': '客户端认证',
        '1.3.6.1.5.5.7.3.3': '代码签名',
        '1.3.6.1.5.5.7.3.4': '安全邮件',
        '1.3.6.1.5.5.7.3.8': '时间戳'
      }
      return (Array.isArray(val) ? val : []).map(v => map[v] || v).join('、')
    }
  }
}
</script>
<style scoped lang="less">
::v-deep .ant-modal-content {
  border-radius: 12px;
}

.cert-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.cert-header-info {
  flex: 1;
}

.cert-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 2px;
}

.cert-meta {
  display: flex;
  gap: 18px;
  font-size: 13px;
  color: #888;
  align-items: center;
}

.cert-sslkey {
  color: #888;
}

.cert-expire {
  color: #52c41a;
  font-weight: 500;
}

.cert-expire.expired {
  color: #ff4d4f;
}

.cert-info-card {
  margin-bottom: 18px;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.04);
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

.cert-content-collapse {
  margin-top: 8px;
  border-radius: 8px;
  background: #fafbfc;
}

.content-actions {
  margin-bottom: 6px;
}

.content-pre {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  max-height: 180px;
  overflow: auto;
  margin-top: 6px;
  font-family: 'Menlo', 'Monaco', 'Consolas', 'Courier New', monospace;
}
</style> 