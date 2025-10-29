<template>
  <div class="product-details-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <a-button
          type="text"
          icon="left"
          @click="back()"
          class="back-btn"
        />
      </div>
    </div>

    <!-- 自定义标签页 -->
    <div class="custom-tabs-container">
      <a-spin :spinning="loading" tip="Loading...">
        <div class="custom-tabs-nav">
          <div
            class="custom-tab-item"
            :class="{ active: activeKey === '1' }"
            @click="activeKey = '1'"
          >
            产品信息
          </div>
          <div
            class="custom-tab-item"
            :class="{ active: activeKey === '2' }"
            @click="activeKey = '2'"
          >
            物模型
          </div>
          <div
            class="custom-tab-item"
            :class="{ active: activeKey === '3' }"
            @click="activeKey = '3'"
          >
            设备管理
          </div>
          <div
            v-if="productDetails.creatorId === $store.state.user.name"
            class="custom-tab-item"
            :class="{ active: activeKey === '4' }"
            @click="activeKey = '4'"
          >
            协议管理
          </div>
          <div
            v-if="['tcp','udp','mqtt'].includes(productDetails.thirdPlatform) && productDetails.creatorId === $store.state.user.name && productDetails.deviceNode !== 'GATEWAY_SUB_DEVICE'"
            class="custom-tab-item"
            :class="{ active: activeKey === '5' }"
            @click="activeKey = '5'"
          >
            连接信息
          </div>
          <!-- <div
            v-if="['sniTcp', 'mqtt'].includes(productDetails.thirdPlatform) &&  productDetails.deviceNode !== 'GATEWAY_SUB_DEVICE' && productDetails.creatorId === $store.state.user.name"
            class="custom-tab-item"
            :class="{ active: activeKey === '6' }"
            @click="activeKey = '6'"
          >
            证书管理
          </div> -->
        </div>

        <div class="custom-tab-content">
          <!-- 产品信息 -->
          <div v-show="activeKey === '1'" class="tab-pane">
            <div class="product-info-content">
              <!-- 产品概览 -->
              <div class="product-overview">
                <!-- 产品信息布局 -->
                <div class="product-layout">
                  <!-- 左侧：产品详情 -->
                  <div class="product-left-section">
                    <div class="product-header-section">
                      <h1 class="product-name">{{ productDetails.name || '产品名称' }}</h1>
                      <div class="product-meta-tags">
                        <a-tag color="blue">{{ productDetails.thirdPlatform || '-' }}</a-tag>
                        <a-tag color="cyan">{{ getDeviceNodeText(productDetails.deviceNode) || '-' }}</a-tag>
                        <a-tag v-if="productDetails.transportProtocol" color="purple">{{ productDetails.transportProtocol }}</a-tag>
                      </div>
                      <div class="product-key-row">
                        <span class="key-label">ProductKey</span>
                        <code class="key-value">{{ productDetails.productKey || '-' }}</code>
                        <a-tooltip title="复制">
                          <a-button
                            type="text"
                            size="small"
                            icon="copy"
                            @click="copyToClipboard(productDetails.productKey)"
                            class="copy-btn"
                          />
                        </a-tooltip>
                      </div>
                    </div>

                    <!-- 产品详细信息 -->
                    <div class="product-details-grid">
                      <div class="detail-item">
                        <span class="detail-label">产品ID</span>
                        <span class="detail-value">{{ productDetails.productId || '-' }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">厂商简称</span>
                        <span class="detail-value">{{ productDetails.companyNo || '-' }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">所属分类</span>
                        <span class="detail-value">{{ productSortName || '-' }}</span>
                      </div>
                      <div class="detail-item"
                           v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin">
                        <span class="detail-label">{{ $t('app.creator') }}</span>
                        <span class="detail-value">{{ productDetails.creatorId || '-' }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">接入方式</span>
                        <span class="detail-value">{{ productDetails.thirdPlatform || '-' }}</span>
                      </div>
                      <div class="detail-item"
                           v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin">
                        <span class="detail-label">数据协议</span>
                        <span class="detail-value">{{ productDetails.transportProtocol || '-' }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">设备节点</span>
                        <span class="detail-value">{{ getDeviceNodeText(productDetails.deviceNode) || '-' }}</span>
                      </div>
                      <div class="detail-item" v-if="productDetails.storePolicy">
                        <span class="detail-label">存储类型</span>
                        <span class="detail-value">
                          <!-- 编辑模式：显示下拉选择 -->
                          <template v-if="editStorePolicyMode">
                            <a-select
                              v-model="productDetails.storePolicy"
                              placeholder="请选择存储策略"
                              style="width: 150px"
                              @change="handleStorePolicyChange"
                            >
                              <a-select-option
                                v-for="item in storePolicyOptions"
                                :key="item.value"
                                :value="item.value"
                              >
                                {{ item.label }}
                              </a-select-option>
                            </a-select>
                            <a-button
                              type="text"
                              size="small"
                              icon="close"
                              @click="cancelEditStorePolicy"
                              style="margin-left: 8px"
                            />
                          </template>
                          <!-- 查看模式：显示文本 + 编辑按钮 -->
                          <template v-else>
                            <span>{{ getStorePolicyLabel(productDetails.storePolicy) }}</span>
                            <a-button
                              v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin"
                              type="text"
                              size="small"
                              icon="edit"
                              @click="startEditStorePolicy"
                              style="margin-left: 8px"
                            />
                          </template>
                        </span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">产品状态</span>
                        <span class="detail-value">
                          <a-badge :status="productDetails.state === 0 ? 'success' : 'default'" />
                          {{ productDetails.state === 0 ? '启用' : '禁用' }}
                        </span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">设备实例</span>
                        <span class="detail-value">{{ productDetails.instance || 0 }} 个</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">创建时间</span>
                        <span class="detail-value">{{ formatTime(productDetails.createTime) || '-' }}</span>
                      </div>
                      <div class="detail-item" v-if="productDetails.updateTime">
                        <span class="detail-label">更新时间</span>
                        <span class="detail-value">{{ formatTime(productDetails.updateTime) || '-' }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 右侧：产品图片 -->
                  <div class="product-right-section">
                    <div class="product-image-section">
                      <div class="image-wrapper" @click="openImagePreview" :class="{ clickable: !!productImageUrl }">
                        <img v-if="productImageUrl" :src="productImageUrl" alt="产品图片" class="product-image"/>
                        <div v-else class="image-placeholder">
                          <a-icon type="picture" class="placeholder-icon"/>
                          <span class="placeholder-text">暂无图片</span>
                        </div>
                      </div>
                      <div class="image-actions">
                        <a v-if="productImageUrl" @click.prevent="openImagePreview" class="action-link">
                          <a-icon type="eye"/>
                          {{ $t('app.view') }}
                        </a>
                        <a-upload
                          v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin"
                          :customRequest="handleCustomImageUpload"
                          :show-upload-list="false"
                          :before-upload="beforeUploadImage"
                        >
                          <a class="action-link">
                            <a-icon type="upload"/>
                            {{ productImageUrl ? '更换' : '上传' }}
                          </a>
                        </a-upload>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 产品描述 -->
                <div class="product-description" v-if="productDetails.describe">
                  <span class="description-label">产品描述</span>
                  <p class="description-text">{{ productDetails.describe }}</p>
                </div>
              </div>

              <!-- 设备标签卡片 -->
              <div class="info-card"  v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin">
                <div class="card-header">
                  <div class="card-title">
                    <a-icon type="tags"/>
                    <span>设备标签</span>
                  </div>
                  <a-button
                    type="link"
                    icon="edit"
                    @click="editTags"
                    class="edit-btn"
                  >
                    {{ editTagStatus ? '取消' : '编辑' }}
                  </a-button>
                </div>
                <div class="card-content">
                  <div v-show="!editTagStatus" class="tags-display">
                    <a-tag
                      v-for="(item, index) in productDetails.tagsName"
                      :key="index"
                      class="device-tag"
                    >
                      {{ item }}
                    </a-tag>
                    <span v-if="!productDetails.tagsName || productDetails.tagsName.length === 0"
                          class="no-tags">
                      暂无标签
                    </span>
                  </div>
                  <div v-show="editTagStatus" class="tags-edit">
                    <a-select
                      mode="multiple"
                      placeholder="请选择设备标签"
                      v-model="tagList"
                      allow-clear
                      @change="handleTagChange"
                      class="tag-select"
                    >
                      <a-select-option v-for="item in productTags" :value="item.id" :key="item.id">
                        {{ item.name }}
                      </a-select-option>
                    </a-select>
                    <a-button
                      type="primary"
                      @click="confirmTags"
                      class="save-btn"
                    >
                      保存
                    </a-button>
                  </div>
                </div>
              </div>

              <!-- 网关产品卡片 -->
              <div class="info-card gateway-card" v-if="productDetails.deviceNode === 'GATEWAY_SUB_DEVICE'">
                <div class="card-header">
                  <div class="card-title">
                    <a-icon type="gateway"/>
                    <span>关联网关</span>
                  </div>
                  <a-button
                    v-if="!selectedGateway"
                    type="primary"
                    size="small"
                    icon="plus"
                    @click="openGatewayModal"
                    v-hasPermi="['system:product:edit']"
                  >
                    添加网关
                  </a-button>
                  <a-button
                    v-else
                    type="default"
                    size="small"
                    icon="swap"
                    @click="openGatewayModal"
                    v-hasPermi="['system:product:edit']"
                  >
                    更换网关
                  </a-button>
                </div>
                <div class="card-content">
                  <div v-if="selectedGateway" class="gateway-selected">
                    <div class="gateway-info-box">
                      <!-- 网关图片或默认图标 -->
                      <div class="gateway-image">
                        <img v-if="gatewayImageUrl" :src="gatewayImageUrl" alt="网关图片" class="gateway-photo"/>
                        <a-icon v-else type="gateway" class="gateway-icon"/>
                      </div>
                      <div class="gateway-details">
                        <div class="gateway-name">{{ selectedGateway.name }}</div>
                        <div class="gateway-key">ProductKey: {{ selectedGateway.productKey }}</div>
                      </div>
                      <a-button
                        type="link"
                        size="small"
                        icon="close"
                        @click="removeGateway"
                        v-hasPermi="['system:product:edit']"
                        class="remove-btn"
                      >
                        移除
                      </a-button>
                    </div>
                  </div>
                  <div v-else class="gateway-empty">
                    <a-icon type="disconnect" class="empty-icon"/>
                    <p class="empty-text">暂未关联网关产品</p>
                    <p class="empty-hint">子设备需要通过网关连接到平台</p>
                  </div>
                  <SelectGateway ref="selectGateway" @add="addGateway" :selectedGateway="selectedGateway"/>
                </div>
              </div>

              <!-- 配置信息区域 -->
              <template
                v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin">
                <!-- 使用新的配置编辑器组件 -->
                <ConfigurationEditor
                  v-if="publicConfigurationDicts.length > 0 || configurationDicts.length > 0"
                  :product-id="productId"
                  :public-configuration-array="publicConfigurationArray"
                  :configuration-array="configurationArray"
                  :public-configuration-dicts="publicConfigurationDicts"
                  :configuration-dicts="configurationDicts"
                  @config-updated="getProductDeatils"
                />

                <!-- 存储策略 -->
                <!-- <div class="info-card" v-if="storePolicyCfgArray.length > 0"> -->
                <div class="info-card">
                  <div class="card-header">
                    <div class="card-title">
                      <a-icon type="database"/>
                      <span>设备日志存储类型</span>
                    </div>
                    <a-button
                      type="link"
                      icon="edit"
                      @click="editConfig('storageConfiguration')"
                      v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin"
                      v-hasPermi="['system:product:edit']"
                      class="edit-btn"
                    >
                      编辑
                    </a-button>
                  </div>
                  <div class="card-content">
                    <a-table
                      :data-source="storePolicyCfgArray"
                      :columns="storageColumns"
                      :pagination="false"
                      size="middle"
                      class="storage-table"
                    >
                      <span slot="key" slot-scope="text, record">
                        <div class="storage-identifier">
                          <a-icon type="tag"/>
                          <span>{{ record.key }}</span>
                        </div>
                      </span>
                      <span slot="value" slot-scope="text, record">
                        <div class="storage-limit">
                          <span class="limit-value">{{ record.value }}</span>
                          <span class="limit-unit">条</span>
                        </div>
                      </span>
                      <span slot="type" slot-scope="text, record">
                        <a-tag :color="record.type === 'properties' ? 'blue' : 'green'">
                          {{ record.type === 'properties' ? '属性' : '事件' }}
                        </a-tag>
                      </span>
                    </a-table>
                  </div>
                </div>

                <div class="info-card">
                  <div class="card-header">
                    <div class="card-title">
                      <a-icon type="setting"/>
                      <span>第三方平台参数</span>
                    </div>
                    <a-button
                      type="link"
                      icon="edit"
                      @click="editConfig('otherConfiguration')"
                      v-if="productDetails.creatorId === $store.state.user.name || $store.state.user.currentAdmin"
                      v-hasPermi="['system:product:edit']"
                      class="edit-btn"
                    >
                      {{ otherConfiguration.length > 0 ? '编辑' : '配置' }}
                    </a-button>
                  </div>
                  <div class="card-content">
                    <div v-if="otherConfiguration.length > 0" class="platform-params">
                      <div
                        v-for="item in otherConfiguration"
                        :key="item.id"
                        class="param-item"
                      >
                        <div class="param-header">
                          <div class="param-info">
                            <span class="param-key">{{ item.id || "参数键" }}</span>
                            <span class="param-label">{{ item.name || "参数名称" }}</span>
                          </div>
                          <a-tag color="blue" class="param-tag">{{ item.id }}</a-tag>
                        </div>
                        <div class="param-description">
                          <a-icon type="info-circle" class="desc-icon"/>
                          {{ item.description || "暂无描述" }}
                        </div>
                      </div>
                    </div>
                    <div v-else class="no-platform-params">
                      <div class="empty-state">
                        <a-icon type="setting" class="empty-icon"/>
                        <p class="empty-text">暂无第三方平台参数</p>
                        <p class="empty-desc">配置第三方平台所需的认证参数，如安全码、API密钥等</p>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <!-- 物模型 -->
          <div v-show="activeKey === '2'" class="tab-pane">
            <!-- 物模型组件 -->
            <div class="metadata-wrapper">
              <metadata
                ref="metadata"
                :product-id="productId"
                :creator-id="productDetails.creatorId"
                :product-key="productDetails.productKey"
                @openMetadataDetail="openMetadataDetail"
                @openImportMetadata="openImportMetadata"/>
            </div>
          </div>

          <!-- 设备管理 -->
          <div v-show="activeKey === '3'" class="tab-pane">
            <!-- <div class="simple-header">
              <h3>设备管理</h3>
              <p>管理该产品下的所有设备实例</p>
            </div> -->
            <product-instance
              v-if="activeKey === '3' && productDetails.productKey"
              ref="instance"
              :product-key="productDetails.productKey"
            />
          </div>

          <!-- 协议管理 -->
          <div v-show="activeKey === '4'" class="tab-pane"
               v-if="productDetails.creatorId === $store.state.user.name">
            <!-- <div class="simple-header">
              <h3>协议管理</h3>
              <p>管理产品的通信协议和数据解析规则</p>
            </div> -->
            <product-protocol
              ref="protocol"
              :product-key="productDetails.productKey"
              :product-name="productDetails.name"
              :user-name="productDetails.creatorId"/>
          </div>

          <!-- 连接信息 -->
          <div v-show="activeKey === '5'" class="tab-pane"
               v-if="['tcp','udp', 'mqtt'].includes(productDetails.thirdPlatform)">
            <!-- TCP/UDP连接信息 -->
            <tcp-connection-info
              v-if="['tcp', 'udp'].includes(productDetails.thirdPlatform)"
              :product="productDetails"
              @refresh="getProductDeatils"
            />
            <!-- MQTT连接信息 -->
            <network-component-bind
              v-else
              :product="productDetails"
              @refresh="getProductDeatils"
            />
          </div>
          <!-- 证书管理 -->
          <div v-show="activeKey === '6'" class="tab-pane"
               v-if="['sniTcp', 'mqtt'].includes(productDetails.thirdPlatform)">
            <!-- <div class="simple-header">
              <h3>证书绑定</h3>
              <p>管理产品证书的绑定与信息</p>
            </div> -->
            <CertificateBind
              :key="getSslKeyFromConfig(productDetails.configuration) || 'no-cert'"
              :productKey="productDetails.productKey"
              :bindCertKey="getSslKeyFromConfig(productDetails.configuration)"
              @refresh="getProductDeatils"
            />
          </div>
        </div>
      </a-spin>
    </div>

    <!-- 修改产品信息 -->
    <create-form ref="createForm" @ok="getList"/>
    <!-- 修改产品存储策略配置信息 -->
    <storage-configuration-form ref="storageConfigurationForm" @ok="storageConfigOk"/>
    <!-- 修改产品配置信息 -->
    <configuration-form ref="configurationForm" :dicts="dicts" @ok="getProductDeatils"/>
    <!-- 修改产品其他配置信息 -->
    <other-config-form ref="otherConfigurationForm" @ok="getProductDeatils"/>
    <!-- 物模型详细信息 -->
    <metadata-show
      ref="metadataShow"
      :product-id="productId"
      :show="metadataShow"
      :gid="productDetails.configuration"
      :type="metadataShowType"
      @close="closeMetadataDetail"/>
    <!-- 图片预览 -->
    <a-modal :visible="imagePreviewVisible" :footer="null" @cancel="imagePreviewVisible=false" :width="520">
      <img v-if="productImageUrl" :src="productImageUrl" alt="产品图片" style="width: 100%; border-radius: 8px;"/>
    </a-modal>
    <!-- 物模型导入 -->
    <import-metadata
      ref="importMetadata"
      :product-id="productId"
      :show="importMetadataShow"
      @close="closeImportMetadata"
      @ok="successUpdateMetadata"/>
  </div>
</template>
<style scoped lang="less">
.product-details-container {
  background: #ffffff;
  padding: 0;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e8eaed;

  ÷
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .back-btn {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;

      &:hover {
        background: #e2e8f0;
        border-color: #1966ff;
        color: #1966ff;
        transform: scale(1.05);
      }
    }
  }
}

/* 自定义标签页样式 */
.custom-tabs-container {
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

.custom-tabs-nav {
  display: flex;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.custom-tabs-nav::-webkit-scrollbar {
  display: none;
}

.custom-tab-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  min-width: fit-content;
  border-right: 1px solid #e8e8e8;
  background: #fafafa;
}

.custom-tab-item:last-child {
  border-right: none;
}

.custom-tab-item:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.custom-tab-item.active {
  background: #ffffff;
  color: #1890ff;
  border-bottom: 2px solid #1890ff;
  margin-bottom: -1px;
}

.custom-tab-content {
  min-height: 500px;
  background: #fff;
}

.tab-pane {
  padding: 10px;
}

/* 产品信息内容区域 */
.product-info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 产品概览 - 全新设计 */
.product-overview {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  // margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  // border: 1px solid #f1f5f9;

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .product-title {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
        color: #1a202c;
      }

      .product-key {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 4px;

        .key-label {
          font-size: 14px;
          color: #64748b;
        }

        .key-value {
          font-family: 'Monaco', 'Menlo', monospace;
          font-size: 16px;
          font-weight: 600;
          color: #1a202c;
          word-break: break-all;
          flex: 1;
        }

        .copy-btn {
          background: #f8fafc;
          border: 1px solid #e2e8f0;
          color: #64748b;
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.2s ease;

          &:hover {
            background: #1966ff;
            border-color: #1966ff;
            color: white;
            transform: scale(1.05);
          }
        }
      }

      .product-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .ant-tag {
          border-radius: 6px;
          font-weight: 500;
          font-size: 12px;
          padding: 2px 8px;
          border: none;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }

  .overview-layout {
    display: grid;
    grid-template-columns: 1fr 260px;
    gap: 16px;
  }

  .overview-left {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 8px 16px;
  }

  .overview-right {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding: 16px;
    border: 1px dashed #e2e8f0;
    border-radius: 12px;
    background: #fff;
    min-height: 320px;
  }

  .product-image-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    width: 100%;
    max-width: 260px;
  }

  .image-container {
    width: 220px;
    height: 220px;
    border-radius: 12px;
    overflow: hidden;
    border: 2px solid #f1f5f9;
    background: #fafbfc;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    cursor: pointer;

    &.clickable:hover {
      border-color: #3b82f6;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
      transform: translateY(-2px);
    }

    .product-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: #94a3b8;

      .placeholder-icon {
        font-size: 48px;
      }

      .placeholder-text {
        font-size: 14px;
        color: #64748b;
      }
    }
  }

  .image-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    width: 100%;
  }

  .image-actions {
    display: flex;
    gap: 12px;
    align-items: center;

    .action-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: 6px;
      font-size: 14px;
      color: #3b82f6;
      background: #eff6ff;
      border: 1px solid #dbeafe;
      transition: all 0.2s ease;
      text-decoration: none;

      &:hover {
        background: #dbeafe;
        color: #1d4ed8;
        transform: translateY(-1px);
      }
    }
  }

  .image-tips {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #64748b;
    text-align: center;
    line-height: 1.4;
  }

  .product-title {
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 12px 0;
      line-height: 1.3;
    }

    .product-key {
      display: flex;
      align-items: center;
      gap: 8px;

      .key-label {
        font-size: 14px;
        color: #64748b;
        font-weight: 500;
      }

      .key-value {
        font-size: 14px;
        color: #1f2937;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        background: #f8fafc;
        padding: 4px 8px;
        border-radius: 4px;
        border: 1px solid #e2e8f0;
      }

      .copy-btn {
        color: #64748b;

        &:hover {
          color: #3b82f6;
        }
      }
    }
  }

  .product-info {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 12px 20px;
  }

  .overview-left .info-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 8px 0;

    &.full-width {
      grid-column: 1 / -1;
      flex-direction: column;
      align-items: flex-start;
      gap: 6px;
    }

    .label {
      font-size: 14px;
      color: #64748b;
      min-width: 80px;
      font-weight: 500;
      flex-shrink: 0;
    }

    .value {
      font-size: 15px;
      color: #1f2937;
      flex: 1;
      line-height: 1.4;

      &.description {
        color: #4b5563;
        line-height: 1.5;
      }
    }
  }
}

