import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

/* Layout */
import Layout from '@/layout/Layout'
import CustomerLayout from '@/layout/CustomerLayout'

// 解决在使用 push/replace 重复导航到当前路由时抛出的 NavigationDuplicated 错误
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch(err => {
    // 只吞掉因重复导航导致的异常，其他异常继续向外抛出
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace (location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalReplace.call(this, location, onResolve, onReject)
  }
  return originalReplace.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home'),
        meta: { title: '首页', roles: ['admin', 'employee'] }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/Category'),
        meta: { title: '分类管理', roles: ['admin', 'employee'] }
      },
      {
        path: 'dish',
        name: 'Dish',
        component: () => import('@/views/dish/Dish'),
        meta: { title: '菜品管理', roles: ['admin', 'employee'] }
      },
      {
        path: 'table',
        name: 'Table',
        component: () => import('@/views/table/Table'),
        meta: { title: '餐桌管理', roles: ['admin', 'employee'] }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/Order'),
        meta: { title: '订单管理', roles: ['admin', 'employee'] }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail'),
        meta: { title: '订单详情', roles: ['admin', 'employee'] },
        props: true
      },
      {
        path: 'review',
        name: 'Review',
        component: () => import('@/views/review/Review'),
        meta: { title: '评价管理', roles: ['admin', 'employee'] }
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/employee/Employee'),
        meta: { title: '员工管理', roles: ['admin'] }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/customer/Customer'),
        meta: { title: '顾客管理', roles: ['admin'] }
      },
      {
        path: 'profile',
        name: 'EmployeeProfile',
        component: () => import('@/views/employee/Profile'),
        meta: { title: '个人中心', roles: ['admin', 'employee'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics'),
        meta: { title: '数据统计', roles: ['admin', 'employee'] }
      }
    ]
  },
  {
    path: '/customer',
    component: CustomerLayout,
    redirect: '/customer/menu',
    children: [
      {
        path: 'menu',
        name: 'CustomerMenu',
        component: () => import('@/views/customer/Menu'),
        meta: { title: '点菜', icon: 'el-icon-dish', roles: ['customer'] }
      },
      {
        path: 'cart',
        name: 'CustomerCart',
        component: () => import('@/views/customer/Cart'),
        meta: { title: '购物车', icon: 'el-icon-shopping-cart-2', roles: ['customer'] }
      },
      {
        path: 'order',
        name: 'CustomerOrder',
        component: () => import('@/views/customer/MyOrder'),
        meta: { title: '我的订单', icon: 'el-icon-s-order', roles: ['customer'] }
      },
      {
        path: 'review',
        name: 'CustomerReview',
        component: () => import('@/views/customer/MyReview'),
        meta: { title: '我的评价', icon: 'el-icon-chat-line-round', roles: ['customer'] }
      },
      {
        path: 'profile',
        name: 'CustomerProfile',
        component: () => import('@/views/customer/Profile'),
        meta: { title: '个人中心', icon: 'el-icon-user', roles: ['customer'] }
      }
    ]
  },
  {
    path: '*',
    redirect: '/login'
  }
]

const router = new VueRouter({
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫 - 目标路由:', to.path, '来源路由:', from.path);
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 餐厅管理系统` : '餐厅管理系统'
  
  // 获取token
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const userRole = userInfo.role ? userInfo.role.toLowerCase() : '' // 统一转为小写
  
  console.log('路由守卫 - 用户信息:', { token: !!token, userRole });

  // 如果前往登录或注册页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    if (token && userRole) {
      console.log('已登录用户访问登录页，重定向到首页');
      // 已登录用户重定向到对应角色的首页
      if (userRole === 'customer') {
        return next('/customer/menu')
      } else {
        return next('/home')
      }
    } else {
      console.log('未登录用户访问登录页，允许访问');
      return next()
    }
  }

  // 未登录用户重定向到登录页
  if (!token) {
    console.log('未登录用户访问受保护页面，重定向到登录页');
    return next('/login')
  }

  // 检查用户是否有权限访问该路由
  if (to.meta.roles && to.meta.roles.length > 0) {
    // 将路由要求的角色也转为小写进行比较
    const requiredRoles = to.meta.roles.map(role => role.toLowerCase())
    if (!requiredRoles.includes(userRole)) {
      console.log('用户无权访问该页面，重定向到对应角色的首页');
      // 无权限，重定向到对应角色的首页
      if (userRole === 'customer') {
        return next('/customer/menu')
      } else {
        return next('/home')
      }
    }
  }

  console.log('用户有权访问该页面，允许访问');
  next()
})

export default router 