import storage from 'store'
import {getInfo, login, logout} from '@/api/login'
import {ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN} from '@/store/mutation-types'

const user = {
  state: {
    token: '',
    name: '',
    welcome: '',
    avatar: '',
    roles: [],
    info: {},
    parentUnionId: null,
    currentAdmin: false
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_CURRENTADMIN: (state, currentAdmin) => {
      state.currentAdmin = currentAdmin
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      state.info = info
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    },
    SET_PARENT: (state, parentUnionId) => {
      state.parentUnionId = parentUnionId
    }
  },

  actions: {
    // 登录
    Login({commit, dispatch}, userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo).then(res => {
          const stramp = +new Date() + res.expires_in * 1000
          storage.set(ACCESS_TOKEN, res.access_token, 7 * 24 * 60 * 60 * 1000)
          storage.set(REFRESH_TOKEN, res.refresh_token, 7 * 24 * 60 * 60 * 1000)
          storage.set(EXPIRES_IN, stramp, 7 * 24 * 60 * 60 * 1000)
          commit('SET_TOKEN', res.access_token)
          resolve()
        })
        .catch(error => {
          reject(error)
        })
      })
    },

    // 半小时刷新一次token
    RefreshToken({commit, dispatch, state}, immediate) {
      const stramp = storage.get(EXPIRES_IN)
      if (+new Date() > stramp) {
        return
      }
      const refreshToken = storage.get(REFRESH_TOKEN)
      const params = {
        grant_type: 'refresh_token',
        client_id: 'web',
        client_secret: 'web',
        refresh_token: refreshToken,
        username: state.name
      }

      function refresh() {
        login(params).then(res => {
          storage.set(ACCESS_TOKEN, res.access_token, 7 * 24 * 60 * 60 * 1000)
          storage.set(REFRESH_TOKEN, res.refresh_token, 7 * 24 * 60 * 60 * 1000)
          storage.set(EXPIRES_IN, (+new Date() + res.expires_in * 1000),
            7 * 24 * 60 * 60 * 1000)
          commit('SET_TOKEN', res.access_token)
        })
      }

      if (immediate) {
        refresh()
      }
      if (window.refresh_token_timer) {
        clearTimeout(window.refresh_token_timer)
      }
      window.refresh_token_timer = setTimeout(() => {
        refresh()
        dispatch('RefreshToken')
      }, 1800 * 1000)
    },

    // 获取用户信息
    GetInfo({commit, state}) {
      return new Promise((resolve, reject) => {
        getInfo(state.token).then(res => {
          const user = res.data.user
          const avatar = !user.avatar ? require(
            '@/assets/images/user-avatar.svg') : process.env.VUE_APP_BASE_API
            + user.avatar
          if (res.data.roles && res.data.roles.length > 0) { // 验证返回的roles是否是一个非空数组
            commit('SET_ROLES', res.data.roles)
            commit('SET_PERMISSIONS', res.data.permissions)
          } else {
            commit('SET_ROLES', ['ROLE_DEFAULT'])
          }
          commit('SET_CURRENTADMIN', user.admin)
          commit('SET_PARENT', user.parentUnionId)
          commit('SET_NAME', user.username)
          commit('SET_AVATAR', avatar)
          // console.log(user)
          // console.log(user.parentUnionId)
          resolve(res.data)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 登出
    Logout({commit, state}) {
      return new Promise((resolve, reject) => {
        logout().then(() => {
          resolve()
        }).catch(error => {
          reject(error)
        }).finally(() => {
          setTimeout(() => {
            commit('SET_TOKEN', '')
            commit('SET_ROLES', [])
            commit('SET_PERMISSIONS', [])
            storage.remove(ACCESS_TOKEN)
            storage.remove(REFRESH_TOKEN)
            storage.remove(EXPIRES_IN)
            resolve()
          }, 0)
        })
        // setTimeout(() => {
        //   commit('SET_TOKEN', '')
        //   commit('SET_ROLES', [])
        //   commit('SET_PERMISSIONS', [])
        //   storage.remove(ACCESS_TOKEN)
        //   storage.remove(REFRESH_TOKEN)
        //   storage.remove(EXPIRES_IN)
        //   resolve()
        // }, 0)
      })
    }

  }
}

export default user
