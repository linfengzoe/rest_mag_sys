import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 获取员工列表
export function getEmployeeList(params) {
  return request({
    url: '/employee/list',
    method: 'get',
    params
  })
}

// 添加员工
export function addEmployee(data) {
  return request({
    url: '/employee',
    method: 'post',
    data
  })
}

// 更新员工
export function updateEmployee(data) {
  return request({
    url: '/employee',
    method: 'put',
    data
  })
}

// 删除员工
export function deleteEmployee(id) {
  return request({
    url: `/employee/${id}`,
    method: 'delete'
  })
}

// 获取顾客列表
export function getCustomerList(params) {
  return request({
    url: '/customer/list',
    method: 'get',
    params
  })
}

// 添加顾客
export function addCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data
  })
}

// 更新顾客
export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data
  })
}

// 删除顾客
export function deleteCustomer(id) {
  return request({
    url: `/customer/${id}`,
    method: 'delete'
  })
}

// 更新用户密码
export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 用户退出登录
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 更新用户状态
export function updateUserStatus(data) {
  return request({
    url: '/user/status',
    method: 'post',
    data
  })
}

// 重置用户密码
export function resetPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'put',
    data
  })
}

// 更新员工状态
export function updateEmployeeStatus(data) {
  return request({
    url: '/employee/status',
    method: 'put',
    data
  })
}

// 更新顾客状态
export function updateCustomerStatus(data) {
  return request({
    url: '/customer/status',
    method: 'put',
    data
  })
}

// 更新用户个人信息
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 获取员工个人信息
export function getEmployeeProfile() {
  return request({
    url: '/employee/profile',
    method: 'get'
  })
}

// 更新员工个人信息
export function updateEmployeeProfile(data) {
  return request({
    url: '/employee/profile',
    method: 'put',
    data
  })
}

// 管理员更新其他员工信息
export function updateEmployeeByAdmin(data) {
  return request({
    url: '/employee/profile/admin',
    method: 'put',
    data
  })
}

// 获取顾客个人信息
export function getCustomerProfile() {
  return request({
    url: '/customer/profile',
    method: 'get'
  })
}

// 更新顾客个人信息
export function updateCustomerProfile(data) {
  return request({
    url: '/customer/profile',
    method: 'put',
    data
  })
}

// 启用/禁用用户
export function changeUserStatus(id, status) {
  return request({
    url: '/user/status',
    method: 'post',
    params: { id, status }
  })
} 