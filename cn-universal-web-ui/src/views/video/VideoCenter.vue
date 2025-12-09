<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="平台名称" prop="name">
                <a-input v-model="queryParam.name" placeholder="请输入平台名称" @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="平台类型" prop="platformType">
                <a-select v-model="queryParam.platformType" placeholder="请选择平台类型" allow-clear style="width: 100%">
                  <a-select-option v-for="d in platformTypeOptions" :key="d.dictValue" :value="d.dictValue">
                    {{ d.dictLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="状态" prop="enabled">
                <a-select v-model="queryParam.enabled" placeholder="请选择状态" allow-clear style="width: 100%">
                  <a-select-option :value="1">已启用</a-select-option>
                  <a-select-option :value="0">已禁用</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery">
                  <a-icon type="search"/>查询
                </a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">
                  <a-icon type="redo"/>重置
                </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 操作按钮 -->
      <div class="table-operations">
        <a-button type="primary" @click="showCreateModal" v-hasPermi="['video:platform:add']">
          <a-icon type="plus"/>新增
        </a-button>
        <a-button type="primary" size="small" :loading="loading" :style="{ float: 'right' }" @click="fetchPlatforms" v-hasPermi="['video:platform:query']">
          <a-icon type="sync" :spin="loading"/>
        </a-button>
      </div>

      <!-- 平台卡片网格 -->
      <div class="platform-grid">
        <div
          v-for="item in displayPlatforms"
          :key="item.id"
          class="platform-card"
          :class="{ 
            'platform-card-disabled': item.platformType !== 'wvp'
          }"
          @click="item.platformType === 'wvp' ? handleCardClick(item) : null"
        >
          <!-- 禁用遮罩层 -->
          <div v-if="item.platformType !== 'wvp'" class="disabled-overlay" @click.stop>
            <div class="disabled-badge">
              <a-icon type="lock" />
              <span>暂未开放</span>
            </div>
          </div>

          <!-- 卡片头部：Logo + 名称 + 状态 -->
          <div class="card-header" @click.stop="item.platformType === 'wvp' ? handleCardClick(item) : null">
            <div class="card-logo">
              <img :src="getPlatformLogo(item.platformType)" :alt="platformTypeFormat(item.platformType)" />
            </div>
            <div class="card-info">
              <div class="card-name">{{ item.name }}</div>
              <div class="card-type">
                <a-tag :color="getPlatformTypeColor(item.platformType)" size="small">
                  {{ platformTypeFormat(item.platformType) }}
                </a-tag>
              </div>
            </div>
            <div class="card-status">
              <a-badge :status="item.enabled === 1 ? 'success' : 'default'" />
              <span class="status-text">{{ item.enabled === 1 ? '已启用' : '禁用' }}</span>
            </div>
          </div>

          <!-- 卡片内容：配置信息 -->
          <div class="card-content" @click.stop="item.platformType === 'wvp' ? handleCardClick(item) : null">
            <div class="config-section">
              <div class="config-title">
                <a-icon type="cloud-server" />
                <span>平台信息</span>
              </div>
              <div class="config-item">
                <span class="label">实例Key：</span>
                <span class="value">{{ item.instanceKey }}</span>
              </div>
              <div class="config-item">
                <span class="label">地址：</span>
                <span class="value" :title="item.endpoint">{{ item.endpoint }}</span>
              </div>
            </div>

            <!-- 创建信息 -->
            <div class="create-info">
              <div class="info-item">
                <a-icon type="user" />
                <span class="label">创建者：</span>
                <span class="value">{{ item.creatorId || '系统' }}</span>
              </div>
              <div class="info-item">
                <a-icon type="clock-circle" />
                <span class="label">创建时间：</span>
                <span class="value">{{ parseTime(item.createTime) }}</span>
              </div>
              <div class="info-item">
                <a-icon type="calendar" />
                <span class="label">更新时间：</span>
                <span class="value">{{ parseTime(item.updateTime) || '未更新' }}</span>
              </div>
            </div>
          </div>

          <!-- 卡片操作 -->
          <div class="card-actions">
            <a-button 
              type="link" 
              size="small" 
              @click.stop="handleSync(item)" 
              :loading="syncingPlatforms.has(item.instanceKey)"
              v-hasPermi="['video:platform:sync']"
            >
              <a-icon type="sync" />同步设备
            </a-button>
            <a-button type="link" size="small" @click.stop="handleTest(item)" v-hasPermi="['video:platform:test']">
              <a-icon type="api" />测试连接
            </a-button>
            <a-button type="link" size="small" @click.stop="showEditModal(item)" v-hasPermi="['video:platform:edit']">
              <a-icon type="edit" />编辑
            </a-button>
            <a-button type="link" size="small" @click.stop="handleDelete(item)" v-hasPermi="['video:platform:remove']">
              <a-icon type="delete" />删除
            </a-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <a-pagination 
        class="ant-table-pagination" 
        show-size-changer 
        show-quick-jumper
        :current="queryParam.pageNum"
        :page-size="queryParam.pageSize"
        :total="total"
        :showTotal="total => `共 ${total} 条`"
        @change="changeSize"
        @showSizeChange="onShowSizeChange"
      />
    </a-card>

    <!-- 新增/编辑平台实例弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleSave"
      @cancel="handleCancel"
      width="700px"
      :confirmLoading="saveLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="平台类型" required>
          <a-select v-model:value="formData.platformType" :disabled="!!formData.id" @change="onPlatformTypeChange">
            <a-select-option v-for="d in platformTypeOptions" :key="d.dictValue" :value="d.dictValue">
              {{ d.dictLabel }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="实例Key" required>
          <a-input
            v-model:value="formData.instanceKey"
            :disabled="!!formData.id"
            placeholder="唯一标识，如wvp_main"
          />
        </a-form-item>
        <a-form-item label="名称" required>
          <a-input v-model:value="formData.name" placeholder="为平台起一个名称" />
        </a-form-item>

        <!-- 动态配置字段 -->
        <a-form-item
          v-for="field in currentPlatformFields"
          :key="field.dictValue"
          :label="field.dictLabel"
          :required="isFieldRequired(field)"
        >
            <a-input
              v-if="getFieldType(field) === 'input'"
              v-model:value="formData.authConfig[field.dictValue]"
              :placeholder="getFieldPlaceholder(field)"
            />
            <a-input-password
              v-else-if="getFieldType(field) === 'password'"
              v-model:value="formData.authConfig[field.dictValue]"
              :placeholder="getFieldPlaceholder(field)"
            />
            <a-input-number
              v-else-if="getFieldType(field) === 'number'"
              v-model:value="formData.authConfig[field.dictValue]"
              :min="getFieldMin(field)"
              :max="getFieldMax(field)"
              style="width: 100%"
            />
            <a-select
              v-else-if="getFieldType(field) === 'select'"
              v-model:value="formData.authConfig[field.dictValue]"
              placeholder="请选择"
              allow-clear
            >
              <a-select-option v-for="opt in getFieldOptions(field)" :key="opt" :value="opt">
                {{ opt }}
              </a-select-option>
            </a-select>
        </a-form-item>

        <a-form-item v-if="formData.platformType === 'wvp'" label="自动创建产品">
          <a-checkbox v-model:checked="formData.autoCreateProductsChecked">自动创建GB/级联产品</a-checkbox>
        </a-form-item>
        <a-form-item label="启用">
          <a-checkbox v-model:checked="formData.enabledChecked" />
        </a-form-item>
        <a-divider />
        <a-form-item label="事件订阅">
          <a-switch v-model:checked="formData.subscriptionEnabled" />
        </a-form-item>
        <a-form-item label="订阅事件" v-if="formData.subscriptionEnabled">
          <a-select v-model:value="formData.subscriptionTopics" mode="multiple" allow-clear placeholder="选择订阅事件">
            <a-select-option v-for="opt in subscriptionTypeOptionsDefault" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="回调地址" v-if="formData.subscriptionEnabled">
          <a-input v-model:value="formData.subscriptionCallback" placeholder="https://example.com/webhook" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 删除确认弹窗 -->
    <a-modal
      v-model:visible="deleteModalVisible"
      title="确认删除"
      @ok="confirmDelete"
      @cancel="cancelDelete"
      width="500px"
      okText="确认删除"
      cancelText="取消"
      okType="danger"
    >
      <div style="margin-bottom: 16px">
        <a-icon type="exclamation-circle" style="color: #faad14; font-size: 22px; margin-right: 12px" />
        <span style="font-size: 16px; font-weight: 500">
          确定删除平台实例「{{currentDeleteTarget?.name}}」？
        </span>
      </div>
      
      <a-alert
        message="提示"
        description="普通删除仅移除平台配置，已同步的设备数据保留。如需同时清除设备数据，请勾选下方「强制删除」。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-checkbox v-model:checked="forceDeleteChecked">
        <span style="font-weight: 500; color: #ff4d4f">强制删除（同时清除所有关联设备数据）</span>
      </a-checkbox>
      
      <a-alert
        v-if="forceDeleteChecked"
        message="警告"
        description="强制删除将永久清除该平台下所有已同步的设备、通道、录像等数据，此操作不可恢复！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import { listPlatform, savePlatform, delPlatform, testPlatform, syncPlatform } from '@/api/video/platform'

export default {
  name: 'VideoCenter',
  data() {
    return {
      loading: false,
      saveLoading: false,
      platforms: [],
      total: 0,
      queryParam: {
        pageNum: 1,
        pageSize: 10,
        platformType: undefined,
        name: undefined,
        enabled: undefined
      },
      columns: [
        { title: '平台类型', dataIndex: 'platformType', key: 'platformType', scopedSlots: { customRender: 'platformType' }, align: 'center' },
        { title: '实例Key', dataIndex: 'instanceKey', key: 'instanceKey', align: 'center' },
        { title: '名称', dataIndex: 'name', key: 'name', align: 'center' },
        { title: '地址', dataIndex: 'endpoint', key: 'endpoint', ellipsis: true, align: 'center' },
        { title: '版本', dataIndex: 'version', key: 'version', align: 'center' },
        { title: '启用', dataIndex: 'enabled', key: 'enabled', scopedSlots: { customRender: 'enabled' }, align: 'center' },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', scopedSlots: { customRender: 'createTime' }, align: 'center' },
        { title: '操作', key: 'operation', width: '20%', scopedSlots: { customRender: 'operation' }, align: 'center' }
      ],
      modalVisible: false,
      modalTitle: '新增平台实例',
      formData: {
        id: null,
        platformType: 'wvp',
        instanceKey: '',
        name: '',
        endpoint: '',
        version: '',
        authConfig: {},
        autoCreateProductsChecked: false,
        enabledChecked: true,
        subscriptionEnabled: false,
        subscriptionTopics: [],
        subscriptionCallback: ''
      },
      platformTypeOptions: [],
      wvpFieldsOptions: [],
      hikIscFieldsOptions: [],
      iccFieldsOptions: [],
      subscriptionTypeOptions: [],
      metrics: { total: 0, enabled: 0, disabled: 0 },
      syncingPlatforms: new Set(), // 跟踪正在同步的平台
      deleteModalVisible: false,
      currentDeleteTarget: null,
      forceDeleteChecked: false
    }
  },
  computed: {
    currentPlatformFields() {
      if (this.formData.platformType === 'wvp') return this.wvpFieldsOptions
      if (this.formData.platformType === 'isc') return this.hikIscFieldsOptions
      if (this.formData.platformType === 'icc') return this.iccFieldsOptions
      return []
    },
    // 分页后的数据
    displayPlatforms() {
      const start = (this.queryParam.pageNum - 1) * this.queryParam.pageSize
      const end = start + this.queryParam.pageSize
      return this.platforms.slice(start, end)
    }
  },
  created() {
    this.fetchPlatforms()
    this.loadDicts()
  },
  methods: {
    async loadDicts() {
      const [typeRes, wvpRes, hikRes, iccRes, subTypes] = await Promise.all([
        this.getDicts('video_platform_type'),
        this.getDicts('video_wvp_fields'),
        this.getDicts('video_isc_fields'),
        this.getDicts('video_icc_fields'),
        this.getDicts('video_subscription_types')
      ])
      this.platformTypeOptions = typeRes.data || []
      this.wvpFieldsOptions = wvpRes.data || []
      this.hikIscFieldsOptions = hikRes.data || []
      this.iccFieldsOptions = iccRes.data || []
      this.subscriptionTypeOptions = (subTypes.data || []).map(d => ({ label: d.dictLabel, value: d.dictValue }))
    },
    async fetchPlatforms() {
      this.loading = true
      try {
        const res = await listPlatform()
        const all = res.data || []
        // 先过滤，再分页
        const { platformType, name, enabled } = this.queryParam
        const filtered = all.filter(item => {
          const matchType = platformType ? item.platformType === platformType : true
          const matchName = name ? (item.name || '').toLowerCase().includes(name.toLowerCase()) : true
          const matchEnabled = (enabled === 0 || enabled === 1) ? item.enabled === enabled : true
          return matchType && matchName && matchEnabled
        })
        this.platforms = filtered
        this.total = filtered.length
        this.metrics.total = all.length
        this.metrics.enabled = all.filter(i => i.enabled === 1).length
        this.metrics.disabled = all.filter(i => i.enabled !== 1).length
      } finally {
        this.loading = false
      }
    },
    platformTypeFormat(value) {
      return this.selectDictLabel(this.platformTypeOptions, value)
    },
    handleQuery() {
      this.queryParam.pageNum = 1
      this.fetchPlatforms()
    },
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10,
        platformType: undefined,
        name: undefined,
        enabled: undefined
      }
      this.handleQuery()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageNum = 1
      this.queryParam.pageSize = pageSize
      this.fetchPlatforms()
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
    },
    showCreateModal() {
      this.modalTitle = '新增平台实例'
      this.formData = {
        id: null,
        platformType: 'wvp',
        instanceKey: '',
        name: '',
        endpoint: '',
        version: '',
        authConfig: {},
        autoCreateProductsChecked: false,
        enabledChecked: true
      }
      this.modalVisible = true
    },
    showEditModal(record) {
      this.modalTitle = '编辑平台实例'
      let authConfig = {}
      try {
        if (record.auth) authConfig = JSON.parse(record.auth)
        let options = {}
        if (record.options) options = JSON.parse(record.options)
        const sub = options.subscription || {}
        this.formData.subscriptionEnabled = !!sub.enabled
        this.formData.subscriptionTopics = sub.topics || []
        this.formData.subscriptionCallback = sub.callback || ''
      } catch (e) {
        console.warn('解析配置失败', e)
      }
      this.formData = {
        id: record.id,
        platformType: record.platformType,
        instanceKey: record.instanceKey,
        name: record.name,
        endpoint: record.endpoint || '',
        version: record.version || '',
        authConfig,
        autoCreateProductsChecked: record.autoCreateProducts === 1,
        enabledChecked: record.enabled === 1
      }
      this.modalVisible = true
    },
    handleCancel() {
      this.modalVisible = false
    },
    async handleSave() {
      this.saveLoading = true
      try {
        const payload = {
          id: this.formData.id,
          platformType: this.formData.platformType,
          instanceKey: this.formData.instanceKey,
          name: this.formData.name,
          endpoint: this.formData.authConfig.endpoint || this.formData.endpoint,
          version: this.formData.authConfig.version || this.formData.version,
          auth: JSON.stringify(this.formData.authConfig),
          options: JSON.stringify({ subscription: { enabled: this.formData.subscriptionEnabled, topics: this.formData.subscriptionTopics || [], callback: this.formData.subscriptionCallback || '' } }),
          autoCreateProducts: this.formData.autoCreateProductsChecked ? 1 : 0,
          enabled: this.formData.enabledChecked ? 1 : 0
        }
        await savePlatform(payload)
        this.$message.success('保存成功')
        this.modalVisible = false
        this.fetchPlatforms()
      } catch (e) {
        this.$message.error('保存失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.saveLoading = false
      }
    },
    async handleTest(record) {
      try {
        const res = await testPlatform({ instanceKey: record.instanceKey })
        const { reachable, latencyMs } = res.data || {}
        if (reachable) {
          this.$message.success(`连接测试成功，延时${latencyMs}ms`)
        } else {
          this.$message.error('连接测试失败')
        }
      } catch (e) {
        this.$message.error('测试失败：' + (e.response?.data?.msg || e.message))
      }
    },
    async handleSync(record) {
      if (this.syncingPlatforms.has(record.instanceKey)) {
        this.$message.warning('正在同步中，请勿重复操作')
        return
      }
      
      this.syncingPlatforms.add(record.instanceKey)
      try {
        await syncPlatform(record.instanceKey)
        this.$message.success('同步成功')
      } catch (e) {
      } finally {
        this.syncingPlatforms.delete(record.instanceKey)
      }
    },
    handleDelete(record) {
      this.currentDeleteTarget = record
      this.forceDeleteChecked = false
      this.deleteModalVisible = true
    },
    async confirmDelete() {
      try {
        await delPlatform(this.currentDeleteTarget.id, { force: this.forceDeleteChecked })
        this.$message.success('删除成功')
        this.deleteModalVisible = false
        this.fetchPlatforms()
      } catch (e) {
        this.$message.error('删除失败：' + (e.response?.data?.msg || e.message))
      }
    },
    cancelDelete() {
      this.deleteModalVisible = false
      this.forceDeleteChecked = false
    },
    onPlatformTypeChange() {
      this.formData.authConfig = {}
    },
    getFieldType(field) {
      try {
        const config = JSON.parse(field.remark || '{}')
        return config.type || 'input'
      } catch (e) {
        return 'input'
      }
    },
    isFieldRequired(field) {
      try {
        const config = JSON.parse(field.remark || '{}')
        return config.required === true
      } catch (e) {
        return false
      }
    },
    getFieldPlaceholder(field) {
      try {
        const config = JSON.parse(field.remark || '{}')
        return config.placeholder || ''
      } catch (e) {
        return ''
      }
    },
    getFieldMin(field) {
      try {
        const config = JSON.parse(field.remark || '{}')
        return config.min || 0
      } catch (e) {
        return 0
      }
    },
    getFieldMax(field) {
      try {
        const config = JSON.parse(field.remark || '{}')
        return config.max || 999999
      } catch (e) {
        return 999999
      }
    },
    getFieldOptions(field) {
      try {
        const config = JSON.parse(field.remark || '{}')
        return config.options || []
      } catch (e) {
        return []
      }
    },
    handleCardClick(item) {
      // 跳转到平台设备列表页（不是设备详情页）
      const routePath = `/videoCenter/detail/${item.instanceKey}`
      
      this.$router.push(routePath).catch(err => {
        console.error('路由跳转失败:', err)
        this.$message.error('页面跳转失败，请稍后重试')
      })
    },
    handlePageChange(page, pageSize) {
      this.queryParam.pageNum = page
      this.queryParam.pageSize = pageSize
    },
    handleSizeChange(current, size) {
      this.queryParam.pageNum = 1
      this.queryParam.pageSize = size
      this.fetchPlatforms()
    },
    getPlatformLogo(type) {
      const logoMap = {
        wvp: require('@/assets/video/wvp.svg'),
        isc: require('@/assets/video/hik.svg'),
        icc: require('@/assets/video/dahua.svg')
      }
      return logoMap[type] || require('@/assets/images/logo.png')
    },
    getPlatformTypeColor(type) {
      const colorMap = {
        wvp: 'blue',
        isc: 'green',
        icc: 'orange'
      }
      return colorMap[type] || 'default'
    },
    goDetail(item) {
      this.$router.push({ name: 'VideoPlatformDetail', params: { instanceKey: item.instanceKey } })
    },
    subscriptionTypeOptionsDefault() {
      const fallback = [
        { label: '设备上线', value: 'online' },
        { label: '设备下线', value: 'offline' },
        { label: '通用告警', value: 'alarm' },
        { label: '移动侦测', value: 'motion' },
        { label: '开始录像', value: 'record_start' },
        { label: '停止录像', value: 'record_stop' }
      ]
      return this.subscriptionTypeOptions && this.subscriptionTypeOptions.length ? this.subscriptionTypeOptions : fallback
    }
  }
}
</script>

<style lang="less" scoped>
.video-center {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .left-actions {
    display: flex;
    gap: 8px;
  }

  .right-actions {
    display: flex;
    gap: 8px;
  }
}

.platform-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.platform-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 280px;
  position: relative;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #e6f7ff;
  }
  
  // 禁用状态样式
  &.platform-card-disabled {
    cursor: not-allowed;
    
    &:hover {
      transform: none;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border-color: transparent;
    }
    
    .card-header,
    .card-content {
      cursor: not-allowed;
      pointer-events: none;
    }
  }
}

