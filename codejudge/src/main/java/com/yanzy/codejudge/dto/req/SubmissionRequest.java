package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SubmissionRequest {

    @NotBlank(message = "代码不能为空")
    private String code;

    @NotBlank(message = "语言不能为空")
    private String language;

    @Positive(message = "题目 id 无效")
    private int problemId;

}
