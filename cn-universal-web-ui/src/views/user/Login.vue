<template>
  <div class="login-bg">
    <div class="login-container">
      <div class="login-left">
        <div class="logo-title-row">
          <!-- 更大LOGO -->
          <svg class="login-logo" viewBox="0 0 110 110" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="iotGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#6a8dff"/>
                <stop offset="100%" stop-color="#1f26c2"/>
              </linearGradient>
            </defs>
            <rect
              x="30"
              y="30"
              width="50"
              height="50"
              rx="12"
              fill="url(#iotGrad)"/>
            <rect
              x="15"
              y="50"
              width="15"
              height="12"
              rx="3"
              fill="#fff"
              opacity="0.8"/>
            <rect
              x="80"
              y="50"
              width="15"
              height="12"
              rx="3"
              fill="#fff"
              opacity="0.8"/>
            <rect
              x="50"
              y="15"
              width="12"
              height="15"
              rx="3"
              fill="#fff"
              opacity="0.8"/>
            <rect
              x="50"
              y="80"
              width="12"
              height="15"
              rx="3"
              fill="#fff"
              opacity="0.8"/>
            <line
              x1="30"
              y1="55"
              x2="15"
              y2="55"
              stroke="#1f26c2"
              stroke-width="2"/>
            <line
              x1="80"
              y1="55"
              x2="95"
              y2="55"
              stroke="#1f26c2"
              stroke-width="2"/>
            <line
              x1="55"
              y1="30"
              x2="55"
              y2="15"
              stroke="#1f26c2"
              stroke-width="2"/>
            <line
              x1="55"
              y1="80"
              x2="55"
              y2="95"
              stroke="#1f26c2"
              stroke-width="2"/>
            <circle cx="55" cy="55" r="9" fill="#fff" opacity="0.9"/>
          </svg>
          <span class="logo-title">IoT Connector</span>
        </div>
        <div class="login-desc">万物智联，协议主动适配，更不屑于标准</div>
        <div class="login-intro">
          连接万物，智控未来。IoT平台为设备互联、数据采集与智能分析提供一站式解决方案，支持多协议、多插件扩展，强调主动适配各种物联网场景。
        </div>
        <div class="iot-icons">
          <svg viewBox="0 0 32 32" class="iot-icon">
            <circle cx="16" cy="16" r="12" fill="#1f26c2" opacity="0.18"/>
            <rect
              x="13"
              y="7"
              width="6"
              height="6"
              rx="2"
              fill="#6a8dff"/>
            <rect
              x="7"
              y="19"
              width="6"
              height="6"
              rx="2"
              fill="#6a8dff"/>
            <rect
              x="19"
              y="19"
              width="6"
              height="6"
              rx="2"
              fill="#6a8dff"/>
          </svg>
          <svg viewBox="0 0 32 32" class="iot-icon">
            <rect
              x="6"
              y="6"
              width="20"
              height="20"
              rx="6"
              fill="#1f26c2"
              opacity="0.18"/>
            <circle cx="16" cy="16" r="5" fill="#6a8dff"/>
          </svg>
          <svg viewBox="0 0 32 32" class="iot-icon">
            <rect
              x="10"
              y="10"
              width="12"
              height="12"
              rx="4"
              fill="#6a8dff"
              opacity="0.18"/>
            <rect
              x="14"
              y="14"
              width="4"
              height="4"
              rx="2"
              fill="#1f26c2"/>
          </svg>
        </div>
      </div>
      <div class="login-right">
        <a-form-model id="formLogin" ref="form" class="user-layout-login" :model="form"
                      :rules="rules">
          <a-alert
            v-if="isLoginError"
            type="error"
            showIcon
            style="margin-bottom: 24px;"
            :message="loginErrorInfo"
            closable
            :after-close="handleCloseLoginError"
          />
          <div class="title">
            <h2>欢迎登录</h2>
            <p>请输入您的账户信息</p>
          </div>
          <a-form-model-item prop="username">
            <a-input v-model="form.username" size="large" placeholder="请输入用户名">
              <a-icon slot="prefix" type="user" :style="{ color: '#1f26c2' }"/>
            </a-input>
          </a-form-model-item>
          <a-form-model-item prop="password">
            <a-input-password v-model="form.password" size="large" placeholder="请输入密码">
              <a-icon slot="prefix" type="lock" :style="{ color: '#1f26c2' }"/>
            </a-input-password>
          </a-form-model-item>
          <a-row :gutter="16" v-if="captchaOnOff">
            <a-col class="gutter-row" :span="16">
              <a-form-model-item prop="code">
                <a-input v-model="form.code" size="large" type="text" autocomplete="off"
                         placeholder="验证码">
                  <a-icon slot="prefix" type="security-scan" :style="{ color: '#1f26c2' }"/>
                </a-input>
              </a-form-model-item>
            </a-col>
            <a-col class="gutter-row" :span="8">
              <img class="getCaptcha" :src="codeUrl" @click="getCode">
            </a-col>
          </a-row>
          <a-form-model-item prop="rememberMe">
            <a-checkbox :checked="form.rememberMe" @change="rememberMe">记住密码</a-checkbox>
          </a-form-model-item>
          <a-form-item style="margin-top:24px">
            <a-button
              size="large"
              type="primary"
              htmlType="submit"
              class="login-button"
              :loading="logining"
              :disabled="logining"
              @click="handleSubmit"
            >登录
            </a-button>
          </a-form-item>
          <div class="user-login-other">
            <!--
              ruoyi后台不支持获取是否开启账户.
              故此处不做隐藏处理. 在ruoyi原前端中是在注册页面定义一个属性手动修改处理.
            -->
            <!-- <router-link class="register" :to="{ name: 'register' }">注册账户</router-link> -->
          </div>
        </a-form-model>
      </div>
    </div>
  </div>
