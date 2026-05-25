package com.yanzy.codejudge.auth;

/**
 * 登录请求中的 userType 仅能通过 UsernamePasswordAuthenticationToken 间接传递，
 * UserDetailsService 接口无法接收额外参数，故在认证流程内使用 ThreadLocal。
 */
public final class LoginContext {

    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();

    private LoginContext() {
    }

    public static void setUserType(String userType) {
        USER_TYPE.set(userType);
    }

    public static String getUserType() {
        return USER_TYPE.get();
    }

    public static void clear() {
        USER_TYPE.remove();
    }
}
