<template>
  <div class="tcp-connection-info">
    <!-- TCP连接信息展示 -->
    <div class="connection-info-section">
      <div class="section-header">
        <a-icon type="link" style="color: #1890ff; margin-right: 8px;"/>
        <span class="section-title">TCP连接信息</span>
      </div>

      <!-- 连接方式选择 -->
      <div v-if="connectionInfo" class="connection-mode-selector">
        <div class="selector-title">
          <a-icon type="setting" style="color: #666; margin-right: 6px;"/>
          <span>连接方式选择</span>
        </div>
        <div class="selector-content">
          <a-radio-group v-model="currentConnectionMode" @change="onConnectionModeChange">
            <a-radio-button value="server" :disabled="!connectionInfo.network.enabled">
              <a-icon type="cloud-server" style="margin-right: 4px;"/>
              {{ getPlatformTitle() }}
            </a-radio-button>
            <a-radio-button value="sni" v-if="product.thirdPlatform === 'tcp'">
              <a-icon type="global" style="margin-right: 4px;"/>
              SNI域名（设备需支持TLS）
            </a-radio-button>
          </a-radio-group>
          <!-- <a-button 
            type="default" 
            size="small" 
            @click="showServerManagementModal"
            style="margin-left: 12px;"
            v-if="currentConnectionMode === 'server'"
          >
            <a-icon type="tool" style="margin-right: 4px;"/>
            管理{{ getPlatformTitle() }}服务端
          </a-button> -->
        </div>

        <!-- 连接方式说明 -->
        <div class="connection-mode-description">
          <div class="mode-item">
            <a-icon type="cloud-server" style="color: #1890ff; margin-right: 8px;"/>
            <div class="mode-info">
              <div class="mode-name">{{ getPlatformTitle() }}服务端</div>
              <div class="mode-desc">通过{{
                  getPlatformTitle()
                }}服务端组件连接，需配置允许连接的IP地址和端口，设备连接到服务端
              </div>
            </div>
          </div>
          <div class="mode-item" v-if="product.thirdPlatform === 'tcp'">
            <a-icon type="global" style="color: #52c41a; margin-right: 8px;"/>
            <div class="mode-info">
              <div class="mode-name">SNI域名（设备需支持TLS）</div>
              <div class="mode-desc">使用TLS
                SNI方式，域名格式为：productKey.tcp.nexiot.xyz:38883，支持加密传输，所有TCP产品均默认支持该方式连接
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 当前连接信息展示 -->
      <div v-if="connectionInfo" class="current-connection-display">
        <div class="current-connection-header">
          <div class="connection-status">
            <a-icon :type="getCurrentConnectionIcon()" :style="{ color: getCurrentConnectionColor() }"/>
            <span class="status-text">{{ getCurrentConnectionTitle() }}</span>
            <a-badge v-if="currentConnectionMode === 'server'"
                     :status="connectionInfo.network.state ? 'success' : 'default'"
                     :text="connectionInfo.network.state ? '运行中' : '已停止'"/>
          </div>
        </div>

        <!-- 当前连接详细信息 -->
        <div class="connection-details">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="连接方式">
              {{ getCurrentConnectionInfo().connectionType }}
            </a-descriptions-item>
            <a-descriptions-item label="允许地址">
              {{ getCurrentConnectionInfo().host || '未配置' }}
            </a-descriptions-item>
            <a-descriptions-item label="端口">
              {{ getCurrentConnectionInfo().port || '未配置' }}
            </a-descriptions-item>
            <a-descriptions-item label="配置说明">
              {{ getCurrentConnectionInfo().description || '请妥善保管连接信息，注意安全保密' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>

      <!-- 加载中状态 -->
      <div v-else class="loading-connection">
        <a-spin size="large">
          <div class="loading-text">正在加载{{ getPlatformTitle() }}连接信息...</div>
        </a-spin>
      </div>
    </div>

    <!-- 服务端管理弹窗 -->
    <a-modal
      :title="`${getPlatformTitle()}服务端管理`"
      :visible="serverManagementModalVisible"
      @cancel="closeServerManagementModal"
      :footer="null"
      width="900px"
      :destroyOnClose="true"
    >
      <div class="server-management-modal">
        <!-- 当前绑定状态 -->
        <div class="current-binding-status">
          <div class="status-header">
            <a-icon type="info-circle" style="color: #1890ff; margin-right: 8px;"/>
            <span>当前绑定状态</span>
          </div>
          <div v-if="product.networkUnionId" class="bound-status">
            <a-alert
              :message="`已配置${getPlatformTitle()}服务端`"
              :description="`当前产品已配置${getPlatformTitle()}服务端，接入标识：${product.networkUnionId}`"
              type="success"
              show-icon
              style="margin-bottom: 16px"
            />
            <div v-if="boundComponent" class="bound-component-info">
              <a-descriptions :column="2" size="small" bordered>
                <a-descriptions-item label="组件名称">
                  {{ boundComponent.name }}
                </a-descriptions-item>
                <a-descriptions-item label="组件类型">
                  {{ getComponentTypeLabel(boundComponent.type) }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('common.running.status')">
                  <a-badge :status="boundComponent.running ? 'success' : 'default'"/>
                  {{ boundComponent.running ? '运行中' : '已停止' }}
                </a-descriptions-item>
                <a-descriptions-item label="操作">
                  <a-space>
                    <a-button type="default" size="small" @click="viewComponentDetail">
                      查看详情
                    </a-button>
                    <a-button type="danger" size="small" @click="unbindComponent" :loading="unbinding">
                      解绑组件
                    </a-button>
                  </a-space>
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </div>
          <div v-else class="unbound-status">
            <a-alert
              :message="`未配置${getPlatformTitle()}服务端`"
              :description="`当前产品未配置${getPlatformTitle()}服务端，您可以选择下方已配置完整且启动状态的${getPlatformTitle()}服务端组件进行绑定`"
              type="info"
              show-icon
            />
          </div>
        </div>

        <!-- 服务端组件列表 -->
        <div class="component-selection">
          <div class="selection-header">
            <h4>可用{{ getPlatformTitle() }}服务端组件（已配置且启动）</h4>
            <a-button
              type="primary"
              icon="reload"
              size="small"
              @click="loadComponentList"
              :loading="loading"
            >
              刷新列表
            </a-button>
          </div>

          <a-spin :spinning="loading">
            <div v-if="!componentList || componentList.length === 0" class="empty-state">
              <a-empty
                :description="`暂无可用的${getPlatformTitle()}服务端组件（需要配置完整且启动状态）`"
                :image="emptyImage"
              >
                <a-button type="primary" @click="goToCreate">
                  创建{{ getPlatformTitle() }}服务端组件
                </a-button>
              </a-empty>
            </div>

            <div v-else class="component-grid">
              <a-card
                v-for="item in filteredComponentList"
                :key="item.id"
                hoverable
                class="network-card"
                :class="{ 'selected': selectedComponent && selectedComponent.id === item.id }"
                @click="selectComponent(item)"
              >
                <div class="card-header">
                  <span style="display:flex;align-items:center;">
                    <a-badge :status="item.running ? 'success' : 'default'"
                             :class="{ 'breath-badge': item.running }"
                             style="margin-right:12px;font-size:18px;line-height:1;"/>
                  </span>
                  <a class="card-title">
                    {{ item.name }}
                  </a>
                </div>

                <div class="card-body">
                  <div class="card-row">
                    <a-icon type="cloud-server" style="margin-right:4px;"/>
                    服务地址：
                    <a-tooltip :title="getConfigValue(item, 'host')">{{
                        getConfigValue(item, 'host')
                      }}
                    </a-tooltip>
                  </div>
                  <div class="card-row">
                    <a-icon type="key" style="margin-right:4px;"/>
                    标识：{{ item.unionId }}
                  </div>
                  <div class="card-row">
                    <a-icon type="poweroff" style="margin-right:4px;"/>
                    {{$t('common.status')}}：
                    <span :class="getStatusClass(item)">
                      {{ getStatusText(item) }}
                    </span>
                  </div>
                </div>

                <div class="card-actions">
                  <div class="action-btn bind-btn"
                       @click.stop="bindComponent(item)"
                       :class="{ disabled: product.networkUnionId === item.unionId }">
                    <a-icon type="link"/>
                  </div>
                  <div class="action-btn edit-btn"
                       @click.stop="viewComponentDetail(item)"
                       v-hasPermi="['network:tcp:edit']">
                    <a-icon type="edit"/>
                  </div>
                </div>
              </a-card>
            </div>
          </a-spin>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import {listNetwork} from '@/api/system/network'
import {getProductConnectInfo, updateProductNetworkUnionId} from '@/api/system/dev/product'
import {parseTime} from '@/utils/ruoyi'

export default {
  name: 'TcpConnectionInfo',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      binding: false,
      unbinding: false,
      componentList: [],
      selectedComponent: null,
      boundComponent: null,
      emptyImage: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNDEiIHZpZXdCb3g9IjAgMCA2NCA0MSIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTMyIDQwQzQ0LjA5MDkgNDAgNTQgMzAuMDkwOSA1NCAxOEM1NCA1LjkwOTEgNDQuMDkwOSAtNCAzMiAtNEMxOS45MDkxIC00IDEwIDUuOTA5MSAxMCAxOEMxMCAzMC4wOTA5IDE5LjkwOTEgNDAgMzIgNDBaIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0zMiAzNkM0Mi4wNTg5IDM2IDUwIDI4LjA1ODkgNTAgMThDNTAgNy45NDExIDQyLjA1ODkgMCAzMiAwQzIxLjk0MTEgMCAxNCA3Ljk0MTEgMTQgMThDMTQgMjguMDU4OSAyMS45NDExIDM2IDMyIDM2WiIgZmlsbD0iI0YwRjBGMCIvPgo8cGF0aCBkPSJNMzIgMjhDMzYuNDE4MyAyOCA0MCAyNC40MTgzIDQwIDIwQzQwIDE1LjU4MTcgMzYuNDE4MyAxMiAzMiAxMkMyNy41ODE3IDEyIDI0IDE1LjU4MTcgMjQgMjBDMjQgMjQuNDE4MyAyNy41ODE3IDI4IDMyIDI4WiIgZmlsbD0iI0YwRjBGMCIvPgo8L3N2Zz4K',
      // 连接信息相关
      connectionInfo: null,
      // 当前连接模式：'sni' 或 'server'
      currentConnectionMode: 'sni',
      // TCP服务端管理弹窗
      serverManagementModalVisible: false
    }
  },
  watch: {
    product: {
      handler(newProduct) {
        if (newProduct.networkUnionId) {
          this.loadBoundComponent()
        } else {
          this.boundComponent = null
          this.loadComponentList()
        }
        this.loadConnectionInfo()
      },
      immediate: true
    }
  },
  created() {
    if (this.product.networkUnionId) {
      this.loadBoundComponent()
    } else {
      this.loadComponentList()
    }
  },
  computed: {
    /** 过滤已配置且启动的组件列表 */
    filteredComponentList() {
      if (!this.componentList || !Array.isArray(this.componentList)) {
        return []
      }
      return this.componentList.filter(item => this.isTcpConfigured(item) && item.running)
    }
  },
  methods: {
    parseTime,
    /** 获取平台标题 */
    getPlatformTitle() {
      if (this.product.thirdPlatform === 'tcp') {
        return 'TCP'
      }
      if (this.product.thirdPlatform === 'udp') {
        return 'UDP'
      }
      return '网络'
    },
    /** 获取组件类型标签 */
    getComponentTypeLabel(type) {
      if (type === 'TCP_CLIENT') {
        return 'TCP客户端'
      }
      if (type === 'TCP_SERVER') {
        return 'TCP服务端'
      }
      if (type === 'UDP') {
        return 'UDP服务'
      }
      return type
    },
    /** 通用配置值获取方法 */
    getConfigValue(component, key, defaultValue = null) {
      try {
        const config = JSON.parse(component.configuration)
        return config[key] || defaultValue
      } catch (error) {
        return null
      }
    },
    /** 加载组件列表 */
    async loadComponentList() {
      this.loading = true
      try {
        const types = this.product.thirdPlatform === 'tcp' ? ['TCP_SERVER'] : ['UDP']
        const response = await listNetwork({
          pageNum: 1,
          pageSize: 100,
          type: types
        })
        this.componentList = response.rows || []
      } catch (error) {
        this.$message.error('加载组件列表失败：' + (error.message || error))
      } finally {
        this.loading = false
      }
    },
    /** 选择组件 */
    selectComponent(component) {
      this.selectedComponent = component
    },
    /** 绑定组件 */
    async bindComponent(component) {
      this.binding = true
      try {
        await updateProductNetworkUnionId({
          id: this.product.id,
          networkUnionId: component.unionId
        })
        this.$message.success('绑定成功')
        this.$emit('refresh')
        await this.loadConnectionInfo()
        this.closeServerManagementModal()
      } catch (error) {
        this.$message.error('绑定失败：' + (error.message || error))
      } finally {
        this.binding = false
      }
    },
    /** 解绑组件 */
    async unbindComponent() {
      this.$confirm({
        title: '确认解绑',
        content: `确定要解绑当前${this.getPlatformTitle()}服务端组件吗？解绑后产品将无法使用${this.getPlatformTitle()}服务端连接功能。`,
        onOk: async () => {
          this.unbinding = true
          try {
            await updateProductNetworkUnionId({
              id: this.product.id,
              networkUnionId: ''
            })
            this.$message.success('解绑成功')
            this.$emit('refresh')
            await this.loadConnectionInfo()
            this.closeServerManagementModal()
          } catch (error) {
            this.$message.error('解绑失败：' + (error.message || error))
          } finally {
            this.unbinding = false
          }
        }
      })
    },
    /** 查看组件详情 */
    viewComponentDetail(item) {
      if (!item) {
        return
      }
      this.$router.push(`/system/network/tcp/detail/${item.id}`)
    },
    /** 跳转到创建页面 */
    goToCreate() {
      this.$router.push('/system/network/tcp')
    },
    /** 加载已绑定组件信息 */
    async loadBoundComponent() {
      if (!this.product.networkUnionId) {
        this.boundComponent = null
        return
      }

      this.loading = true
      try {
        const response = await listNetwork({
          pageNum: 1,
          pageSize: 100,
          unionId: this.product.networkUnionId
        })

        if (response.rows && response.rows.length > 0) {
          this.boundComponent = response.rows[0]
        } else {
          if (this.componentList && this.componentList.length > 0) {
            this.boundComponent = this.componentList.find(
              item => item.unionId === this.product.networkUnionId
            )
          }
        }
      } catch (error) {
        console.error('加载已绑定组件失败：', error)
        this.$message.error('加载已绑定组件信息失败')
      } finally {
        this.loading = false
      }
    },
    /** 加载连接信息 */
    async loadConnectionInfo() {
      if (!this.product.productKey) {
        return
      }

      try {
        const response = await getProductConnectInfo(this.product.productKey)
        if (response.code === 200 || response.code === 0) {
          this.connectionInfo = response.data
          this.initConnectionMode()
        } else {
          this.$message.error('加载连接信息失败：' + response.msg)
        }
      } catch (error) {
        this.$message.error('加载连接信息失败：' + error.message)
      }
    },

    /** 初始化连接模式 */
    initConnectionMode() {
      if (!this.connectionInfo) return

      // 默认选择服务端，如果服务端不可用且是TCP协议，则选择SNI
      if (this.product.networkUnionId && this.connectionInfo.network.enabled) {
        this.currentConnectionMode = 'server'
      } else if (this.product.thirdPlatform === 'tcp') {
        this.currentConnectionMode = 'sni'
      } else {
        this.currentConnectionMode = 'server'
      }
    },

    /** 连接模式切换 */
    onConnectionModeChange(e) {
      this.currentConnectionMode = e.target.value
    },

    /** 获取当前连接信息 */
    getCurrentConnectionInfo() {
      if (!this.connectionInfo) return {}

      if (this.currentConnectionMode === 'server' && this.connectionInfo.network.enabled) {
        // 服务端连接信息
        const config = this.getConfigValue(this.boundComponent, 'allowedAddresses')
        return {
          connectionType: `${this.getPlatformTitle()}服务端`,
          host: this.connectionInfo.network.host,
          port: this.connectionInfo.network.port,
          allowedAddresses: config || '未配置',
          description: `请注意保护端口安全，避免被恶意使用`
        }
      } else {
        // SNI连接信息（仅TCP）
        return {
          connectionType: this.connectionInfo.builtin.connectionType,
          host: this.connectionInfo.builtin.host,
          port: this.connectionInfo.builtin.port,
          description: this.connectionInfo.builtin.description
        }
      }
    },

    /** 获取当前连接图标 */
    getCurrentConnectionIcon() {
      if (this.currentConnectionMode === 'server') {
        return 'cloud-server'
      } else {
        return 'global'
      }
    },

    /** 获取当前连接颜色 */
    getCurrentConnectionColor() {
      if (this.currentConnectionMode === 'server') {
        return '#1890ff'
      } else {
        return '#52c41a'
      }
    },

    /** 获取当前连接标题 */
    getCurrentConnectionTitle() {
      if (this.currentConnectionMode === 'server' && this.connectionInfo && this.connectionInfo.network.enabled) {
        return `${this.getPlatformTitle()} (${this.connectionInfo.network.name})`
      } else {
        return 'SNI域名（设备需支持TLS）'
      }
    },

    /** 显示TCP服务端管理弹窗 */
    showServerManagementModal() {
      this.serverManagementModalVisible = true
      if (!this.componentList || this.componentList.length === 0) {
        this.loadComponentList()
      }
    },

    /** 关闭TCP服务端管理弹窗 */
    closeServerManagementModal() {
      this.serverManagementModalVisible = false
      this.selectedComponent = null
    },


    /** 检查TCP配置是否完整 */
    isTcpConfigured(item) {
      if (!item.configuration) {
        // console.log('isTcpConfigured: 无配置信息', item)
        return false
      }
      try {
        const config = typeof item.configuration === 'string'
          ? JSON.parse(item.configuration)
          : item.configuration

        // console.log('isTcpConfigured: 解析后的配置', config)
        // console.log('isTcpConfigured: host=', config.host, 'port=', config.port)

        const isConfigured = !!(config.host && config.port)
        // console.log('isTcpConfigured: 配置完整=', isConfigured)

        return isConfigured
      } catch (e) {
        // console.log('isTcpConfigured: 配置解析失败', e, item.configuration)
        return false
      }
    },

    /** 获取状态文本 */
    getStatusText(item) {
      // console.log('getStatusText: 检查状态', {
      //   running: item.running,
      //   state: item.state,
      //   configuration: item.configuration
      // })

      if (item.running) {
        // console.log('getStatusText: 返回运行中')
        return '运行中'
      } else if (this.isTcpConfigured(item)) {
        // console.log('getStatusText: 返回已停止')
        return '已停止'
      } else {
        // console.log('getStatusText: 返回未配置')
        return '未配置'
      }
    },

    /** 获取状态样式类 */
    getStatusClass(item) {
      if (item.running) {
        return 'status-running'
      } else if (this.isTcpConfigured(item)) {
        return 'status-stopped'
      } else {
        return 'status-unconfigured'
      }
    }
  }
}
</script>

<style lang="less" scoped>
.tcp-connection-info {
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #1a202c;
    }
  }

  .connection-info-section {
    margin-bottom: 32px;
    padding: 20px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px solid #e8e8e8;

    .connection-mode-selector {
      margin-bottom: 20px;
      padding: 16px;
      background: #ffffff;
      border-radius: 6px;
      border: 1px solid #e8e8e8;

      .selector-title {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }

      .selector-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .connection-mode-description {
        margin-top: 16px;
        padding: 12px;
        background: #f8f9fa;
        border-radius: 6px;
        border: 1px solid #e8e8e8;

        .mode-item {
          display: flex;
          align-items: flex-start;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .mode-info {
            flex: 1;

            .mode-name {
              font-size: 13px;
              font-weight: 600;
              color: #333;
              margin-bottom: 2px;
            }

            .mode-desc {
              font-size: 12px;
              color: #666;
              line-height: 1.4;
            }
          }
        }
      }

      .ant-radio-group {
        .ant-radio-button-wrapper {
          border-radius: 4px;
          margin-right: 8px;

          &:hover {
            border-color: #1890ff;
            color: #1890ff;
          }

          &.ant-radio-button-wrapper-checked {
            background: #1890ff;
            border-color: #1890ff;
            color: #ffffff;

            &:hover {
              background: #40a9ff;
              border-color: #40a9ff;
            }
          }

          &.ant-radio-button-wrapper-disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
        }
      }
    }

    .current-connection-display {
      .current-connection-header {
        margin-bottom: 16px;

        .connection-status {
          display: flex;
          align-items: center;
          padding: 12px 16px;
          background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
          border-radius: 6px;
          border: 1px solid #b7eb8f;

          .status-text {
            margin-left: 8px;
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }

          .ant-badge {
            margin-left: 12px;
          }
        }
      }

      .connection-details {
        .ant-descriptions {
          background: #ffffff;
          border-radius: 6px;
        }
      }
    }

    .loading-connection {
      text-align: center;
      padding: 40px 0;

      .loading-text {
        margin-top: 16px;
        color: #666;
      }
    }
  }
}

