<template>
  <div class="product-selector">
    <a-modal
      title="选择产品"
      :visible="visible"
      @ok="handleOk"
      @cancel="handleCancel"
      width="800px"
      :confirmLoading="loading"
    >
      <!-- 搜索区域 -->
      <div class="search-wrapper">
        <a-form layout="inline">
          <a-form-item :label="$t('product.name')">
            <a-input v-model="queryParam.productName" placeholder="请输入产品名称" allow-clear/>
          </a-form-item>
          <a-form-item label="产品Key">
            <a-input v-model="queryParam.productKey" placeholder="请输入产品Key" allow-clear/>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery">
              <a-icon type="search"/>
              {{ $t('button.query') }}
            </a-button>
            <a-button style="margin-left: 8px" @click="resetQuery">
              <a-icon type="reload"/>
              {{ $t('button.reset') }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 产品列表 -->
      <a-table
        :loading="loading"
        :columns="columns"
        :data-source="list"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange, type: 'radio' }"
        :pagination="false"
        rowKey="productKey"
        size="middle"
        :scroll="{ y: 300 }"
      >
        <span slot="status" slot-scope="text, record">
          <a-tag :color="record.status === '1' ? 'green' : 'red'">
            {{ record.status === '1' ? '启用' : '停用' }}
          </a-tag>
        </span>
        <span slot="action" slot-scope="text, record">
          <a-space>
            <a-button type="link" size="small" @click="previewProduct(record)">
              <a-icon type="eye"/>预览
            </a-button>
            <a-button type="link" size="small" @click="viewMetadata(record)">
              <a-icon type="setting"/>物模型
            </a-button>
          </a-space>
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

      <!-- 产品预览弹窗 -->
      <a-modal
        title="产品预览"
        :visible="previewVisible"
        @cancel="previewVisible = false"
        :footer="null"
        width="600px"
      >
        <div v-if="previewProduct" class="product-preview">
          <a-descriptions title="产品信息" :column="1" bordered>
            <a-descriptions-item :label="$t('product.name')">
              {{ previewProduct.productName }}
            </a-descriptions-item>
            <a-descriptions-item label="产品Key">
              {{ previewProduct.productKey }}
            </a-descriptions-item>
            <a-descriptions-item label="产品描述">
              {{ previewProduct.description || '暂无描述' }}
            </a-descriptions-item>
            <a-descriptions-item label="产品状态">
              <a-tag :color="previewProduct.status === '1' ? 'green' : 'red'">
                {{ previewProduct.status === '1' ? '启用' : '停用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item :label="$t('time.create')">
              {{ previewProduct.createTime }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-modal>

      <!-- 物模型预览弹窗 -->
      <a-modal
        title="物模型预览"
        :visible="metadataVisible"
        @cancel="metadataVisible = false"
        :footer="null"
        width="800px"
      >
        <div v-if="selectedProductMetadata" class="metadata-preview">
          <a-tabs>
            <a-tab-pane key="properties" tab="属性">
              <a-table
                :data-source="selectedProductMetadata.properties || []"
                :columns="propertyColumns"
                size="small"
                :pagination="false"
              >
                <span slot="dataType" slot-scope="text, record">
                  <a-tag>{{ record.valueType?.type || 'unknown' }}</a-tag>
                </span>
                <span slot="mode" slot-scope="text, record">
                  <a-tag :color="record.mode === 'r' ? 'blue' : 'green'">
                    {{ record.mode === 'r' ? '只读' : '读写' }}
                  </a-tag>
                </span>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="events" tab="事件">
              <a-table
                :data-source="selectedProductMetadata.events || []"
                :columns="eventColumns"
                size="small"
                :pagination="false"
              >
                <span slot="level" slot-scope="text, record">
                  <a-tag :color="getEventLevelColor(record.expands?.level)">
                    {{ getEventLevelLabel(record.expands?.level) }}
                  </a-tag>
                </span>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="functions" tab="功能">
              <a-table
                :data-source="selectedProductMetadata.functions || []"
                :columns="functionColumns"
                size="small"
                :pagination="false"
              >
                <span slot="inputs" slot-scope="text, record">
                  <a-tag v-if="record.inputs && record.inputs.length > 0" color="blue">
                    {{ record.inputs.length }} 个参数
                  </a-tag>
                  <a-tag v-else color="gray">无参数</a-tag>
                </span>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-modal>
    </a-modal>
  </div>
</template>

<script>
import {getModel, listAllProduct} from '@/api/system/dev/product'

export default {
  name: 'ProductSelector',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      selectedRowKeys: [],
      selectedRows: [],
      previewVisible: false,
      metadataVisible: false,
      previewProduct: null,
      selectedProductMetadata: null,
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        productName: undefined,
        productKey: undefined
      },
      columns: [
        {
          title: this.$t('device.productName'),
          dataIndex: 'productName',
          width: 150,
          ellipsis: true
        },
        {
          title: '产品Key',
          dataIndex: 'productKey',
          width: 200,
          ellipsis: true
        },
        {
          title: '产品描述',
          dataIndex: 'description',
          ellipsis: true
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          width: 80,
          scopedSlots: {customRender: 'status'}
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'action',
          width: 120,
          scopedSlots: {customRender: 'action'}
        }
      ],
      propertyColumns: [
        {
          title: '属性标识',
          dataIndex: 'id',
          width: 120
        },
        {
          title: '属性名称',
          dataIndex: 'name',
          width: 120
        },
        {
          title: '数据类型',
          dataIndex: 'dataType',
          width: 100,
          scopedSlots: {customRender: 'dataType'}
        },
        {
          title: '读写权限',
          dataIndex: 'mode',
          width: 100,
          scopedSlots: {customRender: 'mode'}
        },
        {
          title: this.$t('rule.description'),
          dataIndex: 'description',
          ellipsis: true
        }
      ],
      eventColumns: [
        {
          title: '事件标识',
          dataIndex: 'id',
          width: 120
        },
        {
          title: '事件名称',
          dataIndex: 'name',
          width: 120
        },
        {
          title: '事件级别',
          dataIndex: 'level',
          width: 100,
          scopedSlots: {customRender: 'level'}
        },
        {
          title: this.$t('rule.description'),
          dataIndex: 'description',
          ellipsis: true
        }
      ],
      functionColumns: [
        {
          title: '功能标识',
          dataIndex: 'id',
          width: 120
        },
        {
          title: '功能名称',
          dataIndex: 'name',
          width: 120
        },
        {
          title: '输入参数',
          dataIndex: 'inputs',
          width: 100,
          scopedSlots: {customRender: 'inputs'}
        },
        {
          title: this.$t('rule.description'),
          dataIndex: 'description',
          ellipsis: true
        }
      ]
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.getList()
      }
    }
  },
  methods: {
    // 获取产品列表
    getList() {
      this.loading = true
      listAllProduct().then(response => {
        this.list = response.data || []
        this.total = this.list.length
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    // 查询
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },

    // 重置查询
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        productName: undefined,
        productKey: undefined
      }
      this.handleQuery()
    },

    // 选择变化
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },

    // 分页变化
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },

    // 确认选择
    handleOk() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择一个产品')
        return
      }

      const selectedProduct = this.selectedRows[0]
      this.$emit('product-selected', selectedProduct)
      this.handleCancel()
    },

    // 取消
    handleCancel() {
      this.$emit('update:visible', false)
      this.reset()
    },

    // 重置
    reset() {
      this.selectedRowKeys = []
      this.selectedRows = []
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        productName: undefined,
        productKey: undefined
      }
    },

    // 预览产品
    previewProduct(product) {
      this.previewProduct = product
      this.previewVisible = true
    },

    // 查看物模型
    async viewMetadata(product) {
      try {
        const response = await getModel(product.productKey)
        this.selectedProductMetadata = response.data
        this.metadataVisible = true
      } catch (error) {
        this.$message.error('加载物模型失败')
        console.error('加载物模型失败:', error)
      }
    },

    // 获取事件级别标签
    getEventLevelLabel(level) {
      const levelMap = {
        info: '信息',
        warn: '警告',
        error: '错误',
        urgent: '紧急'
      }
      return levelMap[level] || level || '普通'
    },

    // 获取事件级别颜色
    getEventLevelColor(level) {
      const colorMap = {
        info: 'blue',
        warn: 'orange',
        error: 'red',
        urgent: 'red'
      }
      return colorMap[level] || 'default'
    }
  }
}
</script>

<style lang="less" scoped>
.product-selector {
  .search-wrapper {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }

  .product-preview {
    .ant-descriptions-item-label {
      font-weight: bold;
    }
  }

  .metadata-preview {
    .ant-table {
      .ant-table-thead > tr > th {
        background: #fafafa;
        font-weight: bold;
      }
    }
  }
}
</style> 