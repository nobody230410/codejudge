package com.yanzy.codejudge.dto.resp;

import lombok.Data;

@Data
public class RegisterResponse {

    private int id;

    private String username;

    public RegisterResponse(int id, String username) {
        this.id = id;
        this.username = username;
    }
}
