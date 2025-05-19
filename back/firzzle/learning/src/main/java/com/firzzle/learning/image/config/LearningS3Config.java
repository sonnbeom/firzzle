package com.firzzle.learning.image.config;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.learning.image.service.LearningS3Manager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Class Name : LearningS3Config.java
 * @Description : Learning 모듈 S3 설정
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Configuration
@ConditionalOnProperty(prefix = "aws.s3", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LearningS3Config {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.baseUrl}")
    private String baseUrl;

    /**
     * Learning 모듈용 S3 매니저 빈 생성
     */
    @Bean
    public LearningS3Manager learningS3Manager() {
        try {
            return new LearningS3Manager(accessKey, secretKey, region, bucketName, baseUrl);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "Learning S3Manager 초기화 실패: " + e.getMessage());
        }
    }
}