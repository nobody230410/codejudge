package com.yanzy.codejudge.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class JudgeRequest {

    private String language;

    private String code;

    /** 秒，写入 Envoy options.timeLimit */
    private Double timeLimit;

    /** KB，写入 Envoy options.memoryLimit */
    private Integer memoryLimit;

    private List<Case> testCases;

    private int submissionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Case {

        private String input;

        private String expectedOutput;
    }
}