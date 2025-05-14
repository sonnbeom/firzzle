package com.firzzle.learning.image.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Class Name : ImageService.java
 * @Description : 이미지 관리 서비스
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final MyBatisSupport myBatisSupport;
    private final S3ImageManager s3ImageManager;

    /**
     * 이미지 업로드
     * 이미지를 S3에 업로드하고 정보를 DB에 저장합니다.
     *
     * @param file - 업로드할 이미지 파일
     * @param box - 이미지 정보가 담긴 RequestBox
     * @return DataBox - 업로드된 이미지 정보
     */
    public DataBox uploadImage(MultipartFile file, RequestBox box) {
        logger.debug("이미지 업로드 요청 - 파일명: {}, 카테고리: {}, UUID: {}",
                file.getOriginalFilename(), box.getString("category"), box.getString("uuid"));

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        DataBox result = new DataBox();

        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                throw new BusinessException(ErrorCode.S3_INVALID_FILE, "업로드할 파일이 비어있습니다.");
            }

            // 파일 크기 검증 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.S3_FILE_TOO_LARGE, "이미지 파일 크기는 5MB를 초과할 수 없습니다.");
            }

            // 파일 타입 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ErrorCode.S3_INVALID_FILE_TYPE, "이미지 파일만 업로드할 수 있습니다.");
            }

            // 트랜잭션 시작
            transaction.start();

            // 임시 파일 생성
            File tempFile = File.createTempFile("upload-", "-temp");
            file.transferTo(tempFile);

            // 디렉토리 설정 (기본값: images)
            String directory = StringUtils.hasText(box.getString("category")) ?
                    box.getString("category") : "images";

            // S3 업로드 (공개 여부 설정)
            boolean isPublic = box.getBoolean("isPublic", true);
            String savedFilename = s3ImageManager.uploadImage(tempFile, directory, file.getOriginalFilename(), isPublic);

            // 임시 파일 삭제
            tempFile.delete();

            // S3 URL 생성
            String imageUrl = s3ImageManager.getImageUrl(savedFilename);

            // DB에 이미지 정보 저장 (ImageDAO 구현 필요)
            // imageDAO.insertImage(box);

            // 현재는 DB 저장 없이 S3 정보만 반환
            result.put("imageSeq", 0); // DB 저장 시 시퀀스 값으로 변경
            result.put("filename", savedFilename);
            result.put("imageUrl", imageUrl);
            result.put("category", directory);
            result.put("description", box.getString("description"));
            result.put("isPublic", isPublic);

            // 현재 시간을 YYYYMMDDHHMMSS 형식으로 추가
            try {
                String currentDateTime = FormatDate.getDate("yyyyMMddHHmmss");
                result.put("indate", currentDateTime);
            } catch (Exception e) {
                logger.error("현재 시간 포맷 설정 중 오류 발생: {}", e.getMessage());
                result.put("indate", ""); // 기본값으로 설정
            }

            // 성공 시 커밋
            transaction.commit();

            logger.info("이미지 업로드 성공 - 파일명: {}, URL: {}", savedFilename, imageUrl);

            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 이미지 정보 조회
     *
     * @param box - 이미지 파일명이 담긴 RequestBox
     * @return DataBox - 이미지 정보
     */
    public DataBox getImageInfo(RequestBox box) {
        logger.debug("이미지 정보 조회 요청 - 파일명: {}", box.getString("filename"));

        try {
            String filename = box.getString("filename");
            if (!StringUtils.hasText(filename)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일명이 없습니다.");
            }

            // 이미지 존재 여부 확인
            if (!s3ImageManager.isImageExists(filename)) {
                throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "요청한 이미지가 존재하지 않습니다: " + filename);
            }

            // S3 이미지 URL 생성
            String imageUrl = s3ImageManager.getImageUrl(filename);

            // DB에서 이미지 정보 조회 (ImageDAO 구현 필요)
            // DataBox imageData = imageDAO.selectImageByFilename(box);

            // 현재는 DB 조회 없이 S3 정보만 반환
            DataBox result = new DataBox();
            result.put("imageSeq", 0); // DB 조회 시 시퀀스 값으로 변경
            result.put("filename", filename);
            result.put("imageUrl", imageUrl);
            result.put("category", s3ImageManager.getDirName(filename));
            result.put("description", ""); // DB 구현 시 실제 설명으로 변경
            result.put("isPublic", true); // DB 구현 시 실제 값으로 변경
            result.put("indate", ""); // DB 구현 시 실제 등록일시로 변경

            logger.info("이미지 정보 조회 성공 - 파일명: {}", filename);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("이미지 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 삭제
     *
     * @param box - 삭제할 이미지 파일명이 담긴 RequestBox
     * @return boolean - 삭제 성공 여부
     */
    public boolean deleteImage(RequestBox box) {
        logger.debug("이미지 삭제 요청 - 파일명: {}", box.getString("filename"));

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();

        try {
            String filename = box.getString("filename");
            if (!StringUtils.hasText(filename)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일명이 없습니다.");
            }

            // 트랜잭션 시작
            transaction.start();

            // 이미지 존재 여부 확인
            if (!s3ImageManager.isImageExists(filename)) {
                throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "요청한 이미지가 존재하지 않습니다: " + filename);
            }

            // S3에서 이미지 삭제
            s3ImageManager.deleteImage(filename);

            // DB에서 이미지 정보 삭제 (ImageDAO 구현 필요)
            // imageDAO.deleteImage(box);

            // 성공 시 커밋
            transaction.commit();

            logger.info("이미지 삭제 성공 - 파일명: {}", filename);
            return true;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("이미지 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED, "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 이미지 URL 생성 (Presigned URL)
     *
     * @param box - 이미지 파일명 및 만료 시간(분)이 담긴 RequestBox
     * @return DataBox - Presigned URL 정보
     */
    public DataBox getPresignedUrl(RequestBox box) {
        logger.debug("Presigned URL 생성 요청 - 파일명: {}, 만료시간(분): {}",
                box.getString("filename"), box.getInt("expirationMinutes"));

        try {
            String filename = box.getString("filename");
            if (!StringUtils.hasText(filename)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일명이 없습니다.");
            }

            // 이미지 존재 여부 확인
            if (!s3ImageManager.isImageExists(filename)) {
                throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "요청한 이미지가 존재하지 않습니다: " + filename);
            }

            // 만료 시간 설정 (기본값: 60분)
            int expirationMinutes = box.getInt("expirationMinutes", 60);

            // Presigned URL 생성
            String presignedUrl = s3ImageManager.getPresignedUrl(filename, expirationMinutes);

            // 결과 반환
            DataBox result = new DataBox();
            result.put("filename", filename);
            result.put("presignedUrl", presignedUrl);
            result.put("expirationMinutes", expirationMinutes);

            logger.info("Presigned URL 생성 성공 - 파일명: {}", filename);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Presigned URL 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_INVALID_PRESIGNED_URL, "Presigned URL 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}