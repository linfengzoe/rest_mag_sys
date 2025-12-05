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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        log.info("JWT拦截器处理请求: {} {}", request.getMethod(), requestPath);
        
        // 获取请求头中的token
        String authHeader = request.getHeader("Authorization");
        log.info("获取到的Authorization头: {}", authHeader != null ? "***" + authHeader.substring(Math.max(0, authHeader.length() - 10)) : "null");

        // 如果token为空，返回错误
        if (!StringUtils.hasLength(authHeader)) {
            log.warn("Authorization头为空，请求路径: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 处理Bearer前缀
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        try {
            // 解析token
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null || jwtUtil.isTokenExpired(claims)) {
                log.error("Token已过期或无效，请求路径: {}", requestPath);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 获取用户ID并存入ThreadLocal
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = claims.get("username").toString();
            String role = claims.get("role").toString();
            
            log.info("JWT解析成功，用户ID: {}, 用户名: {}, 角色: {}, 请求路径: {}", userId, username, role, requestPath);
            
            BaseContext.setCurrentId(userId);
            
            // 验证通过，放行
            return true;
        } catch (Exception e) {
            log.error("JWT解析异常，请求路径: {}", requestPath, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
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