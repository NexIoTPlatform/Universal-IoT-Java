<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>{{ formTitle }}</b>
    </a-divider>
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item label="上级分类" prop="parentId">
        <a-tree-select
          v-model="form.parentId"
          style="width: 100%"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          :tree-data="sortOptions"
          placeholder="请选择"
          :replaceFields="{children:'children', title:'classifiedName', key:'id', value: 'id' }"
          :disabled="form.id"
          tree-default-expand-all
        >
        </a-tree-select>
      </a-form-model-item>
      <a-form-model-item label="是否有子节点" prop="hasChild" v-show="false">
        <a-input v-model="form.hasChild" placeholder="请输入是否有子节点"/>
      </a-form-model-item>
      <a-form-model-item label="分类名称" prop="classifiedName">
        <a-input v-model="form.classifiedName" placeholder="请输入分类名称"/>
      </a-form-model-item>
      <a-form-model-item label="英文标识" prop="identification">
        <a-input v-model="form.identification" placeholder="请输入英文标识"/>
      </a-form-model-item>
      <a-form-model-item label="说明" prop="description">
        <a-input v-model="form.description" placeholder="请输入说明"/>
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
import {addSort, getSort, updateSort} from '@/api/system/dev/sort'

export default {
  name: 'CreateForm',
  props: {
    sortOptions: {
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

        parentId: null,

        hasChild: null,

        identification: null,

        classifiedName: null,

        description: null,

        createTime: null,

        createBy: null

      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        identification: [
          {required: true, message: '标识不能为空', trigger: 'blur'}
        ],

        classifiedName: [
          {required: true, message: '分类名称不能为空', trigger: 'blur'}
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
      this.formType = 1
      this.form = {
        id: null,

        parentId: null,

        hasChild: null,

        identification: null,

        classifiedName: null,

        description: null,

        createTime: null,

        createBy: null

      }
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset()
      this.$emit('select-tree')
      if (row != null && row.id) {
        this.form.parentId = row.id
      } else {
        this.form.parentId = 0
      }
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.reset()
      this.$emit('select-tree')
      this.formType = 2
      const id = row ? row.id : ids
      getSort(id).then(response => {
        this.form = response.data
        this.open = true
        this.formTitle = '修改'
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.parentId.length === 9) {
            this.$message.error('分类最多为三级')
          } else {
            if (this.form.id !== undefined && this.form.id !== null) {
              updateSort(this.form).then(response => {
                this.$message.success(
                  '修改成功',
                  3
                )
                this.open = false
                this.$emit('ok')
              })
            } else {
              addSort(this.form).then(response => {
                this.$message.success(
                  '新增成功',
                  3
                )
                this.open = false
                this.$emit('ok')
              })
            }
          }
        } else {
          return false
        }
      })
    }
  }
}
</script>
