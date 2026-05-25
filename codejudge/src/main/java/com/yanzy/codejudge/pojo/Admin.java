package com.yanzy.codejudge.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Admin {

    private Integer id;

    private String username;

    private String email;

    private String password;

    private LocalDateTime createdAt;

}
