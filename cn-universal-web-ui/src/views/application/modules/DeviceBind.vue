<template>
  <a-card :bordered="false">
    <!-- 条件搜索 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="设备序列号" prop="deviceId">
              <a-input v-model.trim="queryParam.deviceId" placeholder="请输入设备序列号"
                       allow-clear/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="设备名称" prop="deviceName">
              <a-input v-model.trim="queryParam.deviceName" placeholder="请输入设备名称"
                       allow-clear/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="产品名称" prop="productName">
              <a-input v-model.trim="queryParam.productName" placeholder="请输入产品名称"
                       allow-clear/>
            </a-form-item>
          </a-col>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span
              class="table-page-search-submitButtons"
              :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="handleQuery"><iot-icon
                type="icon-search"/>查询</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 操作 -->
    <div class="table-operations">
      <a-button type="primary" @click="showBindModel" v-hasPermi="['application:application:bind']">
        <iot-icon type="icon-u-add"/>
        绑定
      </a-button>
      <a-button type="primary" @click="unbind" v-hasPermi="['application:application:unbind']">
        <iot-icon type="icon-u-del"/>
        解绑
      </a-button>
      <a-button type="primary" @click="handleExport" v-hasPermi="['system:instance:export']">
        <iot-icon type="icon-u-export"/>
        导出
      </a-button>
      <a-button type="primary" @click="showGroup" v-hasPermi="['application:application:bind']">
        <iot-icon type="icon-u-add"/>
        按组绑定
      </a-button>
      <a-button
        type="dashed"
        shape="circle"
        :loading="loading"
        :style="{float: 'right'}"
        icon="reload"
        @click="getList"/>
    </div>
    <div>
      <a-modal :visible="groupVisible" title="分组选择" @ok="handleOk" @cancel="closeModel">
        <a-form-item label="选择要绑定的设备群组">
          <a-tree
            ref="tree"
            :tree-data="devGroup"
            :show-line="true"
            :show-icon="true"
            :default-expanded-keys="[]"
            @select="onSelect"
          >
          </a-tree>
        </a-form-item>
      </a-modal>
    </div>
    <div>
      <a-modal :visible="modelVisible" title="设备选择" width="80%" @ok="handleOk"
               @cancel="closeModel">
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item label="设备序列号" prop="deviceId">
                  <a-input v-model="unQueryParam.deviceId" placeholder="请输入设备序列号"
                           allow-clear/>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="设备名称" prop="deviceName">
                  <a-input v-model="unQueryParam.deviceName" placeholder="请输入设备名称"
                           allow-clear/>
                </a-form-item>
              </a-col>
              <span
                class="table-page-search-submitButtons"
                :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="getUnBindList"><iot-icon
                  type="icon-search"/>查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQueryUn"><iot-icon
                  type="icon-refresh"/>重置</a-button>
              </span>
            </a-row>
          </a-form>
        </div>
        <a-table
          :loading="loading"
          :size="tableSize"
          rowKey="id"
          :columns="unColumns"
          :data-source="unBindList"
          :row-selection="{ selectedRowKeys: selectedRowKeysUn, onChange: onSelectChangeUn,
                            onSelect:onSelectUn, onSelectAll:onSelectAll,}"
          :pagination="false"
        >
          <span
            slot="state"
            slot-scope="text, record"
            :class="{'status online': record.state, 'status offline': !record.state}">
            <span>{{ stateFormat(record) }}</span>
          </span>
          <span slot="onlineTime" slot-scope="text, record">
            {{ parseTime(record.onlineTime) }}
          </span>
          <span slot="application" slot-scope="text, record">
            {{ appNameFormat(record) }}
          </span>
          <!--          <span slot="operation" slot-scope="text, record">-->
          <!--        <a @click="bind(record)" v-hasPermi="['application:application:bind']" :hidden="record.application">-->
          <!--                      <platform-icon type="icon-u-add" />绑定-->
          <!--                    </a>-->
          <!--            &lt;!&ndash;        <a @click="unbind(record)" v-hasPermi="['application:application:unbind']" :hidden="!record.application">&ndash;&gt;-->
          <!--            &lt;!&ndash;          <platform-icon type="icon-u-del" />解绑&ndash;&gt;-->
          <!--            &lt;!&ndash;        </a>&ndash;&gt;-->
          <!--      </span>-->
        </a-table>
        <!-- 分页 -->
        <a-pagination
          style="float:right;margin-top:16px"
          show-size-changer
          show-quick-jumper
          :current="unQueryParam.pageNum"
          :total="unBindTotal"
          :page-size="unQueryParam.pageSize"
          :showTotal="total => `共 ${unBindTotal} 条`"
          @showSizeChange="onShowUnSizeChange"
          @change="unChangeSize"
        />
        <div style="clear:both"></div>
        <!--        <a-form-item label="选择要绑定的应用">-->
        <!--          <a-select placeholder="请选择" v-model="bindApp" style="width: 100%" allow-clear>-->
        <!--            <a-select-option v-for="(d, index) in applications" :key="index" :value="d.appUniqueId">{{-->
        <!--                d.appName-->
        <!--              }}-->
        <!--            </a-select-option>-->
        <!--          </a-select>-->
        <!--        </a-form-item>-->
      </a-modal>
    </div>
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
      <span slot="productName" slot-scope="text, record">
          <div class="product-name">
            {{ record.productName || '未知产品' }}
            <span v-if="record.deviceNode === 'DEVICE'" class="sub-device-badge">直</span>
            <span v-if="record.deviceNode === 'GATEWAY'" class="sub-device-badge">网</span>
            <span v-if="record.deviceNode === 'GATEWAY_SUB_DEVICE'"
                  class="sub-device-badge">子</span>
          </div>
        </span>
      <span
        slot="state"
        slot-scope="text, record"
        :class="{'status online': record.state, 'status offline': !record.state}">
        <span>{{ stateFormat(record) }}</span>
      </span>
      <span slot="onlineTime" slot-scope="text, record">
        {{ parseTime(record.onlineTime) }}
      </span>
      <span slot="application" slot-scope="text, record">
        {{ appNameFormat(record) }}
      </span>
      <span slot="operation" slot-scope="text, record">
        <a @click="unbind(record)" v-hasPermi="['application:application:unbind']"
           :hidden="!record.application">
          <iot-icon type="icon-u-del"/>解绑
        </a>
      </span>
    </a-table>
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
import {bindInstance, listInstance, listUnInstance} from '@/api/system/dev/instance'
import {listApplication} from '@/api/application/application'
import {deviceListByGroupId, listGroup} from '@/api/system/dev/devGroup'

