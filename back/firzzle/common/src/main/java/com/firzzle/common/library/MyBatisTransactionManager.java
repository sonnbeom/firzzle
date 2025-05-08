package com.firzzle.common.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Class Name : MyBatisTransactionManager
 * @Description : 마이바티스 트랜젝션 매니저
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 30.
 */
@Service
@Scope("prototype")
public class MyBatisTransactionManager extends DefaultTransactionDefinition {

    /** 서블릿 UID */
    private static final long serialVersionUID = 7407829348017696779L;

    /** 로거 */
    protected static final Logger logger = LoggerFactory.getLogger(MyBatisTransactionManager.class);

    /** 트랜젝션 매니저 */
    @Autowired
    @Qualifier("transactionManager")
    PlatformTransactionManager transactionMgr;

    /** 트랜젝션 상태 */
    TransactionStatus status;

    /**
     * 트랜젝션 시작
     * @throws TransactionException
     */
    public void start() throws TransactionException {
        start(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    /**
     * 트랜젝션 시작
     * @int propagation : transaction 전파레벨 설정(Nullable)
     * @throws TransactionException
     */
    public void start(int propagation) throws TransactionException {
        //transaction 전파 설정 (default PROPAGATION_REQUIRED)
        propagation = Optional.ofNullable(propagation)
                .filter(value-> Arrays.asList(Propagation.values()).contains(value))
                .orElse(TransactionDefinition.PROPAGATION_REQUIRED);
        this.setPropagationBehavior(propagation);
        status = transactionMgr.getTransaction(this);
    }

    /**
     * 커밋처리
     *
     * @throws TransactionException
     */
    public void commit() throws TransactionException {
        if (!status.isCompleted()) {
            transactionMgr.commit(status);
        }
    }

    /**
     * 롤백처리
     *
     * @throws TransactionException
     */
    public void rollback() throws TransactionException {
        if (!status.isCompleted()) {
            transactionMgr.rollback(status);
        }
    }

    /**
     * 트랜젝션 종료
     *
     * @throws TransactionException
     */
    public void end() throws TransactionException {
        rollback();
        //logger.debug("MyBatisTransaction END");
    }
}
