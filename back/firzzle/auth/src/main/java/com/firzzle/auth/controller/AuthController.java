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
import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.firzzle.common.logging.dto.UserActionLog.*;
import static com.firzzle.common.logging.service.LoggingService.*;

/**
 * @Class Name : AuthController.java
 * @Description : 인증 관련 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "로그인, 로그아웃 및 토큰 관리 관련 API를 제공합니다")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final OAuthRedirectService oAuthRedirectService;

    /**
     * 카카오 로그인 API
     * @param loginRequest 카카오 로그인 요청 정보
     * @param request HTTP 요청 객체
     * @return 토큰 응답
     */
    @Deprecated
    @PostMapping(value = "/login/kakao", produces = "application/json;charset=UTF-8")
    @Operation(summary = "카카오 로그인", description = "카카오 소셜 로그인을 통해 사용자를 인증하고 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효하지 않은 카카오 액세스 토큰 또는 필수 파라미터 누락"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 유효하지 않은 카카오 사용자 정보"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 - 카카오 API 연동 실패 또는 내부 서버 오류"
            )
    })
    public ResponseEntity<Response<TokenResponseDTO>> kakaoLogin(
            @Parameter(
                    description = "카카오 로그인 정보 (액세스 토큰 포함)",
                    required = true
            )
            @Valid @RequestBody KakaoLoginRequestDTO loginRequest,
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

            // 로그인 조회 => ELK
            log(userLoginLog());

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("카카오 로그인 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("카카오 로그인 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 로그인 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토큰 갱신 API
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 갱신된 토큰 응답
     */
    @PostMapping(value = "/refresh", produces = "application/json;charset=UTF-8")
    @Operation(summary = "토큰 갱신", description = "HTTP-Only 쿠키에 저장된 리프레시 토큰을 사용하여 만료된 액세스 토큰을 새로 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 리프레시 토큰 누락"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 유효하지 않은 리프레시 토큰 또는 만료된 토큰"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 - 토큰 갱신 처리 중 내부 서버 오류"
            )
    })
    public ResponseEntity<Response<TokenResponseDTO>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("토큰 갱신 요청");

        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = extractRefreshTokenFromCookie(request);
            if (refreshToken == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 없습니다.");
            }

            RequestBox box = RequestManager.getBox(request);
            box.put("refreshToken", refreshToken);

            DataBox tokenDataBox = authService.refreshToken(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            // 리프레시 토큰을 HTTP-Only 쿠키로 설정 (갱신된 경우)
            if (tokenResponseDTO.getRefreshToken() != null && !tokenResponseDTO.getRefreshToken().isEmpty()) {
                setRefreshTokenCookie(response, tokenResponseDTO.getRefreshToken());
            }

            // 응답에서 리프레시 토큰 제거
            TokenResponseDTO responseDto = TokenResponseDTO.builder()
                    .accessToken(tokenResponseDTO.getAccessToken())
                    .expiresIn(tokenResponseDTO.getExpiresIn())
                    .tokenType(tokenResponseDTO.getTokenType())
                    .issuedAt(tokenResponseDTO.getIssuedAt())
                    .build();

            Response<TokenResponseDTO> apiResponse = Response.<TokenResponseDTO>builder()
                    .status(Status.OK)
                    .message("토큰 갱신 성공")
                    .data(responseDto)
                    .build();

            return ResponseEntity.ok(apiResponse);
        } catch (BusinessException e) {
            logger.error("토큰 갱신 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("토큰 갱신 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다.");
        }
    }

    /**
     * 쿠키에서 리프레시 토큰 추출
     * @param request HTTP 요청 객체
     * @return 리프레시 토큰 또는 null
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 로그아웃 API
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 로그아웃 응답
     */
    @PostMapping(value = "/logout", produces = "application/json;charset=UTF-8")
    @Operation(summary = "로그아웃", description = "사용자의 현재 세션을 종료하고, 발급된 리프레시 토큰을 서버에서 무효화하며 클라이언트의 쿠키를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 리프레시 토큰 누락"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 유효하지 않은 토큰"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 - 로그아웃 처리 중 내부 서버 오류"
            )
    })
    public ResponseEntity<Response<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("로그아웃 요청");

        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = extractRefreshTokenFromCookie(request);
            if (refreshToken == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 없습니다.");
            }

            RequestBox box = RequestManager.getBox(request);
            box.put("refreshToken", refreshToken);

            authService.logout(box);

            // 리프레시 토큰 쿠키 삭제
            deleteRefreshTokenCookie(response);

            Response<Void> apiResponse = Response.<Void>builder()
                    .status(Status.OK)
                    .message("로그아웃 성공")
                    .build();

            return ResponseEntity.ok(apiResponse);
        } catch (BusinessException e) {
            logger.error("로그아웃 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("로그아웃 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "로그아웃 중 오류가 발생했습니다.");
        }
    }

    /**
     * 리프레시 토큰 쿠키 삭제
     * @param response HTTP 응답 객체
     */
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        // SameSite 속성 설정
        String cookieHeader = String.format("%s=%s; Max-Age=0; Path=%s; HttpOnly; Secure; SameSite=Lax",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath());

        response.addHeader("Set-Cookie", cookieHeader);
        response.addCookie(cookie);
    }

    /**
     * 카카오 로그인 콜백 API
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return void
     */
    @GetMapping(value = "/kakao/callback")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 로그인 후 리다이렉트되는 엔드포인트로, 인증 코드를 처리하여 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 콜백 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 인증 코드 누락 또는 유효하지 않은 코드"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 카카오 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류 - 카카오 API 연동 실패 또는 내부 서버 오류")
    })
    public void kakaoCallback(
            @Parameter(description = "카카오 인증 코드", required = true) @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("카카오 로그인 콜백 요청 - 인증 코드: {}", code);

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("code", code);

            if (code == null || code.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "인증 코드가 없습니다.");
            }

            // OAuth 처리 및 토큰 발급
            DataBox tokenDataBox = authService.processKakaoCallback(box);
            TokenResponseDTO tokenResponseDTO = convertToTokenResponseDTO(tokenDataBox);

            // 리프레시 토큰을 HTTP-Only 쿠키로 설정
            setRefreshTokenCookie(response, tokenResponseDTO.getRefreshToken());

            // 리다이렉트 URL 결정
            String baseRedirectUrl = oAuthRedirectService.determineClientRedirectUri();

            // 액세스 토큰을 쿼리 파라미터로 추가
            String redirectUrl = baseRedirectUrl + "?accessToken=" + tokenResponseDTO.getAccessToken();

            logger.info("카카오 로그인 성공 - 리다이렉트: {}", redirectUrl);

            response.sendRedirect(redirectUrl);
        } catch (BusinessException e) {
            logger.error("카카오 콜백 처리 중 비즈니스 예외 발생: {}", e.getMessage());
            String clientErrorRedirectUri = oAuthRedirectService.determineClientRedirectUri();
            try {
                // URL 인코딩 시 StandardCharsets.UTF_8 사용
                String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
                clientErrorRedirectUri += "?error=business&message=" + encodedMessage;
                response.sendRedirect(clientErrorRedirectUri);
            } catch (IOException ex) {
                logger.error("리다이렉트 중 예외 발생: {}", ex.getMessage(), ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("카카오 콜백 처리 중 예외 발생: {}", e.getMessage(), e);
            String clientErrorRedirectUri = oAuthRedirectService.determineClientRedirectUri();
            try {
                // URL 인코딩 시 StandardCharsets.UTF_8 사용
                String encodedMessage = URLEncoder.encode("서버 오류가 발생했습니다.", StandardCharsets.UTF_8.toString());
                clientErrorRedirectUri += "?error=server&message=" + encodedMessage;
                response.sendRedirect(clientErrorRedirectUri);
            } catch (IOException ex) {
                logger.error("리다이렉트 중 예외 발생: {}", ex.getMessage(), ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 리프레시 토큰을 HTTP-Only 쿠키로 설정
     * @param response HTTP 응답 객체
     * @param refreshToken 리프레시 토큰
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        // 쿠키 만료 시간 (예: 7일)
        int cookieMaxAge = 7 * 24 * 60 * 60;

        // 리프레시 토큰 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);          // JavaScript에서 접근 불가
        refreshTokenCookie.setSecure(false);            // HTTPS에서만 전송, but 개발 중이므로 false
        refreshTokenCookie.setPath("/api/v1/auth");    // 모든 경로에서 접근 가능
        refreshTokenCookie.setMaxAge(cookieMaxAge);    // 쿠키 유효 기간

        // SameSite 속성 설정 (크로스 사이트 요청 제한)
        // HttpServletResponse가 직접 SameSite 속성을 지원하지 않아 헤더로 추가
        String cookieHeader = String.format("%s=%s; Max-Age=%d; Path=%s; HttpOnly; Secure; SameSite=Lax",
                refreshTokenCookie.getName(),
                refreshTokenCookie.getValue(),
                refreshTokenCookie.getMaxAge(),
                refreshTokenCookie.getPath());

        response.addHeader("Set-Cookie", cookieHeader);
        response.addCookie(refreshTokenCookie);
    }

    /**
     * DataBox를 TokenResponseDTO로 변환
     * @param dataBox 데이터 박스
     * @return 토큰 응답 DTO
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
     * @param dateTimeStr 날짜 문자열
     * @return LocalDateTime 객체
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