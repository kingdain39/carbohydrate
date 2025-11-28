package kr.co.carbohydrate.chat.controller;

import org.apache.logging.log4j.message.Message;
import org.springframework.stereotype.Controller;

import kr.co.carbohydrate.chat.entity.ChatUser;
import kr.co.carbohydrate.chat.service.ChatService;
import kr.co.carbohydrate.chat.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController{

    private final ChatService messageService;
    private final UserService userService;
    private final SimpleMessagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendRequest request){
        ChatUser sender = userService.findById(request.getSenderId());

        Message message = messageService.saveMessage(
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

    @MessageMapping("/chat.join")
    public void joinChat(@Payload JoinRequest request,
                         SimpleMessageHeaderAccessor headerAccessor){
        User user = userService.findById(request.getUserId());


        headerAccessor.getSessionAttributes().put("userId", user.getId());
        headerAccessor.getSessionAttributes().put("userName", user.getUserName());

        userService.addActiveUser(user);
        SystemMessageResponse response = SystemMessageResponse.builder()
                .type("JOIN")
                .content(user.getUserName() + "님이 입장하셨습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/user",response);
    }


}
