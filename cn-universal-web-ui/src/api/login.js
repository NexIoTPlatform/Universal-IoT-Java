import request from '@/utils/request'

const userApi = {
  Login: '/oauth/token',
  Logout: '/admin/logout',
  Register: '/register',
  // get my info
  UserInfo: '/admin/getInfo'
}

/**
 * login func
 * @param parameter
 * @returns {*}
 */
export function login(parameter) {
  return request({
    url: userApi.Login,
    method: 'post',
    data: parameter,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 注册方法
export function register(data) {
  return request({
    url: userApi.Register,
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

export function getInfo() {
  return request({
    url: userApi.UserInfo,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

export function logout() {
  return request({
    url: userApi.Logout,
    method: 'get'
  })
}

// 获取验证码
export function getCaptchaCode() {
  return request({
    url: '/getCaptchaCode',
    method: 'get'
  })
}
