package com.firzzle.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@Getter
public class UserActionLog extends LogEvent {
    private String fromContent;
    private String toContent;
    private String detail;
    private String recommendAction;
    private String summaryPreference;

    public static UserActionLog fromVisit (String detail) {

        return UserActionLog.builder()
                .detail(detail)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    public static UserActionLog userActionLog (String userId, String detail) {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .userId(userId)
                .detail(detail)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }
    public static UserActionLog userTestActionLog (String userId, String detail, LocalDateTime localDateTime) {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .userId(userId)
                .detail(detail)
                .timestamp(localDateTime)
                .build();
    }
    public static UserActionLog userLoginLog () {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .detail("LOGIN")
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }
    public static UserActionLog userRecommendActionLog (String userId, String recommendAction) {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .userId(userId)
                .detail("RECOMMEND")
                .recommendAction(recommendAction)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }
    public static UserActionLog userPreferenceLog(String userId, String fromContent, String toContent) {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .userId(userId)
                .detail("PREFERENCE")
                .timestamp(LocalDateTime.now().withNano(0))
                .fromContent(fromContent)
                .toContent(toContent)
                .build();
    }
    public static UserActionLog testUserPreferenceLog(String userId, String fromContent, String toContent, LocalDateTime localDateTime) {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .userId(userId)
                .detail("PREFERENCE")
                .timestamp(LocalDateTime.now().withNano(0))
                .fromContent(fromContent)
                .toContent(toContent)
                .build();
    }


    public static UserActionLog summaryPreferenceLog(String userId, String summaryPreference) {

        return UserActionLog.builder()
                .event("USER_ACTION")
                .userId(userId)
                .detail("SUMMARY_PREFERENCE")
                .timestamp(LocalDateTime.now().withNano(0))
                .summaryPreference(summaryPreference)
                .build();
    }
}