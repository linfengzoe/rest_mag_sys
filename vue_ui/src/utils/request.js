import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import store from '@/store'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api', // url = base url + request url
  timeout: 5000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    console.log('请求配置:', config.url, config.method)
    console.log('store状态:', {
      isAuthenticated: store.getters.isAuthenticated,
      token: store.state.token ? store.state.token.substring(0, 20) + '...' : '无token',
      userInfo: store.state.userInfo
    })
    
    if (store.getters.isAuthenticated) {
      // 让每个请求携带token
      config.headers['Authorization'] = `Bearer ${store.state.token}`
      console.log('已添加Authorization头:', config.headers['Authorization'] ? '是' : '否')
    } else {
      console.log('用户未认证，不添加Authorization头')
    }
    
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log('请求错误:', error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  /**
   * 如果获取诸如头或状态之类的http信息
   * 返回 response => response
  */
  /**
   * 通过自定义代码确定请求状态
   * 
   * 可以通过HTTP状态码来判断状态
   */
  response => {
    const res = response.data

    // 如果自定义代码不是200，则判断为错误。
    if (res.code !== 200) {
      // 针对菜品/桌位等业务提示使用 warning，避免使用 error 的大红框
      if (res.msg && (res.msg.includes('Duplicate entry') || res.msg.includes('菜品') || res.msg.includes('桌') || res.msg.includes('桌位') || res.msg.includes('存在关联'))) {
        let warnMsg = res.msg;
        if (res.msg.includes('Duplicate entry')) {
          if (res.msg.includes('t_user.username')) warnMsg = '用户名已存在，请更换';
          else if (res.msg.includes('t_category.name')) warnMsg = '分类名称已存在';
          else if (res.msg.includes('table_info.name')) warnMsg = '餐桌名称已存在';
          else warnMsg = '数据已存在，不能重复';
        }
        Message({
          message: warnMsg,
          type: 'warning',
          duration: 5 * 1000
        })
      } else {
        Message({
          message: res.msg || '错误',
          type: 'error',
          duration: 5 * 1000
        })
      }

      // 401: 未登录或token过期
      // 403: 权限不足
      if (res.code === 401) {
        // 重新登录
        MessageBox.confirm('您已登出，可以取消以停留在此页面，或重新登录', '确认登出', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          store.dispatch('logout').then(() => {
            // 避免重复导航
            if (router.currentRoute.path !== '/login') {
              router.push(`/login?redirect=${router.currentRoute.fullPath}`)
            }
          })
        })
      }
      return Promise.reject(new Error(res.msg || '错误'))
    } else {
      return res
    }
  },
  error => {
    console.log('响应错误详情:', error) // 详细错误信息
    
    // 记录更多错误信息用于调试
    if (error.response) {
      // 如果后端返回重复键或唯一约束错误，改为 warning
      if (error.response.data && typeof error.response.data === 'string' && error.response.data.includes('Duplicate entry')) {
        let zhMsg = '数据已存在，不能重复';
        const raw = error.response.data;
        if (raw.includes('t_user.username')) {
          zhMsg = '用户名已存在，请更换';
        } else if (raw.includes('t_category.name')) {
          zhMsg = '分类名称已存在';
        } else if (raw.includes('table_info.name')) {
          zhMsg = '餐桌名称已存在';
        }
        Message({
          message: zhMsg,
          type: 'warning',
          duration: 5 * 1000
        })
        return Promise.reject(error) // 仍返回拒绝，调用处需 catch
      }
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          store.dispatch('logout').then(() => {
            // 避免重复导航
            if (router.currentRoute.path !== '/login') {
              router.push('/login')
            }
          })
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求错误，未找到该资源'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = `连接错误${error.response.status}`
      }
    } else {
      message = '网络异常，请检查您的网络连接'
    }
    
    Message({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service 