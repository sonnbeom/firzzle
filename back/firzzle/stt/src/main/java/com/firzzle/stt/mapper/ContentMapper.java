package com.firzzle.stt.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import com.firzzle.stt.dto.ContentDTO;
import java.util.Optional;

@Mapper
public interface ContentMapper {
    // 콘텐츠 삽입
    int insertContent(ContentDTO contentDTO);
    // 콘텐츠 조회 
    Long existsByVideoId(String videoId);
}
