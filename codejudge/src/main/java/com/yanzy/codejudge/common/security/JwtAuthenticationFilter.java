package com.yanzy.codejudge.common.security;

import com.yanzy.codejudge.common.util.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    // 注意：你需要一个 UserDetailsService 来根据用户名加载用户详情（权限等）
    // 如果你还没有写，后面我会教你怎么写
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(TokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求头中获取 JWT 字符串
            String jwt = getJwtFromRequest(request);

            // 2. 如果存在 Token 且验证通过
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 3. 从 Token 中获取用户名
                String username = tokenProvider.getUsernameFromToken(jwt);

                // 4. 根据用户名加载用户权限信息 (UserDetails)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 创建认证令牌，告诉 Spring Security "这个人已经登录了"
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities() // 这里包含了从 Token 或数据库读取的权限列表
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. 将认证信息放入上下文，后续的业务代码就可以获取当前用户了
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证信息", ex);
        }

        // 7. 继续执行过滤器链（如果认证失败，后续的 Controller 会因为未认证而被拦截）
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     * 格式通常是：Authorization: Bearer <token>
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 截取 "Bearer " 后面的部分
        }
        return null;
    }
}