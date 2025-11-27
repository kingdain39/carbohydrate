package com.example.chatapp.handler;

import com.example.chatapp.dto.ChatMessage;
import com.example.chatapp.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // JSON → ChatMessage 변환
        ChatMessage msg = objectMapper.readValue(message.getPayload(), ChatMessage.class);

        // 비즈니스 로직은 전부 service에서 처리
        chatService.handleMessage(session, msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 접속 종료
        chatService.userExit(session);
    }
}

