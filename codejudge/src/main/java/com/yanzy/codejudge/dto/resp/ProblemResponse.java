package com.yanzy.codejudge.dto.resp;

import com.yanzy.codejudge.vo.ProblemDetailVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemResponse {

    private int problemId;

    private String difficulty;

    private String title;

    private String description;

    private Double timeLimit;

    private Integer memoryLimit;

    private String code;

    private String language;

    private String status;

    private String judgeMessage;

    private LocalDateTime submitTime;

    public ProblemResponse(ProblemDetailVO problemDetailVO) {
        this.problemId = problemDetailVO.getProblemId();
        this.difficulty = problemDetailVO.getDifficulty();
        this.title = problemDetailVO.getTitle();
        this.description = problemDetailVO.getDescription();
        this.timeLimit = problemDetailVO.getTimeLimit();
        this.memoryLimit = problemDetailVO.getMemoryLimit();
        this.code = problemDetailVO.getCode();
        this.language = problemDetailVO.getLanguage();
        this.status = problemDetailVO.getStatus();
        this.judgeMessage = problemDetailVO.getJudgeMessage();
        this.submitTime = problemDetailVO.getSubmitTime();
    }
}
