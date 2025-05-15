package com.firzzle.stt.mapper;

import com.firzzle.stt.dto.UserContentDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserContentMapper {

    /**
     * 사용자 콘텐츠 정보 등록
     * @param dto 사용자 콘텐츠 DTO
     * @return 등록된 row 수 (성공 시 1)
     */
    int insertUserContent(UserContentDTO dto);
}
