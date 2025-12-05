<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <div class="logo-container">
        <h1 class="logo">餐厅管理系统</h1>
      </div>
      <!-- 菜单 -->
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/home">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        
        <!-- 管理员和员工共用菜单 -->
        <el-menu-item index="/dish">
          <i class="el-icon-dish"></i>
          <span slot="title">菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <i class="el-icon-menu"></i>
          <span slot="title">分类管理</span>
        </el-menu-item>
        <el-menu-item index="/table">
          <i class="el-icon-s-grid"></i>
          <span slot="title">餐桌管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <i class="el-icon-s-order"></i>
          <span slot="title">订单管理</span>
        </el-menu-item>
        <el-menu-item index="/review">
          <i class="el-icon-chat-dot-square"></i>
          <span slot="title">评价管理</span>
        </el-menu-item>
        
        <!-- 管理员专用菜单 -->
        <template v-if="userRole === 'admin'">
          <el-menu-item index="/employee">
            <i class="el-icon-s-custom"></i>
            <span slot="title">员工管理</span>
          </el-menu-item>
          <el-menu-item index="/customer">
            <i class="el-icon-s-custom"></i>
            <span slot="title">顾客管理</span>
          </el-menu-item>
        </template>
        
        <!-- 统计分析菜单 -->
        <el-menu-item index="/statistics">
          <i class="el-icon-s-data"></i>
          <span slot="title">数据统计</span>
        </el-menu-item>
      </el-menu>
    </div>
    <!-- 主要内容区域 -->
    <div class="main-container">
      <!-- 头部 -->
      <div class="navbar">
        <div class="right-menu">
          <el-dropdown trigger="click">
            <span class="el-dropdown-link">
              {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="goToProfile">个人信息</el-dropdown-item>
              <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <!-- 内容区域 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapGetters([
      'userRole'
    ]),
    username() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return userInfo.username || ''
    },
    activeMenu() {
      const route = this.$route
      return route.path
    }
  },
  methods: {
    logout() {
      this.$store.dispatch('logout')
      this.$router.push('/login')
    },
    goToProfile() {
      // 检查当前路由是否已经是个人信息页面，避免重复导航
      if (this.$route.path !== '/profile') {
        this.$router.push('/profile')
      }
    }
  }
}
</script>

<style scoped>
.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  display: flex;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  overflow: hidden;
  background-color: #304156;
}

.logo-container {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b2f3a;
}

.logo {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
}

.main-container {
  min-height: 100%;
  margin-left: 210px;
  position: relative;
  flex: 1;
}

.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.right-menu {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.app-main {
  padding: 20px;
  min-height: calc(100vh - 50px);
  background-color: #f0f2f5;
}
</style> 