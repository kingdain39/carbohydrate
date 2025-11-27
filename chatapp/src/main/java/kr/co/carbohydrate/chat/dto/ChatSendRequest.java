package kr.co.carbohydrate.chat.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class ChatSendRequest {
    private Long senderId;
    private String content;
}