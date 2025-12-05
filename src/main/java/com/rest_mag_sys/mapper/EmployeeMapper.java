package com.rest_mag_sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rest_mag_sys.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工Mapper接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
} 