package com.firzzle.llm.controller;

import com.firzzle.llm.dto.LlmRequestDTO;
import com.firzzle.llm.service.RegistrationService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "LLM API", description = "LLM 기반 요약")
public class LlmController {

    private final RegistrationService registrationService;

    @PostMapping("/summary")
    @Operation(summary = "요약 생성", description = "업로드된 텍스트를 요약합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (요약 텍스트 누락 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (요약 중 내부 문제)")
    })
    public CompletableFuture<ResponseEntity<Response<String>>> postSummary(
            @Valid @RequestBody LlmRequestDTO request) {

        return registrationService.summarizeContents(request)
                .thenApply(summary -> ResponseEntity.ok(
                        Response.<String>builder()
                                .status(Status.OK)
                                .message("요약 생성 성공")
                                .data(summary)
                                .build()))
                .exceptionally(e -> ResponseEntity.status(500).body(
                        Response.<String>builder()
                                .status(Status.FAIL)
                                .message("요약 생성 중 오류: " + e.getMessage())
                                .build()));
    }
}
