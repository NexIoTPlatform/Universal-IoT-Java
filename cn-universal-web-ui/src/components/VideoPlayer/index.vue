<template>
  <div class="video-player-wrapper">
    <jessibuca 
      ref="player" 
      :videoUrl="streamUrl" 
      :hasAudio="hasAudio"
      :hasScale="true"
      @play="handlePlay"
      @pause="handlePause"
      @stop="handleStop"
    />
    <div v-if="error" class="error-mask">
      <a-icon type="exclamation-circle" style="font-size: 48px; color: #ff4d4f;" />
      <div style="margin-top: 12px; color: #fff;">{{ error }}</div>
    </div>
    <div v-if="loading" class="loading-mask">
      <a-spin size="large" />
      <div style="margin-top: 12px; color: #fff;">加载中...</div>
    </div>
  </div>
</template>

<script>
import Jessibuca from '@/components/Jessibuca'

export default {
  name: 'VideoPlayer',
  components: { Jessibuca },
  props: {
    url: {
      type: String,
      default: ''
    },
    hasAudio: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      streamUrl: '',
      loading: false,
      error: null
    }
  },
  watch: {
    url: {
      handler(val) {
        if (val) {
          this.loadStream(val)
        } else {
          this.streamUrl = ''
          this.error = null
        }
      },
      immediate: true
    }
  },
  methods: {
    loadStream(url) {
      this.loading = true
      this.error = null
      this.streamUrl = url
      setTimeout(() => {
        this.loading = false
      }, 500)
    },
    handlePlay() {
      this.$emit('play')
    },
    handlePause() {
      this.$emit('pause')
    },
    handleStop() {
      this.$emit('stop')
      this.streamUrl = ''
    },
    stop() {
      if (this.$refs.player) {
        this.$refs.player.stop()
      }
    }
  }
}
</script>

<style scoped>
.video-player-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
}

.error-mask,
.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.8);
  z-index: 100;
}
</style>
