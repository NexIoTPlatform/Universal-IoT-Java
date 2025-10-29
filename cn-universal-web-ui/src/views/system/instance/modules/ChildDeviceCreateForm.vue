<template>
  <a-drawer width="35%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>{{ formTitle }}</b>
    </a-divider>
    <a-form-model ref="form" :model="form" :rules="rules">
      <a-row>
        <a-form-model-item
          v-if="pageCtrl && pageCtrl.ext1Label"
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
          v-if="pageCtrl && pageCtrl.ext2Label"
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
          v-if="pageCtrl && pageCtrl.ext3Label"
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
          v-if="pageCtrl && pageCtrl.ext4Label"
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
          v-if="pageCtrl && pageCtrl.validSecretKeyFun"
          label="设备秘钥"
          prop="secretKey">
          <a-input
            v-model="form.secretKey"
            :maxlength="100"
            :disabled="isUpdate"
            :placeholder="pageCtrl.secretKeyRemark?pageCtrl.secretKeyRemark:'请输入设备秘钥'"/>
        </a-form-model-item>
      </a-row>
      <a-form-model-item label="产品类型" prop="productKey">
        <a-select
          placeholder="请选择产品类型"
          v-model="form.productKey"
          style="width: 100%"
          allow-clear
          show-search
          option-filter-prop="children"
          :disabled="isUpdate"
          :filter-option="filterOption">
          <a-select-option v-for="(item) in productTypeList" :key="item.productKey"
                           :value="item.productKey">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
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
          :disabled="isUpdate"
          placeholder="请输入从站地址(1-247)"
          style="width: 100%"
          @change="onSlaveAddressChange"
        />
        <div class="form-item-help">
          Modbus从站地址，范围1-247，设备ID将自动生成
        </div>
      </a-form-model-item>

      <a-form-model-item label="设备序列号" prop="deviceId">
        <a-input v-model="form.deviceId" placeholder="请输入设备序列号" :disabled="isUpdate || isModbusSubDevice"/>
        <div v-if="isModbusSubDevice" class="form-item-help">
          设备ID将根据父设备ID和从站地址自动生成
        </div>
      </a-form-model-item>
      <a-form-model-item :label="$t('device.name')" prop="deviceName">
        <a-input v-model="form.deviceName" placeholder="请输入设备名称"/>
      </a-form-model-item>
      <a-form-model-item label="设备坐标" prop="longitude">
        <a-input v-model="locationStr" :disabled="true" style="width: 80%"/>
        <a-button type="primary" style="width: 20%;" :disabled="isUpdate" @click="openMap">
          获取
        </a-button>
      </a-form-model-item>
      <a-form-model-item :label="$t('common.remark')" prop="detail">
        <a-textarea v-model="form.detail" :disabled="isUpdate" placeholder="请输入备注"/>
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
  </a-drawer>
</template>

<script>
import {addInstance, getInstance, getModels, updateInstance} from '@/api/system/dev/instance'
import {getProByKey, getSubdeviceGateways} from '@/api/system/dev/product'
import Map from '@/components/Map'

