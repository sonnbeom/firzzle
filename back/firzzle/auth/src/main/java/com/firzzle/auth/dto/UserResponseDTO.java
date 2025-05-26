package com.firzzle.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정보 응답 DTO")
public class UserResponseDTO {

    @Schema(description = "사용자 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String uuid;

    @Schema(description = "사용자명", example = "firzzle_user")
    private String username;

    @Schema(description = "이메일", example = "user@firzzle.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "역할", example = "user")
    private String role;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "마지막 로그인 시간")
    private String lastLogin;

    @Schema(description = "가입 유형", example = "O")
    private String signupType;
}