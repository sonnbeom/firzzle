package com.firzzle.llm.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 이미터 저장소
 */
@Component
public class SseEmitterRepository {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterRepository.class);

    // 활성 SSE 연결을 저장하는 맵 (taskId -> SseEmitter)
    private static final Map<String, SseEmitter> EMITTERS = new ConcurrentHashMap<>();

    @Value("${app.sse.timeout:180000}")
    private long timeout;

    @Value("${app.sse.heartbeat-interval:30000}")
    private long heartbeatInterval;

    /**
     * 새 SSE 이미터 생성 및 등록
     */
    public SseEmitter create(String taskId) {
        // 이전 연결이 있으면 제거
        remove(taskId);

        // 새 이미터 생성
        SseEmitter emitter = new SseEmitter(timeout);

        // 자동 제거를 위한 콜백 등록
        emitter.onCompletion(() -> {
            logger.debug("SSE 연결 완료: taskId={}", taskId);
            remove(taskId);
        });

        emitter.onTimeout(() -> {
            logger.debug("SSE 연결 타임아웃: taskId={}", taskId);
            remove(taskId);
        });

        emitter.onError(e -> {
            logger.error("SSE 연결 오류: taskId={}, error={}", taskId, e.getMessage());
            remove(taskId);
        });

        // 맵에 등록
        EMITTERS.put(taskId, emitter);
        logger.info("SSE 이미터 등록: taskId={}, 현재 활성 연결 수: {}", taskId, EMITTERS.size());

        // 하트비트 설정
        scheduleHeartbeat(taskId, emitter);

        // 연결 유지를 위한 초기 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data(Map.of(
                            "message", "요약 작업 스트림에 연결되었습니다.",
                            "taskId", taskId,
                            "timestamp", System.currentTimeMillis()
                    )));

            logger.debug("SSE 초기 이벤트 전송 성공: taskId={}", taskId);
        } catch (IOException e) {
            logger.error("초기 이벤트 전송 실패: taskId={}, error={}", taskId, e.getMessage());
            remove(taskId);
        }

        return emitter;
    }

    /**
     * 하트비트 스케줄링
     */
    private void scheduleHeartbeat(String taskId, SseEmitter emitter) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                if (EMITTERS.containsKey(taskId)) {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(""));
                    logger.trace("하트비트 전송: taskId={}", taskId);
                } else {
                    executor.shutdown();
                }
            } catch (IOException e) {
                logger.error("하트비트 전송 실패: taskId={}, error={}", taskId, e.getMessage());
                EMITTERS.remove(taskId);
                executor.shutdown();
            }
        }, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 특정 taskId의 이미터 제거
     */
    public void remove(String taskId) {
        SseEmitter emitter = EMITTERS.remove(taskId);
        if (emitter != null) {
            emitter.complete();
            logger.debug("SSE 이미터 제거됨: taskId={}, 남은 연결 수: {}", taskId, EMITTERS.size());
        }
    }

    /**
     * 특정 taskId에 이벤트 전송
     */
    public void sendToClient(String taskId, String eventName, Object data) {
        SseEmitter emitter = EMITTERS.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                logger.debug("이벤트 전송 성공: taskId={}, event={}", taskId, eventName);
            } catch (IOException e) {
                logger.error("이벤트 전송 실패: taskId={}, event={}, error={}",
                        taskId, eventName, e.getMessage());
                remove(taskId);
            }
        } else {
            logger.warn("이벤트 전송 대상 없음: taskId={}, event={}", taskId, eventName);
        }
    }

    /**
     * 특정 taskId의 연결 완료
     */
    public void complete(String taskId) {
        sendToClient(taskId, "complete", Map.of(
                "message", "요약 작업이 완료되었습니다.",
                "timestamp", System.currentTimeMillis()
        ));
        remove(taskId);
    }

    /**
     * 특정 taskId에 오류 이벤트 전송 후 연결 종료
     */
    public void completeWithError(String taskId, String errorMessage) {
        SseEmitter emitter = EMITTERS.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of(
                                "message", errorMessage,
                                "timestamp", System.currentTimeMillis()
                        )));

                emitter.completeWithError(new RuntimeException(errorMessage));
                logger.error("오류와 함께 SSE 연결 종료: taskId={}, error={}", taskId, errorMessage);
            } catch (IOException e) {
                logger.error("오류 이벤트 전송 실패: taskId={}, error={}", taskId, e.getMessage());
            } finally {
                remove(taskId);
            }
        }
    }

    /**
     * 특정 taskId의 연결 존재 여부 확인
     */
    public boolean exists(String taskId) {
        return EMITTERS.containsKey(taskId);
    }

    /**
     * 활성 연결 수 조회
     */
    public int getActiveCount() {
        return EMITTERS.size();
    }
}