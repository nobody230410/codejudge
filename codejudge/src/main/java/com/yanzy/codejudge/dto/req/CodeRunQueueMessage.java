package com.yanzy.codejudge.dto.req;

import lombok.Data;

/**
 * 代码运行请求队列消息（含 correlationId，用于同步等待结果）。
 */
@Data
public class CodeRunQueueMessage {

    private String correlationId;

    private String language;

    private String code;

    private String stdin;
}
