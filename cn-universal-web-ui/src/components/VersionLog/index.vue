<template>
  <div>
    <!-- 中间弹窗模式 -->
    <a-modal
      v-model="modalVisible"
      :title="null"
      :footer="null"
      :width="600"
      :centered="true"
      :maskClosable="false"
      :closable="true"
      wrapClassName="version-modal-wrapper"
      @cancel="handleModalClose"
    >
      <div class="version-modal-content">
        <div class="version-modal-header">
          <div class="header-icon">
            <a-icon type="rocket" theme="twoTone" :style="{ fontSize: '48px' }" />
          </div>
          <h2 class="header-title">{{ $t('version.new.release') || '🎉 新版本发布' }}</h2>
          <div class="header-version">
            <a-tag color="blue" style="font-size: 16px; padding: 4px 16px;">{{ currentVersion }}</a-tag>
          </div>
        </div>
        
        <a-spin :spinning="loading">
          <!-- 调试信息 -->
          <div v-if="loading" style="text-align: center; padding: 40px;">
            <a-icon type="loading" />
            <span style="margin-left: 8px;">加载版本信息中...</span>
          </div>
          
          <!-- 公告信息（优先显示，需要开启开关）-->
          <div v-if="!loading && announcement && announcement.enabled !== false" class="announcement-section">
            <div class="announcement-content">
              <div class="announcement-header">
                <h3>{{ announcement.title }}</h3>
              </div>
              <p class="announcement-text">{{ announcement.content }}</p>
              
              <!-- 投票按钮（醒目）-->
              <div class="announcement-actions">
                <a-button 
                  type="primary" 
                  size="large" 
                  icon="like"
                  @click="openVoteLink" 
                  block
                  class="vote-button"
                >
                  {{ announcement.voteText }}
                </a-button>
                
                <!-- 项目地址按钮 -->
                <a-button 
                  type="default" 
                  size="large" 
                  icon="github"
                  @click="openProjectLink" 
                  block
                  class="project-button"
                >
                  {{ announcement.projectText }}
                </a-button>
              </div>
              
              <!-- 重要提示 -->
              <div v-if="announcement.notice" class="announcement-notice">
                <a-alert :message="announcement.notice" type="info" show-icon />
              </div>
            </div>
            
            <!-- 分隔线 -->
            <a-divider v-if="latestVersion">版本更新信息</a-divider>
          </div>
          
          <!-- 最新版本信息 -->
          <div v-if="!loading && latestVersion" class="latest-version-content">
            <div class="version-info-section">
              <div class="version-meta">
                <span class="version-number">{{ latestVersion.version }}</span>
                <span class="version-date">{{ latestVersion.date }}</span>
                <a-tag :color="getVersionColor(latestVersion.type)">{{ getVersionTypeLabel(latestVersion.type) }}</a-tag>
              </div>
              
              <h3 class="version-title">{{ latestVersion.title }}</h3>
              <p class="version-description">{{ latestVersion.description }}</p>
              
              <div v-if="latestVersion.features && latestVersion.features.length" class="version-features">
                <h4>✨ 主要更新</h4>
                <ul>
                  <li v-for="feature in latestVersion.features" :key="feature">{{ feature }}</li>
                </ul>
              </div>
            </div>
            
            <div class="version-actions">
              <a-button type="primary" size="large" @click="markAsReadAndClose" block>
                {{ $t('version.i.know') || '我知道了' }}
              </a-button>
              <a-button type="link" size="large" @click="viewChangelog(latestVersion)" v-if="latestVersion.changelog" block>
                {{ $t('version.view.changelog') || '查看完整更新日志' }}
              </a-button>
            </div>
          </div>
        </a-spin>
      </div>
    </a-modal>
    
    <!-- 右上角图标触发器（保留手动查看功能） -->
    <span @click="openModal" class="version-trigger" ref="versionRef">
      <a-icon style="font-size: 20px;" type="rocket" />
      <a-badge :count="newVersionCount" :offset="[10, 0]" v-if="newVersionCount > 0" />
    </span>
  </div>
</template>

<script>
import versionService from '@/services/versionService'

