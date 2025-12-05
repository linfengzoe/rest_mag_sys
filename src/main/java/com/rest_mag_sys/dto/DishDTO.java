package com.rest_mag_sys.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rest_mag_sys.entity.Dish;
import lombok.Data;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品数据传输对象
 */
@Data
public class DishDTO extends Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 口味列表
     */
    private List<String> flavors = new ArrayList<>();

    // Getter and Setter methods
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public List<String> getFlavors() {
        return flavors;
    }
    
    public void setFlavors(List<String> flavors) {
        this.flavors = flavors;
    }

    /**
     * 自定义status字段的setter方法，支持字符串和整数类型的转换
     * @param status 状态值，可以是字符串("ENABLED"/"DISABLED")或整数(0/1)
     */
    @JsonSetter("status")
    public void setStatusFromJson(Object status) {
        if (status instanceof String) {
            String statusStr = (String) status;
            if ("ENABLED".equalsIgnoreCase(statusStr)) {
                super.setStatus(1);
            } else if ("DISABLED".equalsIgnoreCase(statusStr)) {
                super.setStatus(0);
            } else {
                // 尝试解析为数字
                try {
                    super.setStatus(Integer.parseInt(statusStr));
                } catch (NumberFormatException e) {
                    super.setStatus(1); // 默认为启用状态
                }
            }
        } else if (status instanceof Number) {
            super.setStatus(((Number) status).intValue());
        } else {
            super.setStatus(1); // 默认为启用状态
        }
    }

    /**
     * 自定义price字段的setter方法，支持数字和字符串类型的转换
     * @param price 价格值，可以是数字或字符串
     */
    @JsonSetter("price")
    public void setPriceFromJson(Object price) {
        if (price == null) {
            super.setPrice(null);
            return;
        }
        
        if (price instanceof BigDecimal) {
            super.setPrice((BigDecimal) price);
        } else if (price instanceof Number) {
            super.setPrice(new BigDecimal(price.toString()));
        } else if (price instanceof String) {
            try {
                super.setPrice(new BigDecimal((String) price));
            } catch (NumberFormatException e) {
                super.setPrice(BigDecimal.ZERO);
            }
        } else {
            super.setPrice(BigDecimal.ZERO);
        }
    }

    /**
     * 自定义createTime字段的setter方法，支持字符串格式的时间
     * @param createTime 创建时间，可以是字符串或LocalDateTime
     */
    @JsonSetter("createTime")
    public void setCreateTimeFromJson(Object createTime) {
        if (createTime == null) {
            super.setCreateTime(null);
            return;
        }
        
        if (createTime instanceof LocalDateTime) {
            super.setCreateTime((LocalDateTime) createTime);
        } else if (createTime instanceof String) {
            try {
                String timeStr = (String) createTime;
                // 支持多种时间格式
                if (timeStr.contains("T")) {
                    // ISO格式：2024-01-01T00:00:00
                    super.setCreateTime(LocalDateTime.parse(timeStr));
                } else {
                    // 标准格式：2024-01-01 00:00:00
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    super.setCreateTime(LocalDateTime.parse(timeStr, formatter));
                }
            } catch (Exception e) {
                // 解析失败时忽略，让数据库自动填充
                super.setCreateTime(null);
            }
        } else {
            super.setCreateTime(null);
        }
    }

    /**
     * 自定义updateTime字段的setter方法，支持字符串格式的时间
     * @param updateTime 更新时间，可以是字符串或LocalDateTime
     */
    @JsonSetter("updateTime")
    public void setUpdateTimeFromJson(Object updateTime) {
        if (updateTime == null) {
            super.setUpdateTime(null);
            return;
        }
        
        if (updateTime instanceof LocalDateTime) {
            super.setUpdateTime((LocalDateTime) updateTime);
        } else if (updateTime instanceof String) {
            try {
                String timeStr = (String) updateTime;
                // 支持多种时间格式
                if (timeStr.contains("T")) {
                    // ISO格式：2024-01-01T00:00:00
                    super.setUpdateTime(LocalDateTime.parse(timeStr));
                } else {
                    // 标准格式：2024-01-01 00:00:00
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    super.setUpdateTime(LocalDateTime.parse(timeStr, formatter));
                }
            } catch (Exception e) {
                // 解析失败时忽略，让数据库自动填充
                super.setUpdateTime(null);
            }
        } else {
            super.setUpdateTime(null);
        }
    }

    @Override
    public String getImage() {
        String img = super.getImage();
        if (img == null || img.isEmpty()) {
            return img;
        }
        if (img.startsWith("http")) {
            return img;
        }
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        if (!img.startsWith("/")) {
            img = "/" + img;
        }
        return baseUrl + img;
    }
} 