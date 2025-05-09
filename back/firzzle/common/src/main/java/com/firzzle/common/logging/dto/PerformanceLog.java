package com.firzzle.common.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@AllArgsConstructor
public class PerformanceLog extends LogEvent {
    private String detail;
    private long duration;

    public static PerformanceLog performanceLog (String userId, String detail, long duration) {
        return PerformanceLog.builder()
                .event("PERFORMANCE")
                .userId(userId)
                .detail(detail)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
