<template>
  <div>
    <a-modal width="50%" :visible="open" @cancel="cancel" @ok="submitForm">
      <a-divider orientation="left">
        <b>{{ formTitle }}</b>
      </a-divider>
      <a-form-model ref="form" :model="form" :rules="rules">
        <a-row>
          <a-col :span="12">
            <a-form-model-item :label="$t('linkage.sceneName')" prop="sceneName">
              <a-input v-model="form.sceneName" placeholder="请输入场景名称" style="width: 80%"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="沉默周期(s)" prop="sleepCycle">
              <a-input v-model="form.sleepCycle" placeholder="请输入沉默周期（单位秒）"
                       style="width: 80%"/>
            </a-form-model-item>
          </a-col>
        </a-row>
        <!--        <a-form-model-item label="触发条件" prop="triggerCondition">-->
        <!--          <a-input v-model="form.triggerCondition" placeholder="请输入内容" type="textarea" allow-clear/>-->
        <!--        </a-form-model-item>-->
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="触发条件">
              需满足以下
              <a-select
                v-model="form.touch"
                @change="onChangeTouch"
                placeholder="请选择"
                style="width: 100px;"
                size="small">
                <a-select-option
                  v-for="(item) in touches"
                  :key="item.value"
                  :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
              条件：
              <div class="tagger" v-if="open" v-for="(item,index) in taggers" :key="index">
                <strong>触发器{{ index + 1 }}</strong>
                <a-button type="text" style="float:right;" @click="delTagger(index)">{{ $t('button.delete') }}</a-button>
                <tagger
                  v-model="taggers[index]"
                  :index="index"
                  :touch="form.touch"
                  :reset="resetNum"
                  @filter-change="onFilterChange"/>
              </div>
              <a-button type="text" @click="addTagger">+ 新增触发器</a-button>
            </a-form-model-item>
          </a-col>
        </a-row>

        <a-row>
          <a-col :span="24">
            <a-form-model-item label="执行动作">
              <div class="tagger" v-if="open" v-for="(item,index) in actions" :key="index">
                <strong>动作{{ index + 1 }}</strong>
                <a-button type="text" style="float:right;" @click="delAction(index)">{{ $t('button.delete') }}</a-button>
                <exec
                  ref="exec"
                  v-model="actions[index]"
                  :index="index"
                  :reset="resetActionNum"
                  @filter-change="onActionChange"/>
              </div>
              <a-button type="text" @click="addAction">+ 新增动作</a-button>
            </a-form-model-item>
          </a-col>
        </a-row>
        <!--        <a-form-model-item label="执行动作" prop="execAction">-->
        <!--          <a-input v-model="form.execAction" placeholder="请输入内容" type="textarea" allow-clear/>-->
        <!--        </a-form-model-item>-->
        <!--        <a-form-model-item label="设备id" prop="devId">-->
        <!--          <a-input v-model="form.devId" placeholder="请输入设备id"/>-->
        <!--        </a-form-model-item>-->
      </a-form-model>
    </a-modal>
  </div>
</template>

<script>
import {addLinkage, getLinkage, manualExec, updateLinkage} from '@/api/linkage/linkage'
import Tagger from './tagger'
import Exec from './exec'
import Device from './device'

