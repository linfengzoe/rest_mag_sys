<template>
  <el-container class="app-wrapper">
    <el-header class="app-header">
      <div class="logo">餐厅点餐系统</div>
      <el-menu
        :default-active="activeIndex"
        class="nav-menu"
        mode="horizontal"
        router
        background-color="#409EFF"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/customer/menu">
          <i class="el-icon-dish"></i>
          <span>点菜</span>
        </el-menu-item>
        <el-menu-item index="/customer/order">
          <i class="el-icon-s-order"></i>
          <span>我的订单</span>
        </el-menu-item>
        <el-menu-item index="/customer/review">
          <i class="el-icon-chat-line-round"></i>
          <span>我的评价</span>
        </el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
          <el-button type="text" icon="el-icon-shopping-cart-2" @click="goToCart">购物车</el-button>
        </el-badge>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="el-dropdown-link">
            {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
    <el-footer class="app-footer">
      <p>© 2025 设计者：林风 版权所有</p>
    </el-footer>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'CustomerLayout',
  computed: {
    ...mapGetters([
      'username',
      'cartCount'
    ]),
    activeIndex() {
      return this.$route.path
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('logout')
        this.$router.push('/login')
      } else if (command === 'profile') {
        this.$router.push('/customer/profile')
      }
    },
    goToCart() {
      this.$router.push('/customer/cart')
    }
  }
}
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
}

.app-header {
  background-color: #409EFF;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 20px;
  width: 150px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #fff;
  margin-left: 20px;
}

.app-main {
  padding: 0;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.app-footer {
  background-color: #f5f7fa;
  text-align: center;
  color: #909399;
  line-height: 60px;
  height: 60px;
}

.cart-badge {
  margin-right: 15px;
}

.cart-badge >>> .el-button--text {
  color: #fff;
  font-size: 14px;
}
</style> 