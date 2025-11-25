package com.example.chatapp.service;

import com.example.chatapp.dto.ChatMessage;
import com.example.chatapp.entity.ChatUser;
import com.example.chatapp.entity.MessageEntity;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    // 현재 접속 중인 사용자 → 세션 매핑
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatService(UserRepository userRepository,
                       MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // 메세지 처리 (일반 / 귓속말 구분)
    public void handleMessage(WebSocketSession session, ChatMessage msg) throws Exception {

        // 귓속말 여부
        if (msg.getContent().startsWith("/w ")) {
            whisperMsgSend(session, msg);
            return;
        }

        // 일반 메시지 broadcast
        broadcast(msg);
    }

    // -----------------------------
    // 🔥 귓속말 송신 (아연 담당 핵심 기능)
    // -----------------------------
    private void whisperMsgSend(WebSocketSession senderSession, ChatMessage msg) throws Exception {

        String full = msg.getContent().substring(3); // "/w "
        String[] parts = full.split(" ", 2);

        if (parts.length < 2) {
            ChatMessage error = new ChatMessage(
                    "SYSTEM",
                    "SERVER",
                    "귓속말 사용법: /w [닉네임] [메시지]",
                    formatter.format(LocalDateTime.now())
            );
            sendToSession(senderSession, error);
            return;
        }

        String targetName = parts[0]; // 받는 사람
        String realMsg = parts[1];    // 메시지

        // 대상 세션 찾기
        WebSocketSession targetSession = userSessions.get(targetName);

        if (targetSession == null) {
            ChatMessage error = new ChatMessage(
                    "SYSTEM",
                    "SERVER",
                    "[" + targetName + "] 님은 접속 중이 아닙니다.",
                    formatter.format(LocalDateTime.now())
            );
            sendToSession(senderSession, error);
            return;
        }

        // 귓속말 메시지 객체 생성
        ChatMessage whisper = new ChatMessage(
                "WHISPER",
                msg.getSender(),
                realMsg,
                formatter.format(LocalDateTime.now())
        );

        // 보낸 사람 + 받는 사람에게만 전송
        sendToSession(targetSession, whisper);
        sendToSession(senderSession, whisper);

        // DB 저장
        saveMsgToDB(whisper, targetName);
    }

    // -----------------------------
    // 전체 Broadcast
    // -----------------------------
    private void broadcast(ChatMessage msg) throws Exception {
        msg.setSendAt(formatter.format(LocalDateTime.now()));

        String json = objectMapper.writeValueAsString(msg);

        for (WebSocketSession session : userSessions.values()) {
            session.sendMessage(new TextMessage(json));
        }

        // 일반 메시지도 DB 저장 (recipient = null)
        saveMsgToDB(msg, null);
    }

    // -----------------------------
    // 1:1 메시지 전송
    // -----------------------------
    private void sendToSession(WebSocketSession session, ChatMessage msg) throws Exception {
        String json = objectMapper.writeValueAsString(msg);
        session.sendMessage(new TextMessage(json));
    }

    // -----------------------------
    // DB 저장
    // -----------------------------
    public void saveMsgToDB(ChatMessage msg, String recipientName) {

        Optional<ChatUser> sender = userRepository.findByUserName(msg.getSender());
        if (sender.isEmpty()) return;

        Long senderId = sender.get().getId();
        Long recipientId = null;

        if (recipientName != null) {
            Optional<ChatUser> recipient = userRepository.findByUserName(recipientName);
            if (recipient.isPresent()) {
                recipientId = recipient.get().getId();
            }
        }

        MessageEntity entity = new MessageEntity();
        entity.setSenderId(senderId);
        entity.setRecipientId(recipientId);
        entity.setContent(msg.getContent());
        entity.setSendAt(LocalDateTime.now());

        messageRepository.save(entity);
    }

    // -----------------------------
    // 유저 입장
    // -----------------------------
    public void userEnter(String username, WebSocketSession session) {
        userSessions.put(username, session);
    }

    // -----------------------------
    // 유저 퇴장
    // -----------------------------
    public void userExit(WebSocketSession session) {
        userSessions.values().remove(session);
    }
}