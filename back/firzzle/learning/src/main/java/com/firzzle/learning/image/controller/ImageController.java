package com.firzzle.learning.image.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.image.dto.ImageResponseDTO;
import com.firzzle.learning.image.dto.ImageUploadDTO;
import com.firzzle.learning.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.firzzle.common.logging.dto.UserActionLog.userActionLog;
import static com.firzzle.common.logging.service.LoggingService.log;

/**
 * @Class Name : ImageController.java
 * @Description : 이미지 관리 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
@Tag(name = "이미지 관리 API", description = "이미지 업로드, 조회, 삭제 등 이미지 관리 관련 API")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    /**
     * 이미지 업로드
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json;charset=UTF-8")
    @Operation(summary = "이미지 업로드", description = "이미지 파일을 S3에 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    public ResponseEntity<Response<ImageResponseDTO>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "이미지 업로드 정보")
            @Valid @ModelAttribute ImageUploadDTO uploadDTO,

            HttpServletRequest request) {

        logger.info("이미지 업로드 요청 - 파일명: {}, 카테고리: {}", file.getOriginalFilename(), uploadDTO.getCategory());

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("category", uploadDTO.getCategory());
            box.put("description", uploadDTO.getDescription());
            box.put("isPublic", uploadDTO.getIsPublic() != null ? uploadDTO.getIsPublic() : true);

            DataBox dataBox = imageService.uploadImage(file, box);
            ImageResponseDTO imageResponseDTO = convertToImageResponseDTO(dataBox);

            Response<ImageResponseDTO> response = Response.<ImageResponseDTO>builder()
                    .status(Status.OK)
                    .message("이미지가 성공적으로 업로드되었습니다.")
                    .data(imageResponseDTO)
                    .build();

            // 이미지 업로드 로깅 => ELK
            String userId = box.getString("uuid");
            log(userActionLog(userId, "IMAGE_UPLOADED"));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            logger.error("이미지 업로드 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("이미지 업로드 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 정보 조회
     */
    @GetMapping(value = "/{filename}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "이미지 정보 조회", description = "업로드된 이미지의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<ImageResponseDTO>> getImageInfo(
            @Parameter(description = "조회할 이미지 파일명", required = true)
            @PathVariable("filename") String filename,

            HttpServletRequest request) {

        logger.info("이미지 정보 조회 요청 - 파일명: {}", filename);

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("filename", filename);

            DataBox dataBox = imageService.getImageInfo(box);
            ImageResponseDTO imageResponseDTO = convertToImageResponseDTO(dataBox);

            Response<ImageResponseDTO> response = Response.<ImageResponseDTO>builder()
                    .status(Status.OK)
                    .data(imageResponseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("이미지 정보 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("이미지 정보 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 삭제
     */
    @DeleteMapping(value = "/{filename}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "이미지 삭제", description = "업로드된 이미지를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    public ResponseEntity<Response<Void>> deleteImage(
            @Parameter(description = "삭제할 이미지 파일명", required = true)
            @PathVariable("filename") String filename,

            HttpServletRequest request) {

        logger.info("이미지 삭제 요청 - 파일명: {}", filename);

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("filename", filename);

            boolean result = imageService.deleteImage(box);

            Response<Void> response = Response.<Void>builder()
                    .status(Status.OK)
                    .message("이미지가 성공적으로 삭제되었습니다.")
                    .build();

            // 이미지 삭제 로깅 => ELK
            String userId = box.getString("uuid");
            log(userActionLog(userId, "IMAGE_DELETED"));

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("이미지 삭제 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("이미지 삭제 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED, "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 Presigned URL 생성
     */
    @GetMapping(value = "/presigned/{filename}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "이미지 Presigned URL 생성", description = "비공개 이미지에 접근할 수 있는 시간 제한 URL을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<Object>> getPresignedUrl(
            @Parameter(description = "이미지 파일명", required = true)
            @PathVariable("filename") String filename,

            @Parameter(description = "URL 만료 시간(분)", schema = @Schema(type = "integer", defaultValue = "60"))
            @RequestParam(value = "expirationMinutes", required = false, defaultValue = "60") Integer expirationMinutes,

            HttpServletRequest request) {

        logger.info("Presigned URL 생성 요청 - 파일명: {}, 만료시간(분): {}", filename, expirationMinutes);

        try {
            RequestBox box = RequestManager.getBox(request);
            box.put("filename", filename);
            box.put("expirationMinutes", expirationMinutes);

            DataBox dataBox = imageService.getPresignedUrl(box);

            Response<Object> response = Response.builder()
                    .status(Status.OK)
                    .data(dataBox.toMap())
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("Presigned URL 생성 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Presigned URL 생성 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_INVALID_PRESIGNED_URL, "Presigned URL 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * DataBox를 ImageResponseDTO로 변환
     */
    private ImageResponseDTO convertToImageResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        // 날짜 형식 변환 (YYYYMMDDHHMMSS -> YYYY-MM-DD HH:MM:SS)
        String indate = "";
        if (StringUtils.isNotEmpty(dataBox.getString("indate"))) {
            try {
                indate = FormatDate.getFormatDate(dataBox.getString("indate"), "yyyy-MM-dd HH:mm:ss");
            } catch (Exception e) {
                logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            }
        }

        return ImageResponseDTO.builder()
                .imageSeq(dataBox.getLong2("imageSeq"))
                .filename(dataBox.getString("filename"))
                .imageUrl(dataBox.getString("imageUrl"))
                .category(dataBox.getString("category"))
                .description(dataBox.getString("description"))
                .isPublic(dataBox.getBoolean("isPublic", true))
                .indate(indate)
                .build();
    }
}