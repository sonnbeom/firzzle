package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ContentSummaryResponseDTO;
import com.firzzle.learning.dto.SectionDTO;
import com.firzzle.learning.service.ContentSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.firzzle.common.logging.dto.UserActionLog.*;
import static com.firzzle.common.logging.service.LoggingService.*;

/**
 * @Class Name : ContentSummaryController.java
 * @Description : 콘텐츠 요약 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "콘텐츠 요약 API (AI)", description = "콘텐츠 요약 관련 API")
public class ContentSummaryController {

    private static final Logger logger = LoggerFactory.getLogger(ContentSummaryController.class);

    private final ContentSummaryService summaryService;

    /**
     * 요약 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping(value = "/{contentSeq}/summary", produces = "application/json;charset=UTF-8")
    @Operation(summary = "요약 조회", description = "콘텐츠의 요약 정보를 조회합니다. 쉬운 버전과 어려운 버전의 요약을 모두 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ContentSummaryResponseDTO>> getSummary(
            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        logger.info("콘텐츠 요약 조회 요청 - 콘텐츠 일련번호: {}", userContentSeq);

        try {
            // RequestBox 생성
            RequestBox box = RequestManager.getBox(request);
            box.put("userContentSeq", userContentSeq);

            // 쉬운 버전과 어려운 버전의 요약 정보 조회
            DataBox easySummaryDataBox = summaryService.getSummary(box, "E");
            DataBox hardSummaryDataBox = summaryService.getSummary(box, "H");

            // 응답 DTO 생성
            ContentSummaryResponseDTO responseDTO = new ContentSummaryResponseDTO();
            responseDTO.setContentSeq(userContentSeq);

            // 쉬운 버전 요약 설정
            if (easySummaryDataBox != null) {
                responseDTO.setEasySummarySeq(easySummaryDataBox.getLong2("d_summary_seq"));
                responseDTO.setEasySections(convertToSectionDTOList((List<DataBox>)easySummaryDataBox.getObject("sections")));
                responseDTO.setEasyIndate(formatDateTime(easySummaryDataBox.getString("d_indate")));

                //요약 쉽게 로깅 => ELK
                String userId = box.getString("uuid");
                log(summaryPreferenceLog(userId,"EASY"));
            }

            // 어려운 버전 요약 설정
            if (hardSummaryDataBox != null) {
                responseDTO.setHardSummarySeq(hardSummaryDataBox.getLong2("d_summary_seq"));
                responseDTO.setHardSections(convertToSectionDTOList((List<DataBox>)hardSummaryDataBox.getObject("sections")));
                responseDTO.setHardIndate(formatDateTime(hardSummaryDataBox.getString("d_indate")));

                //요약 어렵게 로깅 => ELK
                String userId = box.getString("uuid");
                log(summaryPreferenceLog(userId,"DIFFICULT"));
            }

            if(hardSummaryDataBox == null && easySummaryDataBox == null){
                return ResponseEntity.noContent().build();
            }

            Response<ContentSummaryResponseDTO> response = Response.<ContentSummaryResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            //요약 로깅 => ELK
            String referer = box.getString("referer");
            String userId = box.getString("uuid");
            log(userPreferenceLog(userId, referer.toUpperCase(), "SUMMARY"));

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("콘텐츠 요약 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 요약 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 요약 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * DataBox 리스트를 SectionDTO 리스트로 변환
     */
    private List<SectionDTO> convertToSectionDTOList(List<DataBox> sectionDataBoxes) {
        if (sectionDataBoxes == null) {
            return new ArrayList<>();
        }

        List<SectionDTO> sections = new ArrayList<>();
        for (DataBox sectionDataBox : sectionDataBoxes) {
            sections.add(SectionDTO.builder()
                    .sectionSeq(sectionDataBox.getLong2("d_section_seq"))
                    .title(sectionDataBox.getString("d_title"))
                    .startTime(sectionDataBox.getInt2("d_start_time"))
                    .details(sectionDataBox.getString("d_details"))
                    .build());
        }

        return sections;
    }

    /**
     * YYYYMMDDHHMMSS 형식의 날짜 문자열을 포맷된 문자열로 변환
     */
    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            return FormatDate.getFormatDate(dateTimeStr, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            return dateTimeStr;
        }
    }
}