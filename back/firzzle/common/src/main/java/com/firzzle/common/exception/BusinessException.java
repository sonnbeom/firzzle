package com.firzzle.common.exception;


import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] messageArgs;
    // 모든 인스턴스가 공유할 수 있는 상수 빈 배열
    private static final Object[] EMPTY_ARGS = new Object[0];


    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageArgs = EMPTY_ARGS;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.messageArgs = EMPTY_ARGS;
    }

    /**
     * 메시지 파라미터를 포함한 비즈니스 예외 생성
     * @param errorCode 에러 코드
     * @param messageArgs 메시지 파라미터
     */
    public BusinessException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs != null ? messageArgs : EMPTY_ARGS;
    }

    /**
     * MessageSource를 통해 국제화된 메시지를 반환합니다.
     * @param messageSource 메시지 소스
     * @return 국제화된 메시지
     */
    public String getLocalizedMessage(MessageSource messageSource) {
        // 만약 생성자에서 명시적으로 메시지를 지정했다면 그것을 우선 사용
        if (!getMessage().equals(errorCode.getMessage())) {
            return getMessage();
        }

        // 그렇지 않다면 ErrorCode의 메시지 키와 인자를 사용하여 국제화된 메시지 생성
        return messageSource.getMessage(
                errorCode.getMessageKey(),
                messageArgs,
                errorCode.getDefaultMessage(), // 번역이 없을 경우 기본 메시지 사용
                LocaleContextHolder.getLocale()
        );

    }
}
