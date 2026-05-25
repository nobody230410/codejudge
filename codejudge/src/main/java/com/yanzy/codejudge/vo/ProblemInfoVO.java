package com.yanzy.codejudge.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemInfoVO {

    private int problemId;

    private String difficulty;

    private String title;

    /** 创建时间，列表按此排序 */
    private LocalDateTime createdAt;

}
