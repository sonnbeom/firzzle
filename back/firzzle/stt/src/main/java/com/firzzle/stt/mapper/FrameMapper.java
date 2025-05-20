package com.firzzle.stt.mapper;

import com.firzzle.stt.dto.FrameDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FrameMapper {

    /**
     * 프레임 정보 등록
     * @param frameDTO 프레임 DTO
     * @return 영향받은 행 수
     */
    int insertFrame(FrameDTO frameDTO);
}
