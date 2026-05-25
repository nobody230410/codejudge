package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProblemUpdateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "难度不能为空")
    @Pattern(regexp = "EASY|MIDDLE|HARD", message = "难度须为 EASY / MIDDLE / HARD")
    private String difficulty;

    /** 判题时间上限（秒），可选 */
    private Double timeLimit;

    /** 内存上限（KB），可选 */
    private Integer memoryLimit;
}
