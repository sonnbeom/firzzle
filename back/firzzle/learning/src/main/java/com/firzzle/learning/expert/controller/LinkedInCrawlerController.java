package com.firzzle.learning.expert.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.expert.dao.LinkedInProfileDAO;
import com.firzzle.learning.expert.dto.LinkedInCrawlerRequestDTO;
import com.firzzle.learning.expert.dto.LinkedInCrawlerResponseDTO;
import com.firzzle.learning.expert.dto.LinkedInProfileDTO;
import com.firzzle.learning.expert.service.LinkedInCrawlingIntegrationService;
import com.firzzle.learning.expert.service.LinkedInProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @Class Name : LinkedInCrawlerController.java
 * @Description : LinkedIn 프로필 크롤링 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expert/linkedin")
@Tag(name = "LinkedIn 프로필 크롤링 API", description = "LinkedIn 프로필 크롤링 관련 API")
public class LinkedInCrawlerController {

    private final Logger logger = LoggerFactory.getLogger(LinkedInCrawlerController.class);

    private final LinkedInCrawlingIntegrationService linkedInCrawlingService;
    private final LinkedInProfileService linkedInProfileService;

    /**
     * LinkedIn 프로필 크롤링 실행
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    @PostMapping("/crawl")
    @Operation(summary = "LinkedIn 프로필 크롤링", description = "키워드 또는 URL 목록을 기반으로 LinkedIn 프로필을 크롤링합니다.")
    public ResponseEntity<Response<LinkedInCrawlerResponseDTO>> crawlLinkedInProfiles(
            @Parameter(description = "크롤링 요청 정보", required = true) @RequestBody LinkedInCrawlerRequestDTO requestDTO) {

        logger.info("LinkedIn 프로필 크롤링 요청 - 키워드: {}, 제한: {}",
                requestDTO.getKeyword(), requestDTO.getLimit());

        try {
            // 요청 검증
            if ((requestDTO.getKeyword() == null || requestDTO.getKeyword().isEmpty()) &&
                    (requestDTO.getLinkedinUrls() == null || requestDTO.getLinkedinUrls().isEmpty())) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "검색 키워드 또는 LinkedIn URL 목록이 필요합니다.");
            }

            // 크롤링 제한 설정 (기본값: 3)
            if (requestDTO.getLimit() == null || requestDTO.getLimit() <= 0) {
                requestDTO.setLimit(10);
            }

            // 크롤링 서비스 호출
            LinkedInCrawlerResponseDTO responseDTO = linkedInCrawlingService.crawlAndSaveLinkedInProfiles(requestDTO);

            // 응답 생성
            Response<LinkedInCrawlerResponseDTO> response = Response.<LinkedInCrawlerResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 크롤링 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 크롤링 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 크롤링 중 오류가 발생했습니다.");
        }
    }

    /**
     * LinkedIn 프로필 목록 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping
    @Operation(summary = "LinkedIn 프로필 목록 조회", description = "저장된 LinkedIn 프로필 목록을 조회합니다.")
    public ResponseEntity<Response<LinkedInCrawlerResponseDTO>> getLinkedInProfiles(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "회사명") @RequestParam(required = false) String company,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") Integer limit) {

        logger.info("LinkedIn 프로필 목록 조회 요청 - 키워드: {}, 회사: {}, 페이지: {}, 크기: {}",
                keyword, company, page, limit);

        try {
            LinkedInCrawlerRequestDTO searchDTO = LinkedInCrawlerRequestDTO.builder()
                    .keyword(keyword)
                    .company(company)
                    .page(page)
                    .limit(limit)
                    .build();

            LinkedInCrawlerResponseDTO responseDTO = linkedInProfileService.getLinkedInProfiles(searchDTO);

            Response<LinkedInCrawlerResponseDTO> response = Response.<LinkedInCrawlerResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 목록 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 목록 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * LinkedIn 프로필 상세 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping("/{profileSeq}")
    @Operation(summary = "LinkedIn 프로필 상세 조회", description = "특정 LinkedIn 프로필의 상세 정보를 조회합니다.")
    public ResponseEntity<Response<LinkedInProfileDTO>> getLinkedInProfile(
            @Parameter(description = "LinkedIn 프로필 일련번호", required = true) @PathVariable Long profileSeq) {

        logger.info("LinkedIn 프로필 상세 조회 요청 - 프로필 일련번호: {}", profileSeq);

        try {
            LinkedInProfileDTO profileDTO = linkedInProfileService.getLinkedInProfileBySeq(profileSeq);

            Response<LinkedInProfileDTO> response = Response.<LinkedInProfileDTO>builder()
                    .status(Status.OK)
                    .data(profileDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 상세 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 상세 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 상세 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * LinkedIn 프로필 URL로 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping("/url")
    @Operation(summary = "LinkedIn 프로필 URL로 조회", description = "LinkedIn URL로 프로필을 조회합니다.")
    public ResponseEntity<Response<LinkedInProfileDTO>> getLinkedInProfileByUrl(
            @Parameter(description = "LinkedIn URL", required = true) @RequestParam String linkedinUrl) {

        logger.info("LinkedIn 프로필 URL로 조회 요청 - URL: {}", linkedinUrl);

        try {
            LinkedInProfileDTO profileDTO = linkedInProfileService.getLinkedInProfileByUrl(linkedinUrl);

            Response<LinkedInProfileDTO> response = Response.<LinkedInProfileDTO>builder()
                    .status(Status.OK)
                    .data(profileDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 URL로 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 URL로 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 URL로 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * LinkedIn 프로필 삭제
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    @DeleteMapping("/{profileSeq}")
    @Operation(summary = "LinkedIn 프로필 삭제", description = "LinkedIn 프로필을 삭제합니다. (논리적 삭제)")
    public ResponseEntity<Response<Boolean>> deleteLinkedInProfile(
            @Parameter(description = "LinkedIn 프로필 일련번호", required = true) @PathVariable Long profileSeq) {

        logger.info("LinkedIn 프로필 삭제 요청 - 프로필 일련번호: {}", profileSeq);

        try {
            boolean result = linkedInProfileService.deleteLinkedInProfile(profileSeq);

            Response<Boolean> response = Response.<Boolean>builder()
                    .status(Status.OK)
                    .data(result)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 삭제 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 삭제 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 키워드 기반 LinkedIn 프로필 검색
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping("/search")
    @Operation(summary = "LinkedIn 프로필 검색", description = "키워드를 기반으로 LinkedIn 프로필을 검색합니다.")
    public ResponseEntity<Response<LinkedInCrawlerResponseDTO>> searchLinkedInProfiles(
            @Parameter(description = "검색 키워드", required = true) @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") Integer limit) {

        logger.info("LinkedIn 프로필 검색 요청 - 키워드: {}, 페이지: {}, 크기: {}", keyword, page, limit);

        try {
            LinkedInCrawlerResponseDTO responseDTO = linkedInCrawlingService.searchLinkedInProfiles(keyword, page, limit);

            Response<LinkedInCrawlerResponseDTO> response = Response.<LinkedInCrawlerResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 검색 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 검색 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 검색 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 연계 LinkedIn 프로필 크롤링
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    @PostMapping("/contents/{contentSeq}/crawl")
    @Operation(summary = "콘텐츠 연계 LinkedIn 프로필 크롤링", description = "콘텐츠와 관련된 키워드로 LinkedIn 프로필을 크롤링합니다.")
    public ResponseEntity<Response<LinkedInCrawlerResponseDTO>> crawlContentRelatedProfiles(
            @Parameter(description = "콘텐츠 일련번호", required = true) @PathVariable Long contentSeq,
            @Parameter(description = "크롤링 요청 정보") @RequestBody(required = false) LinkedInCrawlerRequestDTO requestDTO) {

        logger.info("콘텐츠 연계 LinkedIn 프로필 크롤링 요청 - 콘텐츠 일련번호: {}", contentSeq);

        try {
            // RequestDTO 생성 또는 업데이트
            if (requestDTO == null) {
                requestDTO = new LinkedInCrawlerRequestDTO();
            }

            // 콘텐츠 일련번호 설정
            requestDTO.setContentSeq(contentSeq);

            // 키워드나 URL이 없는 경우 콘텐츠에서 키워드 추출 (예시)
            if ((requestDTO.getKeyword() == null || requestDTO.getKeyword().isEmpty()) &&
                    (requestDTO.getLinkedinUrls() == null || requestDTO.getLinkedinUrls().isEmpty())) {
                // 실제로는 콘텐츠 서비스를 통해 키워드를 가져와야 합니다.
                // 예: contentService.getContentKeywords(contentSeq)
                requestDTO.setKeyword("content " + contentSeq + " keywords");
            }

            // 크롤링 제한 설정 (기본값: 3)
            if (requestDTO.getLimit() == null || requestDTO.getLimit() <= 0) {
                requestDTO.setLimit(3);
            }

            // 크롤링 서비스 호출
            LinkedInCrawlerResponseDTO responseDTO = linkedInCrawlingService.crawlAndSaveLinkedInProfiles(requestDTO);

            // 응답 생성
            Response<LinkedInCrawlerResponseDTO> response = Response.<LinkedInCrawlerResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("콘텐츠 연계 LinkedIn 프로필 크롤링 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 연계 LinkedIn 프로필 크롤링 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 연계 LinkedIn 프로필 크롤링 중 오류가 발생했습니다.");
        }
    }
}