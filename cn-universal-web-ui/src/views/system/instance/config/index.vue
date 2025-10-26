<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="设备序列号" prop="deviceId">
                <a-input v-model="queryParam.deviceId" placeholder="请输入设备序列号"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="所属应用" prop="application">
                <a-select show-search :value="queryParam.application" placeholder="请输入关键字搜索"
                          :default-active-first-option="false" :show-arrow="false"
                          :filter-option="false"
                          :not-found-content="null" @search="appSearch" @change="appChange">
                  <a-select-option v-for="app in appList" :key="app.appUniqueId">
                    {{ app.appName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="产品名称" prop="productKey">
                <a-select show-search placeholder="请选择产品名称" option-filter-prop="children"
                          :value="queryParam.productKey"
                          :filter-option="productFilter" @change="productChange">
                  <a-select-option v-for="product in productList" :key="product.productKey">
                    {{ product.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery"><iot-icon
                  type="icon-search"/>查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operations">
        <a-button type="primary" @click="batchConfig" v-hasPermi="['device:batch:config']">
          <a-icon type="setting"/>
          批量配置
        </a-button>
        <a-button type="primary" @click="batchHistory" v-hasPermi="['device:batch:history']">
          <a-icon type="clock-circle"/>
          批量历史
        </a-button>
        <a-button type="dashed" shape="circle" :loading="loading" :style="{ float: 'right' }"
                  icon="reload"
                  @click="getList"/>
      </div>

      <a-table :loading="loading" rowKey="id" :columns="columns" :data-source="list"
               :row-selection="{
        columnWidth: 100,
        selectedRowKeys: selectedRowKeys,
        onChange: onSelectChange,
        onSelectAll: onSelectAll,
        onSelect: onSelect
      }" :pagination="false">
        <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
          <a-popover placement="rightTop" trigger="hover" :title="'设备配置信息'"
                     v-if="getConfigInfo(record.configuration)">
            <template slot="content">
              <div class="config-popover">
                <div v-for="(value, key) in getConfigInfo(record.configuration)" :key="key"
                     class="config-popover-item">
                  <span class="config-popover-key">{{ key }}:</span>
                  <span class="config-popover-value">{{ value }}</span>
                </div>
              </div>
            </template>
            <div class="device-info-content">
              <div class="device-name">
                <a-button type="link"
                          @click="$router.push(`/system/instance/instance-details/${record.id}`)"
                          class="device-name-link">
                  {{ record.deviceName || '未命名设备' }}
                </a-button>
              </div>
              <div class="device-id">{{ record.deviceId }}</div>
              <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
            </div>
          </a-popover>
          <div v-else class="device-info-content">
            <div class="device-name">
              <a-button type="link"
                        @click="$router.push(`/system/instance/instance-details/${record.id}`)"
                        class="device-name-link">
                {{ record.deviceName || '未命名设备' }}
              </a-button>
            </div>
            <div class="device-id">{{ record.deviceId }}</div>
            <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
          </div>
        </div>
        <div slot="productInfo" slot-scope="text, record" class="product-info-cell">
          <div class="product-name">{{ record.productName || '未知产品' }}</div>
          <div class="product-key">{{ record.productKey }}</div>
        </div>
        <div slot="state" slot-scope="text, record" class="state-cell">
          <div
            :class="{ 'status-badge online': record.state, 'status-badge offline': !record.state }">
            <span class="status-dot"></span>
            <span class="status-text">{{ stateFormat(record) }}</span>
          </div>
        </div>
        <div slot="onlineTime" slot-scope="text, record" class="online-time-cell">
          <div class="time-main">{{ formatOnlineTime(record.onlineTime) }}</div>
          <div class="time-ago">{{ getTimeAgo(record.onlineTime) }}</div>
        </div>
      </a-table>
      <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper
                    :current="queryParam.pageNum"
                    :total="total" :page-size="queryParam.pageSize"
                    :showTotal="total => `共 ${total} 条`"
                    @showSizeChange="onShowSizeChange" @change="changeSize"/>
    </a-card>

    <Metadata ref="metadata" :queryParam="queryParam" :selectedRowKeys="selectedRowKeys"
              :unSelectRowKeys="unSelectRowKeys" :chooseAll="chooseAll" :productList="productList"/>
  </page-header-wrapper>
</template>

<script>
import {listApp, listBatchConfig, listProduct} from '@/api/system/dev/batch'
import Metadata from './components/Metadata.vue'

export default {
  name: 'DevicesConfig',
  components: {
    Metadata
  },
  data() {
    return {
      appList: [],
      productList: [],
      list: [],

      selectedRowKeys: [], // 选中项
      unSelectRowKeys: [], // 全选后取消勾选项
      chooseAll: false, // 是否全选

      loading: false,
      total: 0,
      // 0-离线，1-在线字典
      stateOptions: [],
      // 查询参数
      queryParam: {
        deviceId: undefined,
        application: undefined,
        productKey: undefined,
        pageNum: 1,
        pageSize: 10
      },
      columns: [
        {
          title: '设备名称/DeviceId',
          dataIndex: 'deviceInfo',
          scopedSlots: {customRender: 'deviceInfo'},
          width: '30%',
          align: 'left'
        },
        {
          title: '所属产品/ProductKey',
          dataIndex: 'productInfo',
          scopedSlots: {customRender: 'productInfo'},
          width: '25%',
          align: 'left'
        },
        {
          title: '设备状态',
          dataIndex: 'state',
          scopedSlots: {customRender: 'state'},
          width: '15%',
          align: 'center'
        },
        {
          title: '通信状态',
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
          width: '30%',
          align: 'center'
        }
      ]
    }
  },
  created() {
    this.init()
  },
  methods: {
    // 0-离线，1-在线 字典翻译
    stateFormat(row) {
      return row.state ? '在线' : '离线'
    },
    // 格式化在线时间
    formatOnlineTime(time) {
      return this.parseTime(time)
    },
    // 获取相对时间
    getTimeAgo(timestamp) {
      if (!timestamp || timestamp === '0') {
        return '从未通信'
      }
      const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
      const timeMs = time.toString().length === 10 ? time * 1000 : time
      const now = Date.now()
      const diff = now - timeMs

      if (diff < 60000) { // 1分钟内
        return '刚刚在线'
      } else if (diff < 3600000) { // 1小时内
        const minutes = Math.floor(diff / 60000)
        return `${minutes}分钟前`
      } else if (diff < 86400000) { // 1天内
        const hours = Math.floor(diff / 3600000)
        return `${hours}小时前`
      } else if (diff < 2592000000) { // 30天内
        const days = Math.floor(diff / 86400000)
        return `${days}天前`
      } else {
        return '长时间未通信'
      }
    },
    async init() {
      await this.getAppList()
      if (this.appList.length) {
        this.queryParam.application = this.appList[0].appUniqueId
      }
      await this.getProductList(this.queryParam.application)
    },
    getAppList(appName) {
      return listApp(appName).then(res => {
        this.appList = res.data
      })
    },
    appSearch(keyword) {
      if (this.appTimer) {
        clearTimeout(this.appTimer)
      }
      this.appTimer = setTimeout(() => {
        this.getAppList(keyword)
      }, 500)
    },
    appChange(value) {
      this.queryParam.application = value
      if (!value) {
        this.queryParam.productKey = undefined
        return
      }
      this.getProductList(value)
    },
    getProductList(id) {
      if (!id) {
        return
      }
      return listProduct(id).then(res => {
        this.productList = res.data
        this.queryParam.productKey = undefined
        if (res.data.length) {
          this.productChange(res.data[0].productKey)
        } else {
          this.list = []
        }
      })
    },
    productChange(value) {
      if (this.queryParam.productKey !== value) {
        this.selectedRowKeys = []
        this.unSelectRowKeys = []
        this.chooseAll = false
        this.queryParam.productKey = value
        this.getList()
      }
    },
    productFilter(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    /** 查询设备列表 */
    getList() {
      if (!this.queryParam.application) {
        this.$notification.error({
          message: '错误',
          description: '请选择应用！',
          duration: 3
        })
        return
      }
      if (!this.queryParam.productKey) {
        this.$notification.error({
          message: '错误',
          description: '当前应用暂无产品，请先前往应用添加产品！',
          duration: 3
        })
        return
      }
      this.loading = true
      listBatchConfig(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.handleChooseAll()
      }).finally(() => {
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
        deviceId: undefined,
        application: undefined,
        productKey: undefined,
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
    onSelectChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    // 选中/取消 回调
    onSelect(record, selected) {
      if (!selected) {
        this.unSelectRowKeys = Array.from(new Set([...this.unSelectRowKeys, record.id]))
      } else {
        this.unSelectRowKeys.splice(this.unSelectRowKeys.indexOf(record.id))
      }
    },
    onSelectAll(selected) {
      this.chooseAll = selected
      if (!selected) {
        this.selectedRowKeys = []
        this.unSelectRowKeys = []
      }
    },
    // 处理翻页后的批量配置
    handleChooseAll() {
      if (!this.chooseAll) {
        return
      }
      const {list, selectedRowKeys, unSelectRowKeys} = this
      const rows = list.map(t => t.id)
      const newPageIds = rows.filter(item => !unSelectRowKeys.find(t => t === item))
      const newIds = Array.from(new Set([...selectedRowKeys, ...newPageIds]))
      this.selectedRowKeys = newIds
    },
    batchConfig() {
      if (!this.selectedRowKeys.length) {
        this.$notification.info({
          message: '提示',
          description: '请至少选中一个操作的设备！',
          duration: 3
        })
        return
      }
      this.$refs.metadata.open()
    },
    batchHistory() {
      this.$router.push('/instance/devicesconfig/history')
    },
    getConfigInfo(config) {
      if (!config) {
        return {}
      }
      try {
        const parsedConfig = JSON.parse(config)
        return parsedConfig
      } catch (e) {
        return {}
      }
    }
  }
}
</script>

<style scoped lang="less">
:deep(.ant-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  background: #fff;
}

:deep(.ant-table) {
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  font-size: 14px;
}

:deep(.ant-table-thead > tr > th) {
  background: #f7f8fa;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 8px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  color: #262626;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f7fa;
}

:deep(.ant-pagination) {
  margin-top: 24px;
  text-align: right;
}

:deep(.ant-btn) {
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}

:deep(.ant-btn-primary) {
  background: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-btn-primary:hover) {
  background: #40a9ff;
  border-color: #40a9ff;
}

:deep(.ant-btn-link) {
  color: #1890ff;
  font-weight: 500;
  padding: 0;
}

:deep(.ant-btn-link:hover) {
  color: #40a9ff;
}

.state-cell {
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

.product-info-cell {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;

  .product-name {
    font-size: 14px;
    color: #262626;
    font-weight: 500;
    line-height: 1.4;
  }

  .product-key {
    font-size: 12px;
    color: #595959;
    font-family: 'Courier New', monospace;
    line-height: 1.4;
  }
}

.device-info-cell {
  padding: 12px 0;
}

.device-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-name {
  line-height: 1.4;
}

.device-name-link {
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

.device-name-link:focus,
.device-name-link:active {
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.device-name-link:hover {
  color: #40a9ff;
  padding: 0 !important;
  margin: 0;
  border: none;
  box-shadow: none;
  background: none;
}

.device-id {
  font-size: 13px;
  color: #595959;
  font-family: 'Courier New', monospace;
  font-weight: 600;
  line-height: 1.4;
}

.iot-id {
  font-size: 11px;
  color: #8c8c8c;
  font-family: 'Courier New', monospace;
  line-height: 1.4;
}

.online-time-cell {
  text-align: center;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.time-main {
  font-size: 13px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
}

.time-ago {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.3;
  font-weight: 400;
}

.config-popover {
  padding: 8px;
  font-size: 12px;
  color: #262626;
  line-height: 1.6;
}

.config-popover-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.config-popover-key {
  font-weight: 500;
  color: #515151;
}

.config-popover-value {
  color: #8c8c8c;
  font-weight: 400;
}
</style>
