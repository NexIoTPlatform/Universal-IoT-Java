import request from '@/utils/request'

// 查询分组树
export function listGroup() {
  return request({
    url: '/admin/v1/instance/group/list',
    method: 'get'
  }).then(response => {
    const nodes = response.data.map(node => {
      return buildTreeNode(node)
    })
    return nodes
  })
}

function buildTreeNode(node) {
  node.key = node.id
  node.value = node.id
  node.title = node.groupName
  node.label = node.groupName
  if (node.children) {
    node.children.forEach(n => {
      return buildTreeNode(n)
    })
  }
  return node
}

// 查询分组详情
export function getGroup(id) {
  return request({
    url: '/admin/v1/instance/group/' + id,
    method: 'get'
  })
}

// 新增分组
export function addGroup(data) {
  return request({
    url: '/admin/v1/instance/group',
    method: 'post',
    data: data
  })
}

// 修改分组
export function updateGroup(data) {
  return request({
    url: '/admin/v1/instance/group',
    method: 'put',
    data: data
  })
}

// 删除分组
export function delGroup(id) {
  return request({
    url: '/admin/v1/instance/group/' + id,
    method: 'delete'
  })
}

// 批量绑定设备
export function bindsDev(data) {
  return request({
    url: '/admin/v1/instance/group/binds',
    method: 'post',
    data: data
  })
}

// 查询分组id下的设备列表
export function deviceListByGroupId(data) {
  return request({
    url: '/admin/v1/instance/group/device/list',
    method: 'get',
    params: data
  })
}

// 删除分组中的设备
export function delDevByGroupId(groupId, devId) {
  return request({
    url: '/admin/v1/instance/group/' + groupId + '/' + devId,
    method: 'delete'
  })
}
