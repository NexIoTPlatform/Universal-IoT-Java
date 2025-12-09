<template>
  <div class="multi-video-preview">
    <!-- 工具栏 -->
    <div class="preview-toolbar">
      <a-space>
        <a-radio-group v-model="layout" button-style="solid" @change="handleLayoutChange">
          <a-radio-button value="1x1">
            <a-icon type="border"/>单画面
          </a-radio-button>
          <a-radio-button value="2x2">
            <a-icon type="border"/>4画面
          </a-radio-button>
          <a-radio-button value="3x3">
            <a-icon type="border"/>9画面
          </a-radio-button>
          <a-radio-button value="4x4">
            <a-icon type="border"/>16画面
          </a-radio-button>
        </a-radio-group>

        <a-divider type="vertical"/>

        <a-button size="small" @click="playAll">
          <a-icon type="play-circle"/>全部播放
        </a-button>
        <a-button size="small" @click="stopAll">
          <a-icon type="pause-circle"/>全部停止
        </a-button>
        <a-button size="small" @click="clearAll" type="danger" ghost>
          <a-icon type="close-circle"/>清空
        </a-button>

        <a-divider type="vertical"/>

        <span class="layout-info">当前: {{ layoutInfo }}</span>
      </a-space>
    </div>

    <!-- 视频网格 -->
    <div 
      class="video-grid" 
      :class="`layout-${layout}`"
      :style="{ height: gridHeight + 'px' }"
    >
      <div 
        v-for="index in gridCount" 
        :key="index"
        class="video-cell"
        :class="{ 
          'active': activeCellIndex === index,
          'has-video': cells[index - 1] && cells[index - 1].url,
          'fullscreen': fullscreenIndex === index
        }"
        @click="handleCellClick(index)"
        @dblclick="handleCellDblClick(index)"
      >
        <!-- 播放器 -->
        <div v-if="cells[index - 1] && cells[index - 1].url" class="video-player-wrapper">
          <video-player
            :ref="`player_${index}`"
            :url="cells[index - 1].url"
            :hasAudio="cells[index - 1].hasAudio !== false"
            :autoplay="cells[index - 1].autoplay !== false"
            @play="handleVideoPlay(index)"
            @stop="handleVideoStop(index)"
            @error="handleVideoError(index, $event)"
          />
          
          <!-- 视频信息覆盖层 -->
          <div class="video-overlay">
            <div class="video-title">{{ cells[index - 1].title || `画面 ${index}` }}</div>
            <div class="video-actions">
              <a-tooltip title="关闭">
                <a-icon type="close-circle" @click.stop="removeVideo(index)"/>
              </a-tooltip>
              <a-tooltip title="全屏">
                <a-icon type="fullscreen" @click.stop="handleCellDblClick(index)"/>
              </a-tooltip>
            </div>
          </div>
        </div>

        <!-- 空白占位 -->
        <div v-else class="empty-cell">
          <div class="cell-number">{{ index }}</div>
          <div class="cell-tips">点击添加视频</div>
        </div>
      </div>
    </div>

    <!-- 全屏遮罩 -->
    <div v-if="fullscreenIndex !== null" class="fullscreen-mask" @click="exitFullscreen">
      <div class="fullscreen-content" @click.stop>
        <video-player
          :ref="`fullscreen_player`"
          :url="cells[fullscreenIndex - 1].url"
          :hasAudio="cells[fullscreenIndex - 1].hasAudio !== false"
          :autoplay="true"
        />
        <div class="fullscreen-close" @click="exitFullscreen">
          <a-icon type="close-circle"/>退出全屏
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import VideoPlayer from './VideoPlayer'

