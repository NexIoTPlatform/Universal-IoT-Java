<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="证书名称">
                <a-input v-model="queryParam.name" placeholder="请输入证书名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="备注">
                <a-input v-model="queryParam.remark" placeholder="请输入备注"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span
                class="table-page-search-submitButtons"
                :style="advanced && { float: 'right', overflow: 'hidden' } || {}">
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
        <a-button type="primary" @click="openForm()" v-hasPermi="['network:cert:add']">
          <a-icon type="plus"/>
          新增
        </a-button>
        <a-button type="danger" :disabled="multiple" @click="handleBatchDelete"
                  v-hasPermi="['network:cert:delete']">
          <a-icon type="delete"/>
          删除
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }"
                  @click="getList">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>
      <certificate-form ref="createForm" :visible.sync="formVisible" :record="currentRecord"
                        @refresh="getList" v-hasPermi="[formPermi]"/>
      <certificate-detail v-if="detailVisible" :visible.sync="detailVisible"
                          :record="formatDetailRecord(currentRecord)"
                          v-hasPermi="['network:cert:query']"/>
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="id"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :pagination="false">
        <span slot="expireTime" slot-scope="text, record">
          <div class="expire-time-main">{{ parseTime(record.expireTime) }}</div>
        </span>
        <span slot="status" slot-scope="text, record">
          <a-tag :color="getStatusColor(record.expireTime)">
            {{ getExpireStatusText(record.expireTime) }}
          </a-tag>
        </span>
        <span slot="name" slot-scope="text, record">
          <div class="certificate-info-cell">
            <div class="certificate-info-content">
              <div class="certificate-name">
                <a @click="openDetail(record)" class="certificate-name-link">{{ record.name }}</a>
                <span v-if="record.sslKey === 'default-tcp'"
                      style="color:#faad14; margin-left: 4px;">（默认）</span>
              </div>
              <div class="certificate-id">{{ record.sslKey }}</div>
            </div>
          </div>
        </span>

        <span slot="remark" slot-scope="text, record">
          <div class="certificate-remark">{{ record.remark || '-' }}</div>
        </span>
        <span slot="operation" slot-scope="text, record">
          <div class="operation-buttons">
            <a-button type="link" icon="edit" @click="openForm(record)"
                      v-hasPermi="['network:cert:edit']" class="operation-btn">编辑</a-button>
            <a-button
              v-if="hasPermission('network:cert:delete')"
              type="link"
              icon="delete"
              :disabled="record.sslKey === 'default-tcp'"
              @click="record.sslKey !== 'default-tcp' && handleDelete(record)"
              class="operation-btn"
            >
              <span v-if="record.sslKey === 'default-tcp'">
                删除
                <a-tooltip placement="top">
                  <template slot="title">默认证书不可删除</template>
                  <a-icon type="info-circle" style="margin-left: 4px; color: #aaa;"/>
                </a-tooltip>
              </span>
              <span v-else>删除</span>
            </a-button>
          </div>
        </span>
      </a-table>
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
  </page-header-wrapper>
</template>

<script>
import {deleteCertificate, getCertificateList} from '@/api/system/certificate'
import CertificateForm from './CertificateForm.vue'
import CertificateDetail from './CertificateDetail.vue'

