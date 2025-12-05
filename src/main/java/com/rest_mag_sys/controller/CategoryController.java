package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Category;
import com.rest_mag_sys.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category 分类信息
     * @return 结果
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("新增分类：{}", category.getName());
        try {
            categoryService.save(category);
            return R.success("新增分类成功");
        } catch (Exception e) {
            log.error("新增分类异常", e);
            return R.error("新增分类失败");
        }
    }

    /**
     * 分页查询
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        log.info("分页查询分类，页码：{}，每页记录数：{}", page, pageSize);
        try {
            return categoryService.getPageList(page, pageSize, name);
        } catch (Exception e) {
            log.error("查询分类列表异常", e);
            return R.error("查询分类列表失败");
        }
    }

    /**
     * 根据ID查询
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public R<Category> getById(@PathVariable Long id) {
        log.info("根据ID查询分类：{}", id);
        try {
            Category category = categoryService.getById(id);
            return R.success(category);
        } catch (Exception e) {
            log.error("查询分类详情异常", e);
            return R.error("查询分类详情失败");
        }
    }

    /**
     * 修改分类
     * @param category 分类信息
     * @return 结果
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类：{}", category.getId());
        try {
            categoryService.updateById(category);
            return R.success("修改分类成功");
        } catch (Exception e) {
            log.error("修改分类异常", e);
            return R.error("修改分类失败");
        }
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        log.info("删除分类：{}", id);
        try {
            categoryService.removeById(id);
            return R.success("删除分类成功");
        } catch (com.rest_mag_sys.common.CustomException ce) {
            log.warn("删除分类业务异常: {}", ce.getMessage());
            return R.error(ce.getMessage());
        } catch (Exception e) {
            log.error("删除分类异常", e);
            return R.error("删除分类失败");
        }
    }

    /**
     * 获取所有分类
     * @return 分类列表
     */
    @GetMapping("/list")
    public R<List<Category>> list() {
        log.info("查询所有分类");
        try {
            List<Category> list = categoryService.list();
            return R.success(list);
        } catch (Exception e) {
            log.error("查询分类列表异常", e);
            return R.error("查询分类列表失败");
        }
    }

    /**
     * 更新分类状态
     * @param id 分类ID
     * @param status 状态
     * @return 结果
     */
    @PutMapping("/status/{id}")
    public R<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新分类状态：{}，状态：{}", id, status);
        try {
            Category category = new Category();
            category.setId(id);
            category.setStatus(status);
            categoryService.updateById(category);
            return R.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新分类状态异常", e);
            return R.error("更新分类状态失败");
        }
    }
} 