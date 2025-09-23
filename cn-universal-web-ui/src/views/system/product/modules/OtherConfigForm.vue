<template>
  <!-- 第三方平台参数配置 -->
  <a-modal
    width="1000px"
    :title="formTitle"
    :visible="open"
    @cancel="cancel"
    @ok="submitForm"
    :maskClosable="false"
  >
    <div class="config-header">
      <div class="header-info">
        <a-icon type="info-circle" class="info-icon"/>
        <span class="info-text">配置第三方平台所需的认证参数，如安全码、API密钥等</span>
      </div>
      <a-button type="primary" @click="addParams" class="add-btn">
        <a-icon type="plus"/>
        添加参数
      </a-button>
    </div>
    
    <a-form-model ref="form" :model="form" class="platform-form">

      <template v-for="(item , index) in form.arr">
        <a-row v-if="index === 0" :key="index">
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                参数键名，用于后端识别，如：appKey、securityCode
              </template>
              <a-form-model-item
                label="参数键名"
                prop="ext1Id"
                :rules="{required: true , message: '参数键名不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext1Id" placeholder="如：appKey、securityCode" class="param-input"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                参数显示名称，用户友好的中文标签
              </template>
              <a-form-model-item
                label="显示名称"
                prop="ext1Name"
                :rules="{required: true , message: '显示名称不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext1Name" placeholder="如：安全码、API密钥" class="param-input"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="7" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                参数说明，帮助用户理解如何填写
              </template>
              <a-form-model-item
                label="参数说明"
                prop="ext1Des"
                :rules="{required: true , message: '参数说明不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext1Des" placeholder="如：设备机身二维码上的安全码" class="param-input"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="4" style="margin-top: 44px">
            <a-button type="primary" @click="deleteParams(index)">删除</a-button>
          </a-col>
        </a-row>
        <a-row v-if="index === 1" :key="index">
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                对应后端字段(一般为英文字母组合)
              </template>
              <a-form-model-item
                label="英文标识"
                prop="ext2Id"
                :rules="{required: true , message: '英文标识' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext2Id" placeholder="英文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                中文标识，前端提示的名称
              </template>
              <a-form-model-item
                label="中文标识"
                prop="ext2Name"
                :rules="{required: true , message: '中文标识' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext2Name" placeholder="中文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="7" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                详细描述，如：输入规则、输入位数等等
              </template>
              <a-form-model-item
                label="详细描述"
                prop="ext2Des"
                :rules="{required: true , message: '详细描述' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext2Des" placeholder="详细描述"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="4" style="margin-top: 44px">
            <a-button type="primary" @click="deleteParams(index)">删除</a-button>
          </a-col>
        </a-row>
        <a-row v-if="index === 2" :key="index">
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                对应后端字段(一般为英文字母组合)
              </template>
              <a-form-model-item
                label="英文标识"
                prop="ext3Id"
                :rules="{required: true , message: '英文标识' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext3Id" placeholder="英文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                中文标识，前端提示的名称
              </template>
              <a-form-model-item
                label="中文标识"
                prop="ext3Name"
                :rules="{required: true , message: '中文标识' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext3Name" placeholder="中文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="7" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                详细描述，如：输入规则、输入位数等等
              </template>
              <a-form-model-item
                label="详细描述"
                prop="ext3Des"
                :rules="{required: true , message: '详细描述' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext3Des" placeholder="中文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="4" style="margin-top: 44px">
            <a-button type="primary" @click="deleteParams(index)">删除</a-button>
          </a-col>
        </a-row>
        <a-row v-if="index === 3" :key="index">
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                对应后端字段(一般为英文字母组合)
              </template>
              <a-form-model-item
                label="英文标识"
                prop="ext4Id"
                :rules="{required: true , message: '英文标识' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext4Id" placeholder="英文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="6" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                中文标识，前端提示的名称
              </template>
              <a-form-model-item
                label="中文标识"
                prop="ext4Name"
                :rules="{required: true , message: '中文标识' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext4Name" placeholder="中文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="7" style="margin-right: 10px">
            <a-tooltip>
              <template slot="title">
                详细描述，如：输入规则、输入位数等等
              </template>
              <a-form-model-item
                label="详细描述"
                prop="ext4Des"
                :rules="{required: true , message: '详细描述' + '不能为空', trigger: 'blur'}">
                <a-input v-model="form.ext4Des" placeholder="中文标识"/>
              </a-form-model-item>
            </a-tooltip>
          </a-col>
          <a-col :span="4" style="margin-top: 44px">
            <a-button type="primary" @click="deleteParams(index)">删除</a-button>
          </a-col>
        </a-row>
      </template>

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
import {getProduct, updateOtherConfig} from '@/api/system/dev/product'