export default {
  name: 'MultiVideoPreview',
  components: { VideoPlayer },
  props: {
    // 初始布局
    initialLayout: {
      type: String,
      default: '2x2',
      validator: (value) => ['1x1', '2x2', '3x3', '4x4'].includes(value)
    },
    // 网格高度
    gridHeight: {
      type: Number,
      default: 600
    },
    // 初始视频列表
    videos: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      layout: this.initialLayout,
      cells: [],
      activeCellIndex: null,
      fullscreenIndex: null,
      playerStates: {} // 记录每个播放器的状态
    }
  },
  computed: {
    // 网格数量
    gridCount() {
      const layoutMap = {
        '1x1': 1,
        '2x2': 4,
        '3x3': 9,
        '4x4': 16
      }
      return layoutMap[this.layout] || 4
    },

    // 布局信息
    layoutInfo() {
      const infoMap = {
        '1x1': '1画面',
        '2x2': '4画面 (2x2)',
        '3x3': '9画面 (3x3)',
        '4x4': '16画面 (4x4)'
      }
      const activeCount = this.cells.filter(cell => cell && cell.url).length
      return `${infoMap[this.layout]} | 已加载: ${activeCount}/${this.gridCount}`
    }
  },
  watch: {
    videos: {
      handler(newVideos) {
        if (newVideos && newVideos.length > 0) {
          this.loadVideos(newVideos)
        }
      },
      immediate: true
    }
  },
  methods: {
    // 切换布局
    handleLayoutChange() {
      // 保留已有的视频,如果超出新布局的网格数则截断
      if (this.cells.length > this.gridCount) {
        this.cells = this.cells.slice(0, this.gridCount)
      }
      this.$emit('layoutChange', this.layout)
    },

    // 加载视频列表
    loadVideos(videos) {
      this.cells = []
      videos.slice(0, this.gridCount).forEach((video, index) => {
        this.addVideo(index + 1, video)
      })
    },

    // 添加视频到指定位置
    addVideo(index, videoInfo) {
      const cellIndex = index - 1
      
      if (cellIndex >= this.gridCount) {
        this.$message.warning('超出当前布局最大画面数')
        return false
      }

      this.$set(this.cells, cellIndex, {
        url: videoInfo.url,
        title: videoInfo.title || videoInfo.channelName || `画面 ${index}`,
        hasAudio: videoInfo.hasAudio !== false,
        autoplay: videoInfo.autoplay !== false,
        channelId: videoInfo.channelId,
        deviceId: videoInfo.deviceId,
        ...videoInfo
      })

      this.$emit('videoAdded', { index, videoInfo })
      return true
    },

    // 移除视频
    removeVideo(index) {
      const cellIndex = index - 1
      const videoInfo = this.cells[cellIndex]
      
      // 停止播放
      const playerRef = this.$refs[`player_${index}`]
      if (playerRef && playerRef[0]) {
        playerRef[0].stop()
      }

      this.$set(this.cells, cellIndex, null)
      this.$emit('videoRemoved', { index, videoInfo })
    },

    // 点击单元格
    handleCellClick(index) {
      this.activeCellIndex = index
      const videoInfo = this.cells[index - 1]
      
      if (!videoInfo || !videoInfo.url) {
        // 空白格,触发添加事件
        this.$emit('cellClick', index)
      } else {
        this.$emit('videoClick', { index, videoInfo })
      }
    },

    // 双击单元格 - 全屏
    handleCellDblClick(index) {
      const videoInfo = this.cells[index - 1]
      if (videoInfo && videoInfo.url) {
        this.fullscreenIndex = index
        this.$emit('fullscreenEnter', { index, videoInfo })
      }
    },

    // 退出全屏
    exitFullscreen() {
      this.fullscreenIndex = null
      this.$emit('fullscreenExit')
    },

    // 全部播放
    playAll() {
      this.cells.forEach((cell, index) => {
        if (cell && cell.url) {
          const playerRef = this.$refs[`player_${index + 1}`]
          if (playerRef && playerRef[0] && playerRef[0].play) {
            playerRef[0].play()
          }
        }
      })
      this.$emit('playAll')
    },

    // 全部停止
    stopAll() {
      this.cells.forEach((cell, index) => {
        if (cell && cell.url) {
          const playerRef = this.$refs[`player_${index + 1}`]
          if (playerRef && playerRef[0] && playerRef[0].stop) {
            playerRef[0].stop()
          }
        }
      })
      this.$emit('stopAll')
    },

    // 清空所有视频
    clearAll() {
      this.$confirm({
        title: '确认清空',
        content: '确定要清空所有视频吗?',
        onOk: () => {
          this.stopAll()
          this.cells = []
          this.$emit('clearAll')
        }
      })
    },

    // 视频播放事件
    handleVideoPlay(index) {
      this.$set(this.playerStates, index, 'playing')
      this.$emit('videoPlay', { index, videoInfo: this.cells[index - 1] })
    },

    // 视频停止事件
    handleVideoStop(index) {
      this.$set(this.playerStates, index, 'stopped')
      this.$emit('videoStop', { index, videoInfo: this.cells[index - 1] })
    },

    // 视频错误事件
    handleVideoError(index, error) {
      this.$set(this.playerStates, index, 'error')
      this.$emit('videoError', { index, videoInfo: this.cells[index - 1], error })
    },

    // 交换两个画面位置
    swapVideos(index1, index2) {
      if (index1 < 1 || index1 > this.gridCount || index2 < 1 || index2 > this.gridCount) {
        return false
      }

      const temp = this.cells[index1 - 1]
      this.$set(this.cells, index1 - 1, this.cells[index2 - 1])
      this.$set(this.cells, index2 - 1, temp)
      
      this.$emit('videosSwapped', { index1, index2 })
      return true
    },

    // 获取所有活动的视频
    getActiveVideos() {
      return this.cells
        .map((cell, index) => ({ ...cell, index: index + 1 }))
        .filter(cell => cell && cell.url)
    }
  },
  beforeDestroy() {
    // 组件销毁前停止所有播放
    this.stopAll()
  }
}
</script>

