<template>
  <div class="metadata-container">
    <!-- 标签页导航和操作按钮 -->
    <div class="metadata-header">
      <div class="metadata-tabs">
        <div class="tab-nav-item" :class="{ active: activeTab === 'properties' }"
             @click="switchTab('properties')">
          <i class="anticon anticon-setting"></i>
          <span>属性</span>
          <span class="tab-count" v-if="propertieslist.length > 0">({{
              propertieslist.length
            }})</span>
        </div>
        <div class="tab-nav-item" :class="{ active: activeTab === 'events' }"
             @click="switchTab('events')">
          <i class="anticon anticon-bell"></i>
          <span>事件</span>
          <span class="tab-count" v-if="eventslist.length > 0">({{ eventslist.length }})</span>
        </div>
        <div class="tab-nav-item" :class="{ active: activeTab === 'functions' }"
             @click="switchTab('functions')">
          <i class="anticon anticon-function"></i>
          <span>功能</span>
          <span class="tab-count" v-if="functionslist.length > 0">({{
              functionslist.length
            }})</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="metadata-actions">
        <a-button
          type="primary"
          icon="eye"
          size="small"
          @click="openMetadataDetail"
          class="action-btn"
        >
          查看物模型
        </a-button>
        <a-button
          v-if="creatorId === $store.state.user.name"
          v-hasPermi="['system:metadata:export']"
          type="default"
          icon="upload"
          size="small"
          @click="openImportMetadata"
          class="action-btn"
        >
          导入物模型
        </a-button>
      </div>
    </div>

    <!-- 标签页内容 -->
    <div class="metadata-content">
      <!-- 属性标签页 -->
      <div v-show="activeTab === 'properties'" class="tab-pane">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="action-bar">
            <div class="action-left">
              <a-button
                type="primary"
                icon="plus"
                @click="addProperties"
                v-if="creatorId === $store.state.user.name"
                v-hasPermi="['system:metadata:add']"
                class="add-btn"
              >
                添加属性
              </a-button>
            </div>
            <div class="action-right">
              <a-button
                type="text"
                :loading="loading"
                @click="getList"
                class="refresh-btn"
              >
                <a-icon type="sync" :spin="loading"/>
                刷新
              </a-button>
            </div>
          </div>
          <!-- 数据展示 -->
          <a-card class="meta-table-card" bordered>
            <a-table
              :loading="loading"
              size="middle"
              :row-key="record => record.id"
              :columns="properties"
              :data-source="propertieslist"
              :expandedRowKeys="expandedRowKeys"
              :expandIcon="() => null"
              :expandIconAsCell="false"
              :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
              :pagination="false"
              class="meta-table"
            >
              <template slot="action" slot-scope="text, record">
                <a-button
                  class="expand-btn"
                  v-if="record.type === 'enum'"
                  icon="plus"
                  type="default"
                  size="small"
                  @click="handleExpand(record.id)"
                />
              </template>
              <span slot="mode" slot-scope="text, record">
                <span v-if="record.mode === 'r'">{{ '只读' }}</span>
                <span v-else-if="record.mode === 'rw'">{{ '读写' }}</span>
              </span>
              <span slot="source" slot-scope="text, record">
                <span v-if="record.source === 'device'">{{ '设备' }}</span>
                <span v-else-if="record.source === 'extended'">{{ '扩展' }}</span>
                <span v-else>{{ record.source }}</span>
              </span>
              <span slot="specs" slot-scope="text, record">
                <div v-if="record.specs" class="specs-display">
                  <div v-if="record.specs.min" class="spec-item">
                    <span class="spec-label">最小值:</span>
                    <span class="spec-value">{{ record.specs.min }}</span>
                  </div>
                  <div v-if="record.specs.max" class="spec-item">
                    <span class="spec-label">最大值:</span>
                    <span class="spec-value">{{ record.specs.max }}</span>
                  </div>
                  <div v-if="record.specs.step" class="spec-item">
                    <span class="spec-label">步长:</span>
                    <span class="spec-value">{{ record.specs.step }}</span>
                  </div>
                  <div v-if="record.specs.length" class="spec-item">
                    <span class="spec-label">长度:</span>
                    <span class="spec-value">{{ record.specs.length }}</span>
                  </div>
                  <div v-if="record.specs.unit" class="spec-item">
                    <span class="spec-label">单位:</span>
                    <span class="spec-value">{{ record.specs.unit }}</span>
                  </div>
                </div>
              </span>
              <span slot="operation" slot-scope="text, record"
                    v-if="creatorId === $store.state.user.name">
                <a @click="editDetail(record, 'properties')" v-hasPermi="['system:metadata:edit']"
                   style="margin-left: 5px;margin-right: 5px">
                  <iot-icon type="icon-u-edit"/>修改
                </a>
                <a @click="modelDel(record, 'properties')" v-hasPermi="['system:metadata:delete']">
                  <iot-icon type="icon-u-del"/>删除
                </a>
              </span>
              <template slot="expandedRowRender" slot-scope="record">
                <div class="meta-expand-content">
                  <Analysis type="properties" :record="record"/>
                </div>
              </template>
            </a-table>
          </a-card>
        </div>
      </div>

      <!-- 事件标签页 -->
      <div v-show="activeTab === 'events'" class="tab-pane">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="action-bar">
            <div class="action-left">
              <a-button
                type="primary"
                icon="plus"
                @click="addEvent"
                v-if="creatorId === $store.state.user.name"
                v-hasPermi="['system:metadata:add']"
                class="add-btn"
              >
                添加事件
              </a-button>
            </div>
            <div class="action-right">
              <a-button
                type="text"
                :loading="loading"
                @click="getEventsList"
                class="refresh-btn"
              >
                <a-icon type="sync" :spin="loading"/>
                刷新
              </a-button>
            </div>
          </div>
          <!-- 数据展示 -->
          <a-card class="meta-table-card" bordered>
            <a-table
              :loading="loading"
              size="middle"
              :row-key="record => record.id"
              :columns="events"
              :data-source="eventslist"
              :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
              :pagination="false"
              class="meta-table"
            >
              <span slot="level" slot-scope="text, record">
                <span v-if="record.level === 'warn'">{{ '提醒告警' }}</span>
                <span v-else-if="record.level === 'ordinary'">{{ '普通告警' }}</span>
                <span v-else-if="record.level === 'urgent'">{{ '紧急告警' }}</span>
              </span>
              <span slot="outputData" slot-scope="text, record">
                <div v-if="record.outputData && record.outputData.length > 0"
                     class="output-data-display">
                  <div v-for="(item, index) in record.outputData" :key="index" class="output-item">
                    <span class="output-label">{{ item.identifier }}:</span>
                    <span class="output-type">{{ item.dataType }}</span>
                    <span v-if="item.name" class="output-name">({{ item.name }})</span>
                  </div>
                </div>
              </span>
              <span slot="operation" slot-scope="text, record">
                <a @click="editDetail(record, 'events')" v-if="creatorId === $store.state.user.name"
                   v-hasPermi="['system:metadata:edit']" style="margin-left: 5px;margin-right: 5px">
                  <iot-icon type="icon-u-edit"/>修改
                </a>
                <a @click="modelDel(record, 'events')" v-if="creatorId === $store.state.user.name"
                   v-hasPermi="['system:metadata:delete']">
                  <iot-icon type="icon-u-del"/>删除
                </a>
              </span>
            </a-table>
          </a-card>
        </div>
      </div>

      <!-- 功能标签页 -->
      <div v-show="activeTab === 'functions'" class="tab-pane">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="action-bar">
            <div class="action-left">
              <a-button
                type="primary"
                icon="plus"
                @click="addFunction"
                v-if="creatorId === $store.state.user.name"
                v-hasPermi="['system:metadata:add']"
                class="add-btn"
              >
                添加功能
              </a-button>
            </div>
            <div class="action-right">
              <a-button
                type="text"
                :loading="loading"
                @click="getFunctionsList"
                class="refresh-btn"
              >
                <a-icon type="sync" :spin="loading"/>
                刷新
              </a-button>
            </div>
          </div>
          <!-- 数据展示 -->
          <a-card class="meta-table-card" bordered>
            <a-table
              :loading="loading"
              size="middle"
              :row-key="record => record.id"
              :columns="functions"
              :data-source="functionslist"
              :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
              :pagination="false"
              class="meta-table"
            >
              <span slot="id" slot-scope="text, record">
                <a-button type="link" @click="openFunctionParamsDetails(record)">{{
                    record.id
                  }}</a-button>
              </span>
              <span slot="inputData" slot-scope="text, record">
                <div v-if="record.inputData && record.inputData.length > 0"
                     class="input-data-display">
                  <div v-for="(item, index) in record.inputData" :key="index" class="input-item">
                    <span class="input-label">{{ item.identifier }}:</span>
                    <span class="input-type">{{ item.dataType }}</span>
                    <span v-if="item.name" class="input-name">({{ item.name }})</span>
                  </div>
                </div>
              </span>
              <span slot="outputData" slot-scope="text, record">
                <div v-if="record.outputData && record.outputData.length > 0"
                     class="output-data-display">
                  <div v-for="(item, index) in record.outputData" :key="index" class="output-item">
                    <span class="output-label">{{ item.identifier }}:</span>
                    <span class="output-type">{{ item.dataType }}</span>
                    <span v-if="item.name" class="output-name">({{ item.name }})</span>
                  </div>
                </div>
              </span>
              <span slot="operation" slot-scope="text, record">
                <a @click="editDetail(record, 'functions')"
                   v-if="creatorId === $store.state.user.name" v-hasPermi="['system:metadata:edit']"
                   style="margin-left: 5px;margin-right: 5px">
                  <iot-icon type="icon-u-edit"/>修改
                </a>
                <a @click="modelDel(record, 'functions')"
                   v-if="creatorId === $store.state.user.name"
                   v-hasPermi="['system:metadata:delete']">
                  <iot-icon type="icon-u-del"/>删除
                </a>
              </span>
            </a-table>
          </a-card>
        </div>
      </div>
    </div>
    <PropertiesForm
      ref="functionPropertiesForm"
      :product-id="productId"
      :show="propertiesShow"
      :product-key="productKey"
      @ok="closeProForm"
    />
    <FunctionForm
      ref="functionFunctionForm"
      :product-id="productId"
      :show="functionShow"
      :product-key="productKey"
      @ok="closeFunctionForm"
    />
    <EventForm
      ref="functionEventForm"
      :product-id="productId"
      :show="eventShow"
      :product-key="productKey"
      @ok="closeEventForm"
    />
    <!--物模型功能参数详情-->
    <FunctionParamsDetails
      ref="functionParamsDetails"
      :functionId="functionId"
      :show="functionParamsDetailsShow"
      :function-params="functionParams"
      @close="closeFunctionParamsDetails"
    />
  </div>
