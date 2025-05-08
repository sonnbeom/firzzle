package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component; 

@Component
public class GptTestPrompt {

    public String createPrompt(String question) {
        return question;
    }

    public String createInstruction(String instruction) {
        return """
        	아무말이나 해봐.
        """;
    }
}
