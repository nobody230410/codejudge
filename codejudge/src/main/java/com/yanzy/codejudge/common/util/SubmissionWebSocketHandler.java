package com.yanzy.codejudge.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanzy.codejudge.vo.JudgeResult;
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
public class SubmissionWebSocketHandler extends TextWebSocketHandler {

    private final Map<Integer, WebSocketSession> sessionPool = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SubmissionWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String path = session.getUri().getPath();
        int submissionId = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
        sessionPool.put(submissionId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        for (Iterator<Map.Entry<Integer, WebSocketSession>> it = sessionPool.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, WebSocketSession> e = it.next();
            if (e.getValue().equals(session)) {
                it.remove();
                break;
            }
        }
    }

    public void pushJudgeResult(int submissionId, JudgeResult result) {
        WebSocketSession session = sessionPool.get(submissionId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
