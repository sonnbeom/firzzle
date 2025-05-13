package com.firzzle.admin.strategy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(indexName = "spring-logs-*")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString

@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ 이 한 줄 추가!
public class UserActionLog {

    @Id
    private String id;
    private String event;
    private String detail;
    private String userId;
    private String fromContent;
    private String toContent;
    @JsonProperty("@timestamp")
    private String timestampRaw;

    @JsonIgnore
    public Instant getTimestamp() {
        if (timestampRaw == null || timestampRaw.isBlank()) return null;
        return Instant.parse(timestampRaw);
    }

}