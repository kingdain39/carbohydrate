package kr.co.carbohydrate.chat.controller;

import kr.co.carbohydrate.chat.dto.ChatMessageResponse;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.JoinRequest;
import kr.co.carbohydrate.chat.dto.WhisperRequest;
import kr.co.carbohydrate.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController{

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendRequest request){
        //빌드하는거 싹 삭제하고 서비스 불러주미
        ChatMessageResponse response = chatService.savePublicMessage(request);//requst dto그대로 걍 넘겨줌.
        messagingTemplate.convertAndSend("/topic/public",response); //이거가 응답을 /topic/public을 구독중인 모두에게 브로드캐스팅하는거
    }


    @MessageMapping("/chat.whisper")
    public void sendWhisper(@Payload WhisperRequest request) {
        ChatMessageResponse response = chatService.saveWhisperMessage(request);

        messagingTemplate.convertAndSendToUser( // 특정유저의 /queue/whisper에다가만 브로드캐슽ㅇ
                response.getSenderName(),  //이거는 자기가 남에게 보낸 귓속말 메세지
                "/queue/whisper",
                response
        );
        messagingTemplate.convertAndSendToUser(
                response.getRecipientName(), //이건 수신자용 남으로부터 받은 귓속말메세지띄워주기
                "/queue/whisper",
                response
        );
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload JoinRequest request,
                         SimpMessageHeaderAccessor headerAccessor){
        ChatMessageResponse response = chatService.handleJoin(request, headerAccessor);
        messagingTemplate.convertAndSend("/topic/user",response);
    }





}