<style lang="less" scoped>
.multi-video-preview {
  .preview-toolbar {
    padding: 12px 16px;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    margin-bottom: 16px;

    .layout-info {
      font-size: 12px;
      color: #666;
    }
  }

  .video-grid {
    display: grid;
    gap: 8px;
    background: #f0f2f5;
    padding: 8px;
    border-radius: 4px;

    &.layout-1x1 {
      grid-template-columns: repeat(1, 1fr);
    }

    &.layout-2x2 {
      grid-template-columns: repeat(2, 1fr);
    }

    &.layout-3x3 {
      grid-template-columns: repeat(3, 1fr);
    }

    &.layout-4x4 {
      grid-template-columns: repeat(4, 1fr);
    }

    .video-cell {
      position: relative;
      background: #000;
      border: 2px solid transparent;
      border-radius: 4px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #1890ff;
      }

      &.active {
        border-color: #1890ff;
        box-shadow: 0 0 8px rgba(24, 144, 255, 0.5);
      }

      &.has-video {
        .video-player-wrapper {
          width: 100%;
          height: 100%;
        }

        .video-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          padding: 8px;
          background: linear-gradient(to bottom, rgba(0,0,0,0.6), transparent);
          display: flex;
          justify-content: space-between;
          align-items: center;
          opacity: 0;
          transition: opacity 0.2s;

          .video-title {
            color: #fff;
            font-size: 12px;
            font-weight: 500;
          }

          .video-actions {
            i {
              color: #fff;
              font-size: 16px;
              margin-left: 8px;
              cursor: pointer;
              transition: color 0.2s;

              &:hover {
                color: #1890ff;
              }
            }
          }
        }

        &:hover .video-overlay {
          opacity: 1;
        }
      }

      .empty-cell {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        background: #1f1f1f;
        color: #666;

        .cell-number {
          font-size: 48px;
          font-weight: 300;
          margin-bottom: 8px;
        }

        .cell-tips {
          font-size: 12px;
          opacity: 0;
          transition: opacity 0.2s;
        }

        &:hover {
          background: #2a2a2a;

          .cell-tips {
            opacity: 1;
          }
        }
      }
    }
  }

  .fullscreen-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.95);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;

    .fullscreen-content {
      position: relative;
      width: 90vw;
      height: 90vh;
    }

    .fullscreen-close {
      position: absolute;
      top: 20px;
      right: 20px;
      padding: 8px 16px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      transition: background 0.2s;

      &:hover {
        background: rgba(24, 144, 255, 0.8);
      }

      i {
        margin-right: 4px;
      }
    }
  }
}
</style>
