package com.rest_mag_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rest_mag_sys.common.CustomException;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Category;
import com.rest_mag_sys.entity.Dish;
import com.rest_mag_sys.mapper.CategoryMapper;
import com.rest_mag_sys.mapper.DishMapper;
import com.rest_mag_sys.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜品分类服务实现类
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 分页查询分类
     * @param pageQueryDTO 分页查询参数
     * @return 分类分页结果
     */
    @Override
    public Page<Category> pageQuery(PageQueryDTO pageQueryDTO) {
        // 构造分页对象
        Page<Category> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        
        // 构造查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageQueryDTO.getKeyword()), Category::getName, pageQueryDTO.getKeyword());
        queryWrapper.orderByAsc(Category::getSort);
        
        // 执行查询
        this.page(page, queryWrapper);
        
        return page;
    }

    /**
     * 查询所有启用的分类
     * @return 分类列表
     */
    @Override
    public List<Category> listEnabled() {
        // 构造查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getStatus, 1); // 只查询启用的分类
        queryWrapper.orderByAsc(Category::getSort);
        
        // 执行查询
        return this.list(queryWrapper);
    }

    /**
     * 新增分类
     * @param category 分类信息
     * @return 是否成功
     */
    @Override
    public boolean save(Category category) {
        if (category.getSort() == null) {
            Integer maxSort = categoryMapper.selectMaxSort();
            category.setSort(maxSort == null ? 1 : maxSort + 1);
        }
        return super.save(category);
    }

    /**
     * 修改分类
     * @param category 分类信息
     * @return 是否成功
     */
    @Override
    public boolean updateCategory(Category category) {
        // 更新分类
        return this.updateById(category);
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否成功
     */
    @Override
    public boolean removeCategory(Long id) {
        // 判断分类是否关联菜品
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        Long count = dishMapper.selectCount(dishQueryWrapper);
        if (count > 0) {
            throw new CustomException("该分类下关联了菜品，不能删除");
        }
        
        // 删除分类
        return this.removeById(id);
    }

    /**
     * 分页查询分类列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 分类名称
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name) {
        // 创建分页对象
        Page<Category> pageInfo = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 根据名称模糊查询
        queryWrapper.like(StringUtils.isNotBlank(name), Category::getName, name);
        // 按排序字段升序排序
        queryWrapper.orderByAsc(Category::getSort);

        // 执行查询
        this.page(pageInfo, queryWrapper);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("records", pageInfo.getRecords());

        return R.success(result);
    }

    /**
     * 根据ID删除分类，删除前需要判断是否关联了菜品
     * @param id 分类ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Long id) {
        // 查询是否关联了菜品
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        Long count = dishMapper.selectCount(dishQueryWrapper);
        if (count > 0) {
            // 已关联菜品，不能删除
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 调用父类的removeById方法
        return super.removeById(id);
    }

    @Override
    public boolean saveCategory(Category category) {
        // 设置默认值
        category.setStatus(1); // 默认启用
        return this.save(category);
    }
} 