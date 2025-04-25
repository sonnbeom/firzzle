package com.firzzle.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {
    private String message;
    private Long seq;    // 필요한 경우에만 사용
    private String value;    // 필요한 경우에만 사용
    
    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
    
    public static MessageResponse of(String message, Long recordSeq) {
        return MessageResponse.builder()
                .message(message)
                .seq(recordSeq)
                .build();
    }
    
    public static MessageResponse of(String message, String value) {
        return MessageResponse.builder()
                .message(message)
                .value(value)
                .build();
    }
}
