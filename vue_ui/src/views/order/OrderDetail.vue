<template>
  <div class="order-detail-container" v-loading="loading">
    <div class="page-header">
      <div class="header-title">
        <el-page-header @back="goBack" :content="`订单 #${orderId}`"></el-page-header>
      </div>
      <div class="header-actions">
        <el-button
          v-if="orderData.status === 1"
          type="success"
          size="small"
          @click="handleAccept"
        >
          接单
        </el-button>
        <el-button
          v-if="orderData.status === 2"
          type="success"
          size="small"
          @click="handleComplete"
        >
          完成订单
        </el-button>
        <el-button
          v-if="orderData.status === 1"
          type="danger"
          size="small"
          @click="handleCancel"
        >
          取消订单
        </el-button>
      </div>
    </div>

    <el-card class="box-card">
      <div slot="header" class="card-header">
        <span>订单信息</span>
      </div>
      <div class="order-info">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <span class="label">订单号：</span>
              <span>{{ orderData.id }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <span class="label">顾客：</span>
              <span>{{ orderData.customerName }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <span class="label">餐桌号：</span>
              <span>{{ orderData.tableName || '无' }}</span>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getOrderStatusType(orderData.status)">
                {{ getOrderStatusText(orderData.status) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <span class="label">下单时间：</span>
              <span>{{ orderData.orderTime }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <span class="label">总金额：</span>
              <span class="amount">¥{{ orderData.amount ? orderData.amount.toFixed(2) : '0.00' }}</span>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="info-item">
              <span class="label">备注：</span>
              <span>{{ orderData.remark || '无' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="box-card">
      <div slot="header" class="card-header">
        <span>订单明细</span>
      </div>
      <el-table :data="orderItems" border style="width: 100%">
        <el-table-column type="index" label="#" width="50"></el-table-column>
        <el-table-column prop="name" label="菜品名称"></el-table-column>
        <el-table-column prop="price" label="单价" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="number" label="数量" width="100" align="center"></el-table-column>
        <el-table-column label="小计" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ (scope.row.price * scope.row.number).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="order-summary">
        <div class="summary-item">
          <span>总计：</span>
          <span class="total-price">¥{{ orderData.amount ? orderData.amount.toFixed(2) : '0.00' }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="box-card" v-if="orderData.status === 3">
      <div slot="header" class="card-header">
        <span>顾客评价</span>
      </div>
      <div v-if="reviews.length > 0">
        <div v-for="(review, index) in reviews" :key="index" class="review-item">
          <div class="review-header">
            <div class="review-info">
              <span class="dish-name">{{ review.dishName }}</span>
              <el-rate v-model="review.rating" disabled text-color="#ff9900"></el-rate>
              <span class="review-time">{{ review.createTime }}</span>
            </div>
          </div>
          <div class="review-content">
            {{ review.content }}
          </div>
          <div v-if="review.reply" class="review-reply">
            <div class="reply-header">商家回复：</div>
            <div class="reply-content">{{ review.reply }}</div>
          </div>
          <div v-else class="review-actions">
            <el-button type="text" @click="handleReply(review)">回复</el-button>
          </div>
        </div>
      </div>
      <div v-else class="no-review">
        <el-empty description="暂无评价"></el-empty>
      </div>
    </el-card>

    <el-dialog title="回复评价" :visible.sync="replyDialogVisible" width="50%">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="评价内容">
          <div class="review-content-preview">{{ currentReview.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容" prop="reply">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderDetail, updateOrderStatus, cancelOrder, acceptOrder, completeOrder } from '@/api/order'
import { getReviewList, replyReview } from '@/api/review'

export default {
  name: 'OrderDetail',
  data() {
    return {
      loading: false,
      orderId: null,
      orderData: {},
      orderItems: [],
      reviews: [],
      replyDialogVisible: false,
      currentReview: {},
      replyForm: {
        reply: ''
      }
    }
  },
  created() {
    this.orderId = this.$route.params.id
    console.log('订单详情页面 - 订单ID:', this.orderId, '类型:', typeof this.orderId)
    this.fetchOrderDetail()
  },
  methods: {
    fetchOrderDetail() {
      this.loading = true
      console.log('开始查询订单详情，订单ID:', this.orderId)
      getOrderDetail(this.orderId).then(response => {
        console.log('订单详情查询响应:', response)
        if (response.code === 200) {
          this.orderData = response.data
          this.orderItems = response.data.orderDetails || []
          this.fetchReviews()
        }
      }).catch(error => {
        console.error('查询订单详情失败:', error)
        this.$message.error('查询订单详情失败，请稍后再试')
        this.$router.push('/order')
      }).finally(() => {
        this.loading = false
      })
    },
    fetchReviews() {
      getReviewList({ orderId: this.orderId }).then(response => {
        if (response.code === 200) {
          this.reviews = response.data.records || []
        }
      }).catch(error => {
        console.error('查询评价失败:', error)
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
    goBack() {
      this.$router.push('/order')
    },
    handleAccept() {
      this.$confirm('确认接受该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        acceptOrder(this.orderId).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '订单已接受!'
            })
            this.fetchOrderDetail()
          }
        }).catch(error => {
          console.error('接单失败:', error)
          this.$message.error('接单失败，请稍后再试')
        })
      })
    },
    handleComplete() {
      this.$confirm('确认完成该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        completeOrder(this.orderId).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '订单已完成!'
            })
            this.fetchOrderDetail()
          }
        }).catch(error => {
          console.error('完成订单失败:', error)
          this.$message.error('完成订单失败，请稍后再试')
        })
      })
    },
    handleCancel() {
      this.$confirm('确认取消该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelOrder(this.orderId).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '订单已取消!'
            })
            this.fetchOrderDetail()
          }
        }).catch(error => {
          console.error('取消订单失败:', error)
          this.$message.error('取消订单失败，请稍后再试')
        })
      })
    },
    handleReply(review) {
      this.currentReview = review
      this.replyForm.reply = ''
      this.replyDialogVisible = true
    },
    submitReply() {
      if (!this.replyForm.reply.trim()) {
        this.$message.error('请输入回复内容')
        return
      }

      replyReview({
        id: this.currentReview.id,
        reply: this.replyForm.reply
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('回复成功')
          this.replyDialogVisible = false
          this.fetchReviews()
        }
      })
    }
  }
}
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  padding: 10px 0;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  width: 100px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.order-summary {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.summary-item {
  margin-left: 20px;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-left: 10px;
}

.review-item {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.review-info {
  display: flex;
  align-items: center;
}

.dish-name {
  font-weight: bold;
  margin-right: 10px;
}

.review-time {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.review-content {
  margin-bottom: 10px;
  line-height: 1.5;
}

.review-reply {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.reply-header {
  font-weight: bold;
  margin-bottom: 5px;
}

.review-content-preview {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
}

.no-review {
  padding: 30px 0;
}
</style> 