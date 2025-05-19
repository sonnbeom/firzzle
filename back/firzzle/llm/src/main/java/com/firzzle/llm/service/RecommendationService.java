package com.firzzle.llm.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.llm.client.OpenAiClient;
import com.firzzle.llm.client.QdrantClient;
import com.firzzle.llm.dto.RecommendRequestDTO;
import com.firzzle.llm.dto.RecommendResponseDTO;
import com.firzzle.llm.dto.UserContentDTO;
import com.firzzle.llm.mapper.ChatMapper;
import com.firzzle.llm.mapper.ExamsMapper;
import com.firzzle.llm.mapper.UserContentMapper;
import com.firzzle.llm.mapper.UserMapper;
import com.firzzle.llm.prompt.PromptFactory;
import com.firzzle.llm.util.QdrantCollections;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final UserContentMapper userContentMapper;
    private final UserMapper userMapper;
    

    /**
     * 입력된 contentSeq 기반으로 유사한 콘텐츠 ID 리스트를 추천합니다.
     * - 저장된 content_vectors 컬렉션을 기반으로 검색
     * - payload에는 contentSeq만 저장되어 있음
     */
    public CompletableFuture<List<RecommendResponseDTO>> searchSimilarContents(String uuid, Long userContentSeq, RecommendRequestDTO request){
        // 1. UUID로 사용자 번호 조회
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);

        // 2. 콘텐츠 매핑 정보 조회
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 3. 권한 체크
        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "해당 콘텐츠에 대한 접근 권한이 없습니다.");
        }
        
        
        
        
    	return null;
    }

}
