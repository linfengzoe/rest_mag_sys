<template>
  <div class="customer-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="顾客姓名"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-input
        v-model="listQuery.phone"
        placeholder="联系电话"
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
      <el-table-column label="用户名" width="150">
        <template slot-scope="scope">
          {{ scope.row.username }}
        </template>
      </el-table-column>
      <el-table-column label="电话" width="140">
        <template slot-scope="scope">
          {{ scope.row.phone }}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" width="180">
        <template slot-scope="scope">
          {{ scope.row.email }}
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80" align="center">
        <template slot-scope="scope">
          {{ scope.row.sex === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column label="生日" width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.birthday }}
        </template>
      </el-table-column>
      <el-table-column label="地址" width="200">
        <template slot-scope="scope">
          {{ scope.row.address }}
        </template>
      </el-table-column>
      <el-table-column label="积分" width="80" align="center">
        <template slot-scope="scope">
          {{ scope.row.points || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="会员等级" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getMemberLevelType(scope.row.memberLevel)">
            {{ getMemberLevelText(scope.row.memberLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180" align="center">
        <template slot-scope="scope">
          {{ scope.row.createTime }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width">
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
          <el-button
            size="mini"
            type="info"
            @click="handleViewOrders(scope.row)"
          >
            查看订单
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

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="80px"
        style="width: 400px; margin-left: 50px;"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="temp.name" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="temp.username" />
        </el-form-item>
        <el-form-item v-if="dialogStatus === 'create'" label="密码" prop="password">
          <el-input v-model="temp.password" type="password" show-password />
        </el-form-item>
        
        <el-row v-if="dialogStatus === 'update'">
          <el-col :span="24">
            <el-form-item>
              <el-checkbox v-model="showPasswordFields" @change="onPasswordFieldChange">修改密码</el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="dialogStatus === 'update' && showPasswordFields">
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
        <el-form-item label="电话" prop="phone">
          <el-input v-model="temp.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="temp.email" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="temp.sex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="temp.birthday"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="temp.address" />
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input v-model="temp.points" type="number" />
        </el-form-item>
        <el-form-item label="会员等级" prop="memberLevel">
          <el-select v-model="temp.memberLevel" placeholder="请选择会员等级" style="width: 100%;">
            <el-option label="普通用户" :value="0"></el-option>
            <el-option label="初级会员" :value="1"></el-option>
            <el-option label="中级会员" :value="2"></el-option>
            <el-option label="高级会员" :value="3"></el-option>
          </el-select>
        </el-form-item>
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

    <el-dialog title="顾客订单" :visible.sync="orderDialogVisible" width="80%">
      <el-table
        v-loading="orderLoading"
        :data="orderList"
        element-loading-text="Loading"
        border
        fit
        highlight-current-row
      >
        <el-table-column label="餐桌号" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.tableNumber }}
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
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="备注">
          <template slot-scope="scope">
            {{ scope.row.remark }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleViewOrderDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="dialog-footer" style="margin-top: 20px; text-align: right;">
        <el-button @click="orderDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCustomerList, addCustomer, updateCustomer, deleteCustomer } from '@/api/user'
import { getOrdersByCustomerId } from '@/api/order'
import Pagination from '@/components/Pagination'

export default {
  name: 'Customer',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        pageSize: 10,
        name: undefined,
        phone: undefined
      },
      temp: {
        id: undefined,
        name: '',
        username: '',
        password: '',
        newPassword: '',
        confirmNewPassword: '',
        phone: '',
        email: '',
        sex: 1,
        birthday: '',
        address: '',
        points: 0,
        memberLevel: 0
      },
      showPasswordFields: false,
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑顾客',
        create: '添加顾客'
      },
      rules: {
        name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
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
        points: [
          { pattern: /^\d+$/, message: '积分必须为非负整数', trigger: 'blur' }
        ],
        memberLevel: [{ required: true, message: '会员等级不能为空', trigger: 'change' }]
      },
      orderDialogVisible: false,
      orderLoading: false,
      orderList: [],
      currentCustomer: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    validateNewPassword(rule, value, callback) {
      if (this.dialogStatus === 'update' && this.showPasswordFields) {
        if (!value || value.trim() === '') {
          callback(new Error('新密码不能为空'))
        } else if (value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
        } else {
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
      getCustomerList(this.listQuery).then(response => {
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
        newPassword: '',
        confirmNewPassword: '',
        phone: '',
        email: '',
        sex: 1,
        birthday: '',
        address: '',
        points: 0,
        memberLevel: 0
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
          addCustomer(this.temp).then(response => {
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
      this.$set(this.temp,'newPassword','')
      this.$set(this.temp,'confirmNewPassword','')
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
          
          // 如果修改密码，将新密码赋值给password字段（留给后端处理用户表更新）
          if (this.showPasswordFields && tempData.newPassword) {
            tempData.password = tempData.newPassword
          }
          
          // 删除临时密码字段
          delete tempData.newPassword
          delete tempData.confirmNewPassword

          /*
           * 仅保留后端 /customer PUT 接口支持的字段，
           * 避免发送 LocalDateTime 等后端无法解析的字段导致 500 错误
           */
          const customerPayload = {
            id: tempData.id,
            userId: tempData.userId,
            username: tempData.username,
            email: tempData.email,
            birthday: tempData.birthday,
            name: tempData.name,
            phone: tempData.phone,
            sex: tempData.sex,
            address: tempData.address,
            points: tempData.points,
            memberLevel: tempData.memberLevel
          }

          updateCustomer(customerPayload).then(response => {
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
      this.$confirm('确认删除该顾客吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCustomer(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.getList()
          }
        }).catch(()=>{})
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    },
    getMemberLevelType(level) {
      switch (level) {
        case 0:
          return 'info'
        case 1:
          return 'primary'
        case 2:
          return 'warning'
        case 3:
          return 'success'
        default:
          return 'info'
      }
    },
    getMemberLevelText(level) {
      switch (level) {
        case 0:
          return '普通用户'
        case 1:
          return '初级会员'
        case 2:
          return '中级会员'
        case 3:
          return '高级会员'
        default:
          return '普通用户'
      }
    },

    handleViewOrders(row) {
      this.currentCustomer = row
      this.orderDialogVisible = true
      this.orderLoading = true
      console.log('查看顾客订单，顾客ID:', row.id, '用户ID:', row.userId)
      
      // 使用userId而不是customer id来查询订单
      const customerId = row.userId || row.id
      getOrdersByCustomerId(customerId).then(response => {
        console.log('订单API响应:', response)
        if (response.code === 200) {
          this.orderList = response.data.records || []
          console.log('订单列表:', this.orderList)
        } else {
          console.error('获取订单失败:', response.msg)
          this.$message.error(response.msg || '获取订单列表失败')
          this.orderList = []
        }
        this.orderLoading = false
      }).catch(error => {
        console.error('获取订单异常:', error)
        this.$message.error('获取订单列表失败')
        this.orderList = []
        this.orderLoading = false
      })
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
    handleViewOrderDetail(row) {
      this.$router.push(`/order/${row.id}`)
      this.orderDialogVisible = false
    }
  }
}
</script>

<style scoped>
.customer-container {
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