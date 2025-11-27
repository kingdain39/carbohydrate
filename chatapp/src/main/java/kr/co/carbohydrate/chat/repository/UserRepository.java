package kr.co.carbohydrate.chat.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.carbohydrate.chat.entity.ChatUser;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<ChatUser, Long> {

    // 닉네임으로 user 찾기
    Optional<ChatUser> findByUserName(String userName);

    // 닉네임 중복 체크
    boolean existsByUserName(String userName);
}

