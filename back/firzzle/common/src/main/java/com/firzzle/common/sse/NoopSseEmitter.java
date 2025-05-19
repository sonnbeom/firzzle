package com.firzzle.common.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Class Name : NoopSseEmitter
 * @Description : SSE 기능을 사용하지 않는 경우의 기본 구현체
 *
 * @author Firzzle
 * @since 2025. 5. 16.
 */
public class NoopSseEmitter implements SseEmitter {
    private static final Logger logger = LoggerFactory.getLogger(NoopSseEmitter.class);

    @Override
    public void send(String eventName, Object data) {
        logger.debug("NoopSseEmitter - 이벤트 무시: {}", eventName);
    }

    @Override
    public void complete() {
        logger.debug("NoopSseEmitter - 완료 이벤트 무시");
    }

    @Override
    public void error(Throwable throwable) {
        logger.debug("NoopSseEmitter - 오류 이벤트 무시: {}", throwable.getMessage());
    }

    @Override
    public String getClientId() {
        return "noop-client";
    }
}