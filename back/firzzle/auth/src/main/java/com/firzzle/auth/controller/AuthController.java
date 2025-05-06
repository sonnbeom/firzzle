package com.firzzle.auth.controller;

import com.firzzle.auth.dto.KakaoLoginRequestDTO;
import com.firzzle.auth.dto.TokenRequestDTO;
import com.firzzle.auth.dto.TokenResponseDTO;
import com.firzzle.auth.service.AuthService;
import com.firzzle.auth.service.OAuthRedirectService;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Class Name : AuthController.java
 * @Description : 인증 관련 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "로그인 및 토큰 관리 관련 API")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    // OAuthRedirectService 주입 추가
    private final OAuthRedirectService oAuthRedirectService;

    @PostMapping(value = "/login/kakao", produces = "application/json;charset=UTF-8")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인을 통해 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<TokenResponseDTO>> kakaoLogin(
            @Parameter(description = "카카오 로그인 정보", required = true) @Valid @RequestBody KakaoLoginRequestDTO loginRequest,
            HttpServletRequest request) {

        logger.info("카카오 로그인 요청 - 액세스 토큰 길이: {}", loginRequest.getAccessToken().length());

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("accessToken", loginRequest.getAccessToken());

            DataBox tokenDataBox = authService.kakaoLogin(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            Response<TokenResponseDTO> response = Response.<TokenResponseDTO>builder()
                    .status(Status.OK)
                    .message("카카오 로그인 성공")
                    .data(tokenResponseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("카카오 로그인 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("카카오 로그인 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 로그인 중 오류가 발생했습니다.");
        }
    }

    @PostMapping(value = "/refresh", produces = "application/json;charset=UTF-8")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<TokenResponseDTO>> refresh(
            @Parameter(description = "리프레시 토큰 정보", required = true) @Valid @RequestBody TokenRequestDTO tokenRequest,
            HttpServletRequest request) {

        logger.info("토큰 갱신 요청");

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("refreshToken", tokenRequest.getRefreshToken());

            DataBox tokenDataBox = authService.refreshToken(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            Response<TokenResponseDTO> response = Response.<TokenResponseDTO>builder()
                    .status(Status.OK)
                    .message("토큰 갱신 성공")
                    .data(tokenResponseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("토큰 갱신 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("토큰 갱신 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다.");
        }
    }

    @PostMapping(value = "/logout", produces = "application/json;charset=UTF-8")
    @Operation(summary = "로그아웃", description = "토큰을 무효화하고 로그아웃 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<Void>> logout(
            @Parameter(description = "리프레시 토큰 정보", required = true) @Valid @RequestBody TokenRequestDTO tokenRequest,
            HttpServletRequest request) {

        logger.info("로그아웃 요청");

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("refreshToken", tokenRequest.getRefreshToken());

            authService.logout(box);

            Response<Void> response = Response.<Void>builder()
                    .status(Status.OK)
                    .message("로그아웃 성공")
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("로그아웃 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("로그아웃 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "로그아웃 중 오류가 발생했습니다.");
        }
    }

    /**
     * 카카오 로그인 콜백 메서드 - 기존 메서드는 주석 처리
     */
    /*
    @GetMapping(value = "/kakao/callback", produces = "application/json;charset=UTF-8")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 로그인 후 리다이렉트되는 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 콜백 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<TokenResponseDTO>> kakaoCallback(HttpServletRequest request) {
        logger.info("카카오 로그인 콜백 요청");

        try {
            RequestBox box = RequestManager.getBox(request);
            String code = box.getString("code");

            if (code.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "인증 코드가 없습니다.");
            }

            DataBox tokenDataBox = authService.processKakaoCallback(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            Response<TokenResponseDTO> response = Response.<TokenResponseDTO>builder()
                    .status(Status.OK)
                    .message("카카오 로그인 성공")
                    .data(tokenResponseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("카카오 콜백 처리 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("카카오 콜백 처리 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 콜백 처리 중 오류가 발생했습니다.");
        }
    }
    */

    /*
            @GetMapping(value = "/kakao/callback", produces = "application/json;charset=UTF-8")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 로그인 후 리다이렉트되는 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 콜백 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<Map<String, Object>>> kakaoCallback(HttpServletRequest request) {
        logger.info("카카오 로그인 콜백 요청");

        try {
            RequestBox box = RequestManager.getBox(request);
            String code = box.getString("code");

            if (code.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "인증 코드가 없습니다.");
            }

            DataBox tokenDataBox = authService.processKakaoCallback(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            // 응답 형식 변경 - Map으로 변환하여 더 많은 정보 포함
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", tokenResponseDTO);
            responseData.put("user", createUserInfo(tokenDataBox));
            responseData.put("timestamp", System.currentTimeMillis());

            Response<Map<String, Object>> response = Response.<Map<String, Object>>builder()
                    .status(Status.OK)
                    .message("카카오 로그인 성공")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("카카오 콜백 처리 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("카카오 콜백 처리 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 콜백 처리 중 오류가 발생했습니다.");
        }
    }
    */

    /**
     * 카카오 로그인 콜백 메서드 - 리다이렉트 처리 추가
     */
    @GetMapping(value = "/kakao/callback", produces = "application/json;charset=UTF-8")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 로그인 후 리다이렉트되는 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 콜백 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public void kakaoCallback(HttpServletRequest request, HttpServletResponse response) {
        logger.info("카카오 로그인 콜백 요청");

        try {
            RequestBox box = RequestManager.getBox(request);
            String code = box.getString("code");

            if (code.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "인증 코드가 없습니다.");
            }

            // OAuth 처리 및 토큰 발급
            DataBox tokenDataBox = authService.processKakaoCallback(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            // 클라이언트 리다이렉트 URI 결정 (요청 출처에 따라 동적으로)
            String clientRedirectUri = oAuthRedirectService.determineClientRedirectUri();

            // 토큰을 쿼리 파라미터로 추가하여 클라이언트로 리다이렉트
            String redirectUrl = clientRedirectUri +
                    "?accessToken=" + tokenResponseDTO.getAccessToken() +
                    "&refreshToken=" + tokenResponseDTO.getRefreshToken() +
                    "&expiresIn=" + tokenResponseDTO.getExpiresIn();

            logger.info("클라이언트 리다이렉트 URI: {}", clientRedirectUri);

            // 클라이언트로 리다이렉트
            response.sendRedirect(redirectUrl);
        } catch (BusinessException e) {
            logger.error("카카오 콜백 처리 중 비즈니스 예외 발생: {}", e.getMessage());
            try {
                // 에러 정보와 함께 클라이언트로 리다이렉트
                String errorRedirectUrl = oAuthRedirectService.determineClientRedirectUri() +
                        "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
                response.sendRedirect(errorRedirectUrl);
            } catch (Exception ex) {
                logger.error("에러 리다이렉트 중 오류 발생: {}", ex.getMessage(), ex);
            }
        } catch (Exception e) {
            logger.error("카카오 콜백 처리 중 예외 발생: {}", e.getMessage(), e);
            try {
                // 에러 정보와 함께 클라이언트로 리다이렉트
                String errorRedirectUrl = oAuthRedirectService.determineClientRedirectUri() +
                        "?error=" + URLEncoder.encode("카카오 로그인 중 오류가 발생했습니다.", StandardCharsets.UTF_8.toString());
                response.sendRedirect(errorRedirectUrl);
            } catch (Exception ex) {
                logger.error("에러 리다이렉트 중 오류 발생: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * DataBox를 TokenResponseDTO로 변환
     */
    private TokenResponseDTO convertToTokenResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        try {
            return TokenResponseDTO.builder()
                    .accessToken(dataBox.getString("accessToken"))
                    .refreshToken(dataBox.getString("refreshToken"))
                    .expiresIn(dataBox.getLong2("expiresIn"))
                    .tokenType(dataBox.getString("tokenType"))
                    .issuedAt(parseDateTime(FormatDate.getDate("yyyyMMddHHmmss")))
                    .build();
        } catch (Exception e) {
            logger.error("TokenResponseDTO 변환 중 오류 발생: {}", e.getMessage(), e);
            return new TokenResponseDTO();
        }
    }

    /**
     * FormatDate를 사용하여 YYYYMMDDHHMMSS -> LocalDateTime 변환
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            // YYYYMMDDHHMMSS -> "YYYY-MM-DD HH:MM:SS" 형식으로 변환
            String formattedDateTime = FormatDate.getFormatDate(dateTimeStr, "yyyy-MM-dd HH:mm:ss");

            if (formattedDateTime == null || formattedDateTime.isEmpty()) {
                return null;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(formattedDateTime, formatter);
        } catch (Exception e) {
            logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}