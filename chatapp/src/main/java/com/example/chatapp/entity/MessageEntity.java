package com.example.chatapp.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 보내는 사용자 ID
    @Column(nullable = false)
    private Long senderId;

    // 받는 사용자 ID (일반 메시지는 null 가능)
    private Long recipientId;

    // 내용
    @Column(nullable = false, length = 500)
    private String content;

    // 보낸 시간
    @Column(nullable = false)
    private LocalDateTime sendAt = LocalDateTime.now();
}
