<template>
  <div class="wrapper" v-if="show" ref="wrapper">
    <vue-resizable
      class="resizabler"
      ref="resizabler"
      :width="`${width}px`"
      :height="`${height}px`"
      :minWidth="100"
      :minHeight="200"
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
            帮助中心
          </div>
          <div>
            <a-icon @click="handleFullScreen" class="icon"
                    :type="isFullscreen ? 'fullscreen-exit' : 'fullscreen'"/>
            <a-icon @click="close" class="icon" type="close"/>
          </div>
        </header>

        <div class="content">
          <iframe ref="iframe" style="pointer-events: auto;" :src="url" frameborder="0"></iframe>
        </div>

        <footer class="footer">
          <a :href="url" target="_blank">
            帮助中心打开
          </a>
          <a-icon type="link"/>
        </footer>
      </div>
    </vue-resizable>
  </div>
</template>

<script>
import VueResizable from 'vue-resizable'

const URL_MAP = {
  创建应用: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/application/application?id=%e5%88%9b%e5%bb%ba%e5%ba%94%e7%94%a8',
  设备添加: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/instance/addinstance',
  数据订阅: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/application/application?id=%e5%ba%94%e7%94%a8%e8%af%a6%e6%83%85',
  创建产品: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/product/addproduct',
  定义物模型: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/metadata/metadata?id=%e7%89%a9%e6%a8%a1%e5%9e%8b%e6%a6%82%e8%a7%88',
  写编解码插件: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/protocol/protocol?id=%e5%88%9b%e5%bb%ba%e5%8d%8f%e8%ae%ae',
  调试验证: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/protocol/protocol?id=%e5%8d%8f%e8%ae%ae%e8%b0%83%e8%af%95'
}

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
      height: window.innerHeight - 200
    }
  },
  mounted() {
    if (this.width < 400) {
      this.width = 360
    }
    if (this.width > 600) {
      this.width = 600
    }
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
    open(queryName) {
      this.left = window.innerWidth - 85
      this.url = URL_MAP[queryName]
      if (!this.url) {
        this.url = queryName
      }
      this.show = true
      this.$nextTick(() => {
        this.resizabler = this.$refs.resizabler.$el
        this.resizabler.classList.add('resizabler-animate')
        this.left = window.innerWidth - this.width - 85
        setTimeout(() => {
          this.resizabler.classList.remove('resizabler-animate')
        }, 300)
      })
    },

    close() {
      if (!this.resizabler) {
        return
      }
      this.resizabler.classList.add('resizabler-animate')
      this.left = window.innerWidth - 85
      setTimeout(() => {
        this.resizabler.classList.remove('resizabler-animate')
        this.show = false
      }, 300)
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
  /* border-radius: 4px; */
  overflow: hidden;
  box-shadow: rgba(0, 0, 0, 0.21) 0px 4px 16px 0px;
}

.top {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 0 15px;
  height: 32px;
  color: #fff;
  background-color: #222;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: bold;
  // cursor: default;
  .back-icon {
    cursor: pointer;
  }

  .icon {
    margin-left: 8px;
    cursor: pointer;
  }
}

.footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background-color: #fff;
  box-shadow: 0 1px 12px 0px #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 15px;
  color: #1966ff;
  font-size: 14px;

  a {
    margin-right: 4px;
  }
}

.content {
  width: 100%;
  height: 100%;
  padding: 40px 0 40px 10px;

  iframe {
    width: 100%;
    height: 100%;
  }
}

.handle-tm, .handle-mr, .handle-bm, .handle-ml {
  /* visibility: hidden; */
  background-color: transparent;
}

.resizabler-animate {
  transition: left ease .3s;
}

</style>
