package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserChangePasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
