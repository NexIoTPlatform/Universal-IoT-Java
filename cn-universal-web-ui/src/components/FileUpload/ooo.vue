<template>
  <div class="upload-file">
    <a-upload
      :action="uploadFileUrl"
      :before-upload="handleBeforeUpload"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      @change="display"
    >
      <!-- 上传按钮 -->
      <a-button size="small" type="primary">选取文件</a-button>
      <!-- 上传提示 -->
      <!--      <div v-if="showTip">-->
      <!--        请上传-->
      <!--        <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b></template>-->
      <!--        <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b></template>-->
      <!--        的文件-->
      <!--      </div>-->
    </a-upload>

    <!-- 文件列表 -->
    <!--    <transition-group>-->
    <!--      <a-list :key="file.uid" v-for="(file, index) in fileList">-->
    <!--        <a :href="`${file.url}`"  target="_blank">-->
    <!--          <span> {{ getFileName(file.name) }} </span>-->
    <!--        </a>-->
    <!--        <div>-->
    <!--          <a @click="handleDelete(index)" type="danger">{{ $t('button.delete') }}</a>-->
    <!--        </div>-->
    <!--      </a-list>-->
    <!--    </transition-group>-->
  </div>
</template>

<script>

import storage from 'store'
import {ACCESS_TOKEN} from '@/store/mutation-types'

export default {
  name: 'FileUpload',
  props: {
    // 值
    value: [String, Object, Array],
    // 数量限制
    limit: {
      type: Number,
      default: 5
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 5
    },
    // 文件类型, 例如['png', 'jpg', 'jpeg']
    fileType: {
      type: Array,
      default: () => ['jar']
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadFileUrl: process.env.VUE_APP_BASE_API + '/admin/system/oss/upload', // 上传的图片服务器地址
      headers: {
        Authorization: 'Bearer ' + storage.get(ACCESS_TOKEN)
      },
      fileList: []
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          let temp = 1
          // 首先将值转为数组
          const list = Array.isArray(val) ? val : this.value.split(',')
          // 然后将数组转为对象数组
          this.fileList = list.map(item => {
            if (typeof item === 'string') {
              item = {name: item, url: item}
            }
            item.uid = item.uid || new Date().getTime() + temp++
            return item
          })
        } else {
          this.fileList = []
          return []
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    // 是否显示提示
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize)
    }
  },
  methods: {

    display(info) {
      // this.fileList = info.fileList
      const status = info.file.status
      if (status !== 'uploading') {
        console.log(info.file, info.fileList)
      }
      if (status === 'done') {
        this.handleUploadSuccess(info.file.response)
      } else if (status === 'error') {
        this.handleUploadError()
      }
    },
    // 上传前校检格式和大小
    handleBeforeUpload(file) {
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
    },
    // 文件个数超出
    handleExceed() {
      this.$message.error(`上传文件数量不能超过 ${this.limit} 个!`)
    },
    // 上传失败
    // eslint-disable-next-line handle-callback-err
    handleUploadError(err) {
      this.$message.error('上传失败, 请重试')
    },
    // 上传成功回调
    handleUploadSuccess(res, file) {
      this.$message.success('上传成功')
      this.fileList.push({name: res.data.fileName, url: res.data.url})
      this.$emit('fileList', {name: res.data.fileName, url: res.data.url})
    },
    // 删除文件
    handleDelete(index) {
      this.fileList.splice(index, 1)
      this.$emit('input', this.listToString(this.fileList))
    },
    // 获取文件名称
    getFileName(name) {
      if (name.lastIndexOf('/') > -1) {
        return name.slice(name.lastIndexOf('/') + 1).toLowerCase()
      } else {
        return ''
      }
    },
    // 对象转成指定字符串分隔
    listToString(list, separator) {
      let strs = ''
      separator = separator || ','
      for (const i in list) {
        strs += list[i].url + separator
      }
      return strs !== '' ? strs.substr(0, strs.length - 1) : ''
    }
  }
}
</script>
