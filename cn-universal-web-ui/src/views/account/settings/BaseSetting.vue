<template>
  <div class="account-settings-info-view">
    <a-row :gutter="16">
      <a-col :md="24" :lg="16">

        <a-form-model
          layout="vertical"
          :rules="rules"
          :model="user"
          ref="form"
        >
          <a-form-model-item
            label="昵称"
            ref="alias"
            prop="alias"
          >
            <a-input v-model="user.alias" placeholder="给自己起个名字" :maxLength="30"/>
          </a-form-model-item>
          <a-form-model-item
            label="手机"
            prop="mobile"
          >
            <a-input v-model="user.mobile" placeholder="请填写手机号"/>
          </a-form-model-item>
          <a-form-model-item
            label="电子邮件"
            prop="email"
          >
            <a-input v-model="user.email" placeholder="请填写手机号邮箱"/>
          </a-form-model-item>

          <a-form-model-item
            label="单点登录"
            prop="exclusiveFirstLogin"
          >
            <a-switch checked-children="开" un-checked-children="关"
                      v-model="user.exclusiveFirstLogin"/>
          </a-form-model-item>

          <a-form-model-item>
            <a-button type="primary" @click="submit">保存</a-button>
          </a-form-model-item>
        </a-form-model>

      </a-col>

    </a-row>

  </div>
</template>

<script>
import AvatarModal from './AvatarModal'
import {getUserProfile, updateUserProfile} from '@/api/system/user'
import {mapGetters} from 'vuex'

export default {
  name: 'BaseSettings',
  components: {
    AvatarModal
  },
  data() {
    return {
      // cropper
      sexValue: {
        men: '0',
        women: '1'
      },
      user: {},
      preview: {},
      option: {
        img: this.avatar,
        info: true,
        size: 1,
        outputType: 'jpeg',
        canScale: false,
        autoCrop: true,
        // 只有自动截图开启 宽度高度才生效
        autoCropWidth: 180,
        autoCropHeight: 180,
        fixedBox: true,
        // 开启宽度和高度比例
        fixed: true,
        fixedNumber: [1, 1]
      },
      rules: {
        alias: [
          {required: true, message: '请输入昵称', trigger: 'blur'}
        ],
        email: [
          {required: true, message: '邮箱不能为空', trigger: 'blur'},
          {
            type: 'email',
            message: '请正确填写邮箱地址',
            trigger: ['blur', 'change']
          }
        ],
        mobile: [
          {required: true, message: '手机号不能为空', trigger: 'blur'},
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请正确填写手机号',
            trigger: 'blur'
          }
        ],
        exclusiveFirstLogin: [
          {required: true, message: '', trigger: 'change'}
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['nickname', 'avatar'])
  },
  mounted() {
    this.getUser()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data.user
        this.roleGroup = response.data.roleGroup
        try {
          const config = JSON.parse(response.data.user.cfg)
          this.$set(this.user, 'exclusiveFirstLogin', config.exclusiveFirstLogin)
        } catch (error) {
          this.$set(this.user, 'exclusiveFirstLogin', false)
        }
        console.log(this.user)
      })
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.user.cfg = JSON.stringify({exclusiveFirstLogin: this.user.exclusiveFirstLogin})
          updateUserProfile(this.user).then(response => {
            this.$notification.open({
              message: '提示',
              description:
                '修改成功',
              icon: <a-icon type="check" style="color: #1966ff"/>,
              duration: 5
            })
          })
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>

.avatar-upload-wrapper {
  height: 200px;
  width: 100%;
}

.ant-upload-preview {
  position: relative;
  margin: 0 auto;
  width: 100%;
  max-width: 180px;
  border-radius: 50%;
  box-shadow: 0 0 4px #ccc;

  .upload-icon {
    position: absolute;
    top: 0;
    right: 10px;
    font-size: 1.4rem;
    padding: 0.5rem;
    background: rgba(222, 221, 221, 0.7);
    border-radius: 50%;
    border: 1px solid rgba(0, 0, 0, 0.2);
  }

  .mask {
    opacity: 0;
    position: absolute;
    background: rgba(0, 0, 0, 0.4);
    cursor: pointer;
    transition: opacity 0.4s;

    &:hover {
      opacity: 1;
    }

    i {
      font-size: 2rem;
      position: absolute;
      top: 50%;
      left: 50%;
      margin-left: -1rem;
      margin-top: -1rem;
      color: #d6d6d6;
    }
  }

  img, .mask {
    width: 100%;
    max-width: 180px;
    height: 100%;
    border-radius: 50%;
    overflow: hidden;
  }
}
</style>
