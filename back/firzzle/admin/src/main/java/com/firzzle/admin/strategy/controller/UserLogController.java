package com.firzzle.admin.strategy.controller;

import com.firzzle.admin.strategy.domain.UserActionLog;
import com.firzzle.admin.strategy.dto.response.ResponseUserLogTransitionDto;
import com.firzzle.admin.strategy.repository.UserActionLogRepository;
import com.firzzle.admin.strategy.service.UserLogService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static com.firzzle.common.logging.dto.UserActionLog.userActionLog;
import static com.firzzle.common.logging.service.LoggingService.log;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/strategy")
public class UserLogController {

    private final UserLogService userLogService;
    private final UserActionLogRepository userActionLogRepository;

    @GetMapping("/transitions")
    public Object getTransitions(@RequestParam LocalDate date) {
        List<ResponseUserLogTransitionDto> responseDto = userLogService.getGroupedTransitions(date);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/simple")
    public Object getSimpleLogs() {
        List<UserActionLog> logs = userActionLogRepository.findByDetail("PREFERENCE");
        logs.forEach(log -> System.out.println("🔥 로그 데이터: " + log));
        return logs;
    }
}
