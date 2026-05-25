package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "ADMIN|USER", message = "不支持的用户类型")
    private String userType;

}
