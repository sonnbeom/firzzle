package com.firzzle.stt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.firzzle.stt.dto.ContentDTO;

@Mapper
public interface ContentMapper {

    /**
     * 콘텐츠 삽입
     */
    int insertContent(ContentDTO contentDTO);

    /**
     * videoId 기준 콘텐츠 존재 여부 확인 (존재 시 contentSeq 반환)
     */
    Long existsByVideoId(@Param("videoId") String videoId);

    /**
     * contentSeq 기준으로 URL 조회
     */
    String selectUrlByContentSeq(@Param("contentSeq") Long contentSeq);
}
