package com.firzzle.admin.strategy.service;

import com.firzzle.admin.common.domain.UserActionLog;
import com.firzzle.admin.common.dto.response.ResponseUserLogTransitionDto;
import com.firzzle.admin.common.service.LoggingCommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLogService {

    private final LoggingCommonService loggingCommonService;

    public List<ResponseUserLogTransitionDto> getGroupedTransitions(LocalDate startDate, LocalDate endDate) {
        List<UserActionLog> logs = loggingCommonService.getLogsFromES(startDate, endDate, "PREFERENCE");

        Map<String, Map<String, Integer>> dailyCounts = new TreeMap<>();

        for (UserActionLog log : logs) {
            String date = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();
            String path = log.getFromContent() + "=>" + log.getToContent();

            if ("SUMMARY=>RECOMMEND_CONTENT".equals(path) || "SUMMARY=>RECOMMEND_EXPERT".equals(path)) {
                path = "SUMMARY=>RECOMMEND";
            }

            Map<String, Integer> pathMap = dailyCounts.computeIfAbsent(date, d -> new HashMap<>());
            pathMap.put(path, pathMap.getOrDefault(path, 0) + 1);
        }

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();

        // 시작일부터 종료일까지 반복
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String key = current.toString();
            Map<String, Integer> transitions = dailyCounts.getOrDefault(key, new HashMap<>());
            result.add(ResponseUserLogTransitionDto.builder()
                    .date(key)
                    .transitions(transitions)
                    .build());
            current = current.plusDays(1);
        }

        return result;
    }

    public List<ResponseUserLogTransitionDto> getLearningRate(LocalDate startDate, LocalDate endDate) {
        // CONTENT_CREATED, START_LEARNING 로그를 주어진 기간 동안 조회
        List<UserActionLog> createdLogs = loggingCommonService.getLogsFromES(startDate, endDate, "CONTENT_CREATED");
        List<UserActionLog> startedLogs = loggingCommonService.getLogsFromES(startDate, endDate, "START_LEARNING");

        Map<String, Integer> createdPerDay = groupByDay(createdLogs);
        Map<String, Integer> startedPerDay = groupByDay(startedLogs);

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();

        // 날짜 순회하며 응답 생성
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String key = current.toString();

            Map<String, Integer> transitions = new HashMap<>();
            transitions.put("CONTENT_CREATED", createdPerDay.getOrDefault(key, 0));
            transitions.put("START_LEARNING", startedPerDay.getOrDefault(key, 0));

            result.add(ResponseUserLogTransitionDto.builder()
                    .date(key)
                    .transitions(transitions)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    private Map<String, Integer> groupByDay(List<UserActionLog> logs) {
        Map<String, Integer> dailyCounts = new TreeMap<>();

        for (UserActionLog log : logs) {
            String dateKey = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();
            // 1. 해당 날짜가 이미 Map에 있으면 값 +1, 없으면 1로 시작
            if (dailyCounts.containsKey(dateKey)) {
                int current = dailyCounts.get(dateKey);
                dailyCounts.put(dateKey, current + 1);
            } else {
                // 2. 값이 없으면 1로 초기화
                dailyCounts.put(dateKey, 1);
            }
        }
        return dailyCounts;
    }

    public List<ResponseUserLogTransitionDto> getLoginRate(LocalDate startDate, LocalDate endDate) {
        // LOGIN, VISIT 로그 각각 조회
        List<UserActionLog> loginLogs = loggingCommonService.getLogsFromES(startDate, endDate, "LOGIN");
        List<UserActionLog> visitLogs = loggingCommonService.getLogsFromES(startDate, endDate, "VISIT");

        Map<String, Integer> loginPerDay = groupByDay(loginLogs);
        Map<String, Integer> visitPerDay = groupByDay(visitLogs);

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();

        // 날짜 순회하며 응답 생성
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String key = current.toString();

            Map<String, Integer> transitions = new HashMap<>();
            transitions.put("LOGIN", loginPerDay.getOrDefault(key, 0));
            transitions.put("ViSIT", visitPerDay.getOrDefault(key, 0));

            result.add(ResponseUserLogTransitionDto.builder()
                    .date(key)
                    .transitions(transitions)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }
}

