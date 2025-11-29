package kr.co.carbohydrate.chat.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class JoinRequest {
    private Long userId;     // 입장한 사람 id
}