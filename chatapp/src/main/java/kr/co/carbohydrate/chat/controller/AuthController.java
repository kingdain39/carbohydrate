package kr.co.carbohydrate.chat.controller;

import kr.co.carbohydrate.chat.dto.*;
import kr.co.carbohydrate.chat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public UserResponse signup(@RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserResponse getMe(@RequestHeader("Authorization") String authHeader) {
        return authService.getMe(authHeader);
    }

    @PutMapping("/me")
    public UserResponse updateMe(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateUserRequest req
    ) {
        return authService.updateMe(authHeader, req);
    }

    @PostMapping("/logout")
    public String logout() {
        return "logout ok";
    }
}
