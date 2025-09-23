<template>
  <div class="right-content-wrapper">
    <a-space size="default" class="header-actions">
      <!-- <a-tooltip placement="bottom">
        <template slot="title">
          源码地址
        </template>
        <a-icon type="github" @click="toGithub" :style="{ fontSize: '20px' }"/>
      </a-tooltip> -->
      <!-- <a-tooltip placement="bottom">
        <template slot="title">
          文档地址
        </template>
        <a-icon type="question-circle-o" @click="toDoc" :style="{ fontSize: '20px' }"/>
      </a-tooltip> -->
      <screenfull/>
      <notice-icon v-hasPermi="['system:notice:list']"/>
      <avatar-dropdown :menu="showMenu" :current-user="currentUser"/>
      <!-- 暂只支持中文，国际化可自行扩展 -->
      <!-- 国际化开关 -->
      <!-- <select-lang :class="prefixCls" /> -->
    </a-space>
  </div>
</template>

<script>
import AvatarDropdown from './AvatarDropdown'
import NoticeIcon from '@/components/NoticeIcon'
import Screenfull from '@/components/Screenfull'
import SelectLang from '@/components/SelectLang'
import {mapGetters} from 'vuex'

export default {
  name: 'RightContent',
  components: {
    AvatarDropdown,
    NoticeIcon,
    Screenfull,
    SelectLang
  },
  props: {
    prefixCls: {
      type: String,
      default: 'ant-pro-global-header-index-action'
    },
    isMobile: {
      type: Boolean,
      default: () => false
    },
    topMenu: {
      type: Boolean,
      required: false,
      default: false
    },
    theme: {
      type: String,
      required: false,
      default: 'light'
    }
  },
  computed: {
    ...mapGetters([
      'avatar',
      'nickname'
    ]),
    currentUser() {
      return {
        name: this.nickname,
        avatar: this.avatar
      }
    }
  },
  data() {
    return {
      showMenu: true,
      docUrl: '#',
      githubUrl: '#'
    }
  },
  methods: {
    toDoc() {
      window.open(this.docUrl)
    },
    toGithub() {
      window.open(this.githubUrl)
    }
  }
}
</script>

<style lang="less" scoped>
.right-content-wrapper {
  display: flex !important;
  align-items: center !important;
  height: 64px !important;
  width: 100% !important;

  // 强制右对齐
  justify-content: flex-end !important;
  margin-left: auto !important;

  .header-actions {
    display: flex !important;
    align-items: center !important;
    justify-content: flex-end !important;
    gap: 12px !important;
    height: 100% !important;

    // 确保所有子元素垂直居中
    > * {
      display: flex !important;
      align-items: center !important;
      height: 32px !important;
    }
  }

  // 响应式适配
  @media (max-width: 768px) {
    padding-right: 4px;

    .header-actions {
      gap: 8px !important;
    }
  }
}
</style>

<style lang="less">
// 全局样式，确保在ProLayout中也能正确右对齐
.ant-pro-global-header .right-content-wrapper {
  margin-left: auto !important;
  justify-content: flex-end !important;
}

// 确保ProLayout的右侧内容区域正确对齐
.ant-pro-global-header-index-right {
  display: flex !important;
  align-items: center !important;
  justify-content: flex-end !important;
  margin-left: auto !important;
}
</style>