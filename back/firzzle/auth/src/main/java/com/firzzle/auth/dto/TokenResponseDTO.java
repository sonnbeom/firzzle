package com.firzzle.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Class Name : TokenResponseDTO.java
 * @Description : 토큰 응답 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
    private LocalDateTime issuedAt;
}