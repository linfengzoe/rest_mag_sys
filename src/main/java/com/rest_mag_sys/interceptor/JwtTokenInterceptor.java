package com.rest_mag_sys.interceptor;

import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT令牌拦截器
 */
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 拦截请求，验证令牌
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestPath = request.getRequestURI();
        log.info("JWT拦截器处理请求: {} {}", request.getMethod(), requestPath);

        // 放行CORS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // 获取请求头中的token
        String authHeader = request.getHeader("Authorization");

        // 如果token为空，返回错误
        if (!StringUtils.hasLength(authHeader)) {
            log.warn("Authorization头为空，请求路径: {}", requestPath);
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Authorization头格式非法，请求路径: {}", requestPath);
            writeUnauthorized(response, "登录凭证格式错误");
            return false;
        }

        // 处理Bearer前缀
        String token = authHeader.substring(7);

        try {
            // 解析token
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null || jwtUtil.isTokenExpired(claims)) {
                log.error("Token已过期或无效，请求路径: {}", requestPath);
                writeUnauthorized(response, "登录已过期，请重新登录");
                return false;
            }

            // 获取用户ID并存入ThreadLocal
            Long userId = Long.valueOf(claims.get("userId").toString());
            log.info("JWT解析成功，请求路径: {}", requestPath);
            
            BaseContext.setCurrentId(userId);
            
            // 验证通过，放行
            return true;
        } catch (Exception e) {
            log.error("JWT解析异常，请求路径: {}", requestPath, e);
            writeUnauthorized(response, "登录状态无效，请重新登录");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\"}");
        } catch (IOException e) {
            log.error("写入401响应失败", e);
        }
    }

    /**
     * 请求完成后清理ThreadLocal
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
} 