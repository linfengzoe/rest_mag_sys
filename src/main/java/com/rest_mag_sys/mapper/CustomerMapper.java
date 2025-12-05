package com.rest_mag_sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rest_mag_sys.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 顾客Mapper接口
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
} 