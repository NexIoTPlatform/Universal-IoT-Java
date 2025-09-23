import request from '@/utils/request'

// 通知渠道管理
export function listNoticeChannel(query) {
  return request({
    url: '/admin/v1/notice/channel/list',
    method: 'get',
    params: query
  })
}

export function getNoticeChannel(id) {
  return request({
    url: '/admin/v1/notice/channel/get',
    method: 'get',
    params: {id}
  })
}

export function saveNoticeChannel(data) {
  return request({
    url: '/admin/v1/notice/channel/save',
    method: 'post',
    data: data
  })
}

export function deleteNoticeChannel(ids) {
  const idsArray = Array.isArray(ids) ? ids : [ids];

  return request({
    url: '/admin/v1/notice/channel/delete',
    method: 'post',
    headers: {
      'Content-Type': 'application/json' // 显式设置Content-Type为JSON
    },
    data: idsArray // 直接传递数组
  })
}

// 通知模板管理
export function listNoticeTemplate(query) {
  return request({
    url: '/admin/v1/notice/template/list',
    method: 'get',
    params: query
  })
}

export function getNoticeTemplate(id) {
  return request({
    url: '/admin/v1/notice/template/get',
    method: 'get',
    params: {id}
  })
}

export function saveNoticeTemplate(data) {
  return request({
    url: '/admin/v1/notice/template/save',
    method: 'post',
    data: data
  })
}

export function deleteNoticeTemplate(ids) {
  const idsArray = Array.isArray(ids) ? ids : [ids];

  return request({
    url: '/admin/v1/notice/template/delete',
    method: 'post',
    headers: {
      'Content-Type': 'application/json' // 显式设置Content-Type为JSON
    },
    data: idsArray // 直接传递数组
  })
}

// 批量启用通知模板
export function enableNoticeTemplate(ids) {
  const idsArray = Array.isArray(ids) ? ids : [ids];
  return request({
    url: '/admin/v1/notice/template/enable',
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data: idsArray
  })
}

// 批量停用通知模板
export function disableNoticeTemplate(ids) {
  const idsArray = Array.isArray(ids) ? ids : [ids];
  return request({
    url: '/admin/v1/notice/template/disable',
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data: idsArray
  })
}

export function testNoticeTemplate(data) {
  return request({
    url: '/admin/v1/notice/template/test',
    method: 'post',
    data: data
  })
}

export function sendNotice(data) {
  return request({
    url: '/admin/v1/notice/send',
    method: 'post',
    data: data
  })
} 