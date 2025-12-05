package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.R;
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
    public R<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        try {
            boolean result = dishService.saveWithFlavor(dishDTO);
            return result ? R.success("新增成功") : R.error("新增失败");
        } catch (Exception e) {
            log.error("新增菜品异常", e);
            String errorMsg = e.getMessage();
            return R.error("新增菜品失败：" + (errorMsg != null ? errorMsg : "系统异常"));
        }
    }

    /**
     * 分页查询
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Page<DishDTO>> page(PageQueryDTO pageQueryDTO) {
        log.info("分页查询菜品，参数：{}", pageQueryDTO);
        try {
            Page<DishDTO> page = dishService.pageQuery(pageQueryDTO);
            return R.success(page);
        } catch (Exception e) {
            log.error("查询菜品列表异常", e);
            return R.error("查询菜品列表失败");
        }
    }

    /**
     * 根据ID查询
     * @param id 菜品ID
     * @return 菜品信息
     */
    @GetMapping("/{id}")
    public R<DishDTO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品：{}", id);
        try {
            DishDTO dishDTO = dishService.getByIdWithFlavor(id);
            return R.success(dishDTO);
        } catch (Exception e) {
            log.error("查询菜品详情异常", e);
            return R.error("查询菜品详情失败");
        }
    }

    /**
     * 修改菜品
     * @param dishDTO 菜品信息
     * @return 结果
     */
    @PutMapping
    public R<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        try {
            boolean result = dishService.updateWithFlavor(dishDTO);
            return result ? R.success("修改成功") : R.error("修改失败");
        } catch (Exception e) {
            log.error("修改菜品异常", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("删除菜品失败")) {
                return R.error(errorMsg);
            } else if (errorMsg != null && errorMsg.contains("更新菜品失败")) {
                return R.error(errorMsg);
            } else {
                return R.error("修改菜品失败：" + (errorMsg != null ? errorMsg : "系统异常"));
            }
        }
    }

    /**
     * 删除菜品
     * @param ids 菜品ID列表
     * @return 结果
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}", ids);
        try {
            boolean result = dishService.removeWithFlavor(ids);
            return result ? R.success("删除成功") : R.error("删除失败");
        } catch (Exception e) {
            log.error("删除菜品异常", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("删除菜品失败")) {
                return R.error(errorMsg);
            } else {
                return R.error("删除菜品失败：" + (errorMsg != null ? errorMsg : "系统异常"));
            }
        }
    }

    /**
     * 根据ID删除单个菜品
     * @param id 菜品ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public R<String> deleteById(@PathVariable Long id) {
        log.info("根据ID删除菜品：{}", id);
        try {
            List<Long> ids = List.of(id);
            boolean result = dishService.removeWithFlavor(ids);
            return result ? R.success("删除成功") : R.error("删除失败");
        } catch (Exception e) {
            log.error("删除菜品异常", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("删除菜品失败")) {
                return R.error(errorMsg);
            } else {
                return R.error("删除菜品失败：" + (errorMsg != null ? errorMsg : "系统异常"));
            }
        }
    }

    /**
     * 分页查询（用于前端listDishes API）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    @GetMapping("/list")
    public R<Page<DishDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        log.info("查询菜品列表，页码：{}，每页记录数：{}, 名称：{}, 分类ID：{}, 状态：{}", page, pageSize, name, categoryId, status);

        try {
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
        } catch (Exception e) {
            log.error("查询菜品列表异常", e);
            return R.error("查询菜品列表失败");
        }
    }

    /**
     * 根据分类ID查询菜品列表
     * @param categoryId 分类ID
     * @return 菜品列表
     */
    @GetMapping("/listByCategoryId")
    public R<List<DishDTO>> listByCategoryId(Long categoryId) {
        log.info("根据分类ID查询菜品列表：{}", categoryId);
        try {
            List<DishDTO> list = dishService.listWithFlavor(categoryId);
            return R.success(list);
        } catch (Exception e) {
            log.error("查询菜品列表异常", e);
            return R.error("查询菜品列表失败");
        }
    }

    /**
     * 修改菜品状态
     * @param status 状态
     * @param ids 菜品ID列表
     * @return 结果
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("修改菜品状态，状态：{}，菜品ID：{}", status, ids);
        try {
            boolean result = dishService.updateStatus(status, ids);
            return result ? R.success("修改成功") : R.error("修改失败");
        } catch (Exception e) {
            log.error("修改菜品状态异常", e);
            return R.error("修改菜品状态失败");
        }
    }

    /**
     * 上传菜品图片
     * @param file 图片文件
     * @return 图片路径
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info("上传菜品图片：{}", file.getOriginalFilename());
        try {
            String relativePath = dishService.uploadImage(file);
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            String url = relativePath.startsWith("/") ? baseUrl + relativePath : baseUrl + "/" + relativePath;
            return R.success(url);
        } catch (Exception e) {
            log.error("上传菜品图片异常", e);
            return R.error("上传菜品图片失败");
        }
    }
} 