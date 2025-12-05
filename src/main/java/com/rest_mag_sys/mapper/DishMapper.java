package com.rest_mag_sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rest_mag_sys.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品Mapper接口
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
} 