package com.yanzy.codejudge.service.impl;

import com.yanzy.codejudge.common.config.RabbitMQConfig;
import com.yanzy.codejudge.dto.req.JudgeRequest;
import com.yanzy.codejudge.mapper.ProblemMapper;
import com.yanzy.codejudge.pojo.Problem;
import com.yanzy.codejudge.service.JudgeService;
import com.yanzy.codejudge.service.TestCaseService;
import com.yanzy.codejudge.vo.TestCaseVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JudgeServiceImpl implements JudgeService {

    private static final double DEFAULT_TIME_LIMIT_SEC = 10.0;
    private static final int DEFAULT_MEMORY_LIMIT_KB = 262_144;

    private final TestCaseService testCaseService;
    private final RabbitTemplate rabbitTemplate;
    private final ProblemMapper problemMapper;

    public JudgeServiceImpl(TestCaseService testCaseService, RabbitTemplate rabbitTemplate, ProblemMapper problemMapper) {
        this.testCaseService = testCaseService;
        this.rabbitTemplate = rabbitTemplate;
        this.problemMapper = problemMapper;
    }

    @Override
    public void judge(int problemId, String code, String language, int submissionId) {

        List<TestCaseVO> testCases = testCaseService.getTestCaseListByProblemId(problemId);

        Problem problem = problemMapper.selectById(problemId);
        double timeSec = DEFAULT_TIME_LIMIT_SEC;
        int memKb = DEFAULT_MEMORY_LIMIT_KB;
        if (problem != null) {
            if (problem.getTimeLimit() != null && problem.getTimeLimit() > 0) {
                timeSec = problem.getTimeLimit();
            }
            if (problem.getMemoryLimit() != null && problem.getMemoryLimit() > 0) {
                memKb = problem.getMemoryLimit();
            }
        }

        JudgeRequest judgeRequest = new JudgeRequest();
        judgeRequest.setCode(code);
        judgeRequest.setLanguage(language);
        judgeRequest.setTimeLimit(timeSec);
        judgeRequest.setMemoryLimit(memKb);
        judgeRequest.setSubmissionId(submissionId);
        List<JudgeRequest.Case> cases = new ArrayList<>();
        for (TestCaseVO testCase : testCases) {
            cases.add(new JudgeRequest.Case(testCase.getInput(), testCase.getOutput()));
        }
        judgeRequest.setTestCases(cases);

        rabbitTemplate.convertAndSend(RabbitMQConfig.JUDGE_EXCHANGE, RabbitMQConfig.JUDGE_REQUEST_ROUTING_KEY, judgeRequest);
    }
}
