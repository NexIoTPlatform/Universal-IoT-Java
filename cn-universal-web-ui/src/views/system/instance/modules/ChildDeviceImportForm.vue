<template>
  <!-- 增加修改 -->
  <a-modal
    width="800px"
    :title="formTitle"
    :visible="open"
    @cancel="cancel"
    @ok="submitForm"
    :maskClosable="false"
  >
    <a-form-model ref="form" :model="form" :rules="rules">

      <a-form-model-item label="产品类型" prop="productKey">
        <a-select
          placeholder="请选择产品类型"
          v-model="form.productKey"
          style="width: 100%"
          allow-clear
          show-search
          option-filter-prop="children"
          :filter-option="filterOption">
          <a-select-option v-for="(item) in productTypeList" :key="item.productKey"
                           :value="item.productKey">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-model-item>

      <a-form-model-item label="文件上传" v-if="importShow" prop="fileList">
        <a-upload-dragger
          accept=".json"
          :multiple="false"
          :fileList="form.fileList"
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
      <div class="bottom-control">
        <a-space>
          <a-button type="primary" :loading="loading" @click="submitForm">
            保存
          </a-button>
          <a-button type="dashed" @click="cancel">
            取消
          </a-button>
        </a-space>
      </div>
    </a-form-model>
    <a-row style="text-align:center;" v-if="importShow">
      <a-button style="margin:0 auto;" type="link" @click="downloadTemplate(form.productKey)">
        下载模板
      </a-button>
    </a-row>
  </a-modal>
</template>

<script>
import {getSubdeviceGateways} from '@/api/system/dev/product'
import {importDevice} from '@/api/system/dev/instance'

export default {
  name: 'ImportProductForm',
  props: {
    gwProductKey: {
      type: String,
      default: ''
    },
    gwDeviceId: {
      type: String,
      required: true,
      default: ''
    }
  },
  data() {
    return {
      fileList: [],
      headers: {Authorization: 'Bearer ' + this.$store.getters.token},
      isUploading: false,
      actionUrl: process.env.VUE_APP_BASE_API + '/admin/v1/product/import',
      importShow: true,
      loading: false,
      formTitle: '导入设备',
      // 表单参数
      form: {
        productKey: undefined,
        fileList: []
      },

      open: false,
      productTypeList: [],
      rules: {
        productKey: [{required: true, message: '请选择产品类型', trigger: 'change'}],
        fileList: [
          {required: true, message: '请选择文件', trigger: 'change'}
          // { validator: checkFile, trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getSubdevice()
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      // 在进入路由之前执行逻辑
      vm.created()
      next()
    })
  },
  methods: {
    // 取消按钮
    cancel() {
      this.open = false
      // this.importShow = false
      this.form.fileList = []
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
          if (this.form.fileList.length === 0) {
            this.$message.error('请选择上传文件')
            return
          }

          this.loading = true
          const formData = new FormData()

          formData.append('file', this.form.fileList[0])

          importDevice(formData, this.form.productKey, this.gwDeviceId).then((response) => {
            this.$message.success(response.msg, 3)
            this.open = false
            this.form.fileList = []
            this.$emit('ok')
          })
          .finally(() => {
            this.loading = false
          })
        }
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
    getSubdevice() {
      getSubdeviceGateways(this.gwProductKey).then(res => {
        this.productTypeList = res.data
      })
    },
    // 下载模板
    downloadTemplate(productKey) {
      if (productKey) {
        this.download('/admin/v1/device/import/template?productKey=' + productKey, null,
          `product_import_template_${new Date().getTime()}.json`)
      } else {
        // this.download('admin/product/import/template', null, `product_import_template_${new Date().getTime()}.json`)
        this.$message.error('请选择产品类型')
      }
    },
    // 上传之前
    beforeUpload(file, fileList) {
      // console.log('file = ', file)
      this.form.fileList = [file]
      return false
    },
    // 移除文件
    removeFile(file) {
      this.form.fileList = []
      return true
    }
  }
}
</script>