// 禁用遮罩层
.disabled-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.45);  // 降低透明度，让文字更清晰
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5;
  cursor: not-allowed;  // 显示禁止光标
  
  .disabled-badge {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: rgba(0, 0, 0, 0.75);
    color: white;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    
    .anticon {
      font-size: 14px;
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.card-logo {
  width: 48px;
  height: 48px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 8px;

  img {
    width: 32px;
    height: 32px;
    object-fit: contain;
  }
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  line-height: 1.3;
}

.card-type {
  display: flex;
  align-items: center;
}

.card-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8c8c8c;

  .status-text {
    font-weight: 500;
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-section {
  .config-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 0;

    .anticon {
      color: #1890ff;
      font-size: 14px;
    }
  }

  .config-item {
    font-size: 13px;
    color: #595959;
    line-height: 1.6;
    display: flex;
    gap: 6px;

    .label {
      font-weight: 500;
      color: #8c8c8c;
      min-width: 70px;
    }

    .value {
      flex: 1;
      color: #262626;
      word-break: break-all;
    }
  }
}

.create-info {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #8c8c8c;

    .anticon {
      font-size: 12px;
      color: #bfbfbf;
    }

    .label {
      font-weight: 500;
      min-width: 60px;
    }

    .value {
      color: #595959;
      flex: 1;
    }
  }
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;

  .ant-btn-link {
    padding: 4px 8px;
    height: auto;
    font-size: 12px;
    color: #595959;

    &:hover {
      color: #1890ff;
      background: #f0f8ff;
    }

    .anticon {
      margin-right: 4px;
      font-size: 12px;
    }
  }
}
</style>
