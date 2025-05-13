package com.firzzle.admin.strategy.controller;

import com.firzzle.admin.strategy.dto.response.ResponseUserLogTransitionDto;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/strategy")
public class UserLogController {

    private final UserLogService userLogService;

    @GetMapping("/transitions")
    public Object getTransitions(@RequestParam LocalDate date) {
        List<ResponseUserLogTransitionDto> responseDto = userLogService.getGroupedTransitions(date);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/learning-rate")
    public Object getLearningRate(@RequestParam LocalDate date) {
        List<ResponseUserLogTransitionDto> responseDto = userLogService.getLearningRate(date);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }

}
