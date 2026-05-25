package com.yanzy.codejudge.auth.model;

import org.springframework.security.core.userdetails.UserDetails;

public interface LoginUser extends UserDetails {
    int getId();

    String getUsername();

    String getUserType();

    String getPassword();

    /** 普通用户邮箱；管理员账号无邮箱时返回 null */
    default String getEmail() {
        return null;
    }
}
