<template>
  <div class="wrapper" v-if="show" ref="wrapper">
    <vue-resizable
      class="resizabler"
      ref="resizabler"
      :width="`${width}px`"
      :height="`${height}px`"
      :minWidth="300"
      :minHeight="533"
      dragSelector=".top"
      :maximize="isFullscreen"
      :top="top"
      :left="left"
      :fitParent="true"
      @drag:start="dragStart"
    >
      <div class="box">
        <header class="top">
          <div>
            <!-- <a-icon class="back-icon" type="left-square" title="返回文档首页" @click="goHome" /> -->
            {{ isImage ? '产品详情图片' : '产品详情页面' }}
          </div>
          <div>
            <a-icon @click="handleFullScreen" class="icon"
                    :type="isFullscreen ? 'fullscreen-exit' : 'fullscreen'"/>
            <a-icon @click="close" class="icon" type="close"/>
          </div>
        </header>

        <div class="content">
          <!-- 图片显示 -->
          <img v-if="isImage" :src="photoUrl" width="100%" style="max-width: 100%; height: auto;"/>
          <!-- 网页显示 -->
          <iframe v-else ref="iframe"
                  style="pointer-events: auto; width: 100%; height: 100%; border: none;"
                  :src="photoUrl" frameborder="0"></iframe>
        </div>

        <footer class="footer">
          <a v-if="isImage" :href="photoUrl" target="_blank">
            详情图片下载
          </a>
          <a v-else :href="photoUrl" target="_blank">
            在新窗口打开
          </a>
          <a-icon type="link"/>
        </footer>
      </div>
    </vue-resizable>
  </div>
</template>

<script>
import VueResizable from 'vue-resizable'

export default {
  name: 'App',
  components: {
    VueResizable
  },
  data() {
    return {
      show: false,
      url: '',
      isFullscreen: false,
      left: 4,
      top: 100,
      width: 0,
      height: window.innerHeight - 200,
      photoUrl: ''
    }
  },
  computed: {
    isImage() {
      if (!this.photoUrl) {
        return false
      }
      // 检查是否为图片URL
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg']
      const isImageUrl = imageExtensions.some(ext =>
        this.photoUrl.toLowerCase().includes(ext) ||
        this.photoUrl.toLowerCase().startsWith('data:image/')
      )
      return isImageUrl
    }
  },
  mounted() {
    // 设置手机预览的宽高比例（类似iPhone的9:16比例）
    this.width = 375
    this.height = 667
    // 调整初始位置，让弹窗更居中
    this.top = Math.max(50, (window.innerHeight - this.height) / 2)
    this.left = window.innerWidth - this.width - 50
    window.addEventListener('resize', this.winResize)
    document.addEventListener('mousedown', this.handleMousedown)
    document.addEventListener('mouseup', this.handleMouseup)
  },
  methods: {
    handleMousedown() {
      const iframe = this.$refs.iframe
      if (iframe) {
        iframe.style['pointer-events'] = 'none'
      }
    },
    handleMouseup() {
      const iframe = this.$refs.iframe
      if (iframe) {
        iframe.style['pointer-events'] = 'auto'
      }
    },
    winResize() {
      if (!this.resizabler) {
        return
      }
      this.left = window.innerWidth - this.width - 85
      // this.$refs.resizabler.h = window.innerHeight
    },
    open(photoUrl) {
      this.photoUrl = photoUrl
      this.show = true

      // 根据内容类型调整窗口大小，保持手机预览比例
      if (this.isImage) {
        // 图片模式：使用手机预览比例
        this.width = 375
        this.height = 667
      } else {
        // 网页模式：使用手机预览比例，但不超过屏幕限制
        this.width = 375
        this.height = 667
        // 如果屏幕高度不够，按比例缩小
        if (this.height > window.innerHeight - 100) {
          this.height = window.innerHeight - 100
          this.width = Math.round(this.height * 0.5625) // 9:16比例
        }
      }

      // 计算最佳位置，让弹窗更居中
      this.top = Math.max(50, (window.innerHeight - this.height) / 2)
      this.left = window.innerWidth - this.width - 50

      this.$nextTick(() => {
        this.resizabler = this.$refs.resizabler.$el
        this.resizabler.classList.add('resizabler-animate')
        setTimeout(() => {
          this.resizabler.classList.remove('resizabler-animate')
        }, 300)
      })
    },

    close() {
      this.show = false
    },
    dragStart() {
      this.isFullscreen = false
    },
    handleFullScreen() {
      this.isFullscreen = !this.isFullscreen
    },
    // onResizing (left, top, width, height) {
    //   console.log(left, top, width, height)
    // },
    // onResizestop (left, top, width, height) {
    //   console.log(left, top, width, height)
    // },
    goHome() {
      this.url = 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/?t='
        + new Date().getTime()
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.winResize)
    window.removeEventListener('mousedown', this.handleMousedown)
    window.removeEventListener('mouseup', this.handleMouseup)
  }
}
</script>

<style lang="less" scoped>
.wrapper {
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  position: fixed;
  visibility: visible;
  pointer-events: none;
  cursor: move;
  z-index: 3000;
}

.resizabler {
  pointer-events: auto;
}

.box {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.top {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 0 20px;
  height: 48px;
  color: #fff;
  background: #3370ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  border-radius: 16px 16px 0 0;

  .back-icon {
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      transform: scale(1.1);
    }
  }

  .icon {
    margin-left: 12px;
    cursor: pointer;
    padding: 6px;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(255, 255, 255, 0.2);
      transform: scale(1.1);
    }
  }
}

.footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  border-radius: 0 0 16px 16px;

  a {
    margin-right: 6px;
    padding: 8px 16px;
    border-radius: 6px;
    background: #3370ff;
    color: white;
    text-decoration: none;
    transition: background 0.2s ease;

    &:hover {
      background: #2b5cd9;
    }
  }
}

.content {
  width: 100%;
  height: 100%;
  padding: 48px 16px;
  overflow: auto;
  background: #ffffff;

  img {
    display: block;
    margin: 0 auto;
    max-width: 100%;
    height: auto;
    object-fit: contain;
  }

  iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
}

.handle-tm, .handle-mr, .handle-bm, .handle-ml {
  /* visibility: hidden; */
  background-color: transparent;
}

.resizabler-animate {
  transition: all ease .3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 弹窗出现动画 */
.wrapper {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 拖拽手柄样式 */
.handle-tm, .handle-mr, .handle-bm, .handle-ml {
  background-color: #e9ecef;
}

</style>
