package com.firzzle.stt.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")  // ✅ 설정에 맞게 수정
    private String bucket;

    @Value("${app.s3.base-url}")      // ✅ 설정에 맞게 수정
    private String baseUrl;

    public String upload(File file, String dir) {
        String key = dir + UUID.randomUUID() + ".jpg";

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("image/jpeg")
                        .build(),
                Path.of(file.getAbsolutePath()));

        return baseUrl + "/" + key;
    }
}