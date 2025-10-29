<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <a-tabs v-model="activeTab" type="card">
        <a-tab-pane key="channel" tab="通知渠道配置">
          <NoticeChannel/>
        </a-tab-pane>
        <a-tab-pane key="template" tab="通知模板">
          <NoticeTemplate/>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import NoticeChannel from './NoticeChannel.vue'
import NoticeTemplate from './NoticeTemplate.vue'

export default {
  name: 'NoticeManager',
  components: {
    NoticeChannel,
    NoticeTemplate
  },
  data() {
    return {
      activeTab: 'channel'
    }
  }
}
</script>

<style lang="less" scoped>
.ant-tabs-card > .ant-tabs-nav .ant-tabs-tab {
  padding: 8px 16px;
}
</style> 