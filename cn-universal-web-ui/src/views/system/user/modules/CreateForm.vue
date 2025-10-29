<template>
  <!-- 增加修改 -->
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>{{ formTitle }}</b>
    </a-divider>
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item label="用户昵称" prop="alias">
        <a-input v-model="form.alias" placeholder="请输入" :maxLength="30"/>
      </a-form-model-item>
      <a-form-model-item label="手机号" prop="mobile">
        <a-input v-model="form.mobile" placeholder="请输入"/>
      </a-form-model-item>
      <a-form-model-item label="邮箱" prop="email">
        <a-input v-model="form.email" placeholder="请输入"/>
      </a-form-model-item>
      <a-form-model-item label="用户名" prop="username" v-if="form.id == undefined">
        <a-input v-model="form.username" placeholder="请输入"/>
      </a-form-model-item>
      <a-form-model-item label="密码" prop="password" v-if="form.id == undefined">
        <a-input-password v-model="form.password" placeholder="请输入" :maxLength="20"/>
      </a-form-model-item>
      <a-form-model-item :label="$t('common.status')" prop="status">
        <a-radio-group v-model="form.status" button-style="solid">
          <a-radio-button v-for="(d, index) in statusOptions" :key="index" :value="d.dictValue">{{
              d.dictLabel
            }}
          </a-radio-button>
        </a-radio-group>
      </a-form-model-item>
      <a-form-model-item label="角色" prop="roleIds">
        <a-select
          mode="multiple"
          v-model="form.roleIds"
          placeholder="请选择"
        >
          <a-select-option v-for="(d,index) in roleOptions" :key="index" :value="d.roleId">
            {{ d.roleName }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item :label="$t('common.remark')" prop="remark">
        <a-input v-model="form.remark" placeholder="请输入" type="textarea" allow-clear/>
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
  </a-drawer>
</template>

<script>

import {addUser, getUser, updateUser} from '@/api/system/user'
import {validPwdLevel2} from '@/utils/validate'

export default {
  name: 'CreateForm',
  props: {
    statusOptions: {
      type: Array,
      required: true
    },
    sexOptions: {
      type: Array,
      required: true
    }
  },
  components: {},
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (!validPwdLevel2(value)) {
        callback(new Error('密码中必须包含字母、数字、特殊字符，至少8个字符，最多30个字符'))
      } else {
        callback()
      }
    }
    return {
      replaceFields: {children: 'children', title: 'label', key: 'id', value: 'id'},
      // 岗位选项
      postOptions: [],
      // 角色选项
      roleOptions: [],
      // 默认密码
      initPassword: undefined,
      formTitle: '',
      // 表单参数
      form: {
        id: undefined,
        username: undefined,
        alias: undefined,
        password: undefined,
        mobile: undefined,
        email: undefined,
        status: '0',
        remark: undefined,
        roleIds: []
      },
      open: false,
      rules: {
        username: [
          {required: true, message: '用户名不能为空', trigger: 'blur'}
        ],
        alias: [
          {required: true, message: '用户昵称不能为空', trigger: 'blur'}
        ],
        roleIds: [
          {required: true, message: '请选择角色', trigger: 'blur'}
        ],
        password: [
          {required: true, validator: validatePass, trigger: 'blur'}
          // { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
        ],
        email: [
          {
            type: 'email',
            message: '请正确填写邮箱地址',
            trigger: ['blur', 'change']
          }
        ],
        mobile: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请正确填写手机号',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    onClose() {
      this.open = false
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        username: undefined,
        alias: undefined,
        password: undefined,
        mobile: undefined,
        email: undefined,
        status: '0',
        remark: undefined,
        roleIds: []
      }
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      // this.$emit('select-tree')
      getUser().then(response => {
        // this.postOptions = response.posts
        this.roleOptions = response.data.roles
        this.open = true
        this.formTitle = '新增用户'
        this.form.password = ''
      })
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.reset()
      // this.$emit('select-tree')
      const userId = row ? row.id : ids
      getUser(userId).then(response => {
        // console.log(response.data)
        this.form = response.data.data
        // this.form.status = this.form.status.toString()
        // this.postOptions = response.posts
        // this.form.postIds = response.postIds
        this.roleOptions = response.data.roles
        // this.form.roleIds = response.data.roleIds
        this.open = true
        this.formTitle = '修改用户'
        this.form.password = ''
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateUser(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else {
            addUser(this.form).then(response => {
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
