package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.vo.MonthlySummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, MonthlySummaryId> {

    List<MonthlySummary> findByIdUserIdAndIdRecordkey(Long userId, String recordkey);
}
