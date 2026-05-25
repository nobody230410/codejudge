package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.auth.service.AuthService;
import com.yanzy.codejudge.dto.req.LoginRequest;
import com.yanzy.codejudge.dto.req.RegisterRequest;
import com.yanzy.codejudge.dto.resp.LoginResponse;
import com.yanzy.codejudge.dto.resp.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {this.authService = authService;}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid RegisterRequest request) {return authService.register(request);}

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // 1. 从请求头中提取 Token
        // 假设前端在 Header 中存放 Token 的 Key 是 "Authorization"
//        String token = request.getHeader("Authorization");

        // 简单的非空校验
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 调用 Service 处理登出逻辑
        authService.logout(token);

        // 3. 返回 200 OK，无需返回任何数据
        return ResponseEntity.ok().build();
    }
}
