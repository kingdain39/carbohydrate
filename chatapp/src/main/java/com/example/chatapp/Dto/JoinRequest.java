import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class JoinRequest {
    private Long userId;     // 입장한 사람 ID
    private String userName; // 입장한 사람 이름 (명세서에 있길래 추가)
}