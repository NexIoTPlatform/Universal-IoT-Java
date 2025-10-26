<template>
  <div class="clearfix">
    <a-upload
      :action="uploadFileUrl"
      :show-upload-list="false"
      :headers="headers"
      :before-upload="beforeUpload"
      @change="handleChange"
    >
      <a-button size="small" type="primary">选取文件</a-button>
    </a-upload>
  </div>
</template>

<script>
import storage from 'store'
import {ACCESS_TOKEN} from '@/store/mutation-types'

export default {
  name: 'FileUpload',
  props: {
    fileTyp: {
      type: Array,
      default: () => ['jar']
    },
    count: {
      type: Number,
      default: 1
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 10
    }
  },
  components: {},
  data() {
    return {
      loading: false,
      open: false,
      uploadFileUrl: process.env.VUE_APP_BASE_API + '/admin/system/oss/upload',
      headers: {
        Authorization: 'Bearer ' + storage.get(ACCESS_TOKEN)
      }
    }
  },
  mounted() {
  },

  methods: {
    handleCancel() {
      this.previewVisible = false
    },
    async handleChange(info) {
      if (info.file.status === 'uploading') {
        this.loading = true
        return
      }
      if (info.file.status === 'done') {
        if (info.file.response.code !== 0) {
          this.$message.error('上传失败' + info.file.response.msg)
          this.loading = false
          return
        }
        this.loading = false
        this.$message.success('上传成功')
        this.$emit('fileList',
          {name: info.file.response.data.fileName, url: info.file.response.data.url})
        // this.$emit('input', info.file.response.url)
      }
    },
    beforeUpload(file) {
      // 校检文件类型
      if (this.fileType) {
        let fileExtension = ''
        if (file.name.lastIndexOf('.') > -1) {
          fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1)
        }
        const isTypeOk = this.fileType.some((type) => {
          if (file.type.indexOf(type) > -1) {
            return true
          }
          if (fileExtension && fileExtension.indexOf(type) > -1) {
            return true
          }
          return false
        })
        if (!isTypeOk) {
          this.$message.error(`文件格式不正确, 请上传${this.fileType.join('/')}格式文件!`)
          return false
        }
      }
      // 校检文件大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$message.error(`上传文件大小不能超过 ${this.fileSize} MB!`)
          return false
        }
      }
      return true
    }
  }
}
</script>
<style lang="less" scoped>
img {
  width: 128px;
  height: 128px;
}

</style>
