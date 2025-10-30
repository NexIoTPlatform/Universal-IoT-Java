<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('rule.ruleName')" prop="name">
                <a-input v-model="queryParam.name" placeholder="请输入规则名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="桥接类型" prop="bridgeType">
                <a-select v-model="queryParam.bridgeType" placeholder="请选择桥接类型"
                          style="width: 100%"
                          allow-clear>
                  <a-select-option value="JDBC">JDBC</a-select-option>
                  <a-select-option value="KAFKA">Kafka</a-select-option>
                  <a-select-option value="MQTT">MQTT</a-select-option>
                  <a-select-option value="HTTP">HTTP</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery">{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">{{
                    $t('button.reset')
                  }}</a-button>
                <!-- <a-button type="primary" style="margin-left: 8px" @click="handleAdd" v-hasPermi="['databridge:config:add']">新增规则</a-button> -->
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" @click="handleAdd" v-hasPermi="['databridge:config:add']">{{
            $t('button.add')
          }}
        </a-button>
        <a-button type="primary" :disabled="single"
                  @click="handleUpdate(undefined, ids)"
                  v-hasPermi="['databridge:config:edit']">{{ $t('button.edit') }}
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['databridge:config:remove']">{{ $t('button.delete') }}
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="getList">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
      <!-- 增加修改 -->
      <config-form
        :visible="formVisible"
        :form-data="formData"
        :resource-list="resourceList"
        @cancel="handleFormCancel"
        @ok="handleFormOk"
      />
      <!-- 数据展示 -->
      <a-table :loading="loading" :size="tableSize" rowKey="id" :columns="columns"
               :data-source="list"
               :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
               :pagination="false">
        <div slot="ruleInfo" slot-scope="text, record" class="rule-info-cell">
          <div class="rule-info-content">
            <div class="rule-name">
              <a-button type="link" @click="handleUpdate(record, undefined)" class="rule-name-link">
                {{ record.name || '未命名规则' }}
                <device-type-badge color="green" :text="record.bridgeType"/>
              </a-button>
            </div>
          </div>
        </div>

        <div slot="targetConnection" slot-scope="text, record" class="target-connection-cell">
          <div class="target-connection-content">
            <div class="resource-name" :title="record.targetResourceName">
              {{ record.targetResourceName || '未设置' }}
            </div>
          </div>
        </div>

        <div slot="dataSource" slot-scope="text, record" class="data-source-cell">
          <div class="data-source-content">
            <!-- <div class="source-scope">
              <span class="scope-text">{{ getSourceScopeText(record.sourceScope) }}</span>
            </div> -->
            <div class="source-detail">
              <div v-if="record.sourceScope === 'ALL_PRODUCTS'" class="source-all">
                <span class="source-text">全量数据</span>
                <span class="source-icon">🌐</span>
              </div>
              <div v-else-if="record.sourceScope === 'SPECIFIC_PRODUCTS'" class="source-products">
                <span class="source-text">{{
                    getSourceProductCount(record.sourceProductKeys)
                  }}个产品</span>
                <a-tooltip v-if="getSourceProductKeys(record.sourceProductKeys).length > 0"
                           :title="getSourceProductNames(record.sourceProductNames).join(', ')">
                  <span class="source-info">ℹ️</span>
                </a-tooltip>
              </div>
              <div v-else-if="record.sourceScope === 'APPLICATION'" class="source-app">
                <span class="source-text">{{ record.sourceApplicationName || '应用数据' }}</span>
                <span class="source-icon">📱</span>
              </div>
            </div>
          </div>
        </div>

        <div slot="status" slot-scope="text, record" class="status-cell">
          <div
            :class="{ 'status-badge online': record.status === 1, 'status-badge offline': record.status !== 1 }">
            <span class="status-dot"></span>
            <span class="status-text">{{ record.status === 1 ? '启用' : '禁用' }}</span>
          </div>
        </div>

        <span slot="operation" slot-scope="text, record" class="operation-buttons">
          <a @click="validateConfig(record)" v-hasPermi="['databridge:config:validate']"
             class="operation-btn">
            {{ $t('button.verify') }} </a>
          <!-- <a-divider type="vertical" v-hasPermi="['databridge:config:edit']"/>
          <a @click="handleUpdate(record, undefined)" v-hasPermi="['databridge:config:edit']" class="operation-btn">
            {{ $t('button.edit') }} </a> -->
          <a-divider type="vertical" v-hasPermi="['databridge:config:remove']"/>
          <a style="color:#F53F3F" @click="handleDelete(record)"
             v-hasPermi="['databridge:config:remove']"
             class="operation-btn">
            {{ $t('button.delete') }} </a>
        </span>
      </a-table>
      <!-- 分页 -->
      <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper
                    :current="queryParam.pageNum"
                    :total="total" :page-size="queryParam.pageSize"
                    :showTotal="total => `共 ${total} 条`"
                    @showSizeChange="onShowSizeChange" @change="changeSize"/>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import {
  createConfig,
  deleteConfig,
  getConfigList,
  updateConfig,
  validateConfig
} from '@/api/databridge/config'
import {getResourceList} from '@/api/databridge/resource'
import ConfigForm from './modules/ConfigForm'

