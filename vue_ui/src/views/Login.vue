<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-logo">
        <h2>餐厅点餐管理系统</h2>
      </div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        auto-complete="on"
        label-position="left"
      >
        <div class="title-container">
          <h3 class="title">用户登录</h3>
        </div>

        <el-form-item prop="username">
          <el-input
            ref="username"
            v-model="loginForm.username"
            placeholder="用户名"
            name="username"
            type="text"
            tabindex="1"
            auto-complete="on"
            prefix-icon="el-icon-user"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            :key="passwordType"
            ref="password"
            v-model="loginForm.password"
            :type="passwordType"
            placeholder="密码"
            name="password"
            tabindex="2"
            auto-complete="on"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin"
          />
          <span class="show-pwd" @click="showPwd">
            <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-hide'"></i>
          </span>
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          style="width: 100%; margin-bottom: 30px"
          @click.native.prevent="handleLogin"
        >
          登录
        </el-button>

        <div class="tips">
          <router-link to="/register">
            <span>没有账号？立即注册</span>
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/user'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码不能少于6个字符'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  methods: {
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          login(this.loginForm)
            .then(response => {
              console.log('登录响应:', response);
              if (response.code === 200) {
                const data = response.data;
                console.log('登录成功，用户数据:', data);
                
                // 从返回的数据中提取token和用户信息
                const token = data.token;
                const userInfo = {
                  id: data.id,
                  username: data.username,
                  name: data.name,
                  role: data.role
                };
                
                console.log('存储到Vuex的数据:', { token, userInfo });
                
                // 使用Vuex存储用户信息和token
                this.$store.dispatch('login', { token, userInfo })
                
                // 根据用户角色跳转到不同的首页
                if (userInfo.role === 'customer') {
                  console.log('跳转到顾客首页');
                  this.$router.push('/customer/menu');
                } else {
                  console.log('跳转到管理员/员工首页');
                  this.$router.push(this.redirect || '/');
                }
                
                this.$message({
                  message: '登录成功',
                  type: 'success'
                })
              } else {
                console.log('登录失败:', response.msg);
                this.$message.error(response.msg || '登录失败')
              }
            })
            .catch(error => {
              console.error('登录错误:', error)
              this.$message.error('登录失败，请重试')
            })
            .finally(() => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$bg: #f5f7fa;
$light_gray: #eee;
$cursor: #fff;

@supports (-webkit-backdrop-filter: none) or (backdrop-filter: none) {
  .login-container {
    backdrop-filter: blur(10px);
  }
}

.login-container {
  height: 100vh;
  width: 100%;
  background-color: $bg;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .login-box {
    width: 400px;
    padding: 30px;
    background-color: #fff;
    border-radius: 5px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .login-logo {
    text-align: center;
    margin-bottom: 30px;
    
    h2 {
      margin: 0;
      color: #409EFF;
    }
  }

  .title-container {
    position: relative;
    margin-bottom: 30px;

    .title {
      font-size: 20px;
      color: #303133;
      margin: 0px auto 20px auto;
      text-align: center;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: #889aa4;
    cursor: pointer;
    user-select: none;
  }

  .tips {
    font-size: 14px;
    color: #606266;
    margin-bottom: 10px;
    text-align: center;

    a {
      color: #409EFF;
      text-decoration: none;
    }
  }
}
</style> 