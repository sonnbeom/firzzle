package com.firzzle.llm.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.llm.dto.ChatHistoryResponseDTO;
import com.firzzle.llm.dto.ExamAnswerRequestDTO;
import com.firzzle.llm.dto.ExamAnswerResponseDTO;
import com.firzzle.llm.dto.ExamsDTO;
import com.firzzle.llm.dto.LearningChatRequestDTO;
import com.firzzle.llm.dto.LearningChatResponseDTO;
import com.firzzle.llm.dto.NextExamResponseDTO;
import com.firzzle.llm.dto.ExamHistoryResponseDTO;
import com.firzzle.llm.service.LearningChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "LearningChat API", description = "챗봇 기능")
public class LearningChatConteller {
    private final LearningChatService learningChatService;
    
    @PostMapping("/{ContentSeq}/chat")
    @Operation(summary = "러닝챗 질문", description = "영상 기반 사용자 질문에 대해 LLM이 응답합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 응답 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (질문 누락 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (LLM 처리 중 오류)")
    })
    public CompletableFuture<ResponseEntity<Response<LearningChatResponseDTO>>> trylearningChat(
        @Parameter(description = "사용자 UUID", example = "abc-123-xyz")
        @RequestHeader(value = "X-User-UUID", required = true) String userUUID,
        @Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("ContentSeq") Long userContentSeq,
        @Valid @RequestBody LearningChatRequestDTO request)
    {

        return learningChatService.learningChat(userUUID, userContentSeq, request)
                .thenApply(result -> ResponseEntity.ok(
                        Response.<LearningChatResponseDTO>builder()
                                .status(Status.OK)
                                .message("질문 응답 성공")
                                .data(result)
                                .build()))
                .exceptionally(e -> ResponseEntity.status(500).body(
                        Response.<LearningChatResponseDTO>builder()
                                .status(Status.FAIL)
                                .message("러닝챗 오류: " + e.getMessage())
                                .build()));
    }
    
    @GetMapping("/{ContentSeq}/chat")
    @Operation(summary = "대화 기록 조회", description = "사용자 콘텐츠 기준으로 이전 대화 목록을 조회합니다. 무한 스크롤을 위한 lastIndate, limit 지원")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "기록 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<List<ChatHistoryResponseDTO>>> getChatHistory(
            @Parameter(description = "사용자 UUID", example = "abc-123-xyz")
            @RequestHeader(value = "X-User-UUID", required = true) String userUUID,

            @Parameter(description = "사용자 콘텐츠 일련번호", required = true)
            @PathVariable("ContentSeq") Long userContentSeq,

            @Parameter(description = "가장 마지막 메시지 indate (무한 스크롤용)", example = "20250515132000")
            @RequestParam(value = "lastIndate", required = false) String lastIndate,

            @Parameter(description = "가져올 메시지 개수", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        List<ChatHistoryResponseDTO> messages = learningChatService.getChatsByContentAndUser(
                userUUID,
                userContentSeq,
                lastIndate,
                limit
        );

        return ResponseEntity.ok(Response.<List<ChatHistoryResponseDTO>>builder()
                .status(Status.OK)
                .message("대화 기록 조회 성공")
                .data(messages)
                .build());
    }

    @GetMapping("/exam/{ContentSeq}/next")
    @Operation(summary = "시험 모드 질문 조회회", description = "시험 모드에서 질문을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 응답 성공"),
            @ApiResponse(responseCode = "404", description = "질문 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
        })
    public CompletableFuture<ResponseEntity<Response<NextExamResponseDTO>>> getNextExam(
        @Parameter(description = "사용자 UUID", example = "abc-123-xyz")
        @RequestHeader(value = "X-User-UUID", required = true) String userUUID,
    		
        @Parameter(description = "사용자 콘텐츠 일련번호", required = true)
        @PathVariable("ContentSeq") Long userContentSeq
    ) {
        return learningChatService.getNextExam(userUUID, userContentSeq)
                .thenApply(result -> ResponseEntity.ok(
                        Response.<NextExamResponseDTO>builder()
                                .status(Status.OK)
                                .message("질문 응답 성공")
                                .data(result)
                                .build()
                ))
                .exceptionally(e -> ResponseEntity.status(500).body(
                        Response.<NextExamResponseDTO>builder()
                                .status(Status.FAIL)
                                .message("러닝챗 오류: " + e.getMessage())
                                .build()
                ));
    }
    
    @Operation(summary = "시험 모드 질문 응답", description = "시험 모드에서 질문에 대한 답변을 제출합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 응답 성공"),
            @ApiResponse(responseCode = "404", description = "질문 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/exam/{ContentSeq}")
    public CompletableFuture<ResponseEntity<Response<ExamAnswerResponseDTO>>> postExamAnswer(
	    @Parameter(description = "사용자 UUID", example = "abc-123-xyz")
	    @RequestHeader(value = "X-User-UUID", required = true) String userUUID,
        @Parameter(description = "사용자 콘텐츠 일련번호", required = true)
        @PathVariable("ContentSeq") Long userContentSeq,
	    @Valid @RequestBody ExamAnswerRequestDTO request
    ) {
        return learningChatService.submitExamAnswer(userUUID, userContentSeq,request)
            .thenApply(result -> ResponseEntity.ok(
                    Response.<ExamAnswerResponseDTO>builder()
                            .status(Status.OK)
                            .message("답변 처리 성공")
                            .data(result)
                            .build()
            ))
            .exceptionally(e -> ResponseEntity.status(500).body(
                    Response.<ExamAnswerResponseDTO>builder()
                            .status(Status.FAIL)
                            .message("답변 처리 오류: " + e.getMessage())
                            .build()
            ));
    }
    
    @GetMapping("/exam/{ContentSeq}/history")
    @Operation(summary = "시험 모드 대화 목록 불러오기", description = "시험 모드에서 한 대화 내용을 불러옵니다. 무한 스크롤을 위해 lastIndate, limit 쿼리 지원")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "대화 목록 불러오기 성공"),
            @ApiResponse(responseCode = "404", description = "대화 목록 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CompletableFuture<ResponseEntity<Response<List<ExamHistoryResponseDTO>>>> getExamHistory(
            @Parameter(description = "사용자 UUID", example = "abc-123-xyz")
            @RequestHeader(value = "X-User-UUID", required = true) String userUUID,

            @Parameter(description = "사용자 콘텐츠 일련번호", required = true)
            @PathVariable("ContentSeq") Long userContentSeq,

            @Parameter(description = "가장 마지막 메시지 indate (무한 스크롤용)", example = "20250515132000")
            @RequestParam(value = "lastIndate", required = false) String lastIndate,

            @Parameter(description = "가져올 메시지 개수", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return learningChatService.getExamHistory(userUUID, userContentSeq, lastIndate, limit)
                .thenApply(result -> ResponseEntity.ok(
                        Response.<List<ExamHistoryResponseDTO>>builder()
                                .status(Status.OK)
                                .message("시험 응답 기록 조회 성공")
                                .data(result)
                                .build()
                ))
                .exceptionally(e -> ResponseEntity.status(500).body(
                        Response.<List<ExamHistoryResponseDTO>>builder()
                                .status(Status.FAIL)
                                .message("시험 응답 기록 조회 오류: " + e.getMessage())
                                .build()
                ));
    }


}
