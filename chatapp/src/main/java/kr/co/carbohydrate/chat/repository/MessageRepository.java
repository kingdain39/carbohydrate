package kr.co.carbohydrate.chat.repository;


import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.co.carbohydrate.chat.entity.MessageEntity;

import java.awt.print.Pageable;
import java.util.List;
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

	@Query("SELECT m FROM MessageEntity m " +
	           "WHERE m.recipientId IS NULL " +
	           "OR m.senderId = :userId " +
	           "OR m.recipientId = :userId " +
	           "ORDER BY m.sendAt ASC")
	List<MessageEntity> findHistoryByUserId(@Param("userId") Long userId);

}
