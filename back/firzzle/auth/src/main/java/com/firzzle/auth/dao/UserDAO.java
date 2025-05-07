package com.firzzle.auth.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @Class Name : UserDAO.java
 * @Description : 사용자 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Repository
public class UserDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private static final String NAMESPACE = "AuthMapper";

    /**
     * 이메일로 사용자 조회
     */
    public DataBox selectUserByEmail(RequestBox box) {
        logger.debug("이메일로 사용자 조회 DAO - 이메일: {}", box.getString("email"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserByEmail", box);
    }

    /**
     * 사용자명으로 사용자 조회
     */
    public DataBox selectUserByUsername(RequestBox box) {
        logger.debug("사용자명으로 사용자 조회 DAO - 사용자명: {}", box.getString("username"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserByUsername", box);
    }

    /**
     * UUID로 사용자 조회
     */
    public DataBox selectUserByUuid(RequestBox box) {
        logger.debug("UUID로 사용자 조회 DAO - UUID: {}", box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserByUuid", box);
    }

    /**
     * 사용자 등록
     */
    public int insertUser(RequestBox box) {
        logger.debug("사용자 등록 DAO - 사용자명: {}, 이메일: {}", box.getString("username"), box.getString("email"));
        return sqlSession.insert(NAMESPACE + ".insertUser", box);
    }

    /**
     * 사용자 정보 업데이트
     */
    public int updateUser(RequestBox box) {
        logger.debug("사용자 정보 업데이트 DAO - UUID: {}", box.getString("uuid"));
        return sqlSession.update(NAMESPACE + ".updateUser", box);
    }
}