export default {
  name: 'CreateForm',
  props: {
    template: {
      type: Object,
      default: null
    }
  },
  components: {Tagger, Device, Exec},
  data() {
    return {
      loading: false,
      formTitle: '',
      taggers: [{}],
      // 执行动作
      actions: [{}],
      touches: [{
        value: 'one',
        label: '任意一个'
      }, {
        value: 'all',
        label: '全部'
      }],
      resetNum: 0,
      resetActionNum: 0,
      // 表单参数
      form: {
        id: null,

        sceneName: null,

        touch: 'one',

        triggerCondition: null,

        execAction: null,

        sleepCycle: null,

        createBy: null,

        createTime: null,

        updateBy: null,

        updateTime: null,

        status: 0,

        devId: null

      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        sceneName: [{required: true, message: '场景名不能为空', trigger: 'blur'}],
        sleepCycle: [{required: true, message: '沉默周期不能为空', trigger: 'blur'},
          {
            match: true,
            // eslint-disable-next-line no-useless-escape
            pattern: /^(0|[1-9][0-9]*)$/,
            message: '请输入0或正整数'
          }]
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

        sceneName: null,

        touch: 'one',

        triggerCondition: null,

        execAction: null,

        sleepCycle: null,

        createBy: null,

        createTime: null,

        updateBy: null,

        updateTime: null,

        status: 0,

        devId: null

      }
      this.resetTagger()
    },
    resetTagger() {
      this.taggers = [{}]
      this.actions = [{}]
      this.resetNum = this.resetNum + 1
      this.resetActionNum = this.resetActionNum + 1
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset()
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.reset()
      this.formType = 2
      const id = row ? row.id : ids
      getLinkage(id).then(response => {
        this.form = response.data
        this.taggers = JSON.parse(this.form.triggerCondition)
        this.actions = JSON.parse(this.form.execAction)
        
        // 检查并警告缺少 productKey 的情况
        this.checkMissingProductKey()
        
        this.open = true
        this.formTitle = '修改'
      })
    },
    /** 检查缺少 productKey 的情况 */
    checkMissingProductKey() {
      let hasMissingProductKey = false
      
      this.taggers.forEach((tagger, index) => {
        if (tagger.deviceId && !tagger.productKey) {
          console.warn(`触发器 ${index + 1} 缺少 productKey，deviceId: ${tagger.deviceId}`)
          hasMissingProductKey = true
        }
      })
      
      this.actions.forEach((action, index) => {
        if (action.deviceId && !action.productKey) {
          console.warn(`动作 ${index + 1} 缺少 productKey，deviceId: ${action.deviceId}`)
          hasMissingProductKey = true
        }
      })
      
      if (hasMissingProductKey) {
        this.$message.warning('检测到部分设备缺少产品Key，可能影响编辑功能。建议重新选择设备以确保功能正常。')
      }
    },
    /** 手动触发按钮操作 */
    manualExec(row, ids) {
      const id = row.id
      manualExec(id).then(response => {
        // const result = JSON.parse(response.msg)
        this.$message.success(response.msg)
        // this.$message.success(result.length + '条下发指令失败')

      })
    },

    onChangeTouch(data) {
      this.resetNum = this.resetNum + 1
      this.taggers = [{}]
    },
    addTagger() {
      this.taggers.push({})
    },
    addAction() {
      this.actions.push({})
    },
    delTagger(index) {
      this.taggers.splice(index, 1)
    },
    delAction(index) {
      this.actions.splice(index, 1)
    },
    onFilterChange(index, value) {
      this.taggers[index].filters = value
    },
    onActionChange(index, value) {
      this.actions[index].filters = value
    },
    /** 提交按钮 */
    submitForm: function () {
      // 确保 productKey 被正确设置
      this.ensureProductKeyInData()
      
      this.form.triggerCondition = this.taggers
      this.form.execAction = this.actions
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id !== undefined && this.form.id !== null) {
            updateLinkage(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else {
            addLinkage(this.form).then(response => {
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
    /** 确保数据中包含 productKey */
    ensureProductKeyInData() {
      // 处理触发器数据
      this.taggers.forEach(tagger => {
        if (tagger.deviceId && !tagger.productKey) {
          console.warn(`触发器缺少 productKey，deviceId: ${tagger.deviceId}`)
          // 这里可以尝试从设备选择组件获取 productKey
          // 或者提示用户重新选择设备
        }
      })
      
      // 处理执行动作数据
      this.actions.forEach(action => {
        if (action.deviceId && !action.productKey) {
          console.warn(`动作缺少 productKey，deviceId: ${action.deviceId}`)
          // 这里可以尝试从设备选择组件获取 productKey
          // 或者提示用户重新选择设备
        }
      })
    }
  }
}
</script>
<style scoped>
.tagger {
  margin-top: 5px;
  margin-bottom: 5px;
  background-color: #f7fbff;
  padding: 10px 20px;
}
</style>
