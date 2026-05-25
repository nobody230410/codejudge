package com.yanzy.codejudge.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private int id;
    private String username;
    private String email;
}
