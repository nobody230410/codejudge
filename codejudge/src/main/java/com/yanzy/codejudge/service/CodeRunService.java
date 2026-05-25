package com.yanzy.codejudge.service;

import com.yanzy.codejudge.dto.req.CodeRunRequest;
import com.yanzy.codejudge.dto.resp.CodeRunAcceptedResponse;

public interface CodeRunService {

    /** 入队运行任务；结果通过 {@code /ws/run/{runId}} 推送。 */
    CodeRunAcceptedResponse enqueue(CodeRunRequest request);
}
