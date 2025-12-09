<template>
  <div class="app-container">
    <a-card :bordered="false">
      <div class="page-header">
        <div class="header-left">
          <a-button type="text" icon="left" @click="$router.back()" class="back-btn" />
          <div class="page-title">
            <h1>{{ networkInfo.name || 'TCP网络组件' }}</h1>
          </div>
          <a-tag :color="networkInfo.state ? 'green' : 'red'" style="margin-left: 12px;">
            {{ networkInfo.state ? '运行中' : '已停止' }}
          </a-tag>
        </div>
      </div>
      
      <a-spin :spinning="loading" tip="Loading...">
        <!-- 自定义标签页导航 -->
        <div class="custom-tabs-container">
          <div class="custom-tabs-nav">
            <div class="custom-tab-item" :class="{ active: activeTab === '1' }" @click="switchTab('1')">
              基础信息
            </div>
            <div class="custom-tab-item" :class="{ active: activeTab === '2' }" @click="switchTab('2')">
              配置信息
            </div>
            <div class="custom-tab-item" :class="{ active: activeTab === '3' }" @click="switchTab('3')">
              通信配置
            </div>
          </div>

          <!-- 标签页内容 -->
          <div class="custom-tab-content">
            <!-- 基础信息 -->
            <div v-show="activeTab === '1'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>基础信息</h3>
                </div>
                
                <div class="basic-info-grid">
                  <div class="info-item">
                    <span class="info-label">组件类型</span>
                    <a-tag color="green">
                      <template v-if="networkInfo.type === 'TCP_CLIENT'">TCP客户端</template>
                      <template v-else-if="networkInfo.type === 'TCP_SERVER'">TCP服务端</template>
                      <template v-else>{{ networkInfo.type }}</template>
                    </a-tag>
                  </div>
                  <div class="info-item info-item-full">
                    <span class="info-label">唯一标识</span>
                    <div class="info-value-group">
                      <span class="info-value">{{ networkInfo.unionId }}</span>
                      <a-button type="text" size="small" class="copy-action-btn" @click.stop="copyToClipboard(networkInfo.unionId)" title="复制">
                        <a-icon type="copy"/>
                      </a-button>
                    </div>
                  </div>
                  <div class="info-item info-item-full">
                    <span class="info-label">产品Key</span>
                    <div class="info-value-group">
                      <span class="info-value">{{ networkInfo.productKey }}</span>
                      <a-button type="text" size="small" class="copy-action-btn" @click.stop="copyToClipboard(networkInfo.productKey)" title="复制">
                        <a-icon type="copy"/>
                      </a-button>
                    </div>
                  </div>
                  <div class="info-item">
                    <span class="info-label">创建时间</span>
                    <span class="info-value">{{ parseTime(networkInfo.createDate) }}</span>
                  </div>
                  <div class="info-item info-item-full" v-if="networkInfo.description">
                    <span class="info-label">描述信息</span>
                    <span class="info-value">{{ networkInfo.description }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 配置信息 -->
            <div v-show="activeTab === '2'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>配置信息</h3>
                  <a-button v-if="!editing" type="link" size="small" @click="editing = true" v-hasPermi="['network:tcp:edit']">
                    <a-icon type="edit" /> 编辑
                  </a-button>
                </div>
                
                <template v-if="!editing">
                  <div class="basic-info-grid">
                    <div class="info-item" v-for="field in configFields" :key="field.key" v-if="!field.hide">
                      <span class="info-label">{{ field.label }}</span>
                      <span class="info-value">{{ renderReadValue(field) }}</span>
                    </div>
                  </div>
                </template>
                
                <template v-else>
                  <a-form :model="formData" layout="vertical">
                    <a-row :gutter="16">
                      <a-col :span="8" v-for="field in configFields" :key="field.key" v-if="!field.hide">
                        <a-form-item :label="field.label">
                          <template v-if="field.type === 'int'">
                            <a-input-number v-model="formData[field.key]" :placeholder="field.remark" style="width:100%" :step="1" :precision="0"/>
                          </template>
                          <template v-else-if="field.type === 'json'">
                            <a-textarea v-model="formData[field.key]" :placeholder="field.remark" :rows="3" />
                          </template>
                          <template v-else-if="field.type === 'select'">
                            <a-select v-model="formData[field.key]" :placeholder="field.remark">
                              <a-select-option v-for="opt in field.options" :key="opt.value" :value="opt.value">
                                {{ opt.label }}
                              </a-select-option>
                            </a-select>
                          </template>
                          <template v-else-if="field.type === 'boolean'">
                            <a-select v-model="formData[field.key]" :placeholder="field.remark">
                              <a-select-option v-for="opt in field.options" :key="opt.value" :value="opt.value">
                                {{ opt.label }}
                              </a-select-option>
                            </a-select>
                          </template>
                          <template v-else>
                            <a-input v-model="formData[field.key]" :placeholder="field.remark"/>
                          </template>
                        </a-form-item>
                      </a-col>
                    </a-row>
                    <div class="form-actions">
                      <a-button @click="cancelEdit">取消</a-button>
                      <a-button type="primary" @click="handleSaveConfig" style="margin-left: 8px;" v-hasPermi="['network:tcp:edit']">
                        保存配置
                      </a-button>
                    </div>
                  </a-form>
                </template>
              </div>
            </div>

            <!-- TCP通信配置 -->
            <div v-show="activeTab === '3'" class="tab-pane">
              <div class="device-basic-info">
                <div class="basic-info-header">
                  <h3>{{ networkInfo.type === 'TCP_CLIENT' ? 'TCP客户端配置' : 'TCP服务端配置' }}</h3>
                  <a-tooltip title="${productKey} 和 ${deviceId} 会被实际值替换">
                    <a-icon type="question-circle" style="color: #999; cursor: pointer;" />
                  </a-tooltip>
                </div>
                
                <template v-if="networkInfo.type === 'TCP_CLIENT'">
                  <div class="basic-info-grid">
                    <div class="info-item">
                      <span class="info-label">连接地址</span>
                      <span class="info-value highlight">{{ getConfigValue(networkInfo, 'host') }}:{{ getConfigValue(networkInfo, 'port') }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">设备标识</span>
                      <span class="info-value code">${productKey}/${deviceId}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">心跳间隔</span>
                      <span class="info-value">{{ getConfigValue(networkInfo, 'heartbeat', '30秒') }}</span>
                    </div>
                  </div>
                </template>

                <template v-if="networkInfo.type === 'TCP_SERVER'">
                  <div class="basic-info-grid">
                    <div class="info-item">
                      <span class="info-label">监听地址</span>
                      <span class="info-value highlight">0.0.0.0:{{ getConfigValue(networkInfo, 'port') }}</span>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </a-card>
    
    <a-modal v-model="previewVisible" title="请确认即将提交的配置JSON" :footer="null" width="600px">
      <pre
        style="background:#f6f6f6;padding:16px;max-height:400px;overflow:auto;">{{
          previewJson
        }}</pre>
      <div style="text-align:right;margin-top:16px;">
        <a-button @click="previewVisible=false">{{ $t('button.cancel') }}</a-button>
        <a-button type="primary" style="margin-left:8px;" v-hasPermi="['network:tcp:edit']" @click="confirmSaveConfig">{{ $t('button.confirm') }}提交
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script>
import {getNetwork, restartNetwork, startNetwork, stopNetwork, updateNetwork} from '@/api/system/network'
import {getDictMap, getDicts} from '@/api/system/dict/data'
import * as echarts from 'echarts'

export default {
  name: 'TcpNetworkDetail',
  data() {
    return {
      loading: false,
      networkInfo: {
        id: undefined,
        type: undefined,
        unionId: undefined,
        productKey: undefined,
        name: undefined,
        description: undefined,
        configuration: '{}',
        state: false,
        createDate: undefined
      },
      configFields: [],
      formData: {},
      editing: false,
      previewVisible: false,
      previewJson: '',
      formDataBackup: {},
      healthData: {},
      activeTab: '1'
    }
  },
  computed: {
    formattedConfig() {
      try {
        const config = JSON.parse(this.networkInfo.configuration)
        return JSON.stringify(config, null, 2)
      } catch (error) {
        return this.networkInfo.configuration
      }
    },
    configCardExtra() {
      if (!this.editing) {
        return this.$createElement('a-button', {
          props: {type: 'link', size: 'small'},
          on: {
            click: () => {
              this.editing = true
            }
          },
          attrs: {icon: 'edit'},
          style: {float: 'right', padding: '0 8px'}
        }, '编辑')
      } else {
        return this.$createElement('span', [
          this.$createElement('a-button', {
            props: {type: 'link', size: 'small'},
            on: {click: this.handleSaveConfig},
            style: {padding: '0 8px'}
          }, '保存'),
          this.$createElement('a-button', {
            style: {marginLeft: '8px', padding: '0 8px'},
            props: {size: 'small', type: 'link'},
            on: {click: this.cancelEdit}
          }, '取消')
        ])
      }
    }
  },
  created() {
    this.getNetworkDetail()
  },
  mounted() {
  },
  methods: {
    /** 获取TCP网络组件详情 */
    getNetworkDetail() {
      const id = this.$route.params.id
      if (!id) {
        this.$message.error('TCP网络组件ID不能为空')
        this.goBack()
        return
      }
      this.loading = true
      getNetwork(id).then(async response => {
        this.networkInfo = response.data
        this.loading = false
        await this.loadConfigFields()
      }).catch(() => {
        this.loading = false
        this.goBack()
      })
    },
    /** 编辑 */
    handleEdit() {
      this.$router.push(`/system/network/tcp/edit/${this.networkInfo.id}`)
    },
    /** 启动/停止 */
    handleToggleState() {
      const action = this.networkInfo.state ? '停止' : '启动'
      this.$confirm({
        title: '确认操作',
        content: `确定要${action}TCP网络组件"${this.networkInfo.name}"吗？`,
        onOk: () => {
          const api = this.networkInfo.state ? stopNetwork : startNetwork
          api(this.networkInfo.id).then(response => {
            this.$message.success(`${action}成功`)
            this.getNetworkDetail()
          })
        }
      })
    },
    /** 重启 */
    handleRestart() {
      this.$confirm({
        title: '确认操作',
        content: `确定要重启TCP网络组件"${this.networkInfo.name}"吗？`,
        onOk: () => {
          restartNetwork(this.networkInfo.id).then(response => {
            this.$message.success('重启成功')
            this.getNetworkDetail()
          })
        }
      })
    },
    /** 复制配置 */
    copyConfig() {
      navigator.clipboard.writeText(this.formattedConfig).then(() => {
        this.$message.success('配置已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },
    /** 下载配置 */
    downloadConfig() {
      const blob = new Blob([this.formattedConfig], {type: 'application/json'})
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${this.networkInfo.name}_config.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      this.$message.success('配置已下载')
    },
    /** 返回 */
    goBack() {
      this.$router.go(-1)
    },
    async loadConfigFields() {
      const res = await getDictMap(['network_tcp_config'])
      const dicts = res.data['network_tcp_config'] || []
      this.configFields = []
      for (const d of dicts) {
        let meta = {}
        if (d.remark && typeof d.remark === 'string') {
          try {
            meta = JSON.parse(d.remark)
          } catch (e) {
            meta = {}
          }
        }
        const field = {
          key: d.dictValue,
          label: d.dictLabel,
          remark: meta.remark || '',
          type: meta.type || 'string',
          hide: meta.hide === 'true',
          options: [],
          default: meta.default
        }
        // select类型需要请求url对应的字典
        if (field.type === 'select' && meta.url) {
          const dictRes = await getDicts(meta.url)
          field.options = (dictRes.data || []).map(
            opt => ({label: opt.dictLabel, value: opt.dictValue}))
        }
        // boolean类型固定是/否
        if (field.type === 'boolean') {
          field.options = [
            {label: '是', value: true},
            {label: '否', value: false}
          ]
        }
        this.configFields.push(field)
      }
      let config = {}
      try {
        config = JSON.parse(this.networkInfo.configuration)
      } catch {
      }
      this.configFields.forEach(f => {
        const val = config[f.key]
        this.$set(this.formData, f.key,
          val !== undefined ? val : f.default !== undefined ? f.default : '')
      })
      console.log('configFields:', this.configFields)
      // 备份初始值用于取消恢复
      this.formDataBackup = JSON.parse(JSON.stringify(this.formData))
    },
    renderReadValue(field) {
      const val = this.formData[field.key]
      if (field.type === 'boolean') {
        return val === true || val === 'true' ? '是' : '否'
      }
      if (field.type === 'select' && field.options && field.options.length) {
        const opt = field.options.find(o => o.value === val)
        return opt ? opt.label : val
      }
      return val
    },
    handleSaveConfig() {
      this.previewJson = JSON.stringify(this.formData, null, 2)
      this.previewVisible = true
    },
    confirmSaveConfig() {
      this.previewVisible = false
      // 修正select/boolean类型字段的值类型
      this.configFields.forEach(field => {
        if ((field.type === 'select' || field.type === 'boolean') && field.options
          && field.options.length) {
          const val = this.formData[field.key]
          // 统一转为option.value的类型
          const match = field.options.find(opt => opt.value === val)
          if (match) {
            this.formData[field.key] = match.value
          }
        }
      })
      updateNetwork({
        id: this.networkInfo.id,
        type: this.networkInfo.type,
        unionId: this.networkInfo.unionId,
        productKey: this.networkInfo.productKey,
        name: this.networkInfo.name,
        description: this.networkInfo.description,
        configuration: JSON.stringify(this.formData)
      }).then(() => {
        this.$message.success('配置保存成功')
        this.editing = false
        this.getNetworkDetail()
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    cancelEdit() {
      this.formData = JSON.parse(JSON.stringify(this.formDataBackup))
      this.editing = false
    },
    /** 通用配置值获取方法 */
    getConfigValue(item, key, defaultValue = '未配置') {
      try {
        const config = JSON.parse(item.configuration)
        return config[key] || defaultValue
      } catch (error) {
        return '配置错误'
      }
    },
    switchTab(tab) {
      this.activeTab = tab
    },
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
    }
  }
}
</script>

<style lang="less" scoped>
/* 页面容器样式 */
.app-container {
  background: #ffffff;
}

.ant-card {
  background: #ffffff;
  border: none;
  box-shadow: none;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e8eaed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

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
  color: #64748b;

  &:hover {
    background: #e2e8f0;
    border-color: #1966ff;
    color: #1966ff;
    transform: scale(1.05);
  }
}

.page-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  line-height: 1.2;
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

/* 设备信息页面样式 */
.device-basic-info {
  background: #ffffff;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;
}

.basic-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.basic-info-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.basic-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.info-item-full {
  grid-column: 1 / -1;
}

.info-label {
  min-width: 90px;
  width: 90px;
  font-size: 13px;
  color: #8c8c8c;
  margin-right: 12px;
  flex-shrink: 0;
  text-align: left;
}

.info-value-group {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.info-value {
  font-size: 13px;
  color: #262626;
  line-height: 1.4;
  
  &.code {
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    color: #1890ff;
  }
  
  &.highlight {
    color: #1890ff;
    font-weight: 500;
  }
}

.copy-action-btn {
  width: 18px;
  height: 18px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  background: transparent;
  border: none;
  transition: all 0.2s ease;
  font-size: 10px;
  margin-left: 2px;
  flex-shrink: 0;
}

.copy-action-btn:hover {
  color: #1890ff;
  background: #f0f8ff;
}

/* 表单操作按钮 */
.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
</style>
