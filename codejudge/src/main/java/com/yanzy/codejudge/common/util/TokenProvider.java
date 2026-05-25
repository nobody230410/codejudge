package com.yanzy.codejudge.common.util;

import com.yanzy.codejudge.auth.model.LoginUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.security.Key;

@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 1. 生成 Token：接收你的 LoginUser 对象
    public String generateToken(LoginUser user) {
        // 核心：只取用户名（String）放进 Token
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. 获取用户名：从 Token 里把用户名取出来
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 3. 验证密钥工具方法
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 1. 验证 Token 是否有效
     * 如果 Token 没过期且签名正确，返回 true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 2. 获取 Token 的过期时间
     * 直接从 Claims 中读取 expiration 属性
     */
    public Date getExpirationDate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}