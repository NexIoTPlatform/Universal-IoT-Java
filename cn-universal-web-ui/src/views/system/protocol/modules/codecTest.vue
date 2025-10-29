<template>
  <a-drawer width="55%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>{{ formTitle }}</b>
    </a-divider>
    <a-form-model ref="form">
      <div>
        <a-tabs v-model="activeKey">
          <a-tab-pane key="1" tab="解码">
            <div style="height: 200px;width:600px">
              <CodeEditor
                v-if="open"
                v-model="form.payloadDe"
                language="plaintext"
              />
            </div>
            <button @click="messageCodec(true)">解码测试</button>
          </a-tab-pane>
          <a-tab-pane key="2" tab="编码">
            <div style="height: 200px;width:600px">
              <CodeEditor
                v-if="open"
                v-model="form.payloadEn"
                language="json"
              />
            </div>
            <button @click="messageCodec(false)">编码测试</button>
          </a-tab-pane>
        </a-tabs>
        <br><br>
        <p>运行结果</p>
        <json-viewer :value="result" :expand-depth="3" copyable>
          <template v-slot:copy>复制结果</template>
        </json-viewer>
      </div>
      <div class="bottom-control">
        <a-space>
          <a-button type="primary" @click="cancel">
            关闭
          </a-button>
        </a-space>
      </div>
    </a-form-model>
  </a-drawer>
</template>

<script>
import {messageCodec} from '@/api/system/protocol'
import CodeEditor from '@/components/CodeEditor.vue'
import JsonViewer from 'vue-json-viewer'

export default {
  name: 'CodecTest',
  props: {},
  components: {
    CodeEditor,
    JsonViewer
  },
  data() {
    return {
      // decode: true,
      payload: undefined,
      result: '',
      activeKey: '1',
      type: undefined,
      buttonLoading: false,
      loading: false,
      productName: undefined,
      formTitle: '',
      data: [],
      // 表单参数
      form: {
        id: undefined,
        payloadDe: '{"APPdata":"B3EAOjYAAoUxxxx"}',
        payloadEn: '{\n' +
          '\t"messageType": "FUNCTIONS",\n' +
          '\t"function": "DEV_MONITOR_TURN",\n' +
          '\t"data": {}\n' +
          '}',
        name: null,

        description: null,

        state: 1,

        type: undefined,

        configuration: null,
        fileName: undefined,
        url: undefined,
        jscript: undefined

      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {},
      options: {
        enableBasicAutocompletion: true, // 启用基本自动补全
        enableSnippets: true, // 启用代码段
        enableLiveAutocompletion: true, // 启用实时自动完成
        // printMarginColumn: 30,
        // displayIndentGuides: false, // 显示参考线
        enableEmmet: true, // 启用Emmet
        // tabSize: 6, // 标签大小
        // fontSize: 14, // 设置字号
        // useWorker: true, // 使用辅助对象
        // showPrintMargin: false, // 去除编辑器里的竖线
        // enableMultiselect: true, //     选中多处
        // readOnly: false, // 是否只读
        showFoldWidgets: true, // 显示折叠部件
        fadeFoldWidgets: true // 淡入折叠部件
        // wrap: true // 换行

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
    reset() {
      this.form.id = undefined
      this.form.payloadDe = '{"APPdata":"B3EAOjYAAoUxxxx"}'
      this.form.payloadEn = '{\n' +
        '\t"function": {\n' +
        '\t\t"messageType": "FUNCTIONS",\n' +
        '\t\t"function": "",\n' +
        '\t\t"data": {}\n' +
        '\t}\n' +
        '}'
      this.result = ''
    },
    messageCodec(decode) {
      if (decode) {
        messageCodec({id: this.form.id, payload: this.form.payloadDe, decode: decode}).then(
          response => {
            if (response.data === null) {
              this.result = response.msg
            } else {
              this.result = JSON.parse(response.data)
            }
          }).catch(e => {
        })
      } else {
        messageCodec({id: this.form.id, payload: this.form.payloadEn, decode: decode}).then(
          response => {
            this.result = response.msg
          })
      }
    },
    onClose() {
      this.open = false
      this.reset()
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    /** 新增按钮操作 */
    openCodecTest(row) {
      this.form.id = row.id
      if (row.example) {
        const ex = JSON.parse(row.example)
        this.form.payloadDe = ex.decode ? ex.decode : this.form.payloadDe
        this.form.payloadEn = ex.encode ? ex.encode : this.form.payloadEn
      }
      this.open = true
      this.formTitle = '编解码测试'
    }
  }
}
</script>
