import request from '@/utils/request'

// 根据资源类型获取支持的插件类型选项
export function getPluginTypesByResourceType(resourceType) {
  return request({
    url: `/databridge/resource-types/${resourceType}/plugin-types`,
    method: 'get'
  })
}

// 获取所有资源类型和插件类型的映射关系
export function getResourcePluginMappings() {
  return request({
    url: '/databridge/resource-plugin-mappings',
    method: 'get'
  })
}