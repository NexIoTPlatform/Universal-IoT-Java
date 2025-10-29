import request from '@/utils/request'

// 批量配置列表
export function listBatchConfig(params) {
  return request({
    url: '/admin/v1/function/batch/dev/list',
    method: 'get',
    params
  })
}

// 获取应用列表
export function listApp(appName) {
  return request({
    url: '/admin/v1/function/batch/application/list',
    method: 'get',
    params: {
      appName
    }
  })
}

// 获取产品列表
export function listProduct(id) {
  return request({
    url: `/admin/v1/function/batch/product/list/${id}`,
    method: 'get'
  })
}

// 新建批量下发任务
export function batchConfigTask(data) {
  return request({
    url: `/admin/v1/function/batch/task`,
    method: 'post',
    data
  })
}

// 查询任务列表
export function batchTaskList(params) {
  return request({
    url: `/admin/v1/function/batch/task/list`,
    method: 'get',
    params
  })
}

// 根据任务id查询下发历史列表
export function queryTaskById(params) {
  return request({
    url: `/admin/v1/function/batch/task/history`,
    method: 'get',
    params
  })
}

// 重新下发任务
export function reConfigTask(data) {
  return request({
    url: `/admin/v1/function/batch/task/retry`,
    method: 'post',
    data
  })
}
