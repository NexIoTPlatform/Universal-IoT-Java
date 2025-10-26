<template>
  <page-header-wrapper>
    <!-- 对接流程-->
    <!-- <use-flow /> -->
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="所属产品" prop="id">
                <a-input v-model.trim="queryParam.id" placeholder="请输入ProductKey" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="协议名称" prop="name">
                <a-input v-model.trim="queryParam.name" placeholder="请输入协议名称" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="协议类型" prop="type">
                <a-select placeholder="请选择协议类型" style="width: 100%" v-model="queryParam.type"
                          allow-clear>
                  <a-select-option v-for="(d, index) in protocolTypeOptions" :key="index"
                                   :value="d.dictValue">
                    <span class="type-cell">
                      <template v-if="d.dictValue === 'jar'">
                        <svg class="type-icon" viewBox="0 0 32 32"><g><path fill="#5382a1"
                                                                            d="M23.47 27.19s.59.48-.65.85c-2.36.72-9.77.94-11.84.03-.74-.32.66-.76 1.1-.85.46-.1.72-.08.72-.08-.83-.58-5.37 1.15-2.3 1.65 8.36 1.36 15.23-.61 13.98-1.6z"/><path
                          fill="#e76f00"
                          d="M12.87 19.7s-3.81.91-1.35 1.24c1.04.14 3.12.11 5.06-.06 1.59-.14 3.18-.44 3.18-.44s-.56.24-.97.52c-3.91 1.03-11.47.55-9.29-.5 1.84-.87 3.37-.76 3.37-.76z"/><path
                          fill="#5382a1"
                          d="M21.06 22.13c3.97-2.06 2.14-4.04.86-3.77-.32.07-.46.13-.46.13s.12-.19.36-.27c2.66-.94 4.71 2.76-.86 4.01z"/><path
                          fill="#e76f00"
                          d="M13.32 24.13c-2.1-.47-4.33-.36-4.33-.36s.44.36 1.32.65c3.98 1.2 12.09.32 9.78-.62-1.93-.77-3.77-.67-6.77-.33z"/><path
                          fill="#5382a1"
                          d="M25.5 29.5c-3.98.76-9.02.67-11.97.18-.6-.1-.73-.16-.73-.16s.09.07.26.15c2.01.88 8.36 1.17 13.23.34.93-.15.29-.51-.79-.51z"/><path
                          fill="#e76f00"
                          d="M19.5 15.5c1.5-1.5 2.5-3.5 1.5-5.5-1-2-3.5-2.5-5-1.5-1.5 1-2 3.5-1 5.5 1 2 3.5 2.5 5 1.5z"/></g></svg>
                        <span>Java</span>
                      </template>
                      <template v-else-if="d.dictValue === 'jscript'">
                        <svg class="type-icon" viewBox="0 0 32 32">
                          <rect width="32" height="32" rx="6" fill="#fff"/>
                          <text
                            x="7"
                            y="24"
                            font-size="20"
                            font-family="Arial"
                            font-weight="bold"
                            fill="#f7df1e"
                            stroke="#222"
                            stroke-width="1">JS</text>
                        </svg>
                        <span>JavaScript</span>
                      </template>
                      <template v-else-if="d.dictValue === 'magic'">
                        <svg class="type-icon" viewBox="0 0 32 32"><circle cx="16" cy="16" r="14"
                                                                           fill="#722ed1"/><path
                          d="M16 8v8l6 3" stroke="#fff" stroke-width="2" stroke-linecap="round"
                          fill="none"/><circle cx="16" cy="16" r="2" fill="#fff"/></svg>
                        <span>Magic</span>
                      </template>
                      <template v-else>
                        <span>{{ d.dictLabel }}</span>
                      </template>
                    </span>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="协议状态" prop="state">
                <a-select placeholder="请选择协议状态" style="width: 100%"
                          v-model="queryParam.state" allow-clear>
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
                  <a-icon type="search"/>查询
                </a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">
                  <a-icon type="reload"/>重置
                </a-button>
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
          新增
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleDelete"
                  v-hasPermi="['protocol:protocol:remove']">
          <a-icon type="delete"/>
          删除
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
      >
        <template slot="内置编码">
          <a-tooltip placement="top" title="集成在系统内部，无法变更">
            <span style="cursor: help;">内置编码</span>
          </a-tooltip>
        </template>
        <span slot="protocolType" slot-scope="text, record">
          <div class="type-cell">
            <template v-if="record.type === 'jar'">
              <!-- Java 官方 Logo SVG -->
              <svg class="type-icon" viewBox="0 0 32 32"><g><path fill="#5382a1"
                                                                  d="M23.47 27.19s.59.48-.65.85c-2.36.72-9.77.94-11.84.03-.74-.32.66-.76 1.1-.85.46-.1.72-.08.72-.08-.83-.58-5.37 1.15-2.3 1.65 8.36 1.36 15.23-.61 13.98-1.6z"/><path
                fill="#e76f00"
                d="M12.87 19.7s-3.81.91-1.35 1.24c1.04.14 3.12.11 5.06-.06 1.59-.14 3.18-.44 3.18-.44s-.56.24-.97.52c-3.91 1.03-11.47.55-9.29-.5 1.84-.87 3.37-.76 3.37-.76z"/><path
                fill="#5382a1"
                d="M21.06 22.13c3.97-2.06 2.14-4.04.86-3.77-.32.07-.46.13-.46.13s.12-.19.36-.27c2.66-.94 4.71 2.76-.86 4.01z"/><path
                fill="#e76f00"
                d="M13.32 24.13c-2.1-.47-4.33-.36-4.33-.36s.44.36 1.32.65c3.98 1.2 12.09.32 9.78-.62-1.93-.77-3.77-.67-6.77-.33z"/><path
                fill="#5382a1"
                d="M25.5 29.5c-3.98.76-9.02.67-11.97.18-.6-.1-.73-.16-.73-.16s.09.07.26.15c2.01.88 8.36 1.17 13.23.34.93-.15.29-.51-.79-.51z"/><path
                fill="#e76f00"
                d="M19.5 15.5c1.5-1.5 2.5-3.5 1.5-5.5-1-2-3.5-2.5-5-1.5-1.5 1-2 3.5-1 5.5 1 2 3.5 2.5 5 1.5z"/></g></svg>
              <span>Java</span>
            </template>
            <template v-else-if="record.type === 'jscript'">
              <!-- JavaScript 官方 Logo SVG（透明底黄字黑描边） -->
              <svg class="type-icon" viewBox="0 0 32 32">
                <rect width="32" height="32" rx="6" fill="#fff"/>
                <text
                  x="7"
                  y="24"
                  font-size="20"
                  font-family="Arial"
                  font-weight="bold"
                  fill="#f7df1e"
                  stroke="#222"
                  stroke-width="1">JS</text>
              </svg>
              <span>JavaScript</span>
            </template>
            <template v-else-if="record.type === 'magic'">
              <!-- Magic Universal 新风格 SVG -->
              <svg class="type-icon" viewBox="0 0 32 32"><circle cx="16" cy="16" r="14"
                                                                 fill="#722ed1"/><path
                d="M16 8v8l6 3" stroke="#fff" stroke-width="2" stroke-linecap="round" fill="none"/><circle
                cx="16" cy="16" r="2" fill="#fff"/></svg>
              <span>Magic</span>
            </template>
            <template v-else>
              <span>{{ record.type }}</span>
            </template>
          </div>
        </span>
        <span slot="type" slot-scope="text, record">
          <template v-if="checkLocal(record)">
            <a-tooltip placement="top" title="集成在系统内部，无法变更">
              <span style="cursor: help; color: #722ed1;">是</span>
            </a-tooltip>
          </template>
          <template v-else>
            <span>否</span>
          </template>
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
        <span slot="description" slot-scope="text">
          <a-tooltip :title="text">
            <span class="desc-ellipsis">{{ text }}</span>
          </a-tooltip>
        </span>
        <span slot="operation" slot-scope="text, record">
          <div class="operation-buttons">
            <a-button type="link" icon="bug" @click="goToMagicWeb"
                      v-hasPermi="['protocol:protocol:codec']">
              调试
            </a-button>
            <a-button type="link" icon="edit"
                      @click="$refs.createForm.handleUpdate(record, undefined)"
                      v-hasPermi="['protocol:protocol:edit']">
              修改
            </a-button>
            <a-button type="link" icon="delete" danger @click="handleDelete(record)"
                      v-hasPermi="['protocol:protocol:remove']">
              删除
            </a-button>
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
          title: 'ProductKey',
          dataIndex: 'id',
          width: '20%',
          ellipsis: true,
          align: 'left'
        },
        {
          title: '协议名',
          dataIndex: 'name',
          width: '20%',
          ellipsis: true,
          align: 'left'
        },
        {
          title: '类型',
          dataIndex: 'type',
          width: '10%',
          scopedSlots: { customRender: 'protocolType' },
          align: 'center'
        },
        {
          title: '内置编码',
          dataIndex: 'innerType',
          width: '10%',
          scopedSlots: { customRender: 'type' },
          align: 'center'
        },
        {
          title: '描述',
          dataIndex: 'description',
          width: '10%',
          scopedSlots: { customRender: 'description' },
          align: 'left'
        },
        {
          title: '状态',
          dataIndex: 'state',
          width: '10%',
          scopedSlots: { customRender: 'protocolState' },
          align: 'center'
        },
        {
          title: '操作',
          dataIndex: 'operation',
          width: '20%',
          scopedSlots: { customRender: 'operation' },
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


</style>
