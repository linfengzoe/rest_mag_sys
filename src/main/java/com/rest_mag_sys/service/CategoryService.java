package com.rest_mag_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Category;

import java.util.List;
import java.util.Map;

/**
 * 菜品分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分页查询分类
     * @param pageQueryDTO 分页查询参数
     * @return 分类分页结果
     */
    Page<Category> pageQuery(PageQueryDTO pageQueryDTO);

    /**
     * 查询所有启用的分类
     * @return 分类列表
     */
    List<Category> listEnabled();

    /**
     * 新增分类
     * @param category 分类信息
     * @return 是否成功
     */
    boolean saveCategory(Category category);

    /**
     * 修改分类
     * @param category 分类信息
     * @return 是否成功
     */
    boolean updateCategory(Category category);

    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否成功
     */
    boolean removeCategory(Long id);
    
    /**
     * 根据ID删除分类，删除前需要判断是否关联了菜品
     * @param id 分类ID
     * @return 是否成功
     */
    boolean removeById(Long id);
    
    /**
     * 分页查询分类列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 分类名称
     * @return 分页结果
     */
    R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name);
} 