</template>

<script>
import {
  getEventsList,
  getFunctionsList,
  getPropertiesList,
  metadataDelete
} from '@/api/system/dev/product'
import PropertiesForm from './PropertiesForm'
import FunctionForm from './FunctionForm'
import EventForm from './EventForm'
import FunctionParamsDetails from './FunctionParamsDetails'
import Analysis from './Analysis'

export default {
  name: 'MetadataPage',
  props: {
    productId: {
      type: String,
      require: true,
      default: ''
    },
    productKey: {
      type: String,
      require: true,
      default: ''
    },
    creatorId: {
      type: String,
      require: true,
      default: ''
    }
  },
  components: {
    PropertiesForm,
    FunctionForm,
    EventForm,
    FunctionParamsDetails,
    Analysis
  },
  data() {
    return {
      // 当前激活的标签页
      activeTab: 'properties',
      // 物模型功能参数数组
      functionParams: [],
      // 物模型功能参数详情显示控制
      functionParamsDetailsShow: false,
      functionId: '',
      // 属性添加显示控制
      propertiesShow: false,
      // 属性添加显示控制
      functionShow: false,
      // 事件添加显示控制
      eventShow: false,
      messages: undefined,
      createTimeRange: [],
      // 选择应用弹窗可视
      modelVisible: false,
      propertieslist: [],
      eventslist: [],
      functionslist: [],
      selectedRowKeys: [],
      selectedRows: [],
      expandedRowKeys: [],
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      ids: [],
      loading: false,
      total: 0,
      properties: [
        {
          title: '',
          dataIndex: 'action',
          width: '50px',
          scopedSlots: {customRender: 'action'},
          align: 'center'
        },
        {
          title: '属性标识',
          dataIndex: 'id',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '属性名称',
          dataIndex: 'name',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '数据类型',
          dataIndex: 'type',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '单位',
          dataIndex: 'unit',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '读写权限',
          dataIndex: 'mode',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'mode'}
        },
        {
          title: '属性来源',
          dataIndex: 'source',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'source'}
        },
        {
          title: '描述',
          dataIndex: 'description',
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
      ],
      events: [
        {
          title: '事件标识',
          dataIndex: 'id',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '事件名称',
          dataIndex: 'name',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '事件级别',
          dataIndex: 'level',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'level'}
        },
        {
          title: '输出参数',
          dataIndex: 'outputData',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'outputData'}
        },
        {
          title: '描述',
          dataIndex: 'description',
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
      ],
      functions: [
        {
          title: '功能标识',
          dataIndex: 'id',
          scopedSlots: {customRender: 'id'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '功能名称',
          dataIndex: 'name',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '输入参数',
          dataIndex: 'inputData',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'inputData'}
        },
        {
          title: '输出参数',
          dataIndex: 'outputData',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'outputData'}
        },
        {
          title: '描述',
          dataIndex: 'description',
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
    this.getList()
    this.getEventsList()
    this.getFunctionsList()
  },
  computed: {},
  watch: {},
  methods: {
    // 切换标签页
    switchTab(tab) {
      this.activeTab = tab
    },
    handleExpand(rowkey) {
      // 只允许当行展开逻辑
      if (this.expandedRowKeys.length > 0) {
        const index = this.expandedRowKeys.indexOf(rowkey)
        if (index > -1) {
          this.expandedRowKeys.splice(index, 1)
        } else {
          this.expandedRowKeys.splice(0, this.expandedRowKeys.length)
          this.expandedRowKeys.push(rowkey)
        }
      } else {
        this.expandedRowKeys.push(rowkey)
      }
    },
    /** 查询log列表 */
    getList() {
      this.loading = true
      getPropertiesList(this.productId).then(response => {
        this.propertieslist = response.data
        console.log('this.propertieslist = ', this.propertieslist)
        this.total = response.data.length
        this.loading = false
      })
    },
    getEventsList() {
      this.loading = true
      getEventsList(this.productId).then(response => {
        this.eventslist = response.data
        this.total = response.data.length
        this.loading = false
      })
    },
    getFunctionsList() {
      this.loading = true
      getFunctionsList(this.productId).then(response => {
        this.functionslist = response.data
        this.total = response.data.length
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        messageType: undefined,
        createTime: undefined,
        params: {}
      }
      this.createTimeRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    changeSize(current, pageSize) {
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
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
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
          that.download('system/log/export', {
            ...that.queryParam
          }, `log_${new Date().getTime()}.xlsx`)
        },
        onCancel() {
        }
      })
    },
    editDetail(row, type) {
      console.log('row.content = ', row)
      if (type === 'properties') {
        this.$refs.functionPropertiesForm.editProperties(row)
      } else if (type === 'functions') {
        this.$refs.functionFunctionForm.editFunctions(row)
      } else if (type === 'events') {
        this.$refs.functionEventForm.editEvents(row)
      }
    },
    closeModel() {
      this.modelVisible = false
    },
    // 添加属性
    addProperties() {
      if (this.propertiesShow) {
        this.propertiesShow = false
      } else {
        this.propertiesShow = true
      }
    },
    // 关闭属性表单
    closeProForm() {
      this.propertiesShow = false
      this.getList()
    },
    // 添加功能
    addFunction() {
      if (this.functionShow) {
        this.functionShow = false
      } else {
        this.functionShow = true
      }
    },
    // 关闭功能表单
    closeFunctionForm() {
      this.functionShow = false
      this.getFunctionsList()
    },
    // 添加事件
    addEvent() {
      if (this.eventShow) {
        this.eventShow = false
      } else {
        this.eventShow = true
      }
    },
    // 关闭事件表单
    closeEventForm() {
      this.eventShow = false
      this.getEventsList()
    },
    // 关闭物模型功能参数详情
    closeFunctionParamsDetails() {
      this.functionParamsDetailsShow = false
    },
    // 打开物模型功能参数详情
    openFunctionParamsDetails(recode) {
      this.functionParams = JSON.parse(recode.inputs)
      this.functionId = recode.id
      if (this.functionParamsDetailsShow) {
        this.functionParamsDetailsShow = false
      } else {
        this.functionParamsDetailsShow = true
      }
    },
    // 删除物模型
    modelDel(row, type) {
      console.log('row = ', row)
      const data = {
        // key
        productKey: this.productKey,
        // 属性、事件、功能类型，三选一
        type: type,
        // 用来判断删除或者修改那个属性
        oldId: row.id
      }
      const that = this
      this.$confirm({
        title: '确认删除所选中的属性/事件/功能?',
        content: '当前选中的标识为: ' + row.id,
        onOk() {
          return metadataDelete(data)
          .then(res => {
            if (type === 'properties') {
              that.getList()
            } else if (type === 'events') {
              that.getEventsList()
            } else if (type === 'functions') {
              that.getFunctionsList()
            }
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
    // 刷新物模型
    refreshMetadata() {
      this.getList()
      this.getEventsList()
      this.getFunctionsList()
    },
    // 打开物模型详情
    openMetadataDetail() {
      this.$emit('openMetadataDetail')
    },
    // 打开导入物模型
    openImportMetadata() {
      this.$emit('openImportMetadata')
    }
  }
}
</script>
<style scoped lang="less">
.metadata-container {
  padding: 0;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.metadata-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 12px;
  // border-bottom: 1px solid #f1f5f9;

  .metadata-tabs {
    display: flex;
    background: #ffffff;
    border-bottom: 1px solid #e2e8f0;
    padding: 0 16px;

    .tab-nav-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 10px 16px;
      font-size: 14px;
      font-weight: 500;
      color: #64748b;
      cursor: pointer;
      transition: all 0.3s ease;
      border-bottom: 2px solid transparent;
      position: relative;

      i {
        font-size: 14px;
      }

      .tab-count {
        font-size: 12px;
        color: #94a3b8;
        margin-left: 4px;
      }

      &:hover {
        color: #1966ff;
        background: #f8fafc;
      }

      &.active {
        color: #1966ff;
        border-bottom-color: #1966ff;
        background: #f0f9ff;

        .tab-count {
          color: #1966ff;
        }
      }
    }
  }

  .metadata-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      height: 32px;
      padding: 0 16px;
      border-radius: 6px;
      font-weight: 500;
      font-size: 13px;
      box-shadow: 0 1px 3px rgba(25, 102, 255, 0.15);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(25, 102, 255, 0.25);
      }
    }
  }


}

/* 标签页内容 */
.metadata-content {
  padding: 8px 12px;

  .tab-pane {
    animation: fadeIn 0.3s ease-in-out;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 标签页内容 */
.tab-content {
  background: #ffffff;
  border-radius: 12px;
  // border: 1px solid #e8eaed;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  margin-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;

  .action-left {
    display: flex;
    gap: 12px;

    .add-btn {
      height: 36px;
      padding: 0 20px;
      border-radius: 8px;
      font-weight: 500;
      box-shadow: 0 2px 4px rgba(25, 102, 255, 0.2);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 8px rgba(25, 102, 255, 0.3);
      }
    }
  }

  .action-right {
    .refresh-btn {
      height: 36px;
      padding: 0 16px;
      border-radius: 8px;
      color: #64748b;
      border: 1px solid #e2e8f0;
      background: #ffffff;

      &:hover {
        color: #1966ff;
        border-color: #1966ff;
        background: #f0f9ff;
      }
    }
  }
}

/* 表格卡片风格 */
.meta-table-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eaed;

  .ant-card-body {
    padding: 0;
    border-radius: 12px;
  }
}

.meta-table {
  border-radius: 12px;
  overflow: hidden;
}

.meta-table :deep(.ant-table) {
  border-radius: 12px;
  background: transparent;
  font-size: 14px;
}

.meta-table :deep(.ant-table-thead > tr > th) {
  background: #f8fafc;
  border-bottom: 1.5px solid #e2e8f0;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
  padding: 6px 8px;
  height: 36px;
}

.meta-table :deep(.ant-table-tbody > tr > td) {
  padding: 6px 8px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  background: #fff;
  line-height: 1.5;
  height: 36px;
}

.meta-table :deep(.ant-table-tbody > tr) {
  transition: all 0.2s ease;

  &:hover > td {
    background: #f4f8ff;
  }

  &.ant-table-row-selected > td {
    background: #e6f0ff;
  }
}

.meta-table :deep(.ant-table-expanded-row) {
  background: transparent;

  > td {
    padding: 0 !important;
    border-bottom: none !important;
  }

  .ant-table-expanded-row-content {
    margin: 0;
    padding: 16px 24px;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
    font-size: 14px;
    line-height: 1.6;
    min-width: 0;
  }
}

.meta-expand-content {
  padding: 0;
  background: transparent;
}

/* 展开按钮 */
.expand-btn {
  width: 20px;
  height: 20px;
  padding: 0;
  font-size: 10px;
  border-radius: 3px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #64748b;
  min-width: 20px;
  min-height: 20px;

  &:hover {
    color: #1966ff;
    border-color: #1966ff;
    background: #f0f9ff;
  }
}

/* 规格参数显示 */
.specs-display {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .spec-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;

    .spec-label {
      color: #64748b;
      font-weight: 500;
    }

    .spec-value {
      color: #1966ff;
      font-weight: 600;
    }
  }
}

