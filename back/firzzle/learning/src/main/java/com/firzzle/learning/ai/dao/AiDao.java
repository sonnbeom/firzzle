package com.firzzle.learning.ai.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


/**
 * @Class Name : AiDao.java
 * @Description : GPT 데이터 접근 객체
 * @author Firzzle
 * @since
 */
@Repository
public class AiDao extends MyBatisSupport {

	private static final Logger logger = LoggerFactory.getLogger(AiDao.class);


	/**
	 * 프롬프트 조회
	 * @param box
	 * @return
	 * @throws Exception
	 */
	public DataBox selectAiPrompt(RequestBox box) {
		return sqlSession.selectDataBox("AiMapper.selectAiPrompt", box);
	}

	public DataBox selectApiKey(RequestBox box) {
		return sqlSession.selectDataBox("AiMapper.selectApiKey", box);
	}
}
