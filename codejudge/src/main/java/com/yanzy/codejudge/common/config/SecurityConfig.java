package com.yanzy.codejudge.common.config;

import com.yanzy.codejudge.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启 @PreAuthorize 等注解的支持
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) { this.jwtAuthenticationFilter = jwtAuthenticationFilter; }

    /**
     * 核心配置方法
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF（前后端分离 + 无状态 Token 不需要 CSRF 保护）
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 设置会话管理策略为无状态
                // 这意味着 Spring Security 不会创建 HttpSession，也不会使用 JSESSIONID
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // --- 放行规则 ---
                        // 允许匿名访问登录接口
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // 先登录才能登出
                        .requestMatchers("/api/auth/logout").authenticated()
                        // 允许访问静态资源（如果有）
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // --- 权限规则 ---
                        // 只有拥有 'ADMIN' 角色的用户才能访问 /api/admin/**
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // --- 兜底规则 ---
                        // 所有其他请求都需要认证（即必须登录）
                        .anyRequest().authenticated()
                )

                // 4. 添加 JWT 过滤器
                // 将我们的过滤器放在 UsernamePasswordAuthenticationFilter 之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 暴露 AuthenticationManager Bean
     * 这个 Bean 通常在登录接口（AuthService）中需要用到，用来验证用户名密码
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
