<template>
  <a-modal
    :visible="open"
    :title="formTitle"
    width="600px"
    :footer="null"
    @cancel="onClose"
    centered
    class="instance-create-modal"
  >
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-form-model-item label="产品分类" prop="classifiedId" v-if="!isEdit">
        <a-tree-select
          placeholder="请选择产品分类"
          v-model="form.classifiedId"
          checkable
          showSearch
          treeNodeFilterProp="title"
          :tree-data="productSortOptions"
          :replaceFields="defaultProps"
          :disable-branch-nodes="false"
          @change="onChangeSort"
        />
      </a-form-model-item>
      <a-form-model-item
        v-if="!isEdit"
        label="产品类型"
        prop="name">
        <a-select
          placeholder="请选择产品类型"
          v-model="form.name"
          style="width: 100%"
          allow-clear
          @change="onChangeName"
          show-search
          option-filter-prop="children"
          :filter-option="filterOption">
          <a-select-option v-for="(d, index) in sorts" :key="index" :value="d.dictValue">{{
              d.dictLabel
            }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="所属网关设备" prop="extDeviceId"
                         v-if="deviceNode === 'GATEWAY_SUB_DEVICE'">
        <a-select
          placeholder="请选择所属网关设备"
          v-model="form.extDeviceId"
          style="width: 100%"
          allow-clear
          show-search
          option-filter-prop="children"
          :filter-option="filterOption">
          <a-select-option v-for="(item) in gatewayList" :key="item.deviceId"
                           :value="item.deviceId">
            {{ item.deviceName }}- {{ item.deviceId }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-row>
        <a-form-model-item
          v-if="pageCtrl.ext1Label"
          :label="pageCtrl.ext1Label"
          prop="ext1"
          :rules="{
            required: true , message: pageCtrl.ext1Label + '不能为空', trigger: 'blur'
          }"
        >
          <a-input
            v-model="form.ext1"
            :maxLength="200"
            :disabled="isUpdate"
            :placeholder="pageCtrl.ext1LabelDes"/>
        </a-form-model-item>
      </a-row>

      <a-row>
        <a-form-model-item
          :label="pageCtrl.ext2Label"
          v-if="pageCtrl.ext2Label"
          prop="ext2"
          :rules="{
            required: true , message: pageCtrl.ext2Label + '不能为空', trigger: 'blur'}">
          <a-input
            v-model="form.ext2"
            :maxLength="200"
            :disabled="isUpdate"
            :placeholder="pageCtrl.ext2LabelDes"/>
        </a-form-model-item>
      </a-row>
      <a-row>
        <a-form-model-item
          v-if="pageCtrl.ext3Label"
          :label="pageCtrl.ext3Label"
          prop="ext3"
          :rules="{
            required: true , message: pageCtrl.ext3Label + '不能为空', trigger: 'blur'
          }">
          <a-input
            v-model="form.ext3"
            :maxLength="200"
            :disabled="isUpdate"
            :placeholder="pageCtrl.ext3LabelDes"/>
        </a-form-model-item>
      </a-row>
      <a-row>
        <a-form-model-item
          v-if="pageCtrl.ext4Label"
          :label="pageCtrl.ext4Label"
          prop="ext4"
          :rules="{
            required: true , message: pageCtrl.ext4Label + '不能为空', trigger: 'blur'}">
          <a-input
            v-model="form.ext4"
            :maxLength="200"
            :disabled="isUpdate"
            :placeholder="pageCtrl.ext4LabelDes"/>
        </a-form-model-item>
      </a-row>
      <a-row>
        <a-form-model-item
          v-if="pageCtrl.validSecretKeyFun"
          label="设备秘钥"
          prop="secretKey">
          <a-input
            v-model="form.secretKey"
            :maxlength="100"
            :disabled="isUpdate"
            :placeholder="pageCtrl.secretKeyRemark?pageCtrl.secretKeyRemark:'请输入设备秘钥'"/>
        </a-form-model-item>
      </a-row>
      <!-- Modbus子设备从站地址配置 -->
      <a-form-model-item
        v-if="isModbusSubDevice"
        label="从站地址"
        prop="slaveAddress"
      >
        <a-input-number
          v-model="form.slaveAddress"
          :min="1"
          :max="247"
          :disabled="isEdit"
          placeholder="请输入从站地址(1-247)"
          style="width: 100%"
          @change="onSlaveAddressChange"
        />
        <div class="form-item-help">
          Modbus从站地址，范围1-247，设备ID将自动生成
        </div>
      </a-form-model-item>

      <a-form-model-item label="设备序列号" prop="deviceId">
        <a-input v-model="form.deviceId" placeholder="请输入设备序列号" :disabled="isEdit || isModbusSubDevice"/>
        <div v-if="isModbusSubDevice" class="form-item-help">
          设备ID将根据父设备ID和从站地址自动生成
        </div>
      </a-form-model-item>
      <a-form-model-item :label="$t('device.name')" prop="deviceName">
        <a-input v-model="form.deviceName" placeholder="请输入设备名称"/>
      </a-form-model-item>

      <a-form-model-item label="设备坐标" prop="longitude">
        <a-input v-model="locationStr" :disabled="true" style="width: 75%; margin-right: 8px;"/>
        <a-button type="primary" style="width: 20%;" :disabled="isEdit" @click="openMap">
          获取
        </a-button>
      </a-form-model-item>
      <a-form-model-item :label="$t('common.remark')" prop="detail">
        <a-textarea v-model="form.detail" :disabled="isEdit" placeholder="请输入备注"/>
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

    <a-modal
      :maskClosable="false"
      title="坐标获取"
      v-model="mapShow"
      width="800px"
      @ok="handleMap"
      @cancel="closeMap"
    >
      <div style="height: 600px">
        <Map v-if="mapShow" :lnglat="location" ref="bdMap"/>
      </div>
    </a-modal>
  </a-modal>
