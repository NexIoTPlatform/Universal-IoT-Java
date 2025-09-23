<template>
  <div class="network-component-bind">
    <!-- 连接信息展示 -->
    <div class="connection-info-section">
      <div class="section-header">
        <a-icon type="link" style="color: #1890ff; margin-right: 8px;"/>
        <span class="section-title">连接信息</span>
      </div>
      
      <!-- 连接方式选择 -->
      <div v-if="connectionInfo" class="connection-mode-selector">
        <div class="selector-title">
          <a-icon type="setting" style="color: #666; margin-right: 6px;"/>
          <span>连接方式选择</span>
        </div>
        <div class="selector-content">
          <a-radio-group v-model="currentConnectionMode" @change="onConnectionModeChange">
            <a-radio-button value="builtin">
              <a-icon type="home" style="margin-right: 4px;"/>
              平台直连
            </a-radio-button>
            <a-radio-button value="network" :disabled="!connectionInfo.network.enabled">
              <a-icon type="api" style="margin-right: 4px;"/>
              自建接入
            </a-radio-button>
          </a-radio-group>
          <a-button 
            type="default" 
            size="small" 
            @click="showNetworkManagementModal"
            style="margin-left: 12px;"
          >
            <a-icon type="tool" style="margin-right: 4px;"/>
            管理自建接入
          </a-button>
        </div>
        
        <!-- 连接方式说明 -->
        <div class="connection-mode-description">
          <div class="mode-item">
            <a-icon type="home" style="color: #52c41a; margin-right: 8px;"/>
            <div class="mode-info">
              <div class="mode-name">平台直连</div>
              <div class="mode-desc">使用平台内置MQTT Broker，设备直接连接，配置简单</div>
            </div>
          </div>
          <div class="mode-item">
            <a-icon type="api" style="color: #1890ff; margin-right: 8px;"/>
            <div class="mode-info">
              <div class="mode-name">自建接入</div>
              <div class="mode-desc">通过自建MQTT组件连接，需公网访问和Topic订阅权限</div>
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
            <a-badge v-if="currentConnectionMode === 'network'" 
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
            <a-descriptions-item label="连接地址">
              {{ getCurrentConnectionInfo().host || '未配置' }}
            </a-descriptions-item>
            <a-descriptions-item label="端口">
              {{ getCurrentConnectionInfo().port || '未配置' }}
            </a-descriptions-item>
            <a-descriptions-item v-if="getCurrentConnectionInfo().username" label="用户名">
              {{ getCurrentConnectionInfo().username }}
            </a-descriptions-item>
            <a-descriptions-item v-if="getCurrentConnectionInfo().password" label="密码">
              <span v-if="getCurrentConnectionInfo().password === '点击查看密码'">
                <a-button type="link" size="small" @click="showMqttPassword">点击查看密码</a-button>
              </span>
              <span v-else>{{ getCurrentConnectionInfo().password }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="配置说明">
              {{ getCurrentConnectionInfo().description || '请妥善保管连接信息，注意安全保密' }}
            </a-descriptions-item>
          </a-descriptions>
          
          <!-- 当前连接的MQTT主题信息 -->
          <div v-if="getCurrentTopics()" class="topics-section">
            <div class="topics-title">
              <a-icon type="message" style="color: #1890ff; margin-right: 6px;"/>
              <span>主题列表</span>
            </div>
            
            <div class="topics-content">
              <div v-if="getCurrentTopics().thingTopics" class="topic-category">
                <a-icon type="appstore" style="color: #1890ff; margin-right: 4px;"/>
                <span class="category-title">物模型主题</span>
              </div>
              <div v-if="getCurrentTopics().thingTopics" class="topic-list">
                <div class="topic-item">
                  <span class="topic-label">属性上报：</span>
                  <span class="topic-value">{{ getCurrentTopics().thingTopics.propertyUp }}</span>
                  <span class="topic-note">（设备发布）</span>
                </div>
                <div class="topic-item">
                  <span class="topic-label">事件上报：</span>
                  <span class="topic-value">{{ getCurrentTopics().thingTopics.eventUp }}</span>
                  <span class="topic-note">（设备发布）</span>
                </div>
                <div class="topic-item">
                  <span class="topic-label">指令发布：</span>
                  <span class="topic-value">{{ getCurrentTopics().thingTopics.commandDown }}</span>
                  <span class="topic-note">（设备订阅）</span>
                </div>
              </div>
              
              <div v-if="getCurrentTopics().passthroughTopics" class="topic-category">
                <a-icon type="swap" style="color: #fa8c16; margin-right: 4px;"/>
                <span class="category-title">透传主题</span>
              </div>
              <div v-if="getCurrentTopics().passthroughTopics" class="topic-list">
                <div class="topic-item">
                  <span class="topic-label">数据上报：</span>
                  <span class="topic-value">{{ getCurrentTopics().passthroughTopics.dataUp }}</span>
                  <span class="topic-note">（设备发布）</span>
                </div>
                <div class="topic-item">
                  <span class="topic-label">指令发布：</span>
                  <span class="topic-value">{{ getCurrentTopics().passthroughTopics.commandDown }}</span>
                  <span class="topic-note">（设备订阅）</span>
                </div>
              </div>
            </div>
            
            <!-- 主题访问安全提示 -->
            <div class="topic-security-notice">
              <a-alert
                message="主题使用说明"
                type="info"
                show-icon
                style="margin-top: 12px"
              >
                <template slot="description">
                  <div class="topic-usage-guide">
                    <p>本项目使用EMQX作为MQTT Broker，设置了主题访问控制，仅允许访问上方列出的主题。</p>
                    <p><strong>设备使用方式：</strong></p>
                    <ol>
                      <li>设备需要订阅指令发布主题（如 <code>$thing/down/${productKey}/${deviceId}</code>）来接收平台下发的指令</li>
                      <li>设备可以发布数据到上报主题（如 <code>$thing/up/property/${productKey}/${deviceId}</code>）来上报数据</li>
                      <li>如果越权访问其他主题，设备会<code>踢出</code>连接，设置了自动重连可能会出现不断尝试重连的情况</li>
                    </ol>
                  </div>
                </template>
              </a-alert>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 加载中状态 -->
      <div v-else class="loading-connection">
        <a-spin size="large">
          <div class="loading-text">正在加载连接信息...</div>
        </a-spin>
      </div>
    </div>


    <!-- MQTT密码弹窗 -->
    <a-modal
      :title="getPasswordModalTitle()"
      :visible="passwordModalVisible"
      @cancel="closePasswordModal"
      :footer="null"
      width="600px"
    >
      <div v-if="passwordInfo" class="password-info">
        <!-- 根据当前连接模式显示对应的密码信息 -->
        <div v-if="currentConnectionMode === 'builtin'" class="password-section">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="用户名">
              {{ passwordInfo.builtin.username }}
            </a-descriptions-item>
            <a-descriptions-item label="密码">
              <a-input-password :value="passwordInfo.builtin.password" readonly />
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 自建接入MQTT密码 -->
        <div v-else-if="currentConnectionMode === 'network' && passwordInfo.network.enabled" class="password-section">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="用户名">
              {{ passwordInfo.network.username }}
            </a-descriptions-item>
            <a-descriptions-item label="密码">
              <a-input-password :value="passwordInfo.network.password" readonly />
            </a-descriptions-item>
          </a-descriptions>
        </div>
        
        <!-- 自建接入未启用时的提示 -->
        <div v-else class="password-section">
          <a-alert
            :message="passwordInfo.network.message || '当前未配置自建接入'"
            type="info"
            show-icon
          />
        </div>
      </div>
    </a-modal>

    <!-- 自建接入管理弹窗 -->
    <a-modal
      title="自建接入管理"
      :visible="networkManagementModalVisible"
      @cancel="closeNetworkManagementModal"
      :footer="null"
      width="900px"
      :destroyOnClose="true"
    >
      <div class="network-management-modal">
        <!-- 当前绑定状态 -->
        <div class="current-binding-status">
          <div class="status-header">
            <a-icon type="info-circle" style="color: #1890ff; margin-right: 8px;"/>
            <span>当前绑定状态</span>
          </div>
          <div v-if="product.networkUnionId" class="bound-status">
            <a-alert
              message="已配置自建接入"
              :description="`当前产品已配置自建接入，接入标识：${product.networkUnionId}`"
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
                <a-descriptions-item label="运行状态">
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
              message="未配置自建接入"
              description="当前产品未配置自建接入，您可以选择下方已配置完整且启动状态的接入组件进行绑定"
              type="info"
              show-icon
            />
          </div>
        </div>

        <!-- 接入组件列表 -->
        <div class="component-selection">
          <div class="selection-header">
            <h4>可用{{ getPlatformTitle() }}接入组件（已配置且启动）</h4>
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
                description="暂无可用的接入组件（需要配置完整且启动状态）"
                :image="emptyImage"
              >
                <a-button type="primary" @click="goToCreate">
                  创建接入组件
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
                <!-- MQTT类型标识 - 右上角 -->
                <div class="mqtt-type-badge">
                  <span v-if="item.type === 'MQTT_SERVER'" class="mqtt-badge-server">服务端</span>
                  <span v-if="item.type === 'MQTT_CLIENT'" class="mqtt-badge-client">客户端</span>
                </div>
                
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
                    Broker：
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
                    状态：
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
                       v-hasPermi="['network:mqtt:edit']">
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
import {updateProductNetworkUnionId, getProductConnectInfo, getProductMqttPassword} from '@/api/system/dev/product'
import {parseTime} from '@/utils/ruoyi'

export default {
  name: 'NetworkComponentBind',
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
      passwordInfo: null,
      showPasswordModal: false,
      passwordModalVisible: false,
      // 当前连接模式：'builtin' 或 'network'
      currentConnectionMode: 'builtin',
      // 网络组件管理弹窗
      networkManagementModalVisible: false
    }
  },
  watch: {
    product: {
      handler(newProduct) {
        if (newProduct.networkUnionId) {
          // 如果已绑定，加载已绑定组件信息
          this.loadBoundComponent()
        } else {
          // 如果未绑定，清空已绑定组件并加载组件列表
          this.boundComponent = null
          this.loadComponentList()
        }
        // 加载连接信息
        this.loadConnectionInfo()
      },
      immediate: true
    }
  },
  created() {
    // 如果已绑定，只加载已绑定组件信息
    if (this.product.networkUnionId) {
      this.loadBoundComponent()
    } else {
      // 如果未绑定，加载组件列表
      this.loadComponentList()
    }
  },
  methods: {
    parseTime,
    /** 获取平台标题 */
    getPlatformTitle() {
      if (this.product.thirdPlatform === 'tcp') {
        return 'TCP'
      }
      if (this.product.thirdPlatform === 'mqtt') {
        return 'MQTT'
      }
      if (this.product.thirdPlatform === 'udp') {
        return 'UDP'
      }
      return '网络'
    },
    /** 获取组件图标 */
    getComponentIcon(type) {
      if (type.includes('TCP_CLIENT')) {
        return 'user'
      }
      if (type.includes('TCP_SERVER')) {
        return 'cloud-server'
      }
      if (type.includes('MQTT_CLIENT')) {
        return 'cloud'
      }
      if (type.includes('MQTT_SERVER')) {
        return 'cloud-server'
      }
      if (type.includes('UDP')) {
        return 'wifi'
      }
      return 'api'
    },
    /** 获取组件颜色 */
    getComponentColor(type) {
      if (type.includes('TCP_CLIENT')) {
        return '#1890ff'
      }
      if (type.includes('TCP_SERVER')) {
        return '#52c41a'
      }
      if (type.includes('MQTT_CLIENT')) {
        return '#fa8c16'
      }
      if (type.includes('MQTT_SERVER')) {
        return '#eb2f96'
      }
      if (type.includes('UDP')) {
        return '#722ed1'
      }
      return '#666'
    },
    /** 获取组件类型标签 */
    getComponentTypeLabel(type) {
      if (type === 'TCP_CLIENT') {
        return 'TCP客户端'
      }
      if (type === 'TCP_SERVER') {
        return 'TCP服务端'
      }
      if (type === 'MQTT_CLIENT') {
        return 'MQTT客户端'
      }
      if (type === 'MQTT_SERVER') {
        return 'MQTT服务端'
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
    /** 获取组件主机地址 */
    getComponentHost(component) {
      const host = this.getConfigValue(component, 'host')
      const port = this.getConfigValue(component, 'port')

      if (host) {
        if (port) {
          return `${host}:${port}`
        }
        return host
      }
      return null
    },
    /** 格式化时间 */
    formatTime(time) {
      if (!time) {
        return '-'
      }
      return this.parseTime(time)
    },
    /** 加载组件列表 */
    async loadComponentList() {
      this.loading = true
      try {
        const types = this.getComponentTypes()
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
    /** 获取组件类型 */
    getComponentTypes() {
      if (this.product.thirdPlatform === 'tcp') {
        return ['TCP_CLIENT', 'TCP_SERVER', 'UDP']
      }
      if (this.product.thirdPlatform === 'mqtt') {
        return ['MQTT_CLIENT', 'MQTT_SERVER']
      }
      if (this.product.thirdPlatform === 'udp') {
        return ['UDP']
      }
      return []
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
        // 刷新连接信息
        await this.loadConnectionInfo()
        // 关闭弹窗
        this.closeNetworkManagementModal()
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
        content: '确定要解绑当前网络组件吗？解绑后产品将无法使用网络通信功能。',
        onOk: async () => {
          this.unbinding = true
          try {
            await updateProductNetworkUnionId({
              id: this.product.id,
              networkUnionId: ''
            })
            this.$message.success('解绑成功')
            this.$emit('refresh')
            // 刷新连接信息
            await this.loadConnectionInfo()
            // 关闭弹窗
            this.closeNetworkManagementModal()
          } catch (error) {
            this.$message.error('解绑失败：' + (error.message || error))
          } finally {
            this.unbinding = false
          }
        }
      })
    },
    /** 查看组件详情 */
    viewComponentDetail() {
      if (!this.boundComponent) {
        return
      }

      const route = this.product.thirdPlatform === 'tcp'
        ? `/system/network/tcp/detail/${this.boundComponent.id}`
        : (this.product.thirdPlatform === 'udp'
          ? `/system/network/tcp/detail/${this.boundComponent.id}`
          : `/system/network/mqtt/detail/${this.boundComponent.id}`)

      this.$router.push(route)
    },
    /** 跳转到创建页面 */
    goToCreate() {
      const route = this.product.thirdPlatform === 'tcp'
        ? '/system/network/tcp'
        : (this.product.thirdPlatform === 'udp' ? '/system/network/tcp' : '/system/network/mqtt')

      this.$router.push(route)
    },
    /** 加载已绑定组件信息 */
    async loadBoundComponent() {
      if (!this.product.networkUnionId) {
        this.boundComponent = null
        return
      }

      this.loading = true
      try {
        // 直接查询已绑定的组件信息
        const response = await listNetwork({
          pageNum: 1,
          pageSize: 100,
          unionId: this.product.networkUnionId
        })

        if (response.rows && response.rows.length > 0) {
          this.boundComponent = response.rows[0]
        } else {
          // 如果没找到，尝试从组件列表中查找
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
          // 智能选择连接模式：如果绑定了网络组件且可用，优先使用网络组件
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
      
      // 如果产品绑定了网络组件且网络组件可用，优先使用网络组件
      if (this.product.networkUnionId && this.connectionInfo.network.enabled) {
        this.currentConnectionMode = 'network'
      } else {
        this.currentConnectionMode = 'builtin'
      }
    },
    /** 查看MQTT密码 */
    async showMqttPassword() {
      if (!this.product.productKey) {
        return
      }
      
      try {
        const response = await getProductMqttPassword(this.product.productKey)
        if (response.code === 200 || response.code === 0) {
          this.passwordInfo = response.data
          this.passwordModalVisible = true
        } else {
          this.$message.error('获取密码信息失败：' + response.msg)
        }
      } catch (error) {
        this.$message.error('获取密码信息失败：' + error.message)
      }
    },
    /** 关闭密码弹窗 */
    closePasswordModal() {
      this.passwordModalVisible = false
      this.passwordInfo = null
    },
    
    /** 连接模式切换 */
    onConnectionModeChange(e) {
      this.currentConnectionMode = e.target.value
    },
    
    /** 获取当前连接信息 */
    getCurrentConnectionInfo() {
      if (!this.connectionInfo) return {}
      
      if (this.currentConnectionMode === 'network' && this.connectionInfo.network.enabled) {
        return {
          connectionType: this.getComponentTypeLabel(this.connectionInfo.network.type),
          host: this.connectionInfo.network.host,
          port: this.connectionInfo.network.port,
          username: this.connectionInfo.network.username,
          password: this.connectionInfo.network.password,
          description: `使用网络组件：${this.connectionInfo.network.name}`
        }
      } else {
        return {
          connectionType: this.connectionInfo.builtin.connectionType,
          host: this.connectionInfo.builtin.host,
          port: this.connectionInfo.builtin.port,
          username: this.connectionInfo.builtin.username,
          password: this.connectionInfo.builtin.password,
          description: this.connectionInfo.builtin.description
        }
      }
    },
    
    /** 获取当前主题信息 */
    getCurrentTopics() {
      if (!this.connectionInfo) return null
      
      if (this.currentConnectionMode === 'network' && this.connectionInfo.network.enabled) {
        return this.connectionInfo.network.subscribeTopics
      } else {
        return this.connectionInfo.builtin.topics
      }
    },
    
    /** 获取当前连接图标 */
    getCurrentConnectionIcon() {
      if (this.currentConnectionMode === 'network') {
        return 'api'
      } else {
        return 'home'
      }
    },
    
    /** 获取当前连接颜色 */
    getCurrentConnectionColor() {
      if (this.currentConnectionMode === 'network') {
        return '#1890ff'
      } else {
        return '#52c41a'
      }
    },
    
    /** 获取当前连接标题 */
    getCurrentConnectionTitle() {
      if (this.currentConnectionMode === 'network' && this.connectionInfo && this.connectionInfo.network.enabled) {
        return `自建接入 (${this.connectionInfo.network.name})`
      } else {
        return '平台直连'
      }
    },
    
    /** 显示网络组件管理弹窗 */
    showNetworkManagementModal() {
      this.networkManagementModalVisible = true
      // 如果还没有加载组件列表，则加载
      if (!this.componentList || this.componentList.length === 0) {
        this.loadComponentList()
      }
    },
    
    /** 关闭网络组件管理弹窗 */
    closeNetworkManagementModal() {
      this.networkManagementModalVisible = false
      this.selectedComponent = null
    },
    
    /** 获取密码弹窗标题 */
    getPasswordModalTitle() {
      if (this.currentConnectionMode === 'network') {
        return '自建接入MQTT密码'
      } else {
        return '平台直连MQTT密码'
      }
    },
    
    /** 过滤已配置且启动的组件列表 */
    get filteredComponentList() {
      if (!this.componentList || !Array.isArray(this.componentList)) {
        return []
      }
      return this.componentList.filter(item => this.isMqttConfigured(item) && item.running)
    },
    
    /** 检查MQTT配置是否完整 */
    isMqttConfigured(item) {
      if (!item.configuration) return false
      try {
        const config = typeof item.configuration === 'string' 
          ? JSON.parse(item.configuration) 
          : item.configuration
        return !!(config.host && config.username && config.password)
      } catch (e) {
        return false
      }
    },
    
    /** 获取状态文本 */
    getStatusText(item) {
      if (item.running) {
        return '运行中'
      } else if (this.isMqttConfigured(item)) {
        return '已停止'
      } else {
        return '未配置'
      }
    },
    
    /** 获取状态样式类 */
    getStatusClass(item) {
      if (item.running) {
        return 'status-running'
      } else if (this.isMqttConfigured(item)) {
        return 'status-stopped'
      } else {
        return 'status-unconfigured'
      }
    },
    
    /** 查看组件详情 */
    viewComponentDetail(item) {
      if (!item) {
        return
      }

      const route = this.product.thirdPlatform === 'tcp'
        ? `/system/network/tcp/detail/${item.id}`
        : (this.product.thirdPlatform === 'udp'
          ? `/system/network/tcp/detail/${item.id}`
          : `/system/network/mqtt/detail/${item.id}`)

      this.$router.push(route)
    }
  }
}
</script>

<style lang="less" scoped>
.network-component-bind {
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
        
        .topics-section {
          margin-top: 16px;
          padding: 16px;
          background: #ffffff;
          border-radius: 6px;
          border: 1px solid #e8e8e8;
          
          .topics-title {
            display: flex;
            align-items: center;
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 12px;
          }
          
          .topic-security-notice {
            .ant-alert {
              border-radius: 6px;
              border-left: 3px solid #1890ff;
              background: #f6f8ff;
              
              .ant-alert-icon {
                color: #1890ff;
              }
              
              .ant-alert-message {
                font-weight: 500;
                color: #1890ff;
                font-size: 13px;
              }
              
              .ant-alert-description {
                color: #666;
                line-height: 1.4;
                font-size: 12px;
              }
            }
          }
          
          .topics-content {
            .topic-category {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
              margin-top: 12px;
              
              .category-title {
                font-size: 13px;
                font-weight: 500;
                color: #333;
              }
            }
            
            .topic-list {
              .topic-item {
                display: flex;
                align-items: flex-start;
                margin-bottom: 6px;
                font-size: 12px;
                
                .topic-label {
                  color: #666;
                  min-width: 80px;
                  margin-right: 8px;
                  flex-shrink: 0;
                }
                
                .topic-value {
                  color: #1890ff;
                  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                  background: #f8f9fa;
                  padding: 2px 6px;
                  border-radius: 3px;
                  border: 1px solid #e8e8e8;
                  word-break: break-all;
                  flex: 1;
                  font-size: 11px;
                  line-height: 1.4;
                  margin-right: 8px;
                }
                
                .topic-note {
                  color: #52c41a;
                  font-size: 11px;
                  font-weight: 500;
                  flex-shrink: 0;
                  background: #f6ffed;
                  padding: 2px 6px;
                  border-radius: 3px;
                  border: 1px solid #b7eb8f;
                }
              }
            }
          }
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

  .network-binding-section {
    .state-header {
      margin-bottom: 20px;
    }
  }

  .component-list {
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
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
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 16px;

      .component-card {
        border-radius: 8px;
        transition: all 0.2s ease;
        cursor: pointer;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.selected {
          border-color: #1890ff;
          box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .component-type {
            display: flex;
            align-items: center;
            gap: 8px;

            .type-label {
              font-size: 12px;
              font-weight: 500;
              color: #666;
            }
          }
        }

        .card-body {
          .component-name {
            font-size: 16px;
            font-weight: 600;
            color: #1a202c;
            margin-bottom: 8px;
          }

          .component-info {
            .info-item {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 13px;
              color: #666;
              margin-bottom: 4px;

              .anticon {
                font-size: 12px;
              }
            }
          }
        }

        .card-actions {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid #f0f0f0;
        }
      }
    }
  }

  .bound-component {
    .loading-bound {
      padding: 40px 0;
      text-align: center;
    }

    .bound-card {
      border-radius: 8px;
      border: 2px solid #52c41a;
      background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .component-type {
          display: flex;
          align-items: center;
          gap: 8px;

          .type-label {
            font-size: 14px;
            font-weight: 600;
            color: #52c41a;
          }
        }
      }

      .card-body {
        .component-name {
          font-size: 18px;
          font-weight: 600;
          color: #1a202c;
          margin-bottom: 12px;
        }

        .component-info {
          .info-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #666;
            margin-bottom: 6px;

            .anticon {
              font-size: 14px;
            }
          }
        }

        /* 主题格式样式 */

        .topic-section {
          margin-top: 16px;
          padding-top: 16px;
          border-top: 1px solid #f0f0f0;

          .topic-header {
            display: flex;
            align-items: center;
            margin-bottom: 12px;

            .topic-title {
              font-size: 14px;
              font-weight: 600;
              color: #333;
            }
          }

          .topic-content {
            .topic-category {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
              margin-top: 12px;

              .category-title {
                font-size: 13px;
                font-weight: 500;
                color: #333;
              }
            }

            .topic-list {
              .topic-item {
                display: flex;
                align-items: flex-start;
                margin-bottom: 6px;
                font-size: 12px;

                .topic-label {
                  color: #666;
                  min-width: 80px;
                  margin-right: 8px;
                  flex-shrink: 0;
                }

                .topic-value {
                  color: #1890ff;
                  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                  background: #f8f9fa;
                  padding: 2px 6px;
                  border-radius: 3px;
                  border: 1px solid #e8e8e8;
                  word-break: break-all;
                  flex: 1;
                  font-size: 11px;
                  line-height: 1.4;
                }
              }
            }
          }
        }
      }

      .card-actions {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #f0f0f0;
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

// 响应式设计
@media (max-width: 768px) {
  .network-component-bind {
    .component-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .component-card {
      .card-body {
        .component-name {
          font-size: 14px;
        }

        .component-info {
          .info-item {
            font-size: 12px;
          }
        }
      }
    }
  }
}

// 密码弹窗样式
.password-info {
  .password-section {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .section-title {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }
    
    .ant-descriptions {
      background: #fafafa;
      border-radius: 6px;
    }
    
    .ant-alert {
      background: #fafafa;
      border-radius: 6px;
    }
  }
}

// 网络管理弹窗样式
.network-management-modal {
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

// MQTT类型标识样式
.mqtt-type-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1;
}

.mqtt-badge-server,
.mqtt-badge-client {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  color: #666666;
  line-height: 1.2;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.mqtt-badge-server {
  color: #8c8c8c;
  background: #fafafa;
}

.mqtt-badge-client {
  color: #8c8c8c;
  background: #fafafa;
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

// 主题使用说明样式
.topic-usage-guide {
  p {
    margin: 0 0 8px 0;
    line-height: 1.5;
  }
  
  ol {
    margin: 8px 0 0 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 4px;
      line-height: 1.5;
      
      code {
        background: #f5f5f5;
        padding: 2px 4px;
        border-radius: 3px;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        font-size: 12px;
        color: #d73a49;
      }
    }
  }
}
</style> 