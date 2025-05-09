package com.firzzle.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @Class Name : KakaoLoginRequestDTO.java
 * @Description : 카카오 로그인 요청 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginRequestDTO {
    @NotBlank(message = "카카오 액세스 토큰은 필수 입력값입니다.")
    @Size(min = 10, message = "카카오 액세스 토큰은 최소 10자 이상이어야 합니다.")
    private String accessToken;
}