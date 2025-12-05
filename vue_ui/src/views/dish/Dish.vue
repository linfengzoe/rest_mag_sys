<template>
  <div class="dish-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="菜品名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.categoryId"
        placeholder="分类"
        clearable
        style="width: 200px"
        class="filter-item"
      >
        <el-option
          v-for="item in categoryOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
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
      <el-table-column label="图片" width="140" align="center">
        <template slot-scope="scope">
          <el-image
            style="width: 80px; height: 80px"
            :src="scope.row.image"
            fit="cover"
            :preview-src-list="scope.row.image ? [scope.row.image] : []"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="名称">
        <template slot-scope="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column label="分类" width="110" align="center">
        <template slot-scope="scope">
          {{ getCategoryName(scope.row.categoryId) }}
        </template>
      </el-table-column>
      <el-table-column label="价格" width="110" align="center">
        <template slot-scope="scope">
          ¥{{ (scope.row.price || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
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

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="80px"
        style="width: 400px; margin-left: 50px;"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="temp.name" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="temp.categoryId" class="filter-item" placeholder="请选择">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="temp.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-input v-model="temp.image" placeholder="图片URL" style="width: 220px; margin-right: 10px;" />
          <el-upload
            class="upload-demo"
            action="/api/dish/upload"
            :show-file-list="false"
            :on-success="handleImageUploadSuccess"
            :before-upload="beforeImageUpload"
            :headers="uploadHeaders"
            :data="{}"
            accept="image/*"
          >
            <el-button size="small" type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="temp.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="temp.status"
            :active-value="1"
            :inactive-value="0"
          ></el-switch>
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
import { listDishes as getDishList, addDish, updateDish, deleteDish, getAllCategories } from '@/api/dish'
import Pagination from '@/components/Pagination'

export default {
  name: 'Dish',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        pageSize: 10,
        name: '',
        categoryId: undefined
      },
      categoryOptions: [],
      temp: {
        id: undefined,
        name: '',
        categoryId: '',
        price: 0,
        image: '',
        description: '',
        status: 1
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑菜品',
        create: '添加菜品'
      },
      rules: {
        name: [{ required: true, message: '菜品名称不能为空', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        price: [{ required: true, message: '价格不能为空', trigger: 'blur' }]
      },
      uploadHeaders: {}, // 若有token可在此加上
    }
  },
  created() {
    // 初始化Authorization头
    const token = this.$store.state.token || localStorage.getItem('token');
    if (token) {
      this.uploadHeaders = {
        Authorization: 'Bearer ' + token
      };
    }
    this.getList();
    this.getCategoryOptions();
  },
  methods: {
    getList() {
      this.listLoading = true
      // 处理搜索参数
      const params = {
        page: this.listQuery.page,
        pageSize: this.listQuery.pageSize
      }
      
      // 只有当name不为空时才添加name参数
      if (this.listQuery.name && this.listQuery.name.trim()) {
        params.name = this.listQuery.name.trim()
      }
      
      // 只有当categoryId不为空时才添加categoryId参数
      if (this.listQuery.categoryId) {
        params.categoryId = this.listQuery.categoryId
      }
      
      console.log('菜品查询参数:', params)
      
      getDishList(params).then(response => {
        console.log('菜品API响应:', response)
        // 检查响应格式
        if (response.code === 1 && response.data && response.data.records) {
          this.list = response.data.records
          this.total = parseInt(response.data.total) || 0
        } else if (response.data && response.data.records) {
          this.list = response.data.records
          this.total = parseInt(response.data.total) || 0
        } else if (Array.isArray(response.data)) {
          this.list = response.data
          this.total = response.data.length
        } else {
          console.error('未知的菜品响应格式:', response)
          this.list = []
          this.total = 0
        }
        this.listLoading = false
      }).catch(error => {
        console.error('查询菜品列表失败:', error)
        this.$message.error('查询菜品列表失败，请稍后再试')
        this.list = []
        this.total = 0
        this.listLoading = false
      })
    },
    getCategoryOptions() {
      getAllCategories().then(response => {
        console.log('获取分类选项API响应:', response)
        // getAllCategories 调用 /category/list，返回 R<List<Category>>
        if (response.code === 1 && response.data && Array.isArray(response.data)) {
          this.categoryOptions = response.data
        } else if (response.data && Array.isArray(response.data)) {
          this.categoryOptions = response.data
        } else {
          console.error('未知的分类响应格式:', response)
          this.categoryOptions = []
        }
      }).catch(error => {
        console.error('获取分类列表失败:', error)
        this.$message.error('获取分类列表失败')
        this.categoryOptions = []
      })
    },
    getCategoryName(categoryId) {
      const category = this.categoryOptions.find(item => item.id === categoryId)
      return category ? category.name : ''
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        name: '',
        categoryId: '',
        price: 0,
        image: '',
        description: '',
        status: 1
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
          // 过滤掉时间字段和其他不需要的字段
          const tempData = Object.assign({}, this.temp)
          delete tempData.createTime
          delete tempData.updateTime
          delete tempData.categoryName
          
          console.log('发送新增数据:', tempData)
          
          addDish(tempData).then(() => {
            this.dialogFormVisible = false
            this.getList()
            this.$message({
              type: 'success',
              message: '添加成功'
            })
          }).catch(error => {
            console.error('添加菜品失败:', error)
            this.$message.error('添加菜品失败，请稍后再试')
          })
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
          // 过滤掉时间字段，这些字段应该由后端自动处理
          const tempData = Object.assign({}, this.temp)
          delete tempData.createTime
          delete tempData.updateTime
          delete tempData.categoryName // 也删除分类名称，这个是查询时添加的
          
          console.log('发送更新数据:', tempData)
          
          updateDish(tempData).then(() => {
            this.dialogFormVisible = false
            this.getList()
            this.$message({
              type: 'success',
              message: '更新成功'
            })
          }).catch(error => {
            console.error('更新菜品失败:', error)
            this.$message.error('更新菜品失败，请稍后再试')
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除菜品"${row.name}"吗？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteDish(row.id).then(() => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getList()
        }).catch(error => {
          console.error('删除菜品失败:', error)
          this.$message.error('删除菜品失败，请稍后再试')
        })
      }).catch(err => {
        if (err === 'cancel' || err === 'close') return;
        console.error(err);
      })
    },
    handleStatusChange(row) {
      // 只发送必要的字段进行状态更新
      const updateData = {
        id: row.id,
        name: row.name,
        categoryId: row.categoryId,
        price: row.price,
        image: row.image,
        description: row.description,
        status: row.status
      }
      
      console.log('发送状态更新数据:', updateData)
      
      updateDish(updateData).then(() => {
        this.$message({
          type: 'success',
          message: '状态更新成功'
        })
      }).catch(error => {
        console.error('状态更新失败:', error)
        this.$message.error('状态更新失败，请稍后再试')
        // 恢复原来的状态
        row.status = row.status === 1 ? 0 : 1
      })
    },
    handleImageUploadSuccess(res) {
      if ((res.code === 200 || res.code === 1) && res.data) {
        this.temp.image = res.data;
        this.$message.success('图片上传成功');
      } else if (typeof res === 'string') {
        this.temp.image = res;
        this.$message.success('图片上传成功');
      } else {
        this.$message.error('图片上传失败');
      }
    },
    beforeImageUpload(file) {
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
  }
}
</script>

<style scoped>
.dish-container {
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

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}
</style> 