</template>

<script>
import {mapActions} from 'vuex'
import {timeFix} from '@/utils/util'
import {getCaptchaCode} from '@/api/login'
import {LOGIN_PASSWORD, LOGIN_REMEMBERME, LOGIN_USERNAME} from '@/store/mutation-types'
import storage from 'store'
import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import {validPwdLevel2} from '@/utils/validate'

export default {
  components: {},
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (!validPwdLevel2(value)) {
        callback(new Error('密码中必须包含字母、数字、特殊字符，至少8个字符，最多30个字符'))
      } else {
        callback()
      }
    }
    return {
      codeUrl: '',
      isLoginError: false,
      loginErrorInfo: '',
      rsaKey: undefined,
      form: {
        username: '',
        password: '',
        code: '',
        uuid: '',
        rememberMe: false,
        grant_type: 'captcha',
        client_id: 'web',
        client_secret: 'web',
        scope: 'all'
      },
      rules: {
        username: [{required: true, message: '请输入帐户名', trigger: 'blur'}],
        password: [{required: true, validator: validatePass, trigger: 'blur'}],
        code: [{required: true, message: '请输入验证码', trigger: 'blur'}]
      },
      logining: false,
      captchaOnOff: true
    }
  },
  created() {
    this.getStorage()
    this.getCode()
  },
  methods: {
    getCode() {
      getCaptchaCode().then(res => {
        this.captchaOnOff = res.captchaOnOff === undefined ? true : res.captchaOnOff
        if (this.captchaOnOff) {
          this.codeUrl = 'data:image/gif;base64,' + res.data.img
          this.form.uuid = res.data.uuid
          this.rsaKey = res.data.key
        }
      })
    },
    getStorage() {
      const username = storage.get(LOGIN_USERNAME)
      const password = storage.get(LOGIN_PASSWORD)
      const rememberMe = storage.get(LOGIN_REMEMBERME)
      if (username) {
        this.form.username = username
        this.form.password = password
        this.form.rememberMe = rememberMe
      }
    },
    rememberMe(e) {
      this.form.rememberMe = e.target.checked
    },
    ...mapActions(['Login', 'Logout']),
    handleSubmit() {
      this.logining = true
      this.$refs.form.validate(valid => {
        if (valid) {
          // 密码加密后传输
          const encryptor = new JSEncrypt()
          encryptor.setPublicKey(this.rsaKey)
          const resoucePwd = this.form.password
          this.form.password = encryptor.encrypt(this.form.password)

          if (this.form.rememberMe) {
            storage.set(LOGIN_USERNAME, this.form.username)
            storage.set(LOGIN_PASSWORD, resoucePwd)
            storage.set(LOGIN_REMEMBERME, this.form.rememberMe)
          } else {
            storage.remove(LOGIN_USERNAME)
            storage.remove(LOGIN_PASSWORD)
            storage.remove(LOGIN_REMEMBERME)
          }
          this.Login(this.form)
          .then((res) => this.loginSuccess(res))
          .catch(err => this.requestFailed(err))
          .finally(() => {
            this.form.password = resoucePwd
            this.logining = false
          })
        } else {
          setTimeout(() => {
            this.logining = false
          }, 600)
        }
      })
    },
    loginSuccess(res) {
      // 登录成功后将 access_token 写入 cookie，7天有效
      const token = localStorage.getItem('access_token')
      if (token) {
        document.cookie = 'access_token=' + encodeURIComponent(token) + '; path=/; max-age=' + (7
          * 24 * 60 * 60)
      }
      this.$router.push({path: '/'})
      // 延迟 1 秒显示欢迎信息
      setTimeout(() => {
        this.$notification.success({
          message: '欢迎',
          description: `${timeFix()}，欢迎回来`,
          duration: 5
        })
      }, 1000)
      this.handleCloseLoginError()
    },
    requestFailed(err) {
      this.isLoginError = true
      this.loginErrorInfo = err.response.data.error_description
      this.form.code = undefined
      if (this.captchaOnOff) {
        this.getCode()
      }
    },
    handleCloseLoginError() {
      this.isLoginError = false
      this.loginErrorInfo = ''
    }
  }
}
</script>

