package com.firzzle.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

/**
 * 1. 새로운 모듈 섹션을 추가할 때:
 *    - 각 모듈 섹션은 100  개 단위로 구분됩니다 (ex: 70001, 70101, 70201...)
 *
 * 3. 모듈별 에러 코드 범위:
 *    - Common: MSG_0000070001 ~ MSG_0000070099
 *    - Auth: MSG_0000070101 ~ MSG_0000070199
 *    - JWT: MSG_0000070201 ~ MSG_0000070299
 *    - Business: MSG_0000070301 ~ MSG_0000070399
 *    - User: MSG_0000070401 ~ MSG_0000070499
 *    - Admin: MSG_0000070501 ~ MSG_0000070599
 *    - Learning: MSG_0000070601 ~ MSG_0000070699
 *    - LLM: MSG_0000070701 ~ MSG_0000070799
 *    - STT: MSG_0000070801 ~ MSG_0000070899
 *    - Main: MSG_0000070901 ~ MSG_0000070999
 *    - Gateway: MSG_0000071001 ~ MSG_0000071099
 *    - AI: MSG_0000071101 ~ MSG_0000071199
 *    - (신규 모듈은 여기에 추가)
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // =========== Common ===========
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "MSG_0000070001", "유효하지 않은 입력값입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "MSG_0000070002", "올바르지 않은 타입입니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070003", "리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070004", "서버 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "MSG_0000070005", "지원하지 않는 HTTP 메소드입니다"),

    // =========== Auth ===========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MSG_0000070101", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "MSG_0000070102", "접근 권한이 없습니다"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "MSG_0000070103", "해당 리소스에 접근할 권한이 없습니다"),
    MISSING_OAUTH_INFO(HttpStatus.BAD_REQUEST, "MSG_0000070104", "필수 OAuth 정보가 누락되었습니다"),
    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "MSG_0000070105", "유효하지 않은 OAuth 토큰입니다"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "MSG_0000070106", "외부 API 호출 중 오류가 발생했습니다"),
    NOT_SUPPORTED_OAUTH(HttpStatus.BAD_REQUEST, "MSG_0000070107", "지원되지 않는 OAuth 제공자입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "MSG_0000070108", "유효하지 않은 토큰입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MSG_0000070109", "잘못된 사용자명 또는 비밀번호입니다"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "MSG_0000070110", "계정이 비활성화되었습니다"),

    // =========== JWT ===========
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "MSG_0000070201", "토큰이 만료되었습니다"),
    JWT_INVALID(HttpStatus.FORBIDDEN, "MSG_0000070202", "유효하지 않은 토큰입니다"),
    JWT_REQUIRED(HttpStatus.UNAUTHORIZED, "MSG_0000070203", "로그인 후 이용해주세요"),

    // =========== Business ===========
    INVALID_PAGE_PARAMETERS(HttpStatus.BAD_REQUEST, "MSG_0000070301", "유효하지 않은 페이지 파라미터입니다"),
    UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070302", "데이터 업데이트 중 오류가 발생했습니다"),
    ALREADY_DELETED(HttpStatus.BAD_REQUEST, "MSG_0000070303", "이미 삭제된 리소스입니다"),

    // =========== User Module ===========
    // 여기부터 MSG_0000070401 시작
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070401", "사용자를 찾을 수 없습니다"),

    // =========== Admin Module ===========
    // 여기부터 MSG_0000070501 시작

    // =========== Learning Module ===========
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "MSG_0000070601", "이미 존재하는 리소스입니다"),
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070602", "콘텐츠를 찾을 수 없습니다"),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070603", "퀴즈를 찾을 수 없습니다"),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070604", "문제를 찾을 수 없습니다"),
    SNAP_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070605", "스냅리뷰를 찾을 수 없습니다"),
    FRAME_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070606", "프레임을 찾을 수 없습니다"),
    SHARE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070607", "공유 코드를 찾을 수 없습니다"),
    SHARE_CODE_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "MSG_0000070608", "이미 취소된 공유 코드입니다"),
    LINKEDIN_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070609", "LinkedIn 프로필을 찾을 수 없습니다"),
    LINKEDIN_CRAWLING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070610", "LinkedIn 프로필 크롤링에 실패했습니다"),
    LINKEDIN_URL_INVALID(HttpStatus.BAD_REQUEST, "MSG_0000070611", "유효하지 않은 LinkedIn URL입니다"),
    LINKEDIN_API_ERROR(HttpStatus.BAD_GATEWAY, "MSG_0000070612", "LinkedIn API 호출 중 오류가 발생했습니다"),
    LINKEDIN_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "MSG_0000070613", "LinkedIn API 호출 한도를 초과했습니다"),
    LINKEDIN_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "MSG_0000070614", "이미 등록된 LinkedIn 프로필입니다"),
    LINKEDIN_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070615", "LinkedIn 프로필 검색에 실패했습니다"),
    APIFY_API_ERROR(HttpStatus.BAD_GATEWAY, "MSG_0000070616", "Apify API 호출 중 오류가 발생했습니다"),
    GOOGLE_SEARCH_FAILED(HttpStatus.BAD_GATEWAY, "MSG_0000070617", "Google 검색 중 오류가 발생했습니다"),
    LINKEDIN_PROFILE_DATA_INVALID(HttpStatus.BAD_REQUEST, "MSG_0000070618", "LinkedIn 프로필 데이터가 유효하지 않습니다"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070619", "요청한 데이터를 찾을 수 없습니다"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "MSG_0000070620", "유효하지 않은 파라미터입니다"),
    KAFKA_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070621", "Kafka 메시지 전송에 실패했습니다"),
    LINKEDIN_PROFILE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "MSG_0000070622", "LinkedIn 프로필 크롤링 한도를 초과했습니다"),

    // =========== LLM Module ===========
    LLM_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "MSG_0000070701", "LLM 서비스가 일시적으로 이용 불가합니다"),
    LLM_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "MSG_0000070702", "LLM 요청 처리에 실패했습니다"),
    LLM_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "MSG_0000070703", "LLM 응답이 유효하지 않습니다"),
    LLM_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "MSG_0000070704", "LLM 서비스 호출 한도를 초과했습니다"),
    LLM_CONTEXT_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "MSG_0000070705", "입력 컨텍스트가 너무 큽니다"),
    LLM_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070706", "요청한 LLM 모델을 찾을 수 없습니다"),
    LLM_PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070707", "요청한 프롬프트를 찾을 수 없습니다"),
    LLM_REQUEST_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "MSG_0000070708", "너무 많은 LLM 요청이 발생했습니다"),
    LLM_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070709", "LLM 응답 파싱에 실패했습니다"),
    OPENAI_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "MSG_0000070710", "OpenAI 응답 생성 중 오류가 발생했습니다"),
    ANSWER_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MSG_0000070711", "모든 시험 문제를 이미 완료했습니다"),
    VECTOR_EMBEDDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070712", "질문 임베딩 처리 중 오류가 발생했습니다"),
    KAFKA_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070713", "카프카 요청 처리 중 오류가 발생했습니다"),
    SUMMARY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070714", "요약 저장 중 오류가 발생했습니다"),
    SUMMARY_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070715", "요약 DB 저장 중 오류가 발생했습니다"),
    OXQUIZ_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070716", "OX 퀴즈 저장 중 오류가 발생했습니다"),
    EXAM_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070717", "서술형 퀴즈 저장 중 오류가 발생했습니다"),
    VECTOR_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070718", "벡터 DB 저장 중 오류가 발생했습니다"),
    CONTENT_TAG_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070719", "콘텐츠 태그 저장 중 오류가 발생했습니다"),
    SNAP_REVIEW_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070720", "SnapReview Kafka 전송 중 오류가 발생했습니다"),


    

    // =========== STT Module ===========
    INVALID_YOUTUBE_URL(HttpStatus.BAD_REQUEST, "MSG_0000070801", "유효하지 않은 YouTube URL입니다"),
    SCRIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000070802", "해당 영상에서 자막을 찾을 수 없습니다"),
    VIDEO_TOO_SHORT(HttpStatus.BAD_REQUEST, "MSG_0000070803", "영상 길이가 너무 짧습니다"),
    VIDEO_TOO_LONG(HttpStatus.BAD_REQUEST, "MSG_0000070804", "영상 길이가 너무 깁니다"),
    SNAP_REVIEW_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "MSG_0000070805", "SnapReview 외부 API 호출에 실패했습니다"),
    SNAP_REVIEW_IMAGE_MISMATCH(HttpStatus.BAD_REQUEST, "MSG_0000070806", "타임라인 수와 이미지 수가 일치하지 않습니다"),
    SNAP_REVIEW_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070807", "SnapReview 처리 중 알 수 없는 오류가 발생했습니다"),
    STT_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070808", "STT 처리 중 오류가 발생했습니다"),
    SUBTITLE_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070809", "자막 추출에 실패했습니다"),
    YTDLP_EXECUTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000070810", "yt-dlp 실행 중 오류가 발생했습니다"),


    

    // =========== Main Module ===========
    // 여기부터 MSG_0000070901 시작

    // =========== Gateway Module ===========
    // 여기부터 MSG_0000071001 시작

    // =========== AI Module ===========
    // 여기부터 MSG_0000071101 시작
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "MSG_0000071101", "AI 서비스가 일시적으로 이용 불가합니다"),
    AI_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "MSG_0000071102", "AI 요청 처리에 실패했습니다"),
    AI_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "MSG_0000071103", "AI 응답이 유효하지 않습니다"),
    AI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "MSG_0000071104", "AI 서비스 호출 한도를 초과했습니다"),
    AI_CONTENT_MODERATION_FAILED(HttpStatus.BAD_REQUEST, "MSG_0000071105", "콘텐츠 검수에 실패했습니다"),
    AI_GENERATION_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "MSG_0000071106", "AI 응답 생성 시간이 초과되었습니다"),
    AI_CONTEXT_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "MSG_0000071107", "입력 컨텍스트가 너무 큽니다"),
    AI_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000071108", "요청한 AI 모델을 찾을 수 없습니다"),
    AI_PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000071109", "요청한 프롬프트를 찾을 수 없습니다"),
    AI_REQUEST_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "MSG_0000071110", "너무 많은 AI 요청이 발생했습니다"),
    AI_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000071111", "AI 응답 파싱에 실패했습니다"),

    // AI 모듈 아래에 S3 모듈을 추가합니다 (71201부터 시작)
// =========== S3 Module ===========
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000071201", "S3 업로드에 실패했습니다"),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000071202", "S3 파일 삭제에 실패했습니다"),
    S3_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000071203", "S3 파일 다운로드에 실패했습니다"),
    S3_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000071204", "S3 파일을 찾을 수 없습니다"),
    S3_INVALID_FILE(HttpStatus.BAD_REQUEST, "MSG_0000071205", "유효하지 않은 파일입니다"),
    S3_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MSG_0000071206", "S3 리소스에 대한 접근이 거부되었습니다"),
    S3_BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG_0000071207", "S3 버킷을 찾을 수 없습니다"),
    S3_CLIENT_NOT_INITIALIZED(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000071208", "S3 클라이언트가 초기화되지 않았습니다"),
    S3_INVALID_PRESIGNED_URL(HttpStatus.BAD_REQUEST, "MSG_0000071209", "유효하지 않은 사전 서명 URL입니다"),
    S3_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "MSG_0000071210", "파일 크기가 너무 큽니다"),
    S3_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "MSG_0000071211", "지원하지 않는 파일 타입입니다"),
    S3_CONFIG_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "MSG_0000071212", "S3 설정 정보가 누락되었습니다");

    private final HttpStatus status;
    private final String messageKey;
    private final String defaultMessage;

    /**
     * 기본 메시지를 반환합니다 (기존 코드와의 호환성 유지).
     * @return 기본 오류 메시지
     */
    public String getMessage() {
        return defaultMessage;
    }

    /**
     * MessageSource를 통해 국제화된 메시지를 반환합니다.
     * @param messageSource 메시지 소스
     * @param args 메시지 파라미터
     * @return 국제화된 메시지
     */
    public String getMessage(MessageSource messageSource, Object... args) {
        return messageSource.getMessage(
                messageKey,
                args,
                defaultMessage,
                LocaleContextHolder.getLocale()
        );
    }
}