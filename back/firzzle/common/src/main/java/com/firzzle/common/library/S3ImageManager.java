package com.firzzle.common.library;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.ConfigSet;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * @Class Name : S3ImageManager
 * @Description : S3 이미지 처리 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2025. 5. 12.
 */
public class S3ImageManager {

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(S3ImageManager.class);

    /** S3 클라이언트 */
    private S3Client s3Client;

    /** S3 프리사이너 (시간 제한 URL 생성용) */
    private S3Presigner s3Presigner;

    /** 버킷 이름 */
    private String bucketName;

    /** 이미지 서비스 기본 URL */
    private String baseUrl;

    /** 초기화 여부 */
    private boolean initialized = false;

    /**
     * 기본 생성자 - 초기화하지 않음
     * 이 생성자는 빈 등록을 위해 사용되며, 실제 사용 전에 initialize 메서드 호출 필요
     */
    public S3ImageManager() {
        // 아무 작업도 수행하지 않음
    }

    /**
     * 파라미터 생성자 - 모든 값을 받아 초기화
     *
     * @param accessKey AWS 액세스 키
     * @param secretKey AWS 시크릿 키
     * @param region AWS 리전
     * @param bucketName S3 버킷 이름
     * @param baseUrl S3 기본 URL
     */
    public S3ImageManager(String accessKey, String secretKey, String region, String bucketName, String baseUrl) {
        initialize(accessKey, secretKey, region, bucketName, baseUrl);
    }

    /**
     * S3Config를 사용하는 생성자
     *
     * @param s3Config S3 설정 객체
     * @throws BusinessException 초기화 오류 시 발생
     */
    public S3ImageManager(com.firzzle.common.s3.S3Config s3Config) throws BusinessException {
        if (s3Config == null) {
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "S3 설정이 null입니다.");
        }

