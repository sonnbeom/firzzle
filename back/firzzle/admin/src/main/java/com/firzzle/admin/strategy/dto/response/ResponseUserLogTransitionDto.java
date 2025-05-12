package com.firzzle.admin.strategy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class ResponseUserLogTransitionDto {
    private String date;
    private Map<String, Integer> transitions;
}
