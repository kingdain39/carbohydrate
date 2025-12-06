package kr.co.carbohydrate.ui;

import kr.co.carbohydrate.ui.SignupFrame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField tfUserName;
    private JPasswordField pfPassword;
    private JButton btnLogin;
    private JButton btnSignup;

    public LoginFrame() {
        setTitle("로그인");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("아이디:"));
        tfUserName = new JTextField();
        panel.add(tfUserName);

        panel.add(new JLabel("비밀번호:"));
        pfPassword = new JPasswordField();
        panel.add(pfPassword);

        btnLogin = new JButton("로그인");
        panel.add(btnLogin);

        btnSignup = new JButton("회원가입");
        panel.add(btnSignup);

        add(panel);

        btnLogin.addActionListener(e -> doLogin());

        btnSignup.addActionListener(e -> {
            dispose();
            new SignupFrame();
        });

        setVisible(true);
    }

    private void doLogin() {
        try {
            String username = tfUserName.getText();
            String password = new String(pfPassword.getPassword());

            URL url = new URL("http://localhost:8080/api/v1/users/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String body = String.format("{\"userName\":\"%s\", \"password\":\"%s\"}", username, password);
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            int code = conn.getResponseCode();

            if (code == 200) {
                // JSON 읽기
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = br.readLine();

                JOptionPane.showMessageDialog(this, "로그인 성공!");

                dispose();

                // ChatFrame(token, username 등 전달 가능)
                new ChatFrame(username);
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "오류 발생: " + ex.getMessage());
        }
    }
}