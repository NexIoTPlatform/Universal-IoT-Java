<template>
  <div>
    <a-modal :visible="modelVisible" title="公共产品选择" width="80%" @ok="handleOk"
             @cancel="closeModel">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="设备类型">
                <a-input v-model="queryParam.searchValue" placeholder="请输入设备类型" allow-clear/>
              </a-form-item>
            </a-col>
            <span
              class="table-page-search-submitButtons"
              :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="getList"><iot-icon
                type="icon-search"/>查询</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>重置</a-button>
            </span>
          </a-row>
        </a-form>
      </div>
      <a-table
        :loading="loading"
        :size="tableSize"
        :columns="columns"
        :data-source="list"
        :row-key="record => record.publicProductID"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange,type:'radio'}"
        :pagination="false"
      >
      </a-table>
      <!-- 分页 -->
      <a-pagination
        style="float:right;margin-top:16px"
        show-size-changer
        show-quick-jumper
        :current="queryParam.pageNum"
        :total="total"
        :page-size="queryParam.pageSize"
        :showTotal="total => `共 ${total} 条`"
        @showSizeChange="onShowSizeChange"
        @change="changeSize"
      />
      <div style="clear:both"></div>
    </a-modal>

  </div>
</template>

<script>
import {getPublicProduct} from '@/api/system/dev/product'

export default {
  name: 'PublicProduct',
  props: {},

  data() {
    return {
      list: [],
      selectedRowKeys: [],
      // 弹出的绑定页面
      selectedRows: [],
      // 弹出的绑定页面
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      id: undefined,
      ids: [],
      loading: false,
      total: 0,
      open: false,
      devGroup: [],
      // 0-离线，1-在线字典
      stateOptions: [],
      // 设备选择弹窗可视
      modelVisible: false,
      // 分组可视
      groupVisible: false,
      // 查询参数
      queryParam: {
        searchValue: undefined,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '公共产品ID',
          dataIndex: 'publicProductID',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '设备类型',
          dataIndex: 'deviceType',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '设备型号',
          dataIndex: 'deviceModel',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '厂商简称',
          dataIndex: 'vendorName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '通讯协议',
          dataIndex: 'productProtocol',
          ellipsis: true,
          align: 'center'
        }
      ]
    }
  },
  watch: {},
  created() {
    // this.getList()
  },
  methods: {
    /** 查询产品列表 */
    getList() {
      this.loading = true
      getPublicProduct(this.queryParam).then(response => {
        const res = JSON.parse(response.data.msg)
        this.list = res.list
        this.total = res.total
        this.loading = false
      })
    },
    openModals() {
      this.getList()
      this.modelVisible = true
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        searchValue: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
    },
    handleOk() {
      if (this.ids.length === 0) {
        this.$message.error('请先选择产品')
        return
      }
      const str = this.ids[0].split('^&')
      console.log(str)
      this.$emit('getProductInfo', {
        'publicProductID': str[0],
        'deviceType': str[1],
        'deviceModel': str[2],
        'vendorName': str[3]
      })
      this.modelVisible = false
    },
    closeModel() {
      this.modelVisible = false
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
      this.ids = this.selectedRows.map(
        item => item.publicProductID + '^&' + item.deviceType + '^&' + item.deviceModel + '^&'
          + item.vendorName)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    }
  }
}
</script>
