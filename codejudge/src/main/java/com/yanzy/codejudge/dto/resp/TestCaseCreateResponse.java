package com.yanzy.codejudge.dto.resp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestCaseCreateResponse {

    private List<Integer> testCaseList;

    public TestCaseCreateResponse(List<Integer> testCaseList) {

        this.testCaseList = testCaseList == null ? new ArrayList<>() : new ArrayList<>(testCaseList);

    }
}
