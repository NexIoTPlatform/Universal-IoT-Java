import request from '@/utils/request'
import {praseStrEmpty} from '@/utils/ruoyi'

// 查询用户列表
export function listUser(query) {
  return request({
    url: '/admin/system/user/list',
    method: 'get',
    params: query
  })
}

// 查询用户详细
export function getUser(userId) {
  return request({
    url: '/admin/system/user/' + praseStrEmpty(userId),
    method: 'get'
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/admin/system/user',
    method: 'post',
    data: data
  })
}

// 修改用户
export function updateUser(data) {
  return request({
    url: '/admin/system/user',
    method: 'put',
    data: data
  })
}

// 删除用户
export function delUser(userId) {
  return request({
    url: '/admin/system/user/' + userId,
    method: 'delete'
  })
}

// 用户密码重置
export function resetUserPwd(data) {
  return request({
    url: '/admin/system/user',
    method: 'put',
    data: data
  })
}

// 用户状态修改
export function changeUserStatus(data) {
  return request({
    url: '/admin/system/user',
    method: 'put',
    data: data
  })
}

// 查询用户个人信息
export function getUserProfile() {
  return request({
    url: '/admin/system/user/profile',
    method: 'get'
  })
}

// 修改用户个人信息
export function updateUserProfile(data) {
  return request({
    url: '/admin/system/user/profile',
    method: 'put',
    data: data
  })
}

// 子账号授权
export function addSubAccount(userId, isSub) {
  return request({
    url: '/admin/system/user/sub/' + userId + '/' + isSub,
    method: 'get'
  })
}

// 用户密码重置
export function updateUserPwd(newPassword, oldPassword) {
  return request({
    url: '/admin/system/user/profile/updatePwd',
    method: 'put',
    data: {
      newPassword,
      oldPassword
    }
  })
}

// 用户头像上传
export function uploadAvatar(data) {
  return request({
    url: '/admin/system/user/profile/avatar',
    method: 'post',
    data: data
  })
}

// 导入用户
export function importData(data) {
  return request({
    url: '/admin/system/user/importData',
    method: 'post',
    data: data
  })
}

// 查询授权角色
export function getAuthRole(userId) {
  return request({
    url: '/admin/system/user/authRole/' + userId,
    method: 'get'
  })
}

// 保存授权角色
export function updateAuthRole(data) {
  // console.log(data)
  return request({
    url: '/admin/system/user/authRole',
    method: 'put',
    params: data
  })
}
