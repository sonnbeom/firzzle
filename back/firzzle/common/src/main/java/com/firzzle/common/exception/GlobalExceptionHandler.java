package com.firzzle.common.exception;

import com.firzzle.common.response.CustomApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.security.SignatureException;

/**
 * Global Exception Handler
 * 모든 예외를 처리하는 핸들러 클래스
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * ============================================
     * 1. 인증 관련 예외 처리 (401 Unauthorized)
     * ============================================
     */

    // 잘못된 인증 정보
//    @ExceptionHandler(BadCredentialsException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
//        log.error("Bad Credentials Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "아이디 또는 비밀번호가 일치하지 않습니다."
//            ));
//    }

    // 계정 만료
//    @ExceptionHandler(AccountExpiredException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleAccountExpiredException(AccountExpiredException e) {
//        log.error("Account Expired Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "계정이 만료되었습니다. 관리자에게 문의하세요."
//            ));
//    }

    // 계정 잠김
//    @ExceptionHandler(LockedException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleLockedException(LockedException e) {
//        log.error("Locked Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "계정이 잠겼습니다. 관리자에게 문의하세요."
//            ));
//    }

    // 비활성화된 계정
//    @ExceptionHandler(DisabledException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleDisabledException(DisabledException e) {
//        log.error("Disabled Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "비활성화된 계정입니다. 관리자에게 문의하세요."
//            ));
//    }

    // 불충분한 인증
//    @ExceptionHandler(InsufficientAuthenticationException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleInsufficientAuthenticationException(InsufficientAuthenticationException e) {
//        log.error("Insufficient Authentication Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "추가 인증이 필요합니다."
//            ));
//    }

    // 인증 정보 없음
//    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleAuthenticationCredentialsNotFoundException(
//            AuthenticationCredentialsNotFoundException e) {
//        log.error("Authentication Credentials Not Found Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "인증 정보를 찾을 수 없습니다. 다시 로그인해주세요."
//            ));
//    }

    /**
     * ============================================
     * 2. JWT 관련 예외 처리 (401 Unauthorized)
     * ============================================
     */

    // JWT 토큰 만료
//    @ExceptionHandler(ExpiredJwtException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
//        log.error("Expired JWT Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "인증이 만료되었습니다. 다시 로그인해주세요."
//            ));
//    }

    // JWT 서명 오류
    @ExceptionHandler(SignatureException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleSignatureException(SignatureException e) {
        log.error("JWT Signature Exception 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(CustomApiResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "유효하지 않은 인증입니다. 다시 로그인해주세요."
                ));
    }

    // JWT 형식 오류
//    @ExceptionHandler(MalformedJwtException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleMalformedJwtException(MalformedJwtException e) {
//        log.error("Malformed JWT Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.UNAUTHORIZED)
//            .body(CustomApiResponse.error(
//                HttpStatus.UNAUTHORIZED.value(),
//                "잘못된 형식의 인증입니다. 다시 로그인해주세요."
//            ));
//    }

    // 헤더 누락 처리
    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("Required header is missing: {}", e.getHeaderName(), e);

        String text = "유효하지 않은 입력값입니다.";
        if(e.getHeaderName().equals("RefreshToken")) text = "RefreshToken이 존재하지 않습니다.";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        text
                ));
    }

    // 쿠키 누락 처리
    @ExceptionHandler(MissingRequestCookieException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleMissingRequestCookieException(MissingRequestCookieException e) {
        log.error("Required cookie is missing: {}", e.getCookieName(), e);

        String text = "유효하지 않은 입력값입니다.";
        if(e.getCookieName().equals("RefreshToken")) text = "RefreshToken이 존재하지 않습니다.";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        text
                ));
    }

    /**
     * ============================================
     * 3. 권한 관련 예외 처리 (403 Forbidden)
     * ============================================
     */

    // 접근 권한 없음
//    @ExceptionHandler(AccessDeniedException.class)
//    protected ResponseEntity<CustomApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
//        log.error("Access Denied Exception 발생: ", e);
//        return ResponseEntity
//            .status(HttpStatus.FORBIDDEN)
//            .body(CustomApiResponse.error(
//                HttpStatus.FORBIDDEN.value(),
//                "해당 리소스에 대한 접근 권한이 없습니다."
//            ));
//    }

    /**
     * ============================================
     * 4. 요청 값 검증 예외 처리 (400 Bad Request)
     * ============================================
     */

    // 요청 파라미터 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage() // 추후 양식에 맞게 수정 예정
                ));
    }

    // 요청 바인딩 실패
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleBindException(BindException e) {
        log.error("BindException", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage() // 추후 양식에 맞게 수정 예정
                ));
    }

    // 제약 조건 위반
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("ConstraintViolationException", e);

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CustomApiResponse.error(
                        errorCode.getStatus().value(),
                        errorCode.getMessage(messageSource)
                ));
    }

    // 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException", e);
        ErrorCode errorCode = ErrorCode.INVALID_TYPE_VALUE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CustomApiResponse.error(errorCode.getStatus().value(), errorCode.getMessage(messageSource)));
    }

    /**
     * ============================================
     * 5. HTTP 메소드 관련 예외 처리 (405 Method Not Allowed)
     * ============================================
     */

    // 지원하지 않는 HTTP 메소드 요청
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CustomApiResponse.error(
                        errorCode.getStatus().value(),
                        errorCode.getMessage(messageSource)
                ));
    }

    /**
     * ============================================
     * 6. 데이터베이스 관련 예외 처리 (502 Bad Gateway)
     * ============================================
     */

    // 데이터베이스 제약조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("데이터베이스 제약조건 위반: ", e);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(CustomApiResponse.error(
                        HttpStatus.BAD_GATEWAY.value(),
                        "데이터 처리 중 오류가 발생했습니다."
                ));
    }

    /**
     * ============================================
     * 7. 서버 오류 관련 예외 처리 (500 Internal Server Error)
     * ============================================
     */

    // Null Pointer 예외
    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleNullPointerException(NullPointerException e) {
        log.error("Null Pointer Exception 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "서버 처리 중 오류가 발생했습니다."
                ));
    }

    // Runtime 예외
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime Exception 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "서버 처리 중 오류가 발생했습니다."
                ));
    }

    // 기타 모든 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleException(Exception e) {
        log.error("서버 오류 발생: ", e);  // 로그에 스택트레이스 포함
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                ));
    }

    /**
     * ============================================
     * 8. 커스텀 예외 처리
     * ============================================
     */

    // 사용자 정의 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException", e);

        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = errorCode.getStatus() != null ? errorCode.getStatus() : HttpStatus.BAD_REQUEST;

        // 국제화된 메시지 가져오기
        String message = e.getLocalizedMessage(messageSource);

        return ResponseEntity
                .status(status)
                .body(CustomApiResponse.error(
                        status.value(),
                        message
                ));
    }

    /**
     * ============================================
     * 9. 파일 예외 처리
     * ============================================
     */

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("File Size Exceeded Exception 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(CustomApiResponse.error(
                        HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        "파일 크기가 허용된 최대 크기를 초과했습니다. 파일 크기를 확인해 주세요."
                ));
    }

    @ExceptionHandler(MultipartException.class)
    protected ResponseEntity<CustomApiResponse<Void>> handleMultipartException(MultipartException e) {
        log.error("Multipart Exception 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        "파일 업로드 처리 중 오류가 발생했습니다. 다시 시도해 주세요."
                ));
    }
}