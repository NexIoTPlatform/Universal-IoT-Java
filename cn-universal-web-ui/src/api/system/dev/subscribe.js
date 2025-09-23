import request from '@/utils/request'

// 查询订阅列表
export function listSubscribe(query) {
  return request({
    url: '/admin/v1/subscribe/list',
    method: 'get',
    params: query
  })
}

// 查看订阅详情
export function getSubscribe(id) {
  return request({
    url: '/admin/v1/subscribe/' + id,
    method: 'get'
  })
}

// 新增订阅
export function addSubscribe(data) {
  return request({
    url: '/admin/v1/subscribe/add',
    method: 'post',
    data: data
  })
}

// 修改订阅
export function updateSubscribe(data) {
  return request({
    url: '/admin/v1/subscribe/update',
    method: 'post',
    data: data
  })
}

// 删除订阅
export function deleteSubscribe(iotId, pro, data) {
  return request({
    url: '/admin/v1/subscribe/delete/' + pro + '/' + iotId,
    method: 'post',
    data: data
  })
}