/* 产品布局 */
.product-layout {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

/* 左侧区域 */
.product-left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 产品头部区域 - 优化 */
.product-header-section {
  .product-name {
    font-size: 24px;
    font-weight: 600;
    color: #1a202c;
    margin: 0 0 12px 0;
    line-height: 1.3;
  }

  .product-meta-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    flex-wrap: wrap;

    .ant-tag {
      margin: 0;
      padding: 4px 12px;
      font-size: 13px;
      border-radius: 4px;
      font-weight: 500;
    }
  }

  .product-key-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;

    .key-label {
      font-size: 13px;
      color: #64748b;
      font-weight: 500;
    }

    .key-value {
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      font-size: 13px;
      color: #1a202c;
      flex: 1;
      background: transparent;
      border: none;
      padding: 0;
    }

    .copy-btn {
      color: #64748b;
      transition: all 0.2s;

      &:hover {
        color: #1890ff;
        transform: scale(1.1);
      }
    }
  }
}

/* 产品详细信息网格 */
.product-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;

  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px;
    border-radius: 8px;
    border: 1px solid #f1f5f9;
    background: #fafafa;
    transition: all 0.2s;

    &:hover {
      border-color: #e2e8f0;
      background: #f8fafc;
    }

    .detail-label {
      font-size: 12px;
      color: #64748b;
      font-weight: 500;
    }

    .detail-value {
      font-size: 15px;
      color: #1e293b;
      font-weight: 600;
      line-height: 1.4;
      
      // 状态badge样式优化
      :deep(.ant-badge) {
        margin-right: 6px;
      }
    }
  }
}

