import DeviceTypeBadge from './index.vue'

// 为组件提供 install 安装方法，供按需引入
DeviceTypeBadge.install = function (Vue) {
  Vue.component(DeviceTypeBadge.name, DeviceTypeBadge)
}

export default DeviceTypeBadge