/* 输入输出参数显示 */
.input-data-display,
.output-data-display {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .input-item,
  .output-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;

    .input-label,
    .output-label {
      color: #64748b;
      font-weight: 500;
    }

    .input-type,
    .output-type {
      color: #1966ff;
      font-weight: 600;
    }

    .input-name,
    .output-name {
      color: #059669;
      font-style: italic;
    }
  }
}

@media (max-width: 768px) {
  .metadata-container {
    padding: 0;
  }

  .metadata-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .metadata-tabs {
      padding: 0 12px;

      .tab-nav-item {
        padding: 8px 12px;
        font-size: 13px;

        i {
          font-size: 12px;
        }
      }
    }

    .metadata-actions {
      justify-content: center;
      gap: 8px;

      .action-btn {
        height: 28px;
        padding: 0 12px;
        font-size: 12px;
      }
    }
  }

  .metadata-content {
    padding: 8px 12px;
  }

  .action-bar {
    padding: 8px 0;
    flex-direction: column;
    gap: 8px;
    align-items: stretch;

    .action-left, .action-right {
      justify-content: center;
    }
  }

  .meta-table-card {
    margin-bottom: 12px;
  }

  .meta-table :deep(.ant-table-tbody > tr > td) {
    &:first-child {
      padding-left: 8px;
    }

    &:last-child {
      padding-right: 8px;
    }
  }
}
</style>
