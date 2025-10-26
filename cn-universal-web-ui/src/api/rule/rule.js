import request from '@/utils/request'

// 查询规则列表
export function listRule(query) {
  return request({
    url: '/admin/v1/rule/list',
    method: 'get',
    params: query
  })
}

// 查询规则列表
export function changeRuleStatus(data) {
  return request({
    url: '/admin/v1/rule/status',
    method: 'post',
    data
  })
}

// sql测试
export function testRuleSql(data) {
  return request({
    url: '/admin/v1/rule/sql/test',
    method: 'post',
    data
  })
}

// 新增规则
export function addRule(data) {
  return request({
    url: '/admin/v1/rule',
    method: 'post',
    data
  })
}

// 更新规则
export function updateRule(data) {
  return request({
    url: '/admin/v1/rule',
    method: 'put',
    data
  })
}

// 更新规则调用目标
export function updateRuleTargets(data) {
  return request({
    url: '/admin/v1/rule/targets',
    method: 'put',
    data
  })
}

// 根据id查询规则
export function getRule(id) {
  return request({
    url: '/admin/v1/rule?id=' + id,
    method: 'get'
  })
}

// 测试调用目标
export function testTarget(data) {
  return request({
    url: '/admin/v1/rule/target/test',
    method: 'post',
    data
  })
}

// 删除规则
export function deleteRule(id) {
  return request({
    url: '/admin/v1/rule/' + id,
    method: 'delete'
  })
}
