package com.firzzle.llm.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.PageResponseDTO;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.firzzle.llm.domain.*;
import com.firzzle.llm.service.*;

/**
 * @Class Name : LlmController.java
 * @Description : Llm 기능 API 컨트롤러
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }
    
    /**
     * 🎯 업로드된 파일로부터 요약문을 생성하는 API
     * @param String 요약할 텍스트
     * @return 요약 결과 (LLM 기반 처리)
     */
    @PostMapping("/summary")
    public CompletableFuture<ResponseEntity<String>> postSummary(@RequestBody SummaryRequest request) {
        return llmService.summarizeContents(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return ResponseEntity.status(500).body("요약 생성 중 오류 발생: " + e.getMessage());
                });
    }
    
	 /**
	  * 🎯 영상 내용을 바탕으로 사용자와 토론을 진행하는 API
	  * @param 사용자의 질문 
	  * @return 요약 결과 (LLM 기반 처리)
	  */
	 @PostMapping("/runningchat")
	 public ResponseEntity<?> TryRunningChat(@RequestBody RunningChatRequest request) {
	     try {
	         return ResponseEntity.ok("null");
	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(500).body("LLM 처리 중 오류 발생: " + e.getMessage());
	     }
	 }
    
    /**
     * 🎯 테스트용 API
     * @Body prompt 입력 프롬프트 
     * @return 요약 결과 (LLM 기반 처리)
     */
    @PostMapping("/chat-test")
    public CompletableFuture<ResponseEntity<String>> ChatTest(@RequestBody String question) {
        return llmService.testGptResponse(question)
            .thenApply(result -> ResponseEntity.ok(result))
            .exceptionally(e -> {
                e.printStackTrace();
                return ResponseEntity.status(500).body("LLM 처리 중 오류 발생: " + e.getMessage());
            });
    }

    
    /**
     * 🎯 테스트용 API
     * @Body prompt 입력 프롬프트 
     * @return 요약 결과 (LLM 기반 처리)
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        llmService.register(request.getId(), request.getContent());
        return ResponseEntity.ok("✅ 등록 완료");
    }
}