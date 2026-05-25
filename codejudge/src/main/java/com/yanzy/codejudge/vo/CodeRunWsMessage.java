package com.yanzy.codejudge.vo;

import com.yanzy.codejudge.dto.resp.CodeRunResponse;
import lombok.Data;

/**
 * 代码运行 WebSocket 推送载荷。
 */
@Data
public class CodeRunWsMessage {

    private String output;

    private String status;

    private Double time;

    private Double memory;

    /** 非空表示运行失败（上游或业务错误） */
    private String error;

    public static CodeRunWsMessage fromResponse(CodeRunResponse response) {
        CodeRunWsMessage msg = new CodeRunWsMessage();
        msg.setOutput(response.getOutput());
        msg.setStatus(response.getStatus());
        msg.setTime(response.getTime());
        msg.setMemory(response.getMemory());
        return msg;
    }

    public static CodeRunWsMessage fromError(String error) {
        CodeRunWsMessage msg = new CodeRunWsMessage();
        msg.setError(error);
        return msg;
    }
}
