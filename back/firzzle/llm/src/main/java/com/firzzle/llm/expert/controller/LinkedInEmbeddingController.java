package com.firzzle.llm.expert.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.llm.expert.dto.LinkedInEmbeddingRequestDTO;
import com.firzzle.llm.expert.dto.LinkedInSimilarityRequestDTO;
import com.firzzle.llm.expert.dto.LinkedInSimilarityResponseDTO;
import com.firzzle.llm.expert.service.LinkedInEmbeddingBatchService;
import com.firzzle.llm.expert.service.LinkedInEmbeddingService;
import com.firzzle.llm.expert.service.LinkedInVectorSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * LinkedIn 프로필 임베딩 및 검색 API 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expert/embeddings")
@Tag(name = "LinkedIn 프로필 임베딩 API", description = "LinkedIn 프로필 임베딩 및 유사도 검색 API")
public class LinkedInEmbeddingController {

    private final LinkedInEmbeddingService embeddingService;
    private final LinkedInVectorSearchService vectorSearchService;
    private final LinkedInEmbeddingBatchService batchService;

    /**
     * LinkedIn 프로필 임베딩 생성 및 저장
     */
//    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    @PostMapping("/linkedin/profiles")
    @Operation(summary = "LinkedIn 프로필 임베딩 생성", description = "LinkedIn 프로필 정보를 임베딩하여 벡터 DB에 저장합니다.")
    public ResponseEntity<Response<Boolean>> createProfileEmbedding(
            @Parameter(description = "LinkedIn 프로필 정보", required = true) @RequestBody LinkedInEmbeddingRequestDTO request) {

        log.info("LinkedIn 프로필 임베딩 요청 - 프로필 일련번호: {}", request.getProfileSeq());

        try {
            // 컬렉션 존재 확인
            if (!vectorSearchService.ensureCollectionExists()) {
                return ResponseEntity.ok(Response.<Boolean>builder()
                        .status(Status.FAIL)
                        .message("벡터 데이터베이스 컬렉션이 존재하지 않습니다.")
                        .data(false)
                        .build());
            }

            boolean result = vectorSearchService.saveProfileToVectorDb(request);

            Response<Boolean> response = Response.<Boolean>builder()
                    .status(result ? Status.OK : Status.FAIL)
                    .message(result ? "임베딩 생성 성공" : "임베딩 생성 실패")
                    .data(result)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("LinkedIn 프로필 임베딩 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 임베딩 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 배치 이벤트 수신 처리 (Kafka 이벤트 처리)
     */
//    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    @PostMapping("/linkedin/batch-event")
    @Operation(summary = "LinkedIn 프로필 배치 임베딩", description = "배치 이벤트 수신 시 프로필 임베딩을 처리합니다. (Kafka 이벤트 테스트용)")
    public ResponseEntity<Response<String>> processBatchEvent(
            @Parameter(description = "배치 이벤트 메시지", required = true) @RequestBody String eventMessage) {

        log.info("LinkedIn 프로필 배치 이벤트 수신: {}", eventMessage);

        try {
            // 여기서는 단순히 이벤트 수신 확인만 하고, 실제 처리는 Kafka 리스너에서 수행
            Response<String> response = Response.<String>builder()
                    .status(Status.OK)
                    .data("배치 이벤트 수신 완료: " + eventMessage)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("LinkedIn 프로필 배치 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "배치 이벤트 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 태그 기반 유사 LinkedIn 프로필 검색
     */
//    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping("/linkedin/similar")
    @Operation(summary = "유사 LinkedIn 프로필 검색", description = "콘텐츠 태그를 기반으로 유사한 LinkedIn 프로필을 검색합니다.")
    public CompletableFuture<ResponseEntity<Response<LinkedInSimilarityResponseDTO>>> searchSimilarProfiles(
            @Parameter(description = "콘텐츠 일련번호", required = true) @RequestParam Long contentSeq,
            @Parameter(description = "콘텐츠 태그 (쉼표로 구분)", required = true) @RequestParam String tags,
            @Parameter(description = "최소 유사도 점수 (0-1)") @RequestParam(required = false) Float minScore,
            @Parameter(description = "검색할 프로필 수") @RequestParam(required = false) Integer limit,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "3") Integer pageSize) {

        log.info("유사 LinkedIn 프로필 검색 요청 - 콘텐츠: {}, 태그: {}, 페이지: {}/{}", contentSeq, tags, page, pageSize);

        try {
            // 컬렉션 존재 확인
            if (!vectorSearchService.ensureCollectionExists()) {
                CompletableFuture<ResponseEntity<Response<LinkedInSimilarityResponseDTO>>> future = new CompletableFuture<>();
                future.complete(ResponseEntity.ok(Response.<LinkedInSimilarityResponseDTO>builder()
                        .status(Status.FAIL)
                        .message("벡터 데이터베이스 컬렉션이 존재하지 않습니다.")
                        .build()));
                return future;
            }

            LinkedInSimilarityRequestDTO request = LinkedInSimilarityRequestDTO.builder()
                    .contentSeq(contentSeq)
                    .tags(tags)
                    .minScore(minScore)
                    .limit(limit)
                    .page(page)
                    .pageSize(pageSize)
                    .build();

            return vectorSearchService.searchSimilarProfiles(request)
                    .thenApply(result -> {
                        log.debug("result.toString() : " + result.toString());
                        Response<LinkedInSimilarityResponseDTO> response = Response.<LinkedInSimilarityResponseDTO>builder()
                                .status(Status.OK)
                                .data(result)
                                .build();

                        return ResponseEntity.ok(response);
                    })
                    .exceptionally(e -> {
                        log.error("유사 LinkedIn 프로필 검색 중 오류 발생: {}", e.getMessage(), e);
                        Response<LinkedInSimilarityResponseDTO> response = Response.<LinkedInSimilarityResponseDTO>builder()
                                .status(Status.FAIL)
                                .message("유사 LinkedIn 프로필 검색 중 오류가 발생했습니다: " + e.getMessage())
                                .build();
                        return ResponseEntity.ok(response);
                    });
        } catch (Exception e) {
            log.error("유사 LinkedIn 프로필 검색 요청 처리 중 오류 발생: {}", e.getMessage(), e);
            CompletableFuture<ResponseEntity<Response<LinkedInSimilarityResponseDTO>>> future = new CompletableFuture<>();
            future.complete(ResponseEntity.ok(Response.<LinkedInSimilarityResponseDTO>builder()
                    .status(Status.FAIL)
                    .message("유사 LinkedIn 프로필 검색 요청 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build()));
            return future;
        }
    }

    /**
     * 지정된 LinkedIn 프로필 목록 일괄 임베딩
     */
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/linkedin/batch")
    @Operation(summary = "LinkedIn 프로필 일괄 임베딩", description = "지정된 LinkedIn 프로필 목록을 일괄적으로 임베딩합니다.")
    public CompletableFuture<ResponseEntity<Response<LinkedInEmbeddingBatchService.BatchProcessResult>>> batchProcessProfiles(
            @Parameter(description = "프로필 일련번호 목록", required = true) @RequestBody List<Long> profileSeqs) {

        log.info("LinkedIn 프로필 일괄 임베딩 요청 - 프로필 수: {}", profileSeqs.size());

        if (profileSeqs.isEmpty()) {
            CompletableFuture<ResponseEntity<Response<LinkedInEmbeddingBatchService.BatchProcessResult>>> future = new CompletableFuture<>();
            future.complete(ResponseEntity.badRequest().body(
                    Response.<LinkedInEmbeddingBatchService.BatchProcessResult>builder()
                            .status(Status.FAIL)
                            .message("프로필 목록이 비어있습니다.")
                            .build()
            ));
            return future;
        }

        return batchService.processBatch(profileSeqs)
                .thenApply(result -> {
                    Status status = result.getFailedCount() == 0 ? Status.OK : Status.FAIL;
                    String message = null;

                    if (result.getErrorMessage() != null) {
                        status = Status.FAIL;
                        message = result.getErrorMessage();
                    } else if (result.getFailedCount() > 0) {
                        message = String.format("일부 프로필 처리에 실패했습니다. 성공: %d, 실패: %d",
                                result.getSuccessCount(), result.getFailedCount());
                    }

                    Response<LinkedInEmbeddingBatchService.BatchProcessResult> response = Response.<LinkedInEmbeddingBatchService.BatchProcessResult>builder()
                            .status(status)
                            .message(message)
                            .data(result)
                            .build();

                    return ResponseEntity.ok(response);
                })
                .exceptionally(e -> {
                    log.error("일괄 처리 중 오류 발생: {}", e.getMessage(), e);
                    Response<LinkedInEmbeddingBatchService.BatchProcessResult> response = Response.<LinkedInEmbeddingBatchService.BatchProcessResult>builder()
                            .status(Status.FAIL)
                            .message("일괄 처리 중 오류가 발생했습니다: " + e.getMessage())
                            .build();
                    return ResponseEntity.ok(response);
                });
    }
}