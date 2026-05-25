package com.yanzy.codejudge.dto.resp;

import lombok.Data;

/**
 * 代码运行结果队列消息。
 */
@Data
public class CodeRunQueueResult {

    private String correlationId;

    /** Envoy 成功时非空 */
    private CodeRunResponse response;

    /** 调用失败时的错误说明 */
    private String errorMessage;
}
