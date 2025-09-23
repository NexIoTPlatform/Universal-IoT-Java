<template>
  <a-modal
    width="35%"
    :title="title"
    :visible="showDialog"
    @ok="submitForm"
    @cancel="cancel"
  >
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item label="父级分组名称" prop="parentId" v-if="formType!==1">
        <template>
          <a-tree-select
            v-model="form.parentId"
            :treeData="groupTree"
            :show-search="false"
            @change="changeValue"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            placeholder="请选择父级分组名称"
            allow-clear
            :disabled="treeDisabled"
          >
          </a-tree-select>
        </template>
      </a-form-model-item>
      <a-form-model-item label="分组名称" prop="groupName">
        <a-input v-model="form.groupName" placeholder="请输入分组名称" allow-clear/>
      </a-form-model-item>
      <a-form-model-item label="分组描述" prop="groupDescribe">
        <a-input v-model="form.groupDescribe" placeholder="请输入分组描述" type="textarea"
                 allow-clear/>
      </a-form-model-item>
    </a-form-model>
  </a-modal>
</template>

<script>
import {addGroup, updateGroup} from '@/api/system/dev/devGroup'

export default {
  name: 'GroupForm',
  props: {
    // 1增加根节点,2增加子节点,3修改分组,
    formType: {
      type: Number,
      default: 0
    },
    showDialog: {
      type: Boolean,
      default: false
    },
    groupTree: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      // 是否禁用树
      treeDisabled: false,
      // 标题
      title: '',
      // 控制显示
      confirmLoading: false,

      // 表单参数
      form: {
        id: null,
        groupName: null,
        groupCode: null,
        groupDescribe: null,
        hasChild: undefined,
        groupLevel: undefined,
        parentId: null
      },
      rules: {
        groupName: [
          {required: true, message: '分组名称不能为空', trigger: 'blur'},
          {min: 0, max: 50, message: '输入不能超过50个字符'}
        ]
      }
    }
  },
  filters: {},
  created() {
  },
  computed: {},
  watch: {
    showDialog(val) {
    },
    form: {
      handler(val) {
      },
      deep: true
    },
    'form.parentId': {
      handler(val) {
      },
      deep: true
    }
  },
  mounted() {
  },
  methods: {
    // 取消按钮
    cancel() {
      this.$emit('update:showDialog', false)
      this.$emit('update:formType', 0)
      this.reset()
    },
    // 表单重置
    reset() {
      this.treeDisabled = false
      this.form = {
        id: null,
        groupName: null,
        groupCode: null,
        groupDescribe: null,
        hasChild: undefined,
        groupLevel: undefined,
        parentId: null
      }
    },
    /** 新增根分组按钮操作 */
    handleAdd(data, id) {
      this.reset()
      if (data === 1) {
        this.title = '新增根分组'
      } else if (data === 2) {
        this.title = '新增子分组'
        this.form.parentId = id
      }
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      this.title = '修改分组'
      this.form = row
      this.treeDisabled = true
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id !== undefined && this.form.id !== null) {
            updateGroup(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.cancel()
              this.$emit('reset')
            })
          } else {
            if (this.form.parentId === null || this.form.parentId === undefined) {
              this.form.parentId = 0
            }
            addGroup(this.form).then(response => {
              this.$message.success(
                '新增成功',
                3
              )
              this.cancel()
              this.$emit('ok')
            })
          }
        } else {
          return false
        }
      })
    },
    changeValue(e) {
    }
  }
}
</script>
