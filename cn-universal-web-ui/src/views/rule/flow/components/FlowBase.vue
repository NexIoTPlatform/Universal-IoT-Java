<template>
  <div>
    <a-form-model :model="form" ref="form" :rules="rules" :label-col="{ span: 2 }"
                  :wrapper-col="{ span: 10 }">

      <a-form-model-item label="规则名称" prop="ruleName">
        <div class="sqlRules">
          <a-input v-model="form.ruleName" placeholder="请输入规则名称" :maxLength="100"/>
          <div class="tips">
            <p>
              规则引擎是数据订阅之上基于SQL的核心数据处理与分发组件，可以基于SQL筛选满足条件的应用消息，并将数据分发移动到HTTP
              Server、数据库、消息队列甚至是另一个MQTT Broker中。
            </p>
            <p>
              1.选择应用ID为【applicationId】的所有消息，并提取全部字段:
            </p>
            <p class="tipsBox">
              SELECT * FORM applicationId
            </p>
            <p>
              2.SELECT 字段可以使用简单的内置函数和自定义字段的设置筛选条件:
            </p>
            <p class="tipsBox">
              SELECT properties.battery AS bt,iotId,toUpperCase(iotId) AS IOTID,deviceId AS
              data.deviceId<br><br>

              FROM 0lh7j3sybattoq6n<br><br>

              WHERE iotId ='ae0e2153ef644d5ea2f61668c7ac38d6' <br>
            </p>
            <p>
              3.输出结果：
              {"IOTID":"AE0E2153EF644D5EA2F61668C7AC38D6","data":{"deviceId":"1234567890"},"bt":"70","iotId":"ae0e2153ef644d5ea2f61668c7ac38d6"}
            </p>
            <p>
              未尽事宜，请联系管理员！
            </p>
          </div>
        </div>
      </a-form-model-item>
      <a-form-model-item label="数据级别" prop="dataLevel">
        <a-select
          placeholder="请选择数据级别"
          v-model="form.dataLevel"
          style="width: 100%"
          @change="dataLevelChange"
          allow-clear>
          <a-select-option v-for="(d, index) in dataLevelList" :key="index" :value="d.key">
            {{ d.value }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="产品"
        prop="productKey"
        v-if="form.dataLevel === 'product' || form.dataLevel === 'device'"
        :rules="{
          required: true,
          message: '请选择产品',
          trigger: 'change',
        }">
        <a-select
          placeholder="请选择产品"
          v-model="form.productKey"
          style="width: 100%"
          @change="productChange"
          allow-clear>
          <a-select-option v-for="(d) in productList" :key="d.productKey" :value="d.productKey">
            {{ d.name + ' ' + d.productKey }}
          </a-select-option>
        </a-select>
      </a-form-model-item>

      <a-form-model-item
        label="设备群组"
        prop="groupId"
        v-if="form.dataLevel === 'group'"
        :rules="{
          required: true,
          message: '请选择设备群组',
          trigger: 'change',
        }">
        <a-tree-select
          v-model="form.groupId"
          style="width: 100%"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          :tree-data="deviceGroup"
          placeholder="请选择设备群组"
        >
        </a-tree-select>
      </a-form-model-item>

      <a-form-model-item
        label="设备"
        prop="relationIds"
        v-if="form.dataLevel === 'device' && form.productKey"
        :rules="{
          required: true,
          message: '请选择设备',
          trigger: 'change',
        }">
        <a-select
          placeholder="请选择设备"
          mode="multiple"
          v-model="form.relationIds"
          style="width: 100%"
          allow-clear>
          <a-select-option v-for="(d) in deviceList" :key="d.iotId" :value="d.iotId">
            {{ d.deviceName }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="描述" prop="description">
        <a-textarea v-model="form.description" placeholder="请输入描述" :maxLength="255"/>
      </a-form-model-item>
      <a-form-model-item
        label="规则内容"
      >
        <!-- <div style="height: 300px">
          <MonacoEditor :key="form.id ? form.id + 1 : 1 " language="sql" v-model="form.config.sql" @change="editorChange('config.sql')"/>
        </div> -->
        <a-form-model-item
          prop="config.fields"
          :rules="{
            required: true,
            message: '查询字段不能为空',
            trigger: 'blur',
          }"
        >
          <span>SELECT</span>
          <a-textarea v-model="form.config.fields" placeholder="请输入字段"/>
        </a-form-model-item>
        <a-form-model-item
          prop="config.appId"
          :rules="{
            required: true,
            message: '应用不能为空',
            trigger: 'blur',
          }"
        >
          <span>FROM</span>
          <a-select
            placeholder="请选择应用"
            v-model="form.config.appId"
            style="width: 100%"
            allow-clear>
            <a-select-option v-for="(d, index) in list" :key="index" :value="d.appId">
              {{ d.appName }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item prop="config.condition">
          <span>WHERE</span>
          <a-textarea v-model="form.config.condition" placeholder="请填入条件"/>
        </a-form-model-item>
        <div class="sqlViews">
          <div class="viewBox">
            <h4>数据过滤语句</h4>
            <div class="viewTips">SELECT {{ form.config.fields }}</div>
            <div class="viewTips">FROM {{ form.config.appId }}</div>
            <div class="viewTips" v-if="form.config.condition">WHERE {{
                form.config.condition
              }}
            </div>
          </div>
        </div>
      </a-form-model-item>
      <a-form-model-item label="SQL 测试">
        <a-switch :checked="testSql" @change="testSqlChange"/>
      </a-form-model-item>
      <a-form-model-item
        v-if="testSql"
        label="payload"
        prop="payload"
        :rules="{
          required: true,
          message: 'payload不能为空',
          trigger: 'blur',
        }"
      >
        <div style="height: 300px">
          <CodeEditor :key="form.id ? form.id + 2 : 2" v-model="form.payload" language="json"/>
        </div>
        <a-button type="primary" @click="submitSqlTest">测试</a-button>
      </a-form-model-item>
      <a-form-model-item v-if="testSql" label="测试输出">
        <a-input v-model="testSqlResult" :rows="12" type="textarea"/>
      </a-form-model-item>
    </a-form-model>
  </div>
