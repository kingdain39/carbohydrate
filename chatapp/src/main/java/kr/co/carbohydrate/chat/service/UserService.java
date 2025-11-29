package kr.co.carbohydrate.chat.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.repository.MessageRepository;
import kr.co.carbohydrate.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//사용자정보관리
@Service
@RequiredArgsConstructor 
public class UserService {
	private final UserRepository userRepository;
	// [핵심] 동시 접속자 명단 (Thread-safe한 Set 사용)
    // 서버가 재부팅되면 초기화됩니다. (영구 저장이 필요하면 Redis 사용 권장)
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
    
	// ID로 유저 찾기 (Controller에서 사용)
    @Transactional
    public ChatUserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다: " + userId));
    }
    //접속중인 유저 관리(명단에 추가)
	public void addActiveUser(String username) {
		// TODO Auto-generated method stub
		activeUsers.add(username);
        System.out.println("현재 접속자 추가: " + username);
	}


}