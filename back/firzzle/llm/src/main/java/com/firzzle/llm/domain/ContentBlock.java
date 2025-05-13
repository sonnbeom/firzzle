package com.firzzle.llm.domain;

import lombok.Data;

@Data
public class ContentBlock {
    private String title;
    private String time;
    private String summary_Easy;
    private String summary_High;
    private OxQuizBlock oxQuiz;
    private DescriptiveQuizBlock descriptiveQuiz;
}