.product-title-section {
  .product-name {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 12px 0;
    line-height: 1.2;
  }

  .product-key-section {
    display: flex;
    align-items: center;
    gap: 12px;

    .key-label {
      font-size: 14px;
      color: #64748b;
      font-weight: 500;
    }

    .key-value {
      font-size: 14px;
      color: #1e293b;
      font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
      background: #f8fafc;
      padding: 6px 12px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      font-weight: 500;
    }

    .copy-btn {
      color: #64748b;
      transition: color 0.2s ease;

      &:hover {
        color: #3b82f6;
      }
    }
  }
}

/* 左侧产品详细信息 */
.product-details-left {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;

  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px;
    // background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #f1f5f9;

    .detail-label {
      font-size: 12px;
      color: #64748b;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .detail-value {
      font-size: 15px;
      color: #1e293b;
      font-weight: 600;
      line-height: 1.4;
    }
  }
}

/* 右侧区域 */
.product-right-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 280px;
  max-width: 320px;
}

/* 产品图片区域 */
.product-image-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;

  .image-wrapper {
    width: 200px;
    height: 200px;
    border-radius: 16px;
    overflow: hidden;
    border: 2px solid #f1f5f9;
    background: #fafbfc;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    cursor: pointer;

    &.clickable:hover {
      border-color: #3b82f6;
      box-shadow: 0 8px 25px rgba(59, 130, 246, 0.15);
      transform: translateY(-2px);
    }

    .product-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: #94a3b8;

      .placeholder-icon {
        font-size: 48px;
      }

      .placeholder-text {
        font-size: 14px;
        color: #64748b;
      }
    }
  }

  .image-actions {
    display: flex;
    gap: 12px;

    .action-link {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: 8px;
      font-size: 14px;
      color: #3b82f6;
      background: #eff6ff;
      border: 1px solid #dbeafe;
      transition: all 0.2s ease;
      text-decoration: none;

      &:hover {
        background: #dbeafe;
        color: #1d4ed8;
        transform: translateY(-1px);
      }
    }
  }
}

