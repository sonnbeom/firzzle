package com.firzzle.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)

public abstract class LogEvent {
    private String event;                  // PERFORMANCE, USER_ACTION, ERROR
    private String userId;                // 사용자 ID
    private LocalDateTime timestamp;      // 로그 시각
}