package com.yanzy.codejudge.service;

import com.yanzy.codejudge.dto.resp.ProblemResponse;
import com.yanzy.codejudge.vo.ProblemDetailVO;
import com.yanzy.codejudge.vo.ProblemInfoVO;

import java.util.List;

public interface ProblemService {
    /**
     * @param title 包含题目信息的请求对象
     * @return 提交的题目id
     **/
    int createProblem(String title, String description, String difficulty, int creator, Double timeLimit, Integer memoryLimit);

    List<ProblemInfoVO> getProblemList();

    ProblemDetailVO getProblemById(int userId, int problemId);

    void updateProblem(int problemId, String title, String description, String difficulty, Double timeLimit, Integer memoryLimit);

    void deleteProblem(int problemId);

    /** 清除某题详情缓存（提交记录变更后调用，避免读到旧的 Redis 快照） */
    void invalidateProblemDetailCaches(int problemId);
}
