<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-logo">
        <h2>餐厅点餐管理系统</h2>
      </div>
      <el-form
        ref="registerForm"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        auto-complete="on"
        label-position="left"
      >
        <div class="title-container">
          <h3 class="title">用户注册</h3>
        </div>

        <el-form-item prop="username">
          <el-input
            ref="username"
            v-model="registerForm.username"
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
            v-model="registerForm.password"
            :type="passwordType"
            placeholder="密码"
            name="password"
            tabindex="2"
            auto-complete="on"
            prefix-icon="el-icon-lock"
          />
          <span class="show-pwd" @click="showPwd">
            <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-hide'"></i>
          </span>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            :key="confirmPasswordType"
            ref="confirmPassword"
            v-model="registerForm.confirmPassword"
            :type="confirmPasswordType"
            placeholder="确认密码"
            name="confirmPassword"
            tabindex="3"
            auto-complete="on"
            prefix-icon="el-icon-lock"
          />
          <span class="show-pwd" @click="showConfirmPwd">
            <i :class="confirmPasswordType === 'password' ? 'el-icon-view' : 'el-icon-hide'"></i>
          </span>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            ref="phone"
            v-model="registerForm.phone"
            placeholder="手机号码"
            name="phone"
            type="text"
            tabindex="4"
            auto-complete="on"
            prefix-icon="el-icon-mobile-phone"
          />
        </el-form-item>

        <el-form-item prop="name">
          <el-input
            ref="name"
            v-model="registerForm.name"
            placeholder="姓名"
            name="name"
            type="text"
            tabindex="5"
            auto-complete="on"
            prefix-icon="el-icon-s-custom"
          />
        </el-form-item>

        <el-form-item prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          style="width: 100%; margin-bottom: 30px"
          @click.native.prevent="handleRegister"
        >
          注册
        </el-button>

        <div class="tips">
          <router-link to="/login">
            <span>已有账号？立即登录</span>
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/user'

export default {
  name: 'Register',
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
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入手机号码'))
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号码'))
      } else {
        callback()
      }
    }
    return {
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        phone: '',
        name: '',
        gender: 'MALE'
      },
      registerRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        confirmPassword: [{ required: true, trigger: 'blur', validator: validateConfirmPassword }],
        phone: [{ required: true, trigger: 'blur', validator: validatePhone }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
      },
      loading: false,
      passwordType: 'password',
      confirmPasswordType: 'password'
    }
  },
  methods: {
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    showConfirmPwd() {
      this.confirmPasswordType = this.confirmPasswordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.confirmPassword.focus()
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          
          const registerData = {
            username: this.registerForm.username,
            password: this.registerForm.password,
            phone: this.registerForm.phone,
            name: this.registerForm.name,
            gender: this.registerForm.gender,
            role: 'CUSTOMER' // 默认注册为顾客角色
          }
          
          register(registerData)
            .then(response => {
              if (response.code === 200) {
                this.$message({
                  message: '注册成功，请登录',
                  type: 'success'
                })
                this.$router.push('/login')
              } else {
                this.$message.error(response.msg || '注册失败')
              }
            })
            .catch(error => {
              console.error('注册错误:', error)
              this.$message.error('注册失败，请重试')
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
  .register-container {
    backdrop-filter: blur(10px);
  }
}

.register-container {
  height: 100vh;
  width: 100%;
  background-color: $bg;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .register-box {
    width: 400px;
    padding: 30px;
    background-color: #fff;
    border-radius: 5px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .register-logo {
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