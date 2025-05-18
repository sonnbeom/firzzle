package com.firzzle.llm.controller;

import com.firzzle.llm.sse.SseEmitterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/sse")
@Tag(name = "SSE API", description = "Server-Sent Events 연결")
public class SseController {
    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    @Autowired
    private SseEmitterRepository emitterRepository;

    @GetMapping("/summary/{taskId}")
    @Operation(summary = "요약 작업 SSE 연결", description = "특정 요약 작업에 대한 실시간 이벤트 스트림 연결")
    @ApiResponse(responseCode = "200", description = "SSE 연결 성공")
    public SseEmitter connectToSummary(@PathVariable String taskId) {
        logger.info("요약 작업 SSE 연결 요청: taskId={}", taskId);
        return emitterRepository.create(taskId);
    }
}