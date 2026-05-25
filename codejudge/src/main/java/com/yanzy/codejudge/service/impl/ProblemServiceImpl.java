package com.yanzy.codejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanzy.codejudge.mapper.ProblemMapper;
import com.yanzy.codejudge.mapper.SubmissionMapper;
import com.yanzy.codejudge.pojo.Problem;
import com.yanzy.codejudge.pojo.Submission;
import com.yanzy.codejudge.service.ProblemService;
import com.yanzy.codejudge.service.TestCaseService;
import com.yanzy.codejudge.vo.ProblemDetailVO;
import com.yanzy.codejudge.vo.ProblemInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ProblemServiceImpl implements ProblemService {

    private static final Logger log = LoggerFactory.getLogger(ProblemServiceImpl.class);

    private final ProblemMapper problemMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SubmissionMapper submissionMapper;
    private final TestCaseService testCaseService;
    private static final String PROBLEM_CACHE_KEY_PREFIX = "problem:id:";

    private static final String USER_PREFIX = "user:id:";

    public ProblemServiceImpl(ProblemMapper problemMapper, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, SubmissionMapper submissionMapper, TestCaseService testCaseService) {
        this.problemMapper = problemMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.submissionMapper = submissionMapper;
        this.testCaseService = testCaseService;
    }

    @Override
    public int createProblem(String title, String description, String difficulty, int creator, Double timeLimit, Integer memoryLimit) {
        Problem problem = new Problem();
        problem.setTitle(title != null ? title.trim() : "");
        problem.setDescription(description != null ? description : "");
        problem.setDifficulty(parseDifficulty(difficulty));
        problem.setCreator(creator);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);

        problemMapper.insert(problem);

        safeRedisDelete("problem:list");

        return problem.getId();
    }

    @Override
    public List<ProblemInfoVO> getProblemList() {
        String listKey = "problem:list";

        Object cacheObj = safeRedisGet(listKey);
        // 缓存命中
        if (cacheObj != null) {
            // 2. 使用 convertValue 安全转换，消除 unchecked cast 警告
            // 这里利用了 TypeReference 来保留泛型信息

            return objectMapper.convertValue(cacheObj,
                    new TypeReference<List<ProblemInfoVO>>() {});
        }

        // 缓存不命中
        List<ProblemInfoVO> dbList = problemMapper.getProblemList();
        if (dbList != null) {
            safeRedisSet(listKey, dbList, 10, TimeUnit.MINUTES);
        }
        return dbList;
    }

    @Override
    public ProblemDetailVO getProblemById(int userId, int problemId) {
        String key = PROBLEM_CACHE_KEY_PREFIX + problemId + USER_PREFIX + userId;

        ProblemDetailVO cacheProblem = (ProblemDetailVO) safeRedisGet(key);
        // 命中
        if (cacheProblem != null) {
            return cacheProblem;
        }
        // 未命中
        Problem problem = problemMapper.selectById(problemId);

        if (problem == null) { return null; }

        ProblemDetailVO problemDetailVO = new ProblemDetailVO();
        problemDetailVO.getProblemVO(problem);

        LambdaQueryWrapper<Submission> submissionQueryWrapper = new LambdaQueryWrapper<>();
        submissionQueryWrapper
                .eq(Submission::getUserId, userId)
                .eq(Submission::getProblemId, problemId)
                .orderByDesc(Submission::getId)
                .last("LIMIT 1");
        Submission submission = submissionMapper.selectOne(submissionQueryWrapper);
        problemDetailVO.getSubmissionVO(submission);

        safeRedisSet(key, problemDetailVO, 30, TimeUnit.MINUTES);

        return problemDetailVO;
    }

    @Override
    public void updateProblem(int problemId, String title, String description, String difficulty, Double timeLimit, Integer memoryLimit) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        problem.setTitle(title);
        problem.setDescription(description);
        problem.setDifficulty(parseDifficulty(difficulty));
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);
        problemMapper.updateById(problem);
        invalidateProblemCaches(problemId);
    }

    @Override
    public void invalidateProblemDetailCaches(int problemId) {
        invalidateProblemCaches(problemId);
    }

    @Override
    @Transactional
    public void deleteProblem(int problemId) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        submissionMapper.delete(new LambdaQueryWrapper<Submission>().eq(Submission::getProblemId, problemId));
        testCaseService.deleteTestCasesByProblemId(problemId);
        problemMapper.deleteById(problemId);
        invalidateProblemCaches(problemId);
    }

    private static Problem.diff parseDifficulty(String difficulty) {
        if (Objects.equals(difficulty, "EASY")) {
            return Problem.diff.EASY;
        }
        if (Objects.equals(difficulty, "MIDDLE")) {
            return Problem.diff.MIDDLE;
        }
        return Problem.diff.HARD;
    }

    private void invalidateProblemCaches(int problemId) {
        safeRedisDelete("problem:list");
        try {
            Set<String> keys = redisTemplate.keys(PROBLEM_CACHE_KEY_PREFIX + problemId + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis 清理题目缓存失败: {}", e.getMessage());
        }
    }

    private Object safeRedisGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过读缓存 key={}: {}", key, e.getMessage());
            return null;
        }
    }

    private void safeRedisSet(String key, Object value, long ttl, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, unit);
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过写缓存 key={}: {}", key, e.getMessage());
        }
    }

    private void safeRedisDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过删缓存 key={}: {}", key, e.getMessage());
        }
    }
}
