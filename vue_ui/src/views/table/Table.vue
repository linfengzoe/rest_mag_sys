<template>
  <div class="table-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="餐桌名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.status"
        placeholder="状态"
        clearable
        style="width: 120px"
        class="filter-item"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
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
      <el-table-column label="餐桌名称" width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column label="座位数" width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.capacity }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="getTableStatusType(scope.row.status)">
            {{ getTableStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="位置">
        <template slot-scope="scope">
          {{ scope.row.location }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="400" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="handleUpdate(scope.row)">
            编辑
          </el-button>
          
          <!-- 空闲状态：可以设为预订或占用 -->
          <el-button
            v-if="scope.row.status === 0"
            size="mini"
            type="info"
            @click="handleChangeStatus(scope.row, 1)"
          >
            设为预订
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            size="mini"
            type="warning"
            @click="handleChangeStatus(scope.row, 2)"
          >
            设为占用
          </el-button>
          
          <!-- 已预订状态：可以设为空闲或占用 -->
          <el-button
            v-if="scope.row.status === 1"
            size="mini"
            type="success"
            @click="handleChangeStatus(scope.row, 0)"
          >
            设为空闲
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            size="mini"
            type="warning"
            @click="handleChangeStatus(scope.row, 2)"
          >
            客人到达
          </el-button>
          
          <!-- 就餐中状态：只能设为空闲 -->
          <el-button
            v-if="scope.row.status === 2"
            size="mini"
            type="success"
            @click="handleChangeStatus(scope.row, 0)"
          >
            设为空闲
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

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="80px"
        style="width: 400px; margin-left: 50px;"
      >
        <el-form-item label="餐桌名称" prop="name">
          <el-input v-model="temp.name" />
        </el-form-item>
        <el-form-item label="座位数" prop="capacity">
          <el-input-number v-model="temp.capacity" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="temp.status" class="filter-item" placeholder="请选择">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="temp.location" type="textarea" :rows="2" />
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
  </div>
</template>

<script>
import { getTableList, addTable, updateTable, deleteTable, updateTableStatus } from '@/api/order'
import Pagination from '@/components/Pagination'

export default {
  name: 'Table',
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
        status: undefined
      },
      statusOptions: [
        { label: '空闲', value: 0 },
        { label: '已预订', value: 1 },
        { label: '就餐中', value: 2 }
      ],
      temp: {
        id: undefined,
        name: '',
        capacity: 4,
        status: 0,
        location: ''
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑餐桌',
        create: '添加餐桌'
      },
      rules: {
        name: [{ required: true, message: '餐桌名称不能为空', trigger: 'blur' }],
        capacity: [{ required: true, message: '座位数不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      getTableList(this.listQuery).then(response => {
        this.list = response.data.records
        this.total = parseInt(response.data.total) || 0
        this.listLoading = false
      })
    },
    getTableStatusType(status) {
      switch (status) {
        case 0:
          return 'success'
        case 1:
          return 'warning'
        case 2:
          return 'danger'
        default:
          return 'info'
      }
    },
    getTableStatusText(status) {
      switch (status) {
        case 0:
          return '空闲'
        case 1:
          return '已预订'
        case 2:
          return '就餐中'
        default:
          return '未知'
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        name: '',
        capacity: 4,
        status: 0,
        location: ''
      }
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
          addTable(this.temp).then(() => {
            this.dialogFormVisible = false
            this.getList()
            this.$message({
              type: 'success',
              message: '添加成功'
            })
          }).catch(() => {/* 已在 axios 拦截器中提示，此处吞掉避免全局报错遮罩 */})
        }
      })
    },
    handleUpdate(row) {
      this.temp = Object.assign({}, row)
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
          updateTable(tempData).then(() => {
            this.dialogFormVisible = false
            this.getList()
            this.$message({
              type: 'success',
              message: '更新成功'
            })
          }).catch(() => {})
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该餐桌吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTable(row.id).then(() => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getList()
        })
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    },
    handleChangeStatus(row, status) {
      const statusMap = {
        0: '空闲',
        1: '已预订', 
        2: '就餐中'
      }
      
      const actionMap = {
        0: { from: [1, 2], message: '空闲' },
        1: { from: [0], message: '预订' },
        2: { from: [0, 1], message: status === 2 && row.status === 1 ? '客人到达，开始就餐' : '占用' }
      }
      
      const currentStatusText = statusMap[row.status]
      const targetStatusText = statusMap[status]
      const actionText = actionMap[status].message
      
      // 检查状态转换是否合法
      if (!actionMap[status].from.includes(row.status)) {
        this.$message({
          type: 'warning',
          message: `无法从"${currentStatusText}"状态直接转换为"${targetStatusText}"状态`
        })
        return
      }
      
      let confirmMessage = `确认将餐桌 ${row.name} 从"${currentStatusText}"状态设置为"${targetStatusText}"状态吗?`
      if (status === 2 && row.status === 1) {
        confirmMessage = `确认客人已到达餐桌 ${row.name}，开始就餐吗?`
      }
      
      this.$confirm(confirmMessage, '状态转换确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateTableStatus(row.id, status).then(() => {
          this.$notify({
            title: '成功',
            message: `餐桌状态已更新为"${targetStatusText}"`,
            type: 'success',
            duration: 2000
          })
          // 更新本地状态
          row.status = status
          
          // 如果需要，可以重新加载列表以确保数据一致性
          // this.getList()
        }).catch(error => {
          this.$message({
            type: 'error',
            message: '状态更新失败，请稍后重试'
          })
          console.error('更新餐桌状态失败:', error)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        })
      })
    }
  }
}
</script>

<style scoped>
.table-container {
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