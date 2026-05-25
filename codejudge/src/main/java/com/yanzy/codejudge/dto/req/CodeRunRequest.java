package com.yanzy.codejudge.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 普通用户代码运行（转发 Envoy {@code POST /run}）。
 * 不传 {@code options.timeLimit} / {@code memoryLimit}，由判题服务默认限制。
 */
@Data
public class CodeRunRequest {

    @NotBlank(message = "language 不能为空")
    private String language;

    @NotBlank(message = "code 不能为空")
    private String code;

    /** 标准输入； Envoy 若支持 {@code stdin} 字段则一并转发 */
    private String stdin;

    /**
     * 可选。客户端预生成并先连接 {@code /ws/run/{runId}} 时传入，避免结果先于 WebSocket 到达。
     */
    private String runId;
}
