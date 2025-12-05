package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.CustomException;
import com.rest_mag_sys.common.JwtUtil;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.LoginDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.dto.UserDTO;
import com.rest_mag_sys.entity.User;
import com.rest_mag_sys.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${upload.dir:}")
    private String rawUploadDir;

    private String getUploadDir() {
        if (rawUploadDir == null || rawUploadDir.trim().isEmpty()) {
            return System.getProperty("user.dir") + File.separator + "upload" + File.separator;
        }
        // 如果配置的是相对路径，则转换为绝对路径
        File f = new File(rawUploadDir);
        if (f.isAbsolute()) {
            return rawUploadDir.endsWith(File.separator) ? rawUploadDir : rawUploadDir + File.separator;
        }
        return System.getProperty("user.dir") + File.separator + rawUploadDir + (rawUploadDir.endsWith(File.separator) ? "" : File.separator);
    }

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO.getUsername());

        try {
            // 1. 根据用户名查询用户
            User user = userService.getByUsername(loginDTO.getUsername());
            log.info("查询用户结果：{}", user);

            // 2. 判断用户是否存在
            if (user == null) {
                log.warn("用户名不存在：{}", loginDTO.getUsername());
                return R.error("用户名不存在");
            }

            // 3. 密码比对 - 对用户输入的密码进行MD5加密后与数据库中的密码比对
            String encryptedPassword = org.springframework.util.DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
            log.info("密码比对：{} vs {} (加密后: {})", user.getPassword(), loginDTO.getPassword(), encryptedPassword);
            if (!user.getPassword().equals(encryptedPassword)) {
                log.warn("密码错误：{}", loginDTO.getUsername());
                return R.error("密码错误");
            }

            // 4. 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            log.info("生成JWT令牌：{}", token);

            // 5. 将用户信息返回给前端
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("name", user.getName());
            map.put("role", user.getRole());
            map.put("token", token);

            return R.success(map);
        } catch (Exception e) {
            log.error("登录异常", e);
            return R.error("登录异常：" + e.getMessage());
        }
    }

    /**
     * 用户注册
     * @param userDTO 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public R<String> register(@RequestBody UserDTO userDTO) {
        log.info("用户注册：{}", userDTO.getUsername());
        boolean result = userService.register(userDTO);
        return result ? R.success("注册成功") : R.error("注册失败");
    }

    /**
     * 分页查询用户
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Page<User>> page(PageQueryDTO pageQueryDTO) {
        Page<User> page = userService.pageQuery(pageQueryDTO);
        return R.success(page);
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 密码不返回给前端
        }
        return R.success(user);
    }

    /**
     * 更新用户信息
     * @param userDTO 用户信息
     * @return 更新结果
     */
    @PutMapping
    public R<String> update(@RequestBody UserDTO userDTO) {
        log.info("更新用户信息：{}", userDTO.getId());
        boolean result = userService.updateUser(userDTO);
        return result ? R.success("更新成功") : R.error("更新失败");
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        log.info("删除用户：{}", id);
        boolean result = userService.removeById(id);
        return result ? R.success("删除成功") : R.error("删除失败");
    }

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public R<String> deleteBatch(@RequestParam List<Long> ids) {
        log.info("批量删除用户：{}", ids);
        boolean result = userService.removeByIds(ids);
        return result ? R.success("删除成功") : R.error("删除失败");
    }

    /**
     * 修改密码
     * @param map 包含旧密码和新密码的Map
     * @return 修改结果
     */
    @PutMapping("/password")
    public R<String> updatePassword(@RequestBody Map<String, String> map) {
        log.info("修改密码");
        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");
        Long userId = BaseContext.getCurrentId();

        boolean result = userService.updatePassword(userId, oldPassword, newPassword);
        return result ? R.success("修改成功") : R.error("修改失败");
    }

    /**
     * 获取当前登录用户信息
     * @return
     */
    @GetMapping("/info")
    public R<UserDTO> getCurrentUser() {
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        if (user == null) {
            return R.error("用户不存在");
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setPassword(null);

        return R.success(userDTO);
    }

    /**
     * 更新个人信息
     * @param userDTO
     * @return
     */
    @PutMapping("/profile")
    public R<String> updateProfile(@RequestBody UserDTO userDTO) {
        Long userId = BaseContext.getCurrentId();
        if (!userId.equals(userDTO.getId())) {
            return R.error("无权修改他人信息");
        }

        boolean result = userService.updateProfile(userDTO);
        return result ? R.success("个人信息更新成功") : R.error("个人信息更新失败");
    }

    /**
     * 获取用户列表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role) {
        
        return userService.getPageList(page, pageSize, name, role);
    }

    /**
     * 添加用户
     * @param userDTO
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody UserDTO userDTO) {
        log.info("添加用户：{}", userDTO.getUsername());

        // 检查用户名是否已存在
        User existUser = userService.getByUsername(userDTO.getUsername());
        if (existUser != null) {
            return R.error("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 密码加密
        String password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(password);

        // 设置默认状态为启用
        user.setStatus(1);

        userService.save(user);
        return R.success("添加成功");
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 更新结果
     */
    @PostMapping("/status")
    public R<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("更新用户状态，用户ID：{}，状态：{}", id, status);
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean result = userService.updateById(user);
        return result ? R.success("状态更新成功") : R.error("状态更新失败");
    }

    /**
     * 批量更新用户状态
     * @param ids 用户ID列表
     * @param status 状态
     * @return 更新结果
     */
    @PostMapping("/status/batch")
    public R<String> updateStatusBatch(@RequestParam List<Long> ids, @RequestParam Integer status) {
        log.info("批量更新用户状态，用户ID：{}，状态：{}", ids, status);
        boolean result = userService.updateStatusBatch(ids, status);
        return result ? R.success("状态更新成功") : R.error("状态更新失败");
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout() {
        // JWT无需服务端注销，前端删除token即可
        return R.success("退出成功");
    }

    /**
     * 测试用户认证状态
     * @return 认证信息
     */
    @GetMapping("/auth/test")
    public R<Map<String, Object>> testAuth() {
        try {
            Long userId = BaseContext.getCurrentId();
            log.info("测试认证，当前用户ID：{}", userId);
            
            if (userId == null) {
                return R.error("用户未认证");
            }
            
            User user = userService.getById(userId);
            if (user == null) {
                return R.error("用户不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("username", user.getUsername());
            result.put("name", user.getName());
            result.put("role", user.getRole());
            result.put("status", "认证成功");
            
            return R.success(result);
        } catch (Exception e) {
            log.error("测试认证异常", e);
            return R.error("认证异常：" + e.getMessage());
        }
    }

    @PostMapping("/uploadAvatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传头像，原始文件名：{}，保存目录：{}", file.getOriginalFilename(), getUploadDir());
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String newFileName = "avatar_" + UUID.randomUUID().toString() + (ext != null ? ("." + ext) : "");
        String dirPath = getUploadDir() + "avatars" + File.separator;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            log.info("创建上传目录{}: {}", dir.getAbsolutePath(), created);
        }
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("头像保存失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件保存失败: " + e.getMessage());
        }
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = baseUrl + "/upload/avatars/" + newFileName;
        log.info("头像上传成功，访问URL：{}", url);
        return ResponseEntity.ok(url);
    }
} 