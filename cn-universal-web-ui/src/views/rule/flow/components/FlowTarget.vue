<template>
  <div>
    <div class="top">
      <a-button type="primary" @click="addTargets()">添加</a-button>
      <span>最多添加10个转发目标</span>
    </div>
    <a-table
      rowKey="id"
      :columns="columns"
      :data-source="dataForm.config.targets"
      :showHeader="false"
      :pagination="false">
      <span slot="type" slot-scope="text, record">
        {{ convertTargetType(record) }}
      </span>
      <span slot="action" slot-scope="text, record" :key="record.id">
        <a @click="openCallTest(record)">测试</a>
        <a-divider type="vertical"/>
        <a @click="modify(record)">{{ $t('button.edit') }}</a>
        <a-divider type="vertical"/>
        <a @click="deleteTarget(record)">{{ $t('button.delete') }}</a>
      </span>
    </a-table>

    <a-modal v-model="showModal" title="添加转发目标" @ok="handleOk">
      <a-form-model
        :model="targetForm"
        ref="targetForm"
        :rules="targetRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 12 }">

        <a-form-model-item label="转发目标" prop="type">
          <a-select v-model="targetForm.type" placeholder="请选择转发目标">
            <a-select-option v-for="(d, index) in targetTypes" :key="index" :value="d.value">
              {{ d.name }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item label="推送URL" prop="url"
                           extra="http推送，请求方法要求POST，数据格式为application/json">
          <a-input v-model="targetForm.url" placeholder="请输入转发URL"></a-input>
        </a-form-model-item>

      </a-form-model>
    </a-modal>

    <a-drawer
      width="560"
      title="连通性测试"
      placement="right"
      :closable="true"
      :visible="showDrawer"
      @close="showDrawer = false"
    >
      <div class="block">
        <p class="test-title">测试数据：</p>
        <div style="width: 100%; height: 320px">
          <CodeEditor :key="targetForm.id" v-model="transmitForm.param" language="json"/>
        </div>
      </div>

      <div class="block" style="margin-top: 20px">
        <p class="test-title">测试结果：</p>
        <a-input v-model="transmitResult" type="textarea" class="textarea" :rows="10"
                 disabled></a-input>
      </div>

      <a-divider/>
      <a-button type="primary" @click="submitTest()">连通性测试</a-button>

    </a-drawer>
  </div>
</template>

<script>
import CodeEditor from '@/components/CodeEditor.vue'
import {testTarget, updateRuleTargets} from '@/api/rule/rule'
import cloneDeep from 'lodash.clonedeep'

export default {
  name: 'FlowTarget',
  components: {
    CodeEditor
  },
  // eslint-disable-next-line vue/require-prop-types
  props: ['current', 'data'],
  data() {
    return {
      showModal: false,
      showDrawer: false,
      dataForm: {},
      targetForm: {
        id: undefined,
        type: 'http',
        url: ''
      },
      transmitForm: {
        param: '{\n' +
          '    "data":"不要告诉我爱你"\n' +
          '}',
        ruleTarget: {}
      },
      targetTypes: [
        {name: '第三方应用服务(HTTP推送)', value: 'http'}
      ],
      transmitResult: '',
      columns: [
        {
          title: '转发目标',
          dataIndex: 'type',
          ellipsis: true,
          width: '25%',
          align: 'left',
          scopedSlots: {customRender: 'type'}
        },
        {
          title: '推送URL',
          dataIndex: 'url',
          ellipsis: true,
          width: '50%',
          align: 'left'
        },
        {
          title: this.$t('user.operation'),
          key: 'action',
          width: '25%',
          align: 'center',
          scopedSlots: {customRender: 'action'}
        }
      ],
      targetRules: {
        type: [
          {required: true, message: '请选择转发目标', trigger: 'change'}
        ],
        url: [
          {required: true, message: '请输入转发地址', trigger: 'blur'},
          {type: 'url', message: 'URL格式不正确', trigger: 'blur'}
        ]
      }
    }
  },
  watch: {
    data: {
      handler: function (val, oldvalue) {
        this.dataForm = val
      },
      deep: true
    }
  },
  created() {
    this.dataForm = this.data
  },
  methods: {
    convertTargetType(row) {
      let text = ''
      this.targetTypes.forEach(t => {
        if (t.value === row.type) {
          text = t.name
        }
      })
      return text
    },
    resetTargetForm() {
      this.targetForm = {
        id: undefined,
        type: 'http',
        url: ''
      }
    },
    openCallTest(row) {
      this.showDrawer = true
      this.transmitResult = ''
      this.transmitForm.ruleTarget = row
    },
    addTargets() {
      if (this.dataForm.config.targets.length >= 10) {
        this.$message.error('最多添加10个转发目标')
        return
      }
      this.showModal = true
      this.resetTargetForm()
    },
    handleOk() {
      this.$refs.targetForm.validate(valid => {
        if (valid) {
          const data = this.dataForm
          const form = this.targetForm
          if (form.id) {
            data.config.targets = data.config.targets.map(item => {
              if (item.id === form.id) {
                item = {...item, ...form}
              }
              return item
            })
          } else {
            form.id = this.guid()
            data.config.targets.push(cloneDeep(form))
          }
          this.updateTargets(data).then(() => {
            this.showModal = false
          })
        } else {
          return false
        }
      })
    },
    updateTargets(data) {
      return new Promise(resolve => {
        updateRuleTargets(data).then(res => {
          this.$emit('updateTargets', data.config.targets)
          resolve()
        })
      })
    },
    guid() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0
        const v = c === 'x' ? r : (r & 0x3 | 0x8)
        return v.toString(16)
      })
    },
    modify(record) {
      this.targetForm = cloneDeep(record)
      this.showModal = true
    },
    deleteTarget(record) {
      const data = this.dataForm
      data.config.targets.map((item, idx) => {
        if (item.id === record.id) {
          data.config.targets.splice(idx, 1)
        }
      })
      this.updateConfig(data)
    },
    submitTest() {
      testTarget(this.transmitForm).then(res => {
        this.transmitResult = res.msg
      })
    }
  }
}
</script>

<style scoped lang="less">
.top {
  display: flex;
  align-items: center;

  & > span {
    margin-left: 10px;
    color: #999;
  }
}

.test-title {
  // font-size: 16px;
  margin-bottom: 0;
  font-weight: bold;
}

.textarea {
  cursor: text;
  color: #333;
}
</style>
