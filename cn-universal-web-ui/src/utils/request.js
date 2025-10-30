import axios from 'axios'
import store from '@/store'
import storage from 'store'
import notification from 'ant-design-vue/es/notification'
import message from 'ant-design-vue/es/message'
import {VueAxios} from './axios'
import {ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN} from '@/store/mutation-types'
import errorCode from '@/utils/errorCode'
import qs from 'qs'
import {blobValidate} from '@/utils/ruoyi'
import {saveAs} from 'file-saver'

// 创建 axios 实例
const request = axios.create({
  // API 请求的默认前缀
  baseURL: process.env.VUE_APP_BASE_API,
  // baseURL: 'https://ruoyi.setworld.net',
  timeout: 20000 // 请求超时时间
})

// 异常拦截处理器
const errorHandler = (error) => {
  let errorMessage = error.message
  const requestUrl = error.config?.url || ''

  if (requestUrl.indexOf('/oauth/token') !== -1) {
    errorMessage = error.response.data.error_description
  } else {
    // 处理网络错误
    if (errorMessage === 'Network Error') {
      errorMessage = '后端接口连接异常'
    } else if (errorMessage.includes('timeout')) {
      errorMessage = '系统接口请求超时'
    } else if (errorMessage.includes('Request failed with status code')) {
      // 处理HTTP状态码错误
      const statusCode = errorMessage.substr(errorMessage.length - 3)
      if (statusCode === '401') {
        storage.remove(ACCESS_TOKEN)
        storage.remove(REFRESH_TOKEN)
        storage.remove(EXPIRES_IN)
        location.href = '/index'
        return Promise.reject(error)
      } else if (statusCode === '500') {
        errorMessage = '内部错误，请联系管理员'
      } else if (statusCode === '404') {
        errorMessage = '请求的资源不存在'
      } else if (statusCode === '403') {
        errorMessage = '没有权限访问该资源'
      } else if (statusCode === '400') {
        errorMessage = '请求参数错误'
      } else if (statusCode === '502') {
        errorMessage = '网关错误'
      } else if (statusCode === '503') {
        errorMessage = '服务不可用'
      } else {
        errorMessage = `请求失败，状态码：${statusCode}，请联系管理员`
      }
    }

    // 如果有响应数据，优先使用响应中的错误信息
    if (error.response && error.response.data) {
      const responseData = error.response.data
      const responseMsg = responseData.msg || responseData.message
        || responseData.error
      if (responseMsg) {
        errorMessage = responseMsg
      }
    }

    // 确保errorMessage不为空
    if (!errorMessage) {
      errorMessage = '未知错误'
    }

    // 显示错误通知（排除登录接口）
    if (requestUrl !== '/login' && requestUrl.indexOf('/oauth/token') === -1) {
      notification.error({
        message: "系统异常",
        description: errorMessage,
        duration: 5
      })
    }
  }
  return Promise.reject(error)
}

// request interceptor
request.interceptors.request.use((config) => {
  const token = storage.get(ACCESS_TOKEN)
  // 如果 token 存在
  // 让每个请求携带自定义 token 请根据实际情况自行修改
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token // 让每个请求携带自定义token 请根据实际情况自行修改
    // config.headers['accessAccess-Token'] = token
  }
  if (config.headers['Content-Type'] === 'application/x-www-form-urlencoded') {
    config.data = qs.stringify(config.data)
  }
  // 处理params参数
  if (config.params) {
    const url = config.url + '?' + qs.stringify(config.params, {indices: false})
    config.params = {}
    config.url = url
  }
  return config
}, errorHandler)

// response interceptor
request.interceptors.response.use((res) => {
  // 请求rul
  const requestUrl = res.config.url
  // 未设置状态码则默认成功状态
  const code = res.data.code || 0
  // 获取错误信息
  const msg = errorCode[code] || res.data.msg || errorCode['default']
  // 二进制数据则直接返回
  if (res.request.responseType === 'blob' || res.request.responseType
    === 'arraybuffer') {
    return res.data
  }
  if (code === 401) {
    notification.open({
      message: '系统提示',
      description: '登录状态已过期，请重新登录',
      btn: (h) => {
        return h(
          'a-button',
          {
            props: {
              type: 'primary',
              size: 'small'
            },
            on: {
              click: () => {
                store.dispatch('Logout').then(() => {
                  location.href = '/index'
                })
              }
            }
          },
          '确认'
        )
      },
      duration: 5,
      onClose: close
    })
  } else if (code === 500) {
    if (requestUrl !== '/login') {
      notification.warning({
        message: "业务异常",
        description: msg,
        duration: 5
      })
    }
  } else if (code === 788) {
    return
  } else if (code !== 0) {
    // 显示业务错误通知
    if (requestUrl !== '/login') {
      notification.error({
        message: "操作失败",
        description: msg,
        duration: 5
      })
    }
    // 保留原始错误对象，便于业务层获取详细信息
    const error = new Error(msg)
    error.response = res
    error.code = code
    return Promise.reject(error)
  } else {
    return res.data
  }
  return Promise.reject(msg)
}, errorHandler)

const installer = {
  vm: {},
  install(Vue) {
    Vue.use(VueAxios, request)
  }
}

// 通用下载方法
export function download(url, params, filename) {
  const notificationKey = 'download'
  notification.open({
    key: notificationKey,
    message: '正在下载数据，请稍候',
    duration: 5,
    icon: (h) => {
      return h('a-icon', {
        props: {
          type: 'loading'
        }
      })
    }
  })
  return request
  .post(url, qs.stringify(params, {indices: false}), {
    responseType: 'blob'
  })
  .then(async (data) => {
    // 检查文件名扩展名，如果是.json，则按JSON处理
    if (filename && filename.toLowerCase().endsWith('.json')) {
      // 对于JSON文件，直接保存为JSON格式
      const blob = new Blob([data], {type: 'application/json'})
      saveAs(blob, filename)
      message.success('下载成功')
    } else {
      // 对于其他文件类型，使用blobValidate检查
      const isBlob = await blobValidate(data)
      if (isBlob) {
        const blob = new Blob([data])
        saveAs(blob, filename)
        message.success('下载成功')
      } else {
        const resText = await data.text()
        const rspObj = JSON.parse(resText)
        const errMsg = errorCode[rspObj.code] || rspObj.msg
          || errorCode['default']
        message.error(errMsg)
      }
    }
    notification.close(notificationKey)
  })
  .catch((r) => {
    message.error('下载文件出现错误，请联系管理员！')
    notification.close(notificationKey)
  })
}

export default request

export {installer as VueAxios, request as axios}
