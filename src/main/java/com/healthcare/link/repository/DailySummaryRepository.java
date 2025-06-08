package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.DailySummary;
import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.vo.DailySummaryId;
import com.healthcare.link.domain.vo.MonthlySummaryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySummaryRepository extends JpaRepository<DailySummary, DailySummaryId> {
}