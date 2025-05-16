package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.ChatDTO;
import com.firzzle.llm.dto.ChatHistoryResponseDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {

    // 대화 등록
    int insertChat(ChatDTO chat);

    List<ChatDTO> selectChatsByCursor(
    	    @Param("contentSeq") Long contentSeq,
    	    @Param("userSeq") Long userSeq,
    	    @Param("lastIndate") String lastIndate,  // null이면 처음 로딩
    	    @Param("limit") int limit                // 한 번에 가져올 개수
    	);
}