        if (!s3Config.isValid()) {
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "S3 필수 설정값이 누락되었습니다.");
        }

        initialize(
                s3Config.getAccessKeyId(),
                s3Config.getSecretKey(),
                s3Config.getRegion(),
                s3Config.getBucketName(),
                s3Config.getBaseUrl()
        );
    }

    /**
     * ConfigSet을 사용하는 생성자
     *
     * @param conf ConfigSet 인스턴스
     * @throws BusinessException 초기화 오류 시 발생
     */
    public S3ImageManager(ConfigSet conf) throws BusinessException {
        if (conf == null) {
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "ConfigSet이 null입니다.");
        }

        try {
            String accessKey = conf.getProperty("aws.accessKeyId");
            String secretKey = conf.getProperty("aws.secretKey");
            String region = conf.getProperty("aws.region");
            String bucketName = conf.getProperty("aws.s3.bucket");
            String baseUrl = conf.getProperty("aws.s3.baseUrl");

            if (accessKey == null || secretKey == null || region == null || bucketName == null) {
                throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "AWS S3 필수 설정이 누락되었습니다");
            }

            initialize(accessKey, secretKey, region, bucketName, baseUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "S3 설정 로드 실패: " + e.getMessage());
        }
    }

    /**
     * S3 클라이언트 초기화
     *
     * @param accessKey AWS 액세스 키
     * @param secretKey AWS 시크릿 키
     * @param region AWS 리전
     * @param bucketName S3 버킷 이름
     * @param baseUrl S3 기본 URL
     */
    private void initialize(String accessKey, String secretKey, String region, String bucketName, String baseUrl) {
        if (accessKey == null || secretKey == null || region == null || bucketName == null) {
            throw new BusinessException(ErrorCode.S3_CONFIG_MISSING, "필수 AWS 설정값이 누락되었습니다.");
        }

        this.bucketName = bucketName;
        this.baseUrl = baseUrl != null ? baseUrl :
                "https://" + bucketName + ".s3." + region + ".amazonaws.com";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.initialized = true;
        logger.debug("S3ImageManager 초기화 완료 - 버킷: {}, 리전: {}", bucketName, region);
    }

    /**
     * 초기화 여부 확인
     */
    private void checkInitialized() {
        if (!initialized) {
            throw new BusinessException(ErrorCode.S3_CLIENT_NOT_INITIALIZED, "S3ImageManager가 초기화되지 않았습니다. 사용하기 전에 초기화해주세요.");
        }
    }

    /**
     * 이미지를 S3에 업로드
     *
     * @param imageFile 업로드할 이미지 파일
     * @param directory 업로드 디렉토리 (예: "profile", "product" 등)
     * @return 저장된 파일 이름 (예: profile_1234567890_image.jpg)
     * @throws BusinessException 업로드 실패 시 발생
     */
    public String uploadImage(File imageFile, String directory) throws BusinessException {
        return uploadImage(imageFile, directory, imageFile.getName(), true);
    }

    /**
     * 이미지를 S3에 업로드 (파일명 지정)
     *
     * @param imageFile 업로드할 이미지 파일
     * @param directory 업로드 디렉토리 (예: "profile", "product" 등)
     * @param filename 저장할 파일명
     * @param isPublic 공개 접근 여부
     * @return 저장된 파일 이름 (예: profile_1234567890_image.jpg)
     * @throws BusinessException 업로드 실패 시 발생
     */
    public String uploadImage(File imageFile, String directory, String filename, boolean isPublic) throws BusinessException {
        checkInitialized();

        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            throw new BusinessException(ErrorCode.S3_INVALID_FILE, "유효하지 않은 이미지 파일입니다: " + filename);
        }

        if (directory == null || directory.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "디렉토리 이름은 필수입니다.");
        }

        try {
            String extension = FilenameUtils.getExtension(filename);
            String safeFilename = sanitizeFilename(FilenameUtils.getBaseName(filename)) +
                    (extension != null && !extension.isEmpty() ? "." + extension : "");

            // 고유한 파일명 생성 (디렉토리_UUID_파일명.확장자)
            String savedFilename = directory + "_" + UUID.randomUUID().toString().replace("-", "") + "_" + safeFilename;

            // S3 키 경로 생성 (디렉토리/파일명)
            String s3Key = directory + "/" + savedFilename;

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(getContentType(extension));

            // 공개 접근 설정
            if (isPublic) {
                requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            PutObjectRequest request = requestBuilder.build();

            s3Client.putObject(request, RequestBody.fromFile(imageFile));

            logger.info("이미지 업로드 성공: {}", savedFilename);
            return savedFilename;
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}, 파일: {}", bucketName, filename, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (S3Exception e) {
            logger.error("S3 업로드 실패: {}, 파일: {}", e.getMessage(), filename, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "S3 업로드 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("이미지 업로드 실패: {}, 파일: {}", e.getMessage(), filename, e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "이미지 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * 바이트 배열을 S3에 이미지로 업로드
     *
     * @param imageBytes 이미지 바이트 배열
     * @param directory 업로드 디렉토리
     * @param filename 원본 파일명
     * @param isPublic 공개 접근 여부
     * @return 저장된 파일 이름
     * @throws BusinessException 업로드 실패 시 발생
     */
    public String uploadImageFromBytes(byte[] imageBytes, String directory, String filename, boolean isPublic) throws BusinessException {
        checkInitialized();

        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.S3_INVALID_FILE, "이미지 데이터가 비어있습니다.");
        }

        if (directory == null || directory.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "디렉토리 이름은 필수입니다.");
        }

        try {
            String extension = FilenameUtils.getExtension(filename);
            String safeFilename = sanitizeFilename(FilenameUtils.getBaseName(filename)) +
                    (extension != null && !extension.isEmpty() ? "." + extension : "");

            // 고유한 파일명 생성
            String savedFilename = directory + "_" + UUID.randomUUID().toString().replace("-", "") + "_" + safeFilename;

            // S3 키 경로 생성
            String s3Key = directory + "/" + savedFilename;

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(getContentType(extension));

            // 공개 접근 설정
            if (isPublic) {
                requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            PutObjectRequest request = requestBuilder.build();

            s3Client.putObject(request, RequestBody.fromBytes(imageBytes));

            logger.info("이미지 업로드 성공: {}", savedFilename);
            return savedFilename;
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}, 파일: {}", bucketName, filename, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (S3Exception e) {
            logger.error("S3 업로드 실패: {}, 파일: {}", e.getMessage(), filename, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "S3 업로드 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("이미지 업로드 실패: {}, 파일: {}", e.getMessage(), filename, e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "이미지 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * 저장된 이미지를 삭제
     *
     * @param savedFilename 저장된 파일 이름 (예: profile_1234567890_image.jpg)
     * @throws BusinessException 삭제 실패 시 발생
     */
    public void deleteImage(String savedFilename) throws BusinessException {
        checkInitialized();

        if (savedFilename == null || savedFilename.isEmpty()) {
            return; // 파일명이 비어있으면 아무것도 하지 않음
        }

        try {
            String directory = getDirName(savedFilename);
            String s3Key = directory + "/" + savedFilename;

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(request);
            logger.info("이미지 삭제 성공: {}", savedFilename);
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}, 파일: {}", bucketName, savedFilename, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (S3Exception e) {
            logger.error("S3 삭제 실패: {}, 파일: {}", e.getMessage(), savedFilename, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else if (e.statusCode() == 404) {
                throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "삭제할 파일을 찾을 수 없습니다: " + savedFilename);
            } else {
                throw new BusinessException(ErrorCode.S3_DELETE_FAILED, "S3 삭제 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("이미지 삭제 실패: {}, 파일: {}", e.getMessage(), savedFilename, e);
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED, "이미지 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 URL 생성 (공개 접근용)
     *
     * @param savedFilename 저장된 파일 이름
     * @return 이미지 URL
     * @throws BusinessException URL 생성 실패 시 발생
     */
    public String getImageUrl(String savedFilename) throws BusinessException {
        checkInitialized();

        if (savedFilename == null || savedFilename.isEmpty()) {
            return "";
        }

        String directory = getDirName(savedFilename);
        String s3Key = directory + "/" + savedFilename;

        return baseUrl + "/" + s3Key;
    }

    /**
     * 이미지 URL 생성 (시간 제한 있는 비공개 접근용)
     *
     * @param savedFilename 저장된 파일 이름
     * @param expirationMinutes URL 유효 시간(분)
     * @return 시간 제한이 있는 이미지 URL
     * @throws BusinessException URL 생성 실패 시 발생
     */
    public String getPresignedUrl(String savedFilename, int expirationMinutes) throws BusinessException {
        checkInitialized();

        if (savedFilename == null || savedFilename.isEmpty()) {
            return "";
        }

        if (expirationMinutes <= 0) {
            expirationMinutes = 60; // 기본값 1시간
        }

        String directory = getDirName(savedFilename);
        String s3Key = directory + "/" + savedFilename;

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}, 파일: {}", bucketName, savedFilename, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (S3Exception e) {
            logger.error("사전 서명 URL 생성 실패: {}, 파일: {}", e.getMessage(), savedFilename, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else if (e.statusCode() == 404) {
                throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "파일을 찾을 수 없습니다: " + savedFilename);
            } else {
                throw new BusinessException(ErrorCode.S3_INVALID_PRESIGNED_URL, "사전 서명 URL 생성 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("사전 서명 URL 생성 실패: {}, 파일: {}", e.getMessage(), savedFilename, e);
            throw new BusinessException(ErrorCode.S3_INVALID_PRESIGNED_URL, "사전 서명 URL 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지를 로컬 파일로 다운로드
     *
     * @param savedFilename 저장된 파일 이름
     * @param targetFile 저장할 로컬 파일
     * @throws BusinessException 다운로드 실패 시 발생
     */
    public void downloadImage(String savedFilename, File targetFile) throws BusinessException {
        checkInitialized();

        if (savedFilename == null || savedFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일명이 비어있습니다.");
        }

        if (targetFile == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대상 파일이 null입니다.");
        }

        String directory = getDirName(savedFilename);
        String s3Key = directory + "/" + savedFilename;

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);

            try (FileOutputStream fos = new FileOutputStream(targetFile);
                 InputStream is = response) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            logger.info("이미지 다운로드 성공: {}", savedFilename);
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}, 파일: {}", bucketName, savedFilename, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (NoSuchKeyException e) {
            logger.error("S3 파일을 찾을 수 없음: {}", s3Key, e);
            throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "파일을 찾을 수 없습니다: " + savedFilename);
        } catch (S3Exception e) {
            logger.error("S3 다운로드 실패: {}, 파일: {}", e.getMessage(), savedFilename, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.S3_DOWNLOAD_FAILED, "S3 다운로드 실패: " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error("이미지 다운로드 I/O 오류: {}, 파일: {}", e.getMessage(), savedFilename, e);
            throw new BusinessException(ErrorCode.S3_DOWNLOAD_FAILED, "이미지 다운로드 I/O 오류: " + e.getMessage());
        } catch (Exception e) {
            logger.error("이미지 다운로드 실패: {}, 파일: {}", e.getMessage(), savedFilename, e);
            throw new BusinessException(ErrorCode.S3_DOWNLOAD_FAILED, "이미지 다운로드 실패: " + e.getMessage());
        }
    }

    /**
     * 디렉토리 내 이미지 목록 조회
     *
     * @param directory 디렉토리 이름
     * @return 이미지 파일명 목록
     * @throws BusinessException 목록 조회 실패 시 발생
     */
    public List<String> listImages(String directory) throws BusinessException {
        checkInitialized();

        if (directory == null || directory.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "디렉토리 이름은 필수입니다.");
        }

        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(directory + "/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            List<String> fileNames = new ArrayList<>();
            for (S3Object s3Object : response.contents()) {
                String key = s3Object.key();
                if (key.startsWith(directory + "/")) {
                    String fileName = key.substring(key.lastIndexOf("/") + 1);
                    fileNames.add(fileName);
                }
            }

            return fileNames;
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}, 디렉토리: {}", bucketName, directory, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (S3Exception e) {
            logger.error("S3 이미지 목록 조회 실패: {}, 디렉토리: {}", e.getMessage(), directory, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "이미지 목록 조회 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("이미지 목록 조회 실패: {}, 디렉토리: {}", e.getMessage(), directory, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 파일 복제
     *
     * @param sourceFilename 원본 파일명
     * @param targetDirectory 대상 디렉토리
     * @param targetFilename 대상 파일명 (null인 경우 원본과 동일하게 사용)
     * @param isPublic 공개 접근 여부
     * @return 복제된 파일명
     * @throws BusinessException 복제 실패 시 발생
     */
    public String copyImage(String sourceFilename, String targetDirectory, String targetFilename, boolean isPublic) throws BusinessException {
        checkInitialized();

        if (sourceFilename == null || sourceFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "원본 파일명이 비어있습니다.");
        }

        if (targetDirectory == null || targetDirectory.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대상 디렉토리 이름은 필수입니다.");
        }

        String sourceDirectory = getDirName(sourceFilename);
        String sourceKey = sourceDirectory + "/" + sourceFilename;

        if (targetFilename == null || targetFilename.isEmpty()) {
            // 원본 파일명에서 파일명 부분만 추출 (디렉토리_UUID_ 제외)
            int lastUnderscoreIndex = sourceFilename.lastIndexOf("_");
            if (lastUnderscoreIndex > 0 && sourceFilename.indexOf("_") != lastUnderscoreIndex) {
                targetFilename = sourceFilename.substring(lastUnderscoreIndex + 1);
            } else {
                targetFilename = sourceFilename;
            }
        }

        String extension = FilenameUtils.getExtension(targetFilename);
        String safeFilename = sanitizeFilename(FilenameUtils.getBaseName(targetFilename)) +
                (extension != null && !extension.isEmpty() ? "." + extension : "");

        String newFilename = targetDirectory + "_" + UUID.randomUUID().toString().replace("-", "") + "_" + safeFilename;
        String targetKey = targetDirectory + "/" + newFilename;

        try {
            // S3 복사 요청 생성
            CopyObjectRequest.Builder requestBuilder = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(targetKey);

            // 공개 접근 설정
            if (isPublic) {
                requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            CopyObjectRequest request = requestBuilder.build();
            s3Client.copyObject(request);

            logger.info("이미지 복제 성공: {} -> {}", sourceFilename, newFilename);
            return newFilename;
        } catch (NoSuchBucketException e) {
            logger.error("S3 버킷을 찾을 수 없음: {}", bucketName, e);
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_FOUND, "S3 버킷을 찾을 수 없습니다: " + bucketName);
        } catch (NoSuchKeyException e) {
            logger.error("S3 원본 파일을 찾을 수 없음: {}", sourceKey, e);
            throw new BusinessException(ErrorCode.S3_FILE_NOT_FOUND, "원본 파일을 찾을 수 없습니다: " + sourceFilename);
        } catch (S3Exception e) {
            logger.error("S3 이미지 복제 실패: {}, 원본: {}, 대상: {}", e.getMessage(), sourceFilename, newFilename, e);
            if (e.statusCode() == 403) {
                throw new BusinessException(ErrorCode.S3_ACCESS_DENIED, "S3 리소스에 대한 접근이 거부되었습니다: " + e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "S3 이미지 복제 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("이미지 복제 실패: {}, 원본: {}, 대상: {}", e.getMessage(), sourceFilename, newFilename, e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED, "이미지 복제 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 존재 여부 확인
     *
     * @param savedFilename 저장된 파일 이름
     * @return 존재 여부
     */
    public boolean isImageExists(String savedFilename) {
        checkInitialized();

        if (savedFilename == null || savedFilename.isEmpty()) {
            return false;
        }

        String directory = getDirName(savedFilename);
        String s3Key = directory + "/" + savedFilename;

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.warn("이미지 존재 여부 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 리소스 정리 (애플리케이션 종료 시 호출 권장)
     */
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }

        if (s3Presigner != null) {
            s3Presigner.close();
        }

        initialized = false;
        logger.debug("S3ImageManager 리소스 정리 완료");
    }

    /**
     * 파일이 존재하는 디렉토리명 반환
     *
     * @param savedFilename 저장된 파일명
     * @return 디렉토리명
     */
    public static String getDirName(String savedFilename) {
        if (savedFilename == null || savedFilename.isEmpty()) {
            return "";
        }

        String dirName = savedFilename;
        StringTokenizer st = new StringTokenizer(savedFilename, "_");
        if (st.hasMoreElements()) {
            dirName = st.nextToken().toLowerCase();
        }
        return dirName;
    }

    /**
     * 파일명 정리 (보안을 위해 특수문자 제거)
     *
     * @param filename 원본 파일명
     * @return 정리된 파일명
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }

        // 위험한 문자 제거 및 길이 제한
        String sanitized = filename.replaceAll("[^a-zA-Z0-9가-힣.-]", "_")
                .replaceAll("\\.+", ".")
                .replaceAll("\\s+", "_");

        // 파일명 길이 제한 (최대 50자)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }

        return sanitized;
    }

    /**
     * 파일 확장자에 따른 Content-Type 반환
     *
     * @param extension 파일 확장자
     * @return Content-Type
     */
    private String getContentType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";
            case "tif":
            case "tiff":
                return "image/tiff";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 버킷 이름 확인
     */
    public String getBucketName() {
        return this.bucketName;
    }

    /**
     * 기본 URL 확인
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }
}