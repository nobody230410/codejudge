package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.dto.req.CodeRunRequest;
import com.yanzy.codejudge.dto.resp.CodeRunAcceptedResponse;
import com.yanzy.codejudge.service.CodeRunService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/run")
public class RunController {

    private final CodeRunService codeRunService;

    public RunController(CodeRunService codeRunService) {
        this.codeRunService = codeRunService;
    }

    /**
     * 登录用户提交运行任务；立即返回 {@code runId}，结果经 {@code /ws/run/{runId}} 推送。
     */
    @PostMapping
    public CodeRunAcceptedResponse run(@RequestBody @Valid CodeRunRequest request) {
        return codeRunService.enqueue(request);
    }
}
