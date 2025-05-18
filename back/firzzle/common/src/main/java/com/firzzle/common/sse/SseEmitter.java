package com.firzzle.common.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Class Name : SseEmitter
 * @Description : SSE(Server-Sent Events) 공통 인터페이스
 *
 * @author Firzzle
 * @since 2025. 5. 16.
 */
public interface SseEmitter {
    /**
     * 이벤트 전송
     * @param eventName 이벤트 이름
     * @param data 전송할 데이터
     */
    void send(String eventName, Object data);

    /**
     * 완료 이벤트 전송
     */
    void complete();

    /**
     * 오류 이벤트 전송
     * @param throwable 에러 정보
     */
    void error(Throwable throwable);

    /**
     * 클라이언트 ID 반환
     * @return 클라이언트 고유 ID
     */
    String getClientId();
}
