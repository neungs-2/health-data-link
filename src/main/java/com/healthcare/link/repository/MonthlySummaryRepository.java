package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.entity.StepsRecord;
import com.healthcare.link.domain.vo.MonthlySummaryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, MonthlySummaryId> {
}