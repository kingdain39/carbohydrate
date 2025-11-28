package kr.co.carbohydrate.chat.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.dto.ChatMessage;
import kr.co.carbohydrate.chat.dto.ChatMessageResponse;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.WhisperRequest;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.entity.MessageEntity;
import kr.co.carbohydrate.chat.repository.MessageRepository;
import kr.co.carbohydrate.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.awt.print.Pageable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.query.Page;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;


@Service
@RequiredArgsConstructor //final 필드 자동 주입
@Transactional
public class ChatService {
	
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private SimpMessageSendingOperations messagingTemplate;
	//시간 포매터
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	/*
	//메세지 분배 (Controller에서 호출)
		public void handleMessage(WebSocketSession session,ChatMessage msg) {
			String content = msg.getContent();
			//귓속말인지 판단
			if (content != null && content.startsWith("/w")) {
				whisperMsgSend(session, msg);
			}
			// 모두에게 보내는 메세지
			else {
				broadcast(msg);
				}
		}
		*/
	//어떤 메세지인지 판단할 필요 없다. 
	
	
		//1. 전체 채팅 처리 (DB에 저장, DTO반환)
		@Transactional
		public ChatMessageResponse savePublicMessage(ChatSendRequest request) {
			// 1. 보낸 사람 확인
	        ChatUserEntity sender = userRepository.findById(request.getSenderId())
	                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
			
	        //2. DB 엔티티 생성 및 저장
	        MessageEntity entity = new MessageEntity();
	        entity.setSenderId(sender.getId());
	        entity.setRecipientId(null); // 전체 채팅이므로 null
	        entity.setContent(request.getContent());
	        entity.setSendAt(LocalDateTime.now());
	        
	        messageRepository.save(entity);
	        
	     // 3. 응답용 DTO로 변환해서 반환 (Controller가 이걸 받아서 뿌림)
	        return toResponse(entity, sender.getUserName(), null, "CHAT");
	        
		}
		
		//2. 귓속말 저장 및 응답 DTO 반환
		@Transactional
		public ChatMessageResponse saveWhisperMessage(WhisperRequest request) {
			// 1. 보낸 사람 & 받는 사람 확인
			ChatUserEntity sender = userRepository.findById(request.getSenderId())
	                .orElseThrow(() -> new IllegalArgumentException("보낸 사람이 존재하지 않습니다."));
	        ChatUserEntity recipient = userRepository.findById(request.getRecipientId())
	                .orElseThrow(() -> new IllegalArgumentException("받는 사람이 존재하지 않습니다."));
	     // 2. DB 엔티티 생성 및 저장
	        MessageEntity entity = new MessageEntity();
	        entity.setSenderId(sender.getId());
	        entity.setRecipientId(recipient.getId()); // 받는 사람 ID 저장
	        entity.setContent(request.getContent());
	        entity.setSendAt(LocalDateTime.now());

	        messageRepository.save(entity);

	        // 3. 응답용 DTO 반환
	        return toResponse(entity, sender.getUserName(), recipient.getUserName(), "WHISPER");
		}
		
		
		// 엔티티 -> Response DTO 변환 로직 (중복 제거)
	    private ChatMessageResponse toResponse(MessageEntity entity, String senderName, String recipientName, String type) {
	        return ChatMessageResponse.builder()
	                .id(entity.getId())
	                .type(type)
	                .senderName(senderName)
	                .recipientName(recipientName)
	                .content(entity.getContent())
	                .timestamp(entity.getSendAt())
	                .build();
	    }
	    
	    
	    //전 메세지 로드
		public void loadHistory(String username) {
			// TODO Auto-generated method stub
			
		}
		
		//유저입장
		public void userJoin(String username) {
			loadHistory(username);  //지난 내역 불러오기
			ChatMessageResponse systemChatMessage=new ChatMessageResponse();
	        systemChatMessage.setSenderName("[시스템]"); // 보낸 사람 설정
	        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
	        systemChatMessage.setContent(username + "님이 입장하셨습니다. | " + time);
	     // 5. 전체 방송 (모두에게 알림)
	        messagingTemplate.convertAndSend("/topic/public",systemChatMessage);
		}
		
		//유저 퇴장
		public void userExit(String username) {
			ChatMessageResponse chatMessage = new ChatMessageResponse();
            chatMessage.setSenderName("[시스템]");
            String time = LocalDateTime.now().format(TIME_FORMATTER);
            chatMessage.setContent(username + "님이 퇴장하셨습니다. | " + time);
            
         // 3. 전체 방송
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
		}
		
		
		
		/*
		
	//회원가입. 성공하면 true반환
		@Transactional
		public boolean userRegister(String username,String password) {
				//유저가 있는지 확인
				if(userRepository.exists(username)) {
					System.out.println("존재하는 아이디 입니다.");
					return false;
				}
				else {
					ChatUserEntity user=new ChatUserEntity();
					user.setUserName(username);
					user.setUserPassword(password);
					user.setCreatedAt(LocalDateTime.now());
					
					userRepository.save(user);//DB에 저장
					
					System.out.println("회원가입 성공: " + username);
					return true; //성공!
				}
		}
		
	//로그인
	public boolean userLogin(String username,String password) {
		// 1. 아이디로 유저 정보 가져오기 (없으면 null 반환)
        ChatUserEntity user = userRepository.findByUsername(username).orElse(null);
     // 2. 유저가 존재하고(null이 아님) && 비밀번호가 일치하는지 확인
		if(user != null && user.getUserPassword().equals(password)) {
			return true;//로그인 성공
		}
		else{
            return false; // 아이디가 없거나, 비밀번호가 틀림
        }
	}
	*/
	
	
}
