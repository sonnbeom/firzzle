package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.common.response.PageResponseDTO;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ContentRequestDTO;
import com.firzzle.learning.dto.ContentResponseDTO;
import com.firzzle.learning.dto.ContentSearchDTO;
import com.firzzle.learning.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.firzzle.common.logging.dto.UserActionLog.*;
import static com.firzzle.common.logging.service.LoggingService.*;

/**
 * @Class Name : ContentController.java
 * @Description : 콘텐츠 관리 API 컨트롤러
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "콘텐츠 관리 API", description = "콘텐츠 등록, 조회 등 콘텐츠 관리 관련 API")
public class ContentController {

    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    private final ContentService contentService;

    /**
     * 콘텐츠 등록
     */
    @PostMapping(produces = "application/json;charset=UTF-8")
    @Operation(summary = "콘텐츠 등록", description = "새 영상을 등록하고 분석 큐에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "콘텐츠 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    public ResponseEntity<Response<ContentResponseDTO>> registerContent(
            @Parameter(description = "콘텐츠 등록 정보", required = true)  @Valid @RequestBody ContentRequestDTO contentRequestDTO,
            HttpServletRequest request
//            , @RequestBody Map<String, Object> requestBody // ContentRequestDTO를 없앤다면
    ) {
        logger.info("콘텐츠 등록 요청 - URL: {}", contentRequestDTO.getYoutubeUrl());

        try {
            RequestBox box = RequestManager.getBox(request);
            // POST
            // Form Submit 방식 => Content-Type: application/x-www-form-urlencoded 또는 multipart/form-data
            // JSON Fetch 방식 => Content-Type: application/json
            // => RequestManager.getBox 에서 자동 처리 못해줌으로 생략 불가
            box.put("youtubeUrl", contentRequestDTO.getYoutubeUrl());
            box.put("title", contentRequestDTO.getTitle() != null ? contentRequestDTO.getTitle() : "");
            box.put("description", contentRequestDTO.getDescription() != null ? contentRequestDTO.getDescription() : "");
            box.put("category", contentRequestDTO.getCategory() != null ? contentRequestDTO.getCategory() : "");
            box.put("tags", contentRequestDTO.getTags() != null ? contentRequestDTO.getTags() : "");

            DataBox dataBox = contentService.insertContent(box);

            Response<ContentResponseDTO> response;

            if (dataBox == null) {
                // 콘텐츠가 생성 중인 경우
                response = Response.<ContentResponseDTO>builder()
                        .status(Status.OK)
                        .message("콘텐츠가 생성 중입니다. 잠시 후 확인해주세요.")
                        .data(null)
                        .build();
            } else {
                // 콘텐츠가 이미 생성된 경우
                ContentResponseDTO registeredContent = convertToContentResponseDTO(dataBox);

                response = Response.<ContentResponseDTO>builder()
                        .status(Status.OK)
                        .message("콘텐츠가 성공적으로 등록되었습니다.")
                        .data(registeredContent)
                        .build();
            }

            // 컨텐츠 생성 로깅 => ELK
            String userId = box.getString("uuid");
            log(userActionLog(userId, "CONTENT_CREATED"));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            logger.error("콘텐츠 등록 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 등록 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 정보 조회
     */
    @GetMapping(value = "/{contentSeq}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "콘텐츠 정보 조회", description = "영상 정보와 분석 정보를 일괄 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘텐츠 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<ContentResponseDTO>> getContentInfo(
            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        logger.info("콘텐츠 정보 조회 요청 - 콘텐츠 일련번호: {}", userContentSeq);
        LoggingService.log(UserActionLog.userActionLog("TEST_ID", "CONTENT_CREATED"));
        try {
            // PathVariable => Content-Type 관계 없이 넣어줘야 함
            RequestBox box = RequestManager.getBox(request);
            box.put("userContentSeq", userContentSeq);

            DataBox dataBox = contentService.selectContent(box);
            ContentResponseDTO contentResponseDTO = convertToContentResponseDTO(dataBox);

            Response<ContentResponseDTO> response = Response.<ContentResponseDTO>builder()
                    .status(Status.OK)
                    .data(contentResponseDTO)
                    .build();

            // 컨텐츠 조회 로깅 => ELK
            String userId = box.getString("uuid");
            log(userActionLog(userId, "START_LEARNING"));

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("콘텐츠 정보 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 정보 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 목록 조회
     */
    @GetMapping(produces = "application/json;charset=UTF-8")
    @Operation(summary = "콘텐츠 목록 조회", description = "등록된 영상 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘텐츠 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<PageResponseDTO<ContentResponseDTO>>> getContentList(
            @Parameter(description = "검색 및 페이지 요청 정보") ContentSearchDTO searchDTO,
            HttpServletRequest request) {

        logger.info("콘텐츠 목록 조회 요청 - 페이지: {}, 사이즈: {}, 키워드: {}",
                searchDTO.getP_pageno(), searchDTO.getP_pagesize(), searchDTO.getKeyword());

        try {
            RequestBox box = RequestManager.getBox(request);
            // get 요청 => json 형태가 아니므로 RequestManager.getBox 에서 자동 처리
//            box.put("p_pageno", searchDTO.getP_pageno());
//            box.put("p_pagesize", searchDTO.getP_pagesize());
//            box.put("p_order", searchDTO.getP_order());
//            box.put("p_sortorder", searchDTO.getP_sortorder());
//            box.put("keyword", searchDTO.getKeyword());
//            box.put("category", searchDTO.getCategory());
//            box.put("status", searchDTO.getStatus());

            List<DataBox> contentListDataBox = contentService.selectContentList(box);
            int totalCount = contentService.selectContentCount(box);

            List<ContentResponseDTO> contentList = convertToContentResponseDTOList(contentListDataBox);

            PageResponseDTO<ContentResponseDTO> pageResponse = PageResponseDTO.<ContentResponseDTO>builder()
                    .content(contentList)
                    .p_pageno(searchDTO.getP_pageno())
                    .p_pagesize(searchDTO.getP_pagesize())
                    .totalElements(totalCount)
                    .build();

            Response<PageResponseDTO<ContentResponseDTO>> response = Response.<PageResponseDTO<ContentResponseDTO>>builder()
                    .status(Status.OK)
                    .data(pageResponse)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("콘텐츠 목록 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 목록 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 태그별 콘텐츠 목록 조회
     */
    @Deprecated
    @GetMapping(value = "/tags/{tag}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "태그별 콘텐츠 목록 조회", description = "특정 태그를 가진 영상 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그별 콘텐츠 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<PageResponseDTO<ContentResponseDTO>>> getContentListByTag(
            @Parameter(description = "조회할 태그", required = true) @PathVariable("tag") String tag,
            @Parameter(description = "검색 및 페이지 요청 정보") ContentSearchDTO searchDTO,
            HttpServletRequest request) {

        logger.info("태그별 콘텐츠 목록 조회 요청 - 태그: {}, 페이지: {}, 사이즈: {}",
                tag, searchDTO.getP_pageno(), searchDTO.getP_pagesize());

        try {
            RequestBox box = RequestManager.getBox(request);
//            box.put("p_pageno", searchDTO.getP_pageno());
//            box.put("p_pagesize", searchDTO.getP_pagesize());
//            box.put("p_order", searchDTO.getP_order());
//            box.put("p_sortorder", searchDTO.getP_sortorder());
//            box.put("keyword", searchDTO.getKeyword());
//            box.put("category", searchDTO.getCategory());
//            box.put("status", searchDTO.getStatus());
//            box.put("tag", tag);

            List<DataBox> contentListDataBox = contentService.selectContentListByTag(box);
            int totalCount = contentService.selectContentCountByTag(box);

            List<ContentResponseDTO> contentList = convertToContentResponseDTOList(contentListDataBox);

            PageResponseDTO<ContentResponseDTO> pageResponse = PageResponseDTO.<ContentResponseDTO>builder()
                    .content(contentList)
                    .p_pageno(searchDTO.getP_pageno())
                    .p_pagesize(searchDTO.getP_pagesize())
                    .totalElements(totalCount)
                    .build();

            Response<PageResponseDTO<ContentResponseDTO>> response = Response.<PageResponseDTO<ContentResponseDTO>>builder()
                    .status(Status.OK)
                    .data(pageResponse)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("태그별 콘텐츠 목록 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("태그별 콘텐츠 목록 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "태그별 콘텐츠 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * DataBox를 ContentResponseDTO로 변환
     */
    private ContentResponseDTO convertToContentResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }
        LocalDateTime indate = parseDateTime(dataBox.getString("d_indate"));
        String formattedIndate = indate != null ?
                indate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
        LocalDateTime completedAt = parseDateTime(dataBox.getString("d_completed_at"));
        String formattedCompletedAt = completedAt != null ?
                completedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
        try {
            return ContentResponseDTO.builder()
                    .contentSeq(dataBox.getLong2("d_content_seq"))
                    .title(dataBox.getString("d_title"))
                    .description(dataBox.getString("d_description"))
                    .contentType(dataBox.getString("d_category"))
                    .videoId(dataBox.getString("d_video_id"))
                    .url(dataBox.getString("d_url"))
                    .thumbnailUrl(dataBox.getString("d_thumbnail_url"))
                    .duration(dataBox.getInt2("d_duration"))
                    .processStatus(dataBox.getString("d_process_status"))
                    .tags(dataBox.getString("d_tags"))
                    .analysisData(dataBox.getString("d_analysis_data"))
                    .transcript(dataBox.getString("d_transcript"))
                    .indate(formattedIndate)
                    .completedAt(formattedCompletedAt)
                    .deleteYn(dataBox.getString("d_delete_yn"))
                    .build();

        } catch (Exception e) {
            logger.error("ContentResponseDTO 변환 중 오류 발생: {}", e.getMessage(), e);
            return new ContentResponseDTO();
        }
    }

    /**
     * DataBox 리스트를 ContentResponseDTO 리스트로 변환
     */
    private List<ContentResponseDTO> convertToContentResponseDTOList(List<DataBox> dataBoxList) {
        if (dataBoxList == null) {
            return new ArrayList<>();
        }

        List<ContentResponseDTO> result = new ArrayList<>();
        for (DataBox dataBox : dataBoxList) {
            result.add(convertToContentResponseDTO(dataBox));
        }

        return result;
    }

    /**
     * FormatDate를 사용하여 YYYYMMDDHHMMSS -> LocalDateTime 변환
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            // YYYYMMDDHHMMSS -> "YYYY-MM-DD HH:MM:SS" 형식으로 변환
            String formattedDateTime = FormatDate.getFormatDate(dateTimeStr, "yyyy-MM-dd HH:mm:ss");

            if (formattedDateTime == null || formattedDateTime.isEmpty()) {
                return null;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(formattedDateTime, formatter);
        } catch (Exception e) {
            logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}