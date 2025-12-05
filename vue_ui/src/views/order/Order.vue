<template>
  <div class="order-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.id"
        placeholder="订单号"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.status"
        placeholder="订单状态"
        clearable
        style="width: 200px"
        class="filter-item"
      >
        <el-option
          v-for="item in statusOptions"
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
          {{ scope.row.number || scope.row.id }}
        </template>
      </el-table-column>
      <el-table-column label="顾客" width="120">
        <template slot-scope="scope">
          {{ scope.row.customerName }}
        </template>
      </el-table-column>
      <el-table-column label="餐桌号" width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.tableName || '无' }}
        </template>
      </el-table-column>
      <el-table-column label="金额" width="120" align="right">
        <template slot-scope="scope">
          ¥{{ scope.row.amount.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="getOrderStatusType(scope.row.status)">
            {{ getOrderStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="180" align="center">
        <template slot-scope="scope">
          {{ scope.row.orderTime }}
        </template>
      </el-table-column>
      <el-table-column label="备注">
        <template slot-scope="scope">
          {{ scope.row.remark }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleDetail(scope.row)"
          >
            详情
          </el-button>
          <el-button
            size="mini"
            type="info"
            @click="handleDetailPage(scope.row)"
          >
            更多
          </el-button>

          <el-button
            v-if="scope.row.status === 1"
            size="mini"
            type="success"
            @click="handleAccept(scope.row)"
          >
            接单
          </el-button>
          <el-button
            v-if="scope.row.status === 2"
            size="mini"
            type="success"
            @click="handleComplete(scope.row)"
          >
            完成
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            size="mini"
            type="danger"
            @click="handleCancel(scope.row)"
          >
            取消
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

    <el-dialog title="订单详情" :visible.sync="detailDialogVisible" width="60%">
      <div class="order-detail">
        <div class="order-info">
          <div class="info-item">
            <span class="label">订单号：</span>
            <span>{{ currentOrder.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">顾客：</span>
            <span>{{ currentOrder.customerName }}</span>
          </div>
          <div class="info-item">
            <span class="label">餐桌号：</span>
            <span>{{ currentOrder.tableName || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">订单状态：</span>
            <el-tag :type="getOrderStatusType(currentOrder.status)">
              {{ getOrderStatusText(currentOrder.status) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span>{{ currentOrder.orderTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">备注：</span>
            <span>{{ currentOrder.remark || '无' }}</span>
          </div>
        </div>

        <div class="order-items">
          <h3>订单明细</h3>
          <el-table :data="orderItems" border style="width: 100%">
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
          <div class="order-total">
            <span>总计：</span>
            <span class="total-price">¥{{ currentOrder.amount ? currentOrder.amount.toFixed(2) : '0.00' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderList, getOrderDetail, acceptOrder, completeOrder, cancelOrder, rejectOrder } from '@/api/order'
import Pagination from '@/components/Pagination'

export default {
  name: 'Order',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        pageSize: 10,
        id: undefined,
        status: undefined,
        startDate: undefined,
        endDate: undefined
      },
      dateRange: [],
      statusOptions: [
        { label: '待支付', value: 0 },
        { label: '已支付', value: 1 },
        { label: '制作中', value: 2 },
        { label: '已完成', value: 3 },
        { label: '已取消', value: 4 }
      ],
      detailDialogVisible: false,
      currentOrder: {},
      orderItems: []
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
      getOrderList(this.listQuery).then(response => {
        this.list = response.data.records
        this.total = parseInt(response.data.total) || 0
        this.listLoading = false
      }).catch(error => {
        console.error('查询订单列表失败:', error)
        this.$message.error('查询订单列表失败，请稍后再试')
        this.list = []
        this.total = 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
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
    handleDetail(row) {
      this.currentOrder = Object.assign({}, row)
      getOrderDetail(row.id).then(response => {
        if (response.code === 200) {
          this.orderItems = response.data.orderDetails || []
          this.detailDialogVisible = true
        }
      }).catch(error => {
        console.error('查询订单详情失败:', error)
        this.$message.error('查询订单详情失败，请稍后再试')
      })
    },
    handleDetailPage(row) {
      this.$router.push(`/order/${row.id}`)
    },
    handleAccept(row) {
      this.$confirm('确认接受该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        acceptOrder(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '订单已确认!'
            })
            this.getList()
          }
        }).catch(error => {
          console.error('接单失败:', error)
          this.$message.error('接单失败，请稍后再试')
        })
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    },
    handleComplete(row) {
      this.$confirm('确认完成该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        completeOrder(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '订单已完成!'
            })
            this.getList()
          }
        }).catch(error => {
          console.error('完成订单失败:', error)
          this.$message.error('完成订单失败，请稍后再试')
        })
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    },
    handleCancel(row) {
      this.$confirm('确认取消该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelOrder(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '订单已取消!'
            })
            this.getList()
          }
        }).catch(error => {
          console.error('取消订单失败:', error)
          this.$message.error('取消订单失败，请稍后再试')
        })
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    },

  }
}
</script>

<style scoped>
.order-container {
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

.order-detail {
  padding: 20px;
}

.order-info {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  width: 100px;
}

.order-items h3 {
  margin-bottom: 20px;
}

.order-total {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-left: 10px;
}
  /* 订单号列自动换行 */
  .order-num .cell {
    white-space: normal !important;
    word-break: break-all;
    line-height: 18px;
}
</style> 