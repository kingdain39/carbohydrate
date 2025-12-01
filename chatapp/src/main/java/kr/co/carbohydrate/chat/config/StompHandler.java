package kr.co.carbohydrate.chat.config;

import kr.co.carbohydrate.chat.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    // 메시지가 채널로 전송되기 전에(preSend) 낚아채서 검사
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 1. 메시지 껍데기 벗기기 (Header 접근용)
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 2. 이게 "연결 요청(CONNECT)" 메시지인지 확인
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 3. 헤더에서 'Authorization' (토큰) 꺼내기
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("CONNECT 요청 옴. 헤더 확인: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("토큰이 없습니다.");
                throw new RuntimeException("JWT 토큰이 없거나 유효하지 않습니다."); // 연결 거부됨
            }

            // 4. 토큰 검증 (JwtProvider 이용)
            try {
                // 토큰에서 userId 꺼내기 (위조되었거나 만료되면 여기서 에러 남)
                Long userId = jwtProvider.getUserIdFromHeader(authHeader);

                log.info("인증 성공! 접속자 ID: {}", userId);

                // (선택사항) 여기서 세션에 userId를 심어줄 수도 있음
                accessor.getSessionAttributes().put("userId", userId);

            } catch (Exception e) {
                log.error("인증 실패: {}", e.getMessage());
                throw new RuntimeException("인증 실패"); // 연결 거부됨
            }
        }

        // CONNECT가 아니거나 인증 통과하면 메시지 그대로 통과
        return message;
    }
}