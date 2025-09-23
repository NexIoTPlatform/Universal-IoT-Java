import request from '@/utils/request'

// 查询产品分类列表
export function listSort(query) {
  return request({
    url: '/admin/sort/list',
    method: 'get',
    params: query
  })
}

// 查询产品分类详细
export function getSort(id) {
  return request({
    url: '/admin/sort/' + id,
    method: 'get'
  })
}

// 查询产品分类详细
export function getInfo(data) {
  return request({
    url: '/admin/sort/info',
    method: 'post',
    data: data
  })
}

// 新增产品分类
export function addSort(data) {
  return request({
    url: '/admin/sort',
    method: 'post',
    data: data
  })
}

// 修改产品分类
export function updateSort(data) {
  return request({
    url: '/admin/sort',
    method: 'put',
    data: data
  })
}

// 删除产品分类
export function delSort(id) {
  return request({
    url: '/admin/sort/' + id,
    method: 'delete'
  })
}
