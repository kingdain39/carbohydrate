@Controller
@RequiredArgsConstructor
public class ChatController{

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendRequest request){
        User sender = userService.findById(request.getSenderId()); //UserService 파일따로 만들어 둿길래 그냥 일케 둘게

        //빌드하는거 싹 삭제하고 서비스 불러주미
        ChatMessageResponse response = chatService.savePublicMessage(request);//requst dto그대로 걍 넘겨줌.
        messagingTemplate.convertAndSend("/topic/public",response); //이거가 응답을 /topic/public을 구독중인 모두에게 브로드캐스팅하는거
    }


    @MessageMapping("/chat.whisper")
    public void sendWhisper(@Payload WhisperRequest request) {
        User sender = userService.findById(request.getSenderId());
        User recipient = userService.findById(request.getRecipientId());

        messagingTemplate.convertAndSendToUser( // 특정유저의 /queue/whisper에다가만 브로드캐슽ㅇ
                sender.getUserName(),  //이거는 자기가 남에게 보낸 귓속말 메세지
                "/queue/whisper",
                response
        );
        messagingTemplate.convertAndSendToUser(
                recipient.getUserName(), //이건 수신자용 남으로부터 받은 귓속말메세지띄워주기
                "/queue/whisper",
                response
        );
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload JoinRequest request,
                         SimpMessageHeaderAccessor headerAccessor){
        User user = userService.findById(request.getUserId());
        ChatMessageResponse response = chatService.handleJoin(request, headerAccessor);
        messagingTemplate.convertAndSend("/topic/user",response);
    }





}
