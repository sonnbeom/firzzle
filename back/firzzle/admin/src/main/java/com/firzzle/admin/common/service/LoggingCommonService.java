package com.firzzle.admin.common.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.firzzle.admin.common.domain.UserActionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingCommonService {

    private final ElasticsearchClient elasticsearchClient;

    public List<UserActionLog> getLogsFromES(LocalDate startDate, LocalDate endDate, String detailType) {
        String from = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        String to = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString(); // 다음날 자정 미만까지

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
}
