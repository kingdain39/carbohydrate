package kr.co.carbohydrate.chat.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.repository.MessageRepository;
import kr.co.carbohydrate.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class UserService {
	private final UserRepository userRepository;
	
	// ID로 유저 찾기 (Controller에서 사용)
    @Transactional
    public ChatUserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다: " + userId));
    }
    //접속중인 유저 관리
	public void addActiveUser(ChatUserEntity user) {
		// TODO Auto-generated method stub
		//처리
		System.out.println("유저 접속 처리: " + user.getUserName());
	}


}