export default {
  name: 'OtherConfigForm',
  props: {},
  components: {},
  data() {
    return {
      // 标题
      formTitle: '修改产品其他配置信息',
      // 显示控制
      open: false,
      // 表单参数
      form: {
        ext1Id: undefined,
        ext1Name: undefined,
        ext1Des: undefined,
        ext2Id: undefined,
        ext2Name: undefined,
        ext2Des: undefined,
        ext3Id: undefined,
        ext3Name: undefined,
        ext3Des: undefined,
        ext4Id: undefined,
        ext4Name: undefined,
        ext4Des: undefined,
        productId: undefined,
        arr: []
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
      this.form = {
        ext1Id: undefined,
        ext1Name: undefined,
        ext1Des: undefined,
        ext2Id: undefined,
        ext2Name: undefined,
        ext2Des: undefined,
        ext3Id: undefined,
        ext3Name: undefined,
        ext3Des: undefined,
        ext4Id: undefined,
        ext4Name: undefined,
        ext4Des: undefined,
        productId: undefined,
        arr: []
      }
    },
    /** 修改按钮操作 */
    handleUpdateAndAdd(id) {
      this.reset()
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      getProduct(id).then(res => {
        console.log('res = ', res)
        if (res.data.thirdConfiguration !== undefined) {
          const str = JSON.parse(res.data.thirdConfiguration)
          if (str.customField !== undefined) {
            this.form.arr = str.customField
            for (let i = 0; i < str.customField.length; i++) {
              this.form[`ext` + (i + 1) + `Id`] = str.customField[i].id
              this.form[`ext` + (i + 1) + `Name`] = str.customField[i].name
              this.form[`ext` + (i + 1) + `Des`] = str.customField[i].description
            }
          }
        }
      })
      this.form.productId = id
      this.open = true
    },
    /** 提交按钮 */
    submitForm: function () {
      var that = this
      this.$refs.form.validate(valid => {
        if (valid) {
          updateOtherConfig(this.form).then((res) => {
            console.log('res = ', res)
            that.$message.success(
              '修改成功',
              3
            )
            this.open = false
            this.$emit('ok')
          })
        } else {
          return false
        }
      })
    },
    // 删除
    deleteParams(index) {
      if (index === 0) {
        this.form.ext1Id = this.form.ext2Id
        this.form.ext1Name = this.form.ext2Name
        this.form.ext1Des = this.form.ext2Des
        this.form.ext2Id = this.form.ext3Id
        this.form.ext2Name = this.form.ext3Name
        this.form.ext2Des = this.form.ext3Des
        this.form.ext3Id = this.form.ext4Id
        this.form.ext3Name = this.form.ext4Name
        this.form.ext3Des = this.form.ext4Des
      } else if (index === 1) {
        this.form.ext2Id = this.form.ext3Id
        this.form.ext2Name = this.form.ext3Name
        this.form.ext2Des = this.form.ext3Des
        this.form.ext3Id = this.form.ext4Id
        this.form.ext3Name = this.form.ext4Name
        this.form.ext3Des = this.form.ext4Des
      } else if (index === 2) {
        this.form.ext3Id = this.form.ext4Id
        this.form.ext3Name = this.form.ext4Name
        this.form.ext3Des = this.form.ext4Des
      }
      this.form.ext4Id = undefined
      this.form.ext4Name = undefined
      this.form.ext4Des = undefined
      this.form.arr.splice(index, 1)
    },
    // 增加参数
    addParams() {
      if (this.form.arr.length >= 4) {
        this.$message.error(`最多只能设置 4 个额外参数!`)
        return
      }
      this.form.arr.push({id: undefined, name: undefined, description: undefined})
      console.log(this.form)
    }
  }
}
</script>

<style lang="less" scoped>
.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #1890ff;

  .header-info {
    display: flex;
    align-items: center;
    flex: 1;

    .info-icon {
      color: #1890ff;
      margin-right: 8px;
      font-size: 16px;
    }

    .info-text {
      color: #666;
      font-size: 14px;
    }
  }

  .add-btn {
    height: 32px;
    border-radius: 4px;
  }
}

.platform-form {
  .param-input {
    border-radius: 4px;
    
    &:focus {
      border-color: #1890ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }

  .ant-form-item-label {
    font-weight: 500;
    color: #333;
  }

  .ant-row {
    margin-bottom: 16px;
    padding: 16px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    background: #fafafa;
    transition: all 0.3s ease;

    &:hover {
      border-color: #1890ff;
      background: #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }

  .ant-btn {
    border-radius: 4px;
    
    &.ant-btn-primary {
      background: #1890ff;
      border-color: #1890ff;
      
      &:hover {
        background: #40a9ff;
        border-color: #40a9ff;
      }
    }
  }
}
</style>