export default {
  name: 'DeviceBind',
  components: {
    // CreateForm
  },
  props: {
    appUniqueId: {
      type: String,
      default: '',
      required: false
    }
  },
  data() {
    return {
      list: [],
      unBindList: [],
      applications: [],
      selectedRowKeys: [],
      // 弹出的绑定页面
      selectedRowKeysUn: [],
      selectedRows: [],
      // 弹出的绑定页面
      selectedRowsUn: [],
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      singleUn: true,
      // 非多个禁用
      multiple: true,
      multipleUn: true,
      id: undefined,
      ids: [],
      idsUn: [],
      loading: false,
      total: 0,
      unBindTotal: 0,
      open: false,
      devGroup: [],
      // 0-离线，1-在线字典
      stateOptions: [],
      // 设备选择弹窗可视
      modelVisible: false,
      // 分组可视
      groupVisible: false,
      // 当前选择绑定的应用
      bindApp: null,
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
        state: null,
        detail: null,
        registryTime: null,
        onlineTime: null,
        areasId: null,
        pageNum: 1,
        pageSize: 10
      },
      unQueryParam: {
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
        state: null,
        detail: null,
        registryTime: null,
        onlineTime: null,
        areasId: null,
        pageNum: 1,
        pageSize: 10
      },
      groupId: undefined,
      columns: [
        // {
        //   title: '编号',
        //   dataIndex: 'id',
        //   ellipsis: true,
        //   align: 'center'
        // },
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
        // {
        //   title: '${comment}',
        //   dataIndex: 'creatorId',
        //   ellipsis: true,
        //   align: 'center'
        // },
        // {
        //   title: '实例名称',
        //   dataIndex: 'instance',
        //   ellipsis: true,
        //   align: 'center'
        // },
        // {
        //   title: '归属应用',
        //   dataIndex: 'application',
        //   scopedSlots: { customRender: 'application' },
        //   ellipsis: true,
        //   align: 'center'
        // },
        // {
        //   title: '${comment}',
        //   dataIndex: 'creatorName',
        //   ellipsis: true,
        //   align: 'center'
        // },
        // {
        //   title: '绑定状态',
        //   dataIndex: 'state',
        //   scopedSlots: { customRender: 'state' },
        //   // customRender: (state) => { return state },
        //   ellipsis: true,
        //   align: 'center'
        // },
        {
          title: '操作',
          dataIndex: 'operation',
          width: '18%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      unColumns: [
        // {
        //   title: '编号',
        //   dataIndex: 'id',
        //   ellipsis: true,
        //   align: 'center'
        // },
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
        }
      ]
    }
  },
  filters: {},
  created() {
    this.getList()
    this.getDicts('dev_instance_state').then(response => {
      this.stateOptions = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询设备列表 */
    getList() {
      this.bindApp = this.appUniqueId
      this.loading = true
      this.queryParam.application = this.appUniqueId
      listInstance(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
      // 查询账号所拥有应用列表
      listApplication(null).then(response => {
        this.applications = response.rows
      })
      this.getUnBindList()
    },
    getUnBindList() {
      listUnInstance(this.unQueryParam).then(response => {
        this.unBindList = response.rows
        this.unBindTotal = response.total
        this.loading = false
      })
    },
    handleOk() {
      if (this.idsUn.length === 0) {
        alert('请先选择设备')
      } else {
        this.bind({})
      }
      // else if (this.bindApp === null || this.bindApp === undefined) {
      //   alert('请先选择应用')
      // }
    },
    onSelect(selectedKeys, e) {
      if (e.selected) {
        // 获取设备列表
        this.groupId = e.selectedNodes[0].data.props.id
        this.ids = []
        this.getDeviceList()
      } else {
        this.ids = []
      }
    },
    onSelectUn(record, selected, selectedRows) {
      if (selected) {
        this.selectedRowsUn.push(record)
      }

      if (!selected) {
        const delIndex = this.selectedRowsUn.findIndex(val => {
          return val.id === record.id
        })
        this.selectedRowsUn.splice(delIndex, 1)
      }
      this.idsUn = this.selectedRowsUn.map(item => item.id)
    },
    onSelectAll(selected, selectedRows, changeRows) {
      if (selected) {
        this.selectedRowsUn = this.selectedRowsUn.concat(changeRows)
      }
      if (!selected) {
        let selectedRows = JSON.parse(JSON.stringify(this.selectedRowsUn))
        const delIndex = []
        selectedRows.forEach((item, index) => {
          changeRows.forEach((val, itemIndex) => {
            if (item.id === val.id) {
              delIndex.push(index)
            }
          })
        })
        delIndex.forEach(item => {
          delete selectedRows[item]
        })
        selectedRows = selectedRows.filter(item => {
          return item !== undefined
        })
        this.selectedRowsUn = selectedRows
      }
      this.idsUn = this.selectedRowsUn.map(item => item.id)
    },
    // 根据分组id获取设备列表
    getDeviceList() {
      this.loading = true
      deviceListByGroupId({
        groupId: this.groupId
      }).then(response => {
        this.idsUn = response.rows.map(item => {
          return item.id
        })
        if (this.idsUn.length === 0) {
          this.$message.error('该分组下无设备,请重新选择分组！')
        }
        this.loading = false
      })
    },
    closeModel() {
      this.modelVisible = false
      this.groupVisible = false
    },
    showGroup() {
      listGroup().then(res => {
        this.groupVisible = true
        this.devGroup = res
        // this.modelVisible = true
      })
    },
    showBindModel(row) {
      // this.groupVisible = false
      this.getUnBindList()
      this.modelVisible = true
      this.id = row.id
      this.selectedRowKeysUn = []
      this.selectedRowsUn = []
    },
    // 应用名称翻译
    appNameFormat(row) {
      var actions = []
      const aa = this.applications
      Object.keys(aa).some((key) => {
        if (aa[key].appUniqueId === ('' + row.application)) {
          actions.push(aa[key].appName)
          return true
        }
      })
      return actions.join('')
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
    /** 重置按钮操作 */
    resetQueryUn() {
      this.unQueryParam = {
        deviceId: undefined,
        deviceName: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.getUnBindList()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    onShowUnSizeChange(current, pageSize) {
      this.unQueryParam.pageSize = pageSize
      this.getUnBindList()
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    unChangeSize(current, pageSize) {
      this.unQueryParam.pageNum = current
      this.unQueryParam.pageSize = pageSize
      this.getUnBindList()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    onSelectChangeUn(selectedRowKeys, selectedRows) {
      this.selectedRowKeysUn = selectedRowKeys
      // this.selectedRowsUn = selectedRows
      // this.idsUn = this.selectedRowsUn.map(item => item.id)
      this.singleUn = selectedRowKeys.length !== 1
      this.multipleUn = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    bind(row) {
      var that = this
      const ids = row.id || this.idsUn.toString()
      const appUniqueId = this.bindApp
      this.$confirm({
        title: '确认绑定所选设备？',
        content: '当前选中编号为' + ids + '的设备',
        onOk() {
          return bindInstance({ids: ids, appUniqueId: appUniqueId})
          .then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success(
              '绑定成功',
              3
            )
            that.closeModel()
          })
        },
        onCancel() {
        }
      })
    },
    // 跳转设备详情页
    toDetails(row) {
      const deviceId = row.id
      this.$router.push(`/system/instance/instance-details/${deviceId}`)
    },
    /** 删除按钮操作 */
    unbind(row) {
      var that = this
      const ids = row.id || this.ids.toString()
      if (ids === null || ids === '') {
        alert('请选择要解绑的设备!')
      } else {
        this.$confirm({
          title: '解绑设备',
          content: '确定解绑设备编号"' + row.deviceId + '"的设备',
          onOk() {
            return bindInstance({ids: ids, appUniqueId: null})
            .then(() => {
              that.onSelectChange([], [])
              that.getList()
              that.$message.success(
                '解绑成功',
                3
              )
              that.closeModel()
            })
          },
          onCancel() {
          }
        })
      }
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

/* 子设备标识样式 */
.sub-device-badge {
  display: inline-block;
  background-color: #ff7a45;
  color: white;
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 8px;
  margin-left: 6px;
  font-weight: 500;
  line-height: 1.2;
}
</style>
