<template>
  <div class="my-review-container">
    <div class="page-header">
      <h3>我的评价</h3>
    </div>

    <div class="filter-container">
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

    <div class="review-list">
      <el-card v-for="review in reviewList" :key="review.id" class="review-card">
        <div slot="header" class="review-header">
          <div class="review-info">
            <span class="review-date">{{ review.createTime }}</span>
            <el-rate
              v-model="review.rating"
              disabled
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            ></el-rate>
          </div>
        </div>
        <div class="review-content">
          <div class="order-info">
            <span class="order-number">订单号：{{ review.orderNumber }}</span>
            <span class="order-date">下单时间：{{ review.orderTime }}</span>
          </div>
          <div class="review-text">
            <p>{{ review.content }}</p>
          </div>
          <div class="dish-list">
            <span class="dish-info">菜品：{{ review.dishName }}</span>
          </div>
          <div class="review-reply" v-if="review.reply">
            <div class="reply-header">
              <i class="el-icon-s-custom"></i>
              <span>商家回复：</span>
            </div>
            <div class="reply-content">
              <p>{{ review.reply }}</p>
              <span class="reply-time">{{ review.replyTime }}</span>
            </div>
          </div>
        </div>
        <div class="review-footer">
          <el-button size="small" type="danger" @click="handleDelete(review)" v-if="!review.reply">删除评价</el-button>
          <el-button size="small" type="primary" @click="handleEdit(review)" v-if="!review.reply">编辑评价</el-button>
        </div>
      </el-card>

      <div class="empty-result" v-if="reviewList.length === 0">
        <el-empty description="暂无评价"></el-empty>
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

    <el-dialog title="编辑评价" :visible.sync="editDialogVisible" width="40%">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="editForm.rating"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            :show-text="true"
            :texts="['很差', '较差', '一般', '较好', '很好']"
          ></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            type="textarea"
            v-model="editForm.content"
            :rows="4"
            placeholder="请输入您的评价内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyReviews, updateReview, deleteReview } from '@/api/review'

export default {
  name: 'MyReview',
  data() {
    return {
      queryParams: {
        page: 1,
        pageSize: 10
      },
      dateRange: [],
      reviewList: [],
      total: 0,
      editDialogVisible: false,
      editForm: {
        id: '',
        rating: 5,
        content: ''
      },
      editRules: {
        rating: [
          { required: true, message: '请选择评分', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入评价内容', trigger: 'blur' },
          { min: 5, max: 200, message: '评价内容长度在 5 到 200 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getReviewList()
  },
  methods: {
    getReviewList() {
      const params = { ...this.queryParams }
      
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }

      getMyReviews(params).then(response => {
        if (response.code === 200) {
          this.reviewList = response.data.records
          this.total = Number(response.data.total) || 0
        }
      })
    },
    handleQuery() {
      this.queryParams.page = 1
      this.getReviewList()
    },
    resetQuery() {
      this.queryParams = {
        page: 1,
        pageSize: 10
      }
      this.dateRange = []
      this.getReviewList()
    },
    handleSizeChange(size) {
      this.queryParams.pageSize = size
      this.getReviewList()
    },
    handleCurrentChange(page) {
      this.queryParams.page = page
      this.getReviewList()
    },
    handleDateRangeChange(val) {
      this.dateRange = val
    },
    handleEdit(review) {
      this.editForm = {
        id: review.id,
        rating: review.rating,
        content: review.content
      }
      this.editDialogVisible = true
    },
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          updateReview(this.editForm).then(response => {
            if (response.code === 200) {
              this.$message.success('评价更新成功')
              this.editDialogVisible = false
              this.getReviewList()
            }
          })
        }
      })
    },
    handleDelete(review) {
      this.$confirm('确定要删除该评价吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteReview(review.id).then(response => {
          if (response.code === 200) {
            this.$message.success('评价已删除')
            this.getReviewList()
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.my-review-container {
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

.review-card {
  margin-bottom: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-date {
  color: #909399;
  margin-right: 15px;
}

.review-content {
  padding: 10px 0;
}

.order-info {
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
}

.order-number {
  margin-right: 20px;
}

.review-text {
  margin-bottom: 15px;
}

.review-text p {
  margin: 0;
  line-height: 1.6;
}

.dish-list {
  margin-bottom: 15px;
}

.dish-info {
  color: #606266;
  font-size: 14px;
  background-color: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

.dish-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.review-reply {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin-top: 15px;
}

.reply-header {
  color: #606266;
  margin-bottom: 5px;
}

.reply-header i {
  margin-right: 5px;
}

.reply-content p {
  margin: 5px 0;
  line-height: 1.6;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  display: block;
  text-align: right;
  margin-top: 5px;
}

.review-footer {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.empty-result {
  margin-top: 40px;
}
</style> 