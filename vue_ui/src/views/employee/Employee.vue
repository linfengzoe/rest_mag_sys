<template>
  <div class="employee-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="员工姓名"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleFilter"
      >
        搜索
      </el-button>
      <el-button
        class="filter-item"
        style="margin-left: 10px;"
        type="primary"
        icon="el-icon-plus"
        @click="handleCreate"
      >
        添加
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
      <el-table-column label="姓名" width="120">
        <template slot-scope="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column label="用户名" width="120">
        <template slot-scope="scope">
          {{ scope.row.username }}
        </template>
      </el-table-column>
      <el-table-column label="电话" width="140">
        <template slot-scope="scope">
          {{ scope.row.phone }}
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80" align="center">
        <template slot-scope="scope">
          {{ scope.row.sex === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column label="职位" width="120">
        <template slot-scope="scope">
          {{ scope.row.position }}
        </template>
      </el-table-column>
      <el-table-column label="薪资" width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.salary ? '¥' + scope.row.salary : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="入职时间" width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.hireDate }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="handleUpdate(scope.row)">
            编辑
          </el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >
            删除
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

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible" width="50%">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="80px"
        style="width: 90%; margin-left: 50px;"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="temp.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="temp.username" :disabled="dialogStatus === 'update'" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="dialogStatus === 'create'">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="temp.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="temp.confirmPassword" type="password" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="dialogStatus === 'update'">
          <el-col :span="24">
            <el-form-item>
              <el-checkbox v-model="showPasswordFields" @change="onPasswordFieldChange">修改密码</el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="dialogStatus === 'update' && showPasswordFields">
          <el-col :span="12">
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="temp.newPassword" 
                type="password" 
                show-password 
                placeholder="请输入新密码"
                clearable
                autocomplete="new-password"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmNewPassword">
              <el-input 
                v-model="temp.confirmNewPassword" 
                type="password" 
                show-password 
                placeholder="请再次输入新密码"
                clearable
                autocomplete="new-password"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="temp.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="temp.email" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="temp.sex">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="temp.birthday"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idNumber">
              <el-input v-model="temp.idNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="temp.position" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="薪资" prop="salary">
              <el-input v-model="temp.salary" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="temp.hireDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="地址" prop="address">
              <el-input v-model="temp.address" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="dialogStatus === 'create' ? createData() : updateData()">
          确认
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEmployeeList, addEmployee, updateEmployee, deleteEmployee } from '@/api/user'
import Pagination from '@/components/Pagination'

export default {
  name: 'Employee',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        pageSize: 10,
        name: undefined
      },
      temp: {
        id: undefined,
        name: '',
        username: '',
        password: '',
        confirmPassword: '',
        newPassword: '',
        confirmNewPassword: '',
        phone: '',
        email: '',
        sex: 1,
        birthday: '',
        idNumber: '',
        position: '',
        salary: '',
        hireDate: '',
        address: ''
      },
      showPasswordFields: false,
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑员工',
        create: '添加员工'
      },
      rules: {
        name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
        confirmPassword: [
          { required: true, message: '确认密码不能为空', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ],
        newPassword: [
          { validator: this.validateNewPassword, trigger: 'change' }
        ],
        confirmNewPassword: [
          { validator: this.validateConfirmNewPassword, trigger: 'change' }
        ],
        phone: [
          { required: true, message: '电话不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        sex: [{ required: true, message: '性别不能为空', trigger: 'change' }],
        idNumber: [
          { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号码', trigger: 'blur' }
        ],
        position: [{ required: true, message: '职位不能为空', trigger: 'blur' }],
        salary: [
          { required: true, message: '薪资不能为空', trigger: 'blur' },
          { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的薪资金额', trigger: 'blur' }
        ],
        hireDate: [{ required: true, message: '入职日期不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    validateConfirmPassword(rule, value, callback) {
      if (this.dialogStatus === 'create') {
        if (value !== this.temp.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    },
    validateNewPassword(rule, value, callback) {
      if (this.dialogStatus === 'update' && this.showPasswordFields) {
        if (!value || value.trim() === '') {
          callback(new Error('新密码不能为空'))
        } else if (value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
        } else {
          // 如果确认密码已有值，重新验证确认密码
          if (this.temp.confirmNewPassword) {
            this.$refs.dataForm.validateField('confirmNewPassword')
          }
          callback()
        }
      } else {
        callback()
      }
    },
    validateConfirmNewPassword(rule, value, callback) {
      if (this.dialogStatus === 'update' && this.showPasswordFields) {
        if (!value || value.trim() === '') {
          callback(new Error('确认密码不能为空'))
        } else if (value !== this.temp.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    },
    onPasswordFieldChange(value) {
      // 当修改密码复选框状态改变时，清空密码字段和验证错误
      if (!value) {
        this.temp.newPassword = ''
        this.temp.confirmNewPassword = ''
      }
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate(['newPassword', 'confirmNewPassword'])
      })
    },
    getList() {
      this.listLoading = true
      getEmployeeList(this.listQuery).then(response => {
        this.list = response.data.records
        this.total = parseInt(response.data.total, 10) || 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        name: '',
        username: '',
        password: '',
        confirmPassword: '',
        newPassword: '',
        confirmNewPassword: '',
        phone: '',
        email: '',
        sex: 1,
        birthday: '',
        idNumber: '',
        position: '',
        salary: '',
        hireDate: new Date().toISOString().slice(0, 10),
        address: ''
      }
      this.showPasswordFields = false
    },
    handleCreate() {
      this.resetTemp()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    createData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          addEmployee(this.temp).then(response => {
            if (response.code === 200) {
              this.dialogFormVisible = false
              this.getList()
              this.$message({
                type: 'success',
                message: '添加成功'
              })
            }
          }).catch(()=>{})
        }
      })
    },
    handleUpdate(row) {
      this.temp = Object.assign({}, row)
      this.$set(this.temp, 'newPassword', '')
      this.$set(this.temp, 'confirmNewPassword', '')
      this.showPasswordFields = false
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          const tempData = Object.assign({}, this.temp)
          // 编辑时不发送原密码字段
          delete tempData.password
          delete tempData.confirmPassword
          
          // 如果修改密码，将新密码赋值给password字段
          if (this.showPasswordFields && tempData.newPassword) {
            tempData.password = tempData.newPassword
          }
          
          // 删除临时密码字段
          delete tempData.newPassword
          delete tempData.confirmNewPassword
          
          updateEmployee(tempData).then(response => {
            if (response.code === 200) {
              this.dialogFormVisible = false
              this.getList()
              this.$message({
                type: 'success',
                message: '更新成功'
              })
            }
          }).catch(()=>{})
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该员工吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteEmployee(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.getList()
          }
        })
      }).catch(() => {
        // 用户点击取消或ESC键时，不做任何操作
        console.log('删除操作已取消')
      })
    }
  }
}
</script>

<style scoped>
.employee-container {
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
</style> 