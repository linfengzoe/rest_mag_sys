<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>数据统计与分析</h2>
      <p>餐厅经营数据统计分析</p>
    </div>

    <!-- 数据概览卡片 -->
    <div class="dashboard-cards">
      <div class="card">
        <div class="card-icon today">📊</div>
        <div class="card-content">
          <h3>今日销售额</h3>
          <div class="card-value">¥{{ todayData.total_sales || 0 }}</div>
          <div class="card-subtitle">{{ todayData.order_count || 0 }} 笔订单</div>
        </div>
      </div>
      <div class="card">
        <div class="card-icon month">💰</div>
        <div class="card-content">
          <h3>本月销售额</h3>
          <div class="card-value">¥{{ monthData.month_sales || 0 }}</div>
          <div class="card-subtitle">{{ monthData.month_orders || 0 }} 笔订单</div>
        </div>
      </div>
      <div class="card">
        <div class="card-icon customer">👥</div>
        <div class="card-content">
          <h3>活跃顾客</h3>
          <div class="card-value">{{ customerCount }}</div>
          <div class="card-subtitle">本月活跃用户</div>
        </div>
      </div>
      <div class="card">
        <div class="card-icon dish">🍽️</div>
        <div class="card-content">
          <h3>热销菜品</h3>
          <div class="card-value">{{ topDishes.length }}</div>
          <div class="card-subtitle">销量排行榜</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 销售趋势 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>最近7天销售趋势</h3>
          <div class="date-range">
            <label>日期范围：</label>
            <input type="date" v-model="startDate" @change="loadOrderStatistics">
            <span>至</span>
            <input type="date" v-model="endDate" @change="loadOrderStatistics">
          </div>
        </div>
        <div class="chart-content">
          <!-- 折线图容器，高度需固定 -->
          <div ref="salesChart" style="width: 100%; height: 350px;"></div>
        </div>
      </div>

      <!-- 菜品销售排行 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>菜品销售排行榜</h3>
          <div class="ranking-controls">
            <select v-model="rankingType" @change="loadDishRanking">
              <option value="quantity">按销量排行</option>
              <option value="sales">按销售额排行</option>
              <option value="review">按评论星级排行</option>
            </select>
            <select v-model="rankingLimit" @change="loadDishRanking">
              <option value="5">前5名</option>
              <option value="10">前10名</option>
              <option value="20">前20名</option>
            </select>
            <button class="toggle-btn" @click="showChart = !showChart">
              {{ showChart ? '显示列表' : '显示图表' }}
            </button>
          </div>
        </div>
        <div class="chart-content">
          <div v-show="showChart" ref="dishChart" class="dish-chart" style="width:100%;height:400px;"></div>
          <div v-show="!showChart" class="ranking-list" style="max-height:300px;overflow:auto;">
            <div v-for="(dish, index) in dishRanking" :key="dish.dish_id" class="ranking-item">
              <div class="rank-number">{{ index + 1 }}</div>
              <div class="dish-info">
                <div class="dish-name">{{ dish.dish_name }}</div>
                <div class="dish-category">{{ dish.category_name }}</div>
              </div>
              <div class="dish-stats">
                <div class="stat-value">
                  <template v-if="rankingType === 'quantity'">
                    {{ dish.total_quantity }}份
                  </template>
                  <template v-else-if="rankingType === 'sales'">
                    ¥{{ dish.total_sales }}
                  </template>
                  <template v-else>
                    <span style="color:#f7ba2a;font-weight:bold;">{{ dish.avg_rating ? dish.avg_rating.toFixed(1) : '-' }}</span>
                    <span style="margin-left:4px;color:#999;">★</span>
                  </template>
                </div>
                <div class="stat-subtitle">
                  <template v-if="rankingType === 'quantity'">销量</template>
                  <template v-else-if="rankingType === 'sales'">销售额</template>
                  <template v-else>评论数：{{ dish.review_count }}</template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分析报表 -->
    <div class="analysis-section">
      <div class="tabs">
        <div class="tab-buttons">
          <button 
            v-for="tab in tabs" 
            :key="tab.key"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key; loadTabData(tab.key)"
          >
            {{ tab.name }}
          </button>
        </div>
        
        <div class="tab-content">
          <!-- 顾客分析 -->
          <div v-if="activeTab === 'customer'" class="tab-panel">
            <h4>顾客行为分析</h4>
            <table class="analysis-table">
              <thead>
                <tr>
                  <th>顾客姓名</th>
                  <th>会员等级</th>
                  <th>订单次数</th>
                  <th>总消费</th>
                  <th>平均消费</th>
                  <th>客户类型</th>
                  <th>最后消费</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="customer in customerAnalysis" :key="customer.user_id">
                  <td>{{ customer.customer_name }}</td>
                  <td>
                    <span :class="'level-' + customer.member_level">
                      {{ getMemberLevelName(customer.member_level) }}
                    </span>
                  </td>
                  <td>{{ customer.order_count }}</td>
                  <td>¥{{ customer.total_consumption }}</td>
                  <td>¥{{ customer.average_consumption }}</td>
                  <td>{{ customer.customer_level }}</td>
                  <td>{{ formatDate(customer.last_order_date) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 员工绩效 -->
          <div v-if="activeTab === 'employee'" class="tab-panel">
            <h4>员工绩效统计</h4>
            <table class="analysis-table">
              <thead>
                <tr>
                  <th>员工姓名</th>
                  <th>职位</th>
                  <th>处理订单</th>
                  <th>销售额</th>
                  <th>平均订单</th>
                  <th>绩效等级</th>
                  <th>入职时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="employee in employeePerformance" :key="employee.user_id">
                  <td>{{ employee.employee_name }}</td>
                  <td>{{ employee.position }}</td>
                  <td>{{ employee.handled_orders }}</td>
                  <td>¥{{ employee.total_sales }}</td>
                  <td>¥{{ employee.average_order_value }}</td>
                  <td>
                    <span :class="'performance-' + getPerformanceClass(employee.performance_level)">
                      {{ employee.performance_level }}
                    </span>
                  </td>
                  <td>{{ formatDate(employee.hire_date) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 时间分析 -->
          <div v-if="activeTab === 'time'" class="tab-panel">
            <h4>时间段分析</h4>
            <table class="analysis-table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>时段</th>
                  <th>订单数</th>
                  <th>销售额</th>
                  <th>平均订单</th>
                  <th>占比</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="time in timeDistribution" :key="time.hour_of_day">
                  <td>{{ time.hour_of_day }}:00</td>
                  <td>{{ time.time_period }}</td>
                  <td>{{ time.order_count }}</td>
                  <td>¥{{ time.total_sales }}</td>
                  <td>¥{{ time.average_order_value }}</td>
                  <td>{{ calculateTimePercentage(time) }}%</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 餐桌统计 -->
          <div v-if="activeTab === 'table'" class="tab-panel">
            <h4>餐桌累消统计</h4>
            <table class="analysis-table">
              <thead>
                <tr>
                  <th>餐桌名称</th>
                  <th>累计消费</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="table in tableStatistics" :key="table.table_name">
                  <td>{{ table.table_name || table.table_id }}</td>
                  <td>¥{{ table.total_sales }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  getDashboardData,
  getOrderStatistics,
  getDishSalesRanking,
  getCustomerBehaviorAnalysis,
  getEmployeePerformance,
  getHourlyOrderDistribution,
  getDishReviewRanking,
  getTableUtilizationStatistics
} from '@/api/statistics'
import * as echarts from 'echarts'

export default {
  name: 'Statistics',
  data() {
    return {
      // 仪表板数据
      todayData: {},
      monthData: {},
      customerCount: 0,
      topDishes: [],
      
      // 查询条件
      startDate: '',
      endDate: '',
      rankingType: 'quantity',
      rankingLimit: 10,
      
      // 统计数据
      orderStatistics: [],
      dishRanking: [],
      customerAnalysis: [],
      employeePerformance: [],
      timeDistribution: [],
      tableStatistics: [],
      
      // 标签页
      tabs: [
        { key: 'customer', name: '顾客分析' },
        { key: 'employee', name: '员工绩效' },
        { key: 'time', name: '时间分析' },
        { key: 'table', name: '餐桌统计' }
      ],
      activeTab: 'customer',
      // 保存 ECharts 实例
      salesChartInstance: null,
      dishChartInstance: null,
      showChart: true,
    }
  },
  mounted() {
    this.initializeDates()
    this.loadDashboardData()
    this.loadOrderStatistics()
    this.loadDishRanking()
    this.loadTabData('customer')
  },
  methods: {
    initializeDates() {
      const today = new Date()
      this.endDate = this.formatDateYMD(today)
      const sevenDaysAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
      this.startDate = this.formatDateYMD(sevenDaysAgo)
    },
    
    async loadDashboardData() {
      try {
        const response = await getDashboardData()
        if (response.code === 200) {
          const data = response.data
          this.todayData = data.today || {}
          this.monthData = data.thisMonth || {}
          this.topDishes = data.topDishes || []
          this.customerCount = data.thisMonth?.unique_customers || 0
        }
      } catch (error) {
        console.error('加载仪表板数据失败:', error)
      }
    },
    
    async loadOrderStatistics() {
      try {
        const response = await getOrderStatistics({
          startDate: this.startDate,
          endDate: this.endDate
        })
        if (response.code === 200) {
          this.orderStatistics = response.data || []
          // 加载完数据后渲染折线图
          this.$nextTick(() => {
            this.renderSalesTrendChart()
          })
        }
      } catch (error) {
        console.error('加载订单统计失败:', error)
      }
    },
    
    async loadDishRanking() {
      try {
        if (this.rankingType === 'review') {
          const response = await getDishReviewRanking({ limit: this.rankingLimit })
          if (response.code === 200) {
            this.dishRanking = response.data || []
            this.$nextTick(()=>{ this.renderDishChart() })
          }
        } else {
          const response = await getDishSalesRanking({
            limit: this.rankingLimit,
            type: this.rankingType
          })
          if (response.code === 200) {
            this.dishRanking = response.data || []
            this.$nextTick(()=>{ this.renderDishChart() })
          }
        }
      } catch (error) {
        console.error('加载菜品排行失败:', error)
      }
    },
    
    async loadTabData(tab) {
      try {
        switch (tab) {
          case 'customer':
            const customerResponse = await getCustomerBehaviorAnalysis({ limit: 20 })
            if (customerResponse.code === 200) {
              this.customerAnalysis = customerResponse.data || []
            }
            break
            
          case 'employee':
            const employeeResponse = await getEmployeePerformance({ limit: 20 })
            if (employeeResponse.code === 200) {
              this.employeePerformance = employeeResponse.data || []
            }
            break
            
          case 'time':
            const timeResponse = await getHourlyOrderDistribution()
            if (timeResponse.code === 200) {
              this.timeDistribution = timeResponse.data || []
            }
            break
            
          case 'table':
            const tableResponse = await getTableUtilizationStatistics();
            if (tableResponse.code === 200) {
              this.tableStatistics = tableResponse.data || [];
            }
            break;
        }
      } catch (error) {
        console.error(`加载${tab}数据失败:`, error)
      }
    },
    
    // 工具方法
    calculateCompletionRate(item) {
      if (!item.order_count) return 0
      return Math.round((item.completed_orders / item.order_count) * 100)
    },
    
    // 新增：将日期格式化为 YYYY-MM-DD（本地时区）
    formatDateYMD(date) {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      return `${y}-${m}-${d}`;
    },
    
    calculateTimePercentage(time) {
      const total = this.timeDistribution.reduce((sum, item) => sum + Number(item.order_count || 0), 0)
      const count = Number(time.order_count || 0)
      return total ? Math.round((count / total) * 100) : 0
    },
    
    getMemberLevelName(level) {
      const levels = { 0: '普通', 1: '银卡', 2: '金卡', 3: 'VIP' }
      return levels[level] || '普通'
    },
    
    getPerformanceClass(level) {
      const classes = { '优秀': 'excellent', '良好': 'good', '一般': 'normal', '待提高': 'poor' }
      return classes[level] || 'normal'
    },
    
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleDateString()
    },
    
    // 渲染最近7天销售趋势折线图
    renderSalesTrendChart() {
      if (!this.$refs.salesChart) return

      // 初始化或获取已有实例
      if (!this.salesChartInstance) {
        this.salesChartInstance = echarts.init(this.$refs.salesChart)
        // 自适应窗口大小
        window.addEventListener('resize', () => {
          this.salesChartInstance && this.salesChartInstance.resize()
        })
      }

      // 构造 x 轴日期 & y 轴数据
      const dates = this.orderStatistics
        .map(i => {
          if (typeof i.order_date === 'number') {
            const d = new Date(i.order_date)
            return this.formatDateYMD(d)
          }
          return i.order_date
        })
        .reverse() // 日期升序

      const orderCounts = [...this.orderStatistics].map(i => i.order_count).reverse()
      const totalSales = [...this.orderStatistics].map(i => i.total_sales).reverse()

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['订单数', '销售额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: [
          {
            type: 'value',
            name: '订单数',
            position: 'left'
          },
          {
            type: 'value',
            name: '销售额',
            position: 'right',
            axisLabel: {
              formatter: '¥{value}'
            }
          }
        ],
        series: [
          {
            name: '订单数',
            type: 'line',
            data: orderCounts
          },
          {
            name: '销售额',
            type: 'line',
            yAxisIndex: 1,
            data: totalSales,
            smooth: true
          }
        ]
      }

      this.salesChartInstance.setOption(option)
    },
    renderDishChart(){
      if(!this.$refs.dishChart) return;
      if(!this.dishChartInstance){
        this.dishChartInstance = echarts.init(this.$refs.dishChart)
        window.addEventListener('resize', ()=>{ this.dishChartInstance && this.dishChartInstance.resize() })
      }
      const names = this.dishRanking.map(d=>d.dish_name).reverse();
      const values = this.dishRanking.map(d=>{
        if(this.rankingType==='quantity') return d.total_quantity;
        if(this.rankingType==='sales') return d.total_sales;
        return d.avg_rating;
      }).reverse();
      const unit = this.rankingType==='quantity' ? '份': (this.rankingType==='sales'?'¥':'★');
      const gradient= new echarts.graphic.LinearGradient(0,0,1,0,[{offset:0,color:'#4facfe'},{offset:1,color:'#00f2fe'}]);
      const option={
        tooltip:{trigger:'axis',axisPointer:{type:'shadow'},formatter:(params)=>{
          const p=params[0];return `${p.name}<br/>${p.value}${unit}`;
        }},
        grid:{left:100,right:20,top:10,bottom:20},
        xAxis:{type:'value',axisLabel:{formatter:this.rankingType==='sales'? '¥{value}':'{value}'}},
        yAxis:{type:'category',data:names,inverse:true},
        series:[{type:'bar',data:values,label:{show:true,position:'right',formatter:v=>`${v.value}${unit}`},barWidth:20,itemStyle:{color:gradient,barBorderRadius:[6,6,6,6]}}]
      };
      this.dishChartInstance.setOption(option);
    },
    watch:{
      showChart(val){
        if(val){
          this.$nextTick(()=>{
            this.renderDishChart();
            this.dishChartInstance && this.dishChartInstance.resize();
          })
        }
      }
    },
  }
}
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  color: #333;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  margin: 0;
}

/* 仪表板卡片 */
.dashboard-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.card-icon.today { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.card-icon.month { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.card-icon.customer { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.card-icon.dish { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.card-content h3 {
  color: #666;
  font-size: 14px;
  margin: 0 0 8px 0;
  font-weight: normal;
}

.card-value {
  color: #333;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.card-subtitle {
  color: #999;
  font-size: 12px;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  color: #333;
}

.date-range label,
.ranking-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-range input,
.ranking-controls select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
}

.chart-content {
  padding: 20px;
}

/* 趋势表格 */
.trend-table {
  width: 100%;
  border-collapse: collapse;
}

.trend-table th,
.trend-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.trend-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

/* 排行榜 */
.ranking-list {
  max-height: 400px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.rank-number {
  width: 40px;
  height: 40px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 16px;
}

.ranking-item:nth-child(1) .rank-number { background: #ffd700; color: white; }
.ranking-item:nth-child(2) .rank-number { background: #c0c0c0; color: white; }
.ranking-item:nth-child(3) .rank-number { background: #cd7f32; color: white; }

.dish-info {
  flex: 1;
  margin-right: 16px;
}

.dish-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.dish-category {
  color: #666;
  font-size: 12px;
}

.dish-stats {
  text-align: right;
}

.stat-value {
  font-weight: bold;
  color: #333;
}

.stat-subtitle {
  color: #666;
  font-size: 12px;
}

/* 分析报表 */
.analysis-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #eee;
}

.tab-buttons button {
  padding: 16px 24px;
  border: none;
  background: white;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tab-buttons button.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.tab-buttons button:hover {
  background: #f8f9fa;
}

.tab-content {
  padding: 20px;
}

.tab-panel h4 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

/* 分析表格 */
.analysis-table {
  width: 100%;
  border-collapse: collapse;
}

.analysis-table th,
.analysis-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.analysis-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

/* 会员等级标签 */
.level-0 { color: #909399; }
.level-1 { color: #c0c4cc; }
.level-2 { color: #f39c12; }
.level-3 { color: #e74c3c; }

/* 绩效等级标签 */
.performance-excellent { color: #67c23a; }
.performance-good { color: #409eff; }
.performance-normal { color: #e6a23c; }
.performance-poor { color: #f56c6c; }

/* 响应式设计 */
@media (max-width: 768px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .dashboard-cards {
    grid-template-columns: 1fr;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .tab-buttons {
    flex-wrap: wrap;
  }
  
  .analysis-table {
    font-size: 12px;
  }
  
  .analysis-table th,
  .analysis-table td {
    padding: 8px 4px;
  }
}
</style> 