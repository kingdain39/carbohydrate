package com.example.chatapp.repository;


import com.example.chatapp.entity.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ChatUser, Long> {

    // 닉네임으로 user 찾기
    Optional<ChatUser> findByUserName(String userName);

    // 닉네임 중복 체크
    boolean existsByUserName(String userName);
}