/* 产品详细信息 - 右侧布局 */
.product-details-right {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #f8fafc;
    border-radius: 6px;
    border: 1px solid #f1f5f9;

    .detail-label {
      font-size: 13px;
      color: #64748b;
      font-weight: 500;
      min-width: 80px;
    }

    .detail-value {
      font-size: 14px;
      color: #1e293b;
      font-weight: 600;
      text-align: right;
      flex: 1;
    }
  }
}

/* 产品详细信息 - 紧凑布局 */
.product-details-compact {
  margin-bottom: 16px;

  .detail-row {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .detail-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #f1f5f9;

    .detail-label {
      font-size: 12px;
      color: #64748b;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .detail-value {
      font-size: 15px;
      color: #1e293b;
      font-weight: 600;
      line-height: 1.4;
    }
  }
}

/* 产品描述 */
.product-description {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #f1f5f9;

  .description-label {
    display: block;
    font-size: 13px;
    color: #64748b;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 8px;
  }

  .description-text {
    font-size: 15px;
    color: #475569;
    line-height: 1.6;
    margin: 0;
  }
}

/* 信息卡片 */
.info-card {
  background: #ffffff;
  border: 1px solid #e8eaed;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
  margin-bottom: 16px;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #f8fafc;
    border-bottom: 1px solid #e8eaed;

    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #1a202c;

      .anticon {
        color: #1966ff;
        font-size: 16px;
      }
    }

    .edit-btn, .add-btn {
      border-radius: 6px;
      font-weight: 500;
      height: 32px;
      padding: 0 12px;
    }
  }

  .card-content {
    padding: 20px;
  }
}


/* 产品图片样式 */
.product-image-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  min-height: 120px;
}

.product-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  object-fit: cover;
  background: #f8fafc;
}

.no-image {
  color: #94a3b8;
  font-style: italic;
}

.image-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}

/* 概览内缩略图样式 */
.thumb-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.thumb-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  object-fit: cover;
  background: #f8fafc;
}

.image-inline {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thumb-box {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: box-shadow .2s ease, transform .2s ease;
}

.thumb-box.clickable:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
  cursor: pointer;
}

.thumb-placeholder {
  color: #cbd5e1;
  font-size: 20px;
}

.image-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-link {
  color: #1890ff;
}

.image-subtip {
  color: #94a3b8;
  font-size: 12px;
  margin-left: 4px;
}


/* 设备标签 */
.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .device-tag {
    border-radius: 6px;
    font-weight: 500;
    background: #f0f9ff;
    border-color: #bae6fd;
    color: #0369a1;
  }

  .no-tags {
    color: #94a3b8;
    font-style: italic;
    padding: 8px 0;
  }
}

.tags-edit {
  display: flex;
  gap: 12px;
  align-items: center;

  .tag-select {
    flex: 1;
    max-width: 400px;
  }

  .save-btn {
    border-radius: 6px;
    height: 32px;
    padding: 0 16px;
  }
}

/* 网关标签 */
.gateway-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .gateway-tag {
    border-radius: 6px;
    font-weight: 500;
    background: #fef3c7;
    border-color: #fbbf24;
    color: #92400e;
  }

  .no-gateways {
    color: #94a3b8;
    font-style: italic;
    padding: 8px 0;
  }
}

