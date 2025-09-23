import request from '@/utils/request'

// 创建规则流程
export function createRuleFlow(data, creatorId) {
  return request({
    url: '/admin/v1/rule/ruleflow/create',
    method: 'post',
    data: data,
    params: {creatorId}
  })
}

// 更新规则流程
export function updateRuleFlow(data, creatorId) {
  return request({
    url: '/admin/v1/rule/ruleflow/update',
    method: 'put',
    data: data,
    params: {creatorId}
  })
}

// 获取规则流程详情
export function getRuleFlow(id, creatorId) {
  return request({
    url: `/admin/v1/rule/ruleflow/${id}`,
    method: 'get',
    params: {creatorId}
  })
}

// 删除规则流程
export function deleteRuleFlow(id, creatorId) {
  return request({
    url: `/admin/v1/rule/ruleflow/${id}`,
    method: 'delete',
    params: {creatorId}
  })
}

// 获取规则流程列表
export function listRuleFlows(creatorId, flowName, status) {
  return request({
    url: '/admin/v1/rule/ruleflow/list',
    method: 'get',
    params: {creatorId, flowName, status}
  })
}

// 启用/禁用规则流程
export function changeRuleFlowStatus(id, status, creatorId) {
  return request({
    url: `/admin/v1/rule/ruleflow/${id}/status`,
    method: 'put',
    params: {status, creatorId}
  })
}

// 执行规则流程
export function executeRuleFlow(data) {
  return request({
    url: '/admin/v1/rule/ruleflow/execute',
    method: 'post',
    data: data
  })
}

// 验证规则流程
export function validateRuleFlow(flowData) {
  return request({
    url: '/admin/v1/rule/ruleflow/validate',
    method: 'post',
    params: {flowData}
  })
}

// 根据模板ID获取规则流程
export function getRuleFlowsByTemplate(templateId) {
  return request({
    url: `/admin/v1/rule/ruleflow/template/${templateId}`,
    method: 'get'
  })
}

// 根据产品KEY获取规则流程
export function getRuleFlowsByProduct(productKey) {
  return request({
    url: `/admin/v1/rule/ruleflow/product/${productKey}`,
    method: 'get'
  })
} 