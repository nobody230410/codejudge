package com.yanzy.codejudge.dto.resp;

import com.yanzy.codejudge.vo.TestCaseVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestCaseListResponse {

    private List<TestCaseVO> testCaseList;

    public TestCaseListResponse(List<TestCaseVO> testCaseList) {
        this.testCaseList = testCaseList == null ? new ArrayList<>() : new ArrayList<>(testCaseList);
    }
}
