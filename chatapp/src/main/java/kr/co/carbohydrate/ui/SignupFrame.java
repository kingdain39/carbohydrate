package kr.co.carbohydrate.ui;


import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupFrame extends JFrame {

    private JTextField tfUserName;
    private JPasswordField pfPassword;
    private JButton btnSignup;
    private JButton btnGoLogin;

    public SignupFrame() {
        setTitle("회원가입");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("아이디:"));
        tfUserName = new JTextField();
        panel.add(tfUserName);

        panel.add(new JLabel("비밀번호:"));
        pfPassword = new JPasswordField();
        panel.add(pfPassword);

        btnSignup = new JButton("회원가입");
        panel.add(btnSignup);

        btnGoLogin = new JButton("로그인으로");
        panel.add(btnGoLogin);

        add(panel);

        // 회원가입 버튼 클릭
        btnSignup.addActionListener(e -> doSignup());

        // 로그인으로 이동
        btnGoLogin.addActionListener(e -> {
            dispose();
            new kr.co.carbohydrate.ui.LoginFrame();
        });

        setVisible(true);
    }

    private void doSignup() {
        try {
            String username = tfUserName.getText();
            String password = new String(pfPassword.getPassword());

            URL url = new URL("http://localhost:8080/api/v1/users/signup");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String body = String.format("{\"userName\":\"%s\", \"password\":\"%s\"}",
                    username, password);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            int code = conn.getResponseCode();

            if (code == 200) {
                JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인하세요.");
                dispose();
                new kr.co.carbohydrate.ui.LoginFrame();
            } else {
                JOptionPane.showMessageDialog(this, "회원가입 실패! 아이디 중복일 수 있음.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "오류 발생: " + ex.getMessage());
        }
    }
}