package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.DailySummary;
import com.healthcare.link.domain.vo.DailySummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary, DailySummaryId> {

    List<DailySummary> findByIdRecordkeyAndIdUserId(String recordkey, Long userId);

    @Query("SELECT d FROM DailySummary d WHERE d.id.userId = :userId AND d.id.recordkey = :recordkey AND d.id.date = CURRENT_DATE")
    Optional<DailySummary> findTodaySummary(@Param("userId") Long userId, @Param("recordkey") String recordkey);
}
