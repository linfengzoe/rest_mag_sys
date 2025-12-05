import request from '@/utils/request'

// 获取评价列表
export function getReviewList(params) {
  return request({
    url: '/review/list',
    method: 'get',
    params
  })
}

// 获取评价详情
export function getReviewDetail(id) {
  return request({
    url: `/review/${id}`,
    method: 'get'
  })
}

// 添加评价
export function addReview(data) {
  return request({
    url: '/review',
    method: 'post',
    data
  })
}

// 更新评价
export function updateReview(data) {
  return request({
    url: '/review',
    method: 'put',
    data
  })
}

// 删除评价
export function deleteReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'delete'
  })
}

// 获取我的评价
export function getMyReviews(params) {
  return request({
    url: '/review/userPage',
    method: 'get',
    params
  })
}

// 回复评价
export function replyReview(data) {
  return request({
    url: `/review/reply`,
    method: 'put',
    data
  })
}

// 获取菜品评价
export function getDishReviews(dishId, params) {
  return request({
    url: `/review/dish/${dishId}`,
    method: 'get',
    params
  })
}

// 根据订单ID获取评价
export function getReviewByOrderId(orderId) {
  return request({
    url: `/review/order/${orderId}`,
    method: 'get'
  })
} 