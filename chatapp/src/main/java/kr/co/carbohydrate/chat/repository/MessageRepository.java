package kr.co.carbohydrate.chat.repository;


import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kr.co.carbohydrate.chat.entity.MessageEntity;

import java.awt.print.Pageable;
import java.util.List;
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 특정 사용자의 전체 대화 기록 불러오기
    @Query("SELECT m FROM MessageEntity m WHERE m.senderId = :myId OR m.recipientId = :myId ORDER BY m.sendAt ASC")
    List<MessageEntity> findHistoryByUserId(Long myId);
    //message 조회하는데 필요한 기능
    @EntityGraph(attributePaths = {"sender", "recipient"}) 
    Slice<MessageEntity> findBy(Pageable pageable);
}
