package com.rest_mag_sys.service.impl;

import com.alibaba.fastjson2.JSON;
import com.rest_mag_sys.dto.StatisticsAskRequest;
import com.rest_mag_sys.dto.StatisticsInterpretRequest;
import com.rest_mag_sys.service.StatisticsLlmService;
import com.rest_mag_sys.service.StatisticsService;
import com.rest_mag_sys.service.llm.DeepSeekClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统计分析LLM服务实现
 */
@Service
public class StatisticsLlmServiceImpl implements StatisticsLlmService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_CUSTOMER_LIMIT = 5;
    private static final int DEFAULT_EMPLOYEE_LIMIT = 20;
    private static final int DEFAULT_BEHAVIOR_LIMIT = 20;
    private static final int DEFAULT_MONTHS = 12;
    private static final String DEFAULT_PERIOD = "month";
    private static final String DEFAULT_RANK_TYPE = "quantity";

    private final StatisticsService statisticsService;
    private final DeepSeekClient deepSeekClient;

    public StatisticsLlmServiceImpl(StatisticsService statisticsService, DeepSeekClient deepSeekClient) {
        this.statisticsService = statisticsService;
        this.deepSeekClient = deepSeekClient;
    }

    @Override
    public Map<String, Object> interpretReport(StatisticsInterpretRequest request) {
        String reportType = normalizeReportType(request.getReportType());
        ReportParams params = ReportParams.fromInterpret(request);
        Map<String, Object> reportData = loadReportData(reportType, params);

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildInterpretPrompt(reportType, reportData);
        String analysis = deepSeekClient.chat(systemPrompt, userPrompt);

        Map<String, Object> result = new HashMap<>();
        result.put("reportType", reportType);
        result.put("analysis", analysis);
        return result;
    }

    @Override
    public Map<String, Object> answerQuestion(StatisticsAskRequest request) {
        List<String> reportTypes = normalizeReportTypes(request.getReportTypes());
        ReportParams params = ReportParams.fromAsk(request);

        Map<String, Object> context = new LinkedHashMap<>();
        for (String reportType : reportTypes) {
            context.put(reportType, loadReportData(reportType, params));
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildQuestionPrompt(request.getQuestion(), context);
        String answer = deepSeekClient.chat(systemPrompt, userPrompt);

        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        result.put("usedReportTypes", reportTypes);
        return result;
    }

    private String buildSystemPrompt() {
        return "你是餐厅经营数据分析师，只能基于给定数据做分析，"
            + "不得编造或臆测。输出使用中文，给出结论、指标解读和可执行建议。"
            + "若数据不足，请明确指出需要哪些补充数据。";
    }

    private String buildInterpretPrompt(String reportType, Map<String, Object> reportData) {
        String dataJson = JSON.toJSONString(reportData);
        return "请解读以下统计报表数据。\n"
            + "报表类型: " + reportType + "\n"
            + "数据: " + dataJson + "\n"
            + "要求: 先给关键结论，再逐项解读指标，最后给出改进建议。";
    }

    private String buildQuestionPrompt(String question, Map<String, Object> context) {
        String dataJson = JSON.toJSONString(context);
        return "用户问题: " + question + "\n"
            + "可用数据: " + dataJson + "\n"
            + "要求: 直接回答问题，并给出依据和可执行建议。";
    }

    private Map<String, Object> loadReportData(String reportType, ReportParams params) {
        switch (reportType) {
            case "dashboard":
                return statisticsService.getDashboardData();
            case "sales-summary":
                return statisticsService.getSalesSummaryReport(
                    params.period == null ? DEFAULT_PERIOD : params.period,
                    params.startDate,
                    params.endDate
                );
            case "orders":
                return wrapData(statisticsService.getOrderStatistics(params.startDate, params.endDate));
            case "dish-ranking":
                return wrapData(statisticsService.getDishSalesRanking(
                    params.limit == null ? DEFAULT_LIMIT : params.limit,
                    params.rankType == null ? DEFAULT_RANK_TYPE : params.rankType
                ));
            case "customer-behavior":
                return wrapData(statisticsService.getCustomerBehaviorAnalysis(
                    params.limit == null ? DEFAULT_BEHAVIOR_LIMIT : params.limit
                ));
            case "customer-preferences":
                if (params.customerId == null) {
                    throw new IllegalArgumentException("customerId不能为空");
                }
                return wrapData(statisticsService.getCustomerDishPreferences(
                    params.customerId,
                    params.limit == null ? DEFAULT_CUSTOMER_LIMIT : params.limit
                ));
            case "employee-performance":
                return wrapData(statisticsService.getEmployeePerformance(
                    params.limit == null ? DEFAULT_EMPLOYEE_LIMIT : params.limit
                ));
            case "hourly-distribution":
                return wrapData(statisticsService.getHourlyOrderDistribution());
            case "monthly-trend":
                return wrapData(statisticsService.getMonthlySalesTrend(
                    params.months == null ? DEFAULT_MONTHS : params.months
                ));
            case "table-utilization":
                return wrapData(statisticsService.getTableUtilizationStatistics());
            case "dish-review-ranking":
                return wrapData(statisticsService.getDishReviewRanking(
                    params.limit == null ? DEFAULT_LIMIT : params.limit
                ));
            default:
                throw new IllegalArgumentException("未知报表类型: " + reportType);
        }
    }

    private Map<String, Object> wrapData(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    private String normalizeReportType(String reportType) {
        if (reportType == null || reportType.trim().isEmpty()) {
            return "dashboard";
        }
        return reportType.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> normalizeReportTypes(List<String> reportTypes) {
        if (reportTypes == null || reportTypes.isEmpty()) {
            return Arrays.asList("dashboard", "sales-summary");
        }
        List<String> normalized = new ArrayList<>();
        for (String reportType : reportTypes) {
            if (reportType == null || reportType.trim().isEmpty()) {
                continue;
            }
            String normalizedType = reportType.trim().toLowerCase(Locale.ROOT);
            if ("all".equals(normalizedType)) {
                return Arrays.asList(
                    "dashboard",
                    "sales-summary",
                    "orders",
                    "dish-ranking",
                    "customer-behavior",
                    "employee-performance",
                    "hourly-distribution",
                    "monthly-trend",
                    "table-utilization",
                    "dish-review-ranking"
                );
            }
            if (!normalized.contains(normalizedType)) {
                normalized.add(normalizedType);
            }
        }
        if (normalized.isEmpty()) {
            normalized.add("dashboard");
            normalized.add("sales-summary");
        }
        return normalized;
    }

    private static class ReportParams {
        private String startDate;
        private String endDate;
        private Integer limit;
        private Integer months;
        private String period;
        private Long customerId;
        private String rankType;

        private static ReportParams fromInterpret(StatisticsInterpretRequest request) {
            ReportParams params = new ReportParams();
            params.startDate = request.getStartDate();
            params.endDate = request.getEndDate();
            params.limit = request.getLimit();
            params.months = request.getMonths();
            params.period = request.getPeriod();
            params.customerId = request.getCustomerId();
            params.rankType = request.getType();
            return params;
        }

        private static ReportParams fromAsk(StatisticsAskRequest request) {
            ReportParams params = new ReportParams();
            params.startDate = request.getStartDate();
            params.endDate = request.getEndDate();
            params.limit = request.getLimit();
            params.months = request.getMonths();
            params.period = request.getPeriod();
            params.customerId = request.getCustomerId();
            params.rankType = request.getType();
            return params;
        }
    }
}