export default {
  name: 'InstanceCreateForm',
  props: {
    stateOptions: {
      type: Array,
      required: true
    },
    gwProductKey: {
      type: String,
      required: true
    },
    gwDeviceId: {
      type: String,
      required: true,
      default: ''
    }
  },
  components: {Map},
  data() {
    return {
      // 经纬度
      location: {},
      // 地图弹窗显示
      mapShow: false,
      // 经纬度
      locationStr: undefined,
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
      typeName: undefined,
      companie: undefined,
      model: undefined,
      isUpdate: false,
      deviceNode: '',
      productTypeList: [],
      // 表单参数
      form: {
        modelName: undefined,

        id: null,

        iotId: null,

        deviceId: null,

        extDeviceId: null,

        deriveMetadata: null,

        configuration: null,

        productName: null,

        nickName: null,

        features: null,

        parentId: undefined,

        gwDeviceId: null,

        extDeviceId: null,

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

        // Modbus子设备专用字段
        slaveAddress: null

      },
      // 1增加,2修改
      formType: 1,
      open: false,
      rules: {
        longitude: [
          {required: true, message: '请选择设备坐标', trigger: 'change'}
        ],
        deviceName: [
          {required: true, message: '请输入设备名称', trigger: 'blur'}],
        productKey: [
          {required: true, message: '请选择产品类型', trigger: 'change'}],
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
        slaveAddress: [
          {
            validator: (rule, value, callback) => {
              // 如果是编辑模式，不验证必填
              if (this.isUpdate) {
                return callback()
              }
              // 新增模式下验证必填
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
        ]

      }
    }
  },
  computed: {
    // 判断是否为Modbus子设备
    isModbusSubDevice() {
      return this.form.productKey && this.productTypeList.some(product =>
        product.productKey === this.form.productKey &&
        product.transportProtocol === 'modbus'
      )
    }
  },
  created() {
    console.log('ChildDeviceCreateForm created - gwDeviceId prop:', this.gwDeviceId)
    console.log('ChildDeviceCreateForm created - gwProductKey prop:', this.gwProductKey)
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
      this.location = {}
      this.locationStr = null
      this.formType = 1
      this.isUpdate = false  // 重置编辑模式标志位
      this.models = []
      this.pageCtrl = {}
      this.companies = []
      this.devices = []
      this.typeName = undefined
      this.companie = undefined
      this.form = {
        id: null,

        iotId: null,

        deviceId: null,

        extDeviceId: null,

        deriveMetadata: null,

        configuration: null,

        productName: null,

        nickName: null,

        features: null,

        parentId: undefined,

        gwDeviceId: null,

        extDeviceId: null,

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

        // Modbus子设备专用字段
        slaveAddress: null

      }
    },
    /** 新增按钮操作 */
    handleAdd(parentIotId) {
      console.log('handleAdd called - parentIotId:', parentIotId)
      console.log('handleAdd called - this.gwDeviceId:', this.gwDeviceId)
      console.log('handleAdd called - this.gwProductKey:', this.gwProductKey)

      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {
        }
      })
      this.reset()
      getProByKey(this.gwProductKey).then(response => {
        this.form.deviceModel = response.data.productId
        this.form.deviceModelName = response.data.name
        this.form.protocol = response.data.transportProtocol
        this.form.gwDeviceId = this.gwDeviceId
        this.form.extDeviceId = this.gwDeviceId
        this.pageCtrl = JSON.parse(response.data.thirdConfiguration)
        if (!this.pageCtrl) {
          this.pageCtrl = {}
        }
        this.getSubdevice()
      })
      this.formType = 1
      this.open = true
      this.formTitle = '添加'
      console.log('子设备表单数据:', this.form)
      console.log('网关设备ID:', this.gwDeviceId)
      console.log('表单中的gwDeviceId:', this.form.gwDeviceId)
      console.log('表单中的extDeviceId:', this.form.extDeviceId)
    },
    getSubdevice() {
      getSubdeviceGateways(this.gwProductKey).then(res => {
        this.productTypeList = res.data
      })
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.$nextTick(() => {
        try {
          this.$refs.form.resetFields()
        } catch (e) {
        }
      })
      this.reset()
      this.isUpdate = true  // 设置为编辑模式
      this.formType = 2
      const id = row ? row.id : ids
      getInstance(id).then(response => {
        if (response.data.params.locationStr !== undefined && response.data.params.locationStr
          !== null) {
          response.data.latitude = response.data.params.locationStr.split(',')[0]
          response.data.longitude = response.data.params.locationStr.split(',')[1]
          this.location.lng = response.data.longitude
          this.location.lat = response.data.latitude
          this.locationStr = response.data.longitude + ',' + response.data.latitude
        } else {
          this.location = {}
          response.data.latitude = null
          response.data.longitude = null
          this.locationStr = null
        }
        console.log(111, response.data)
        const data = response.data
        this.form = response.data

        // 解析从站地址（如果是Modbus子设备）
        if (data.configuration) {
          try {
            const config = JSON.parse(data.configuration)
            if (config.slaveAddress) {
              this.form.slaveAddress = parseInt(config.slaveAddress)
              console.log('解析从站地址成功:', this.form.slaveAddress)
            }
          } catch (e) {
            console.warn('解析从站地址配置失败:', e)
          }
        }

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

        this.getSubdevice()
      })
    },
    /** 详情界面修改按钮操作 */
    detailesHandleUpdate(ids) {
      this.reset()
      this.isUpdate = true  // 设置为编辑模式
      this.formType = 2
      const id = ids
      getInstance(id).then(response => {
        const data = response.data
        this.form = response.data

        // 解析从站地址（如果是Modbus子设备）
        if (data.configuration) {
          try {
            const config = JSON.parse(data.configuration)
            if (config.slaveAddress) {
              this.form.slaveAddress = parseInt(config.slaveAddress)
              console.log('解析从站地址成功:', this.form.slaveAddress)
            }
          } catch (e) {
            console.warn('解析从站地址配置失败:', e)
          }
        }

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

            // 如果是Modbus子设备，添加特殊配置
            if (this.isModbusSubDevice && this.form.slaveAddress) {
              this.form.configuration = JSON.stringify({
                protocolType: 'modbus',
                slaveAddress: this.form.slaveAddress,
                deviceInfo: {
                  parentDeviceId: this.gwDeviceId,
                  parentProductKey: this.gwProductKey,
                  generatedDeviceId: this.form.deviceId
                }
              })
            }

            console.log('提交子设备数据:', this.form)
            console.log('网关设备ID (gwDeviceId):', this.form.gwDeviceId)
            console.log('扩展设备ID (extDeviceId):', this.form.extDeviceId)
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
    // 打开地图弹出层
    openMap() {
      this.mapShow = true
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
    },
    // 关闭地图弹出层
    closeMap() {
      this.mapShow = false
    },
    getPro(value) {
      getProByKey(value).then(response => {
        this.form.deviceModel = response.data.productId
        this.form.deviceModelName = response.data.name
        this.form.protocol = response.data.transportProtocol
        this.form.productKey = response.data.productKey
        this.pageCtrl = JSON.parse(response.data.thirdConfiguration)
        if (!this.pageCtrl) {
          this.pageCtrl = {}
        }
      })
    },
    // 搜索|过滤产品
    filterOption(value, option) {
      return (
        option.componentOptions.children[0].children[0].text.toLowerCase().indexOf(
          value.toLowerCase()) >= 0
      )
    },
    // 从站地址变更处理
    onSlaveAddressChange(value) {
      if (value && this.gwDeviceId) {
        this.form.deviceId = `${this.gwDeviceId}-${value}`
      }
    },
    // 检查从站地址是否已存在
    checkSlaveAddressExists(slaveAddress, callback) {
      // 这里需要调用API检查从站地址是否已存在
      // 暂时先通过验证
      callback()
    }
  }
}
</script>

<style scoped>
.form-item-help {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
