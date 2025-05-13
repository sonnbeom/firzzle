package com.firzzle.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
        // 스프링 컨텍스트가 정상적으로 로드되는지 테스트
    }

    @Test
    void testNotFoundErrorHandling() {
        // 존재하지 않는 경로 요청 시 404 오류 처리 테스트
        webTestClient.get()
                .uri("/non-existent-path")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FAIL")
                .jsonPath("$.message").exists();
    }

    @Test
    void testRouteToNonExistentService() {
        // 존재하지 않는 서비스로 라우팅 시 오류 처리 테스트
        // 게이트웨이에 non-existent-service에 대한 라우트가 설정되어 있어야 함
        webTestClient.get()
                .uri("/api/non-existent/test")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FAIL")
                .jsonPath("$.message").exists();
    }

    @Test
    void testTimeoutHandling() {
        // 타임아웃 오류 처리 테스트
        // 게이트웨이에 짧은 타임아웃이 설정된 라우트가 있어야 함
        webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(10)) // 테스트 클라이언트의 타임아웃은 길게 설정
                .build()
                .get()
                .uri("/api/timeout-test")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                .expectBody()
                .jsonPath("$.status").isEqualTo("FAIL")
                .jsonPath("$.message").value(message -> {
                    assertTrue(message.toString().contains("시간이 초과되었습니다"));
                });
    }

    @Test
    void testBadRequestHandling() {
        // 잘못된 요청 형식의 오류 처리 테스트
        webTestClient.post()
                .uri("/api/some-service/validate")
                .bodyValue("invalid-json{")  // 잘못된 JSON 형식
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FAIL");
    }

    // 테스트 컨트롤러가 구현되어 있는 경우 사용
    @Test
    void testServerErrorHandling() {
        webTestClient.get()
                .uri("/test/errors/server-error")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.status").isEqualTo("FAIL")
                .jsonPath("$.message").exists();
    }
}