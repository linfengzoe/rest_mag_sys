package com.rest_mag_sys.service.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek LLM客户端
 */
@Service
@Slf4j
public class DeepSeekClient {

    private final RestTemplate restTemplate;

    @Value("${llm.deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${llm.deepseek.api-key:}")
    private String apiKey;

    @Value("${llm.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${llm.deepseek.temperature:0.2}")
    private Double temperature;

    @Value("${llm.deepseek.max-tokens:800}")
    private Integer maxTokens;

    public DeepSeekClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String chat(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("DeepSeek API Key未配置");
        }

        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
        long startAt = System.currentTimeMillis();
        log.info("DeepSeek请求开始: model={}, url={}", model, url);

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Arrays.asList(systemMessage, userMessage));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("DeepSeek响应为空");
            }

            Object choicesObj = body.get("choices");
            if (!(choicesObj instanceof List)) {
                log.warn("DeepSeek响应格式异常: {}", body.keySet());
                throw new IllegalStateException("DeepSeek响应格式异常");
            }

            List<?> choices = (List<?>) choicesObj;
            if (choices.isEmpty()) {
                throw new IllegalStateException("DeepSeek未返回内容");
            }

            Object first = choices.get(0);
            if (!(first instanceof Map)) {
                throw new IllegalStateException("DeepSeek响应格式异常");
            }

            Map<?, ?> firstChoice = (Map<?, ?>) first;
            Object messageObj = firstChoice.get("message");
            if (!(messageObj instanceof Map)) {
                throw new IllegalStateException("DeepSeek响应格式异常");
            }

            Map<?, ?> message = (Map<?, ?>) messageObj;
            Object contentObj = message.get("content");
            log.info("DeepSeek请求完成: costMs={}", System.currentTimeMillis() - startAt);
            return contentObj == null ? "" : contentObj.toString().trim();
        } catch (RestClientResponseException e) {
            log.warn("DeepSeek请求异常: costMs={}", System.currentTimeMillis() - startAt);
            log.error("DeepSeek请求失败: status={}, body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("DeepSeek请求失败: " + e.getRawStatusCode());
        } catch (Exception e) {
            log.warn("DeepSeek请求异常: costMs={}", System.currentTimeMillis() - startAt);
            log.error("DeepSeek请求异常", e);
            throw e;
        }
    }
}
