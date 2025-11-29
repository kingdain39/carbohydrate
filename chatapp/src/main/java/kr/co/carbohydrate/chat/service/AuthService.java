package kr.co.carbohydrate.chat.service;

import kr.co.carbohydrate.chat.dto.*;
import kr.co.carbohydrate.chat.entity.ChatUserEntity;
import kr.co.carbohydrate.chat.jwt.JwtProvider;
import kr.co.carbohydrate.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    public UserResponse signup(SignupRequest req) {

        if (userRepository.existsByUserName(req.userName())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        ChatUserEntity user = new ChatUserEntity();
        user.setUserName(req.userName());
        user.setUserPassword(passwordEncoder.encode(req.password()));

        userRepository.save(user);

        return new UserResponse(user.getId(), user.getUserName(), null);
    }

    // 로그인
    public UserResponse login(LoginRequest req) {

        ChatUserEntity user = userRepository.findByUserName(req.userName())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        if (!passwordEncoder.matches(req.password(), user.getUserPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        String token = jwtProvider.createToken(user.getId(), user.getUserName());

        return new UserResponse(user.getId(), user.getUserName(), token);
    }

    // 인증된 유저 정보 읽기
    public UserResponse getMe(String authHeader) {

        Long userId = jwtProvider.getUserIdFromHeader(authHeader);

        ChatUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return new UserResponse(user.getId(), user.getUserName(), null);
    }

    // 닉네임 수정(userName 변경)
    public UserResponse updateMe(String authHeader, UpdateUserRequest req) {

        Long userId = jwtProvider.getUserIdFromHeader(authHeader);

        ChatUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        user.setUserName(req.userName());
        userRepository.save(user);

        return new UserResponse(user.getId(), user.getUserName(), null);
    }
}
