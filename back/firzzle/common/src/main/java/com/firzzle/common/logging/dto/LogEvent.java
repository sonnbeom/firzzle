package com.firzzle.common.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogEvent {
    private String event;
    private String userId;
    private String action;
    private long duration;
    private LocalDateTime timestamp;

    public static LogEvent performanceEvent(String api, String action, long duration) {
        return new LogEvent("PERFORMANCE", api, action, duration, LocalDateTime.now());
    }

    public static LogEvent userAction(String userId, String action) {
        return new LogEvent("USER_ACTION", userId, action, 0L, LocalDateTime.now());
    }

    public static LogEvent errorEvent(String userId, String action) {
        return new LogEvent("ERROR", userId, action, 0L, LocalDateTime.now());
    }
}
