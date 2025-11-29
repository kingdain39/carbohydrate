package kr.co.carbohydrate.chat.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class ChatSendRequest {
    private Long senderId;
    private String content;
}