export default {
  name: 'VersionLog',
  data() {
    return {
      modalVisible: false,
      loading: false,
      currentVersion: '',
      newVersionCount: 0,
      versionList: [],
      latestVersion: null,
      changelogUrl: '',
      hasShownModal: false, // 标记当前会话是否已显示过弹窗
      announcement: null // 公告信息
    }
  },
  async mounted() {
    // 延迟加载并自动显示弹窗
    setTimeout(async () => {
      await this.loadVersionData()
      // 检查是否需要自动弹出
      this.$nextTick(() => {
        this.checkAndShowModal()
      })
    }, 1000)
  },
  created() {
    // 确保版本服务正确初始化
    console.log('VersionLog 组件已创建')
    console.log('versionService:', versionService)
  },
  methods: {
    openModal() {
      // 手动打开弹窗
      this.modalVisible = true
      this.loadVersionData()
    },
    handleModalClose() {
      // 关闭弹窗时标记为已显示
      this.modalVisible = false
      this.hasShownModal = true
    },
    checkAndShowModal() {
      // 检查是否需要自动显示弹窗
      console.log('=== checkAndShowModal 开始检查 ===')
      console.log('hasShownModal:', this.hasShownModal)
      console.log('announcement:', this.announcement)
      console.log('latestVersion:', this.latestVersion)
      console.log('newVersionCount:', this.newVersionCount)
      
      if (this.hasShownModal) {
        console.log('当前会话已显示过弹窗，跳过')
        return
      }
      
      // 优先检查公告（公告开启且设置为每次都显示）
      if (this.announcement && this.announcement.enabled !== false && this.announcement.showOnce === false) {
        // 检查今天是否已经显示过
        const today = new Date().toDateString() // 格式: "Mon Oct 30 2025"
        const lastShownDate = localStorage.getItem('announcement_last_shown_date')
        
        if (lastShownDate === today) {
          console.log('今天已显示过公告，跳过')
          return
        }
        
        console.log('检测到公告（已开启，每次显示），自动显示弹窗')
        this.modalVisible = true
        this.hasShownModal = true
        // 记录今天已显示
        localStorage.setItem('announcement_last_shown_date', today)
        return
      }
      
      if (!this.latestVersion) {
        console.log('没有最新版本数据，跳过')
        return
      }
      
      // 检查最新版本的 isNew 标记
      if (!this.latestVersion.isNew) {
        console.log('最新版本不是新版本，跳过')
        return
      }
      
      // 检查最新版本是否已读
      const isRead = versionService.isVersionRead(this.latestVersion.version)
      console.log(`版本 ${this.latestVersion.version} 是否已读:`, isRead)
      
      if (!isRead) {
        console.log('检测到新版本，自动显示弹窗:', this.latestVersion.version)
        this.modalVisible = true
        this.hasShownModal = true
      } else {
        console.log('最新版本已读，不显示弹窗')
      }
    },
    async loadVersionData() {
      try {
        this.loading = true
        console.log('开始加载版本数据...')
        
        const versionInfo = await versionService.getVersionInfo()
        console.log('版本数据加载成功:', versionInfo)
        
        this.currentVersion = versionInfo.currentVersion || '1.0.0'
        this.versionList = versionInfo.versions || []
        this.changelogUrl = versionInfo.changelogUrl || 'https://gitee.com/NexIoT/Universal-IoT-Java/releases'
        this.announcement = versionInfo.announcement || null // 加载公告
        
        console.log('公告信息:', this.announcement)
        
        // 获取最新版本（第一个版本）
        if (this.versionList.length > 0) {
          this.latestVersion = this.versionList[0]
        }
        
        console.log('当前版本:', this.currentVersion)
        console.log('最新版本:', this.latestVersion)
        console.log('版本列表:', this.versionList)
        console.log('版本数量:', this.versionList.length)
        
        // 延迟更新新版本计数，确保数据加载完成
        this.$nextTick(() => {
          this.newVersionCount = versionService.getNewVersionCount()
          console.log('新版本数量:', this.newVersionCount)
        })
      } catch (error) {
        console.error('Failed to load version data:', error)
        this.$message.error('加载版本信息失败')
        
        // 如果加载失败，使用默认数据
        this.currentVersion = '1.0.0'
        this.versionList = []
        this.latestVersion = null
        this.announcement = null
        this.changelogUrl = 'https://gitee.com/NexIoT/Universal-IoT-Java/releases'
      } finally {
        this.loading = false
      }
    },
    getVersionColor(type) {
      return versionService.getVersionTypeColor(type)
    },
    getVersionTypeLabel(type) {
      return versionService.getVersionTypeLabel(type, this.$i18n.locale)
    },
    markAsReadAndClose() {
      // 标记最新版本为已读并关闭弹窗
      if (this.latestVersion) {
        versionService.markAsRead(this.latestVersion.version)
        this.updateNewVersionCount()
        this.$message.success(`${this.$t('version.marked.read') || '已标记为已读'}: ${this.latestVersion.version}`)
      }
      this.modalVisible = false
    },
    markAsReadAndView(item) {
      // 标记为已读并查看详情
      versionService.markAsRead(item.version)
      this.updateNewVersionCount()
      this.$message.success(`${this.$t('version.marked.read')}: ${item.version}`)
    },
    viewDetails(item) {
      // 查看版本详情
      this.$message.info(`${this.$t('version.view.details')}: ${item.version}`)
      // 标记为已读
      versionService.markAsRead(item.version)
      // 更新新版本计数
      this.updateNewVersionCount()
    },
    viewChangelog(item) {
      // 打开外部更新日志
      if (item.changelog) {
        window.open(item.changelog, '_blank')
        // 标记为已读
        versionService.markAsRead(item.version)
        this.updateNewVersionCount()
      } else {
        this.$message.warning('该版本暂无更新日志')
      }
    },
    viewAllVersions() {
      // 打开所有版本页面
      window.open(this.changelogUrl, '_blank')
    },
    updateNewVersionCount() {
      // 更新新版本计数
      this.newVersionCount = versionService.getNewVersionCount()
    },
    openVoteLink() {
      // 打开投票链接
      if (this.announcement && this.announcement.voteLink) {
        window.open(this.announcement.voteLink, '_blank')
        this.$message.success('感谢您的支持！')
      }
    },
    openProjectLink() {
      // 打开项目链接
      if (this.announcement && this.announcement.projectLink) {
        window.open(this.announcement.projectLink, '_blank')
      }
    }
  }
}
</script>

