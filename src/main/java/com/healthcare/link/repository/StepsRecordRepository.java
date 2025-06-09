package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.StepsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StepsRecordRepository extends JpaRepository<StepsRecord, Long> {

    @Query("SELECT s FROM StepsRecord s JOIN s.source src WHERE src.id.userId = :userId AND src.id.recordkey = :recordkey")
    List<StepsRecord> findByUserIdAndRecordkey(@Param("userId") Long userId, @Param("recordkey") String recordkey);
}
