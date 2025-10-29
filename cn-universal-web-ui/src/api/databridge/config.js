import request from '@/utils/request'

// 获取桥接配置列表
export function getConfigList(params) {
  return request({
    url: '/databridge/configs',
    method: 'get',
    params
  })
}

// 获取桥接配置详情
export function getConfig(id) {
  return request({
    url: `/databridge/configs/${id}`,
    method: 'get'
  })
}

// 创建桥接配置
export function createConfig(data) {
  return request({
    url: '/databridge/configs',
    method: 'post',
    data
  })
}

// 更新桥接配置
export function updateConfig(id, data) {
  return request({
    url: `/databridge/configs/${id}`,
    method: 'put',
    data
  })
}

// 删除桥接配置
export function deleteConfig(id) {
  return request({
    url: `/databridge/configs/${id}`,
    method: 'delete'
  })
}

// 验证桥接配置
export function validateConfig(id) {
  return request({
    url: `/databridge/configs/${id}/validate`,
    method: 'post'
  })
}