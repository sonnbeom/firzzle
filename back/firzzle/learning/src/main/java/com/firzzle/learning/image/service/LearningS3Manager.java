package com.firzzle.learning.image.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.S3ImageManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * S3ImageManager 래퍼 클래스
 * LearningModule에 필요한 S3 기능만 제공
 */
public class LearningS3Manager {

    private static final Logger logger = LoggerFactory.getLogger(LearningS3Manager.class);

    private final S3ImageManager s3ImageManager;

    @Getter
    private final String bucketName;

    @Getter
    private final String baseUrl;

    /**
     * 생성자
     */
    public LearningS3Manager(String accessKeyId, String secretKey, String region,
                             String bucketName, String baseUrl) {
        try {
            this.s3ImageManager = new S3ImageManager(accessKeyId, secretKey, region, bucketName, baseUrl);
            this.bucketName = bucketName;
            this.baseUrl = baseUrl;
            logger.info("LearningS3Manager 초기화 성공 - 버킷: {}", bucketName);
        } catch (Exception e) {
            logger.error("LearningS3Manager 초기화 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "S3Manager 초기화 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 업로드
     */
    public String uploadImage(File imageFile, String directory, String filename, boolean isPublic) {
        try {
            return s3ImageManager.uploadImage(imageFile, directory, filename, isPublic);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "이미지 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 URL 조회
     */
    public String getImageUrl(String savedFilename) {
        try {
            return s3ImageManager.getImageUrl(savedFilename);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 URL 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 존재 여부 확인
     */
    public boolean isImageExists(String savedFilename) {
        return s3ImageManager.isImageExists(savedFilename);
    }

    /**
     * 이미지 삭제
     */
    public void deleteImage(String savedFilename) {
        try {
            s3ImageManager.deleteImage(savedFilename);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED, "이미지 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * Presigned URL 생성
     */
    public String getPresignedUrl(String savedFilename, int expirationMinutes) {
        try {
            return s3ImageManager.getPresignedUrl(savedFilename, expirationMinutes);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_INVALID_PRESIGNED_URL,
                    "Presigned URL 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 디렉토리명 추출
     */
    public String getDirName(String savedFilename) {
        return S3ImageManager.getDirName(savedFilename);
    }
}