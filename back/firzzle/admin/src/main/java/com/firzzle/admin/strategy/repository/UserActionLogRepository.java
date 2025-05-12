package com.firzzle.admin.strategy.repository;


import com.firzzle.admin.strategy.domain.UserActionLog;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActionLogRepository extends ElasticsearchRepository<UserActionLog, String> {

    @Query("""
{
  "bool": {
    "must": [
      { "term": { "event.keyword": "?0" } },
      { "term": { "detail.keyword": "?1" } },
      {
        "range": {
          "@timestamp": {
            "gte": "?2",
            "lt": "?3"
          }
        }
      }
    ]
  }
}
""")
    List<UserActionLog> findLogs(String event, String detail, String from, String to);

    List<UserActionLog> findByDetail(String detail);

}