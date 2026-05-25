package com.yanzy.codejudge.service.listener;

import com.yanzy.codejudge.common.config.RabbitMQConfig;
import com.yanzy.codejudge.dto.req.EnvoyJudgeRequest;
import com.yanzy.codejudge.dto.req.JudgeRequest;
import com.yanzy.codejudge.dto.resp.JudgeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class JudgeRequestListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeRequestListener.class);

    private final WebClient webClient;
    private final RabbitTemplate rabbitTemplate;

    public JudgeRequestListener(WebClient.Builder webClientBuilder, @Value("${envoy.url}") String envoyUrl, RabbitTemplate rabbitTemplate) {
        this.webClient = webClientBuilder.baseUrl(envoyUrl).build();
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.JUDGE_REQUEST_QUEUE)
    public void run(JudgeRequest request) {
        int submissionId = request.getSubmissionId();
        try {
            EnvoyJudgeRequest body = EnvoyJudgeRequest.fromJudgeQueueMessage(request);
            JudgeResponse response = webClient.post()
                    .uri("/judge")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JudgeResponse.class)
                    .block();

            if (response == null || response.getVerdict() == null) {
                publishError(submissionId, "ERROR", "判题服务返回空结果");
                return;
            }
            response.setSubmissionId(submissionId);
            rabbitTemplate.convertAndSend(RabbitMQConfig.JUDGE_EXCHANGE, RabbitMQConfig.JUDGE_RESPONSE_ROUTING_KEY, response);
        } catch (WebClientResponseException e) {
            log.warn("判题服务 HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            String body = truncate(e.getResponseBodyAsString(), 2000);
            publishError(submissionId, "ERROR", "判题服务返回 " + e.getStatusCode() + (body.isEmpty() ? "" : ": " + body));
        } catch (Exception e) {
            log.error("判题调用异常 submissionId={}", submissionId, e);
            publishError(submissionId, "ERROR", e.getMessage() != null ? e.getMessage() : "判题调用异常");
        }
    }

    private void publishError(int submissionId, String status, String message) {
        JudgeResponse.Verdict verdict = new JudgeResponse.Verdict();
        verdict.setStatus(status);
        verdict.setTotalTime(0.0);
        verdict.setTotalMemory(0);
        verdict.setMessage(message);

        JudgeResponse response = new JudgeResponse();
        response.setSubmissionId(submissionId);
        response.setVerdict(verdict);

        rabbitTemplate.convertAndSend(RabbitMQConfig.JUDGE_EXCHANGE, RabbitMQConfig.JUDGE_RESPONSE_ROUTING_KEY, response);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() <= max ? t : t.substring(0, max) + "...";
    }
}
