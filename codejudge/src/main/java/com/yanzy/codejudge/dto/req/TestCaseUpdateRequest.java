package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestCaseUpdateRequest {

    /** 用于校验该用例是否属于本题 */
    @NotNull(message = "problemId 不能为空")
    private Integer problemId;

    private String input;

    private String output;
}
