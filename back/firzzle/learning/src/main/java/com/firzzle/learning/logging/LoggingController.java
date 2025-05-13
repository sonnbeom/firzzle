package com.firzzle.learning.logging;

import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logging")
public class LoggingController {

    @PostMapping("/visit")
    public String loggingVisit() {
        LoggingService.log(UserActionLog.fromVisit("VISIT"));
        return "inserted visit logs";
    }
}
