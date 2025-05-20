package com.firzzle.llm.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    public static String getCurrentTimestamp14() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }
    
    public static String formatSecondsToHHMMSS(String secondsStr) {
        try {
            int totalSeconds = Integer.parseInt(secondsStr);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (NumberFormatException e) {
            return "00:00:00"; // fallback
        }
    }
}