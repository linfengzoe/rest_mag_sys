<template>
  <div class="category-container">
    <!-- 顶部操作栏 -->
    <div class="filter-container">
      <el-input
        v-model="searchKeyword"
        placeholder="分类名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleSearch"
        clearable
      />
      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleSearch"
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

    <!-- 分类表格 -->
    <el-table
      v-loading="listLoading"
      :data="filteredList"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
    >
      <el-table-column label="分类名称" min-width="200">
        <template slot-scope="scope">
          <span>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
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

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[10, 20, 30, 50]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="80px"
        style="width: 400px; margin-left: 50px;"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="temp.name" placeholder="请输入分类名称" />
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
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api/dish'

export default {
  name: 'Category',
  data() {
    return {
      list: [],
      filteredList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      listLoading: true,
      searchKeyword: '',
      temp: {
        id: undefined,
        name: ''
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑分类',
        create: '添加分类'
      },
      rules: {
        name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    // 计算分页后的数据
    paginatedList() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredList.slice(start, end)
    }
  },
  watch: {
    // 监听筛选结果变化，更新分页数据
    filteredList(newList) {
      this.total = newList.length
      this.currentPage = 1
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      getAllCategories().then(response => {
        console.log('分类API响应:', response)
        
        // 处理API响应
        if (response.code === 1 && response.data && Array.isArray(response.data)) {
          // 成功响应，data是数组
          this.list = response.data
        } else if (response.data && Array.isArray(response.data)) {
          // 如果response.data是数组，直接使用
          this.list = response.data
        } else if (response.data && response.data.records && Array.isArray(response.data.records)) {
          // 如果是分页格式，使用records
          this.list = response.data.records
        } else {
          console.error('未知的响应格式:', response)
          this.list = []
        }
        
        this.filteredList = [...this.list]
        this.listLoading = false
        console.log('分类列表:', this.list)
        console.log('分类数量:', this.list.length)
      }).catch(error => {
        console.error('查询分类列表失败:', error)
        this.$message.error('查询分类列表失败，请稍后再试')
        this.list = []
        this.filteredList = []
        this.listLoading = false
      })
    },
    
    handleSearch() {
      if (!this.searchKeyword.trim()) {
        this.filteredList = [...this.list]
      } else {
        this.filteredList = this.list.filter(category =>
          category.name.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
    },
    
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    
    handleCurrentChange(val) {
      this.currentPage = val
    },
    
    resetTemp() {
      this.temp = {
        id: undefined,
        name: ''
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
          addCategory(this.temp).then(() => {
            this.dialogFormVisible = false
            this.getList()
            this.$message({
              type: 'success',
              message: '添加成功'
            })
          }).catch(() => {})
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
          updateCategory(tempData).then(() => {
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
      this.$confirm(`确认删除分类"${row.name}"吗？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCategory(row.id).then(() => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getList()
        }).catch(err => {
          // 兜底处理后端返回的删除失败，防止Promise未捕获异常冒泡到全局
          // 已有全局Message弹窗，这里静默或console.error即可
          console.error('删除分类失败:', err)
        })
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    }
  }
}
</script>

<style scoped>
.category-container {
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

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.el-table {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style> 