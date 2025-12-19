# Git与GitHub使用指南

## 1. Git环境配置

### 1.1 安装Git
- 下载地址：https://git-scm.com/downloads
- 安装完成后，确认Git安装路径（示例：`E:\software\Git`）

### 1.2 配置Git用户信息

**命令解释**：
- `git config --global`：设置全局Git配置，所有本地仓库都会使用这些信息
- `user.name`：设置Git提交时显示的用户名
- `user.email`：设置Git提交时显示的邮箱（需与GitHub账号关联的邮箱一致）

```bash
E:\software\Git\cmd\git.exe config --global user.name "你的用户名"
E:\software\Git\cmd\git.exe config --global user.email "你的邮箱"
```

**修改用户名和邮箱**：
如需更改已配置的信息，只需再次运行相同命令，替换为新的用户名和邮箱即可：

```bash
# 更改用户名
E:\software\Git\cmd\git.exe config --global user.name "新用户名"

# 更改邮箱
E:\software\Git\cmd\git.exe config --global user.email "新邮箱"
```

### 1.3 查看Git配置
```bash
E:\software\Git\cmd\git.exe config --list
```

## 2. GitHub仓库创建

1. 登录GitHub账号（https://github.com）
2. 点击右上角「+」按钮，选择「New repository」
3. 填写仓库信息：
   - Repository name：仓库名称
   - Description：仓库描述（可选）
   - Visibility：仓库可见性（Public/Private）
4. 点击「Create repository」创建仓库

## 3. 本地仓库初始化与连接

### 3.1 初始化本地仓库
在项目根目录下执行：
```bash
E:\software\Git\cmd\git.exe init
```

### 3.2 连接GitHub仓库
```bash
E:\software\Git\cmd\git.exe remote add origin https://github.com/用户名/仓库名.git
```

### 3.3 查看远程仓库信息
```bash
E:\software\Git\cmd\git.exe remote -v
```

## 4. 代码提交与推送

### 4.1 创建.gitignore文件
在项目根目录创建.gitignore文件，添加需要忽略的文件和目录：
```gitignore
# Node.js & Vue 前端依赖
node_modules/
# 前端打包目录
dist/
# VSCode 编辑器配置
.vscode/

# Java 后端构建输出
target/
# IDEA/其他IDE配置
.idea/
*.iml

# 日志文件
*.log
logs/

# 系统文件
.DS_Store
Thumbs.db

# 上传目录（如有临时文件）
upload/

# 其他常见忽略
.env
.env.*
.settings.xml
```

### 4.2 查看Git状态
```bash
E:\software\Git\cmd\git.exe status
```

### 4.3 添加文件到暂存区
```bash
# 添加所有文件
E:\software\Git\cmd\git.exe add .

# 添加单个文件
E:\software\Git\cmd\git.exe add 文件名
```

### 4.4 提交代码到本地仓库
```bash
E:\software\Git\cmd\git.exe commit -m "提交信息"
```

### 4.5 推送到GitHub仓库
```bash
E:\software\Git\cmd\git.exe push origin master
```

## 5. 文件更新与管理

### 5.1 更新文件后提交
```bash
# 添加更新的文件
E:\software\Git\cmd\git.exe add 文件名

# 提交更新
E:\software\Git\cmd\git.exe commit -m "更新文件：文件名"

# 推送到GitHub
E:\software\Git\cmd\git.exe push origin master
```

### 5.2 删除文件
```bash
# 删除本地文件并记录删除操作
E:\software\Git\cmd\git.exe rm 文件名

# 提交删除操作
E:\software\Git\cmd\git.exe commit -m "删除文件：文件名"

# 推送到GitHub
E:\software\Git\cmd\git.exe push origin master
```

### 5.3 查看提交历史
```bash
# 查看最近10次提交
E:\software\Git\cmd\git.exe log --oneline -n 10

# 查看完整提交历史
E:\software\Git\cmd\git.exe log
```

## 6. 常见问题与解决方法

### 6.1 网络连接问题
如果遇到"Connection was reset"或类似网络错误：
1. 检查网络连接状态
2. 等待一段时间后重试
3. 检查防火墙或代理设置
4. 尝试使用SSH协议替代HTTPS协议（需要配置SSH密钥）

### 6.2 文件权限问题
在Windows系统中，确保Git可以访问项目目录。

### 6.3 提交信息规范
提交信息应清晰描述本次提交的内容，建议使用以下格式：
```
类型：描述

详细说明（可选）
```

类型包括：feat（新功能）、fix（修复bug）、docs（文档更新）、style（代码格式调整）、refactor（代码重构）、test（测试相关）、chore（其他杂项）

## 7. 高级操作

### 7.1 分支管理
```bash
# 创建新分支
E:\software\Git\cmd\git.exe branch 分支名

# 切换分支
E:\software\Git\cmd\git.exe checkout 分支名

# 创建并切换到新分支
E:\software\Git\cmd\git.exe checkout -b 分支名

# 合并分支
E:\software\Git\cmd\git.exe merge 分支名
```

### 7.2 标签管理
```bash
# 创建标签
E:\software\Git\cmd\git.exe tag v1.0.0

# 推送标签到GitHub
E:\software\Git\cmd\git.exe push origin --tags
```

---

**注意**：本指南中的Git路径为示例路径（`E:\software\Git\cmd\git.exe`），请根据实际安装路径进行调整。