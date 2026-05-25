package com.yanzy.codejudge.vo;

import com.yanzy.codejudge.pojo.Problem;
import com.yanzy.codejudge.pojo.Submission;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemDetailVO {

    private int problemId;

    private String difficulty;

    private String title;

    private String description;

    /** 判题时间上限（秒） */
    private Double timeLimit;

    /** 内存上限（KB） */
    private Integer memoryLimit;

    private String code;

    private String language;

    private String status;

    /** 判题失败时的提示等信息 */
    private String judgeMessage;

    private LocalDateTime submitTime;

    public void getSubmissionVO(Submission submission) {
        if (submission == null) {
            this.code = "";
            this.language = "cpp";
            this.status = "";
            this.judgeMessage = "";
            this.submitTime = null;
            return;
        }
        this.code = submission.getCode();
        this.language = submission.getLanguage();
        this.status = String.valueOf(submission.getStatus());
        this.judgeMessage = submission.getMessage() != null ? submission.getMessage() : "";
        this.submitTime = submission.getSubmitTime();
    }

    public void getProblemVO(Problem problem) {
        this.problemId = problem.getId();
        this.difficulty = String.valueOf(problem.getDifficulty());
        this.title = problem.getTitle();
        this.description = problem.getDescription();
        this.timeLimit = problem.getTimeLimit();
        this.memoryLimit = problem.getMemoryLimit();
    }
}
