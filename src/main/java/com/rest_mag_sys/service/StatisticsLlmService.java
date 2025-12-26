package com.rest_mag_sys.service;

import com.rest_mag_sys.dto.StatisticsAskRequest;
import com.rest_mag_sys.dto.StatisticsInterpretRequest;

import java.util.Map;

/**
 * 统计分析LLM服务接口
 */
public interface StatisticsLlmService {

    /**
     * 解读报表指标
     * @param request 解读请求
     * @return 解读结果
     */
    Map<String, Object> interpretReport(StatisticsInterpretRequest request);

    /**
     * 统计问答
     * @param request 问答请求
     * @return 回答结果
     */
    Map<String, Object> answerQuestion(StatisticsAskRequest request);
}
