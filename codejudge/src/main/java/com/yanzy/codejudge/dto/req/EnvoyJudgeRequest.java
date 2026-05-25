package com.yanzy.codejudge.dto.req;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Envoy POST /judge 请求体（camelCase，与官方 README 一致）。
 * limits 放在 {@code options} 内；队列消息里的 submissionId 不会发往 HTTP。
 */
@Data
public class EnvoyJudgeRequest {

    private String language;

    private String code;

    private List<Case> testCases;

    private Options options;

    @Data
    public static class Case {

        private String input;

        private String expectedOutput;
    }

    @Data
    public static class Options {

        /** 秒 */
        private Double timeLimit;

        /** KB */
        private Integer memoryLimit;
    }

    public static EnvoyJudgeRequest fromJudgeQueueMessage(JudgeRequest r) {
        EnvoyJudgeRequest out = new EnvoyJudgeRequest();
        out.setLanguage(r.getLanguage());
        out.setCode(r.getCode());
        List<Case> cases = new ArrayList<>();
        if (r.getTestCases() != null) {
            for (JudgeRequest.Case c : r.getTestCases()) {
                Case ec = new Case();
                ec.setInput(c.getInput());
                ec.setExpectedOutput(c.getExpectedOutput());
                cases.add(ec);
            }
        }
        out.setTestCases(cases);
        Options opts = new Options();
        opts.setTimeLimit(r.getTimeLimit());
        opts.setMemoryLimit(r.getMemoryLimit());
        out.setOptions(opts);
        return out;
    }
}
