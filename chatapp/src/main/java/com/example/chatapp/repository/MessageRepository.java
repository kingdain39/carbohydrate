package com.example.chatapp.repository;


import com.example.chatapp.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 특정 사용자의 전체 대화 기록 불러오기
    @Query("SELECT m FROM MessageEntity m WHERE m.senderId = :myId OR m.recipientId = :myId ORDER BY m.sendAt ASC")
    List<MessageEntity> findHistoryByUserId(Long myId);
}
