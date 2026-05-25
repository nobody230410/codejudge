package com.yanzy.codejudge.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudgeResponse {

    private Verdict verdict;

    private List<Result> results;

    private int submissionId;

    @Data
    public static class Verdict {

        private String status;

        private Double totalTime;

        private Integer totalMemory;

        private String input;

        private String output;

        private String expectedOutput;

        /** 判题服务或本地组装的错误说明（非 WRONG_ANSWER 时也可展示） */
        private String message;
    }

    @Data
    public static class Result {

        private String status;

        private Double time;

        private Integer memory;

        private String input;

        private String output;

        private String expectedOutput;
    }
}