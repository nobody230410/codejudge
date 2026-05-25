package com.yanzy.codejudge.service.impl;

import com.yanzy.codejudge.common.config.RabbitMQConfig;
import com.yanzy.codejudge.common.exception.BusinessException;
import com.yanzy.codejudge.common.exception.ErrorCodeEnum;
import com.yanzy.codejudge.dto.req.CodeRunQueueMessage;
import com.yanzy.codejudge.dto.req.CodeRunRequest;
import com.yanzy.codejudge.dto.resp.CodeRunAcceptedResponse;
import com.yanzy.codejudge.service.CodeRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CodeRunServiceImpl implements CodeRunService {

    private static final Logger log = LoggerFactory.getLogger(CodeRunServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;

    public CodeRunServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public CodeRunAcceptedResponse enqueue(CodeRunRequest request) {
        String runId = request.getRunId();
        if (runId == null || runId.isBlank()) {
            runId = UUID.randomUUID().toString();
        } else {
            runId = runId.trim();
        }

        CodeRunQueueMessage message = new CodeRunQueueMessage();
        message.setCorrelationId(runId);
        message.setLanguage(request.getLanguage().trim());
        message.setCode(request.getCode());
        message.setStdin(request.getStdin());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RUN_EXCHANGE, RabbitMQConfig.RUN_REQUEST_ROUTING_KEY, message);
        } catch (Exception e) {
            log.warn("代码运行入队失败 runId={}: {}", runId, e.toString());
            throw new BusinessException(ErrorCodeEnum.CODE_RUN_UPSTREAM, "代码运行请求入队失败");
        }
        return new CodeRunAcceptedResponse(runId);
    }
}
