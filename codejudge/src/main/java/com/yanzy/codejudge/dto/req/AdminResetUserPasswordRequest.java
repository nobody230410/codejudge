package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetUserPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
