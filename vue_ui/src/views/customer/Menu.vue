<template>
  <div class="menu-container">
    <div class="menu-header">
      <el-input
        placeholder="搜索菜品"
        v-model="searchQuery"
        class="search-input"
        prefix-icon="el-icon-search"
        clearable
        @clear="fetchDishes"
      ></el-input>
      <el-button type="primary" @click="searchDishes">搜索</el-button>
    </div>

    <div class="menu-content">
      <div class="category-sidebar">
        <el-menu
          :default-active="activeCategory && activeCategory.id ? activeCategory.id.toString() : ''"
          class="category-menu"
          @select="handleCategorySelect"
        >
          <el-menu-item index="all">
            <span>全部菜品</span>
          </el-menu-item>
          <el-menu-item
            v-for="category in categories"
            :key="category.id"
            :index="category.id ? category.id.toString() : ''"
          >
            <span>{{ category.name }}</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="dish-list">
        <div class="category-title" v-if="activeCategory">
          <h3>{{ activeCategory.name || '全部菜品' }}</h3>
        </div>

        <el-row :gutter="20">
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            v-for="dish in displayDishes"
            :key="dish.id"
          >
            <el-card class="dish-card" shadow="hover">
              <div class="dish-image">
                <el-image 
                  :src="dish.image" 
                  fit="cover"
                  @error="handleImageError"
                >
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                    <div>暂无图片</div>
                  </div>
                </el-image>
              </div>
              <div class="dish-info">
                <h4 class="dish-name">{{ dish.name }}</h4>
                <div class="dish-desc">{{ dish.description }}</div>
                <div class="dish-price-actions">
                  <div class="dish-price">¥{{ dish.price ? dish.price.toFixed(2) : '0.00' }}</div>
                  <div class="dish-actions">
                    <el-button
                      type="primary"
                      icon="el-icon-plus"
                      size="mini"
                      circle
                      @click="addToCart(dish)"
                    ></el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="empty-result" v-if="displayDishes.length === 0">
          <el-empty description="暂无菜品"></el-empty>
        </div>

        <div class="pagination-container" v-if="total > pageSize">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[8, 16, 24, 32]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
          ></el-pagination>
        </div>
      </div>
    </div>

    <div class="cart-fab" @click="goToCart">
      <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
        <i class="el-icon-shopping-cart-2"></i>
      </el-badge>
    </div>

    <el-dialog title="添加到购物车" :visible.sync="addCartDialogVisible" width="30%">
      <div class="add-cart-content">
        <div class="dish-preview">
          <el-image 
            :src="currentDish.image" 
            fit="cover" 
            style="width: 100px; height: 100px;"
            @error="handleImageError"
          >
            <div slot="error" class="image-slot" style="width: 100px; height: 100px;">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
          <div class="dish-preview-info">
            <h4>{{ currentDish.name }}</h4>
            <div class="dish-preview-price">¥{{ currentDish.price && currentDish.price.toFixed(2) }}</div>
          </div>
        </div>
        <div class="quantity-selector">
          <span>数量：</span>
          <el-input-number v-model="quantity" :min="1" :max="99" size="small"></el-input-number>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addCartDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddToCart">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCategories } from '@/api/dish'
import { listDishes, getDishDetail } from '@/api/dish'
import { mapGetters, mapMutations } from 'vuex'

