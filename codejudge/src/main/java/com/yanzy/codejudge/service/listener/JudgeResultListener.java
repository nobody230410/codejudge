package com.yanzy.codejudge.service.listener;

import com.yanzy.codejudge.common.config.RabbitMQConfig;
import com.yanzy.codejudge.common.util.SubmissionWebSocketHandler;
import com.yanzy.codejudge.dto.resp.JudgeResponse;
import com.yanzy.codejudge.service.SubmissionService;
import com.yanzy.codejudge.vo.JudgeResult;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class JudgeResultListener {

    private final SubmissionService submissionService;
    private final SubmissionWebSocketHandler submissionWebSocketHandler;

    public JudgeResultListener(SubmissionService submissionService,
                               SubmissionWebSocketHandler submissionWebSocketHandler) {
        this.submissionService = submissionService;
        this.submissionWebSocketHandler = submissionWebSocketHandler;
    }

    @RabbitListener(queues = RabbitMQConfig.JUDGE_RESPONSE_QUEUE)
    public void getResult(JudgeResponse judgeResponse) {
        //TODO 持久化、传给webSocketServer，组装JudgeResult
        JudgeResult result = new JudgeResult(judgeResponse);
        submissionService.update(result, judgeResponse.getSubmissionId());
        submissionWebSocketHandler.pushJudgeResult(judgeResponse.getSubmissionId(), result);
    }
}
