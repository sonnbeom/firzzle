package com.firzzle.llm.dto;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamHistoryWrapperDTO {
    private InfoDTO info; // 진행 중 문제 번호 (없으면 null)
    private List<ExamHistoryResponseDTO> historyList;
}