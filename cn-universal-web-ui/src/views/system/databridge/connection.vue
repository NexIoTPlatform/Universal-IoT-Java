<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="连接名称" prop="name">
                <a-input v-model="queryParam.name" placeholder="请输入连接名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="连接类型" prop="type">
                <a-select v-model="queryParam.type" placeholder="请选择连接类型" style="width: 100%"
                          allow-clear>
                  <a-select-option value="MYSQL">MySQL</a-select-option>
                  <a-select-option value="KAFKA">Kafka</a-select-option>
                  <a-select-option value="MQTT">MQTT</a-select-option>
                  <a-select-option value="HTTP">HTTP</a-select-option>
                  <a-select-option value="IOTDB">IoTDB</a-select-option>
                  <a-select-option value="INFLUXDB">InfluxDB</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="插件类型" prop="pluginType">
                <a-select v-model="queryParam.pluginType" placeholder="请选择插件类型" style="width: 100%"
                          allow-clear>
                  <a-select-option value="JDBC">JDBC</a-select-option>
                  <a-select-option value="KAFKA">Kafka</a-select-option>
                  <a-select-option value="MQTT">MQTT</a-select-option>
                  <a-select-option value="HTTP">HTTP</a-select-option>
                  <a-select-option value="IOTDB">IoTDB</a-select-option>
                  <a-select-option value="INFLUXDB">InfluxDB</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery">{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" @click="handleAdd" v-hasPermi="['databridge:resource:add']">{{ $t('button.add') }}
        </a-button>
        <a-button type="primary" :disabled="single"
                  @click="handleUpdate(undefined, ids)"
                  v-hasPermi="['databridge:resource:edit']">{{ $t('button.edit') }}
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="getList">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
      <!-- 增加修改 -->
      <resource-form
        :visible="formVisible"
        :form-data="formData"
        @cancel="handleFormCancel"
        @ok="handleFormOk"
      />
      <!-- 数据展示 -->
      <a-table :loading="loading" :size="tableSize" rowKey="id" :columns="columns"
               :data-source="list"
               :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
               :pagination="false">
        <div slot="connectionInfo" slot-scope="text, record" class="connection-info-cell">
          <div class="connection-info-content">
            <div class="connection-name">
              <a-button type="link" @click="handleUpdate(record, undefined)" class="connection-name-link">
                {{ record.name || '未命名连接' }}
              </a-button>
            </div>
            <div class="connection-type">{{ getConnectionTypeName(record) }}
              <device-type-badge color="blue" :text="record.pluginType"/>
            </div>


          </div>
        </div>

        <div slot="connectionDetails" slot-scope="text, record" class="connection-details-cell">
          <div class="host-port">
            {{ formatHostPort(record.host, record.port) }}
          </div>
          <div v-if="record.databaseName" class="database-name">{{ record.databaseName }}</div>
        </div>

        <div slot="dataDirection" slot-scope="text, record" class="data-direction-cell">
          <div class="direction-info">
            <span class="direction-icon">{{ getDataDirectionSymbol(record.dataDirection || record.direction) }}</span>
            <span class="direction-text">{{ getDataDirectionText(record.dataDirection || record.direction) }}</span>
          </div>
        </div>

        <div slot="status" slot-scope="text, record" class="status-cell">
          <div :class="{ 'status-badge online': record.status === 1, 'status-badge offline': record.status !== 1 }">
            <span class="status-dot"></span>
            <span class="status-text">{{ record.status === 1 ? '启用' : '禁用' }}</span>
          </div>
        </div>

        <span slot="operation" slot-scope="text, record" class="operation-buttons">
          <a @click="testResource(record)" v-hasPermi="['databridge:resource:test']" class="operation-btn">
            {{ $t('button.test') }}</a>
          <!-- <a-divider type="vertical" v-hasPermi="['databridge:resource:edit']"/>
          <a @click="handleUpdate(record, undefined)" v-hasPermi="['databridge:resource:edit']" class="operation-btn">
            {{ $t('button.edit') }} </a> -->
          <a-divider type="vertical" v-hasPermi="['databridge:resource:remove']"/>
          <a style="color:#F53F3F" @click="handleDelete(record)" v-hasPermi="['databridge:resource:remove']"
             class="operation-btn">
            {{ $t('button.delete') }}</a>
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
import {createResource, deleteResource, getResourceList, testResource, updateResource} from '@/api/databridge/resource'
import ResourceForm from './modules/ResourceForm'

