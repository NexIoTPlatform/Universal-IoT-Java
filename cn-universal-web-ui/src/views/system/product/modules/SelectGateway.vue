<template>
  <a-modal
    v-model="visible"
    title="选择网关产品"
    @ok="handleOk"
    width="600px"
    :bodyStyle="{ padding: '20px' }"
  >
    <div class="gateway-selector">
      <a-input-search
        allowClear
        v-model.trim="keywords"
        placeholder="搜索网关名称"
        size="large"
        @input="onSearch"
        @keyup.enter.native="onSearch"
        @search="onSearch"
        class="search-input"
      />

      <div class="gateway-list">
        <a-empty v-if="list.length === 0" description="暂无可用网关"/>
        <a-radio-group v-else v-model="selectedValue" class="gateway-radio-group">
          <div
            v-for="item in list"
            :key="item.productKey"
            class="gateway-item"
            :class="{ selected: selectedValue === item.productKey }"
            @click="selectedValue = item.productKey"
          >
            <a-radio :value="item.productKey" class="gateway-radio">
              <div class="gateway-card-content">
                <!-- 网关图片 -->
                <div class="gateway-image">
                  <img v-if="getProductImage(item)" :src="getProductImage(item)" alt="网关图片"/>
                  <div v-else class="default-image">
                    <SvgDefaultProduct/>
                  </div>
                </div>

                <!-- 网关信息 -->
                <div class="gateway-info">
                  <div class="gateway-header">
                    <div class="gateway-name">{{ item.name }}</div>
                    <div class="gateway-tags">
                      <a-tag size="small" color="blue">{{ item.thirdPlatform || 'tcp' }}</a-tag>
                      <a-tag size="small" color="cyan">{{
                          getDeviceNodeText(item.deviceNode)
                        }}
                      </a-tag>
                    </div>
                  </div>
                  <div class="gateway-meta">
                    <div class="meta-item">
                      <span class="label">ProductKey:</span>
                      <code class="value">{{ item.productKey }}</code>
                    </div>
                    <div class="meta-item" v-if="item.transportProtocol">
                      <span class="label">数据协议:</span>
                      <span class="value">{{ item.transportProtocol }}</span>
                    </div>
                    <div class="meta-item" v-if="item.companyNo">
                      <span class="label">厂商:</span>
                      <span class="value">{{ item.companyNo }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </a-radio>
          </div>
        </a-radio-group>
      </div>
    </div>
  </a-modal>
</template>
<script>
import {gatewayList} from '@/api/system/dev/product'
import SvgDefaultProduct from './SvgDefaultProduct'

export default {
  components: {
    SvgDefaultProduct
  },
  props: {
    selectedGateway: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      visible: false,
      originList: [],
      list: [],
      keywords: '',
      selectedValue: ''
    }
  },
  watch: {
    selectedValue(value) {
      // 单选不需要复杂的逻辑
    }
  },
  methods: {
    showModal() {
      this.keywords = ''
      this.selectedValue = this.selectedGateway ? this.selectedGateway.productKey : ''
      this.visible = true
      this.getList()
    },

    handleOk() {
      if (this.selectedValue) {
        const selectedItem = this.originList.find(i => i.productKey === this.selectedValue)
        this.$emit('add', selectedItem)
      }
      this.visible = false
    },

    onSearch() {
      this.list = this.originList.filter(item => item.name.includes(this.keywords))
    },

    getList() {
      gatewayList().then(res => {
        this.originList = res.data
        this.list = res.data
      })
    },

    // 获取产品图片
    getProductImage(product) {
      if (!product.photoUrl) {
        return ''
      }
      try {
        const photoObj = typeof product.photoUrl === 'string'
          ? JSON.parse(product.photoUrl)
          : product.photoUrl
        return photoObj && photoObj.img ? photoObj.img : ''
      } catch (e) {
        return ''
      }
    },

    // 获取设备节点文本
    getDeviceNodeText(deviceNode) {
      const nodeMap = {
        'DEVICE': '直连设备',
        'GATEWAY': '网关设备',
        'GATEWAY_SUB_DEVICE': '网关子设备'
      }
      return nodeMap[deviceNode] || deviceNode || '-'
    }
  }
}
</script>

<style lang="less" scoped>
.gateway-selector {
  .search-input {
    margin-bottom: 20px;
  }

  .gateway-list {
    max-height: 450px;
    overflow-y: auto;
    padding: 4px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d1d5db;
      border-radius: 3px;

      &:hover {
        background: #9ca3af;
      }
    }
  }

  .gateway-radio-group {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .gateway-item {
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    padding: 0;
    cursor: pointer;
    transition: all 0.3s;
    background: #fff;
    overflow: hidden;

    &:hover {
      border-color: #60a5fa;
      background: #f0f9ff;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
      transform: translateY(-2px);
    }

    &.selected {
      border-color: #3b82f6;
      background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    .gateway-radio {
      width: 100%;
      padding: 0;
      margin: 0;

      :deep(.ant-radio) {
        position: absolute;
        top: 16px;
        left: 16px;
        z-index: 1;
      }
    }

    .gateway-card-content {
      display: flex;
      gap: 16px;
      padding: 16px 16px 16px 44px;
      align-items: flex-start;
    }

    .gateway-image {
      width: 80px;
      height: 80px;
      border-radius: 8px;
      overflow: hidden;
      border: 2px solid #f1f5f9;
      background: #fafafa;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .default-image {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;

        // SvgDefaultProduct组件样式
        :deep(svg) {
          width: 60px;
          height: 60px;
        }
      }
    }

    .gateway-info {
      flex: 1;
      min-width: 0;
    }

    .gateway-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      gap: 12px;

      .gateway-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .gateway-tags {
        display: flex;
        gap: 6px;
        flex-shrink: 0;

        .ant-tag {
          margin: 0;
          border-radius: 4px;
          font-size: 12px;
        }
      }
    }

    .gateway-meta {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;

        .label {
          color: #64748b;
          font-weight: 500;
          min-width: 80px;
        }

        .value {
          color: #334155;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        code.value {
          font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
          font-size: 12px;
          background: #f1f5f9;
          padding: 2px 6px;
          border-radius: 4px;
          color: #475569;
        }
      }
    }
  }
}
</style>
