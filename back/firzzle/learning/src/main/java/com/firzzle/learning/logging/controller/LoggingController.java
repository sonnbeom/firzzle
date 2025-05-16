package com.firzzle.learning.logging.controller;

import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.learning.logging.common.service.HttpServletRequestCheckService;
import com.firzzle.learning.logging.dto.request.RequestTransitionDto;
import com.firzzle.learning.logging.service.LearningServerLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logging")
public class LoggingController {

    private final LearningServerLoggingService learningServerLoggingService;
    private final HttpServletRequestCheckService servletRequestCheckService;

    @PostMapping("/visit")
    public String loggingVisit() {
        LoggingService.log(UserActionLog.fromVisit("VISIT"));
        return "inserted visit logs";
    }

    @PostMapping("/transition")
    public String loggingTransition(@RequestBody RequestTransitionDto reqDto, HttpServletRequest request) throws Exception {
        String userId = servletRequestCheckService.getUserUUID(request);
        learningServerLoggingService.loggingLearningTransition(userId, reqDto);
        return "inserted transition logs";
    }

}
