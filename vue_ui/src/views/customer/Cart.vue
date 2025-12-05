<template>
  <div class="cart-container">
    <div class="page-header">
      <h3>我的购物车</h3>
    </div>

    <div v-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车空空如也">
        <el-button type="primary" @click="goToMenu">去点餐</el-button>
      </el-empty>
    </div>

    <div v-else class="cart-content">
      <el-card class="box-card">
        <div slot="header" class="card-header">
          <div class="select-all">
            <el-checkbox v-model="selectAll" @change="handleSelectAllChange">全选</el-checkbox>
          </div>
          <el-button type="text" @click="handleClearCart">清空购物车</el-button>
        </div>
        <div class="cart-items">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <el-checkbox v-model="item.selected" @change="handleItemSelectChange"></el-checkbox>
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
              <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
            <div class="item-quantity">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="99"
                size="small"
                @change="handleQuantityChange(item)"
              ></el-input-number>
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            <div class="item-action">
              <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="handleRemoveItem(item)"></el-button>
            </div>
          </div>
        </div>
      </el-card>

      <div class="cart-footer">
        <div class="cart-summary">
          <div class="summary-item">
            <span>已选择 {{ selectedCount }} 件商品</span>
          </div>
          <div class="summary-item">
            <span>合计：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        <div class="cart-actions">
          <el-button type="primary" size="medium" @click="handleCheckout" :disabled="selectedCount === 0">
            去结算
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog title="确认订单" :visible.sync="checkoutDialogVisible" width="50%">
      <div class="checkout-content">
        <el-form :model="orderForm" :rules="orderRules" ref="orderForm" label-width="100px">
          <el-form-item label="餐桌选择" prop="tableId">
            <el-select v-model="orderForm.tableId" placeholder="请选择餐桌" style="width: 100%">
              <el-option
                v-for="table in availableTables"
                :key="table.id"
                :label="`${table.name} (${table.capacity}人)`"
                :value="table.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              type="textarea"
              v-model="orderForm.remark"
              :rows="3"
              placeholder="请输入备注信息（选填）"
            ></el-input>
          </el-form-item>
        </el-form>

        <div class="order-items">
          <h4>订单明细</h4>
          <el-table :data="selectedItems" border style="width: 100%">
            <el-table-column prop="name" label="菜品名称"></el-table-column>
            <el-table-column prop="price" label="单价" width="100" align="right">
              <template slot-scope="scope">
                ¥{{ scope.row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" align="center"></el-table-column>
            <el-table-column label="小计" width="120" align="right">
              <template slot-scope="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="order-total">
            <span>总计：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAvailableTables } from '@/api/order'
import { createOrder } from '@/api/order'
import { mapGetters, mapMutations } from 'vuex'

export default {
  name: 'Cart',
  data() {
    return {
      selectAll: false,
      checkoutDialogVisible: false,
      availableTables: [],
      orderForm: {
        tableId: '',
        remark: ''
      },
      orderRules: {
        tableId: [
          { required: true, message: '请选择餐桌', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters([
      'cartItems'
    ]),
    selectedItems() {
      return this.cartItems.filter(item => item.selected)
    },
    selectedCount() {
      return this.selectedItems.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice() {
      return this.selectedItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
    }
  },
  created() {
    this.initSelection()
  },
  methods: {
    ...mapMutations([
      'updateCartItem',
      'removeCartItem',
      'clearCart'
    ]),
    initSelection() {
      // 默认全选
      this.selectAll = this.cartItems.length > 0
      this.cartItems.forEach(item => {
        item.selected = true
      })
    },
    handleSelectAllChange(val) {
      this.cartItems.forEach(item => {
        item.selected = val
      })
    },
    handleItemSelectChange() {
      this.selectAll = this.cartItems.length > 0 && this.cartItems.every(item => item.selected)
    },
    handleQuantityChange(item) {
      this.updateCartItem({
        id: item.id,
        quantity: item.quantity
      })
    },
    handleRemoveItem(item) {
      this.$confirm('确定从购物车中移除该商品?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.removeCartItem(item.id)
        this.$message({
          type: 'success',
          message: '商品已移除!'
        })
      }).catch(() => {})
    },
    handleClearCart() {
      if (this.cartItems.length === 0) return
      
      this.$confirm('确定清空购物车?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.clearCart()
        this.$message({
          type: 'success',
          message: '购物车已清空!'
        })
      }).catch(() => {})
    },
    goToMenu() {
      this.$router.push('/customer/menu')
    },
    handleCheckout() {
      if (this.selectedCount === 0) {
        this.$message.warning('请至少选择一件商品')
        return
      }

      // 获取可用餐桌
      getAvailableTables().then(response => {
        if (response.code === 200) {
          this.availableTables = response.data || []
          if (this.availableTables.length === 0) {
            this.$message.warning('暂无可用餐桌，请稍后再试')
            return
          }
          this.checkoutDialogVisible = true
        }
      }).catch(error => {
        console.error('获取可用餐桌失败:', error)
        this.$message.error('获取可用餐桌失败，请稍后再试')
      })
    },
    submitOrder() {
      this.$refs.orderForm.validate(valid => {
        if (valid) {
          const orderItems = this.selectedItems.map(item => ({
            dishId: item.id,
            quantity: item.quantity
          }))

          const orderData = {
            tableId: this.orderForm.tableId,
            remark: this.orderForm.remark,
            items: orderItems
          }

          createOrder(orderData).then(response => {
            if (response.code === 200) {
              this.$message.success('订单提交成功!')
              
              // 移除已下单的商品
              this.selectedItems.forEach(item => {
                this.removeCartItem(item.id)
              })
              
              this.checkoutDialogVisible = false
              
              // 跳转到订单页面
              this.$router.push('/customer/order')
            }
          }).catch(error => {
            console.error('提交订单失败:', error)
            
            // 如果是餐桌被占用错误，重新获取可用餐桌
            if (error.message && error.message.includes('餐桌已被占用')) {
              this.$message.warning('选择的餐桌已被占用，请重新选择餐桌')
              
              // 重新获取可用餐桌列表
              getAvailableTables().then(response => {
                if (response.code === 200) {
                  this.availableTables = response.data || []
                  if (this.availableTables.length === 0) {
                    this.$message.warning('暂无可用餐桌，请稍后再试')
                    this.checkoutDialogVisible = false
                  } else {
                    // 重置餐桌选择
                    this.orderForm.tableId = ''
                  }
                }
              }).catch(e => {
                console.error('重新获取餐桌失败:', e)
                this.$message.error('获取可用餐桌失败，请稍后再试')
                this.checkoutDialogVisible = false
              })
            } else {
              this.$message.error('提交订单失败，请稍后再试')
            }
          })
        }
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
.cart-container {
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

.empty-cart {
  padding: 40px 0;
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-items {
  padding: 10px 0;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  margin: 0 15px;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
  border-radius: 4px;
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
  font-size: 12px;
  border-radius: 4px;
}

.image-slot i {
  font-size: 16px;
  margin-bottom: 4px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  margin-bottom: 5px;
}

.item-price {
  color: #f56c6c;
}

.item-quantity {
  width: 120px;
  margin: 0 20px;
}

.item-subtotal {
  width: 100px;
  color: #f56c6c;
  font-weight: bold;
  text-align: right;
}

.item-action {
  width: 60px;
  text-align: center;
}

.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background-color: #f5f7fa;
  padding: 15px 20px;
  border-radius: 4px;
}

.cart-summary {
  margin-right: 20px;
}

.summary-item {
  margin-bottom: 5px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-left: 5px;
}

.checkout-content {
  padding: 10px;
}

.order-items {
  margin-top: 20px;
}

.order-items h4 {
  margin-bottom: 15px;
}

.order-total {
  margin-top: 15px;
  text-align: right;
  font-size: 16px;
}
</style> 