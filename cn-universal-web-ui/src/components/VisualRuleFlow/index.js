import ProductNodeGenerator from './ProductNodeGenerator.vue'
import NotificationTemplateSelector from './NotificationTemplateSelector.vue'
import ProductSelector from './ProductSelector.vue'

export {
  ProductNodeGenerator,
  NotificationTemplateSelector,
  ProductSelector
}

export default {
  install(Vue) {
    Vue.component('ProductNodeGenerator', ProductNodeGenerator)
    Vue.component('NotificationTemplateSelector', NotificationTemplateSelector)
    Vue.component('ProductSelector', ProductSelector)
  }
} 