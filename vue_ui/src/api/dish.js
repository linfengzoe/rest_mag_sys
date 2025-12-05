import request from '@/utils/request'

// 获取菜品列表
export function listDishes(params) {
  return request({
    url: '/dish/list',
    method: 'get',
    params
  })
}

// 获取菜品详情
export function getDishDetail(id) {
  return request({
    url: `/dish/${id}`,
    method: 'get'
  })
}

// 添加菜品
export function addDish(data) {
  return request({
    url: '/dish',
    method: 'post',
    data
  })
}

// 更新菜品
export function updateDish(data) {
  return request({
    url: '/dish',
    method: 'put',
    data
  })
}

// 删除菜品
export function deleteDish(id) {
  return request({
    url: `/dish/${id}`,
    method: 'delete'
  })
}

// 更新菜品状态
export function updateDishStatus(id, status) {
  return request({
    url: `/dish/status/${status}`,
    method: 'post',
    params: { ids: [id] }
  })
}

// 获取分类列表（分页）
export function listCategories(params) {
  return request({
    url: '/category/page',
    method: 'get',
    params
  })
}

// 获取所有分类选项（不分页）
export function getAllCategories() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 获取分类详情
export function getCategoryDetail(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

// 添加分类
export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(data) {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}

// 上传图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/dish/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 