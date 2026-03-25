package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.common.RequireRoles;
import com.rest_mag_sys.dto.DishDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
//import com.rest_mag_sys.entity.Dish;
import com.rest_mag_sys.service.DishService;
import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
//import java.util.Map;

/**
 * 菜品控制器
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO 菜品信息
     * @return 结果
     */
    @PostMapping
    @RequireRoles({"admin", "employee"})
    public R<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        boolean result = dishService.saveWithFlavor(dishDTO);
        return result ? R.success("新增成功") : R.error("新增失败");
    }

    /**
     * 分页查询
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @RequireRoles({"admin", "employee"})
    public R<Page<DishDTO>> page(PageQueryDTO pageQueryDTO) {
        log.info("分页查询菜品，参数：{}", pageQueryDTO);
        Page<DishDTO> page = dishService.pageQuery(pageQueryDTO);
        return R.success(page);
    }

    /**
     * 根据ID查询
     * @param id 菜品ID
     * @return 菜品信息
     */
    @GetMapping("/{id}")
    @RequireRoles({"admin", "employee", "customer"})
    public R<DishDTO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品：{}", id);
        DishDTO dishDTO = dishService.getByIdWithFlavor(id);
        return R.success(dishDTO);
    }

    /**
     * 修改菜品
     * @param dishDTO 菜品信息
     * @return 结果
     */
    @PutMapping
    @RequireRoles({"admin", "employee"})
    public R<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        boolean result = dishService.updateWithFlavor(dishDTO);
        return result ? R.success("修改成功") : R.error("修改失败");
    }

    /**
     * 删除菜品
     * @param ids 菜品ID列表
     * @return 结果
     */
    @DeleteMapping
    @RequireRoles({"admin", "employee"})
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}", ids);
        boolean result = dishService.removeWithFlavor(ids);
        return result ? R.success("删除成功") : R.error("删除失败");
    }

    /**
     * 根据ID删除单个菜品
     * @param id 菜品ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @RequireRoles({"admin", "employee"})
    public R<String> deleteById(@PathVariable Long id) {
        log.info("根据ID删除菜品：{}", id);
        List<Long> ids = List.of(id);
        boolean result = dishService.removeWithFlavor(ids);
        return result ? R.success("删除成功") : R.error("删除失败");
    }

    /**
     * 分页查询（用于前端listDishes API）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    @GetMapping("/list")
    @RequireRoles({"admin", "employee", "customer"})
    public R<Page<DishDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        log.info("查询菜品列表，页码：{}，每页记录数：{}, 名称：{}, 分类ID：{}, 状态：{}", page, pageSize, name, categoryId, status);

        PageQueryDTO pageQueryDTO = new PageQueryDTO();
        pageQueryDTO.setPage(page);
        pageQueryDTO.setPageSize(pageSize);
        if (name != null && !name.trim().isEmpty()) {
            pageQueryDTO.setName(name.trim());
        }
        if (categoryId != null) {
            pageQueryDTO.setCategoryId(categoryId);
        }
        if (status != null) {
            pageQueryDTO.setStatus(status);
        }

        Page<DishDTO> pageResult = dishService.pageQuery(pageQueryDTO);
        return R.success(pageResult);
    }

    /**
     * 根据分类ID查询菜品列表
     * @param categoryId 分类ID
     * @return 菜品列表
     */
    @GetMapping("/listByCategoryId")
    @RequireRoles({"admin", "employee", "customer"})
    public R<List<DishDTO>> listByCategoryId(Long categoryId) {
        log.info("根据分类ID查询菜品列表：{}", categoryId);
        List<DishDTO> list = dishService.listWithFlavor(categoryId);
        return R.success(list);
    }

    /**
     * 修改菜品状态
     * @param status 状态
     * @param ids 菜品ID列表
     * @return 结果
     */
    @PostMapping("/status/{status}")
    @RequireRoles({"admin", "employee"})
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("修改菜品状态，状态：{}，菜品ID：{}", status, ids);
        boolean result = dishService.updateStatus(status, ids);
        return result ? R.success("修改成功") : R.error("修改失败");
    }

    /**
     * 上传菜品图片
     * @param file 图片文件
     * @return 图片路径
     */
    @PostMapping("/upload")
    @RequireRoles({"admin", "employee"})
    public R<String> upload(MultipartFile file) {
        log.info("上传菜品图片：{}", file.getOriginalFilename());
        String relativePath = dishService.uploadImage(file);
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = relativePath.startsWith("/") ? baseUrl + relativePath : baseUrl + "/" + relativePath;
        return R.success(url);
    }
} 