package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

import com.firzzle.llm.domain.ModelType;
import com.firzzle.llm.dto.ChatCompletionRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PromptFactory {
	 private final UserPromptManager userPromptManager;
	 private final SystemPromptManager systemPromptManager;

	 
	 public ChatCompletionRequest createRunningChatRequest(String question, String context) {
        return ChatCompletionRequest.builder()
                .systemMessage(systemPromptManager.getRunningChatSystemPrompt())
                .userPrompt(userPromptManager.getlearningChatUserPrompt(question, context, ""))
                .modelType(ModelType.RUNNINGCHAT)
                .temperature(0.7)
                .topP(1.0)
                .maxTokens(1024)
                .build();
     }

     public ChatCompletionRequest createSummaryRequest(String transcript) {
         return ChatCompletionRequest.builder()
                .systemMessage(systemPromptManager.getSummarySystemPrompt())
                .userPrompt(transcript)
                .modelType(ModelType.SUMMARY)
                .temperature(0.3)
                .topP(1.0)
                .maxTokens(null)
                .build();
	 }
     
     public ChatCompletionRequest createTimelineyRequest(String transcript) {
         return ChatCompletionRequest.builder()
                .systemMessage(systemPromptManager.getTimelineSystemPrompt())
                .userPrompt(transcript)
                .modelType(ModelType.TIMELINE)
                .temperature(0.3)
                .topP(1.0)
                .maxTokens(2048)
                .build();
	 }
}