/* 配置列表 */
.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .config-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: #f1f5f9;
      border-color: #cbd5e1;
      transform: translateX(4px);
    }

    .config-label {
      font-size: 14px;
      color: #64748b;
      font-weight: 500;
    }

    .config-value {
      font-size: 14px;
      color: #1a202c;
      font-weight: 500;
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

/* 存储策略表格 */
.storage-table {
  border-radius: 14px;
  overflow: hidden;

  :deep(.ant-table) {
    border-radius: 14px;
    background: transparent;
    font-size: 14px;
  }

  :deep(.ant-table-thead > tr > th) {
    background: #f8fafc;
    border-bottom: 1.5px solid #e2e8f0;
    color: #374151;
    font-weight: 600;
    font-size: 14px;
    padding: 8px 10px;
    height: 40px;
  }

  :deep(.ant-table-tbody > tr > td) {
    padding: 8px 10px;
    border-bottom: 1px solid #f1f5f9;
    font-size: 14px;
    background: #fff;
    line-height: 1.5;
    height: 40px;
  }

  :deep(.ant-table-tbody > tr) {
    transition: all 0.2s ease;

    &:hover > td {
      background: #f4f8ff;
    }
  }
}

.storage-identifier {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #1966ff;

  .anticon {
    font-size: 14px;
    color: #64748b;
  }
}

.storage-limit {
  display: flex;
  align-items: center;
  gap: 4px;

  .limit-value {
    font-size: 16px;
    font-weight: 600;
    color: #0c4a6e;
  }

  .limit-unit {
    font-size: 12px;
    color: #64748b;
  }
}

/* 自定义参数 */
.custom-params {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .param-item {
    padding: 16px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: #f1f5f9;
      border-color: #cbd5e1;
      transform: translateX(4px);
    }

    .param-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .param-id {
        font-family: 'Monaco', 'Menlo', monospace;
        font-size: 12px;
        color: #1966ff;
        font-weight: 600;
        background: #eff6ff;
        padding: 4px 8px;
        border-radius: 4px;
        border: 1px solid #dbeafe;
      }

      .param-name {
        font-size: 14px;
        color: #1a202c;
        font-weight: 500;
      }
    }

    .param-description {
      font-size: 13px;
      color: #64748b;
      line-height: 1.5;
      padding-left: 4px;
    }
  }
}

