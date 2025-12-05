package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.TableInfo;
import com.rest_mag_sys.service.TableInfoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 桌位控制器
 */
@RestController
@RequestMapping("/table")
@Slf4j
public class TableInfoController {

    @Autowired
    private TableInfoService tableInfoService;

    /**
     * 新增桌位
     * @param tableInfo 桌位信息
     * @return 结果
     */
    @PostMapping
    public R<String> save(@RequestBody TableInfo tableInfo) {
        log.info("新增桌位：{}", tableInfo.getName());
        try {
            tableInfoService.save(tableInfo);
            return R.success("新增桌位成功");
        } catch (org.springframework.dao.DuplicateKeyException dupEx) {
            log.error("新增桌位失败，名称重复", dupEx);
            return R.error("餐桌名称已存在，请更换名称");
        } catch (Exception e) {
            log.error("新增桌位异常", e);
            return R.error("新增桌位失败");
        }
    }

    /**
     * 分页查询
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 桌位名称
     * @param status 桌位状态
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        log.info("分页查询桌位，页码：{}，每页记录数：{}，状态：{}", page, pageSize, status);
        try {
            return tableInfoService.getPageList(page, pageSize, name, status);
        } catch (Exception e) {
            log.error("查询桌位列表异常", e);
            return R.error("查询桌位列表失败");
        }
    }

    /**
     * 根据ID查询
     * @param id 桌位ID
     * @return 桌位信息
     */
    @GetMapping("/{id}")
    public R<TableInfo> getById(@PathVariable Long id) {
        log.info("根据ID查询桌位：{}", id);
        try {
            TableInfo tableInfo = tableInfoService.getById(id);
            return R.success(tableInfo);
        } catch (Exception e) {
            log.error("查询桌位详情异常", e);
            return R.error("查询桌位详情失败");
        }
    }

    /**
     * 修改桌位
     * @param tableInfo 桌位信息
     * @return 结果
     */
    @PutMapping
    public R<String> update(@RequestBody TableInfo tableInfo) {
        log.info("修改桌位：{}", tableInfo.getId());
        log.info("修改桌位详细信息：{}", tableInfo);
        
        try {
            // 先检查餐桌是否存在
            if (tableInfo.getId() == null) {
                log.error("餐桌ID不能为空");
                return R.error("餐桌ID不能为空");
            }
            
            log.info("查询现有餐桌信息，ID：{}", tableInfo.getId());
            TableInfo existingTable = tableInfoService.getById(tableInfo.getId());
            if (existingTable == null) {
                log.error("餐桌不存在，ID：{}", tableInfo.getId());
                return R.error("餐桌不存在");
            }
            
            log.info("现有餐桌信息：{}", existingTable);
            log.info("准备更新餐桌，新信息：{}", tableInfo);
            
            // 调用Service方法更新
            boolean result = tableInfoService.updateById(tableInfo);
            
            log.info("更新餐桌结果：{}", result);
            
            if (result) {
                log.info("修改桌位成功，ID：{}", tableInfo.getId());
                return R.success("修改桌位成功");
            } else {
                log.error("修改桌位失败，更新返回false，ID：{}", tableInfo.getId());
                return R.error("修改桌位失败");
            }
        } catch (Exception e) {
            log.error("修改桌位异常，ID：{}，错误信息：{}", tableInfo.getId(), e.getMessage(), e);
            // 打印完整的异常栈
            e.printStackTrace();
            return R.error("修改桌位失败：" + e.getMessage());
        }
    }

    /**
     * 删除桌位
     * @param id 桌位ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        log.info("删除桌位：{}", id);
        try {
            tableInfoService.removeById(id);
            return R.success("删除桌位成功");
        } catch (Exception e) {
            log.error("删除桌位异常", e);
            return R.error("删除桌位失败");
        }
    }

    /**
     * 分页查询桌位列表（支持分页参数）
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping("/list")
    public R<Page<TableInfo>> list(PageQueryDTO params) {
        log.info("分页查询桌位，参数：{}", params);
        try {
            Page<TableInfo> page = tableInfoService.pageQuery(params);
            return R.success(page);
        } catch (Exception e) {
            log.error("查询桌位列表异常", e);
            return R.error("查询桌位列表失败");
        }
    }

    /**
     * 更新桌位状态
     * @param id 桌位ID
     * @param status 状态
     * @return 结果
     */
    @PutMapping("/status/{id}")
    public R<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新桌位状态：{}，状态：{}", id, status);
        
        try {
            // 验证状态值是否合法
            if (status < 0 || status > 2) {
                log.error("无效的状态值：{}", status);
                return R.error("无效的状态值，状态必须为0(空闲)、1(已预订)或2(就餐中)");
            }
            
            // 先查询当前餐桌信息
            TableInfo existingTable = tableInfoService.getById(id);
            if (existingTable == null) {
                log.error("餐桌不存在，ID：{}", id);
                return R.error("餐桌不存在");
            }
            
            log.info("当前餐桌状态：{}，目标状态：{}", existingTable.getStatus(), status);
            
            // 如果状态没有变化，直接返回成功
            if (existingTable.getStatus().equals(status)) {
                log.info("餐桌状态未发生变化，当前已是目标状态：{}", status);
                return R.success("餐桌状态未发生变化");
            }
            
            // 构建状态映射
            String[] statusNames = {"空闲", "已预订", "就餐中"};
            String currentStatusName = statusNames[existingTable.getStatus()];
            String targetStatusName = statusNames[status];
            
            log.info("状态转换：{} -> {}", currentStatusName, targetStatusName);
            
            // 创建新的TableInfo对象进行状态更新
            TableInfo tableInfo = new TableInfo();
            tableInfo.setId(id);
            tableInfo.setStatus(status);
            
            boolean result = tableInfoService.updateById(tableInfo);
            
            if (result) {
                log.info("餐桌状态更新成功：{} -> {}", currentStatusName, targetStatusName);
                return R.success("餐桌状态已更新为：" + targetStatusName);
            } else {
                log.error("餐桌状态更新失败，updateById返回false");
                return R.error("餐桌状态更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新桌位状态异常，ID：{}，目标状态：{}，错误信息：{}", id, status, e.getMessage(), e);
            return R.error("更新桌位状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用桌位列表
     * @return 可用桌位列表
     */
    @GetMapping("/available")
    public R<List<TableInfo>> getAvailableTables() {
        log.info("获取可用桌位列表");
        try {
            List<TableInfo> availableTables = tableInfoService.getAvailableTables();
            return R.success(availableTables);
        } catch (Exception e) {
            log.error("获取可用桌位列表异常", e);
            return R.error("获取可用桌位列表失败");
        }
    }
} 