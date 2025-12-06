package kr.co.carbohydrate.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.carbohydrate.chat.dto.ChatMessage;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.WhisperRequest;
import kr.co.carbohydrate.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 1) JSON → ChatMessage 변환
        ChatMessage msg = objectMapper.readValue(message.getPayload(), ChatMessage.class);

        // 2) type에 따라 분기 (클라이언트가 type 설정하여 보냄)
        switch (msg.getType()) {

            case "TALK": // 전체 메시지
                handlePublicMessage(msg);
                break;

            case "WHISPER": // 귓속말 메시지
                handleWhisperMessage(msg);
                break;

            case "ENTER": // 입장 메시지
                chatService.userJoin(msg.getSender());
                break;

            case "EXIT": // 퇴장 메시지
                chatService.userExit(msg.getSender());
                break;

            default:
                System.out.println("알 수 없는 메시지 타입: " + msg.getType());
        }
    }

    // -------------------------
    // 전체 메시지
    // -------------------------
    private void handlePublicMessage(ChatMessage msg) {

        // senderName → senderId로 변환 필요 (DB에서 userId 가져옴)
        // 클라이언트가 senderId를 안 보내는 구조라면, 별도 맵핑 필요.
        // 지금 구조에서는 sender(닉네임)에 해당하는 userId를 저장하고 있어야 함.

        // chatService.savePublicMessage(new ChatSendRequest(senderId, msg.getContent()));

        System.out.println("[전체메시지] sender=" + msg.getSender() + ", content=" + msg.getContent());
    }

    // -------------------------
    // 귓속말 메시지
    // -------------------------
    private void handleWhisperMessage(ChatMessage msg) {

        // 귓속말은 senderId + recipientId 필요
        // 지금 ChatMessage에는 recipientId 없음 → 프론트가 따로 보내야 함

        System.out.println("[귓속말] sender=" + msg.getSender() + " → msg=" + msg.getContent());
    }
}
