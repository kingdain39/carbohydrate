package kr.co.carbohydrate.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import kr.co.carbohydrate.chat.dto.ChatMessageResponse;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.WhisperRequest;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.service.ChatService;
import kr.co.carbohydrate.chat.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController{

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessageSendingOperations messagingTemplate;

    
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendRequest request,
                            SimpMessageHeaderAccessor headerAccessor
    ){
        //senderId 덮어쓰기(클라이언트가 보낸값을 그대로 쓰지않고 세션에서 정확한 userId를 추출해서 서버로 넘기기위함) : 먼저 세션 userId추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        //request 의 senderId로 덮어씀.
        request.setSenderId(userId);
        //빌드하는거 싹 삭제하고 서비스 불러주미
        ChatMessageResponse response = chatService.savePublicMessage(request);//requst dto그대로 걍 넘겨줌.
        messagingTemplate.convertAndSend("/topic/public",response); //이거가 응답을 /topic/public을 구독중인 모두에게 브로드캐스팅하는거
    }

    @MessageMapping("/chat.whisper")
    public void sendWhisper(@Payload WhisperRequest request,
                            SimpMessageHeaderAccessor headerAccessor
    ) {

        //senderId 덮어쓰기(클라이언트가 보낸값을 그대로 쓰지않고 세션에서 정확한 userId를 추출해서 서버로 넘기기위함) : 먼저 세션 userId추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        //request 의 senderId로 덮어씀.
        request.setSenderId(userId);
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
    //입장처리 클라이언트가 /app/chat.join으로 메시지를 보내면 실행됨
    @MessageMapping("/chat.join")
    public void joinChat(SimpMessageHeaderAccessor headerAccessor){

        // 세션에서 userId 꺼내기 (StompHandler가 저장한 값)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        // 디비에서 유저 정보 조회
        ChatUserEntity user = userService.findById(userId);

        // 접속자명단에 해당사용자 추가!!!!
        userService.addActiveUser(user.getUserName());

        //세션에 username 저장 (퇴장 감지 때문에 필요한거 )
        headerAccessor.getSessionAttributes().put("username", user.getUserName());

        // 입장 처리(조인서비스호출)
        chatService.userJoin(user.getUserName());
    }
    
    


}
