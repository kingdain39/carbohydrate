package com.example.chatapp.service;
import com.example.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;


@Service
@RequiredArgsConstructor //final 필드 자동 주입
public class ChatService {
	
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final ObjectMapper objectMapper; // JSON 변환기
	
	//접속한 유저 관리 (메모리저장소)
	private static final Map<String , WebSocketSession> userSessions = new ConcurrentHashMap<>();
	
	//시간 포매터
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	
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
		
		
		//모두에게 메세지 전송 (Broadcast)
		private void broadcast(ChatMessage msg) throws IOException{
			//DB 저장 (전체 채팅은 recipient가 null)
			saveMsgToDB(msg, null);
			//현재 시간 문자열 만들기 ("10:05")
			String time = LocalDateTime.now().format(TIME_FORMATTER);
			
			//시간 붙이기
			msg.setContent(msg.getContent() + " | " + time);
			
			//현재 접속해 있는 사람들에게 전송
			for (WebSocketSession s : userSessions.values()) {
		        if (s.isOpen()) {
		        	sendToSession(s, msg);
		        }
		    }	
		}
		
		//보내기
		private void sendToSession(WebSocketSession targetSession,ChatMessage msg) {
			if(targetSession.isOpen()) {
				// 객체를 JSON 문자열로 변환
				try {
					String json = objectMapper.writeValueAsString(msg);
					targetSession.sendMessage(new TextMessage(json));
				} catch(IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
		//귓속말
		private void whisperMsgSend(WebSocketSession senderSession,ChatMessage msg) throws IOException{
			msg.setSend_At(LocalDateTime.now());
			String[] parts = msg.getContent().split(" ", 3);//메세지 자르기 ["/w", "예림", "안녕"]
			// 형식이 틀렸을 때
			if (parts.length < 3) {
				ChatMessage whisperfailMsg=new ChatMessage();
				whisperfailMsg.setSender("시스템");
				whisperfailMsg.setContent("형식 오류: /w [이름] [내용]");
				sendToSession(senderSession, whisperfailMsg);
				return;
			}
			//형식이 맞다면 보내기
			String targetName = parts[1];   // 받는 사람 (예림)
			String msgContent = parts[2];  // 진짜 내용 (안녕)
			
			WebSocketSession targetSession = userSessions.get(targetName);
			
			if(targetSession!=null&&targetSession.isOpen()) {
				
				String time = LocalDateTime.now().format(TIME_FORMATTER);
				msg.setContent(msgContent);
				saveMsgToDB(msg, targetName);
				
				//나한테 보내기
				ChatMessage sentMsg = new ChatMessage();
		        sentMsg.setSender(msg.getSender()); // 보낸 사람 (나)
		        sentMsg.setContent("(나->"+ targetName + "<보냄>): " + msgContent+ " | " + time);
		        sendToSession(senderSession, sentMsg);
		        
		        //받는사람에게 보내기
		        ChatMessage receivedMsg = new ChatMessage();
		        receivedMsg.setSender(msg.getSender()); // 보낸 사람 (나)
		        receivedMsg.setContent("("+msg.getSender()+"->"+ targetName + "<받음>): " + msgContent+ " | " + time);
		        sendToSession(targetSession, receivedMsg);
		        
			}
			else {
				//상대가 존재하지 않을 떄
				ChatMessage errorMsg = new ChatMessage();
				errorMsg.setSender("[ERROR]");
		        errorMsg.setContent(targetName + "님은 현재 접속 중이 아닙니다.");
		        sendToSession(senderSession, errorMsg);
			}
			
		}
		
		
		//메세지 로딩 (지난 대화 불러오기)
		private void loadingMsgs(WebSocketSession session, String username) {
			//1.내 정보 찾기
			ChatUser me = userRepository.findByUsername(username).orElse(null);
			if(me==null) return;
			
			//2. DB에서 내역 조회 id로 DB가져오기(userID를 통해 )
			List<MessageEntity> history = messageRepository.findHistoryByUserId(me.getId());
			
			for(MessageEntity entity:history) {
				ChatMessage msg = new ChatMessage();
				
				//3.보낸 사람 이름 찾기 (ID->이름)
				String senderName=userRepository.findById(entity.getSenderId()).map(ChatUser::getUserName).orElse("(알수없음)");
				msg.setSender(senderName);
				
				String time = entity.getSendAt().format(TIME_FORMATTER);
				
				//4. 내용 출력하기
				//전체 채팅인 경우
				if (entity.getRecipientId() == null || entity.getRecipientId() == 0) {
                    msg.setContent(entity.getContent());
                }
				//귓속말 채팅인 경우
				else {
					//1) 내가 보낸 귓속말인경우 (Sender ID가 내 ID와 같음)
					if(entity.getSenderId().equals(me.getId())) {
						//이름 찾아오기
						String recipientName=userRepository.findById(entity.getRecipientId()).map(ChatUser::getUserName).orElse("(알수없음)");
						
						msg.setContent("(나->"+ recipientName + "<보냄>): " + entity.getContent());
					}
					//2) 내가 받은 귓속말
					else {
						//이름 찾아오기
						String SendertName=userRepository.findById(entity.getSenderId()).map(ChatUser::getUserName).orElse("(알수없음)");
						msg.setContent("("+SendertName+"->"+  me.getName() + "<받음>): " + entity.getContent());
					}
					
				}
				msg.setContent(msg.getContent() + " | " + time);
				//5)나에게만 전송
				sendToSession(session, msg);
			}
			
			ChatMessage sysMsg = new ChatMessage();
            sysMsg.setSender("시스템");
            sysMsg.setContent("------------ 지난 대화 내용 ------------");
            sendToSession(session, sysMsg);
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
	
	
	
	//msg DB 저장
	@Transactional
	public void saveMsgToDB(ChatMessage msg,String recipientName) {
		try {
			// 시스템 메시지는 저장하지 않음 
			if ("시스템".equals(msg.getSender()) || "[시스템]".equals(msg.getSender())) {
					return;
				}
						
			// 보낸 사람 찾기
			ChatUser sender = userRepository.findByUsername(msg.getSender())
					.orElseThrow(() -> new RuntimeException("User not found"));
			
			Long recipientId = null;
			// 받는 사람 찾기 (귓속말일 때만)
			if (recipientName != null ) {
				ChatUser recipient = userRepository.findByUsername(recipientName).orElse(null);
				if (recipient != null) recipientId = recipient.getId();
			}
			
			// Entity 생성
			MessageEntity entity = new MessageEntity();
			entity.setSenderId(sender.getId());
			entity.setRecipientId(recipientId);
			entity.setContent(msg.getContent());
			entity.setSendAt(LocalDateTime.now());
			
			// 저장
			messageRepository.save(entity);
			
		} catch (Exception e) {
			System.out.println("DB 저장 실패: " + e.getMessage());
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
