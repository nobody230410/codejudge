package com.yanzy.codejudge.common.exception;

public enum ErrorCodeEnum {
    SUCCESS(200, "操作成功"),
    PARAMS_ERROR(400, "参数错误"),
    LOGIN_FAILED(401, "账号或密码错误"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),

    USER_ALREADY_EXISTS(20000, "用户名已被注册"),

    // 认证模块错误 (20000 - 29999)
    USER_NOT_FOUND(20001, "用户不存在"),
    EMAIL_ALREADY_IN_USE(20004, "该邮箱已被其他账号使用"),
    OLD_PASSWORD_INCORRECT(20005, "原密码错误"),
    TOKEN_EXPIRED(20002, "登录已过期"),
    ACCOUNT_DISABLED(20003, "账号已被禁用"),

    /** 转发 Envoy {@code POST /run} 失败 */
    CODE_RUN_UPSTREAM(20007, "代码运行服务调用失败");

    private final int code;
    private final String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
