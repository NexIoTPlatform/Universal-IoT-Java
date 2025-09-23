import request from '@/utils/request'

// 查询设备协议列表
export function listProtocol(query) {
  return request({
    url: '/admin/v1/protocol/list',
    method: 'get',
    params: query
  })
}

// 查询所有设备协议列表 不分页
export function allProtocol() {
  return request({
    url: '/admin/v1/protocol/list/ids',
    method: 'get'
  })
}

// 查询设备协议详细
export function getProtocol(id) {
  return request({
    url: '/admin/v1/protocol/' + id,
    method: 'get'
  })
}

// 新增设备协议
export function addProtocol(data) {
  return request({
    url: '/admin/v1/protocol',
    method: 'post',
    data: data
  })
}

// 修改设备协议
export function updateProtocol(data) {
  return request({
    url: '/admin/v1/protocol',
    method: 'put',
    data: data
  })
}

// 删除设备协议
export function delProtocol(id) {
  return request({
    url: '/admin/v1/protocol/' + id,
    method: 'delete'
  })
}

export function messageCodec(data) {
  return request({
    url: '/admin/v1/protocol/codec',
    method: 'post',
    data: data
  })
}
