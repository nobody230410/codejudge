package com.yanzy.codejudge.dto.req;

import com.yanzy.codejudge.vo.TestCaseVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TestCaseCreateRequest {

    @NotNull(message = "测试用例列表不能为 null")
    private List<TestCaseVO> testCaseList;

}
