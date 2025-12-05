import request from '@/utils/request'

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/orders/list',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/orders/details/${id}`,
    method: 'get'
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/orders/submit',
    method: 'post',
    data
  })
}

// 更新订单状态 - 接单
export function acceptOrder(id) {
  return request({
    url: `/orders/accept/${id}`,
    method: 'put'
  })
}

// 更新订单状态 - 完成订单
export function completeOrder(id) {
  return request({
    url: `/orders/complete/${id}`,
    method: 'put'
  })
}

// 取消订单
export function cancelOrder(orderId) {
  console.log('cancelOrder API - 订单ID:', orderId, '类型:', typeof orderId)
  return request({
    url: '/orders/cancel',
    method: 'put',
    data: { id: orderId.toString() }
  })
}

// 拒绝订单
export function rejectOrder(id, reason) {
  return request({
    url: '/orders/reject',
    method: 'put',
    data: { id, reason }
  })
}

// 获取我的订单列表
export function getMyOrders(params) {
  return request({
    url: '/orders/userPage',
    method: 'get',
    params
  })
}

// 根据顾客ID获取订单列表
export function getOrdersByCustomerId(customerId) {
  return request({
    url: `/orders/customer/${customerId}`,
    method: 'get'
  })
}

// 获取订单统计数据
export function getOrderStatistics() {
  return request({
    url: '/orders/statistics',
    method: 'get'
  })
}

// 获取餐桌列表
export function getTableList(params) {
  return request({
    url: '/table/list',
    method: 'get',
    params
  })
}

// 添加餐桌
export function addTable(data) {
  return request({
    url: '/table',
    method: 'post',
    data
  })
}

// 更新餐桌信息
export function updateTable(data) {
  return request({
    url: '/table',
    method: 'put',
    data
  })
}

// 删除餐桌
export function deleteTable(id) {
  return request({
    url: `/table/${id}`,
    method: 'delete'
  })
}

// 更新餐桌状态
export function updateTableStatus(id, status) {
  return request({
    url: `/table/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 获取可用餐桌列表
export function getAvailableTables() {
  return request({
    url: '/table/available',
    method: 'get'
  })
}

// 支付订单
export function payOrder(orderId, paymentMethod) {
  console.log('payOrder API - 订单ID:', orderId, '类型:', typeof orderId)
  console.log('payOrder API - 支付方式:', paymentMethod)
  return request({
    url: '/orders/pay',
    method: 'put',
    data: { 
      id: orderId.toString(),
      paymentMethod: paymentMethod
    }
  })
}

 