</template>

<script>
import {addInstance, getInstance, getModels, updateInstance} from '@/api/system/dev/instance'
import {
  getInstanceGateway,
  getProByKey,
  listProduct,
  treeselect as productSortTreeselect
} from '@/api/system/dev/product'
import Map from '@/components/Map'

export default {
  name: 'CreateForm',
  props: {
    stateOptions: {
      type: Array,
      required: true
    }
  },
  components: {Map},
  data() {
    return {
      // 是否是修改
      isEdit: false,
      // 经纬度
      location: {},
      // 经纬度
      locationStr: undefined,
      // 地图弹窗显示
      mapShow: false,
      productSortOptions: [],
      props: {
        label: 'orgName',
        children: 'children'
      },
      loading: false,
      formTitle: '',
      models: [],
      pageCtrl: {},
      companies: [],
      devices: [],
      sorts: [],
      typeName: undefined,
      companie: undefined,
      model: undefined,
      isUpdate: false,
      deviceNode: undefined,
      // 查询参数
      queryParam: {
        classifiedId: null
      },
      // 表单参数
      form: {
        modelName: undefined,

        id: null,

        name: undefined,

        iotId: null,

        deviceId: null,

        deriveMetadata: null,

        configuration: null,

        productName: null,

        nickName: null,

        features: null,

        orgId: null,

        productKey: null,

        secretKey: null,

        deviceName: null,

        creatorId: null,

        instance: null,

        application: null,

        creatorName: null,

        state: null,

        detail: null,

        createTime: null,

        registryTime: null,

        onlineTime: null,

        areasId: null,

        longitude: null,
        latitude: null,

        // Modbus子设备专用字段
        slaveAddress: null

      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        name: [
          {required: true, message: '请选择产品类型', trigger: 'blur'}
        ],
        classifiedId: [
          {required: true, message: '请选择产品分类', trigger: ['change', 'blur']}
        ],
        longitude: [
          {required: true, message: '请选择设备坐标', trigger: 'change'}
        ],
        deviceName: [
          {required: true, message: '请输入设备名称', trigger: 'blur'}],
        extDeviceId: [
          {required: true, message: '请选择所属网关设备', trigger: 'blur'}],
        deviceId: [
          {required: true, message: '请输入设备序列号', trigger: 'blur'},
          {
            validator: (rule, value, callback) => {
              if (this.pageCtrl.validDeviceIdFun) {
                // eslint-disable-next-line no-eval
                const fun = eval(this.pageCtrl.validDeviceIdFun)
                const result = fun(value)
                if (result) {
                  return callback(result)
                }
              }
              callback()
            },
            trigger: 'blur'
          }
        ],
        secretKey: [
          {required: true, message: '请输入设备秘钥', trigger: 'blur'},
          {
            validator: (rule, value, callback) => {
              if (this.pageCtrl.validSecretKeyFun) {
                // eslint-disable-next-line no-eval
                const fun = eval(this.pageCtrl.validSecretKeyFun)
                const result = fun(value)
                if (result) {
                  return callback(result)
                }
              }
              callback()
            },
            trigger: 'blur'
          }
        ],
        slaveAddress: [
          {required: true, message: '请输入从站地址', trigger: 'blur'},
          {
            validator: (rule, value, callback) => {
              if (!value) {
                return callback(new Error('请输入从站地址'))
              }
              if (value < 1 || value > 247) {
                return callback(new Error('从站地址必须在1-247之间'))
              }
              // 检查从站地址是否已存在
              this.checkSlaveAddressExists(value, callback)
            },
            trigger: 'blur'
          }
        ]

      },
      defaultProps: {
        title: 'classifiedName',
        value: 'id',
        key: 'id'
      },
      gatewayList: []
    }
  },
  computed: {
    // 判断是否为Modbus子设备
    isModbusSubDevice() {
      return this.deviceNode === 'GATEWAY_SUB_DEVICE' &&
        this.form.productKey &&
        this.getProductTransportProtocol() === 'modbus'
    }
  },
  created() {
    this.getProductSortTreeselect()
  },
  methods: {
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
      this.location = {}
      this.locationStr = null
      this.isEdit = false
      this.formType = 1
      this.models = []
      this.pageCtrl = {}
      this.companies = []
      this.devices = []
      this.typeName = undefined
      this.companie = undefined
      this.sorts = []
      this.form = {
        id: null,
        name: undefined,
        iotId: null,

        deviceId: null,

        gwDeviceId: null,

        deriveMetadata: null,

        configuration: null,

        productName: null,

        nickName: null,

        features: null,

        extDeviceId: undefined,

        orgId: null,

        productKey: undefined,

        deviceName: null,

        creatorId: null,

        instance: null,

        application: null,

        creatorName: null,

        state: null,

        detail: null,

        createTime: null,

        registryTime: null,

        onlineTime: null,

        areasId: null,

        longitude: null,
        latitude: null,

        // Modbus子设备专用字段
        slaveAddress: null

      }
    },
    /** 新增按钮操作 */
    handleAdd(parentIotId) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.reset()
      this.loadModels(typeof (parentIotId) === 'string')
      if (typeof (parentIotId) === 'string') {
        this.form.parentIotId = parentIotId
      }
      console.log(this.form.id)
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.reset()
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.isEdit = true
      this.formType = 2
      const id = row ? row.id : ids
      getInstance(id).then(response => {
        if (response.data.params.locationStr !== undefined && response.data.params.locationStr
          !== null) {
          response.data.longitude = response.data.params.locationStr.split(',')[0]
          response.data.latitude = response.data.params.locationStr.split(',')[1]
          this.location.lng = response.data.longitude
          this.location.lat = response.data.latitude
          this.locationStr = response.data.longitude + ',' + response.data.latitude
        } else {
          this.location = {}
          response.data.latitude = null
          response.data.longitude = null
          this.locationStr = null
        }
        const data = response.data
        this.form = response.data
        this.open = true
        this.formTitle = '修改'
        // eslint-disable-next-line no-undef
        this.loadModels(!!data.parentIotId).then(() => {
          this.companies.forEach(com => {
            // eslint-disable-next-line no-undef
            if (com.companyNo === data.companyNo) {
              this.devices = com.devices
              com.devices.forEach(dev => {
                this.models = dev.models
                dev.models.forEach(mol => {
                  // eslint-disable-next-line no-undef
                  if (mol.productKey === data.productKey) {
                    this.pageCtrl = mol.pageCtrl
                  }
                })
              })
            }
          })
        })
      })
    },
    /** 详情界面修改按钮操作 */
    detailesHandleUpdate(ids) {
      this.reset()
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {

        }
      })
      this.isEdit = true
      this.formType = 2
      const id = ids
      getInstance(id).then(response => {
        if (response.data.params.locationStr !== undefined && response.data.params.locationStr
          !== null) {
          response.data.longitude = response.data.params.locationStr.split(',')[0]
          response.data.latitude = response.data.params.locationStr.split(',')[1]
          this.location.lng = response.data.longitude
          this.location.lat = response.data.latitude
          this.locationStr = response.data.longitude + ',' + response.data.latitude
        } else {
          this.location = {}
          response.data.latitude = null
          response.data.longitude = null
          this.locationStr = null
        }
        const data = response.data
        this.form = response.data
        this.open = true
        this.formTitle = '修改'
        // eslint-disable-next-line no-undef
        this.loadModels(!!data.parentIotId).then(() => {
          this.companies.forEach(com => {
            // eslint-disable-next-line no-undef
            if (com.companyNo === data.companyNo) {
              this.devices = com.devices
              com.devices.forEach(dev => {
                this.models = dev.models
                dev.models.forEach(mol => {
                  // eslint-disable-next-line no-undef
                  if (mol.productKey === data.productKey) {
                    this.pageCtrl = mol.pageCtrl
                  }
                })
              })
            }
          })
        })
      })
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id !== undefined && this.form.id !== null) {
            // 确保 gwDeviceId 和 extDeviceId 保持一致
            if (this.form.extDeviceId) {
              this.form.gwDeviceId = this.form.extDeviceId
            }
            console.log('修改设备数据:', this.form)
            console.log('所属网关 (extDeviceId):', this.form.extDeviceId)
            console.log('网关设备ID (gwDeviceId):', this.form.gwDeviceId)
            updateInstance(this.form).then(response => {
              this.$message.success(
                '修改成功',
                3
              )
              this.open = false
              this.$emit('ok')
            })
          } else {
            var otherParams = {}
            if (this.pageCtrl !== undefined && this.pageCtrl !== null) {
              if (this.pageCtrl.customField !== undefined) {
                for (let i = 0; i < this.pageCtrl.customField.length; i++) {
                  const j = i + 1
                  otherParams[this.pageCtrl.customField[i].id] = this.form[`ext` + j]
                }
              }
            }
            this.form.otherParams = otherParams
            // 确保 gwDeviceId 和 extDeviceId 保持一致
            if (this.form.extDeviceId) {
              this.form.gwDeviceId = this.form.extDeviceId
            }

            // 如果是Modbus子设备，添加特殊配置
            if (this.isModbusSubDevice && this.form.slaveAddress) {
              this.form.configuration = JSON.stringify({
                protocolType: 'modbus',
                slaveAddress: this.form.slaveAddress,
                deviceInfo: {
                  parentDeviceId: this.form.extDeviceId,
                  parentProductKey: this.form.productKey,
                  generatedDeviceId: this.form.deviceId
                }
              })
            }

            console.log('提交设备数据:', this.form)
            console.log('所属网关 (extDeviceId):', this.form.extDeviceId)
            console.log('网关设备ID (gwDeviceId):', this.form.gwDeviceId)
            addInstance(this.form).then(response => {
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
    onChangeSort(value) {
      this.sorts = []
      this.loading = true
      this.form.name = undefined
      this.queryParam.classifiedId = value
      listProduct(this.queryParam).then(response => {
        response.rows.forEach(item => {
          this.sorts.push({dictValue: item.productKey, dictLabel: item.name})
        })
      })
    },
    onChangeName(value) {
      if (value !== undefined) {
        getProByKey(value).then(response => {
          this.form.deviceModel = response.data.productId
          this.form.deviceModelName = response.data.name
          this.form.protocol = response.data.transportProtocol
          this.form.productKey = response.data.productKey
          this.pageCtrl = JSON.parse(response.data.thirdConfiguration)
          if (!this.pageCtrl) {
            this.pageCtrl = {}
          }
          this.deviceNode = response.data.deviceNode
          if (this.deviceNode === 'GATEWAY_SUB_DEVICE') {
            this.getGatewayList()
          }
        })
      }
    },
    getGatewayList() {
      getInstanceGateway(this.form.productKey).then(res => {
        this.gatewayList = res.data
      })
    },
    /** 查询设备管理列表 */
    loadModels(child) {
      return getModels({
        child: child
      }).then(response => {
        this.companies = response
        this.companies.forEach(item => {
          item.value = item.companyNo
          item.label = item.companyName
        })
      })
    },
    // 关闭地图弹出层
    closeMap() {
      this.mapShow = false
    },
    // 处理地图经纬度
    handleMap() {
      if (!this.$refs.bdMap.location) {
        this.mapShow = false
        return
      }
      this.locationStr = this.$refs.bdMap.locationStr
      this.form.longitude = this.$refs.bdMap.location.lng
      this.form.latitude = this.$refs.bdMap.location.lat
      this.location.lng = this.form.longitude
      this.location.lat = this.form.latitude
      this.mapShow = false
      this.$refs.form.validateField('longitude')
      console.log('this.location = ', this.location)
      console.log('this.form = ', this.form)
      console.log('this.$refs.bdMap.location = ', this.$refs.bdMap.location)
      console.log('this.$refs.bdMap.locationStr = ', this.$refs.bdMap.locationStr)
    },
    // 打开地图弹出层
    openMap() {
      this.mapShow = true
    },
    // 筛选选择框
    filterOption(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    // 从站地址变更处理
    onSlaveAddressChange(value) {
      if (value && this.form.extDeviceId) {
        this.form.deviceId = `${this.form.extDeviceId}-${value}`
      }
    },
    // 检查从站地址是否已存在
    checkSlaveAddressExists(slaveAddress, callback) {
      // 这里需要调用API检查从站地址是否已存在
      // 暂时先通过验证
      callback()
    },
    // 获取产品的传输协议
    getProductTransportProtocol() {
      // 这里需要根据productKey获取产品的transportProtocol
      // 暂时返回modbus作为示例
      return 'modbus'
    }
  }
}
</script>

<style scoped>
.instance-create-modal >>> .ant-modal-content {
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  box-shadow: 0 4px 24px 0 rgba(25, 102, 255, 0.10);
  padding: 0;
}

.instance-create-modal >>> .ant-modal-header {
  border-radius: 14px 14px 0 0;
  background: #f8fafc;
  font-size: 18px;
  font-weight: bold;
  padding: 24px 32px 12px 32px;
}

.instance-create-modal >>> .ant-modal-title {
  font-size: 18px;
  font-weight: bold;
}

.instance-create-modal >>> .ant-modal-body {
  flex: 1 1 auto;
  padding: 0 32px 0 32px;
  max-height: 60vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 0 0 14px 14px;
  margin-bottom: 0;
  /* 美化滚动条 */
  scrollbar-width: thin;
  scrollbar-color: #d1d5db #f3f4f6;
}

.instance-create-modal >>> .ant-modal-body::-webkit-scrollbar {
  width: 6px;
  background: #f3f4f6;
  border-radius: 6px;
}

.instance-create-modal >>> .ant-modal-body::-webkit-scrollbar-thumb {
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

.form-item-help {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
