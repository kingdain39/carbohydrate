package kr.co.carbohydrate.chat.client.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ChatPanel extends JPanel {

    private JTextPane chatArea;       // 채팅 내용 (색상 지원)
    private JTextField inputField;    // 입력창
    private JButton sendButton;       // 전송 버튼
    private JLabel userCountLabel;    // 접속자 수

    private Long myId;
    private String myName;

    public ChatPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 헤더
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("귓속말 채팅 프로그램");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        userCountLabel = new JLabel("사람 수: 0");
        userCountLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userCountLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 채팅 영역
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // === 하단: 입력 영역 ===
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        plusButton.setPreferredSize(new Dimension(40, 40));

        inputField = new JTextField();
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        sendButton = new JButton(">>전송");
        sendButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        sendButton.setPreferredSize(new Dimension(45, 40));
        sendButton.setBackground(new Color(30, 80, 60));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);

        inputPanel.add(plusButton, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // 이벤트 연결
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    // 서버 연결 (로그인 성공 후 호출됨)
    public void connectToServer(Long userId, String userName) {
        this.myId = userId;
        this.myName = userName;

        // TODO: 실제 서버 연결 로직만들어야함.
        appendMessage(myName + "님이 입장하셨습니다.", new Color(200, 50, 50), true);
    }

    private void sendMessage() {
        String content = inputField.getText().trim();
        if (content.isEmpty()) return;

        // TODO: 실제 서버로 전송로직 만들어야함
        appendMessage(myName + ": " + content, Color.BLACK, false);

        inputField.setText("");
    }

    // 채팅창에 메시지 추가 (색상 지정 가능)
    public void appendMessage(String message, Color color, boolean isSystem) {
        StyledDocument doc = chatArea.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);

        if (isSystem) {
            // 시스템 메시지는 입장 부분만 빨간색으로
        }

        try {
            doc.insertString(doc.getLength(), message + "\n\n", style);
            chatArea.setCaretPosition(doc.getLength());  // 스크롤 아래로
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // 접속자 수 업데이트
    public void updateUserCount(int count) {
        userCountLabel.setText("인원 수: " + count);
    }
}