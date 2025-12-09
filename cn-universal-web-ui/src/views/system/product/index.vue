<template>
  <page-header-wrapper>
    <!-- 对接流程-->
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('product.name')" prop="name">
                <a-select
                  v-model="queryParam.name"
                  placeholder="请选择或搜索产品名称"
                  show-search
                  :filter-option="false"
                  :loading="productSearchLoading"
                  @search="handleProductNameSearch"
                  @focus="handleProductNameFocus"
                  allow-clear
                  style="width: 100%"
                >
                  <a-select-option
                    v-for="product in productSearchList"
                    :key="product.id"
                    :value="product.name"
                    :title="product.name"
                  >
                    <div class="product-search-option">
                      <div class="product-search-name">{{ product.name }}</div>
                      <div class="product-search-key">{{ product.productKey }}</div>
                    </div>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('device.manufacturer')" prop="companyNo">
                <a-input v-model="queryParam.companyNo" placeholder="请输入厂商名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="ProductKey" prop="productKey">
                <a-input v-model="queryParam.productKey" placeholder="请输入ProductKey"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>

            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('device.dataProtocol')">
                  <a-select placeholder="请选择" v-model="queryParam.transportProtocol"
                            style="width: 100%" allow-clear>
                    <a-select-option v-for="d in transportProtocols" :key="d.name" :value="d.name">
                      {{
                        d.id
                      }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('device.accessMethod')">
                  <a-select placeholder="请选择" v-model="queryParam.thirdPlatform"
                            style="width: 100%" allow-clear>
                    <a-select-option v-for="d in thirdPlatforms" :key="d.name" :value="d.name">{{
                        d.id
                      }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('device.node')">
                  <a-select placeholder="请选择" v-model="queryParam.deviceNode" style="width: 100%"
                            allow-clear>
                    <a-select-option v-for="d in deviceNodes" :key="d.name" :value="d.name">{{
                        d.id
                      }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item :label="$t('device.tags')">
                  <a-select
                    mode="multiple"
                    placeholder="请选择"
                    v-model="tagList"
                    style="width: 100%"
                    allow-clear
                    @change="handleTagChange"
                  >
                    <a-select-option v-for="item in productTags" :key="item.id" :value="item.id">{{
                        item.name
                      }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span
                class="table-page-search-submitButtons"
                :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="handleQuery"><iot-icon
                  type="icon-search"/>{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? $t('button.collapse') : $t('button.expand') }}
                  <a-icon :type="advanced ? 'up' : 'down'"/>
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 操作 -->
      <div class="table-operations">
        <a-button type="primary" size="default" @click="$refs.createForm.handleAdd()"
                  v-hasPermi="['system:product:add']">
          <iot-icon type="icon-u-add"/>{{ $t('button.add') }}
        </a-button>
        <a-button type="primary" size="default" @click="$refs.ImportProductForm.handleImport()"
                  v-hasPermi="['system:product:import']">
          <a-icon type="import"/>{{ $t('button.import') }}
        </a-button>
        <a-button type="primary" size="default" @click="handleExport"
                  v-hasPermi="['system:product:export']">
          <iot-icon type="icon-u-export"/>{{ $t('button.export') }}
        </a-button>
        <a-button
          type="primary"
          size="small"
          :loading="loading"
          :style="{float: 'right'}"
          @click="getList"
        >
          <a-icon type="sync" :spin="loading"/>{{ $t('button.refresh') }}
        </a-button>
        <!-- <div style="float: right" class="screen-btns">
          <span @click="toggleScreen('all')" class="screen" :class="{'selected': (!queryParam.hasDevice && !queryParam.self)}">全部</span>
          <span @click="toggleScreen('self')" class="screen" :class="{ 'selected': queryParam.self }">我的</span>
          <span @click="toggleScreen('hasDevice')" class="screen" :class="{ 'selected': queryParam.hasDevice }">有设备</span>
        </div> -->
      </div>
      <!-- 增加修改 -->
      <create-form ref="createForm" :stateOptions="stateOptions" @ok="getList"/>
      <!-- 数据展示 -->
      <div class="loading-wrap" v-show="loading">
        <a-spin/>
      </div>
      <Empty v-show="!loading && !list.length"></Empty>
      <div class="cardWrap" v-show="!loading && list.length">
        <div class="product-card" v-for="(value, index) in list" :key="index"
             v-hasPermi="['system:product:query']">
          <div class="card-header">
            <div class="header-left">
              <div class="product-title">
                <div class="company-badge">{{ value.companyNo }}</div>
                <div class="product-name" :title="value.name">{{ value.name }}</div>
              </div>
            </div>
          </div>
          <!-- 标签区域 - 移到右上角 -->
          <div class="tag-corner" v-if="value.tags && value.tags.length">
            <div class="tag-list">
              <span class="tag-item" v-for="(tag, tagIndex) in value.tags" :key="tagIndex">
                {{ tag }}
              </span>
            </div>
          </div>
          <!-- 操作按钮区域 - 卡片中央 -->
          <div class="card-actions"
               v-if="value.creatorId === username || value.creatorId === parentUnionId">
            <a-button
              type="text"
              size="small"
              class="action-btn edit-btn"
              @click="$refs.createForm.handleUpdate(value, undefined)"
              v-hasPermi="['system:product:edit']"
              title="编辑"
            >
              <iot-icon type="icon-u-edit"/>
            </a-button>
            <a-button
              type="text"
              size="small"
              class="action-btn export-btn"
              @click="handleSingleExport(value)"
              v-hasPermi="['system:product:export']"
              :title="$t('button.export')"
            >
              <iot-icon type="icon-u-export"/>
            </a-button>
            <a-button
              type="text"
              size="small"
              class="action-btn delete-btn"
              @click="handleDelete(value)"
              v-hasPermi="['system:product:remove']"
              title="删除"
            >
              <iot-icon type="icon-u-del"/>
            </a-button>
          </div>
          <div class="card-down">
            <div class="info-section" @click="checkDetails(value)">
              <div class="info-row">
                <span class="label">设备数量</span>
                <span class="content" style="color: #1966ff">{{
                    !!devNumMap[value.productKey] ? devNumMap[value.productKey] : 0
                  }}</span>
              </div>
              <div class="info-row">
                <span class="label">设备节点</span>
                <span class="content">{{ getDeviceNode(value.deviceNode) }}</span>
              </div>
              <div class="info-row">
                <span class="label">数据协议</span>
                <span class="content" style="white-space: normal; word-break: break-word;">{{
                    value.transportProtocol
                  }}</span>
              </div>
              <div class="info-row">
                <span class="label">接入方式</span>
                <span class="content"
                      style="color: #005aff;font-weight: 600;white-space: nowrap;line-height: 20px;">{{
                    value.thirdPlatform
                  }}</span>
              </div>
            </div>
            <div class="image-section">
              <div class="pic" v-if="!!value.photoUrl.img" @click="queryDoc(value.photoUrl)">
                <img :src="value.photoUrl.img" alt="产品图片"/>
              </div>
              <div class="pic-placeholder" v-else @click="handleDefaultClick(value)">
                <SvgDefaultProduct/>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 分页 -->
      <a-pagination
        class="ant-table-pagination"
        show-size-changer
        show-quick-jumper
        :current="queryParam.pageNum"
        :total="total"
        :page-size="queryParam.pageSize"
        :pageSizeOptions="['12', '24', '36']"
        :showTotal="(total) => `共 ${total} 条`"
        @showSizeChange="onShowSizeChange"
        @change="changeSize"
      />
    </a-card>
    <import-product-form ref="ImportProductForm"/>
    <Resizabler ref="resizabler"/>
  </page-header-wrapper>
</template>

<script>
import {delProduct, getDevNumByProduct, queryProductList, updateProductState} from '@/api/system/dev/product'
import request from '@/utils/request'
import CreateForm from './modules/CreateForm'
import ImportProductForm from '@/views/system/product/modules/ImportForm'
import {Empty} from 'ant-design-vue'
import Resizabler from './modules/Resizabler.vue'
import SvgDefaultProduct from './modules/SvgDefaultProduct.vue'

export default {
  name: 'ProductList',
  components: {
    ImportProductForm,
    CreateForm,
    Empty,
    SvgDefaultProduct,
    Resizabler
  },
  data() {
    return {
      list: [],
      selectedRowKeys: [],
      selectedRows: [],
      selectedProduct: null,
      username: this.$store.state.user.name,
      parentUnionId: this.$store.state.user.parentUnionId,
      devNumMap: {},
      // 高级搜索 展开/关闭
      advanced: false,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 表格列定义
      columns: [
        {
          title: this.$t('device.productName'),
          dataIndex: 'name',
          key: 'name',
          scopedSlots: {customRender: 'name'},
          width: 150
        },
        {
          title: '产品类型',
          dataIndex: 'deviceNode',
          key: 'deviceNode',
          scopedSlots: {customRender: 'deviceNode'},
          width: 120
        },
        {
          title: '节点类型',
          dataIndex: 'messageProtocol',
          key: 'messageProtocol',
          scopedSlots: {customRender: 'messageProtocol'},
          width: 100
        },
        {
          title: '接入协议',
          dataIndex: 'transportProtocol',
          key: 'transportProtocol',
          scopedSlots: {customRender: 'transportProtocol'},
          width: 100
        },
        {
          title: '联网方式',
          dataIndex: 'networkWay',
          key: 'networkWay',
          scopedSlots: {customRender: 'networkWay'},
          width: 100
        },
        {
          title: '开发状态',
          dataIndex: 'state',
          key: 'state',
          scopedSlots: {customRender: 'state'},
          width: 100
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          key: 'createTime',
          scopedSlots: {customRender: 'createTime'},
          width: 160
        },
        {
          title: this.$t('user.operation'),
          key: 'action',
          scopedSlots: {customRender: 'action'},
          width: 200,
          fixed: 'right'
        }
      ],
      ids: [],
      loading: false,
      total: 0,
      // 0-发布，1-审核，2-停用
      stateOptions: [],
      // 节点类型
      nodeOptions: [],
      productTags: [],
      // 产品名称搜索相关
      productSearchList: [],
      productSearchLoading: false,
      productSearchTimer: null,
      // 查询参数
      queryParam: {
        productId: null,
        productKey: null,
        thirdPlatform: undefined,
        companyNo: null,
        networkWay: null,
        deviceNode: undefined,
        messageProtocol: null,
        name: null,
        creatorId: null,
        storePolicy: null,
        transportProtocol: undefined,
        protocolName: null,
        state: 0, // 默认
        hasDevice: false,
        self: false,
        pageNum: 1,
        pageSize: 12,
        tags: undefined
      },
      // 设备标签
      tagList: []
    }
  },
  filters: {},
  async created() {
    this.deviceNodes = []
    this.thirdPlatforms = []
    this.transportProtocols = []
    this.productTags = []
    await this.getDictArray()
    await this.getDevNum()
    this.loadProductNameSearchList() // 初始化产品搜索列表
    this.getList()
  },
  computed: {},
  watch: {},
  beforeDestroy() {
    // 清理定时器
    if (this.productSearchTimer) {
      clearTimeout(this.productSearchTimer)
    }
  },
  methods: {
    // 产品详情图展示
    queryDoc(photoUrl) {
      if (photoUrl && photoUrl.detail) {
        this.$refs.resizabler.open(photoUrl.detail)
      }
    },
    color16(index) { // 十六进制颜色随机
      var colorList = ['#db8b4b', '#0066a8', '#539958']
      if (index >= colorList.length) {
        return colorList[colorList.length - 1]
      } else {
        var randomItem = colorList[index]
        return randomItem
      }
    },
    // 设备标签选择
    handleTagChange(value) {
      this.queryParam.tags = value.join()
    },
    getDeviceNode(e) {
      const actions = []
      Object.keys(this.nodeOptions).some((key) => {
        if (this.nodeOptions[key].dictValue === '' + e) {
          actions.push(this.nodeOptions[key].dictLabel)
          return true
        }
      })
      return actions.join('')
    },
    getThirdPlatform(e) {
      const actions = []
      Object.keys(this.thirdPlatforms).some((key) => {
        if (this.thirdPlatforms[key].name === '' + e) {
          actions.push(this.thirdPlatforms[key].id)
          return true
        }
      })
      return actions.join('')
    },
    getProductTags(e) {
      const actions = []
      Object.keys(this.productTags).some((key) => {
        const tag = e ? e.split(',') : []
        tag.forEach((item) => {
          if (this.productTags[key].id === '' + item) {
            actions.push(this.productTags[key].name)
            return true
          }
        })
      })
      return actions
    },
    getPhotoPics(e) {
      if (e) {
        return JSON.parse(e)
      }
      return {}
    },
    /** 查询设备产品列表 */
    getList() {
      this.loading = true
      if (this.queryParam.productKey !== undefined && this.queryParam.productKey !== null) {
        this.queryParam.productKey = this.queryParam.productKey.replace(/^\s*|\s*$/g, '')
      }
      if (this.queryParam.classifiedName !== undefined && this.queryParam.classifiedName !== null) {
        this.queryParam.classifiedName = this.queryParam.classifiedName.replace(/^\s*|\s*$/g, '')
      }
      if (this.queryParam.companyNo !== undefined && this.queryParam.companyNo !== null) {
        this.queryParam.companyNo = this.queryParam.companyNo.replace(/^\s*|\s*$/g, '')
      }
      queryProductList(this.queryParam).then((response) => {
        response.rows.forEach((cur, index) => {
          const thirdPlatformName = this.getThirdPlatform(cur.thirdPlatform)
          cur.thirdPlatform = thirdPlatformName || cur.thirdPlatform
          cur.createTime = this.parseTime(cur.createTime)
          const productTag = this.getProductTags(cur.tags)
          cur.tags = productTag && productTag.length > 0 ? productTag : [] // 临时添加默认标签用于测试
          cur.photoUrl = this.getPhotoPics(cur.photoUrl)
        })
        this.list = response.rows
        // this.list.forEach((element) => this.calculateTagWidth(element.productId))
        this.total = response.total
        this.loading = false
      })
    },
    // 查询各产品设备数量
    async getDevNum() {
      await getDevNumByProduct().then((res) => {
        this.devNumMap = res.data
      })
    },
    // 获取协议状态
    getProtocolStatus() {
      return request({
        url: '/admin/protocol/status',
        method: 'get'
      })
    },
    /// / 获取字典数据
    async getDictArray() {
      var dictArray = []
      dictArray.push('product_state')
      dictArray.push('instance_node')
      dictArray.push('transport_protocol')
      // 标签
      dictArray.push('product_tags')

      await this.getDictMap(dictArray).then((res) => {
        this.stateOptions = res.data['product_state']
        this.nodeOptions = res.data['instance_node']
        this.nodeOptions.forEach((item) => {
          this.deviceNodes.push({
            id: item.dictLabel,
            name: item.dictValue
          })
        })
        res.data['transport_protocol'].forEach((item) => {
          this.transportProtocols.push({
            id: item.dictLabel,
            name: item.dictValue
          })
        })
      })

      // 从新的协议状态 API 获取接入方式数据
      await this.getProtocolStatus().then(response => {
        if (response.code === 0 && response.data && response.data.protocols) {
          const protocols = response.data.protocols
          // 显示后台返回的所有协议，保存完整信息
          Object.keys(protocols).forEach(key => {
            const protocol = protocols[key]
            this.thirdPlatforms.push({
              id: protocol.name, // 显示名称
              name: protocol.code, // 协议代码
              isManualCreatable: protocol.isManualCreatable, // 是否支持手动创建
              notCreatableReason: protocol.notCreatableReason, // 不可创建原因
              description: protocol.description // 协议描述
            })
          })
        }
      }).catch(error => {
        console.error('获取协议状态失败:', error)
        // 如果接口失败，使用默认数据作为备份
        var dictArray2 = ['third_platform']
        this.getDictMap(dictArray2).then(res => {
          res.data['third_platform'].forEach((item) => {
            this.thirdPlatforms.push({
              id: item.dictLabel,
              name: item.dictValue,
              isManualCreatable: true, // 默认支持
              notCreatableReason: '',
              description: item.remark || ''
            })
          })
        })
      })

      // 获取产品标签数据
      await this.getDictMap(['product_tags']).then((res) => {
        res.data['product_tags'].forEach((item) => {
          this.productTags.push({
            id: item.dictValue,
            name: item.dictLabel
          })
        })
      })
    },
    /* 产品状态修改 */
    confirmHandleState(row) {
      const text = row.state === 1 ? '启用' : '关闭'
      console.log(row.state)
      row.state = row.state === 0 ? 1 : 0
      updateProductState(row.id, row.state)
        .then(() => {
          this.$message.success(text + '成功', 3)
        })
        .catch(function () {
          this.$message.error(text + '异常', 3)
        })
    },
    cancelHandleState(row) {
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        productId: undefined,
        productKey: undefined,
        productSecret: undefined,
        thirdPlatform: undefined,
        thirdConfiguration: undefined,
        companyNo: undefined,
        classifiedId: undefined,
        configuration: undefined,
        networkWay: undefined,
        deviceNode: undefined,
        projectName: undefined,
        projectId: undefined,
        classifiedName: undefined,
        messageProtocol: undefined,
        orgId: undefined,
        name: undefined,
        creatorId: undefined,
        describe: undefined,
        storePolicy: undefined,
        storePolicyConfiguration: undefined,
        transportProtocol: undefined,
        photoUrl: undefined,
        protocolName: undefined,
        metadata: undefined,
        state: 0,
        pageNum: 1,
        pageSize: 12,
        tags: undefined
      }
      this.tagList = []
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
    selectProduct(product) {
      this.selectedProduct = product
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      if (row.devNum > 0) {
        that.$message.warn('该产品有' + row.devNum + '个设备,无法删除', 2)
        return
      }
      const ids = row.id || this.ids
      this.$confirm({
        title: '删除产品?',
        content: '确定删除【' + row.name + '】？',
        onOk() {
          return delProduct(ids).then(() => {
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
        content: '此操作将导出当前条件下所有产品的完整配置（包括协议、网络、物模型）',
        onOk() {
          that.download(
            'admin/v1/product/transfer/export',
            {
              ...that.queryParam
            },
            `product_packages_${new Date().getTime()}.json`
          )
        },
        onCancel() {
        }
      })
    },
    /** 单个产品导出按钮操作 */
    handleSingleExport(row) {
      var that = this
      this.$confirm({
        title: '是否确认导出?',
        content: '此操作将导出【' + row.name + '】的完整配置（包括协议、网络、物模型）',
        onOk() {
          that.download(
            'admin/v1/product/transfer/export',
            {
              'id': row.id
            },
            `${row.name}_${new Date().getTime()}.json`
          )
        },
        onCancel() {
        }
      })
    },
    /** 查看产品详情 */
    checkDetails(row) {
      const productId = row.id
      this.$router.push(`/product/details/${productId}`)
    },
    // hrefClick () {
    //   window.open('https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/product/addproduct')
    // }

    toggleScreen(tag) {
      switch (tag) {
        case 'all':
          this.queryParam.hasDevice = false
          this.queryParam.self = false
          break
        case 'hasDevice':
          this.queryParam.hasDevice = !this.queryParam.hasDevice
          break
        case 'self':
          this.queryParam.self = !this.queryParam.self
          break
      }
      this.getList()
    },

    /** 加载产品名称搜索列表 */
    async loadProductNameSearchList(searchValue = '') {
      try {
        this.productSearchLoading = true
        const params = {
          pageNum: 1,
          pageSize: 50, // 限制返回数量
          name: searchValue, // 根据名称搜索
          state: 0 // 只搜索发布状态的产品
        }
        const response = await queryProductList(params)
        this.productSearchList = response.rows || []
      } catch (error) {
        console.error('加载产品搜索列表失败:', error)
        this.$message.error('加载产品搜索列表失败')
      } finally {
        this.productSearchLoading = false
      }
    },

    /** 处理产品名称搜索 */
    handleProductNameSearch(value) {
      // 防抖处理
      clearTimeout(this.productSearchTimer)
      this.productSearchTimer = setTimeout(() => {
        this.loadProductNameSearchList(value)
      }, 300)
    },

    /** 处理产品名称下拉框获得焦点 */
    handleProductNameFocus() {
      // 如果产品搜索列表为空，则加载初始数据
      if (this.productSearchList.length === 0) {
        this.loadProductNameSearchList()
      }
    }
  }
}
</script>

<style scoped lang="less">
// 页面容器样式
.page-header-wrapper {
  max-width: 100%;
  overflow-x: hidden;
}

.ant-pro-page-header-wrap {
  overflow-x: auto;
}

.ant-card {
  // 移除固定最小宽度，允许自适应
  // min-width: 1400px;
  max-width: 100%;
  overflow-x: hidden;
}

/* 页面按钮统一样式覆盖 */
.ant-btn {
  transform: none !important;
  box-shadow: none !important;

  &:hover {
    transform: none !important;
    box-shadow: none !important;
  }

  &:active {
    transform: none !important;
    box-shadow: none !important;
  }

  &.ant-btn-sm {
    height: 20px !important;
    padding: 0 6px !important;
    font-size: 10px !important;
    border-radius: 3px !important;
    line-height: 1 !important;

    .anticon {
      font-size: 10px !important;
    }
  }

  &.ant-btn-circle.ant-btn-sm {
    width: 20px !important;
    height: 20px !important;
    min-width: 20px !important;
    padding: 0 !important;

    .anticon {
      font-size: 10px !important;
    }
  }
}

/* 操作区域样式 */
.table-operations {
  margin-bottom: 12px;

  .ant-btn {
    margin-right: 8px;

    &:last-child {
      margin-right: 0;
    }
  }

  .screen-btns {
    margin-right: 8px;

    .screen {
      padding: 2px 6px;
      font-size: 11px;
      border-radius: 3px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: #f1f5f9;
        color: #1966ff;
      }

      &.selected {
        background: #1966ff;
        color: #ffffff;
      }
    }
  }
}

/* 产品名称搜索选项样式 */
.product-search-option {
  display: flex;
  flex-direction: column;
  padding: 2px 0;
}

.product-search-name {
  font-size: 13px;
  color: #1a202c;
  font-weight: 500;
  line-height: 1.4;
}

.product-search-key {
  font-size: 11px;
  color: #718096;
  font-family: 'Courier New', monospace;
  line-height: 1.2;
  margin-top: 2px;
}

.cardWrap {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  grid-gap: 20px;
  padding: 10px;
  max-width: 100%;
  overflow-x: hidden;

  // 响应式布局
  @media (max-width: 1200px) {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    grid-gap: 16px;
    padding: 8px;
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    grid-gap: 12px;
    padding: 6px;
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr;
    grid-gap: 10px;
    padding: 5px;
  }
}

.product-card {
  border-radius: 12px;
  border: 1px solid #e5e8ef;
  padding: 0;
  min-height: 200px;
  position: relative;
  background: #ffffff;
  transition: all 0.3s ease;
  overflow: hidden;
  width: 100%;
  max-width: 100%;

  &:hover {
    border-color: #1966ff;
    box-shadow: 0 4px 20px rgba(25, 102, 255, 0.1);
    transform: translateY(-2px);
  }
  /* 标签角落定位 - 移动到左下角 */

  .tag-corner {
    position: absolute;
    bottom: 8px;
    left: 8px;
    z-index: 10;

    .tag-list {
      display: flex;
      flex-wrap: wrap;
      gap: 3px;
      justify-content: flex-start;
      align-items: center;

      .tag-item {
        padding: 1px 4px;
        border-radius: 6px;
        background: rgba(34, 197, 94, 0.1);
        color: #16a34a;
        border: 1px solid rgba(34, 197, 94, 0.2);
        font-size: 9px;
        font-weight: 500;
        white-space: nowrap;
        transition: all 0.2s ease;
        backdrop-filter: blur(4px);
        line-height: 1.2;

        &:hover {
          background: rgba(34, 197, 94, 0.15);
          border-color: rgba(34, 197, 94, 0.3);
          transform: scale(1.05);
        }

        &:nth-child(2) {
          background: rgba(168, 85, 247, 0.1);
          color: #9333ea;
          border-color: rgba(168, 85, 247, 0.2);

          &:hover {
            background: rgba(168, 85, 247, 0.15);
            border-color: rgba(168, 85, 247, 0.3);
          }
        }

        &:nth-child(3) {
          background: rgba(249, 115, 22, 0.1);
          color: #ea580c;
          border-color: rgba(249, 115, 22, 0.2);

          &:hover {
            background: rgba(249, 115, 22, 0.15);
            border-color: rgba(249, 115, 22, 0.3);
          }
        }
      }
    }
  }
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 16px 12px 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
  min-height: 50px;

  .header-left {
    flex: 1;
    min-width: 0;

    .product-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .company-badge {
        background: linear-gradient(135deg, #1966ff 0%, #1d4ed8 100%);
        color: #ffffff;
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 0.5px;
        white-space: nowrap;
        box-shadow: 0 2px 8px rgba(25, 102, 255, 0.2);
        flex-shrink: 0;
      }

      .product-name {
        // font-size: 16px;
        font-weight: 700;
        color: #0f172a;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
        letter-spacing: 0.3px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      }
    }
  }

  .header-right {
    flex: 0 0 auto;
    margin-left: 8px;
  }
}

.card-down {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;

  .info-section {
    flex: 1;
    min-width: 0;
    cursor: pointer;

    .info-row {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        flex: 0 0 auto;
        font-size: 12px;
        color: #86909c;
        margin-right: 12px;
        min-width: 60px;
      }

      .content {
        flex: 1;
        font-size: 12px;
        color: #4a4a4a;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    &:hover {
      .info-row .content {
        color: #1966ff;
      }
    }
  }

  .image-section {
    flex: 0 0 64px;
    width: 64px;
    height: 64px;

    .pic, .pic-placeholder {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      background: linear-gradient(135deg, #f7f8fa 0%, #e8f4ff 100%);
      border: 2px solid #e5e8ef;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;
      overflow: hidden;
      position: relative;

      &:hover {
        border-color: #1966ff;
        background: linear-gradient(135deg, #e8f4ff 0%, #d6ebff 100%);
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(25, 102, 255, 0.15);
      }

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.1) 50%, transparent 70%);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover::before {
        opacity: 1;
      }
    }

    .pic img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 10px;
    }

    .pic-placeholder {
      color: #86909c;
      font-size: 24px;

      &:hover {
        color: #1966ff;
      }
    }
  }
}

/* 卡片右下角操作按钮样式 */
.card-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 2px;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 20;
}

.product-card:hover .card-actions {
  opacity: 1;
  visibility: visible;
}

.action-btn {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  &.edit-btn {
    color: #ffffff;
    background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);

    &:hover {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    }
  }

  &.export-btn {
    color: #ffffff;
    background: linear-gradient(135deg, #34d399 0%, #10b981 100%);

    &:hover {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    }
  }

  &.delete-btn {
    color: #ffffff;
    background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);

    &:hover {
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    }
  }
}

/* 下拉菜单样式 */
:deep(.ant-dropdown-menu) {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;

  .ant-dropdown-menu-item {
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: #f1f5f9;
    }

    &.danger-item {
      color: #ef4444;

      &:hover {
        background: #fef2f2;
        color: #dc2626;
      }
    }
  }

  .ant-dropdown-menu-item-divider {
    margin: 4px 0;
    background: #e2e8f0;
  }
}

// .screen-btns /deep/ .ant-btn-primary{
//   background-color: #f2f3f5;
//   color: #1966ff;
//   border-color: #f2f3f5;
// }
.screen-btns {
  .screen {
    margin-right: 10px;
    padding: 0 14px;
    display: inline-block;
    height: 32px;
    line-height: 32px;
    background-color: #f2f3f5;
    border-radius: 32px;
    color: #333;
    cursor: pointer;
  }

  .selected {
    color: #1966ff;
  }
}

.loading-wrap {
  width: 100%;
  height: 400px;
  text-align: center;
  line-height: 400px;
}

/* 搜索表单响应式样式 */
.table-page-search-wrapper {
  .ant-form {
    .ant-row {
      // 响应式间距
      @media (max-width: 768px) {
        margin: 0 -8px;

        .ant-col {
          padding: 0 8px;
        }
      }

      @media (max-width: 480px) {
        margin: 0 -4px;

        .ant-col {
          padding: 0 4px;
        }
      }
    }
  }
}
</style>
