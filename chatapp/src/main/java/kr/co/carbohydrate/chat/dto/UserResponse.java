package kr.co.carbohydrate.chat.dto;

public record UserResponse(
        Long userId,
        String userName,
        String token
) {}
