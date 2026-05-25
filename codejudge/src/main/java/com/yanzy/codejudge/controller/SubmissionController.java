package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.auth.model.LoginUser;
import com.yanzy.codejudge.dto.req.SubmissionRequest;
import com.yanzy.codejudge.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping({"", "/"})
    public int submit(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid SubmissionRequest submissionRequest) {
        if ("ADMIN".equals(loginUser.getUserType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员无法提交代码；提交记录关联 user 表，请使用普通用户账号做题。");
        }
        return submissionService.submit(
                submissionRequest.getCode(),
                submissionRequest.getLanguage(),
                loginUser.getId(),
                submissionRequest.getProblemId());
    }
}
