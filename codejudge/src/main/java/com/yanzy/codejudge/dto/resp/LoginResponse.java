package com.yanzy.codejudge.dto.resp;

import com.yanzy.codejudge.vo.UserInfoVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {

    private String token;

    private String tokenType;

    private LocalDateTime expiresAt;

    private UserInfoVO userInfo;

}