/* 物模型标签页样式 */
.metadata-tab-content {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.metadata-wrapper {
  background: #ffffff;
  // border: 1px solid #e8eaed;
  border-top: none;
  border-radius: 0 0 12px 12px;
  overflow: hidden;

  :deep(.metadata-container) {
    background: transparent;
    min-height: auto;
    padding: 0;

    .metadata-tabs {
      :deep(.ant-tabs-bar) {
        background: #f8fafc;
        margin: 0;
        border-radius: 0;
        border: none;
        // border-bottom: 1px solid #e8eaed;
      }
    }

    .tab-content {
      border: none;
      border-radius: 0;
      box-shadow: none;
    }
  }
}


@media (max-width: 768px) {
  .product-details-container {
    padding: 0;
  }

  .page-header {
    padding: 16px 16px;
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .product-overview-card {
    padding: 20px;

    .overview-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;

      .product-title h2 {
        font-size: 18px;
      }

      .product-key .key-value {
        font-size: 14px;
      }
    }

    .overview-info {
      grid-template-columns: 1fr;
      gap: 12px;

      .info-item {
        padding: 12px;
      }
    }
  }

  .info-card {
    margin-bottom: 12px;

    .card-header {
      padding: 12px 16px;
    }

    .card-content {
      padding: 16px;
    }
  }

  .config-list, .storage-list {
    gap: 8px;

    .config-item, .storage-item {
      padding: 10px 12px;
    }
  }

  .custom-params {
    gap: 8px;

    .param-item {
      padding: 12px;
    }
  }
}


@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.product-overview-card,
.info-card {
  animation: slideIn 0.3s ease-out;
}

.info-card:nth-child(2) {
  animation-delay: 0.1s;
}

.info-card:nth-child(3) {
  animation-delay: 0.2s;
}

.info-card:nth-child(4) {
  animation-delay: 0.3s;
}

.info-card:nth-child(5) {
  animation-delay: 0.4s;
}

.info-card:nth-child(6) {
  animation-delay: 0.5s;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .custom-tabs-nav {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding: 0 4px;
  }

  .custom-tab-item {
    padding: 8px 12px;
    font-size: 12px;
    min-width: 80px;
    margin: 0 1px;
  }

  .custom-tab-item:first-child {
    margin-left: 4px;
  }

  .custom-tab-item:last-child {
    margin-right: 4px;
  }

  .tab-pane {
    padding: 16px;
  }

  .custom-tabs-container {
    margin: 0 16px;
  }

  .page-header {
    padding: 16px 16px;
  }
}

@media (max-width: 480px) {
  .custom-tab-item {
    padding: 10px 12px;
    font-size: 12px;
    min-width: 80px;
  }

  .tab-pane {
    padding: 12px;
  }
}
</style>
<script>

import {getProduct, modifyProductGateway, updateProduct, uploadProductImage} from '@/api/system/dev/product'
import {getInfo} from '@/api/system/dev/sort'
import CreateForm from './CreateForm'
import metadata from './metadata'
import configurationForm from './configurationForm'
import storageConfigurationForm from './storageConfigurationForm1'
import OtherConfigForm from './OtherConfigForm'
import productInstance from './productInstance'
import metadataShow from './metadataShow'
import importMetadata from './importMetadata'
import productProtocol from './productProtocol'
import SelectGateway from './SelectGateway'
import NetworkComponentBind from './NetworkComponentBind'
import TcpConnectionInfo from './TcpConnectionInfo'
import {bindCertificate, getCertificateList, unbindCertificate} from '@/api/system/certificate'
import CertificateBind from './CertificateBind.vue'
import ConfigurationEditor from './ConfigurationEditor.vue'
import storage from 'store'
import {ACCESS_TOKEN} from '@/store/mutation-types'

export default {
  name: 'ProductDetails',
  components: {
    productInstance,
    CreateForm,
    configurationForm,
    metadataShow,
    importMetadata,
    metadata,
    productProtocol,
    OtherConfigForm,
    storageConfigurationForm,
    SelectGateway,
    NetworkComponentBind,
    TcpConnectionInfo,
    CertificateBind,
    ConfigurationEditor
  },
  props: [],
  data() {
    return {
      otherConfiguration: [],
      // 导入物模型输入框是否显示
      importMetadataShow: false,
      // 物模型详情是否显示
      metadataShow: false,
      // 物模型显示类型 type === 1 IoT物模型；type === 2 的物模型
      metadataShowType: 1,
      // 物模型
      metadata: undefined,
      // 字典数据
      dicts: [],
      configurationDicts: [],
      publicConfigurationDicts: [],
      // 加载开关
      loading: false,
      // 配置信息数组
      configurationArray: [],
      // 公共配置信息数组
      publicConfigurationArray: [],
      // 存储策略显示数组
      storePolicyCfgArray: [],
      // 产品分组id
      productSortName: undefined,
      // 产品id
      productId: undefined,
      // 产品详情
      productDetails: {
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
        state: undefined
      },
      activeKey: '1',
      // 设备标签字典
      productTags: [],
      // 标签选择
      tagList: [],
      // 是否编辑标签
      editTagStatus: false,
      // 网关标签
      selectedGateway: null,
      // 网关产品图片
      gatewayImageUrl: '',
      showCertModal: false,
      certList: [],
      certLoading: false,
      certPagination: {current: 1, pageSize: 10, total: 0},
      certColumns: [
        {title: this.$t('network.certName'), dataIndex: 'name', align: 'center'},
        {title: '证书标识', dataIndex: 'sslKey', align: 'center'},
        {
          title: this.$t('network.expireTime'),
          dataIndex: 'expireTime',
          scopedSlots: {customRender: 'expireTime'},
          align: 'center'
        }
      ],
      selectedCertId: null,
      bindCert: null,
      // 产品图片
      productImageUrl: '',
      imagePreviewVisible: false,
      uploadFileUrl: process.env.VUE_APP_BASE_API + '/admin/v1/product/uploadImage',
      uploadHeaders: {
        Authorization: 'Bearer ' + storage.get(ACCESS_TOKEN)
      },
      // 存储策略表格列定义
      storageColumns: [
        {
          title: '存储标识',
          dataIndex: 'key',
          scopedSlots: {customRender: 'key'},
          align: 'left'
        },
        {
          title: '存储限制',
          dataIndex: 'value',
          scopedSlots: {customRender: 'value'},
          align: 'left'
        },
        {
          title: '类型',
          dataIndex: 'type',
          scopedSlots: {customRender: 'type'},
          align: 'left'
        }
      ],
      // 存储策略选项
      storePolicyOptions: [],
      // 是否编辑存储策略
      editStorePolicyMode: false,
      // 原始存储策略值（用于取消时恢复）
      originalStorePolicy: null
    }
  },
  created() {
    this.productId = this.$route.params.id
    console.log('this.productId', this.productId)
    // 获取存储策略字典
    this.getDictMap(['product_tags', 'log_store_policy']).then(res => {
      // 产品标签
      res.data['product_tags'].forEach((item) => {
        this.productTags.push({
          id: item.dictValue,
          name: item.dictLabel
        })
      })
      // 存储策略
      if (res.data['log_store_policy']) {
        this.storePolicyOptions = res.data['log_store_policy'].map(item => ({
          label: item.dictLabel,
          value: item.dictValue
        }))
      }
    })
    this.getProductDeatils()
  },
  methods: {
    // 设备标签选择
    handleTagChange(value) {
      this.productDetails.tags = value.join()
    },
    openImagePreview() {
      if (this.productImageUrl) {
        this.imagePreviewVisible = true
      }
    },
    // 编辑设备标签
    editTags() {
      this.editTagStatus = !this.editTagStatus
    },
    // 保存标签修改
    confirmTags() {
      const data = {
        id: this.productDetails.id,
        tags: this.productDetails.tags
      }
      updateProduct(data).then(res => {
        this.$message.success('修改成功', 3)
        this.editTagStatus = false
        this.getProductDeatils()
      })
    },
    /** 查询设备产品列表(无作用) */
    getList() {
    },
    color16() { // 十六进制颜色随机
      var colorList = ['#428BCA', '#2db7f5', '108ee9', '#87d068']
      var randomItem = colorList[Math.random() * colorList.length | 0]
      return randomItem
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
    // 根据设备id获取设备详情及设备配置信息
    getProductDeatils() {
      console.log(this.$store.state)
      this.loading = true
      this.configurationDicts = []
      this.publicConfigurationDicts = []
      this.dicts = []
      this.configurationArray = []
      this.publicConfigurationArray = []
      this.storePolicyCfgArray = []
      getProduct(this.productId).then(response => {
        // console.log('response = ', JSON.parse(response.data.metadata))

        // 新的API结构：直接返回 gwName、gwProductKey 和 gwPhotoUrl
        if (response.data.gwProductKey && response.data.gwName) { 
          this.selectedGateway = {
            name: response.data.gwName,
            productKey: response.data.gwProductKey
          }
          // 直接使用后端返回的网关图片
          if (response.data.gwPhotoUrl) {
            try {
              const photoObj = typeof response.data.gwPhotoUrl === 'string'
                ? JSON.parse(response.data.gwPhotoUrl)
                : response.data.gwPhotoUrl
              this.gatewayImageUrl = photoObj && photoObj.img ? photoObj.img : ''
            } catch (e) {
              this.gatewayImageUrl = ''
            }
          } else {
            this.gatewayImageUrl = ''
          }
        } else {
          this.selectedGateway = null
          this.gatewayImageUrl = ''
        }

        response.data.tagsName = this.getProductTags(response.data.tags)
        this.productDetails = response.data
        
        // 解析产品图片 - 优先使用 photoUrl 中的 img 字段
        this.productImageUrl = ''
        if (this.productDetails.photoUrl) {
          try {
            const photoObj = typeof this.productDetails.photoUrl === 'string'
              ? JSON.parse(this.productDetails.photoUrl)
              : this.productDetails.photoUrl
            this.productImageUrl = photoObj && photoObj.img ? photoObj.img : ''
          } catch (e) {
            this.productImageUrl = ''
          }
        }
        
        if (this.productDetails.tags) {
          this.tagList = this.productDetails.tags.split(',')
        }
        console.log('this.productDetails = ', this.productDetails)
        if (this.productDetails.thirdConfiguration) {
          var thirdCfg = JSON.parse(this.productDetails.thirdConfiguration)
          if (thirdCfg) {
            this.otherConfiguration = thirdCfg.customField
            console.log('this.otherConfiguration = ', this.otherConfiguration)
          }
        }
        let configArray = []
        let configData = {}
        if (this.productDetails.configuration !== undefined && this.productDetails.configuration
          !== null) {
          // 配置信息key数组
          configArray = Object.keys(JSON.parse(this.productDetails.configuration))
          // 配置信息key对象
          configData = JSON.parse(this.productDetails.configuration)
        }
        if (this.productDetails.storePolicyConfiguration) {
          const storeJson = JSON.parse(response.data.storePolicyConfiguration)
          const properties = storeJson.properties
          const eventsList = storeJson.event
          if (properties) {
            for (let i = 0; i < properties.length; i++) {
              this.storePolicyCfgArray.push({
                key: properties[i].id,
                value: properties[i].maxStorage,
                type: 'properties'
              })
            }
          }
          if (eventsList) {
            for (let j = 0; j < eventsList.length; j++) {
              this.storePolicyCfgArray.push({
                key: eventsList[j].id,
                value: eventsList[j].maxStorage,
                type: 'events'
              })
            }
          }
        }
        getInfo({id: this.productDetails.classifiedId}).then(response => {
          if (response.data !== undefined && response.data !== null) {
            this.productSortName = response.data.classifiedName
          }
        })

        // 获取公共配置和私有配置
        var dictArray = []
        dictArray.push('product_configuration_' + this.productDetails.thirdPlatform)
        dictArray.push('product_configuration_public')

        this.getDictMap(dictArray).then(res => {
          // 私有配置
          if (res.data['product_configuration_' + this.productDetails.thirdPlatform] !== undefined
            && res.data['product_configuration_' + this.productDetails.thirdPlatform] !== null) {
            this.configurationDicts = res.data['product_configuration_'
            + this.productDetails.thirdPlatform]
            if (this.configurationDicts.length > 0) {
              if (configArray !== undefined) {
                for (let j = 0; j < this.configurationDicts.length; j++) {
                  let config = {}
                  try {
                    if (this.configurationDicts[j].remark !== undefined ||
                      this.configurationDicts[j].remark !== null ||
                      this.configurationDicts[j].remark !== '') {
                      config = JSON.parse(this.configurationDicts[j].remark)
                    }
                  } catch (e) {
                    config = {}
                  }
                  for (let i = 0; i < configArray.length; i++) {
                    if (configArray[i] === this.configurationDicts[j].dictValue) {
                      this.configurationArray.push({
                        key: this.configurationDicts[j].dictLabel,
                        value: configData[configArray[i]],
                        remark: config.remark,
                        type: config.type
                      })
                      break
                    }
                  }
                  if (this.configurationArray[j] === undefined || this.configurationArray[j]
                    === null) {
                    this.configurationArray.push({
                      key: this.configurationDicts[j].dictLabel,
                      value: undefined,
                      remark: config.remark,
                      type: config.type
                    })
                  }
                }
              } else {
                for (let j = 0; j < this.configurationDicts.length; j++) {
                  let config = {}
                  try {
                    if (this.configurationDicts[j].remark !== undefined ||
                      this.configurationDicts[j].remark !== null ||
                      this.configurationDicts[j].remark !== '') {
                      config = JSON.parse(this.configurationDicts[j].remark)
                    }
                  } catch (e) {
                    config = {}
                  }
                  this.configurationArray.push({
                    key: this.configurationDicts[j].dictLabel,
                    value: undefined,
                    remark: config.remark,
                    type: config.type
                  })
                }
              }
            }
            console.log('this.configurationArray', this.configurationArray)
          }

          // 公共配置
          this.publicConfigurationDicts = res.data['product_configuration_public']
          if (this.publicConfigurationDicts.length > 0) {
            if (configArray) {
              for (let j = 0; j < this.publicConfigurationDicts.length; j++) {
                for (let i = 0; i < configArray.length; i++) {
                  if (configArray[i] === this.publicConfigurationDicts[j].dictValue) {
                    this.publicConfigurationArray.push({
                      key: this.publicConfigurationDicts[j].dictLabel,
                      value: configData[configArray[i]],
                      remark: this.publicConfigurationDicts[j].remark
                    })
                    break
                  }
                }
                if (this.publicConfigurationArray[j] === undefined
                  || this.publicConfigurationArray[j] === null) {
                  this.publicConfigurationArray.push({
                    key: this.publicConfigurationDicts[j].dictLabel,
                    value: undefined,
                    remark: this.publicConfigurationDicts[j].remark
                  })
                }
              }
            } else {
              for (let j = 0; j < this.publicConfigurationDicts.length; j++) {
                this.publicConfigurationArray.push({
                  key: this.publicConfigurationDicts[j].dictLabel,
                  value: undefined,
                  remark: this.publicConfigurationDicts[j].remark
                })
              }
            }
          }
        })
      }).finally(() => {
        this.loading = false
      })
    },
    // 返回上一级
    back() {
      this.$router.back()
    },
    // 修改配置
    editConfig(type) {
      if (type === 'publicConfiguration') {
        this.dicts = this.publicConfigurationDicts
        this.$refs.configurationForm.handleUpdateById(this.productId, 'publicConfiguration')
      } else if (type === 'privateConfiguration') {
        console.log('this.configurationDicts +++++ ', this.configurationDicts)
        this.dicts = this.configurationDicts
        this.$refs.configurationForm.handleUpdateById(this.productId, 'privateConfiguration')
      } else if (type === 'otherConfiguration') {
        this.dicts = this.configurationDicts
        this.$refs.otherConfigurationForm.handleUpdateAndAdd(this.productId)
      } else if (type === 'storageConfiguration') {
        this.$refs.storageConfigurationForm.handleUpdateById(this.productId)
      }
    },
    // 打开物模型详情
    openMetadataDetail() {
      if (this.metadataShow) {
        this.metadataShowType = 1
        this.metadataShow = false
      } else {
        this.metadataShowType = 1
        this.metadataShow = true
      }
    },
    // 关闭物模型详情
    closeMetadataDetail() {
      this.metadataShow = false
    },
    // 打开导入物模型
    openImportMetadata() {
      if (this.importMetadataShow) {
        this.importMetadataShow = false
      } else {
        this.importMetadataShow = true
      }
    },
    // 关闭导入物模型
    closeImportMetadata() {
      this.importMetadataShow = false
    },
    // 成功更新物模型
    successUpdateMetadata() {
      this.importMetadataShow = false
      this.$refs.metadata.refresMetadata()
    },

    // 存储策略配置成功
    storageConfigOk() {
      console.log('存储策略配置成功')
    },

    // 网关相关
    openGatewayModal() {
      this.$refs.selectGateway.showModal()
    },
    addGateway(val) {
      this.selectedGateway = val
      // 直接从选择的网关对象中获取图片
      if (val.photoUrl) {
        try {
          const photoObj = typeof val.photoUrl === 'string'
            ? JSON.parse(val.photoUrl)
            : val.photoUrl
          this.gatewayImageUrl = photoObj && photoObj.img ? photoObj.img : ''
        } catch (e) {
          this.gatewayImageUrl = ''
        }
      } else {
        this.gatewayImageUrl = ''
      }
      this.modifyGateway()
    },
    removeGateway() {
      this.$confirm({
        title: '提示',
        content: '确认删除网关关联?',
        onOk: () => {
          this.selectedGateway = null
          this.gatewayImageUrl = ''
          this.modifyGateway()
        }
      })
    },
    modifyGateway() {
      const gwProductKey = this.selectedGateway ? this.selectedGateway.productKey : ''
      modifyProductGateway(this.productId, gwProductKey)
        .then(() => {
          this.$message.success('操作成功!')
        })
    },
    // 搜索|过滤产品
    filterOption(value, option) {
      return (
        option.componentOptions.children[0].children[0].text.toLowerCase().indexOf(
          value.toLowerCase()) >= 0
      )
    },
    // 复制到剪贴板
    copyToClipboard(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopyTextToClipboard(text)
        })
      } else {
        this.fallbackCopyTextToClipboard(text)
      }
    },
    // 兜底复制方法
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea')
      textArea.value = text
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        document.execCommand('copy')
        this.$message.success('已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败')
      }
      document.body.removeChild(textArea)
    },
    // 获取平台颜色
    getPlatformColor(platform) {
      const colorMap = {
        'universal': '#1890ff',
        'aliyun': '#fa8c16',
        'tencent': '#722ed1',
        'huawei': '#eb2f96',
        'baidu': '#13c2c2'
      }
      return colorMap[platform] || '#1890ff'
    },
    // 获取设备节点文本
    getDeviceNodeText(deviceNode) {
      const nodeMap = {
        'DEVICE': '直连设备',
        'GATEWAY': '网关设备',
        'GATEWAY_SUB_DEVICE': '网关子设备'
      }
      return nodeMap[deviceNode] || deviceNode || '-'
    },
    loadBindCert() {
      // 假设产品详情有bindCertKey字段
      if (this.productDetails.bindCertKey) {
        getCertificateList({sslKey: this.productDetails.bindCertKey}).then(res => {
          if (res.rows && res.rows.length) {
            this.bindCert = res.rows[0]
          } else {
            this.bindCert = null
          }
        })
      } else {
        this.bindCert = null
      }
    },
    loadCertList(page = 1, size = 10) {
      this.certLoading = true
      getCertificateList({pageNum: page, pageSize: size}).then(res => {
        this.certList = res.rows || []
        this.certPagination.total = res.total || 0
        this.certPagination.current = page
        this.certPagination.pageSize = size
      }).finally(() => {
        this.certLoading = false
      })
    },
    onCertSelect(selectedRowKeys) {
      this.selectedCertId = selectedRowKeys[0]
    },
    handleBind() {
      const cert = this.certList.find(c => c.id === this.selectedCertId)
      if (!cert) {
        return
      }
      bindCertificate(this.productDetails.productKey, cert.sslKey).then(res => {
        this.$message.success('绑定成功')
        this.showCertModal = false
        this.loadBindCert()
      })
    },
    handleUnbind() {
      unbindCertificate(this.productDetails.productKey).then(res => {
        this.$message.success('解绑成功')
        this.loadBindCert()
      })
    },
    // 图片上传相关
    beforeUploadImage(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
      if (!isImage) {
        this.$message.error('仅支持 PNG 或 JPG 格式')
        return false
      }
      const isLt5M = file.size / 1024 / 1024 <= 5
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB')
        return false
      }
      return true
    },
    handleCustomImageUpload({file, onSuccess, onError}) {
      const formData = new FormData()
      formData.append('id', this.productDetails.id)
      formData.append('file', file)

      // 使用 product.js 中的上传函数
      uploadProductImage(formData).then(res => {
        if (res && res.code === 0) {
          const url = res.data && res.data.url
          if (url) {
            this.productImageUrl = url
          }
          this.$message.success('上传成功')
          this.getProductDeatils()
          onSuccess(res)
        } else {
          this.$message.error('上传失败' + (res && res.msg ? ('：' + res.msg) : ''))
          onError(res)
        }
      }).catch(err => {
        this.$message.error('上传失败')
        onError(err)
      })
    },
    updateProductImage(url) {
      const payload = {
        id: this.productDetails.id,
        photoUrl: JSON.stringify({img: url, detail: ''})
      }
      updateProduct(payload).then(() => {
        this.$message.success('图片已更新')
        this.productImageUrl = url
        this.getProductDeatils()
      })
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
    
    formatTime(val) {
      if (!val) return '-'
      // 如果是时间戳，转换为日期
      const timestamp = typeof val === 'string' ? new Date(val).getTime() : val
      const d = new Date(timestamp)
      if (isNaN(d.getTime())) return '-'
      
      const pad = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },
    getSslKeyFromConfig(config) {
      if (!config) {
        return ''
      }
      try {
        const obj = typeof config === 'string' ? JSON.parse(config) : config
        return obj.sslKey || ''
      } catch (e) {
        return ''
      }
    },
    
    // 处理存储策略变更
    handleStorePolicyChange(value) {
      // 显示确认对话框
      this.$confirm({
        title: '确认修改存储策略？',
        content: '切换数据源后，已存储在原数据源的数据将无法查询。建议在新建产品时配置，不要随意切换。',
        okText: '确认修改',
        cancelText: '取消',
        okType: 'danger',
        onOk: () => {
          const data = {
            id: this.productDetails.id,
            storePolicy: value
          }
          updateProduct(data).then(res => {
            this.$message.success('修改成功', 3)
            this.editStorePolicyMode = false
            this.getProductDeatils()
          }).catch(err => {
            this.$message.error('修改失败')
            // 恢复原值
            this.productDetails.storePolicy = this.originalStorePolicy
            this.editStorePolicyMode = false
          })
        },
        onCancel: () => {
          // 取消时恢复原值
          this.productDetails.storePolicy = this.originalStorePolicy
          this.editStorePolicyMode = false
        }
      })
    },
    
    // 开始编辑存储策略
    startEditStorePolicy() {
      // 保存原始值
      this.originalStorePolicy = this.productDetails.storePolicy
      this.editStorePolicyMode = true
    },
    
    // 取消编辑存储策略
    cancelEditStorePolicy() {
      // 恢复原值
      this.productDetails.storePolicy = this.originalStorePolicy
      this.editStorePolicyMode = false
    },
    
    // 获取存储策略标签
    getStorePolicyLabel(value) {
      const option = this.storePolicyOptions.find(item => item.value === value)
      return option ? option.label : value || '-'
    }
  },
  watch: {
    activeKey(val) {
      if (val === '5') {
        this.loadBindCert()
        this.loadCertList()
      }
    }
  }
}
</script>

