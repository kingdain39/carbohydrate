package kr.co.carbohydrate.chat.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

// 사용자 정보 관리
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // [수정됨 1] Set(이름만) -> Map(이름 + ID)으로 변경
    // Thread-safe한 ConcurrentHashMap 사용
    private final Map<String, Long> activeUsers = new ConcurrentHashMap<>();

    // ID로 유저 찾기
    @Transactional
    public ChatUserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다: " + userId));
    }

    // [수정됨 2] 접속자 추가 로직 변경 (이름만 넣지 않고 ID도 같이 넣음)
    public void addActiveUser(String username) {
        // 1. DB에서 유저 정보를 조회해서 ID를 알아s내기
        ChatUserEntity user = findByUserName(username);

        // 2. 맵에 (이름, ID) 짝꿍으로 저장합니다.
        activeUsers.put(username, user.getId());

        System.out.println("★ 접속자 추가: " + username + " (ID: " + user.getId() + ")");
    }

    // 이름으로 조회
    @Transactional
    public ChatUserEntity findByUserName(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. Name: " + username));
    }

    // 여러 명 조회
    @Transactional
    public List<ChatUserEntity> findAllByIds(Set<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    // [수정됨 3] 접속 중인지 확인 (Map의 Key에 이름이 있는지 확인)
    public boolean isUserActive(String username) {
        return activeUsers.containsKey(username);
    }

    // [수정됨 4] 접속자 제거 (Map에서 삭제)
    public void removeActiveUser(String username) {
        activeUsers.remove(username);
        System.out.println("현재 접속자 제거: " + username);
    }

    // [수정됨 5] 접속자 명단 반환 (이제 Map을 반환함!)
    public Map<String, Long> getActiveUsers() {
        return activeUsers;
    }
}