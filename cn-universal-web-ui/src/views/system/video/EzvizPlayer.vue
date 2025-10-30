<template>
  <div
    style="width:100vw;height:100vh;background:#000;display:flex;align-items:center;justify-content:center;">
    <div id="ezviz-container" style="width:100%;height:100%;"></div>
  </div>
</template>

<script>
import EzuikitFlv from 'ezuikit-flv'

export default {
  name: 'EzvizPlayer',
  data() {
    return {player: null}
  },
  mounted() {
    const q = this.$route.query || {}
    const url = q.url || ''
    if (!url) {
      this.$message.error('缺少萤石播放地址')
      return
    }
    const decoder = q.decoder || '/ezviz/decoder.js'
    try {
      this.player = new EzuikitFlv({
        url,
        container: 'ezviz-container',
        decoder,
        useMSE: true,
        isNotMute: true,
        autoPlay: true
      })
      this.player.play()
    } catch (e) {
      this.$message.error('初始化萤石播放器失败')
    }
  },
  beforeDestroy() {
    try {
      this.player && this.player.destroy && this.player.destroy()
    } catch (e) {
    }
    this.player = null
  }
}
</script>
