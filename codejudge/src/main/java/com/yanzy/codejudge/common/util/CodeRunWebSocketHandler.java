package com.yanzy.codejudge.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanzy.codejudge.vo.CodeRunWsMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CodeRunWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessionPool = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public CodeRunWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String runId = extractRunId(session);
        if (runId != null && !runId.isBlank()) {
            sessionPool.put(runId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        for (Iterator<Map.Entry<String, WebSocketSession>> it = sessionPool.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, WebSocketSession> e = it.next();
            if (e.getValue().equals(session)) {
                it.remove();
                break;
            }
        }
    }

    public void pushRunResult(String runId, CodeRunWsMessage message) {
        WebSocketSession session = sessionPool.get(runId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            // ignore send failure; client may have disconnected
        }
    }

    private static String extractRunId(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