export default {
  name: 'Menu',
  data() {
    return {
      categories: [],
      dishes: [],
      activeCategory: null,
      currentPage: 1,
      pageSize: 8,
      total: 0,
      searchQuery: '',
      addCartDialogVisible: false,
      currentDish: {},
      quantity: 1
    }
  },
  computed: {
    ...mapGetters([
      'cartCount'
    ]),
    displayDishes() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.dishes.slice(start, end)
    }
  },
  created() {
    this.fetchCategories()
    this.fetchDishes()
  },
  methods: {
    ...mapMutations([
      'addToCart'
    ]),
    fetchCategories() {
      getAllCategories().then(response => {
        if (response.code === 200) {
          // 确保返回的是数组
          const data = Array.isArray(response.data) ? response.data : []
          // 过滤掉无效的分类数据和状态为0的分类
          this.categories = data.filter(category => 
            category && category.id && category.name && category.status === 1
          )
        }
      }).catch(error => {
        console.error('获取分类失败:', error)
        this.categories = []
      })
    },
    fetchDishes(categoryId = null) {
      const params = {
        page: 1,
        pageSize: 1000, // 获取所有菜品，前端分页
        status: 1 // 只获取启用状态的菜品
      }

      if (categoryId && categoryId !== 'all') {
        params.categoryId = categoryId
      }

      if (this.searchQuery) {
        params.name = this.searchQuery
      }

      listDishes(params).then(response => {
        if (response.code === 200) {
          // 过滤掉无效的菜品数据
          this.dishes = (response.data.records || []).filter(dish => 
            dish && dish.id && dish.name && dish.price != null
          )
          this.total = this.dishes.length
          this.currentPage = 1
        }
      }).catch(error => {
        console.error('获取菜品失败:', error)
        this.dishes = []
        this.total = 0
      })
    },
    handleCategorySelect(index) {
      if (index === 'all') {
        this.activeCategory = { id: 'all', name: '全部菜品' }
        this.fetchDishes()
      } else {
        const category = this.categories.find(c => c.id && c.id.toString() === index)
        if (category) {
          this.activeCategory = category
          this.fetchDishes(category.id)
        }
      }
    },
    searchDishes() {
      this.fetchDishes(this.activeCategory && this.activeCategory.id !== 'all' ? this.activeCategory.id : null)
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handleCurrentChange(page) {
      this.currentPage = page
    },
    addToCart(dish) {
      this.currentDish = dish
      this.quantity = 1
      this.addCartDialogVisible = true
    },
    confirmAddToCart() {
      const cartItem = {
        id: this.currentDish.id,
        name: this.currentDish.name,
        price: this.currentDish.price,
        image: this.currentDish.image,
        quantity: this.quantity,
        selected: true
      }
      
      this.$store.commit('addToCart', cartItem)
      this.addCartDialogVisible = false
      
      this.$message({
        message: '已添加到购物车',
        type: 'success'
      })
    },
    goToCart() {
      this.$router.push('/customer/cart')
    },
    handleImageError() {
      // 处理图片加载失败后的逻辑
      console.error('图片加载失败')
    }
  }
}
</script>

<style scoped>
.menu-container {
  padding: 20px;
  position: relative;
}

.menu-header {
  display: flex;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
  margin-right: 10px;
}

.menu-content {
  display: flex;
}

.category-sidebar {
  width: 200px;
  margin-right: 20px;
  border-right: 1px solid #ebeef5;
}

.category-menu {
  border-right: none;
}

.dish-list {
  flex: 1;
}

.category-title {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.category-title h3 {
  margin: 0;
}

.dish-card {
  margin-bottom: 20px;
  height: 100%;
}

.dish-image {
  height: 200px;
  overflow: hidden;
  border-radius: 4px 4px 0 0;
}

.dish-image .el-image {
  width: 100%;
  height: 100%;
}

.dish-info {
  padding: 10px 0;
}

.dish-name {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dish-desc {
  color: #666;
  font-size: 12px;
  margin-bottom: 10px;
  height: 36px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-price-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.empty-result {
  margin-top: 40px;
}

.cart-fab {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  z-index: 1000;
}

.cart-fab i {
  font-size: 24px;
}

.add-cart-content {
  padding: 10px;
}

.dish-preview {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.dish-preview-info {
  margin-left: 15px;
}

.dish-preview-info h4 {
  margin: 0 0 10px;
}

.dish-preview-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.quantity-selector {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.quantity-selector span {
  margin-right: 10px;
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
  font-size: 14px;
}

.image-slot i {
  font-size: 24px;
  margin-bottom: 8px;
}
</style> 