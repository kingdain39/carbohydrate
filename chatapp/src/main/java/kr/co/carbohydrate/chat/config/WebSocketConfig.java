package kr.co.carbohydrate.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;



@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

   

    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 연결 주소: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // (선택) 낮은 버전 브라우저 지원
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. 메시지 보낼 때 (SEND) 붙일 주소 접두사
        // 클라이언트가 "/app/chat.send"로 보내면 컨트롤러가 잡음
        registry.setApplicationDestinationPrefixes("/app");

        // 3. 메시지 구독할 때 (SUBSCRIBE) 붙일 주소 접두사
        // 서버가 "/topic/public"으로 쏘면 구독자들이 받음
        registry.enableSimpleBroker("/topic", "/queue"); 
    }

	
    
}
