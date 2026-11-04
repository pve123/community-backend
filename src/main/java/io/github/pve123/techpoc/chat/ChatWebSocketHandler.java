package io.github.pve123.techpoc.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 접속한 모든 사람이 한 방에서 이야기하는 익명 채팅입니다.
 * 한 사람이 보낸 메시지를 나머지 모두에게 그대로 전달합니다.
 *
 * <p>접속자 목록을 서버 메모리에 두므로 서버가 여러 대면 서로의 메시지를 보지 못합니다.
 * 여러 대로 늘리려면 Redis Pub/Sub 같은 중계가 필요합니다.
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        broadcast(new TextMessage(session.getId() + "님이 들어왔습니다."));
    }

    @Override
    protected void handleTextMessage(WebSocketSession sender, TextMessage message) {
        sessions.values().stream()
                .filter(session -> !session.getId().equals(sender.getId()))
                .forEach(session -> send(session, message));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        broadcast(new TextMessage(session.getId() + "님이 나갔습니다."));
    }

    private void broadcast(TextMessage message) {
        sessions.values().forEach(session -> send(session, message));
    }

    private void send(WebSocketSession session, TextMessage message) {
        try {
            // 한 세션에 여러 스레드가 동시에 쓰면 예외가 나므로 세션 단위로 묶습니다.
            synchronized (session) {
                session.sendMessage(message);
            }
        } catch (IOException e) {
            log.warn("메시지를 보내지 못했습니다. session={}", session.getId(), e);
        }
    }
}
