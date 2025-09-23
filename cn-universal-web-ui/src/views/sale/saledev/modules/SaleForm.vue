<template>
  <!-- 增加修改 -->
  <a-modal
    width="1000px"
    :title="formTitle"
    :visible="open"
    @cancel="cancel"
    @ok="submitForm"
    :maskClosable="false"
  >
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-row class="wrapper">
        <a-col :span="10">
          <a-form-model-item label="常用销售人员" prop="saleStaff">
            <a-select
              style="width: 100%"
              placeholder="请选择"
              v-model="form.saleStaff"
              :allow-clear="true"
              show-search
              option-filter-prop="children"
              :filter-option="filterOption"
              @change="saleTypeChange">
              <a-select-option v-for="(d, index) in saleStaff" :key="index" :value="d.dictValue">
                {{ d.dictLabel }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item label="人员名称" prop="salePerson">
            <a-input v-model="form.salePerson" placeholder="请输入" :maxLength="30"/>
          </a-form-model-item>
          <a-form-model-item label="手机号" prop="salePhone">
            <a-input v-model="form.salePhone" placeholder="请输入"/>
          </a-form-model-item>
          <a-form-model-item label="地址" prop="saleAddr">
            <a-input v-model="form.saleAddr" placeholder="请输入"/>
          </a-form-model-item>
        </a-col>
        <a-col :span="4" class="aa">
          <div><span class="saleLable">出售</span></div>
          <div>
            <a-icon type="transaction" style="font-size: 100px;color: #1890ff"/>
          </div>
        </a-col>
        <a-col :span="10">
          <a-form-model-item label="常用购买客户" prop="buyStaff">
            <a-select
              style="width: 100%"
              placeholder="请选择"
              v-model="form.buyStaff"
              :allow-clear="true"
              show-search
              option-filter-prop="children"
              :filter-option="filterOption"
              @change="buyTypeChange">
              <a-select-option v-for="(d, index) in buyStaff" :key="index" :value="d.dictValue">
                {{ d.dictLabel }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item label="人员名称" prop="buyPerson">
            <a-input v-model="form.buyPerson" placeholder="请输入" :maxLength="30"/>
          </a-form-model-item>
          <a-form-model-item label="手机号" prop="buyPhone">
            <a-input v-model="form.buyPhone" placeholder="请输入"/>
          </a-form-model-item>
          <a-form-model-item label="地址" prop="buyAddr">
            <a-input v-model="form.buyAddr" placeholder="请输入"/>
          </a-form-model-item>
        </a-col>
      </a-row>
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
import {editSaleDevice, saleDevice} from '@/api/sale/sale'
import moment from 'moment'

const BASE_FORM = {
  id: null,
  saleStaff: undefined,
  buyStaff: undefined,
  recode: undefined,
  salePerson: undefined,
  salePhone: undefined,
  saleAddr: undefined,
  buyPerson: undefined,
  buyPhone: undefined,
  buyAddr: undefined
}

export default {
  name: 'SaleForm',
  props: {},
  components: {},
  data() {
    return {
      saleType: 1,
      saleStaff: [],
      buyStaff: [],
      formTitle: '出售设备',
      ids: undefined,
      // 表单参数
      form: {...BASE_FORM},
      open: false,
      rules: {
        salePerson: [
          {required: true, message: '请输入名称', trigger: 'blur'}
        ],
        buyPerson: [
          {required: true, message: '请输入名称', trigger: 'blur'}
        ],
        salePhone: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请正确填写手机号',
            trigger: 'blur'
          }
        ],
        buyPhone: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请正确填写手机号',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  filters: {},
  created() {
    this.getDicts('sales_staff').then(response => {
      this.saleStaff = response.data
      console.log('this.salesStaff = ', this.salesStaff)
    })
    this.getDicts('buy_staff').then(response => {
      this.buyStaff = response.data
      console.log('this.buyStaff = ', this.buyStaff)
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
      this.reset()
    },
    // 表单重置
    reset() {
      this.saleType = 1
      this.ids = undefined
      this.form = {...BASE_FORM}
    },
    /** 更改出售按钮操作 */
    editSaleUpdate(row, ids) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.ids = ids
      this.saleType = 2
      console.log('this.ids = ', this.ids)
      this.form.buyPerson = row.buy.name
      this.form.buyPhone = row.buy.phone
      this.form.buyAddr = row.buy.addr
      this.form.salePerson = row.sale.name
      this.form.salePhone = row.sale.phone
      this.form.saleAddr = row.sale.addr
      this.open = true
    },
    /** 出售按钮操作 */
    saleUpdate(row, ids) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.ids = row ? row.id : ids
      this.saleType = 1
      console.log('this.ids = ', this.ids)
      this.open = true
    },
    /** 提交按钮 */
    submitForm: function () {
      console.log('this.form = ', this.form)
      this.$refs.form.validate(valid => {
        if (valid) {
          const msg = {
            msg: {
              sale: {
                name: this.form.salePerson,
                phone: this.form.salePhone,
                addr: this.form.saleAddr
              },
              buy: {name: this.form.buyPerson, phone: this.form.buyPhone, addr: this.form.buyAddr}
            },
            index: 'sale',
            createId: this.$store.state.user.name,
            time: moment().format('YYYY-MM-DD HH:mm:ss')
          }
          if (this.saleType === 1) {
            this.form.recode = JSON.stringify(msg)
            console.log('this.form = ', this.form)
            saleDevice(this.ids, this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else if (this.saleType === 2) {
            this.form.recode = JSON.stringify(msg)
            console.log('this.form = ', this.form)
            editSaleDevice(this.ids, this.form).then(response => {
              this.$message.success(
                '修改成功',
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
    },
    // 选择常用销售联系人
    saleTypeChange(value, option) {
      if (!value) {
        this.form.salePhone = undefined
        this.form.saleAddr = undefined
        this.form.salePerson = undefined
      } else {
        for (let i = 0; i < this.saleStaff.length; i++) {
          const item = this.saleStaff[i]
          if (item.dictValue === value) {
            this.form.salePhone = JSON.parse(item.remark).phone
            this.form.saleAddr = JSON.parse(item.remark).addr
            this.form.salePerson = item.dictLabel
            break
          }
        }
      }
    },
    // 选择常用客户联系人
    buyTypeChange(value, option) {
      if (value === undefined || value === null) {
        this.form.buyPhone = undefined
        this.form.buyAddr = undefined
        this.form.buyPerson = undefined
      } else {
        for (let i = 0; i < this.buyStaff.length; i++) {
          const item = this.buyStaff[i]
          if (item.dictValue === value) {
            this.form.buyPhone = JSON.parse(item.remark).phone
            this.form.buyAddr = JSON.parse(item.remark).addr
            this.form.buyPerson = item.dictLabel
            break
          }
        }
      }
    }
  }
}
</script>

<style scoped lang="less">
.wrapper {
  display: flex;
}

.aa {
  height: inherit;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.saleLable {
  color: rgb(255 181 181);
  font-size: 32px;
  margin-bottom: 30px;
  display: block;
  font-weight: bold;
}
</style>
