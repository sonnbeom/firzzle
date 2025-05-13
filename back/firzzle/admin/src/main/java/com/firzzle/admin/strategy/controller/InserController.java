package com.firzzle.admin.strategy.controller;

import com.firzzle.common.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;

import static com.firzzle.common.logging.dto.UserActionLog.*;
import static com.firzzle.common.logging.service.LoggingService.log;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/insert")
public class InserController {

    @GetMapping("/log")
    public Object getTransitions() {
        LocalDateTime now = LocalDateTime.now().withNano(0);  // 최초 기준 시각도 정리

        for (int day = 0; day < 30; day++) {
            LocalDateTime baseDate = now.minusDays(day).withHour(10).withMinute(0).withSecond(0).withNano(0);

            for (int i = 0; i < 5; i++) {
                log(testUserPreferenceLog("TEST_ID", "SUMMARY", "QUIZ_READ", baseDate.plusMinutes(i).withNano(0)));
                log(testUserPreferenceLog("TEST_ID", "QUIZ_READ", "SNAP_REVIEW_READ", baseDate.plusMinutes(i + 5).withNano(0)));
                log(testUserPreferenceLog("TEST_ID", "SNAP_REVIEW_READ", "RECOMMEND", baseDate.plusMinutes(i + 10).withNano(0)));
            }
        }

        return "inserted test logs";
    }
    @PostMapping("/learning")
    public Object postLearningData() {
        LocalDateTime now = LocalDateTime.now().withNano(0);  // 최초 기준 시각도 정리

        for (int day = 0; day < 5; day++) {
            for (int i = 0; i < 5; i++) {
                log(userActionLog("TEST_LEARNING_ID", "CONTENT_CREATED"));
                log(userActionLog("TEST_LEARNING_ID", "CONTENT_CREATED"));
                log(userActionLog("TEST_LEARNING_ID", "START_LEARNING"));
            }
        }


        return "inserted test logs";
    }


}
