<template>
  <div class="my-order-container">
    <div class="page-header">
      <h3>我的订单</h3>
    </div>

    <div class="filter-container">
      <el-select v-model="queryParams.status" placeholder="订单状态" clearable @change="handleQuery">
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        @change="handleDateRangeChange"
      ></el-date-picker>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="order-list">
      <el-card v-for="(order, orderIndex) in orderList" :key="`order-${order.id}-${orderIndex}`" class="order-card">
        <div slot="header" class="order-header">
          <div class="order-info">
            <span class="order-number">订单号：{{ order.number }}</span>
            <span class="order-date">{{ order.createTime }}</span>
          </div>
          <div class="order-status">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
        </div>
        <div class="order-content">
          <div class="order-items">
            <div v-for="(item, itemIndex) in (order.orderDetails || [])" :key="`item-${item.id}-${itemIndex}-${order.id}`" class="order-item">
              <div class="item-image">
                <el-image 
                  :src="item.image" 
                  fit="cover"
                  @error="handleImageError"
                >
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
              </div>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-price-qty">
                  <span class="item-price">¥{{ item.price ? item.price.toFixed(2) : '0.00' }}</span>
                                      <span class="item-qty">x {{ item.number || item.quantity || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="order-summary">
            <div class="order-table">餐桌：{{ order.tableName }}</div>
            <div class="order-total">
              <span>共{{ getTotalItems(order) }}件商品</span>
              <span class="total-price">合计：¥{{ order.amount ? order.amount.toFixed(2) : '0.00' }}</span>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <div class="order-remark" v-if="order.remark">
            备注：{{ order.remark }}
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 0"
              type="primary"
              size="small"
              @click="handlePay(order)"
            >
              立即支付
            </el-button>
            <el-button
              v-if="order.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(order)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 3 && !order.reviewed"
              type="primary"
              size="small"
              @click="handleReview(order)"
            >
              评价
            </el-button>
            <el-button
              v-if="order.status === 3 && order.reviewed"
              type="info"
              size="small"
              @click="viewReview(order)"
            >
              查看评价
            </el-button>
          </div>
        </div>
      </el-card>

      <div class="empty-result" v-if="orderList.length === 0">
        <el-empty description="暂无订单"></el-empty>
      </div>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParams.page"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </div>

    <el-dialog title="订单评价" :visible.sync="reviewDialogVisible" width="40%">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewForm" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="reviewForm.rating"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            :show-text="true"
            :texts="['很差', '较差', '一般', '较好', '很好']"
          ></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            type="textarea"
            v-model="reviewForm.content"
            :rows="4"
            placeholder="请输入您的评价内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </div>
    </el-dialog>

    <el-dialog title="评价详情" :visible.sync="viewReviewDialogVisible" width="40%">
      <div class="review-detail" v-if="currentReview">
        <div class="review-rating">
          <span class="label">评分：</span>
          <el-rate
            v-model="currentReview.rating"
            disabled
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          ></el-rate>
        </div>
        <div class="review-content">
          <span class="label">评价内容：</span>
          <div class="content">{{ currentReview.content }}</div>
        </div>
        <div class="review-time">
          <span class="label">评价时间：</span>
          <span>{{ currentReview.createTime }}</span>
        </div>
        <div class="review-reply" v-if="currentReview.reply">
          <span class="label">商家回复：</span>
          <div class="reply">{{ currentReview.reply }}</div>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="选择支付方式" :visible.sync="payDialogVisible" width="30%">
      <div class="pay-methods">
        <div class="order-info">
          <div>订单号：{{ currentOrder?.number }}</div>
          <div class="amount">支付金额：<span class="price">¥{{ currentOrder?.amount?.toFixed(2) }}</span></div>
        </div>
        <div class="payment-methods">
          <el-radio-group v-model="selectedPaymentMethod">
            <el-radio :label="1" class="payment-option">
              <div class="method-content">
                <i class="el-icon-message"></i>
                <span>微信支付</span>
              </div>
            </el-radio>
            <el-radio :label="2" class="payment-option">
              <div class="method-content">
                <i class="el-icon-coin"></i>
                <span>支付宝</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyOrders, cancelOrder, payOrder } from '@/api/order'
import { addReview, getReviewByOrderId } from '@/api/review'

export default {
  name: 'MyOrder',
  data() {
    return {
      queryParams: {
        page: 1,
        pageSize: 10,
        status: ''
      },
      dateRange: [],
      orderList: [],
      total: 0,
      statusOptions: [
        { value: 0, label: '待支付' },
        { value: 1, label: '已支付' },
        { value: 2, label: '制作中' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已取消' }
      ],
      reviewDialogVisible: false,
      viewReviewDialogVisible: false,
      reviewForm: {
        orderId: '',
        rating: 5,
        content: ''
      },
      reviewRules: {
        rating: [
          { required: true, message: '请选择评分', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入评价内容', trigger: 'blur' },
          { min: 5, max: 200, message: '评价内容长度在 5 到 200 个字符', trigger: 'blur' }
        ]
      },
      currentReview: null,
      currentOrder: null,
      payDialogVisible: false,
      selectedPaymentMethod: 1
    }
  },
  created() {
    this.getOrderList()
  },
  methods: {
    getOrderList() {
      const params = { ...this.queryParams }
      
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }

      getMyOrders(params).then(response => {
        if (response.code === 200) {
          // 确保所有订单ID都是字符串类型，避免JavaScript大数字精度问题
          const records = response.data.records.map(order => ({
            ...order,
            id: String(order.id), // 强制转换订单ID为字符串
            orderDetails: order.orderDetails ? order.orderDetails.map(detail => ({
              ...detail,
              id: String(detail.id), // 订单详情ID也转换为字符串
              orderId: String(detail.orderId || detail.order_id || order.id) // 确保关联的订单ID也是字符串
            })) : []
          }))
          
          console.log('处理后的订单数据示例:', records[0] ? {
            id: records[0].id,
            idType: typeof records[0].id,
            originalId: response.data.records[0] ? response.data.records[0].id : 'undefined'
          } : '无订单数据')
          
          this.orderList = records
          this.total = response.data.total
        }
      }).catch(error => {
        console.error('查询订单列表失败:', error)
        this.$message.error('查询订单列表失败，请稍后再试')
        this.orderList = []
        this.total = 0
      })
    },
    handleQuery() {
      this.queryParams.page = 1
      this.getOrderList()
    },
    resetQuery() {
      this.queryParams = {
        page: 1,
        pageSize: 10,
        status: ''
      }
      this.dateRange = []
      this.getOrderList()
    },
    handleSizeChange(size) {
      this.queryParams.pageSize = size
      this.getOrderList()
    },
    handleCurrentChange(page) {
      this.queryParams.page = page
      this.getOrderList()
    },
    handleDateRangeChange(val) {
      this.dateRange = val
    },
    getStatusType(status) {
      const statusMap = {
        0: 'warning',
        1: 'primary',
        2: 'primary',
        3: 'success',
        4: 'info'
      }
      return statusMap[status] || 'info'
    },
    getStatusText(status) {
      const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '制作中',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知状态'
    },
    getTotalItems(order) {
      if (!order.orderDetails || !Array.isArray(order.orderDetails)) {
        return 0
      }
      return order.orderDetails.reduce((total, item) => total + (item.number || item.quantity || 0), 0)
    },
    handleCancel(order) {
      console.log('取消订单 - 原始订单对象:', order)
      console.log('取消订单 - 订单ID:', order.id, '类型:', typeof order.id)
      
      // 确保订单ID作为字符串处理，避免JavaScript大数字精度问题
      const orderIdStr = String(order.id)
      console.log('取消订单 - 订单ID字符串:', orderIdStr)
      
      this.$confirm('确定要取消该订单吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log('确认取消订单，调用API，订单ID:', orderIdStr)
        cancelOrder(orderIdStr).then(response => {
          console.log('取消订单API响应:', response)
          if (response.code === 200) {
            this.$message.success('订单取消成功')
            this.getOrderList()
          }
        }).catch(error => {
          console.error('取消订单失败:', error)
          this.$message.error('取消订单失败，请稍后再试')
        })
      }).catch(() => {})
    },
    handleReview(order) {
      this.currentOrder = order
      this.reviewForm = {
        orderId: String(order.id), // 确保ID作为字符串处理
        rating: 5,
        content: ''
      }
      this.reviewDialogVisible = true
    },
    submitReview() {
      this.$refs.reviewForm.validate(valid => {
        if (valid) {
          addReview(this.reviewForm).then(response => {
            if (response.code === 200) {
              this.$message.success('评价提交成功')
              this.reviewDialogVisible = false
              this.getOrderList()
            }
          })
        }
      })
    },
    viewReview(order) {
      getReviewByOrderId(String(order.id)).then(response => {
        if (response.code === 200) {
          const list = response.data || []
          if (list.length > 0) {
            this.currentReview = list[0]
            this.viewReviewDialogVisible = true
          } else {
            this.$message.info('暂无评价内容')
          }
        }
      }).catch(() => {
        this.$message.error('获取评价失败')
      })
    },
    handlePay(order) {
      console.log('支付订单 - 原始订单对象:', order)
      console.log('支付订单 - 订单ID:', order.id, '类型:', typeof order.id)
      
      // 确保订单ID作为字符串处理，避免JavaScript大数字精度问题
      const orderIdStr = String(order.id)
      console.log('支付订单 - 订单ID字符串:', orderIdStr)
      
      this.currentOrder = { ...order, id: orderIdStr } // 创建副本并确保ID为字符串
      this.payDialogVisible = true
    },
    confirmPay() {
      if (!this.selectedPaymentMethod) {
        this.$message.warning('请选择支付方式')
        return
      }
      
      payOrder(this.currentOrder.id, this.selectedPaymentMethod).then(response => {
        if (response.code === 200) {
          this.$message.success('支付成功')
          this.payDialogVisible = false
          this.getOrderList()
        }
      }).catch(error => {
        console.error('支付失败:', error)
        this.$message.error('支付失败，请稍后再试')
      })
    },
    handleImageError() {
      // 处理图片加载错误后的逻辑
      console.error('图片加载失败')
    }
  }
}
</script>

<style scoped>
.my-order-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-number {
  font-weight: bold;
  margin-right: 15px;
}

.order-date {
  color: #909399;
}

.order-content {
  padding: 10px 0;
}

.order-item {
  display: flex;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.order-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.item-image {
  width: 60px;
  height: 60px;
  margin-right: 10px;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  margin-bottom: 5px;
}

.item-price-qty {
  display: flex;
  justify-content: space-between;
}

.item-price {
  color: #f56c6c;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.total-price {
  color: #f56c6c;
  font-weight: bold;
  margin-left: 10px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.order-remark {
  color: #909399;
  font-size: 13px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.empty-result {
  margin-top: 40px;
}

.review-detail {
  padding: 10px;
}

.review-detail .label {
  font-weight: bold;
  margin-right: 5px;
}

.review-rating,
.review-content,
.review-time,
.review-reply {
  margin-bottom: 15px;
}

.review-content .content,
.review-reply .reply {
  margin-top: 5px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pay-methods {
  padding: 10px;
}

.order-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.order-info .amount {
  margin-top: 8px;
  font-size: 16px;
}

.order-info .price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.payment-methods {
  margin-top: 15px;
}

.payment-option {
  display: block;
  width: 100%;
  margin-bottom: 10px;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.payment-option:hover {
  border-color: #409eff;
}

.payment-option.is-checked {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.method-content {
  display: flex;
  align-items: center;
}

.method-content i {
  font-size: 24px;
  margin-right: 10px;
  color: #409eff;
}

.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 10px;
  border-radius: 4px;
}

.image-slot i {
  font-size: 14px;
  margin-bottom: 2px;
}

.order-actions {
  margin-left: auto;
  display: flex;
  gap: 10px;
}
</style> 