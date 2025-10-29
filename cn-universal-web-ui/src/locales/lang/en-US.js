import antdEnUS from 'ant-design-vue/es/locale-provider/en_US'
import setting from './en-US/setting'
import common from './en-US/common'

const components = {
  antLocale: antdEnUS,
  momentName: 'en',
  momentLocale: {}
}

export default {
  'message': '-',

  ...components,
  ...setting,
  ...common
}