<style lang="less" scoped>
.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;

  .ant-space {
    .ant-btn {
      height: 28px;
      padding: 0 12px;
      font-size: 12px;
      border-radius: 4px;

      &.ant-btn-primary {
        background: #1890ff;
        border-color: #1890ff;

        &:hover {
          background: #40a9ff;
          border-color: #40a9ff;
        }
      }

      &.ant-btn-default {
        border-color: #d9d9d9;
        color: #666;

        &:hover {
          border-color: #40a9ff;
          color: #40a9ff;
        }
      }
    }
  }
}

.bind-cert-header {
  display: flex;
  align-items: center;
  font-size: 15px;
  margin-bottom: 12px;
}

/* 网关卡片优化 */
.gateway-card {
  .gateway-selected {
    .gateway-info-box {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
      border-radius: 8px;
      border: 1px solid #bae6fd;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15);
        transform: translateY(-2px);
      }

      .gateway-icon {
        font-size: 32px;
        color: #0284c7;
      }

      .gateway-image {
        width: 48px;
        height: 48px;
        flex-shrink: 0;
        border-radius: 8px;
        overflow: hidden;
        background: #fff;
        border: 1px solid #e0f2fe;
        display: flex;
        align-items: center;
        justify-content: center;

        .gateway-photo {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .gateway-icon {
          font-size: 28px;
          color: #0284c7;
        }
      }

      .gateway-details {
        flex: 1;

        .gateway-name {
          font-size: 16px;
          font-weight: 600;
          color: #0c4a6e;
          margin-bottom: 4px;
        }

        .gateway-key {
          font-size: 12px;
          color: #64748b;
          font-family: 'Monaco', 'Menlo', monospace;
        }
      }

      .remove-btn {
        color: #94a3b8;

        &:hover {
          color: #ef4444;
        }
      }
    }
  }

  .gateway-empty {
    text-align: center;
    padding: 40px 20px;

    .empty-icon {
      font-size: 48px;
      color: #cbd5e1;
      margin-bottom: 16px;
    }

    .empty-text {
      font-size: 15px;
      color: #64748b;
      margin: 0 0 8px 0;
      font-weight: 500;
    }

    .empty-hint {
      font-size: 13px;
      color: #94a3b8;
      margin: 0;
    }
  }
}

