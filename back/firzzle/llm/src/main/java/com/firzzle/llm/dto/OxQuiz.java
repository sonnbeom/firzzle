package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class OxQuiz {
    private String problem;
    private String answer;
    private int timestamp;
    private String explanation;
}
