package com.firzzle.llm.domain;

import lombok.Data;

@Data
public class RunningChatRequest {
	String Question;
	String videoSeq;
	String UserSeq;
}
