import request from '@/utils/request'

// 查询场景联动列表
export function listLinkage(query) {
  return request({
    url: '/admin/v1/scene/linkage/list',
    method: 'get',
    params: query
  })
}

// 查询场景联动详细
export function getLinkage(id) {
  return request({
    url: '/admin/v1/scene/linkage/' + id,
    method: 'get'
  })
}

// 手动触发
export function manualExec(id) {
  return request({
    url: '/admin/v1/scene/linkage/exec/' + id,
    method: 'get'
  })
}

// 新增场景联动
export function addLinkage(data) {
  return request({
    url: '/admin/v1/scene/linkage',
    method: 'post',
    data: data
  })
}

// 修改场景联动
export function updateLinkage(data) {
  return request({
    url: '/admin/v1/scene/linkage',
    method: 'put',
    data: data
  })
}

// 删除场景联动
export function delLinkage(id) {
  return request({
    url: '/admin/v1/scene/linkage/' + id,
    method: 'delete'
  })
}

// 查询场景联动执行日志
export function getLinkageLogs(sceneId, params = {}) {
  return request({
    url: `/admin/v1/scene/linkage/log/${sceneId}`,
    method: 'get',
    params
  })
}
