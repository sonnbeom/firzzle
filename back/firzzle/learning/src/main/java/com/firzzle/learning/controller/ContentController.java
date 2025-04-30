package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.request.PageRequestDTO;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ContentRequestDTO;
import com.firzzle.learning.dto.ContentResponseDTO;
import com.firzzle.learning.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Class Name : ContentController.java
 * @Description : 콘텐츠 관리 API 컨트롤러
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "콘텐츠 관리 API", description = "콘텐츠 등록, 조회 등 콘텐츠 관리 관련 API")
public class ContentController {

    private final ContentService contentService;

    /**
     * 콘텐츠 등록
     *
     * @param contentRequestDTO - 콘텐츠 등록 요청 정보 (YouTube URL 포함)
     * @return ResponseEntity<Response<ContentResponseDTO>> - 등록된 콘텐츠 정보
     */
    @PostMapping
    @Operation(summary = "콘텐츠 등록", description = "새 영상을 등록하고 분석 큐에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "콘텐츠 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ContentResponseDTO>> registerContent(
            @Parameter(description = "콘텐츠 등록 정보", required = true)
            @Valid @RequestBody ContentRequestDTO contentRequestDTO) {

        log.info("콘텐츠 등록 요청 - URL: {}", contentRequestDTO.getYoutubeUrl());

        try {
            ContentResponseDTO registeredContent = contentService.insertContent(contentRequestDTO);

            Response<ContentResponseDTO> response = Response.<ContentResponseDTO>builder()
                    .status(Status.OK)
                    .message("콘텐츠가 성공적으로 등록되었습니다.")
                    .data(registeredContent)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            log.error("콘텐츠 등록 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("콘텐츠 등록 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 정보 조회
     *
     * @param contentSeq - 조회할 콘텐츠 일련번호
     * @return ResponseEntity<Response<ContentResponseDTO>> - 조회된 콘텐츠 정보
     */
    @GetMapping("/{contentSeq}")
    @Operation(summary = "콘텐츠 정보 조회", description = "영상 정보와 분석 정보를 일괄 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘텐츠 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ContentResponseDTO>> getContentInfo(
            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long contentSeq) {

        log.info("콘텐츠 정보 조회 요청 - 콘텐츠 일련번호: {}", contentSeq);

        try {
            ContentResponseDTO contentResponseDTO = contentService.selectContent(contentSeq);

            Response<ContentResponseDTO> response = Response.<ContentResponseDTO>builder()
                    .status(Status.OK)
                    .data(contentResponseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.error("콘텐츠 정보 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("콘텐츠 정보 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 정보 조회 중 오류가 발생했습니다.");
        }
    }

//    /**
//     * 콘텐츠 정보 조회
//     *
//     * @param contentSeq - 조회할 콘텐츠 일련번호
//     * @return ResponseEntity<Response<ContentResponseDTO>> - 조회된 콘텐츠 정보
//     */
//    @GetMapping("/{contentSeq}")
//    @Operation(summary = "콘텐츠 정보 조회", description = "영상 정보와 분석 정보를 일괄 조회합니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "콘텐츠 조회 성공"),
//            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    public ResponseEntity<Response<DataBox>> getContentInfo(
//            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long contentSeq
//            , HttpServletRequest request
//    ) {
//
//        log.info("콘텐츠 정보 조회 요청 - 콘텐츠 일련번호: {}", contentSeq);
//
//        try {
//            RequestBox box = RequestManager.getBox(request);
//            box.put("contentSeq", contentSeq); // @PathVariable 은 직접
//            DataBox contentResponseDTO = contentService.selectContent(box);
//
//            Response<DataBox> response = Response.<DataBox>builder()
//                    .status(Status.OK)
//                    .data(contentResponseDTO)
//                    .build();
//
//            return ResponseEntity.ok(response);
//        } catch (BusinessException e) {
//            log.error("콘텐츠 정보 조회 중 비즈니스 예외 발생: {}", e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("콘텐츠 정보 조회 중 예외 발생: {}", e.getMessage(), e);
//            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 정보 조회 중 오류가 발생했습니다.");
//        }
//    }

    /**
     * 콘텐츠 목록 조회
     *
     * @param pageRequestDTO - 페이지 요청 정보
     * @return ResponseEntity<Response<List<ContentResponseDTO>>> - 조회된 콘텐츠 목록
     */
    @GetMapping
    @Operation(summary = "콘텐츠 목록 조회", description = "등록된 영상 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘텐츠 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<List<ContentResponseDTO>>> getContentList(
            @Parameter(description = "페이지 요청 정보") PageRequestDTO pageRequestDTO) {

        log.info("콘텐츠 목록 조회 요청 - 페이지: {}, 사이즈: {}",
                pageRequestDTO.getPageNumber(), pageRequestDTO.getPageSize());

        try {
            List<ContentResponseDTO> contentList = contentService.selectContentList(pageRequestDTO);

            Response<List<ContentResponseDTO>> response = Response.<List<ContentResponseDTO>>builder()
                    .status(Status.OK)
                    .data(contentList)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.error("콘텐츠 목록 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("콘텐츠 목록 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 태그별 콘텐츠 목록 조회
     *
     * @param tag - 조회할 태그
     * @param pageRequestDTO - 페이지 요청 정보
     * @return ResponseEntity<Response<List<ContentResponseDTO>>> - 조회된 콘텐츠 목록
     */
    @GetMapping("/tags/{tag}")
    @Operation(summary = "태그별 콘텐츠 목록 조회", description = "특정 태그를 가진 영상 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그별 콘텐츠 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<List<ContentResponseDTO>>> getContentListByTag(
            @Parameter(description = "조회할 태그", required = true) @PathVariable("tag") String tag,
            @Parameter(description = "페이지 요청 정보") PageRequestDTO pageRequestDTO) {

        log.info("태그별 콘텐츠 목록 조회 요청 - 태그: {}, 페이지: {}, 사이즈: {}",
                tag, pageRequestDTO.getPageNumber(), pageRequestDTO.getPageSize());

        try {
            List<ContentResponseDTO> contentList = contentService.selectContentListByTag(tag, pageRequestDTO);

            Response<List<ContentResponseDTO>> response = Response.<List<ContentResponseDTO>>builder()
                    .status(Status.OK)
                    .data(contentList)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.error("태그별 콘텐츠 목록 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("태그별 콘텐츠 목록 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "태그별 콘텐츠 목록 조회 중 오류가 발생했습니다.");
        }
    }
}