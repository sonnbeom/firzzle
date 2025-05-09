package com.firzzle.llm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TestEntity{

    @Id
    private Integer id;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;
}
