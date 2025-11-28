package kr.co.carbohydrate.chat.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.message.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import kr.co.carbohydrate.chat.dto.ChatMessageResponse;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.JoinRequest;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.service.ChatService;
import kr.co.carbohydrate.chat.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController{

    private ChatService chatService;
    private UserService userService;
    private SimpMessageSendingOperations messagingTemplate;

    
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendRequest request){
    	ChatUserEntity sender = userService.findById(request.getSenderId());

        Message message = chatService.saveMessage(
                sender,
                request.getContent(),
                null
        );


        ChatMessageResponse response = ChatMessageResponse.builder()
                .type("CHAT")
                .senderName(sender.getUserName())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.converAndSend("/topic/public",response);
    }


    @MessageMapping("/chat.whisper")
    public void sendWhisper(@Payload WhisperRequest request) {
        ChatUser sender = userService.findById(request.getSenderId());
        ChatUser recipient = userService.findById(request.getRecipieintId());


        Message message = messageService.saveMesseage(
                sender,
                request.getContent(),
                recipient
        );

        ChatMessageResponse response = ChatMessageResponse.builder()
                .type("WHISPER")
                .senderName(sender.getUserName())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .build();


        messagingTemplate.convertAndSendToUser(
                sender.getUserName(),
                "/queue/whisper",
                response
        );
        messagingTemplate.convertAndSendToUser(
                recipient.getUserName(),
                "/queue/whisper",
                response
        );
    }
    //입장처리 클라이언트가 /app/chat.join으로 메시지를 보내면 실행됨
    @MessageMapping("/chat.join")
    public void joinChat(@Payload JoinRequest request,
    		SimpMessageHeaderAccessor headerAccessor){
    	// 1. 유저 정보 조회
        ChatUserEntity user = userService.findById(request.getUserId());
        // 2. 웹소켓 세션에 사용자 이름 저장! (퇴장 감지용)
        // ★ 중요: 여기서 저장한 "userName"을 나중에 Listener에서 꺼내 씁니다.
        headerAccessor.getSessionAttributes().put("userName", user.getUserName());
        //3.서비스 호출
        chatService.userJoin(user.getUserName());
        
    }
    
    


}
