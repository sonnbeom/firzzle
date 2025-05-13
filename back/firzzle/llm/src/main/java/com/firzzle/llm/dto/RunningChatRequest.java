package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class RunningChatRequest {
	Long contentSeq;
	String Question;
	String PreviousQuestion; // 2개 정도? 
}
