package com.rest_mag_sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rest_mag_sys.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 菜品分类Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    @Select("SELECT MAX(sort) FROM category")
    Integer selectMaxSort();
} 