package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class ContentBlock {
    private String title;
    private String time;
    private String summary_Easy;
    private String summary_High;
    private OxQuiz oxQuiz;
    private DescriptiveQuiz descriptiveQuiz;
}