.gateway-info {
  margin-bottom: 16px;

  .selected-gateway {
    margin-bottom: 8px;

    .gateway-tag {
      font-size: 14px;
      padding: 4px 8px;
      border-radius: 4px;
    }
  }
}

// 第三方平台参数空状态样式
.no-platform-params {
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: #999;

    .empty-icon {
      font-size: 48px;
      color: #d9d9d9;
      margin-bottom: 16px;
    }

    .empty-text {
      font-size: 16px;
      color: #666;
      margin-bottom: 8px;
    }

    .empty-desc {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }
}

// 第三方平台参数列表样式
.platform-params {
  .param-item {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    transition: all 0.3s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #fafafa;
      border-radius: 6px;
      padding: 16px 12px;
      margin: 0 -12px;
    }

    .param-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .param-info {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .param-key {
          font-size: 12px;
          color: #999;
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        }

        .param-label {
          font-weight: 500;
          color: #333;
          font-size: 14px;
        }
      }

      .param-tag {
        font-size: 12px;
        border-radius: 4px;
      }
    }

    .param-description {
      display: flex;
      align-items: flex-start;
      color: #666;
      font-size: 13px;
      line-height: 1.6;
      background: #f8f9fa;
      padding: 8px 12px;
      border-radius: 4px;
      border-left: 3px solid #1890ff;

      .desc-icon {
        margin-right: 6px;
        margin-top: 2px;
        color: #1890ff;
        font-size: 12px;
      }
    }
  }
}

.no-gateways {
  color: #999;
  font-style: italic;
}
</style>
