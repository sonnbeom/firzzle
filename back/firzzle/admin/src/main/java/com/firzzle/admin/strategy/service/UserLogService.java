package com.firzzle.admin.strategy.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.firzzle.admin.strategy.domain.UserActionLog;
import com.firzzle.admin.strategy.dto.response.ResponseUserLogTransitionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;


import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLogService {

    private final ElasticsearchClient elasticsearchClient;

    public List<ResponseUserLogTransitionDto> getGroupedTransitions(LocalDate requestDate) {
        // 최근 10일치의 USER_ACTION 로그를 Elasticsearch에서 조회 (디테일: PREFERENCE)
        List<UserActionLog> logs = getLogsFromES(requestDate, "PREFERENCE", 10);

        // 날짜별 + 행동 경로별로 개수를 세기 위한 map
        Map<String, Map<String, Integer>> dailyCounts = new TreeMap<>();

        // 각 로그를 날짜별/경로별로 분류 및 개수 카운팅
        for (UserActionLog log : logs) {
            String date = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();
            String path = log.getFromContent() + "=>" + log.getToContent();

            // 행동 경로가 특정 2가지 중 하나면 단일 RECOMMEND로 통합
            if ("SUMMARY=>RECOMMEND_CONTENT".equals(path) || "SUMMARY=>RECOMMEND_EXPERT".equals(path)) {
                path = "SUMMARY=>RECOMMEND";
            }

            // 날짜별, 경로별로 카운트 누적
//            dailyCounts.computeIfAbsent(date, d -> new HashMap<>())
//                    .merge(path, 1, Integer::sum);
            // 1. 날짜에 해당하는 Map이 있는지 확인
            Map<String, Integer> pathMap = dailyCounts.get(date);

            // 2. 없으면 새 HashMap을 만들어서 넣는다
            if (pathMap == null) {
                pathMap = new HashMap<>();
                dailyCounts.put(date, pathMap);
            }

            // 3. 해당 경로(path)의 값이 있는지 확인하고 없으면 0으로 처리
            int currentCount = pathMap.getOrDefault(path, 0);

            // 4. 1을 더해서 다시 저장
            pathMap.put(path, currentCount + 1);
        }

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();

        // 최근 10일 날짜 기준으로 출력
        for (int i = 9; i >= 0; i--) {
            LocalDate date = requestDate.minusDays(i);
            String key = date.toString();

            Map<String, Integer> transitions = dailyCounts.getOrDefault(key, new HashMap<>());

            result.add(ResponseUserLogTransitionDto.builder()
                    .date(key)
                    .transitions(transitions)
                    .build());
        }

        return result;
    }


    private List<UserActionLog> getLogsFromES(LocalDate requestDate, String detailType) {
        String from = requestDate.minusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        String to = requestDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString();

        try {
            Query query = Query.of(q -> q
                    .bool(b -> b
                            .must(m -> m.term(t -> t.field("event.keyword").value("USER_ACTION")))
                            .must(m -> m.term(t -> t.field("detail.keyword").value(detailType)))
                            .must(m -> m.range(r -> r
                                    .field("@timestamp")
                                    .gte(JsonData.of(from))
                                    .lt(JsonData.of(to))
                            ))
                    )
            );

            SearchRequest req = SearchRequest.of(s -> s
                    .index("spring-logs-*")
                    .query(query)
                    .size(1000)
            );

            SearchResponse<UserActionLog> response =
                    elasticsearchClient.search(req, UserActionLog.class);

            log.info("응답값: {}", response);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.error("Elasticsearch 쿼리 실패", e);
            return List.of();
        }
    }
    private List<UserActionLog> getLogsFromES(LocalDate requestDate, String detailType, int days) {
        // ✅ 조회 기간 설정: from ~ to
        // 예) requestDate = 2025-05-13, days = 10 이라면
        // from = 2025-05-04T00:00:00Z (UTC 기준)
        // to   = 2025-05-14T00:00:00Z (UTC 기준 → 다음 날 자정 전까지 포함시키기 위함)
        String from = requestDate.minusDays(days - 1).atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        String to = requestDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString();

        try {
            Query query = Query.of(q -> q
                    .bool(b -> b
                            // event가 "USER_ACTION"인 문서만 조회
                            .must(m -> m.term(t -> t.field("event.keyword").value("USER_ACTION")))
                            // detail이 "PREFERENCE" 또는 전달된 detailType과 일치해야 함
                            .must(m -> m.term(t -> t.field("detail.keyword").value(detailType)))
                            // @timestamp가 from ~ to 사이여야 함
                            .must(m -> m.range(r -> r
                                    .field("@timestamp")
                                    // 	greater than or equal 	이상 (≥)
                                    .gte(JsonData.of(from))
                                    // less than	미만 (<)
                                    .lt(JsonData.of(to))
                            ))
                    )
            );

            SearchRequest req = SearchRequest.of(s -> s
                    .index("spring-logs-*")  // spring-logs-2025.05.13 같은 인덱스 대상
                    .query(query)
                    .size(1000) // 최대 1000개 문서만 반환
            );

            // ✅ Elasticsearch에 요청 보내고 결과 받기
            SearchResponse<UserActionLog> response = elasticsearchClient.search(req, UserActionLog.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.error("Elasticsearch 쿼리 실패", e);
            return List.of();
        }
    }

    public List<ResponseUserLogTransitionDto> getLearningRate(LocalDate requestDate) {
        // 최근 10일치 CONTENT_CREATED, START_LEARNING 로그 가져오기
        List<UserActionLog> createdLogs = getLogsFromES(requestDate, "CONTENT_CREATED", 10);
        List<UserActionLog> startedLogs = getLogsFromES(requestDate, "START_LEARNING", 10);

        Map<String, Integer> createdPerDay = groupByDay(createdLogs);
        Map<String, Integer> startedPerDay = groupByDay(startedLogs);

        List<ResponseUserLogTransitionDto> result = new ArrayList<>();

        for (int i = 9; i >= 0; i--) {
            LocalDate date = requestDate.minusDays(i);
            String key = date.toString();

            Map<String, Integer> transitions = new HashMap<>();
            transitions.put("CONTENT_CREATED", createdPerDay.getOrDefault(key, 0));
            transitions.put("START_LEARNING", startedPerDay.getOrDefault(key, 0));

            result.add(ResponseUserLogTransitionDto.builder()
                    .date(key)
                    .transitions(transitions)
                    .build());
        }

        return result;
    }

    private Map<String, Integer> groupByDay(List<UserActionLog> logs) {
        Map<String, Integer> dailyCounts = new TreeMap<>();

        for (UserActionLog log : logs) {
            String dateKey = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();
//            dailyCounts.merge(dateKey, 1, Integer::sum);
            // 2. 해당 날짜가 이미 Map에 있으면 값 +1, 없으면 1로 시작
            if (dailyCounts.containsKey(dateKey)) {
                int current = dailyCounts.get(dateKey);
                dailyCounts.put(dateKey, current + 1);
            } else {
                // 3. 값이 없으면 1로 초기화
                dailyCounts.put(dateKey, 1);
            }
        }
        return dailyCounts;
    }

}

