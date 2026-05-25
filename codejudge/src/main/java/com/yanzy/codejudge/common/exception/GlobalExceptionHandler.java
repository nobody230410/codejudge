package com.yanzy.codejudge.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        ErrorCodeEnum errorCode = e.getErrorCode();
        log.warn("业务异常 code={} message={}", errorCode != null ? errorCode.getCode() : null, e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", errorCode != null ? errorCode.getCode() : 500);
        body.put("message", e.getMessage());
        body.put("data", null);
        return ResponseEntity.status(httpStatusFor(errorCode)).body(body);
    }

    private static HttpStatus httpStatusFor(ErrorCodeEnum ec) {
        if (ec == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (ec) {
            case PARAMS_ERROR, OLD_PASSWORD_INCORRECT -> HttpStatus.BAD_REQUEST;
            case LOGIN_FAILED, TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ACCOUNT_DISABLED -> HttpStatus.FORBIDDEN;
            case USER_ALREADY_EXISTS, EMAIL_ALREADY_IN_USE -> HttpStatus.CONFLICT;
            case CODE_RUN_UPSTREAM -> HttpStatus.BAD_GATEWAY;
            case SYSTEM_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case SUCCESS -> HttpStatus.OK;
        };
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        log.warn("登录凭证校验失败: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", "账号或密码错误");
        body.put("data", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        Map<String, Object> body = new HashMap<>();
        body.put("code", ErrorCodeEnum.PARAMS_ERROR.getCode());
        body.put("message", message);
        body.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("未捕获异常", e);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 500);
        body.put("message", "系统内部错误: " + e.getMessage());
        body.put("data", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
