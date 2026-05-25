package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.auth.model.LoginUser;
import com.yanzy.codejudge.dto.req.ProblemCreateRequest;
import com.yanzy.codejudge.dto.req.ProblemUpdateRequest;
import com.yanzy.codejudge.dto.resp.ProblemListResponse;
import com.yanzy.codejudge.dto.resp.ProblemResponse;
import com.yanzy.codejudge.service.ProblemService;
import com.yanzy.codejudge.vo.ProblemDetailVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {
    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public int createProblem(@RequestBody @Valid ProblemCreateRequest request) {
        return problemService.createProblem(
                request.getTitle(),
                request.getDescription(),
                request.getDifficulty(),
                request.getCreator(),
                request.getTimeLimit(),
                request.getMemoryLimit());
    }

    @GetMapping("/list")
    public ProblemListResponse getProblemList() {
        return new ProblemListResponse(problemService.getProblemList());
    }

    @GetMapping("/{problemId}")
    public ProblemResponse getProblemById(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable int problemId) {
        ProblemDetailVO detail = problemService.getProblemById(loginUser.getId(), problemId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        return new ProblemResponse(detail);
    }

    @PutMapping("/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateProblem(@PathVariable int problemId, @RequestBody @Valid ProblemUpdateRequest request) {
        problemService.updateProblem(
                problemId,
                request.getTitle(),
                request.getDescription(),
                request.getDifficulty(),
                request.getTimeLimit(),
                request.getMemoryLimit());
    }

    @DeleteMapping("/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProblem(@PathVariable int problemId) {
        problemService.deleteProblem(problemId);
    }
}
