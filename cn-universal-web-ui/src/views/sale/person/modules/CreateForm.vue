<template>
  <!-- 增加修改 -->
  <a-modal
    width="800px"
    :title="formTitle"
    :visible="open"
    @cancel="onClose"
    @ok="submitForm"
    :maskClosable="false"
  >
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item label="人员类型" prop="dictType">
        <a-select style="width: 100%" placeholder="请选择" v-model="form.dictType"
                  :allow-clear="true">
          <a-select-option v-for="(d, index) in statusOptions" :key="index" :value="d.dictValue">
            {{ d.dictLabel }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="人员名称" prop="dictLabel">
        <a-input v-model="form.dictLabel" placeholder="请输入" :maxLength="30"/>
      </a-form-model-item>
      <a-form-model-item label="手机号" prop="phone">
        <a-input v-model="form.phone" placeholder="请输入"/>
      </a-form-model-item>
      <a-form-model-item label="地址" prop="addr">
        <a-input v-model="form.addr" placeholder="请输入"/>
      </a-form-model-item>
      <div class="bottom-control">
        <a-space>
          <a-button type="primary" @click="submitForm">
            保存
          </a-button>
          <a-button type="dashed" @click="onClose">
            取消
          </a-button>
        </a-space>
      </div>
    </a-form-model>
  </a-modal>
</template>

<script>

import {addSaleBuyPersonData, getData, updateData} from '@/api/system/dict/data'

export default {
  name: 'CreateForm',
  props: {
    statusOptions: {
      type: Array,
      required: true
    }
  },
  components: {},
  data() {
    return {
      // 表单参数
      form: {
        dictCode: undefined,
        dictType: undefined,
        dictLabel: undefined,
        phone: undefined,
        addr: undefined
      },
      // 显示控制
      open: false,
      // 校验规则
      rules: {
        dictType: [
          {required: true, message: '人员类型不能为空', trigger: 'blur'}
        ],
        dictLabel: [
          {required: true, message: '人员名称', trigger: 'blur'}
        ],
        phone: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请正确填写手机号',
            trigger: 'blur'
          }
        ]
      },
      // 标题
      formTitle: ''
    }
  },
  filters: {},
  created() {
  },
  computed: {},
  watch: {},
  methods: {
    // 取消按钮
    onClose() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        dictCode: undefined,
        dictType: undefined,
        dictLabel: undefined,
        phone: undefined,
        addr: undefined
      }
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.formTitle = '新增人员'
      this.open = true
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
      // this.$emit('select-tree')
      const dictCode = row ? row.id : ids
      getData(dictCode).then(response => {
        console.log(response.data)
        response.data.phone = JSON.parse(response.data.remark).phone
        response.data.addr = JSON.parse(response.data.remark).addr
        this.form = response.data
        this.formTitle = '修改人员信息'
        this.open = true
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.dictCode !== undefined) {
            this.form.remark = JSON.stringify({'phone': this.form.phone, 'addr': this.form.addr})
            console.log('this.form = ', this.form)
            updateData(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else {
            this.form.remark = JSON.stringify({'phone': this.form.phone, 'addr': this.form.addr})
            console.log('this.form = ', this.form)
            addSaleBuyPersonData(this.form).then(response => {
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
    }
  }
}
</script>
