<template>
  <div
    ref="container"
    @dblclick="fullscreenSwich"
    style="width:100%; height: 100%; background-color: #000000;margin:0 auto;position: relative;">
    <div style="width:100%; padding-top: 56.25%; position: relative;"></div>
    <div class="buttons-box" id="buttonsBox">
      <div class="buttons-box-left">
        <i v-if="!playing" class="iconfont icon-play jessibuca-btn" @click="playBtnClick"></i>
        <i v-if="playing" class="iconfont icon-pause jessibuca-btn" @click="pause"></i>
        <i class="iconfont icon-stop jessibuca-btn" @click="stop"></i>
        <i v-if="isNotMute" class="iconfont icon-audio-high jessibuca-btn" @click="mute()"></i>
        <i v-if="!isNotMute" class="iconfont icon-audio-mute jessibuca-btn"
           @click="cancelMute()"></i>
        <select @change="scaleChanged" v-if="hasScale">
          <option value="0.25">0.25</option>
          <option value="0.5">0.5</option>
          <option selected="selected" value="1.0">1.0</option>
          <option value="2.0">2.0</option>
          <option value="4.0">4.0</option>
        </select>
      </div>
      <div class="buttons-box-right">
        <span class="jessibuca-btn">{{ kBps }} kb/s</span>
        <i
          class="iconfont icon-camera1196054easyiconnet jessibuca-btn"
          @click="screenshot"
          style="font-size: 1rem !important"></i>
        <i class="iconfont icon-shuaxin11 jessibuca-btn" @click="playBtnClick"></i>
        <i v-if="!fullscreen" class="iconfont icon-weibiaoti10 jessibuca-btn"
           @click="fullscreenSwich"></i>
        <i v-if="fullscreen" class="iconfont icon-weibiaoti11 jessibuca-btn"
           @click="fullscreenSwich"></i>
      </div>
    </div>
  </div>
</template>

