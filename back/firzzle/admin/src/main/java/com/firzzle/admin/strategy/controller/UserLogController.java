package com.firzzle.admin.strategy.controller;

import com.firzzle.admin.common.dto.response.ResponseUserLogTransitionDto;
import com.firzzle.admin.strategy.service.UserLogService;
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
@RequestMapping("/api/v1/strategy")
public class UserLogController {

    private final UserLogService userLogService;

    @GetMapping(value = "/transitions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTransitions(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<ResponseUserLogTransitionDto> responseDto = userLogService.getGroupedTransitions(startDate, endDate);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/learning-rate")
    public Object getLearningRate(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<ResponseUserLogTransitionDto> responseDto = userLogService.getLearningRate(startDate, endDate);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/login-rate")
    public Object getLoginRate(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<ResponseUserLogTransitionDto> responseDto = userLogService.getLoginRate(startDate, endDate);
        Response<List<ResponseUserLogTransitionDto>> response = Response.<List<ResponseUserLogTransitionDto>>builder()
                .status(Status.OK)
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }

}
