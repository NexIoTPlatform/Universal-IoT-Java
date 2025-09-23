import request from '@/utils/request'

// 查询用户应用信息列表
export function listApplication(query) {
  return request({
    url: '/admin/v1/apply/list',
    method: 'get',
    params: query
  })
}

// 查询用户应用信息详细
export function getApplication(uuid) {
  return request({
    url: '/admin/v1/apply/' + uuid,
    method: 'get'
  })
}

// 新增用户应用信息
export function addApplication(data) {
  return request({
    url: '/admin/v1/apply',
    method: 'post',
    data: data
  })
}

// 修改用户应用信息
export function updateApplication(data) {
  return request({
    url: '/admin/v1/apply',
    method: 'put',
    data: data
  })
}

// 修改用户应用信息
export function enable(data) {
  return request({
    url: '/admin/v1/apply/enable',
    method: 'post',
    data: data
  })
}

// 删除用户应用信息
export function delApplication(uuid) {
  return request({
    url: '/admin/v1/apply/' + uuid,
    method: 'delete'
  })
}

// 重置密钥
export function resetSecret(data) {
  return request({
    url: '/admin/v1/apply/reset',
    method: 'put',
    data: data
  })
}

// 地址测试
export function checkUrl(query) {
  return request({
    url: '/admin/v1/apply/check',
    method: 'get',
    params: query
  })
}

// 启用/停用推送配置
export function enableOrDisablePush(appUniqueId, pushType, enable) {
  return request({
    url: `/admin/v1/apply/${appUniqueId}/${pushType}/${enable}`,
    method: 'post'
  })
}
