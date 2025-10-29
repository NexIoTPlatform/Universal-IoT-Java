import request from '@/utils/request'

// 查询规则链列表
export function listChain(query) {
  return request({
    url: '/admin/v1/rulego/chain/list',
    method: 'get',
    params: query
  })
}

// 查询规则链详细
export function getChain(id) {
  return request({
    url: '/admin/v1/rulego/chain/' + id,
    method: 'get'
  })
}

// 根据rulegoId查询规则链详细
export function getChainByRulegoId(rulegoId) {
  return request({
    url: '/admin/v1/rulego/chain/detail',
    method: 'get',
    params: {rulegoId}
  })
}

// 新增规则链
export function addChain(data) {
  return request({
    url: '/admin/v1/rulego/chain',
    method: 'post',
    data: data
  })
}

// 修改规则链
export function updateChain(data) {
  return request({
    url: '/admin/v1/rulego/chain',
    method: 'put',
    data: data
  })
}

// 删除规则链
export function delChain(id) {
  return request({
    url: '/admin/v1/rulego/chain/' + id,
    method: 'delete'
  })
}

// 部署规则链
export function deployChain(id) {
  return request({
    url: '/admin/v1/rulego/chain/' + id + '/deploy',
    method: 'post'
  })
}

// 停止规则链
export function stopChain(id) {
  return request({
    url: '/admin/v1/rulego/chain/' + id + '/stop',
    method: 'post'
  })
}

// 同步规则链DSL
export function syncChain(id) {
  return request({
    url: '/admin/v1/rulego/chain/' + id + '/sync',
    method: 'post'
  })
}

// 获取设计器URL
export function getDesignerUrl(id) {
  return request({
    url: '/admin/v1/rulego/chain/' + id + '/designer',
    method: 'get'
  })
}
