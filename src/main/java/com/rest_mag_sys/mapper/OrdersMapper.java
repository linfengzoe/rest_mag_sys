package com.rest_mag_sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rest_mag_sys.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
} 