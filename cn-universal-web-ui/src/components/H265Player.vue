<template>
  <div
    ref="container"
    style="width:100%; height: 100%; background-color: #000000; position: relative;">
    <canvas ref="canvas" style="width:100%; height: 100%;"></canvas>
    <div class="buttons-box" id="h265ButtonsBox">
      <div class="buttons-box-left">
        <i v-if="!playing" class="iconfont icon-play h265-btn" @click="playBtnClick"></i>
        <i v-if="playing" class="iconfont icon-pause h265-btn" @click="pause"></i>
        <i class="iconfont icon-stop h265-btn" @click="stop"></i>
        <i v-if="isNotMute" class="iconfont icon-audio-high h265-btn" @click="mute()"></i>
        <i v-if="!isNotMute" class="iconfont icon-audio-mute h265-btn" @click="cancelMute()"></i>
      </div>
      <div class="buttons-box-right">
        <span class="h265-btn">{{ kBps }} kb/s</span>
        <i class="iconfont icon-camera1196054easyiconnet h265-btn" @click="screenshot" style="font-size: 1rem !important"></i>
        <i class="iconfont icon-shuaxin11 h265-btn" @click="playBtnClick"></i>
      </div>
    </div>
  </div>
</template>

<script>
let player = null

export default {
  name: 'H265Player',
  data() {
    return {
      playing: false,
      isNotMute: false,
      kBps: 0,
      wsUrl: ''
    }
  },
  props: ['videoUrl', 'hasAudio'],
  watch: {
    videoUrl: {
      handler(val) {
        if (val) {
          this.$nextTick(() => {
            this.play(val)
          })
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.loadMissileScript()
  },
  beforeDestroy() {
    this.destroy()
  },
  methods: {
    loadMissileScript() {
      // 检查是否已加载
      if (window.Missile) {
        return
      }
      
      // 动态加载missile.js
      const script = document.createElement('script')
      script.src = '/js/h265/missile-256mb.js'
      script.onload = () => {
        console.log('Missile H265 SDK loaded')
      }
      script.onerror = () => {
        console.error('Failed to load Missile H265 SDK')
        this.$message.error('H265播放器加载失败')
      }
      document.head.appendChild(script)
    },
    
    create() {
      if (!window.Missile) {
        console.error('Missile not loaded')
        this.$message.error('H265 SDK未加载，请刷新页面重试')
        return
      }
      
      try {
        player = new window.Missile({
          canvas: this.$refs.canvas,
          audioEngine: this.hasAudio !== false
        })
        
        // 监听事件
        if (player.on) {
          player.on('play', () => {
            this.playing = true
          })
          
          player.on('pause', () => {
            this.playing = false
          })
        }
      } catch (e) {
        console.error('Create Missile player error:', e)
        this.$message.error('播放器创建失败')
      }
    },
    
    play(url) {
      console.log('H265 play:', url)
      
      if (!url) return
      
      // 如果已有播放器，先销毁
      if (player) {
        this.destroy()
      }
      
      // 等待SDK加载完成
      const checkSDK = () => {
        if (window.Missile) {
          this.create()
          
          if (player && player.load) {
            try {
              player.load(url)
              this.wsUrl = url
              this.playing = true
            } catch (e) {
              console.error('Play error:', e)
              this.$message.error('播放失败：' + e.message)
            }
          }
        } else {
          // SDK还未加载，等待500ms后重试
          setTimeout(checkSDK, 500)
        }
      }
      
      checkSDK()
    },
    
    playBtnClick() {
      if (this.videoUrl) {
        this.play(this.videoUrl)
      }
    },
    
    pause() {
      if (player && player.pause) {
        player.pause()
        this.playing = false
      }
    },
    
    stop() {
      if (player && player.stop) {
        player.stop()
        this.playing = false
      }
    },
    
    mute() {
      if (player && player.mute) {
        player.mute()
        this.isNotMute = false
      }
    },
    
    cancelMute() {
      if (player && player.unmute) {
        player.unmute()
        this.isNotMute = true
      }
    },
    
    screenshot() {
      if (this.$refs.canvas) {
        try {
          const link = document.createElement('a')
          link.download = 'screenshot_' + Date.now() + '.png'
          link.href = this.$refs.canvas.toDataURL()
          link.click()
          this.$message.success('截图成功')
        } catch (e) {
          console.error('Screenshot error:', e)
          this.$message.error('截图失败')
        }
      }
    },
    
    destroy() {
      if (player) {
        try {
          if (player.destroy) {
            player.destroy()
          } else if (player.close) {
            player.close()
          }
        } catch (e) {
          console.error('H265 destroy error:', e)
        }
        player = null
      }
      this.playing = false
    }
  }
}
</script>

<style scoped>
.buttons-box {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  z-index: 10;
}

.buttons-box-left,
.buttons-box-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.h265-btn {
  color: #fff;
  cursor: pointer;
  font-size: 18px;
  transition: color 0.3s;
}

.h265-btn:hover {
  color: #1890ff;
}
</style>
