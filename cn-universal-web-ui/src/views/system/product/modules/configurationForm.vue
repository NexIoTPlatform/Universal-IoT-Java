<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-spin :spinning="loading" tip="Loading...">
      <a-divider orientation="left">
        <b>{{ formTitle }}</b>
      </a-divider>
      <a-form-model ref="form" :model="form" :rules="rules">
        <!-- 遍历组件 -->
        <template v-for="item in configurationArray">

          <!-- boolean 值渲染 -->
          <a-form-model-item
            v-if="item.valueType === 'boolean' && item.hide !== 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-radio-group v-model="form[item.key]" :name="item.key">
              <a-radio :value="true">
                true
              </a-radio>
              <a-radio :value="false">
                false
              </a-radio>
            </a-radio-group>
          </a-form-model-item>

          <!-- 下拉框渲染 -->
          <a-form-model-item
            v-else-if="item.valueType === 'select' && item.hide !== 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-select
              show-search
              v-model="form[item.key]"
              :placeholder="`请选择${item.name}`"
              option-filter-prop="children"
              style="width: 200px"
            >
              <template v-for="itemSelect in item.valueData">
                <a-select-option :value="itemSelect.dictValue" :key="itemSelect.dictValue">
                  {{ itemSelect.dictLabel }}
                </a-select-option>
                <!--              <a-select-option value="jack">
                                Jack
                              </a-select-option>
                              <a-select-option value="lucy">
                                Lucy
                              </a-select-option>
                              <a-select-option value="tom">
                                Tom
                              </a-select-option>-->
              </template>
            </a-select>
          </a-form-model-item>

          <!-- 字符串渲染 -->
          <a-form-model-item
            v-else-if="item.valueType === 'string' && item.hide !== 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <!-- 字符串渲染 -->
          <a-form-model-item
            v-else-if="item.valueType === 'string' && item.hide !== 'true'"
            :key="item.key"
            :prop="item.key">
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <!-- 数字渲染 -->
          <a-form-model-item
            v-else-if="item.valueType === 'int' && item.hide !== 'true'"
            :key="item.key"
            :prop="item.key">
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" type="number" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
        </template>

        <a-button v-if="proType === 'tcp'" type="primary" @click="highConfig"
                  style="margin-bottom: 20px">
          {{ tcpHightConfigTitle }}
        </a-button>

        <!-- 遍历高级组件(遍历高级组件，仅仅用于tcp) -->
        <template v-if="proType === 'tcp'" v-for="item in configurationArray">

          <!-- tcp 的 ssl 开关(单选框) -->
          <a-form-model-item
            v-show="highConfigShow"
            v-if="proType === 'tcp' && item.key === 'ssl' && item.valueType === 'boolean' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-radio-group
              v-model="form[item.key]"
              :name="item.key"
              @change="changeRadio">
              <a-radio :value="true">
                true
              </a-radio>
              <a-radio :value="false">
                false
              </a-radio>
            </a-radio-group>
          </a-form-model-item>

          <!-- tcp 的 ssl 证书(文件)，根据 ssl 的值显示或者隐藏-->
          <a-form-model-item
            v-show="isShowSslCert"
            v-else-if="proType === 'tcp' && item.key === 'rootCa' && item.valueType === 'file' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" style="width:70%" :disabled="true"></a-input>
            <file-upload @fileList="fileListRootCa" type="file"
                         style="display: inline"></file-upload>
          </a-form-model-item>

          <!-- tcp 的 ssl 证书(文件)，根据 ssl 的值显示或者隐藏-->
          <a-form-model-item
            v-show="isShowSslCert"
            v-else-if="proType === 'tcp' && item.key === 'key' && item.valueType === 'file' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" style="width:70%" :disabled="true"></a-input>
            <file-upload @fileList="fileListKey" type="file" style="display: inline"></file-upload>
          </a-form-model-item>

          <!-- tcp 的 ssl 证书(文件)，根据 ssl 的值显示或者隐藏-->
          <a-form-model-item
            v-show="isShowSslCert"
            v-else-if="proType === 'tcp' && item.key === 'cert' && item.valueType === 'file' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" style="width:70%" :disabled="true"></a-input>
            <file-upload @fileList="fileListCert" type="file" style="display: inline"></file-upload>
          </a-form-model-item>

          <!-- 永远预解码 开关(单选框) -->
          <a-form-model-item
            v-show="highConfigShow"
            v-else-if="proType === 'tcp' && item.key === 'alwaysPreDecode' && item.valueType === 'boolean' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-radio-group
              v-model="form[item.key]"
              :name="item.key">
              <a-radio :value="true">
                true
              </a-radio>
              <a-radio :value="false">
                false
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
          <!-- 设备不存在时直接新增该设备	 开关(单选框) -->
          <a-form-model-item
            v-show="highConfigShow"
            v-else-if="proType === 'tcp' && item.key === 'allowInsert' && item.valueType === 'boolean' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-radio-group
              v-model="form[item.key]"
              :name="item.key">
              <a-radio :value="true">
                true
              </a-radio>
              <a-radio :value="false">
                false
              </a-radio>
            </a-radio-group>
          </a-form-model-item>

          <!-- 通用下拉框渲染（支持通过url动态加载选项，包括parserType、delimiter等所有select类型字段） -->
          <a-form-model-item
            v-show="highConfigShow"
            v-else-if="item.valueType === 'select' && item.hide === 'true' && item.valueData && item.valueData.length > 0"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-select
              show-search
              v-model="form[item.key]"
              :placeholder="`请选择${item.name}`"
              :default-value="item.key === 'parserType' ? 'DELIMITED' : undefined"
              option-filter-prop="children"
              @change="handleSelectChange(item.key, $event)"
              style="width: 200px"
            >
              <template v-for="itemSelect in item.valueData">
                <a-select-option :value="itemSelect.dictValue" :key="itemSelect.dictValue">
                  {{ itemSelect.dictLabel }}
                </a-select-option>
              </template>
            </a-select>
          </a-form-model-item>

          <!-- 消息头包含消息长度（分隔符解析器） -->
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'boolean' && item.key === 'byteOrderLittle' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'maxFrameLength' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'lengthFieldOffset' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'lengthFieldLength' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'lengthAdjustment' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'initialBytesToStrip' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <a-form-model-item
            v-show="highConfigShow && fieldLengthShow"
            v-else-if="item.valueType === 'boolean' && item.key === 'failFast' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>

          <!-- boolean 值渲染 -->
          <a-form-model-item
            v-show="highConfigShow"
            v-else-if="item.valueType === 'boolean' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-radio-group v-model="form[item.key]" :name="item.key">
              <a-radio :value="true">
                true
              </a-radio>
              <a-radio :value="false">
                false
              </a-radio>
            </a-radio-group>
          </a-form-model-item>

          <!-- 通用下拉框渲染（兜底：处理valueData为空或未加载的情况） -->
          <a-form-model-item
            v-show="highConfigShow"
            v-else-if="item.valueType === 'select' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-select
              show-search
              v-model="form[item.key]"
              :placeholder="`请选择${item.name}`"
              option-filter-prop="children"
              @change="handleSelectChange(item.key, $event)"
              style="width: 200px"
            >
              <template v-if="item.valueData && item.valueData.length > 0">
                <template v-for="itemSelect in item.valueData">
                  <a-select-option :value="itemSelect.dictValue" :key="itemSelect.dictValue">
                    {{ itemSelect.dictLabel }}
                  </a-select-option>
                </template>
              </template>
              <a-select-option v-else :value="form[item.key]" :key="form[item.key]">
                {{ form[item.key] || '暂无选项' }}
              </a-select-option>
            </a-select>
          </a-form-model-item>

          <!-- 分隔符已改为select类型，通过通用下拉框渲染，此处删除特殊处理 -->

          <!-- 分隔的最大长度（分隔符解析器） -->
          <a-form-model-item
            v-show="highConfigShow && delimitedMaxlengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'delimitedMaxlength' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>

          <!-- 固定长度（固定长度解析器） -->
          <a-form-model-item
            v-show="highConfigShow && fixedLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'fixedLength' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <!-- 换行最大长度（换行符解析器） -->
          <a-form-model-item
            v-show="highConfigShow && lineMaxLengthShow"
            v-else-if="item.valueType === 'string' && item.key === 'lineMaxLength' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>
          <!-- 字符串渲染 -->
          <a-form-model-item
            v-show="highConfigShow"
            v-else-if="item.valueType === 'string' && item.hide === 'true'"
            :key="item.key"
            :prop="item.key"
          >
            <span slot="label">
              {{ item.name }}
              <a-tooltip
                style="background-color: #efcbc4;padding: 2px;border-radius: 50%;font-size: 14px;">
                <template slot="title">
                  {{ item.remark }}
                </template>
                <a-icon type="question"/>
              </a-tooltip>
            </span>
            <a-input v-model="form[item.key]" :placeholder="`请输入${item.name}`"/>
          </a-form-model-item>

        </template>

        <div>
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
    </a-spin>
  </a-drawer>
