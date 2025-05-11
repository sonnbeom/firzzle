package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class OxQuizBlock {
    private String problem;
    private String answer;
    private int timestamp;
    private String explanation;
}