<style lang="less" scoped>
.version-trigger {
  display: inline-block;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  
  span {
    vertical-align: initial;
  }
  
  &:hover {
    transform: scale(1.1);
  }
}

.version-modal-content {
  .version-modal-header {
    text-align: center;
    padding: 20px 0 24px;
    border-bottom: 1px solid #f0f0f0;
    
    .header-icon {
      margin-bottom: 16px;
      animation: bounce 2s infinite;
    }
    
    .header-title {
      font-size: 24px;
      font-weight: 600;
      margin: 12px 0;
      color: #1890ff;
    }
    
    .header-version {
      margin-top: 12px;
    }
  }
  
  .latest-version-content {
    padding: 24px 0;
    
    .announcement-section {
      margin-bottom: 24px;
      
      .announcement-content {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px;
        padding: 24px;
        color: white;
        margin-bottom: 16px;
        
        .announcement-header {
          text-align: center;
          margin-bottom: 16px;
          
          h3 {
            font-size: 20px;
            font-weight: 600;
            color: white;
            margin: 0;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          }
        }
        
        .announcement-text {
          font-size: 15px;
          line-height: 1.8;
          color: rgba(255, 255, 255, 0.95);
          text-align: center;
          margin-bottom: 20px;
        }
        
        .announcement-actions {
          display: flex;
          flex-direction: column;
          gap: 12px;
          margin-bottom: 16px;
          
          .vote-button {
            height: 50px;
            font-size: 16px;
            font-weight: 600;
            background: #ff4d4f;
            border-color: #ff4d4f;
            box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
            animation: pulse 2s infinite;
            
            &:hover {
              background: #ff7875;
              border-color: #ff7875;
              transform: translateY(-2px);
              box-shadow: 0 6px 16px rgba(255, 77, 79, 0.5);
            }
          }
          
          .project-button {
            height: 44px;
            font-size: 15px;
            background: white;
            color: #667eea;
            border: 2px solid white;
            
            &:hover {
              background: rgba(255, 255, 255, 0.9);
              color: #764ba2;
              transform: translateY(-2px);
            }
          }
        }
        
        .announcement-notice {
          margin-top: 16px;
          
          /deep/ .ant-alert {
            background: rgba(255, 255, 255, 0.15);
            border: 1px solid rgba(255, 255, 255, 0.3);
            
            .ant-alert-message {
              color: white;
              font-weight: 500;
            }
            
            .ant-alert-icon {
              color: white;
            }
          }
        }
      }
    }
    
    .version-info-section {
      margin-bottom: 24px;
      
      .version-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
        
        .version-number {
          font-size: 18px;
          font-weight: 600;
          color: #1890ff;
        }
        
        .version-date {
          font-size: 14px;
          color: #999;
        }
      }
      
      .version-title {
        font-size: 18px;
        font-weight: 600;
        margin: 12px 0;
        color: #333;
      }
      
      .version-description {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
        margin-bottom: 16px;
      }
      
      .version-features {
        background: #f7f9fc;
        border-radius: 8px;
        padding: 16px;
        margin-top: 16px;
        
        h4 {
          font-size: 15px;
          font-weight: 600;
          color: #333;
          margin: 0 0 12px 0;
        }
        
        ul {
          margin: 0;
          padding-left: 20px;
          
          li {
            margin: 8px 0;
            color: #555;
            font-size: 14px;
            line-height: 1.6;
          }
        }
      }
    }
    
    .version-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
    }
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
  }
  50% {
    box-shadow: 0 6px 20px rgba(255, 77, 79, 0.6);
  }
  100% {
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
  }
}
</style>

<style lang="less">
.version-modal-wrapper {
  .ant-modal-header {
    display: none;
  }
  
  .ant-modal-body {
    padding: 24px;
  }
  
  .ant-modal-close {
    top: 16px;
    right: 16px;
  }
}
</style>
