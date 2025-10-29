import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import {createPinia} from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './global.less'

// Create app instance
const app = createApp(App)

// Use plugins
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// Mount app
app.mount('#app') 