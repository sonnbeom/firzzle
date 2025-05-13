package com.firzzle.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.gateway.response.Response;
import com.firzzle.gateway.response.Status;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.netty.channel.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @Class Name : GatewayExceptionHandler.java
 * @Description : API 게이트웨이 전용 글로벌 예외 처리기
 * @author Firzzle
 * @since 2025. 5. 12.
 */
@Configuration
@Order(-2) // 높은 우선순위로 설정
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 응답 헤더 설정
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 상태 코드와 에러 메시지 결정
        final HttpStatus status;
        final String errorMessage;
        final String finalErrorCause;  // final 변수 선언

        // 예외 유형별 처리 - 로그 레벨 변경
        if (ex instanceof ResponseStatusException) {
            status = HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
            errorMessage = "요청을 처리할 수 없습니다.";
            finalErrorCause = ex.getMessage();
        } else if (ex instanceof NotFoundException) {
            // 서비스를 찾을 수 없음 (라우팅 실패)
            status = HttpStatus.NOT_FOUND;
            errorMessage = "요청한 서비스를 찾을 수 없습니다.";
            finalErrorCause = null;
            logger.error("서비스를 찾을 수 없음: {}", ex.getMessage()); // error에서 info로 변경
        } else if (ex instanceof ConnectTimeoutException) {
            // 연결 시간 초과
            status = HttpStatus.GATEWAY_TIMEOUT;
            errorMessage = "서비스 연결 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.";
            finalErrorCause = null;
            logger.error("서비스 연결 시간 초과: {}", ex.getMessage()); // error에서 info로 변경
        } else if (ex instanceof TimeoutException || ex instanceof java.util.concurrent.TimeoutException) {
            // 요청 시간 초과
            status = HttpStatus.GATEWAY_TIMEOUT;
            errorMessage = "요청 처리 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.";
            finalErrorCause = null;
            logger.error("요청 처리 시간 초과: {}", ex.getMessage()); // error에서 info로 변경
        } else if (ex instanceof CallNotPermittedException) {
            // 서킷 브레이커 오픈 (회로 차단기)
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorMessage = "현재 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해 주세요.";
            finalErrorCause = "서비스 과부하";
            logger.error("서비스 회로 차단됨 (서킷 브레이커): {}", ex.getMessage()); // error에서 info로 변경
        } else if (ex.getCause() != null && ex.getCause().toString().contains("TooManyRequests")) {
            // 속도 제한 초과
            status = HttpStatus.TOO_MANY_REQUESTS;
            errorMessage = "요청 횟수가 제한을 초과했습니다. 잠시 후 다시 시도해 주세요.";
            finalErrorCause = "요청 제한 초과";
            logger.error("요청 횟수 제한 초과: {}", ex.getMessage()); // error에서 info로 변경
        } else {
            // 기타 모든 예외
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = "서버 처리 중 오류가 발생했습니다.";
            finalErrorCause = null;
            logger.error("게이트웨이 처리 중 오류 발생: {}", ex.getMessage()); // error에서 info로 변경, 스택 트레이스 제거
        }

        response.setStatusCode(status);

        // 응답 생성에 필요한 최종 상태 코드
        final HttpStatus finalStatus = status;
        final String finalErrorMessage = errorMessage;

        // 응답 본문 생성
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();

            // 응답 객체 생성 (기존 Response 클래스 활용)
            Response<Void> errorResponse = Response.<Void>builder()
                    .status(Status.FAIL)
                    .message(finalErrorMessage)  // final 변수 사용
                    .cause(finalErrorCause)     // final 변수 사용
                    .build();

            try {
                // 객체를 JSON으로 직렬화
                byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                return bufferFactory.wrap(bytes);
            } catch (JsonProcessingException e) {
                // JSON 직렬화 실패 시 기본 에러 메시지
                String fallbackMessage = "{\"status\":\"FAIL\",\"message\":\"서버 오류가 발생했습니다.\"}";
                return bufferFactory.wrap(fallbackMessage.getBytes(StandardCharsets.UTF_8));
            }
        }));
    }
}