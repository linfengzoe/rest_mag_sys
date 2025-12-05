package com.rest_mag_sys.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // 令牌秘钥
    @Value("${jwt.secret-key}")
    private String secret;

    // 过期时间（默认24小时）
    @Value("${jwt.expiration:86400000}")
    private long expire;

    /**
     * 生成token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return token
     */
    public String generateToken(Long userId, String username, String role) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析token
     *
     * @param token token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT token解析异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证token是否过期
     *
     * @param claims token claims
     * @return 是否过期
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从token中获取用户ID
     *
     * @param token token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? Long.valueOf(claims.get("userId").toString()) : null;
    }

    /**
     * 从token中获取用户名
     *
     * @param token token
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username").toString() : null;
    }

    /**
     * 从token中获取用户角色
     *
     * @param token token
     * @return 用户角色
     */
    public String getUserRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role").toString() : null;
    }
} 