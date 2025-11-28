package kr.co.carbohydrate.chat.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.dto.ChatMessage;
import kr.co.carbohydrate.chat.dto.ChatMessageResponse;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.WhisperRequest;
import kr.co.carbohydrate.chat.entity.ChatUser;
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
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;


@Service
@RequiredArgsConstructor //final 필드 자동 주입
@Transactional
public class ChatService {
	
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	
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
	        ChatUser sender = userRepository.findById(request.getSenderId())
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
			ChatUser sender = userRepository.findById(request.getSenderId())
	                .orElseThrow(() -> new IllegalArgumentException("보낸 사람이 존재하지 않습니다."));
	        ChatUser recipient = userRepository.findById(request.getRecipientId())
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
		
		
		//*ID로 유저 이름 찾기 (입장/퇴장 메시지 처리용)
		public String findUsername(Long userId) {
	        return userRepository.findById(userId)
	                .map(ChatUser::getUserName)
	                .orElse("(알수없음)");
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
		
		
		
		
	//회원가입. 성공하면 true반환
		@Transactional
		public boolean userRegister(String username,String password) {
				//유저가 있는지 확인
				if(userRepository.existsByUsername(username)) {
					System.out.println("존재하는 아이디 입니다.");
					return false;
				}
				else {
					ChatUser user=new ChatUser();
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
        ChatUser user = userRepository.findByUsername(username).orElse(null);
     // 2. 유저가 존재하고(null이 아님) && 비밀번호가 일치하는지 확인
		if(user != null && user.getUserPassword().equals(password)) {
			return true;//로그인 성공
		}
		else{
            return false; // 아이디가 없거나, 비밀번호가 틀림
        }
	}
	
	
	//채팅방 입장
		public void userEnter(String username,WebSocketSession userSession) {
			//채팅방 유저 목록에 저장!
			userSessions.put(username, userSession);
			//메세지 보내기! (입장알림방송)
			ChatMessage welcomeMsg=new ChatMessage();
			welcomeMsg.setSender("[시스템]");
			welcomeMsg.setContent(username + "님이 입장 하셨습니다.");
			loadingMsgs(userSession,username);
			try {
		        broadcast(welcomeMsg);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		//채팅방 퇴장
		public void userExit(String username) {
			//채팅방 유저 목록에서 뺴기
			userSessions.remove(username);
			//메세지 보내기! (입장알림방송)
			ChatMessage exitMsg=new ChatMessage();
			exitMsg.setSender("[시스템]");
			exitMsg.setContent(username + "님이 퇴장 하셨습니다.");
			exitMsg.setSend_At(LocalDateTime.now());
			try {
		        broadcast(exitMsg);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}

}