export default {
  name: 'DataBridgeConnection',
  components: {
    ResourceForm
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
        type: null,
        pluginType: null,
        pageNum: 1,
        pageSize: 10
      },
      // 表单相关
      formVisible: false,
      formData: {},
      columns: [
        {
          title: this.$t('compound.connectionNameType'),
          dataIndex: 'connectionInfo',
          scopedSlots: {customRender: 'connectionInfo'},
          width: '28%',
          align: 'left'
        },
        {
          title: this.$t('network.connectionInfo'),
          dataIndex: 'connectionDetails',
          scopedSlots: {customRender: 'connectionDetails'},
          width: '22%',
          align: 'left'
        },
        {
          title: this.$t('network.dataFlow'),
          dataIndex: 'dataDirection',
          scopedSlots: {customRender: 'dataDirection'},
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          width: '18%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '20%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询连接列表 */
    getList() {
      this.loading = true
      if (this.queryParam.name !== undefined && this.queryParam.name !== null) {
        this.queryParam.name = this.queryParam.name.replace(/^\s*|\s*$/g, '')
      }
      getResourceList(this.queryParam).then(response => {
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
        type: null,
        pluginType: null,
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
      if (!row || !row.id) {
        this.$message.warning('请选择要删除的数据')
        return
      }
      this.$confirm({
        title: '确认删除数据?',
        content: `确认删除数据源 "${row.name || '未命名'}" 吗？`,
        onOk() {
          return deleteResource(row.id)
            .then(() => {
              that.onSelectChange([], [])
              that.getList()
              that.$message.success('删除成功', 3)
            })
            .catch(() => {
              that.$message.error('删除失败', 3)
            })
        },
        onCancel() {
        }
      })
    },
    /** 测试连接 */
    async testResource(record) {
      try {
        const response = await testResource(record.id)
        if (response.code === 0) {
          this.$message.success('连接测试成功')
        } else {
          this.$message.error(response.msg || '连接测试失败')
        }
      } catch (error) {
        this.$message.error('连接测试失败')
      }
    },
    /** 获取连接类型名称 */
    getConnectionTypeName(record) {
      const typeMap = {
        'MYSQL': 'MySQL',
        'KAFKA': 'Kafka',
        'MQTT': 'MQTT',
        'HTTP': 'HTTP',
        'IOTDB': 'IoTDB',
        'INFLUXDB': 'InfluxDB'
      }
      return typeMap[record.type] || record.type || '未知'
    },
    /** 获取数据方向符号 */
    getDataDirectionSymbol(direction) {
      const symbolMap = {
        'INPUT': '↓',
        'OUTPUT': '↑',
        'BIDIRECTIONAL': '↕',
        'IN': '↓',
        'OUT': '↑',
        'BOTH': '↕'
      }
      return symbolMap[direction] || '?'
    },
    /** 获取数据方向文本 */
    getDataDirectionText(direction) {
      const textMap = {
        'INPUT': '输入',
        'OUTPUT': '输出',
        'BIDIRECTIONAL': '双向',
        'IN': '输入',
        'OUT': '输出',
        'BOTH': '双向'
      }
      return textMap[direction] || '未知'
    },
    /** 格式化主机地址和端口显示 */
    formatHostPort(host, port) {
      if (!host) return '未设置'
      if (!port || port === 0 || port === '0') {
        return host
      }
      return `${host}:${port}`
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
          response = await updateResource(formData.id, formData)
        } else {
          // 新增
          response = await createResource(formData)
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

/* 连接信息单元格样式 */
.connection-info-cell {
  padding: 12px 0;
}

.connection-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.connection-name {
  line-height: 1.4;
}

.connection-name-link {
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

.connection-name-link:focus,
.connection-name-link:active {
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.connection-name-link:hover {
  color: #40a9ff;
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.connection-type {
  font-size: 13px;
  color: #595959;
  font-weight: 600;
  line-height: 1.4;
}

.plugin-type {
  font-size: 11px;
  color: #8c8c8c;
  line-height: 1.4;
}

/* 连接详情单元格样式 */
.connection-details-cell {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.host-port {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
}

.database-name {
  font-size: 12px;
  color: #595959;
  line-height: 1.4;
}

/* 数据方向单元格样式 */
.data-direction-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.direction-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.direction-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  color: white;
  background-color: #1890ff;
}

.direction-text {
  font-size: 12px;
  color: #666;
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