import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'

Vue.config.productionTip = false

// 全局错误处理
Vue.config.errorHandler = function(err, vm, info) {
  console.error('Vue错误:', err)
  console.error('错误组件:', vm)
  console.error('错误信息:', info)
}

// 捕获Promise错误
window.addEventListener('unhandledrejection', event => {
  console.error('未处理的Promise拒绝:', event.reason)
  event.preventDefault()
})

// 使用ElementUI
Vue.use(ElementUI)

// 配置axios
axios.defaults.baseURL = '/api'
axios.defaults.timeout = 10000
axios.interceptors.request.use(config => {
  // 从localStorage中获取token
  const token = localStorage.getItem('token')
  if (token) {
    // 设置请求头
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  console.error('Axios请求错误:', error)
  return Promise.reject(error)
})

axios.interceptors.response.use(response => {
  return response
}, error => {
  console.error('Axios响应错误:', error)
  if (error.response && error.response.status === 401) {
    // 未登录或token过期
    localStorage.removeItem('token')
    router.push('/login')
  }
  return Promise.reject(error)
})

Vue.prototype.$http = axios

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app') 