</template>

<script>
import CodeEditor from '@/components/CodeEditor.vue'
import {addRule, testRuleSql, updateRule} from '@/api/rule/rule'
import {listInstance} from '@/api/system/dev/instance'
import {listApplication} from '@/api/application/application'

export default {
  name: 'FlowBase',
  components: {CodeEditor},
  // eslint-disable-next-line vue/require-prop-types
  props: ['current', 'data', 'productList', 'deviceGroup'],
  data() {
    return {
      list: [
        {appName: '全部', appId: '*'}
      ],
      testSql: false,
      testSqlResult: '',
      dataLevelList: [
        {key: 'product', value: '产品'},
        {key: 'group', value: '设备群组'},
        {key: 'device', value: '设备'}
      ],
      deviceList: [],
      form: {},
      rules: {
        ruleName: [
          {required: true, message: '规则名称不能为空', trigger: 'blur'}
        ],
        dataLevel: [
          {required: true, message: '请选择数据级别', trigger: 'change'}
        ]
      }
    }
  },
  watch: {
    data(val) {
      this.form = val
    },
    'form.productKey': function (val) {
      if (val) {
        if (this.form.dataLevel === 'device') {
          listInstance({
            productKey: val,
            pageNum: 1,
            pageSize: 99999999
          }).then(res => {
            this.deviceList = res.rows
            this.$emit('loadDeviceList', this.deviceList)
          })
        }
      }
    }
  },
  created() {
    this.getList()
    this.form = this.data
  },
  methods: {
    /** 查询用户应用信息列表 */
    getList() {
      listApplication(this.queryParam).then(res => {
        this.list = this.list.concat(res.rows)
      })
    },
    testSqlChange(val) {
      this.testSql = val
    },
    editorChange(key) {
      setTimeout(() => {
        this.$refs['form'].validateField(key)
      }, 2)
    },
    dataLevelChange(val) {
      this.form.relationIds = []
      this.form.productKey = undefined
      this.form.groupId = undefined
    },
    productChange(val) {
      this.form.relationIds = []
    },
    submitRule() {
      return new Promise(resolve => {
        this.$refs.form.validate(valid => {
          if (valid) {
            addRule(this.form).then(res => {
              resolve(res.data)
            })
          } else {
            return false
          }
        })
      })
    },
    updateRule() {
      return new Promise(resolve => {
        this.$refs.form.validate(valid => {
          if (valid) {
            updateRule(this.form).then(res => {
              resolve()
            })
          } else {
            return false
          }
        })
      })
    },
    submitSqlTest() {
      this.$refs.form.validate(valid => {
        if (valid) {
          testRuleSql(this.form).then(res => {
            this.testSqlResult = JSON.stringify(res.data)
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style scoped lang='less'>
.sqlRules {
  position: relative;

  .tips {
    width: 100%;
    height: 390px;
    background-color: #fff;
    position: absolute;
    left: 110%;
    top: 0;
    border: 3px dashed #ccc;
    border-radius: 5px;
    padding: 10px;
    overflow: auto;

    p {
      font-size: 12px;
      line-height: 20px;
    }

    .tipsBox {
      font-size: 12px;
      line-height: 20px;
      background-color: #ccc;
      padding: 5px;
      border-radius: 5px;
    }
  }
}

.sqlViews {
  position: relative;

  .viewBox {
    width: 100%;
    height: 180px;
    box-sizing: border-box;
    position: absolute;
    left: 110%;
    bottom: -20px;
    // border: 1px solid #ccc;
    padding: 0 10px;
    overflow: auto;

    .viewTips {
      color: #8B5DE4;
    }
  }
}
</style>
