<template>
  <div class="right-content-wrapper">
    <a-space size="default" class="header-actions">
      <!-- <a-tooltip placement="bottom">
        <template slot="title">
          源码地址
        </template>
        <a-icon type="gitee" @click="toGitee" :style="{ fontSize: '20px' }"/>
      </a-tooltip> -->
      <a-tooltip placement="bottom">
        <template slot="title">
          文档地址
        </template>
        <a-icon type="book" @click="toDoc" :style="{ fontSize: '20px' }"/>
      </a-tooltip>
      <screenfull/>
      <notice-icon />
      <version-log />
      <!-- 国际化开关 -->
      <select-lang :class="prefixCls" />
      <avatar-dropdown :menu="showMenu" :current-user="currentUser"/>
    </a-space>
  </div>
</template>

<script>
import AvatarDropdown from './AvatarDropdown'
import NoticeIcon from '@/components/NoticeIcon'
import VersionLog from '@/components/VersionLog'
import Screenfull from '@/components/Screenfull'
import SelectLang from '@/components/SelectLang'
import {mapGetters} from 'vuex'

export default {
  name: 'RightContent',
  components: {
    AvatarDropdown,
    NoticeIcon,
    VersionLog,
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
      docUrl: 'https://docs.nexiot.cc/',
      gitee: 'https://gitee.com/NexIoT/Universal-IoT-Java'
    }
  },
  methods: {
    toDoc() {
      window.open(this.docUrl)
    },
    toGitee() {
      window.open(this.gitee)
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

    // 确保所有子元素垂直居中和水平对齐
    > * {
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      height: 32px !important;
      min-width: 32px !important;
    }

    // 图标特殊处理
    .anticon {
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      font-size: 20px !important;
      line-height: 1 !important;
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