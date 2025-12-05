<template>
  <div class="profile-container">
    <el-card class="box-card">
      <div slot="header" class="card-header">
        <span>个人信息</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="handleEdit"
        >
          编辑
        </el-button>
      </div>
      <div class="profile-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="avatar-container">
              <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" @error="handleAvatarError"></el-avatar>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="info-list">
              <div class="info-item">
                <span class="label">用户名：</span>
                <span>{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">姓名：</span>
                <span>{{ userInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">电话：</span>
                <span>{{ userInfo.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span>{{ userInfo.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">性别：</span>
                <span>{{ userInfo.sex === 1 ? '男' : '女' }}</span>
              </div>
              <div class="info-item">
                <span class="label">身份证号：</span>
                <span>{{ userInfo.idNumber || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">地址：</span>
                <span>{{ userInfo.address || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">生日：</span>
                <span>{{ userInfo.birthday || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">职位：</span>
                <span>{{ userInfo.position || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">薪资：</span>
                <span>{{ userInfo.salary ? '￥' + userInfo.salary : '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">入职日期：</span>
                <span>{{ userInfo.hireDate || '未设置' }}</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="box-card">
      <div slot="header" class="card-header">
        <span>账号安全</span>
      </div>
      <div class="security-content">
        <div class="security-item">
          <div class="security-info">
            <i class="el-icon-lock"></i>
            <span>修改密码</span>
          </div>
          <el-button type="text" @click="handleChangePassword">修改</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog title="编辑个人信息" :visible.sync="editDialogVisible">
      <el-form
        ref="editForm"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/user/uploadAvatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="editForm.sex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="editForm.idNumber"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address"></el-input>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <!-- 管理员才能编辑的字段 -->
        <template v-if="isAdmin">
          <el-form-item label="职位" prop="position">
            <el-input v-model="editForm.position"></el-input>
          </el-form-item>
          <el-form-item label="薪资" prop="salary">
            <el-input-number v-model="editForm.salary" :min="0" :precision="2"></el-input-number>
          </el-form-item>
          <el-form-item label="入职日期" prop="hireDate">
            <el-date-picker
              v-model="editForm.hireDate"
              type="datetime"
              placeholder="选择日期时间"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 100%;"
            ></el-date-picker>
          </el-form-item>
        </template>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确认</el-button>
      </div>
    </el-dialog>

    <el-dialog title="修改密码" :visible.sync="passwordDialogVisible">
      <el-form
        ref="passwordForm"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password"></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChangePassword">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEmployeeProfile, updateEmployeeProfile, updatePassword } from '@/api/user'
import { uploadImage } from '@/api/dish'

export default {
  name: 'EmployeeProfile',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      userInfo: {},
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      editDialogVisible: false,
      passwordDialogVisible: false,
      isAdmin: false,
      editForm: {
        id: '',
        name: '',
        phone: '',
        email: '',
        sex: 1,
        idNumber: '',
        address: '',
        birthday: '',
        avatar: '',
        position: '',
        salary: 0,
        hireDate: ''
      },
      editRules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
        ],
        sex: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchUserInfo()
    this.checkAdminRole()
  },
  methods: {
    fetchUserInfo() {
      getEmployeeProfile().then(response => {
        if (response.code === 200) {
          const localInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          const merged = { ...localInfo, ...response.data }
          this.userInfo = merged
          this.$store.commit('setUserInfo', merged)
        }
      })
    },
    checkAdminRole() {
      // 从localStorage或vuex中获取用户角色
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      this.isAdmin = userInfo.role === 'admin'
    },
    handleEdit() {
      this.editForm = {
        id: this.userInfo.id,
        name: this.userInfo.name,
        phone: this.userInfo.phone,
        email: this.userInfo.email,
        sex: this.userInfo.sex,
        idNumber: this.userInfo.idNumber || '',
        address: this.userInfo.address || '',
        birthday: this.userInfo.birthday,
        avatar: this.userInfo.avatar,
        position: this.userInfo.position || '',
        salary: this.userInfo.salary || 0,
        hireDate: this.formatDateTime(this.userInfo.hireDate)
      }
      this.editDialogVisible = true
    },
    handleChangePassword() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.passwordDialogVisible = true
    },
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          // 创建要提交的数据，过滤掉空字段
          const submitData = {
            id: this.editForm.id,
            name: this.editForm.name,
            phone: this.editForm.phone,
            email: this.editForm.email,
            sex: this.editForm.sex,
            birthday: this.editForm.birthday,
            avatar: this.editForm.avatar
          }

          // 添加员工扩展字段（如果有值）
          if (this.editForm.idNumber) {
            submitData.idNumber = this.editForm.idNumber
          }
          if (this.editForm.address) {
            submitData.address = this.editForm.address
          }

          // 管理员才能提交的字段
          if (this.isAdmin) {
            if (this.editForm.position) {
              submitData.position = this.editForm.position
            }
            if (this.editForm.salary !== null && this.editForm.salary !== undefined) {
              submitData.salary = this.editForm.salary
            }
            if (this.editForm.hireDate) {
              submitData.hireDate = this.editForm.hireDate
            }
          }

          console.log('提交员工信息:', submitData)
          updateEmployeeProfile(submitData).then(response => {
            if (response.code === 200) {
              this.$message.success('个人信息更新成功')
              this.editDialogVisible = false
              this.fetchUserInfo()
            } else {
              this.$message.error(response.msg || '更新失败')
            }
          }).catch(error => {
            console.error('更新员工信息失败:', error)
            this.$message.error('更新失败，请检查网络连接')
          })
        }
      })
    },
    submitChangePassword() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          updatePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          }).then(response => {
            if (response.code === 200) {
              this.$message.success('密码修改成功，请重新登录')
              this.passwordDialogVisible = false
              setTimeout(() => {
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                this.$router.push('/login')
              }, 1500)
            }
          })
        }
      })
    },
    handleAvatarSuccess(res) {
      if ((res.code === 200 || res.code === 1) && res.data) {
        this.editForm.avatar = res.data;
        this.$message.success('头像上传成功');
      } else if (typeof res === 'string') {
        this.editForm.avatar = res;
        this.$message.success('头像上传成功');
      } else {
        this.$message.error('头像上传失败');
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImage) {
        this.$message.error('只能上传图片文件!');
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过2MB!');
      }
      return isImage && isLt2M;
    },
    formatDateTime(dateTime) {
      if (dateTime) {
        const date = new Date(dateTime)
        if (isNaN(date.getTime())) {
          return ''
        }
        // 格式化为 yyyy-MM-dd HH:mm:ss
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      return ''
    },
    handleAvatarError() {
      this.$message.error('头像加载失败');
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.info-list {
  padding: 10px 0;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
}

.label {
  width: 100px;
  font-weight: bold;
}

.security-content {
  padding: 10px 0;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.security-item:last-child {
  border-bottom: none;
}

.security-info {
  display: flex;
  align-items: center;
}

.security-info i {
  margin-right: 10px;
  font-size: 18px;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style> 