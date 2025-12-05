package com.rest_mag_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rest_mag_sys.common.CustomException;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Orders;
import com.rest_mag_sys.entity.TableInfo;
import com.rest_mag_sys.mapper.OrdersMapper;
import com.rest_mag_sys.mapper.TableInfoMapper;
import com.rest_mag_sys.service.TableInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 桌位服务实现类
 */
@Service
@Slf4j
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements TableInfoService {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 分页查询桌位列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 桌位名称
     * @param status 桌位状态
     * @return 分页结果
     */
    public R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name, Integer status) {
        Page<TableInfo> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<TableInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), TableInfo::getName, name);
        queryWrapper.eq(status != null, TableInfo::getStatus, status);
        queryWrapper.orderByAsc(TableInfo::getId);
        this.page(pageInfo, queryWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("records", pageInfo.getRecords());
        return R.success(result);
    }

    /**
     * 分页查询桌位列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 桌位名称
     * @return 分页结果
     */
    public R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name) {
        // 兼容原有接口
        return getPageList(page, pageSize, name, null);
    }

    /**
     * 分页查询餐桌
     * @param pageQueryDTO 分页查询参数
     * @return 餐桌分页结果
     */
    @Override
    public Page<TableInfo> pageQuery(PageQueryDTO pageQueryDTO) {
        // 构造分页对象
        Page<TableInfo> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 构造查询条件
        LambdaQueryWrapper<TableInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 支持 keyword 或 name 模糊查询
        String keyword = pageQueryDTO.getKeyword();
        String name = pageQueryDTO.getName();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(TableInfo::getName, keyword);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(TableInfo::getName, name);
        }
        // 支持状态精确查询
        Integer status = pageQueryDTO.getStatus();
        queryWrapper.eq(status != null, TableInfo::getStatus, status);

        // 按名称升序
        queryWrapper.orderByAsc(TableInfo::getName);

        // 执行查询
        this.page(page, queryWrapper);

        return page;
    }

    /**
     * 查询所有空闲的餐桌
     * @return 餐桌列表
     */
    @Override
    public List<TableInfo> listIdle() {
        // 构造查询条件
        LambdaQueryWrapper<TableInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableInfo::getStatus, 0); // 只查询空闲的餐桌
        queryWrapper.orderByAsc(TableInfo::getName);
        
        // 执行查询
        return this.list(queryWrapper);
    }

    /**
     * 修改餐桌状态
     * @param id 餐桌ID
     * @param status 状态
     * @return 是否成功
     */
    @Override
    public boolean updateStatus(Long id, Integer status) {
        // 获取餐桌信息
        TableInfo tableInfo = this.getById(id);
        if (tableInfo == null) {
            throw new CustomException("餐桌不存在");
        }
        
        // 修改状态
        tableInfo.setStatus(status);
        
        // 更新餐桌
        return this.updateById(tableInfo);
    }

    /**
     * 新增餐桌
     * @param tableInfo 餐桌信息
     * @return 是否成功
     */
    @Override
    public boolean saveTableInfo(TableInfo tableInfo) {
        // 设置默认值
        tableInfo.setStatus(0); // 默认空闲
        
        // 保存餐桌
        return this.save(tableInfo);
    }

    /**
     * 修改餐桌
     * @param tableInfo 餐桌信息
     * @return 是否成功
     */
    @Override
    public boolean updateTableInfo(TableInfo tableInfo) {
        log.info("TableInfoService - 更新餐桌信息：{}", tableInfo);
        try {
            // 更新餐桌
            boolean result = this.updateById(tableInfo);
            log.info("TableInfoService - 更新结果：{}", result);
            return result;
        } catch (Exception e) {
            log.error("TableInfoService - 更新餐桌异常：{}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除餐桌
     * @param id 餐桌ID
     * @return 是否成功
     */
    @Override
    public boolean removeTableInfo(Long id) {
        // 调用removeById方法
        return this.removeById(id);
    }

    /**
     * 根据ID删除桌位，删除前需要判断是否有关联的订单
     * @param id 桌位ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        // 查询是否有关联的订单
        Long longId = (Long) id;
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getTableId, longId);
        Long count = ordersMapper.selectCount(queryWrapper);
        if (count > 0) {
            // 已关联订单，不能删除
            throw new CustomException("当前桌位已关联订单，不能删除");
        }

        // 调用父类的removeById方法
        return super.removeById(id);
    }

    /**
     * 获取所有可用桌位
     * @return 可用桌位列表
     */
    @Override
    public List<TableInfo> getAvailableTables() {
        // 构造查询条件：查询空闲状态的桌位
        LambdaQueryWrapper<TableInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableInfo::getStatus, 0); // 状态为0表示空闲可用
        queryWrapper.orderByAsc(TableInfo::getName);
        
        // 执行查询
        return this.list(queryWrapper);
    }
} 