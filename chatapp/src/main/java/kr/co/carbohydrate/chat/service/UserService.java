package kr.co.carbohydrate.chat.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//사용자정보관리
@Service
@RequiredArgsConstructor 
public class UserService {
	private final UserRepository userRepository;
	// [상태 관리] 동시 접속자 명단 (Thread-safe)
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
		System.out.println("★ 접속자 추가: " + username + " (총 " + activeUsers.size() + "명)");
	}
	
	//접속중인 유저 관리(명단 제거)
		public void removeActiveUser(String username) {
			// TODO Auto-generated method stub
			activeUsers.remove(username);
	        System.out.println("현재 접속자 제거: " + username);
		}
	
	    //  접속자 명단 반환 (데이터 제공 책임)
	    public Set<String> getActiveUsers() {
	        return activeUsers;
	    }

}