package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.dto.req.TestCaseCreateRequest;
import com.yanzy.codejudge.dto.req.TestCaseUpdateRequest;
import com.yanzy.codejudge.dto.resp.TestCaseCreateResponse;
import com.yanzy.codejudge.dto.resp.TestCaseListResponse;
import com.yanzy.codejudge.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/testcase")
@PreAuthorize("hasRole('ADMIN')")
public class TestCaseController {
    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @PostMapping("/create")
    public TestCaseCreateResponse createTestCase(@RequestBody @Valid TestCaseCreateRequest request) {
        return new TestCaseCreateResponse(testCaseService.createTestCase(request.getTestCaseList()));
    }

    @PutMapping("/{id}")
    public void updateTestCase(@PathVariable int id, @RequestBody @Valid TestCaseUpdateRequest request) {
        testCaseService.updateTestCase(id, request);
    }

    @GetMapping("/problem/{problemId}")
    public TestCaseListResponse getTestCaseListByProblemId(@PathVariable int problemId) {
        return new TestCaseListResponse(testCaseService.getTestCaseListByProblemId(problemId));
    }

    @DeleteMapping("/{id}")
    public void deleteTestCaseById(@PathVariable int id) {
        testCaseService.deleteTestCaseById(id);
    }
}
