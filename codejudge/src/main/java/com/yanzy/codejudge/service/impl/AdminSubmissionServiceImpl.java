package com.yanzy.codejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzy.codejudge.dto.resp.AdminSubmissionPageResponse;
import com.yanzy.codejudge.mapper.ProblemMapper;
import com.yanzy.codejudge.mapper.SubmissionMapper;
import com.yanzy.codejudge.mapper.UserMapper;
import com.yanzy.codejudge.pojo.Problem;
import com.yanzy.codejudge.pojo.Submission;
import com.yanzy.codejudge.pojo.User;
import com.yanzy.codejudge.service.AdminSubmissionService;
import com.yanzy.codejudge.vo.AdminSubmissionDetailVO;
import com.yanzy.codejudge.vo.AdminSubmissionListItemVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminSubmissionServiceImpl implements AdminSubmissionService {

    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;
    private final ProblemMapper problemMapper;

    public AdminSubmissionServiceImpl(SubmissionMapper submissionMapper, UserMapper userMapper, ProblemMapper problemMapper) {
        this.submissionMapper = submissionMapper;
        this.userMapper = userMapper;
        this.problemMapper = problemMapper;
    }

    @Override
    public AdminSubmissionPageResponse pageSubmissions(Integer userId, String username, Integer problemId, int page, int pageSize) {
        int p = Math.max(1, page);
        int ps = Math.min(Math.max(pageSize, 1), 100);

        LambdaQueryWrapper<Submission> qw = new LambdaQueryWrapper<>();
        if (problemId != null && problemId > 0) {
            qw.eq(Submission::getProblemId, problemId);
        }
        if (userId != null && userId > 0) {
            qw.eq(Submission::getUserId, userId);
        }
        if (username != null && !username.isBlank()) {
            List<User> users = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getUsername, username.trim()));
            Set<Integer> ids = users.stream()
                    .map(User::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (ids.isEmpty()) {
                return new AdminSubmissionPageResponse(List.of(), 0, p, ps);
            }
            qw.in(Submission::getUserId, ids);
        }

        long total = submissionMapper.selectCount(qw);
        int offset = (p - 1) * ps;
        qw.orderByDesc(Submission::getSubmitTime).orderByDesc(Submission::getId);
        qw.last("LIMIT " + offset + ", " + ps);
        List<Submission> records = submissionMapper.selectList(qw);
        if (records.isEmpty()) {
            return new AdminSubmissionPageResponse(List.of(), total, p, ps);
        }

        Set<Integer> uids = records.stream().mapToInt(Submission::getUserId).boxed().collect(Collectors.toSet());
        Set<Integer> pids = records.stream().mapToInt(Submission::getProblemId).boxed().collect(Collectors.toSet());

        Map<Integer, String> usernameById = new HashMap<>();
        if (!uids.isEmpty()) {
            List<User> userRows = userMapper.selectBatchIds(uids);
            for (User u : userRows) {
                if (u.getId() != null) {
                    usernameById.put(u.getId(), u.getUsername() != null ? u.getUsername() : "");
                }
            }
        }
        Map<Integer, String> titleByPid = new HashMap<>();
        if (!pids.isEmpty()) {
            List<Problem> problems = problemMapper.selectBatchIds(pids);
            for (Problem pr : problems) {
                if (pr.getId() != null) {
                    titleByPid.put(pr.getId(), pr.getTitle() != null ? pr.getTitle() : "");
                }
            }
        }

        List<AdminSubmissionListItemVO> vos = new ArrayList<>(records.size());
        for (Submission s : records) {
            vos.add(new AdminSubmissionListItemVO(
                    s.getId(),
                    s.getUserId(),
                    usernameById.getOrDefault(s.getUserId(), ""),
                    s.getProblemId(),
                    titleByPid.getOrDefault(s.getProblemId(), ""),
                    s.getLanguage() != null ? s.getLanguage() : "",
                    s.getStatus() != null ? s.getStatus() : "",
                    s.getSubmitTime()));
        }
        return new AdminSubmissionPageResponse(vos, total, p, ps);
    }

    @Override
    public AdminSubmissionDetailVO getSubmissionDetail(int submissionId) {
        Submission s = submissionMapper.selectById(submissionId);
        if (s == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在");
        }
        User user = userMapper.selectById(s.getUserId());
        Problem problem = problemMapper.selectById(s.getProblemId());
        return new AdminSubmissionDetailVO(
                s.getId(),
                s.getUserId(),
                user != null && user.getUsername() != null ? user.getUsername() : "",
                s.getProblemId(),
                problem != null && problem.getTitle() != null ? problem.getTitle() : "",
                s.getLanguage() != null ? s.getLanguage() : "",
                s.getCode() != null ? s.getCode() : "",
                s.getStatus() != null ? s.getStatus() : "",
                s.getMessage() != null ? s.getMessage() : "",
                s.getExecTime(),
                s.getExecMemory(),
                s.getSubmitTime());
    }
}
