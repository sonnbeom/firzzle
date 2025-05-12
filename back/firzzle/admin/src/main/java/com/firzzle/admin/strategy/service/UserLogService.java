package com.firzzle.admin.strategy.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.firzzle.admin.strategy.domain.UserActionLog;
import com.firzzle.admin.strategy.dto.response.ResponseUserLogTransitionDto;
import com.firzzle.admin.strategy.repository.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
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

    private final UserActionLogRepository logRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;


//    public Map<String, Map<String, Integer>> getGroupedTransitions(LocalDate requestDate) {
//        String end = requestDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toString(); // ★ 하루 뒤
//        String start = requestDate.minusDays(30).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toString();
//
////        List<UserActionLog> logs = logRepository.findLogs("USER_ACTION", "PREFERENCE", start, end);
//        List<UserActionLog> logs = findLogs("USER_ACTION", "PREFERENCE", start, end);
//        log.info("조회된 로그 수: {}", logs.size());
//
//        Map<String, Map<String, Integer>> dailyCounts = new TreeMap<>();
//
//        for (UserActionLog log : logs) {
//            LocalDate date = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate();
//            String dateKey = date.toString(); // yyyy-MM-dd
//            String path = log.getFromContent() + "=>" + log.getToContent();
//
//            if ("SUMMARY=>RECOMMEND_CONTENT".equals(path) || "SUMMARY=>RECOMMEND_EXPERT".equals(path)) {
//                path = "SUMMARY=>RECOMMEND";
//            }
//
//            dailyCounts.computeIfAbsent(dateKey, d -> new HashMap<>())
//                    .merge(path, 1, Integer::sum);
//        }
//
//        // 3일 단위로 묶기
//        List<String> dates = new ArrayList<>(dailyCounts.keySet());
//        dates.sort(Comparator.naturalOrder());
//
//        Map<String, Map<String, Integer>> groupedBy3Days = new LinkedHashMap<>();
//        for (int i = 0; i < dates.size(); i += 3) {
//            String startDate = dates.get(i);
//            String endDateStr = dates.get(Math.min(i + 2, dates.size() - 1));
//            String range = startDate.equals(endDateStr) ? startDate : startDate + "~" + endDateStr;
//
//            Map<String, Integer> temp = new HashMap<>();
//            for (int j = i; j < i + 3 && j < dates.size(); j++) {
//                Map<String, Integer> dayMap = dailyCounts.get(dates.get(j));
//                for (Map.Entry<String, Integer> e : dayMap.entrySet()) {
//                    temp.merge(e.getKey(), e.getValue(), Integer::sum);
//                }
//            }
//
//            groupedBy3Days.put(range, temp);
//        }
//
//        return groupedBy3Days;
//    }
public List<ResponseUserLogTransitionDto> getGroupedTransitions(LocalDate requestDate) {
    List<UserActionLog> logs = getLogsFromES(requestDate);
    Map<String, Map<String, Integer>> dailyCounts = new TreeMap<>();

    for (UserActionLog log : logs) {
        String date = log.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate().toString();
        String path = log.getFromContent() + "=>" + log.getToContent();

        if ("SUMMARY=>RECOMMEND_CONTENT".equals(path) || "SUMMARY=>RECOMMEND_EXPERT".equals(path)) {
            path = "SUMMARY=>RECOMMEND";
        }

        dailyCounts.computeIfAbsent(date, d -> new HashMap<>())
                .merge(path, 1, Integer::sum);
    }

    List<ResponseUserLogTransitionDto> result = new ArrayList<>();
    for (Map.Entry<String, Map<String, Integer>> entry : dailyCounts.entrySet()) {
        result.add(ResponseUserLogTransitionDto.builder()
                .date(entry.getKey())
                .transitions(entry.getValue())
                .build());
    }

    return result;
}

    public List<UserActionLog> getLogsFromES(LocalDate requestDate) {
        String from = requestDate.minusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        String to = requestDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString();

        try {
            Query query = Query.of(q -> q
                    .bool(b -> b
                            .must(m -> m.term(t -> t.field("event.keyword").value("USER_ACTION")))
                            .must(m -> m.term(t -> t.field("detail.keyword").value("PREFERENCE")))
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
}

