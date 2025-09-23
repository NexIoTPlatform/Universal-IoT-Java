import request from '@/utils/request'

// 更新网络状态，必须传产品主键id和networnUnionId
export function updateProductNetworkUnionId(data) {
  return request({
    url: '/admin/v1/product/updateNetworkUnionId',
    method: 'post',
    data: data
  })
}

// 查询设备产品列表
export function searchProduct(query) {
  return request({
    url: '/admin/v1/product/search',
    method: 'get',
    params: query
  })
}

// 查询设备产品列表
export function listProduct(query) {
  return request({
    url: '/admin/v1/product/v4/list',
    method: 'get',
    params: query
  })
}

// 查询设备所有产品列表
export function listAllProduct() {
  return request({
    url: '/admin/v1/product/all/list',
    method: 'get'
  })
}

// 通过ProductKey查询产品详情
export function getProByKey(key) {
  return request({
    url: '/admin/v1/product/get/pro/' + key,
    method: 'get'
  })
}

// 获取产品连接信息
export function getProductConnectInfo(productKey) {
  return request({
    url: '/admin/v1/product/connect/info',
    method: 'post',
    params: { productKey }
  })
}

// 获取产品MQTT密码
export function getProductMqttPassword(productKey) {
  return request({
    url: '/admin/v1/product/connect/mqtt/password',
    method: 'post',
    params: { productKey }
  })
}

// 查询设备产品列表
export function queryProductList(query) {
  return request({
    url: '/admin/v1/product/v4/list',
    method: 'get',
    params: query
  })
}

// 查询设备产品详细
export function getProduct(id) {
  return request({
    url: '/admin/v1/product/' + id,
    method: 'get'
  })
}

// 新增设备产品
export function getPublicProduct(data) {
  return request({
    url: '/admin/v1/product/ctwing/pubpro',
    method: 'post',
    data: data
  })
}

// 新增设备产品
export function addProduct(data) {
  return request({
    url: '/admin/v1/product',
    method: 'post',
    data: data
  })
}

// 电信新增设备产品
export function addProductCtwing(data) {
  return request({
    url: '/admin/v1/product/nb',
    method: 'post',
    data: data
  })
}

// 电信新增公共产品
export function addCtwingPubPro(data) {
  return request({
    url: '/admin/v1/product/ctwing/pubproadd',
    method: 'post',
    data: data
  })
}

// 修改设备产品
export function updateProduct(data) {
  return request({
    url: '/admin/v1/product',
    method: 'put',
    data: data
  })
}

// 修改设备产品状态
export function updateProductState(id, state) {
  const data = {
    id,
    state
  }
  return request({
    url: '/admin/v1/product',
    method: 'put',
    data: data
  })
}

// 查询菜单下拉树结构
export function treeselect() {
  return request({
    url: '/admin/sort/treeselect',
    method: 'get'
  })
}

// 删除设备产品
export function delProduct(id) {
  return request({
    url: '/admin/v1/product/' + id,
    method: 'delete'
  })
}

// 修改设备产品
export function updateConfig(data) {
  return request({
    url: '/admin/v1/product/config',
    method: 'put',
    data: data
  })
}

// 修改产品其他配置信息
export function updateOtherConfig(data) {
  return request({
    url: '/admin/v1/product/otherConfig',
    method: 'put',
    data: data
  })
}

// 修改设备产品
export function updateStoreConfig(data) {
  return request({
    url: '/admin/v1/product/storeConfig',
    method: 'put',
    data: data
  })
}

// 查询model
export function getModel(id) {
  return request({
    url: '/admin/v1/product/model/' + id,
    method: 'get'
  })
}

// 查询设备产品物模型属性列表
export function getPropertiesList(id) {
  return request({
    url: '/admin/v1/product/properties/' + id,
    method: 'get'
  })
}

// 查询设备产品物模型属性列表(功能下发属性)
export function getFunctionPropertiesList(id) {
  return request({
    url: '/admin/v1/product/functionProperties/' + id,
    method: 'get'
  })
}

// 查询设备产品物模型事件列表
export function getEventsList(id) {
  return request({
    url: '/admin/v1/product/events/' + id,
    method: 'get'
  })
}

// 根据设备id查询物模型
export function getMeta(devId) {
  return request({
    url: '/admin/v1/product/metadata/' + devId,
    method: 'get'
  })
}

// 查询设备产品物模型方法列表
export function getFunctionsList(id) {
  return request({
    url: '/admin/v1/product/functions/' + id,
    method: 'get'
  })
}

// 查询设备产品对应设备数量
export function getDevNumByProduct() {
  return request({
    url: '/admin/v1/product/device/count',
    method: 'get'
  })
}

// 物模型字段新增
export function metadataAdd(data) {
  return request({
    url: '/admin/v1/product/metadata/add',
    method: 'post',
    data: data
  })
}

// 物模型字段修改
export function metadataUpdate(data) {
  return request({
    url: '/admin/v1/product/metadata/update',
    method: 'post',
    data: data
  })
}

// 物模型字段查询
export function metadataGet(data) {
  return request({
    url: '/admin/v1/product/metadata/get',
    method: 'post',
    data: data
  })
}

// 物模型字段删除
export function metadataDelete(data) {
  return request({
    url: '/admin/v1/product/metadata/delete',
    method: 'post',
    data: data
  })
}

// 产品导入
export function importProduct(data) {
  return request({
    url: '/admin/v1/product/import',
    method: 'post',
    data
  })
}

// 上传产品图片
export function uploadProductImage(formData) {
  return request({
    url: '/admin/v1/product/uploadImage',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 查询可绑定网关产品列表
export function gatewayList() {
  return request({
    url: '/admin/v1/product/gateway/list',
    method: 'get'
  })
}

// 修改产品对应网关信息
export function modifyProductGateway(id, gwProductKey) {
  return request({
    url: '/admin/v1/product/gateway',
    method: 'put',
    data: {
      id,
      gwProductKey
    }
  })
}

// 修改产品对应网关信息
export function getInstanceGateway(productKey) {
  return request({
    url: `/admin/v1/device/gateway/${productKey}`,
    method: 'get'
  })
}
// 通过ProductKey查询产品详情
export function getGatewaySubProducts(key) {
  return request({
    url: '/admin/v1/product/gateway/subProducts/' + key,
    method: 'get'
  })
}
// 查询可绑定网关子设备产品列表
export function getSubdeviceGateways(productKey) {
  return request({
    url: `/admin/v1/product/sub/device/${productKey}`,
    method: 'get'
  })
}
