import request from '@/utils/request'

// 获取证书列表（分页/多条件）
export function getCertificateList(params) {
  return request({
    url: '/admin/v1/certificate/list',
    method: 'get',
    params // 支持name、sslKey、expireStart、expireEnd、pageNum、pageSize
  })
}

// 新增证书（内容直传）
export function addCertificate(data) {
  return request({
    url: '/admin/v1/certificate/add',
    method: 'post',
    data // JSON: sslKey, name, certContent, keyContent, expireTime, ...
  })
}

// 上传证书文件（推荐）
export function uploadCertificateFile(formData) {
  return request({
    url: '/admin/v1/certificate/uploadFile',
    method: 'post',
    data: formData,
    headers: {'Content-Type': 'multipart/form-data'}
  })
}

// 上传证书内容（文本）
export function uploadCertificateContent(data) {
  return request({
    url: '/admin/v1/certificate/uploadContent',
    method: 'post',
    data // form-data或JSON: sslKey, certContent, keyContent, name
  })
}

// 获取证书详情
export function getCertificateDetail(sslKey) {
  return request({
    url: '/admin/v1/certificate/get',
    method: 'get',
    params: {sslKey}
  })
}

// 更新证书
export function updateCertificate(data) {
  return request({
    url: '/admin/v1/certificate/update',
    method: 'post',
    data // 同新增，需sslKey
  })
}

// 删除证书（单个）
export function deleteCertificate(id) {
  return request({
    url: '/admin/v1/certificate/delete',
    method: 'post',
    params: {id} // 直接作为URL参数传递
  })
}

// 绑定证书到产品
export function bindCertificate(productKey, sslKey) {
  return request({
    url: '/admin/v1/product/bindCertificate',
    method: 'post',
    params: {productKey, sslKey}
  })
}

// 解绑证书
export function unbindCertificate(productKey) {
  return request({
    url: '/admin/v1/product/unbindCertificate',
    method: 'post',
    params: {productKey}
  })
} 