// 网络管理弹窗样式
.server-management-modal {
  .current-binding-status {
    margin-bottom: 24px;

    .status-header {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .bound-component-info {
      .ant-descriptions {
        background: #fafafa;
        border-radius: 6px;
      }
    }
  }

  .component-selection {
    .selection-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1a202c;
      }
    }

    .empty-state {
      padding: 40px 0;
      text-align: center;
    }

    .component-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;

      .network-card {
        margin-bottom: 16px;
        border-radius: 8px;
        box-shadow: 0 2px 8px #f0f1f2;
        transition: box-shadow 0.2s;
        position: relative;
        padding-bottom: 8px;
        cursor: pointer;

        &:hover {
          box-shadow: 0 4px 16px #e6f7ff;
        }

        &.selected {
          border-color: #1890ff;
          box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
        }

        .card-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 8px;
        }

        .card-title {
          margin-left: 8px;
          cursor: pointer;
          color: #1890ff;
          transition: color 0.2s;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          display: block;
          flex: 1;

          &:hover {
            color: #40a9ff;
          }
        }

        .card-body {
          margin: 12px 0 8px 0;
        }

        .card-row {
          font-size: 13px;
          color: #666;
          margin-bottom: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .card-actions {
          display: flex;
          gap: 12px;
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid #f0f0f0;
          justify-content: center;
          align-items: center;
        }

        .action-btn {
          width: 36px;
          height: 36px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.2s ease;
          border: 1px solid #e8e8e8;
          font-size: 16px;
          background: #ffffff;

          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }

          &.disabled {
            opacity: 0.3;
            cursor: not-allowed;

            &:hover {
              transform: none;
              box-shadow: none;
            }
          }
        }

        .bind-btn {
          color: #52c41a;

          &:hover:not(.disabled) {
            background: #f6ffed;
            border-color: #b7eb8f;
            color: #52c41a;
          }
        }

        .edit-btn {
          color: #0958d9;

          &:hover:not(.disabled) {
            background: #e6f7ff;
            border-color: #91d5ff;
            color: #1890ff;
          }
        }
      }
    }
  }
}

.breath-badge {
  animation: breath-scale 1.2s infinite ease-in-out;
}

@keyframes breath-scale {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

// 状态样式
.status-running {
  color: #52c41a;
  font-weight: 500;
}

.status-stopped {
  color: #fa8c16;
  font-weight: 500;
}

.status-unconfigured {
  color: #999;
  font-weight: 500;
}
</style>