<script>
const jessibucaPlayer = {}
export default {
  name: 'Jessibuca',
  data() {
    return {
      playing: false,
      isNotMute: false,
      quieting: false,
      fullscreen: false,
      loaded: false,
      speed: 0,
      performance: '',
      kBps: 0,
      btnDom: null,
      videoInfo: null,
      volume: 1,
      rotate: 0,
      vod: true,
      forceNoOffscreen: false,
      playerTime: 0 // 播放器时间（毫秒）
    }
  },
  props: ['videoUrl', 'error', 'hasAudio', 'height', 'hasScale'],
  created() {
    const paramUrl = decodeURIComponent(this.$route.params.url)
    this.$nextTick(() => {
      this.updatePlayerSize()
      window.onresize = this.updatePlayerSize
      if (typeof (this.videoUrl) === 'undefined') {
        this.videoUrl = paramUrl
      }
      this.btnDom = document.getElementById('buttonsBox')
    })
  },
  mounted() {
    this.updatePlayerSize()
  },
  watch: {
    videoUrl: {
      handler(val, oldVal) {
        // 只有当 URL 有效且发生变化时才播放，避免播放空 URL
        // 忽略从 undefined/null 到 undefined/null 的变化
        if (val && typeof val === 'string' && val.trim() !== '' && val !== oldVal) {
          console.log('Jessibuca watch videoUrl 变化，准备播放:', val)
          this.$nextTick(() => {
            this.play(val)
          })
        } else if (!val || (typeof val === 'string' && val.trim() === '')) {
          console.log('Jessibuca watch videoUrl 为空，跳过播放:', val)
        }
      },
      immediate: false // 改为 false，避免初始化时触发空 URL 播放
    }
  },
  methods: {
    scaleChanged(e) {
      this.$emit('scale', e.target.value)
    },
    updatePlayerSize() {
      // const dom = this.$refs.container
      // let width = dom.parentNode.clientWidth
      // let height = (9 / 16) * width
      //
      // if (height > dom.clientHeight) {
      //   height = dom.clientHeight
      //   width = (16 / 9) * height
      // }
      // if (width > 0 && height > 0) {
      //   dom.style.width = width + 'px'
      //   dom.style.height = height + 'px'
      //   dom.style.paddingTop = 0
      // }
    },
    create() {
      const options = {
        container: this.$refs.container,
        videoBuffer: 0.2,
        isResize: false,
        decoder: '/js/jessibuca3-3-22/decoder.js',
        useMSE: true,
        useWCS: location.hostname === 'localhost' || location.protocol === 'https:',
        text: '',
        loadingText: '请稍等, 视频加载中......',
        debug: false,
        showBandwidth: false,
        operateBtns: {
          fullscreen: false,
          screenshot: false,
          play: false,
          audio: false,
          record: false
        },
        hasAudio: typeof (this.hasAudio) === 'undefined' ? true : this.hasAudio,
        supportDblclickFullscreen: false,
        heartTimeout: 5,
        heartTimeoutReplay: true,
        heartTimeoutReplayTimes: 3,
        loadingTimeout: 10,
        loadingTimeoutReplay: true,
        loadingTimeoutReplayTimes: 3,
        timeout: 10
      }
      console.log('Jessibuca -> options: ', options)
      jessibucaPlayer[this._uid] = new window.Jessibuca({...options})

      const jessibuca = jessibucaPlayer[this._uid]
      const _this = this
      jessibuca.on('pause', function () {
        _this.playing = false
      })
      jessibuca.on('play', function () {
        _this.playing = true
      })
      jessibuca.on('fullscreen', function (msg) {
        _this.fullscreen = msg
      })
      jessibuca.on('mute', function (msg) {
        _this.isNotMute = !msg
      })
      jessibuca.on('performance', function (performance) {
        let show = '卡顿'
        if (performance === 2) {
          show = '非常流畅'
        } else if (performance === 1) {
          show = '流畅'
        }
        _this.performance = show
      })
      jessibuca.on('kBps', function (kBps) {
        _this.kBps = Math.round(kBps)
      })
      jessibuca.on('videoInfo', function (msg) {
        console.log('Jessibuca -> videoInfo: ', msg)
      })
      jessibuca.on('audioInfo', function (msg) {
        console.log('Jessibuca -> audioInfo: ', msg)
      })
      jessibuca.on('error', function (msg) {
        console.log('Jessibuca -> error: ', msg)
      })
      jessibuca.on('timeout', function (msg) {
        console.log('Jessibuca -> timeout: ', msg)
      })
      jessibuca.on('loadingTimeout', function (msg) {
        console.log('Jessibuca -> timeout: ', msg)
      })
      // 添加时间更新事件（参考 wvp 官方实现）
      jessibuca.on('timeUpdate', (videoPTS) => {
        if (jessibuca.videoPTS !== undefined) {
          _this.playerTime += (videoPTS - jessibuca.videoPTS) * 1000 // 转换为毫秒
          _this.$emit('playTimeChange', _this.playerTime)
        }
        jessibuca.videoPTS = videoPTS
      })
    },
    playBtnClick: function (event) {
      // 确保 videoUrl 存在才播放
      if (this.videoUrl && this.videoUrl.trim() !== '') {
        this.play(this.videoUrl)
        this.$emit('play')
      } else {
        console.warn('playBtnClick: videoUrl 为空，无法播放')
      }
    },
    play: async function (url) {
      console.log('Jessibuca -> play called with url: ', url)
      
      // 验证 URL 是否有效
      if (!url || (typeof url === 'string' && url.trim() === '')) {
        console.error('Jessibuca -> play: URL 为空，无法播放', { url, type: typeof url })
        return
      }
      
      try {
        if (jessibucaPlayer[this._uid]) {
          await this.destroy()
        }
        this.create()
        
        if (!jessibucaPlayer[this._uid]) {
          console.error('Jessibuca -> play: 播放器创建失败')
          return
        }
        
        // 重置播放器时间
        this.playerTime = 0
        if (jessibucaPlayer[this._uid].videoPTS !== undefined) {
          jessibucaPlayer[this._uid].videoPTS = undefined
        }
        
        jessibucaPlayer[this._uid].on('play', () => {
          this.playing = true
          this.loaded = true
        })
        
        if (jessibucaPlayer[this._uid].hasLoaded()) {
          jessibucaPlayer[this._uid].play(url)
        } else {
          jessibucaPlayer[this._uid].on('load', () => {
            jessibucaPlayer[this._uid].play(url)
          })
        }
      } catch (error) {
        console.error('Jessibuca -> play 异常:', error)
      }
    },
    pause: function () {
      if (jessibucaPlayer[this._uid]) {
        jessibucaPlayer[this._uid].pause()
        this.$emit('pause')
      }
      this.playing = false
      this.err = ''
      this.performance = ''
    },
    screenshot: function () {
      if (jessibucaPlayer[this._uid]) {
        jessibucaPlayer[this._uid].screenshot()
      }
    },
    mute: function () {
      if (jessibucaPlayer[this._uid]) {
        jessibucaPlayer[this._uid].mute()
      }
    },
    cancelMute: function () {
      if (jessibucaPlayer[this._uid]) {
        jessibucaPlayer[this._uid].cancelMute()
      }
    },
    stop() {
      this.$emit('stop')
      this.destroy()
    },
    async destroy() {
      if (jessibucaPlayer[this._uid]) {
        await jessibucaPlayer[this._uid].destroy()
      }
      // 检查DOM是否存在，避免在组件销毁时出错
      if (this.$refs.container && document.getElementById('buttonsBox') == null) {
        this.$refs.container.appendChild(this.btnDom)
      }
      jessibucaPlayer[this._uid] = null
      this.playing = false
      this.err = ''
      this.performance = ''
    },
    fullscreenSwich: function () {
      if (!jessibucaPlayer[this._uid]) {
        console.warn('Jessibuca player 未初始化，无法切换全屏')
        return
      }
      const isFull = this.isFullscreen()
      jessibucaPlayer[this._uid].setFullscreen(!isFull)
      this.fullscreen = !isFull
    },
    isFullscreen: function () {
      return document.fullscreenElement ||
        document.msFullscreenElement ||
        document.mozFullScreenElement ||
        document.webkitFullscreenElement || false
    }
  },
  destroyed() {
    if (jessibucaPlayer[this._uid]) {
      jessibucaPlayer[this._uid].destroy()
      this.$emit('stop')
    }
    this.playing = false
    this.loaded = false
    this.performance = ''
    window.onresize = null
  }
}
</script>

<style>
.buttons-box {
  width: 100%;
  height: 28px;
  background-color: rgba(43, 51, 63, 0.7);
  position: absolute;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  left: 0;
  bottom: 0;
  user-select: none;
  z-index: 10;
}

.jessibuca-btn {
  width: 20px;
  color: rgb(255, 255, 255);
  line-height: 27px;
  margin: 0px 10px;
  padding: 0px 2px;
  cursor: pointer;
  text-align: center;
  font-size: 0.8rem !important;
}

.buttons-box-right {
  position: absolute;
  right: 0;
}
</style>
