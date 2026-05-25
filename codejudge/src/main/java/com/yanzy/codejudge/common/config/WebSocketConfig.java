package com.yanzy.codejudge.common.config;

import com.yanzy.codejudge.common.util.CodeRunWebSocketHandler;
import com.yanzy.codejudge.common.util.SubmissionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // 开启 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    private final SubmissionWebSocketHandler submissionWebSocketHandler;
    private final CodeRunWebSocketHandler codeRunWebSocketHandler;

    public WebSocketConfig(SubmissionWebSocketHandler submissionWebSocketHandler,
                           CodeRunWebSocketHandler codeRunWebSocketHandler) {
        this.submissionWebSocketHandler = submissionWebSocketHandler;
        this.codeRunWebSocketHandler = codeRunWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(submissionWebSocketHandler, "/ws/submission/{submissionId}")
                .setAllowedOrigins("*");
        registry.addHandler(codeRunWebSocketHandler, "/ws/run/{runId}")
                .setAllowedOrigins("*");
    }
}