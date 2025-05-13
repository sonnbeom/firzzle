package com.firzzle.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.gateway.exception.GatewayExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayExceptionHandlerTest {

    private GatewayExceptionHandler exceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private DataBufferFactory dataBufferFactory;

    @Mock
    private DataBuffer dataBuffer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        exceptionHandler = new GatewayExceptionHandler();

        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(dataBufferFactory.wrap(any(byte[].class))).thenReturn(dataBuffer);
        when(response.writeWith(any())).thenReturn(Mono.empty());
    }

    @Test
    void handleNotFoundExceptionTest() {
        // 준비
        NotFoundException notFoundException = new NotFoundException("Route not found");

        // 실행
        Mono<Void> result = exceptionHandler.handle(exchange, notFoundException);

        // 검증
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
        verify(response).writeWith(any(Mono.class));
    }

    @Test
    void handleTimeoutExceptionTest() {
        // 준비
        TimeoutException timeoutException = new TimeoutException("Request timed out");

        // 실행
        Mono<Void> result = exceptionHandler.handle(exchange, timeoutException);

        // 검증
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
        verify(response).writeWith(any(Mono.class));
    }

    @Test
    void handleResponseStatusExceptionTest() {
        // 준비
        ResponseStatusException responseStatusException =
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");

        // 실행
        Mono<Void> result = exceptionHandler.handle(exchange, responseStatusException);

        // 검증
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.BAD_REQUEST);
        verify(response).writeWith(any(Mono.class));
    }

    @Test
    void handleGenericExceptionTest() {
        // 준비
        RuntimeException runtimeException = new RuntimeException("Unexpected error");

        // 실행
        Mono<Void> result = exceptionHandler.handle(exchange, runtimeException);

        // 검증
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(response).writeWith(any(Mono.class));
    }
}