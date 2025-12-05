package com.rest_mag_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.TableInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 餐桌服务接口
 */
public interface TableInfoService extends IService<TableInfo> {

    /**
     * 分页查询餐桌
     * @param pageQueryDTO 分页查询参数
     * @return 餐桌分页结果
     */
    Page<TableInfo> pageQuery(PageQueryDTO pageQueryDTO);

    /**
     * 查询所有空闲的餐桌
     * @return 餐桌列表
     */
    List<TableInfo> listIdle();

    /**
     * 修改餐桌状态
     * @param id 餐桌ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 新增餐桌
     * @param tableInfo 餐桌信息
     * @return 是否成功
     */
    boolean saveTableInfo(TableInfo tableInfo);

    /**
     * 修改餐桌
     * @param tableInfo 餐桌信息
     * @return 是否成功
     */
    boolean updateTableInfo(TableInfo tableInfo);

    /**
     * 删除餐桌
     * @param id 餐桌ID
     * @return 是否成功
     */
    boolean removeTableInfo(Long id);
    
    /**
     * 根据ID删除桌位，删除前需要判断是否有关联的订单
     * @param id 桌位ID
     * @return 是否成功
     */
    boolean removeById(Serializable id);

    /**
     * 分页查询桌位列表，支持状态条件
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 桌位名称
     * @param status 桌位状态
     * @return 分页结果
     */
    R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name, Integer status);

    /**
     * 获取所有可用桌位
     * @return 可用桌位列表
     */
    List<TableInfo> getAvailableTables();
} 