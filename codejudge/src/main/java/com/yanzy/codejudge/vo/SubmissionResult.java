package com.yanzy.codejudge.vo;

import lombok.Data;

@Data
public class SubmissionResult {

    private int problemId;

    private String code;

    private String language;

    private String status;

    private int execTime;

    private int execMemory;

    private String stderr;
}
