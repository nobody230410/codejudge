package com.yanzy.codejudge.service.listener;

import com.yanzy.codejudge.common.config.RabbitMQConfig;
import com.yanzy.codejudge.common.util.CodeRunWebSocketHandler;
import com.yanzy.codejudge.dto.resp.CodeRunQueueResult;
import com.yanzy.codejudge.vo.CodeRunWsMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RunResultListener {

    private final CodeRunWebSocketHandler codeRunWebSocketHandler;

    public RunResultListener(CodeRunWebSocketHandler codeRunWebSocketHandler) {
        this.codeRunWebSocketHandler = codeRunWebSocketHandler;
    }

    @RabbitListener(queues = RabbitMQConfig.RUN_RESPONSE_QUEUE)
    public void handle(CodeRunQueueResult result) {
        String runId = result.getCorrelationId();
        if (runId == null || runId.isBlank()) {
            return;
        }
        String errorMessage = result.getErrorMessage();
        if (errorMessage != null && !errorMessage.isBlank()) {
            codeRunWebSocketHandler.pushRunResult(runId, CodeRunWsMessage.fromError(errorMessage));
            return;
        }
        if (result.getResponse() == null) {
            codeRunWebSocketHandler.pushRunResult(runId, CodeRunWsMessage.fromError("代码运行服务返回空响应"));
            return;
        }
        codeRunWebSocketHandler.pushRunResult(runId, CodeRunWsMessage.fromResponse(result.getResponse()));
    }
}
