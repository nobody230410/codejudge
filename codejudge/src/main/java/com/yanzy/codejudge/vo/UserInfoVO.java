package com.yanzy.codejudge.vo;

import com.yanzy.codejudge.auth.model.LoginUser;
import lombok.Data;

@Data
public class UserInfoVO {

        private int id;

        private String username;

        private String userType;

        private String email;

        // 快捷构造方法：直接从 LoginUser 接口转换
        public UserInfoVO(LoginUser loginUser) {

            this.id = loginUser.getId();
            this.username = loginUser.getUsername();
            this.userType = loginUser.getUserType();
            this.email = loginUser.getEmail();

        }

}
