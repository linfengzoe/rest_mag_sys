<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col v-if="userRole?.toUpperCase() === 'ADMIN'" :span="4">
        <el-card class="data-card">
          <div slot="header" class="card-header">
            <span>员工总数</span>
          </div>
          <div class="card-content">
            <div class="data-value">{{ employeeCount }}</div>
            <div class="data-icon">
              <i class="el-icon-user-solid"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="data-card">
          <div slot="header" class="card-header">
            <span>顾客总数</span>
          </div>
          <div class="card-content">
            <div class="data-value">{{ customerCount }}</div>
            <div class="data-icon">
              <i class="el-icon-user"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="data-card">
          <div slot="header" class="card-header">
            <span>今日订单数</span>
          </div>
          <div class="card-content">
            <div class="data-value">{{ todayOrderCount }}</div>
            <div class="data-icon">
              <i class="el-icon-s-order"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="data-card">
          <div slot="header" class="card-header">
            <span>今日销售额</span>
          </div>
          <div class="card-content">
            <div class="data-value">¥{{ todaySales.toFixed(2) }}</div>
            <div class="data-icon">
              <i class="el-icon-money"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="data-card">
          <div slot="header" class="card-header">
            <span>待确认订单</span>
          </div>
          <div class="card-content">
            <div class="data-value">{{ pendingOrderCount }}</div>
            <div class="data-icon">
              <i class="el-icon-warning"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="data-card">
          <div slot="header" class="card-header">
            <span>处理中订单</span>
          </div>
          <div class="card-content">
            <div class="data-value">{{ processingOrderCount }}</div>
            <div class="data-icon">
              <i class="el-icon-loading"></i>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header" class="card-header">
            <span>最近订单</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToOrders">查看更多</el-button>
          </div>
          <el-table :data="recentOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="订单号" width="80"></el-table-column>
            <el-table-column prop="customerName" label="顾客" width="100"></el-table-column>
            <el-table-column prop="tableNumber" label="餐桌号" width="80"></el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.amount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getOrderStatusType(scope.row.status)">{{ getOrderStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header" class="card-header">
            <span>最新评价</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToReviews">查看更多</el-button>
          </div>
          <el-table :data="recentReviews" style="width: 100%" v-loading="loading">
            <el-table-column prop="customerName" label="顾客" width="100"></el-table-column>
            <el-table-column prop="dishName" label="菜品" width="120"></el-table-column>
            <el-table-column prop="rating" label="评分" width="100">
              <template slot-scope="scope">
                <el-rate v-model="scope.row.rating" disabled text-color="#ff9900"></el-rate>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容"></el-table-column>
            <el-table-column prop="createTime" label="时间" width="180"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getOrderList, getOrderStatistics } from '@/api/order'
import { getReviewList } from '@/api/review'
import { listDishes } from '@/api/dish'
import { getCustomerList } from '@/api/user'
import { getDashboardData } from '@/api/statistics'

