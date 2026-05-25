package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.dto.resp.AdminSubmissionPageResponse;
import com.yanzy.codejudge.service.AdminSubmissionService;
import com.yanzy.codejudge.vo.AdminSubmissionDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/submissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubmissionController {

    private final AdminSubmissionService adminSubmissionService;

    public AdminSubmissionController(AdminSubmissionService adminSubmissionService) {
        this.adminSubmissionService = adminSubmissionService;
    }

    @GetMapping
    public AdminSubmissionPageResponse page(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer problemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int pageSize) {
        return adminSubmissionService.pageSubmissions(userId, username, problemId, page, pageSize);
    }

    @GetMapping("/{submissionId}")
    public AdminSubmissionDetailVO detail(@PathVariable int submissionId) {
        return adminSubmissionService.getSubmissionDetail(submissionId);
    }
}
