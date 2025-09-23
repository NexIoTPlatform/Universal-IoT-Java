<template>
  <div id="userLayout" :class="['user-layout-wrapper', isMobile && 'mobile']">

    <div class="container">

      <router-view/>

      <div class="footer">
        <div class="copyright">

          Copyright &copy; 2025 <a href="" target="_blank">IoT Universal</a>
          <p>
            <!-- <a href="https://beian.miit.gov.cn/" target="__blank">xxxx-2</a> -->
          </p>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import {deviceMixin} from '@/store/device-mixin'

export default {
  name: 'UserLayout',
  mixins: [deviceMixin],
  mounted() {
    document.body.classList.add('userLayout')
  },
  beforeDestroy() {
    document.body.classList.remove('userLayout')
  }
}
</script>

<style lang="less" scoped>
#userLayout.user-layout-wrapper {
  height: 100%;
  min-height: 100vh;

  &.mobile {
    .container {
      .main {
        max-width: 368px;
        width: 98%;
      }
    }
  }

  .container {
    width: 100%;
    min-height: 100%;
    background: linear-gradient(to right, #1464FF, #3B7EFF);
    position: relative;
    display: flex;
    align-items: center;

    a {
      text-decoration: none;
    }

    .main {
      min-width: 260px;
      width: 368px;
      position: absolute;
      right: 15%;
    }

    .footer {
      position: absolute;
      width: 100%;
      bottom: 0;
      padding: 0 16px;
      margin: 48px 0 24px;
      text-align: center;

      .copyright {
        color: #333;
        font-size: 14px;

        a {
          color: #333;
        }

        p {
          margin-top: 10px;
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>
