package com.yanzy.codejudge.service;

import com.yanzy.codejudge.dto.req.TestCaseUpdateRequest;
import com.yanzy.codejudge.vo.TestCaseVO;

import java.util.List;

public interface TestCaseService {

    List<Integer> createTestCase(List<TestCaseVO> list);

    void updateTestCase(int id, TestCaseUpdateRequest request);

    List<TestCaseVO> getTestCaseListByProblemId(int problemId);

    void deleteTestCaseById(int id);

    void deleteTestCasesByProblemId(int problemId);
}
