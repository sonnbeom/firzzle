package com.firzzle.learning.ai.dao;

import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @Class Name : AiLogDao.java
 * @Description : GPT LOG 데이터 접근 객체
 * @author Firzzle
 * @since
 */
@Repository
public class AiLogDao extends MyBatisSupport {

	/**
	 * 성공 로그 삽입
	 * @param box
	 * @return
	 * @throws Exception
	 */
	public int insertApiLog(RequestBox box) {
		return sqlSession.insert("AiMapper.insertApiLog", box);
	}

	/**
	 * 실패 로그 삽입
	 * @param box
	 * @return
	 * @throws Exception
	 */
	public int insertFailApiLog(Map<String,Object> box) {
		return sqlSession.insert("AiMapper.insertFailApiLog", box);
	}
}
