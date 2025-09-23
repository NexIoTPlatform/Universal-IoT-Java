<template>
  <pro-layout
    :menus="menus"
    :collapsed="collapsed"
    :mediaQuery="query"
    :isMobile="isMobile"
    :handleMediaQuery="handleMediaQuery"
    :handleCollapse="handleCollapse"
    :i18nRender="i18nRender"
    :siderWidth="208"
    :logo="logo"
    :title="title"
    v-bind="settings"
  >
    <!-- layout content -->
    <a-layout-content :style="{ height: '100%', margin: '0 0 0px 0'}">
      <multi-tab v-if="settings.multiTab"></multi-tab>
      <transition name="page-transition">
      </transition>
    </a-layout-content>

    <!-- 移除自定义的menuHeaderRender，让ProLayout使用默认的logo渲染 -->

    <!-- 使用框架自带的右侧内容区域 -->
    <template v-slot:rightContentRender>
      <right-content
        :theme="settings.theme"
        :topMenu="settings.layout === 'topmenu'"
        :isMobile="isMobile"
      />
    </template>

    <router-view/>
  </pro-layout>
</template>

<script>
import MultiTab from '@/components/MultiTab'
import {updateTheme} from '@/components/ProLayout'
import {i18nRender} from '@/locales'
import {mapGetters, mapState} from 'vuex'
import {Modal} from 'ant-design-vue'
import {CONTENT_WIDTH_TYPE, SIDEBAR_TYPE, TOGGLE_MOBILE_TYPE} from '@/store/mutation-types'

import defaultSettings from '@/config/defaultSettings'
import RightContent from '@/components/GlobalHeader/RightContent'
import GlobalFooter from '@/components/GlobalFooter'
import Ads from '@/components/Other/CarbonAds'
import {baseMixin} from '@/store/app-mixin'

export default {
  name: 'BasicLayout',
  components: {
    RightContent,
    GlobalFooter,
    Ads,
    MultiTab
  },
  mixins: [baseMixin],
  data() {
    return {
      // service
      menus: [],
      // 侧栏展开状态
      collapsed: false,
      title: defaultSettings.title,
      logo: '/logo.svg',
      settings: {
        // 布局类型
        layout: defaultSettings.layout, // 'sidemenu', 'topmenu'
        // CONTENT_WIDTH_TYPE
        contentWidth: defaultSettings.layout === 'sidemenu' ? CONTENT_WIDTH_TYPE.Fluid
          : defaultSettings.contentWidth,
        // 主题 'dark' | 'light'
        theme: defaultSettings.navTheme,
        // 主色调
        primaryColor: defaultSettings.primaryColor,
        fixedHeader: defaultSettings.fixedHeader,
        fixSiderbar: defaultSettings.fixSiderbar,
        multiTab: defaultSettings.multiTab,
        colorWeak: defaultSettings.colorWeak,

        hideHintAlert: true,
        hideCopyButton: false
      },
      // 媒体查询
      query: {},

      // 是否手机模式
      isMobile: false
    }
  },
  computed: {
    ...mapState({
      // 动态主路由
      mainMenu: state => state.permission.addRouters
    }),
    ...mapGetters([
      'avatar',
      'nickname'
    ])
  },
  created() {
    const routes = this.mainMenu.find(item => item.path === '/')
    // 适配ruoyi一级菜单
    for (let index = 0; index < routes.children.length; index++) {
      let route = routes.children[index]
      if (route.children && route.children.length === 1 && !(route.children.children
        && route.children.children.length > 0) && !route.alwaysShow) {
        route = route.children[0]
        route.children = undefined
      }
      routes.children[index] = route
    }
    this.menus = (routes && routes.children) || []
    // 处理侧栏展开状态
    this.$watch('collapsed', () => {
      this.$store.commit(SIDEBAR_TYPE, this.collapsed)
    })
    this.$watch('isMobile', () => {
      this.$store.commit(TOGGLE_MOBILE_TYPE, this.isMobile)
    })
  },
  mounted() {
    const userAgent = navigator.userAgent
    if (userAgent.indexOf('Edge') > -1) {
      this.$nextTick(() => {
        this.collapsed = !this.collapsed
        setTimeout(() => {
          this.collapsed = !this.collapsed
        }, 16)
      })
    }

    // first update color
    // TIPS: THEME COLOR HANDLER!! PLEASE CHECK THAT!!
    if (process.env.NODE_ENV !== 'production' || process.env.VUE_APP_PREVIEW === 'true') {
      updateTheme(this.settings.primaryColor)
    }
  },
  methods: {
    i18nRender,
    handleMediaQuery(val) {
      this.query = val
      if (this.isMobile && !val['screen-xs']) {
        this.isMobile = false
        return
      }
      if (!this.isMobile && val['screen-xs']) {
        this.isMobile = true
        this.collapsed = false
        this.settings.contentWidth = CONTENT_WIDTH_TYPE.Fluid
        // this.settings.fixSiderbar = false
      }
    },
    handleCollapse(val) {
      this.collapsed = val
    },
    handleToCenter() {
      this.$router.push({path: '/account/center'})
    },
    handleToSettings() {
      this.$router.push({path: '/account/settings'})
    },
    handleLogout(e) {
      Modal.confirm({
        title: '提示',
        content: '确定注销并退出系统吗？',
        onOk: () => {
          return this.$store.dispatch('Logout').then(() => {
            location.href = '/index'
          })
        },
        onCancel() {
        }
      })
    }
  }
}
</script>

<style lang="less">
@import "./BasicLayout.less";
</style>
