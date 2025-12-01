package kr.co.carbohydrate.chat.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class WhisperRequest {
    private Long senderId;
    private Long recipientId;   // 누구에게 (★추가됨)
    private String content;     // 내용을

    public void setSenderId(Long userId) {
    }
}