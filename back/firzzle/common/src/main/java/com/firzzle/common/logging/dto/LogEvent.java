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
    private String event;                  // PERFORMANCE, USER_ACTION, ERROR
    private String userId;                // 사용자 ID
    private String action;                // 액션 키워드
    private String detail;                // 추가 정보 (EASY/HIGH, 탭 이름, 유입 경로 등)
    private int count;                    // 반복 수 (단어 드래그 횟수 등)
    private long duration;                // 성능 측정(ms)
    private LocalDateTime timestamp;      // 로그 시각


    /*
    1. API 소요 시간
    2. 요약본 조회 (EASY/HIGH 선택 이력) 선호도
    3. 단어 드래그 이벤트 빈도
    4. 러닝챗 모드 전환 기록
    5. 탭 조회 횟수(선호도 조사)
    6. 어디서 방문했는지 (광고)
    7. 학습 모드에서, 시험 모드에서 몇번 요청했는지
    * */

    // 1. API 소요 시간
    public static LogEvent apiPerformance(String userId, String apiPath, long duration) {
        return LogEvent.builder()
                .event("PERFORMANCE")
                .userId(userId)
                .action(apiPath)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 2. 요약본 조회 (EASY/HIGH)
    public static LogEvent summaryLevel(String userId, String level) {
        return LogEvent.builder()
                .event("USER_ACTION")
                .userId(userId)
                .action("SUMMARY_LEVEL_PREFERENCE")
                .detail(level)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 3. 단어 드래그 이벤트 (count = 몇 번 드래그했는지)
    public static LogEvent wordHighlight(String userId, int count) {
        return LogEvent.builder()
                .event("USER_ACTION")
                .userId(userId)
                .action("WORD_HIGHLIGHT_COUNT")
                .count(count)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 4. 러닝챗 모드 전환
    public static LogEvent modeSwitch(String userId, String fromTo) {
        return LogEvent.builder()
                .event("USER_ACTION")
                .userId(userId)
                .action("CHAT_MODE_SWITCH")
                .detail(fromTo) // 예: "STUDY_TO_EXAM"
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 5. 탭 조회
    public static LogEvent tabView(String userId, String tabName) {
        return LogEvent.builder()
                .event("USER_ACTION")
                .userId(userId)
                .action("FUNCTION_PREFERENCE")
                .detail(tabName) // 예: "QUIZ", "NOTE"
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 6. 광고 유입
    public static LogEvent visitFromAd(String userId, String channel) {
        return LogEvent.builder()
                .event("USER_ACTION")
                .userId(userId)
                .action("VISIT")
                .detail(channel) // 예: "GOOGLE_AD", "INSTAGRAM"
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 7. 학습/시험 모드에서 몇 번 요청했는지
    public static LogEvent modeRequestCount(String userId, String mode, int count) {
        return LogEvent.builder()
                .event("USER_ACTION")
                .userId(userId)
                .action(mode.equals("STUDY") ? "LEARNING_QUESTION_ASK" : "EXAM_ANSWER_SUBMIT")
                .count(count)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
