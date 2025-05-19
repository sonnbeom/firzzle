package com.firzzle.llm.controller;

import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.llm.dto.LearningChatResponseDTO;
import com.firzzle.llm.dto.RecommendRequestDTO;
import com.firzzle.llm.dto.RecommendResponseDTO;
import com.firzzle.llm.dto.UserContentDTO;
import com.firzzle.llm.service.RecommendationService;
import com.firzzle.llm.mapper.UserContentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{userContentSeq}/recommendations")
    @Operation(summary = "영상 유사도 기반 추천", description = "입력된 userContentSeq와 유사한 영상 목록(contentSeq)을 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CompletableFuture<ResponseEntity<Response<RecommendResponseDTO>>> recommendSimilarVideos(
            @RequestHeader(value = "X-User-UUID", required = true) String userUUID,
            @PathVariable("userContentSeq") Long userContentSeq,
            @ModelAttribute RecommendRequestDTO recommendRequestDTO // ✅ QueryParam -> 객체 자동 바인딩
    ) {
//        return recommendationService.searchSimilarContents(
//                userUUID,
//                userContentSeq,
//                recommendRequestDTO
//        ).thenApply(result -> ResponseEntity.ok(
//                Response.<RecommendationsResponseDTO>builder()
//                    .status(Status.OK)
//                    .message("추천 콘텐츠 조회 성공")
//                    .data(result)
//                    .build()
//        )).exceptionally(e -> ResponseEntity.status(500).body(
//                Response.<RecommendationsResponseDTO>builder()
//                    .status(Status.FAIL)
//                    .message("추천 콘텐츠 조회 오류: " + e.getMessage())
//                    .build()
//        ));
    	return null;
    }
}
