<template>
  <a-config-provider :locale="locale">
    <div id="app">
      <router-view/>
      <Resizabler ref="resizabler"/>
    </div>
  </a-config-provider>
</template>

<script>
import {domTitle, setDocumentTitle} from '@/utils/domUtil'
import {i18nRender} from '@/locales'
import Resizabler from '@/views/dashboard/components/Resizabler.vue'
import {createWatermark, removeWatermark} from '@/utils/watermark'
import config from '@/config/defaultSettings'

export default {
  components: {Resizabler},
  computed: {
    locale() {
      // 只是为了切换语言时，更新标题
      const {title} = this.$route.meta
      title && (setDocumentTitle(`${i18nRender(title)} - ${domTitle}`))

      return this.$i18n.getLocaleMessage(this.$store.getters.lang).antLocale
    }
  },
  watch: {
    // 监听用户名变化，更新水印
    '$store.state.user.name'(newName) {
      this.initWatermark()
    }
  },
  mounted() {
    this.$bus.$on('resizeabler', (queryName) => {
      this.$refs.resizabler.open(queryName)
    })
    // 初始化水印
    this.initWatermark()
  },
  beforeDestroy() {
    // 组件销毁时移除水印
    removeWatermark()
  },
  methods: {
    /**
     * 初始化水印
     */
    initWatermark() {
      const watermarkConfig = config.watermark || {}
      
      // 如果未启用，移除水印
      if (!watermarkConfig.enabled) {
        removeWatermark()
        return
      }

      // 获取用户名
      const username = this.$store.state.user.name || ''
      
      // 构建水印文本数组
      const textArray = []
      if (username) {
        textArray.push(username)
      }
      if (watermarkConfig.customText) {
        textArray.push(watermarkConfig.customText)
      }

      // 如果没有文本内容，不显示水印
      if (textArray.length === 0) {
        removeWatermark()
        return
      }

      // 创建水印
      createWatermark({
        text: textArray,
        fontSize: watermarkConfig.fontSize,
        fontColor: watermarkConfig.fontColor,
        fontFamily: watermarkConfig.fontFamily,
        gap: watermarkConfig.gap,
        rotate: watermarkConfig.rotate,
        zIndex: watermarkConfig.zIndex,
        enabled: watermarkConfig.enabled
      })
    }
  }
}
</script>
