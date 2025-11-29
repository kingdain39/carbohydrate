package kr.co.carbohydrate.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import kr.co.carbohydrate.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
	// 메시지를 직접 쏘는 도구 (Controller의 @SendTo 대신 씀)
	
	private ChatService chatService;
	
	//[퇴장 감지] 연결이 끊기면(Disconnect) 스프링이 자동으로 이 함수를 실행시킴
	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		//1. controller에서 저장한 소켓 이름으로 꺼내기
		String username=(String) headerAccessor.getSessionAttributes().get("username");
		
		if(username != null) {
            log.info("User Disconnected : " + username);
            
            // 2. 서비스에게 "이 사람 나갔어"라고 알려줍니다.
            chatService.userExit(username);
        }
		
		}
	}

