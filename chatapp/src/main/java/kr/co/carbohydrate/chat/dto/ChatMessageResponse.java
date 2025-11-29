package kr.co.carbohydrate.chat.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@NoArgsConstructor
public class ChatMessageResponse {

    private Long id;             // 메시지 고유 id (DB 저장된 번호)
    private String type;         // CHAT, JOIN, WHISPER (클라가 구분할 때 씀)
    private String senderName;   // 보낸 사람 이름 (id 대신 이름으로 바꿔서 주기)
    private String recipientName;// 귓속말일 때만 사용, 아니면 null
    private String content;
    private LocalDateTime timestamp; // 언제 보냈는지


}