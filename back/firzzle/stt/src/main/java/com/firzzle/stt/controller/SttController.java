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

import com.firzzle.stt.service.SttService;

import lombok.RequiredArgsConstructor;


/**
 * @Class Name : SttController.java
 * @Description : 콘텐츠 스크립트 추출 API 컨트롤러
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@RestController
@RequestMapping("/api/stt")
@RequiredArgsConstructor
public class SttController {
	private static final Logger logger = LoggerFactory.getLogger(SttController.class);
	
    private final SttService sttService;
    
    @PostMapping("/transcribeByUrl")
    @Operation(summary = "유튜브 영상 스크립트 추출", description = "유튜브 영상에서 스크립트를 추출합니다.")
    public ResponseEntity<?> transcribeByYoutubeUrl(@RequestParam("url") String youtubeUrl) {
        try {
            return ResponseEntity.ok(sttService.transcribeFromYoutube(youtubeUrl));
        }catch (BusinessException e) {
            logger.error("유튜브 스크립트 추출 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("유튜브 스크립트 추출 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스크립트 수집 중 오류가 발생했습니다.");
        }
    }
    
// 파일을 통한 STT 변환 STT 테스트용 API     
//    @PostMapping("/transcribe")
//    @Operation(summary = "파일 STT 변환", description = "파일을 등록하여 STT 변환을 실행합니다.")
//    public ResponseEntity<?> transcribe(@RequestParam("file") MultipartFile file) throws Exception{
//        return ResponseEntity.ok(sttService.transcribeFromFile(file));
//    }
}
