package com.yanzy.codejudge.service.listener;

import com.yanzy.codejudge.common.config.RabbitMQConfig;
import com.yanzy.codejudge.dto.req.CodeRunQueueMessage;
import com.yanzy.codejudge.dto.resp.CodeRunQueueResult;
import com.yanzy.codejudge.dto.resp.CodeRunResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RunRequestListener {

    private static final Logger log = LoggerFactory.getLogger(RunRequestListener.class);

    private final WebClient webClient;
    private final RabbitTemplate rabbitTemplate;

    public RunRequestListener(WebClient.Builder webClientBuilder,
                              @Value("${envoy.url}") String envoyUrl,
                              RabbitTemplate rabbitTemplate) {
        this.webClient = webClientBuilder.baseUrl(envoyUrl).build();
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.RUN_REQUEST_QUEUE)
    public void handle(CodeRunQueueMessage message) {
        String correlationId = message.getCorrelationId();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("language", message.getLanguage().trim());
        body.put("code", message.getCode());
        String stdin = message.getStdin();
        if (stdin != null && !stdin.isEmpty()) {
            body.put("stdin", stdin);
        }

        try {
            CodeRunResponse resp = webClient.post()
                    .uri("/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(CodeRunResponse.class)
                    .timeout(Duration.ofSeconds(120))
                    .block(Duration.ofSeconds(125));
            if (resp == null) {
                publishError(correlationId, "代码运行服务返回空响应");
                return;
            }
            publishSuccess(correlationId, resp);
        } catch (WebClientResponseException e) {
            String detail = truncate(e.getResponseBodyAsString(), 800);
            log.warn("Envoy POST /run 失败 status={} body={}", e.getStatusCode().value(), detail);
            publishError(correlationId,
                    "代码运行服务返回错误 (" + e.getStatusCode().value() + "): " + detail);
        } catch (Exception e) {
            log.warn("Envoy POST /run 调用异常 correlationId={}: {}", correlationId, e.toString());
            publishError(correlationId, "代码运行服务不可用: " + truncate(e.getMessage(), 400));
        }
    }

    private void publishSuccess(String correlationId, CodeRunResponse response) {
        CodeRunQueueResult result = new CodeRunQueueResult();
        result.setCorrelationId(correlationId);
        result.setResponse(response);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RUN_EXCHANGE, RabbitMQConfig.RUN_RESPONSE_ROUTING_KEY, result);
    }

    private void publishError(String correlationId, String errorMessage) {
        CodeRunQueueResult result = new CodeRunQueueResult();
        result.setCorrelationId(correlationId);
        result.setErrorMessage(errorMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RUN_EXCHANGE, RabbitMQConfig.RUN_RESPONSE_ROUTING_KEY, result);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\n', ' ').trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
