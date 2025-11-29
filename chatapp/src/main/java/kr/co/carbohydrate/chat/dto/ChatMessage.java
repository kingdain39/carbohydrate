package kr.co.carbohydrate.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    // TALK, ENTER, WHISPER, SYSTEM 등
    private String type;

    // 보내는 사람 닉네임
    private String sender;

    // 실제 내용
    private String content;

    // 문자열 형태의 시간 (yyyy-MM-dd HH:mm:ss)
    private String sendAt;
}

