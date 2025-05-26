package com.firzzle.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Class Name : DateUtil.java
 * @Description : 날짜 관련 유틸리티 클래스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
public class DateUtil {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 현재 날짜와 시간을 YYYYMMDDHHMMSS 형식의 문자열로 반환합니다.
     *
     * @return 현재 날짜와 시간 (YYYYMMDDHHMMSS)
     */
    public static String getCurrentDatetime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * LocalDateTime을 YYYYMMDDHHMMSS 형식의 문자열로 변환합니다.
     *
     * @param dateTime 변환할 LocalDateTime
     * @return 변환된 문자열 (YYYYMMDDHHMMSS)
     */
    public static String formatDatetime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * YYYYMMDDHHMMSS 형식의 문자열을 LocalDateTime으로 변환합니다.
     *
     * @param datetimeStr 변환할 문자열 (YYYYMMDDHHMMSS)
     * @return 변환된 LocalDateTime
     */
    public static LocalDateTime parseDatetime(String datetimeStr) {
        return LocalDateTime.parse(datetimeStr, DATETIME_FORMATTER);
    }
}