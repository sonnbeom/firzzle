package com.firzzle.admin.learning.service;

import com.firzzle.admin.common.domain.UserActionLog;
import com.firzzle.admin.common.dto.response.ResponseUserLogTransitionDto;
import com.firzzle.admin.common.service.LoggingCommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingLearningService {

    private final LoggingCommonService loggingCommonService;

    public List<ResponseUserLogTransitionDto> getSummaryPreferenceRate(LocalDate startDate, LocalDate endDate) {
        List<UserActionLog> logs = loggingCommonService.getLogsFromES(startDate, endDate, "SUMMARY_PREFERENCE");

        // 날짜별 → 요약 선호도별 카운팅
        Map<String, Map<String, Integer>> dailyCounts = new TreeMap<>();

        for (UserActionLog log : logs) {
            String dateKey = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();
            String preference = log.getSummaryPreference();

            // 날짜별 Map
            Map<String, Integer> preferenceMap = dailyCounts.computeIfAbsent(dateKey, d -> new HashMap<>());
            preferenceMap.put(preference, preferenceMap.getOrDefault(preference, 0) + 1);
        }

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();

        // 날짜 순회
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateKey = current.toString();
            Map<String, Integer> preferences = dailyCounts.getOrDefault(dateKey, new HashMap<>());

            result.add(ResponseUserLogTransitionDto.builder()
                    .date(dateKey)
                    .transitions(preferences)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    public List<ResponseUserLogTransitionDto> getSnapReviewWriteRate(LocalDate startDate, LocalDate endDate) {
        List<UserActionLog> snapLogs = loggingCommonService.getLogsFromES(startDate, endDate, "SNAP_REVIEW_INPUT");
        List<UserActionLog> learnLogs = loggingCommonService.getLogsFromES(startDate, endDate, "START_LEARNING");

        Map<String, Map<String, Integer>> dailyCounts = new TreeMap<>();

        // 공통 메소드 사용
        countLogByDetail(snapLogs, "SNAP_REVIEW_INPUT", dailyCounts);
        countLogByDetail(learnLogs, "START_LEARNING", dailyCounts);

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            String dateKey = current.toString();
            Map<String, Integer> counts = dailyCounts.getOrDefault(dateKey, new HashMap<>());

            result.add(ResponseUserLogTransitionDto.builder()
                    .date(dateKey)
                    .transitions(counts)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }
    private void countLogByDetail(List<UserActionLog> logs, String detailKey, Map<String, Map<String, Integer>> dailyCounts) {
        for (UserActionLog log : logs) {
            String dateKey = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();

            Map<String, Integer> countMap = dailyCounts.get(dateKey);
            if (countMap == null) {
                countMap = new HashMap<>();
                dailyCounts.put(dateKey, countMap);
            }

            Integer currentCount = countMap.get(detailKey);
            if (currentCount == null) {
                currentCount = 0;
            }

            countMap.put(detailKey, currentCount + 1);
        }
    }

}
