package com.rest_mag_sys.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计报表解读请求
 */
@Data
public class StatisticsInterpretRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报表类型
     */
    private String reportType;

    /**
     * 开始日期 (yyyy-MM-dd)
     */
    private String startDate;

    /**
     * 结束日期 (yyyy-MM-dd)
     */
    private String endDate;

    /**
     * 返回条数限制
     */
    private Integer limit;

    /**
     * 月度趋势月数
     */
    private Integer months;

    /**
     * 汇总周期: day, week, month, year
     */
    private String period;

    /**
     * 顾客ID
     */
    private Long customerId;

    /**
     * 排序类型: quantity, sales
     */
    private String type;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