export default {
  name: 'CertificateManager',
  components: {
    CertificateForm,
    CertificateDetail
  },
  data() {
    return {
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      multiple: true,
      loading: false,
      total: 0,
      tableSize: 'default',
      formVisible: false,
      detailVisible: false,
      currentRecord: null,
      queryParam: {
        name: '',
        sslKey: '',
        remark: '',
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '证书名称',
          dataIndex: 'name',
          align: 'left',
          width: '25%',
          scopedSlots: {customRender: 'name'}
        },
        {
          title: '到期时间',
          dataIndex: 'expireTime',
          scopedSlots: {customRender: 'expireTime'},
          align: 'center',
          width: '20%'
        },
        {
          title: '状态',
          key: 'status',
          align: 'left',
          width: '12%',
          scopedSlots: {customRender: 'status'}
        },
        {
          title: '备注',
          dataIndex: 'remark',
          align: 'left',
          width: '28%',
          scopedSlots: {customRender: 'remark'}
        },
        {
          title: '操作',
          key: 'operation',
          align: 'center',
          width: '15%',
          scopedSlots: {customRender: 'operation'}
        }
      ]
    }
  },
  computed: {
    formPermi() {
      return this.currentRecord && this.currentRecord.sslKey ? 'network:cert:edit'
        : 'network:cert:add'
    },
    // 权限检查方法
    hasPermission() {
      return (permission) => {
        const allPermission = '*:*:*'
        const permissions = this.$store.getters && this.$store.getters.permissions

        if (!permissions || !Array.isArray(permissions)) {
          return false
        }

        // 检查是否有超级管理员权限
        if (permissions.includes(allPermission)) {
          return true
        }

        // 检查具体权限
        return permissions.includes(permission)
      }
    }
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.queryParam.pageNum,
          pageSize: this.queryParam.pageSize,
          name: this.queryParam.name,
          sslKey: this.queryParam.sslKey,
          remark: this.queryParam.remark
        }
        const res = await getCertificateList(params)
        if (res.code === 0 && Array.isArray(res.rows)) {
          this.list = res.rows
          this.total = res.total || 0
        } else {
          this.list = []
          this.total = 0
        }
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParam = {name: '', sslKey: '', remark: '', pageNum: 1, pageSize: 10}
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
      this.multiple = !selectedRowKeys.length
    },
    openForm(record) {
      if (record) {
        this.currentRecord = {...record}
      } else {
        this.currentRecord = {
          name: '',
          sslKey: '',
          remark: '',
          expireTime: '',
          certContent: '',
          keyContent: '',
          certPassword: '',
          keyPassword: ''
        }
      }
      this.formVisible = true
    },
    openDetail(record) {
      this.currentRecord = record
      this.detailVisible = true
    },
    async handleDelete(record) {
      this.$confirm({
        title: '确认删除该证书？',
        content: `sslKey: ${record.sslKey}`,
        onOk: async () => {
          const res = await deleteCertificate(record.id)
          if (res.code === 0) {
            this.$message.success('删除成功')
            this.getList()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        }
      })
    },
    async handleBatchDelete() {
      this.$confirm({
        title: '确认批量删除？',
        content: `共选中 ${this.selectedRowKeys.length} 个证书`,
        onOk: async () => {
          // 逐个删除，传主键id
          for (const id of this.selectedRowKeys) {
            await deleteCertificate(id)
          }
          this.$message.success('批量删除成功')
          this.getList()
        }
      })
    },
    expireStyle(dateStr) {
      if (!dateStr) {
        return {}
      }
      const d = new Date(dateStr)
      const now = new Date()
      const diff = (d - now) / (1000 * 60 * 60 * 24)
      if (diff < 0) {
        return {color: '#ff4d4f'}
      }
      if (diff < 7) {
        return {color: '#faad14'}
      }
      return {}
    },
    formatDetailRecord(record) {
      if (!record) {
        return record
      }
      return {
        ...record,
        expireTime: this.parseTime(record.expireTime),
        createTime: this.parseTime(record.createTime),
        updateTime: this.parseTime(record.updateTime)
      }
    },
    parseTime(val) {
      if (!val) {
        return ''
      }
      const d = new Date(val)
      if (isNaN(d.getTime())) {
        return val
      }
      const pad = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
        d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    getExpireStatusClass(dateStr) {
      if (!dateStr) {
        return ''
      }
      const d = new Date(dateStr)
      const now = new Date()
      const diff = (d - now) / (1000 * 60 * 60 * 24)
      if (diff < 0) {
        return 'expired'
      }
      if (diff < 7) {
        return 'warning'
      }
      return 'normal'
    },
    getExpireStatusText(dateStr) {
      if (!dateStr) {
        return ''
      }
      const d = new Date(dateStr)
      const now = new Date()
      const diff = (d - now) / (1000 * 60 * 60 * 24)
      if (diff < 0) {
        return '已过期'
      }
      if (diff < 7) {
        return '即将过期'
      }
      return '正常'
    },
    getStatusColor(dateStr) {
      if (!dateStr) {
        return 'default'
      }
      const d = new Date(dateStr)
      const now = new Date()
      const diff = (d - now) / (1000 * 60 * 60 * 24)
      if (diff < 0) {
        return 'red'
      }
      if (diff < 7) {
        return 'orange'
      }
      return 'green'
    }
  },
  mounted() {
    this.getList()
  }
}
</script>

<style scoped lang="less">
/* 证书信息单元格样式 */
.certificate-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.certificate-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.certificate-name {
  display: flex;
  align-items: center;
  gap: 4px;
}

.certificate-name-link {
  font-size: 14px;
  font-weight: 500;
  color: #1890ff;
  text-decoration: none;
}

.certificate-name-link:hover {
  color: #40a9ff;
}

.certificate-id {
  font-size: 12px;
  color: #666;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.certificate-remark {
  font-size: 13px;
  color: #666;
}

/* 过期时间单元格样式 */
.expire-time-main {
  font-size: 13px;
  color: #262626;
  font-weight: 500;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 100px;
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

.operation-btn .anticon {
  font-size: 12px;
}
</style>
