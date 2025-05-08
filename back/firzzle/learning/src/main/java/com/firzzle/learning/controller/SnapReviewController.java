package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.PageResponseDTO;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.*;
import com.firzzle.learning.service.SnapReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Class Name : SnapReviewController.java
 * @Description : 스냅리뷰 관련 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "스냅리뷰 API", description = "콘텐츠 학습 스냅리뷰 관련 API")
public class SnapReviewController {

    private static final Logger logger = LoggerFactory.getLogger(SnapReviewController.class);

    private final SnapReviewService snapReviewService;

    /**
     * 스냅리뷰 조회
     * 사용자 콘텐츠에 대한 스냅리뷰를 조회합니다.
     *
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param request HTTP 요청 객체
     * @return 스냅리뷰 정보
     */
    @GetMapping(value = "/contents/{contentSeq}/snap-review", produces = "application/json;charset=UTF-8")
    @Operation(summary = "스냅리뷰 조회", description = "콘텐츠에 대한 스냅리뷰를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스냅리뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<SnapReviewResponseDTO>> getSnapReview(
            @Parameter(description = "콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        try {
            RequestBox box = RequestManager.getBox(request);
            logger.info("스냅리뷰 조회 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, box.getString("uuid"));

            box.put("userContentSeq", userContentSeq);

            DataBox dataBox = snapReviewService.selectContentWithFrames(box);
            SnapReviewResponseDTO responseDTO = convertToSnapReviewResponseDTO(dataBox);

            Response<SnapReviewResponseDTO> response = Response.<SnapReviewResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("스냅리뷰 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("스냅리뷰 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스냅리뷰 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 스냅리뷰 공유 여부 조회
     * 사용자 콘텐츠에 대한 스냅리뷰를 조회합니다.
     *
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param request HTTP 요청 객체
     * @return 쉐어코드 정보
     */
    @GetMapping(value = "/contents/{contentSeq}/share-code", produces = "application/json;charset=UTF-8")
    @Operation(summary = "스냅리뷰 공유 여부 조회", description = "스냅리뷰 공유 여부를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스냅리뷰 공유 여부 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<SnapReviewShareResponseDTO>> getShareCode(
            @Parameter(description = "콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {
        try {
            RequestBox box = RequestManager.getBox(request);
            logger.info("스냅리뷰 공유 여부 조회 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, box.getString("uuid"));
            box.put("userContentSeq", userContentSeq);

            DataBox dataBox = snapReviewService.selectShareCodeByContent(box);

            // 공유 코드가 없는 경우 204 No Content 반환
            if (dataBox == null) {
                return ResponseEntity.noContent().build();
            }

            SnapReviewShareResponseDTO responseDTO = convertToSnapReviewShareResponseDTO(dataBox);

            Response<SnapReviewShareResponseDTO> response = Response.<SnapReviewShareResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("스냅리뷰 공유 여부 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("스냅리뷰 공유 여부 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스냅리뷰 공유 여부 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 스냅리뷰 공유 취소
     * 사용자가 이전에 공유한 스냅리뷰의 공유를 취소합니다.
     *
     * @param shareCode 취소할 공유 코드
     * @return 공유 취소 결과
     */
    @DeleteMapping(value = "/share/{shareCode}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "스냅리뷰 공유 취소", description = "사용자가 이전에 공유한 스냅리뷰의 공유를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공유 취소 성공"),
            @ApiResponse(responseCode = "401", description = "인증 오류"),
            @ApiResponse(responseCode = "404", description = "공유 코드를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<DataBox>> cancelShareSnapReview(
            @Parameter(description = "취소할 공유 코드", required = true) @PathVariable("shareCode") String shareCode,
            HttpServletRequest request) {

        try {
            // RequestManager를 사용하여 box 객체 생성
            RequestBox box = RequestManager.getBox(request);
            box.put("shareCode", shareCode);

            logger.info("스냅리뷰 공유 취소 요청 - 공유 코드: {}", shareCode);

            DataBox resultBox = snapReviewService.cancelShareCode(box);

            Response<DataBox> response = Response.<DataBox>builder()
                    .status(Status.OK)
                    .message("공유가 성공적으로 취소되었습니다.")
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("스냅리뷰 공유 취소 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("스냅리뷰 공유 취소 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스냅리뷰 공유 취소 중 오류가 발생했습니다.");
        }
    }

    /**
     * 내 스냅리뷰 사진첩 목록 일별 그룹 조회
     * 사용자의 스냅리뷰 사진첩 목록을 일별로 그룹화하여 조회합니다.
     *
     * @param searchDTO 검색 및 페이징 요청 정보
     * @param request HTTP 요청 객체
     * @return 일별 그룹화된 스냅리뷰 사진첩 목록
     */
    @GetMapping(value = "/snap-reviews", produces = "application/json;charset=UTF-8")
    @Operation(summary = "내 스냅리뷰 사진첩 목록 일별 그룹 조회", description = "사용자의 스냅리뷰 사진첩 목록을 일별로 그룹화하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일별 그룹화된 스냅리뷰 사진첩 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<PageResponseDTO<SnapReviewDailyGroupResponseDTO>>> getMySnapReviewsByDaily(
            @Parameter(description = "페이지 요청 정보") SnapReviewSearchDTO searchDTO,
            HttpServletRequest request) {

        try {
            RequestBox box = RequestManager.getBox(request);
            logger.info("내 스냅리뷰 사진첩 목록 일별 그룹 조회 요청 - 페이지: {}, 사이즈: {}, UUID: {}",
                    searchDTO.getP_pageno(), searchDTO.getP_pagesize(), box.getString("uuid"));

            logger.debug("box.toString() : {}", box.toString());

            // 기존 서비스 메서드를 사용하여 데이터 조회
            List<DataBox> snapReviewListDataBox = snapReviewService.selectContentListWithFrames(box);
            int totalCount = snapReviewService.selectContentWithFramesCount(box);

            // 스냅리뷰 목록을 DTO로 변환 (날짜 포맷 변환 포함)
            List<SnapReviewListResponseDTO> snapReviewList = convertToSnapReviewListResponseDTO(snapReviewListDataBox);

            // 일별로 그룹화
            Map<String, List<SnapReviewListResponseDTO>> dailySnapReviews =
                    snapReviewList.stream()
                            .collect(Collectors.groupingBy(
                                    review -> {
                                        // substring 전에 문자열 길이 확인
                                        String date = review.getIndate();
                                        if (date != null && date.length() >= 10) {
                                            return date.substring(0, 10);  // YYYY-MM-DD 형식으로 그룹화
                                        } else {
                                            logger.warn("날짜 형식이 올바르지 않습니다: {}", date);
                                            return "날짜 없음";  // 날짜 형식이 올바르지 않은 경우 기본값 사용
                                        }
                                    },
                                    LinkedHashMap::new,  // 삽입 순서 유지를 위한 LinkedHashMap 사용
                                    Collectors.toList()
                            ));

            // 일별 그룹 DTO 생성
            SnapReviewDailyGroupResponseDTO dailyGroupResponse = SnapReviewDailyGroupResponseDTO.builder()
                    .dailySnapReviews(dailySnapReviews)
                    .totalDays(dailySnapReviews.size())
                    .build();

            // 페이지 응답 DTO 생성
            List<SnapReviewDailyGroupResponseDTO> contentList = Collections.singletonList(dailyGroupResponse);
            PageResponseDTO<SnapReviewDailyGroupResponseDTO> pageResponse = PageResponseDTO.<SnapReviewDailyGroupResponseDTO>builder()
                    .content(contentList)
                    .p_pageno(searchDTO.getP_pageno())
                    .p_pagesize(searchDTO.getP_pagesize())
                    .totalElements(totalCount)
                    .build();

            Response<PageResponseDTO<SnapReviewDailyGroupResponseDTO>> response = Response.<PageResponseDTO<SnapReviewDailyGroupResponseDTO>>builder()
                    .status(Status.OK)
                    .data(pageResponse)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("내 스냅리뷰 사진첩 목록 일별 그룹 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("내 스냅리뷰 사진첩 목록 일별 그룹 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "내 스냅리뷰 사진첩 목록 일별 그룹 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 스냅리뷰 공유 코드 생성
     * 스냅리뷰 공유를 위한 코드를 생성합니다.
     *
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param request HTTP 요청 객체
     * @return 생성된 공유 코드 정보
     */
    @PostMapping(value = "/contents/{contentSeq}/snap-review/share", produces = "application/json;charset=UTF-8")
    @Operation(summary = "스냅리뷰 공유 코드 생성", description = "스냅리뷰 공유를 위한 코드를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "공유 코드 생성 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    public ResponseEntity<Response<SnapReviewShareResponseDTO>> createShareCode(
            @Parameter(description = "콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        try {
            RequestBox box = RequestManager.getBox(request);
            logger.info("스냅리뷰 공유 코드 생성 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, box.getString("uuid"));

            box.put("userContentSeq", userContentSeq);

            DataBox dataBox = snapReviewService.createShareCode(box);
            SnapReviewShareResponseDTO responseDTO = convertToSnapReviewShareResponseDTO(dataBox);

            Response<SnapReviewShareResponseDTO> response = Response.<SnapReviewShareResponseDTO>builder()
                    .status(Status.OK)
                    .message("스냅리뷰 공유 코드가 성공적으로 생성되었습니다.")
                    .data(responseDTO)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            logger.error("스냅리뷰 공유 코드 생성 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("스냅리뷰 공유 코드 생성 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스냅리뷰 공유 코드 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 스냅리뷰 일괄 수정
     * 스냅리뷰의 프레임 정보를 일괄 수정합니다.
     *
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param frameUpdateRequestDTO 프레임 설명 수정 정보
     * @param request HTTP 요청 객체
     * @return 수정된 프레임 정보 목록
     */
    @PatchMapping(value = "/contents/{contentSeq}/snap-review", produces = "application/json;charset=UTF-8")
    @Operation(summary = "스냅리뷰 일괄 수정", description = "스냅리뷰의 프레임 정보를 일괄 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스냅리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    public ResponseEntity<Response<List<FrameResponseDTO>>> updateFrameDescriptions(
            @Parameter(description = "콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            @Parameter(description = "프레임 설명 수정 정보", required = true) @Valid @RequestBody FrameUpdateRequestDTO frameUpdateRequestDTO,
            HttpServletRequest request) {

        try {
            RequestBox box = RequestManager.getBox(request);
            logger.info("스냅리뷰 일괄 수정 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, box.getString("uuid"));

            box.put("userContentSeq", userContentSeq);

            // 개별 프레임 정보 설정
            for (int i = 0; i < frameUpdateRequestDTO.getFrames().size(); i++) {
                FrameUpdateRequestDTO.FrameInfo frame = frameUpdateRequestDTO.getFrames().get(i);
                box.put("frameSeq_" + i, frame.getFrameSeq());
                box.put("comment_" + i, frame.getComment());
            }
            box.put("frameCount", frameUpdateRequestDTO.getFrames().size());

            List<DataBox> updatedFrameListDataBox = snapReviewService.updateFrames(box);
            List<FrameResponseDTO> updatedFrameList = convertToFrameResponseDTO(updatedFrameListDataBox);

            Response<List<FrameResponseDTO>> response = Response.<List<FrameResponseDTO>>builder()
                    .status(Status.OK)
                    .message("스냅리뷰가 성공적으로 수정되었습니다.")
                    .data(updatedFrameList)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("스냅리뷰 일괄 수정 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("스냅리뷰 일괄 수정 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스냅리뷰 일괄 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 공유 코드로 스냅리뷰 조회
     * 공유 코드를 통해 스냅리뷰 정보를 조회합니다. 로그인하지 않은 사용자도 접근 가능합니다.
     *
     * @param shareCode 공유 코드
     * @return 스냅리뷰 정보
     */
    @GetMapping(value = "/share/{shareCode}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "공유 코드로 스냅리뷰 조회", description = "공유 코드를 통해 스냅리뷰 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스냅리뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "공유 코드를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<SnapReviewResponseDTO>> getSharedSnapReview(
            @Parameter(description = "공유 코드", required = true) @PathVariable("shareCode") String shareCode) {

        try {
            // 공유 코드 파라미터만 있으면 되므로 UUID는 필요 없음
            RequestBox box = new RequestBox("shareSnapReviewBox");
            box.put("shareCode", shareCode);
            logger.info("공유 코드로 스냅리뷰 조회 요청 - 공유 코드: {}", shareCode);

            DataBox dataBox = snapReviewService.selectSharedContentWithFrames(box);
            SnapReviewResponseDTO responseDTO = convertToSnapReviewResponseDTO(dataBox);

            Response<SnapReviewResponseDTO> response = Response.<SnapReviewResponseDTO>builder()
                        .status(Status.OK)
                        .data(responseDTO)
                        .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("공유 코드로 스냅리뷰 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("공유 코드로 스냅리뷰 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드로 스냅리뷰 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * DataBox를 SnapReviewResponseDTO로 변환
     *
     * @param dataBox 스냅리뷰 데이터
     * @return SnapReviewResponseDTO 변환된 DTO
     */
    private SnapReviewResponseDTO convertToSnapReviewResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        // 프레임 정보 변환
        List<SnapReviewResponseDTO.FrameDTO> frames = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<DataBox> frameDataBoxes = (List<DataBox>) dataBox.getObject("d_frames");
        if (frameDataBoxes != null) {
            for (DataBox frameDataBox : frameDataBoxes) {
                // 타임스탬프를 HH:MM:SS 형식으로 변환
                String formattedTimestamp = FormatDate.formatHourMinSec(frameDataBox.getInt2("d_timestamp"));

                SnapReviewResponseDTO.FrameDTO frame = SnapReviewResponseDTO.FrameDTO.builder()
                        .frameSeq(frameDataBox.getLong2("d_frame_seq"))
                        .imageUrl(frameDataBox.getString("d_image_url"))
                        .timestamp(frameDataBox.getInt2("d_timestamp"))
                        .comment(frameDataBox.getString("d_comment"))
                        .formattedTimestamp(formattedTimestamp)
                        .build();

                frames.add(frame);
            }
        }

        return SnapReviewResponseDTO.builder()
                .contentSeq(dataBox.getLong2("d_content_seq"))
                .contentTitle(dataBox.getString("d_title"))
                .thumbnailUrl(dataBox.getString("d_thumbnail_url"))
                .indate(parseDateTime(dataBox.getString("d_indate")))
                .frames(frames)
                .build();
    }

    /**
     * DataBox 형태의 스냅리뷰 목록을 DTO로 변환합니다.
     *
     * @param snapReviewListDataBox 데이터박스 형태의 스냅리뷰 목록
     * @return DTO 형태의 스냅리뷰 목록
     */
    private List<SnapReviewListResponseDTO> convertToSnapReviewListResponseDTO(List<DataBox> snapReviewListDataBox) {
        return snapReviewListDataBox.stream().map(dataBox -> {
            String originalIndate = dataBox.getString("d_indate");
            String formattedDate = originalIndate; // 기본값으로 원본 날짜 사용

            try {
                // FormatDate 클래스를 사용하여 날짜 포맷 변환 (20250510130000 -> 2025-05-10 13:00:00)
                formattedDate = FormatDate.getFormatDate(originalIndate, "yyyy-MM-dd HH:mm:ss");
            } catch (Exception e) {
                logger.error("날짜 포맷 변환 중 오류 발생: {}", e.getMessage(), e);
                // 예외 발생 시 원본값 사용 (이미 기본값으로 설정됨)
            }
            logger.debug(dataBox.toString());
            return SnapReviewListResponseDTO.builder()
                    .contentSeq(dataBox.getLong2("d_content_seq"))
                    .contentTitle(dataBox.getString("d_content_title"))
                    .category(dataBox.getString("d_category"))
                    .thumbnailUrl(dataBox.getString("d_thumbnail_url"))
                    .representativeImageUrl(dataBox.getString("d_representative_image_url"))
                    .indate(formattedDate) // FormatDate로 변환된 날짜 사용
                    .frameCount(dataBox.getInt2("d_frame_count"))
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * DataBox를 SnapReviewShareResponseDTO로 변환
     *
     * @param dataBox 스냅리뷰 공유 코드 데이터
     * @return SnapReviewShareResponseDTO 변환된 DTO
     */
    private SnapReviewShareResponseDTO convertToSnapReviewShareResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        return SnapReviewShareResponseDTO.builder()
                .shareCode(dataBox.getString("d_share_code"))
                .contentSeq(dataBox.getLong2("d_content_seq"))
                .shareUrl(dataBox.getString("d_share_url"))
                .indate(parseDateTime(dataBox.getString("d_indate")))
                .build();
    }

    /**
     * DataBox 리스트를 FrameResponseDTO 리스트로 변환
     *
     * @param dataBoxList 프레임 데이터 목록
     * @return List<FrameResponseDTO> 변환된 DTO 목록
     */
    private List<FrameResponseDTO> convertToFrameResponseDTO(List<DataBox> dataBoxList) {
        if (dataBoxList == null) {
            return new ArrayList<>();
        }

        List<FrameResponseDTO> result = new ArrayList<>();

        for (DataBox dataBox : dataBoxList) {
            // 타임스탬프를 HH:MM:SS 형식으로 변환
            String formattedTimestamp = FormatDate.formatHourMinSec(dataBox.getInt2("d_timestamp"));

            FrameResponseDTO dto = FrameResponseDTO.builder()
                    .frameSeq(dataBox.getLong2("d_frame_seq"))
                    .contentSeq(dataBox.getLong2("d_content_seq"))
                    .imageUrl(dataBox.getString("d_image_url"))
                    .timestamp(dataBox.getInt2("d_timestamp"))
                    .comment(dataBox.getString("d_comment"))
                    .formattedTimestamp(formattedTimestamp)
                    .indate(parseDateTime(dataBox.getString("d_indate")))
                    .ldate(parseDateTime(dataBox.getString("d_ldate")))
                    .build();

            result.add(dto);
        }

        return result;
    }

    /**
     * FormatDate를 사용하여 YYYYMMDDHHMMSS -> String 변환
     */
    private String parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            // YYYYMMDDHHMMSS -> "YYYY-MM-DD HH:MM:SS" 형식으로 변환
            String formattedDateTime = FormatDate.getFormatDate(dateTimeStr, "yyyy-MM-dd HH:mm:ss");

            if (formattedDateTime == null || formattedDateTime.isEmpty()) {
                return null;
            }

            return formattedDateTime;
        } catch (Exception e) {
            logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}