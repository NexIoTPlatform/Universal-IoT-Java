<template>
  <!-- 增加修改 -->
  <a-modal
    width="800px"
    :title="formTitle"
    :visible="open"
    @cancel="cancel"
    :maskClosable="false"
    :footer="null"
  >
    <div class="import-form-container">
      <a-form-model ref="form" :model="form">
        <a-form-model-item label="文件上传" v-if="importShow">
          <a-upload-dragger
            accept=".json"
            :multiple="false"
            :fileList="fileList"
            :headers="headers"
            :disabled="isUploading"
            :action="actionUrl"
            :auto-upload="false"
            :remove="removeFile"
            :beforeUpload="beforeUpload"
            @change="handleChange"
          >
            <p class="ant-upload-drag-icon">
              <a-icon type="inbox"/>
            </p>
            <p class="ant-upload-text">
              将文件拖拽到此处，或者<a>点击上传</a>
            </p>
            <p class="ant-upload-hint">
              仅允许导入JSON格式文件。
            </p>
          </a-upload-dragger>
        </a-form-model-item>
      </a-form-model>

      <div class="form-actions">
        <a-space>
          <a-button @click="cancel">
            取消
          </a-button>
          <a-button type="primary" :loading="loading" @click="submitForm">
            确定
          </a-button>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script>
import {getProduct, importProduct} from '@/api/system/dev/product'
import {importDevice} from '@/api/system/dev/instance'


export default {
  name: 'ImportProductForm',
  props: {},
  components: {},
  data() {
    return {
      fileList: [],
      headers: {Authorization: 'Bearer ' + this.$store.getters.token},
      isUploading: false,
      actionUrl: process.env.VUE_APP_BASE_API + '/admin/v1/product/transfer/import',
      importShow: true,
      loading: false,
      formTitle: '导入产品',
      // 表单参数
      form: {
        productKey: undefined
      },
      productId: undefined,

      open: false
    }
  },
  filters: {},
  created() {
    this.productId = this.$route.params.id
    this.getProductKeyById()
  },

  beforeRouteEnter(to, from, next) {
    next(vm => {
      // 在进入路由之前执行逻辑
      vm.created()
      next()
    })
  },
  computed: {},
  watch: {},
  mounted() {
  },
  methods: {
    // 取消按钮
    cancel() {
      this.open = false
      // this.importShow = false
      this.fileList = []
      this.reset()
    },
    // 表单重置
    reset() {
    },
    /** 导入按钮操作 */
    handleImport(row) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {
        }
      })
      this.reset()
      this.open = true
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          // console.log('this.fileList = ', this.fileList)
          this.loading = true
          const formData = new FormData()
          formData.append('file', this.fileList[0])
          if (this.productId) {
            importDevice(formData, this.form.productKey).then((response) => {
              this.$message.success(
                response.msg,
                3
              )
              this.open = false
              this.fileList = []
              this.$emit('ok')
            })
          } else {
            importProduct(formData).then((response) => {
              this.$message.success(
                response.msg,
                3
              )
              this.open = false
              this.fileList = []
              this.$emit('ok')
            })
          }
        } else {
          return false
        }
        this.loading = false
      })
    },
    // 筛选选择框
    filterOption(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    // 导入处理
    handleChange(info) {
      const status = info.file.status
      if (status !== 'uploading') {
        // console.log(info.file, info.fileList)
      }
      if (status === 'done') {
        this.$message.success(`${info.file.name} 文件上传成功`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 文件上传失败`)
      }
    },
    getProductKeyById() {
      if (this.productId) {
        getProduct(this.productId).then(response => {
          this.form.productKey = response.data.productKey
        }).finally(() => {
          this.loading = false
        })
      }
    },
    // 下载模板
    downloadTemplate(productKey) {
      if (productKey) {
        this.download('admin/v1/device/import/template?productKey=' + productKey, null,
          `device_import_template_${new Date().getTime()}.json`)
      } else {
        // 产品导入不需要模板，JSON格式已经很清晰
        this.$message.info('产品导入使用JSON格式，可以直接导出现有产品作为参考')
      }
    },
    // 上传之前
    beforeUpload(file, fileList) {
      // console.log('file = ', file)
      this.fileList = [file]
      return false
    },
    // 移除文件
    removeFile(file) {
      this.fileList = []
      return true
    },
    // 产品类型发生变化
    typeChange(value, option) {
      if (value !== undefined && value !== null) {
        this.importShow = true
      } else {
        this.importShow = false
      }
    }
  }
}
</script>

<style scoped>
.import-form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.template-download {
  text-align: center;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

.form-actions .ant-space {
  gap: 12px;
}
</style>
