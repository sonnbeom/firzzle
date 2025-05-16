package com.firzzle.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * S3 설정 정보를 담는 클래스
 * 각 서비스 모듈에서 자신의 S3 설정으로 초기화하여 사용
 */
@Getter
@Setter
public class S3Config {
    private String accessKeyId;
    private String secretKey;
    private String region;
    private String bucketName;
    private String baseUrl;

    /**
     * 기본 생성자
     */
    public S3Config() {
    }

    /**
     * baseUrl을 자동 생성하는 생성자
     */
    public S3Config(String accessKeyId, String secretKey, String region, String bucketName) {
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
        this.region = region;
        this.bucketName = bucketName;
        this.baseUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com";
    }

    /**
     * 모든 필드를 설정하는 생성자
     */
    public S3Config(String accessKeyId, String secretKey, String region, String bucketName, String baseUrl) {
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
        this.region = region;
        this.bucketName = bucketName;
        this.baseUrl = baseUrl;
    }

    /**
     * 설정이 유효한지 검증
     * @return 유효 여부
     */
    public boolean isValid() {
        return accessKeyId != null && !accessKeyId.isEmpty()
                && secretKey != null && !secretKey.isEmpty()
                && region != null && !region.isEmpty()
                && bucketName != null && !bucketName.isEmpty();
    }
}