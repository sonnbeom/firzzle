package com.firzzle.admin.learning.controller;

import com.firzzle.admin.learning.service.LoggingLearningService;
import com.firzzle.admin.common.dto.response.ResponseUserLogTransitionDto;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning")
public class LoggingLearningController {

    private final LoggingLearningService loggingLearningService;

    @GetMapping(value = "/summary-level", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getSummaryPreferenceRate(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<ResponseUserLogTransitionDto> responseDto = loggingLearningService.getSummaryPreferenceRate(startDate, endDate);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
    @GetMapping(value = "/snap-review", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getSnapReviewWriteRate(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<ResponseUserLogTransitionDto> responseDto = loggingLearningService.getSnapReviewWriteRate(startDate, endDate);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
}
