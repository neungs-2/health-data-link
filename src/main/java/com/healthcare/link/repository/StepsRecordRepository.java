package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.StepsRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepsRecordRepository extends JpaRepository<StepsRecord, Long> {
}
