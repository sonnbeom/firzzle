package com.firzzle.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : AdminLoginRequestDTO.java
 * @Description : 관리자 로그인 요청 DTO
 * @author Firzzle
 * @since 2025. 5. 13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 로그인 요청")
public class AdminLoginRequestDTO {

    @NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "관리자 사용자명", example = "admin")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "관리자 비밀번호", example = "password")
    private String password;
}