</template>

<script>
import FileUpload from '@/components/FileUpload'
import {getProduct, updateConfig} from '@/api/system/dev/product'

export default {
  name: 'ConfigurationForm',
  props: {
    dicts: {
      type: Array,
      default: () => {
      }
    }
  },
  components: {
    FileUpload
  },
  data() {
    return {
      // 分隔符是否显示（分隔符解析器）
      delimitedShow: false,
      // 分隔的最大长度是否显示（分隔符解析器）
      delimitedMaxlengthShow: false,
      // 换行最大长度是否显示（换行符解析器）
      lineMaxLengthShow: false,
      // 固定长度是否显示（固定长度解析器）
      fixedLengthShow: false,
      // 消息头附带长度是否显示（固定长度解析器）
      fieldLengthShow: false,
      // 高级配置标题(仅仅用于tcp)
      tcpHightConfigTitle: '高级配置展开',
      // 高级配置是否显示(仅仅用于tcp)
      highConfigShow: false,
      // 是否显示tcp证书
      isShowSslCert: false,
      // 产品id
      productId: undefined,
      // 产品配置type
      productConfigType: undefined,
      // 加载过度显示开关
      loading: false,
      // 标题
      formTitle: '修改产品配置信息',
      // 表单参数
      form: {},
      // 表单开关
      open: false,
      // 平台类型
      proType: undefined,
      // 解析类型
      pType: undefined,
      // 规则
      rules: {},
      // 配置信息数组
      configurationArray: []
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
    // 单选选择
    changeRadio(e) {
      if (e.target.value) {
        this.isShowSslCert = true
      } else {
        this.isShowSslCert = false
      }
      // console.log('eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee ', e)
    },
    // 关闭
    onClose() {
      this.open = false
      this.form = {}
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.configurationArray = []
      this.isShowSslCert = false
      this.tcpHightConfigTitle = '高级配置展开'
      this.highConfigShow = false
      this.delimitedMaxlengthShow = false
      this.delimitedShow = false
      this.fixedLengthShow = false
      this.lineMaxLengthShow = false
      this.fieldLengthShow = false
      this.productConfigType = undefined
      this.pType = undefined
      this.form = {}
    },
    /** 修改按钮操作 */
    handleUpdateById(productId, type) {
      this.productId = productId
      // console.log('productId = ', productId)
      // console.log('dictsaaaaa = ', this.dicts)
      this.reset()
      this.productConfigType = type
      const id = productId
      this.loading = true
      getProduct(id).then(response => {
        this.open = true
        this.proType = response.data.thirdPlatform
        // console.log('response', response)

        const productDetails = response.data
        let configArray = []
        let configData = {}

        if (productDetails.configuration !== undefined && productDetails.configuration !== null) {
          // 配置信息key数组
          configArray = Object.keys(JSON.parse(productDetails.configuration))
          // 配置信息key对象
          configData = JSON.parse(productDetails.configuration)
        }

        // console.log('productDetails', productDetails)
        // console.log('configArray', configArray)
        // console.log('configData', configData)
        // console.log('this.dicts', this.dicts)

        this.pType = configData.parserType

        if (this.dicts.length > 0) {
          if (configArray !== undefined) {
            for (let j = 0; j < this.dicts.length; j++) {
              let configDemo = {}
              try {
                if (this.dicts[j].remark !== undefined ||
                  this.dicts[j].remark !== null ||
                  this.dicts[j].remark !== '') {
                  configDemo = JSON.parse(this.dicts[j].remark)
                }
              } catch (e) {
                configDemo = {}
              }
              const t = j
              // console.log('qqqqqqqqqqqqqqqqqq = ', configDemo.type)
              if (configDemo.type !== undefined && configDemo.type === 'select') {
                let config = {}
                try {
                  if (this.dicts[t].remark !== undefined ||
                    this.dicts[t].remark !== null ||
                    this.dicts[t].remark !== '') {
                    config = JSON.parse(this.dicts[t].remark)
                    // console.log('tttttttttttt = ', t)
                    // console.log('ttttttttttttconfig = ', config)
                  }
                } catch (e) {
                  config = {}
                }
                // console.log('ttttttttttttconfig = ', config)
                if (config.url !== undefined && config.url !== '') {
                  this.getDicts(config.url).then(response => {
                    // console.log('ttttttttttttconfig = ', config)
                    let configurationArrayDemo
                    for (let i = 0; i < configArray.length; i++) {
                      if (configArray[i] === this.dicts[t].dictValue) {
                        configurationArrayDemo = {
                          name: this.dicts[t].dictLabel,
                          key: this.dicts[t].dictValue,
                          type: typeof configData[configArray[i]],
                          value: configData[configArray[i]],
                          remark: config.remark,
                          valueType: config.type,
                          valueData: response.data,
                          hide: config.hide
                        }
                        this.$set(this.form, configurationArrayDemo.key,
                          configurationArrayDemo.value)
                        // this.form[configArray[i]] = configData[configArray[i]]
                        this.configurationArray.push(configurationArrayDemo)
                        break
                      }
                    }
                    if (configurationArrayDemo === undefined || configurationArrayDemo === null) {
                      this.configurationArray.push({
                        name: this.dicts[t].dictLabel,
                        key: this.dicts[t].dictValue,
                        type: undefined,
                        value: undefined,
                        remark: config.remark,
                        valueType: config.type,
                        valueData: response.data,
                        hide: config.hide
                      })
                    }
                    // console.log('tttttttttttttttttttconfig = ', config)
                    // console.log('config.url response.data this.configurationArray = ', config.url, response.data, this.configurationArray)
                  })
                }
              } else {
                let config = {}
                try {
                  if (this.dicts[t].remark !== undefined ||
                    this.dicts[t].remark !== null ||
                    this.dicts[t].remark !== '') {
                    config = JSON.parse(this.dicts[j].remark)
                  }
                } catch (e) {
                  config = {}
                }
                let configurationArrayDemo
                for (let i = 0; i < configArray.length; i++) {
                  if (configArray[i] === this.dicts[t].dictValue) {
                    configurationArrayDemo = {
                      name: this.dicts[t].dictLabel,
                      key: this.dicts[t].dictValue,
                      type: typeof configData[configArray[i]],
                      value: configData[configArray[i]] === '' ? config.default
                        : configData[configArray[i]],
                      remark: config.remark,
                      valueType: config.type,
                      hide: config.hide
                    }
                    // 显示ssl三个文件传输组件
                    if (this.proType === 'tcp' && this.dicts[t].dictValue === 'ssl' && config.type
                      === 'boolean') {
                      this.isShowSslCert = configData[configArray[i]]
                    }
                    // 存放组件数据
                    this.configurationArray.push(configurationArrayDemo)
                    break
                  }
                }
                if (configurationArrayDemo === undefined || configurationArrayDemo === null) {
                  this.configurationArray.push({
                    name: this.dicts[t].dictLabel,
                    key: this.dicts[t].dictValue,
                    type: undefined,
                    value: config.default,
                    remark: config.remark,
                    valueType: config.type,
                    hide: config.hide
                  })
                }
              }
            }
          } else {
            for (let j = 0; j < this.dicts.length; j++) {
              let configDemo = {}
              try {
                if (this.dicts[j].remark !== undefined ||
                  this.dicts[j].remark !== null ||
                  this.dicts[j].remark !== '') {
                  configDemo = JSON.parse(this.dicts[j].remark)
                }
              } catch (e) {
                configDemo = {}
              }
              const t = j
              if (configDemo.type !== undefined && configDemo.type === 'select') {
                let config = {}
                try {
                  if (this.dicts[t].remark !== undefined ||
                    this.dicts[t].remark !== null ||
                    this.dicts[t].remark !== '') {
                    config = JSON.parse(this.dicts[t].remark)
                  }
                } catch (e) {
                  config = {}
                }
                if (config.url !== undefined && config.url !== '') {
                  this.getDicts(config.url).then(response => {
                    this.configurationArray.push({
                      name: this.dicts[t].dictLabel,
                      key: this.dicts[t].dictValue,
                      type: undefined,
                      value: undefined,
                      remark: config.remark,
                      valueType: config.type,
                      valueData: response.data,
                      hide: config.hide
                    })
                    // console.log('config.url response.data = ', config.url, response.data)
                  })
                }
              } else {
                let config = {}
                try {
                  if (this.dicts[t].remark !== undefined ||
                    this.dicts[t].remark !== null ||
                    this.dicts[t].remark !== '') {
                    config = JSON.parse(this.dicts[j].remark)
                  }
                } catch (e) {
                  config = {}
                }
                this.configurationArray.push({
                  name: this.dicts[t].dictLabel,
                  key: this.dicts[t].dictValue,
                  type: undefined,
                  value: config.default,
                  remark: config.remark,
                  valueType: config.type,
                  hide: config.hide
                })
              }
            }
          }
        }
        // console.log('this.configurationArray', this.configurationArray)
        for (let i = 0; i < this.configurationArray.length; i++) {
          // this.form[this.configurationArray[i].key] = this.configurationArray[i].value
          this.$set(this.form, this.configurationArray[i].key, this.configurationArray[i].value)
        }
        // console.log('this.form', this.form)
      }).finally(() => {
        if (this.pType) {
          this.parserTypeChange(this.pType)
        }
        this.loading = false
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      // console.log('form', this.form)
      // console.log('formStr', JSON.stringify(this.form))
      // console.log('id', this.productId)
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.productId !== undefined && this.productId !== null) {
            updateConfig({
              id: this.productId,
              configuration: JSON.stringify(this.form),
              type: this.productConfigType
            }).then(response => {
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
    fileListRootCa(val) {
      // console.log('fileList = ', val)
      // this.form.fileName = val.name
      this.form['rootCa'] = val.url
      // console.log('fileList this.form.url = ', this.form)
    },
    fileListKey(val) {
      // console.log('fileList = ', val)
      // this.form.fileName = val.name
      this.form['key'] = val.url
      // console.log('fileList this.form.url = ', this.form)
    },
    fileListCert(val) {
      // console.log('fileList = ', val)
      // this.form.fileName = val.name
      this.form['cert'] = val.url
      // console.log('fileList this.form.url = ', this.form)
    },
    /** 通用下拉框change事件处理（支持所有select类型字段） */
    handleSelectChange(fieldKey, value) {
      // 如果是parserType字段，触发特殊处理逻辑
      if (fieldKey === 'parserType') {
        this.parserTypeChange(value)
      }
      // 其他字段可以在这里添加特殊处理逻辑
      // 例如：if (fieldKey === 'delimiter') { ... }
    },
    parserTypeChange(value) {
      if (value) {
        if (value === 'DELIMITED') {
          this.delimitedMaxlengthShow = true
          this.delimitedShow = true
          this.fixedLengthShow = false
          this.lineMaxLengthShow = false
          this.fieldLengthShow = false
        } else if (value === 'FIXED_LENGTH') {
          this.delimitedMaxlengthShow = false
          this.delimitedShow = false
          this.fixedLengthShow = true
          this.lineMaxLengthShow = false
          this.fieldLengthShow = false
          //长度域解码器
        } else if (value === 'LENGTH_FIELD') {
          this.delimitedMaxlengthShow = false
          this.delimitedShow = false
          this.fixedLengthShow = false
          this.lineMaxLengthShow = false
          this.fieldLengthShow = true
        } else if (value === 'LINE') {
          this.delimitedMaxlengthShow = false
          this.delimitedShow = false
          this.fixedLengthShow = false
          this.lineMaxLengthShow = true
          this.fieldLengthShow = false
        } else {
          this.delimitedMaxlengthShow = false
          this.delimitedShow = false
          this.fixedLengthShow = false
          this.lineMaxLengthShow = false
          this.fieldLengthShow = false
        }
      }
      // console.log('parserTypeChange parserTypeChange parserTypeChange', value)
    },
    // 高级配置
    highConfig() {
      if (this.highConfigShow) {
        this.tcpHightConfigTitle = '高级配置展开'
        this.highConfigShow = false
      } else {
        this.tcpHightConfigTitle = '高级配置隐藏'
        this.highConfigShow = true
      }
    }
  }
}
</script>
