package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProblemCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题过长")
    private String title;

    /** 允许为空串；JSON 缺省时反序列化为 null，服务端再兜底 */
    @Size(max = 65535, message = "描述过长")
    private String description;

    @NotBlank(message = "难度不能为空")
    @Pattern(regexp = "EASY|MIDDLE|HARD", message = "难度须为 EASY / MIDDLE / HARD")
    private String difficulty;

    @NotNull(message = "创建者不能为空")
    @Positive(message = "创建者 id 无效")
    private Integer creator;

    /** 判题时间上限（秒），可选 */
    private Double timeLimit;

    /** 内存上限（KB），可选 */
    private Integer memoryLimit;
}
