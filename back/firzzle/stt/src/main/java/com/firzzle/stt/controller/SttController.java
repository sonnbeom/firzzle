package com.firzzle.stt.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firzzle.stt.dto.SttRequestDTO;
import com.firzzle.stt.service.ScriptProcessorService;

import lombok.RequiredArgsConstructor;


/**
 * @Class Name : SttController.java
 * @Description : 콘텐츠 스크립트 추출 API 컨트롤러
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@RestController
@RequestMapping("/api/v1/stt")
@RequiredArgsConstructor
@Tag(name = "STT", description = "유튜브 자막 추출 API")
public class SttController {
    private static final Logger logger = LoggerFactory.getLogger(SttController.class);

    private final ScriptProcessorService sttService;

    @PostMapping("/transcribeByUrl")
    @Operation(summary = "유튜브 영상 스크립트 추출", description = "userSeq와 유튜브 URL을 받아 스크립트를 추출합니다.")
    public ResponseEntity<?> transcribeByYoutubeUrl(@RequestBody SttRequestDTO request) {
        try {
            return ResponseEntity.ok(sttService.transcribeFromYoutube(request.getUuid(), request.getUrl(), ""));
        } catch (BusinessException e) {
            logger.error("유튜브 스크립트 추출 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("유튜브 스크립트 추출 중 알 수 없는 예외 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스크립트 수집 중 오류가 발생했습니다.");
        }
    }
}
