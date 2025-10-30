<template>
  <a-modal :visible.sync="visible" :title="formTitle" @cancel="close" :footer="null" width="600px">
    <a-form :model="form" :label-col="{span:6}" :wrapper-col="{span:16}" @submit.native.prevent>
      <a-form-item :label="$t('network.certName')" required>
        <a-input v-model="form.name" placeholder="请输入证书名称"/>
      </a-form-item>
      <a-form-item label="上传证书/私钥">
        <a-upload
          :before-upload="beforeUpload"
          :file-list="fileList"
          :multiple="true"
          :show-upload-list="true"
          :custom-request="handleUpload"
          accept=".pem,.key"
        >
          <a-button icon="upload">选择文件</a-button>
        </a-upload>
        <div style="margin-top:8px; color:#888; font-size:12px;">支持 .pem/.key，上传后自动读取内容
        </div>
      </a-form-item>
      <a-form-item label="证书内容">
        <a-textarea v-model="form.certContent" rows="3" placeholder="可粘贴证书内容（PEM）"/>
      </a-form-item>
      <a-form-item label="私钥内容">
        <a-textarea v-model="form.keyContent" rows="3" placeholder="可粘贴私钥内容（KEY）"/>
      </a-form-item>
      <a-form-item label="证书密码">
        <a-input v-model="form.certPassword" placeholder="如有请输入证书密码"/>
      </a-form-item>
      <a-form-item label="私钥密码">
        <a-input v-model="form.keyPassword" placeholder="如有请输入私钥密码"/>
      </a-form-item>
      <a-form-item :label="$t('common.remark')">
        <a-input v-model="form.remark" placeholder="备注"/>
      </a-form-item>
      <a-form-item :wrapper-col="{span:16, offset:6}">
        <a-button type="primary" @click="handleSubmit" :loading="loading">{{
            $t('button.save')
          }}
        </a-button>
        <a-button style="margin-left:12px;" @click="close">{{ $t('button.cancel') }}</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script>
import {addCertificate, updateCertificate} from '@/api/system/certificate'

export default {
  name: 'CertificateForm',
  props: {
    visible: Boolean,
    record: Object
  },
  data() {
    return {
      form: {
        name: '',
        sslKey: '',
        expireTime: '',
        remark: '',
        certContent: '',
        keyContent: '',
        certPassword: '',
        keyPassword: ''
      },
      fileList: [],
      loading: false
    }
  },
  computed: {
    formTitle() {
      return this.record && this.record.sslKey ? '编辑证书' : '新增证书'
    }
  },
  watch: {
    record: {
      immediate: true,
      handler(val) {
        if (val) {
          this.form = {...val}
        }
      }
    },
    visible(val) {
      if (!val) {
        this.resetForm()
      }
    }
  },
  methods: {
    beforeUpload(file) {
      const isPemOrKey = file.name.endsWith('.pem') || file.name.endsWith('.key')
      if (!isPemOrKey) {
        this.$message.error('仅支持.pem/.key文件')
        return false
      }
      return true
    },
    async handleUpload({file}) {
      const isPem = file.name.endsWith('.pem')
      const isKey = file.name.endsWith('.key')
      const reader = new FileReader()
      reader.onload = e => {
        const content = e.target.result
        // 简单校验PEM/KEY内容
        if (isPem && !/^-----BEGIN/.test(content)) {
          this.$message.error('证书文件内容格式不正确，应以-----BEGIN开头')
          return
        }
        if (isKey && !/^-----BEGIN/.test(content)) {
          this.$message.error('私钥文件内容格式不正确，应以-----BEGIN开头')
          return
        }
        if (isPem) {
          this.form.certContent = content
        }
        if (isKey) {
          this.form.keyContent = content
        }
      }
      reader.readAsText(file)
      // 不自动上传到后端，前端解析并回填
      return false
    },
    handleSubmit() {
      this.loading = true
      const api = (this.record && this.record.sslKey) ? updateCertificate : addCertificate
      api(this.form).then(() => {
        this.$message.success('保存成功')
        this.$emit('refresh')
        this.close()
      }).finally(() => {
        this.loading = false
      })
    },
    close() {
      this.$emit('update:visible', false)
    },
    resetForm() {
      this.form = {
        name: '',
        sslKey: '',
        expireTime: '',
        remark: '',
        certContent: '',
        keyContent: '',
        certPassword: '',
        keyPassword: ''
      }
      this.fileList = []
      this.loading = false
    }
  }
}
</script>
<style scoped>
</style> 