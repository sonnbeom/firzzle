package com.firzzle.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmRequest {
	private Long userContentSeq;
    private Long contentSeq;
    private String script;
    private String taskId;
    private boolean isError;
    private Exception e;
}