export default {
  name: 'DataBridgeConfig',
  components: {
    ConfigForm
  },
  data() {
    return {
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      ids: [],
      loading: false,
      total: 0,
      // 查询参数
      queryParam: {
        name: null,
        bridgeType: null,
        pageNum: 1,
        pageSize: 10
      },
      // 表单相关
      formVisible: false,
      formData: {},
      // 资源列表（用于桥接规则表单）
      resourceList: [],
      columns: [
        {
          title: this.$t('rule.ruleType'),
          dataIndex: 'ruleInfo',
          scopedSlots: {customRender: 'ruleInfo'},
          width: '28%',
          align: 'left'
        },
        {
          title: this.$t('network.targetConnection'),
          dataIndex: 'targetConnection',
          scopedSlots: {customRender: 'targetConnection'},
          width: '22%',
          align: 'left'
        },
        {
          title: this.$t('network.associatedProduct'),
          dataIndex: 'dataSource',
          scopedSlots: {customRender: 'dataSource'},
          width: '30%',
          align: 'left'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          width: '10%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '10%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  created() {
    this.getList()
    this.loadResourceList()
  },
  methods: {
    /** 查询桥接规则列表 */
    getList() {
      this.loading = true
      if (this.queryParam.name !== undefined && this.queryParam.name !== null) {
        this.queryParam.name = this.queryParam.name.replace(/^\s*|\s*$/g, '')
      }
      getConfigList(this.queryParam).then(response => {
        if (response.data && Array.isArray(response.data)) {
          this.list = response.data
          this.total = response.data.length
        } else if (response.data && response.data.rows) {
          this.list = response.data.rows || []
          this.total = response.data.total || 0
        } else {
          this.list = []
          this.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        name: null,
        bridgeType: null,
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
    /** 新增按钮操作 */
    handleAdd() {
      this.formData = {}
      this.formVisible = true
    },
    /** 修改按钮操作 */
    handleUpdate(row, ids) {
      this.formData = {...row}
      this.formVisible = true
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return deleteConfig(ids)
          .then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success('删除成功', 3)
          })
        },
        onCancel() {
        }
      })
    },
    /** 验证配置 */
    async validateConfig(record) {
      try {
        const response = await validateConfig(record.id)
        if (response.code === 0 && response.data) {
          this.$message.success('配置验证成功')
        } else {
          this.$message.error('配置验证失败')
        }
      } catch (error) {
        this.$message.error('配置验证失败')
      }
    },
    /** 获取数据源范围文本 */
    getSourceScopeText(scope) {
      const textMap = {
        'ALL_PRODUCTS': '所有产品',
        'SPECIFIC_PRODUCTS': '定向产品',
        'APPLICATION': '应用级'
      }
      return textMap[scope] || scope || '未知'
    },
    /** 获取产品键列表 */
    getSourceProductKeys(sourceProductKeys) {
      if (!sourceProductKeys) {
        return []
      }
      try {
        if (typeof sourceProductKeys === 'string') {
          return JSON.parse(sourceProductKeys)
        }
        return sourceProductKeys
      } catch (e) {
        return []
      }
    },
    /** 获取产品数量 */
    getSourceProductCount(sourceProductKeys) {
      const keys = this.getSourceProductKeys(sourceProductKeys)
      return keys.length
    },
    /** 获取产品名称列表 */
    getSourceProductNames(sourceProductNames) {
      if (!sourceProductNames || !Array.isArray(sourceProductNames)) {
        return []
      }
      return sourceProductNames.map(product => {
        return product.name || product.productKey || '未知产品'
      })
    },
    /** 表单取消 */
    handleFormCancel() {
      this.formVisible = false
      this.formData = {}
    },
    /** 表单提交成功 */
    async handleFormOk(formData) {
      try {
        let response
        if (formData.id) {
          // 更新
          response = await updateConfig(formData.id, formData)
        } else {
          // 新增
          response = await createConfig(formData)
        }
        if (response.code === 0) {
          this.$message.success('保存成功')
          this.formVisible = false
          this.formData = {}
          this.getList()
        } else {
          this.$message.error(response.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    /** 加载资源列表 */
    async loadResourceList() {
      try {
        const response = await getResourceList()
        if (response.data && Array.isArray(response.data)) {
          this.resourceList = response.data
        } else if (response.data && response.data.rows) {
          this.resourceList = response.data.rows || []
        } else {
          this.resourceList = []
        }
      } catch (error) {
        console.error('加载资源列表失败:', error)
        this.resourceList = []
      }
    }
  }
}
</script>

<style scoped lang="less">
.table-page-search-wrapper {
  margin-bottom: 16px;
}

.table-operations {
  margin-bottom: 16px;
}

/* 规则信息单元格样式 */
.rule-info-cell {
  padding: 12px 0;
}

.rule-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rule-name {
  line-height: 1.4;
}

.rule-name-link {
  font-size: 15px;
  font-weight: 600;
  padding: 0 !important;
  margin: 0;
  height: auto;
  color: #1890ff;
  text-align: left;
  justify-content: flex-start;
  line-height: 1.4;
  border: none;
  box-shadow: none;
  background: none;
}

.rule-name-link:focus,
.rule-name-link:active {
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.rule-name-link:hover {
  color: #40a9ff;
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.bridge-type {
  font-size: 13px;
  color: #595959;
  font-weight: 600;
  line-height: 1.4;
}

/* 目标连接单元格样式 */
.target-connection-cell {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.target-connection-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.resource-name {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.connection-type {
  font-size: 12px;
  color: #595959;
  line-height: 1.4;
}

/* 数据源单元格样式 */
.data-source-cell {
  padding: 12px 0;
}

.data-source-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.source-scope {
  display: flex;
  align-items: center;
  gap: 6px;
}

.scope-text {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.source-detail {
  display: flex;
  align-items: center;
  gap: 6px;
}

.source-all,
.source-products,
.source-app {
  display: flex;
  align-items: center;
  font-size: 12px;
  gap: 4px;
}

.source-icon {
  font-size: 14px;
}

.source-text {
  color: #666;
}

.source-info {
  font-size: 12px;
  color: #999;
  cursor: help;
  margin-left: 4px;
}

/* 状态单元格样式 */
.status-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-badge {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}

.status-badge.offline {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  color: #cf1322;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-badge.online .status-dot {
  background-color: #52c41a;
}

.status-badge.offline .status-dot {
  background-color: #f5222d;
}

/* 操作按钮样式优化 */
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 120px;
}

.operation-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}
</style>