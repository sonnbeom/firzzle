package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.ChatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {

    // 대화 등록
    int insertChat(ChatDTO chat);

    // 무한 스크롤: 마지막 chatSeq 기준 이전 대화 조회 (최신 순)
    List<ChatDTO> selectChatsWithPagination(
        @Param("contentSeq") Long contentSeq,
        @Param("userSeq") Long userSeq,
        @Param("lastChatSeq") Long lastChatSeq,
        @Param("limit") int limit
    );
}