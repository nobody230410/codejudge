package com.yanzy.codejudge.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzy.codejudge.auth.LoginContext;
import com.yanzy.codejudge.auth.model.LoginUser;
import com.yanzy.codejudge.common.util.TokenProvider;
import com.yanzy.codejudge.common.exception.BusinessException;
import com.yanzy.codejudge.common.exception.ErrorCodeEnum;
import com.yanzy.codejudge.dto.req.LoginRequest;
import com.yanzy.codejudge.dto.req.RegisterRequest;
import com.yanzy.codejudge.dto.resp.LoginResponse;
import com.yanzy.codejudge.dto.resp.RegisterResponse;
import com.yanzy.codejudge.mapper.UserMapper;
import com.yanzy.codejudge.pojo.User;
import com.yanzy.codejudge.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务类
 * 负责编排登录流程：调度策略 -> 验证 -> 生成Token -> 组装响应
 */
@Service
public class AuthService {


    // 1. 核心：Spring Security 的认证管理器（代替了 StrategyFactory）
    private final AuthenticationManager authenticationManager;

    // 2. 生成 JWT 的工具类
    private final TokenProvider tokenProvider;

    // 3. 注册功能仍需要直接操作数据库
    private final UserMapper userMapper;

    // 4. 密码加密器（用于注册时加密）
    private final PasswordEncoder passwordEncoder;

    // 5. Redis（用于登出黑名单）
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       TokenProvider tokenProvider,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 核心登录方法
     */
    public LoginResponse login(@NotNull LoginRequest request) {
        LoginContext.setUserType(request.getUserType());
        try {
            return doLogin(request);
        } finally {
            LoginContext.clear();
        }
    }

    private LoginResponse doLogin(@NotNull LoginRequest request) {
        // --- 1. 构建认证令牌 ---
        // 这里只需要用户名和密码
        // Spring Security 会自动调用 CustomUserDetailsService 去查库
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        // --- 2. 执行认证 ---
        // 如果密码错误或用户不存在，这里会直接抛出异常（BadCredentialsException 等）
        // 不需要自己写 try-catch 查库逻辑了
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // --- 3. 获取登录用户信息 ---
        // 认证通过后，从上下文中获取 Principal
        // 因为 CustomUserDetailsService 返回的是 UserAdapter 或 AdminAdapter，它们都实现了 LoginUser
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // --- 4. 生成 Token ---
        String token = tokenProvider.generateToken(loginUser);

        // --- 5. 计算过期时间 ---
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        // --- 6. 组装响应 ---
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresAt(expiresAt);

        // 将 LoginUser 转换为前端需要的 VO
        response.setUserInfo(new UserInfoVO(loginUser));

        return response;
    }

    /**
     * 注册方法（保持不变，但建议使用 passwordEncoder）
     */
    public RegisterResponse register(@NotNull RegisterRequest req) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, req.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ErrorCodeEnum.USER_ALREADY_EXISTS);
        }

        // 2. 创建新用户对象
        User newUser = new User();
        newUser.setUsername(req.getUsername());

        // 3. 【重要】密码加密后再存储
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));

        // 4. 保存到数据库
        userMapper.insert(newUser);

        // 5. 构造并返回响应对象
        return new RegisterResponse(newUser.getId(), newUser.getUsername());
    }

    /**
     * 登出方法（保持不变）
     */
    public void logout(String token) {
        // 1. 去除前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 验证 Token 有效性
        if (!tokenProvider.validateToken(token)) {
            return;
        }

        // 3. 加入黑名单
        Date expiration = tokenProvider.getExpirationDate(token);
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();

        if (remainingMillis > 0) {
            String key = "token:blacklist:" + token;
            try {
                redisTemplate.opsForValue().set(key, "logout", remainingMillis, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {
                // Redis 不可用时跳过黑名单，避免登出接口 500
            }
        }
    }
}