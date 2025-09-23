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
          :filter-option="filterOption">
          <a-select-option v-for="(d, index) in productList" :key="index" :value="d.productKey">
            {{ d.companyNo + d.classifiedName }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="设备序列号" prop="deviceId">
        <a-input v-model="form.deviceId" placeholder="设备序列号"/>
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
  </a-modal>
</template>

<script>
import {addSale, getSale, updateSale} from '@/api/sale/sale'

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
      loading: false,
      formTitle: '',
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
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        deviceId: [
          {required: true, message: '设备序列号不能为空', trigger: 'blur'}
        ],
        productKey: [
          {required: true, message: '请选择设备所属产品', trigger: 'blur'}
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
      this.reset()
    },
    // 表单重置
    reset() {
      this.formType = 1
      this.form = {
        id: null,

        deviceId: null,

        productKey: undefined,

        status: 0,

        recode: null,

        creatorId: null,

        createTime: null,

        updateTime: null

      }
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.formType = 2
      const id = row ? row.id : ids
      getSale(id).then(response => {
        this.form = response.data
        this.open = true
        this.formTitle = '修改'
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id !== undefined && this.form.id !== null) {
            updateSale(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else {
            addSale(this.form).then(response => {
              this.$message.success(
                '新增成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          }
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
    }
  }
}
</script>
