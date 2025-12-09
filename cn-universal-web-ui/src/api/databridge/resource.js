import request from '@/utils/request'

// 获取资源连接列表
export function getResourceList(params) {
  return request({
    url: '/databridge/resources',
    method: 'get',
    params
  })
}

// 获取资源连接详情
export function getResource(id) {
  return request({
    url: `/databridge/resources/${id}`,
    method: 'get'
  })
}

// 创建资源连接
export function createResource(data) {
  return request({
    url: '/databridge/resources',
    method: 'post',
    data
  })
}

// 更新资源连接
export function updateResource(id, data) {
  return request({
    url: `/databridge/resources/${id}`,
    method: 'put',
    data
  })
}

// 删除资源连接
export function deleteResource(id) {
  return request({
    url: `/databridge/resources/${id}`,
    method: 'delete'
  })
}

// 测试资源连接（已保存的）
export function testResource(id) {
  return request({
    url: `/databridge/resources/${id}/test`,
    method: 'post'
  })
}

// 测试资源连接（未保存的配置）
export function testResourceConfig(data) {
  return request({
    url: '/databridge/resources/test',
    method: 'post',
    data
  })
}

// 获取所有资源类型
export function getAllResourceTypes(params) {
  return request({
    url: '/databridge/resources/types',
    method: 'get',
    params
  })
}