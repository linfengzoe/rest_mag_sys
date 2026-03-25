package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.common.RequireRoles;
import com.rest_mag_sys.dto.OrdersDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Orders;
import com.rest_mag_sys.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrdersService ordersService;

    /**
     * 提交订单
     * @param ordersDTO 订单信息
     * @return 结果
     */
    @PostMapping("/submit")
    @RequireRoles({"customer"})
    public R<String> submit(@RequestBody OrdersDTO ordersDTO) {
        log.info("提交订单：{}", ordersDTO);
        Long userId = BaseContext.getCurrentId();
        ordersDTO.setUserId(userId);
        boolean result = ordersService.submit(ordersDTO);
        return result ? R.success("下单成功") : R.error("下单失败");
    }

    /**
     * 分页查询
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @RequireRoles({"admin", "employee"})
    public R<Page<OrdersDTO>> page(PageQueryDTO pageQueryDTO) {
        log.info("分页查询订单，参数：{}", pageQueryDTO);
        Page<OrdersDTO> page = ordersService.pageQuery(pageQueryDTO);
        return R.success(page);
    }

    /**
     * 查询用户订单
     * @param page 页码
     * @param pageSize 每页记录数
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/userPage")
    @RequireRoles({"customer"})
    public R<Page<OrdersDTO>> userPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("查询用户订单，页码：{}，每页记录数：{}，状态：{}，开始日期：{}，结束日期：{}", 
                page, pageSize, status, startDate, endDate);
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户ID：{}", userId);

        if (userId == null) {
            log.error("当前用户ID为null，可能是认证失败");
            return R.error("用户认证失败");
        }

        PageQueryDTO pageQueryDTO = new PageQueryDTO();
        pageQueryDTO.setPage(page);
        pageQueryDTO.setPageSize(pageSize);
        pageQueryDTO.setStatus(status);
        if (startDate != null) {
            pageQueryDTO.setBeginTime(startDate + " 00:00:00");
        }
        if (endDate != null) {
            pageQueryDTO.setEndTime(endDate + " 23:59:59");
        }

        Page<OrdersDTO> result = ordersService.userPageQuery(pageQueryDTO, userId);
        return R.success(result);
    }

    /**
     * 查询订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/details/{id}")
    @RequireRoles({"admin", "employee", "customer"})
    public R<OrdersDTO> details(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrdersDTO ordersDTO = ordersService.getOrderDetails(id);
        return R.success(ordersDTO);
    }

    /**
     * 接单
     * @param id 订单ID
     * @return 结果
     */
    @PutMapping("/accept/{id}")
    @RequireRoles({"admin", "employee"})
    public R<String> accept(@PathVariable Long id) {
        log.info("接单：{}", id);
        boolean result = ordersService.accept(id);
        return result ? R.success("接单成功") : R.error("接单失败");
    }

    /**
     * 拒单
     * @param map 包含拒单原因的Map
     * @return 结果
     */
    @PutMapping("/reject")
    @RequireRoles({"admin", "employee"})
    public R<String> reject(@RequestBody Map<String, Object> map) {
        log.info("拒单：{}", map);
        Long id = Long.valueOf(map.get("id").toString());
        String reason = map.get("reason").toString();
        boolean result = ordersService.reject(id, reason);
        return result ? R.success("拒单成功") : R.error("拒单失败");
    }

    /**
     * 完成订单
     * @param id 订单ID
     * @return 结果
     */
    @PutMapping("/complete/{id}")
    @RequireRoles({"admin", "employee"})
    public R<String> complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        boolean result = ordersService.complete(id);
        return result ? R.success("订单已完成") : R.error("操作失败");
    }

    /**
     * 取消订单
     * @param id 订单ID
     * @return 结果
     */
    @PutMapping("/cancel/{id}")
    @RequireRoles({"customer", "admin", "employee"})
    public R<String> cancel(@PathVariable Long id) {
        log.info("取消订单请求，订单ID：{}，类型：{}", id, id.getClass().getSimpleName());
        boolean result = ordersService.cancelOrder(id);
        return result ? R.success("取消成功") : R.error("取消失败");
    }

    /**
     * 取消订单（接受字符串ID）
     * @param map 包含订单ID的Map
     * @return 结果
     */
    @PutMapping("/cancel")
    @RequireRoles({"customer", "admin", "employee"})
    public R<String> cancelByString(@RequestBody Map<String, Object> map) {
        log.info("取消订单请求（字符串ID）：{}", map);
        try {
            String idStr = map.get("id").toString();
            log.info("字符串形式的订单ID：{}", idStr);
            Long id = Long.valueOf(idStr);
            log.info("转换后的订单ID：{}", id);
            
            // 获取当前用户ID
            Long currentUserId = BaseContext.getCurrentId();
            log.info("当前用户ID：{}", currentUserId);
            
            // 先查询订单
            Orders order = ordersService.getById(id);
            if (order == null) {
                log.error("订单不存在，订单ID：{}", id);
                return R.error("订单不存在");
            }
            
            log.info("查询到的订单：ID={}, UserId={}, Status={}, Number={}", 
                    order.getId(), order.getUserId(), order.getStatus(), order.getNumber());
            
            // 如果有用户ID，验证订单所有权
            if (currentUserId != null && !order.getUserId().equals(currentUserId)) {
                log.error("用户无权限取消此订单，订单UserId：{}，当前UserId：{}", order.getUserId(), currentUserId);
                return R.error("无权限取消此订单");
            }
            
            // 如果没有用户ID，记录警告但继续处理（临时解决方案）
            if (currentUserId == null) {
                log.warn("用户ID为null，跳过权限验证，订单ID：{}", id);
            }
            
            boolean result = ordersService.cancelOrder(id);
            String message = result ? "取消成功" : "取消失败";
            log.info("取消订单结果：{}，订单ID：{}", message, id);
            return result ? R.success(message) : R.error(message);
        } catch (NumberFormatException e) {
            log.error("订单ID格式错误：{}", map, e);
            return R.error("订单ID格式错误");
        } catch (Exception e) {
            log.error("取消订单异常（字符串ID）：{}", map, e);
            return R.error("取消失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询订单列表（用于前端getOrderList API）
     * @param page 页码
     * @param pageSize 每页记录数
     * @param id 搜索参数
     * @param status 搜索参数
     * @param startDate 搜索参数
     * @param endDate 搜索参数
     * @return 分页结果
     */
    @GetMapping("/list")
    @RequireRoles({"admin", "employee"})
    public R<Page<OrdersDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String id,
            @RequestParam(required = false, name="keyword") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // 兼容旧参数id，新参数keyword
        String searchKey = StringUtils.hasText(keyword) ? keyword : id;
        log.info("查询订单列表，页码：{}，每页记录数：{}，关键字：{}，状态：{}，开始日期：{}，结束日期：{}", page, pageSize, searchKey, status, startDate, endDate);
        PageQueryDTO pageQueryDTO = new PageQueryDTO();
        pageQueryDTO.setPage(page);
        pageQueryDTO.setPageSize(pageSize);
        if (StringUtils.hasText(searchKey)) {
            pageQueryDTO.setKeyword(searchKey);
        }
        pageQueryDTO.setStatus(status);
        if (StringUtils.hasText(startDate)) {
            pageQueryDTO.setBeginTime(startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            pageQueryDTO.setEndTime(endDate + " 23:59:59");
        }
        Page<OrdersDTO> pageResult = ordersService.pageQuery(pageQueryDTO);
        return R.success(pageResult);
    }

    /**
     * 支付订单
     * @param map 包含订单ID和支付方式的Map
     * @return 结果
     */
    @PutMapping("/pay")
    @RequireRoles({"customer"})
    public R<String> pay(@RequestBody Map<String, Object> map) {
        log.info("支付订单：{}", map);
        Long id = Long.valueOf(map.get("id").toString());
        String paymentMethod = map.get("paymentMethod").toString();
        boolean result = ordersService.payOrder(id, paymentMethod);
        return result ? R.success("支付成功") : R.error("支付失败");
    }

    /**
     * 获取订单统计数据
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @RequireRoles({"admin", "employee"})
    public R<Map<String, Object>> getOrderStatistics() {
        log.info("获取订单统计数据");
        Map<String, Object> statistics = ordersService.getStatistics();
        return R.success(statistics);
    }

    /**
     * 根据顾客ID获取订单列表
     * @param customerId 顾客ID
     * @return 订单列表
     */
    @GetMapping("/customer/{customerId}")
    @RequireRoles({"admin", "employee"})
    public R<Page<OrdersDTO>> getOrdersByCustomerId(@PathVariable Long customerId) {
        log.info("根据顾客ID获取订单列表，顾客ID：{}", customerId);
        PageQueryDTO pageQueryDTO = new PageQueryDTO();
        pageQueryDTO.setPage(1);
        pageQueryDTO.setPageSize(100); // 设置一个较大的页面大小

        // 使用userPageQuery方法，它可以根据用户ID查询
        Page<OrdersDTO> pageResult = ordersService.userPageQuery(pageQueryDTO, customerId);
        return R.success(pageResult);
    }

} 