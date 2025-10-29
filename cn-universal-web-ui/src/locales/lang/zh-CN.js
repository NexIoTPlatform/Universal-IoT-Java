import antd from 'ant-design-vue/es/locale-provider/zh_CN'
import momentCN from 'moment/locale/zh-cn'
import setting from './zh-CN/setting'
import common from './zh-CN/common'

const components = {
  antLocale: antd,
  momentName: 'zh-cn',
  momentLocale: momentCN
}

export default {
  'message': '-',

  ...components,
  ...setting,
  ...common
}
