package com.firzzle.llm.domain;

import lombok.Data;

@Data
public class OxQuizBlock {
    private String problem;
    private String answer;
    private int timestamp;
    private String explanation;
}
