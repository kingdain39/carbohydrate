package kr.co.carbohydrate.chat.service;
import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.dto.ChatMessageResponse;
import kr.co.carbohydrate.chat.dto.ChatSendRequest;
import kr.co.carbohydrate.chat.dto.WhisperRequest;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.entity.MessageEntity;
import kr.co.carbohydrate.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor 
@Transactional
public class ChatService {
	

	private final MessageRepository messageRepository;
	private final SimpMessageSendingOperations messagingTemplate;
	private final UserService userService;
	//1. 전체 채팅 처리 (DB에 저장, DTO반환)
		@Transactional
		public ChatMessageResponse savePublicMessage(ChatSendRequest request) {
			// 1. 보낸 사람 확인
			ChatUserEntity sender = userService.findById(request.getSenderId());
			
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
			ChatUserEntity sender = userService.findById(request.getSenderId());
			ChatUserEntity recipient = userService.findById(request.getRecipientId());
			
			//2. 받는 사람이 현재 접속 중인지 확인!
	        if (!userService.isUserActive(recipient.getUserName())) {
	            throw new IllegalArgumentException(recipient.getUserName() + "님은 현재 접속 중이 아닙니다.");
	        }
			
			// 3. DB 엔티티 생성 및 저장
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
	    
	    
	    
	    
	    /**
	     * 채팅 히스토리 로드
	     * 책임: 해당 유저가 볼 수 있는 모든 메시지를 조회하고, 
	     * ID로 저장된 보낸이/받는이 정보를 실제 이름(UserName)으로 변환하여 전송.
	     */
		public void loadHistory(String username) {
			// TODO Auto-generated method stub
			//1. 요청한 유저 정보 조회
			ChatUserEntity me = userService.findByUserName(username);
			// 2. DB에서 권한이 있는 모든 메시지 조회
	        List<MessageEntity> historyEntities = messageRepository.findHistoryByUserId(me.getId());
	        
	        if (historyEntities.isEmpty()) return;
	        
	        // 3. 메시지 리스트에 등장하는 모든 유저 ID 수집 (senderId, recipientId)
	        Set<Long> userIds = new HashSet<>();
	        for (MessageEntity msg : historyEntities) {
	            userIds.add(msg.getSenderId());
	            if (msg.getRecipientId() != null) {
	                userIds.add(msg.getRecipientId());
	            }
	        }
	        // 4. 수집된 ID들의 유저 정보를 '한 번의 쿼리'로 모두 조회
	        List<ChatUserEntity> users = userService.findAllByIds(userIds);
	        // 5. 조회된 유저 정보를 검색하기 쉬운 Map<ID, 이름> 형태로 변환
	        Map<Long, String> userNameMap = users.stream()
	                .collect(Collectors.toMap(ChatUserEntity::getId, ChatUserEntity::getUserName));
	        
	        // 6. Entity -> Response DTO 변환 (Map에서 이름 찾아 넣기)
	        List<ChatMessageResponse> responseList = historyEntities.stream()
	                .map(msg -> {
	                    // ID에 해당하는 이름 찾기 (없으면 알수없음 처리)
	                    String senderName = userNameMap.getOrDefault(msg.getSenderId(), "(알수없음)");
	                    String recipientName = (msg.getRecipientId() != null) 
	                            ? userNameMap.getOrDefault(msg.getRecipientId(), "(알수없음)") 
	                            : null;
	                    
	                    String type = (msg.getRecipientId() != null) ? "WHISPER" : "CHAT";

	                    return toResponse(msg, senderName, recipientName, type);
	                })
	                .collect(Collectors.toList());
	        
	        // 7. 해당 유저에게만 히스토리 리스트 전송
	        // 클라이언트는 /user/queue/history 를 구독하고 있어야 함
	        messagingTemplate.convertAndSendToUser(
	                username, 
	                "/queue/history", 
	                responseList
	        );
	    
		}
		
		//유저입장
		public void userJoin(String username) {
			loadHistory(username);  //지난 내역 불러오기 (개인)
			//입장알림(전체)
			ChatMessageResponse systemMsg = ChatMessageResponse.builder()
	                .senderName("[시스템]")
	                .content(username + "님이 입장하셨습니다.")
	                .type("JOIN")
	                .timestamp(LocalDateTime.now())
	                .build();
	     // 5. 전체 방송 (모두에게 알림)
	        messagingTemplate.convertAndSend("/topic/public",systemMsg);
	        
	     // 6. 갱신된 접속자 명단 방송 (전체)
	        // UserService에서 명단을 가져와서 '/topic/users' 구독자들에게 쏩니다.
	        messagingTemplate.convertAndSend("/topic/users", userService.getActiveUsers());
		}
		
		
		//유저 퇴장
		public void userExit(String username) {
			// (1) 퇴장 알림 방송
	        ChatMessageResponse systemMsg = ChatMessageResponse.builder()
	                .senderName("[시스템]")
	                .content(username + "님이 퇴장하셨습니다.")
	                .type("LEAVE")
	                .timestamp(LocalDateTime.now())
	                .build();
	        messagingTemplate.convertAndSend("/topic/public", systemMsg);

	        // (2) [핵심] 갱신된 접속자 명단 방송 (누군가 나갔으니 다시 쏴야 함)
	        messagingTemplate.convertAndSend("/topic/users", userService.getActiveUsers());
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
