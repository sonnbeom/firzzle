package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

import com.firzzle.llm.domain.ModelType;
import com.firzzle.llm.dto.ChatCompletionRequestDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PromptFactory {
	 private final UserPromptManager userPromptManager;
	 private final SystemPromptManager systemPromptManager;

	 
	 public ChatCompletionRequestDTO createLearningChatRequest(String question, String context, String previous) {
        return ChatCompletionRequestDTO.builder()
                .systemMessage(systemPromptManager.getLearningChatSystemPrompt())
                .userPrompt(userPromptManager.getlearningChatUserPrompt(question, context, previous))
                .modelType(ModelType.LEARNINGCHAT)
                .temperature(0.7)
                .topP(1.0)
                .maxTokens(8192)
                .build();
     }

     public ChatCompletionRequestDTO createSummaryRequest(String transcript) {
         return ChatCompletionRequestDTO.builder()
                .systemMessage(systemPromptManager.getSummarySystemPrompt())
                .userPrompt(transcript)
                .modelType(ModelType.SUMMARY)
                .temperature(0.3)
                .topP(1.0)
                .maxTokens(8192)
                .build();
	 }
     
     public ChatCompletionRequestDTO createTimelineyRequest(String transcript) {
         return ChatCompletionRequestDTO.builder()
                .systemMessage(systemPromptManager.getTimelineSystemPrompt())
                .userPrompt(transcript)
                .modelType(ModelType.TIMELINE)
                .temperature(0.3)
                .topP(1.0)
                .maxTokens(8192)
                .build();
	 }

     public ChatCompletionRequestDTO createExamAnswerRequest(String userAnswer, String modelAnswer, String referenceText) {
         return ChatCompletionRequestDTO.builder()
                 .systemMessage(systemPromptManager.getExamSystemPrompt())
                 .userPrompt(userPromptManager.getExamUserPrompt(userAnswer, modelAnswer, referenceText))
                 .modelType(ModelType.LEARNINGCHAT)
                 .temperature(0.3)
                 .topP(1.0)
                 .maxTokens(1024)
                 .build();
     }
}
