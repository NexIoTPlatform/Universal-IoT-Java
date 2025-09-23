<template>
  <div class="box">
    <header class="header">
      <h3>{{ title }}</h3>
      <div v-if="$slots.extra" class="header-extra">
        <slot name="extra"/>
      </div>
    </header>
    <div class="content">
      <slot/>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped lang="less">
.box {
  width: 100%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-left: 22px;
    padding-right: 22px;
    height: 50px;
    line-height: 50px;
    border-bottom: 1px solid #F2F3F5;

    h3 {
      height: 100%;
      font-size: 16px;
      font-weight: 600;
      padding-left: 8px;
      position: relative;
      margin: 0;

      &::before {
        content: '';
        display: inline-block;
        width: 3px;
        height: 18px;
        background-color: #1966ff;
        position: absolute;
        left: 0;
        top: calc(50% - 9px);
      }
    }

    .header-extra {
      display: flex;
      align-items: center;
    }
  }

  .content {
    padding: 20px 22px;
  }
}
</style>
