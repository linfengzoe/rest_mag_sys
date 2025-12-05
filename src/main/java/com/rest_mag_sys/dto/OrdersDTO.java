package com.rest_mag_sys.dto;

import com.rest_mag_sys.entity.OrderDetail;
import com.rest_mag_sys.entity.Orders;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 订单数据传输对象
 */
@Data
public class OrdersDTO extends Orders {

    /**
     * 订单明细列表
     */
    private List<OrderDetail> orderDetails;

    /**
     * 前端传递的商品项目列表（用于接收前端数据）
     */
    private List<Map<String, Object>> items;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地址
     */
    private String address;

    /**
     * 收货人
     */
    private String consignee;

    /**
     * 桌位名称
     */
    private String tableName;

    /**
     * 顾客姓名
     */
    private String customerName;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 是否已评价（true 表示该订单已经提交过评价）
     */
    private Boolean reviewed;

    // Getter and Setter methods
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getConsignee() {
        return consignee;
    }
    
    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }
} 