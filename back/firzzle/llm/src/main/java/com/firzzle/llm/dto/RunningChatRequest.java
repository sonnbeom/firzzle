package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class RunningChatRequest {
	String Question;
	Long contentSeq;
//	String PreviousQuestion;
}
