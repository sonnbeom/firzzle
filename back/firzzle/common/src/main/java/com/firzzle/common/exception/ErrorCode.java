package com.firzzle.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // =========== Common ===========
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "MSG_0000070001", "유효하지 않은 입력값입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "MSG_0000070002", "올바르지 않은 타입입니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070003", "리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070004", "서버 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "MSG_0000070005", "지원하지 않는 HTTP 메소드입니다"),

    // =========== Auth ===========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MSG_0000070101", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "MSG_0000070102", "접근 권한이 없습니다"),

    // =========== JWT ===========
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "MSG_0000070201", "토큰이 만료되었습니다"),
    JWT_INVALID(HttpStatus.FORBIDDEN, "MSG_0000070202", "유효하지 않은 토큰입니다"),
    JWT_REQUIRED(HttpStatus.UNAUTHORIZED, "MSG_0000070203", "로그인 후 이용해주세요"),

    // =========== Business ===========
    INVALID_PAGE_PARAMETERS(HttpStatus.BAD_REQUEST, "MSG_0000070301", "유효하지 않은 페이지 파라미터입니다"),

    // =========== User Module ===========
    // 여기부터 MSG_0000070401 시작

    // =========== Admin Module ===========
    // 여기부터 MSG_0000070501 시작

    // =========== Learning Module ===========
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "MSG_0000070601", "이미 존재하는 리소스입니다"),
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070602", "콘텐츠를 찾을 수 없습니다");

    // =========== LLM Module ===========
    // 여기부터 MSG_0000070701 시작

    // =========== STT Module ===========
    // 여기부터 MSG_0000070801 시작

    // =========== Main Module ===========
    // 여기부터 MSG_0000070901 시작

    // =========== Gateway Module ===========
    // 여기부터 MSG_0000071001 시작

    private final HttpStatus status;
    private final String messageKey;
    private final String defaultMessage;

    /**
     * 기본 메시지를 반환합니다 (기존 코드와의 호환성 유지).
     * @return 기본 오류 메시지
     */
    public String getMessage() {
        return defaultMessage;
    }

    /**
     * MessageSource를 통해 국제화된 메시지를 반환합니다.
     * @param messageSource 메시지 소스
     * @param args 메시지 파라미터
     * @return 국제화된 메시지
     */
    public String getMessage(MessageSource messageSource, Object... args) {
        return messageSource.getMessage(
                messageKey,
                args,
                defaultMessage,
                LocaleContextHolder.getLocale()
        );
    }
}