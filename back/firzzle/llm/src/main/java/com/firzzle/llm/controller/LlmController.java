package com.firzzle.llm.controller;

import com.firzzle.llm.dto.LlmRequest;
import com.firzzle.llm.dto.learningChatRequestDTO;
import com.firzzle.llm.service.RegistrationService;
import com.firzzle.llm.service.learningChatService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "LLM API", description = "LLM 기반 요약 및 러닝챗 기능")
public class LlmController {

    private final RegistrationService registrationService;
    private final learningChatService learningChatService;

    @PostMapping("/summary")
    @Operation(summary = "요약 생성", description = "업로드된 텍스트를 요약합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (요약 텍스트 누락 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (요약 중 내부 문제)")
    })
    public CompletableFuture<ResponseEntity<Response<String>>> postSummary(
            @Valid @RequestBody LlmRequest request) {

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
    
    @PostMapping("/{contentSeq}/chat")
    @Operation(summary = "러닝챗 질문", description = "영상 기반 사용자 질문에 대해 LLM이 응답합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 응답 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (질문 누락 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (LLM 처리 중 오류)")
    })
    public CompletableFuture<ResponseEntity<Response<String>>> trylearningChat(
    		@Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
    		@Valid @RequestBody learningChatRequestDTO request,
            @Parameter(description = "사용자 UUID", example = "abc-123-xyz")
            @RequestHeader(value = "X-User-UUID", required = false) String userID) {

        return learningChatService.learningChat(userContentSeq, request, userID)
                .thenApply(result -> ResponseEntity.ok(
                        Response.<String>builder()
                                .status(Status.OK)
                                .message("질문 응답 성공")
                                .data(result)
                                .build()))
                .exceptionally(e -> ResponseEntity.status(500).body(
                        Response.<String>builder()
                                .status(Status.FAIL)
                                .message("러닝챗 오류: " + e.getMessage())
                                .build()));
    }
}
