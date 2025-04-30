package com.firzzle.common.logging.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.firzzle.common.logging.dto.LogEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class LoggingService {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 추가
    private static final Logger logger = LoggerFactory.getLogger("ELK_LOGGER");

    public static void log(LogEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            logger.info(json); // 여기 logger는 ELK_LOGGER 이름으로 출력
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize LogEvent", e);
        }
    }
}