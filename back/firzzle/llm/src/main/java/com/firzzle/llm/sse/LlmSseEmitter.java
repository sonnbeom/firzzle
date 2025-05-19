package com.firzzle.llm.sse;

import com.firzzle.common.sse.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * LLM 모듈의 SSE 이미터 구현체
 */
public class LlmSseEmitter implements SseEmitter {
    private static final Logger logger = LoggerFactory.getLogger(LlmSseEmitter.class);

    @Autowired
    private SseEmitterRepository emitterRepository;

    private final String clientId;

    public LlmSseEmitter() {
        this.clientId = UUID.randomUUID().toString();
        logger.debug("LlmSseEmitter 생성: clientId={}", clientId);
    }

    @Override
    public void send(String eventName, Object data) {
        logger.debug("이벤트 전송 요청: clientId={}, event={}", clientId, eventName);
        emitterRepository.sendToClient(clientId, eventName, data);
    }

    @Override
    public void complete() {
        logger.debug("연결 완료 요청: clientId={}", clientId);
        emitterRepository.complete(clientId);
    }

    @Override
    public void error(Throwable throwable) {
        logger.debug("오류 발생: clientId={}, error={}", clientId, throwable.getMessage());
        emitterRepository.completeWithError(clientId, throwable.getMessage());
    }

    @Override
    public String getClientId() {
        return clientId;
    }
}