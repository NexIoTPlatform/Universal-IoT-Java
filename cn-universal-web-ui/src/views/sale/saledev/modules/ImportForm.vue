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
          style="width: 100%"
          placeholder="请选择产品"
          v-model="form.productKey"
          :allow-clear="true"
          show-search
          option-filter-prop="children"
          :filter-option="filterOption"
          @change="typeChange">
          <a-select-option v-for="(d, index) in productList" :key="index" :value="d.productKey">
            {{ d.companyNo + d.classifiedName }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="文件上传" v-if="importShow">
        <a-upload-dragger
          accept=".xlsx, .xls"
          :multiple="false"
          :fileList="fileList"
          :headers="headers"
          :disabled="isUploading"
          :data="form"
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
            仅允许导入xls、xlsx格式文件，每次最多导入500条数据。
          </p>
        </a-upload-dragger>
      </a-form-model-item>
      <div class="bottom-control">
        <a-space>
          <a-button type="primary" @click="submitForm">
            保存
          </a-button>
          <a-button type="dashed" @click="cancel">
            取消
          </a-button>
        </a-space>
      </div>
    </a-form-model>
    <a-row style="text-align:center;" v-if="importShow">
      <a-button style="margin:0 auto;" type="link" @click="downloadTemplate">下载模板</a-button>
    </a-row>
  </a-modal>
</template>

<script>
import {importDevice} from '@/api/sale/sale'

export default {
  name: 'CreateForm',
  props: {
    productList: {
      type: Array,
      required: true
    }
  },
  components: {},
  data() {
    return {
      fileList: [],
      headers: {Authorization: 'Bearer ' + this.$store.getters.token},
      isUploading: false,
      actionUrl: process.env.VUE_APP_BASE_API + '/admin/sale/import',
      importShow: false,
      loading: false,
      formTitle: '导入设备',
      // 表单参数
      form: {
        productKey: undefined
      },
      open: false,
      rules: {
        productKey: [
          {required: true, message: '请选择产品类型', trigger: 'blur'}
        ]
      }
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
      this.importShow = false
      this.fileList = []
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        productKey: undefined
      }
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
          console.log('this.fileList = ', this.fileList)
          const formData = new FormData()
          formData.append('file', this.fileList[0])
          formData.append('productKey', this.form.productKey)
          importDevice(formData).then(response => {
            this.$message.success(
              '导入成功',
              3
            )
            this.open = false
            this.importShow = false
            this.fileList = []
            this.$emit('ok')
          })
        } else {
          return false
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
    // 下载模板
    downloadTemplate() {
      this.download('admin/sale/import/template', null, `sale_${new Date().getTime()}.xlsx`)
    },
    // 上传之前
    beforeUpload(file, fileList) {
      console.log('file = ', file)
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