<style lang="less" scoped>
.login-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(120deg, #ffffff 0%, #89b1e2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-container {
  display: flex;
  background: transparent;
  border-radius: 18px;
  max-width: 900px;
  width: 100%;
  min-height: 480px;
  overflow: hidden;
}

.login-left {
  flex: 1.2;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 56px 40px 56px 48px;
  text-align: left;
  box-shadow: 8px 0 32px 0 rgba(44, 80, 255, 0.10);
  border-radius: 18px 0 0 18px;
}

.logo-title-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 18px;
}

.login-logo {
  width: 90px;
  height: 90px;
  flex-shrink: 0;
}

.logo-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: #1f26c2;
  letter-spacing: 2px;
  line-height: 1.1;
}

.login-left h1 {
  display: none;
}

.login-desc {
  font-size: 16px;
  color: #6a8dff;
  margin-bottom: 12px;
  font-weight: 600;
}

.login-intro {
  font-size: 14px;
  color: #444;
  margin-bottom: 12px;
  line-height: 1.7;
}

.iot-icons {
  display: flex;
  gap: 12px;
  margin-top: 18px;
  justify-content: flex-start;
}

.iot-icon {
  width: 32px;
  height: 32px;
  display: inline-block;
}

.login-right {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 40px;
  border-radius: 0 18px 18px 0;
  box-shadow: 0 8px 32px rgba(44, 80, 255, 0.13);
}

.user-layout-login {
  width: 100%;
  max-width: 340px;
}

.getCaptcha {
  max-width: 100px;
  max-height: 36px;
  border-radius: 6px;
  margin-left: 8px;
  vertical-align: middle;
  box-shadow: 0 2px 8px rgba(44, 80, 255, 0.06);
}

@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
  }

  .login-left, .login-right {
    padding: 32px 16px;
    border-radius: 18px 18px 0 0;
    box-shadow: none;
  }

  .logo-title-row {
    justify-content: center;
  }

  .iot-icons {
    justify-content: center;
  }

  .login-right {
    border-radius: 0 0 18px 18px;
  }
}
</style>
