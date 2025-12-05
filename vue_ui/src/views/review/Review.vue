<template>
  <div class="review-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.content"
        placeholder="评价内容"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.rating"
        placeholder="评分"
        clearable
        style="width: 120px"
        class="filter-item"
      >
        <el-option
          v-for="item in ratingOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        class="filter-item"
        style="width: 350px"
      />
      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleFilter"
      >
        搜索
      </el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
    >
      <el-table-column label="订单号" width="100" align="center" class-name="order-num">
        <template slot-scope="scope">
          {{ scope.row.orderId }}
        </template>
      </el-table-column>

      <el-table-column label="顾客" width="120">
        <template slot-scope="scope">
          {{ scope.row.customerName }}
        </template>
      </el-table-column>
      <el-table-column label="菜品" width="150">
        <template slot-scope="scope">
          {{ scope.row.dishName }}
        </template>
      </el-table-column>
      <el-table-column label="评分" width="150" align="center">
        <template slot-scope="scope">
          <el-rate
            v-model="scope.row.rating"
            disabled
            text-color="#ff9900"
          ></el-rate>
        </template>
      </el-table-column>
      <el-table-column label="评价内容">
        <template slot-scope="scope">
          {{ scope.row.content }}
        </template>
      </el-table-column>
      <el-table-column label="评价时间" width="180" align="center">
        <template slot-scope="scope">
          {{ scope.row.createTime }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-if="!scope.row.reply"
            size="mini"
            type="primary"
            @click="handleReply(scope.row)"
          >
            回复
          </el-button>
          <el-button
            v-else
            size="mini"
            type="info"
            @click="handleViewReply(scope.row)"
          >
            查看回复
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="dialogStatus === 'reply' ? '回复评价' : '查看回复'" :visible.sync="dialogVisible" width="50%">
      <div class="review-detail">
        <div class="review-info">
          <div class="info-item">
            <span class="label">顾客：</span>
            <span>{{ currentReview.customerName }}</span>
          </div>
          <div class="info-item">
            <span class="label">菜品：</span>
            <span>{{ currentReview.dishName }}</span>
          </div>
          <div class="info-item">
            <span class="label">评分：</span>
            <el-rate
              v-model="currentReview.rating"
              disabled
              text-color="#ff9900"
            ></el-rate>
          </div>
          <div class="info-item">
            <span class="label">评价时间：</span>
            <span>{{ currentReview.createTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">评价内容：</span>
            <div class="review-content">{{ currentReview.content }}</div>
          </div>
        </div>

        <div v-if="dialogStatus === 'view'" class="reply-info">
          <div class="info-item">
            <span class="label">回复内容：</span>
            <div class="reply-content">{{ currentReview.reply }}</div>
          </div>
          <div class="info-item">
            <span class="label">回复时间：</span>
            <span>{{ currentReview.replyTime }}</span>
          </div>
        </div>

        <el-form v-else :model="replyForm" label-width="80px">
          <el-form-item label="回复内容" prop="reply">
            <el-input
              v-model="replyForm.reply"
              type="textarea"
              :rows="4"
              placeholder="请输入回复内容"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ dialogStatus === 'view' ? '关闭' : '取消' }}</el-button>
        <el-button v-if="dialogStatus === 'reply'" type="primary" @click="submitReply">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getReviewList, replyReview } from '@/api/review'
import Pagination from '@/components/Pagination'

export default {
  name: 'Review',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        pageSize: 10,
        content: undefined,
        rating: undefined,
        startDate: undefined,
        endDate: undefined
      },
      dateRange: [],
      ratingOptions: [
        { label: '1星', value: 1 },
        { label: '2星', value: 2 },
        { label: '3星', value: 3 },
        { label: '4星', value: 4 },
        { label: '5星', value: 5 }
      ],
      dialogVisible: false,
      dialogStatus: '',
      currentReview: {},
      replyForm: {
        reply: ''
      }
    }
  },
  watch: {
    dateRange(val) {
      if (val) {
        this.listQuery.startDate = val[0]
        this.listQuery.endDate = val[1]
      } else {
        this.listQuery.startDate = undefined
        this.listQuery.endDate = undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      getReviewList(this.listQuery).then(response => {
        this.list = response.data.records
        this.total = parseInt(response.data.total) || 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleReply(row) {
      this.dialogStatus = 'reply'
      this.currentReview = Object.assign({}, row)
      this.replyForm.reply = ''
      this.dialogVisible = true
    },
    handleViewReply(row) {
      this.dialogStatus = 'view'
      this.currentReview = Object.assign({}, row)
      this.dialogVisible = true
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
          this.dialogVisible = false
          this.getList()
        }
      })
    }
  }
}
</script>

<style scoped>
.review-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 10px;
}

.filter-item {
  display: inline-block;
  vertical-align: middle;
  margin-bottom: 10px;
  margin-right: 10px;
}

.review-detail {
  padding: 10px;
}

.review-info, .reply-info {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.label {
  font-weight: bold;
  width: 100px;
  flex-shrink: 0;
}

.review-content, .reply-content {
  flex: 1;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.5;
}
  /* 订单号自动换行 */
  .order-num .cell {
    white-space: normal !important;
    word-break: break-all;
    line-height: 18px;
  }
</style> 