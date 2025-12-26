import request from '@/utils/request'

/**
 * 统计分析API
 */

// 获取订单统计数据
export function getOrderStatistics(params) {
  return request({
    url: '/statistics/orders',
    method: 'get',
    params
  })
}

// 获取菜品销售排行榜
export function getDishSalesRanking(params) {
  return request({
    url: '/statistics/dishes/ranking',
    method: 'get',
    params
  })
}

// 获取顾客行为分析数据
export function getCustomerBehaviorAnalysis(params) {
  return request({
    url: '/statistics/customers/behavior',
    method: 'get',
    params
  })
}

// 获取顾客菜品偏好分析
export function getCustomerDishPreferences(customerId, params) {
  return request({
    url: `/statistics/customers/${customerId}/preferences`,
    method: 'get',
    params
  })
}

// 获取员工绩效统计
export function getEmployeePerformance(params) {
  return request({
    url: '/statistics/employees/performance',
    method: 'get',
    params
  })
}

// 获取菜品评论星级排行
export function getDishReviewRanking(params) {
  return request({
    url: '/statistics/dishes/review-ranking',
    method: 'get',
    params
  })
}

// 获取时间段订单分布
export function getHourlyOrderDistribution() {
  return request({
    url: '/statistics/orders/hourly-distribution',
    method: 'get'
  })
}

// 获取月度销售趋势
export function getMonthlySalesTrend(params) {
  return request({
    url: '/statistics/sales/monthly-trend',
    method: 'get',
    params
  })
}

// 获取餐桌使用率统计
export function getTableUtilizationStatistics() {
  return request({
    url: '/statistics/tables/utilization',
    method: 'get'
  })
}

// 获取综合经营仪表板数据
export function getDashboardData() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

// 获取销售汇总报表
export function getSalesSummaryReport(params) {
  return request({
    url: '/statistics/reports/sales-summary',
    method: 'get',
    params
  })
}

// LLM解读统计报表
export function interpretStatisticsReport(data) {
  return request({
    url: '/statistics/llm/interpret',
    method: 'post',
    data,
    timeout: 60000,
    skipErrorHandler: true
  })
}

// LLM统计问答
export function askStatisticsQuestion(data) {
  return request({
    url: '/statistics/llm/ask',
    method: 'post',
    data,
    timeout: 60000,
    skipErrorHandler: true
  })
}