export default {
  name: 'Home',
  data() {
    return {
      loading: false,
      todayOrderCount: 0,
      todaySales: 0,
      dishCount: 0,
      employeeCount: 0,
      customerCount: 0,
      pendingOrderCount: 0,
      processingOrderCount: 0,
      recentOrders: [],
      recentReviews: [],
      userRole: '',
      statCards: []
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true;
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      this.userRole = userInfo.role;
      getDashboardData().then(res => {
        if (res.code === 200) {
          const dashboard = res.data;
          this.todayOrderCount = dashboard.today?.order_count || 0;
          this.todaySales = dashboard.today?.total_sales || 0;
          this.employeeCount = dashboard.employeeCount || 0;
          this.customerCount = dashboard.customerCount || 0;
          this.pendingOrderCount = dashboard.orderStatus?.find(i=>i.status===1)?.count || 0;
          this.processingOrderCount = dashboard.orderStatus?.find(i=>i.status===2)?.count || 0;
        }
        this.loading = false;
      });
      // 补全：加载最近订单和最新评论
      this.fetchOrders();
      this.fetchReviews();
    },
    fetchOrders() {
      console.log('开始获取订单数据...')
      return getOrderList({ page: 1, pageSize: 5 })
        .then(res => {
          console.log('订单API响应:', res)
          if (res.code === 200) {
            this.recentOrders = res.data.records
            console.log('最近订单:', this.recentOrders)
            
            // 计算今日订单数和销售额
            const today = new Date()
            today.setHours(0, 0, 0, 0)
            console.log('今日起始时间:', today)
            
            const todayOrders = res.data.records.filter(order => {
              console.log('原始订单时间:', order.createTime, '类型:', typeof order.createTime)
              let orderDate
              
              // 检查时间字段是否存在
              if (!order.createTime) {
                console.log('订单时间字段不存在或为空')
                return false
              }
              
              if (typeof order.createTime === 'string') {
                // 处理字符串格式的时间 "yyyy-MM-dd HH:mm:ss"
                orderDate = new Date(order.createTime.replace(/-/g, '/'))
              } else if (Array.isArray(order.createTime)) {
                // 处理数组格式的时间 [2024, 7, 3, 14, 30, 0]
                orderDate = new Date(order.createTime[0], order.createTime[1] - 1, order.createTime[2], 
                                   order.createTime[3] || 0, order.createTime[4] || 0, order.createTime[5] || 0)
              } else {
                orderDate = new Date(order.createTime)
              }
              
              console.log('解析后订单时间:', orderDate, '是否今日:', orderDate >= today)
              
              // 检查日期是否有效
              if (isNaN(orderDate.getTime())) {
                console.log('订单时间解析失败:', order.createTime)
                return false
              }
              
              // 仅统计今日且已完成(status === 3)的订单
              return orderDate >= today && order.status === 3
            })
            
            console.log('今日订单:', todayOrders)
            this.todayOrderCount = todayOrders.length
            this.todaySales = todayOrders.reduce((total, order) => total + order.amount, 0)
          } else {
            console.error('订单API返回错误:', res)
          }
        })
        .catch(error => {
          console.error('获取订单数据失败:', error)
          this.recentOrders = []
          this.todayOrderCount = 0
          this.todaySales = 0
          // 注意：pendingOrderCount 和 processingOrderCount 由独立的统计API设置
          return Promise.resolve() // 防止Promise.all失败
        })
    },
    fetchOrderStatistics() {
      console.log('开始获取订单统计数据...')
      console.log('当前用户角色:', this.userRole)
      
      return getOrderStatistics()
        .then(res => {
          console.log('订单统计API响应:', res)
          if (res.code === 200) {
            const stats = res.data
            console.log('订单统计数据:', stats)
            
            // 设置待确认订单数（已支付等待接单）
            this.pendingOrderCount = stats.pendingConfirm || 0
            // 设置处理中订单数（制作中）
            this.processingOrderCount = stats.processing || 0
            
            console.log('更新后的统计：待确认=', this.pendingOrderCount, '处理中=', this.processingOrderCount)
          } else {
            console.error('订单统计API返回错误:', res)
          }
        })
        .catch(error => {
          console.error('获取订单统计数据失败:', error)
          console.log('错误详情:', error.response || error.message || error)
          
          // 如果API失败，尝试手动设置一些测试数据来验证显示
          console.log('设置测试数据')
          this.pendingOrderCount = 1
          this.processingOrderCount = 2
          
          return Promise.resolve() // 防止Promise.all失败
        })
    },
    fetchReviews() {
      console.log('开始获取评价数据...')
      return getReviewList({ page: 1, pageSize: 5 })
        .then(res => {
          console.log('评价API响应:', res)
          if (res.code === 200) {
            this.recentReviews = res.data.records
            console.log('最近评价:', this.recentReviews)
          } else {
            console.error('评价API返回错误:', res)
          }
        })
        .catch(error => {
          console.error('获取评价数据失败:', error)
          this.recentReviews = []
          return Promise.resolve() // 防止Promise.all失败
        })
    },
    fetchDishCount() {
      console.log('开始获取菜品数量...')
      return listDishes({ page: 1, pageSize: 1 })
        .then(res => {
          console.log('菜品API响应:', res)
          if (res.code === 200) {
            this.dishCount = res.data.total
            console.log('菜品总数:', this.dishCount)
          } else {
            console.error('菜品API返回错误:', res)
          }
        })
        .catch(error => {
          console.error('获取菜品数量失败:', error)
          this.dishCount = 0
          return Promise.resolve() // 防止Promise.all失败
        })
    },
    fetchCustomerCount() {
      console.log('开始获取顾客数量...')
      return getCustomerList({ page: 1, pageSize: 1 })
        .then(res => {
          console.log('顾客API响应:', res)
          if (res.code === 200) {
            this.customerCount = res.data.total
            console.log('顾客总数:', this.customerCount)
          } else {
            console.error('顾客API返回错误:', res)
          }
        })
        .catch(error => {
          console.error('获取顾客数量失败:', error)
          this.customerCount = 0 // 设置默认值
          return Promise.resolve() // 防止Promise.all失败
        })
    },
    testDatabaseStats() {
      console.log('测试数据库连接和数据统计...')
      // 使用request直接调用API
      import('@/utils/request').then(({ default: request }) => {
        request({
          url: '/test/stats',
          method: 'get'
        }).then(res => {
          console.log('数据库统计结果:', res)
        }).catch(err => {
          console.error('数据库统计失败:', err)
        })
      })
    },
    getOrderStatusType(status) {
      switch (status) {
        case 0:
          return 'warning'
        case 1:
          return 'primary'
        case 2:
          return 'primary'
        case 3:
          return 'success'
        case 4:
          return 'info'
        default:
          return 'info'
      }
    },
    getOrderStatusText(status) {
      switch (status) {
        case 0:
          return '待支付'
        case 1:
          return '已支付'
        case 2:
          return '制作中'
        case 3:
          return '已完成'
        case 4:
          return '已取消'
        default:
          return '未知'
      }
    },
    goToOrders() {
      this.$router.push('/order')
    },
    goToReviews() {
      this.$router.push('/review')
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.data-card {
  height: 150px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
}

.data-value {
  font-size: 30px;
  font-weight: bold;
  color: #303133;
}

.data-icon {
  font-size: 50px;
  color: #909399;
  opacity: 0.3;
}
</style> 