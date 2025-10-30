<template>
  <page-header-wrapper>
    <!-- 对接流程-->
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('compound.productProductKey')" prop="id">
                <a-input v-model.trim="queryParam.id" placeholder="请输入ProductKey" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="驱动名称" prop="name">
                <a-input v-model.trim="queryParam.name" placeholder="请输入驱动名称" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="发布状态" prop="state">
                <a-select placeholder="请选择发布状态" style="width: 100%"
                          v-model="queryParam.state"
                          allow-clear>
                  <a-select-option v-for="(d, index) in protocolStateOptions" :key="index"
                                   :value="d.dictValue">
                    {{ d.dictLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="(!advanced && 8) || 24" :sm="24">
              <span
                class="table-page-search-submitButtons"
                :style="(advanced && { float: 'right', overflow: 'hidden' }) || {}"
              >
                <a-button type="primary" @click="handleQuery">
                  <a-icon type="search"/>{{ $t('button.query') }} </a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">
                  <a-icon type="reload"/>{{ $t('button.reset') }} </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['protocol:protocol:add']">
          <a-icon type="plus"/>
          {{ $t('button.add') }}
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['protocol:protocol:remove']">
          <a-icon type="delete"/>
          {{ $t('button.delete') }}
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="getList">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
      <!-- 增加修改 -->
      <create-form
        ref="createForm"
        :protocolTypeOptions="protocolTypeOptions"
        :protocol-state-options="protocolStateOptions"
        :product-option="productOptions"
        :connectionOptions="connectionOptions"
        @ok="getList"
      />
      <codec-test ref="codecTest"/>
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="id"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false"
        :scroll="{ x: 1130 }"
        tableLayout="fixed"
      >
        <div slot="productInfo" slot-scope="text, record" class="product-info-cell">
          <div class="product-name">
            {{ record.name || '未命名驱动' }}
            <device-type-badge :type="record.type" :text="getProtocolTypeText(record.type)"/>
          </div>
          <div class="product-key">{{ record.id }}</div>
        </div>
        <span slot="createTime" slot-scope="text">
          <span v-if="text">{{ parseTime(text) }}</span>
          <span v-else style="color: #ccc;">-</span>
        </span>
        <span slot="version" slot-scope="text">
          <span v-if="text">{{ text }}</span>
          <span v-else style="color: #ccc;">-</span>
        </span>
        <span slot="description" slot-scope="text">
          <a-tooltip :title="text" v-if="text">
            <span class="desc-ellipsis">{{ text }}</span>
          </a-tooltip>
          <span v-else style="color: #ccc;">-</span>
        </span>
        <span slot="protocolState" slot-scope="text">
          <a-tooltip :title="text == 1 ? '已发布' : '未发布'">
            <a-switch
              :checked="text == 1"
              checked-children="开"
              un-checked-children="关"
              disabled
              style="vertical-align: middle"
            />
          </a-tooltip>
        </span>
        <span slot="operation" slot-scope="text, record">
          <div class="operation-buttons">
            <a-button type="link" icon="bug" @click="goToMagicWeb"
                      v-hasPermi="['protocol:protocol:codec']">
              {{ $t('button.debug') }} </a-button>
            <a-button type="link" icon="edit"
                      @click="$refs.createForm.handleUpdate(record, undefined)"
                      v-hasPermi="['protocol:protocol:edit']">
              {{ $t('button.edit') }} </a-button>
            <a-button type="link" icon="delete" danger @click="handleDelete(record)"
                      v-hasPermi="['protocol:protocol:remove']">
              {{ $t('button.delete') }} </a-button>
          </div>
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
        :showTotal="(total) => `共 ${total} 条`"
        @showSizeChange="onShowSizeChange"
        @change="changeSize"
      />
    </a-card>
  </page-header-wrapper>
</template>

<script>
import {delProtocol, listProtocol, updateProtocol} from '@/api/system/protocol'
import CreateForm from './modules/CreateForm'
import CodecTest from '@/views/system/protocol/modules/codecTest'

export default {
  name: 'ProtocolPage',
  components: {
    CreateForm,
    CodecTest
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
      protocolStateOptions: [],
      protocolTypeOptions: [],
      productOptions: [],
      connectionOptions: [],
      parentUnionId: this.$store.state.user.parentUnionId,
      // 查询参数
      queryParam: {
        id: null,
        name: null,
        description: null,
        state: undefined,
        type: null,
        configuration: null,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '驱动信息',
          dataIndex: 'productInfo',
          width: 280,
          ellipsis: true,
          scopedSlots: {customRender: 'productInfo'},
          align: 'left'
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          width: 160,
          scopedSlots: {customRender: 'createTime'},
          align: 'center'
        },
        {
          title: '版本号',
          dataIndex: 'version',
          width: 100,
          scopedSlots: {customRender: 'version'},
          align: 'center'
        },
        {
          title: '描述信息',
          dataIndex: 'description',
          width: 220,
          ellipsis: true,
          scopedSlots: {customRender: 'description'},
          align: 'left'
        },
        {
          title: '发布状态',
          dataIndex: 'state',
          width: 120,
          scopedSlots: {customRender: 'protocolState'},
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: 200,
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  filters: {},
  created() {
    this.getList()
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询设备协议列表 */
    getList() {
      this.loading = true
      listProtocol(this.queryParam).then((response) => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
      this.getDicts('protocol_state').then((response) => {
        this.protocolStateOptions = response.data
      })
      this.getDicts('connection_protocol').then((response) => {
        this.connectionOptions = response.data
      })
      this.getDicts('protocol_type').then((response) => {
        this.protocolTypeOptions = response.data
      })
      // listProduct(null).then((response) => {
      //   this.productOptions = response.rows.filter((i) => (this.parentUnionId === i.creatorId || this.$store.state.user.name === i.creatorId)).map((i) => {
      //     return { label: i.name, value: i.productKey }
      //   })
      //   allProtocol().then(res => {
      //     const data = res.data
      //     this.productOptions = this.productOptions.filter(o => data.indexOf(o.value) < 0)
      //   })
      //   // console.log(this.productOptions)
      // })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        id: undefined,
        name: undefined,
        description: undefined,
        state: undefined,
        type: undefined,
        configuration: undefined,
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
      this.ids = this.selectedRows.map((item) => item.id)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    checkLocal(row) {
      try {
        return JSON.parse(row.configuration).location === 'local'
      } catch (e) {
        return false
      }
    },
    /* 任务状态修改 */
    confirmHandleStatus(row) {
      const text = row.state === 1 ? '停用' : '发布'
      row.state = row.state === 0 ? 1 : 0
      updateProtocol(row)
      .then(() => {
        this.$message.success(text + '成功', 3)
        this.getList()
      })
      .catch(function () {
        this.$message.error(text + '发生异常', 3)
      })
    },
    cancelHandleStatus(row) {
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id || this.ids
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return delProtocol(ids).then(() => {
            that.onSelectChange([], [])
            that.getList()
            that.$message.success('删除成功', 3)
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
          that.download(
            'protocol/protocol/export',
            {
              ...that.queryParam
            },
            `protocol_${new Date().getTime()}.xlsx`
          )
        },
        onCancel() {
        }
      })
    },
    goToMagicWeb() {
      window.open('/magic/debug/index.html', '_blank')
    },
    getProtocolTypeText(type) {
      const typeMap = {
        'jar': 'Java',
        'jscript': 'JavaScript',
        'magic': 'Magic'
      }
      return typeMap[type] || type
    }
  }
}
</script>

<style scoped lang="less">
.type-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  height: 100%;
}

.type-icon {
  width: 22px;
  height: 22px;
  margin-right: 8px;
  vertical-align: middle;
  display: inline-block;
}

.desc-ellipsis {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 120px;
}

.operation-buttons .ant-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}

.operation-buttons .ant-btn .anticon {
  font-size: 12px;
}

/* 产品信息单元格样式 (与设备列表保持一致) */
.product-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-info-cell .product-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  line-height: 20px;
}

.product-info-cell .product-key {
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  color: #86909c;
  line-height: 18px;
  display: flex;
  align-items: center;
}

/* 描述省略样式 */
.desc-ellipsis {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: break-all;
}

/* 表格单元格宽度约束 */
/deep/ .ant-table-fixed {
  table-layout: fixed;
}

/deep/ .ant-table-tbody > tr > td {
  word-break: break-word;
  word-wrap: break-word;
}

/* 确保描述列不超出宽度 */
/deep/ .ant-table-tbody > tr > td:nth-child(4) {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
