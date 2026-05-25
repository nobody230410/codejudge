package com.yanzy.codejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanzy.codejudge.mapper.SubmissionMapper;
import com.yanzy.codejudge.pojo.Submission;
import com.yanzy.codejudge.service.JudgeService;
import com.yanzy.codejudge.service.ProblemService;
import com.yanzy.codejudge.service.SubmissionService;
import com.yanzy.codejudge.vo.JudgeResult;
import org.springframework.stereotype.Service;

@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    private final JudgeService judgeService;
    private final SubmissionMapper submissionMapper;
    private final ProblemService problemService;

    public SubmissionServiceImpl(JudgeService judgeService, SubmissionMapper submissionMapper, ProblemService problemService) {
        this.judgeService = judgeService;
        this.submissionMapper = submissionMapper;
        this.problemService = problemService;
    }

    @Override
    public int submit(String code, String language, int userId, int problemId) {
        // 1. 构建 Submission 对象（此时还是空的，没有结果）
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);
        submission.setLanguage(language);
        submission.setCode(code);
        submission.setStatus("waiting");
        // 2. 写入数据库，获取submission_id
        this.saveOrUpdate(submission);
        problemService.invalidateProblemDetailCaches(problemId);
        // 3. 调用判题
        judgeService.judge(problemId, code, language, submission.getId());

        return submission.getId();
    }

    @Override
    public void update(JudgeResult result, int submissionId) {
        //由监听器调用，更新数据库
        Submission submission = this.submissionMapper.selectById(submissionId);
        if (submission == null) {
            return;
        }

        LambdaUpdateWrapper<Submission> wrapper = new LambdaUpdateWrapper<>();

        wrapper.set(Submission::getStatus, result.getStatus())
                .set(Submission::getExecMemory, result.getTotalMemory())
                .set(Submission::getExecTime, result.getTotalTime())
                .set(Submission::getMessage, result.getMessage())
                .eq(Submission::getId, submissionId);

        this.update(wrapper);
    }


}
