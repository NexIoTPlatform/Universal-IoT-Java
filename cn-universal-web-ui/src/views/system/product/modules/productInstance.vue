<template>
  <a-card :bordered="false">
    <!-- 条件搜索 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="设备序列号" prop="deviceId">
              <a-input v-model="queryParam.deviceId" placeholder="请输入设备序列号" allow-clear/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="设备名称" prop="deviceName">
              <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称" allow-clear/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="设备iotId" prop="iotId">
              <a-input v-model="queryParam.iotId" placeholder="请输入iotId" allow-clear/>
            </a-form-item>
          </a-col>
          <template v-if="advanced">
            <a-col :md="8" :sm="24">
              <a-form-item label="设备状态">
                <a-select placeholder="请选择" v-model="queryParam.state" style="width: 100%"
                          allow-clear>
                  <a-select-option v-for="(d, index) in stateOptions" :key="index"
                                   :value="d.dictValue">{{ d.dictLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span class="table-page-search-submitButtons"
                  :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="handleQuery"><iot-icon
                type="icon-search"/>查询</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>重置</a-button>
              <a @click="toggleAdvanced" style="margin-left: 8px">
                {{ advanced ? '收起' : '展开' }}
                <a-icon :type="advanced ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 操作 -->
    <div class="table-operations">
      <a-button type="primary" @click="$refs.createForm.handleAdd()"
                v-hasPermi="['system:instance:add']">
        <iot-icon type="icon-u-add"/>
        新增
      </a-button>
      <!--
            <a-button type="primary" @click="$refs.ImportProductForm.handleImport()" v-hasPermi="['product:device:import']">
              <a-icon type="import" />导入
            </a-button> -->

      <a-button type="primary" :disabled="single"
                @click="$refs.createForm.handleUpdate(undefined, ids)"
                v-hasPermi="['system:instance:edit']">
        <iot-icon type="icon-u-edit"/>
        修改
      </a-button>
      <a-button type="danger" :disabled="multiple" @click="handleDelete"
                v-hasPermi="['system:instance:remove']">
        <iot-icon type="icon-u-del"/>
        删除
      </a-button>
      <a-button type="primary" @click="handleExport" v-hasPermi="['system:instance:export']">
        <iot-icon type="icon-u-export"/>
        导出
      </a-button>
      <a-button
        type="dashed"
        shape="circle"
        :loading="loading"
        :style="{float: 'right'}"
        icon="reload"
        @click="getList"/>
    </div>
    <!-- 增加修改 -->
    <instance-create-form
      ref="createForm"
      :stateOptions="stateOptions"
      :product-key="productKey"
      @ok="getList"
    />
    <!-- 数据展示 -->
    <a-table
      :loading="loading"
      :size="tableSize"
      rowKey="id"
      :columns="columns"
      :data-source="list"
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      :pagination="false">
      <span slot="deviceId" slot-scope="text, record">
        <a-button type="link"
                  @click="$router.push(`/system/instance/instance-details/${record.id}`)">{{
            record.deviceId
          }}</a-button>
      </span>
      <span slot="state" slot-scope="text, record"
            :class="{'status online': record.state, 'status offline': !record.state}">
        <span>{{ stateFormat(record) }}</span>
      </span>
      <span slot="onlineTime" slot-scope="text, record">
        {{ parseTime(record.onlineTime) }}
      </span>
      <span slot="operation" slot-scope="text, record">
        <a @click="$refs.createForm.handleUpdate(record, undefined)"
           v-hasPermi="['system:instance:edit']">
          <iot-icon type="icon-u-edit"/>
          修改
        </a>
        <a-divider type="vertical" v-hasPermi="['system:instance:remove']"/>
        <a style="color:#F53F3F" @click="handleDelete(record)"
           v-hasPermi="['system:instance:remove']">
          <iot-icon type="icon-u-del"/>
          删除
        </a>
      </span>
    </a-table>
    <import-product-form ref="ImportProductForm" @ok="getList"/>
    <!-- 分页 -->
    <a-pagination
      class="ant-table-pagination"
      show-size-changer
      show-quick-jumper
      :current="queryParam.pageNum"
      :total="total"
      :page-size="queryParam.pageSize"
      :showTotal="total => `共 ${total} 条`"
      @showSizeChange="onShowSizeChange"
      @change="changeSize"
    />
  </a-card>

</template>

<script>

import instanceCreateForm from './instanceCreateForm'
import ImportProductForm from '@/views/system/product/modules/ImportForm'
import {delInstance, listInstance} from '@/api/system/dev/instance'

export default {
  name: 'ProductInstance',
  components: {
    ImportProductForm,
    instanceCreateForm
  },
  props: {
    productKey: {
      type: String,
      require: true,
      default: ''
    }
  },
  data() {
    return {
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      ids: [],
      loading: false,
      total: 0,
      // 0-离线，1-在线字典
      stateOptions: [],
      // 查询参数
      queryParam: {
        iotId: null,
        deviceId: null,
        extDeviceId: null,
        deriveMetadata: null,
        configuration: null,
        productName: null,
        nickName: null,
        features: null,
        parentId: null,
        orgId: null,
        productKey: null,
        deviceName: null,
        creatorId: null,
        instance: null,
        application: null,
        creatorName: null,
        state: undefined,
        detail: null,
        registryTime: null,
        onlineTime: null,
        areasId: null,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        // {
        //   title: '设备iotId',
        //   dataIndex: 'iotId',
        //   ellipsis: true,
        //   align: 'center'
        // },
        {
          title: '设备序列号',
          dataIndex: 'deviceId',
          ellipsis: true,
          scopedSlots: {customRender: 'deviceId'},
          align: 'center'
        },
        {
          title: '设备名称',
          dataIndex: 'deviceName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '产品名称',
          dataIndex: 'productName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '设备状态',
          dataIndex: 'state',
          scopedSlots: {customRender: 'state'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '最后通信时间',
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '操作',
          dataIndex: 'operation',
          width: '18%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  filters: {},
  created() {
    // 移除自动加载，改为监听productKey变化
    this.getDicts('dev_instance_state').then(response => {
      this.stateOptions = response.data
    })
  },
  watch: {
    productKey: {
      handler(newVal) {
        if (newVal) {
          this.getList()
        }
      },
      immediate: true
    }
  },
  methods: {
    /** 查询设备列表 */
    getList() {
      this.loading = true
      this.queryParam.productKey = this.productKey
      listInstance(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 0-离线，1-在线字典翻译
    stateFormat(row, column) {
      return this.selectDictLabel(this.stateOptions, row.state)
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        iotId: undefined,
        deviceId: undefined,
        extDeviceId: undefined,
        deriveMetadata: undefined,
        configuration: undefined,
        productName: undefined,
        nickName: undefined,
        features: undefined,
        parentId: undefined,
        orgId: undefined,
        productKey: undefined,
        deviceName: undefined,
        creatorId: undefined,
        instance: undefined,
        application: undefined,
        creatorName: undefined,
        state: undefined,
        detail: undefined,
        registryTime: undefined,
        onlineTime: undefined,
        areasId: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return delInstance(ids)
          .then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success(
              '删除成功',
              3
            )
          })
        },
        onCancel() {
        }
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      var that = this
      this.$confirm({
        title: '是否确认导出?',
        content: '此操作将导出当前条件下所有数据而非选中数据',
        onOk() {
          that.download('/admin/v1/device/export', {
            ...that.queryParam
          }, `instance_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    /** 查看设备详情 */
    checkDetails(row) {
      const deviceId = row.id
      this.$router.push(`/system/instance/instance-details/${deviceId}`)
    }
  }
}
</script>
<style scoped lang="less">
.status {
  display: flex;
  align-items: center;
  justify-content: center;
}

.online {
  &::before {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #52c41a;
  }
}

.offline {
  &::before {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #f5222d;
  }
}
</style>
