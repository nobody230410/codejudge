package com.yanzy.codejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzy.codejudge.common.exception.BusinessException;
import com.yanzy.codejudge.common.exception.ErrorCodeEnum;
import com.yanzy.codejudge.common.util.MD5Util;
import com.yanzy.codejudge.dto.req.TestCaseUpdateRequest;
import com.yanzy.codejudge.mapper.TestCaseMapper;
import com.yanzy.codejudge.pojo.TestCase;
import com.yanzy.codejudge.service.TestCaseService;
import com.yanzy.codejudge.vo.TestCaseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseMapper testCaseMapper;

    public TestCaseServiceImpl(TestCaseMapper testCaseMapper) { this.testCaseMapper = testCaseMapper; }

    @Override
    @Transactional
    public List<Integer> createTestCase(List<TestCaseVO> list) {

        if(list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 将 VO 列表转换为 Entity 列表
        List<TestCase> entities = list.stream().map(vo -> {
            TestCase tc = new TestCase();
            tc.setInput(vo.getInput());
            tc.setOutput(vo.getOutput());
            tc.setProblemId(vo.getProblemId());
            tc.setInputHash(MD5Util.getMD5(vo.getInput()));
            return tc;
        }).toList();

        // 2. 一次性批量插入 (只交互一次数据库)
        testCaseMapper.batchInsert(entities);

        // 3. 收集 ID (MyBatis 批量插入后，entities 里的对象也会自动回填 ID)
        return entities.stream()
                .map(TestCase::getId)
                .toList();
    }

    @Override
    @Transactional
    public void updateTestCase(int id, TestCaseUpdateRequest request) {
        TestCase tc = testCaseMapper.selectById(id);
        if (tc == null) {
            throw new BusinessException(ErrorCodeEnum.PARAMS_ERROR, "测试用例不存在");
        }
        if (!tc.getProblemId().equals(request.getProblemId())) {
            throw new BusinessException(ErrorCodeEnum.PARAMS_ERROR, "测试用例不属于该题目");
        }
        String in = request.getInput() != null ? request.getInput() : "";
        String out = request.getOutput() != null ? request.getOutput() : "";
        tc.setInput(in);
        tc.setOutput(out);
        tc.setInputHash(MD5Util.getMD5(in));
        testCaseMapper.updateById(tc);
    }

    @Override
    public List<TestCaseVO> getTestCaseListByProblemId(int problemId) {

        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProblemId, problemId);

        List<TestCase> list = testCaseMapper.selectList(wrapper);
        return list.stream().map(testCase -> {
            TestCaseVO vo = new TestCaseVO();
            vo.setId(testCase.getId());
            vo.setInput(testCase.getInput());
            vo.setOutput(testCase.getOutput());
            vo.setProblemId(testCase.getProblemId());
            return vo;
        }).toList();
    }

    @Override
    public void deleteTestCaseById(int id) {
        testCaseMapper.deleteById(id);
    }

    @Override
    public void deleteTestCasesByProblemId(int problemId) {
        testCaseMapper.delete(new LambdaQueryWrapper<TestCase>().eq(TestCase::getProblemId, problemId));
    }
}
