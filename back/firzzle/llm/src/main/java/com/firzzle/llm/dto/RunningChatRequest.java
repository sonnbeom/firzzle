package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class RunningChatRequest {
	String Question;
	String contentSeq;
//	String PreviousQuestion;
}
