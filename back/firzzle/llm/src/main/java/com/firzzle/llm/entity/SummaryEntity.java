package com.firzzle.llm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SummaryEntity {
    @Id
    private Integer id;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;
}
