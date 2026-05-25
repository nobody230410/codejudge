package com.yanzy.codejudge.service;

import com.yanzy.codejudge.vo.JudgeResult;

public interface JudgeService {
    public void judge(int problemId, String code, String language, int submissionId);
}
