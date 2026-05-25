package com.yanzy.codejudge.vo;

import com.yanzy.codejudge.dto.resp.JudgeResponse;
import lombok.Data;

@Data
public class JudgeResult {

    private String status;

    private double totalTime;

    private int totalMemory;

    private String message;

    public JudgeResult(JudgeResponse judgeResponse) {
        JudgeResponse.Verdict v = judgeResponse.getVerdict();
        if (v == null) {
            this.status = "ERROR";
            this.totalTime = 0d;
            this.totalMemory = 0;
            this.message = "判题结果格式异常";
            return;
        }
        this.status = v.getStatus();
        this.totalTime = v.getTotalTime() != null ? v.getTotalTime() : 0d;
        this.totalMemory = v.getTotalMemory() != null ? v.getTotalMemory() : 0;
        if ("WRONG_ANSWER".equals(this.status)) {
            this.message = "input: " + v.getInput() + ", expected output: " + v.getExpectedOutput()
                    + " but got output: " + v.getOutput();
        } else if (v.getMessage() != null && !v.getMessage().isBlank()) {
            this.message = v.getMessage();
        } else {
            this.message = "";
        }
    }
}
