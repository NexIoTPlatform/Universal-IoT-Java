/**
 * 操作权限处理
 * Copyright (c) 2019 ruoyi
 */
import store from '@/store'

export default {
  inserted(el, binding, vnode) {
    const {value} = binding
    const allPermission = '*:*:*'
    const permissions = store.getters && store.getters.permissions

    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some(permission => {
        // 检查是否有通配符权限
        if (permission === allPermission) {
          return true
        }
        // 检查用户权限是否包含所需的权限
        return permissionFlag.some(requiredPermission => {
          const hasPermission = permission === requiredPermission
          if (hasPermission) {
          }
          return hasPermission
        })
      })

      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置操作权限标签值`)
    }
  }
}
