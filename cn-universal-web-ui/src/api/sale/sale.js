import request from '@/utils/request'

// 查询sale列表
export function listSale(query) {
  return request({
    url: '/admin/sale/list',
    method: 'get',
    params: query
  })
}

// 查询sale详细
export function getSale(id) {
  return request({
    url: '/admin/sale/' + id,
    method: 'get'
  })
}

// 新增sale
export function addSale(data) {
  return request({
    url: '/admin/sale',
    method: 'post',
    data: data
  })
}

// 修改sale
export function updateSale(data) {
  return request({
    url: '/admin/sale',
    method: 'put',
    data: data
  })
}

// 删除sale
export function delSale(id) {
  return request({
    url: '/admin/sale/' + id,
    method: 'delete'
  })
}

// 设备导入
export function importDevice(data) {
  return request({
    url: '/admin/sale/import',
    method: 'post',
    data
    // headers: {
    //   'Content-Type': 'application/x-www-form-urlencoded'
    // }
  })
}

// 修改出售设备记录
export function editSaleDevice(id, data) {
  return request({
    url: '/admin/sale/edit/sale/' + id,
    method: 'put',
    data
    // headers: {
    //   'Content-Type': 'application/x-www-form-urlencoded'
    // }
  })
}

// 出售设备
export function saleDevice(ids, data) {
  return request({
    url: '/admin/sale/bulk/sale/' + ids,
    method: 'put',
    data
    // headers: {
    //   'Content-Type': 'application/x-www-form-urlencoded'
    // }
  })
}
