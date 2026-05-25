package com.yanzy.codejudge.common.server;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{userId}") // 访问路径，例如 ws://localhost:8080/ws/1001
public class WebSocketServer {

    // 1. 用来存储所有在线用户的 Session
    // Key: userId (用户ID), Value: Session (会话对象)
    // 使用 ConcurrentHashMap 保证线程安全
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 连接建立成功时的回调
     * 前端连接成功后，会自动调用这个方法
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        // 把当前用户的连接存起来
        SESSION_POOL.put(userId, session);
        System.out.println("用户 " + userId + " 连接成功！当前在线人数: " + SESSION_POOL.size());
    }

    /**
     * 连接关闭时的回调
     * 前端断开连接（或浏览器关闭）后，会自动调用这个方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        // 把当前用户的连接移除
        SESSION_POOL.remove(userId);
        System.out.println("用户 " + userId + " 断开连接！当前在线人数: " + SESSION_POOL.size());
    }

    /**
     * 发生错误时的回调
     */
    // @OnError
    // public void onError(Session session, Throwable error) { ... }

    /**
     * 【核心方法】服务端主动推送消息给指定用户
     * 你的监听器里就是调用这个方法
     */
    public void sendToUser(String userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                // 异步发送消息，不会阻塞主线程
                session.getAsyncRemote().sendText(message);
                System.out.println("推送消息给用户 " + userId + ": " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("用户 " + userId + " 不在线，消息推送失败");
        }
    }
}
