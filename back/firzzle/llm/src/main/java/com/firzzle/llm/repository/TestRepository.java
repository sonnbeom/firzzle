package com.firzzle.llm.repository;

import com.firzzle.llm.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestRepository extends JpaRepository<TestEntity, Integer> {
    List<TestEntity> findByIdIn(List<Integer> ids);
}
