<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>{{ formTitle }}</b>
    </a-divider>
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item label="订阅级别" prop="subType">
        <a-select disabled placeholder="请选择订阅级别" size="large" v-model="form.subType"
                  style="width: 100%">
          <a-select-option v-for="(d, index) in subscribeLevel" :key="index" :value="d.dictValue">{{
              d.dictLabel
            }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="消息类型" prop="msgType">
        <a-select placeholder="请选择消息类型" size="large" v-model="form.msgType"
                  style="width: 100%">
          <a-select-option v-for="(d, index) in subscribeMessageType" :key="index"
                           :value="d.dictValue">{{
              d.dictLabel
            }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="目标地址" prop="url">
        <a-input v-model="form.url" placeholder="请输入目标地址(http/https)"/>
        <a-button type="primary" @click="checkUrl">验证连接</a-button>
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
import {addSubscribe} from '@/api/system/dev/subscribe'
import {checkUrl} from '@/api/application/application'

export default {
  name: 'SubscribeCreateForm',
  props: {
    devId: {
      type: String,
      default: '',
      require: true
    },
    productKey: {
      type: String,
      default: '',
      require: true
    },
    subscribeMessageType: {
      type: Array,
      default: () => {
      },
      require: true
    },
    subscribeLevel: {
      type: Array,
      default: () => {
      },
      require: true
    }
  },
  components: {},
  data() {
    return {
      formTitle: '',
      // 表单参数
      form: {
        url: undefined,
        msgType: 'ALL',
        subType: 'DEVICE'
      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        subType: [
          {required: true, message: '消息类型不能为空', trigger: 'blur'}
        ],
        url: [
          {
            required: true,
            message: '推送地址不能为空',
            trigger: 'blur'
          },
          {
            match: true,
            // eslint-disable-next-line no-useless-escape
            pattern: '((http|ftp|https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?',
            message: '地址不合法'
          }
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
      this.reset()
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
        url: undefined,
        msgType: 'ALL',
        subType: 'DEVICE'
      }
    },
    /** 推送地址测试 */
    checkUrl() {
      this.$refs.form.validate(valid => {
        if (valid) {
          checkUrl({notifyUrl: this.form.url}).then(response => {
            if (response.msg === '200') {
              this.$message.success('该地址可正常访问')
            } else {
              this.$message.error(response.msg)
            }
          })
        }
      })
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset()
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
    },
    /** 修改信息操作 */
    handleUpdate(row, id) {
      this.reset()
      this.formType = 2
      this.open = true
      this.formTitle = '修改'
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.form.iotId = this.devId
          this.form.productKey = this.productKey
          addSubscribe(this.form).then(response => {
            this.$message.success(
              '新增成功',
              3
            )
            this.open = false
            this.$emit('ok')
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
