<template>
  <div v-if="loading" class="loading-overlay" :class="{ transparent: transparent }">
    <div class="loading-content">
      <a-spin :size="size" :tip="tip">
        <div class="loading-placeholder"></div>
      </a-spin>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoadingOverlay',
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    tip: {
      type: String,
      default: '加载中...'
    },
    size: {
      type: String,
      default: 'large'
    },
    transparent: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style lang="less" scoped>
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(1px);
  transition: opacity 0.3s ease;

  &.transparent {
    background: rgba(255, 255, 255, 0.5);
  }

  .loading-content {
    text-align: center;
  }

  .loading-placeholder {
    width: 60px;
    height: 60px;
  }
}
</style> 