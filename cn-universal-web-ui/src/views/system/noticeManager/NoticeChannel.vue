<template>
  <div class="notice-channel">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="left-actions">
        <a-button type="primary" @click="handleAdd" v-hasPermi="['notice:channel:add']">
          <a-icon type="plus"/>
          新增
        </a-button>
        <a-button :disabled="!selectedRowKeys.length" @click="handleEdit"
                  v-hasPermi="['notice:channel:edit']">
          <a-icon type="edit"/>
          修改
        </a-button>
        <a-button :disabled="!selectedRowKeys.length" @click="handleDelete"
                  v-hasPermi="['notice:channel:del']">
          <a-icon type="delete"/>
          删除
        </a-button>
      </div>
      <div class="right-actions">
        <a-button @click="handleRefresh">
          <a-icon type="reload"/>
        </a-button>
      </div>
    </div>

    <!-- 渠道卡片网格 -->
    <div class="channel-grid">
      <div
        v-for="item in channelList"
        :key="item.id"
        class="channel-card"
        :class="{ 'channel-card-selected': selectedRowKeys.includes(item.id) }"
        @click="handleCardClick(item)"
      >
        <!-- 卡片头部：Logo + 名称 + 状态 -->
        <div class="card-header">
          <div class="card-logo">
            <img :src="getChannelLogo(item.channelType)"
                 :alt="getChannelTypeLabel(item.channelType)"/>
          </div>
          <div class="card-info">
            <div class="card-name">{{ item.name }}</div>
            <div class="card-type">
              <a-tag :color="getChannelTypeColor(item.channelType)" size="small">
                {{ getChannelTypeLabel(item.channelType) }}
              </a-tag>
            </div>
          </div>
          <div class="card-status">
            <a-badge :status="item.status === '0' ? 'success' : 'default'"/>
            <span class="status-text">{{ item.status === '0' ? '启用' : '停用' }}</span>
          </div>
        </div>

        <!-- 卡片内容：配置信息 -->
        <div class="card-content">
          <div class="config-section">
            <div class="config-title">
              <a-icon type="setting"/>
              <span>配置信息</span>
            </div>
            <div class="config-preview">
              {{ formatConfigPreview(item.config) }}
            </div>
          </div>

          <!-- 创建信息 -->
          <div class="create-info">
            <div class="info-item">
              <a-icon type="user"/>
              <span class="label">创建人：</span>
              <span class="value">{{ item.creator || '系统' }}</span>
            </div>
            <div class="info-item">
              <a-icon type="clock-circle"/>
              <span class="label">创建时间：</span>
              <span class="value">{{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
            </div>
            <div class="info-item">
              <a-icon type="calendar"/>
              <span class="label">更新时间：</span>
              <span class="value">{{
                  parseTime(item.updateTime, '{y}-{m}-{d} {h}:{i}') || '未更新'
                }}</span>
            </div>
          </div>
        </div>

        <!-- 卡片操作 -->
        <div class="card-actions">
          <a-button type="link" size="small" @click.stop="handleView(item)"
                    v-hasPermi="['notice:channel:detail']">
            <a-icon type="eye"/>
            查看
          </a-button>
          <a-button type="link" size="small" @click.stop="handleEdit(item)"
                    v-hasPermi="['notice:channel:edit']">
            <a-icon type="edit"/>
            修改
          </a-button>
          <a-button type="link" size="small" @click.stop="handleDelete(item)"
                    v-hasPermi="['notice:channel:del']">
            <a-icon type="delete"/>
            删除
          </a-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <a-pagination
        :current="queryParams.pageNum"
        :page-size="queryParams.pageSize"
        :total="total"
        :show-size-changer="true"
        :show-quick-jumper="true"
        :show-total="(total, range) => `共 ${total} 条`"
        @change="handlePageChange"
        @showSizeChange="handleSizeChange"
      />
    </div>

    <!-- 表单弹窗 -->
    <NoticeChannelForm
      :visible="formVisible"
      :title="formTitle"
      :data="formData"
      @submit="handleFormSubmit"
      @cancel="handleFormCancel"
    />
  </div>
</template>

<script>
import {deleteNoticeChannel, listNoticeChannel, saveNoticeChannel} from '@/api/system/noticeManager'
import NoticeChannelForm from './NoticeChannelForm.vue'
import {channelTypes} from './channelConfig'
import {parseTime} from '@/utils/ruoyi'

export default {
  name: 'NoticeChannel',
  components: {
    NoticeChannelForm
  },
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      // 渠道列表
      channelList: [],
      // 总数
      total: 0,
      // 选中的行
      selectedRowKeys: [],
      // 表单弹窗
      formVisible: false,
      formTitle: '新增渠道配置',
      formData: {},
      // 通知渠道
      channelTypes
    }
  },
  created() {
    this.getList()
  },
  methods: {
    parseTime,

    // 获取列表
    async getList() {
      try {
        const response = await listNoticeChannel(this.queryParams)
        this.channelList = response.rows || []
        this.total = response.total || 0
      } catch (error) {
        console.error('获取渠道列表失败:', error)
        this.$message.error('获取渠道列表失败')
      }
    },

    // 新增
    handleAdd() {
      this.formData = {}
      this.formTitle = '新增渠道配置'
      this.formVisible = true
    },

    // 编辑
    handleEdit(item) {
      if (item) {
        this.formData = {...item}
        this.formTitle = '编辑渠道配置'
      } else if (this.selectedRowKeys.length === 1) {
        const selectedItem = this.channelList.find(item => item.id === this.selectedRowKeys[0])
        if (selectedItem) {
          this.formData = {...selectedItem}
          this.formTitle = '编辑渠道配置'
        }
      }
      this.formVisible = true
    },

    // 查看
    handleView(item) {
      this.formData = {...item}
      this.formTitle = '查看渠道配置'
      this.formVisible = true
    },

    // 删除
    async handleDelete(item) {
      const ids = item ? [item.id] : this.selectedRowKeys
      if (!ids.length) {
        this.$message.warning('请选择要删除的渠道配置')
        return
      }

      try {
        await this.$confirm('确认删除选中的渠道配置吗？', '提示', {
          type: 'warning'
        })

        await deleteNoticeChannel(ids.join(','))
        this.$message.success('删除成功')
        this.selectedRowKeys = []
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },

    // 刷新
    handleRefresh() {
      this.getList()
    },

    // 卡片点击
    handleCardClick(item) {
      const index = this.selectedRowKeys.indexOf(item.id)
      if (index > -1) {
        this.selectedRowKeys.splice(index, 1)
      } else {
        this.selectedRowKeys = [item.id]
      }
    },

    // 分页变化
    handlePageChange(page, pageSize) {
      this.queryParams.pageNum = page
      this.queryParams.pageSize = pageSize
      this.getList()
    },

    // 每页条数变化
    handleSizeChange(current, size) {
      this.queryParams.pageNum = 1
      this.queryParams.pageSize = size
      this.getList()
    },

    // 表单提交
    async handleFormSubmit(formData) {
      try {
        await saveNoticeChannel(formData)
        this.$message.success(formData.id ? '修改成功' : '新增成功')
        this.formVisible = false
        this.getList()
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败')
      }
    },

    // 表单取消
    handleFormCancel() {
      this.formVisible = false
    },

    // 获取通知渠道标签
    getChannelTypeLabel(type) {
      const channelType = this.channelTypes.find(t => t.value === type)
      return channelType ? channelType.label : type
    },

    // 获取通知渠道图标
    getChannelTypeIcon(type) {
      const channelType = this.channelTypes.find(t => t.value === type)
      return channelType ? channelType.icon : ''
    },

    // 获取渠道Logo
    getChannelLogo(type) {
      const logoMap = {
        'dingTalk': require('@/assets/notice/钉钉.svg'),
        'email': require('@/assets/notice/邮箱.svg'),
        'feishu': require('@/assets/notice/飞书.svg'),
        'sms_ali': require('@/assets/notice/阿里云.svg'),
        'sms_tencent': require('@/assets/notice/腾讯云.svg'),
        'audio_ali': require('@/assets/notice/阿里云.svg'),
        'audio_tencent': require('@/assets/notice/腾讯云.svg'),
        'webhook': require('@/assets/notice/webhook.svg')
      }
      return logoMap[type] || require('@/assets/notice/邮箱.svg')
    },

    // 获取通知渠道颜色
    getChannelTypeColor(type) {
      const colorMap = {
        dingTalk: 'blue',
        email: 'green',
        feishu: 'purple',
        sms_ali: 'orange',
        sms_tencent: 'cyan',
        audio_ali: 'red',
        audio_tencent: 'pink',
        webhook: 'blue'
      }
      return colorMap[type] || 'default'
    },

    // 格式化配置预览
    formatConfigPreview(config) {
      if (!config) {
        return '无配置'
      }

      try {
        const configObj = typeof config === 'string' ? JSON.parse(config) : config
        const keys = Object.keys(configObj)

        if (keys.length === 0) {
          return '无配置'
        }

        // 显示前两个配置项
        const previewItems = keys.slice(0, 2).map(key => {
          const value = configObj[key]
          if (typeof value === 'string' && value.length > 30) {
            return `${key}: ${value.substring(0, 30)}...`
          }
          return `${key}: ${value}`
        })

        return previewItems.join(', ')
      } catch (error) {
        return '配置格式错误'
      }
    }
  }
}
</script>

<style lang="less" scoped>
.notice-channel {
  // padding: 24px;
  // background: #f5f5f5;
  // min-height: 100vh;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .left-actions {
    display: flex;
    gap: 12px;
  }

  .right-actions {
    display: flex;
    gap: 8px;
  }
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.channel-card {
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

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #e6f7ff;
  }

  &.channel-card-selected {
    border-color: #1890ff;
    box-shadow: 0 8px 24px rgba(24, 144, 255, 0.15);
    background: linear-gradient(135deg, #f0f8ff 0%, #e6f7ff 100%);
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
    margin-bottom: 8px;

    .anticon {
      color: #1890ff;
      font-size: 14px;
    }
  }

  .config-preview {
    font-size: 13px;
    color: #595959;
    line-height: 1.4;
    word-break: break-all;
    background: #f8f9fa;
    padding: 8px 12px;
    border-radius: 6px;
    border-left: 3px solid #1890ff;
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

// 响应式布局
@media (max-width: 1200px) {
  .channel-grid {
    grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  }
}

@media (max-width: 768px) {
  .notice-channel {
    padding: 16px;
  }

  .channel-grid {
    grid-template-columns: 1fr;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .left-actions {
      justify-content: center;
    }

    .right-actions {
      justify-content: center;
    }
  }
}
</style> 