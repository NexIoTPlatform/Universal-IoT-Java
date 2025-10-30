<template>
  <a-modal
    :visible="open"
    :title="formTitle"
    width="800px"
    :footer="null"
    @cancel="onClose"
    centered
    class="product-create-modal"
  >
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item
        :label="$t('device.accessMethod')"
        prop="thirdPlatform"
        style="width: 100%"
      >
        <a-select
          placeholder="请选择接入方式"
          v-model="form.thirdPlatform"
          allow-clear
          :disabled="thirdPlatformDisabled"
          @change="thirdPlatformChange"
          style="width: 100%"
          optionLabelProp="label"
        >
          <template slot="suffixIcon">
            <a-icon type="down"/>
          </template>
          <a-select-option
            v-for="(d) in thirdPlatforms"
            :value="d.dictValue"
            :key="d.dictValue"
            :disabled="d.enabled === false"
            :label="d.dictLabel"
          >
            <div class="protocol-option">
              <div class="protocol-info">
                <span class="protocol-name">{{ d.dictLabel }}</span>
                <a-tag
                  v-if="d.status"
                  :color="d.enabled ? 'green' : 'orange'"
                  size="small"
                  style="margin-left: 8px;"
                >
                  {{ d.status }}
                </a-tag>
              </div>
              <div v-if="d.description" class="protocol-description">{{ d.description }}</div>
            </div>
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="产品分类" prop="classifiedId">
        <a-tree-select
          placeholder="请选择产品分类"
          v-model="form.classifiedId"
          checkable
          showSearch
          treeNodeFilterProp="title"
          :tree-data="productSortOptions"
          :replaceFields="defaultProps"
          :disable-branch-nodes="false"
        />
      </a-form-model-item>

      <a-button type="primary" :ghost="ghostManual" @click="manualProductAdd()"
                v-if="publicProductIsShow" style="margin-right: 16px;">
        手动添加
      </a-button>
      <a-button type="primary" :ghost="ghostPublic" @click="publicProductAdd()"
                v-if="publicProductIsShow">
        公共产品
      </a-button>

      <a-form-model-item :label="$t('product.name')" prop="name" v-if="nameIsShow">
        <a-input v-model="form.name" placeholder="请输入产品名称"/>
      </a-form-model-item>
      <a-form-model-item label="厂商简称" prop="companyNo">
        <a-input v-model="form.companyNo" placeholder="请输入厂商简称"
                 :disabled="companyNoDisabled"/>
      </a-form-model-item>
      <a-form-model-item label="设备型号" prop="productId">
        <a-input v-model="form.productId" placeholder="设备型号" :disabled="productIdDisabled"/>
      </a-form-model-item>

      <a-form-model-item label="是否透传" prop="tupIsThrough" v-if="tupIsThroughIsShow">
        <a-select
          placeholder="请选择是否透传"
          v-model="form.tupIsThrough"
          style="width: 100%"
          @change="tupIsThroughChange"
          allow-clear>
          <a-select-option v-for="(d, index) in tupIsThroughOptions" :key="index"
                           :value="d.dictValue">{{
              d.dictLabel
            }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="消息格式" prop="payloadFormat" v-if="payloadFormatIsShow">
        <a-select placeholder="请选择消息格式" v-model="form.payloadFormat" style="width: 100%"
                  allow-clear>
          <a-select-option v-for="(d, index) in payloadFormatOptions" :key="index"
                           :value="d.dictValue">{{
              d.dictLabel
            }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="省电模式" prop="powerModel" v-if="powerModelIsShow">
        <a-select
          placeholder="请选择省电模式"
          v-model="form.powerModel"
          style="width: 100%"
          allow-clear
          @change="powerModelChange">
          <a-select-option v-for="(d, index) in powerModelOptions" :key="index"
                           :value="d.dictValue">{{ d.dictLabel }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="eDRX模式时间窗" prop="lwm2mEdrxTime" v-if="lwm2mEdrxTimeIsShow">
        <a-input v-model="form.lwm2mEdrxTime" placeholder="20-10485.76间的值,精确到小数点后两位"/>
      </a-form-model-item>
      <a-form-model-item
        v-if="deviceNodeIsShow"
        :label="$t('device.node')"
        prop="deviceNode">
        <a-select
          placeholder="请选择设备节点"
          v-model="form.deviceNode"
          :disabled="deviceNodeDisabled"
          allow-clear>
          <a-select-option v-for="(d) in deviceNodes" :value="d.dictValue" :key="d.dictValue">
            {{ d.dictLabel }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        v-if="protocolIsShow"
        :label="$t('device.dataProtocol')"
        prop="transportProtocol">
        <a-select
          placeholder="请选择数据协议"
          v-model="form.transportProtocol"
          :disabled="protocolDisabled"
          allow-clear>
          <a-select-option v-for="(d) in protocols" :value="d.dictValue" :key="d.dictValue">
            {{ d.dictLabel }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item v-if="southboundCloudDockingAddressShow" label="南向云对接地址"
                         prop="southboundCloudDockingAddressShow">
        <a-input v-model="form.southboundCloudDockingAddress"
                 placeholder="通过这个接地址(URL)下发指令"/>
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
    <div>
      <public-product
        @getProductInfo="getProductInfo"
        ref="publicProduct"/>
    </div>

  </a-modal>
</template>

<script>
import {
  addCtwingPubPro,
  addProduct,
  addProductCtwing,
  getProduct,
  treeselect as productSortTreeselect,
  updateProduct
} from '@/api/system/dev/product'
import request from '@/utils/request'
import PublicProduct from './publicProduct'

export default {
  name: 'CreateForm',
  props: {},
  components: {
    PublicProduct
  },
  data() {
    return {
      // 南向对接云地址是否显示(云云对接专用)
      southboundCloudDockingAddressShow: false,
      ghostManual: false,
      ghostPublic: true,
      // 协议表单是否显示
      nameIsShow: true,
      productKeyIsShow: true,
      deviceNodeIsShow: true,
      powerModelIsShow: false,
      payloadFormatIsShow: false,
      tupIsThroughIsShow: false,
      lwm2mEdrxTimeIsShow: false,
      publicProductIsShow: false,
      // 是否控制隐藏
      companyNoDisabled: false,
      thirdPlatformDisabled: false,
      classifiedNameDisabled: false,
      productIdDisabled: false,
      productKeyDisabled: false,
      deviceNodeDisabled: false,
      protocolDisabled: false,
      deviceNodes: [],
      protocols: [],
      thirdPlatforms: [],
      loading: false,
      formTitle: '',
      productSortOptions: [],
      powerModelOptions: [
        {dictValue: 1, dictLabel: 'PSM'},
        {dictValue: 2, dictLabel: 'DRX'},
        {dictValue: 3, dictLabel: 'eDRX'}
      ],
      payloadFormatOptions: [
        {dictValue: 1, dictLabel: 'JSON'},
        {dictValue: 2, dictLabel: '紧凑二进制'}
      ],
      tupIsThroughOptions: [
        {dictValue: 0, dictLabel: '透传'},
        {dictValue: 1, dictLabel: '不透传'}
      ],
      // 表单参数
      form: {
        southboundCloudDockingAddress: null,
        publicProductID: null,

        id: null,

        productId: null,

        productKey: null,

        productSecret: null,

        thirdPlatform: undefined,

        thirdConfiguration: null,

        companyNo: null,

        classifiedId: undefined,

        configuration: null,

        networkWay: null,

        deviceNode: undefined,

        projectName: null,

        projectId: null,

        classifiedName: null,

        messageProtocol: null,

        orgId: null,

        name: null,

        creatorId: null,

        describe: null,

        storePolicy: null,

        storePolicyConfiguration: null,

        transportProtocol: null,

        photoUrl: null,

        protocolName: null,

        metadata: null,

        createTime: null,

        state: null,

        payloadFormat: undefined,

        powerModel: undefined,

        tupIsThrough: undefined,

        lwm2mEdrxTime: undefined

      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        productId: [
          {required: true, message: '设备型号不能为空', trigger: ['change', 'blur']}
        ],
        // productKey: [
        //   { required: true, message: 'productKey不能为空', trigger: 'blur' }
        // ],
        classifiedId: [
          {required: true, message: '产品分类不能为空', trigger: ['change', 'blur']}
        ],
        companyNo: [
          {required: true, message: '厂商简称不能为空', trigger: ['change', 'blur']}
        ],
        classifiedName: [
          {required: true, message: '产品类型不能为空', trigger: ['change', 'blur']}
        ],
        name: [
          {required: true, message: '产品名称不能为空', trigger: ['change', 'blur']}
        ],
        deviceNode: [
          {required: true, message: '设备节点不能为空', trigger: ['change', 'blur']}
        ],
        thirdPlatform: [
          {required: true, message: '接入方式不能为空', trigger: ['change', 'blur']}
        ],
        transportProtocol: [
          {required: true, message: '数据协议不能为空', trigger: ['change', 'blur']}
        ],
        tupIsThrough: [
          {required: true, message: '是否透传不能为空', trigger: ['change', 'blur']}
        ],
        payloadFormat: [
          {required: true, message: '消息格式不能为空', trigger: ['change', 'blur']}
        ],
        powerModel: [
          {required: true, message: '省电模式不能为空', trigger: ['change', 'blur']}
        ],
        lwm2mEdrxTime: [
          {required: true, message: 'eDRX模式时间窗不能为空', trigger: ['change', 'blur']}
        ]
      },
      defaultProps: {
        title: 'classifiedName',
        value: 'id',
        key: 'id'
      }
    }
  },
  filters: {},
  created() {
    this.deviceNodes = []
    this.protocols = []
    this.thirdPlatforms = []
    this.getProductSortTreeselect()
    // 获取字典数据
    var dictArray = []
    dictArray.push('instance_node')
    dictArray.push('transport_protocol')
    this.getDictMap(dictArray).then(res => {
      this.deviceNodes = res.data['instance_node']
      this.protocols = res.data['transport_protocol']
    })

    // 从接口获取第三方平台协议状态
    this.getProtocolStatus().then(response => {
      if (response.code === 0 && response.data && response.data.protocols) {
        const protocols = response.data.protocols

        // 直接使用接口返回的协议数据，转换为下拉选项格式
        this.thirdPlatforms = Object.keys(protocols)
        .map(key => {
          const protocol = protocols[key]
          return {
            dictValue: protocol.code,
            dictLabel: protocol.name,
            enabled: protocol.available,
            status: protocol.status,
            description: protocol.description,
            vendor: protocol.vendor,
            category: protocol.category,
            isCore: protocol.isCore
          }
        })
        .filter(protocol => protocol.enabled) // 只显示可用的协议
      }
    }).catch(error => {
      console.error('获取协议状态失败:', error)
      // 如果接口失败，使用默认数据作为备份
      const dictArray2 = ['third_platform']
      const renderThirdPlatform = ['ctaiot', 'mqtt', 'http', 'tcp', 'udp']
      this.getDictMap(dictArray2).then(res => {
        this.thirdPlatforms = res.data['third_platform'].filter(function (value, index, arr) {
          return renderThirdPlatform.indexOf(value.dictValue) !== -1
        })
      })
    })
  },
  computed: {
    // 根据设备节点控制数据协议字段显示
    protocolIsShow() {
      return this.form.deviceNode === 'GATEWAY' || this.form.deviceNode === 'GATEWAY_SUB_DEVICE'
    }
  },
  watch: {},
  mounted() {
  },
  methods: {
    // 获取协议状态
    getProtocolStatus() {
      return request({
        url: '/admin/protocol/status',
        method: 'get'
      })
    },

    /** 查询菜单树结构 */
    getProductSortTreeselect() {
      function buildTreeNode(node) {
        if (node.hasChild === 0) {
          node.disabled = false
          return node
        }
        if (node.children) {
          node.disabled = true
          node.children.forEach(n => {
            return buildTreeNode(n)
          })
        }
        return node
      }

      productSortTreeselect().then(response => {
        this.productSortOptions = response.data.map(node => {
          return buildTreeNode(node)
        })
        // console.log('this.productSortOptions', this.productSortOptions)
      })
    },
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
      this.nameIsShow = true
      this.thirdPlatformChange()
      this.formType = 1
      this.protocolIsShow = true
      this.productKeyIsShow = true
      this.deviceNodeIsShow = true
      this.powerModelIsShow = false
      this.payloadFormatIsShow = false
      this.tupIsThroughIsShow = false
      this.lwm2mEdrxTimeIsShow = false
      this.companyNoDisabled = false
      this.thirdPlatformDisabled = false
      this.classifiedNameDisabled = false
      this.productIdDisabled = false
      this.productKeyDisabled = false
      this.form = {
        id: null,

        productId: null,

        productKey: null,

        productSecret: null,

        thirdPlatform: undefined,

        thirdConfiguration: null,

        companyNo: null,

        classifiedId: null,

        configuration: null,

        networkWay: null,

        deviceNode: undefined,

        projectName: null,

        projectId: null,

        classifiedName: null,

        messageProtocol: null,

        orgId: null,

        name: null,

        creatorId: null,

        describe: null,

        storePolicy: null,

        storePolicyConfiguration: null,

        transportProtocol: undefined,

        photoUrl: null,

        protocolName: null,

        metadata: null,

        createTime: null,

        state: null

      }
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.$nextTick(() => {
        try {
          this.deviceNodeDisabled = false
          this.protocolDisabled = false
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.formType = 1
      this.open = true
      this.formTitle = '新增产品'
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.reset()
      console.log('row', row)
      this.companyNoDisabled = false
      this.thirdPlatformDisabled = true
      this.classifiedNameDisabled = true
      this.productIdDisabled = true
      this.productKeyDisabled = true
      this.deviceNodeDisabled = true
      this.protocolDisabled = true
      if (row.thirdPlatform === 'ctaiot' || row.thirdPlatform
        === '天翼物联') {
        this.powerModelIsShow = true
        this.deviceNodeIsShow = false
        this.protocolIsShow = false
      }
      this.formType = 2
      const id = row ? row.id : ids
      getProduct(id).then(response => {
        let config = null
        if (response.data.configuration) {
          config = JSON.parse(response.data.configuration)
          response.data.powerModel = config.powerModel
          if (response.data.powerModel === 3) {
            this.lwm2mEdrxTimeIsShow = true
            response.data.lwm2mEdrxTime = config.lwm2mEdrxTime
          } else {
            this.lwm2mEdrxTimeIsShow = false
          }
        }
        this.form = response.data
        // this.form.powerModel = config.powerModel
        this.open = true
        this.formTitle = '产品修改'
      })
    },
    /** 修改按钮操作 */
    handleUpdateById(productId) {
      this.reset()
      this.formType = 2
      const id = productId
      getProduct(id).then(response => {
        this.form = response.data
        this.open = true
        this.formTitle = '产品修改'
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id !== undefined && this.form.id !== null) {
            updateProduct(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else {
            // 新增产品的逻辑
            if (this.form.thirdPlatform === 'ctaiot') {
              // 只有ctaiot平台才调用这两个API
              if (this.ghostPublic === false) {
                // 点击了"公共产品"按钮，调用addCtwingPubPro
                addCtwingPubPro(this.form).then(response => {
                  this.$message.success(
                    '新增成功',
                    3
                  )
                  this.open = false
                  this.$emit('ok')
                })
              } else {
                // 点击了"手动添加"按钮，调用addProductCtwing
                addProductCtwing(this.form).then(response => {
                  this.$message.success(
                    '新增成功',
                    3
                  )
                  this.open = false
                  this.$emit('ok')
                })
              }
            } else {
              // 其他平台调用addProduct
              if (this.form.thirdPlatform === 'http') {
                if (this.form.southboundCloudDockingAddress !== null &&
                  this.form.southboundCloudDockingAddress !== undefined &&
                  this.form.southboundCloudDockingAddress !== '') {
                  const config = {
                    thirdDownSupport: true,
                    thirdDownUrl: this.form.southboundCloudDockingAddress
                  }
                  this.form.configuration = JSON.stringify(config)
                }
              }
              addProduct(this.form).then(response => {
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
    },
    // 产品分类发生改变
    thirdPlatformChange(value, option) {
      console.log('value', value)
      if (value === 'ctaiot') {
        this.southboundCloudDockingAddressShow = false
        this.nameIsShow = true
        this.productIdDisabled = false
        this.protocolIsShow = false
        this.productKeyIsShow = false
        this.deviceNodeIsShow = false
        this.powerModelIsShow = true
        this.payloadFormatIsShow = true
        this.tupIsThroughIsShow = true
        this.publicProductIsShow = true
      } else if (value === 'http') {
        this.southboundCloudDockingAddressShow = true
        this.nameIsShow = true
        this.productIdDisabled = false
        this.protocolIsShow = true
        this.productKeyIsShow = true
        this.deviceNodeIsShow = true
        this.powerModelIsShow = false
        this.payloadFormatIsShow = false
        this.tupIsThroughIsShow = false
        this.publicProductIsShow = false
      } else {
        this.southboundCloudDockingAddressShow = false
        this.nameIsShow = true
        this.productIdDisabled = false
        this.protocolIsShow = true
        this.productKeyIsShow = true
        this.deviceNodeIsShow = true
        this.powerModelIsShow = false
        this.payloadFormatIsShow = false
        this.tupIsThroughIsShow = false
        this.publicProductIsShow = false
      }
    },
    powerModelChange(value, option) {
      if (value === 3) {
        this.lwm2mEdrxTimeIsShow = true
      } else {
        this.lwm2mEdrxTimeIsShow = false
      }
    },
    tupIsThroughChange(value, option) {
      if (value === 1) {
        this.payloadFormatIsShow = true
      } else {
        this.payloadFormatIsShow = false
      }
    },
    getProductInfo(val) {
      console.log(val)
      this.form.companyNo = val.vendorName
      this.form.classifiedName = val.deviceType
      this.form.productId = val.deviceModel
      this.form.publicProductID = val.publicProductID
    },
    publicProductAdd() {
      this.$refs.publicProduct.openModals()
      this.ghostManual = true
      this.ghostPublic = false
      this.nameIsShow = false
      this.payloadFormatIsShow = false
      this.tupIsThroughIsShow = false
      this.powerModelIsShow = false
      this.companyNoDisabled = true
      this.classifiedNameDisabled = true
      this.productIdDisabled = true
    },
    manualProductAdd() {
      this.ghostManual = false
      this.ghostPublic = true
      this.nameIsShow = true
      this.payloadFormatIsShow = true
      this.tupIsThroughIsShow = true
      this.powerModelIsShow = true
      this.companyNoDisabled = false
      this.classifiedNameDisabled = false
      this.productIdDisabled = false
      this.form.companyNo = null
      this.form.classifiedName = null
      this.form.productId = null
      this.form.publicProductID = null
    }
  }
}
</script>

<style scoped>
.product-create-modal >>> .ant-modal-content {
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  box-shadow: 0 4px 24px 0 rgba(25, 102, 255, 0.10);
  padding: 0;
}

.product-create-modal >>> .ant-modal-header {
  border-radius: 14px 14px 0 0;
  background: #f8fafc;
  font-size: 18px;
  font-weight: bold;
  padding: 24px 32px 12px 32px;
}

.product-create-modal >>> .ant-modal-title {
  font-size: 18px;
  font-weight: bold;
}

.product-create-modal >>> .ant-modal-body {
  flex: 1 1 auto;
  padding: 0 32px 0 32px;
  max-height: 75vh;
  min-height: 500px;
  overflow-y: auto;
  background: #fff;
  border-radius: 0 0 14px 14px;
  margin-bottom: 0;
  scrollbar-width: thin;
  scrollbar-color: #d1d5db #f3f4f6;
}

.product-create-modal >>> .ant-modal-body::-webkit-scrollbar {
  width: 6px;
  background: #f3f4f6;
  border-radius: 6px;
}

.product-create-modal >>> .ant-modal-body::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 6px;
}

.bottom-control {
  flex-shrink: 0;
  background: #fff;
  padding: 18px 32px 24px 32px;
  border-top: 1px solid #f1f5f9;
  margin-top: 0;
  text-align: center;
  position: sticky;
  bottom: 0;
  z-index: 2;
}

/* 协议选项样式 */
.protocol-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.protocol-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.protocol-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.protocol-description {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}
</style>
