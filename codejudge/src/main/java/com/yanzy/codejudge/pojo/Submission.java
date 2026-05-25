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
public class Submission {

    private Integer id;

    private int userId;

    private int problemId;

    private String code;

    private String language;

    private double execTime;       //unit: s

    private int execMemory;     //unit: kb

    private String message;

    private LocalDateTime submitTime;

    /** 与库中 status 及判题服务返回值一致，使用字符串避免与库枚举不一致导致映射失败 */
    private String status;

}
