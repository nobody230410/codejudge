package com.yanzy.codejudge.service;

import com.yanzy.codejudge.vo.JudgeResult;

public interface SubmissionService {

    public int submit(String code, String language, int userId, int problemId);

    public void update(JudgeResult result, int submissionId);
}
