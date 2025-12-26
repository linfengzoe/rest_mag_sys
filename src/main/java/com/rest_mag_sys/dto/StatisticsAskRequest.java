package com.rest_mag_sys.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 统计问答请求
 */
@Data
public class StatisticsAskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 报表类型列表
     */
    private List<String> reportTypes;

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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<String> reportTypes) {
        this.reportTypes = reportTypes;
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
